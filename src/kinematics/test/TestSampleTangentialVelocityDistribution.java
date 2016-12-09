package kinematics.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import kinematics.SampleTangentialVelocityDistribution;
import kinematics.TangentialVelocityDistribution;
import numeric.data.Histogram;
import numeric.functions.PositiveLinear;
import Jama.Matrix;
import constants.Galactic;
import density.DensityProfile;
import density.Uniform;
import survey.SurveyVolume;
import utils.AstrometryUtils;
import utils.MagnitudeUtils;

/**
 * This class computes the sample tangential velocity distribution using a Monte Carlo
 * method, and compares it to the same function computed numerically using the methods
 * in SampleTangentialVelocityDistribution class.
 * 
 * @author nrowell
 *
 */
public class TestSampleTangentialVelocityDistribution
{
	
	public static void main(String[] args) throws IOException
	{
		// Line of sight coordinates
		double ra  = Math.toRadians(260.0);
		double dec = Math.toRadians(-30.0);
		
		// Survey parameters
		double mu_min = 0.01;          // Proper motion limits [arcsec/yr]
		double mu_max = 1.0;
		double m_min  = 10.0;		  // Apparent magnitude limits [mags]
		double m_max  = 20.0;
		double omega = 0.01;          // Survey footprint size [steradians]. All-sky has omega = 4*pi.
		
		// Object parameters
		double M = 12;				  // Absolute magnitude (used to restrict distance range)
		
		// Distance limits arising from apparent magnitude thresholds [parsecs].
		double d_min_m = MagnitudeUtils.getDistance(m_min, M);
		double d_max_m = MagnitudeUtils.getDistance(m_max, M);
		
		// Upper limit on tangential velocity, from maximum distance and upper proper motion limit [km/s]
		double vt_max = AstrometryUtils.getVtFromMuAndD(mu_max, d_max_m);
		
		System.out.println("Distance limits arising from magnitude selection:");
		System.out.println(" ¬ d_min_m = "+d_min_m);
		System.out.println(" ¬ d_max_m = "+d_max_m);
		
		// Stellar population parameters
		Matrix cov  = Galactic.spheroid_covariance;
		Matrix mean = Galactic.spheroid_mean;
		DensityProfile density = new Uniform();
		
		// Survey volume functions tabulated at this resolution [pc]
		double d_step = 1.0;
		// Tangential velocity distributions tabulated at this resolution [km/s]
		double vt_step = 0.5;
		
		// Number of elements in tabulated functions
		int n_els_d = (int)Math.ceil(d_max_m/d_step);
		// Number of elements in tabulated functions
		int n_els_vt = (int)Math.ceil(vt_max/vt_step);
		
		
		// Arrays to store lookup tables for survey volume as a function of distance.
		double[] vol_diff_true = new double[n_els_d];
		double[] vol_cum_true  = new double[n_els_d+1];
		
		// Array of corresponding distance values for cumulative volume arrays
		double[] d = new double[n_els_d+1];
		for(int i=0; i<(n_els_d+1); i++)
		{
			d[i] = i*d_step;
		}
		
		// True physical survey volume along LOS. This is used to draw random distances for objects.
		System.out.println("Computing true survey volume...");
		SurveyVolume.getGeneralizedSurveyVolumeAlongLos(cov, mean, vt_step, n_els_vt, ra, dec, mu_min, mu_max, 
					     							    omega, density, d_step, vol_diff_true, vol_cum_true, false, false);
		
		// Create objects to interpolate survey volume at arbitrary distances
		PositiveLinear survey_volume_true = new PositiveLinear(d, vol_cum_true);
		
		// Sample and true tangential velocity distributions
		double[] vtan_sample_pdf = new double[n_els_vt];
		double[] vtan_sample_cdf = new double[n_els_vt+1];
		double[] vtan_true_pdf   = new double[n_els_vt];
		double[] vtan_true_cdf   = new double[n_els_vt+1];
		
		// Array of corresponding tangential velocity values
		double[] vtan = new double[vtan_sample_pdf.length];
		for(int i=0; i<vtan.length; i++)
		{
			vtan[i] = i*vt_step + vt_step/2.0;
		}
		
		System.out.println("Computing sample v_{tan} distribution...");
		SampleTangentialVelocityDistribution.getSampleVtanDistributionTowardsLos(cov, mean, ra, dec, mu_min, mu_max, d_min_m, d_max_m, omega, density, vt_step, vtan_sample_pdf, vtan_sample_cdf, null);
		
		System.out.println("Computing true v_{tan} distribution...");
		TangentialVelocityDistribution.getVtanDistributionTowardsLos(cov, mean, ra, dec, vt_step, vtan_true_pdf, vtan_true_cdf);
		
		// Create object to interpolate true tangential velocity PDF and draw velocities from it
		PositiveLinear vtan_true   = new PositiveLinear(vtan, vtan_true_pdf);
		
		System.out.println("Simulating objects...");
		
		// Number of Monte Carlo shots
		int N = 10000000;
		
		// Record histogram of tangential velocity of objects that pass survey proper motion limits.
		Histogram vtan_hist = new Histogram(0, vt_max, vt_step, true);
		
		// Draw survey objects
		for(int i=0; i<N; i++)
		{
			// Get random tangential velocity
			double vt = vtan_true.drawX();
			
			// Get random distance along LOS
			double distance = survey_volume_true.drawX();
			
			// Does this object pass the proper motion limits?
			double mu = AstrometryUtils.getMuFromDAndVt(distance, vt);
			
			if(mu<mu_min || mu>mu_max) continue;
			
			// Check that objects that pass this selection have the same distribution of
			// tangential velocity as predicted by the vtan_sample
			
			vtan_hist.add(vt);
		}
		
		// Output file for sample tangential velocity distribution
		File outf = new File("test_sample_vtan_distribution.txt");
		BufferedWriter out = new BufferedWriter(new FileWriter(outf));
		
		// Print Monte Carlo sample tangential velocity distribution to file
		out.write(vtan_hist.print(true));
		
		out.write("\n\n");
		
		for(int i=0; i<vtan.length; i++)
		{
			out.write(vtan[i] + " " + vtan_sample_pdf[i] + "\n");
		}
		
		out.close();
				
	}
	
}
