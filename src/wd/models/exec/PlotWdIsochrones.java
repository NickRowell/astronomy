package wd.models.exec;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import infra.io.Gnuplot;
import infra.os.OSChecker;
import photometry.Filter;
import photometry.util.PhotometryUtils;
import util.CharUtil;
import wd.models.algo.WdCoolingModelGrid;
import wd.models.algo.WdCoolingModelSet;
import wd.models.infra.AtmosphereParameter;
import wd.models.infra.WdAtmosphereType;
import wd.models.infra.WdCoolingModels;

/**
 * This class provides a simple application that plots WD isochrones.
 * 
 * @author nrowell
 * @version $Id$
 */
public class PlotWdIsochrones {
	
	/**
	 * Minimum log(t_cool [yr]).
	 */
	private static double minLogTcool = 7.5;
	
	/**
	 * Minimum log(t_cool [yr]).
	 */
	private static double maxLogTcool = 11;
	
	/**
	 * Step in log(t_cool [yr]).
	 */
	private static double stepLogTcool = 0.02;
	
	/**
	 * Masses to plot along each isochrone
	 */
	private static double[] masses = {0.4, 0.45, 0.5, 0.55, 0.6, 0.65, 0.7, 0.75, 0.8, 0.85, 0.9, 0.95, 1.0, 1.05, 1.1, 1.15, 1.2, 1.25, 1.3};
	
	/**
	 * The WD atmosphere type to plot {H|He}.
	 */
	private static WdAtmosphereType atm = WdAtmosphereType.H;
	
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
        
        JLabel label = new JLabel("");
        final ImageIcon icon = new ImageIcon(plotWdIsochrones());
        label.setIcon(icon);
		
        ActionListener al = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) 
            {
            	if(evt.getSource()==wdModelComboBox)
            	{
            		wdCoolingModels = (WdCoolingModels)wdModelComboBox.getSelectedItem();
            	}
            	if(evt.getSource()==wdAtmComboBox)
            	{
            		atm = (WdAtmosphereType)wdAtmComboBox.getSelectedItem();
            	}
            	try {
            		icon.setImage(plotWdIsochrones());
            		label.repaint();
				} catch (IOException e) {
					e.printStackTrace();
				}
            }
        };
        
        
        wdModelComboBox.addActionListener(al);
        wdAtmComboBox.addActionListener(al);
        
        final JPanel buttonPanel = new JPanel(new GridLayout(2,2));
        buttonPanel.add(wdModelLabel);
        buttonPanel.add(wdModelComboBox);
        buttonPanel.add(wdAtmLabel);
        buttonPanel.add(wdAtmComboBox);
        
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
	private static BufferedImage plotWdIsochrones() throws IOException {

		WdCoolingModelSet wdCoolingModelSet = wdCoolingModels.getWdCoolingModels();
		
		// Extract the interpolated data
		WdCoolingModelGrid mbolGrid = wdCoolingModelSet.getCoolingTracks(Filter.M_BOL, atm);
		WdCoolingModelGrid teffGrid = wdCoolingModelSet.getCoolingTracks(AtmosphereParameter.TEFF, atm);
		
		StringBuilder script = new StringBuilder();
		script.append("set terminal pngcairo enhanced color size 640,480\n");
		script.append("set xrange [4.9:3.2]\n");
		script.append("set yrange [-6:-0.6]\n");
		script.append("set key off\n");
		script.append("set xtics in\n");
		script.append("set mxtics 5\n");
		script.append("set xlabel 'log(T_{eff}[K])\n");
		script.append("set ytics 1\n");
		script.append("set mytics 5\n");
		script.append("set ylabel 'log(L/L_{"+CharUtil.solar+"})\n");
		script.append("plot '-' u 1:2 w l\n");
		
		for(double logTcool = minLogTcool; logTcool < maxLogTcool; logTcool += stepLogTcool) {
			
			// Compute isochrone
			double tcool = Math.pow(10, logTcool);
			
			for(double mass : masses) {
				
				// Interpolate M_{bol} and T_{eff} at this mass and cooling time
				double mbol = mbolGrid.quantity(tcool, mass);
				double teff = teffGrid.quantity(tcool, mass);
				
				// Transfrom M_{bol} to log(L/L_{solar})
				double logLL0 = PhotometryUtils.mbolToLogLL0(mbol);
				
				script.append(Math.log10(teff) + "\t" + logLL0 + OSChecker.newline);
			}
			script.append(OSChecker.newline + OSChecker.newline);
		}
		script.append("e" + OSChecker.newline);
		
		BufferedImage plot = Gnuplot.executeScript(script.toString());
		
		return plot;
	}
}