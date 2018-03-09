package ifmr.algo;

/**
 * Supertype for all implementations of the Initial-Final Mass Relation.
 * 
 * TODO: Catalan 2008 IFMR does not properly invert - the clamping at high mass is not correctly implemented.
 * 
 * @author Nicholas Rowell
 * 
 */
public abstract class BaseIfmr {
	
	/**
	 * Get the (final) mass of the white dwarf formed from a main sequence star of the given (initial) mass.
	 * @param mi
	 * 	The initial (main sequence) mass [M_{solar}]
	 * @return
	 * 	The final (white dwarf) mass [M_{solar}]
	 */
    public abstract double getMf(double mi);
    
    /**
	 * Get the (initial) mass of the main sequence star that forms a white dwarf star of the given (final) mass.
	 * @param mf
	 * 	The final (white dwarf) mass [M_{solar}]
	 * @return
	 * 	The initial (main sequence) mass [M_{solar}]
	 */
    public abstract double getMi(double mf);
    
    /**
     * Get initial mass at which final mass = initial mass. This indicates the initial mass
     * below which the relation becomes unphysical.
     * @return
     * 	The mass at which the initial mass equals the final mass [M_{solar}]
     */
    public abstract double getBreakdownInitialMass();
    
    /**
     * Provide a human-readable name for GUI options & output file.
     */
    @Override
    public abstract String toString();
    
}