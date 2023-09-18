package ms.lifetime.algoimpl;

import java.io.File;
import java.io.FileFilter;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;

import ms.lifetime.algo.PreWdLifetimeTabulated;
import numeric.functions.Linear;
import numeric.functions.MonotonicFunction1D;
import numeric.functions.MonotonicLinear;
import util.FileUtil;

/**
 * Class provides an implementation of {@link PreWdLifetime} that encapsulates the pre-WD lifetimes as
 * a function of mass published in:
 * 
 * Bressan A. et al. 2012, MNRAS, 427, 127 (V1.1)
 * Chen Y. et al. 2014, MNRAS, 444, 2525  (very low mass stars down to 0.1 Msun)
 * Tang J. et al. 2014, arXiv:1410.1745   (massive stars up to ~ 350Msun or more)
 * Chen Y. et al. 2014, to be submitted   (new bolometric corrections for massive stars)
 * 
 * Downloaded from:
 * 
 * https://people.sissa.it/~sbressan/CAF09_V1.2S_M36_LT/
 * 
 * and:
 * 
 * http://stev.oapd.inaf.it/PARSEC/tracks_v12s.html
 * 
 * @author nrowell
 * @version $Id$
 */
public class PreWdLifetime_PARSECv1p2 extends PreWdLifetimeTabulated {
	
	/**
	 * The relative path to the top level directory containing the models.
	 */
	private static final String modelPathStr = "resources/ms/parsec_v1p2/";
	
	/**
	 * The name of these models.
	 */
	private static final String name = "PARSEC v1.2";
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getName() {
		return name;
	}
	
	/**
	 * {@inheritDoc}}
	 */
	@Override
	protected void load() {
		
		// URL pointing to the top level directory containing the models
		URL url = getClass().getClassLoader().getResource(modelPathStr);
		File topLevel = null;
		
		// Get a list of all the subdirectories
		try {
		    topLevel = new File(url.toURI());
		}
		catch(URISyntaxException e) {
			logger.log(Level.SEVERE, "Couldn't get top level directory for " + getName() + " models!", e);
			return;
		}
		
		// Get an array of all directories containing models for a particular metallicity
	    File[] dirs = topLevel.listFiles(new ParsecV1p2FileFilter());
	    
	    double[] zy = new double[2];
	    
	    for(File dir : dirs) {
	    	
	    	// Parse the metallicity from directory name
	    	parseMetallicity(dir.getName(), zy);
	    	
	    	Double z = zy[0];
	    	Double y = zy[1];
	    	
	    	// Read the directory contents and load into the map.
	    	
	    	// Initialise the map if this is the first time we've encountered a model with this Z
	    	if(!lifetimeAsFnMassByMetallicity.containsKey(z)) {
	    		lifetimeAsFnMassByMetallicity.put(z, new TreeMap<Double, MonotonicFunction1D>());
	    	}
	    	
	    	// Read the directory contents & create the MonotonicFunction1D that interpolates
	    	// pre-WD lifetime as a function of mass
	    	MonotonicFunction1D fun = readModelSet(dir);
	    	
	    	// Install in the map
	    	lifetimeAsFnMassByMetallicity.get(z).put(y, fun);
	    }
	}
	
	/**
	 * Read the contents of a directory containing a set of PARSECv1.2 stellar evolutionary models
	 * of varying mass (for a single metallicity) and encapsulate the total pre-WD lifetimes
	 * as a function of mass in a {@link MonotonicFunction1D}.
	 * 
	 * @param path
	 * 	Path to the directory containing the full set of models of varying mass, for a single
	 * metallicity.
	 * @return
	 * 	A {@link MonotonicFunction1D} that interpolates the total pre-WD lifetime as a
	 * function of the stellar mass.
	 */
	private MonotonicFunction1D readModelSet(File path) {

		// Parse data from the file names
		// Filenames are of the format:
    	// Z0.017Y0.279OUTA1.74_F7_M001.300.DAT
    	// Z0.017Y0.279OUTA1.74_F7_M1.300.HB.DAT   [if present]
        // "Some filenames may contain the 'ADD' word. Discard these files because they
        // are used only to merge sections with different critical point numbers."
		
        // List to store all main sequence phase mass/lifetime points
        List<double[]> ms = new LinkedList<>();
        
        for(File msTrack : path.listFiles(pathname -> !pathname.getName().contains("HB") && !pathname.getName().contains("ADD"))) {

        	// Get mass from filename; value inside file may decrease due to mass loss
        	double mass = readMassFromFilename(msTrack.getName());

            // Evolution of lower masses is truncated; must omit them from the loaded data to ensure a monotonic interpolation function
            if(mass < 0.75) {
            	continue;
            }
            
            if(path.getName().equals("Z0.04Y0.321") && mass > 90) {
            	// High mass (>90 M_{solar}) models for Z=0.04 Y=0.321 are non-monotonic; exclude them
            	continue;
            }
            if(path.getName().equals("Z0.03Y0.302") && mass > 95) {
            	// High mass (>95 M_{solar}) models for Z=0.03 Y=0.302 are non-monotonic; exclude them
            	continue;
            }
            if(path.getName().equals("Z0.06Y0.356") && mass > 80) {
            	// High mass (>80 M_{solar}) models for Z=0.06 Y=0.356 are non-monotonic; exclude them
            	continue;
            }
            if(path.getName().equals("Z0.0001Y0.249") && mass > 250) {
            	// High mass (>250 M_{solar}) models for Z=0.0001 Y=0.249 are non-monotonic; exclude them
            	continue;
            }
        	
        	// We exclude the following models that lie around the intermediate/high mass star transition point because they
        	// cause non-monotonicity in the total pre-WD lifetimes. We interpolate over the point instead. It seems the problem
        	// is that the higher mass models are computed right up to the TPAGB in a single track, whereas the lower/intermediate
        	// mass models are split into pre-MS to He flash, then horizontal branch phase, and are thus missing the AGB phase.
        	if(msTrack.getName().equals("Z0.017Y0.279OUTA1.74_F7_M001.900.DAT")) {
        		continue;
        	}
        	if(msTrack.getName().equals("Z0.006Y0.259OUTA1.74_F7_M001.800.DAT")) {
        		continue;
        	}
        	if(msTrack.getName().equals("Z0.004Y0.256OUTA1.74_F7_M001.750.DAT")) {
        		continue;
        	}
        	if(msTrack.getName().equals("Z0.02Y0.284OUTA1.74_F7_M001.925.DAT")) {
        		continue;
        	}
        	if(msTrack.getName().equals("Z0.008Y0.263OUTA1.74_F7_M001.800.DAT")) {
        		continue;
        	}
        	if(msTrack.getName().equals("Z0.01Y0.267OUTA1.74_F7_M001.850.DAT")) {
        		continue;
        	}
        	if(msTrack.getName().equals("Z0.01Y0.267OUTA1.74_F7_M150.000.DAT")) {
        		// This model has anomalously low lifetime; higher masses are OK
        		continue;
        	}
        	if(msTrack.getName().equals("Z0.014Y0.273OUTA1.74_F7_M001.900.DAT")) {
        		continue;
        	}
        	if(msTrack.getName().equals("Z0.03Y0.302OUTA1.74_F7_M001.925.DAT")) {
        		continue;
        	}
        	if(msTrack.getName().equals("Z0.06Y0.356OUTA1.74_F7_M020.000.DAT~")) {
        		// Duplicated file
        		continue;
        	}
        	if(msTrack.getName().equals("Z0.0005Y0.249OUTA1.74_F7_M350.000.DAT")) {
        		// This model has anomalously high lifetime
        		continue;
        	}
        	
            String[] finalLine = FileUtil.getTail(msTrack, 1)[0].trim().split("\\s+");
            double duration = Double.parseDouble(finalLine[2]);
            
            ms.add(new double[] {mass, duration});
        }

        // List to store all horizontal branch phase mass/lifetime points
        List<double[]> hb = new LinkedList<>();

        for(File hbTrack : path.listFiles(pathname -> pathname.getName().contains("HB") && !pathname.getName().contains("ADD"))) {

        	// Get mass from filename; value inside file may decrease due to mass loss
        	double mass = readMassFromFilename(hbTrack.getName());

            // Evolution of lower masses is truncated; must omit them from the loaded data to ensure a monotonic interpolation function
            if(mass < 0.75) {
            	continue;
            }
        	
        	// We exclude the following models to avoid non-monotonicity in the pre-WD lifetimes as a function of mass - see
        	// the comment above. For the horizontal branch phase it is also sometimes the case that the other model is computed
        	// right up to the TPAGB so it's not appropriate to add the horizontal branch evolution.
        	if(hbTrack.getName().equals("Z0.017Y0.279OUTA1.74_F7_M1.900.HB.DAT")) {
        		// Associated MS model is computed to TPAGB
        		continue;
        	}
        	if(hbTrack.getName().equals("Z0.002Y0.252OUTA1.74_F7_M1.650.HB.DAT")) {
        		// Associated MS model is computed to TPAGB
        		continue;
        	}
        	
        	if(hbTrack.getName().equals("Z0.02Y0.284OUTA1.74_F7_M1.925.HB.DAT")) {
        		// Associated MS model is computed to TPAGB
        		continue;
        	}
        	if(hbTrack.getName().equals("Z0.001Y0.25OUTA1.74_F7_M1.650.HB.DAT")) {
        		// Associated MS model is computed to TPAGB
        		continue;
        	}
        	if(hbTrack.getName().equals("Z0.008Y0.263OUTA1.74_F7_M1.800.HB.DAT")) {
        		// Associated MS model is computed to TPAGB
        		continue;
        	}
        	if(hbTrack.getName().equals("Z0.04Y0.321OUTA1.74_F7_M1.900.HB.DAT")) {
        		// Associated MS model is computed to TPAGB
        		continue;
        	}
        	if(hbTrack.getName().equals("Z0.01Y0.267OUTA1.74_F7_M1.850.HB.DAT")) {
        		// Associated MS model is computed to TPAGB
        		continue;
        	}
        	if(hbTrack.getName().equals("Z0.014Y0.273OUTA1.74_F7_M1.900.HB.DAT")) {
        		// Associated MS model is computed to TPAGB
        		continue;
        	}
        	if(hbTrack.getName().equals("Z0.03Y0.302OUTA1.74_F7_M1.925.HB.DAT")) {
        		// Associated MS model is computed to TPAGB
        		continue;
        	}
        	if(hbTrack.getName().equals("Z0.06Y0.356OUTA1.74_F7_M1.800.HB.DAT")) {
        		// Associated MS model is computed to TPAGB
        		continue;
        	}
        	if(hbTrack.getName().equals("Z0.0005Y0.249OUTA1.74_F7_M1.650.HB.DAT")) {
        		// Associated MS model is computed to TPAGB
        		continue;
        	}
        	
            String[] finalLine = FileUtil.getTail(hbTrack, 1)[0].trim().split("\\s+");
            double duration = Double.parseDouble(finalLine[2]);
            
            hb.add(new double[] {mass, duration});
        }
        
        // Now sort into ascending order of mass
        Collections.sort(ms, (p1, p2) -> Double.compare(p1[0], p2[0]));
        Collections.sort(hb, (p1, p2) -> Double.compare(p1[0], p2[0]));
        
        // Create interpolation objects for the main sequence and horizontal branches
        
        double[] msMass = new double[ms.size()];
        double[] msLifetime = new double[ms.size()];
        for(int i=0; i<ms.size(); i++) {
        	msMass[i] = ms.get(i)[0];
        	msLifetime[i] = ms.get(i)[1];
        }
        Linear msLifetimeByMass = new Linear(msMass, msLifetime);
        
        double[] hbMass = new double[hb.size()];
        double[] hbLifetime = new double[hb.size()];
        for(int i=0; i<hb.size(); i++) {
        	hbMass[i] = hb.get(i)[0];
        	hbLifetime[i] = hb.get(i)[1];
        }
        Linear hbLifetimeByMass = new Linear(hbMass, hbLifetime);
        
        // Interpolate total (MS + HB) duration at each unique mass
        SortedSet<Double> uniqueMasses = new TreeSet<>();
        ms.forEach(point -> uniqueMasses.add(point[0]));
        hb.forEach(point -> uniqueMasses.add(point[0]));
        
        double[] masses = new double[uniqueMasses.size()];
        double[] preWdLifetimes = new double[uniqueMasses.size()];
        
        int i=0;
        for(double uniqueMass : uniqueMasses) {
        	
        	// Main sequence lifetime
        	double msLife = msLifetimeByMass.interpolateY(uniqueMass)[0];
        	
        	double hbLife = 0.0;
        	
        	if(!hbLifetimeByMass.isXExtrapolated(uniqueMass)) {
        		// Mass lies within the range of the horizontal branch tracks
        		hbLife = hbLifetimeByMass.interpolateY(uniqueMass)[0];
        	}
        	
        	masses[i] = uniqueMass;
        	preWdLifetimes[i] = msLife + hbLife;
        	
        	// XXX
//        	if(path.getName().equals("Z0.017Y0.279")) {
//	        	System.out.println(uniqueMass + "\t" + msLife + "\t" + hbLife + "\t" + preWdLifetimes[i]);
//	        }
        	
        	i++;
        }
        
        return new MonotonicLinear(masses, preWdLifetimes);
	}
	
    /**
     * Reads the mass from the name of a file in the set of PARSEV V1.2s models. Filenames are of the format:
     * 
     * Z0.017Y0.279OUTA1.74_F7_M001.300.DAT
     * Z0.017Y0.279OUTA1.74_F7_M1.300.HB.DAT
     * 
     * @param name
     * 		The file name.
     * @return
     * 		The mass (M_{Solar}).
     */
    private static double readMassFromFilename(String name) {
    	
    	// Split on M and .D or .H:
    	String[] parts = name.split("(M)|(\\.H)|(\\.D)");
    	
    	// Name  = Z0.017Y0.279OUTA1.74_F7_M005.400.DAT
    	// parts = [Z0.017Y0.279OUTA1.74_F7_, 005.400, AT]
    	
        return Double.parseDouble(parts[1]);
    }
    
	/**
	 * Parse the metallicity (Z) and helium content (Y) of a set of models from the name of the
	 * directory that contains them.
	 * 
	 * @param dirName
	 * 	The directory name, e.g. "Z0.017Y0.279".
	 * @param zy
	 * 	Two element array. On exit, contains the Z value (element 0) and the Y value (element 1)
	 * @return
	 * 	True if we were able to parse the Z and Y value (thus indicating that the directory does
	 * contain models), false otherwise.
	 */
	private static boolean parseMetallicity(String dirName, double[] zy) {
		
		// Pattern that we match on:
		//  - name starts with Z
		//  - followed by a string of digits
		//  - then a Y
		//  - followed by a string of digits
		String[] parts = dirName.split("((?<=[ZY])|(?=[ZY]))");
		
		// This will split 'Z0.017Y0.279' into ['Z','0.017','Y','0.279'].
		if(parts.length!=4) {
			return false;
		}
		// Check the metallicity flags are present
		if(!(parts[0].equalsIgnoreCase("Z") && parts[2].equalsIgnoreCase("Y"))) {
			return false;
		}
		
		// Parse the metallicity values
		double z,y;
		try {
			z = Double.parseDouble(parts[1]);
			y = Double.parseDouble(parts[3]);
		}
		catch(NumberFormatException e) {
			// Couldn't parse the metallicity values
			return false;
		}
		
		zy[0] = z;
		zy[1] = y;
		
		return true;
	}
	
	/**
	 * Static inner class used to filter directories containing sets of PARSEC models.
	 *
	 * @author nrowell
	 * @version $Id$
	 */
	private static class ParsecV1p2FileFilter implements FileFilter {

		@Override
		public boolean accept(File pathname) {
			
			if(!pathname.isDirectory()) {
				return false;
			}
			
			return parseMetallicity(pathname.getName(), new double[2]);
		}
	}
	
}
