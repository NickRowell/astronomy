package astrometry.test;

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.JFrame;

import astrometry.Ephemeris;
import infra.gui.IPanel;
import infra.io.Gnuplot;
import infra.os.OSChecker;

public class TestEphemeris {
	
	/**
	 * Time step between points [Julian days]
	 */
	private static double tStep = 10.0;
	
	public static void main(String[] args) throws IOException
	{
		// Get Earth ephemeris
		Ephemeris earth = Ephemeris.getEphemeris(Ephemeris.Body.EARTH);
		
		String script = 
				"set terminal pngcairo enhanced color size 640,480" + OSChecker.newline + 
				"set key off" + OSChecker.newline +
				"set xtics out" + OSChecker.newline +
				"set ytics out" + OSChecker.newline +
				"set ztics out" + OSChecker.newline +
				"set xlabel 'X'" + OSChecker.newline +
				"set ylabel 'Y'" + OSChecker.newline +
				"set zlabel 'Z'" + OSChecker.newline +
				"splot '-' w l lc rgbcolor 'black' lt 1 notitle" + OSChecker.newline;
		
		for(double t = earth.tMin; t<earth.tMax; t+=tStep)
		{
			double[] x = earth.interpolate(t);
			script += String.format("%f\t%f\t%f\n", x[0], x[1], x[2]);
			
		}
		script += "e" + OSChecker.newline;
		
//		System.out.println(script);
		
		BufferedImage plot = Gnuplot.executeScript(script);
		
		final IPanel ipanel = new IPanel(plot);
		
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
                            tester.pack();
                            tester.setVisible(true);
                        }
                    });
		
	}
	
	
	
	
	
}
