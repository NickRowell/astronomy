package astrometry.test;

import java.util.Arrays;

import Jama.Matrix;
import astrometry.DistanceFromParallax;
import astrometry.DistanceFromParallax.METHOD;
import astrometry.util.AstrometryUtils;
import constants.Galactic;
import constants.Units;

public class TestAstrometryUtils
{
	
	public static void main(String[] args)
	{
		// Test utilities for converting between equatorial and Galactic coordinates.
		
		double ra  = Galactic.NGPra;
		double dec = Galactic.NGPdec;
		
		double mu_acosb = 0.00000002387246834;
		double mu_b = 0.00001234;
		
		Matrix vtan = AstrometryUtils.getTangentialVelocityVector(1.0, ra, dec, mu_acosb, mu_b);
		double[] motions = AstrometryUtils.getProperMotionsFromTangentialVelocity(1.0, ra, dec, vtan);
		
		System.out.println("mu[0] = "+motions[0]);
		System.out.println("mu[1] = "+motions[1]);
		
		System.out.println("PARSECS_PER_YEAR_TO_KILOMETRES_PER_SECOND = "+Units.PARSECS_PER_YEAR_TO_KILOMETRES_PER_SECOND);
		System.out.println("KILOMETRES_PER_SECOND_TO_PARSECS_PER_YEAR = "+Units.KILOMETRES_PER_SECOND_TO_PARSECS_PER_YEAR);
		System.out.println("1/KILOMETRES_PER_SECOND_TO_PARSECS_PER_YEAR = "+1.0/Units.KILOMETRES_PER_SECOND_TO_PARSECS_PER_YEAR);
		
		
		
		System.out.println("Input RA/Dec (North Galactic Pole):");
		System.out.println(String.format("RA  = %f\nDec = %f", Math.toDegrees(ra), Math.toDegrees(dec)));
		
		double[] galactic = AstrometryUtils.convertPositionEqToGal(ra, dec);
		
		double lon = galactic[0];
		double lat = galactic[1];
		
		System.out.println("Converted Galactic longitude/latitude:");
		System.out.println(String.format("Longitude = %f\nLatitude  = %f", Math.toDegrees(lon), Math.toDegrees(lat)));
		
		double[] equatorial = AstrometryUtils.convertPositionGalToEq(lon, lat);
		
		if(Math.abs(ra - equatorial[0]) > 1e-5)
			throw new RuntimeException("Inconsistency in equatorial/Galactic coordinate "
					+ "conversion!\nRA in/out = "+Math.toDegrees(ra)+"/"+Math.toDegrees(equatorial[0]));
		
		if(Math.abs(dec - equatorial[1]) > 1e-5)
			throw new RuntimeException("Inconsistency in equatorial/Galactic coordinate "
					+ "conversion!\nDec in/out = "+Math.toDegrees(dec)+"/"+Math.toDegrees(equatorial[1]));
		

		
		System.out.println("\nTesting coordinate conversions");
		double[] angles = {246.63567, -35.2345, 91.2379, -194.5345, 467.3245, -725.25532};
		for(double angle : angles) {
			System.out.println("\nDecimal degrees = "+angle);
			System.out.println("            HMS = "+Arrays.toString(AstrometryUtils.radiansToHMS(Math.toRadians(angle))));
			System.out.println("            DMS = "+Arrays.toString(AstrometryUtils.radiansToDMS(Math.toRadians(angle))));
		}
		
		
		System.out.println("Testing conversion of proper motions to Galactic coordinate frame:");
		
		// Position [radians]
		ra = AstrometryUtils.hmsToRadians(10, 56, 11.57699);
		dec = AstrometryUtils.dmsToRadians(-1, 60, 27, 12.8056);
		// Proper motion [mas/yr]
		mu_acosb = -4.861;   // AG Car
		mu_b = 1.923;        // AG Car
//		double mu_acosb = -4.861;
//		double mu_b = 1.923;
		
		// Parallax [mas] (AG Car)
//		double pi = 0.40003344373316;
//		double sigma_pi = 0.22482879501352;
		
		// Parallax [mas] (Hen 3-519)
		double pi = 0.79583619734035;
		double sigma_pi = 0.57542095456229;
		
		
		System.out.println("Distance from parallax = "+DistanceFromParallax.getDistance(pi, sigma_pi, METHOD.EXP_DEC_VOLUME_DENSITY_PRIOR));
		
		// Distance [pc]
		double d = 8000;
		
		// [radians/yr]
		mu_acosb *= Units.MILLIARCSEC_TO_RADIANS;
		mu_b *= Units.MILLIARCSEC_TO_RADIANS;

		double[] mu = AstrometryUtils.convertPositionAndProperMotionEqToGal(ra, dec, mu_acosb, mu_b);
		
		// Tangential velocity at given distance
		double vt_ra  = AstrometryUtils.getVtFromMuAndD(mu_acosb*Units.RADIANS_TO_ARCSEC, d);
		double vt_dec = AstrometryUtils.getVtFromMuAndD(mu_b*Units.RADIANS_TO_ARCSEC, d);
		
		System.out.println("\nInput (Equatorial frame) quantities:");
		System.out.println("ra     = "+Math.toDegrees(ra) + " [deg]");
		System.out.println("dec    = "+Math.toDegrees(dec) + " [deg]");
		System.out.println("u_ra   = "+mu_acosb*Units.RADIANS_TO_MILLIARCSEC + " [mas/yr]");
		System.out.println("u_dec  = "+mu_b*Units.RADIANS_TO_MILLIARCSEC + " [mas/yr]");
		System.out.println("vt_ra  = " + vt_ra  + " [km/s]");
		System.out.println("vt_dec = " + vt_dec  + " [km/s]");

		System.out.println("\nOutput (Galactic frame) quantities:");
		System.out.println("long    = "+Math.toDegrees(mu[0]) + " [deg]");
		System.out.println("lat     = "+Math.toDegrees(mu[1]) + " [deg]");
		System.out.println("u_long  = "+mu[2]*Units.RADIANS_TO_MILLIARCSEC + " [mas/yr]");
		System.out.println("u_lat   = "+mu[3]*Units.RADIANS_TO_MILLIARCSEC + " [mas/yr]");
		System.out.println("vt_long = " + AstrometryUtils.getVtFromMuAndD(mu[2]*Units.RADIANS_TO_ARCSEC, d)  + " [km/s]");
		System.out.println("vt_lat  = " + AstrometryUtils.getVtFromMuAndD(mu[3]*Units.RADIANS_TO_ARCSEC, d)  + " [km/s]");
		
		
		Matrix N = AstrometryUtils.getNormalFrame(ra, dec);
		Matrix A = AstrometryUtils.getProjectionMatrixA(ra, dec);
		
		N.print(5, 3);
		A.print(5, 3);
		
		
		
	}
	
}
