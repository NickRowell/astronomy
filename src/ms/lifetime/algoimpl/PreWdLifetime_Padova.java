package ms.lifetime.algoimpl;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Level;

import ms.lifetime.algo.PreWdLifetime;
import numeric.functions.MonotonicFunction1D;
import numeric.functions.MonotonicLinear;
import util.FileUtil;

/**
 * Class provides an implementation of the {@link PreWdLifetime} that encapsulates the latest
 * results from the Padova group, which are presented in the papers:
 * 
 * "Scaled solar tracks and isochrones in a large region of the Z-Y plane. I. From the ZAMS to the TP-AGB end for 0.15-2.5 Msun stars"
 *  Bertelli, G., Girardi, L., Marigo, P., Nasi, E., 2008, Astronomy and Astrophysics, 484, 815.
 * 
 * "Scaled solar tracks and isochrones in a large region of the Z-Y plane. II. From 2.5 to 20 Mȯ stars"
 *  Bertelli, G., Nasi, E., Girardi, L., & Marigo, P. 2009, Astronomy and Astrophysics, 508, 355.
 * 	
 * The evolutionary tracks can be divided into several mass ranges:
 * 
 * M < 0.6 M_{Solar} :
 *  - Low mass stars with lifetimes much longer than age of universe: sometimes evolution is halted early
 * 
 * 0.6 M_{Solar} < M < H_{HeFlash}:
 *  - Low mass stars that undergo the Helium flash at the tip of the Red Giant Branch. The evolution is halted there and
 *    restarted at the ZAHB. These models have separate files for the main sequence and horizontal branch evolution, which
 *    should be added to get the total pre-WD lifetime
 *  - Main sequence evolution ends at tip of red giant branch (trgb)
 *  - Horizontal branch evolution ends at first thermal pulse (1sttp)
 *    
 * H_{HeFlash} < M < ?
 *  - Intermediate mass stars. Evolution goes from ZAMS to beginning of thermally pulsing AGB (btpagb)
 *    
 * ? < M
 *  - High mass stars. Evolution goes from ZAMS to carbon ignition
 * 
 * 
 * NOTE that for several (3) metallicity sets, the pre-WD age is not monotonic with mass; there are discontinuities at
 * the boundary between low and intermediate mass stars that must be an artefact in the modelling. In each case there's
 * a single mass that breaks the trend, so we simply ignore these points and load the remaining ones for each metallicity.
 * 
 * @author nrowell
 * @version $Id$
 */
public class PreWdLifetime_Padova extends PreWdLifetime {
	
	/**
	 * The relative path to the top level directory containing the Padova MS evolutionary models.
	 */
	private static final String modelPathStr = "resources/ms/bertelli2008/";
	
	/**
	 * The name of these models.
	 */
	private static final String name = "Padova";
	
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
			logger.log(Level.SEVERE, "Couldn't get top level directory for Padova models!", e);
			return;
		}
		
		// Get an array of all directories containing models for a particular metallicity
	    File[] files = topLevel.listFiles(new BertelliFileFilter());
	    
	    for(File file : files) {
	    	
	    	// Parse the metallicity
	    	double[] zy = new double[2];
	    	parseMetallicity(file.getName(), zy);
	    	Double z = zy[0];
	    	Double y = zy[1];
	    	
	    	// Read the directory contents and load into the map.
	    	
	    	// Initialise the map if this is the first time we've encountered a model with this Z
	    	if(!lifetimeAsFnMassByMetallicity.containsKey(z)) {
	    		lifetimeAsFnMassByMetallicity.put(z, new TreeMap<Double, MonotonicFunction1D>());
	    	}
	    	
	    	// Read the directory contents & create the MonotonicFunction1D
	    	MonotonicFunction1D fun = readModelSet(file);
	    	
	    	// Install in the map
	    	lifetimeAsFnMassByMetallicity.get(z).put(y, fun);
	    }
	}
	
	/**
	 * Read the contents of a directory containing a set of Padova stellar evolutionary models
	 * of varying mass (for a single metallicity) and encapsulate the total pre-WD lifetimes
	 * as a function of mass in a {@link MonotonicFunction1D}.
	 * @param path
	 * 	Path to the directory containing the full set of models of varying mass, for a single
	 * metallicity.
	 * @return
	 * 	A {@link MonotonicFunction1D} that interpolates the total pre-WD lifetime as a
	 * function of the stellar mass.
	 */
	private MonotonicFunction1D readModelSet(File path) {

        // List to store all mass/lifetime points
        List<MassLifetimeCoordinate> points = new LinkedList<MassLifetimeCoordinate>();
        
        /////////////////////////////////////////////////////////////
        //                                                         //
        //        Loop over each main sequence track in turn       //
        //                                                         //
        /////////////////////////////////////////////////////////////
        
        for(File msTrack : path.listFiles(new FilenameFilterBySuffix("ms_"))) {
        
        	// Watch out for a couple of empty files with garbled names that are included in 
        	// the official dataset presumably by accident.
        	if(path.getName().equalsIgnoreCase("z008y40")) {
        		if(msTrack.getName().equalsIgnoreCase("ms_15.0_3.dat") || 
        				msTrack.getName().equalsIgnoreCase("ms_15.0_4.dat")) {
        			continue;
        		}
        	}
        	
            // Get mass
            double mass = readMassFromFilename(msTrack);
            
            // Several sequences have an issue in that the mass vs. lifetime trend is not monotonic;
            // there's an inflexion at the boundary between low and intermediate mass stars.
            if(path.getName().equalsIgnoreCase("z008y23")) {
        		if(Math.abs(mass - 1.95) < 1E-9) {
        			continue;
        		}
        	}
            if(path.getName().equalsIgnoreCase("z017y34")) {
        		if(Math.abs(mass - 1.8) < 1E-9) {
        			continue;
        		}
        	}
            if(path.getName().equalsIgnoreCase("z004y23")) {
        		if(Math.abs(mass - 1.85) < 1E-9) {
        			continue;
        		}
        	}
            
            double msLifetime = 0.0;
            double hbLifetime = 0.0;
            double agbLifetime = 0.0;
            
            // Initialise new point
            MassLifetimeCoordinate point = new MassLifetimeCoordinate(mass);
            
            // Extract the time spent on the Main Sequence
            String[] finalMsLine = FileUtil.getTail(msTrack, 1)[0].trim().split("\\s+");
            msLifetime = Double.parseDouble(finalMsLine[1]);
            
            // Find corresponding Horizontal Branch and Asymptotic Giant Branch tracks (if any)
            for(File hbFile : path.listFiles(new FilenameFilterBySuffix("hb_"))) {
                if(Math.abs(mass - readMassFromFilename(hbFile)) < 1E-9){
                	String[] finalHbLine = FileUtil.getTail(hbFile, 1)[0].trim().split("\\s+");
                	hbLifetime = Double.parseDouble(finalHbLine[1]);
                }
            }
            for(File agbFile : path.listFiles(new FilenameFilterBySuffix("agb_"))) {
                if(Math.abs(mass - readMassFromFilename(agbFile)) < 1E-9){
                	String[] finalAgbLine = FileUtil.getTail(agbFile, 1)[0].trim().split("\\s+");
                	agbLifetime = Double.parseDouble(finalAgbLine[1]);
                }
            }
            
            point.setMsLifetime(msLifetime);
            point.setHbLifetime(hbLifetime);
            point.setAgbLifetime(agbLifetime);
            
            points.add(point);
        }
        
        // Now sort into ascending order of mass
        Collections.sort(points);
        
        // Read out the mass & lifetime points to arrays
        double[] mass = new double[points.size()];
        double[] lifetime = new double[points.size()];
        for(int i=0; i<points.size(); i++) {
        	mass[i] = points.get(i).getMass();
        	lifetime[i] = points.get(i).getTotalLifetime();
        }
        
        return new MonotonicLinear(mass, lifetime);
	}
	
    /**
     * Reads the mass from the name of a file in the set of Bertelli et al (2008) models.
     * @param file
     * 	The File
     * @return
     * 	The mass (M_{Solar}).
     */
    private static double readMassFromFilename(File file){
        String name = file.getName();
        
        int from = name.indexOf("_")+1;
        int to = name.length()-4;
        
        // Trim off leading 'ms_', 'hb_' or 'agb_' and trailing '.dat'
        return Double.parseDouble(name.substring(from, to));
    }
    
	/**
	 * Parse the metallicity (Z) and helium content (Y) of a set of models from the name of the
	 * directory that contains them.
	 * @param dirName
	 * 	The directory name.
	 * @param zy
	 * 	Two element array. On exit, contains the Z value (element 0) and the Y value (element 1)
	 * @return
	 * 	True if we were able to parse the Z and Y value (thus indicating that the directory does
	 * contain models), false otherwise.
	 */
	private static boolean parseMetallicity(String dirName, double[] zy) {
		
		// Pattern that we match on:
		//  - name starts with z
		//  - followed by a string of digits
		//  - then a y
		//  - followed by a string of digits
		String[] parts = dirName.split("((?<=[zy])|(?=[zy]))");
		
		// This will split 'z0004y23' into ['z','0004','y','23'].
		if(parts.length!=4) {
			return false;
		}
		// Check the metallicity flags are present
		if(!(parts[0].equalsIgnoreCase("z") && parts[2].equalsIgnoreCase("y"))) {
			return false;
		}
		
		// Read the metallicity values; the strings give just the number to the right of the decimal point, so
		// we add a '0.' to the start of the string before parsing in order to get the right units.
		double z,y;
		try {
			z = Double.parseDouble("0."+parts[1]);
			y = Double.parseDouble("0."+parts[3]);
		}
		catch(NumberFormatException e) {
			// Couldn't parse the Metallicity values
			return false;
		}
		
		zy[0] = z;
		zy[1] = y;
		
		return true;
	}

    /**
     * Class used to represent single points on the mass/lifetime function.
     *
     * @author nrowell
     * @version $Id$
     */
	private static class MassLifetimeCoordinate implements Comparable<MassLifetimeCoordinate> {
        
        /**
         *  The stellar mass [M_{Solar}]
         */
        public double mass;
    
        /**
         *  Time spent on Main Sequence [yr]
         */
        public double ms = 0;
    
        /**
         *  Time spent on Horizontal Branch [yr]
         */
        public double hb = 0;
    
        /**
         *  Time spent on Asymptotic Giant Branch [yr]
         */
        public double agb = 0;
        
        /**
         * Main constructor
         * @param mass
         * 	The stellar mass [M_{Solar}]
         */
        public MassLifetimeCoordinate(double mass) {
        	this.mass = mass;
        }
        
        /**
         * Set the MS lifetime.
         * @param ms
         * 	The MS lifetime [yr]
         */
        void setMsLifetime(double ms) {
        	this.ms = ms;
        }
        
        /**
         * Set the HB lifetime.
         * @param hb
         * 	The HB lifetime [yr]
         */
        void setHbLifetime(double hb) {
        	this.hb = hb;
        }
        
        /**
         * Set the AGB lifetime.
         * @param agb
         * 	The AGB lifetime [yr]
         */
        void setAgbLifetime(double agb) {
        	this.agb = agb;
        }
        
        /**
         * Get the stellar mass.
         * @return
         * 	The stellar mass [M_{Solar}]
         */
        double getMass() {
        	return mass;
        }
        
        /**
         * Get the total pre-WD lifetime.
         * @return
         * 	The total pre-WD lifetime [yr]
         */
        double getTotalLifetime() {
        	return ms + hb + agb;
        }

		@Override
		public int compareTo(MassLifetimeCoordinate that) {
			
			if(this.mass < that.mass) {
				return -1;
			}
			else if (this.mass > that.mass) {
				return 1;
			}
			else {
				return 0;
			}
		}
    }
	
	/**
	 * Static inner class used to filter directories containing sets of Padova models.
	 *
	 * @author nrowell
	 * @version $Id$
	 */
	private static class BertelliFileFilter implements FileFilter {

		@Override
		public boolean accept(File pathname) {
			
			if(!pathname.isDirectory()) {
				return false;
			}
			
			return parseMetallicity(pathname.getName(), new double[2]);
		}
	}

	/**
	 * Implementation of {@link FilenameFilter} used to filter files
	 * by the suffix.
	 *
	 * @author nrowell
	 * @version $Id$
	 */
	class FilenameFilterBySuffix implements FileFilter {
		
		/**
		 * The suffix to filter.
		 */
		String suffix;
		
		/**
		 * Main constructor.
		 * @param suffix
		 * 	The suffix to filter.
		 */
		public FilenameFilterBySuffix(String suffix) {
			this.suffix = suffix;
		}
		
	    /**
	     * {@inheritDoc}}
	     */
	    @Override
	    public boolean accept(File dir) {
	    	
	    	if(!dir.isFile()) {
	    		return false;
	    	}
	    	
	        if(dir.getName().startsWith(suffix)) {
	        	return true;
	        }
	        return false;
	    }
	}

	
}
