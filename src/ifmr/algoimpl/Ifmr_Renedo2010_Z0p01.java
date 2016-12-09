package ifmr.algoimpl;

import ifmr.algo.BaseIfmr;
import numeric.functions.MonotonicLinear;

/**
 * 
 * Initial-Final mass relation from:
 * 
 *  Renedo, Althaus, Miller Bertolami et al. 2010, ApJ, 717, 183
 * 
 * This relation is not derived empirically and instead emerges from detailed stellar models that simulate the
 * evolution of stars from the ZAMS to the WD cooling track, thus obtaining the IFMR 'for free'.
 * 
 * This implementation corresponds to the Z=0.01 models.
 * 
 * 
 * @author nickrowell
 */
public class Ifmr_Renedo2010_Z0p01 extends BaseIfmr {

	/**
	 * Initial mass points.
	 */
	static double[] mi = {1.00,  1.50,  1.75,  2.00,  2.25,   2.50,  3.00,  3.50,  4.00,  5.00};
	
	/**
	 * Corresponding final mass points. NOTE that the table of values presented in the paper (and republished
	 * on the website at http://evolgroup.fcaglp.unlp.edu.ar/TRACKS/readme_DA2010.html) rounds off the final
	 * masses to 3 decimal places which can introduce significant errors if these values are used to e.g. interpolate
	 * main sequence lifetimes. Therefore, the final WD masses from the models should be used - these are hardcoded
	 * here.
	 */
	static double[] mf = {0.52490, 0.57015, 0.59316, 0.60959, 0.63229, 0.65988, 0.70511, 0.76703, 0.83731, 0.87790};
	
	
	/**
	 * MonotonicLinear instance used to interpolate final mass at a given initial mass.
	 */
	MonotonicLinear ifmr;
	
	/**
	 * Main constructor for {@link Ifmr_Renedo2010_Z0p01}.
	 */
	public Ifmr_Renedo2010_Z0p01() {
		ifmr = new MonotonicLinear(mi, mf);
	}
	
	/**
	 * {@inheritDoc}
	 */
    @Override
    public double getMf(double mi) 
    {
        return ifmr.interpolateY(mi)[0];
    }
    
    /**
	 * {@inheritDoc}
	 */
    @Override
    public double getMi(double mf) 
    {
        return ifmr.interpolateUniqueX(mf)[0];
    }
    
    /**
	 * {@inheritDoc}
	 */
    @Override
    public double getBreakdownInitialMass() {
    	return 0.4780219780219692;
    }
    
    /**
	 * {@inheritDoc}
	 */
    @Override
    public String toString() {
    	return "Renedo et al. (2010)";
    }
    
}