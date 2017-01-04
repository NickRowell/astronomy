package wd.wdlf.algo;

import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import infra.os.OSChecker;
import numeric.data.DiscreteFunction1D;
import photometry.Filter;
import util.ArrayUtil;
import util.ParseUtil;

/**
 * Base class for WDLF types; this can be used as-is for observed WDLFs, and can be extended to add other
 * data for synthetic WDLFs.
 *
 * @author nrowell
 * @version $Id$
 */
public class BaseWdlf {
    
    /** 
     * Density & error represented by {@link DiscreteFunction1D} object.
     */
    public DiscreteFunction1D density;
    
    /**
     * The {@link Filter} that the luminosity function is measured in.
     */
    public Filter filter;
    
    /**
     * Name of WDLF, used to identify it and set plot labels etc.
     */
    public String target;
    
    /**
     * Reference for the WDLF observation (e.g. 'Harris et al (2013)')
     */
    public String reference;
    
    /**
     * Default constructor.
     */
    public BaseWdlf() {
    	target = "default";
    	reference = "default";
    }
    
    /**
     * Main constructor.
     * @param binCentres
     * 	Array of magnitude bin centre
     * @param binWidths
     * 	Array of magnitude bin widths
     * @param density
     * 	Array of density values per magnitude bin
     * @param error
     * 	Array of density error values per magnitude bin (one sigma)
     */
    public BaseWdlf(double[] binCentres, double[] binWidths, double[] density, double[] error) {
    	this(binCentres, binWidths, density, error, 0.0);
    }
    
    /**
     * Alternative constructor that accepts a distance modulus parameter to shift the observed
     * WDLF from apparent to absolute magnitudes, ready for modelling.
     * @param binCentres
     * 	Array of magnitude bin centre
     * @param binWidths
     * 	Array of magnitude bin widths
     * @param density
     * 	Array of density values per magnitude bin
     * @param error
     * 	Array of density error values per magnitude bin (one sigma)
     * @param mu
     * 	Distance modulus
     */
    public BaseWdlf(double[] binCentres, double[] binWidths, double[] density, double[] error, double mu) {
    	
        // Sanity checks
        assert(binCentres.length == binWidths.length);
        assert(binCentres.length == density.length);
        
        double[] shiftedBinCentres = new double[binCentres.length];
        for(int i=0; i<binCentres.length; i++) {
        	shiftedBinCentres[i] = binCentres[i] - mu;
        }
        
        this.density = new DiscreteFunction1D(shiftedBinCentres, binWidths, density, error);
    }
    
    /**
     * Set the {@link #target} of WDLF, used to identify it and set plot labels etc.
     * @param target
     * 	The {@link #target} to set.
     */
    public final void setTarget(String target) { 
    	this.target = target;
    }
    
    public final void setReference(String reference) {
    	this.reference = reference;
    }
    
    /**
     * Set the {@link Filter} for this WDLF.
     * @param filter
     * 	The {@link Filter} to set for this WDLF.
     */
    public final void setFilter(Filter filter) {
    	this.filter = filter;
    }
    
    /**
     * Get the {@link Filter} associated with this {@link BaseWdlf}.
     * @return
     * 	The {@link Filter} of the WDLF magnitudes.
     */
    public Filter getFilter() {
    	return this.filter;
    }
    
    /** 
     * Get a string representation of the WDLF. This is just a table of four columns presenting
     * the WDLF magnitude bin centre, bin width, density and error, with one row per bin.
     */
    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        
        for(int i=0; i<density.size(); i++)
            out.append(density.getBinCentre(i)).append("\t")
                        .append(density.getBinWidth(i)).append("\t")
                        .append(density.getBinContents(i)).append("\t")
                        .append(density.getBinUncertainty(i)).append(OSChecker.newline);
        
        return out.toString();
    }
    
    /**
     * Get the number of WDLF data points.
     * @return
     * 	The number of WDLF data points.
     */
    public int size() { 
    	return density.size();
    }
    
    /**
     * Create and return a self-contained Gnuplot script suitable for plotting the WDLF,
     * including all data points statically within the plot script.
     * @return
     * 	A Gnuplot script suitable for plotting the WDLF,
     * including all data points statically within the plot script.
     */
    public String getLuminosityFunctionGnuplotScript() {
        double[] xrange = getXRange();
        double[] yrange = getYRange();
        
        StringBuilder output = new StringBuilder();
        output.append("set terminal pngcairo enhanced color size 640,480").append(OSChecker.newline);
        output.append("f(s,n) = (s-n>0) ? (s-n) : 9E-18").append(OSChecker.newline);
        output.append("set tics out").append(OSChecker.newline);
        output.append("set xrange ["+(xrange[0]-1)+":"+(xrange[1]+1)+"]").append(OSChecker.newline);
        output.append("set xlabel \"{/"+OSChecker.getFont()+"=14 "+filter.toString()+"}\"").append(OSChecker.newline);
        output.append("set mxtics 2").append(OSChecker.newline);
        output.append("set xtics 2 font \""+OSChecker.getFont()+",10\"").append(OSChecker.newline);
        output.append("set yrange ["+yrange[0]+":"+yrange[1]+"]").append(OSChecker.newline);
        output.append("set ytics 1 font \""+OSChecker.getFont()+",10\"").append(OSChecker.newline);
        output.append("set mytics 2").append(OSChecker.newline);
        output.append("set ylabel \"{/"+OSChecker.getFont()+"=14 Log {/Symbol \106} [N "+filter.toString()+"^{-1}]}\" offset 0,0").append(OSChecker.newline);
        output.append("set style line 1 lt 1 pt 5 ps 0.5  lc rgb \"black\" lw 1").append(OSChecker.newline);
        output.append("set bar 0.25").append(OSChecker.newline);
        output.append("set key top left Left").append(OSChecker.newline);
        output.append("set title '{/"+OSChecker.getFont()+"=10 WDLF for "+target+"}'").append(OSChecker.newline);
        output.append("plot '-' u 1:(log10($3)) w lp ls 1 notitle,\\").append(OSChecker.newline);
        output.append("  	'-' u 1:(log10($3)):(log10(f($3,$4))):(log10($3+$4)) w yerrorbars ls 1 title \""+reference+"\" ").append(OSChecker.newline);
        
        // Need two copies of inline data due to two uses of special
        // filename '-' in gnuplot plot command
        output.append(toString()).append("e").append(OSChecker.newline);
        output.append(toString()).append("e").append(OSChecker.newline);
        
        return output.toString();
    }
    
    /**
     * Get a suitable Y axis range for plotting the WDLF using a logarithmic scale. This range
     * is computed as the logarithm of the extreme values, plus 10% margins on either side.
     * @return
     * 	A suitable Y axis range for plotting logarithmic data points.
     */
    public double[] getYRange() {
    	// Range of density values
        double[] linRange = density.getRangeY();
    	
        double logMin = Math.log10(linRange[0]);
        double logMax = Math.log10(linRange[1]);
    	
        // Add 1/10 of the range on each end, so that function occupies middle
        // 4/5 of plot area
        double range = (logMax - logMin)/10.0;
        
        logMin -= range;
        logMax += range;
        
        return new double[]{logMin, logMax};
    }
    
    
    /**
     * Get a suitable X axis range for plotting the WDLF.
     * @return
     * 	A suitable X axis range for plotting the WDLF.
     */
    public double[] getXRange() {
        return density.getRangeX();
    }

    /**
     * Read WDLF data points from a File. The file must obey the following format: one
     * long table of data with one row per magnitude bin. Each row contains four columns
     * that provide the magnitude bin centre, magnitude bin width, WDLF value (stellar density)
     * and the associated error (one-sigma).
     * 
     * @param resourceLocation
     * 	The resource location for the WDLF data file
     * @return
     * 	The WDLF data formatted as a 2D array with the elements containing the following data:
     * [0] - array of the magnitude bin centres; [1] - array of the magnitude bin widths; [2] - 
     * array of the WDLF values (stellar density); [3] - array of the associated errors.
     * @throws IllegalArgumentException
     *  If there was a problem encountered in parsing the data from the file.
     */
    public static double[][] parseWdlfDataFromFile(String resourceLocation)
    throws IllegalArgumentException {
    	
        List<String> fileContents = ParseUtil.parseResource(resourceLocation);

        // Parse the string representation of WDLF data
        return parseWdlfDataFromStrings(fileContents, false);
    }
    
    /**
     * Read WDLF data points from a String. The String contents must obey the following format: one
     * long table of data with one row per magnitude bin. Each row contains four columns
     * that provide the magnitude bin centre, magnitude bin width, WDLF value (stellar density)
     * and the associated error (one-sigma).
     * 
     * @param text
     * 	The String containing the WDLF text
     * @param binsOnly
     *  Boolean indicating if we're reading just the magnitude bin centres and widths (true) or
     *  if we're reading density and density error values as well.
     * @return
     * 	The WDLF data formatted as a 2D array with the elements containing the following data:
     * [0] - array of the magnitude bin centres; [1] - array of the magnitude bin widths; [2] - 
     * array of the WDLF values (stellar density); [3] - array of the associated errors.
     * @throws IllegalArgumentException
     *  If there was a problem encountered in parsing the data from the String.
     */
    public static double[][] parseWdlfDataFromString(String text, boolean binsOnly)
    throws IllegalArgumentException {
    	
    	// Break text into lines
    	String linesArr[] = text.split("\\r?\\n");
    	List<String> lines = new LinkedList<>();
    	for(String line : linesArr) {
    		lines.add(line);
    	}
    	return parseWdlfDataFromStrings(lines, binsOnly);
    }
    
    /**
     * Read WDLF data points from a List of String. The String contents must obey the following format: one
     * long table of data with one row per magnitude bin. Each row contains four columns
     * that provide the magnitude bin centre, magnitude bin width, WDLF value (stellar density)
     * and the associated error (one-sigma).
     * 
     * @param text
     * 	List of Strings containing the WDLF text
     * @param binsOnly
     *  Boolean indicating if we're reading just the magnitude bin centres and widths (true) or
     *  if we're reading density and density error values as well.
     * @return
     * 	The WDLF data formatted as a 2D array with the elements containing the following data:
     * [0] - array of the magnitude bin centres; [1] - array of the magnitude bin widths; [2] - 
     * array of the WDLF values (stellar density); [3] - array of the associated errors.
     * @throws IllegalArgumentException
     *  If there was a problem encountered in parsing the data from the String.
     */
    public static double[][] parseWdlfDataFromStrings(List<String> text, boolean binsOnly)
    throws IllegalArgumentException {
    	
        List<double[]> data = new LinkedList<double[]>();
            
        // Read file line by line:
        for(String line : text)
        {
            // Skip blank lines
            if(line.length()==0) continue;
            // Skip commented lines
            if(line.substring(0, 1).equals("#")) continue;
                
            // Otherwise, load data from line
            Scanner scan = new Scanner(line);
            
            double mbol=0, width=0, den=0, err=0;
            
            mbol  = nextDouble(scan, data.size());   // Read Mbol bin centre
            width = nextDouble(scan, data.size());   // Read bin width
            if(!binsOnly) {
	            den   = nextDouble(scan, data.size());   // Read LF point
	            err   = nextDouble(scan, data.size());   // Read density error
            }
                
            // Add to data array
            data.add(new double[]{mbol, width, den, err});
            
            scan.close();
        }         
        
        double[] binCentres = new double[data.size()];
        double[] binWidths  = new double[data.size()];
        double[] density    = new double[data.size()];
        double[] error      = new double[data.size()];
             
        for(int d=0; d<data.size(); d++)
        {
            binCentres[d] = data.get(d)[0];
            binWidths[d]  = data.get(d)[1];            
            density[d]    = data.get(d)[2];            
            error[d]      = data.get(d)[3];
        }
        
        // Sanity checks
        boolean passedChecks = true;
        StringBuilder message = new StringBuilder();
        
        // Check that magnitude bins show a monotonic increase
        if(!ArrayUtil.checkIncreasing(binCentres)) {
        	passedChecks = false;
        	message.append("Magnitude bin centres don't have monotonic increase!");
        }
        
        if(!ArrayUtil.checkNonOverlappingBins(binCentres, binWidths)) {
        	passedChecks = false;
        	message.append("Magnitude bins overlap!");
        }
        
        if(!binsOnly) {
        	// Sanity checks on density & error
        	if(!ArrayUtil.checkNonNegative(density)) {
        		passedChecks = false;
            	message.append("Density is negative!");
        	}
        	if(!ArrayUtil.checkNonNegative(error)) {
        		passedChecks = false;
            	message.append("Density error is negative!");
        	}
        }
        
        if(!passedChecks) {
        	throw new IllegalArgumentException(message.toString());
        }
        
        if(binsOnly) {
        	return new double[][]{binCentres, binWidths};
        }
        else {
        	return new double[][]{binCentres, binWidths, density, error};
        }
    }

    /**
     * Scans a double from the scanner, with robust checks. This method is handy because the
     * exceptions thrown by the Scanner in case of no next element or elements that can't be formatted
     * as a double are not helpful at all (the error messages are null). Using this method instead
     * throws an exception with the line number and explanation of what went wrong.
     * 
     * @param scan
     * 	The Scanner to retrieve the next double from
     * @param lineNum
     * 	The number of the line that we're scanning, for verbose error message.
     * @return
     * 	The next double from the Scanner input
     * @throws IllegalArgumentException
     * 	Exception with a verbose error message describing why we couldn't scan the double.
     */
    private static double nextDouble(Scanner scan, int lineNum) throws IllegalArgumentException {
    	
    	if(!scan.hasNext()) {
    		throw new IllegalArgumentException("Line "+lineNum+": missing a value!");
    	}
    	if(!scan.hasNextDouble()) {
    		throw new IllegalArgumentException("Line "+lineNum+": cannot parse "+scan.next()+" as double!");
    	}
    	return scan.nextDouble();
    }
    
    
}