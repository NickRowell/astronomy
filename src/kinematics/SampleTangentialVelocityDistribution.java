package kinematics;

import java.io.BufferedWriter;
import java.io.IOException;

import density.DensityProfile;
import numeric.functions.Linear;
import utils.AstrometryUtils;
import Jama.Matrix;

public class SampleTangentialVelocityDistribution
{
	
	// Number of discrete steps in distance when integrating along LOS.
	public static int DISTANCE_STEPS = 1000;
	
	
	/**
	 * Computes the distribution of observed tangential velocities among objects
	 * that pass the survey proper motion and magnitude limits. This is obtained
	 * by first calculating the true underlying tangential velocity distribution
	 * then integrating the survey volume along the line of sight, considering
	 * the fraction of objects that pass the proper motion limits at each distance.
	 * The magnitude limits enter the equation as fixed limits on the distance
	 * along the LOS; this means that the sample v_{tan} distribution is strictly
	 * true only for a single absolute magnitude.
	 * 
	 * The format of the returned pdf and cdf are the same as for the true tangential
	 * velocity distribution.
	 * @param covariance_G		Population velocity ellipsoid in Galactic coordinates [(km/s)^2]
	 * @param mean_G			Population mean velocity relative to Sun, in Galactic coordinates [km/s]
	 * @param ra				Right ascension of LOS, equatorial coordinates [radians]
	 * @param dec				Declination of LOS, equatorial coordinates [radians]
	 * @param mu_min			Survey proper motion lower limit [arcsec/year]
	 * @param mu_max			Survey proper motion upper limit [arcsec/year]
	 * @param d_range_min		Fixed lower distance [parsecs]; from apparent magnitude limit
	 * @param d_range_max		Fixed upper distance [parsecs]; from apparent magnitude limit
	 * @param omega				Survey footprint size [steradians]
	 * @param density			Population density profile
	 * @param vt_major_step		Step size in tangential velocity between elements in PDF/CDF arrays [km/s]
	 * @param pdf				On exit, contains tangential velocity PDF, tabulated at tangential
	 * 							velocity values from 0+step/2 to (pdf.length-1)*step+step/2 [km/s].
	 * 							Such that the element pdf[x] contains the probability (per unit vtan)
	 * 							that the tangential velocity lies in the range x*vt_major_step -> 
	 * 							(x+1)*vt_major_step.
	 * @param cdf				On exit, contains tangential velocity CDF, tabulated at tangential
	 * 							velocity values from 0 to (cdf.length-1)*step [km/s].
	 * 							Such that the element cdf[x] contains the probability that the
	 * 							tangential velocity lies in the range 0 -> x*vt_major_step.
	 * 							NOTE: cdf array must have one more element than pdf, to contain
	 * 							      initial point at 0. This forces to the user to be aware that
	 * 							      the CDF represents the integral over ranges in v_{tan} whereas
	 * 								  the PDF represents probability density at instantaneous points.
	 * 
	 */
	public static void getSampleVtanDistributionTowardsLos(Matrix covariance_G, Matrix mean_G,
		     double ra, double dec, double mu_min, double mu_max, double d_range_min, double d_range_max,
		     double omega, DensityProfile density, double vt_major_step, double[] pdf, double[] cdf)
	{
		getSampleVtanDistributionTowardsLos(covariance_G, mean_G,
			     ra, dec, mu_min, mu_max, d_range_min, d_range_max,
			     omega, density, vt_major_step, pdf, cdf, null);
	}
	
	/**
	 * Identical to other method but allows a BufferedWriter to be passed in which is used
	 * to write 2D stellar density data in the distance/v_{tan} plane in order to produce
	 * nice plots of the number of stars as a function of distance and tangential velocity.
	 * 
	 */
	public static void getSampleVtanDistributionTowardsLos(Matrix covariance_G, Matrix mean_G,
		     double ra, double dec, double mu_min, double mu_max, double d_range_min, double d_range_max,
		     double omega, DensityProfile density, double vt_major_step, double[] pdf, double[] cdf,
		     BufferedWriter out)
	{
		
		assert(pdf.length+1==cdf.length) : "CDF array must have length = PDF.length+1!";
		
		assert(d_range_min < d_range_max && d_range_min > 0.0) : "Invalid distance range!";
		
		// If we have a non-null writer, then we are in debug mode
		boolean debug = (out!=null);
		
		// Maximum observed tangential velocity; corresponds to an object at the
		// upper proper motion limit and at the maximum distance.
		double vt_survey_max = AstrometryUtils.getVtFromMuAndD(mu_max, d_range_max);
		
		// Sanity check: is maximum observable v_{tan} within range of pdf/cdf tables?
		// It might be better to check if tables are larger than twice the trace of the
		// covariance matrix or something, to check instead that we capture the main 
		// feature of the distribution.
		if(pdf.length*vt_major_step < vt_survey_max)
			System.out.println("WARNING: sample tangential velocity distribution"
					+ " arrays don't contain full range of v_{tan}!"
					+ "\nArray maximum = "+(pdf.length*vt_major_step + " [km/s]")
					+ "\nObserved V_{tan} maximum = "+vt_survey_max + " [km/s]");
		
		// Compute true tangential velocity distribution at same range and resolution
		// as that specified for the sample tangential velocity distribution.
		double[] vt_true_pdf = new double[pdf.length];	// PDF array
		double[] vt_true_cdf = new double[cdf.length];	// CDF array
		double[] vtan = new double[cdf.length];         // Corresponding v_{tan} values for CDF
		for(int i=0; i<vt_true_cdf.length; i++)
			vtan[i] = i*vt_major_step;
		
		// Compute true tangential velocity distribution along this LOS
		TangentialVelocityDistribution.getVtanDistributionTowardsLos(covariance_G, mean_G, ra, dec, vt_major_step, vt_true_pdf, vt_true_cdf);
		
		// Interpolate true tangential velocity distribution.
		Linear vt_true = new Linear(vtan, vt_true_cdf);
		
		// Step size in distance
		double d_step = (d_range_max - d_range_min)/DISTANCE_STEPS;
		
		// Main integral over distance
		for(double d=0; d<DISTANCE_STEPS; d++)
		{
			// Translate loop variable to physical distance
			double d_min = d_range_min + (d+0.0)*d_step;
			double d_mid = d_range_min + (d+0.5)*d_step;
			double d_max = d_range_min + (d+1.0)*d_step;
			
			// True physical volume contained in this distance step [pc^3]
			double volume = omega * (Math.pow(d_max,3)/3.0 - Math.pow(d_min,3)/3.0);
			
			// Correction for population density profile 
			volume *= density.getDensity(d_mid, ra, dec);
			
			// Range of tangential velocities that pass proper motion
			// limits at this distance.
			double vt_min = AstrometryUtils.getVtFromMuAndD(mu_min, d_mid);
			double vt_max = AstrometryUtils.getVtFromMuAndD(mu_max, d_mid);
			
			// Calculate contribution to each sample tangential velocity bin
			for(int vt=0; vt<pdf.length; vt++)
			{
				// Range of sample tangential velocity bin
				double vt_bin_min = (vt+0)*vt_major_step;
				double vt_bin_max = (vt+1)*vt_major_step;
				
				// Write out the number of objects in this distance and
				// tangential velocity bin. This is useful for plotting
				// sampled tangential velocity range against density of
				// objects.
				if(debug)
				{
					double vt_bin_mid = (vt+0.5)*vt_major_step;
					double s = vt_true.interpolateY(vt_bin_max)[0] - vt_true.interpolateY(vt_bin_min)[0];
					try
					{
						// Scale stellar density to stars per unit distance and vtan
						out.write(d_mid+"\t"+vt_bin_mid+"\t"+(volume*s/(vt_major_step*d_step))+"\n");
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
				
				// Bin does not overlap with observed range of vtan at this distance; no contribution.
				if(vt_bin_max < vt_min || vt_bin_min > vt_max) continue;
				
				// Bin straddles upper limit on observed vtan range; truncate bin upper limit
				if(vt_bin_max > vt_max) vt_bin_max = vt_max;
				
				// Bin straddles lower limit on observed vtan range; truncate bin lower limit
				if(vt_bin_min < vt_min) vt_bin_min = vt_min;
				
				// Fraction of objects that lie in this vtan bin
				double s = vt_true.interpolateY(vt_bin_max)[0] - vt_true.interpolateY(vt_bin_min)[0];
				
				// Add the current tangential velocity element to the
				// sample tangential velocity distribution.
				pdf[vt] += volume * s / vt_major_step;
				
			}
			
			if(debug)
			{
				try { out.write("\n");}
				catch (IOException e) {}
			}
		}
		
		// Compute CDF from PDF. Uses the following optimisation:
		//
		// CDF_i = SUM_{j=0}^i(PDF_j) = PDF_i + SUM_{j=0}^{i-1}(PDF_j) = PDF_i + CDF_{i-1}
		//
		cdf[0] = 0.0;
		for(int vt_major=0; vt_major<pdf.length; vt_major++)
		{
			cdf[vt_major+1] = pdf[vt_major]*vt_major_step + cdf[vt_major];
		}
		
		// Normalise the CDF and PDF.
		double norm = cdf[cdf.length-1];
		for(int i=0; i<pdf.length; i++)
		{
			cdf[i+1] /= norm;
			pdf[i]   /= norm;
		}
		
		if(debug)
		{
			try { out.flush();}
			catch (IOException e) {}
		}
		
	}
	
	
}
