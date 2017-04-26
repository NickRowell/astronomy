package projects.tycho2.exec;

import java.util.Collection;

import projects.tycho2.dm.Tycho2Star;
import projects.tycho2.util.Tycho2Utils;

/**
 * Provides a sandpit for playing with the Tycho-2 catalogue data files.
 *
 *
 * @author nrowell
 * @version $Id$
 */
public class Sandpit {
	
	/**
	 * Main application entry point.
	 * @param args
	 * 	The command line arguments (ignored)
	 */
	public static void main(String[] args) {
		
		Collection<Tycho2Star> tycho2Stars = Tycho2Utils.loadTycho2Catalogue();
		
		System.out.println("Got "+tycho2Stars.size()+" Tycho2Stars");
	}
	
}
