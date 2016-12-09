package wd.wdlf.inversion.sandbox;

import imf.algo.BaseImf;
import wd.wdlf.dm.Star;
import wd.wdlf.dm.WdlfModellingParameters;
import wd.wdlf.inversion.util.InversionUtil;

/**
 * Class is designed to check whether the correction for low mass stars, which is based on numerical
 * integration of the IMF, is consistent with the fraction of simulation stars that form WDs.
 *
 * @author nrowell
 * @version $Id$
 */
public class TestLowMassStarCorrection {
	
	
	/**
	 * Main application entry point.
	 * @param args
	 * 	The command line args (ignored)
	 */
	public static void main(String[] args) {
		
		// Get an instance of the WDLFModellingParams
		WdlfModellingParameters params = new WdlfModellingParameters();
		
		// Get the lifetime of the most massive star that forms a WD
        double z = params.getMeanMetallicity();
        double y = params.getMeanHeliumContent();
		double t_lower = params.getPreWdLifetime().getPreWdLifetime(z, y, BaseImf.M_upper)[0];
		
        // Upper edge of formation time bin
        double t_upper = t_lower + 0.5e8;    
        
        int nWdsPerBin = 10000000;
        
        // Count number of simulation stars created in this bin in order
        // to get n_WDs_per_bin WD progenitors
        double N_SIM_STARS = 0;

        double N_WDS = 0;
        
        // Continually create stars uniformly distributed in this formation
        // time bin until n_WD_s_per_bin WD progenitors have been made.
        for(int nWdsFormedInBin=0; nWdsFormedInBin<=nWdsPerBin; ) {
  
            // Create a new Star using current distributions
            Star star = new Star(t_lower, t_upper, params);
        
            N_SIM_STARS++;
        
            if(star.getTotalAge() > star.getPreWdLifetime()) {
            	
                // Star has become a WD
            	nWdsFormedInBin++;
            	N_WDS++;
            }
    
            // Star hasn't yet turned into a WD - take no action.
        }
        
        System.out.println("Fraction of simulation stars that form WDs: "+(N_WDS/N_SIM_STARS));
        
        // Compute the fraction of stars that form WDs in this lookback time range numerically
        double fracWdProgenitors = InversionUtil.getFractionWDProgenitorsInTimeRange(t_lower, t_upper, params);
        
        
        System.out.println("From numerical integration: "+(fracWdProgenitors));
        
	}
	
	
	
}
