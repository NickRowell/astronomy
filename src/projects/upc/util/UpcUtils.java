package projects.upc.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import projects.upc.dm.UpcStar;

/**
 * Utility methods associated with the UPC catalogue.
 *
 *
 * @author nrowell
 * @version $Id$
 */
public class UpcUtils {

	/**
	 * The logger.
	 */
	private static final Logger logger = Logger.getLogger(UpcUtils.class.getName());
	
	/**
	 * Path to the UPC data file
	 */
	private static final File upcFile = new File("/home/nrowell/Astronomy/Data/URAT/upc.dat");
	
	/**
	 * Loads the UPC data file.
	 * 
	 * @return
	 * 	List of {@link UpcStar}s read from the data file.
	 */
	public static List<UpcStar> loadUpcCatalogue() {
		
		// Read the file and load all the contents to a list of UpcStar
		List<UpcStar> upcStars = new LinkedList<>();
		
		// Read file contents into the List
		try (BufferedReader in = new BufferedReader(new FileReader(upcFile))) {
			String starStr;
			while((starStr=in.readLine())!=null) {
				
				// Avoid blank lines
	        	if (starStr.length()==0)
	                continue;
	        	
	            // Avoid any commented out lines
	            if (starStr.substring(0, 1).equals("#"))
	                continue;
	            
				UpcStar source = UpcStar.parseUpcStar(starStr);
				if(source != null) {
					upcStars.add(source);
				}
			}
		}
		catch(IOException e) {
			logger.log(Level.SEVERE, "Could not load the UPC stars!");
		}
		
		logger.log(Level.INFO, "Loaded "+upcStars.size()+" UPC stars.");
		
		return upcStars;
	}
	
	/**
	 * Retrieve the subset of {@link UpcStar}s that have been cross-matched with
	 * Hipparcos.
	 * 
	 * @param upcStars
	 * 	List of all {@link UpcStar}s
	 * @return
	 * 	List of all {@link UpcStar}s that have been cross-matched with Hipparcos.
	 */
	public static List<UpcStar> getHipparcosSubset(List<UpcStar> upcStars) {
		
		List<UpcStar> hipStars = new LinkedList<>();
		for(UpcStar upcStar : upcStars) {
			if(upcStar.isHipparcosStar()) {
				hipStars.add(upcStar);
			}
		}
		
		return hipStars;
	}
	
	/**
	 * Retrieve the subset of {@link UpcStar}s that have not been cross-matched with
	 * any external catalogue.
	 * 
	 * @param upcStars
	 * 	List of all {@link UpcStar}s
	 * @return
	 * 	List of all {@link UpcStar}s that have not been cross-matched with any external catalogue.
	 */
	public static List<UpcStar> getUnmatchedSubset(List<UpcStar> upcStars) {
		
		List<UpcStar> unmatchedStars = new LinkedList<>();
		for(UpcStar upcStar : upcStars) {
			if(upcStar.hasNoExternalCatalogueMatch()) {
				unmatchedStars.add(upcStar);
			}
		}
		return unmatchedStars;
	}
	
}