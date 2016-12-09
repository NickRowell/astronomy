package sfr.algoimpl;

import numeric.data.DiscreteFunction1D;
import sfr.algo.BaseDiscreteSfr;
import sfr.algo.BaseSfr;

/**
 * Class represents a randomly generated Star Formation Rate model using fractal methods.
 * 
 */
public class FractalSFR extends BaseDiscreteSfr {
    	
    /**
     * Magnitude parameter. There are 2^mag + 1 SFR pieces.
     */
    public int magnitude;
    
    /**
     * Rate at end points.
     */
    public double r0;
    
    /**
     * Hurst parameter [0:1].
     */
    public double H;
    
    /**
     * Initial standard deviation.
     */
    public double std;
    
    /**
     * Clamp rate to zero? Otherwise, shift entire function vertically to zero.
     */
    public boolean clamp_to_zero;
    
    /**
     * Default constructor.
     */
    public FractalSFR() {
    	magnitude = 5;
    	r0 = 5E-12;
    	H = 0.5;
    	std = 1E-12;
    	clamp_to_zero = true;
    	init();
    }
    
    /**
     * Main constructor.
     * 
     * @param t_min
     * 	Minimum lookback time [yr]
     * @param t_max
     * 	Maximum lookback time [yr]
     * @param magnitude
     * 	Magnitude parameter. There are 2^mag + 1 SFR pieces.
     * @param r0
     * 	Rate at end points.
     * @param H
     * 	Hurst parameter.
     * @param std
     * 	Initial standard deviation.
     * @param clamp_to_zero
     * 	Clamp rate to zero? Otherwise, shift entire function vertically to zero.
     */
    public FractalSFR(double t_min, double t_max, int magnitude, double r0, double H, double std, boolean clamp_to_zero) {
    	super(t_min, t_max);
    	this.magnitude = magnitude;
    	this.r0 = r0;
    	this.H = H;
    	this.std = std;
    	this.clamp_to_zero = clamp_to_zero;
        init();
    }
    
    /**
     * {@inheritDoc}
     */
	@Override
	public BaseSfr copy() {
		
		// Copy internal parameters of fractal generation
		FractalSFR copy = new FractalSFR(t_min, t_max, magnitude, r0, H, std, clamp_to_zero);
		
		// Copy the current realisation of the star formation rate function
		copy.data = new DiscreteFunction1D(data);
		
		return copy;
	}
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
    	return "Fractal";
    }
    
    /**
     * Generate a new realization of the star formation rate using the current fractal parameters.
     */
    public final void init() {

        // Number of discrete points N = 2^magnitude + 1
        int N = 1;
        for(int n=0; n<magnitude; n++) {
        	N *= 2;
        }
        N++;
        
        // Width of each time bin
        double dT = (t_max - t_min)/N;
        
        // Build bin centres & bin widths arrays
        double[] centres = new double[N];
        double[] widths  = new double[N];
        double[] rates   = new double[N];
        double[] errors   = new double[N];  // errors initialised to zero
        
        for(int i=0; i<N; i++) {
            centres[i] = i*dT + dT/2.0 + t_min;
            widths[i]  = dT;
            errors[i]  = 0;
        }
        
        // Initialise end points
        rates[0] = rates[N-1] = r0;
        // Initialise remaining rate values.
        recursiveMidpointDisplacement(rates, 0, N-1, std, H, 1);
        
        // Now post-process fractal star formation rate so that no rate value
        // is negative. Either clamp negative rates to zero, or shift entire
        // function vertically so that minimum rate is zero.
        if(clamp_to_zero) {
            for(int i=0; i<rates.length; i++) {
                if(rates[i]<0) {
                    rates[i] = 0.0;
                }
            }
        }
        else {
            double min_rate = Double.MAX_VALUE;
            for(int i=0; i<rates.length; i++) {
                if(rates[i]<min_rate) {
                    min_rate = rates[i];
                }
            }
            for(int i=0; i<rates.length; i++) {
                rates[i] -= min_rate;
            }
        }     
        
        data = new DiscreteFunction1D(centres, widths, rates, errors);
    }
    
    /**
     * Recursive midpoint displacement algorithm for generation of fractals.
     * 
     * @param fractal
     * @param A
     * @param B
     * @param init_std
     * @param h
     * @param n
     */
    private static void recursiveMidpointDisplacement(double[] fractal, int A, int B, double init_std, double h, int n) {
        
        // Location of midpoint
        int C = (A+B)/2;
        
        // calculate standard deviation for this level
        double std = init_std * Math.sqrt((1 - Math.pow(2,2*h - 2)) / Math.pow(2,2*n*h-2));
        
        // Midpoint, with no displacement
        fractal[C] = (fractal[A] + fractal[B])/2.0;
        
        // Displacement
        fractal[C] += std * rng.nextGaussian();
        
        // Is this the final level of recursion?
        if(B-A == 2) return;
        
        else{
            recursiveMidpointDisplacement(fractal,A,C,init_std,h,n+1);
            recursiveMidpointDisplacement(fractal,C,B,init_std,h,n+1);
        }
    }
    
}