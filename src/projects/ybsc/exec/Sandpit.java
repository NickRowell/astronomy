package projects.ybsc.exec;

import java.util.Collection;

import projects.ybsc.dm.YaleBscStar;
import projects.ybsc.util.YaleBscUtils;

/**
 * Provides a sandpit for playing with the Yale Bright Star Catalogue data files.
 *
 * @author nrowell
 * @version $Id$
 */
public class Sandpit {
	
	/**
	 * Main application entry point.
	 * 
	 * @param args
	 * 	The command line arguments (ignored)
	 */
	public static void main(String[] args) {
		
		Collection<YaleBscStar> yaleBscStars = YaleBscUtils.loadYaleBrightStarCatalogue();
		
		System.out.println("Got "+yaleBscStars.size()+" YaleBscStars");
	}
	
}
