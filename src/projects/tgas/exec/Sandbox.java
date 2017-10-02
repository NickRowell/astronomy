package projects.tgas.exec;

import Jama.Matrix;
import astrometry.util.AstrometryUtils;
import constants.Galactic;
import constants.Units;

/**
 * Sandbox for the TGAS package.
 *
 *
 * @author nrowell
 * @version $Id$
 */
public class Sandbox {

	/**
	 * Adopted value for the Oort Constant A, in units of radians per year.
	 */
	static double A = Galactic.A * Units.KM_PER_SEC_PER_KPC_TO_RAD_PER_YEAR;

	/**
	 * Adopted value for the Oort Constant B, in units of radians per year.
	 */
	static double B = Galactic.B * Units.KM_PER_SEC_PER_KPC_TO_RAD_PER_YEAR;
	
	/**
	 * Main application entry point.
	 * @param args
	 * 	The command line args (ignored)
	 */
	public static void main(String[] args) {
		
		// Degrees
		double ra_deg = 112.606219;
		double dec_deg = -10.378157;
		// Milliarcseconds per year
		double mu_racosd_masyr = -2.076;
		double mu_dec_masyr = 0.706;
		// Milliarcseconds
		double parallax = 0.25;
		
		// Convert to distance [parsecs]
		double d = 1000.0 / parallax;
		
		// Convert to radians
		double ra = Math.toRadians(ra_deg);
		double dec = Math.toRadians(dec_deg);
		
		// Convert to radians per year
		double mu_racosd = mu_racosd_masyr * Units.MILLIARCSEC_TO_RADIANS;
		double mu_dec = mu_dec_masyr * Units.MILLIARCSEC_TO_RADIANS;
		
		// Convert to Galactic coordinates
		double[] gal = AstrometryUtils.convertPositionAndProperMotionEqToGal(ra, dec, mu_racosd, mu_dec);
		
		double l = gal[0];
		double b = gal[1];
		double mu_lcosb = gal[2];
		double mu_b = gal[3];
		
		System.out.println("\nEquatorial Coordinates:\n");
		
		System.out.println("ra     = " + ra_deg + " [DEGREES]");
		System.out.println("dec    = " + dec_deg + " [DEGREES]");
		System.out.println("pm_ra  = " + mu_racosd_masyr + " [MAS/YR]");
		System.out.println("pm_dec = " + mu_dec_masyr + " [MAS/YR]");
		
		System.out.println("\nra     = " + ra + " [RADIANS]");
		System.out.println("dec    = " + dec + " [RADIANS]");
		System.out.println("pm_ra  = " + mu_racosd + " [RAD/YR]");
		System.out.println("pm_dec = " + mu_dec + " [RAD/YR]");
		
		System.out.println("\npi     = " + parallax + " [MAS]");
		System.out.println("dist   = " + d + " [PARSEC]");

		System.out.println("\nGalactic Coordinates:\n");
		
		System.out.println("l    = " + l + " [RADIANS]");
		System.out.println("b    = " + b + " [RADIANS]");
		System.out.println("pm_l = " + mu_lcosb + " [RAD/YR]");
		System.out.println("pm_b = " + mu_b + " [RAD/YR]");
		
		System.out.println("\nCorrected for Oort constants:\n");
		
		mu_lcosb = mu_lcosb - (A * Math.cos(2 * l) + B) * Math.cos(b);
		mu_b = mu_b + A * Math.sin(2 * l) * Math.cos(b) * Math.sin(b);
		
		System.out.println("pm_l = " + mu_lcosb + " [RAD/YR]");
		System.out.println("pm_b = " + mu_b + " [RAD/YR]");
		

		// Convert to proper motion velocity vector
		Matrix p =  AstrometryUtils.getTangentialVelocityVector(d, l, b, mu_lcosb, mu_b);
		
		// Compute the projection matrix A along the line of sight towards this star
		Matrix AA = AstrometryUtils.getProjectionMatrixA(l, b);
		
		
		System.out.println("\nProper motion velocity:\n");
		
		System.out.println("U    = " + p.get(0, 0) + " [km/s]");
		System.out.println("V    = " + p.get(1, 0) + " [km/s]");
		System.out.println("W    = " + p.get(2, 0) + " [km/s]");
		System.out.println();
		System.out.println("U    = " + p.get(0, 0)*Units.KILOMETRES_PER_SECOND_TO_METRES_PER_YEAR + " [m/yr]");
		System.out.println("V    = " + p.get(1, 0)*Units.KILOMETRES_PER_SECOND_TO_METRES_PER_YEAR + " [m/yr]");
		System.out.println("W    = " + p.get(2, 0)*Units.KILOMETRES_PER_SECOND_TO_METRES_PER_YEAR + " [m/yr]");
		
		
		
		
		System.out.println("\nProjection matrix A:\n");
		
		AA.print(5, 5);
		
		
		
	}
}
