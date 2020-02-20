package projects.gaia.util;

import numeric.stats.Gaussian;
import numeric.stats.StatUtil;

/**
 * Class fits a two component Gaussian (1D) mixture model to the datapoints.
 *
 *
 * @author nrowell
 * @version $Id$
 */
public class Gaussian1DMixtureModelFitter {

	public double[] data;
	
	// Mixture
	public double weight;
	
	// Parameters of Gaussian component 1
	public double mean_1;
	public double std_1;
	
	// Parameters of Gaussian component 2
	public double mean_2;
	public double std_2;
	
	/**
	 * 
	 * @param x
	 * 	The coordinates of the data points.
	 */
	public Gaussian1DMixtureModelFitter(double[] x) {
		data = new double[x.length];
		System.arraycopy(x, 0, data, 0, x.length);
	}
	
	/**
	 * Use the expectation maximisation algorithm to fit the two component Gaussian mixture model.
	 */
	public void invoke() {
		
		// Step 1) - get reasonable starting values for the means and standard deviations of the two components
		
		weight = 0.5;
		
		// Sample covariance
		double s = StatUtil.getSampleCovariance(data);
		
		std_1 = std_2 = Math.sqrt(s);
		
		mean_1 = StatUtil.getRandomElement(data);
		while((mean_2 = StatUtil.getRandomElement(data)) == mean_1) {
			// Iterate until mean_1 and mean_2 are different random elements.
		}
		
		// Iterative part of EM algorithm
		
		for(int j=0; j<100; j++) {
		
			// Expectation step
			double[] exp = new double[data.length];
			
			for(int i=0; i<data.length; i++) {
				
				double p1 = weight * Gaussian.phi(data[i], mean_1, std_1);
				double p2 = (1.0 - weight) * Gaussian.phi(data[i], mean_2, std_2);
				
				exp[i] = p2 / (p1 + p2);
			}
			
			// Maximisation step
			double a=0.0, b=0.0, c=0.0, d=0.0, e=0.0, f=0.0;
			
			for(int i=0; i<data.length; i++) {
				a += (1 - exp[i]) * data[i];
				b += (1 - exp[i]);
				c += exp[i] * data[i];
				d += exp[i];
			}
			
			mean_1 = a/b;
			mean_2 = c/d;
			
			for(int i=0; i<data.length; i++) {
				e += (1 - exp[i]) * (data[i] - mean_1) * (data[i] - mean_1);
				f += exp[i] * (data[i] - mean_2) * (data[i] - mean_2);
			}
			
			std_1 = Math.sqrt(e/b);
			std_2 = Math.sqrt(f/d);
			
			weight = d / data.length;
		
			System.out.println("["+j+"] - " + weight + "\t" + mean_1 + "\t" + std_1 + "\t" + mean_2 + "\t" + std_2);
			
		}
	}
}
