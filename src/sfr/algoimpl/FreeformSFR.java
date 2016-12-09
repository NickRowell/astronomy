package sfr.algoimpl;

import numeric.data.DiscreteFunction1D;
import sfr.algo.BaseDiscreteSfr;
import sfr.algo.BaseSfr;

/**
 * Class represents a free-form star formation rate model defined by a table of
 * formation time bin centres, widths and rates. SFR is assumed to be zero over any unspecified time ranges.
 */
public class FreeformSFR extends BaseDiscreteSfr {
    
    /**
     * Default constructor;
     */
    public FreeformSFR() {
    	double[] centres = new double[]{0.5E9, 1.5E9, 2.5E9, 3.5E9, 4.5E9, 5.5E9};
    	double[] widths  = new double[]{1.0E9, 1.0E9, 1.0E9, 1.0E9, 1.0E9, 1.0E9};
    	double[] rates   = new double[]{1.5E-12, 1.0E-12, 1.5E-12, 2.5E-12, 2.0E-12, 1.5E-12};
    	double[] errors  = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0};

        data = new DiscreteFunction1D(centres, widths, rates, errors);

        // Set t_max from edge of earliest specified SFR bin
        int N = centres.length;
        t_max = centres[N - 1] + widths[N - 1] / 2.0;
        t_min = centres[0] - widths[0] / 2.0;
    }
    
    /**
     * Main constructor.
     * 
     * @param centres
     * 	Bin centres [yr]
     * @param widths
     * 	Bin widths [yr]
     * @param rates
     * 	Star formation rate per bin [N/yr]
     * @param errors
     * 	Standard error on the star formation rate per bin [N/yr]
     */
    public FreeformSFR(double[] centres, double[] widths, double[] rates, double[] errors) {
    	// Reset the t_max & t_min after checking the arguments
    	super(0,0);

        data = new DiscreteFunction1D(centres, widths, rates, errors);

        // Set t_max from edge of earliest specified SFR bin
        int N = centres.length;
        t_max = centres[N - 1] + widths[N - 1] / 2.0;
        t_min = centres[0] - widths[0] / 2.0;
    }

    /**
     * {@inheritDoc}
     */
	@Override
	public BaseSfr copy() {
		
		FreeformSFR copy = new FreeformSFR();
		copy.t_min = t_min;
		copy.t_max = t_max;
		copy.data = new DiscreteFunction1D(data);
		
		return copy;
	}
	
    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
    	return "Freeform";
    }
    
}