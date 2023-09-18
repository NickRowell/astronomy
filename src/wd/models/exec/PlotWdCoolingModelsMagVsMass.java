package wd.models.exec;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import infra.io.Gnuplot;
import infra.os.OSChecker;
import photometry.Filter;
import util.CharUtil;
import util.GuiUtil;
import wd.models.algo.WdCoolingModelGrid;
import wd.models.algo.WdCoolingModelSet;
import wd.models.infra.WdAtmosphereType;
import wd.models.infra.WdCoolingModels;

/**
 * This class plot the various WD cooling model implementations. It should:
 * 
 * @author nrowell
 * @version $Id$
 */
public class PlotWdCoolingModelsMagVsMass {
	
	/**
	 * WD mass range [M_{solar}], lower boundary.
	 */
//	private static double minWdMass = 0.4;
	
	/**
	 * WD mass range [M_{solar}], upper boundary.
	 */
//	private static double maxWdMass = 1.3;
	
	/**
	 * WD mass step size [M_{solar}] when plotting.
	 */
	private static double stepWdMass = 0.05;
	
	/**
	 * The cooling time [Gyr].
	 */
	private static double tCool = 3e9;
	
	/**
	 * The WD atmosphere type to plot {H|He}.
	 */
	private static WdAtmosphereType atm = WdAtmosphereType.H;
	
	/**
	 * The filter to plot. This is initialised programmatically according to what filters are
	 * supported by the default WD cooling models.
	 */
	private static Filter filter;
	
	/**
	 * The WD models to plot.
	 */
	private static WdCoolingModels wdCoolingModels = WdCoolingModels.MONTREAL;
	
	/**
	 * The main application entry point.
	 * 
	 * @param args
	 * 	The args; not used.
	 * @throws IOException
	 * 	If there's an error executing the Gnuplot script.
	 */
	public static void main(String[] args) throws IOException
	{
		
		JLabel wdModelLabel = new JLabel("WD cooling models:");
        final JComboBox<WdCoolingModels> wdModelComboBox = new JComboBox<WdCoolingModels>(WdCoolingModels.values());
        wdModelComboBox.setSelectedItem(wdCoolingModels);
        
        JLabel wdAtmLabel = new JLabel("WD atmosphere type:");
        final JComboBox<WdAtmosphereType> wdAtmComboBox = new JComboBox<WdAtmosphereType>(WdAtmosphereType.values());
        wdAtmComboBox.setSelectedItem(atm);
        
        JLabel filterLabel = new JLabel("Filter:");
        final JComboBox<Filter> filterComboBox = new JComboBox<Filter>(Filter.values());
        filterComboBox.setModel(new DefaultComboBoxModel<Filter>(wdCoolingModels.getWdCoolingModels().getPassbands()));
        // Initialise the selected Filter to the first entry in the set of filters supported by the cooling models
        filter = wdCoolingModels.getWdCoolingModels().getPassbands()[0];
        filterComboBox.setSelectedItem(filter);
        
        final JLabel tCoolLabel = new JLabel("Cooling time [Gyr]: ("+String.format("%4.2f", tCool/1e9)+"):");
        
        final double tCoolMin = 0.0;
		final double tCoolMax = 13.0;
		final JSlider tCoolSlider = GuiUtil.buildSlider(tCoolMin, tCoolMax, 2, "%4.2f");

        JLabel label = new JLabel("");
        final ImageIcon icon = new ImageIcon(plotWdCoolingModels());
        label.setIcon(icon);
		
        ActionListener al = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) 
            {
            	if(evt.getSource()==wdModelComboBox)
            	{
            		wdCoolingModels = (WdCoolingModels)wdModelComboBox.getSelectedItem();
            		
            		// Changed the cooling models - need to update the range of Filters available
            		// according to what the new set support
            		filterComboBox.setModel(new DefaultComboBoxModel<Filter>(wdCoolingModels.getWdCoolingModels().getPassbands()));
            		// Set to default filter for the cooling models
            		filter = wdCoolingModels.getWdCoolingModels().getPassbands()[0];
                    filterComboBox.setSelectedItem(filter);
            	}
            	if(evt.getSource()==wdAtmComboBox)
            	{
            		atm = (WdAtmosphereType)wdAtmComboBox.getSelectedItem();
            	}
            	if(evt.getSource()==filterComboBox)
            	{
            		filter = (Filter)filterComboBox.getSelectedItem();
            	}
            	try {
					icon.setImage(plotWdCoolingModels());
					label.repaint();
				} catch (IOException e) {
					e.printStackTrace();
				}
            }
        };
        ChangeListener cl = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent evt) {
				JSlider source = (JSlider)evt.getSource();
				
				if(source==tCoolSlider) {
					// Compute mass from slider position
					tCool = 1e9 * (tCoolMin + (tCoolMax - tCoolMin)*(source.getValue()/100.0));
					tCoolLabel.setText("Cooling time [Gyr]: ("+String.format("%4.2f", tCool/1e9)+"):");
				}
				try {
					icon.setImage(plotWdCoolingModels());
					label.repaint();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
 
        };
        
        wdModelComboBox.addActionListener(al);
        wdAtmComboBox.addActionListener(al);
        filterComboBox.addActionListener(al);
        tCoolSlider.addChangeListener(cl);
        
        tCoolSlider.setValue((int)Math.rint(100.0*(tCool/1e9-tCoolMin)/(tCoolMax - tCoolMin)));
        
        final JPanel buttonPanel = new JPanel(new GridLayout(4,2));
        buttonPanel.add(wdModelLabel);
        buttonPanel.add(wdModelComboBox);
        buttonPanel.add(wdAtmLabel);
        buttonPanel.add(wdAtmComboBox);
        buttonPanel.add(filterLabel);
        buttonPanel.add(filterComboBox);
        buttonPanel.add(tCoolLabel);
        buttonPanel.add(tCoolSlider);
        
		// Create and display the form
        java.awt.EventQueue.invokeLater(
            new Runnable() 
            {
                @Override
                public void run() 
                {
                    JFrame tester = new JFrame();
                    tester.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    tester.setLayout(new BorderLayout());
                    tester.add(label, BorderLayout.CENTER);
                    tester.add(buttonPanel, BorderLayout.SOUTH);
                    tester.pack();
                    tester.setVisible(true);
                }
            });
	}

	/**
	 * Makes a plot of the currently selected WD cooling models.
	 * 
	 * @return
	 * 	A BufferedImage containing the plot.
	 * @throws IOException
	 * 	If there's an exception executing the Gnuplot script.
	 */
	private static BufferedImage plotWdCoolingModels() throws IOException {

		WdCoolingModelSet wdCoolingModelSet = wdCoolingModels.getWdCoolingModels();
		
		// Array of raw data points. A bit trickier to assemble.
		double[] massGrid = wdCoolingModelSet.getMassGridPoints(atm);
		
		double minWdMass = massGrid[0];
		double maxWdMass = massGrid[massGrid.length - 1];
		
		// Number of points to plot for each type
		int nPoints = (int)Math.ceil((maxWdMass - minWdMass) / stepWdMass) + 1;
		
		// Arrays of names of WD cooling models
		String name = wdCoolingModelSet.toString();
		
		// Extract the interpolated data
		WdCoolingModelGrid wdCoolingModelGrid = wdCoolingModelSet.getCoolingTracks(filter, atm);
		
		// Array of interpolated data points
		double[][] dataInterp = new double[nPoints][2];
		
		for(int idx=0; idx<nPoints; idx++)
		{
			// Translate index to WD mass
			double wdMass = minWdMass + idx*stepWdMass;
			
			// Get the bolometric magnitude
			double mag = wdCoolingModelGrid.quantity(tCool, wdMass);
			
			dataInterp[idx][0] = wdMass;
			dataInterp[idx][1] = mag;
		}
		
		
		
		double[][] dataRaw = new double[massGrid.length][2];
		
		// Loop over mass of each internal cooling track
		for(int idx = 0; idx < massGrid.length; idx++) {
			
			double wdMass = massGrid[idx];
			double mag = wdCoolingModelGrid.quantity(tCool, wdMass);
			
			dataRaw[idx][0] = wdMass;
			dataRaw[idx][1] = mag;
		}
		
		String script = 
				"set terminal pngcairo enhanced color size 640,480" + OSChecker.newline + 
				"set xrange ["+minWdMass+":"+maxWdMass+"]" + OSChecker.newline +
				"set yrange [6:20]" + OSChecker.newline +
				"set key bottom right" + OSChecker.newline +
				"set xtics out" + OSChecker.newline +
				"set ytics out" + OSChecker.newline +
				"set xlabel 'WD mass [M_{"+CharUtil.solar+"}]'" + OSChecker.newline +
				"set ylabel 'Magnitude ("+filter.toString()+") [mag]'" + OSChecker.newline +
				"plot ";
		
		// Plot commands for interpolated WD cooling model
		script += "'-' u 1:2 w l lw 2 lc rgbcolor 'black' title '"+name+"',";
		
		// Plot commands for raw data points
		script += "'-' u 1:2 w p pt 5 ps 1.0 notitle";
		
		script += OSChecker.newline;
		
		// In-place data for the interpolated cooling models
		for(double[] point : dataInterp)
		{
			script += point[0] + " " + point[1] + OSChecker.newline;
		}
		script += "e" + OSChecker.newline;
		
		// In-place data for the raw cooling models
		for(double[] point : dataRaw)
		{
			script += point[0] + " " + point[1] + OSChecker.newline;
		}
		script += "e" + OSChecker.newline;
		
		BufferedImage plot = Gnuplot.executeScript(script);
		
		return plot;
	}
}