package photometry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import constants.Functions;
import numeric.functions.Linear;

/**
 * Utility methods for {@link Filter}s.
 *
 * TODO: cross check Vega constants for filters against external source
 * TODO: cross check effective wavelengths of filters against external source
 * TODO: cross check blackbody colours against external source
 * TODO: use a generalized integrator class for synthetic photometry intergals
 *
 * @author nrowell
 * @version $Id$
 */
public class FilterUtils {

	/**
	 * The logger.
	 */
	private static final Logger logger = Logger.getLogger(FilterUtils.class.getName());
	
	/**
	 * The path to the Vega spectrum.
	 * Vega spectrum is stored in alpha_lyr_stis_004.ascii, downloaded from
	 * STIS website as explained in the file.
	 * 
	 * Units are erg/s/cm**2/A (wavelength flux density) and can be directly integrated.
	 * 
	 * Old spectra (vegaCastelli.dat) is in terms of Eddington frequency flux - erg/s/cm**2/Hz/Sr
	 * and need to be corrected to wavelength flux density before they can be used.
	 * New spectra gives vega constants that are greater by ~0.03 mags.
	 * 
	 * Procedure for doing this, in case it needs to be done in future:
	 * 
	 * Frequency flux density obtained from Eddington frequency flux density by multiplying
	 * by 4*PI*(R/D)**2   where R and D are radius and distance of Vega respectively.
	 * 
	 * Wavelength flux density obtained from frequency flux density by multiplying by frequency
	 * (nu = c/lambda) then dividing by wavelength in Angstroms.
	 */
	private static String vegaSpectrumPath = "resources/spectro/vega/alpha_lyr_stis_004.ascii";
	
	/**
	 * Vega radius (TODO: source?) [metres]
	 */
	public final static double vega_radius = 1.93488E9;
    
    /**
     * Vega heliocentric distance (TODO: source?) [metres]
     */
	public final static double vega_distance = 2.3944E17;
    
    /**
     * Vega solid angle (pi * (R/D)^2) [sr]
     */
	public final static double omega = Math.PI*(vega_radius/vega_distance)*(vega_radius/vega_distance);
    
    /**
     * Linear interpolation object used to interpolated the Vega spectrum. This is initialised
     * when first required then reused.
     * 
     * TODO: add source for Vega spectrum
     * 
     */
    private static Linear vegaSpectrum = null;
    
    /**
     * Get the Vega specturm as {@link Linear} interpolation object.
     * 
     * TODO: add declaration of the units used.
     * 
     * @return
     * 	A {@link Linear} interpolationg object loaded with the Vega spectrum.
     */
    public static Linear getVegaSpectrum() {
    	if(vegaSpectrum==null) {
    		try {
				initialiseVegaSpectrum();
			} catch (IOException e) {
				logger.log(Level.SEVERE, "Unable to initialise Vega spectrum!", e);
			}
    	}
    	return vegaSpectrum;
    }
    
    /**
     * Initialise the Vega spectrum
     * @throws IOException
     * 	If there's a problem reading the Vega spectrum from file
     */
    private static void initialiseVegaSpectrum() throws IOException {
    	
		// Read the Vega spectrum to a Linear interpolation object
		InputStream is = (new FilterUtils()).getClass().getClassLoader().getResourceAsStream(vegaSpectrumPath);
		BufferedReader in = new BufferedReader(new InputStreamReader(is));
		
		List<Double> vegaWavelength = new LinkedList<>();
        List<Double> vegaFlux = new LinkedList<>();
        String record;

        // Loop over table and store all points
        while ((record = in.readLine()) != null) 
        {
        	// Avoid blank lines
        	if (record.length()==0)
                continue;
        	
            // Avoid any commented out lines
            if (record.substring(0, 1).equals("#"))
                continue;
                    
            Scanner scan = new Scanner(record);                 
            
            double angstroms = scan.nextDouble();
            double flux = scan.nextDouble();
            
            vegaWavelength.add(angstroms);
            vegaFlux.add(flux);
            
            scan.close();
        }
		
        vegaSpectrum = new Linear(vegaWavelength, vegaFlux);
    }
    
    /**
     * Compute the effective wavelength of the {@link Filter}.
     * 
     * TODO: add synthetic photometry integral.
     * 
     * @param filter
     * 	The {@link Filter}
     * @return
     * 	The effective wavelength [Angstroms]
     */
    public static double getEffectiveWavelength(Filter filter) {
    	
		// Integration step size, in Angstroms:
		double l_step = 1.0;
		
		// Intergation limits
		double l_min = filter.lambdaMin;
		double l_max = filter.lambdaMax;
    	
		double ls_h  = 0;
	    double ls_2h = 0;
	    double s_h  = 0;
	    double s_2h = 0;
	    
	    for(double lambda = l_min; lambda < l_max; lambda += 2*l_step) {

			double t0 = filter.interpolate(lambda);
			double t1 = filter.interpolate(lambda+l_step);
			double t2 = filter.interpolate(lambda+2*l_step);
    	
    	
			//Contributions to h sums:
			ls_h += (lambda*t0 + (lambda+l_step)*t1)*(l_step/2);
			ls_h += ((lambda+l_step)*t1 + (lambda+2*l_step)*t2)*(l_step/2);
			
			s_h += (t0 + t1)*(l_step/2);
            s_h += (t1 + t2)*(l_step/2);
            
			//Contributions to 2h sums
            ls_2h += (lambda*t0 + (lambda+2*l_step)*t2)*(l_step);
            s_2h += (t0 + t1)*(l_step);
		}

		// Richardson's extrapolation:
	    double ls = (1.0/3.0)*(4.0*ls_h - ls_2h);
    	double s  = (1.0/3.0)*(4.0*s_h - s_2h);
    	
		double lambda_eff = ls/s;
		
    	return lambda_eff;
    }
    
	/**
	 * This method obtains the magnitude zeropoint for the filter in the Vega system.
	 * In the Vega system, Vega is defined as having a magnitude of zero in all filters.
	 * So computing the zeropoint for a given filter amounts to computing the magnitude
	 * of Vega in that band, which is done using synthetic photometry.
	 * 
	 * TODO: add synthetic photometry intergal to comments.
	 * 
	 * @param filter
	 * 	The {@link Filter} for which to compute the Vega zeropoint. The transmission
	 * function must be in transmission as a function of wavelength in Angstroms.
	 * 
	 * @return
	 * 	The magnitude zeropoint for the {@link Filter} in the Vega system.
	 */
	public static double getVegaMagZp(Filter filter) {

		Linear vegaSpec = getVegaSpectrum();
        
		/********************************************************

	        Synthetic photometry integral.

		  Integral is:
		  
		  F_m  =  INT_0^infinity(f_vega(l)*transmission(l).dl) / INT(transmission(l).dl)

	          then Vega constant is defined as:
		  
		  C_m = 2.5 log_10(F_m)

	        Integrate from first wavelength in filter transmission file to last wavelength.

	        Integrate numerically using trapezium rule with interval halving
		and Richardson's extrapolation algorithm.
		Use constant wavelength intervals, interpolating both functions at each point using cubic splines.

		Integration is between first and last points in transmission function

	        ********************************************************/

		// Integration step size, in Angstroms:
		double l_step = 1.0;
		
		// Intergation limits
		double l_min = filter.lambdaMin;
		double l_max = filter.lambdaMax;
		
		// Two integrals;
		// f(lambda)S(lambda) - integrated flux
		// S(lambda)          - normalisation

		double fs_h  = 0;
		double fs_2h = 0;
	    double s_h  = 0;
	    double s_2h = 0;

		for(double lambda = l_min; lambda < l_max; lambda += 2*l_step) {

			double t0 = filter.interpolate(lambda);
			double t1 = filter.interpolate(lambda+l_step);
			double t2 = filter.interpolate(lambda+2*l_step);
			
			double f0 = vegaSpec.interpolateY(lambda)[0];
			double f1 = vegaSpec.interpolateY(lambda+l_step)[0];
			double f2 = vegaSpec.interpolateY(lambda+2*l_step)[0];
			
			//Contributions to h sums:
			fs_h += (f0*t0 + f1*t1)*(l_step/2);
            fs_h += (f1*t1 + f2*t2)*(l_step/2);
            
            s_h += (t0 + t1)*(l_step/2);
            s_h += (t1 + t2)*(l_step/2);

			//Contributions to 2h sums
            fs_2h += (f0*t0 + f2*t2)*(l_step);
            s_2h += (t0 + t1)*(l_step);
		}

		// Richardson's extrapolation:
		double fs = (1.0/3.0)*(4.0*fs_h - fs_2h);
		double s  = (1.0/3.0)*(4.0*s_h  -  s_2h);
		
		double vegaZp = 2.5*Math.log10(fs/s);
		
		return vegaZp;
	}
	    
	/**
	 * Computes the colour of a blackbody of the given temperature, in the given {@link Filter}s.
	 * Specifically, compute the <code>band1 - band2</code> value.
	 * 
	 * TODO: add synthetic photometry integral
	 * 
	 * @param band1
	 * 	The first {@link Filter}
	 * @param band2
	 * 	The second {@link Filter}
	 * @param T
	 * 	The blackbody temperature [K]
	 * @return
	 * 	The colour of a blackbody at temperature K in the given {@link Filter}s
	 */
	public static double blackbodyColour(Filter band1, Filter band2, double T) {
		return blackbodyMagnitude(band1, T) - blackbodyMagnitude(band2, T);
    }

	/**
	 * Computes the magnitude of a blackbody of the given temperature in the given {@link Filter}. Note that
	 * the absolute scale is arbitrary and the returned value is only useful for computing colours of blackbodies
	 * in two bands from the difference of the single-band magnitudes.
	 * 
	 * 
	 * TODO: add synthetic photometry integral
	 * 
	 * @param filter
	 * 	The {@link Filter} for which to determine the blackbody magnitude
	 * @param T
	 * 	The temperature of the blackbody [K]
	 * @return
	 * 	The magnitude of the blackbody through the given {@link Filter}, in the Vega system.
	 * 
	 */
	public static double blackbodyMagnitude(Filter filter, double T) {
		
		/********************************************************
		
        Synthetic photometry integral.

	  Integral is:
	  
	  F_m  =  INT_0^infinity(f_BB(l)*transmission(l).dl) / INT(transmission(l).dl)

          then magnitude is calculated from:

          m = -2.5*log(F_m) + C_m

          where Vega constants are defined at the start of this code.

        Integrate from first wavelength in filter transmission file to last wavelength.

        Integrate numerically using trapezium rule and fixed wavelength intervals,
        interpolating at each point using cubic splines.


        ********************************************************/

		// Integration step size, in Angstroms:
		double l_step = 1.0;
		
		// Intergation limits
		double l_min = filter.lambdaMin;
		double l_max = filter.lambdaMax;
		
		double bs_h  = 0.0;   // Contributions to h and 2h sums
		double bs_2h = 0.0;
		
		double s_h  = 0.0;   // Contributions to h and 2h sums
		double s_2h = 0.0;
		
		// Integrate filter over BB curve at temperature T:
		for(double lambda =l_min; lambda < l_max; lambda += 2.0*l_step) {
			
			double t0 = filter.interpolate(lambda + 0*l_step);
			double t1 = filter.interpolate(lambda + 1*l_step);
			double t2 = filter.interpolate(lambda + 2*l_step);
			
			double bb0 = Functions.planckFunction(T,lambda + 0*l_step);
			double bb1 = Functions.planckFunction(T,lambda + 1*l_step);
			double bb2 = Functions.planckFunction(T,lambda + 2*l_step);
			
		    bs_h  += (t0*bb0 + t1*bb1)*(l_step/2.0);
		    bs_h  += (t1*bb1 + t2*bb2)*(l_step/2.0);
		    bs_2h += (t0*bb0 + t2*bb2)*l_step;


		    s_h  += (t0 + t1)*(l_step/2.0);
		    s_h  += (t1 + t2)*(l_step/2.0);
		    s_2h += (t0 + t2)*l_step;

		}
		
		// Vega zeropoint magnitude for the filter
		double m0 = FilterUtils.getVegaMagZp(filter);
	
		// Richardson extrapolation
	    double mag = -2.5*Math.log10(((1.0/3.0)*(4.0*bs_h - bs_2h))/((1.0/3.0)*(4.0*s_h  - s_2h))) + m0;
	
		return mag;
	}

}
