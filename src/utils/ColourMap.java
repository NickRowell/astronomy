package utils;

import numeric.functions.Linear;

public class ColourMap {

	/**
	 * Array of 24-bit colours that will be interpolated.
	 */
	int[] colours = new int[]{0xe41a1c, 0x377eb8, 0x4daf4a, 0x984ea3, 0xff7f00, 0xffff33};
	
	double min;
	
	double max;
	
	Linear r_interp;
	Linear g_interp;
	Linear b_interp;
	
	public ColourMap(double min, double max) {
		
		this.min = min;
		this.max = max;
		int n = colours.length;
		
		// Construct linear interpolators for the RGB components
		double[] x = new double[n];
		double[] r = new double[n];
		double[] g = new double[n];
		double[] b = new double[n];
		
		for(int i = 0; i < n; i++) {
			
			int colour = colours[i];
			
			x[i] = n == 1 ? 0.0 : (double)i / (double)(n-1);
			r[i] = (colour & 0xFF0000) >> 16;
			g[i] = (colour & 0x00FF00) >> 8;
			b[i] = (colour & 0x0000FF);
		}
		
		r_interp = new Linear(x, r);
		g_interp = new Linear(x, g);
		b_interp = new Linear(x, b);
	}
	
	public int getColour(double x) {
		
		double x_norm = (x - min) / (max - min);
		
		// Clamp to [0:1] range
		x_norm = Math.min(Math.max(x_norm, 0.0), 1.0);
		
		int r = (int)Math.rint(r_interp.interpolateY(x_norm)[0]);
		int g = (int)Math.rint(g_interp.interpolateY(x_norm)[0]);
		int b = (int)Math.rint(b_interp.interpolateY(x_norm)[0]);
		
		return (r << 16) + (g << 8) + b;
	}
	
	public int getInverseColour(double x) {
		
		double x_norm = (x - min) / (max - min);
		
		// Clamp to [0:1] range
		x_norm = Math.min(Math.max(x_norm, 0.0), 1.0);
		
		int r = 255 - (int)Math.rint(r_interp.interpolateY(x_norm)[0]);
		int g = 255 - (int)Math.rint(g_interp.interpolateY(x_norm)[0]);
		int b = 255 - (int)Math.rint(b_interp.interpolateY(x_norm)[0]);
		
		return (r << 16) + (g << 8) + b;
	}
	
	
	
	
}
