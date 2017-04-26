package spectroscopy.exec;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import infra.io.Gnuplot;
import infra.os.OSChecker;
import numeric.functions.Linear;
import spectroscopy.utils.PicklesUtils;
import spectroscopy.utils.PicklesUtils.PicklesMetadata;

/**
 * Application to plot the stellar spectra contained in the Pickles library.
 *
 * @author nrowell
 * @version $Id$
 */
public class PlotPicklesLibrary {
	
	/**
	 * Main application entry point.
	 * 
	 * @param args
	 * 	The command line arguments (ignored).
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		Map<PicklesMetadata, Linear> spectra = PicklesUtils.loadPicklesSpectra();
		
		
		String script = 
				"set terminal pngcairo enhanced color size 640,480" + OSChecker.newline + 
				"set key top right" + OSChecker.newline +
				"set yrange [0:*]" + OSChecker.newline +
				"set xtics out" + OSChecker.newline +
				"set ytics out" + OSChecker.newline +
				"set xlabel 'Wavelength [Angstroms]'" + OSChecker.newline +
				"set ylabel 'Transmission'" + OSChecker.newline +
				"plot -10 notitle";
		
		for(Entry<PicklesMetadata, Linear> entry : spectra.entrySet()) {
			script += ", '-' w l notitle";
		}
		
		script += OSChecker.newline;
		
		for(Entry<PicklesMetadata, Linear> entry : spectra.entrySet()) {
			
			PicklesMetadata metaData = entry.getKey();
			Linear spectrum = entry.getValue();
			
			double lambdaMin = 3000;
			double lambdaMax = 10000;
			double lambdaStep = 10;
			
			for(double lambda = lambdaMin; lambda<lambdaMax; lambda+=lambdaStep)
			{
				double x = spectrum.interpolateY(lambda)[0];
				script += String.format("%f\t%f\n", lambda, x);
				
			}
			script += "e" + OSChecker.newline;
			
		}
		
		Gnuplot.displayImage(Gnuplot.executeScript(script));
		
		
	}
	
	
}
