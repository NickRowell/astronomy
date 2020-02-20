package utils;

/**
 * Utilities related to metallicity calculations.
 *
 * Good references:
 *  "The Chemical Composition of the Sun"
 *   Asplund, Martin; Grevesse, Nicolas; Sauval, A. Jacques; Scott, Pat
 *   Annual Review of Astronomy & Astrophysics, vol. 47, Issue 1, pp.481-522
 *
 *
 *
 * @author nrowell
 * @version $Id$
 */
public final class MetallicityUtils {
	
	/**
	 * Private constructor to enforce non-instantiability.
	 */
	private MetallicityUtils() {
		
	}
	
	/**
	 * Solar Hydrogen mass fraction.
	 * 
	 * From:
	 * "The solar chemical composition"
	 * M. Asplund, N. Grevesse, & A. J. Sauval 2004, astro-ph/0410214 v2
	 * 
	 * @see <a href="http://arxiv.org/pdf/astro-ph/0410214v2.pdf">http://arxiv.org/pdf/astro-ph/0410214v2.pdf</a>
	 * @see <a href="http://astro.wsu.edu/models/calc/asplund04.txt">http://astro.wsu.edu/models/calc/asplund04.txt</a>
	 */
	public static final double solarX = 0.738253;

	/**
	 * Solar helium content.
	 * See {@link MetallicityUtils#solarX}
	 */
	public static final double solarY = 0.249524653;
	
	/**
	 * Solar metallicity value.
	 * See {@link MetallicityUtils#solarX}
	 */
	public static final double solarZ = 0.0122220116;
	
	
	/**
	 * Get the Helium content (Y) corresponding to the given metallicity (Z)
	 * value, using a canonical relationship.
	 * 
	 * This relation assumes that the helium and metal abundance is related
	 * through a constant factor.
	 * 
	 * Casagrande 2007.
	 * 
	 * TODO: get reference for this.
	 * 
	 * 
	 * @param z
	 * 	The metallicity Z
	 * @return
	 * 	The Helium content Y
	 */
	public static double getYFromZ(double z) {
		return 0.23 + 2.41 * z;
	}
	
	/**
	 * Convert the [Fe/H] value to the corresponding Z value using a
	 * canonical relationship.
	 * 
	 * TODO: find the canonical relationship for this.
	 * 
	 * @param feH
	 * 	The [Fe/H] value
	 * @return
	 * 	The corresponding metallicity (Z) value
	 */
	public static double getZFromFeH(double feH) {
		return 0.0;
	}
	
	
	
}
