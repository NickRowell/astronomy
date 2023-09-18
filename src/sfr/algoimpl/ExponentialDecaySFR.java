package sfr.algoimpl;

import infra.os.OSChecker;
import sfr.algo.BaseSfr;

/**
 * Class represents an exponentially decaying star formation rate model.
 * 
 * The star formation rate at a given (positive) lookback time 't' is:
 * 
 * rate = r0 * exp((t - t_max)/lambda)
 * 
 */
public class ExponentialDecaySFR extends BaseSfr {
    
	/**
	 * Initial star formation rate [N/yr]
	 */
    public double r0;
    
    /**
     * Decay constant [yr]
     */
    public double lambda;
    
    /**
     * Default constructor.
     */
    public ExponentialDecaySFR() {
    	r0 = 5E-12;
    	lambda = 1E9;
    }
    
    /**
     * Main constructor.
     * 
     * @param t_min
     * 	Minimum lookback time [yr]
     * @param t_max
     * 	Maximum lookback time [yr]
     * @param r0
     * 	The initial star formation rate [N/yr]
     */
    public ExponentialDecaySFR(double t_min, double t_max, double r0, double lambda) {
    	super(t_min, t_max);
    	
    	// Sanity checks...
    	if(!(lambda < 0.0)) {
    		throw new IllegalArgumentException("Decay constant must be negative! Found "+lambda);
    	}
    	if(!(r0 >= 0.0)) {
    		throw new IllegalArgumentException("Initial star formation rate must be non-negative! Found "+r0);
    	}
    	
    	this.r0 = r0;
    	this.lambda = lambda;
    }
    
    /**
     * {@inheritDoc}}
     */
	@Override
	public BaseSfr copy() {
		return new ExponentialDecaySFR(this.t_min, this.t_max, this.r0, this.lambda);
	}
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
    	return "Exponential Decay";
    }    
    
    /**
     * {@inheritDoc}
     */
	@Override
	public double getSFR(double t) {

		if(t >= t_min && t <= t_max) {
			return r0 * Math.exp((t - t_max)/lambda);
		}
		else {
			return 0;
		}
	}

    /**
     * {@inheritDoc}
     */
	@Override
	public double[] integrateSFR() {
		
		double mean = lambda * r0 * (1.0 - Math.exp((t_min - t_max)/lambda));
		double std = 0.0;
		
		return new double[]{mean, std};
	}

    /**
     * {@inheritDoc}
     */
	@Override
	public double drawCreationTime() {
		
		// Draw point on cumulative distribution randomly distributed in range 0:integrateSFR()
		double cdf = random.nextDouble() * integrateSFR()[0];
		
		// Solve for the lookback time at which this point is reached, by inverting the CDF
		return t_max + lambda*Math.log(cdf/(lambda * r0) + Math.exp((t_min - t_max)/lambda));
	}

    /**
     * {@inheritDoc}
     */
	@Override
	public double getMaxRate() {
		return r0;
	}
	
	/**
     * {@inheritDoc}
     */
	@Override
	public double[] getParams() {
		return new double[]{r0, lambda, t_max};
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setParams(double[] params) {
		r0 = params[0];
		lambda = params[1];
		t_max = params[2];
	}
	
    /**
     * {@inheritDoc}
     */
	@Override
	public String toString() {
		
		// Number of evenly-distributed samples to plot
		final int nSamples = 100;
		
		StringBuilder output = new StringBuilder();
		
		// Step size, so that we fit nSamples points evenly within full range (including end points)
		double dt = (t_max - t_min)/(nSamples-1);
		
		// Point at (t_min, 0)
		output = output.append(t_min).append(" ").append(0).append(" ").append(0).append(OSChecker.newline);
		
		for(int i=0; i<nSamples; i++) {
			double time = t_min + i*dt;
			double rate = getSFR(time);

			output = output.append(time).append(" ").append(rate).append(" ").append(0).append(OSChecker.newline);
			
		}
		// Point at (t_max, 0)
		output = output.append(t_max).append(" ").append(0).append(" ").append(0).append(OSChecker.newline);
		
		return output.toString();
	}

    
}