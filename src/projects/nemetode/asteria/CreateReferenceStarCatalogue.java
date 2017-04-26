package projects.nemetode.asteria;

import java.io.File;
import java.util.logging.Logger;

/**
 * This class provides an application that processes the Tycho-2 catalogue to generate a reference
 * star catalogue, stored either as an ASCII or binary file, that is suitable for use with Asteria
 * for use in calibrating the camera.
 *
 * @author nrowell
 * @version $Id$
 */
public class CreateReferenceStarCatalogue {

	/**
	 * The Logger
	 */
    protected static Logger logger = Logger.getLogger(CreateReferenceStarCatalogue.class.getCanonicalName());
    
    /**
     * Path to the Tycho-2 catalogue file(s)
     */
    private static File cataloguePath = new File("");

	/**
	 * Main application entry point.
	 * @param args
	 * 	The command line arguments (ignored)
	 */
	public static void main(String[] args) {
		
		
		
	}
	
}