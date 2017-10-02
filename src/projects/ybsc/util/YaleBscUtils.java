package projects.ybsc.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import projects.ybsc.dm.YaleBscStar;

/**
 * Utilities related to the Yale Bright Star Catalogue.
 *
 * @author nrowell
 * @version $Id$
 */
public class YaleBscUtils {

	/**
	 * The logger.
	 */
	private static final Logger logger = Logger.getLogger(YaleBscUtils.class.getName());
	
	/**
	 * Path to the Yale Bright Star Catalogue file.
	 */
	public static final File yaleBsCatFile = new File("/home/nrowell/Astronomy/Data/Yale_BSC/bsc5.dat");
	
	/**
	 * Loads the Yale Bright Star Catalogue.
	 * 
	 * @return
	 * 	List of {@link YaleBscStar}s read from the data file.
	 */
	public static Collection<YaleBscStar> loadYaleBrightStarCatalogue() {
		
		// Read the file and load all the contents to a list of YaleBscStar
		List<YaleBscStar> yaleBscStars = new LinkedList<>();
		
		// Read file contents into the List
		try (BufferedReader in = new BufferedReader(new FileReader(yaleBsCatFile))) {
			String starStr;
			while((starStr=in.readLine())!=null) {
				
				// Avoid blank lines
	        	if (starStr.length()==0)
	                continue;
	        	
	            // Avoid any commented out lines
	            if (starStr.substring(0, 1).equals("#"))
	                continue;
	            
				YaleBscStar source = YaleBscStar.parseYaleBscStar(starStr);
				if(source != null) {
					yaleBscStars.add(source);
				}
			}
		}
		catch(IOException e) {
			logger.log(Level.SEVERE, "Could not load the Yale Bright Star Catalogue!");
		}
		
		
		logger.log(Level.INFO, "Loaded "+yaleBscStars.size()+" Yale Bright Star Catalogue stars.");
		
		return yaleBscStars;
	}
	
}