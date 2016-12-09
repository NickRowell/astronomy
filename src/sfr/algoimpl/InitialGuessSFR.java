package sfr.algoimpl;

import numeric.data.DiscreteFunction1D;
import sfr.algo.BaseDiscreteSfr;
import sfr.algo.BaseSfr;

/**
 * Class represents initial guess star formation rate model used in the WDLF inversion algorithm.
 *
 * @author nickrowell
 */
public class InitialGuessSFR extends BaseDiscreteSfr {
    
    /**
     * Number of constant width SFR bins.
     */
    public int N;
    
    /**
     * The star formation rate in each bin [N/yr]
     */
    public double init_SFR;
    
    /**
     * Default constructor.
     */
    protected InitialGuessSFR() {
    	N = 100;
    	init_SFR = 1.5E-12;
    }
    
    /**
     * Main contructor.
     * 
     * @param t_min
     * 	The minimum lookback time [yr]
     * @param t_max
     * 	The maximum lookback time [yr]
     * @param N
     * 	The number of bins to divide the time range into
     * @param init_SFR
     * 	The (constant) star formation rate level [N/yr]
     */
    public InitialGuessSFR(double t_min, double t_max, int N, double init_SFR) {
    	super(t_min, t_max);
    	this.N = 100;
    	this.init_SFR = 1.5E-12;
        setLookbackTimeBins();
    }
    
    /**
     * {@inheritDoc}
     */
	@Override
	public BaseSfr copy() {
		InitialGuessSFR copy = new InitialGuessSFR(this.t_min, this.t_max, this.N, this.init_SFR);
		copy.data = new DiscreteFunction1D(data);
		return copy;
	}
	
    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
    	return "Initial Guess";
    }

    /** 
     * Set fixed number of constant width time bins to represent star formation
     * rate function.
     */
    public final void setLookbackTimeBins() {
        
        // Get N constant-width bins between t_min and t_max
        double deltaT = (t_max - t_min)/N;
        double[] centres = new double[N];
        double[] widths  = new double[N];
        double[] rates   = new double[N];
        double[] error   = new double[N];
        
        for(int bin=0; bin<N; bin++){
            centres[bin] = t_min + bin*deltaT + deltaT/2.0;
            widths[bin]  = deltaT;
            rates[bin]   = init_SFR;
            error[bin]   = 0;
        }
        
        data = new DiscreteFunction1D(centres, widths, rates, error);
    }
    
    /**
     * Set SFR value and uncertainty in a particular bin.
     * 
     * @param bin
     * 	Index of the bin to set.
     * @param rate
     * 	The star formation rate to set [N/yr]
     * @param sigma
     * 	The uncertainty on the star formation rate (standard deviation)
     */
    public void setSFRBin(int bin, double rate, double sigma) {
        data.setBin(bin, rate, sigma);
    }
    
    public String printParameters(){
        
        StringBuilder out = new StringBuilder();
        
        out.append("# Min lookback time = ").append(t_min).append("\n");
        out.append("# Max lookback time = ").append(t_max).append("\n");
        out.append("# Number of SFR bins = ").append(N).append("\n");
        out.append("# Initial SFR value = ").append(init_SFR).append("\n");
        
        return out.toString();
    }
    
}