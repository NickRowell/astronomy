package survey;

import kinematics.TangentialVelocityDistribution;
import numeric.functions.Linear;
import numeric.functions.MonotonicLinear;
import utils.AstrometryUtils;
import Jama.Matrix;
import density.DensityProfile;

/**
 * This class is used to investigate the variation in survey volume element
 * size along the line of sight, considering the fraction of objects that pass
 * the survey proper motion limits at each distance step. This is done by
 * folding in the proper motion limits and population tangential velocity
 * distribution for the chosen line of sight.
 * Note that we assume:
 *  - Population has a uniform density profile.
 * 
 * 
 * @author nrowell
 *
 */
public class SurveyVolume
{
	/**
	 * Compute the differential and total cumulative survey volume as a function of distance along the given
	 * line of sight and for the given survey footprint size.
	 * 
	 * This method optionally includes a correction for a non-uniform population spatial distribution, and also
	 * for the effects of kinematic selection given the population velocity distribution, both of which will
	 * act to reduce the effective survey volume relative to the true physical volume. This method therefore
	 * computes the the 'generalized volume'.
	 * 
	 * TODO: break down the numerical integration so that each annulus is broken down into radial/angular elements,
	 *       so that the varying density profile in very large annuli at low galactic latitude can be modelled.
	 *       
	 * @param covariance_G		Population velocity ellipsoid in Galactic coordinates [(km/s)^2]
	 * @param mean_G			Population mean velocity relative to Sun, in Galactic coordinates [km/s]
	 * @param vt_step			Step size in tangential velocity to use when generalising for kinematic selection [km/s]
	 * @param ra				Right Ascension of LOS, equatorial coordinates [radians]
	 * @param dec				Declination of LOS, equatorial coordinates [radians]
	 * @param mu_min			Survey proper motion lower limit [arcsec/year]
	 * @param mu_max			Survey proper motion upper limit [arcsec/year]
	 * @param omega				Survey footprint size [steradians]
	 * @param density			Population density profile
	 * @param d_step			Step size in distance for tabulated survey volume function [pc]
	 * @param vol_diff			On exit, contains differential survey volume as a function of distance [pc^2] tabulated
	 * 							at distances from (d_step/2.0) to (vol_diff.length-1)*d_step+d_step/2, such that
	 * 							element i gives the survey volume per parsec contained in the interval
	 * 							i*d_step -> (i+1)*d_step.
	 * @param vol_cum			On exit, contains cumulative survey volume as a function of distance [pc^3] tabulated
	 * 							at distances from 0 to (vol_cum-1)*d_step pc, such that the element i gives the
	 * 							total survey volume contained out to distance i*d_step.
	 * @param GENERALIZE_FOR_DENSITY_PROFILE
	 * 							Activates generalisation for non-uniform density profile
	 * @param GENERALIZE_FOR_KINEMATIC_SELECTION
	 * 							Activates generalisation for kinematic selection
	 */
	public static void getGeneralizedSurveyVolumeAlongLos(Matrix covariance_G, Matrix mean_G, double vt_step, int n_steps_vt,
		     double ra, double dec, double mu_min, double mu_max, 
		     double omega, DensityProfile density, double d_step, double[] vol_diff, double[] vol_cum,
		     boolean GENERALIZE_FOR_DENSITY_PROFILE, boolean GENERALIZE_FOR_KINEMATIC_SELECTION)
	{
		
		// Default tangential velocity limits
		double vt_min = 0;
		double vt_max = Double.MAX_VALUE;
		
		getGeneralizedSurveyVolumeAlongLos(covariance_G, mean_G, vt_step, n_steps_vt,
										     ra, dec, mu_min, mu_max, vt_min, vt_max,
										     omega, density, d_step, vol_diff, vol_cum,
										     GENERALIZE_FOR_DENSITY_PROFILE, GENERALIZE_FOR_KINEMATIC_SELECTION);
	}
	
	/**
	 * Compute the differential and total cumulative survey volume as a function of distance along the given
	 * line of sight and for the given survey footprint size.
	 * 
	 * This method optionally includes a correction for a non-uniform population spatial distribution, and also
	 * for the effects of kinematic selection given the population velocity distribution, both of which will
	 * act to reduce the effective survey volume relative to the true physical volume. This method therefore
	 * computes the the 'generalized volume'.
	 * 
	 * TODO: break down the numerical integration so that each annulus is broken down into radial/angular elements,
	 *       so that the varying density profile in very large annuli at low galactic latitude can be modelled.
	 * 
	 * @param covariance_G		Population velocity ellipsoid in Galactic coordinates [(km/s)^2]
	 * @param mean_G			Population mean velocity relative to Sun, in Galactic coordinates [km/s]
	 * @param vt_step			Step size in tangential velocity to use when generalising for kinematic selection [km/s]
	 * @param ra				Right Ascension of LOS, equatorial coordinates [radians]
	 * @param dec				Declination of LOS, equatorial coordinates [radians]
	 * @param mu_min			Survey proper motion lower limit [arcsec/year]
	 * @param mu_max			Survey proper motion upper limit [arcsec/year]
	 * @param vt_min			Survey tangential velocity lower limit [km/s]
	 * @param vt_max			Survey tangential velocity upper limit [km/s]
	 * @param omega				Survey footprint size [steradians]
	 * @param density			Population density profile
	 * @param d_step			Step size in distance for tabulated survey volume function [pc]
	 * @param vol_diff			On exit, contains differential survey volume as a function of distance [pc^2] tabulated
	 * 							at distances from (d_step/2.0) to (vol_diff.length-1)*d_step+d_step/2, such that
	 * 							element i gives the survey volume per parsec contained in the interval
	 * 							i*d_step -> (i+1)*d_step.
	 * @param vol_cum			On exit, contains cumulative survey volume as a function of distance [pc^3] tabulated
	 * 							at distances from 0 to (vol_cum-1)*d_step pc, such that the element i gives the
	 * 							total survey volume contained out to distance i*d_step.
	 * @param GENERALIZE_FOR_DENSITY_PROFILE
	 * 							Activates generalisation for non-uniform density profile
	 * @param GENERALIZE_FOR_KINEMATIC_SELECTION
	 * 							Activates generalisation for kinematic selection
	 */     
	public static void getGeneralizedSurveyVolumeAlongLos(Matrix covariance_G, Matrix mean_G, double vt_step, int n_steps_vt,
		     double ra, double dec, double mu_min, double mu_max, double vt_min, double vt_max,
		     double omega, DensityProfile density, double d_step, double[] vol_diff, double[] vol_cum,
		     boolean GENERALIZE_FOR_DENSITY_PROFILE, boolean GENERALIZE_FOR_KINEMATIC_SELECTION)	     
	{
		
		// Sanity checks
		assert(vol_diff.length+1==vol_cum.length) : "Cumulative array must have one more element than differential array!";
		
		// Get true tangential velocity distribution along LOS, for generalising for kinematic selection
		
		// Arrays to contain tangential velocity distribution.
		double[] vtan_pdf = new double[n_steps_vt];
		double[] vtan_cdf = new double[n_steps_vt+1];
		
		// Corresponding v_{tan} values for CDF points.
		double[] vtan = new double[vtan_cdf.length];
		for(int i=0; i<vtan_cdf.length; i++)
			vtan[i] = i*vt_step;
		
		// Compute tangential velocity distribution for the model population
		// along the chosen line of sight.
		TangentialVelocityDistribution.getVtanDistributionTowardsLos(covariance_G, mean_G, ra, dec, vt_step, vtan_pdf, vtan_cdf);
		
		// Now we construct a Linear object that will be used to interpolate
		// the cumulative distribution at steps along the line of sight.
		Linear vtan_cdf_interpolator = new Linear(vtan, vtan_cdf);
		
		// Commence simulation
		
		// Main survey volume integral over distance
		for(int d=0; d<vol_diff.length; d++)
		{
			// Each step computes the survey volume per parsec over the distance
			// range i*d_step -> (i+1)*d_step.
			// Quantities are evaluated at the centre of the distance range.
			
			// Translate step number to distances
			double d_min = (d+0.0)*d_step;
			double d_mid = (d+0.5)*d_step;
			double d_max = (d+1.0)*d_step;
			
			// Physical volume contained in this distance step (assumes footprint
			// is circular, i.e. survey volume is a cone aligned along line of sight).
			double v = omega * (Math.pow(d_max,3)/3.0 - Math.pow(d_min,3)/3.0);
			
			// Generalise for population density profile
			if(GENERALIZE_FOR_DENSITY_PROFILE)
			{
				// Correct volume for density scale factor
				v *= density.getDensity(d_mid, ra, dec);
			}
			
			// Generalise for kinematic selection
			if(GENERALIZE_FOR_KINEMATIC_SELECTION)
			{
				// Limits on tangential velocity range for objects that pass the
				// survey proper motion limits, evaluated at the bin centre, considering the
				// limits implied by proper motion selection at this distance AND the externally
				// applied tangential velocity limits.
				double vt_min_d = Math.max(AstrometryUtils.getVtFromMuAndD(mu_min, d_mid), vt_min);
				double vt_max_d = Math.min(AstrometryUtils.getVtFromMuAndD(mu_max, d_mid), vt_max);
				
				// Compute fraction of population lying between these limits.
				double discovery_fraction = 0.0;
				
				if(vt_max_d > vt_min_d)
					discovery_fraction = vtan_cdf_interpolator.interpolateY(vt_max_d)[0] - vtan_cdf_interpolator.interpolateY(vt_min_d)[0];
				
				// Correct volume for population discovery fraction
				v *= discovery_fraction;
			}
			
			// Normalise to volume per-parsec along LOS [pc^3 per pc, or pc^2]
			vol_diff[d] = v / d_step;
			
		}
		
		// Now compute CDF from PDF. Uses the following optimisation:
		//
		// CDF_i = SUM_{j=0}^i(PDF_j) = PDF_i + SUM_{j=0}^{i-1}(PDF_j) = PDF_i + CDF_{i-1}
		//
		vol_cum[0] = 0.0;
		for(int d=0; d<vol_diff.length; d++)
		{
			vol_cum[d+1] = vol_diff[d]*d_step + vol_cum[d];
		}
		
	}
	
	
	/**
     * This method provides a MonotonicLinear object that can be used to interpolate the cumulative
     * survey volume as a function of distance. The volume is shaped like an inverted cone
     * with an opening half-angle equal to the given value (such that the total angle from one side
     * of the cone to the other is twice this). The volume is generalised over the density profile
     * of the population, which is that of an exponential disk. The survey line of sight is directly
     * up out of the plane, along the direction of most rapid density falloff.
     * 
     * TODO: provide DensityProfile as a parameter
     * TODO: provide line of sight coordinates
     * TODO: merge with other methods to provide more generalised functions
     * 
     * @param scaleheight   Density scaleheight of disk (pc).
     * @param opening       Opening angle of survey footprint (radians).
     * @return 
     * 		MonotonicLinear object that can be used to interpolate the survey volume as a function
     * of distance.
     * 
     */
    public static MonotonicLinear getSurveyVolume(double scaleheight, double opening)
    {
    	
    	// Integration step size, radial direction (pc).
        double dr = scaleheight/1000;
        // Limit on radial coordinate (pc). Set large enough that exponential 
        // decay is flattened out.
        double r_max = 30*scaleheight;
        // Integration step size, polar direction (radians).
        double dphi = Math.toRadians(0.25);      
        
        // Arrays to store distance & cumulative volume coordinates
        double[] distance = new double[(int)(r_max/dr)+1];
        double[] volume   = new double[(int)(r_max/dr)+1];
        
        // Index over elements
        int index = 0;
        
        // First point has distant=volume=0;
        distance[index]=volume[index++]=0.0;
        
        // Main integration loop over radial coordinate.
        for(double r=0; r<r_max; r+= dr)
        {
            
            // Sum volume elements in this annulus
            volume[index] = 0.0;
            distance[index] = r+dr/2;
            
            // Integrate over angular coordinates to get volume contained in
            // annulus stretching from r to r+dr.
            
            // Secondary loop over polar coordinate
            for(double phi = 0; (phi+dphi/2) < opening; phi += dphi)
            {
                
                // At fixed value of polar coordinate, all azimuthal coordinates
                // have same distance from Galactic plane.
                double z = (r+dr/2) * Math.cos(phi+dphi/2);
                
                // No point numerically integrating over azimuthal coordinate,
                // can solve this part of integral analytically.
                double dV = (r+dr/2) * (r+dr/2) * Math.sin(phi+dphi/2) * dr * dphi * 2 * Math.PI;
                                   
                // Correct for exponential density profile
                dV *= Math.exp(-z/scaleheight);
                    
                volume[index] += dV;
            
            }
        
            // Accumulate volume over all annuli
            volume[index] += volume[index-1];
            
            // Move on to next element
            index++;
            
        }
        
        return new MonotonicLinear(distance, volume, "Scaleheight = "+scaleheight,
                                   MonotonicLinear.Type.INCREASING);
    }
    
}
