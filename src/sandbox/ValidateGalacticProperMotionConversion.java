package sandbox;

import astrometry.util.AstrometryUtils;
import constants.Units;

/**
 * Class converts coordinates from degrees to HMS/DMS.
 *
 *
 * @author nrowell
 * @version $Id$
 */
public class ValidateGalacticProperMotionConversion {

	/**
	 * Application main entry point.
	 * 
	 * @param args
	 * 	The command line args (ignored).
	 */
	public static void main(String[] args) {
		
		// Pick two points on the Galactic equator separated by a short angular distance
		double l1 = Math.toRadians(34.0);
		double b1 = Math.toRadians(0.0);
		
		double l2 = Math.toRadians(34.0000001);
		double b2 = Math.toRadians(0.0);
		
		// Convert postions to equatorial frame
		double[] raDec1 = AstrometryUtils.convertPositionGalToEq(l1, b1);
		double[] raDec2 = AstrometryUtils.convertPositionGalToEq(l2, b2);
		
		System.out.println("Position in equatorial coordinates:");
		System.out.println(" - a1 = " + Math.toDegrees(raDec1[0]));
		System.out.println(" - d1 = " + Math.toDegrees(raDec1[1]));
		System.out.println(" - a2 = " + Math.toDegrees(raDec2[0]));
		System.out.println(" - d2 = " + Math.toDegrees(raDec2[1]));
		
		// Compute proper motion in equatorial frame
		
		// Time between positions [yr]
		double deltaT = 1.0;
		
		// Proper motion components parallel and perpendicular to the equator in rad/yr
		double mu_acosd = (raDec2[0] - raDec1[0]) * Math.cos(raDec1[1]) / deltaT;
		double mu_d     = (raDec2[1] - raDec1[1]) / deltaT;
		
		System.out.println("Proper motion in equatorial coordinates [mas/yr]:");
		System.out.println(" - mu_acosd = " + (mu_acosd / Units.MILLIARCSEC_TO_RADIANS));
		System.out.println(" - mu_d     = " + (mu_d / Units.MILLIARCSEC_TO_RADIANS));
		
		// Convert position and proper motion to Galactic frame
		double[] gal = AstrometryUtils.convertPositionAndProperMotionEqToGal(raDec1[0], raDec1[1], mu_acosd, mu_d);
		double l = gal[0];
		double b = gal[1];
		double mu_lcosb = gal[2];
		double mu_b = gal[3];

		System.out.println("Position in Galactic coordinates:");
		System.out.println(" - l = " + Math.toDegrees(l));
		System.out.println(" - b = " + Math.toDegrees(b));
		
		System.out.println("Proper motion in Galactic coordinates [mas/yr]:");
		System.out.println(" - mu_lcosb = " + (mu_lcosb / Units.MILLIARCSEC_TO_RADIANS));
		System.out.println(" - mu_b     = " + (mu_b / Units.MILLIARCSEC_TO_RADIANS));
		
		System.out.println("Proper motion in Galactic coordinates [deg/yr]:");
		System.out.println(" - mu_lcosb = " + Math.toDegrees(mu_lcosb));
		System.out.println(" - mu_b     = " + Math.toDegrees(mu_b));
		
	}
	
}
