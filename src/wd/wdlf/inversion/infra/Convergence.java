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
     * Number of points to skip from start of chi-squared list when computing the model fit.
     */
    protected static final int POINTS_TO_SKIP = 1;
    
    /**
     * List of chi-square values for each iteration; iterations are counted from 1.
     */
    List<Double> chi2;
    
    /**
     * Print the functional form of the model.
     */
    public abstract String toString();
    
    /**
     * Get the smoothed value of chi-square at the given iteration number.
     * 
     * @param iteration
     * 	Iteration number, counting from 1.
     * @return
     * 	The value of chi-squared at the given iteration number, derived from the model fit.
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
    	return (chi2.size() - POINTS_TO_SKIP) >= getParamsN();
    }
    
    /**
     * Test for convergence by analysis of the model chi-squared.
     * 
     * @param threshold
     * 	Threshold on the absolute value of the relative change in chi-squared over the final two values in the
     * list; if the observed (model) value is less than this then convergence has been reached.
     */
    public boolean hasConverged(double threshold){
    	return getRelativeAbsChangeAtLatestIteration() < threshold;
    }
    
    /**
     * Get absolute value of the relative (fractional) change in chi-square from iteration number N-1 to iteration number N.
     * 
     * @param N
     * 	Iteration number, counting from 1.
     */
    public double getRelativeAbsChangeAtIteration(int N){
    
        // chi-square for latest iteration
        double chi2_N = getChiSquare(N);
        
        // chi-square for previous iteration
        double chi2_Nm1 = getChiSquare(N-1);        
        
        double relChange = (chi2_Nm1-chi2_N)/chi2_Nm1;
        
        // Return absolute value of the relative change.
        return Math.abs(relChange);
    }
    
    /**
     * Get relative (fractional) change in chi-square at the final iteration.
     */
    public double getRelativeAbsChangeAtLatestIteration(){
        return getRelativeAbsChangeAtIteration(chi2.size());
    }
    
    /**
     * TODO: need to update this to reflect the new usage of the chi-squared list and convergence detection.
     * 
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
	    		script.append((i+1)+"\t"+chi2.get(i)).append(OSChecker.newline);
	    	}
	    	script.append("e").append(OSChecker.newline);
	    	for(int i=0; i<chi2.size(); i++) {
	    		script.append((i+1)+"\t"+getChiSquare(i+1)).append(OSChecker.newline);
	    	}
	    	script.append("e").append(OSChecker.newline);
    	}
    	else {
    		script.append("plot '-' w p pt 5 ps 0.5 notitle").append(OSChecker.newline);
	    	for(int i=0; i<chi2.size(); i++) {
	    		script.append((i+1)+"\t"+chi2.get(i)).append(OSChecker.newline);
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
    	double max = -Double.MAX_VALUE;
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
    	
    	throw new RuntimeException("Needs to be revised considering that chi2 list now contains all values including the first!");
        
//        this.chi2 = chi2;
//        
//        if(!isConstrained()) {
//        	return;
//        }
//        
//        // Build design matrix and chi-square column vector from logarithm of 
//        // iteration number and chi-square value.
//        double[][] a = new double[this.chi2.size()][2];
//        double[][] x = new double[this.chi2.size()][1];
//        
//        // index into elements of a and x. Also number of iterations (minus 1), 
//        // but must shift by +1 so that first iteration has number 1. Otherwise
//        // logarithm is invalid.
//        int index=0;
//        
//        for(Double data : this.chi2){
//            a[index][0] = Math.log(index+1);
//            a[index][1] = 1;
//            x[index++][0] = Math.log(data);
//        }
//        
//        // Convert to JAMA matrix types for manipulation
//        Matrix A = new Matrix(a);
//        Matrix X = new Matrix(x);
//        
//        Matrix M = A.solve(X);
//        
//        // log(chi^2) = m * log(iteration #) + c
//        double m = M.get(0,0);
//        double c = M.get(1,0);
//        
//        // chi^2 = S * (iteration # )^T
//        S = Math.exp(c);
//        T = m;
        
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

    /**
     * Maximum window size (smoothing length) for sliding linear function.
     */
    private static final int WINDOW = 5;
    
    /**
     * Gradient of linear fit.
     */
    double m;
    
    /**
     * Offset of linear fit.
     */
    double c;
    
    /**
     * Main constructor.
     * 
     * @param chi2
     * 	The list of Chi-squared points to smooth over.
     */
    public SlidingLinear(List<Double> chi2) {
        
        this.chi2 = chi2;

        if(!isConstrained()) {
        	return;
        }
        
        // How many points are to be used in line fit? Maximum is WINDOW, but
        // for first few iterations there won't be enough points.
        int N = Math.min(chi2.size() - POINTS_TO_SKIP, WINDOW);

        // Build design matrix and chi-square column vector
        double[][] a = new double[N][2];
        double[][] x = new double[N][1];
        
        // Fit straight line to final N elements
        for(int idx = 0; idx < N; idx++) {
        	
        	// Index of element in chi2 list
        	int chi2Idx = this.chi2.size() - N + idx;
        	
        	// Model coefficient is iteration number, which is index in chi-squared list plus one
        	a[idx][0] = chi2Idx + 1;
            a[idx][1] = 1;
            x[idx][0] = this.chi2.get(chi2Idx);  
        }
        
        // Convert to JAMA matrix types for manipulation
        Matrix A = new Matrix(a);
        Matrix X = new Matrix(x);
        
        Matrix M = A.solve(X);
        
        // chi^2 = m * (iteration # counting from zero) + c 
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
    public String toString() {
    
//        StringBuilder out = new StringBuilder();
//        int index = 0;
//        
//        for(Double chi : chi2) {
//        	out.append("chi2(").append(index++).append("): ").append(chi).append("\n");
//        }
//        
//        out.append("\nFitted to final ").append(Math.min(chi2.size(),WINDOW)).append(" points:\nf(x) = ").append(m).append(" * x + ").append(c).append("\n");
//        
//        return out.toString();
        
        return String.format("f(x) = %f * x + %f", m, c);
        
    }
    
    /**
     * Get interpolated chi-square at some iteration number.
     * 
     * @param iteration
     * 	Iteration number; first iteration is number 1.
     * @return
     * 	The model chi-squared value derived from the sliding linear fit.
     */
    @Override
    public double getChiSquare(int iteration){
    	
    	if(!isConstrained()) {
    		throw new IllegalStateException("Convergence instance is not constrained!");
    	}
    	
        return m * iteration + c;
    }    
    
}