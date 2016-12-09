package astrometry;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.analysis.solvers.LaguerreSolver;
import org.apache.commons.math3.complex.Complex;

/**
 * Class implements various distance-from-parallax algorithms as described in
 * 
 * "Estimating Distances from Parallaxes", Coryn A.L. Bailer-Jones
 * 
 * The different options amount to different assumptions about the prior distribution of
 * distances.
 *
 *
 * @author nrowell
 * @version $Id$
 */
public class DistanceFromParallax {
	
	/**
	 * Enumerates the available types of distance estimation method.
	 */
	public enum METHOD {
		NAIVE("Naive"),
		IU_PRIOR("Improper Uniform"),
		PU_PRIOR("Proper Uniform"),
		CONSTANT_VOLUME_DENSITY_PRIOR("Constant volume density"),
		EXP_DEC_VOLUME_DENSITY_PRIOR("Exp. truncated volume density");
		
		String name;
		
		METHOD(String name) {
			this.name = name;
		}
		
		public String toString() {
			return name;
		}
	}
	
	/**
	 * Convert the parallax and standard error to a distance estimate by a variety of methods.
	 * @param p
	 * 	The measured parallax [arcseconds]
	 * @param sigma_p
	 * 	The standard deviation on the measured parallax [arcseconds]
	 * @param method
	 * 	The distance estimation method
	 * @return
	 * 	The distance [pc]
	 */
	public static double getDistance(double p, double sigma_p, METHOD method) {
		switch(method) {
		case NAIVE: return getDistanceFromParallaxNaive(p);
		case IU_PRIOR: return getDistanceImproperUniformPrior(p);
		case PU_PRIOR: return getDistanceProperUniformPrior(p);
		case CONSTANT_VOLUME_DENSITY_PRIOR: return getDistanceConstantVolumeDensityPrior(p, sigma_p);
		case EXP_DEC_VOLUME_DENSITY_PRIOR: return getDistanceExpDecreasingVolumeDensityPrior(p, sigma_p);
		default:
			throw new RuntimeException("Unrecognized distance method: "+method.toString());
		}
	}
	
	/**
	 * Naive distance-from-parallax, severely biased when the fractional parallax error
	 * is significant.
	 * 
	 * @param p
	 * 	The measured parallax [arcseconds]
	 * @return
	 * 	The distance [pc]
	 */
	public static double getDistanceFromParallaxNaive(double p) {
		return 1.0/p;
	}
	
	/**
	 * Returns the mode of the distance posterior distribution, obtained adopting a distance
	 * prior that is 1 for d>0 and 0 otherwise (an 'improper uniform prior'). The mode is the
	 * only reasonable estimator of the distance posterior distribution, as all the moments are
	 * undefined.
	 * 
	 * @param p
	 * 	The measured parallax [arcseconds]
	 * @return
	 * 	The distance [pc]
	 */
	public static double getDistanceImproperUniformPrior(double p) {
		if(p>0) {
			return 1.0/p;
		}
		else {
			return Double.POSITIVE_INFINITY;
		}
	}
	
	/**
	 * Returns the mode of the distance posterior distribution, obtained adopting a distance
	 * prior that is 1/d_lim for 0<d<d_lim and 0 otherwise (a 'proper uniform prior').
	 * 
	 * @param p
	 * 	The measured parallax [arcseconds]
	 * @return
	 * 	The distance [pc]
	 */
	public static double getDistanceProperUniformPrior(double p) {
		
		// Distance at which to truncate the prior [pc]
		double r_lim = 1500.0;
		
		if(p<=0) {
			return r_lim;
		}
		
		double r = 1.0/p;
		
		if(r <= r_lim) {
			return r;
		}
		else {
			return r_lim;
		}
	}
	
	/**
	 * Returns the mode of the distance posterior distribution, obtained adopting a distance
	 * prior based on a constant stellar volume density.
	 * 
	 * @param p
	 * 	The measured parallax [arcseconds]
	 * @param sigma_p
	 * 	The standard deviation on the measured parallax [arcseconds]
	 * @return
	 * 	The distance [pc]
	 */
	public static double getDistanceConstantVolumeDensityPrior(double p, double sigma_p) {

		// Distance at which to truncate the prior [pc]
		double r_lim = 1500.0;
		
		// The fractional parallax error
		double f = sigma_p / p;
		
		// Mode of the distance
		double r_mode = (1.0/p)*(1.0/(4.0*f*f))*(1.0 - Math.sqrt(1 - 8*f*f));
		
		if(p <= 0) {
			return r_lim;
		}
		
		if(f < 1.0/Math.sqrt(8)) {
			if(r_mode > r_lim) {
				return r_lim;
			}
			else {
				return r_mode;
			}
		}
		else {
			return r_lim;
		}
		
	}
	
	/**
	 * Returns the mode of the distance posterior distribution, obtained adopting a distance
	 * prior based on an exponentially decreasing stellar volume density.
	 * 
	 * @param p
	 * 	The measured parallax [arcseconds]
	 * @param sigma_p
	 * 	The standard deviation on the measured parallax [arcseconds]
	 * @return
	 * 	The distance [pc]
	 */
	public static double getDistanceExpDecreasingVolumeDensityPrior(double p, double sigma_p) {
		
		// Length scale for exponential cutoff [parsecs]
		final double L = 5000.0;
		
		// Threshold for floating point zero value: complex roots whose imaginary part is less than
		// this are determined to be real, to within machine precision.
		final double FLOATING_POINT_ZERO_THRESHOLD = 1e-9;
		
		// The solution for the mode of the distance distribution involves finding the
		// roots of a cubic polynomial a3*r^3 + a2*r^2 + a1*r + a0 = 0
		double a3 = 1.0/L;
		double a2 = -2.0;
		double a1 = p/(sigma_p*sigma_p);
		double a0 = -1.0/(sigma_p*sigma_p);
		
		// Use Apache Commons Math LaguerreSolver to solve for complex roots
		// of the cubic polynomial
		LaguerreSolver laguerreSolver = new LaguerreSolver();
		
		Complex[] roots = laguerreSolver.solveAllComplex(new double[]{a0,a1,a2,a3}, 0.0);
		
		List<Double> realRoots = new LinkedList<>();
		
		for(Complex root : roots) {
			if(Math.abs(root.getImaginary()) < FLOATING_POINT_ZERO_THRESHOLD) {
				realRoots.add(root.getReal());
			}
		}
		
		// Sanity check - there should never be zero or two real roots
		if(realRoots.size()==0 || realRoots.size()==2) {
			
			StringBuilder message = new StringBuilder();
			
			message.append("Found "+realRoots.size()+" real roots! {");
			for(Complex root : roots) {
				message.append(root.toString()+", ");
			}
			message.append("}");
			throw new RuntimeException(message.toString());
		}
		
		// Extract the mode from the real roots
		double r_mode = 0.0;
		
		if(realRoots.size()==1) {
			r_mode = realRoots.get(0);
		}
		else {
			// Case of 3 real roots
			
			if(p>=0) {
				// All roots should be positive: select the smallest as the mode
				double min_root = Double.MAX_VALUE;
				for(Double realRoot : realRoots) {
					if(realRoot < 0.0) {
						// Sanity check
						throw new RuntimeException("Found negative real root: "+realRoot);
					}
					min_root = Math.min(min_root, realRoot);
				}
				r_mode = min_root;
			}
			else {
				// Only one root should be positive: select it as the mode
				double positive_root = 0.0;
				
				int nPos = 0;
				int nNeg = 0;
				for(Double realRoot : realRoots) {
					if(realRoot < 0.0) {
						nNeg++;
					}
					else {
						nPos++;
						positive_root = realRoot;
					}
				}
				// Sanity check
				if(nPos != 1) {
					throw new RuntimeException("Found "+nPos+" positive roots!");
				}
				// Sanity check
				if(nNeg != 2) {
					throw new RuntimeException("Found "+nNeg+" negative roots!");
				}
				r_mode = positive_root;
			}
		}
		
		return r_mode;
	}
	
}