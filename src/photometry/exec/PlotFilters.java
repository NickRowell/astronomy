package photometry.exec;

import java.io.IOException;

import infra.io.Gnuplot;
import infra.os.OSChecker;
import photometry.Filter;

/**
 * TODO: add checkboxes to select which filter sets to plot, i.e. 'HST', 'CFHT', 'SuperCOSMOS', ...
 *
 *
 * @author nrowell
 * @version $Id$
 */
public class PlotFilters {

	/**
	 * Wavelength step between points [Angstroms]
	 */
	private static double lambdaStep = 10.0;
	
	public static void main(String[] args) throws IOException {
		
//		Filter[] filtersToPlot = new Filter[]{Filter.BP, Filter.RP, Filter.V, Filter.I};
//		Filter[] filtersToPlot = new Filter[]{Filter.SDSS_U, Filter.SDSS_G, Filter.SDSS_R, Filter.SDSS_I,Filter.SDSS_Z, Filter.G, Filter.BP, Filter.RP};
		Filter[] filtersToPlot = new Filter[]{Filter.U, Filter.B, Filter.V, Filter.R,Filter.I, Filter.BP_NOM_DR2, Filter.RP_NOM_DR2};
		
		String script = 
				"set terminal pngcairo enhanced color size 640,480" + OSChecker.newline + 
				"set key top right" + OSChecker.newline +
				"set yrange [0:*]" + OSChecker.newline +
				"set xtics out" + OSChecker.newline +
				"set ytics out" + OSChecker.newline +
				"set xlabel 'Wavelength [Angstroms]'" + OSChecker.newline +
				"set ylabel 'Transmission'" + OSChecker.newline +
				"plot -10 notitle";
		
		for(Filter type : filtersToPlot) {
			script += ", '-' w l title '"+type.toString()+"'";
		}
		
		script += OSChecker.newline;
		
		for(Filter filter : filtersToPlot) {
			
			for(double lambda = filter.lambdaMin; lambda<filter.lambdaMax; lambda+=lambdaStep) {
				double x = filter.interpolate(lambda);
				script += String.format("%f\t%f\n", lambda, x);
				
			}
			script += "e" + OSChecker.newline;
		}
		
		Gnuplot.displayImage(Gnuplot.executeScript(script));
	}
	
}
