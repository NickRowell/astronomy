package projects.upc.exec;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jfree.chart.ChartColor;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import numeric.data.Histogram;
import projects.upc.dm.UpcStar;
import projects.upc.util.UpcUtils;
import util.GuiUtil;

/**
 * 
 * TODO: add checkboxes to enable plotting only Hipparcos stars, only UPC stars, and all.
 * 
 * @author nrowell
 * @version $Id$
 */
public class PlotParallaxHistograms extends JPanel {
	
	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 2733395264289086410L;

	/**
	 * The logger.
	 */
	private static final Logger logger = Logger.getLogger(PlotParallaxHistograms.class.getName());

	/**
	 * The list of all {@link UpcStar}s.
	 */
	List<UpcStar> upcStars;

	/**
	 * The list of {@link UpcStar}s with Hipparcos counterparts.
	 */
	List<UpcStar> hipStars;

	/**
	 * The list of all unmatched {@link UpcStar}s.
	 */
	List<UpcStar> unmatchedStars;

	/**
	 * Reference to the list of stars to plot.
	 */
	List<UpcStar> starsToPlot;
	
	/**
	 * The current threshold on parallax formal error for plotting.
	 */
	double p;
	
	/**
	 * The {@link ChartPanel} presenting the plot.
	 */
	ChartPanel chartPanel;
	
	
	public PlotParallaxHistograms() {
		
		upcStars = UpcUtils.loadUpcCatalogue();
		hipStars = UpcUtils.getHipparcosSubset(upcStars);
		unmatchedStars = UpcUtils.getUnmatchedSubset(upcStars);
		
		logger.info("Loaded "+upcStars.size()+" stars from the UPC catalogue");
		logger.info("Loaded "+hipStars.size()+" stars with Hipparcos cross matches");
		logger.info("Loaded "+unmatchedStars.size()+" UpcStars with no parallax cross-match");
		
		starsToPlot = hipStars;
		
    	final JCheckBox plotAllCheckBox = new JCheckBox("Plot all UPC stars: ", false);
    	plotAllCheckBox.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				if(plotAllCheckBox.isSelected()) {
					starsToPlot = upcStars;
					updateChart();
				}
			}
    	});
    	
		final JCheckBox plotHipCheckBox = new JCheckBox("Plot Hipparcos stars only: ", true);
    	plotHipCheckBox.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				if(plotHipCheckBox.isSelected()) {
					starsToPlot = hipStars;
					updateChart();
				}
			}
    	});
    	
    	final JCheckBox plotUnmatchedCheckBox = new JCheckBox("Plot all stars with no external match: ", false);
    	plotUnmatchedCheckBox.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				if(plotUnmatchedCheckBox.isSelected()) {
					starsToPlot = unmatchedStars;
					updateChart();
				}
			}
    	});
    	
    	final ButtonGroup bg = new ButtonGroup();
    	bg.add(plotHipCheckBox);
    	bg.add(plotAllCheckBox);
    	bg.add(plotUnmatchedCheckBox);
    	
    	p = 20.0;
    	
    	final JSlider xSlider = GuiUtil.buildSlider(0.0, 20.0, 5, "%3.3f");
		xSlider.setValue((int)Math.rint(p*5.0));
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
					double newX = (source.getValue()/5.0);
					p = newX;
					xLabel.setText(getXLabel());
				}
				updateChart();
			}
		};
		xSlider.addChangeListener(cl);
		// Add a bit of padding to space things out
		xSlider.setBorder(new EmptyBorder(5,5,5,5));
    	
    	 
		// Present the X label and slider in a control panel
		JPanel controls = new JPanel(new GridLayout(2,3));
		controls.add(plotAllCheckBox);
		controls.add(plotHipCheckBox);
		controls.add(plotUnmatchedCheckBox);
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
	 * Displays a plot of the parallax distribution for the stars.
	 */
	private void updateChart() {
		
		Histogram hist = new Histogram(-100.0, 120, 0.5, true);
		
		for(UpcStar upcStar : starsToPlot) {
			
			// Only plot this star if it's formal parallax error is below the threshold
			if(upcStar.absPiErr < p) {
				hist.add(upcStar.absPi+0.05);
			}
		}
		
		XYSeriesCollection data = new XYSeriesCollection();
		
		XYSeries series = new XYSeries("Parallax distribution");
		for(int j=0; j<hist.getNumberOfBins(); j++) {
			series.add(hist.getBinCentre(j), hist.getBinContents(j));
		}
		data.addSeries(series);
		
		// Set up the renderer
    	XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
    	
    	renderer.setSeriesLinesVisible(0, true);
        renderer.setSeriesShapesVisible(0, false);
        renderer.setSeriesPaint(0, ChartColor.DARK_BLUE);
        
        // Configure axes
        NumberAxis xAxis = new NumberAxis("Parallax [mas]");
        xAxis.setRange(-100.0, 120.0);
        
        NumberAxis yAxis = new NumberAxis("N");
        yAxis.setAutoRangeIncludesZero(true);
        
        // Configure plot
        XYPlot xyplot = new XYPlot(data, xAxis, yAxis, renderer);
        xyplot.setBackgroundPaint(Color.white);
		
        JFreeChart chart = new JFreeChart("UPC Parallax Distribution", xyplot);
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
	 * Get a label appropriate for the parallax formal error threshold field.
	 * @return
	 * 	A label appropriate for the parallax formal error threshold field.
	 */
	private String getXLabel() {
		return String.format("Parallax formal error threshold (mas) [%5.3f]:", p);
	}

	/**
	 * Main application entry point.
	 * @param args
	 * 	The args - ignored.
	 */
	public static void main(String[] args) {
		
		final JFrame frame = new JFrame("UPC Parallax Distributions");
		
		SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
            	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setLayout(new BorderLayout());
                frame.add(new PlotParallaxHistograms(), BorderLayout.CENTER);
                frame.setSize(1500, 750);
                frame.pack();
                frame.setVisible(true);
            }
        });
	}
}