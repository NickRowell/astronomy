package ms.lifetime.exec;

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import ifmr.infra.IFMR;
import infra.io.Gnuplot;
import infra.os.OSChecker;
import ms.lifetime.algo.PreWdLifetime;
import ms.lifetime.infra.PreWdLifetimeModels;

/**
 * This class provides an application that plots the minimum WD mass as a function of the total stellar age.
 * 
 * @author nrowell
 * @version $Id$
 */
public class PlotMinWdMassByLookbackTime {

	/**
	 * Lifetime lower limit [Gyr]
	 */
	private static double minLifeTime = 0.0;
	
	/**
	 * Lifetime upper limit [Gyr]
	 */
	private static double maxLifeTime = 4.0;
	
	/**
	 * Lifetime step size for plotting [Gyr]
	 */
	private static double stepLifeTime = 0.01;
	
	/**
	 * The Z value (metallicity)
	 */
	private static double z = 0.017;
	
	/**
	 * The Y value (helium content)
	 */
	private static double y = 0.3;
	
	/**
	 * The {@link PreWdLifetime} to plot
	 */
	private static PreWdLifetimeModels preWdLifetimes = PreWdLifetimeModels.PADOVA;
	
	/**
	 * The {@link IFMR} to use.
	 */
	private static IFMR ifmr = IFMR.CUMMINGS_2018;
	
	/**
	 * The main application entry point.
	 * 
	 * @param args
	 * 	The args; not used.
	 * @throws IOException
	 * 	If there's an error executing the Gnuplot script.
	 */
	public static void main(String[] args) throws IOException {
		
		JLabel label = new JLabel();
		ImageIcon icon = new ImageIcon(plotMinWdMass());
		label.setIcon(icon);
		
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
                    tester.pack();
                    tester.setVisible(true);
                }
            });
	}

	/**
	 * Makes a plot of the minimum WD mass as a function of lookback time.
	 * 
	 * @return
	 * 	A BufferedImage containing the plot.
	 * @throws IOException
	 * 	If there's an exception executing the Gnuplot script.
	 */
	private static BufferedImage plotMinWdMass() throws IOException {

		PreWdLifetime preWdLifetime = preWdLifetimes.getPreWdLifetimeModels();
		
		// Number of points to plot
		int nPoints = (int)Math.ceil((maxLifeTime - minLifeTime) / stepLifeTime);
		
		// Array of interpolated data points
		double[][] dataInterp = new double[nPoints][2];
		
		for(int d=0; d<nPoints; d++)
		{
			// Translate index to lifetime
			double lifetimeGyr = minLifeTime + d*stepLifeTime;
			
			// Get the stellar mass with a lifetime that matches this [M_{Solar}]
			double mi = preWdLifetime.getStellarMass(z, y, lifetimeGyr*1E9)[0];
			
			// Get the WD mass
			double mf = ifmr.getIFMR().getMf(mi);
			
			dataInterp[d][0] = lifetimeGyr;
			dataInterp[d][1] = mf;
		}
		
		String script = 
				"set terminal pngcairo enhanced color size 640,480" + OSChecker.newline + 
				"set xrange [0:"+maxLifeTime+"]" + OSChecker.newline +
				"set yrange [0.5:1.3]" + OSChecker.newline +
				"set key top right" + OSChecker.newline +
				"set xtics out" + OSChecker.newline +
				"set ytics out" + OSChecker.newline +
				"set xlabel 'Pre-WD lifetime [Gyr]'" + OSChecker.newline +
				"set ylabel 'WD mass [M_{Solar}]'" + OSChecker.newline +
				"plot ";
		
		// Plot commands for interpolated data
		script += "'-' u 1:2 w l lw 2 lc rgbcolor 'black' notitle,";
		
		script += OSChecker.newline;
		
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