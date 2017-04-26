package sfr.algo;

import java.util.Random;

import infra.os.OSChecker;

/**
 * Supertype for all implementations of a Star Formation Rate model.
 * 
 * TODO:
 *  2) Write tester GUI that tests the drawing of random creation times
 */
public abstract class BaseSfr {
	
	/**
     * Instance used by extending classes to draw random creation times.
     */
    protected static Random rng = new Random();
	
    /** 
     * Minimum lookback time [yr]. At times younger than this the star formation rate is zero.
     */
    public double t_min;
    
    /** 
     * Maximum lookback time [yr]. At times older than this the star formation rate is zero.
     */
    public double t_max;
    
    /**
     * Default constructor.
     */
    public BaseSfr() {
    	t_min = 0.0;
    	t_max = 13.0e9;
    }
    
    /**
     * Main constructor.
     * 
     * @param t_min
     * 	Minimum lookback time [yr].
     * @param t_max
     * 	Maximum lookback time [yr].
     */
    public BaseSfr(double t_min, double t_max) {
    	
    	// Sanity check
    	if(Double.isNaN(t_min) || Double.isInfinite(t_min) || Double.isNaN(t_max) || Double.isInfinite(t_max)) {
    		throw new IllegalArgumentException("Illegal value for minimum/maximum lookback time: "+t_min+"/"+t_max);
    	}
    	if(t_min >= t_max) {
    		throw new IllegalArgumentException("Minimum lookback time ("+t_min+") must be smaller than maximum lookback time ("+t_max+")!");
    	}
    	
    	this.t_min = t_min;
    	this.t_max = t_max;
    }

    /**
     * Get {@link BaseSfr#t_min}, the minimum lookback time.
     * @return
     *  {@link BaseSfr#t_min}, the minimum lookback time.
     */
//    public double getTmin() {
//    	return t_min;
//    }
//
//    /**
//     * Set {@link BaseSfr#t_min}, the minimum lookback time.
//     * @param t_min
//     *  The minimum lookback time to set.
//     */
//    public void setTmin(double t_min) {
//    	this.t_min = t_min;
//    }
//    
//    /**
//     * Get {@link BaseSfr#t_max}, the maximum lookback time.
//     * @return
//     *  {@link BaseSfr#t_max}, the maximum lookback time.
//     */
//    public double getTmax() {
//    	return t_max;
//    }
//    
//    /**
//     * Set {@link BaseSfr#t_max}, the maximum lookback time.
//     * @param t_max
//     *  The maximum lookback time to set.
//     */
//    public void setTmax(double t_max) {
//    	this.t_max = t_max;
//    }

    /**
     * Create and return a deep copy of the {@link BaseSfr}.
     * @return
     * 	A new instance of the {@link BaseSfr} implementing class.
     */
    public abstract BaseSfr copy();

    /**
     * Suitable descriptive name for the SFR model.
     * @return
     * 	String containing a suitable (short) name for the SFR model type.
     */
    public abstract String getName();
    
    /** 
     * Get SFR at lookback time t [stars/yr]
     * @return
     * 	The star formation rate [stars/yr] at the given lookback time [yr]. Zero
     * lookback time equals the present day.
     */
    public abstract double getSFR(double t);
    
    /** 
     * Integrate SFR to get the total number of stars formed, and the
     * uncertainty.
     * @return
     * 	The total number of stars formed over the whole star formation history
     * (between {@link BaseSfr#t_min} and {@link BaseSfr#t_max}), and the standard error
     * on this.
     */
    public abstract double[] integrateSFR();
    
    /**
     * Draw a random creation time from the star formation rate.
     * 
     * @return
     * 	The lookback time of creation (0 = present day) [yr].
     */
    public abstract double drawCreationTime();
    
    /**
     * Gets a table of data representing the star formation rate, one point per line, formatted into two
     * columns containing the lookback time (column 1) and star formation rate (column 2). The format of
     * the table is such that it is suitable for plotting with Gnuplot.
     * @return
     * 	A table of data representing the star formation rate, one point per line, formatted into two
     * columns containing the lookback time (column 1) and star formation rate (column 2).
     */
    public abstract String toString();
    
    
    /**
     * Get the maximum star formation rate.
     * @return
     * 	The maximum star formation rate [stars/yr]
     */
    public abstract double getMaxRate();
    
    /**
     * Get a Gnuplot script that can be used to make a plot of the SFR model. The data is appended to the script
     * so that no additional data file is necessary.
     * 
     * @return
     * 	String containing a standalone Gnuplot script suitable for plotting the SFR model.
     */
    public String getGnuplotScript() {
    	
        // Get scale factor for SFR to make scale range 0:10
        double sfr_max = getMaxRate();
        int sfr_exp = (int)Math.floor(Math.log10(sfr_max));

		StringBuilder output = new StringBuilder();

		output.append("set terminal pngcairo enhanced color size 410,350").append(OSChecker.newline);

		// Configure X axis
		output.append("set xrange [0:"+t_max*1.1/1e9+"] reverse").append(OSChecker.newline);
		output.append("set xlabel \"{/"+OSChecker.getFont()+"=14 Lookback time [Gyr]}\"").append(OSChecker.newline);
		output.append("set mxtics 2").append(OSChecker.newline);
		output.append("set xtics 2 font \""+OSChecker.getFont()+",10\" out").append(OSChecker.newline);
		
		// Configure Y axis
		output.append("set yrange [0:"+(sfr_max*1.2*Math.pow(10,-sfr_exp))+"]").append(OSChecker.newline);
		output.append("set ytics  font \""+OSChecker.getFont()+",10\"").append(OSChecker.newline);
		output.append("set mytics 2").append(OSChecker.newline);
		output.append("set ylabel \"{/"+OSChecker.getFont()+"=14 SFR [N({/Symbol \\264}10^{"+sfr_exp+"}) / yr]}\" offset 0,0").append(OSChecker.newline);
		
		output.append("set style line 1 lt 1 pt 5 ps 0.5  lc rgb \"black\" lw 1").append(OSChecker.newline);
		output.append("set key top left Left").append(OSChecker.newline);
		output.append("plot 	'-' u ($1/1E9):($2*1E"+(-sfr_exp)+") w l ls 1 notitle").append(OSChecker.newline);
		
		// Append the data inline
        output.append(this.toString());
        output.append("e").append(OSChecker.newline);
            
        return output.toString();
    }
    
}