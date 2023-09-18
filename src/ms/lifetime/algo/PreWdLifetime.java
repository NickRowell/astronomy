package ms.lifetime.algo;

/**
 * Defines the interfaces for classes that provide pre- white dwarf lifetimes as a function of
 * mass and metallicity.
 * 
 * @author nrowell
 * @version $Id$
 */
public interface PreWdLifetime {

	/**
	 * Returns a short (one or two word) description of the model set, suitable for
	 * use in e.g. drop-down menus.
	 * 
	 * @return
	 * 	A short (one or two word) description of the model set.
	 */
	public String getName();
	
	/**
	 * Get the total pre-WD lifetime for the given metallicity and stellar mass, and the first derivative
	 * with respect to the mass.
	 * 
	 * @param z
	 * 	The metallicity Z.
	 * @param y
	 * 	The helium content Y.
	 * @param mass
	 * 	The stellar mass [M_{Solar}]
	 * @return
	 * 	The pre-WD lifetime [yr] for the given metallicity and stellar mass, and the first derivative
	 * with respect to the mass.
	 */
	public double[] getPreWdLifetime(double z, double y, double mass);
	
	/**
	 * Get the stellar mass for the given metallicity and pre-WD lifetime, and the first derivative
	 * with respect to the pre-WD lifetime.
	 * 
	 * @param z
	 * 	The metallicity Z.
	 * @param y
	 * 	The helium content Y.
	 * @param lifetime
	 * 	The pre-WD lifetime [yr]
	 * @return
	 * 	The stellar mass for the given metallicity and pre-WD lifetime [M_{Solar}], and the first
	 * derivative with respect to the pre-WD lifetime.
	 */
	public double[] getStellarMass(double z, double y, double lifetime);
	
}