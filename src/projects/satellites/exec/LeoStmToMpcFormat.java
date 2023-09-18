package projects.satellites.exec;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Scanner;

import astrometry.util.AstrometryUtils;

/**
 * This class converts calibrated satellite observations from the format produced by the leo_stm software
 * to the 80-column format defined by the Minor Plant Center and used by various orbit analysis applications.
 * 
 * See:
 * https://www.projectpluto.com/fo_help.htm
 * 
 * @author nrowell
 *
 */
public class LeoStmToMpcFormat {
	
	/**
	 * Main application entry point.
	 * 
	 * @param args
	 * 	Command line arguments (ignored).
	 * @throws IOException
	 * 	If there's a problem reading the input satellites file or writing the MPC outputs.
	 */
	public static void main(String[] args) throws IOException {
		
		// Input file of satellites; each line is one satellite
		File input = new File("/home/nrowell/Projects/SSA/FireOPAL/results/2022-02-11/satellites_2022-02-11.txt");
		
		// Output folder; one file per satellite
		File outputDir = new File("/home/nrowell/Projects/SSA/FireOPAL/results/2022-02-11/MPC");
		
		// Observatory code for ROE
		String OBS = "277";
		
		// Formatter for date/time strings '2022-02-12 05:01:44'
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
		
		// Running index for satellites
		int satCount = 1;
		
		BufferedReader in = new BufferedReader(new FileReader(input));
		
		String line;
		
		while((line = in.readLine()) != null) {
			
			List<Observation> observations = new LinkedList<>();
			
			// Split line on commas
			try(Scanner scan = new Scanner(line)) {
				scan.useDelimiter(",");
				while(scan.hasNext()) {
					Observation observation = new Observation();
					observation.ra = scan.nextDouble();
					observation.dec = scan.nextDouble();
					observation.dateTime = LocalDateTime.parse(scan.next(), formatter);
					
					observations.add(observation);
				}
			}
			catch(IllegalStateException | NoSuchElementException e) {
				// Illegal input data format. In practise this happens because the Tycho-2 Bt or Vt magnitudes 
				// are in some cases missing and replaced with "" (two double quotes)
				System.out.println("Could parse line: " + line);
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
			
			System.out.println("Got " + observations.size() + " Observations for satellite");
			
			File satFile = new File(outputDir, "satellite_"+(satCount++) + ".txt");
			
			BufferedWriter out = new BufferedWriter(new FileWriter(satFile));
			
			out.write("COD " + OBS + "\n");
			out.write("OBS N. Rowell\n");
			out.write("ACK2 nr@roe.ac.uk\n");
			
			for(Observation obs : observations) {
				
				// Write MPC 80 column format
				// 
				
				// 
			    //    Example1  C1997 10 13.74589 00 37 45.24 +03 53 36.5                      J95
				
				
				out.write("    Example1  C");
				
				// Date/time YYYY MM DD.DDDDD
				String yyyy = String.format("%04d", obs.dateTime.getYear());
				
				String MM = String.format("%02d", obs.dateTime.getMonthValue());
				
				// Compute fractional day DD.DDDDD
				
				double dd = obs.dateTime.getDayOfMonth();
				double hours = obs.dateTime.getHour() + obs.dateTime.getMinute() / 60.0 + obs.dateTime.getSecond() / 3600.0;
				dd += hours/24.0;
				String ddpddddd = String.format("%02.5f", dd);
				
				out.write(yyyy + " " + MM + " " + ddpddddd + " ");
				
				// RA in format HH MM SS.SS
				double[] raHMS = AstrometryUtils.radiansToHMS(Math.toRadians(obs.ra));
				
				String hms = String.format("%02d %02d %05.2f", (int)raHMS[0], (int)raHMS[1], raHMS[2]);
				
				out.write(hms + " ");
				
				// Dec in format (sign)DD MM SS.S
				
				double[] decDms = AstrometryUtils.radiansToDMS(Math.toRadians(obs.dec));
				String dms = String.format("%s%02d %02d %04.1f", decDms[3] < 0 ? "-" : "+", (int)Math.abs(decDms[0]), (int)decDms[1], decDms[2]);
				
				out.write(dms);
				
				out.write("                      " + OBS);
				
				out.newLine();
			}
			
			out.close();
		}
		
		in.close();
	}
	
	private static class Observation {
		// Degrees
		public double ra;
		// Degrees
		public double dec;
		// e.g. 2022-02-12 05:01:44
		public LocalDateTime dateTime;
	}
	
}
