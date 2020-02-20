package wd.models.algoimpl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import ifmr.algo.BaseIfmr;
import ifmr.algoimpl.Ifmr_Renedo2010_Z0p01;
import ms.lifetime.algo.PreWdLifetime;
import ms.lifetime.algoimpl.PreWdLifetime_LPCODE;
import numeric.functions.MonotonicLinear;
import photometry.Filter;
import photometry.util.PhotometryUtils;
import util.ParseUtil;
import wd.models.algo.WdCoolingModelGrid;
import wd.models.algo.WdCoolingModelSet;
import wd.models.infra.WdAtmosphereType;

/**
 * This {@link WdCoolingModelSet} encapsulates the DA and DB White Dwarf cooling models
 * produced using the LPCODE evolutionary code, and presented in the papers:
 * 
 * DA:
 * 
 * "New cooling sequences for old white dwarfs"
 *  Renedo I., Althaus L. G., Miller Bertolami M. M., Romero, A. D., Córsico, A. H., Rohrmann, R. D., García-Berro, E.
 *  2010, The Astrophysical Journal, 717, 183
 *  
 *  http://evolgroup.fcaglp.unlp.edu.ar/TRACKS/tracks_cocore.html
 *  
 *  DB:
 * 
 * "New evolutionary sequences for hot H-deficient white dwarfs on the basis of a full account of progenitor evolution",
 * Althaus, L. G., Panei, J. A., Miller Bertolami, M. M., García-Berro, E., Córsico, A. H., Romero, A. D.,
 * Kepler, S. O., Rohrmann, R. D. 2009, ApJ, 704, 1605
 * 
 * http://evolgroup.fcaglp.unlp.edu.ar/TRACKS/tracks_DODB.html
 * 
 * NOTE: the Renedo et al IFMR and MS lifetimes are used here to subtract off the pre-WD lifetime of the model
 * to obtain just the WD cooling phase. This can then be used in conjunction with other stellar models and
 * IFMRs. The original Renedo et al functions should not be used for modelling: the MS models used an inapproriate
 * equation of state, and the IFMR is too sparsely constrained which introduces features in the simulated LF.
 * 
 * 
 * Specific details of the models used:
 * 
 * DA:
 *  - The tabulated ages in the colours files is in terms of the total stellar age; we need to subtract off the
 *    pre-WD lifetime for each mass, which is done using implementations of {@link BaseIfmr} and 
 *    {@link BaseMainSequenceLifetime} specialised for this purpose.
 * 
 * 
 * DB:
 *  - There are 3 WD masses (0.51465, 0.53000, 0.54198) that correspond to the same progenitor mass (1.0 M_{solar}).
 *    The lowest mass model results from a later thermal pulse and was produced to model a specific star (PG1159).
 *    The other two use two different mass loss rates. We can only keep one in order for the initial-final mass
 *    relation to be unique; I have decided to keep the 0.54198 M_{solar} model as it appears to be closer to the
 *    simulations for the remaining masses.
 *  - The tabulated ages are WD cooling times from the peak of effective temperature; it's not necessary to subtract
 *    off the pre-WD lifetime for these models.
 * 
 * TODO: implement other filters in the Filter enum.
 * 
 * NOTE that the models have been augmented with Gaia G band magnitudes using the application at
 * {@link projects.gaia.exec.AddGaiaGBandToWdModels}.
 *
 * @author nrowell
 * @version $Id$
 */
public class WdCoolingModelSet_LPCODE extends WdCoolingModelSet {
	
	/**
	 * Path to the DA models.
	 */
	private static final String daPath = "resources/wd/cooling/LPCODE/da/z0p01/colours_plus_G/";
	
	/**
	 * Path to the DB models.
	 */
	private static final String dbPath = "resources/wd/cooling/LPCODE/db/z_solar/colours_plus_G/";
	
	/**
	 * Masses of the DA models.
	 */
	private static final double[] daMasses = new double[]{0.52490, 0.57015, 0.59316, 0.60959, 0.63229, 0.65988, 0.70511, 0.76703, 0.83731, 0.87790};
	
	/**
	 * Masses of the DB models. Note that the 0.51465 & 0.53000 mass models are omitted - see note
	 * attached to class.
	 */
	private static final double[] dbMasses = new double[]{0.54198, 0.56471, 0.58419, 0.60895, 0.66409, 0.74110, 0.86969};
	
	
	/**
	 * Constructor for the {@link WdCoolingModelSet_LPCODE}.
	 */
	public WdCoolingModelSet_LPCODE() {
		
		wdAtmosphereTypes.add(WdAtmosphereType.H);
		wdAtmosphereTypes.add(WdAtmosphereType.He);
		
		List<Filter> filtersArrH = Arrays.asList(Filter.M_BOL,
//				Filter.F220W, Filter.F250W, Filter.F330W, Filter.F344N, Filter.F435W,
//				Filter.F475W, Filter.F502N, Filter.F550M, Filter.F555W,
				Filter.F606W_ACS,
//				Filter.F625W, Filter.F658N, Filter.F660N, Filter.F775W,
				Filter.F814W_ACS,
//				Filter.F850LP, Filter.F892N,
				Filter.U, Filter.B, Filter.V, Filter.R,
				Filter.I, Filter.J, Filter.H, Filter.K,
				Filter.G_NOM_DR2, Filter.BP_NOM_DR2, Filter.RP_NOM_DR2
				//, Filter.L
				);
		Set<Filter> filtersH = new HashSet<>(filtersArrH);
		
		List<Filter> filtersArrHe = Arrays.asList(Filter.M_BOL,
//				Filter.F220W, Filter.F250W, Filter.F330W, Filter.F344N, Filter.F435W,
//				Filter.F475W, Filter.F502N, Filter.F550M, Filter.F555W,
				Filter.F606W_ACS,
//				Filter.F625W, Filter.F658N, Filter.F660N, Filter.F775W,
				Filter.F814W_ACS,
//				Filter.F850LP, Filter.F892N,
				Filter.U, Filter.B, Filter.V, Filter.R,	Filter.I,
				Filter.G_NOM_DR2, Filter.BP_NOM_DR2, Filter.RP_NOM_DR2);
		Set<Filter> filtersHe = new HashSet<>(filtersArrHe);
		
		filtersByAtm.put(WdAtmosphereType.H, filtersH);
		filtersByAtm.put(WdAtmosphereType.He, filtersHe);
		
		// LPCODE models have different sets of mass values for the DA and DB types
		massGridByAtm.put(WdAtmosphereType.H, daMasses);
		massGridByAtm.put(WdAtmosphereType.He, dbMasses);
	}

    /** 
     *  {@inheritDoc}
     */
    @Override
    public String getName() {
    	return "LPCODE";
    }
    
    /** 
     *  {@inheritDoc}
     */
	@Override
	protected WdCoolingModelGrid load(Filter filter, WdAtmosphereType atm) {
		
		NavigableMap<Double, MonotonicLinear> mbolAsFnTcoolByMass = new TreeMap<>();
		
		// The DA models provide magnitude as a function of the total stellar lifetime, which
		// necessitates the use of specialised {@link BaseIfmr} and {@link BaseMainSequenceLifetime}
		// types in order to subtract off the pre-WD lifetime.
		BaseIfmr ifmr = null;
		PreWdLifetime preWdLifetimes = null;
		if(atm==WdAtmosphereType.H) {
			ifmr = new Ifmr_Renedo2010_Z0p01();
			preWdLifetimes = new PreWdLifetime_LPCODE();
		}
		else if(atm==WdAtmosphereType.He) {
			// The DB models provide WD cooling times directly; no need to subtract off the pre-WD lifetime.
		}
		
		// Get the directory containing the model set
		String filepath = null;
		if(atm==WdAtmosphereType.H) {
			filepath = daPath;
		}
		else if(atm==WdAtmosphereType.He) {
			filepath = dbPath;
		}
		
		// Indices of relevant columns in data file
        int timeCol = 3;
        int bandCol = getFilterColumn(filter, atm);

		List<String> comments = new LinkedList<>();
		comments.add("#");
		comments.add("Teff");
		
		for(double mass : getMassGridPoints(atm)) {
			
			double preWdLifetime = 0.0;
			if(atm==WdAtmosphereType.H) {
				// The DA models require the pre-WD lifetime to be subtracted off
				double msMass = ifmr.getMi(mass);
				// Z and Y values used in Renedo et al models.
				final double z = 0.001;
				final double y = 0.232;
				preWdLifetime = preWdLifetimes.getPreWdLifetime(z, y, msMass)[0];
			}
			
			// Name of the corresponding 'colours' file
			String filename = null;
			if(atm==WdAtmosphereType.H) {
				filename = String.format("cox_%06.0f.dat", mass*100000);
			}
			else if(atm==WdAtmosphereType.He) {
				filename = String.format("col_%7.5f", mass);
			}
	        
	        // Open reader on file containing WD cooling model
	        InputStream is = getClass().getClassLoader().getResourceAsStream(filepath+filename);
	        
	        double[][] colourData = null;
			
			try(BufferedReader in = new BufferedReader(new InputStreamReader(is))) {
				colourData = ParseUtil.parseFile(in, ParseUtil.whitespaceDelim, comments);
			}
			catch (IOException e) {
				logger.warning("Could not load the LPCODE "+atm+" model files!");
				return null;
			}
	        
	        double[] coolingTimeArray = new double[colourData[0].length];
	        double[] magnitudeArray = new double[colourData[0].length];
	        for (int p = 0; p < colourData[0].length; p++) {
	        	
	        	// WD cooling time from total stellar age minus MS lifetime; in [yr]
	        	coolingTimeArray[p] = (colourData[timeCol][p]*1E9) - preWdLifetime;
	        	
	        	magnitudeArray[p] = colourData[bandCol][p];
	        	if(filter==Filter.M_BOL) {
	        		// We have Log(L/Lo) rather than magnitude
	        		magnitudeArray[p] = PhotometryUtils.logLL0toMbol(magnitudeArray[p]);
	        	}
	        }
	        
	        mbolAsFnTcoolByMass.put(mass, new MonotonicLinear(coolingTimeArray, magnitudeArray));
		}
		
		return new WdCoolingModelGrid(filter, atm, mbolAsFnTcoolByMass);
	}

    /**
     * Get the index of the column in the data files that contains fluxes in the given {@link Filter}.
     * The input files are the 'colours' files contained in the Renedo et al. dataset, i.e. those with
     * filenames <code>cox_059316.dat</code> etc.
     * 
     * @param band
     * 	The {@link Filter} to look up
     * @return
     * 	Index (first column is zero) of the column containing fluxes in the given {@link Filter}.
     */
    private static int getFilterColumn(Filter band, WdAtmosphereType atm) {
    	switch(atm) {
	    	case H: return getFilterColumnDA(band);
	    	case He: return getFilterColumnDB(band);
	    	default:
	    		throw new IllegalArgumentException(WdCoolingModelSet_LPCODE.class.getName()+
						" don't support atmosphere type "+atm);
    	}
    }
    
    /**
     * Get the {@link Filter} to column number mapping for DA models.
     * @param band
     * 	The {@link Filter}
     * @return
     * 	The index of the column containing magnitude in the desired {@link Filter}.
     */
    private static int getFilterColumnDA(Filter band) {
    	
    	switch(band) {
	    	case M_BOL: return 2;
//	    	case F220W: return 5;
//	    	case F250W: return 6;
//	    	case F330W: return 7;
//	    	case F344N: return 8;
//	    	case F435W: return 9;
//	    	case F475W: return 10;
//	    	case F502N: return 11;
//	    	case F550M: return 12;
//	    	case F555W: return 13;
	    	case F606W_ACS:	return 14;
//	    	case F625W: return 15;
//	    	case F658N: return 16;
//	    	case F660N: return 17;
//	    	case F775W: return 18;
	    	case F814W_ACS:	return 19;
//	    	case F850LP: return 20;
//	    	case F892N: return 21;
	    	// Col 22. is bolometric correction
	    	case U:    	return  23;
	    	case B:		return 24;
	    	case V:		return 25;
	    	case R:		return 26;
	    	case I:		return 27;
	    	case J:		return 28;
	    	case H:		return 29;
	    	case K:		return 30;
//	    	case L:		return 31;
	    	case G_NOM_DR2:		return 32;
	    	case BP_NOM_DR2:	return 33;
	    	case RP_NOM_DR2:	return 34;
			default:
				throw new IllegalArgumentException(WdCoolingModelSet_LPCODE.class.getName()+
						" don't support filter "+band+" for atmosphere type "+WdAtmosphereType.H);
    	}
    }
    
    /**
     * Get the {@link Filter} to column number mapping for DB models.
     * @param band
     * 	The {@link Filter}
     * @return
     * 	The index of the column containing magnitude in the desired {@link Filter}.
     */
    private static int getFilterColumnDB(Filter band) {
    	
    	switch(band) {
	    	case M_BOL: return 2;
//	    	case F220W: return 5;
//	    	case F250W: return 6;
//	    	case F330W: return 7;
//	    	case F344N: return 8;
//	    	case F435W: return 9;
//	    	case F475W: return 10;
//	    	case F502N: return 11;
//	    	case F550M: return 12;
//	    	case F555W: return 13;
	    	case F606W_ACS:	return 14;
//	    	case F625W: return 15;
//	    	case F658N: return 16;
//	    	case F660N: return 17;
//	    	case F775W: return 18;
	    	case F814W_ACS:	return 19;
//	    	case F850LP: return 20;
//	    	case F892N: return 21;
	    	// Col 22. is bolometric correction
	    	case U:    	return  23;
	    	case B:		return 24;
	    	case V:		return 25;
	    	case R:		return 26;
	    	case I:		return 27;
	    	case G_NOM_DR2:		return 28;
	    	case BP_NOM_DR2:	return 29;
	    	case RP_NOM_DR2:	return 30;
			default:
				throw new IllegalArgumentException(WdCoolingModelSet_LPCODE.class.getName()+
						" don't support filter "+band+" for atmosphere type "+WdAtmosphereType.He);
    	}
    }
    
}