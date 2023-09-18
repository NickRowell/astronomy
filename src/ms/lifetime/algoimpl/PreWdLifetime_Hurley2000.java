package ms.lifetime.algoimpl;

import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Logger;

import ms.lifetime.algo.PreWdLifetime;

/**
 * Class provides an implementation of the {@link PreWdLifetime} that encapsulates the analytic formulae
 * for stellar evolution as a function of mass and metallicity from:
 * 
 * "Comprehensive analytic formulae for stellar evolution as a function of mass and metallicity",
 *  Hurley, Jarrod R.; Pols, Onno R.; Tout, Christopher A.
 *  Monthly Notices of the Royal Astronomical Society, Volume 315, Issue 3, pp. 543-569 (2000)
 * 
 * @author nrowell
 * @version $Id$
 */
public class PreWdLifetime_Hurley2000 implements PreWdLifetime {

	/**
	 * The {@link Logger}.
	 */
	private static final Logger logger = Logger.getLogger(PreWdLifetime_Hurley2000.class.getName());

	/**
	 * Tolerance on main sequence lifetime in binary search algorithm for stellar mass; we find the mass
	 * whose lifetime is at least this close to the requested lifetime [Myr].
	 */
    private static final double T_MS_TOL = 1e-6;
    
    /**
     * Maximum number of iterations in binary search algorithms.
     */
    private static final int MAX_ITER = 100;
    
    /**
     * Step size in main sequence lifetime to use in finite difference estimate of derivative [Myr].
     */
    private static final double DT_FINITE_DIFF = 1;
    
    /**
     * Step size in stellar mass to use in finite difference estimate of derivative.
     */
    private static final double DM_FINITE_DIFF = 0.001;
    
	/**
	 * Cache of values of the coefficients a1-a10 as a function of metallicity Z.
	 */
	SortedMap<Double, double[]> coeffs;
	
	/**
	 * Main constructor for the {@link PreWdLifetime_Hurley2000}.
	 */
	public PreWdLifetime_Hurley2000() {
		// Initialise coefficients cache
		coeffs = new TreeMap<>();
	}
	
	@Override
	public String getName() {
		return "Hurley et al. (2000)";
	}
	
	/**
	 * Compute the values of the coefficients a1 to a10 for the given metallicity.
	 * 
	 * @param z
	 * 	The metallicity Z.
	 * @return
	 * 	An array containing the values of the coefficients a1 to a10 for the given metallicity.
	 */
	private static double[] getCoeffs(double z) {
		
		double zeta = Math.log10(z / 0.02);
		double zeta2 = zeta * zeta;
		double zeta3 = zeta2 * zeta;
		
		// Coefficients of equation 4
		double[] a = new double[10];
		a[0] = 1.593890e3  + 2.053038e3  * zeta + 1.231226e3  * zeta2 + 2.327785e2  * zeta3;
		a[1] = 2.706708e3  + 1.483131e3  * zeta + 5.772723e2  * zeta2 + 7.411230e1  * zeta3;
		a[2] = 1.466143e2  - 1.048442e2  * zeta - 6.795374e1  * zeta2 - 1.391127e1  * zeta3;
		a[3] = 4.141960e-2 + 4.564888e-2 * zeta + 2.958542e-2 * zeta2 + 5.571483e-3 * zeta3;
		a[4] = 3.426349e-1;
		a[5] = 1.949814e1  + 1.758178e0  * zeta - 6.008212e0  * zeta2 - 4.470533e0  * zeta3;
		a[6] = 4.903830e0;
		a[7] = 5.212154e-2 + 3.166411e-2 * zeta - 2.750074e-3 * zeta2 - 2.271549e-3 * zeta3;
		a[8] = 1.312179e0  - 3.294936e-1 * zeta + 9.231860e-2 * zeta2 + 2.610989e-2 * zeta3;
		a[9] = 8.073972e-1;
		
		return a;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double[] getPreWdLifetime(double z, double y, double mass) {
		
		// Main sequence lifetime [Myr]
		double t_MS = getMainSequenceLifetime(z, mass);
		
		// Estimate first derivative by finite differences [Myr/M_{solar}]
		double dt_dM = (getMainSequenceLifetime(z, mass + DM_FINITE_DIFF) - getMainSequenceLifetime(z, mass - DM_FINITE_DIFF)) / (2 * DM_FINITE_DIFF);
		
		// Scale lifetime to per-year units
		t_MS *= 1e6;
		dt_dM  *= 1e6;
		
		return new double[]{t_MS, dt_dM};
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double[] getStellarMass(double z, double y, double lifetime) {
		
		// Scale lifetime to per-megayear units
		double tMyr = lifetime/1e6;
		
		// Stellar mass
		double mass = getStellarMass(z, tMyr);

		// Estimate first derivative by finite differences
		double dM_dt = (getStellarMass(z, tMyr + DT_FINITE_DIFF) - getStellarMass(z, tMyr - DT_FINITE_DIFF)) / (2 * DT_FINITE_DIFF);

		return new double[]{mass, dM_dt};
	}

	/**
	 * Compute the time to the base of the giant branch, for the given metallicity and mass. This is an
	 * implementation of equation 4.
	 * 
	 * @param z
	 * 	The metallicity Z.
	 * @param mass
	 * 	The stellar mass [M_{solar}]
	 * @return
	 * 	The time to the base of the giant branch [Myr].
	 */
	private double getTimeToBaseOfGiantBranch(double z, double mass) {
		
		// Compute coefficients if necessary
		if(!coeffs.containsKey(z)) {
			coeffs.put(z, getCoeffs(z));
		}
		// Retrieve coefficients
		double[] a = coeffs.get(z);
		
		double mass2 = mass * mass;
		double mass4 = mass2 * mass2;
		double mass7 = mass4 * mass2 * mass;
		double mass5p5 = Math.pow(mass, 5.5);
		
		// Time to the base of the giant branch (equation 4)
		double t_BGB = (a[0] + a[1] * mass4 + a[2] * mass5p5 + mass7) / (a[3] * mass2 + a[4] * mass7);
		
		return t_BGB;
	}
	
	/**
	 * Compute the main sequence lifetime, for the given metallicity and mass. This is an
	 * implementation of equation 5.
	 * 
	 * @param z
	 * 	The metallicity Z.
	 * @param mass
	 * 	The stellar mass [M_{solar}]
	 * @return
	 * 	The main sequence lifetime [Myr].
	 */
	private double getMainSequenceLifetime(double z, double mass) {
		
		double zeta = Math.log10(z / 0.02);

		// Compute coefficients if necessary
		if(!coeffs.containsKey(z)) {
			coeffs.put(z, getCoeffs(z));
		}
		// Retrieve coefficients
		double[] a = coeffs.get(z);
		
		// Time to the base of the giant branch (equation 4)
		double t_BGB = getTimeToBaseOfGiantBranch(z, mass);
		
		// Main sequence lifetime
		double x = Math.max(0.95, Math.min(0.95 - 0.03 * (zeta + 0.30103), 0.99));
		double mu = Math.max(0.5, 1.0 - 0.01 * Math.max(a[5]/Math.pow(mass, a[6]), a[7] + a[8]/Math.pow(mass, a[9])));
		
		double t_MS = Math.max(mu * t_BGB, x * t_BGB);
		
		return t_MS;
	}
	
	/**
	 * Get the (approximate) stellar mass corresponding to the given main sequence lifetime and metallicity.
	 * This uses a binary search algorithm to find the mass whose lifetime is within {@link #T_MS_TOL} of
	 * the requested one.
	 * 
	 * @param z
	 * 	The metallicity Z.
	 * @param lifetime
	 * 	The main sequence lifetime [Myr]
	 * @return
	 * 	The stellar mass [M_{solar}]
	 */
	private double getStellarMass(double z, double lifetime) {
		
		// Binary search algorithm

        // Initialise search domain for the stellar mass
        double min = 0.7;
        double max = 50.0;

        // Current test mass
        double mid = (min + max) / 2.0;
        double t_MS = getMainSequenceLifetime(z, mid);

        int nIter = 0;
        while (Math.abs(t_MS - lifetime) > T_MS_TOL) {

            // Check lifetime at current midpoint:
            if (t_MS < lifetime) {
            	// Lifetime at current mass is shorter than the requested lifetime; the
            	// desired mass is lower than the current test mass and must lie in the
            	// range [min:mid]; shift the upper boundary of the test range.
                max = mid;
            } else {
            	// Lifetime at current mass is longer than the requested lifetime; the
            	// desired mass is higher than the current test mass and must lie in the
            	// range [mid:max]; shift the lower boundary of the test range.
                min = mid;
            }
            
            mid = (min + max) / 2.0;
            
            t_MS = getMainSequenceLifetime(z, mid);

            if (nIter++ > MAX_ITER) {
                throw new RuntimeException("Couldn't locate stellar mass; check main sequence lifetime formula!");
            }
        }
		
        logger.fine("Binary search for stellar mass converged in " + nIter + " iterations");
        
        return mid;
	}

}