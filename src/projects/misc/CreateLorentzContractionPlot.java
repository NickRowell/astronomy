package projects.misc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import infra.io.Gnuplot;

/**
 * Creates a plot depicting the velocity ranges of various physical phenomena to show why Lorentz contraction is
 * outside of everyday experience.
 *
 * @author nrowell
 * @version $Id$
 */
public class CreateLorentzContractionPlot {

	/**
	 * The logger.
	 */
	private static final Logger logger = Logger.getLogger(CreateLorentzContractionPlot.class.getName());
	
	/**
	 * 
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		// File containing velocity range data
		File data = new File("/home/nrowell/Conferences_and_Presentations/2018/2018.05.28_CF/teaching/notes/images/speeds/table.dat");
		
		// Output location of graph
		File output = new File("/home/nrowell/Conferences_and_Presentations/2018/2018.05.28_CF/teaching/notes/images/speeds/");

		// Parse phenomena from file
		List<Phenomenon> phenomena = parsePhenomena(data);
		
		Collections.sort(phenomena);
		
		double c = 299792458.0;
		
		// Make two plots - one at low speeds, one at high speeds
		double[] speedCut = {0.0, 3000, 7e5, 3e8};
		int[] powers = {0, 3, 6};
		
		for(int i=0; i<speedCut.length-1; i++) {
			
			int power = powers[i];
			double factor = 1.0 / Math.pow(10, power);
			
			double speedMin = speedCut[i];
			double speedMax = speedCut[i+1];
			String title = i+"_range";
			
			// Get the time range of the previous plot, for indicating expanded speed range
			double prev_speedMin=0.0, prev_speedMax=0.0;
			if(i>0) {
				prev_speedMin = speedCut[i-1]*factor;
				prev_speedMax = speedCut[i]*factor;
			}
	
			double gamma_max = 1.0 / Math.sqrt(1 - (speedMax*speedMax)/(c*c));
			double gamma_min = 1.0 / Math.sqrt(1 - (speedMin*speedMin)/(c*c));
			double ymax = gamma_max + 0.1 * (gamma_max - gamma_min);
			double ymin = 1.0 - 0.1 * (gamma_max - gamma_min);
			
			
			StringBuilder script = new StringBuilder();
			
			script.append("set term pngcairo enhanced size 640,480\n");
			script.append("set key off\n");
			script.append("set size 1.0,1.0\n");
			
			if(power == 0) {
//				script.append("set xlabel 'Velocity [m/s]' font 'Helvetica-Bold'\n");
				script.append("set xlabel 'Velocity [m/s]'\n");
			}
			else {
//				script.append("set xlabel 'Velocity [10^"+power+" m/s]' font 'Helvetica-Bold'\n");
				script.append("set xlabel 'Velocity [10^"+power+" m/s]'\n");
			}
			script.append("set xtics nomirror out\n");
			script.append("set mxtics 0\n");
			script.append("set xrange [0:"+speedMax*factor+"]\n");
			
			script.append("unset ytics\n");
			
			script.append("set y2label 'Lorentz factor γ' font 'Helvetica-Bold'\n");
			
			// dynamic y range
			script.append("set y2range ["+ymin+":"+ymax+"]\n");
			// fixed y range
			script.append("set y2range [0:8]\n");
			
			script.append("set y2tics out\n");
			
			script.append("set border 9\n");
			
			script.append("c = 299792458.0 * "+factor+"\n");
			script.append("l(v) = 1.0 / sqrt(1.0 - v*v/(c*c))\n");
			
			script.append("set style arrow 1 heads back nofilled linetype 3 linecolor rgb 'black'  linewidth 2.000 size screen 0.006,90.000,90.000\n");
			script.append("set style arrow 2 head back nofilled linetype 3 linecolor rgb 'black'  linewidth 2.000 size screen 0.006,90.000,90.000\n");
			script.append("set style arrow 3 backhead back nofilled linetype 3 linecolor rgb 'black'  linewidth 2.000 size screen 0.006,90.000,90.000\n");
			
			if(i>0) {
				String colour2 = "gray20";
				
				script.append("set obj 1 rect from '"+prev_speedMin+"', graph 0 to '"+prev_speedMax+"', graph 1 fs transparent solid 0.50 noborder fc rgb '"+colour2+"' behind\n");
			}
			
			
			// Counter for number of phenomena included on plot
			int nPh = 0;
			
			for(int m=0; m<phenomena.size(); m++) {
				
				Phenomenon phenomenon = phenomena.get(m);

				double start = phenomenon.lower;
				double end = phenomenon.upper;
				
				if(end < speedMin || start > speedMax ) {
					continue;
				}
				
				start *= factor;
				end *= factor;
				
				String label = phenomenon.official;
				
				
				
				// Calculate the vertical placement of the shower
				int offset = i==2 ? 5 : 3;
				double v = ((nPh++)+offset) * (1.0 / (phenomena.size()));
				
				double margin = 10000000*factor;
				
				if(Double.isNaN(start)) {
					// Only upper limit specified
					script.append("set label '"+label+"' at '"+(end-margin*1.5)+"',graph "+v+" right font 'Helvetica-BoldItalic,10'\n");
					script.append("set arrow from '"+(end - margin)+"',graph "+v+" to '"+end+"',graph "+v+" as 2\n");
				}
				else if(Double.isNaN(end)) {
					// Only lower limit specified
					script.append("set label '"+label+"' at '"+start+"',graph "+v+" offset 0.1,0.7 font 'Helvetica-BoldItalic,10'\n");
					script.append("set arrow from '"+start+"',graph "+v+" to '"+(start + margin)+"',graph "+v+" as 3\n");
				}
				else {
					// Lower and upper limit specified
					script.append("set label '"+label+"' at '"+start+"',graph "+v+" left offset 0.0,0.7 font 'Helvetica-BoldItalic,10'\n");
					script.append("set arrow from '"+start+"',graph "+v+" to '"+end+"',graph "+v+" as 1\n");
				}
				
	//			script.append("set obj rect from '"+start+"',graph "+(v-0.01)+" to '"+end+"',graph "+(v+0.01)+" fs solid 0.5 border fc rgb '"+colour2+"' behind\n");
			}
			script.append("set output '"+output.getAbsolutePath()+"/"+title+".png'\n");
			script.append("plot 1 axes x1y2 w l lt 0 lw 2 lc rgb 'black', l(x) axes x1y2 w l lw 2 lc rgb 'red'\n");
			
			Gnuplot.executeScript(script.toString());
			
			BufferedWriter out = new BufferedWriter(new FileWriter(new File(output, "plot.p")));
			out.write(script.toString());
			out.close();
		}
		
	}
	
	/**
	 * Read the table of speeds of different phenomena and parse individual phenomena.
	 * @param table
	 * 	File containing the table of phenomena.
	 * @return
	 * 	List of parsed {@link Phenomenon}s.
	 */
	private static List<Phenomenon> parsePhenomena(File table) {
		
		List<Phenomenon> showers = new LinkedList<>();
		
		try(BufferedReader in = new BufferedReader(new FileReader(table))) {

			String record;
			
			while((record=in.readLine())!=null) {
				
				// Avoid blank lines
	        	if (record.length()==0)
	                continue;
	        	
	            // Avoid any commented out lines
	            if (record.substring(0, 1).equals("#"))
	                continue;
	            
	            // Tokenize the string
	            String[] parts = record.split("&", -1);
	            
	            // Parse the Phenomenon fields
	            
	            // Extract name
	            String informal = parts[0].trim();
	            
	            // Parse speed limits
	            String startStr = parts[1].trim();
	            double start = Double.NaN;
	            if(!startStr.isEmpty()) {
	            	start = Double.parseDouble(startStr);
	            }
	            String endStr = parts[2].trim();
	            double end = Double.NaN;
	            if(!endStr.isEmpty()) {
	            	end = Double.parseDouble(endStr);
	            }
	            
	            showers.add(new Phenomenon(informal, start, end));
			}
		}
		catch(IOException e) {
			logger.log(Level.SEVERE, "Could not parse phenomena file!", e);
		}
		return showers;
	}
	
	
	
	private static class Phenomenon implements Comparable<Phenomenon> {
		
		/**
		 * Name
		 */
		final String official;
		
		/**
		 * Lower speed [m/s]
		 */
		final double lower;
		
		/**
		 * Upper speed [m/s]
		 */
		final double upper;
		
		/**
		 * Main constructor
		 * @param official
		 * 	Name for the phenomenon
		 * @param start
		 * 	Lower speed [m/s]
		 * @param end
		 * 	Upper speed [m/s]
		 */
		public Phenomenon(String official, double start, double end) {
			this.official = official;
			this.lower = start;
			this.upper = end;
		}


		@Override
		public int compareTo(Phenomenon that) {
			return Double.compare(lower, that.lower);
		}
		
	}
	
}
