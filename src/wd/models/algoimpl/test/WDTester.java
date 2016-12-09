package wd.models.algoimpl.test;

import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;

import ifmr.algo.BaseIfmr;
import ifmr.infra.IFMR;
import ms.lifetime.algo.PreWdLifetime;
import ms.lifetime.infra.PreWdLifetimeModels;
import photometry.Filter;
import wd.models.algo.WdCoolingModelSet;
import wd.models.infra.WdAtmosphereType;
import wd.models.infra.WdCoolingModels;

/**
 * Unit tests associated with WD cooling model classes.
 * 
 * @author nickrowell
 */
public class WDTester {

    /** 
     * Logger 
     */
    private static final Logger logger = Logger.getLogger(WDTester.class.getName());
    
    /**
     * Set up by reading in AstroObservations and setting up the CDB manager.
     * @throws GaiaException
     *  Problem reading AstroObservation file or constructing CdbDataManager
     */
    @Before
    public void setUp() {

		logger.info("Finished setup for WDTester tests");
    }
    
    /**
     * Test whether bilinear interpolation of WD model grid is invertible.
     * 
     * TODO: extend this to test the invertibility of all filter/model/atmosphere combinations
     * 
     */
    @Test
    public void examineTestCase()
    {
    
        WdAtmosphereType atm = WdAtmosphereType.H;
        double mass = 1.1;
        Filter filter = Filter.M_BOL;
        
        WdCoolingModelSet wd = WdCoolingModels.MONTREAL.getWdCoolingModels();

        double tcool_in = 7.692158e9;
        
        // Get interpolated magnitude at cooling time of 7.692158e9 years.
        double mbol_in = wd.mag(tcool_in, mass, atm, filter);
        
        logger.info("mbol("+tcool_in+") = "+mbol_in);

        double tcool_out = wd.tcool(mbol_in, mass, atm, filter);
        
        logger.info("tcool("+mbol_in+") = "+tcool_out);
    }
    
    
    /**
     * This test checks that the solution for the low mass WD limit at given
     * bolometric magnitude and total population age is unique.
     */
    @Test
    public void checkLowMassLimit()
    {
    	
        WdCoolingModelSet wd = WdCoolingModels.MONTREAL.getWdCoolingModels();
        PreWdLifetime ms = PreWdLifetimeModels.PADOVA.getPreWdLifetimeModels();
        
        double z = 0.008;
        double y = 0.30;
        
        BaseIfmr ifmr = IFMR.KALIRAI_2008.getIFMR();
        Filter filter = Filter.M_BOL;
        WdAtmosphereType atm = WdAtmosphereType.H;
        
        // Loop over progenitor mass
        for(double mass = 0.6; mass<7.0; mass+=0.001){
         
            // Previous value of tms + tcool, used to check that gradient
            // is always positive
            double tmsptcool = 0;       
            
            // Inner loop over bolometric magnitude
            for(double mbol = 0; mbol < 19; mbol += 0.01){
                
                // Main sequence lifetime
                double tms = ms.getPreWdLifetime(z, y, mass)[0];
                
                // Cooling time
                double tcool = wd.tcool(mbol, ifmr.getMf(mass), atm, filter);
                
                // check increasing tms+tcool
                if(tms + tcool <= tmsptcool)
                {
                    logger.warning("Non monotonic tms + tcool at M_wd = "
                            + ifmr.getMf(mass) + ", mbol = "+mbol+", M_ms = "+mass+
                            ", tms = "+tms+", tcool = "+tcool+", total = "+
                            (tms+tcool));
                }
                
                tmsptcool = tms + tcool;
                
            }
        }
    }
}
