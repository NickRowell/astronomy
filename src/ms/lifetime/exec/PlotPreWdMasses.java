package ms.lifetime.exec;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.NavigableMap;

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
import ms.lifetime.algo.PreWdLifetime;
import ms.lifetime.infra.PreWdLifetimeModels;
import numeric.functions.MonotonicFunction1D;
import numeric.functions.MonotonicLinear;
import util.GuiUtil;

/**
 * This class provides an application that plots the pre-WD mass as a function of
 * the lifetime, for arbitrary metallicity.
 * 
 * @author nrowell
 * @version $Id$
 */
public class PlotPreWdMasses {
	
	/**
	 * Lifetime lower limit [Gyr]
	 */
	private static double minLifeTime = 0.0;
	
	/**
	 * Lifetime upper limit [Gyr]
	 */
	private static double maxLifeTime = 16.0;
	
	/**
	 * Lifetime step size for plotting [Gyr]
	 */
	private static double stepLifeTime = 0.1;
	
	/**
	 * The current Z value (metallicity)
	 */
	private static double z = 0.017;
	private static double zMin = 0.00000;
	private static double zMax = 0.09;
	
	/**
	 * The current Y value (helium content)
	 */
	private static double y = 0.3;
	private static double yMin = 0.20;
	private static double yMax = 0.44;
	
	/**
	 * The {@link PreWdLifetime} to plot
	 */
	private static PreWdLifetimeModels preWdLifetimes = PreWdLifetimeModels.PADOVA;
	
	/**
	 * The main application entry point.
	 * 
	 * @param args
	 * 	The args; not used.
	 * @throws IOException
	 * 	If there's an error executing the Gnuplot script.
	 */
	public static void main(String[] args) throws IOException {
		
		JLabel preWdModelLabel = new JLabel("Pre-WD lifetime models:");
        final JComboBox<PreWdLifetimeModels> preWdLifetimeModelComboBox = 
        		new JComboBox<PreWdLifetimeModels>(PreWdLifetimeModels.values());
        preWdLifetimeModelComboBox.setSelectedItem(preWdLifetimes);
        
        final JLabel zLabel = new JLabel(getZLabel());
        final JLabel yLabel = new JLabel(getYLabel());
        
		final JSlider zSlider = GuiUtil.buildSlider(zMin, zMax, 2, "%4.2f");
		final JSlider ySlider = GuiUtil.buildSlider(yMin, yMax, 2, "%4.2f");
		
		final IPanel ipanel = new IPanel(plotPreWdMasses());
		
        ActionListener al = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) 
            {
            	if(evt.getSource()==preWdLifetimeModelComboBox)
            	{
            		preWdLifetimes = (PreWdLifetimeModels)preWdLifetimeModelComboBox.getSelectedItem();
            	}
            	try {
					ipanel.setImage(plotPreWdMasses());
				} catch (IOException e) {
					e.printStackTrace();
				}
            }
        };
        ChangeListener cl = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent evt) {
				JSlider source = (JSlider)evt.getSource();
				
				if(source==zSlider) {
					// Compute Z from slider position
					z = (zMin + (zMax - zMin)*(source.getValue()/100.0));
					zLabel.setText(getZLabel());
				}
				if(source==ySlider) {
					// Compute Y from slider position
					y = (yMin + (yMax - yMin)*(source.getValue()/100.0));
					yLabel.setText(getYLabel());
				}
				try {
					ipanel.setImage(plotPreWdMasses());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
 
        };
        
        preWdLifetimeModelComboBox.addActionListener(al);
        zSlider.addChangeListener(cl);
        ySlider.addChangeListener(cl);
        
        zSlider.setValue((int)Math.rint(100.0*(z-zMin)/(zMax - zMin)));
        ySlider.setValue((int)Math.rint(100.0*(y-yMin)/(yMax - yMin)));
        
        final JPanel buttonPanel = new JPanel(new GridLayout(3,2));
        buttonPanel.add(preWdModelLabel);
        buttonPanel.add(preWdLifetimeModelComboBox);
        buttonPanel.add(zLabel);
        buttonPanel.add(zSlider);
        buttonPanel.add(yLabel);
        buttonPanel.add(ySlider);
        
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
	 * Get a label appropriate for the Z field.
	 * @return
	 * 	A label appropriate for the Z field.
	 */
	private static String getZLabel() {
		return "Metallicity (Z): ("+String.format("%6.4f", z)+"):";
	}
	
	/**
	 * Get a label appropriate for the Y field.
	 * @return
	 * 	A label appropriate for the Y field.
	 */
	private static String getYLabel() {
		return "Helium content (Y): ("+String.format("%6.4f", y)+"):";
	}
	
	/**
	 * Makes a plot of the currently selected {@link }.
	 * 
	 * @return
	 * 	A BufferedImage containing the plot.
	 * @throws IOException
	 * 	If there's an exception executing the Gnuplot script.
	 */
	private static BufferedImage plotPreWdMasses() throws IOException {

		PreWdLifetime preWdLifetime = preWdLifetimes.getPreWdLifetimeModels();
		
		// Number of points to plot
		int nPoints = (int)Math.ceil((maxLifeTime - minLifeTime) / stepLifeTime);
		
		// Name of models set
		String name = preWdLifetime.toString();
		
		// Array of interpolated data points
		double[][] dataInterp = new double[nPoints][2];
		
		for(int d=0; d<nPoints; d++)
		{
			// Translate index to lifetime
			double lifetimeGyr = minLifeTime + d*stepLifeTime;
			
			// Get the stellar mass [M_{Solar}]
			double mass = preWdLifetime.getStellarMass(z, y, lifetimeGyr*1E9)[0];
			
			dataInterp[d][0] = lifetimeGyr;
			dataInterp[d][1] = mass;
		}
		
		// Array of raw data points. A bit trickier to assemble.
		NavigableMap<Double, NavigableMap<Double, MonotonicFunction1D>> lifetimeAsFnMassByMetallicity = 
				preWdLifetime.lifetimeAsFnMassByMetallicity;
		
		// Calculate the number of distinct metallicity tracks
		int nMetals = 0;
		for(Entry<Double, NavigableMap<Double, MonotonicFunction1D>> outer : lifetimeAsFnMassByMetallicity.entrySet()) {
			nMetals += outer.getValue().size();
		}
		
		// Create arrays to store refactored data
		String[] titles = new String[nMetals];
		double[][][] data = new double[nMetals][2][];
		
		// Read the tracks out
		int index = 0;
		for(Entry<Double, NavigableMap<Double, MonotonicFunction1D>> outer : lifetimeAsFnMassByMetallicity.entrySet()) {
			double z = outer.getKey();
			for(Entry<Double, MonotonicFunction1D> inner : outer.getValue().entrySet()) {
				double y = inner.getKey();
				titles[index] = String.format("Z=%f Y=%f", z, y);
				// NOTE: this will stop working if we don't use MonotonicLinear to interpolate the lifetimes.
				// Might be better to introduce a new type that encapsulates an interpolated set of points.
				MonotonicLinear track = (MonotonicLinear)inner.getValue();
				data[index][0] = track.Y;
				data[index][1] = track.X;
				index++;
			}
		}
		
		String script = 
				"set terminal pngcairo enhanced color size 640,480" + OSChecker.newline + 
				"set xrange [0:"+maxLifeTime+"]" + OSChecker.newline +
				"set yrange [*:*]" + OSChecker.newline +
				"set key top right" + OSChecker.newline +
				"set xtics out" + OSChecker.newline +
				"set ytics out" + OSChecker.newline +
				"set xlabel 'Pre-WD lifetime [Gyr]'" + OSChecker.newline +
				"set ylabel 'Mass [M_{Solar}]'" + OSChecker.newline +
				"plot ";
		
		// Plot commands for interpolated WD cooling models
		script += "'-' u 1:2 w l lw 2 lc rgbcolor 'black' title '"+name+"',";
		
		// Plot commands for raw data points
		for(int i=0; i<nMetals; i++) {
			script += "'-' u 1:2 w p pt 5 ps 0.25 notitle";
			if(i!=nMetals-1) {
				script += ",";
			}
		}
		
		script += OSChecker.newline;
		
		// In-place data for the interpolated cooling models
		for(double[] series : dataInterp)
		{
			script += series[0] + " " + series[1] + OSChecker.newline;
		}
		script += "e" + OSChecker.newline;
		
		// In-place data for the raw cooling models
		for(int i=0; i<nMetals; i++) {
			for(int j=0; j<data[i][0].length; j++)
			{
				script += data[i][0][j]/1e9 + " " + data[i][1][j] + OSChecker.newline;
			}
			script += "e" + OSChecker.newline;
		}
		
		BufferedImage plot = Gnuplot.executeScript(script);
		
		return plot;
	}
}