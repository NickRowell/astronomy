package projections;

/**
 * Aitoff projection. See
 * 
 * https://en.wikipedia.org/wiki/Aitoff_projection
 *
 *
 * @author nrowell
 * @version $Id$
 */
public class Aitoff extends Projection{

	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		return "Aitoff";
	}
	
	/**
	 * Implements the Aitoff projection equations. See e.g.
	 * 
	 * https://en.wikipedia.org/wiki/Aitoff_projection
	 * 
	 */
	@Override
	public double[] getForwardProjection(double[] in) {
		
		// Right ascension/longitude etc. Angular distance about equator from central meridian.
		double ra  = in[0];
		
		// Right Ascension values larger than 180 degrees (or more formally, the value of the central
		// meridian + 180) need to be mapped to the equivalent negative angle in the range [-PI:0]
		if(ra > Math.PI)
			ra -= 2.0*Math.PI;
		
		// Declination/latitude etc. Angular distance perpendicular to equator.
		double dec = in[1];
		
		// Aitoff projection equations
		double alpha = Math.acos( Math.cos(dec) * Math.cos(ra/2.0));
		
		// Careful for discontinuity in sinc function
		double sincA = (alpha==0 ? 1 : Math.sin(alpha)/alpha);
		
		double x = 2.0 * Math.cos(dec) * Math.sin(ra/2.0) / sincA;
		double y = Math.sin(dec) / sincA;
		
		return new double[]{x,y};
	}

	
	
}
