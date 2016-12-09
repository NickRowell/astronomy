package density;

/**
 * Represents spatially homogeneous populations, i.e. the spheroid.
 * @author nrowell
 *
 */
public class Uniform extends DensityProfile
{

	/**
	 * Uniform density profile has no parameters.
	 */
	public Uniform(){}
	
	/**
	 * Uniform population; density relative to sun equals one.
	 */
	public double getDensity(double r, double ra, double dec)
	{
		return 1.0;
	}
	
	/**
	 * Text description of profile.
	 */
	public String toString()
	{
		return "Uniform";
	}
	
}
