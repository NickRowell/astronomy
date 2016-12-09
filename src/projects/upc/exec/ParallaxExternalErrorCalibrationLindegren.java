package projects.upc.exec;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.geom.Ellipse2D;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import projects.upc.dm.UpcStar;
import projects.upc.util.UpcUtils;
import util.GuiUtil;

/**
 * Class examines and visualises the external errors of the UPC catalogue by the
 * method of Lennart Lindegren, i.e. looking at the correlation between the parallaxes
 * and a weighted mean.
 *
 * TODO: compute & plot the correlation coefficient
 * TODO: add f(x) = 1.0 line
 * TODO: figure out the theory
 *
 * @author nrowell
 * @version $Id$
 */
public class ParallaxExternalErrorCalibrationLindegren extends JPanel {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 7921054854711574643L;

	/**
	 * The logger.
	 */
	private static final Logger logger = Logger.getLogger(ParallaxExternalErrorCalibrationLindegren.class.getName());

	/**
	 * The list of {@link UpcStar}s with Hipparcos counterparts.
	 */
	List<UpcStar> hipStars;
	
	/**
	 * The {@link ChartPanel} presenting the plot.
	 */
	ChartPanel chartPanel;
	
	/**
	 * The current linear combination factor.
	 * <br>
	 * x=0.0 -> Hipparcos parallax
	 * <br>
	 * x=1.0 -> UPC parallax
	 * <br>
	 * 0.0 &lt; x &lt; 1.0 -> Linear combination of the two
	 * 
	 */
	double x;
	
	/**
	 * The current correlation coefficient.
	 */
	double p;
	
	/**
	 * Main constructor.
	 */
	public ParallaxExternalErrorCalibrationLindegren() {

		List<UpcStar> upcStars = UpcUtils.loadUpcCatalogue();
		hipStars = UpcUtils.getHipparcosSubset(upcStars);
		
		logger.info("Loaded "+hipStars.size()+" UpcStars in Hipparcos subset");
		
		x = 1.0;
		
		final JSlider xSlider = GuiUtil.buildSlider(0.0, 1.0, 5, "%3.3f");
		xSlider.setValue((int)Math.rint(100.0*x));
		final JLabel xLabel = new JLabel(getXLabel());
		// Create a change listener fot these
		ChangeListener cl = new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent e)
			{
				JSlider source = (JSlider)e.getSource();
				
				if(source==xSlider) {
					// Compute linear combination factor from slider position
					double newX = (source.getValue()/100.0);
					x = newX;
					xLabel.setText(getXLabel());
				}
				updateChart();
			}
		};
		xSlider.addChangeListener(cl);
		// Add a bit of padding to space things out
		xSlider.setBorder(new EmptyBorder(5,5,5,5));
				
		// Present the X label and slider in a control panel
		JPanel controls = new JPanel(new GridLayout(1,2));
		controls.add(xLabel);
		controls.add(xSlider);
		
		// Initialise the ChartPanel
		updateChart();
		
		// Build the panel contents
		setLayout(new BorderLayout());
		add(chartPanel, BorderLayout.CENTER);
		add(controls, BorderLayout.SOUTH);
	}

	/**
	 * Get a label appropriate for the linear combination factor field.
	 * @return
	 * 	A label appropriate for the linear combination factor field.
	 */
	private String getXLabel() {
		return String.format("Linear combination factor [%5.4f]:", x);
	}
	
	/**
	 * Update the {@link ParallaxExternalErrorCalibrationLindegren#chartPanel}.
	 */
	private void updateChart() {
		
		XYSeries series = new XYSeries("Parallax");
		
		double meanX = 0.0;
		double meanY = 0.0;
		
		for(UpcStar hipStar : hipStars) {
			double upcParallax = hipStar.absPi;
			double hipParallax = hipStar.srcPi;
			
			// Apply the linear combination factor
			double abscissa = (1-x)*hipParallax + x*upcParallax;
			double ordinate = hipParallax - upcParallax;
			series.add(abscissa, ordinate);
			
			// Measure correlation of abscissa and ordinate
			meanX += abscissa;
			meanY += ordinate;
		}
		meanX /= hipStars.size();
		meanY /= hipStars.size();
		
		
		double covXY  = 0.0;
		double sigmaX = 0.0;
		double sigmaY = 0.0;
		for(UpcStar hipStar : hipStars) {
			double upcParallax = hipStar.absPi;
			double hipParallax = hipStar.srcPi;
			double abscissa = (1-x)*hipParallax + x*upcParallax;
			double ordinate = hipParallax - upcParallax;
			covXY  += (abscissa - meanX)*(ordinate - meanY);
			sigmaX += (abscissa - meanX)*(abscissa - meanX);
			sigmaY += (ordinate - meanY)*(ordinate - meanY);
		}
		
		p = covXY / Math.sqrt(sigmaX * sigmaY);
		
		XYSeriesCollection data = new XYSeriesCollection();
		data.addSeries(series);
		
		// Set up the renderer
    	XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
    	
    	renderer.setSeriesLinesVisible(0, false);
        renderer.setSeriesShapesVisible(0, true);
        renderer.setSeriesShape(0, new Ellipse2D.Double(-0.5, -0.5, 1, 1));
        
        // Configure axes
        NumberAxis xAxis = null;
        if(x==0.0) {
        	xAxis = new NumberAxis("ω_Hip [mas]");
        }
        else if(x==1.0){
        	xAxis = new NumberAxis("ω_UPC [mas]");
        }
        else {
        	xAxis = new NumberAxis(String.format("%3.2f*ω_Hip + %3.2f*ω_UPC [mas]", 1-x, x));
        }
        
        xAxis.setRange(-100, 150);
        
        NumberAxis yAxis = new NumberAxis("ω_Hip - ω_UPC [mas]");
        yAxis.setRange(-100, 100);
        
        // Configure plot
        XYPlot xyplot = new XYPlot(data, xAxis, yAxis, renderer);
        xyplot.setBackgroundPaint(Color.white);
        
        XYTextAnnotation a1 = new XYTextAnnotation(String.format("p = %8.7f", p), 100.0, 80.0);
        a1.setFont(new Font("Helvetica", Font.PLAIN, 18));
        xyplot.addAnnotation(a1);
        
        JFreeChart chart = new JFreeChart("Calibration of UPC Parallax Uncertainty wrt Hipparcos", xyplot);
        chart.removeLegend();
        chart.setBackgroundPaint(Color.white);
        
        if(chartPanel==null) {
        	// Branch is used on initialisation
        	chartPanel = new ChartPanel(chart);
        }
        else {
        	chartPanel.setChart(chart);
        }
        
	}
	
	/**
	 * Main application entry point.
	 * @param args
	 * 	The args - ignored.
	 */
	public static void main(String[] args) {
		
		final JFrame frame = new JFrame("UPC external parallax uncertainty calibration");
		
		SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
            	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setLayout(new BorderLayout());
                frame.add(new ParallaxExternalErrorCalibrationLindegren(), BorderLayout.CENTER);
                frame.setSize(1500, 750);
                frame.pack();
                frame.setVisible(true);
            }
        });
	}
	
}
