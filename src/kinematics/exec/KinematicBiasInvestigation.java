package kinematics.exec;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import Jama.Matrix;
import constants.Galactic;
import density.DensityProfile;
import density.Uniform;
import kinematics.TangentialVelocityDistribution;
import numeric.functions.PositiveLinear;
import survey.SurveyVolume;
import utils.AstrometryUtils;
import utils.MagnitudeUtils;

/**
 * This class is used to investigate the bias in the 1/vmax density estimator
 * when generalized over a proper motion selected sample, as discovered by
 * Marco Lam.
 * 
 * The bias arises when the tangential velocity distribution for the population
 * under study varies as a function of the line of sight, with the result that
 * the probability of observing an object of a given tangential velocity varies
 * across the sky. This means that the survey volume along a different line of
 * sight to the one along which an object was originally observed, does not
 * have the same probability of containing the object. This effect is not accounted
 * for in existing generalizations of the 1/vmax method, and leads to on average
 * overestimating the survey volume for a given object, and underestimating the
 * spatial density.
 * 
 * One further clarification: there is a distinction between the true tangential
 * velocity distribution along a given LOS, and the 'sample tangential velocity
 * distribution', which is the distribution of tangential velocity for objects
 * that pass the survey proper motion and magnitude thresholds. This is a function
 * of absolute magnitude, so is different for different magnitudes of stars.
 * 
 * My hypothesis is that when objects have a tangential velocity consistent with
 * the sample tangential velocity distribution (i.e. drawn from it), then on
 * average the 'old' and 'new' methods of computing the survey volume give the
 * same result. This would demonstrate that the 'old' method works fine for
 * populations where the sample tangential velocity distribution doesn't vary (much)
 * across the sky, such as for disk populations.
 * 
 * Algorithm:
 * 
 * Choose survey parameters
 * Choose stellar population velocity distribution & density profile
 * Choose star magnitude
 * 
 * Compute v_{max} for star generalized over density profile and kinematic selection.
 *  - this is the v_{max} computed by the 'new' method, i.e. bias-free
 * 
 * Compute sample tangential velocity distribution for star magnitude, survey parameters and kinematics
 * 
 * Compute many random realizations of observed tangential velocity
 *  - for each, compute v_{max} according to the 'old' method, i.e. find distance range arising from
 *    proper motion limits
 *  - measure expectation value: does it equal 'new' method
 * 
 * 
 * @author nrowell
 *
 */
public class KinematicBiasInvestigation
{
	
	// Parameters:
	// Line of sight coordinates
//	static double ra  = Math.toRadians(260.0);
//	static double dec = Math.toRadians(-30.0);
	static double ra  = Galactic.NGPra;
	static double dec = Galactic.NGPdec;
	
	// Survey parameters
	static double mu_min = 0.1;          // Proper motion limits [arcsec/yr]
	static double mu_max = 1.0;
	static double m_min  = 10.0;		  // Apparent magnitude limits [mags]
	static double m_max  = 20.0;
	static double omega = 0.01;           // Survey footprint size [steradians]. All-sky has omega = 4*pi.
	
	// Object parameters
	static double M = 12;				  // Absolute magnitude (used to restrict distance range)
	
	// Stellar population parameters
	static Matrix cov  = Galactic.spheroid_covariance;
	static Matrix mean = Galactic.spheroid_mean;
	static DensityProfile density = new Uniform();
	
//	static Matrix cov  = Galactic.thin_disk_covariance;
//	static Matrix mean = Galactic.thin_disk_mean;
//	static DensityProfile density = new ExponentialDisk(250);
	
	// True stellar spatial density at the Sun [pc^{-3}], marginalised over tangential velocity.
	static double stellar_density = 10.67;
	
	// Survey volume functions tabulated at this resolution [pc]
	static double d_step = 0.25;
	// Tangential velocity distributions tabulated at this resolution [km/s]
	static double vt_step = 0.125;
	
	
	public static void main(String[] args) throws IOException
	{
		
		// Distance limits arising from apparent magnitude thresholds [parsecs].
		double d_min_m = MagnitudeUtils.getDistance(m_min, M);
		double d_max_m = MagnitudeUtils.getDistance(m_max, M);
		
		// Upper limit on tangential velocity, from maximum distance and upper proper motion limit [km/s]
		double vt_max = AstrometryUtils.getVtFromMuAndD(mu_max, d_max_m);
		
		// Number of elements in tabulated functions
		int n_els_d = (int)Math.ceil(d_max_m/d_step);
		// Number of elements in tabulated functions
		int n_els_vt = (int)Math.ceil(vt_max/vt_step);
		
		System.out.println("Computing various functions required to simulate survey objects:");
		
		// Arrays to store lookup tables for survey volume as a function of distance
		double[] vol_diff_true        = new double[n_els_d];
		double[] vol_cum_true         = new double[n_els_d+1];
		double[] vol_diff_gen_den     = new double[n_els_d];
		double[] vol_cum_gen_den      = new double[n_els_d+1];
		double[] vol_diff_gen_den_kin = new double[n_els_d];
		double[] vol_cum_gen_den_kin  = new double[n_els_d+1];
		
		// Arrays to store lookup tables for sample and true tangential velocity distributions
		double[] vtan_sample_pdf = new double[n_els_vt];
		double[] vtan_sample_cdf = new double[n_els_vt+1];
		double[] vtan_true_pdf   = new double[n_els_vt];
		double[] vtan_true_cdf   = new double[n_els_vt+1];
		
		// Array of distance values corresponding to cumulative volume array.
		double[] d_diff = new double[n_els_d];
		double[] d_cum  = new double[n_els_d+1];
		d_cum[0] = 0.0;
		for(int i=0; i<d_diff.length; i++)
		{
			d_diff[i]  = i*d_step + d_step/2.0;
			d_cum[i+1] = (i+1)*d_step;
		}
		
		// Array of tangential velocity values corresponding to tangential velocity PDF
		double[] vtan = new double[n_els_vt];
		for(int i=0; i<vtan.length; i++)
		{
			vtan[i] = i*vt_step + vt_step/2.0;
		}
		
		// True physical survey volume along LOS.
		System.out.print(" - Computing true survey volume...");
		SurveyVolume.getGeneralizedSurveyVolumeAlongLos(cov, mean, vt_step, n_els_vt, ra, dec, mu_min, mu_max, 
					     							    omega, density, d_step, vol_diff_true, vol_cum_true, false, false);
		System.out.println(" done!");
		
		// Survey volume along LOS, generalized for spatial density profile. This is used to
		// compute v_{max} according to the 'old' method.
		System.out.print(" - Computing survey volume generalised over density...");
		SurveyVolume.getGeneralizedSurveyVolumeAlongLos(cov, mean, vt_step, n_els_vt, ra, dec, mu_min, mu_max, 
			     										omega, density, d_step, vol_diff_gen_den, vol_cum_gen_den, true , false);
		System.out.println(" done!");
		
		// Survey volume along LOS, generalized for spatial density profile and kinematic selection. This is used to
		// compute v_{max} according to the 'new' method.
		System.out.print(" - Computing survey volume generalised over density and kinematic selection...");
		SurveyVolume.getGeneralizedSurveyVolumeAlongLos(cov, mean, vt_step, n_els_vt, ra, dec, mu_min, mu_max, 
			     										omega, density, d_step, vol_diff_gen_den_kin, vol_cum_gen_den_kin, true , true);
		System.out.println(" done!");
		
		// Sample tangential velocity distribution not required
//		System.out.print(" - Computing sample v_{tan} distribution...");
//		SampleTangentialVelocityDistribution.getSampleVtanDistributionTowardsLos(cov, mean, ra, dec, mu_min, mu_max, d_min_m, d_max_m, omega, density, vt_step, vtan_sample_pdf, vtan_sample_cdf, null);
//		System.out.println(" done!");
		
		System.out.print(" - Computing true v_{tan} distribution...");
		TangentialVelocityDistribution.getVtanDistributionTowardsLos(cov, mean, ra, dec, vt_step, vtan_true_pdf, vtan_true_cdf);
		System.out.println(" done!");		
		
		
		// Create objects to interpolate survey volume at arbitrary distances
		PositiveLinear survey_volume_cum_true        = new PositiveLinear(d_cum, vol_cum_true);
		PositiveLinear survey_volume_cum_gen_den     = new PositiveLinear(d_cum, vol_cum_gen_den);
		PositiveLinear survey_volume_diff_gen_den    = new PositiveLinear(d_diff, vol_diff_gen_den);
		PositiveLinear survey_volume_cum_gen_den_kin = new PositiveLinear(d_cum, vol_cum_gen_den_kin);
		
		// Create object to interpolate sample tangential velocity PDF and draw velocities from it
//		PositiveLinear vtan_sample = new PositiveLinear(vtan, vtan_sample_pdf);
		PositiveLinear vtan_true   = new PositiveLinear(vtan, vtan_true_pdf);
		
		
		System.out.println("Simulating survey:");
		
		// True physical survey volume between magnitude limits [pc^{3}]
		double vol_true = survey_volume_cum_true.interpolateY(d_max_m)[0] - survey_volume_cum_true.interpolateY(d_min_m)[0];
		
		// Survey volume between magnitude limits, generalised over density profile [pc^{3}]
		double v_max_old = survey_volume_cum_gen_den.interpolateY(d_max_m)[0] - survey_volume_cum_gen_den.interpolateY(d_min_m)[0];
		
		// Survey volume between magnitude limits, generalised over density profile and kinematic selection [pc^{3}]
		double v_max_new = survey_volume_cum_gen_den_kin.interpolateY(d_max_m)[0] - survey_volume_cum_gen_den_kin.interpolateY(d_min_m)[0];
		
		// Mean number of stars in survey volume considering stellar density profile
		int N = (int)Math.rint(v_max_old * stellar_density);

		System.out.println(" - Distance limits arising from magnitude selection:");
		System.out.println("   > d_min_m = "+d_min_m);
		System.out.println("   > d_max_m = "+d_max_m);
		System.out.println(" - True physical survey volume                                                   = "+vol_true+" [pc^3]");
		System.out.println(" - Survey volume generalised over population density                             = "+v_max_old+" [pc^3]");
		System.out.println(" - Survey volume generalised over population density and kinematic selection     = "+v_max_new+" [pc^3]");
		System.out.println(" - Mean number of stars between magnitude distance limits (# simulation objects) = "+N);
		
		// Output file for sample tangential velocity distribution
		File outf = new File("/home/nrowell/Astronomy/kinematic_bias/test.txt");
		BufferedWriter out = new BufferedWriter(new FileWriter(outf));
		
		// Compute sum of v_max, 1/v_max and (1/v_max)^2 using old method
		double S_inv_v_max_old   = 0.0;
		double S_inv_v_max_old_2 = 0.0;
		double S_v_max_old       = 0.0;
		
		// Compute sum of v_max, 1/v_max and (1/v_max)^2 using new method. Note that in this case v_max is the same for each star and is independent of the
		// tangential velocity and proper motion limits. We only require that each object passes the proper motion limits.
		double S_inv_v_max_new   = 0.0;
		double S_inv_v_max_new_2 = 0.0;
		double S_v_max_new       = 0.0;
		
		// Count number of stars that pass proper motion limits
		int N_obs = 0;
		
		// Draw survey objects
		for(int i=0; i<N; i++)
		{
			// Get random tangential velocity
			double vt = vtan_true.drawX();
			
			// Get random distance along LOS: draw from survey volume as a function of distance, generalized for
			// stellar population density profile.
			double distance = survey_volume_diff_gen_den.drawX(d_min_m, d_max_m);
			
			// Does this object pass the proper motion limits?
			double mu = AstrometryUtils.getMuFromDAndVt(distance, vt);
			
			if(mu<mu_min || mu>mu_max) continue;
			
			// Get random tangential velocity. Stars that passed proper motion selection have this distribution of tangential velocity
//			double vt = vtan_sample.drawX();
			
			N_obs++;
			
			// Get distances at which the proper motion passes the survey selection limits
			double d_max_mu = AstrometryUtils.getDFromMuAndVt(mu_min, vt);
			double d_min_mu = AstrometryUtils.getDFromMuAndVt(mu_max, vt);
			
			// Combine magnitude and proper motion detection limits to get overall distance limits
			double d_max = Math.min(d_max_mu, d_max_m);
			double d_min = Math.max(d_min_mu, d_min_m);
			
			assert(d_max > 0.0) : "d_max negative!";
			assert(d_min > 0.0) : "d_min negative!";
			
			// Get generalised survey volume contained in this distance range
			v_max_old = survey_volume_cum_gen_den.interpolateY(d_max)[0] - survey_volume_cum_gen_den.interpolateY(d_min)[0];
			
//			System.out.println("Distance limits arising from proper motion selection:");
//			System.out.println(" ¬ d_min_mu = "+d_min_mu);
//			System.out.println(" ¬ d_max_mu = "+d_max_mu);
//			System.out.println("Total distance limits:");
//			System.out.println(" ¬ d_min = "+d_min);
//			System.out.println(" ¬ d_max = "+d_max);
//			System.out.println("Survey volume computed using 'old' method = "+v_max_old);
			
			S_inv_v_max_old   +=  1.0/v_max_old;
			S_inv_v_max_old_2 += (1.0/v_max_old)*(1.0/v_max_old);
			S_v_max_old       +=  v_max_old;
			
			S_inv_v_max_new   +=  1.0/v_max_new;
			S_inv_v_max_new_2 += (1.0/v_max_new)*(1.0/v_max_new);
			S_v_max_new       +=  v_max_new;
			
			out.write(String.format("%f %f %f %f %f %f %f %f %f %f %f\n", vt, distance, mu, d_max_mu, d_min_mu, d_max_m, d_min_m, d_max, d_min, v_max_old, v_max_new));
			
		}
		
//		System.out.println("Survey volume computed using 'new' method = "+v_max_new);
//		System.out.println("Mean v_max using old method               = "+(v_max_mean/N));
		
		System.out.println("Simulation results:");
		System.out.println(" - Number of observed stars (passing proper motion limits) = "+N_obs);
		System.out.println(" - Mean v_{max} using original method = "+(S_v_max_old/N_obs));
		System.out.println(" - Mean v_{max} using updated method  = "+(S_v_max_new/N_obs));
		System.out.println(" - True population density                           = "+stellar_density);
		System.out.println(" - Population density computed using original method = "+S_inv_v_max_old+" +/- "+Math.sqrt(S_inv_v_max_old_2));
		System.out.println(" - Population density computed using updated method  = "+S_inv_v_max_new+" +/- "+Math.sqrt(S_inv_v_max_new_2));
		
		out.close();
		
	}
	
	
}
