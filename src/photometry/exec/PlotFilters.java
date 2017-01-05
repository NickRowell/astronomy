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
	
	public static void main(String[] args) throws IOException
	{
		String script = 
				"set terminal pngcairo enhanced color size 640,480" + OSChecker.newline + 
				"set key top right" + OSChecker.newline +
				"set yrange [0:*]" + OSChecker.newline +
				"set xtics out" + OSChecker.newline +
				"set ytics out" + OSChecker.newline +
				"set xlabel 'Wavelength [Angstroms]'" + OSChecker.newline +
				"set ylabel 'Transmission'" + OSChecker.newline +
				"plot -10 notitle";
		
		for(Filter type : Filter.sdss)
		{
			script += ", '-' w l title '"+type.toString()+"'";
		}
		
		script += OSChecker.newline;
		
		for(Filter filter : Filter.sdss)
		{
			
			for(double lambda = filter.lambdaMin; lambda<filter.lambdaMax; lambda+=lambdaStep)
			{
				double x = filter.interpolate(lambda);
				script += String.format("%f\t%f\n", lambda, x);
				
			}
			script += "e" + OSChecker.newline;
		}
		
		Gnuplot.displayImage(Gnuplot.executeScript(script));
	}
	
}
