package imf.exec;

import java.io.IOException;

import imf.infra.IMF;
import imf.algo.BaseImf;
import infra.io.Gnuplot;
import infra.os.OSChecker;
import util.CharUtil;

/**
 * This class plots the various IMF implementations.
 *
 * @author nrowell
 * @version $Id$
 */
public class PlotImfs {
	
	/**
	 * Initial mass range, lower boundary.
	 */
	private static double minMi = BaseImf.M_lower;
	
	/**
	 * Initial mass range, upper boundary.
	 */
	private static double maxMi = BaseImf.M_upper;
	
	/**
	 * Mass step size when plotting IMFs.
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
		// Number of available IMFs
		int nImf = IMF.values().length;
		
		// Number of points to plot for each IFMR
		int nPoints = (int)Math.ceil((maxMi - minMi) / stepMi);
		
		// Arrays of names of IFMRs
		String[] names = new String[nImf];
		double[][][] data = new double[nImf][nPoints][2];
		
		for(int i=0; i<nImf; i++)
		{
			BaseImf imf = IMF.values()[i].getIMF();
			
			names[i] = imf.toString();
			
			for(int d=0; d<nPoints; d++)
			{
				// Translate index to initial mass
				double mi = minMi + d*stepMi;
				double mf = imf.getIMF(mi);
				
				data[i][d][0] = mi;
				data[i][d][1] = mf;
			}
		}
		
		String script = 
				"set terminal pngcairo enhanced color size 640,480" + OSChecker.newline + 
				"set xrange [0.1:10]" + OSChecker.newline +
				"set yrange [*:*]" + OSChecker.newline +
				"set key top right" + OSChecker.newline +
				"set xtics out" + OSChecker.newline +
				"set ytics out" + OSChecker.newline +
				"set logscale x" + OSChecker.newline +
				"set logscale y" + OSChecker.newline +
				"set xlabel 'Stellar mass [M_"+CharUtil.solar+"]'" + OSChecker.newline +
				"set ylabel 'Density [N M_"+CharUtil.solar+"]^{-1}'" + OSChecker.newline +
				"plot 0 notitle";
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
		
		Gnuplot.displayImage(Gnuplot.executeScript(script));
	}
	
}
