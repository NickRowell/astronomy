package ifmr.exec;

import java.io.IOException;

import ifmr.algo.BaseIfmr;
import ifmr.infra.IFMR;
import imf.algo.BaseImf;
import infra.io.Gnuplot;
import infra.os.OSChecker;
import util.CharUtil;

/**
 * This class tests the various IFMR implementations. It should:
 * 
 *  - plot all initial-final mass relations available and display in a JFrame
 *  - compute and print the breakdown mass for each IFMR
 *
 *
 * @author nrowell
 * @version $Id$
 */
public class PlotIfmrs {
	
	/**
	 * Initial mass range, lower boundary.
	 */
	private static double minMi = 0;
	
	/**
	 * Initial mass range, upper boundary.
	 */
	private static double maxMi = BaseImf.M_upper;
	
	/**
	 * Initial mass step size when plotting IFMRs.
	 */
	private static double stepMi = 0.01;
	
	
	/**
	 * Main application entry point
	 * @param args
	 * 	The args; ignored
	 * @throws IOException
	 * 	If there's a problem writing the Gnuplot plotting script to a temp file.
	 */
	public static void main(String[] args) throws IOException
	{
		// Number of available IFMRs
		int nIfmr = IFMR.values().length;
		
		// Number of points to plot for each IFMR
		int nPoints = (int)Math.ceil((maxMi - minMi) / stepMi);
		
		// Arrays of names of IFMRs
		String[] names = new String[nIfmr];
		double[][][] data = new double[nIfmr][nPoints][2];
		
		for(int i=0; i<nIfmr; i++)
		{
			BaseIfmr ifmr = IFMR.values()[i].getIFMR();
			
			names[i] = ifmr.toString();
			
			for(int d=0; d<nPoints; d++)
			{
				// Translate index to initial mass
				double mi = minMi + d*stepMi;
				double mf = ifmr.getMf(mi);
				
				data[i][d][0] = mi;
				data[i][d][1] = mf;
			}
		}
		
		String script = 
				"set terminal pngcairo enhanced color size 640,480" + OSChecker.newline + 
				"set xrange [0:"+maxMi+"]" + OSChecker.newline +
				"set yrange [0.4:1.3]" + OSChecker.newline +
				"set key bottom right" + OSChecker.newline +
				"set xtics out" + OSChecker.newline +
				"set ytics out" + OSChecker.newline +
				"set xlabel 'Initial MS mass [M_"+CharUtil.solar+"]'" + OSChecker.newline +
				"set ylabel 'Final WD mass [M_"+CharUtil.solar+"]'" + OSChecker.newline +
				"f(x) = x" + OSChecker.newline +
				"plot f(x) w l lc rgbcolor 'black' lt 0 title 'M_i = M_f'";
		for(String name : names) {
			script += ", '-' u 1:2 w l title '"+name+"'";
		}
		script += OSChecker.newline;
		for(double[][] series : data)
		{
			for(double[] series2 : series)
			{
				script += series2[0] + " " + series2[1] + OSChecker.newline;
			}
			script += "e" + OSChecker.newline;
		}
				
//		System.out.println(script);
		
		Gnuplot.displayImage(Gnuplot.executeScript(script));
	}
	
}
