package utils;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Logger;

import infra.io.Gnuplot;
import infra.os.OSChecker;
import numeric.stats.StatUtil;
import numeric.stats.StatUtil.ValueMapper;

/**
 * Utility class for plotting.
 *
 * @author nrowell
 * @version $Id$
 */
public final class PlotUtil {

	/**
	 * The logger.
	 */
	private static final Logger logger = Logger.getLogger(PlotUtil.class.getName());
	
	/**
	 * Private class to enforce non-instantiability.
	 */
	private PlotUtil() {
		
	}
	
	/**
	 * Compute and plot the cumulative distribution function for the statistic derived from the objects.
	 * 
	 * @param objects
	 * 	The objects to be processed.
	 * @param mapper
	 * 	Rule for compute the value to be plotted from each object.
	 * @param min
	 * 	Minimum value of the histogram.
	 * @param max
	 * 	Maximum value of the histogram.
	 * @param binWidth
	 * 	The width of each bin; if there's not a whole number of bins between the limits then the uppermost bin
	 * is extended slightly beyond the range.
	 * @param normalise
	 * 	Boolean flag indicating if the histogram should be normalised.
	 * @param ascending
	 * 	Boolean flag indicating if the cumulative distribution should be summed in the ascending or descending direction.
	 * @param xlabel
	 * 	Label for the X axis (not including the units).
	 * @param xunits
	 * 	The units for the data.
	 * @param width
	 * 	Width of the plot to create.
	 * @param height
	 * 	Height of the plot to create.
	 * @return
	 * 	A plot of the (optionally normalised) cumulative distribution, computed in either the ascending or
	 * descending direction as requested.
	 */
	public static <T> BufferedImage plotCdf(Collection<T> objects, ValueMapper<T> mapper, double min, double max, double binWidth, boolean normalise, boolean ascending, String xlabel, String xunits, int width, int height) {
		
		// Compute the cumulative distribution function
        double[] bins = StatUtil.computeCdf(objects, mapper, min, max, binWidth, normalise, ascending);
		
		StringBuilder script = new StringBuilder();
		script.append("set terminal pngcairo enhanced color size "+width+","+height).append(OSChecker.newline);
		
		script.append("set xrange ["+min+":"+max+"]").append(OSChecker.newline);
		script.append("set xlabel '"+xlabel+" ["+xunits+"]'").append(OSChecker.newline);
		script.append("set xtics out").append(OSChecker.newline);
		
		script.append("set yrange [0:*]").append(OSChecker.newline);
		script.append("set ylabel 'CDF("+xlabel+")'").append(OSChecker.newline);
		script.append("set ytics out").append(OSChecker.newline);
		
		// Legend
		script.append("set key off").append(OSChecker.newline);
		
		// Bars style
		script.append("set style fill transparent solid 0.5 noborder").append(OSChecker.newline);
		script.append("set boxwidth 0.95 relative").append(OSChecker.newline);
		
		script.append("plot '-' u 1:2 w boxes lc rgb 'green' notitle").append(OSChecker.newline);
		
		for(int i=0; i<bins.length; i++) {
			double binCentre = min + i * binWidth + binWidth / 2.0;
			script.append(binCentre + "\t" + bins[i]).append(OSChecker.newline);
		}
		script.append("e").append(OSChecker.newline);
		
		BufferedImage image = null;
		
		try {
			image = Gnuplot.executeScript(script.toString());
		} catch (IOException e) {
			logger.info("Problem creating histogram: " + e.getMessage());
		}
		
		return image;
	}
	
	/**
	 * Computes and plots a histogram of the data.
	 * 
	 * @param data
	 * 	An array of the values to be binned.
	 * @param min
	 * 	The lower limit of the range to be binned.
	 * @param max
	 * 	The upper limit of the range to be binned.
	 * @param binWidth
	 * 	The width of each bin; if there's not a whole number of bins between the limits then the uppermost bin
	 * is extended slightly beyond the range.
	 * @param normalise
	 * 	Boolean flag indicating if the histogram should be normalised.
	 * @param xlabel
	 * 	Label for the X axis (not including the units).
	 * @param xunits
	 * 	The units for the data.
	 * @param width
	 * 	Width of the plot to create.
	 * @param height
	 * 	Height of the plot to create.
	 * @return
	 * 	A plot of the histogram.
	 */
	public static BufferedImage plotHistogram(double[] data, double min, double max, double binWidth, boolean normalise, String xlabel, String xunits, int width, int height) {
		
		// Compute the normalised binned data
        double[] bins = StatUtil.computeHistogram(data, min, max, binWidth, normalise);
		
		StringBuilder script = new StringBuilder();
		script.append("set terminal pngcairo enhanced color size "+width+","+height).append(OSChecker.newline);
		
		script.append("set xrange ["+min+":"+max+"]").append(OSChecker.newline);
		script.append("set xlabel '"+xlabel+" ["+xunits+"]'").append(OSChecker.newline);
		script.append("set xtics out").append(OSChecker.newline);
		
		script.append("set yrange [0:*]").append(OSChecker.newline);
		script.append("set ylabel 'P("+xlabel.toLowerCase()+") ["+xunits+"^{-1}]'").append(OSChecker.newline);
		script.append("set ytics out").append(OSChecker.newline);
		
		// Legend
		script.append("set key off").append(OSChecker.newline);
		
		// Bars style
		script.append("set style fill transparent solid 0.5 noborder").append(OSChecker.newline);
		script.append("set boxwidth 0.95 relative").append(OSChecker.newline);
		
		script.append("plot '-' u 1:2 w boxes lc rgb 'green' notitle").append(OSChecker.newline);
		
		for(int i=0; i<bins.length; i++) {
			double binCentre = min + i * binWidth + binWidth / 2.0;
			script.append(binCentre + "\t" + bins[i]).append(OSChecker.newline);
		}
		script.append("e").append(OSChecker.newline);
		
		BufferedImage image = null;
		
		try {
			image = Gnuplot.executeScript(script.toString());
		} catch (IOException e) {
			logger.info("Problem creating histogram: " + e.getMessage());
		}
		
		return image;
	}
	
}
