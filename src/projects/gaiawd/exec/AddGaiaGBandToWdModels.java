package projects.gaiawd.exec;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;

import photometry.util.ColourTransformations;

/**
 * This class provides an application that is used to compute Gaia G band magnitudes
 * for existing sets of WD cooling models that don't contain the G band. This is done
 * using colour transformations applied to bands that are provided by the models.
 *
 * NOTE: for the Montreal WD cooling models, we now have colours in the G, BP & RP bands computed
 * by synthetic photometry by Pierre Bergeron, so this application is obsolete for the
 * Montreal models.
 *
 * @author nrowell
 * @version $Id$
 */
public class AddGaiaGBandToWdModels {

	/**
	 * Main application entry point.
	 * @param args
	 * 	The command line arguments (ignored)
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		processMontrealModels();
//		processLpcodeDaModels();
//		processLpcodeDbModels();
	}
	
	/**
	 * 
	 */
	public static void processLpcodeDaModels() throws IOException {
		
		// Folder containing all the Montreal group models (these are perhaps the easiest to process)
		File inputDir = new File("src/resources/wd/cooling/LPCODE/da/z0p01/colours");
		
		// Output directory for models augmented with Gaia G band
		File outputDir = new File("src/resources/wd/cooling/LPCODE/da/z0p01/colours_plus_G");
		
		// Loop over the files in this directory
		Collection<File> inputFiles = FileUtils.listFiles(inputDir, new String[]{"dat"}, false);
		
		for(File inputFile : inputFiles) {
			
			File outputFile = new File(outputDir, inputFile.getName());
			
			BufferedReader in = new BufferedReader(new FileReader(inputFile));
			BufferedWriter out = new BufferedWriter(new FileWriter(outputFile));
			
			String line;
			
			// Read the first two header lines and copy to augmented file
			out.write(in.readLine());
			out.newLine();
			out.write(in.readLine());
			// Append the G band column header
			out.write("\tG");
			out.newLine();
			
			while((line=in.readLine()) != null) {
				
				out.write(line);
				
				if(line.isEmpty() || line.startsWith("#") || line.startsWith("?")) {
					out.newLine();
					continue;
				}
				
				// Remaining lines contain valid model coordinates: add G band magnitude
				
				Scanner scan = new Scanner(line);
				
				scan.next();  // Teff
				scan.next();  // Logg
				scan.next();  // Log L/Lo
				scan.next();  // Age
				scan.next();  // Y
				scan.next();  // F220W
				scan.next();  // F250W
				scan.next();  // F330W
				scan.next();  // F344N
				scan.next();  // F435W
				scan.next();  // F475W
				scan.next();  // F502N
				scan.next();  // F550M
				scan.next();  // F555W
				scan.next();  // F606W
				scan.next();  // F625W
				scan.next();  // F658N
				scan.next();  // F660N
				scan.next();  // F775W
				scan.next();  // F814W
				scan.next();  // F850LP
				scan.next();  // F892N
				scan.next();  // BC(V)
				scan.next();  // M_U
				double b = scan.nextDouble();  // M_B
				double v = scan.nextDouble();  // M_V
				double r = scan.nextDouble();  // M_R
				double i = scan.nextDouble();  // M_I
				scan.next();  // M_J
				scan.next();  // M_H
				scan.next();  // M_K
				scan.next();  // M_L
				
				// Take average of whatever colour transformations are available
				double g = 0;
				int n = 0;
				
				try {
					g += ColourTransformations.getGaiaGFromJohnsonCousinsVI(v, i);
					n++;
				}
				catch(RuntimeException e) {
				}
				try {
					g += ColourTransformations.getGaiaGFromJohnsonCousinsVRDwarfs(v, r);
					n++;
				}
				catch(RuntimeException e) {
				}
				try {
					g += ColourTransformations.getGaiaGFromJohnsonCousinsBVDwarfs(b, v);
					n++;
				}
				catch(RuntimeException e) {
				}
				
				if(n > 0) {
					g /= n;
					out.write(String.format("\t%6.3f\n", g));
				}
				else {
					out.write(String.format("\n"));
					System.out.println("No G transformation possible!");
				}
				
				scan.close();
			}
			out.close();
			in.close();
			
		}
	}
	
	/**
	 * 
	 */
	public static void processLpcodeDbModels() throws IOException {
		
		// Folder containing all the Montreal group models (these are perhaps the easiest to process)
		File inputDir = new File("src/resources/wd/cooling/LPCODE/db/z_solar/colours/");
		
		// Output directory for models augmented with Gaia G band
		File outputDir = new File("src/resources/wd/cooling/LPCODE/db/z_solar/colours_plus_G");
		
		// Loop over the files in this directory
		Collection<File> inputFiles = FileUtils.listFiles(inputDir, null, false);
		
		for(File inputFile : inputFiles) {
			
			if(!inputFile.getName().startsWith("col_")) {
				// Skip files that don't contain models
				continue;
			}
			
			System.out.println("File "+inputFile.getName());
			
			File outputFile = new File(outputDir, inputFile.getName());
			
			BufferedReader in = new BufferedReader(new FileReader(inputFile));
			BufferedWriter out = new BufferedWriter(new FileWriter(outputFile));
			
			String line;
			
			// Read the first two header lines and copy to augmented file
			out.write(in.readLine());
			out.newLine();
			out.write(in.readLine());
			// Append the G band column header
			out.write("\tG");
			out.newLine();
			
			while((line=in.readLine()) != null) {
				
				out.write(line);
				
				if(line.isEmpty() || line.startsWith("#") || line.startsWith("?")) {
					out.newLine();
					continue;
				}
				
				// Remaining lines contain valid model coordinates: add G band magnitude
				
				Scanner scan = new Scanner(line);
				
				scan.next();  // Teff
				scan.next();  // Logg
				scan.next();  // Log L/Lo
				scan.next();  // Age
				scan.next();  // Y
				scan.next();  // F220W
				scan.next();  // F250W
				scan.next();  // F330W
				scan.next();  // F344N
				scan.next();  // F435W
				scan.next();  // F475W
				scan.next();  // F502N
				scan.next();  // F550M
				scan.next();  // F555W
				scan.next();  // F606W
				scan.next();  // F625W
				scan.next();  // F658N
				scan.next();  // F660N
				scan.next();  // F775W
				scan.next();  // F814W
				scan.next();  // F850LP
				scan.next();  // F892N
				scan.next();  // BC(V)
				scan.next();  // M_U
				double b = scan.nextDouble();  // M_B
				double v = scan.nextDouble();  // M_V
				double r = scan.nextDouble();  // M_R
				double i = scan.nextDouble();  // M_I
				
				// Take average of whatever colour transformations are available
				double g = 0;
				int n = 0;
				
				try {
					g += ColourTransformations.getGaiaGFromJohnsonCousinsVI(v, i);
					n++;
				}
				catch(RuntimeException e) {
				}
				try {
					g += ColourTransformations.getGaiaGFromJohnsonCousinsVRDwarfs(v, r);
					n++;
				}
				catch(RuntimeException e) {
				}
				try {
					g += ColourTransformations.getGaiaGFromJohnsonCousinsBVDwarfs(b, v);
					n++;
				}
				catch(RuntimeException e) {
				}
				
				if(n > 0) {
					g /= n;
					out.write(String.format("\t%6.3f\n", g));
				}
				else {
					out.write(String.format("\n"));
					System.out.println("No G transformation possible!");
				}
				
				scan.close();
			}
			out.close();
			in.close();
			
		}
	}
	
	/**
	 * 
	 */
	public static void processMontrealModels() throws IOException {
		
		// Folder containing all the Montreal group models (these are perhaps the easiest to process)
		File inputDir = new File("src/resources/wd/cooling/Montreal/standard");
		
		// Output directory for models augmented with Gaia G band
		File outputDir = new File("src/resources/wd/cooling/Montreal/standard_plus_G");
		
		// Loop over the files in this directory
		Collection<File> inputFiles = FileUtils.listFiles(inputDir, null, false);
		
		for(File inputFile : inputFiles) {
			
			File outputFile = new File(outputDir, inputFile.getName());
			
			BufferedReader in = new BufferedReader(new FileReader(inputFile));
			BufferedWriter out = new BufferedWriter(new FileWriter(outputFile));
			
			String line;
			
			// Read the first two header lines and copy to augmented file
			out.write(in.readLine());
			out.newLine();
			out.write(in.readLine());
			// Append the G band column header
			out.write("\tG");
			out.newLine();
			
			while((line=in.readLine()) != null) {
				
				out.write(line);
				
				if(line.isEmpty() || line.startsWith("#") || line.startsWith("?")) {
					out.newLine();
					continue;
				}
				
				// Remaining lines contain valid model coordinates: add G band magnitude
				
				Scanner scan = new Scanner(line);
				
				scan.next();  // Teff
				scan.next();  // Logg
				scan.next();  // Mbol
				scan.next();  // BC
				scan.next();  // U
				double b = scan.nextDouble();  // B
				double v = scan.nextDouble();  // V
				double r = scan.nextDouble();  // R
				double i = scan.nextDouble();  // I
				
				// Take average of whatever colour transformations are available
				double g = 0;
				int n = 0;
				
				try {
					g += ColourTransformations.getGaiaGFromJohnsonCousinsVI(v, i);
					n++;
				}
				catch(RuntimeException e) {
				}
				try {
					g += ColourTransformations.getGaiaGFromJohnsonCousinsVRDwarfs(v, r);
					n++;
				}
				catch(RuntimeException e) {
				}
				try {
					g += ColourTransformations.getGaiaGFromJohnsonCousinsBVDwarfs(b, v);
					n++;
				}
				catch(RuntimeException e) {
				}
				
				if(n > 0) {
					g /= n;
					out.write(String.format("\t%6.3f\n", g));
				}
				else {
					System.out.println("No G transformation possible!");
				}
				
				scan.close();
			}
			out.close();
			in.close();
			
		}
	}
}
