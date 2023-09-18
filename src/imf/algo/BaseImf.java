package imf.algo;

/**
 * Supertype for all implementations of the Initial Mass Function.
 *
 * @author nrowell
 * @version $Id$
 */
public abstract class BaseImf 
{
    /** 
     * Lower limit on the stellar mass range. The IMF is normalised over this interval,
     * and no stars outside this range will be created.
     * This is the chosen limit for normalisation because stars lighter than
     * this take longer than a Hubble time to form white dwarfs, so never
     * contribute to any WDLF models.
     */
    public static final double M_lower = 0.6;

    /**
     * Upper limit on the stellar mass range. The IMF is normalised over this interval,
     * and no stars outside this range will be created.
     */
    public static final double M_upper  = 7.0;
    
    /**
     * Get the IMF at the given solar mass.
     * @param M
     * 		The stellar mass [M_{solar}]
     * @return
     * 	The IMF, in stars per solar mass.
     */
    public abstract double getIMF(double M);
    
    /**
     * Get the fraction of stars that have masses lower than M.
     * @param M
     * 		The stellar mass [M_{solar}]
     * @return
     * 		The fraction of stars that have masses in the range [{@link BaseImf#M_lower} : M]
     */
    public abstract double getIntegral(double M);
    
    /**
     * Draw a random mass from the range {@link BaseImf#M_lower} -> {@link BaseImf#M_upper}.
     * @return
     * 		Random stellar mass in the range [{@link BaseImf#M_lower}:{@link BaseImf#M_upper}] (M_{solar})
     */
    public abstract double drawMass();
    
}
