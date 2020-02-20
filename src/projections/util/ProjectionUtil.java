package projections.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Ellipse2D;
import java.io.IOException;
import java.util.List;

import javax.swing.JFrame;

import org.jfree.chart.ChartColor;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.TextAnchor;

import infra.io.Gnuplot;
import infra.jfree.XYShapeAndLineRenderer;
import infra.os.OSChecker;
import projections.Projection;

/**
 * Utilities associated with sky/map projections.
 *
 * @author nrowell
 * @version $Id$
 */
public class ProjectionUtil {
	
	/**
	 * Generate static plot using Gnuplot.
	 * @param points
	 * 	The points to plot, in unprojected coordinates [radians]
	 * @param proj
	 *  The {@link Projection} to use.
	 * @throws IOException
	 *  If there's a problem writing the plot script to a temporary file.
	 */
	public static void makeGnuPlot(double[][] points, Projection proj) throws IOException {
		
		double[] xRange = proj.getRangeX();
		double[] yRange = proj.getRangeY();
		
		StringBuilder script = new StringBuilder();
		
		script.append("set terminal pngcairo enhanced color size 640,480").append(OSChecker.newline);
		script.append("set xrange ["+xRange[0]+":"+xRange[1]+"]").append(OSChecker.newline);
		script.append("set yrange ["+yRange[0]+":"+yRange[1]+"]").append(OSChecker.newline);
		script.append("set key off").append(OSChecker.newline);
		script.append("unset xtics").append(OSChecker.newline);
		script.append("unset ytics").append(OSChecker.newline);
		script.append("set xlabel ''").append(OSChecker.newline);
		script.append("set ylabel ''").append(OSChecker.newline);
		script.append("plot '-' w d notitle ").append(OSChecker.newline);
		
		for(double[] point : points)
		{
			double[] projection = proj.getForwardProjection(point);
			script.append(projection[0] + " " + projection[1]).append(OSChecker.newline);
		}
		script.append("e").append(OSChecker.newline);
		
		Gnuplot.displayImage(Gnuplot.executeScript(script.toString()));
	}
	
	/**
	 * Generate interactive plot using JFreeChart.
	 * @param points
	 * 	List of points to plot, in unprojected coordinates [radians]
	 * @param proj
	 *  The {@link Projection} to use.
	 */
	public static void makeAndDisplayJFreeChartPlot(List<double[]> points, String title, Projection proj) {
		
        JFreeChart chart = makeJFreeChartPlot(points, title, proj);
        
        final ChartPanel chartPanel = new ChartPanel(chart);
		
		// Create and display the form
        java.awt.EventQueue.invokeLater(
                new Runnable() 
                    {
                        @Override
                        public void run() 
                        {
                            JFrame tester = new JFrame("ProjectionUtil");
                            tester.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                            tester.setLayout(new BorderLayout());
                            tester.add(chartPanel, BorderLayout.CENTER);
                            tester.pack();
                            tester.setVisible(true);
                        }
                    });
	}
	
	/**
	 * Generate interactive plot using JFreeChart.
	 * @param points
	 * 	List of points to plot, in unprojected coordinates [radians]
	 * @param proj
	 *  The {@link Projection} to use.
	 */
	public static JFreeChart makeJFreeChartPlot(List<double[]> points, String title, Projection proj) {
		
		double[] xRange = proj.getRangeX();
		double[] yRange = proj.getRangeY();
		
		final XYSeriesCollection data = new XYSeriesCollection();
		
		// Add lines of constant longitude
		int nConstLong = 13;
		List<double[][]> linesLong = proj.getConstantLongitudeLinesProjected(nConstLong);
		
		for(double[][] line : linesLong) {
			XYSeries gridLine = new XYSeries("", false);
			for(double[] point : line) {
				gridLine.add(point[0], point[1]);
			}
			data.addSeries(gridLine);
		}
		
		// Add lines of constant latitude
		int nConstLat = 5;
		List<double[][]> linesLat = proj.getConstantLatitudeLinesProjected(nConstLat);
		
		for(double[][] line : linesLat) {
			XYSeries gridLine = new XYSeries("", false);
			for(double[] point : line) {
				gridLine.add(point[0], point[1]);
			}
			data.addSeries(gridLine);
		}
		
		// Add the data
		XYSeries series = new XYSeries("Data");
		for(double[] point : points) {
			double[] projection = proj.getForwardProjection(point);
			series.add(projection[0], projection[1]);
		}
		data.addSeries(series);
		
    	// Set up the renderer
    	XYShapeAndLineRenderer renderer = new XYShapeAndLineRenderer();
    	
        for(int i=0; i<nConstLong; i++) {
        	renderer.setSeriesLinesVisible(i, true);
            renderer.setSeriesShapesVisible(i, false);
            renderer.setSeriesPaint(i, ChartColor.BLACK);
        }
        
        for(int i=0; i<nConstLat; i++) {
        	renderer.setSeriesLinesVisible(i+nConstLong, true);
            renderer.setSeriesShapesVisible(i+nConstLong, false);
            renderer.setSeriesPaint(i+nConstLong, ChartColor.BLACK);
        }
        
        renderer.setSeriesLinesVisible(nConstLong + nConstLat, false);
        renderer.setSeriesShapesVisible(nConstLong + nConstLat, true);
        renderer.setSeriesShape(nConstLong + nConstLat, new Ellipse2D.Double(-0.25, -0.25, 0.5, 0.5));
        renderer.setSeriesPaint(nConstLong + nConstLat, ChartColor.RED);
        
        // Configure axes
        NumberAxis xAxis = new NumberAxis("");
        xAxis.setRange(xRange[0], xRange[1]);
        xAxis.setVisible(false);
        
        NumberAxis yAxis = new NumberAxis("");
        yAxis.setRange(yRange[0], yRange[1]);
        yAxis.setVisible(false);
        
        // Configure plot
        XYPlot xyplot = new XYPlot(data, xAxis, yAxis, renderer);
        xyplot.setBackgroundPaint(Color.white);
        xyplot.setDomainGridlinePaint(Color.white);
        xyplot.setDomainGridlinesVisible(false);
        xyplot.setRangeGridlinesVisible(false);

        // Add coordinate labels along equator
        int longLabels = 6;
        for(int l=0; l<longLabels; l++) {
        	
        	double labelLong = l*(360.0/longLabels);
        	
	        double[] label = proj.getForwardProjection(new double[]{Math.toRadians(labelLong), Math.toRadians(0)});
	        XYTextAnnotation annot = new XYTextAnnotation(String.format("%.0f", labelLong)+"°", label[0], label[1]);
	        annot.setFont(new Font("SansSerif", Font.BOLD, 16));
	        annot.setTextAnchor(TextAnchor.BASELINE_LEFT);
	        xyplot.addAnnotation(annot);
        }
        
        // Configure chart
        JFreeChart chart = new JFreeChart(title, xyplot);
//        chart.addSubtitle(new TextTitle(proj.toString() + " Projection"));
        chart.removeLegend();
        chart.setBackgroundPaint(Color.white);
        
        return chart;
	}
}
