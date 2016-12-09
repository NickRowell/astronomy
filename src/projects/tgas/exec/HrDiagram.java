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

package projects.tgas.exec;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import projects.tgas.dm.TgasStar;
import projects.tgas.util.TgasUtils;
import util.GuiUtil;
import utils.MagnitudeUtils;

/**
 * Presents a plot of the HR diagram of TGAS stars.
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
	 * Collection of all {@link TgasStar}s
	 */
	Collection<TgasStar> tgasStars;
	
	/**
	 * The {@link ChartPanel} presenting the HR diagram.
	 */
	ChartPanel hrDiagPanel;
	
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

		tgasStars = TgasUtils.loadTgasCatalogue();
		
		method = METHOD.NAIVE;
		fMax = 1.0;
		
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
		fSlider.setValue((int)Math.rint(100.0*fMax/2.0));
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
		JPanel controls = new JPanel(new GridLayout(2,2));
		controls.add(new JLabel("Distance estimation method:"));
		controls.add(methodComboBox);
		controls.add(fLabel);
		controls.add(fSlider);
		
		// Initialise the ChartPanel
		updateChart();
		
		// Build the panel contents
		setLayout(new BorderLayout());
		add(hrDiagPanel, BorderLayout.CENTER);
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
	 * Update the {@link HrDiagram#hrDiagPanel}.
	 */
	private void updateChart() {
		
		XYSeries series = new XYSeries("TGAS HR diagram");
		
		// Count the stars
		int n=0;
		
		for(TgasStar tgasStar : tgasStars) {
			
			// Get the TGAS magnitude and colour indices
			double g = tgasStar.phot_g_mean_mag;
			double bv = tgasStar.bt_mag - tgasStar.vt_mag;
			
			// Use the parallax to correct the apparent magnitude to absolute magnitude.
			
			// Extract the parallax and error to use
			double p = tgasStar.parallax;
			double sigma_p = tgasStar.parallax_error;
			
			// Filter on the fractional parallax error
			double f = sigma_p / Math.abs(p);
			if(f > fMax) {
				continue;
			}
			
			// Filter out objects with no B-V index
			if(bv==0.0) {
				continue;
			}
			
			// Correct to arcseconds
			p /= 1000;
			sigma_p /= 1000;
			
			// Get the distance
			double d = DistanceFromParallax.getDistance(p, sigma_p, method);
			
			// Filter & convert to absolute magnitude
			if(d>0 && !Double.isInfinite(d)) {
				
				n++;
				
				double G = MagnitudeUtils.getAbsoluteMagnitude(d, g);
				series.add(bv, G);
			}
		}

		logger.log(Level.INFO, "Plotting "+n+" Hipparcos stars.");
		
        JFreeChart hrChart = getHrChart(series);
        if(hrDiagPanel==null) {
        	// Branch is used on initialisation
        	hrDiagPanel = new ChartPanel(hrChart);
        }
        else {
        	hrDiagPanel.setChart(hrChart);
        }
	}
	
	/**
	 * Plot the HR diagram chart from the given data.
	 * @param series
	 * 	A {@link XYSeries} containing the HR diagram data.
	 * @return
	 * 	A {@link JFreeChart} containing the plot.
	 */
	private static JFreeChart getHrChart(XYSeries series) {

		XYSeriesCollection hrData = new XYSeriesCollection();
		hrData.addSeries(series);
		
		// Set up the renderer
    	XYLineAndShapeRenderer hrRenderer = new XYLineAndShapeRenderer();
    	
    	hrRenderer.setSeriesLinesVisible(0, false);
        hrRenderer.setSeriesShapesVisible(0, true);
        hrRenderer.setSeriesShape(0, new Ellipse2D.Double(-0.5, -0.5, 1, 1));
        
        // Configure axes
        NumberAxis xAxis = new NumberAxis("B - V [mag]");
        xAxis.setRange(-0.5, 2.25);
        
        NumberAxis yAxis = new NumberAxis("G [mag]");
        yAxis.setInverted(true);
        yAxis.setRange(-5, 13);
        
        // Configure plot
        XYPlot xyplot = new XYPlot(hrData, xAxis, yAxis, hrRenderer);
        xyplot.setBackgroundPaint(Color.white);
        
        JFreeChart hrChart = new JFreeChart("HR diagram of TGAS stars", xyplot);
        hrChart.removeLegend();
        hrChart.setBackgroundPaint(Color.white);
        
        return hrChart;
	}
	
	
	
	/**
	 * Main application entry point.
	 * @param args
	 * 	The args - ignored.
	 */
	public static void main(String[] args) {
		
		final JFrame frame = new JFrame("TGAS HR diagram");
		
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
