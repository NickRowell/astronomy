package ifmr.algoimpl;

import ifmr.algo.BaseIfmr;

/**
 * Extension of {@link BaseIfmr} that implements the Initial-Final Mass Relation from:
 * 
 * "The White Dwarf Initial-Final Mass Relation for Progenitor Stars from 0.85 to 7.5 M⊙",
 * Cummings et al, The Astrophysical Journal, Volume 866, Issue 1, article id. 21, 14 pp. (2018).
 * 
 * @author nickrowell
 */
public class Ifmr_Cummings2018 extends BaseIfmr {
	
	/**
	 * Coefficients of segment 1 (initial mass 0.83 -> 2.85 M_{solar}, final mass 0.5554 -> 0.717 M_{solar})
	 */
	double[] a0 = {0.08, 0.489};

	/**
	 * Coefficients of segment 2 (initial mass 2.85 -> 3.60 M_{solar}, final mass 0.717 -> 0.8572 M_{solar})
	 */
	double[] a1 = {0.187, 0.184};

	/**
	 * Coefficients of segment 3 (initial mass 3.60 -> 7.20 M_{solar}, final mass 0.8572 -> 1.2414 M_{solar})
	 */
	double[] a2 = {0.107, 0.471};
	
	/**
	 * {@inheritDoc}
	 */
    @Override
    public double getMf(double mi) {
    	
    	double a = Double.NaN, b = Double.NaN;
    	
    	if(mi < 0.83) {
    		// Undefined low initial mass end; extrapolate first segment and log a warning
    		a = a0[0];
    		b = a0[1];
    	}
    	else if(mi <= 2.85) {
    		a = a0[0];
    		b = a0[1];
    	}
    	else if(mi <= 3.6) {
    		a = a1[0];
    		b = a1[1];
    	}
    	else if(mi <= 7.2) {
    		a = a2[0];
    		b = a2[1];
    	}
    	else {
    		// Undefined high initial mass end; extrapolate last segment and log a warning
    		a = a2[0];
    		b = a2[1];
    	}
    	
    	return a*mi + b;
    }
    
    /**
	 * {@inheritDoc}
	 */
    @Override
    public double getMi(double mf) {

    	double a = Double.NaN, b = Double.NaN;
    	
    	if(mf < 0.5554) {
    		// Undefined low final mass end; extrapolate first segment and log a warning
    		a = a0[0];
    		b = a0[1];
    	}
    	else if(mf <= 0.717) {
    		a = a0[0];
    		b = a0[1];
    	}
    	else if(mf <= 0.8572) {
    		a = a1[0];
    		b = a1[1];
    	}
    	else if(mf <= 1.2414) {
    		a = a2[0];
    		b = a2[1];
    	}
    	else {
    		// Undefined high final mass end; extrapolate last segment and log a warning
    		a = a2[0];
    		b = a2[1];
    	}
    	
    	return (mf - b)/a;
    }
    
    /**
	 * {@inheritDoc}
	 */
    @Override
    public double getBreakdownInitialMass(){
    	
    	// IFMR is only constrained to initial mass 0.83. However if we extrapolate the first segment
    	// to lower masses we get mi = mf at mi ~= 0.532.
    	
    	return a0[1]/(1.0-a0[0]);
    }
    
    /**
	 * {@inheritDoc}
	 */
    @Override
    public String toString(){ return "Cummings et al (2018)";}
    
}
