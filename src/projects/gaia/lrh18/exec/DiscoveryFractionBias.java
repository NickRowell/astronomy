package projects.gaia.lrh18.exec;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import Jama.Matrix;
import kinematics.TangentialVelocityDistribution;
import numeric.functions.PositiveLinear;

/**
 * This class provides an application that computes the average discovery fraction across the entire sky,
 * given a velocity dispersion tensor, mean solar reflex motion and tangential velocity threshold. This is
 * used to determine the error in WDLF studies that use incorrect kinematic data.
 *
 *
 * @author nrowell
 * @version $Id$
 */
public class DiscoveryFractionBias {
	
	/**
	 * RH11: Mean motion in Galactic frame [km/s].
	 */
//    static Matrix velMean = new Matrix(new double[][]{{-8.62},{-20.04},{-7.1}});
    
    /**
     * Mean motion from bin centred on 14.563
     */
    static Matrix velMean = new Matrix(new double[][]{{-10.15},{-25.80},{-6.88}});
    

    /**
     * RH11: Velocity ellipsoid in Galactic frame.
     */
//    static Matrix velDispTens = new Matrix(new double[][]{{32.4*32.4, 0,         0},
//                                                          {0,         23.0*23.0, 0},
//                                                          {0,         0,         18.1*18.1}});
    
    /**
     * Velocity dispersion tensor from bin centred on 14.563
     */
    static Matrix velDispTens = new Matrix(new double[][]{{1697.80, 299.89, -51.76},
													        { 299.89, 715.78, -58.32},
													        { -51.76, -58.32, 477.75}});
    
    /**
     * Minimum tangential velocity threshold [km s^{-1}]
     */
    static double vtanThreshold = 30.0;
    
    /**
     * Tangential velocity distributions tabulated at this resolution [km/s]
     */
	static double vt_step = 2.0;
    
	/**
     * Main application entry point.
     * 
     * @param args
     * 	The command line arguments (ignored)
     */
    public static void main(String[] args) {
    	
    	// Maximum tangential velocity to compute velocity distribution up to
    	double vt_max = 250.0;
    	
		// Number of elements in tabulated functions
		final int n_els_vt = (int)Math.ceil(vt_max/vt_step);
		
		// Array of tangential velocity values corresponding to tangential velocity PDF
		final double[] vtan = new double[n_els_vt+1];
		for(int i=0; i<vtan.length; i++)
		{
			vtan[i] = i*vt_step;
		}
		
		// Steps in right ascension and declination
		int raSteps = 180;
		int decSteps = 90;
		
		final double raStep = 2 * Math.PI / raSteps;
		final double decStep = Math.PI / decSteps;
		
		// Set up for multiple threaded processing
        final List<Future<Double>> futures = new LinkedList<Future<Double>>();
        final ExecutorService executor = Executors.newFixedThreadPool(8);
        
		for(int i = 0; i < raSteps; i++) {
			
			final double ra = i * raStep + raStep/2.0;
			
			for(int j=0; j < decSteps; j++) {
				
				final double dec = -(Math.PI / 2.0) + j * decStep + decStep/2.0;
				
				
				// Create Callable to perform the work for this time interval
				final Callable<Double> worker = new Callable<Double>() 
	            {
	                @Override
	                public Double call() throws Exception
	                {
				
						// Element of solid angle
						double dOmega = Math.cos(dec) * raStep * decStep / (4.0 * Math.PI);
						
						// Compute the tangential velocity distribution towards the given line of sight
						double[] vtan_true_pdf   = new double[n_els_vt];
						double[] vtan_true_cdf   = new double[n_els_vt+1];
						TangentialVelocityDistribution.getVtanDistributionTowardsLos(velDispTens, velMean, ra, dec, vt_step, vtan_true_pdf, vtan_true_cdf);
						
						// Create interpolation object
						PositiveLinear vtan_true = new PositiveLinear(vtan, vtan_true_cdf);
						
						// Fraction of the population with tangential velocities below the threshold
						double df_lower = vtan_true.interpolateY(vtanThreshold)[0];
						
						return new Double(df_lower * dOmega);
				
	                }
	            };
	            futures.add(executor.submit(worker));
			}
		}
		
		
		// Shutdown the execution
		executor.shutdown();

		// Progress tracking
		int count = 0;
		int nSteps = raSteps * decSteps;
        int tenPercent = nSteps/10;
        
		// Solid-angle weighted sky-average discovery fraction
		double df = 0.0;
        
		// Retrieve calibration sets from each worker thread and compile into a single list.
		for (final Future<Double> future : futures)
		{
			try {
				df += future.get();
				
				// Progress tracking
				if(count%tenPercent==0) {
	        		int nHash = count/tenPercent + 1;
	        		StringBuilder str = new StringBuilder();
	        		str.append("Progress: [");
	        		for(int k=0; k<nHash; k++) {
	        			str.append("#");
	        		}
	        		for(int k=nHash; k<10; k++) {
	        			str.append(".");
	        		}
	        		str.append("]\r");
	        		System.out.print(str.toString());
	        	}
				count++;
				
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("df = " + df);
    }
	
	
}
