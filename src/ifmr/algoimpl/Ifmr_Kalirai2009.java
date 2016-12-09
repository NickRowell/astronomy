package ifmr.algoimpl;

import ifmr.algo.BaseIfmr;

/**
 * Initial-Final mass relation from:
 * 
 * Kalirai, Davis, Richer, Bergeron, Catelan, Hansen & Rich ApJ 705:408-425 (2009). 
 * 
 * @author nickrowell
 */
public class Ifmr_Kalirai2009 extends BaseIfmr {
	
	/**
	 * Parameter A of linear model mf = A*(mi) + B
	 */
	private double A = 0.101;
	
	/**
	 * Parameter B of linear model mf = A*(mi) + B
	 */
	private double B = 0.463;
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
    public double getMf(double mi) { return A * mi + B;}
	
	/**
	 * {@inheritDoc}
	 */
    @Override
    public double getMi(double mf) { return (mf - B) / A;}
    
    /**
     * From the solution of A * mi + B = mi; i.e. getMf(mi) = mi.
	 * {@inheritDoc}
	 */
    @Override
    public double getBreakdownInitialMass(){ return -B/(A-1);}
    
    /**
	 * {@inheritDoc}
	 */
    @Override
    public String toString(){ return "Kalirai et al. (2009)";}
	    
}
