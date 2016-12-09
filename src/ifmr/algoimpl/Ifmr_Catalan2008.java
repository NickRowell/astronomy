package ifmr.algoimpl;

import ifmr.algo.BaseIfmr;

/**
 * 
 * Initial-Final mass relation from:
 * 
 * Catalan, Isern, Garcia-Berro & Ribas MNRAS 387:1693-1706 (2008).
 * 
 * TODO: properly implement the high mass clamping in the inverse IFMR.
 * 
 * @author nickrowell
 */
public class Ifmr_Catalan2008 extends BaseIfmr {

	/**
	 * {@inheritDoc}
	 */
    @Override
    public double getMf(double mi) 
    {
        
        // Two component linear model, artificially limited to mf = 1.2M_0
        // because WD models don't go any higher than this.
        
        if(mi <= 2.7)
            // Low mass component
            return 0.096 * mi + 0.429;
        else
            // High mass component, clamped to 1.2M_0
            return Math.min(1.2, 0.137 * mi + 0.318);
    }
    
    /**
	 * {@inheritDoc}
	 */
    @Override
    public double getMi(double mf) 
    {
        
        // Limit range of WD mass to < 1.2 M_0
        if(mf > 1.2) mf = 1.2;
        
        if(mf > getMf(2.7))
            // High mass component
            return (mf - 0.318)/0.137;
        else
            // Low mass component
            return (mf - 0.429)/0.096;
    }
    
    /**
	 * {@inheritDoc}
	 */
    @Override
    public double getBreakdownInitialMass(){ return 0.4745575221;}
    
    /**
	 * {@inheritDoc}
	 */
    @Override
    public String toString(){ return "Catalan et al. (2008)";}

}
