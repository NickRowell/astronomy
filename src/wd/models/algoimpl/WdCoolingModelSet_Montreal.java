package wd.models.algoimpl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.NavigableMap;
import java.util.Set;
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

public class WdCoolingModelSet_Montreal extends WdCoolingModelSet {
	
	/**
	 * Constructor for the {@link WdCoolingModelSet_Montreal}.
	 */
	public WdCoolingModelSet_Montreal() {
		
		wdAtmosphereTypes.add(WdAtmosphereType.H);
		wdAtmosphereTypes.add(WdAtmosphereType.He);

		Set<Quantity<?>> quantities = new HashSet<>();
		quantities.add(AtmosphereParameter.TEFF);
		quantities.add(AtmosphereParameter.LOGG);
		quantities.add(Filter.M_BOL);
		quantities.add(Filter.U);
		quantities.add(Filter.B);
		quantities.add(Filter.V);
		quantities.add(Filter.R);
		quantities.add(Filter.I);
		quantities.add(Filter.SDSS_U);
		quantities.add(Filter.SDSS_G);
		quantities.add(Filter.SDSS_R);
		quantities.add(Filter.SDSS_I);
		quantities.add(Filter.SDSS_Z);
		quantities.add(Filter.F606W_ACS);
		quantities.add(Filter.F814W_ACS);
		// Gaia magnitudes computed by Pierre Bergeron upon request, using nominal Gaia bands
		quantities.add(Filter.G_NOM_DR2);
		quantities.add(Filter.BP_NOM_DR2);
		quantities.add(Filter.RP_NOM_DR2);
		quantities.add(Filter.G_REV_DR2);
		quantities.add(Filter.BP_REV_DR2);
		quantities.add(Filter.RP_REV_DR2);
		// Montreal models provide the same filters for each atmosphere type
		quantitiesByAtm.put(WdAtmosphereType.H, quantities);
		quantitiesByAtm.put(WdAtmosphereType.He, quantities);
		
		double[] masses = new double[]{0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0, 1.2};
		// Montreal models use the same set of mass gridpoints for each atmosphere type
		massGridByAtm.put(WdAtmosphereType.H, masses);
		massGridByAtm.put(WdAtmosphereType.He, masses);
	}

    /** 
     *  {@inheritDoc}
     */
    @Override
    public String getName() {
    	return "Montreal";
    }
    
    /** 
     *  {@inheritDoc}
     */
	@Override
	protected WdCoolingModelGrid load(Quantity<?> filter, WdAtmosphereType atm) {
		
		NavigableMap<Double, MonotonicLinear> mbolAsFnTcoolByMass = new TreeMap<>();
		
		for(double mass : getMassGridPoints(atm)) {

	        // Resolve parameters into name of corresponding file, and get column numbers
	    	int[] cols = new int[2];
	        String name = resolveFileName(mass, atm, filter, cols);
	        int timeCol = cols[0];
	        int bandCol = cols[1];
	        
	        // Open reader on file containing WD cooling model
	        InputStream is = getClass().getClassLoader().getResourceAsStream("resources/wd/cooling/Montreal/"+name);
	        
	        try(BufferedReader in = new BufferedReader(new InputStreamReader(is))) {
	        	List<String> comments = new LinkedList<>();
	        	comments.add("#");
	        	comments.add("?");
	        	double[][] data = ParseUtil.parseFile(in, ParseUtil.whitespaceDelim, comments);
	        	int nPoints = data[0].length;
	        	double[] coolingTimeArray = new double[nPoints];
		        double[] quantityArray = new double[nPoints];
		        for (int p = 0; p < nPoints; p++) 
		        {
		        	coolingTimeArray[p] = data[timeCol][nPoints - (p+1)];
		        	quantityArray[p] = data[bandCol][nPoints - (p+1)];
		        }
		        try {
		        	mbolAsFnTcoolByMass.put(mass, new MonotonicLinear(coolingTimeArray, quantityArray));
		        }
		        catch(RuntimeException e) {
		        	logger.log(Level.SEVERE, "Unable to load Montreal WD cooling model file "
		        			+ "from "+name.toString(), e);
		        }
		        
	        }
	        catch (IOException e) {
	        	logger.log(Level.SEVERE, "Unable to load Montreal WD cooling model file "
	        			+ "from "+name.toString(), e);
				e.printStackTrace();
	        }
		}
		
		return new WdCoolingModelGrid(filter, atm, mbolAsFnTcoolByMass);
	}
	
    /**
     * Resolve WD mass and {@link WdAtmosphereType} to the corresponding filename. Also get the
     * numbers of the columns containing the cooling time and magnitude in the requested {@link Filter}.
     * 
     * @param mass
     * 		The stellar mass
     * @param atm
     * 		The AtmosphereType
     * @param q
     * 		The {@link Quantity}.
     * @param cols
     * 		Two-element int array; on exit, the elements will contain the indices of the columns that
     * contain the cooling time [0] and desired magnitude [1]. Indexing is zero-based, i.e. first column
     * has index 0.
     * @return 
     * 		The name of the file containing the corresponding WD model.
     */
    private static String resolveFileName(double mass, WdAtmosphereType atm, Quantity<?> q, int[] cols) {
    	
    	String directory = "";
    	// Zero-based indexing of the columns
    	int timeCol = 0;
    	int bandCol = 0;
    	
    	if(q instanceof Filter) {
    	
    		Filter band = (Filter)q;
    		
	    	switch(band) {
	    	case M_BOL:
	    		timeCol = 26;
	    		bandCol = 2;
	    		directory = "standard";
	    		break;
	    	case U:
	    		timeCol = 26;
	    		bandCol = 4;
	    		directory = "standard";
	    		break;
	    	case B:
	    		timeCol = 26;
				bandCol = 5;
				directory = "standard";
				break;
	    	case V:
	    		timeCol = 26;
				bandCol = 6;
				directory = "standard";
				break;
	    	case R:
	    		timeCol = 26;
				bandCol = 7;
				directory = "standard";
				break;
	    	case I:
	    		timeCol = 26;
				bandCol = 8;
				directory = "standard";
				break;
	    	case F606W_ACS:
	    		timeCol = 3;
				bandCol = 7;
				directory = "hst";
				break;
	    	case F814W_ACS:
	    		timeCol = 3;
				bandCol = 11;
				directory = "hst";
				break;
	    	case SDSS_U:
	    		timeCol = 23;
				bandCol = 12;
				// XXX: Hardcode to use the thin H layer models, if H atmosphere is requested
				directory = "gaia" + (atm==WdAtmosphereType.H ? "/thin" : "");
				break;
	    	case SDSS_G:
	    		timeCol = 23;
				bandCol = 13;
				// XXX: Hardcode to use the thin H layer models, if H atmosphere is requested
				directory = "gaia" + (atm==WdAtmosphereType.H ? "/thin" : "");
				break;
	    	case SDSS_R:
	    		timeCol = 23;
				bandCol = 14;
				// XXX: Hardcode to use the thin H layer models, if H atmosphere is requested
				directory = "gaia" + (atm==WdAtmosphereType.H ? "/thin" : "");
				break;
	    	case SDSS_I:
	    		timeCol = 23;
				bandCol = 15;
				// XXX: Hardcode to use the thin H layer models, if H atmosphere is requested
				directory = "gaia" + (atm==WdAtmosphereType.H ? "/thin" : "");
				break;
	    	case SDSS_Z:
	    		timeCol = 23;
				bandCol = 16;
				// XXX: Hardcode to use the thin H layer models, if H atmosphere is requested
				directory = "gaia" + (atm==WdAtmosphereType.H ? "/thin" : "");
				break;
	    	case G_NOM_DR2:
	    		timeCol = 23;
				bandCol = 17;
				// XXX: Hardcode to use the thin H layer models, if H atmosphere is requested
				directory = "gaia" + (atm==WdAtmosphereType.H ? "/thin" : "");
				break;
	    	case BP_NOM_DR2:
	    		timeCol = 23;
				bandCol = 18;
				// XXX: Hardcode to use the thin H layer models, if H atmosphere is requested
				directory = "gaia" + (atm==WdAtmosphereType.H ? "/thin" : "");
				break;
	    	case RP_NOM_DR2:
	    		timeCol = 23;
				bandCol = 19;
				// XXX: Hardcode to use the thin H layer models, if H atmosphere is requested
				directory = "gaia" + (atm==WdAtmosphereType.H ? "/thin" : "");
				break;
	    	case G_REV_DR2:
	    		timeCol = 23;
				bandCol = 20;
				// XXX: Hardcode to use the thin H layer models, if H atmosphere is requested
				directory = "gaia" + (atm==WdAtmosphereType.H ? "/thin" : "");
				break;
	    	case BP_REV_DR2:
	    		timeCol = 23;
				bandCol = 21;
				// XXX: Hardcode to use the thin H layer models, if H atmosphere is requested
				directory = "gaia" + (atm==WdAtmosphereType.H ? "/thin" : "");
				break;
	    	case RP_REV_DR2:
	    		timeCol = 23;
				bandCol = 22;
				// XXX: Hardcode to use the thin H layer models, if H atmosphere is requested
				directory = "gaia" + (atm==WdAtmosphereType.H ? "/thin" : "");
				break;
			default:
				throw new IllegalArgumentException(WdCoolingModelSet_Montreal.class.getName()+" don't support filter "+band);
	    	}
    	}
    	
    	if(q instanceof AtmosphereParameter) {
    		
    		AtmosphereParameter ap = (AtmosphereParameter)q;
    		
    		switch(ap) {
	    	case TEFF:
	    		timeCol = 26;
	    		bandCol = 0;
	    		directory = "standard";
	    		break;
	    	case LOGG:
	    		timeCol = 26;
	    		bandCol = 1;
	    		directory = "standard";
	    		break;
			default:
				throw new IllegalArgumentException(WdCoolingModelSet_Montreal.class.getName()+" don't support atmosphere parameter "+ap);
	    	}
    		
    	}
    	
    	cols[0] = timeCol;
    	cols[1] = bandCol;
    	String filename = String.format(atm + "_%.1f", mass);
    	
        return directory + "/" + filename;
    }
}