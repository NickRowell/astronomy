package projects.gaia.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import infra.io.Gnuplot;
import infra.os.OSChecker;

/**
 * This utility class provides an implementation of the Renormalised Unit Weight Error lookup table.
 *
 * @author nrowell
 * @version $Id$
 */
public final class RenormalisedUnitWeightError {

	/**
	 * Enforce non-instantiation.
	 */
	private RenormalisedUnitWeightError() {	}
	
	/**
	 * The logger.
	 */
	private static final Logger logger = Logger.getLogger(RenormalisedUnitWeightError.class.getName());
	
	/**
	 * Full path to the file table_u0_g_col.txt
	 */
	private static File u0_g_col_file = new File("/home/nrowell/Projects/Astronomy/gaia_WDs/RUWE/DR2_RUWE_V1/table_u0_g_col.txt");
	
	/**
	 * Difference between neighbouring bin centres in G magnitude.
	 */
	private static double gMagStep = 0.01;
	
	/**
	 * Range of G magnitude (read from data).
	 */
	private static double gMin, gMax;
	
	/**
	 * Difference between neighbouring bin centres in BP-RP.
	 */
	private static double bpRpStep = 0.1;

	/**
	 * Range of BP-RP (read from data).
	 */
	private static double bpRpMin, bpRpMax;
	
	/**
	 * Lookup table for u0_g_col implemented as a 2D array.
	 */
	private static double[][] u0_g_col_arr;
	
	/**
	 * Get the normalisation value for the unit weight error at the given G magnitude
	 * and BP-RP value.
	 * 
	 * @param gMag
	 * 	The G manitude.
	 * @param bpRp
	 * 	The BP-RP value.
	 * @return
	 * 	The normalisation value for the unit weight error, or NaN if the given (G, BP-RP)
	 * value is outside the range of the lookup table.
	 */
	public static double getU0(double gMag, double bpRp) {
		
		if(u0_g_col_arr == null) {
			// Lazy initialisation
			initU0GCol();
		}
		
        int gMagBin = (int)Math.ceil((gMag - gMin) / gMagStep);
    	if(gMagBin < 0 || gMagBin >= u0_g_col_arr.length) {
			logger.log(Level.SEVERE, "Sampling lookup table: magnitude " + gMag + " is out of range ["+gMin+":"+gMax+"]");
    		return Double.NaN;
    	}

        int bpRpBin = (int)Math.ceil((bpRp - bpRpMin) / bpRpStep);
    	if(bpRpBin < 0 || bpRpBin >= u0_g_col_arr[gMagBin].length) {
			logger.log(Level.SEVERE, "Sampling lookup table: BP-RP value " + bpRp + " is out of range ["+bpRpMin+":"+bpRpMax+"]");
    		return Double.NaN;
    	}
		
		return u0_g_col_arr[gMagBin][bpRpBin];
	}
	
	/**
	 * Initialise the internal array of lookup table values.
	 */
	private static void initU0GCol() {

		// Load data to a nested NavigableMap then read to an array for convenience
		NavigableMap<Double, NavigableMap<Double, Double>> u0_g_col = new TreeMap<>();
		
		// Read file contents into the List
		try (BufferedReader in = new BufferedReader(new FileReader(u0_g_col_file))) {
			
			// Trim off the header CSV line
			String uoGColStr = in.readLine();
			while((uoGColStr=in.readLine())!=null) {
				
				// Avoid blank lines
	        	if (uoGColStr.length()==0) {
	                continue;
	        	}
	        	
	            // Avoid any commented out lines
	            if (uoGColStr.substring(0, 1).equals("#")) {
	                continue;
	            }
	            
	            try(Scanner scan = new Scanner(uoGColStr)) {
	    			
	    			// Parsing from csv file
	    			scan.useDelimiter(",");
	    			
	    			double gMag = scan.nextDouble();
	    			double bpRp = scan.nextDouble();
	    			double u0 = scan.nextDouble();
	    			
	    			if(!u0_g_col.containsKey(gMag)) {
	    				u0_g_col.put(gMag, new TreeMap<Double, Double>());
	    			}
	    			u0_g_col.get(gMag).put(bpRp, u0);
	    		}
	    		catch(IllegalStateException | NoSuchElementException e) {
	    			logger.log(Level.SEVERE, "Could not parse (g_mag, bp_rp, u0) from " + uoGColStr);
	    			return;
	    		}
			}
		}
		catch(IOException e) {
			logger.log(Level.SEVERE, "Could not read " + u0_g_col_file.getAbsolutePath());
			return;
		}
		
		// Read the data OK. Check number of G & BP-RP bins.
		int nGMagBins = u0_g_col.size();
		int nBpRpBins = u0_g_col.firstEntry().getValue().size();
		for(Entry<Double, NavigableMap<Double, Double>> entry : u0_g_col.entrySet()) {
			// Verify that each G value has the same number of BP-RP values.
			if(entry.getValue().size() != nBpRpBins) {
				logger.log(Level.SEVERE, "Found different number of BP-RP entries in lookup table: " + nBpRpBins + 
						" for G="+u0_g_col.firstEntry().getKey() + " vs. " + entry.getValue().size() + " for G=" + entry.getKey());
				return;
			}
		}
		
		// Establish the range of G magnitude and BP-RP
		
		// G and BP-RP values given in lookup table correspond to the bin centres.
		gMin = u0_g_col.firstEntry().getKey() - gMagStep/2.0;
		gMax = u0_g_col.lastEntry().getKey() + gMagStep/2.0;
		
		NavigableMap<Double, Double> u0_col = u0_g_col.firstEntry().getValue();
		bpRpMin = u0_col.firstEntry().getKey() - bpRpStep/2.0;
		bpRpMax = u0_col.lastEntry().getKey() + bpRpStep/2.0;
		
		logger.log(Level.INFO, "Creating lookup table with " + nGMagBins + " G bins and " + nBpRpBins + 
				" BP-RP bins spanning ["+gMin+":"+gMax+"] and ["+bpRpMin+":"+bpRpMax+"]");
		
		u0_g_col_arr = new double[nGMagBins][nBpRpBins];
		
		for(Entry<Double, NavigableMap<Double, Double>> gMagEntry : u0_g_col.entrySet()) {
			
			double gMag = gMagEntry.getKey();
			
			int gMagBin = (int)Math.floor((gMag - gMin) / gMagStep);
	    	if(gMagBin < 0 || gMagBin >= u0_g_col_arr.length) {
				logger.log(Level.SEVERE, "Creating lookup table: magnitude " + gMag + " is out of range ["+gMin+":"+gMax+"]");
	    		continue;
	    	}
	    	
	    	for(Entry<Double, Double> bpRpEntry : gMagEntry.getValue().entrySet()) {
	    		
	    		double bpRp = bpRpEntry.getKey();
	    		double u0 = bpRpEntry.getValue();
	    		
	    		int bpRpBin = (int)Math.floor((bpRp - bpRpMin) / bpRpStep);
	        	if(bpRpBin < 0 || bpRpBin >= u0_g_col_arr[gMagBin].length) {
	    			logger.log(Level.SEVERE, "Creating lookup table: BP-RP value " + bpRp + " is out of range ["+bpRpMin+":"+bpRpMax+"]");
	        		continue;
	        	}
	    		
	        	// Enter value from lookup table to array
	        	u0_g_col_arr[gMagBin][bpRpBin] = u0;
	    	}
		}
	}
	
	/**
	 * Test application.
	 * 
	 * @param args
	 * 	The command line args (ignored).
	 */
	public static void main(String[] args) {
		
		initU0GCol();
		
		StringBuilder script = new StringBuilder();
		script.append("set terminal pngcairo enhanced color size 640,480").append(OSChecker.newline);
		
		script.append("set xrange ["+gMin+":"+gMax+"]").append(OSChecker.newline);
		script.append("set xlabel 'G [mag]'").append(OSChecker.newline);
		script.append("set xtics out nomirror").append(OSChecker.newline);
		
		script.append("set yrange ["+bpRpMin+":"+bpRpMax+"]").append(OSChecker.newline);
		script.append("set ylabel 'BP-RP [mag]'").append(OSChecker.newline);
		script.append("set ytics out nomirror").append(OSChecker.newline);
		
		// Legend
		script.append("set key off").append(OSChecker.newline);
		
		script.append("set cbrange [0.5:20]").append(OSChecker.newline);
		script.append("set cblabel 'u0(G,BP-RP)'").append(OSChecker.newline);
		script.append("set cbtics ('0.5' 0.5,'1' 1, '2' 2, '3' 3, '4' 4, '5' 5, '10' 10, '20' 20) out nomirror").append(OSChecker.newline);
		script.append("set logscale cb").append(OSChecker.newline);
		
		script.append("set palette color").append(OSChecker.newline);
		script.append("set palette defined ( 0 '#000090', 1 '#000fff', 2 '#0090ff', 3 '#0fffee', 4 '#90ff70',"+
                "5 '#ffee00', 6 '#ff7000', 7 '#ee0000', 8 '#7f0000')").append(OSChecker.newline);
		script.append("set palette negative").append(OSChecker.newline);
		
		// NOTE transposed x and y to get around bug in Gnuplot 5.0.3
		script.append("plot '-' u 2:1:3 w image").append(OSChecker.newline);
		
		for(int j=0; j<u0_g_col_arr[0].length; j++) {
			
			double bpRp = bpRpMin + j * bpRpStep + bpRpStep / 2.0;
			
			for(int i=0; i<u0_g_col_arr.length; i++) {
			
				double gMag = gMin + i * gMagStep + gMagStep / 2.0;
				
				String line = String.format("%.1f\t%.2f\t%.9f", bpRp, gMag, u0_g_col_arr[i][j]);
				script.append(line).append(OSChecker.newline);
			}
			script.append(OSChecker.newline);
		}
		script.append("e").append(OSChecker.newline);
		
		try {
			Gnuplot.displayImage(Gnuplot.executeScript(script.toString()));
		} catch (IOException e) {
			logger.info("Problem creating plot: " + e.getMessage());
		}
	}
	
}
