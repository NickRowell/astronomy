package astrometry.test;

import java.util.Random;

import astrometry.DistanceFromParallax;

/**
 * Class tests the various methods for distance-from-parallax.
 *
 *
 * @author nrowell
 * @version $Id$
 */
public class TestDistanceFromParallax {
	
	/**
	 * Main application entry point.
	 * @param args
	 * 	The command line args (ignored)
	 */
	public static void main(String[] args) {
		
		// Input parameters
		
		// True distance [parsecs]
		double r_true = 100;
		
		// Parallax error standard deviation [arcseconds]
		double sigma_p = 0.001;
		
		// Derived parameters
		
		// True parallax [arcseconds]
		double w_true = 1.0/r_true;
		
		Random random = new Random(System.currentTimeMillis());
		
		
		// Loop over noisy realisation of the observed parallax
		for(int i=0; i<100; i++) {
			
			// Get observed parallax
			double w_obs = w_true + random.nextGaussian()*sigma_p;
			
			// Get various distance estimates
			double distance0 = DistanceFromParallax.getDistanceFromParallaxNaive(w_obs);
			double distance1 = DistanceFromParallax.getDistanceImproperUniformPrior(w_obs);
			double distance2 = DistanceFromParallax.getDistanceProperUniformPrior(w_obs);
			double distance3 = DistanceFromParallax.getDistanceConstantVolumeDensityPrior(w_obs, sigma_p);
			double distance4 = DistanceFromParallax.getDistanceExpDecreasingVolumeDensityPrior(w_obs, sigma_p);
			
			System.out.println("Distance estimates ("+i+")");
			System.out.println("++++++++++++++++++++++++");
			System.out.println(" -                 Naive estimate: "+distance0);
			System.out.println(" -         Improper uniform prior: "+distance1);
			System.out.println(" -           Proper uniform prior: "+distance2);
			System.out.println(" -           Costant volume prior: "+distance3);
			System.out.println(" - Exponentially decreasing prior: "+distance4);
			System.out.println();
		}
		
		
		
	}
	
}
