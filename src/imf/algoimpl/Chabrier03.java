package imf.algoimpl;

import java.util.Random;

import org.apache.commons.math3.distribution.NormalDistribution;

import imf.algo.BaseImf;

/**
 * Implementation of the "Disk IMF for Single Objects" published in Chabrier G., 2003, PASP, 115, 763. Numbers are
 * taken from Table 1.
 * 
 * @author nrowell
 * @version $Id$
 */
public class Chabrier03 extends BaseImf {
	
	/**
	 * Fixed seed to use for random number generation, in order to make applications deterministic.
	 */
	public static final long seed = 58315548397523634L;
	
    /**
     * Instance of {@link Random} used to provide random number generation.
     */
	private static final Random random = new Random(seed);
	
    /**
     * Coefficients of lognormal part
     */
	double a;
	NormalDistribution norm;

    /**
     * Coefficients of power law part
     */
    double A, e;
    
    /**
     * Normalisation constant; initialised to 1 then reconfigured on construction.
     */
    double c = 1.0;
    
    /**
     * Convenience field: integral of lognormal part over [{@link BaseImf#M_lower}:1.0], NOT INCLUDING normalisation factor.
     * 
     * {@link #logNormInt} = 0.949897592379823
     */
    double logNormInt = Double.NaN;
    
    /**
     * Convenience field: cumulative probability in lognormal part to {@link BaseImf#M_lower}, NOT INCLUDING normalisation factor.
     * 
     * {@link #cumProbLowM} = 0.8990437967565382
     */
    double cumProbLowM = Double.NaN;
    
    /**
     * Transition mass between lognormal and power law part.
     */
    final double transMass = 1.0;
    
    /**
     * Convenience field: ln(10)
     */
    final double ln10 = Math.log(10.0);
    
    /**
     * Main constructor for the {@link Chabrier03}.
     */
    public Chabrier03() {
    	
    	// Configure lognormal part
    	a = 0.158 * 0.69 * Math.sqrt(2*Math.PI);
        norm = new NormalDistribution(Math.log10(0.079), 0.69);
    	
        // Configure power law part
        A = 0.0443 / ln10;
        e = -2.3;
        
        // Initialise convenience fields
        cumProbLowM = norm.cumulativeProbability(Math.log10(M_lower));
        logNormInt = a * (norm.cumulativeProbability(Math.log10(transMass)) - cumProbLowM);
        
        // Solve for normalisation constant; set initially to 1.0
        c = 1.0 / getIntegral(M_upper);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public double getIMF(double mass) {
    	
    	if(mass < M_lower || mass > M_upper) {
            throw new RuntimeException("Encountered stellar mass outside of allowed range: " + mass);
        }
        
        if(mass < 1.0) {
        	double imfLogM =  c * a * norm.density(Math.log10(mass));
        	double imfM = imfLogM / (mass * ln10);
        	return imfM;
        }
        else {
        	return c * A * Math.pow(mass, e);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public double getIntegral(double mass) {
    	
    	if(mass < 1.0) {
    		return c * a * (norm.cumulativeProbability(Math.log10(mass)) - cumProbLowM);
    	}
    	else {
    		// Sum total lognormal part and bit of power law part
    		return c * (logNormInt + A * (1/(e+1)) * (Math.pow(mass, e+1) - Math.pow(transMass, e+1)));
    	}
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public double drawMass() {
    	
        double x = random.nextDouble();
        
        if(x < logNormInt * c) {
        	// Mass is in lognormal segment; get inverse cumulative distribution
        	double logM = norm.inverseCumulativeProbability((x/(a*c)) + cumProbLowM);
        	return Math.pow(10, logM);
        }
        else {
        	// Mass is in power law part; get inverse cumulative distribution
        	x -= logNormInt * c;
        	return Math.pow((x/(A*c))*(e+1) + Math.pow(transMass, e+1), 1/(e+1));
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
    	return "Chabrier (2003) table 1";
    }
    
}