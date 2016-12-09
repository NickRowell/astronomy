package sfr.algo;

import infra.os.OSChecker;
import numeric.data.DiscreteFunction1D;

/**
 * Base class for Star Formation Rate models that employ a discrete 1D function to represent
 * an arbitrary function.
 *
 * @author nrowell
 * @version $Id$
 */
public abstract class BaseDiscreteSfr extends BaseSfr {

	/**
	 * Internal representation of the SFR model, as a set of discrete bins.
	 */
	public DiscreteFunction1D data;
	
	/**
	 * Default constructor.
	 */
	public BaseDiscreteSfr() {
		super();
	}
	
	/**
     * Main constructor.
     * 
     * @param t_min
     * 	Minimum lookback time [yr].
     * @param t_max
     * 	Maximum lookback time [yr].
     */
	public BaseDiscreteSfr(double t_min, double t_max) {
		super(t_min, t_max);
	}
	
    /**
     * {@inheritDoc}
     */
	@Override
	public double getSFR(double t) {
        return data.getBinContents(t);
	}

    /**
     * {@inheritDoc}
     */
	@Override
	public double[] integrateSFR() {
        return data.integrate(t_min, t_max);
	}

    /**
     * {@inheritDoc}
     */
	@Override
	public double drawCreationTime() {
        return data.draw(0, t_max);
	}

    /**
     * {@inheritDoc}
     */
	@Override
	public String toString() {
        StringBuilder output = new StringBuilder();
        
        // Point at zero at the lower edge of the SFR histogram (tmin)
        output = output.append(data.getBinLowerEdge(0)).append(" ").append(0).append(" ").append(0).append(OSChecker.newline);
        
        // Loop over all SFR points
        for(int i=0; i<data.size(); i++)
        {
            output = output.append(data.getBinLowerEdge(i)).append(" ").append(data.getBinContents(i)).append(" ")
            		.append(data.getBinUncertainty(i)).append(OSChecker.newline);
            output = output.append(data.getBinUpperEdge(i)).append(" ").append(data.getBinContents(i)).append(" ")
            		.append(data.getBinUncertainty(i)).append(OSChecker.newline);
        }
        
        // Point at zero at the upper edge of the SFR histogram (onset of star formation)
        output = output.append(data.getBinUpperEdge(data.size()-1)).append(" ").append(0).append(" ").append(0).append(OSChecker.newline);
        
        return output.toString();
	}

    /**
     * {@inheritDoc}
     */
	@Override
	public double getMaxRate() {
        // Initially set maximum rate to first SFR bin
        double r_max = data.getBinContents(0);
    
        // Loop over remaining bins and try to find a higher value
        for(int i=1; i<data.size(); i++)
            if(data.getBinContents(i) > r_max)
                r_max = data.getBinContents(i);
        
        return r_max;
	}
	
}
