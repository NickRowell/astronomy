package wd.models.exec;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.NavigableMap;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import infra.gui.IPanel;
import infra.io.Gnuplot;
import infra.os.OSChecker;
import numeric.functions.MonotonicLinear;
import photometry.Filter;
import util.GuiUtil;
import wd.models.algo.WdCoolingModelGrid;
import wd.models.algo.WdCoolingModelSet;
import wd.models.infra.WdAtmosphereType;
import wd.models.infra.WdCoolingModels;

/**
 * Plots the (inverted) magnitude as a function of cooling time.
 * 
 * @author nrowell
 * @version $Id$
 */
public class PlotInvertedWdCoolingModels {
	
	/**
	 * Magnitude range [mag], lower boundary.
	 */
	private static double minMag = 7;
	
	/**
	 * Magnitude range [mag], upper boundary.
	 */
	private static double maxMag = 16.5;
	
	/**
	 * Magnitude step size [mag] when plotting.
	 */
	private static double stepMag = 0.1;
	
	/**
	 * The WD mass to plot [M_{solar}].
	 */
	private static double mass = 0.5;
	
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
        
        final JLabel massLabel = new JLabel("WD mass: ("+String.format("%4.2f", mass)+"):");
        
        final double massMin = 0.4;
		final double massMax = 1.2;
		final JSlider massSlider = GuiUtil.buildSlider(massMin, massMax, 2, "%4.2f");
		
		final IPanel ipanel = new IPanel(plotWdCoolingModels());
		
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
					ipanel.setImage(plotWdCoolingModels());
				} catch (IOException e) {
					e.printStackTrace();
				}
            }
        };
        ChangeListener cl = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent evt) {
				JSlider source = (JSlider)evt.getSource();
				
				if(source==massSlider) {
					// Compute mass from slider position
					mass = (massMin + (massMax - massMin)*(source.getValue()/100.0));
					massLabel.setText("WD mass: ("+String.format("%4.2f", mass)+"):");
				}
				try {
					ipanel.setImage(plotWdCoolingModels());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
 
        };
        
        wdModelComboBox.addActionListener(al);
        wdAtmComboBox.addActionListener(al);
        filterComboBox.addActionListener(al);
        massSlider.addChangeListener(cl);
        
        massSlider.setValue((int)Math.rint(100.0*(mass-massMin)/(massMax - massMin)));
        
        final JPanel buttonPanel = new JPanel(new GridLayout(4,2));
        buttonPanel.add(wdModelLabel);
        buttonPanel.add(wdModelComboBox);
        buttonPanel.add(wdAtmLabel);
        buttonPanel.add(wdAtmComboBox);
        buttonPanel.add(filterLabel);
        buttonPanel.add(filterComboBox);
        buttonPanel.add(massLabel);
        buttonPanel.add(massSlider);
        
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
                    tester.add(ipanel, BorderLayout.CENTER);
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
		
		// Number of points to plot for each type
		int nPoints = (int)Math.ceil((maxMag - minMag) / stepMag);
		
		// Arrays of names of WD cooling models
		String name = wdCoolingModels.toString();
		
		// Extract the interpolated data
		WdCoolingModelGrid wdCoolingModelGrid = wdCoolingModelSet.getCoolingTracks(filter, atm);
		
		// Array of interpolated data points
		double[][] dataInterp = new double[nPoints][2];
		
		for(int d=0; d<nPoints; d++)
		{
			// Translate index to magnitude
			double mag = minMag + d*stepMag;
			
			// Get the cooling time
			double tCool = wdCoolingModelGrid.tcool(mag, mass);
			
			dataInterp[d][0] = mag;
			dataInterp[d][1] = tCool;
		}
		
		// Array of raw data points. A bit trickier to assemble.
		int nTracks = wdCoolingModelSet.getMassGridPoints(atm).length;
		double[][][] dataRaw = new double[nTracks][][];
		
		NavigableMap<Double, MonotonicLinear> tracks = wdCoolingModelGrid.mbolAsFnTcoolByMass;
		
		// Loop over mass of each internal cooling track
		int trackIdx = 0;
		for(Entry<Double, MonotonicLinear> trackEntry : tracks.entrySet()) {
			
			// use this to apply name to tracks?
//			double mass = trackEntry.getKey();
			MonotonicLinear track = trackEntry.getValue();
			
			// Get the number of points in this track:
			int nMassPoints = track.X.length;
			
			double[][] trackData = new double[nMassPoints][2];
			
			for(int i=0; i<nMassPoints; i++) {
				// Magnitude
				trackData[i][0] = track.Y[i];
				// Cooling time
				trackData[i][1] = track.X[i];
			}
			dataRaw[trackIdx++] = trackData;
		}
		
		String script = 
				"set terminal pngcairo enhanced color size 640,480" + OSChecker.newline + 
				"set xrange ["+minMag+":"+maxMag+"]" + OSChecker.newline +
				"set yrange [*:*]" + OSChecker.newline +
				"set key top left" + OSChecker.newline +
				"set xtics out" + OSChecker.newline +
				"set ytics out" + OSChecker.newline +
				"set xlabel 'Magnitude ("+filter.toString()+") [mag]'" + OSChecker.newline +
				"set ylabel 'Cooling time [yr]'" + OSChecker.newline +
				"plot ";
		
		// Plot commands for interpolated WD cooling models
		script += "'-' u 1:2 w l lw 2 lc rgbcolor 'black' title '"+name+"',";
		
		// Plot commands for raw data points
		for(int i=0; i<nTracks; i++) {
			script += "'-' u 1:2 w p pt 5 ps 0.25 notitle";
			if(i!=nTracks-1) {
				script += ",";
			}
		}
		
		script += OSChecker.newline;
		
		// In-place data for the interpolated cooling models
		for(double[] series : dataInterp)
		{
			script += series[0] + " " + series[1]/1e9 + OSChecker.newline;
		}
		script += "e" + OSChecker.newline;
		
		// In-place data for the raw cooling models
		for(int i=0; i<nTracks; i++) {
			for(int j=0; j<dataRaw[i].length; j++)
			{
				script += dataRaw[i][j][0] + " " + dataRaw[i][j][1]/1e9 + OSChecker.newline;
			}
			script += "e" + OSChecker.newline;
		}
		
		BufferedImage plot = Gnuplot.executeScript(script);
		
		return plot;
	}
}