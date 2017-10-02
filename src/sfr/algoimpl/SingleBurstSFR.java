package sfr.algoimpl;

import sfr.algo.BaseSfr;

/**
 * Class represents a single short burst of star formation. This is functionally equivalent to the {@link algoimpl#ConstantSFR}
 * model except it's parameterised differently in the constructor.
 * 
 */
public class SingleBurstSFR extends ConstantSFR {
    
    /**
     * Default constructor.
     */
    public SingleBurstSFR() {
    	super();
    	t_max = 9e9;
    	t_min = 8.9e9;
    }
    
    /**
     * Main constructor.
     * 
     * @param burstOnset
     * 	Lookback time at the onset of the burst [yr]
     * @param burstDuration
     * 	Duration of the burst [yr]
     * @param rate
     * 	The star formation rate during the burst [N/yr]
     */
    public SingleBurstSFR(double burstOnset, double burstDuration, double rate) {
    	t_max = burstOnset;
    	t_min = burstOnset - burstDuration;
    	this.rate = rate;
    }

	/**
     * {@inheritDoc}
     */
	@Override
	public double[] getParams() {
		return new double[]{rate, t_min, t_max};
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setParams(double[] params) {
		rate = params[0];
		t_min = params[1];
		t_max = params[2];
	}
	
    /**
     * {@inheritDoc}}
     */
	@Override
	public BaseSfr copy() {
		double burstOnset = t_max;
		double burstDuration = t_max - t_min;
		return new SingleBurstSFR(burstOnset, burstDuration, this.rate);
	}
	
    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
    	return "Single Burst";
    }
}