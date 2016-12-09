package wd.wdlf.inversion.infra;

import Jama.Matrix;
import infra.io.Gnuplot;
import infra.os.OSChecker;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

/**
 * Instances of class Convergence provide means of interpolating a smoothed
 * chi-square value at a given iteration number. This is used to calculate
 * the approximate reduction in chi-square at the latest iteration, and
 * decide if the algorithm has converged. Implementations must provide a
 * method <code>getChiSquare(int iteration)</code> that returns the smoothed
 * chi-square value, and implement <code>printFit()</code> to print out
 * details of fitting for verification.
 * 
 * @author nickrowell
 */
public abstract class Convergence {
    
	/**
	 * Enumerated type to represent the available convergence functions.
	 */
	public enum Type {
		POWERLAW,
		SLIDINGLINEAR
	};
	
	/**
	 * Factory method to create instances of the different Convergence types.
	 * @param type
	 * @param chi2
	 * @return
	 */
    public static Convergence factory(Type type, List<Double> chi2){
        switch(type){
            case POWERLAW: return new PowerLawConvergence(chi2);
            case SLIDINGLINEAR: return new SlidingLinear(chi2);
            default: return new SlidingLinear(chi2);
        }
    }
    
    /**
     * List of chi-square values.
     */
    List<Double> chi2;
    
    /**
     * Forces concrete classes to implement the toString() method.
     */
    public abstract String toString();
    
    /**
     * Get the smoothed value of Chi-square at the given iteration number.
     * @param iteration
     * @return
     */
    public abstract double getChiSquare(int iteration);
    
    /**
     * Get the number of free parameters in the smoothing function. This is used to
     * determine if the fit is constrained.
     * @return
     */
    public abstract int getParamsN();
    
    /**
     * Determines if enough Chi-square values have been accumulated to solve the
     * parameters of the smoothing function.
     * @return
     */
    public boolean isConstrained() {
    	return chi2.size() >= getParamsN();
    }
    
    /**
     * Test internal list for convergence. Argument is relative change in
     * chi2 at final value that indicates convergence.
     */
    public boolean hasConverged(double threshold){
        
        // Check relative change at latest iteration.
        if(getRelativeChangeAtLatestIteration() < threshold)
            // Converged
            return true;
        
        // Not converged
        return false;
    }
    
    /**
     * Get relative (fractional) change in chi-square from iteration number
     * N-1 to iteration number N. First iteration is numbered 1.
     */
    public double getRelativeChangeAtIteration(int N){
    
        // chi-square for latest iteration
        double chi2_N = getChiSquare(N);
        // chi-square for previous iteration
        double chi2_Nm1 = getChiSquare(N-1);        
        
        double rel_change = (chi2_Nm1-chi2_N)/chi2_Nm1;
        
        //System.out.println("Latest interpolated chi-square; "+chi2_N);
        //System.out.println("Previous interpolated chi-squ.: "+chi2_Nm1);
        //System.out.println("Relative change = "+rel_change);   
        
        // Return relative change.
        return rel_change;      
    
    }
    
    /**
     * Get relative (fractional) change in chi-square at latest iteration.
     * Iteration numbers are counted from 1, so chi2.size() correctly indexes
     * the final iteration in the list.
     */
    public double getRelativeChangeAtLatestIteration(){
        return getRelativeChangeAtIteration(chi2.size());
    }
    
    /**
     * Create a Gnuplot plot of the chi-square trend and model fit. If the list of chi-square
     * values is empty then a placeholder plot is generated.
     * @return
     * 	A {@link BufferedImage} containing the plot.
     */
    public BufferedImage getGnuplotPlot() {
    	
    	double xmax = chi2.isEmpty() ? 1 : chi2.size()+1;
    	double ymax = chi2.isEmpty() ? 10 : (getMax(chi2)*1.1);
    	
    	StringBuilder script = new StringBuilder();
    	
    	script.append("set terminal pngcairo enhanced color crop size 512,256").append(OSChecker.newline);
    	script.append("set xrange [0:"+xmax+"]").append(OSChecker.newline);
    	script.append("set yrange [0:"+ymax+"]").append(OSChecker.newline);
    	script.append("set xlabel 'Iteration'").append(OSChecker.newline);
    	script.append("set ylabel 'Chi-squared'").append(OSChecker.newline);
    	
    	if(isConstrained() ) {
	    	script.append("plot '-' w p pt 5 ps 0.5 notitle, ");
	    	script.append("'' w l notitle").append(OSChecker.newline);
	    	
	    	for(int i=0; i<chi2.size(); i++) {
	    		script.append(i+"\t"+chi2.get(i)).append(OSChecker.newline);
	    	}
	    	script.append("e").append(OSChecker.newline);
	    	for(int i=0; i<chi2.size(); i++) {
	    		script.append(i+"\t"+getChiSquare(i)).append(OSChecker.newline);
	    	}
	    	script.append("e").append(OSChecker.newline);
    	}
    	else {
    		script.append("plot '-' w p pt 5 ps 0.5 notitle").append(OSChecker.newline);
	    	for(int i=0; i<chi2.size(); i++) {
	    		script.append(i+"\t"+chi2.get(i)).append(OSChecker.newline);
	    	}
	    	script.append("e").append(OSChecker.newline);
    	}
    	
        BufferedImage image = null;
		try {
			image = Gnuplot.executeScript(script.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
        
    	return image;
    }
    
    /**
     * Gets the maximum value in the list.
     * @param vals
     * 	The {@link List<Double>}
     * @return
     * 	The maximum {@link Double} value in the list.
     */
    private static double getMax(List<Double> vals) {
    	if(vals.isEmpty()) {
    		return Double.NaN;
    	}
    	double max = vals.get(0);
    	for(Double val : vals) {
    		max = Math.max(val, max);
    	}
    	return max;
    }
    
}


/**
 * Smoothing function of the form
 * 
 *  chi^2 = S * (iteration number) ^ T
 * 
 * @author nickrowell
 */
class PowerLawConvergence extends Convergence{
    
    /** Parameter of power law smoothing function. */
    double S;
    
    /** Parameter of power law smoothing function. */
    double T;

    /**
     * Main constructor.
     * @param chi2
     * 	The list of Chi-squared points to smooth over.
     */
    public PowerLawConvergence(List<Double> chi2){
        
        this.chi2 = chi2;
        
        if(!isConstrained()) {
        	return;
        }
        
        // Build design matrix and chi-square column vector from logarithm of 
        // iteration number and chi-square value.
        double[][] a = new double[this.chi2.size()][2];
        double[][] x = new double[this.chi2.size()][1];
        
        // index into elements of a and x. Also number of iterations (minus 1), 
        // but must shift by +1 so that first iteration has number 1. Otherwise
        // logarithm is invalid.
        int index=0;
        
        for(Double data : this.chi2){
            a[index][0] = Math.log(index+1);
            a[index][1] = 1;
            x[index++][0] = Math.log(data);
        }
        
        // Convert to JAMA matrix types for manipulation
        Matrix A = new Matrix(a);
        Matrix X = new Matrix(x);
        
        Matrix M = A.solve(X);
        
        // log(chi^2) = m * log(iteration #) + c
        double m = M.get(0,0);
        double c = M.get(1,0);
        
        // chi^2 = S * (iteration # )^T
        S = Math.exp(c);
        T = m;
        
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getParamsN() {
    	return 2;
    }
    
    @Override
    public String toString(){
        StringBuilder out = new StringBuilder();
        int index = 0;
        for(Double chi : chi2) out=out.append("chi2(").append(++index).append("): ").append(chi).append("\n");
        
        out=out.append("\nf(x) = ").append(S).append(" * (x ** ").append(T).append(")\n");
        
        return out.toString();
        
    }
     
    /**
     * Get interpolated chi-square at some iteration number. Iterations start
     * counting at 1.
     * @param iteration
     * @return 
     */
    @Override
    public double getChiSquare(int iteration){
    	
    	if(!isConstrained()) {
    		throw new IllegalStateException("Convergence instance is not constrained!");
    	}
    	
        return S * Math.pow(iteration+1, T);
    }    
    
}

/**
 * 
 * Smoothing function of the form
 * 
 *  chi^2 = M * (iteration number) + C
 * 
 * @author nickrowell
 */
class SlidingLinear extends Convergence{

    /** Maximum window size (smoothing length) for sliding linear function. */
    private static int WINDOW = 5;
    
    /** Gradient of linear fit. */
    double m;
    
    /** Offset of linear fit. */
    double c;
    
    /**
     * Main constructor.
     * @param chi2
     * 	The list of Chi-squared points to smooth over.
     */
    public SlidingLinear(List<Double> chi2){
        
        this.chi2 = chi2;

        if(!isConstrained()) {
        	return;
        }
        
        // How many points are to be used in line fit? Maximum is WINDOW, but
        // for first few iterations there won't be enough points.
        int N = Math.min(chi2.size(),WINDOW);

        // Build design matrix and chi-square column vector
        double[][] a = new double[N][2];
        double[][] x = new double[N][1];
        
        // Fit straight line to final WINDOW elements
        
        // index into elements of a and x. Iterations start counting from 1,
        // so that most recent iteration has index chi2.size().
        for(int index = this.chi2.size() - N; index<this.chi2.size(); index++){
            a[index - (this.chi2.size() - N)][0] = index+1;
            a[index - (this.chi2.size() - N)][1] = 1;
            x[index - (this.chi2.size() - N)][0] = this.chi2.get(index);            
        }
        
        // Convert to JAMA matrix types for manipulation
        Matrix A = new Matrix(a);
        Matrix X = new Matrix(x);
        
        Matrix M = A.solve(X);
        
        // chi^2 = m * (iteration #) + c 
        m = M.get(0,0);
        c = M.get(1,0);
        
    }   
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getParamsN() {
    	return 2;
    }
    
    @Override
    public String toString(){
    

        StringBuilder out = new StringBuilder();
        int index = 0;
        for(Double chi : chi2) out=out.append("chi2(").append(++index).append("): ").append(chi).append("\n");
        
        out=out.append("\nFitted to final ").append(Math.min(chi2.size(),WINDOW)).append(" points:\nf(x) = ").append(m).append(" * x + ").append(c).append("\n");
        
        return out.toString();
        
    }
    
    /**
     * Get interpolated chi-square at some iteration number.
     * @param iteration
     * @return 
     */
    @Override
    public double getChiSquare(int iteration){
    	
    	if(!isConstrained()) {
    		throw new IllegalStateException("Convergence instance is not constrained!");
    	}
    	
        return m * (iteration+1) + c;
    }    
    
}