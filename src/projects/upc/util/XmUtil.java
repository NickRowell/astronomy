package projects.upc.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import projects.upc.dm.SsaCrossMatch;
import projects.upc.dm.UpcStar;

public class XmUtil {

	/**
	 * The logger.
	 */
	private static final Logger logger = Logger.getLogger(XmUtil.class.getName());

	/**
	 * Path to the SSA cross matches file
	 */
	private static final File ssaCrossMatchFile = new File("/home/nrowell/Astronomy/Data/URAT/crossmatch/results24_11_19_10_232.csv");
	
	
	/**
	 * Loads the SSA cross matches for the UPC stars and maps them according to the ID
	 * number of the matched UPC star.
	 * 
	 * @param ssaCrossMatchFile
	 * 	File containing a comma-separated-value list of the SSA cross matches.
	 * @return
	 * 	Map of the {@link SsaCrossMatch}es by UPC star ID number.
	 */
	public static Map<Integer, SsaCrossMatch> loadSsaCrossMatches() {
		
		// Read the file and load all the contents to a list of UpcStar
		Map<Integer, SsaCrossMatch> ssaCrossMatches = new HashMap<>();
		
		// Read file contents into the List
		try (BufferedReader in = new BufferedReader(new FileReader(ssaCrossMatchFile))) {
			
			// Skip the first line (column headers)
			in.readLine();
			String starStr;
			while((starStr=in.readLine())!=null) {
				
				// Avoid blank lines
	        	if (starStr.length()==0)
	                continue;
	        	
	            // Avoid any commented out lines
	            if (starStr.substring(0, 1).equals("#"))
	                continue;
	            
				SsaCrossMatch source = SsaCrossMatch.parseSsaCrossMatch(starStr);
				if(source != null) {
					ssaCrossMatches.put(source.upcId, source);
				}
			}
		}
		catch(IOException e) {
			logger.log(Level.SEVERE, "Could not load the SSA cross matches!");
		}
		
		logger.log(Level.INFO, "Loaded "+ssaCrossMatches.size()+" UPC stars.");
		
		return ssaCrossMatches;
	}
	
	/**
	 * Loads the SSA cross-matches to the UPC catalogue and maps them to the {@link UpcStar}s in the given
	 * list.
	 * 
	 * @param upcStars
	 * 	List of {@link UpcStar}s to cross match
	 * @return
	 *  Mapping of {@link UpcStar} to {@link SsaCrossMatch}
	 */
	public static Map<UpcStar, SsaCrossMatch> getUpcStarCrossMatchMap(List<UpcStar> upcStars) {
		
		// Load all cross matches
		Map<Integer, SsaCrossMatch> ssaCrossMatches = loadSsaCrossMatches();
		
		// Map the UpcStars by SsaCrossMatch
		Map<UpcStar, SsaCrossMatch> crossMatchMap = new HashMap<>();
		
		for(UpcStar upcStar : upcStars) {
			
			// Look up the cross match
			if(ssaCrossMatches.containsKey(upcStar.upcId)) {
				
				// We have a cross-match in SSA; retrieve it
				SsaCrossMatch ssaCrossMatch = ssaCrossMatches.get(upcStar.upcId);
				
				// If it's an acceptable cross match then add it to the map
				if(ssaCrossMatch.isAcceptable()) {
					crossMatchMap.put(upcStar, ssaCrossMatch);
				}
			}
		}
		
		return crossMatchMap;
	}
	
	
	
	
	
}
