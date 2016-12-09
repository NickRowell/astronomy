package utils;

import constants.Solar;

/**
 * Class provides various static methods useful for manipulating magnitudes and luminosities.
 * @author nrowell
 *
 */
public class MagnitudeUtils
{
	/**
	 * Get distance from the apparent and absolute magnitudes.
	 * @param m		Apparent magnitude
	 * @param M		Absolute magnitude
	 * @return		Distance [parsecs]
	 */
	public static double getDistance(double m, double M)
	{
		return Math.pow(10, (m-M)/5.0 + 1.0);
	}
	
	/**
	 * Get apparent magnitude from the distance and absolute magnitude.
	 * @param d		Distance [parsecs]
	 * @param M		Absolute magnitude
	 * @return		Apparent magnitude
	 */
	public static double getApparentMagnitude(double d, double M)
	{
		return M + 5*Math.log10(d) - 5;
	}
	
	/**
	 * Get absolute magnitude from the distance and apparent magnitude.
	 * @param d		Distance [parsecs]
	 * @param m		Apparent magnitude
	 * @return		Absolute magnitude
	 */
	public static double getAbsoluteMagnitude(double d, double m)
	{
		return m + 5*(1 - Math.log10(d));
	}
	
	/**
	 * Get absolute magnitude from the parallax and apparent magnitude.
	 * Note the similarity to {@link MagnitudeUtils#getAbsoluteMagnitude(double, double)};
	 * basically Math.log10(p) = -Math.log10(d) because d = 1/p.
	 * 
	 * @param p
	 * 	Parallax [arcseconds]
	 * @param m	
	 * 	Apparent magnitude
	 * @return
	 * 	Absolute magnitude
	 */
	public static double getAbsoluteMagnitudeFromPi(double p, double m)
	{
		return m + 5*(1 + Math.log10(p));
	}
	
	/**
	 * Converts a total luminosity from log(L/L_0) to bolometric magnitude.
	 * 
	 * @param logLL0
	 * 	The total luminosity expressed as the logarithm of the luminosity in Solar units.
	 * @return
	 * 	The total luminosity expressed as a bolometric magnitude.
	 */
	public static double logLL0toMbol(double logLL0)
	{
		return Solar.mbol - 2.5*logLL0;
	}
	
}
