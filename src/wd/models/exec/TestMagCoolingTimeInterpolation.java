package wd.models.exec;

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
		
		WdCoolingModelSet wdModels = WdCoolingModels.BASTI_PS.getWdCoolingModels();
		
		// Fixed parameters
		double mass = 0.9;
		WdAtmosphereType atm = WdAtmosphereType.He;
		Filter filter = Filter.M_BOL;
		
		// Input mag
		double mag = 14.5;
		
		double tcool = wdModels.tcool(mag, mass, atm, filter);
		
		System.out.println("Input mag = "+mag);
		System.out.println("Forward-interpolated tcool = "+tcool);
		mag = wdModels.mag(tcool, mass, atm, filter);
		System.out.println("Reverse-interpolated mag  = "+mag);
	}
	
}