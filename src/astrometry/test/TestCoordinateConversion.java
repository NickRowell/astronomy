package astrometry.test;

import astrometry.util.AstrometryUtils;
import constants.Units;

/**
 * Class tests the conversion of coordinates between Equatorial and Galactic frames.
 *
 *
 * @author nrowell
 * @version $Id$
 */
public class TestCoordinateConversion {
	
	/**
	 * Main application entry point,
	 * 
	 * @param args
	 * 	The command line arguments (ignored)
	 */
	public static void main(String[] args) {
		
		double ra = 45.0343303544;
		double dec = 0.2353916488;
		double pmra = 43.752 * Units.MILLIARCSEC_TO_RADIANS;
		double pmdec = -7.642 * Units.MILLIARCSEC_TO_RADIANS;
		
		double[] lbpm = AstrometryUtils.convertPositionAndProperMotionEqToGal(Math.toRadians(ra), Math.toRadians(dec), pmra, pmdec);
		
		System.out.println("l = "+Math.toDegrees(lbpm[0]));
		System.out.println("b = "+Math.toDegrees(lbpm[1]));

		System.out.println("mu_l = "+lbpm[2] * Units.RADIANS_TO_MILLIARCSEC);
		System.out.println("mu_b = "+lbpm[3] * Units.RADIANS_TO_MILLIARCSEC);
		
		
	}
	
	
	
	
}
