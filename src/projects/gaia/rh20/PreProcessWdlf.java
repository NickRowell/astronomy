package projects.gaia.rh20;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * This class provides a small application that processes the raw WDLF files provided by Nigel and
 * arranges them into the format required for input to the inversion algorithm.
 * 
 * @author nrowell
 */
public class PreProcessWdlf {
	
	/**
	 * Main application entry point.
	 * 
	 * @param args
	 * 	The command line arguments (ignored).
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		// Input file
//		File input = new File("/home/nrowell/Projects/Astronomy/gaia_WDs/Gaia_EDR3_WDLF/wdlf/equalised-WDLF-64-hp5-maglimp80-vgen-h366pc-grp.csv");
//		File input = new File("/home/nrowell/Projects/Astronomy/gaia_WDs/Gaia_EDR3_WDLF/wdlf/equalised-WDLF-128-hp5-maglimp80-vgen-h366pc-grp.csv");
//		File input = new File("/home/nrowell/Projects/Astronomy/gaia_WDs/Gaia_EDR3_WDLF/wdlf/equalised-WDLF-256-hp5-maglimp80-vgen-h366pc-grp.csv");
		File input = new File("/home/nrowell/Projects/Astronomy/gaia_WDs/Gaia_EDR3_WDLF/wdlf/equalised-WDLF-256-hp5-maglimp80-vgen-h366pc-grp-nohyads.csv");
		
		// Output file
		File output = new File("/home/nrowell/Projects/Astronomy/gaia_WDs/Gaia_EDR3_WDLF/wdlf/equalised-WDLF-256-hp5-maglimp80-vgen-h366pc-grp-nohyads_PROCESSED.txt");
		
		BufferedReader in = new BufferedReader(new FileReader(input));
		
		BufferedWriter out = new BufferedWriter(new FileWriter(output));
		
		// Purge header line
		in.readLine();
		
		// Write header line
		out.write("# Column 1: M_{bol} bin centre\n");
		out.write("# Column 2: M_{bol} bin width\n");
		out.write("# Column 3: LF value\n");
		out.write("# Column 4: LF one-sigma uncertainty\n");
		
		List<double[]> data = new LinkedList<>();
		
		String line;
		
		while((line = in.readLine()) != null) {
			
			String[] entries = line.split(",");
			
			double mbol = Double.parseDouble(entries[0]);
			double mbolMin = Double.parseDouble(entries[1]);
			double mbolMax = Double.parseDouble(entries[2]);
			double lf = Double.parseDouble(entries[3]);
			double siglf = Double.parseDouble(entries[4]);
			
			data.add(new double[] {mbol, mbolMin, mbolMax, lf, siglf});
		}
		
		in.close();
		
		// Read out to an array
		double[][] array = new double[data.size()][];
		for(int i=0; i<data.size(); i++) {
			array[i] = data.get(i);
		}
		
		// We have issues with neighbouring magnitude bins overlapping due to low precision in the bin
		// ranges as recorded in the input files. We now post-process the bins to fix this.
		
		// Compute the bin edges
		double[] edges = new double[data.size()+1];
		// Lower edge of first bin requires no tampering
		edges[0] = array[0][0] - array[0][1];
		// Upper edge of final bin requires no tampering
		edges[data.size()] = array[data.size()-1][0] + array[data.size()-1][2];
		// Intermediate boundaries - take average of the edges of neighbouring bins
		for(int i=0; i<data.size()-1; i++) {
			edges[i+1] = (array[i][0] + array[i][2] + array[i+1][0] - array[i+1][1]) / 2.0;
		}
		
		// Now redefine bin centres as the midpoint between the edges, and widths as the distance between the edges
		for(int i=0; i<data.size()-1; i++) {
			array[i][0] = (edges[i] + edges[i+1]) / 2.0;
			array[i][1] = (edges[i+1] - edges[i]);
			
			// XXX Fudge factor to prevent two neighbouring bins from overlapping due to numerical error
			array[i][1] -= 1e-9;
		}
		
		// Detect any overlapping magnitude bins
		for(int i=0; i<data.size()-1; i++) {
			if((array[i][0] + array[i][1]/2.0) > (array[i+1][0] - array[i+1][1]/2.0)) {
				System.out.println("Mag bins "+i+" and "+(i+1)+" overlap!");
				System.out.println("Bin " + i + " extends " + (array[i][0] - array[i][1]/2.0) + " -> " + (array[i][0] + array[i][1]/2.0));
				System.out.println("Bin " + (i+1) + " extends " + (array[i+1][0] - array[i+1][1]/2.0) + " -> " + (array[i+1][0] + array[i+1][1]/2.0));
			}
		}
		
		for(double[] dat : array) {
			out.write(dat[0] + "\t" + dat[1] + "\t" + dat[3] + "\t" + dat[4] + "\n");
		}
		
		out.close();
	}
	
}
