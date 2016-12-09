package sdss.footprint;

import Jama.Matrix;

/**
 * Constants related to the Sloan Digital Sky Survey.
 *
 *
 * @author nrowell
 * @version $Id$
 */
public class Constants {
	
	/**
	 * Parameters of the transformation from the equatorial to survey coordinate triad.
	 */
	
	/**
	 * Equatorial right ascension of the survey coordinates north pole.
	 */
	public static final double ra_NSP = Math.toRadians(95.0);
	
	/**
	 * Equatorial declination of the survey coordinates north pole.
	 */
	public static final double dec_NSP = Math.toRadians(0.0);
	
	/**
	 * Equatorial right ascension of the survey coordinates prime meridian.
	 */
	public static final double ra_eta0 = Math.toRadians(185.0);
	
	/**
	 * Equatorial declination of the survey coordinates prime meridian.
	 */
	public static final double dec_eta0 = Math.toRadians(32.5);
	
	public static final double[][] stn = {{Math.cos(dec_eta0)*Math.cos(ra_eta0),Math.cos(dec_eta0)*Math.sin(ra_eta0),Math.sin(dec_eta0)},
			         {Math.sin(ra_NSP)*Math.sin(dec_eta0),-Math.cos(ra_NSP)*Math.sin(dec_eta0),
                                   Math.cos(dec_eta0)*(Math.cos(ra_NSP)*Math.sin(ra_eta0) - Math.sin(ra_NSP)*Math.cos(ra_eta0))},
			         {Math.cos(ra_NSP),Math.sin(ra_NSP),0.0}};

	/**
	 * Transformation matrix from equatorial to survey coordinate triad.
	 */
	public static final Matrix STN = new Matrix(stn);
	
	
}
