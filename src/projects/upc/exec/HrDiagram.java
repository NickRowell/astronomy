/*
 * Gaia CU5 DU10
 *
 * (c) 2005-2020 Gaia Data Processing and Analysis Consortium
 *
 *
 * CU5 photometric calibration software is free software; you can redistribute
 * it and/or modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * CU5 photometric calibration software is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this CU5 software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *
 *-----------------------------------------------------------------------------
 */

package projects.upc.exec;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
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
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import astrometry.DistanceFromParallax;
import astrometry.DistanceFromParallax.METHOD;
import photometry.util.PhotometryUtils;
import projects.upc.dm.SsaCrossMatch;
import projects.upc.dm.UpcStar;
import projects.upc.util.UpcUtils;
import projects.upc.util.XmUtil;
import util.GuiUtil;

/**
 * Presents a plot of the HR diagram of UPC stars.
 * 
 * @author nrowell
 * @version $Id$
 */
public class HrDiagram extends JPanel {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = -5750081449530288343L;

	/**
	 * The logger.
	 */
	private static final Logger logger = Logger.getLogger(HrDiagram.class.getName());

	/**
	 * Mapping of {@link UpcStar}s to {@link SsaCrossMatch} without any filtering, other
	 * than that the {@link SsaCrossMatch} exists and is acceptable.
	 */
	Map<UpcStar, SsaCrossMatch> upcStarCrossMatches;

	/**
	 * Mapping of Hipparcos subset of all {@link UpcStar}s to {@link SsaCrossMatch}.
	 */
	Map<UpcStar, SsaCrossMatch> hipStarCrossMatches;
	
	/**
	 * Mapping of subset of all {@link UpcStar}s to {@link SsaCrossMatch}, for which the
	 * UPC star has no cross match in an external catalogue.
	 */
	Map<UpcStar, SsaCrossMatch> unmatchedStarCrossMatches;
	
	/**
	 * Mapping of {@link UpcStar}s to {@link SsaCrossMatch} to plot.
	 */
	Map<UpcStar, SsaCrossMatch> starsToPlot;
	
	/**
	 * The {@link ChartPanel} presenting the plot.
	 */
	ChartPanel chartPanel;
	
	/**
	 * Use Hipparcos parallaxes where available (true) or use UPC parallaxes always (false)
	 */
	boolean useHip;
	
	/**
	 * Method of distance from parallax.
	 */
	METHOD method;
	
	/**
	 * Fractional parallax error threshold for filtering selection of data to plot.
	 */
	double fMax;
	
	/**
	 * Main constructor.
	 */
	public HrDiagram() {

		List<UpcStar> upcStars = UpcUtils.loadUpcCatalogue();
		List<UpcStar> hipStars = UpcUtils.getHipparcosSubset(upcStars);
		List<UpcStar> unmatchedStars = UpcUtils.getUnmatchedSubset(upcStars);
		
		upcStarCrossMatches = XmUtil.getUpcStarCrossMatchMap(upcStars);
		hipStarCrossMatches = XmUtil.getUpcStarCrossMatchMap(hipStars);
		unmatchedStarCrossMatches = XmUtil.getUpcStarCrossMatchMap(unmatchedStars);
		
		logger.info("Loaded "+upcStarCrossMatches.size()+" UpcStars with SSA cross matches");
		logger.info("Loaded "+hipStarCrossMatches.size()+" UpcStars with Hipparcos and SSA cross matches");
		logger.info("Loaded "+unmatchedStarCrossMatches.size()+" UpcStars with no parallax cross-match, and with SSA cross matches");
		
		starsToPlot = upcStarCrossMatches;
		
		useHip = false;
		method = METHOD.NAIVE;
		fMax = 1.0;
		
    	final JCheckBox plotAllCheckBox = new JCheckBox("Plot all UPC stars: ", true);
    	plotAllCheckBox.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				if(plotAllCheckBox.isSelected()) {
					starsToPlot = upcStarCrossMatches;
					updateChart();
				}
			}
    	});
    	
		final JCheckBox plotHipCheckBox = new JCheckBox("Plot Hipparcos stars only: ", false);
    	plotHipCheckBox.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				if(plotHipCheckBox.isSelected()) {
					starsToPlot = hipStarCrossMatches;
					updateChart();
				}
			}
    	});
    	
    	final JCheckBox plotUnmatchedCheckBox = new JCheckBox("Plot all stars with no external match: ", false);
    	plotUnmatchedCheckBox.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				if(plotUnmatchedCheckBox.isSelected()) {
					starsToPlot = unmatchedStarCrossMatches;
					updateChart();
				}
			}
    	});

    	final ButtonGroup bg = new ButtonGroup();
    	bg.add(plotHipCheckBox);
    	bg.add(plotAllCheckBox);
    	bg.add(plotUnmatchedCheckBox);
    	
    	JCheckBox useHipCheckBox = new JCheckBox("Use Hipparcos parallaxes when available", useHip);
    	useHipCheckBox.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				useHip = !useHip;
				updateChart();
			}
    	});
    	
    	final JComboBox<METHOD> methodComboBox = new JComboBox<METHOD>(METHOD.values());
    	methodComboBox.setSelectedItem(method);
    	methodComboBox.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent evt) 
             {
            	 method = (METHOD)methodComboBox.getSelectedItem();
            	 updateChart();
             }
    	});
    	
    	final JSlider fSlider = GuiUtil.buildSlider(0.0, 2.0, 5, "%3.3f");
		fSlider.setValue((int)Math.rint(100.0*fMax));
		final JLabel fLabel = new JLabel(getFLabel());
		// Create a change listener fot these
		ChangeListener cl = new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent e)
			{
				JSlider source = (JSlider)e.getSource();
				
				if(source==fSlider) {
					// Compute fractional parallax error from slider position
					double newF = (2.0 * source.getValue()/100.0);
					fMax = newF;
					fLabel.setText(getFLabel());
				}
				updateChart();
			}
		};
		fSlider.addChangeListener(cl);
		// Add a bit of padding to space things out
		fSlider.setBorder(new EmptyBorder(5,5,5,5));
    	
		// Present controls below the HR diagram
		JPanel controls = new JPanel(new GridLayout(3,3));
		controls.add(plotAllCheckBox);
		controls.add(plotHipCheckBox);
		controls.add(plotUnmatchedCheckBox);
		controls.add(new JLabel("Distance estimation method:"));
		controls.add(methodComboBox);
		controls.add(useHipCheckBox);
		controls.add(fLabel);
		controls.add(fSlider);
		
		// Initialise the ChartPanel
		updateChart();
		
		// Build the panel contents
		setLayout(new BorderLayout());
		add(chartPanel, BorderLayout.CENTER);
		add(controls, BorderLayout.SOUTH);
	}

	/**
	 * Get a label appropriate for the fractional parallax error threshold field.
	 * @return
	 * 	A label appropriate for the fractional parallax error threshold field.
	 */
	private String getFLabel() {
		return String.format("Fractional parallax error limit [%3.3f]:", fMax);
	}
	
	/**
	 * Update the {@link HrDiagram#chartPanel}.
	 */
	private void updateChart() {
		
		XYSeries series = new XYSeries("UPC HR diagram");
		
		for(Entry<UpcStar, SsaCrossMatch> xm : starsToPlot.entrySet()) {
			
			UpcStar upcStar = xm.getKey();
			SsaCrossMatch ssa = xm.getValue();
			
			// Get the SSA colours of the UPC star
			double b = ssa.ssaB;
			double r2 = ssa.ssaR2;
//			double i = ssa.ssaI;
			
			// Use the parallax to correct the apparent magnitude to absolute magnitude.
			
			// Extract the parallax and error to use
			double p = (useHip && upcStar.isHipparcosStar()) ? upcStar.srcPi : upcStar.absPi;
			double sigma_p = (useHip && upcStar.isHipparcosStar()) ? upcStar.srcPiErr : upcStar.absPiErr;
			
			// Filter on the fractional parallax error
			double f = sigma_p / p;
			if(f > fMax) {
				continue;
			}
			
			// Correct to arcseconds
			p /= 1000;
			sigma_p /= 1000;
			
			// Get the distance
			double d = DistanceFromParallax.getDistance(p, sigma_p, method);
			// Filter & convert to absolute magnitude
			if(d>0 && !Double.isInfinite(d)) {
				double B = PhotometryUtils.getAbsoluteMagnitude(d, b);
				series.add(b-r2, B);
			}
		}
		
		XYSeriesCollection data = new XYSeriesCollection();
		data.addSeries(series);
		
		// Set up the renderer
    	XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
    	
    	renderer.setSeriesLinesVisible(0, false);
        renderer.setSeriesShapesVisible(0, true);
        renderer.setSeriesShape(0, new Ellipse2D.Double(-0.5, -0.5, 1, 1));
        
        // Configure axes
        NumberAxis xAxis = new NumberAxis("B - R [mag]");
        xAxis.setRange(-1.0, 3.5);
        
        NumberAxis yAxis = new NumberAxis("B [mag]");
        yAxis.setInverted(true);
        yAxis.setRange(-5, 15);
        
        // Configure plot
        XYPlot xyplot = new XYPlot(data, xAxis, yAxis, renderer);
        xyplot.setBackgroundPaint(Color.white);
        
        JFreeChart chart = new JFreeChart("HR diagram of UPC stars with SSA cross-matches", xyplot);
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
		
		final JFrame frame = new JFrame("UPC HR diagram");
		
		SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
            	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setLayout(new BorderLayout());
                frame.add(new HrDiagram(), BorderLayout.CENTER);
                frame.setSize(1500, 750);
                frame.pack();
                frame.setVisible(true);
            }
        });
	}
	
}
