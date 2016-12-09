package density;

/**
 * Defines the methods required for classes representing the density
 * profile of a stellar population, namely those methods relating to
 * the stellar density at a particular point in space.
 * 
 * @author nrowell
 *
 */
public abstract class DensityProfile
{
	
	/**
	 * Returns the stellar density at the given distance along the
	 * LOS, relative to that at the Sun.
	 * @param ra	Right ascension towards line of sight [radians]
	 * @param dec	Declination towards line of sight [radians]
	 * @param r		Range along line of sight [parsecs]
	 * @return
	 */
	public abstract double getDensity(double r, double ra, double dec);
	
	/**
	 * Forces extending classes to override this method and provide a
	 * meaningful description of themselves.
	 */
	public abstract String toString();
	
	
}
