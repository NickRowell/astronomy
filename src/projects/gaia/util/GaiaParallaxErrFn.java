package projects.gaiawd.util;

import numeric.functions.MonotonicLinear;
import numeric.integration.IntegrableFunction;

/**
 * Class provides a function to model the estimated parallax standard error for stars
 * in the Gaia catalogue DR2.
 * 
 * @author nrowell
 * @version $Id$
 */
public class GaiaParallaxErrFn implements IntegrableFunction {
	
	/**
	 * Apparent magnitude coordinates [G]
	 */
	static double[] g = {15, 18, 20};
	
	/**
	 * Corresponding parallax standard error [mas]
	 */
	static double[] sigPi = {0.03, 0.15, 0.7};
	
	/**
	 * Function used to interpolate values of the parallax error within the specified magnitude
	 * range.
	 */
	MonotonicLinear gaiaParallaxErrFn;
	
	/**
	 * Main constructor.
	 */
	public GaiaParallaxErrFn() {
		gaiaParallaxErrFn = new MonotonicLinear(g, sigPi);
	}

	@Override
	public double evaluate(double x) {
		if(x < g[0]) {
			return sigPi[0];
		}
		// Extrapolate the parallax error to larger values at faint magnitudes
//		else if (x > g[g.length - 1]) {
//			return sigPi[g.length - 1];
//		}
		else {
			return gaiaParallaxErrFn.getY(x)[0];
		}
	}

}
