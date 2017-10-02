package photometry.exec;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import numeric.functions.Linear;
import photometry.Filter;
import photometry.util.FilterUtils;
import spectroscopy.util.PicklesUtils;
import spectroscopy.util.PicklesUtils.PicklesMetadata;

/**
 * This class provides an application to estimate the relation between two parameterisations
 * of the colour of a stellar spectrum: the 'photon-weighted effective wavenumber' and the 
 * colour in terms of the difference of two magnitudes in different filters.
 * 
 * It uses two sources of input spectra: simple blackbodies, and stellar spectra from the
 * Pickles library.
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
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		// Choose the two filters to define the stellar colour
		Filter fa = Filter.V;
		Filter fb = Filter.I;
		
		// Filter to modulate the source spectrum when computing the effective wavenumber
		Filter fc = Filter.G;
		
		// Two output files of datapoints for the two spectral sources
		File outBlackbody = new File("/home/nrowell/Temp/nuEffVsVI_blackbody");
		File outPickles = new File("/home/nrowell/Temp/nuEffVsVI_pickles");
		
		// Load Pickles library spectra
		Map<PicklesMetadata, Linear> spectra = PicklesUtils.loadPicklesSpectra();
		
		// Temperature range over which to compute colours/effective wavenumbers for blackbody spectrum [K]
		double tMin = 1500.0;
		double tMax = 50000.0;
		double tStep = 100.0;
		
//		BufferedWriter out = new BufferedWriter(new FileWriter(outBlackbody));
		
//		for(double t=tMin; t<=tMax; t+=tStep) {
//			
//			// Get the blackbody colour at this temperature
//			double colour = FilterUtils.blackbodyColour(fa, fb, t);
//			
//			// Get the effective wavenumber of the blackbody spectrum at this temperature
//			double nu_eff = FilterUtils.getBlackbodyEffectiveWavenumber(fc, t);
//			
//			out.write(colour + "\t" + nu_eff + "\n");
//		}
//		out.close();
		
		BufferedWriter out = new BufferedWriter(new FileWriter(outPickles));
		
		for(Entry<PicklesMetadata, Linear> entry : spectra.entrySet()) {
			
			PicklesMetadata metaData = entry.getKey();
			Linear spectrum = entry.getValue();
			
			double colour = FilterUtils.getSyntheticColour(spectrum, fa, fb);
			
			double nu_eff = FilterUtils.getEffectiveWavenumber(spectrum, fc);
			
			out.write(colour + "\t" + nu_eff + "\t" + metaData.spectralType + "\n");
		}
		out.close();
		
		
		
		
	}
	
}