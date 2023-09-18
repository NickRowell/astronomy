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
 * PARSEC V2.0: Stellar tracks and isochrones of low- and intermediate-mass stars with rotation
 * C. T. Nguyen, G. Costa, L. Girardi, G. Volpato, A. Bressan, Y. Chen, P. Marigo, X. Fu and P. Goudfrooij
 * A&A, 665 (2022) A126
 * DOI: https://doi.org/10.1051/0004-6361/202244166
 * http://stev.oapd.inaf.it/PARSEC/tracks_v2.html
 * 
 * Tracks for masses lower than around 0.7 M_{solar}, and all horizontal branch tracks,
 * are identical to the PARSEC v1.2s models.
 * 
 * @author nrowell
 * @version $Id$
 */
public class PreWdLifetime_PARSECv2p0 extends PreWdLifetimeTabulated {
	
	/**
	 * The relative path to the top level directory containing the models.
	 */
	private static final String modelPathStr = "resources/ms/parsec_v2p0/";
	
	/**
	 * The name of these models.
	 */
	private static final String name = "PARSEC v2.0";
	
	/**
	 * Hardcoded choice of rotational parameter.
	 */
	private static final double omega = 0.0;
	
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
		
		// Get an array of all directories containing models for a particular rotational parameter and metallicity
	    File[] dirs = topLevel.listFiles(new ParsecV2p0FileFilter());
	    
	    double[] rotzy = new double[3];
	    
	    for(File dir : dirs) {
	    	
	    	// Parse the metallicity from directory name
	    	parseRotationMetallicity(dir.getName(), rotzy);
	    	
	    	double rot = rotzy[0];
	    	
	    	if(Math.abs(rot - omega) > 1e-9) {
	    		// Not the value of omega that we want
	    		continue;
	    	}
	    	
	    	Double z = rotzy[1];
	    	Double y = rotzy[2];
	    	
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
        
        for(File msTrack : path.listFiles(pathname -> !pathname.getName().contains("HB"))) {

        	// Get mass from filename; value inside file may decrease due to mass loss
        	double mass = readMassFromFilename(msTrack.getName());

            // Evolution of lower masses is truncated (and is copied from PARSECv1.2s anyway); must omit them from the loaded data
        	// to ensure a monotonic interpolation function
            if(mass < 0.75) {
            	continue;
            }
            
            // Final line repeats the column headers; extract the second last line
            String[] finalLine = FileUtil.getTail(msTrack, 2)[0].trim().split("\\s+");
            
            // Columns in file differ for masses below and above two solar masses, but age is always in the thord column
            double duration = Double.parseDouble(finalLine[2]);
            
            ms.add(new double[] {mass, duration});
        }

        // List to store all horizontal branch phase mass/lifetime points
        List<double[]> hb = new LinkedList<>();

        for(File hbTrack : path.listFiles(pathname -> pathname.getName().contains("HB"))) {

        	// Get mass from filename; value inside file may decrease due to mass loss
        	double mass = readMassFromFilename(hbTrack.getName());

            // Evolution of lower masses is truncated (and is copied from PARSECv1.2s anyway); must omit them from the loaded data
        	// to ensure a monotonic interpolation function
            if(mass < 0.75) {
            	continue;
            }
            
        	// We exclude the following models to avoid non-monotonicity in the pre-WD lifetimes as a function of mass - see
        	// the comment above. For the horizontal branch phase it is also sometimes the case that the other model is computed
        	// right up to the TPAGB so it's not appropriate to add the horizontal branch evolution.
            
        	if(hbTrack.getName().equals("Z0.017Y0.279O_IN0.00OUTA1.74_F7_M2.05.TAB.HB")) {
        		// Is associated MS model is computed to TPAGB?
        		continue;
        	}
        	if(hbTrack.getName().equals("Z0.017Y0.279O_IN0.00OUTA1.74_F7_M2.10.TAB.HB")) {
        		// Is associated MS model is computed to TPAGB?
        		continue;
        	}
            
            // Final line repeats the column headers; extract the second last line
            String[] finalLine = FileUtil.getTail(hbTrack, 2)[0].trim().split("\\s+");
            
            // Columns in file differ for masses below and above two solar masses, but age is always in the third column
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
//        	if(path.getName().equals("VAR_ROT0.00_SH_Z0.017_Y0.279")) {
//	        	System.out.println(uniqueMass + "\t" + msLife + "\t" + hbLife + "\t" + preWdLifetimes[i]);
//	        }
        	
        	i++;
        }
        
        return new MonotonicLinear(masses, preWdLifetimes);
	}
	
    /**
     * Reads the mass from the name of a file in the set of PARSEC V2.0 models. Filenames are of the format e.g.:
     * 
     * Z0.017Y0.279O_IN0.00OUTA1.74_F7_M9.50.TAB
     * Z0.017Y0.279O_IN0.00OUTA1.74_F7_M.95.TAB.HB
     * Z0.017Y0.279O_IN0.00OUTA1.77_F7_M.09.TAB
     * 
     * @param name
     * 		The file name.
     * @return
     * 		The mass (M_{Solar}).
     */
    private static double readMassFromFilename(String name) {
    	
    	// Split on M and .T:
    	String[] parts = name.split("(M)|(\\.T)");
    	
    	// Name  = Z0.017Y0.279O_IN0.00OUTA1.74_F7_M9.50.TAB
    	// parts = [Z0.017Y0.279O_IN0.00OUTA1.74_F7_, 9.50, AB]
    	
        return Double.parseDouble(parts[1]);
    }
    
	/**
	 * Parse the rotational parameter (Omega), metallicity (Z) and helium content (Y) of a set of 
	 * models from the name of the directory that contains them.
	 * 
	 * @param dirName
	 * 	The directory name, e.g. "VAR_ROT0.00_SH_Z0.017_Y0.279".
	 * @param rotzy
	 * 	Three element array. On exit, contains the omega value (element 0), Z value (element 1) and the Y value (element 2).
	 * @return
	 * 	True if we were able to parse the omega, Z and Y values (thus indicating that the directory does
	 * contain models), false otherwise.
	 */
	private static boolean parseRotationMetallicity(String dirName, double[] rotzy) {
		
		// Directory name has format:
		//
		// VAR_ROT0.00_SH_Z0.017_Y0.279
		//
		// ...split into: [VAR_ROT 0.00 _SH_Z 0.017 _Y 0.279]

		if(!dirName.substring(0, 7).equals("VAR_ROT")) {
			return false;
		}
		
		// VAR_ROT0.00_SH_Z0.017_Y0.279 -> 0.00_SH_Z0.017_Y0.279
		String trimmedDirName = dirName.substring(7);

		// Parse rotation parameter
		double rot;
		try {
			rot = Double.parseDouble(trimmedDirName.substring(0, 4));
		}
		catch(NumberFormatException e) {
			// Couldn't parse the rotational parameter
			return false;
		}

		// 0.00_SH_Z0.017_Y0.279 -> Z0.017_Y0.279
		trimmedDirName = trimmedDirName.substring(8);
		
		// Z0.017_Y0.279 -> ["Z0.017", "Y0.279"]
		String[] parts = trimmedDirName.split("[_]");
		if(parts.length!=2) {
			return false;
		}

		// Sanity check that we're about to parse what we think we are:
		if(parts[0].charAt(0) != 'Z') {
			throw new RuntimeException("Failed to parse directory name: " + dirName + "; can't parse metallicity from " + parts[0]);
		}
		if(parts[1].charAt(0) != 'Y') {
			throw new RuntimeException("Failed to parse directory name: " + dirName + "; can't parse helium abundance from " + parts[1]);
		}

		// Parse the metallicity values
		double z,y;
		try {
			z = Double.parseDouble(parts[0].substring(1));
			y = Double.parseDouble(parts[1].substring(1));
		}
		catch(NumberFormatException e) {
			// Couldn't parse the metallicity values
			return false;
		}

		rotzy[0] = rot;
		rotzy[1] = z;
		rotzy[2] = y;
		
		return true;
	}
	
	/**
	 * Static inner class used to filter directories containing sets of PARSEC models.
	 *
	 * @author nrowell
	 * @version $Id$
	 */
	private static class ParsecV2p0FileFilter implements FileFilter {

		@Override
		public boolean accept(File pathname) {
			
			if(!pathname.isDirectory()) {
				return false;
			}
			
			return parseRotationMetallicity(pathname.getName(), new double[3]);
		}
	}
	
}
