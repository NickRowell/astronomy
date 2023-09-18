package wd.models.algoimpl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.NavigableMap;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;

import constants.Solar;
import infra.Quantity;
import numeric.functions.MonotonicLinear;
import photometry.Filter;
import wd.models.algo.WdCoolingModelGrid;
import wd.models.algo.WdCoolingModelSet;
import wd.models.infra.AtmosphereParameter;
import wd.models.infra.WdAtmosphereType;

/**
 * This class provides an implementation of the {@link WdCoolingModelSet} that uses the latest
 * evolutionary models from the Montreal group, downloaded from:
 * 
 * http://www.astro.umontreal.ca/~bergeron/CoolingModels/
 * 
 * Notes:
 * 
 *  - These are the 'evolutionary sequences', which contain the same values of bolometric magnitude,
 *  effective temperature and surface gravity as the 'synthetic colours' tables, but are missing the
 *  magnitudes in different passbands. The main benefit of using these (when colour info is not required)
 *  is that they provide twice as many masses, each track has more densely logged points and is extended
 *  to fainter magnitudes.
 * 
 * @author nrowell
 *
 */
public class WdCoolingModelSet_Montreal_2020_Evolutionary extends WdCoolingModelSet {
    
	/**
	 * Constructor for the {@link WdCoolingModelSet_Montreal_2020_Evolutionary}.
	 */
	public WdCoolingModelSet_Montreal_2020_Evolutionary() {
		
		wdAtmosphereTypes.add(WdAtmosphereType.H);
		wdAtmosphereTypes.add(WdAtmosphereType.He);
		
		Set<Quantity<?>> quantities = new HashSet<>();
		
		quantities.add(AtmosphereParameter.TEFF);
		quantities.add(AtmosphereParameter.LOGG);
		quantities.add(Filter.M_BOL);
		
		// Montreal models provide the same filters for each atmosphere type
		quantitiesByAtm.put(WdAtmosphereType.H, quantities);
		quantitiesByAtm.put(WdAtmosphereType.He, quantities);
		
		double[] masses = new double[]{0.2, 0.25, 0.3, 0.35, 0.4, 0.45, 0.5, 0.55, 0.6, 0.65,
				 0.7, 0.75, 0.8, 0.85, 0.9, 0.95, 1.0, 1.05, 1.1, 1.15, 1.2, 1.25, 1.3};
		
		// Montreal models use the same set of mass gridpoints for each atmosphere type
		massGridByAtm.put(WdAtmosphereType.H, masses);
		massGridByAtm.put(WdAtmosphereType.He, masses);
	}

    /** 
     *  {@inheritDoc}
     */
    @Override
    public String getName() {
    	return "Montreal evolutionary (new 2020)";
    }
    
    /** 
     *  {@inheritDoc}
     */
	@Override
	protected WdCoolingModelGrid load(Quantity<?> quantity, WdAtmosphereType atm) {
		
		// Superclass has verified that the given {@link Quantity} is included in the models.
		
		NavigableMap<Double, MonotonicLinear> quantityAsFnTcoolByMass = new TreeMap<>();
		
		for(double mass : getMassGridPoints(atm)) {

	    	String filename = String.format("seq_%03d_%s.txt", (int)Math.rint(mass*100), atm == WdAtmosphereType.H ? "thick" : "thin");
	    	
	        // Open reader on file containing WD cooling model
	        InputStream is = getClass().getClassLoader().getResourceAsStream("resources/wd/cooling/Montreal/new/evolutionary/"+filename);
	        
	        try(BufferedReader in = new BufferedReader(new InputStreamReader(is))) {
	        	
	        	// Purge 5 header lines
	        	for(int i=0; i<5; i++) {
	        		in.readLine();
	        	}
	        	
	        	// Unsure in advance how many lines to read; use a List
	        	List<double[]> data = new LinkedList<>();
	        	
	        	String line1;
	        	while ((line1 = in.readLine()) != null) {
	        	
	        		// Each record is split over 3 lines; first line contains all required quantities
	        		// so purge lines 2 & 3
	        		in.readLine();
	        		in.readLine();
	        	
	        		Scanner scan = new Scanner(line1);
	        		scan.next();  // Flush #Mod
	        		double teff = scan.nextDouble();
	        		double logg = scan.nextDouble();
	        		scan.next();  // Flush radius
	        		double tcool = scan.nextDouble();
	        		double lnu_cgs = scan.nextDouble();
	        		scan.close();
	        		
	        		// Use established solar values
	        		double mbol = Solar.mbol - 2.5 * Math.log10(lnu_cgs / Solar.lnu_cgs);
	        		
	        		double value = 0.0;
	        		if(quantity == Filter.M_BOL) {
	        			value = mbol;
	        		}
	        		else if(quantity == AtmosphereParameter.TEFF) {
	        			value = teff;
	        		}
	        		else if(quantity == AtmosphereParameter.LOGG) {
	        			value = logg;
	        		}
	        		
	        		data.add(new double[] {tcool, value});
	        	}
	        	
	        	double[] coolingTimeArray = new double[data.size()];
		        double[] quantityArray = new double[data.size()];
		        
		        int idx=0;
		        for(double[] element : data) {
		        	coolingTimeArray[idx] = element[0];
		        	quantityArray[idx] = element[1];
		        	idx++;
		        }
		        
		        try {
		        	quantityAsFnTcoolByMass.put(mass, new MonotonicLinear(coolingTimeArray, quantityArray));
		        }
		        catch(RuntimeException e) {
		        	logger.log(Level.SEVERE, "Unable to load Montreal WD evolutionary sequence file "
		        			+ "from "+filename.toString(), e);
		        }
		        
	        }
	        catch (IOException e) {
	        	logger.log(Level.SEVERE, "Unable to load Montreal WD evolutionary sequence file "
	        			+ "from "+filename.toString(), e);
				e.printStackTrace();
	        }
		}
		
		return new WdCoolingModelGrid(quantity, atm, quantityAsFnTcoolByMass);
	}
}