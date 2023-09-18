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
import numeric.functions.MonotonicLinear;
import photometry.Filter;
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
public class PlotWdCoolingModelsMagVsTcool {
	
	/**
	 * Cooling time range [yr], lower boundary.
	 */
	private static double minTcool = 1e8;
	
	/**
	 * Cooling time range [yr], upper boundary.
	 */
	private static double maxTcool = 13e9;
	
	/**
	 * Cooling time step size [yr] when plotting.
	 */
	private static double stepTcool = 1e8;
	
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
				
				if(source==massSlider) {
					// Compute mass from slider position
					mass = (massMin + (massMax - massMin)*(source.getValue()/100.0));
					massLabel.setText("WD mass: ("+String.format("%4.2f", mass)+"):");
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
		
		// Number of points to plot for each type
		int nPoints = (int)Math.ceil((maxTcool - minTcool) / stepTcool);
		
		// Arrays of names of WD cooling models
		String name = wdCoolingModelSet.toString();
		
		// Extract the interpolated data
		WdCoolingModelGrid wdCoolingModelGrid = wdCoolingModelSet.getCoolingTracks(filter, atm);
		
		// Array of interpolated data points
		double[][] dataInterp = new double[nPoints][2];
		
		for(int d=0; d<nPoints; d++)
		{
			// Translate index to cooling time
			double tCool = minTcool + d*stepTcool;
			
			// Get the bolometric magnitude
			double mag = wdCoolingModelGrid.quantity(tCool, mass);
			
			dataInterp[d][0] = tCool;
			dataInterp[d][1] = mag;
		}
		
		// Array of raw data points. A bit trickier to assemble.
		int nTracks = wdCoolingModelSet.getMassGridPoints(atm).length;
		double[][][] dataRaw = new double[nTracks][][];
		
		NavigableMap<Double, MonotonicLinear> tracks = wdCoolingModelGrid.quantityAsFnTcoolByMass;
		
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
				// Cooling time
				trackData[i][0] = track.X[i];
				// Magnitude
				trackData[i][1] = track.Y[i];
			}
			dataRaw[trackIdx++] = trackData;
		}
		
		String script = 
				"set terminal pngcairo enhanced color size 640,480" + OSChecker.newline + 
				"set xrange [0:"+maxTcool/1e9+"]" + OSChecker.newline +
				"set yrange [6:20]" + OSChecker.newline +
				"set key bottom right" + OSChecker.newline +
				"set xtics out" + OSChecker.newline +
				"set ytics out" + OSChecker.newline +
				"set xlabel 'Cooling time [Gyr]'" + OSChecker.newline +
				"set ylabel 'Magnitude ("+filter.toString()+") [mag]'" + OSChecker.newline +
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
			script += series[0]/1e9 + " " + series[1] + OSChecker.newline;
		}
		script += "e" + OSChecker.newline;
		
		// In-place data for the raw cooling models
		for(int i=0; i<nTracks; i++) {
			for(int j=0; j<dataRaw[i].length; j++)
			{
				script += dataRaw[i][j][0]/1e9 + " " + dataRaw[i][j][1] + OSChecker.newline;
			}
			script += "e" + OSChecker.newline;
		}
		
		BufferedImage plot = Gnuplot.executeScript(script);
		
		return plot;
	}
}