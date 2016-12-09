package wd.models.algo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Logger;

import photometry.Filter;
import wd.models.infra.WdAtmosphereType;

/**
 * Class encapsulates a set of WD cooling models from a particular research group, i.e. Montreal, LPCODE
 * etc. In principle the set provides all atmosphere types and filters required for population modelling.
 *
 * The design of this class enforces a few constraints on the set of models:
 *  - It assumes that each model grid for the same atmosphere type (but different filters) uses the same
 *    set of masses.
 *  - I.e. different filters/colours for the same atmosphere type have the same set of masses for the grid
 *  
 *
 *  - Different WD atmosphere types can have cooling tracks that use a different grid of mass points
 *  - Different WD atmosphere types can provide different filters; the model set publishes the join of
 *    these for use in modelling
 * 
 * 
 * Implementations of this class need to perform two tasks:
 * 	- Implement the load(Filter, WdAtmosphereType) method
 *  - Populate the wdAtmosphereTypes, filtersByAtm and massGridByAtm maps within the constructor
 *
 * @author nrowell
 * @version $Id$
 */
public abstract class WdCoolingModelSet {

	/**
	 * The logger.
	 */
	protected static final Logger logger = Logger.getLogger(WdCoolingModelSet.class.getName());
	
	/**
	 * Mapping of all loaded {@link WdCoolingModelGrid} by {@link Filter} and {@link WdAtmosphereType}.
	 * This map is initialised in a lazy fashion as the model set is used.
	 */
    protected final Map<Filter, Map<WdAtmosphereType, WdCoolingModelGrid>> coolingModelsByFilter = new HashMap<>();
	
	/**
	 * {@link Set} of all {@link WdAtmosphereType}s provided by the Montreal group models.
	 * NOTE: contructors in implementing classes must populate this set.
	 */
	protected final Set<WdAtmosphereType> wdAtmosphereTypes = new HashSet<WdAtmosphereType>();
	
	/**
	 * Map the {@link WdAtmosphereType} to the {@link Set} of all {@link Filter}s.
	 * NOTE: contructors in implementing classes must populate this map.
	 */
	protected final Map<WdAtmosphereType, Set<Filter>> filtersByAtm = new HashMap<>();
	
	/**
	 * Map the {@link WdAtmosphereType} to the array of mass grid points.
	 * NOTE: contructors in implementing classes must populate this map.
	 */
	protected final Map<WdAtmosphereType, double[]> massGridByAtm = new HashMap<>();

    /**
     * Load the {@link WdCoolingModelGrid} corresponding to the given {@link Filter} and
     * {@link WdAtmosphereType}.
     * 
     * @param filter
     * 	The {@link Filter} to load the cooling track data for
     * @param atm
     * 	The {@link WdAtmosphereType} to load the cooling track data for.
     */
    protected abstract WdCoolingModelGrid load(Filter filter, WdAtmosphereType atm);
    
    /**
     * Get the name of the source of the WD cooling models.
     * @return
     * 	String containing the name (usually single word) of the source of the WD cooling models.
     */
    protected abstract String getName();
    
    @Override
    public String toString() {
    	return getName();
    }
    
    /**
     * Get a {@link Set} containing all the {@link WdAtmosphereType}s available
     * in this {@link WdCoolingModelSet}.
     * @return
     * 	A {@link Set} containing all the {@link WdAtmosphereType}s available
     * in this {@link WdCoolingModelSet}.
     */
    public Set<WdAtmosphereType> getWdAtmosphereTypes() {
		return wdAtmosphereTypes;
    }
    
    /**
     * Get a {@link Set} containing the bands that are available in the cooling models for the
     * given {@link Filter}.
     * @return
     * 	A {@link Set} containing the bands that are available in the cooling models for the
     * given {@link Filter}.
     */
    public Set<Filter> getPassbands(WdAtmosphereType atm) {
		return filtersByAtm.get(atm);
    }
    
    /**
     * Get array of stellar mass values of model grid for the given {@link WdAtmosphereType}.
     * @return
     * 	Array containing the values of stellar mass at which the models are computed.
     */
	public double[] getMassGridPoints(WdAtmosphereType atm) {
		return massGridByAtm.get(atm);
	}
    
    /**
     * Get the intersection of the passbands available in all {@link WdAtmosphereType}s included in
     * this model set.
     * @return
     * 	Set of the passbands common to all the {@link WdAtmosphereType} available in this model set.
     */
    public Filter[] getPassbands() {
    	
    	// As long as this {@link NewWdCoolingModelSet} includes at least one
    	// {@link WdAtmosphereType} then se can compute the intersection.
    	if(getWdAtmosphereTypes().size()==0) {
    		return new Filter[0];
    	}
    	
    	Iterator<WdAtmosphereType> it = getWdAtmosphereTypes().iterator();
    	
    	// Get the intersection of the filter sets for each atmosphere type. Use a SortedSet in order to
    	// return the {@link Filter}s in a sensible order, i.e. the order that they occur in the enum definition.
    	SortedSet<Filter> intersection = new TreeSet<>();
    	
    	// Initialise the intersection with all {@link Filter}s available for the first {@link WdAtmosphereType}
    	intersection.addAll(getPassbands(it.next()));
    	
    	// Now compute the intersection with the {@link Filter}s available for the remaining {@link WdAtmosphereType}
    	while(it.hasNext()) {
    		intersection.retainAll(getPassbands(it.next()));
    	}
    	
    	return intersection.toArray(new Filter[intersection.size()]);
    }
    
    /**
     * Get the {@link WdCoolingModelGrid} for the given {@link Filter} and {@link WdAtmosphereType}.
     * 
     * @param filter
     * 	The {@link Filter}
     * @param atm
     * 	The {@link WdAtmosphereType}
     * @return
     * 	The cooling track data.
     */
    public WdCoolingModelGrid getCoolingTracks(Filter filter, WdAtmosphereType atm) {
    	
    	if(!getPassbands(atm).contains(filter)) {
    		// The models don't support the requested filter
    		throw new IllegalArgumentException("Combination of Filter ("+filter+") and atmosphere type ("+atm+") not available"
    				+ " for "+getName()+" models!");
    	}
    	
    	// Lazy initialisation
    	if(!coolingModelsByFilter.containsKey(filter))
    	{
    		coolingModelsByFilter.put(filter, new TreeMap<WdAtmosphereType, WdCoolingModelGrid>());
    	}
    	if(!coolingModelsByFilter.get(filter).containsKey(atm))
    	{
    		coolingModelsByFilter.get(filter).put(atm, load(filter, atm));
    	}
    	
    	return coolingModelsByFilter.get(filter).get(atm);
    }
    
    /**
     * Do the cooling models for the {@link Filter} and {@link WdAtmosphereType} need to be extrapolated
     * at this age & atmosphere type?
     *
     * @param age
     * 	The WD cooling time [yr]
     * @param M mass   
     * 	WD mass [M_{solar}]
     * @param atm   
     * 	The {@link WdAtmosphereType}
     * @param filter
     * 	The {@link Filter}
     * @return		
     * 	True if the models need to be extrapolated
     */
    public boolean isExtrapolated(double age, double mass, WdAtmosphereType atm, Filter filter){
    	return getCoolingTracks(filter, atm).isExtrapolated(age, mass);
    }
    
    /**
     * Interpolate/extrapolate magnitude at arbitrary cooling time, mass and atmosphere type.
     *
     * @param age
     * 	WD cooling time [yr]
     * @param mass
     * 	WD mass [M_{solar}]
     * @param atm   
     * 	The {@link WdAtmosphereType}
     * @param filter
     * 	The {@link Filter}
     * @return
     * 	The magitude in the requested {@link Filter}
     */
    public double mag(double age, double mass, WdAtmosphereType atm, Filter filter) {
    	return getCoolingTracks(filter, atm).mag(age, mass);
    }
    
    /**
     * Interpolate/extrapolate cooling time at arbitrary magnitude, mass and atmosphere
     * type.
     *
     * @param mag
     * 	Magnitude in the requested {@link Filter}
     * @param mass 
     * 	WD mass [M_{solar}]
     * @param atm
     * 	The {@link WdAtmosphereType}
     * @param filter
     * 	The {@link Filter}
     * @return
     * 	Cooling time [yr] to the given magnitude in the given {@link Filter}, for the given WD mass, atmosphere type.
     */
    public double tcool(double mag, double mass, WdAtmosphereType atm, Filter filter) {
    	return getCoolingTracks(filter, atm).tcool(mag, mass);
    }
    
}