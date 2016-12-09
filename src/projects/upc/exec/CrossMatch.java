package projects.upc.exec;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import projects.upc.dm.UpcStar;
import projects.upc.util.UpcUtils;

/**
 * Class used to aid in the cross matching of UPC stars with other survey(s) in order to
 * get colours for sources.
 * 
 * Positions of UPC stars are proper-motion-corrected to epoch 2000.
 *
 * @author nrowell
 * @version $Id$
 */
public class CrossMatch {

	/**
	 * Main application entry point.
	 * 
	 * @param args
	 * 	The command line arguments (ignored)
	 * @throws IOException
	 * 	If there's a problem writing the cross match file
	 */
	public static void main(String[] args) throws IOException {
		
		// Load all the sources
		List<UpcStar> upcStars = UpcUtils.loadUpcCatalogue();
		
		// Write to file containing just the ID number and sky coordinates
		File xmFile = new File("/home/nrowell/Astronomy/Data/URAT/crossmatch/upc_xm.dat");
		
		BufferedWriter out = new BufferedWriter(new FileWriter(xmFile));
		
		final double MAS_TO_DEG = (1.0/1000.0) * (1.0/60.0) * (1.0/60.0);
		
		for(UpcStar upcStar : upcStars) {
			
			// TODO: what epoch is appropriate for SSA cross match?
			double ra1990 = upcStar.ra - (upcStar.muRa * MAS_TO_DEG * 24);
			double dec1990 = upcStar.dec - (upcStar.muDec * MAS_TO_DEG * 24);
			
			// NOTE: for SSA cross ID service, tabs are not read correctly. Must use spaces only.
			out.write("upc_id_"+upcStar.upcId + " " + ra1990 + " " + dec1990 + "\n");
		}
		
		out.close();
	}
	
}
