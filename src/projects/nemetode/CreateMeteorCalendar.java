package projects.nemetode;

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
 * Class reads a data file containing the dates of meteor showers and creates a Gnuplot
 * figure depicting the showers on an annual calendar.
 * 
 * Data file format:
 * 
 * Quadrantids (010 QUA) Dec 28–Jan 12
 * α-Centaurids (102 ACE) Jan 28–Feb 21
 *
 * etc.
 *
 * @author nrowell
 * @version $Id$
 */
public class CreateMeteorCalendar {

	/**
	 * The logger.
	 */
	private static final Logger logger = Logger.getLogger(CreateMeteorCalendar.class.getName());
	
	/**
	 * 
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		// File containing meteor showers data
		File data = new File("/home/nrowell/Projects/NEMETODE/docs/papers/table.dat");
		
		// Output location of graph
		File output = new File("/home/nrowell/Projects/NEMETODE/docs/papers/");
		
		// Parse meteor showers from file
		List<MeteorShower> showers = parseMeteorShowers(data);
		
		Collections.sort(showers);

		SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd");
		
		StringBuilder script = new StringBuilder();
		
		script.append("set term pngcairo enhanced size 1280,960\n");
		script.append("set key off\n");
		script.append("set size 0.9,1.0\n");
		script.append("set ylabel ''\n");
		script.append("set xlabel ''\n");
		script.append("set xdata time\n");
		script.append("set timefmt '%b %d'\n");
		script.append("set format x '%b'\n");
		script.append("set xtics format '%b' offset 4.5,0 font 'Helvetica-Bold,14' scale 0.0001\n");
		script.append("set mxtics 0\n");
		script.append("set xrange ['Jan 1':'Dec 31']\n");
		script.append("set yrange [0:1]\n");
		script.append("unset ytics\n");
		script.append("set border 5\n");
		
		script.append("set obj 15 rect from 'Jan 25', graph 0.73 to 'Jun 5', graph 0.84 front  \n");

		script.append("set label 10 at 'Feb 20', graph 0.8 'Meteor Showers 2016' font 'Helvetica-Bold,16' front left\n");
		script.append("set label 11 at 'Feb 5', graph 0.76 'Based on data from http://www.imo.net/calendar' font 'Helvetica,12' front left\n");
		
		String colour1 = "gray80";
		String colour2 = "gray90";
		
		script.append("set obj 1  rect from 'Jan 1', graph 0 to 'Feb 1', graph 1 fs transparent solid 0.50 noborder fc rgb '"+colour1+"' behind\n");
		script.append("set obj 2  rect from 'Feb 1', graph 0 to 'Mar 1', graph 1 fs transparent solid 0.50 noborder fc rgb '"+colour2+"' behind\n");
		script.append("set obj 3  rect from 'Mar 1', graph 0 to 'Apr 1', graph 1 fs transparent solid 0.50 noborder fc rgb '"+colour1+"' behind\n");
		script.append("set obj 4  rect from 'Apr 1', graph 0 to 'May 1', graph 1 fs transparent solid 0.50 noborder fc rgb '"+colour2+"' behind\n");
		script.append("set obj 5  rect from 'May 1', graph 0 to 'Jun 1', graph 1 fs transparent solid 0.50 noborder fc rgb '"+colour1+"' behind\n");
		script.append("set obj 6  rect from 'Jun 1', graph 0 to 'Jul 1', graph 1 fs transparent solid 0.50 noborder fc rgb '"+colour2+"' behind\n");
		script.append("set obj 7  rect from 'Jul 1', graph 0 to 'Aug 1', graph 1 fs transparent solid 0.50 noborder fc rgb '"+colour1+"' behind\n");
		script.append("set obj 8  rect from 'Aug 1', graph 0 to 'Sep 1', graph 1 fs transparent solid 0.50 noborder fc rgb '"+colour2+"' behind\n");
		script.append("set obj 9  rect from 'Sep 1', graph 0 to 'Oct 1', graph 1 fs transparent solid 0.50 noborder fc rgb '"+colour1+"' behind\n");
		script.append("set obj 10 rect from 'Oct 1', graph 0 to 'Nov 1', graph 1 fs transparent solid 0.50 noborder fc rgb '"+colour2+"' behind\n");
		script.append("set obj 11 rect from 'Nov 1', graph 0 to 'Dec 1', graph 1 fs transparent solid 0.50 noborder fc rgb '"+colour1+"' behind\n");
		script.append("set obj 12 rect from 'Dec 1', graph 0 to 'Dec 31', graph 1 fs transparent solid 0.50 noborder fc rgb '"+colour2+"' behind\n");
		
		script.append("set style arrow 1 heads back nofilled linetype 3 linecolor rgb 'black'  linewidth 2.000 size screen 0.006,90.000,90.000\n");
		script.append("set style arrow 2 head back nofilled linetype 3 linecolor rgb 'black'  linewidth 2.000 size screen 0.006,90.000,90.000\n");
		script.append("set style arrow 3 backhead back nofilled linetype 3 linecolor rgb 'black'  linewidth 2.000 size screen 0.006,90.000,90.000\n");
		
		for(int m=0; m<showers.size(); m++) {
			
			MeteorShower shower = showers.get(m);
			
			// Calculate the vertical placement of the shower
			double v = (m+1) * (1.0 / (showers.size()+1));
			
			String start = dateFormat.format(shower.start);
			String end = dateFormat.format(shower.end);
			String label = shower.informal;
			
			script.append("set label '"+label+"' at '"+end+"',graph "+v+" offset 0.4,0.05 font 'Helvetica-BoldItalic,10'\n");
			
			if(shower.end.after(shower.start)) {
				// Range does not cross new year - single arrow
				
				script.append("set arrow from '"+start+"',graph "+v+" to '"+end+"',graph "+v+" as 1\n");
			}
			else {
				// Range straddles the new year - split into two arrows
				// First arrow - start date until year end
				script.append("set arrow from '"+start+"',graph "+v+" to 'Dec 31',graph "+v+" as 3\n");
				// Second arrow - start of year until end date
				script.append("set arrow from 'Jan 01',graph "+v+" to '"+end+"',graph "+v+" as 2\n");
			}
			
		}
		script.append("set output '"+output.getAbsolutePath()+"/calendar.png'\n");
		script.append("plot -1\n");
		
		Gnuplot.executeScript(script.toString());
		
		BufferedWriter out = new BufferedWriter(new FileWriter(new File(output, "plot.p")));
		out.write(script.toString());
		out.close();
		
	}
	
	/**
	 * Read the table of meteor shower data and parse individual showers name and dates.
	 * @param table
	 * 	File containing the table of meteor shower data.
	 * @return
	 * 	List of parsed {@link MeteorShower}s.
	 */
	private static List<MeteorShower> parseMeteorShowers(File table) {
		
		List<MeteorShower> showers = new LinkedList<>();
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd");
		
		try(BufferedReader in = new BufferedReader(new FileReader(table))) {

			String record;
			
			while((record=in.readLine())!=null) {
				
				// Avoid blank lines
	        	if (record.length()==0)
	                continue;
	        	
	            // Avoid any commented out lines
	            if (record.substring(0, 1).equals("#"))
	                continue;
	            
	            // Parse the meteor shower fields
	            
	            // Dayt. Arietids (171 ARI) May 14–Jun 24
	            
	            String[] parts = record.split("[()]", -1);
	            
	            // Extract informal & official designations
	            String informal = parts[0];
	            String official = parts[1];
	            
	            // Parse dates
	            String[] dates = parts[2].split("[–]", -1);
	            
	            // dates[0] = " Dec 03"
	            // dates[1] = "Dec 15"
	            Date start = dateFormat.parse(dates[0].trim());
	            Date end   = dateFormat.parse(dates[1].trim());
	            
	            showers.add(new MeteorShower(official, informal, start, end));
			}
		}
		catch(IOException e) {
			logger.log(Level.SEVERE, "Could not parse meteor showers file!", e);
		} catch (ParseException e) {
			logger.log(Level.SEVERE, "Could not parse meteor shower date!", e);
		}
		return showers;
	}
	
	
	
	private static class MeteorShower implements Comparable<MeteorShower> {
		
		/**
		 * Official designation, e.g. "171 ARI".
		 */
		final String official;
		
		/**
		 * Informal designation, e.g. "Dayt. Arietids".
		 */
		final String informal;
		
		/**
		 * Starting date
		 */
		final Date start;
		
		/**
		 * Ending date
		 */
		final Date end;
		
		/**
		 * Main constructor.
		 * 
		 * @param official
		 * 	Official designation, e.g. "171 ARI".
		 * @param informal
		 * Informal designation, e.g. "Dayt. Arietids".
		 * @param start
		 *  Starting date
		 * @param end
		 *  Ending date
		 */
		public MeteorShower(String official, String informal, Date start, Date end) {
			this.official = official;
			this.informal = informal;
			this.start = start;
			this.end = end;
		}


		@Override
		public int compareTo(MeteorShower that) {
			return this.end.after(that.end) ? 1 : -1;
		}
		
	}
	
}
