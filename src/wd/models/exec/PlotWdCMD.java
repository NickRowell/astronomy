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
 * This class plots the WD CMD using various WD cooling model implementations and various filters.
 * 
 * @author nrowell
 * @version $Id$
 */
public class PlotWdCMD {
	
	/**
	 * Cooling time range [yr], lower boundary.
	 */
	private static double minTcool = 1e7;
	
	/**
	 * Cooling time range [yr], upper boundary.
	 */
	private static double maxTcool = 10e9;
	
	/**
	 * Cooling time step size [yr] when plotting.
	 */
	private static double stepTcool = 1e7;
	
	/**
	 * The WD mass to plot [M_{solar}].
	 */
	private static double mass = 0.5;
	
	/**
	 * The WD atmosphere type to plot {H|He}.
	 */
	private static WdAtmosphereType atm = WdAtmosphereType.H;
	
	/**
	 * Passband for the absolute magnitude axis.
	 */
	private static Filter magFilter;

	/**
	 * Passbands (blue and red) to use for colour.
	 */
	private static Filter bFilter;
	private static Filter rFilter;
	
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
        
        JLabel magLabel = new JLabel("Band (M):");
        final JComboBox<Filter> magComboBox = new JComboBox<Filter>(Filter.values());
        magComboBox.setModel(new DefaultComboBoxModel<Filter>(wdCoolingModels.getWdCoolingModels().getPassbands()));
        magFilter = wdCoolingModels.getWdCoolingModels().getPassbands()[0];
        magComboBox.setSelectedItem(magFilter);
        
        JLabel bLabel = new JLabel("Band (B):");
        final JComboBox<Filter> bComboBox = new JComboBox<Filter>(Filter.values());
        bComboBox.setModel(new DefaultComboBoxModel<Filter>(wdCoolingModels.getWdCoolingModels().getPassbands()));
        bFilter = wdCoolingModels.getWdCoolingModels().getPassbands()[0];
        bComboBox.setSelectedItem(bFilter);
        
        JLabel rLabel = new JLabel("Band (R):");
        final JComboBox<Filter> rComboBox = new JComboBox<Filter>(Filter.values());
        rComboBox.setModel(new DefaultComboBoxModel<Filter>(wdCoolingModels.getWdCoolingModels().getPassbands()));
        rFilter = wdCoolingModels.getWdCoolingModels().getPassbands()[0];
        rComboBox.setSelectedItem(rFilter);
        
        final JLabel massLabel = new JLabel("WD mass: ("+String.format("%4.2f", mass)+"):");
        
        final double massMin = 0.4;
		final double massMax = 1.2;
		final JSlider massSlider = GuiUtil.buildSlider(massMin, massMax, 2, "%4.2f");

        JLabel label = new JLabel("");
        final ImageIcon icon = new ImageIcon(plotWdCMD());
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
            		magComboBox.setModel(new DefaultComboBoxModel<Filter>(wdCoolingModels.getWdCoolingModels().getPassbands()));
            		// Set to default filter for the cooling models
            		magFilter = wdCoolingModels.getWdCoolingModels().getPassbands()[0];
                    magComboBox.setSelectedItem(magFilter);
                    
                    bComboBox.setModel(new DefaultComboBoxModel<Filter>(wdCoolingModels.getWdCoolingModels().getPassbands()));
            		bFilter = wdCoolingModels.getWdCoolingModels().getPassbands()[0];
                    bComboBox.setSelectedItem(bFilter);
                    
                    rComboBox.setModel(new DefaultComboBoxModel<Filter>(wdCoolingModels.getWdCoolingModels().getPassbands()));
            		rFilter = wdCoolingModels.getWdCoolingModels().getPassbands()[0];
                    rComboBox.setSelectedItem(rFilter);
            	}
            	if(evt.getSource()==wdAtmComboBox)
            	{
            		atm = (WdAtmosphereType)wdAtmComboBox.getSelectedItem();
            	}
            	if(evt.getSource()==magComboBox)
            	{
            		magFilter = (Filter)magComboBox.getSelectedItem();
            	}
            	if(evt.getSource()==bComboBox)
            	{
            		bFilter = (Filter)bComboBox.getSelectedItem();
            	}
            	if(evt.getSource()==rComboBox)
            	{
            		rFilter = (Filter)rComboBox.getSelectedItem();
            	}
            	try {
					icon.setImage(plotWdCMD());
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
					icon.setImage(plotWdCMD());
					label.repaint();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
 
        };
        
        wdModelComboBox.addActionListener(al);
        wdAtmComboBox.addActionListener(al);
        magComboBox.addActionListener(al);
        bComboBox.addActionListener(al);
        rComboBox.addActionListener(al);
        massSlider.addChangeListener(cl);
        
        massSlider.setValue((int)Math.rint(100.0*(mass-massMin)/(massMax - massMin)));
        
        final JPanel buttonPanel = new JPanel(new GridLayout(6,2));
        buttonPanel.add(wdModelLabel);
        buttonPanel.add(wdModelComboBox);
        buttonPanel.add(wdAtmLabel);
        buttonPanel.add(wdAtmComboBox);
        buttonPanel.add(magLabel);
        buttonPanel.add(magComboBox);
        buttonPanel.add(bLabel);
        buttonPanel.add(bComboBox);
        buttonPanel.add(rLabel);
        buttonPanel.add(rComboBox);
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
	 * Makes a plot of the currently selected WD cooling models CMD.
	 * 
	 * @return
	 * 	A BufferedImage containing the plot.
	 * @throws IOException
	 * 	If there's an exception executing the Gnuplot script.
	 */
	private static BufferedImage plotWdCMD() throws IOException {

		WdCoolingModelSet wdCoolingModelSet = wdCoolingModels.getWdCoolingModels();
		
		// Number of points to plot for each type
		int nPoints = (int)Math.ceil((maxTcool - minTcool) / stepTcool);
		
		// Arrays of names of WD cooling models
		String name = wdCoolingModelSet.toString();
		
		// Extract the interpolated data
		WdCoolingModelGrid magModels = wdCoolingModelSet.getCoolingTracks(magFilter, atm);
		WdCoolingModelGrid bModels = wdCoolingModelSet.getCoolingTracks(bFilter, atm);
		WdCoolingModelGrid rModels = wdCoolingModelSet.getCoolingTracks(rFilter, atm);
		
		// Array of interpolated data points
		double[][] dataInterp = new double[nPoints][2];
		
		for(int d=0; d<nPoints; d++)
		{
			// Translate index to cooling time
			double tCool = minTcool + d*stepTcool;
			
			// Get the absolute magnitude
			double mag = magModels.quantity(tCool, mass);
			
			// Get the colour
			double b = bModels.quantity(tCool, mass);
			double r = rModels.quantity(tCool, mass);
			
			dataInterp[d][0] = b-r;
			dataInterp[d][1] = mag;
		}
		
		String script = 
				"set terminal pngcairo enhanced color size 640,480" + OSChecker.newline + 
				"set xrange [*:*]" + OSChecker.newline +
				"set yrange [*:*] reverse" + OSChecker.newline +
				"set key off" + OSChecker.newline +
				"set xtics out" + OSChecker.newline +
				"set ytics out" + OSChecker.newline +
				"set xlabel '"+bFilter.toString()+" - "+rFilter.toString()+" [mag]'" + OSChecker.newline +
				"set ylabel '"+magFilter.toString()+" [mag]'" + OSChecker.newline +
				"plot '-' u 1:2 w l lw 2 lc rgbcolor 'black'" + OSChecker.newline;
		
		// In-place data for the interpolated cooling models
		for(double[] series : dataInterp)
		{
			script += series[0] + " " + series[1] + OSChecker.newline;
		}
		script += "e" + OSChecker.newline;
		
		BufferedImage plot = Gnuplot.executeScript(script);
		
		return plot;
	}
}