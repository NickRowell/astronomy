package projects.tgas.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import projects.tgas.dm.TgasApassStar;
import projects.tgas.dm.TgasStar;

/**
 * Utilities related to the TGAS catalogue.
 *
 *
 * @author nrowell
 * @version $Id$
 */
public class TgasUtils {

	/**
	 * The logger.
	 */
	private static final Logger logger = Logger.getLogger(TgasUtils.class.getName());
	
	/**
	 * Path to the TGAS catalogue file.
	 */
	public static final File tgasCatFile = new File("/home/nrowell/Astronomy/Data/Gaia/TGAS/release/colours/tgas_tycho2.txt");

	/**
	 * Path to the TGAS/APASS cross-matched catalogue file.
	 */
	public static final File tgasApassCatFile = new File("/home/nrowell/Astronomy/Data/Gaia/TGAS/APASS_crossmatch/gaiadr1tgas_apassdr9.txt");
	
	/**
	 * Loads the TGAS catalogue.
	 * 
	 * @return
	 * 	List of {@link TgasStar}s read from the data file.
	 */
	public static Collection<TgasStar> loadTgasCatalogue() {
		
		// Read the file and load all the contents to a list of TgasStar
		List<TgasStar> tgasStars = new LinkedList<>();
		
		int missing = 0;
		
		// Read file contents into the List
		try (BufferedReader in = new BufferedReader(new FileReader(tgasCatFile))) {
			String starStr;
			while((starStr=in.readLine())!=null) {
				
				// Avoid blank lines
	        	if (starStr.length()==0) {
	                continue;
	        	}
	        	
	            // Avoid any commented out lines
	            if (starStr.substring(0, 1).equals("#")) {
	                continue;
	            }
	            
				TgasStar source = TgasStar.parseTgasStar(starStr);
				if(source != null) {
					tgasStars.add(source);
				}
				else {
					missing++;
				}
			}
		}
		catch(IOException e) {
			logger.log(Level.SEVERE, "Could not load the TGAS stars!");
		}
		
		logger.log(Level.INFO, "Loaded "+tgasStars.size()+" TGAS stars.");
		logger.log(Level.INFO, "Failed to load "+missing+" TGAS stars.");
		
		return tgasStars;
	}

	/**
	 * Loads the TGAS/APASS cross-matched catalogue.
	 * 
	 * @return
	 * 	List of {@link TgasApassStar}s read from the data file.
	 */
	public static Collection<TgasApassStar> loadTgasApassCatalogue() {
		
		// Read the file and load all the contents to a list of TgasApassStar
		List<TgasApassStar> tgasStars = new LinkedList<>();
		
		int missing = 0;
		
		// Read file contents into the List
		try (BufferedReader in = new BufferedReader(new FileReader(tgasApassCatFile))) {
			String starStr;
			while((starStr=in.readLine())!=null) {
				
				// Avoid blank lines
	        	if (starStr.length()==0) {
	                continue;
	        	}
	        	
	            // Avoid any commented out lines
	            if (starStr.substring(0, 1).equals("#")) {
	                continue;
	            }
	            
				TgasApassStar source = TgasApassStar.parseTgasApassStar(starStr);
				if(source != null) {
					tgasStars.add(source);
				}
				else {
					missing++;
				}
			}
		}
		catch(IOException e) {
			logger.log(Level.SEVERE, "Could not load the TGASxAPASS stars!");
		}
		
		logger.log(Level.INFO, "Loaded "+tgasStars.size()+" TGASxAPASS stars.");
		logger.log(Level.INFO, "Failed to load "+missing+" TGASxAPASS stars.");
		
		return tgasStars;
	}
	
	
	
	
}