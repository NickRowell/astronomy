package constants.test;

import Jama.Matrix;
import constants.Galactic;
import utils.AstrometryUtils;

public class TestGalactic
{
	
	public static void main(String[] args)
	{
//		testEquatorialNormalFrames();
		testGalacticEquatorialFrames();
	}
	
	
	private static void testEquatorialNormalFrames()
	{
		// Choose a line of sight along which to compute the normal frame
		double ra  = Math.toRadians(238);
		double dec = Math.toRadians(13);
		
		// Get the Normal frame for this line of sight
		Matrix r_N_E = AstrometryUtils.getNormalFrame(ra, dec);
		
		// Now get a unit vector pointing towards this point, in the equatorial frame.
		// This coincides with the normal frame Z axis, if sums are correct.
		Matrix r_E = AstrometryUtils.sphericalPolarToCartesian(1.0, ra, dec);
		
		
		// Now, we express this vector in the Normal frame using the transformation
		//
		// r_N = r_N_E * r_E
		
		Matrix r_N = r_N_E.times(r_E);
		
		System.out.println("r_E = ");
		r_E.print(3, 3);
		
		System.out.println("r_N = ");
		r_N.print(3, 3);
		
	}
	
	
	private static void testGalacticEquatorialFrames()
	{
		// Get vector pointing towards the Galactic centre, expressed in the
		// Equatorial frame.
		Matrix r_GC_E = AstrometryUtils.sphericalPolarToCartesian(1.0, Galactic.GCra, Galactic.GCdec);
		
		
		Matrix r_V_E = AstrometryUtils.cartesianToSphericalPolar(Galactic.V);
		
		System.out.println("ra = "+Math.toDegrees(r_V_E.get(1, 0)));
		System.out.println("dec  = "+Math.toDegrees(r_V_E.get(2, 0)));
		
		
		// Now, we express this vector in the Galactic frame using the transformation
		//
		// r_GC_G = GTN * r_GC_E
		
//		Matrix r_GC_G = Galactic.r_G_E.times(r_GC_E);
//		
//		System.out.println("r_GC_E = ");
//		r_GC_E.print(3, 3);
//		
//		System.out.println("r_GC_G = ");
//		r_GC_G.print(3, 3);
		
	}
	
	
}
