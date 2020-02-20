package projects.gaia.exec;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

import astrometry.util.AstrometryUtils;
import density.DensityProfile;
import density.ExponentialDisk;
import density.Uniform;
import numeric.functions.Linear;
import photometry.util.PhotometryUtils;
import projects.gaia.util.GaiaParallaxErrFn;
import util.ParseUtil;

/**
 * This class provides an application used to predict the numbers of white dwarfs of different
 * magnitudes present in the Gaia Data Release 2, given the white dwarf luminosity function, Gaia's
 * apparent magnitude limit and the population density profile. In addition it computes the number
 * of white dwarfs as a function of parallax significance.
 *
 * @author nrowell
 * @version $Id$
 */
public class WhiteDwarfNumberCounts {

	/**
	 * Main application entry point.
	 * @param args
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException, IOException {
		
		// Required ingredients:
		
		// 1) The White Dwarf Luminosity Function in the Gaia G band
		// 2) The Gaia apparent magnitude limit in g
		// 3) The population density profile
		// 4) The Gaia parallax error as function of apparent magnitude
		
		// Ingredients we can live without:
		
		// -) Gaia completeness as function of sky position
		// -) Extinction as function of line of sight distance and sky position
		
		// 1) The population WDLFs; in numbers per cubic parsec per magnitude
		File thinDiskModelFile = new File("/home/nrowell/Astronomy/gaia_WDs/wdlfs/thin_disk_model_G.txt");
		File thickDiskModelFile = new File("/home/nrowell/Astronomy/gaia_WDs/wdlfs/thick_disk_model_G.txt");
		File spheroidModelFile = new File("/home/nrowell/Astronomy/gaia_WDs/wdlfs/spheroid_model_G.txt");
		
		double[][] thinDiskTable = ParseUtil.parseFile(new BufferedReader(new FileReader(thinDiskModelFile)), ParseUtil.whitespaceDelim, new LinkedList<String>());
		double[][] thickDiskTable = ParseUtil.parseFile(new BufferedReader(new FileReader(thickDiskModelFile)), ParseUtil.whitespaceDelim, new LinkedList<String>());
		double[][] spheroidTable = ParseUtil.parseFile(new BufferedReader(new FileReader(spheroidModelFile)), ParseUtil.whitespaceDelim, new LinkedList<String>());
		
		// Create Linear interpolators for the WDLFs
		Linear thinDiskWdlf = new Linear(thinDiskTable[0], thinDiskTable[2]);
		Linear thickDiskWdlf = new Linear(thickDiskTable[0], thickDiskTable[2]);
		Linear spheroidWdlf = new Linear(spheroidTable[0], spheroidTable[2]);
		
		// 2) Set the fixed G band apparent magnitude limit
		double gBandMagLimit = 20.8;
		
		// 3) Population density profiles for the thin disk, thick disk and halo
		DensityProfile thinDiskProfile = new ExponentialDisk(250.0);
		DensityProfile thickDiskProfile = new ExponentialDisk(1500.0);
		DensityProfile spheroidProfile = new Uniform();
		
		// 4) Get the Gaia parallax error function
		GaiaParallaxErrFn gaiaParallaxErrFn = new GaiaParallaxErrFn();
		
		// Arrays to accumulate the outputs
		
		// Steps in apparent G band magnitude
		double appG_min = 5.0;
		double appG_max = 21.0;
		double appG_step = 0.1;
		int appG_steps = (int)Math.ceil((appG_max - appG_min)/appG_step);
		
		// Steps in absolute G band magnitude
		double absG_min = 5.0;
		double absG_max = 17.0;
		double absG_step = 0.1;
		int absG_steps = (int)Math.ceil((absG_max - absG_min)/absG_step);
		
		// Three elements per magnitude bin: first contains the numbers of WDs with significant
		// parallaxes, the second contains the insignificant parallaxes, the third contains the
		// survey volume.
		double[][] thinDiskWdsAppG = new double[appG_steps][3];
		double[][] thickDiskWdsAppG = new double[appG_steps][3];
		double[][] spheroidWdsAppG = new double[appG_steps][3];
		
		double[][] thinDiskWdsAbsG = new double[absG_steps][3];
		double[][] thickDiskWdsAbsG = new double[absG_steps][3];
		double[][] spheroidWdsAbsG = new double[absG_steps][3];
		
		// Integration loop:
		
		// Loop over celestial coordinates
		
		// Step in right ascension
//		double dra = 2*Math.PI / 1000.0;   // Takes 10 hours
		double dra = 2*Math.PI / 100.0;
		// Step in declination
//		double ddec = Math.PI / 500.0;   // Takes 10 hours
		double ddec = Math.PI / 50.0;
		// Step in parsecs along line of sight
//		double dr = 1.0;   // Takes 10 hours
		double dr = 5.0;
		
		int n=0;
		long tstart = System.currentTimeMillis();
		
		for(double ra = 0.0; ra < 2*Math.PI; ra += dra) {
			
			System.out.println("ra = " + ra);
			
			// Compute run time & predicted time remaining
			long tnow = System.currentTimeMillis();
			if(n>0) {
				double hours_so_far = (tnow - tstart) / (1000.0 * 60.0 * 60.0);
				double hours_remaining = (hours_so_far/n)*(1000 - n);
				System.out.println(n + " / 1000 steps; hours remaining = " + hours_remaining);
			}
			n++;
			
			// Declination; zero at the equator
			for(double dec = -Math.PI/2.0; dec < Math.PI/2.0; dec += ddec) {
				
				// Continue integrating in r until there's no more survey volume
				// XXX: fixed upper limit
				NEXT_LOS:
				for(double r = 0.0; r < 3000; r += dr) {
					
					// Compute volume element
					double dV = r * r * Math.cos(dec) * dr * dra * ddec;
					
					// True stellar parallax at this distance [mas]
					double pi = AstrometryUtils.getParallaxFromDistance(r) * 1000.0;
					
					// Get the population density factor at this position
					double thinDiskDensity = thinDiskProfile.getDensity(r, ra, dec);
					double thickDiskDensity = thickDiskProfile.getDensity(r, ra, dec);
					double spheroidDensity = spheroidProfile.getDensity(r, ra, dec);
					
					// Integrate over the luminosity function
					for(int absG_bin=0; absG_bin<absG_steps; absG_bin++) {
						
						// Transform step number to G band magnitude
						double absG = absG_min + absG_bin*absG_step + absG_step/2.0;
						
						// What is the apparent G band magnitude for stars of this absolute magnitude at this distance?
						double g = PhotometryUtils.getApparentMagnitude(r, absG);
						
						// Which bin in the output arrays does this correspond to?
						int appG_bin = (int)Math.floor((g - appG_min)/appG_step);
						
						// Is this within the range of the arrays used to accumulate outputs?
						if(appG_bin < 0 || appG_bin >= appG_steps) {
							continue;
						}
						
						// What is the parallax standard error for stars at this apparent magnitude?
						double sigma_pi = gaiaParallaxErrFn.evaluate(g);
						
						// Count stars with significant parallaxes separately
						boolean pi_is_significant = pi > 5.0 * sigma_pi;
						
						// Exit conditions
						if(g > gBandMagLimit) {
							if(absG_bin==0) {
								// Brightest star in the WDLF is too faint at this distance; skip to next LOS
								break NEXT_LOS;
							}
							// Stars of this magnitude have dropped below the magnitude limit; halt integration over G
							break;
						}
						
						// Compute the number of stars for each population present in this volume & magnitude bin
						double thinDiskStars = thinDiskWdlf.interpolateY(absG)[0] * absG_step * thinDiskDensity * dV;
						double thickDiskStars = thickDiskWdlf.interpolateY(absG)[0] * absG_step * thickDiskDensity * dV;
						double spheroidStars = spheroidWdlf.interpolateY(absG)[0] * absG_step * spheroidDensity * dV;
						
						thinDiskWdsAppG[appG_bin][2] += thinDiskDensity * dV;
						thickDiskWdsAppG[appG_bin][2] += thickDiskDensity * dV;
						spheroidWdsAppG[appG_bin][2] += spheroidDensity * dV;
						
						thinDiskWdsAbsG[absG_bin][2] += thinDiskDensity * dV;
						thickDiskWdsAbsG[absG_bin][2] += thickDiskDensity * dV;
						spheroidWdsAbsG[absG_bin][2] += spheroidDensity * dV;
						
						if(pi_is_significant) {
							thinDiskWdsAppG[appG_bin][0] += thinDiskStars;
							thickDiskWdsAppG[appG_bin][0] += thickDiskStars;
							spheroidWdsAppG[appG_bin][0] += spheroidStars;
							
							thinDiskWdsAbsG[absG_bin][0] += thinDiskStars;
							thickDiskWdsAbsG[absG_bin][0] += thickDiskStars;
							spheroidWdsAbsG[absG_bin][0] += spheroidStars;
						}
						else {
							thinDiskWdsAppG[appG_bin][1] += thinDiskStars;
							thickDiskWdsAppG[appG_bin][1] += thickDiskStars;
							spheroidWdsAppG[appG_bin][1] += spheroidStars;
							
							thinDiskWdsAbsG[absG_bin][1] += thinDiskStars;
							thickDiskWdsAbsG[absG_bin][1] += thickDiskStars;
							spheroidWdsAbsG[absG_bin][1] += spheroidStars;
						}
					}
				}
			}
		}
		
		// Now write the outputs to file
		File output = new File("/home/nrowell/Astronomy/gaia_WDs/results/gaiaWdNumberCounts.txt");
		
		BufferedWriter out = new BufferedWriter(new FileWriter(output));
		
		double[] cumulative = new double[6];
		
		// First write the apparent G magnitude number counts for each population
		for(int appG_bin=0; appG_bin<appG_steps; appG_bin++) {
			
			// Transform step number to apparent G band magnitude
			double appG = appG_min + appG_bin*appG_step + appG_step/2.0;
			
			cumulative[0] += thinDiskWdsAppG[appG_bin][0];
			cumulative[1] += thinDiskWdsAppG[appG_bin][1];
			cumulative[2] += thickDiskWdsAppG[appG_bin][0];
			cumulative[3] += thickDiskWdsAppG[appG_bin][1];
			cumulative[4] += spheroidWdsAppG[appG_bin][0];
			cumulative[5] += spheroidWdsAppG[appG_bin][1];
			
			out.write(appG + "\t" + thinDiskWdsAppG[appG_bin][0] + "\t" + thinDiskWdsAppG[appG_bin][1]
					 + "\t" + thickDiskWdsAppG[appG_bin][0] + "\t" + thickDiskWdsAppG[appG_bin][1]
					 + "\t" + spheroidWdsAppG[appG_bin][0] + "\t" + spheroidWdsAppG[appG_bin][1]
					 + "\t" + cumulative[0] + "\t" + cumulative[1] + "\t" + cumulative[2]
					 + "\t" + cumulative[3] + "\t" + cumulative[4] + "\t" + cumulative[5] 
					 + "\t" + thinDiskWdsAppG[appG_bin][2] + "\t" + thickDiskWdsAppG[appG_bin][2] + "\t" + spheroidWdsAppG[appG_bin][2] + "\n");
		}
		
		out.newLine();
		out.newLine();
		
		cumulative = new double[6];
		
		// Now write the number counts as a function of absolute G magnitude
		for(int absG_bin=0; absG_bin<absG_steps; absG_bin++) {
			
			// Transform step number to absolute G band magnitude
			double absG = absG_min + absG_bin*absG_step + absG_step/2.0;

			cumulative[0] += thinDiskWdsAbsG[absG_bin][0];
			cumulative[1] += thinDiskWdsAbsG[absG_bin][1];
			cumulative[2] += thickDiskWdsAbsG[absG_bin][0];
			cumulative[3] += thickDiskWdsAbsG[absG_bin][1];
			cumulative[4] += spheroidWdsAbsG[absG_bin][0];
			cumulative[5] += spheroidWdsAbsG[absG_bin][1];
			
			out.write(absG + "\t" + thinDiskWdsAbsG[absG_bin][0] + "\t" + thinDiskWdsAbsG[absG_bin][1]
					 + "\t" + thickDiskWdsAbsG[absG_bin][0] + "\t" + thickDiskWdsAbsG[absG_bin][1]
					 + "\t" + spheroidWdsAbsG[absG_bin][0] + "\t" + spheroidWdsAbsG[absG_bin][1]
					 + "\t" + cumulative[0] + "\t" + cumulative[1] + "\t" + cumulative[2]
					 + "\t" + cumulative[3] + "\t" + cumulative[4] + "\t" + cumulative[5] 
					 + "\t" + thinDiskWdsAbsG[absG_bin][2] + "\t" + thickDiskWdsAbsG[absG_bin][2] + "\t" + spheroidWdsAbsG[absG_bin][2] + "\n");
		}
		
		out.close();
	}
	
}
