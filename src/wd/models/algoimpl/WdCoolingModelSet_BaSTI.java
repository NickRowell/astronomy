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
import photometry.util.PhotometryUtils;
import util.ParseUtil;
import wd.models.algo.WdCoolingModelGrid;
import wd.models.algo.WdCoolingModelSet;
import wd.models.infra.AtmosphereParameter;
import wd.models.infra.WdAtmosphereType;

/**
 * Class representing BaSTI WD cooling models. See:
 * 
 * "A LARGE STELLAR EVOLUTION DATABASE FOR POPULATION SYNTHESIS STUDIES. VI. WHITE DWARF
 *  COOLING SEQUENCES"
 *   - Salaris, Cassisi, Pietrinferni, Kowalski & Isern (2010, ApJ 716, 1241)
 *   
 *   ...and obtained from the BaSTI database at:
 *   
 * http://basti.oa-teramo.inaf.it/index.html
 * 
 */
public class WdCoolingModelSet_BaSTI extends WdCoolingModelSet {
	
	/**
	 * String containing the name of this {@link WdCoolingModelSet_BaSTI}, identifying
	 * whether this instance represents the BaSTI WD cooling models that include the
	 * effects of phase seperation and sedimentation.
	 */
	String name;
	
	/**
	 * Boolean indicating whether this instance uses the WD cooling models that models that include
	 * the effects of phase separation and sedimentation upon crystallisation of the core.
	 */
	boolean includingPhaseSeparation;
	
	/**
	 * Constructor for the {@link WdCoolingModelSet_BaSTI}.
	 */
	public WdCoolingModelSet_BaSTI(boolean includingPhaseSeparation) {
		
		this.includingPhaseSeparation = includingPhaseSeparation;
		name = includingPhaseSeparation ? "BaSTI, inc. phase separation" : "BaSTI, no phase separation";
		
		wdAtmosphereTypes.add(WdAtmosphereType.H);
		wdAtmosphereTypes.add(WdAtmosphereType.He);
		
		Set<Quantity<?>> quantities = new HashSet<>();
		quantities.add(AtmosphereParameter.TEFF);
		quantities.add(Filter.M_BOL);
		quantities.add(Filter.U);
		quantities.add(Filter.B);
		quantities.add(Filter.V);
		quantities.add(Filter.R);
		quantities.add(Filter.I);
		quantities.add(Filter.J);
		quantities.add(Filter.H);
		quantities.add(Filter.K);
		// HST bands
		quantities.add(Filter.F435W_ACS);
		quantities.add(Filter.F475_ACS);
		quantities.add(Filter.F502N_ACS);
		quantities.add(Filter.F550M_ACS);
		quantities.add(Filter.F555W_ACS);
		quantities.add(Filter.F606W_ACS);
		quantities.add(Filter.F625W_ACS);
		quantities.add(Filter.F658N_ACS);
		quantities.add(Filter.F660N_ACS);
		quantities.add(Filter.F775W_ACS);
		quantities.add(Filter.F814W_ACS);
		quantities.add(Filter.F850LP_ACS);
		quantities.add(Filter.F892N_ACS);
		// SDSS bands
		quantities.add(Filter.SDSS_U);
		quantities.add(Filter.SDSS_G);
		quantities.add(Filter.SDSS_R);
		quantities.add(Filter.SDSS_I);
		quantities.add(Filter.SDSS_Z);
		
		// BaSTI models provide the same filters for each atmosphere type
		quantitiesByAtm.put(WdAtmosphereType.H, quantities);
		quantitiesByAtm.put(WdAtmosphereType.He, quantities);
		
		double[] masses = new double[]{0.54, 0.55, 0.61, 0.68, 0.77, 0.87, 1.0, 1.1, 1.2};
		// BaSTI models use the same set of mass gridpoints for each atmosphere type
		massGridByAtm.put(WdAtmosphereType.H, masses);
		massGridByAtm.put(WdAtmosphereType.He, masses);
	}

    /** 
     *  {@inheritDoc}
     */
    @Override
    public String getName() {
    	return name;
    }
    
    /** 
     *  {@inheritDoc}
     */
	@Override
	protected WdCoolingModelGrid load(Quantity<?> quantity, WdAtmosphereType atm) {
		
		NavigableMap<Double, MonotonicLinear> quantityAsFnTcoolByMass = new TreeMap<>();
		
		for(double mass : getMassGridPoints(atm)) {
			
	        // Resolve parameters into name of corresponding file
	        String name = resolveFileName(mass, atm, includingPhaseSeparation, quantity);
	        
	    	// log(t) is in the first column always
	    	int timeCol = 0;
	    	int bandCol = getColumn(quantity);
	        
	        // Open reader on file containing WD cooling model
	        InputStream is = (this).getClass().getClassLoader().getResourceAsStream("resources/wd/cooling/BaSTI/"+name);
	        
	        try(BufferedReader in = new BufferedReader(new InputStreamReader(is))) {
	        	List<String> comments = new LinkedList<>();
	        	comments.add("#");
	        	double[][] data = ParseUtil.parseFile(in, ParseUtil.whitespaceDelim, comments);
	        	int nPoints = data[0].length;
	        	double[] coolingTimeArray = new double[nPoints];
		        double[] quantityArray = new double[nPoints];
		        for (int p = 0; p < nPoints; p++) {
		        	coolingTimeArray[p] = Math.pow(10, data[timeCol][p]);
		        	quantityArray[p] = data[bandCol][p];
		        	if(quantity==Filter.M_BOL) {
		        		quantityArray[p] = PhotometryUtils.logLL0toMbol(data[bandCol][p]);
		        	}
		        	if(quantity==AtmosphereParameter.TEFF) {
		        		quantityArray[p] = Math.pow(10, data[bandCol][p]);
		        	}
		        }
		        
		        try {
		        	quantityAsFnTcoolByMass.put(mass, new MonotonicLinear(coolingTimeArray, quantityArray));
		        }
		        catch(IllegalArgumentException e) {
		        	logger.log(Level.SEVERE, "Unable to interpolate BaSTI WD cooling track loaded from file " + name, e);
					e.printStackTrace();
		        }
	        }
	        catch (IOException e) {
	        	logger.log(Level.SEVERE, "Unable to load BaSTI WD cooling model file "
	        			+ "from InputStream "+is.toString(), e);
				e.printStackTrace();
	        }
		}
		
		return new WdCoolingModelGrid(quantity, atm, quantityAsFnTcoolByMass);
	}
	
    /**
     * Resolve mass/atmosphere/phase separation tuple to the corresponding filename.
     * 
     * @param mass
     * 		The stellar mass
     * @param type
     * 		The AtmosphereType
     * @param phaseSep
     * 		Whether to include the effects of phase seperation
     * @param q
     * 		The {@link Quantity} for the desired cooling models
     * @return 
     * 		The name of the file containing the corresponding WD model.
     */
    private static String resolveFileName(double mass, WdAtmosphereType type, boolean phaseSep, Quantity<?> q)
    {
    	// Resolve atmosphere type
        String spectral_type = null;
        switch(type) {
	        case H: spectral_type = "DA"; break;
	        case He: spectral_type = "DB"; break;
        }

        // Resolve directory and file extension
        String path = null, extension = null;
        
        if(q instanceof AtmosphereParameter) {
        	AtmosphereParameter ap = (AtmosphereParameter)q;
    		switch(ap) {
	    		// These correspond to the files in the UBVRIJHK directory
		    	case TEFF:
		    		path = "UBVRIJHK/";
		    		extension = "Jhn";
		    		break;
				default:
					throw new IllegalArgumentException(WdCoolingModelSet_BaSTI.class.getName()+" don't support atmosphere parameter "+ap);
    		}
        }
        
        else if(q instanceof Filter) {
        	
        	Filter band = (Filter)q;
        
	        switch(band) {
				// These correspond to the files in the UBVRIJHK directory
		    	case M_BOL:
		    	case U:
		    	case B:
		    	case V:
		    	case R:
		    	case I:
		    	case J:
		    	case H:
		    	case K:
		    		path = "UBVRIJHK/";
		    		extension = "Jhn";
		    		break;
		    	// These correspond to the files in the HST_ACS directory
		    	case F435W_ACS:
		    	case F475_ACS:
		    	case F502N_ACS:
		    	case F550M_ACS:
		    	case F555W_ACS:
		    	case F606W_ACS:
		    	case F625W_ACS:
		    	case F658N_ACS:
		    	case F660N_ACS:
		    	case F775W_ACS:
		    	case F814W_ACS:
		    	case F850LP_ACS:
		    	case F892N_ACS:
		    		path = "HST_ACS/";
		    		extension = "acs";
		    		break;
		    	// These correspond to the files in the SDSS directory
		    	case SDSS_U:
		    	case SDSS_G:
		    	case SDSS_R:
		    	case SDSS_I:
		    	case SDSS_Z:
		    		path = "SDSS/";
		    		extension = "sdss";
		    		break;
		    		
				default:
					throw new IllegalArgumentException(WdCoolingModelSet_BaSTI.class.getName()+" don't support filter "+band);
			}
        }
        
        return String.format("%sCOOL%03.0fBaSTIfinale%s%s.%s", path, mass*100, spectral_type, phaseSep ? "sep" : "nosep", extension);
    }
    
    /**
     * Get the index of the column in the data files that contains fluxes in the given {@link Filter}.
     * The data is split into files containing HST colours and standard colours.
     * 
     * @param q
     * 	The {@link Quantity} to look up
     * @return
     * 	Index (first column is zero) of the column containing fluxes in the given {@link Filter}.
     */
    private static int getColumn(Quantity<?> q) {
    	
    	if(q instanceof Filter) {
    	
    		Filter band = (Filter)q;
    	
	    	switch(band) {
	    	
	    		// These correspond to the files in the UBVRIJHK directory
		    	case M_BOL: return 3;
		    	case U:    	return 4;
		    	case B:		return 5;
		    	case V:		return 6;
		    	case R:		return 7;
		    	case I:		return 8;
		    	case J:		return 9;
		    	case H:		return 10;
		    	case K:		return 11;
		    	// These correspond to the files in the HST_ACS directory
		    	case F435W_ACS:  return 4;
		    	case F475_ACS:   return 5;
		    	case F502N_ACS:  return 6;
		    	case F550M_ACS:  return 7;
		    	case F555W_ACS:  return 8;
		    	case F606W_ACS:  return 9;
		    	case F625W_ACS:  return 10;
		    	case F658N_ACS:  return 11;
		    	case F660N_ACS:  return 12;
		    	case F775W_ACS:  return 13;
		    	case F814W_ACS:  return 14;
		    	case F850LP_ACS: return 15;
		    	case F892N_ACS:  return 16;
		    	// These correspond to the files in the SDSS directory
		    	case SDSS_U:  return 4;
		    	case SDSS_G:  return 5;
		    	case SDSS_R:  return 6;
		    	case SDSS_I:  return 7;
		    	case SDSS_Z:  return 8;
		    	
				default:
					throw new IllegalArgumentException(WdCoolingModelSet_BaSTI.class.getName()+" don't support filter "+band);
	    	}
    	}
    	else if(q instanceof AtmosphereParameter) {
    		
    		AtmosphereParameter ap = (AtmosphereParameter)q;
    		switch(ap) {
	    		// These correspond to the files in the UBVRIJHK directory
		    	case TEFF: return 2;
				default:
					throw new IllegalArgumentException(WdCoolingModelSet_BaSTI.class.getName()+" don't support atmosphere parameter "+ap);
    		}
    	}
    	else {
    		throw new IllegalArgumentException(WdCoolingModelSet_BaSTI.class.getName()+" don't support quantity "+q);
    	}
    }
}