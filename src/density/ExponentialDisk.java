package density;

import constants.Galactic;
import Jama.Matrix;
import astrometry.util.AstrometryUtils;

/**
 * Represents populations whose density falls off as an exponential
 * function of the distance to the Galactic plane. No radial effect
 * is included.
 * @author nrowell
 *
 */
public class ExponentialDisk extends DensityProfile {
	/**
	 * Scaleheight of disk [parsec].
	 */
	double H;
	
	public ExponentialDisk(double pH) {
		H = pH;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public double getDensity(double r, double ra, double dec) {
		
		// Position vector of point in Cartesian coordinates, equatorial frame:
		Matrix r_E = AstrometryUtils.sphericalPolarToCartesian(r, ra, dec);
		
		// Rotate this to Galactic frame.
		Matrix r_G = Galactic.r_G_E.times(r_E);
		
		// The Z component gives the Galactic plane distance.
		double z = r_G.get(2, 0);
		
		// Apply exponential density model.
		return Math.exp(-Math.abs(z)/H);
	}
	
	/**
	 * Text description of profile.
	 */
	public String toString() {
		return String.format("Exponential disk, scaleheight = %f [pc]", H);
	}
}
