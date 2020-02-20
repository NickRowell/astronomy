package sandbox;

import astrometry.util.AstrometryUtils;

/**
 * Class converts coordinates from degrees to HMS/DMS.
 *
 *
 * @author nrowell
 * @version $Id$
 */
public class ConvertCoordinates {

	/**
	 * Application main entry point.
	 * 
	 * @param args
	 * 	The command line args (ignored).
	 */
	public static void main(String[] args) {
		
		double ra = Math.toRadians(9.470486329223203);
		double dec = Math.toRadians(59.67009941345594);
		
		double[] hms = AstrometryUtils.radiansToHMS(ra);
		double[] dms = AstrometryUtils.radiansToDMS(dec);
		
		System.out.println("Hours   = " + hms[0]);
		System.out.println("Minutes = " + hms[1]);
		System.out.println("Seconds = " + hms[2]);
		
		System.out.println("Degrees    = " + dms[0]);
		System.out.println("Arcminutes = " + dms[1]);
		System.out.println("Arcseconds = " + dms[2]);
	}
	
}
