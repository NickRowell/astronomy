package projects.gaia.exec;

import projects.gaia.util.GaiaParallaxErrFn;

/**
 * Utility used to plot the Gaia parallax error function.
 *
 *
 * @author nrowell
 * @version $Id$
 */
public class PlotGaiaParallaxErrFn {
	
	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		
		GaiaParallaxErrFn gaiaParallaxErrFn = new GaiaParallaxErrFn();
		
		for(double g=5.0; g<22.0; g+=0.1) {
			System.out.println(g + "\t" + gaiaParallaxErrFn.evaluate(g));
		}
		
		
		
	}
}
