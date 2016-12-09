package survey.exec;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import kinematics.TangentialVelocityDistribution;
import numeric.functions.Linear;
import survey.SurveyVolume;
import utils.AstrometryUtils;
import constants.Galactic;
import density.DensityProfile;
import density.ExponentialDisk;
import density.Uniform;
import Jama.Matrix;

public class SurveyVolumeSingleLOS
{
	
	public static void main(String[] args) throws IOException
	{
		
		// 1) Stellar population parameters
		
		Matrix cov             = Galactic.thin_disk_covariance;
		Matrix mean            = Galactic.thin_disk_mean;
		DensityProfile density = new ExponentialDisk(250.0);
//		Matrix cov             = Galactic.spheroid_covariance;
//		Matrix mean            = Galactic.spheroid_mean;
//		DensityProfile density = new Uniform();
		
		// 2) Survey parameters
		
		double mu_min = 0.1;          	// Proper motion limits [arcsec/yr]
		double mu_max = 1.0;
		double vt_min = 30.0;			// Tangential velocity thresholds [km/s]
		double vt_max = Double.MAX_VALUE;
		double omega = 0.01;          	// Survey footprint size [steradians]. All-sky has omega = 4*pi.
		
		// Line of sight towards north Galactic pole
//		double[] equatorial = AstrometryUtils.convertLongLatToRaDec(0.0, Math.toRadians(30.0));
//		double ra  = equatorial[0];  // Line of sight coordinates
//		double dec = equatorial[1];
		double ra  = Galactic.NGPra;  // Line of sight coordinates
		double dec = Galactic.NGPdec;
		
		
		// 3) Simulation parameters
		
		// Arrays to contain survey volume as a function of distance
		double[] vol_diff = new double[500];
		double[] vol_cum  = new double[501];
		double[] vol_diff_gen_den_kin = new double[500];
		double[] vol_cum_gen_den_kin  = new double[501];
		double[] vol_diff_gen_den = new double[500];
		double[] vol_cum_gen_den  = new double[501];
		
		// Tangential velocity resolution when generalising over kinematically selected samples
		double vt_step = 1.0;
		int n_steps_vt = 1000;
		
		// Distance step size between consecutive elements in arrays
		double d_step  = 1.0;
		
		// 4) Compute the sampled volume as a function of distance towards LOS
		
		SurveyVolume.getGeneralizedSurveyVolumeAlongLos(cov, mean, vt_step, n_steps_vt, ra, dec, mu_min, mu_max, vt_min, vt_max, omega,
				                                        density, d_step, vol_diff_gen_den_kin, vol_cum_gen_den_kin, true, true);
		
		
		SurveyVolume.getGeneralizedSurveyVolumeAlongLos(cov, mean, vt_step, n_steps_vt, ra, dec, mu_min, mu_max, vt_min, vt_max, omega,
                										density, d_step, vol_diff_gen_den, vol_cum_gen_den, true, false);
		
		
		SurveyVolume.getGeneralizedSurveyVolumeAlongLos(cov, mean, vt_step, n_steps_vt, ra, dec, mu_min, mu_max, vt_min, vt_max, omega,
														density, d_step, vol_diff, vol_cum, false, false);
		
		
		
		// 5)  Compute the 'naive' discovery fraction i.e. the discovery fraction given the tangential velocity
		//     distribution and the survey tangential velocity limits.
		
		// Arrays to contain tangential velocity distribution.
		double[] vtan_pdf = new double[n_steps_vt];
		double[] vtan_cdf = new double[n_steps_vt+1];
		
		// Corresponding v_{tan} values for CDF points.
		double[] vtan = new double[vtan_cdf.length];
		for(int i=0; i<vtan_cdf.length; i++)
			vtan[i] = i*vt_step;
		
		// Compute tangential velocity distribution for the model population
		// along the chosen line of sight.
		TangentialVelocityDistribution.getVtanDistributionTowardsLos(cov, mean, ra, dec, vt_step, vtan_pdf, vtan_cdf);
		
		// Now we construct a Linear object that will be used to interpolate
		// the cumulative distribution at steps along the line of sight.
		Linear vtan_cdf_interpolator = new Linear(vtan, vtan_cdf);
		
		double discovery_fraction = vtan_cdf_interpolator.interpolateY(vt_max)[0] - vtan_cdf_interpolator.interpolateY(vt_min)[0];
		
		File outf = new File("/home/nrowell/Publications/In_preparation/Generalized_vmax_estimator/plots/fig1/thin_disk/differential_survey_volume.txt");
		BufferedWriter out = new BufferedWriter(new FileWriter(outf));
		
		out.write("# Proper motion limits = " + mu_min + " -> " + mu_max + " [as/yr]\n");
		out.write("# Fixed v_{tan} limits = " + vt_min + " -> " + vt_max + " [km/s]\n");
		out.write("# Survey footprint     = " + omega + " [sr]\n");
		out.write("# Line of sight RA/Dec = " + ra+"/"+dec + " [radians]\n");
		out.write("# Density profile      = " + density.toString()+"\n");
		double U = mean.get(0, 0); double V = mean.get(1, 0); double W = mean.get(2, 0);
		out.write(String.format("# Stellar mean velocity      = [%f %f %f] [km/s]\n", U, V, W));
		double UU = cov.get(0, 0); double UV = cov.get(1, 0); double UW = cov.get(2, 0);
		double VV = cov.get(1, 1); double VW = cov.get(1, 2);
		double WW = cov.get(2, 2);
		out.write(String.format("# Stellar velocity ellipsoid = [%f %f %f ; %f %f %f ; %f %f %f ] [km/s]\n", UU, UV, UW, UV, VV, VW, UW, VW, WW));
		out.write("# Discovery fraction      = " + discovery_fraction +"\n");
		
		out.write("# Differential survey volume as function of line-of-sight distance\n");
		out.write("# ----------------------------------------------------------------\n");
		out.write("# Column 1: distance along LOS to centre of annulus\n");
		out.write("# Column 2: true physical volume per parsec along LOS [pc^3 / pc]\n");
		out.write("# Column 3: volume generalised for population density profile [pc^3 / pc]\n");
		out.write("# Column 4: volume generalised for population density profile and kinematic selection/v_{tan} distribution [pc^3 / pc]\n");
		
		
		for(int d=0; d<vol_diff.length; d++)
		{
			double distance = (d+0.5)*d_step;
			out.write(distance + "\t" + vol_diff[d] + "\t" + vol_diff_gen_den[d]  + "\t" + vol_diff_gen_den_kin[d] + "\n");
		}
		
		out.close();
		
	}
	
}
