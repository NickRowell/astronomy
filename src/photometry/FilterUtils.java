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
import numeric.integration.IntegrableFunction;
import numeric.integration.IntegrationUtils;
import utils.SpectroscopicUtils;

/**
 * Utility methods for {@link Filter}s.
 *
 * TODO: cross check Vega constants for filters against external source
 * TODO: cross check effective wavelengths of filters against external source
 * TODO: cross check blackbody colours against external source
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
	 * The synthetic photometry integral is:
	 * 
	 * F_m  =  INT_0^infinity(l * transmission(l).dl) / INT(transmission(l).dl)
	 * 
     * @param filter
     * 	The {@link Filter}
     * @return
     * 	The effective wavelength [Angstroms]
     */
    public static double getEffectiveWavelength(final Filter filter) {
    	
		// Integration step size, in Angstroms:
		double l_step = 1.0;
		
		// Integration limits
		double l_min = filter.lambdaMin;
		double l_max = filter.lambdaMax;
    	
		// Function to represent the numerator in the synthetic photometry integral
		class Numerator implements IntegrableFunction
		{
			public double evaluate(double x)
			{
				return x * filter.interpolate(x);
			}
		}
		
		// Function to represent the denominator in the synthetic photometry integral
		class Denominator implements IntegrableFunction
		{
			public double evaluate(double x)
			{
				return filter.interpolate(x);
			}
		}
		
    	double lt = IntegrationUtils.integrate(new Numerator(), l_min, l_max, l_step);
		double  t = IntegrationUtils.integrate(new Denominator(), l_min, l_max, l_step);
		
    	return lt/t;
    }
    
	/**
	 * This method obtains the magnitude zeropoint for the filter in the Vega system.
	 * In the Vega system, Vega is defined as having a magnitude of zero in all filters.
	 * So computing the zeropoint for a given filter amounts to computing the magnitude
	 * of Vega in that band, which is done using synthetic photometry.
	 * 
	 * The synthetic photometry integral is:
	 * 
	 * F_m  =  INT_0^infinity(f_vega(l)*transmission(l).dl) / INT(transmission(l).dl)
	 * 
	 * then Vega constant is defined as:
	 * 
	 * C_m = 2.5 log_10(F_m)
	 * 
	 * @param filter
	 * 	The {@link Filter} for which to compute the Vega zeropoint. The transmission
	 * function must be in transmission as a function of wavelength in Angstroms.
	 * 
	 * @return
	 * 	The magnitude zeropoint for the {@link Filter} in the Vega system.
	 */
	public static double getVegaMagZp(final Filter filter) {

		final Linear vegaSpec = getVegaSpectrum();
        
		// Integration step size [Angstroms]
		double l_step = 0.5;
		
		// Integration limits [Angstroms]
		double l_min = filter.lambdaMin;
		double l_max = filter.lambdaMax;
		
		// Function to represent the numerator in the synthetic photometry integral
		class Numerator implements IntegrableFunction
		{
			public double evaluate(double x)
			{
				return filter.interpolate(x) * vegaSpec.interpolateY(x)[0];
			}
		}
		
		// Function to represent the denominator in the synthetic photometry integral
		class Denominator implements IntegrableFunction
		{
			public double evaluate(double x)
			{
				return filter.interpolate(x);
			}
		}
		
		double ft = IntegrationUtils.integrate(new Numerator(), l_min, l_max, l_step);
		double  t = IntegrationUtils.integrate(new Denominator(), l_min, l_max, l_step);
		
		return 2.5*Math.log10(ft/t);
	}
	    
	/**
	 * Computes the colour of a blackbody of the given temperature, in the given {@link Filter}s.
	 * Specifically, compute the <code>band1 - band2</code> value.
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
	 * The synthetic photometry integral is:
	 * 
	 * F_m  =  INT_0^infinity(f_BB(l)*transmission(l).dl) / INT(transmission(l).dl)
	 * 
	 * then magnitude is calculated from:
	 * 
	 * m = -2.5*log(F_m) + C_m
	 * 
	 * where Vega constants C_m are computed using {@link #getVegaMagZp(Filter)}.
	 * 
	 * @param filter
	 * 	The {@link Filter} for which to determine the blackbody magnitude
	 * @param T
	 * 	The temperature of the blackbody [K]
	 * @return
	 * 	The magnitude of the blackbody through the given {@link Filter}, in the Vega system.
	 * 
	 */
	public static double blackbodyMagnitude(final Filter filter, final double T) {
		
		// Integration step size, in Angstroms:
		double l_step = 1.0;
		
		// Integration limits
		double l_min = filter.lambdaMin;
		double l_max = filter.lambdaMax;
		
		// Function to represent the numerator in the synthetic photometry integral
		class Numerator implements IntegrableFunction
		{
			public double evaluate(double x)
			{
				return filter.interpolate(x) * Functions.planckFunction(T,x);
			}
		}
		
		// Function to represent the denominator in the synthetic photometry integral
		class Denominator implements IntegrableFunction
		{
			public double evaluate(double x)
			{
				return filter.interpolate(x);
			}
		}
		
		double ft = IntegrationUtils.integrate(new Numerator(), l_min, l_max, l_step);
		double  t = IntegrationUtils.integrate(new Denominator(), l_min, l_max, l_step);
		
		// Vega zeropoint magnitude for the filter
		double m0 = getVegaMagZp(filter);
		
		return -2.5*Math.log10(ft/t) + m0;
	}
	
	
	/**
	 * Computes the photon-weighted effective wavenumber for a blackbody source of the
	 * given temperature in the given {@link Filter}.
	 * 
	 * @param filter
	 * 	The {@link Filter} used to modulate the blackbody spectrum
	 * @param T
	 * 	The temperature of the blackbody [K]
	 * @return
	 * 	The photon weighted effective wavenumber [nm^{-1}]
	 */
	public static double getBlackbodyEffectiveWavenumber(final Filter filter, final double T) {

		// Integration step size, in Angstroms:
		double l_step = 1.0;
		
		// Integration limits
		double l_min = filter.lambdaMin;
		double l_max = filter.lambdaMax;
		
		// Function to represent the numerator in the synthetic photometry integral
		class Numerator implements IntegrableFunction
		{
			public double evaluate(double x)
			{
				// Wavelength in nanometres
				double l = x / 10;
				
				return (1.0/l) * filter.interpolate(x) * Functions.planckFunction(T,x);
			}
		}
		
		// Function to represent the denominator in the synthetic photometry integral
		class Denominator implements IntegrableFunction
		{
			public double evaluate(double x)
			{
				return filter.interpolate(x) * Functions.planckFunction(T,x);
			}
		}
		double numer = IntegrationUtils.integrate(new Numerator(), l_min, l_max, l_step);
		double denom = IntegrationUtils.integrate(new Denominator(), l_min, l_max, l_step);
		
		return numer / denom;
	}

}
