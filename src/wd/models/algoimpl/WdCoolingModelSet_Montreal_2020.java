package wd.models.algoimpl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.logging.Level;

import infra.Quantity;
import numeric.functions.MonotonicLinear;
import photometry.Filter;
import util.ParseUtil;
import wd.models.algo.WdCoolingModelGrid;
import wd.models.algo.WdCoolingModelSet;
import wd.models.infra.AtmosphereParameter;
import wd.models.infra.WdAtmosphereType;

/**
 * This class provides an implementation of the {@link WdCoolingModelSet} that uses the latest
 * tables of WD synthetic colours from the Montreal group, downloaded from:
 * 
 * http://www.astro.umontreal.ca/~bergeron/CoolingModels/
 * 
 * Notes:
 * 
 *  - For most (all?) cooling tracks there are a few points at the start (hot end) that have the same age of
 *  zero. This conflicts with the algorithm used to interpolate the cooling times, which must be invertible.
 *  Therefore when loading the data points we ignore all points of zero age apart from the last.
 * 
 * @author nrowell
 *
 */
public class WdCoolingModelSet_Montreal_2020 extends WdCoolingModelSet {

	/**
	 * Column in the data files that contains the cooling time (zero-based indexing).
	 */
    int timeCol = 42;
    
    /**
     * Maps the {@link Quantity}s in the data files to the column number (zero-based indexing).
     */
    Map<Quantity<?>, Integer> colNumsByQuantity = new HashMap<>();
    
	/**
	 * Constructor for the {@link WdCoolingModelSet_Montreal_2020}.
	 */
	public WdCoolingModelSet_Montreal_2020() {
		
		wdAtmosphereTypes.add(WdAtmosphereType.H);
		wdAtmosphereTypes.add(WdAtmosphereType.He);
		
		colNumsByQuantity.put(AtmosphereParameter.TEFF, 0);
		colNumsByQuantity.put(AtmosphereParameter.LOGG, 1);
		colNumsByQuantity.put(Filter.M_BOL, 2);
		colNumsByQuantity.put(Filter.U, 4);
		colNumsByQuantity.put(Filter.B, 5);
		colNumsByQuantity.put(Filter.V, 6);
		colNumsByQuantity.put(Filter.R, 7);
		colNumsByQuantity.put(Filter.I, 8);
		colNumsByQuantity.put(Filter.SDSS_U, 24);
		colNumsByQuantity.put(Filter.SDSS_G, 25);
		colNumsByQuantity.put(Filter.SDSS_R, 26);
		colNumsByQuantity.put(Filter.SDSS_I, 27);
		colNumsByQuantity.put(Filter.SDSS_Z, 28);
		colNumsByQuantity.put(Filter.G_REV_DR2, 34);
		colNumsByQuantity.put(Filter.BP_REV_DR2, 35);
		colNumsByQuantity.put(Filter.RP_REV_DR2, 36);
		colNumsByQuantity.put(Filter.G_DR3, 37);
		colNumsByQuantity.put(Filter.BP_DR3, 38);
		colNumsByQuantity.put(Filter.RP_DR3, 39);
		
		// Montreal models provide the same filters for each atmosphere type
		quantitiesByAtm.put(WdAtmosphereType.H, colNumsByQuantity.keySet());
		quantitiesByAtm.put(WdAtmosphereType.He, colNumsByQuantity.keySet());
		
		double[] masses = new double[]{0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0, 1.1, 1.2, 1.3};
		// Montreal models use the same set of mass gridpoints for each atmosphere type
		massGridByAtm.put(WdAtmosphereType.H, masses);
		massGridByAtm.put(WdAtmosphereType.He, masses);
	}

    /** 
     *  {@inheritDoc}
     */
    @Override
    public String getName() {
    	return "Montreal (new 2020)";
    }
    
    /** 
     *  {@inheritDoc}
     */
	@Override
	protected WdCoolingModelGrid load(Quantity<?> quantity, WdAtmosphereType atm) {
		
		// Superclass has verified that the given {@link Quantity} is included in the models.
		
		NavigableMap<Double, MonotonicLinear> quantityAsFnTcoolByMass = new TreeMap<>();
		
		for(double mass : getMassGridPoints(atm)) {

	    	String filename = String.format(atm + "_%.1f", mass);
	        int bandCol = colNumsByQuantity.get(quantity);
	        
	        // Open reader on file containing WD cooling model
	        InputStream is = getClass().getClassLoader().getResourceAsStream("resources/wd/cooling/Montreal/new/"+filename);
	        
	        try(BufferedReader in = new BufferedReader(new InputStreamReader(is))) {
	        	List<String> comments = new LinkedList<>();
	        	comments.add("#");
	        	comments.add("?");
	        	double[][] data = ParseUtil.parseFile(in, ParseUtil.whitespaceDelim, comments);
	        	
	        	// Determine how many points there are with zero age.
	        	int zeroAgePoints = 0;
	        	for (int p = 0; p < data[0].length; p++) {
	        		if(data[timeCol][p] == 0.0) {
	        			zeroAgePoints++;
	        		}
		        }
	        	
	        	// Determine number of points to read (skipping all but the first point with zero age).
	        	int nPointsToRead = data[0].length - (zeroAgePoints-1);
	        	double[] coolingTimeArray = new double[nPointsToRead];
		        double[] quantityArray = new double[nPointsToRead];
		        
		        for (int p = 0; p < nPointsToRead; p++) {
		        	// Must reverse so the coolingTimeArray is in ascending order
		        	coolingTimeArray[nPointsToRead - (p+1)] = data[timeCol][p];
		        	quantityArray[nPointsToRead - (p+1)] = data[bandCol][p];
		        }
		        
		        try {
		        	quantityAsFnTcoolByMass.put(mass, new MonotonicLinear(coolingTimeArray, quantityArray));
		        }
		        catch(RuntimeException e) {
		        	logger.log(Level.SEVERE, "Unable to load Montreal WD cooling model file "
		        			+ "from "+filename.toString(), e);
		        }
		        
	        }
	        catch (IOException e) {
	        	logger.log(Level.SEVERE, "Unable to load Montreal WD cooling model file "
	        			+ "from "+filename.toString(), e);
				e.printStackTrace();
	        }
		}
		
		return new WdCoolingModelGrid(quantity, atm, quantityAsFnTcoolByMass);
	}
}