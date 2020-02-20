package sandbox;

import astrometry.util.AstrometryUtils;
import constants.Galactic;

/**
 * Class is used to locate the Galactic longitude of the ascending node of the equatorial plane.
 *
 *
 * @author nrowell
 * @version $Id$
 */
public class FindGalacticLongitudeOfAscendingNodeOfEquatorialPlane {
	
	
	public static void main(String[] args) {
		
		// Method works by looping over the Galactic longitude at fixed latitude=0, transforming the
		// coordinate to the equatorial frame and looking for the two points where equatorial latitude = 0.
		
		double b=0;
		
		for(double l=0; l<2*Math.PI; l+=1e-8) {
			
			double[] ra_dec = AstrometryUtils.convertPosition(l, b, Galactic.r_E_G);
			
			double dec = ra_dec[1];
			
			if(Math.abs(dec) < 1e-8) {
				System.out.println("Node at Galactic longitude " + Math.toDegrees(l) + " [deg]");
			}
		}
	}
	
}