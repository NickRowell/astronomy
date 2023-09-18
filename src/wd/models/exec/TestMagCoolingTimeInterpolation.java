package wd.models.exec;

import ifmr.algo.BaseIfmr;
import ifmr.infra.IFMR;
import ms.lifetime.algo.PreWdLifetime;
import ms.lifetime.infra.PreWdLifetimeModels;
import photometry.Filter;
import wd.models.algo.WdCoolingModelSet;
import wd.models.infra.WdAtmosphereType;
import wd.models.infra.WdCoolingModels;

/**
 * Class used to test and develop the WD magnitude vs cooling time interpolation.
 *
 *
 * @author nrowell
 * @version $Id$
 */
public class TestMagCoolingTimeInterpolation {
	
	/**
	 * Main appication entry point
	 * @param args
	 * 	The command line arguments (ignored)
	 */
	public static void main(String[] args) {
		
		WdCoolingModelSet wdModels = WdCoolingModels.MONTREAL_NEW_2020.getWdCoolingModels();
		
		BaseIfmr ifmr = IFMR.CATALAN_2008.getIFMR();
		PreWdLifetime preWd = PreWdLifetimeModels.PARSECV1p2s.getPreWdLifetimeModels();
		
		// Fixed parameters
		double mass = 0.6882;
		WdAtmosphereType atm = WdAtmosphereType.H;
		Filter filter = Filter.M_BOL;
		
		// Input mag
		double mag = 14.84;
		
		double tcool = wdModels.tcool(mag, mass, atm, filter);

		double msMass =  ifmr.getMi(mass);
		double preWdLife = preWd.getPreWdLifetime(0.017, 0.279, msMass)[0];

		System.out.println("Progenitor mass = " + msMass);
		System.out.println("Progenitor life = " + preWdLife);
		System.out.println("Total lifetime  = " + (preWdLife + tcool));
		
		
		System.out.println("Input mag = "+mag);
		System.out.println("Forward-interpolated tcool = "+tcool);
		mag = wdModels.quantity(tcool, mass, atm, filter);
		System.out.println("Reverse-interpolated mag  = "+mag);
	}
	
}