package photometry.exec;

import photometry.Filter;
import photometry.FilterUtils;

/**
 * This class provides an application to estimate the relation between two parameterisations
 * of the colour of a stellar spectrum: the 'photon-weighted effective wavenumber' and the 
 * colour in terms of the difference of two magnitudes in different filters.
 *
 *
 * @author nrowell
 * @version $Id$
 */
public class EffectiveWavenumberVsColour {
	
	/**
	 * Main application entry point.
	 * 
	 * @param args
	 * 	The command line args (ignored)
	 */
	public static void main(String[] args) {
		
		// Choose the two filters to define the stellar colour
		Filter fa = Filter.V;
		Filter fb = Filter.I;
		
		// Filter to modulate the source spectrum when computing the effective wavenumber
		Filter fc = Filter.G;
		
		// Temperature range over which to compute colours/effective wavenumbers for blackbody spectrum [K]
		double tMin = 1500.0;
		double tMax = 50000.0;
		double tStep = 100.0;
		
		for(double t=tMin; t<=tMax; t+=tStep) {
			
			// Get the blackbody colour at this temperature
			double colour = FilterUtils.blackbodyColour(fa, fb, t);
			
			// Get the effective wavenumber of the blackbody spectrum at this temperature
			double nu_eff = FilterUtils.getBlackbodyEffectiveWavenumber(fc, t);
			
			System.out.println(colour + "\t" + nu_eff);
			
			
			
		}
		
		
		
		
		
		
	}
	
}