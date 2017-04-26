package sfr.algoimpl;

import infra.os.OSChecker;
import sfr.algo.BaseSfr;

/**
 * Class represents a constant star formation rate model.
 * 
 */
public class ConstantSFR extends BaseSfr {
	
    /**
     * Star formation rate [N/yr]
     */
    public double rate;
    
    /**
     * Default constructor.
     */
    public ConstantSFR() {
    	rate = 1.5E-12;
    }
    
    /**
     * Main constructor.
     * 
     * @param t_min
     * 	Minimum lookback time [yr]
     * @param t_max
     * 	Maximum lookback time [yr]
     * @param rate
     * 	The star formation rate [N/yr]
     */
    public ConstantSFR(double t_min, double t_max, double rate) {
    	super(t_min, t_max);
    	this.rate = rate;
    }
    
    /**
     * {@inheritDoc}}
     */
	@Override
	public BaseSfr copy() {
		return new ConstantSFR(this.t_min, this.t_max, this.rate);
	}
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
    	return "Constant";
    }
    
    /**
     * {@inheritDoc}
     */
	@Override
	public double getSFR(double t) {
		if(t >= t_min && t <= t_max) {
			return rate;
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
		double mean = (t_max - t_min) * rate;
		double std = 0.0;
		return new double[]{mean, std};
	}

    /**
     * {@inheritDoc}
     */
	@Override
	public double drawCreationTime() {
		return t_min + (t_max - t_min) * rng.nextDouble();
	}

    /**
     * {@inheritDoc}
     */
	@Override
	public String toString() {
		
		StringBuilder output = new StringBuilder();
		
		// Point at (t_min, 0)
		output.append(t_min).append(" ").append(0).append(" ").append(0).append(OSChecker.newline);
		
		// Point at (t_min, rate)
		output.append(t_min).append(" ").append(rate).append(" ").append(0).append(OSChecker.newline);

		// Point at (t_max, rate)
		output.append(t_max).append(" ").append(rate).append(" ").append(0).append(OSChecker.newline);
		
		// Point at (t_max, 0)
		output.append(t_max).append(" ").append(0).append(" ").append(0).append(OSChecker.newline);
		
		return output.toString();
	}

    /**
     * {@inheritDoc}
     */
	@Override
	public double getMaxRate() {
		return rate;
	}
}