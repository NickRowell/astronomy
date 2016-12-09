package projects.upc.exec;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import projections.Aitoff;
import projections.Projection;
import projections.util.ProjectionUtil;
import projects.upc.dm.UpcStar;
import projects.upc.util.UpcUtils;

/**
 * This application is used to load and examine the UPC catalogue data file.
 *
 * @author nrowell
 * @version $Id$
 */
public class SkyPlot extends JPanel {
	
	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = -2121329456773004768L;

	/**
	 * The logger.
	 */
	private static final Logger logger = Logger.getLogger(SkyPlot.class.getName());
	
	/**
	 * The list of all {@link UpcStar}s.
	 */
	List<UpcStar> upcStars;
	
	/**
	 * The list of all unmatched {@link UpcStar}s.
	 */
	List<UpcStar> unmatchedStars;

	/**
	 * The list of {@link UpcStar}s with Hipparcos counterparts.
	 */
	List<UpcStar> hipStars;
	
	/**
	 * The {@link ChartPanel} presenting the plot.
	 */
	ChartPanel chartPanel;
	
	/**
	 * Main constructor.
	 */
	public SkyPlot() {
		
		upcStars = UpcUtils.loadUpcCatalogue();
		hipStars = UpcUtils.getHipparcosSubset(upcStars);
		unmatchedStars = UpcUtils.getUnmatchedSubset(upcStars);

		logger.info("Loaded "+upcStars.size()+" stars from the UPC catalogue");
		logger.info("Loaded "+hipStars.size()+" stars with Hipparcos cross matches");
		logger.info("Loaded "+unmatchedStars.size()+" UpcStars with no parallax cross-match");

    	final JCheckBox plotAllCheckBox = new JCheckBox("Plot all UPC stars: ", true);
    	plotAllCheckBox.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				if(plotAllCheckBox.isSelected()) {
					updateChart(upcStars);
				}
			}
    	});
    	
		final JCheckBox plotHipCheckBox = new JCheckBox("Plot Hipparcos stars only: ", false);
    	plotHipCheckBox.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				if(plotHipCheckBox.isSelected()) {
					updateChart(hipStars);
				}
			}
    	});
    	
    	final JCheckBox plotUnmatchedCheckBox = new JCheckBox("Plot all stars with no external match: ", false);
    	plotUnmatchedCheckBox.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				if(plotUnmatchedCheckBox.isSelected()) {
					updateChart(unmatchedStars);
				}
			}
    	});
    	
    	final ButtonGroup bg = new ButtonGroup();
    	bg.add(plotHipCheckBox);
    	bg.add(plotAllCheckBox);
    	bg.add(plotUnmatchedCheckBox);
    	 
		// Present the X label and slider in a control panel
		JPanel controls = new JPanel(new GridLayout(1,3));
		controls.add(plotAllCheckBox);
		controls.add(plotHipCheckBox);
		controls.add(plotUnmatchedCheckBox);
		
		// Initialise the ChartPanel
		updateChart(upcStars);
		
		// Build the panel contents
		setLayout(new BorderLayout());
		add(chartPanel, BorderLayout.CENTER);
		add(controls, BorderLayout.SOUTH);
	}
	
	/**
	 * Displays a plot of the sky coordinates for all stars.
	 * @param upcStars
	 * 	List of all {@link UpcStar}s to plot.
	 */
	private void updateChart(List<UpcStar> starsToPlot) {
		
		logger.log(Level.FINE, "Plotting sky coordinates.");
		
		// Create array of sky coordinates in radians
		List<double[]> points = new ArrayList<>(starsToPlot.size());
		
		for(UpcStar star : starsToPlot) {
			points.add(new double[]{Math.toRadians(star.ra), Math.toRadians(star.dec)});
		}
		
		Projection proj = new Aitoff();

		JFreeChart chart = ProjectionUtil.makeJFreeChartPlot(points, "UPC Parallax Catalogue", proj);
		
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
		
		final JFrame frame = new JFrame("UPC Sky Projection");
		
		SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
            	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setLayout(new BorderLayout());
                frame.add(new SkyPlot(), BorderLayout.CENTER);
                frame.setSize(1500, 750);
                frame.pack();
                frame.setVisible(true);
            }
        });
	}
	
}
