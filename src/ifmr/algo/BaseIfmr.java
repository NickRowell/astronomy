package ifmr.algo;

/**
 * Supertype for all implementations of the Initial-Final Mass Relation.
 * 
 * TODO: Catalan 2008 IFMR does not properly invert - the clamping at high mass is not correctly implemented.
 * 
 * @author Nicholas Rowell
 * 
 */
public abstract class BaseIfmr
{
   
    /**
     * Return final mass of WD given initial mass of MS star.
     */
    public abstract double getMf(double mi);
    
    /**
     * Return initial mass of MS star given mass of WD.
     */
    public abstract double getMi(double mf);
    
    /**
     * Get initial mass at which final mass = initial mass. This indicates the initial mass
     * below which the relation becomes unphysical.
     */
    public abstract double getBreakdownInitialMass();
    
    /**
     * Provide a human-readable name for GUI options & output file.
     */
    @Override
    public abstract String toString();
    
}