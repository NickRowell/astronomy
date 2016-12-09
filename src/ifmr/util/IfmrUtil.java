package ifmr.util;

import ifmr.algo.BaseIfmr;

/**
 * Utility functions for the Initial-Final Mass Relation.
 *
 *
 * @author nrowell
 * @version $Id$
 */
public class IfmrUtil {

	
	/**
	 * Compute the breakdown mass for the IFMR, i.e. the initial mass at
	 * which M_{initial} = M_{final}.
	 * @param ifmr
	 * 	The {@link BaseIfmr} instance to test
	 * @param verbose
	 * 	For verbose logging
	 * @return
	 * 	The breakdown mass for the IFMR, i.e. the initial mass at
	 * which M_{initial} = M_{final}.
	 */
	public static double getBreakdownMass(BaseIfmr ifmr, boolean verbose) {
		
		// Select an initial mass range to search in
		double mi_lower = 0.25;
		double mi_upper = 7.0;
		
		// Search until the mi=mf to within this threshold
		double tolerance = 0.00000000000001;
		
		// Get the final mass at the mid point of the range
		double mi = (mi_lower + mi_upper)/2.0;
		double mf = ifmr.getMf(mi);
		
		if(verbose) {
			System.out.println(mi + "\t" + mf + "\t["+mi_lower + " : "+mi_upper+"]");
		}
		
		while(Math.abs(mi-mf)>tolerance) {
			
			// Decide in which half of the initial mass range the solution lies
			if(mf > mi) {
				// Breakdown mass is in the range mi -> mi_upper
				mi_lower = mi;
			}
			else {
				// Breakdown mass is in the range mi_lower -> mi
				mi_upper = mi;
			}
			
			mi = (mi_lower + mi_upper)/2.0;
			mf = ifmr.getMf(mi);
			
			if(verbose) {
				System.out.println(mi + "\t" + mf + "\t["+mi_lower + " : "+mi_upper+"]");
			}
		}
		
		if(verbose) {
			System.out.println("Found Mi = Mf for:");
		}
		
		return mi;
	}
	
	
	
}
