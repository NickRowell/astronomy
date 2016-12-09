package projects.hip.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import projects.hip.dm.HipStar;

/**
 * Utilities related to the Hipparcos catalogue.
 *
 *
 * @author nrowell
 * @version $Id$
 */
public class HipUtils {

	/**
	 * The logger.
	 */
	private static final Logger logger = Logger.getLogger(HipUtils.class.getName());
	
	/**
	 * Path to the Hipparcos catalogue file.
	 */
	public static final File hipCatFile = new File("/home/nrowell/Astronomy/Data/Hipparcos/Catalogues/Main_Cat.d");
	
	/**
	 * Loads the Hipparcos catalogue.
	 * 
	 * @return
	 * 	List of {@link HipStar}s read from the data file.
	 */
	public static Collection<HipStar> loadHipCatalogue() {
		
		// Read the file and load all the contents to a list of HipStar
		List<HipStar> hipStars = new LinkedList<>();
		
		// Read file contents into the List
		try (BufferedReader in = new BufferedReader(new FileReader(hipCatFile))) {
			String starStr;
			while((starStr=in.readLine())!=null) {
				
				// Avoid blank lines
	        	if (starStr.length()==0)
	                continue;
	        	
	            // Avoid any commented out lines
	            if (starStr.substring(0, 1).equals("#"))
	                continue;
	            
				HipStar source = HipStar.parseHipStar(starStr);
				if(source != null) {
					hipStars.add(source);
				}
			}
		}
		catch(IOException e) {
			logger.log(Level.SEVERE, "Could not load the Hipparcos stars!");
		}
		
		logger.log(Level.INFO, "Loaded "+hipStars.size()+" Hipparcos stars.");
		
		return hipStars;
	}
	
}