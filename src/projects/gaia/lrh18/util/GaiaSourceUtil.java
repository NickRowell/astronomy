
package projects.gaia.lrh18.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import projects.gaia.lrh18.dm.GaiaSource;


/**
 * Utilities related to the Gaia DR2 source table.
 *
 * @author nrowell
 * @version $Id$
 */
public class GaiaSourceUtil {
	
	/**
	 * The logger.
	 */
	private static final Logger logger = Logger.getLogger(GaiaSourceUtil.class.getName());
	
	/**
	 * Loads {@link GaiaSource}s from the CSV file at the given location.
	 * 
	 * @return
	 * 	List of {@link GaiaSource}s read from the data file.
	 */
	public static Collection<GaiaSource> loadGaiaSources(File csv) {
		
		// Read the file and load all the contents to a list of GaiaSource
		List<GaiaSource> gaiaSources = new LinkedList<>();
		
		int missing = 0;
		
		// Read file contents into the List
		try (BufferedReader in = new BufferedReader(new FileReader(csv))) {
			// Trim off the header CSV line
			String starStr = in.readLine();
			while((starStr=in.readLine())!=null) {
				
				// Avoid blank lines
	        	if (starStr.length()==0) {
	                continue;
	        	}
	        	
	            // Avoid any commented out lines
	            if (starStr.substring(0, 1).equals("#")) {
	                continue;
	            }
	            
				GaiaSource source = GaiaSource.parseGaiaSource(starStr);
				if(source != null) {
					gaiaSources.add(source);
				}
				else {
					missing++;
				}
			}
		}
		catch(IOException e) {
			logger.log(Level.SEVERE, "Could not load the "+GaiaSource.class.getSimpleName()+"s!");
		}
		
		logger.log(Level.INFO, "Loaded " + gaiaSources.size() + " " + GaiaSource.class.getSimpleName() + "s.");
		logger.log(Level.INFO, "Failed to load " + missing + " " + GaiaSource.class.getSimpleName() + "s.");
		
		return gaiaSources;
	}
}
