package ifmr.algoimpl;

import ifmr.algo.BaseIfmr;

/**
 * Initial-Final mass relation from:
 * 
 * Kalirai, Hansen, Kelson, Reitzel, Rich & Richer ApJ 676:594-609 (2008).
 * 
 * @author nickrowell
 */
public class Ifmr_Kalirai2008 extends BaseIfmr {

	/**
	 * Parameter A of linear model mf = A*(mi) + B
	 */
	private double A = 0.109;
	
	/**
	 * Parameter B of linear model mf = A*(mi) + B
	 */
	private double B = 0.428;
	
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
    public String toString(){ return "Kalirai et al. (2008)";}
}
