package projects.tycho2.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import projects.tycho2.dm.Tycho2Star;

/**
 * Utilities related to the Tycho-2 catalogue.
 *
 *
 * @author nrowell
 * @version $Id$
 */
public class Tycho2Utils {

	/**
	 * The logger.
	 */
	private static final Logger logger = Logger.getLogger(Tycho2Utils.class.getName());
	
	/**
	 * Path to the directory containing the Tycho-2 catalogue files. Note that the catalogue is split into 20 files named:
	 * 
	 * tyc2.dat.00
	 * tyc2.dat.01
	 *  ... 
	 * tyc2.dat.19
	 */
	public static final File tycho2CatDir = new File("/home/nrowell/Astronomy/Data/Tycho-2/");
	
	/**
	 * Loads the Tycho-2 catalogue.
	 * 
	 * @return
	 * 	List of {@link Tycho2Star}s read from the data file.
	 */
	public static Collection<Tycho2Star> loadTycho2Catalogue() {
		
		// Get the set of Tycho-2 catalogue files
		Collection<File> files = new LinkedList<>();
		for(int i=0; i<20; i++) {
			File file = new File(tycho2CatDir, String.format("tyc2.dat.%02d", i));
			files.add(file);
		}
		
		// Read the file and load all the contents to a list of Tycho2Star
		List<Tycho2Star> tycho2Stars = new LinkedList<>();
		
		
		// Loop over all the files
		for(File file : files) {
		
			// Read file contents into the List
			try (BufferedReader in = new BufferedReader(new FileReader(file))) {
				String starStr;
				while((starStr=in.readLine())!=null) {
					
					// Avoid blank lines
		        	if (starStr.length()==0)
		                continue;
		        	
		            // Avoid any commented out lines
		            if (starStr.substring(0, 1).equals("#"))
		                continue;
		            
					Tycho2Star source = Tycho2Star.parseTycho2Star(starStr);
					if(source != null) {
						tycho2Stars.add(source);
					}
				}
			}
			catch(IOException e) {
				logger.log(Level.SEVERE, "Could not load the Tycho-2 stars!");
			}
		}
		
		logger.log(Level.INFO, "Loaded "+tycho2Stars.size()+" Tycho-2 stars.");
		
		return tycho2Stars;
	}
	
}