package survey.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import kinematics.TangentialVelocityDistribution;
import numeric.data.Histogram;
import numeric.functions.Linear;
import numeric.functions.PositiveLinear;
import survey.SurveyVolume;
import utils.AstrometryUtils;
import utils.MagnitudeUtils;
import constants.Galactic;
import density.DensityProfile;
import density.Uniform;
import Jama.Matrix;

public class TestSurveyVolume
{
	
	// Line of sight coordinates
	static double ra  = Math.toRadians(260.0);
	static double dec = Math.toRadians(-30.0);
	
	// Survey parameters
	static double mu_min = 0.1;          // Proper motion limits [arcsec/yr]
	static double mu_max = 0.5;
	static double omega = 0.01;          // Survey footprint size [steradians]. All-sky has omega = 4*pi.
	
	// Stellar population parameters
	static Matrix cov  = Galactic.spheroid_covariance;
	static Matrix mean = Galactic.spheroid_mean;
	static DensityProfile density = new Uniform();
	
	// True stellar spatial density at the Sun [pc^{-3}], marginalised over tangential velocity.
	static double stellar_density = 1;
	
	// Upper limit on distance. Survey volume will be computed out this far [pc]
	static double d_max = 1000;
	
	// Upper limit on tangential velocity, when tabulating functions [km/s]
	static double vt_max = 1900;
	
	public static void main(String[] args) throws IOException
	{
		
		// Tangential velocity distributions tabulated at this resolution [km/s]
		double vt_step = 1.0;
		// Survey volume functions tabulated at this resolution [pc]
		double d_step = 1.0;
		
		// Number of elements in tabulated functions
		int n_steps_d  = (int)Math.ceil(d_max/d_step);
		int n_steps_vt = (int)Math.ceil(vt_max/vt_step);
		
		// Arrays to store lookup tables for survey volume as a function of distance
		double[] vol_diff_gen_den_kin = new double[n_steps_d];
		double[] vol_cum_gen_den_kin  = new double[n_steps_d+1];
		// True (non-generalized) physical survey volume
		double[] vol_diff_gen_den = new double[n_steps_d];
		double[] vol_cum_gen_den  = new double[n_steps_d+1];
		
		// Array of corresponding distance values. Need to interpolate both differential and cumulative
		// tables, which are tabulated at different values of distance (offset by a half step).
		double[] d_diff = new double[vol_diff_gen_den.length];
		double[] d_cum  = new double[vol_cum_gen_den.length];
		d_cum[0] = 0.0;
		for(int i=0; i<d_diff.length; i++)
		{
			d_diff[i]  = i*d_step + d_step/2.0;
			d_cum[i+1] = (i+1)*d_step;
		}
		
		// Arrays to store lookup tables for true tangential velocity distribution
		double[] vtan_true_pdf = new double[n_steps_vt];
		double[] vtan_true_cdf = new double[n_steps_vt+1];
		
		// Array of corresponding tangential velocity values
		double[] vtan = new double[vtan_true_pdf.length];
		for(int i=0; i<vtan.length; i++)
		{
			vtan[i] = i*vt_step + vt_step/2.0;
		}
		
		// Survey volume
		System.out.print("Computing survey volume generalized over density profile and kinematic selection...");
		SurveyVolume.getGeneralizedSurveyVolumeAlongLos(cov, mean, vt_step, n_steps_vt, ra, dec, mu_min, mu_max, 
					     							    omega, density, d_step, vol_diff_gen_den_kin, vol_cum_gen_den_kin, true, true);
		System.out.println(" done!");
		
		System.out.print("Computing survey volume generalized over density profile...");
		SurveyVolume.getGeneralizedSurveyVolumeAlongLos(cov, mean, vt_step, n_steps_vt, ra, dec, mu_min, mu_max, 
					     							    omega, density, d_step, vol_diff_gen_den, vol_cum_gen_den, true, false);
		System.out.println(" done!");
		
		System.out.print("Computing true v_{tan} distribution...");
		TangentialVelocityDistribution.getVtanDistributionTowardsLos(cov, mean, ra, dec, vt_step, vtan_true_pdf, vtan_true_cdf);
		System.out.println(" done!");
		
		// Create object to interpolate true tangential velocity PDF and draw velocities from it
		PositiveLinear vtan_true   = new PositiveLinear(vtan, vtan_true_pdf);
		
		// Create objects to interpolate generalised survey volume at arbitrary distances
		PositiveLinear survey_volume_diff_gen_den = new PositiveLinear(d_diff, vol_diff_gen_den);
		PositiveLinear survey_volume_cum_gen_den  = new PositiveLinear(d_cum, vol_cum_gen_den);
		PositiveLinear survey_volume_gen_den_kin  = new PositiveLinear(d_cum, vol_cum_gen_den_kin);
		
		// Total generalised survey volume out to maximum distance, considering stellar density profile [pc^{3}]
		double v_max = survey_volume_cum_gen_den.interpolateY(d_max)[0];
		
		// Total generalised survey volume out to maximum distance, considering density profile and kinematics [pc^{3}]
		double v_max_gen = survey_volume_gen_den_kin.interpolateY(d_max)[0];
		
		// Mean number of stars in survey volume considering stellar density profile, of all velocities
		int N = (int)Math.rint(v_max * stellar_density);
		
		// Record histogram of distance to objects that pass survey proper motion limits.
		Histogram d_hist = new Histogram(0, d_max, d_step, true);
		
		System.out.println("Simulating survey objects:");
		System.out.println(" - Survey volume generalised over population density                             = "+v_max+" [pc^3]");
		System.out.println(" - Survey volume generalised over population density and kinematic selection     = "+v_max_gen+" [pc^3]");
		System.out.println(" - Mean number of stars out to maximum distance (# simulation objects)           = "+N);
		
		// Count number of stars that pass proper motion limits
		int N_obs = 0;
		
		// Draw survey objects
		for(int i=0; i<N; i++)
		{
			// Get random tangential velocity
			double vt = vtan_true.drawX();
			
			// Get random distance along LOS
			double distance = survey_volume_diff_gen_den.drawX();
			
			// Does this object pass the proper motion limits?
			double mu = AstrometryUtils.getMuFromDAndVt(distance, vt);
			
			if(mu<mu_min || mu>mu_max) continue;
			
			// Check that objects that pass this selection have the same distribution of
			// distance as predicted by the generalized survey volume
			d_hist.add(distance);
			N_obs++;
		}
		
		System.out.println("Simulation results:");
		System.out.println(" - Number of observed stars (passing proper motion limits) = "+N_obs);
		System.out.println(" - Compare generalised survey volume function with observed distribution\n"
				         + "   of distance for objects that pass proper motion limits, in order to \n"
				         + "   verify that the survey volume computation is correct.");
		
		
		File outf = new File("./src/survey/test/test_generalised_survey_volume.txt");
		BufferedWriter out = new BufferedWriter(new FileWriter(outf));
		
		// Write out generalised survey volume
		for(int i=0; i<n_steps_d; i++)
		{
			out.write(String.format("%f %f\n", d_diff[i], vol_diff_gen_den_kin[i]));
		}
		
		out.write("\n\n");
		
		// Write out distance distribution of objects that passed the survey selection criteria
		out.write(d_hist.print(false));
		
		out.close();
		
	}
	
}
