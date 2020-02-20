package projects.gaia.exec;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;

import Jama.Matrix;
import infra.io.Gnuplot;
import infra.os.OSChecker;
import wd.models.infra.WdAtmosphereType;

/**
 * This class provides an application that is used to compute Gaia BP and RP band magnitudes
 * for existing sets of WD cooling models that don't contain these bands. This is done
 * using colour transformations applied to bands that are provided by the models.
 *
 * Colour transformations for the BP and RP bands are not yet available, so these are computed
 * on the fly based on the G, BP and RP magnitudes provided in the Montreal WD cooling models.
 * The BP and RP bands can be transformed from the UBVRI magnitudes.
 *
 * @author nrowell
 * @version $Id$
 */
public class AddGaiaGBpRpBandsToWdModels {

	
	public static abstract class ColourTransformation {
		
		public abstract double transformColour(double u, double b, double v, double r, double i);
		
	}
	
	/**
	 * Main application entry point.
	 * @param args
	 * 	The command line arguments (ignored)
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		ColourTransformation[] colTransH = computeColourTransforms(WdAtmosphereType.H);
		ColourTransformation[] colTransHe = computeColourTransforms(WdAtmosphereType.He);
		processLpcodeDaModels(colTransH);
		processLpcodeDbModels(colTransHe);
	}
	
	/**
	 * 
	 */
	public static void processLpcodeDaModels(ColourTransformation[] colTransH) throws IOException {
		
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
			// Append the G, BP and RP band column headers
			out.write("\tG\tBP\tRP");
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
				double u = scan.nextDouble();  // M_U
				double b = scan.nextDouble();  // M_B
				double v = scan.nextDouble();  // M_V
				double r = scan.nextDouble();  // M_R
				double i = scan.nextDouble();  // M_I
				scan.next();  // M_J
				scan.next();  // M_H
				scan.next();  // M_K
				scan.next();  // M_L
				
				// Transform the colours
				double g = colTransH[0].transformColour(u, b, v, r, i);
				double bp = colTransH[1].transformColour(u, b, v, r, i);
				double rp = colTransH[2].transformColour(u, b, v, r, i);
				
				out.write(String.format("\t%6.3f\t%6.3f\t%6.3f\n", g, bp, rp));

				scan.close();
			}
			out.close();
			in.close();
		}
	}
	
	/**
	 * 
	 */
	public static void processLpcodeDbModels(ColourTransformation[] colTransHe) throws IOException {
		
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
			// Append the G, BP and RP band column headers
			out.write("\tG\tBP\tRP");
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
				double u = scan.nextDouble();  // M_U
				double b = scan.nextDouble();  // M_B
				double v = scan.nextDouble();  // M_V
				double r = scan.nextDouble();  // M_R
				double i = scan.nextDouble();  // M_I

				// Transform the colours
				double g = colTransHe[0].transformColour(u, b, v, r, i);
				double bp = colTransHe[1].transformColour(u, b, v, r, i);
				double rp = colTransHe[2].transformColour(u, b, v, r, i);

				out.write(String.format("\t%6.3f\t%6.3f\t%6.3f\n", g, bp, rp));

				scan.close();
			}
			out.close();
			in.close();
			
		}
	}
	
	/**
	 * 
	 */
	public static ColourTransformation[] computeColourTransforms(WdAtmosphereType atm) throws IOException {
		
		// Folder containing all the Montreal group models including the Gaia bands computed from synthetic photometry
		File inputDir = new File("src/resources/wd/cooling/Montreal/gaia");
		
		// Array containing colour transformations for the G, BP and RP bands
		ColourTransformation[] colTrans = new ColourTransformation[3];
		
		// Loop over the files in this directory
		Collection<File> inputFiles = FileUtils.listFiles(inputDir, null, true);
		
		// Read all available (U, B, V, R, I, G, BP, RP) points
		List<Point> points = new LinkedList<>();
		
		for(File inputFile : inputFiles) {
			
			// Skip files not containing WD models. Ad-hoc rule is that all WD model files contains underscores in the name.
			if(inputFile.getName().indexOf("_") == -1 || inputFile.getName().indexOf("~") != -1) {
				continue;
			}
			
			switch(atm) {
				case H: {
					// Skip helium atmosphere models
					if(inputFile.getName().indexOf("He") != -1) {
						continue;
					}
					break;
				}
				case He: {
					// Skip hydrogen atmosphere models
					if(inputFile.getName().indexOf("He") == -1) {
						continue;
					}
					break;
				}
			}
			
			System.out.println("Reading file "+inputFile.getName());
			
			BufferedReader in = new BufferedReader(new FileReader(inputFile));
			
			String line;
			
			while((line=in.readLine()) != null) {
				
				if(line.isEmpty() || line.startsWith("#") || line.startsWith("?")) {
					continue;
				}
				
				// Remaining lines contain valid model coordinates: add G band magnitude
				
				Point point = new Point();
				
				Scanner scan = new Scanner(line);
				
				for(int j=0; j<4; j++) {
					scan.next();
				}
				
				point.U  = scan.nextDouble();  // U
				point.B = scan.nextDouble();  // B
				point.V = scan.nextDouble();  // V
				point.R = scan.nextDouble();  // R
				point.I = scan.nextDouble();  // I
				for(int j=0; j<8; j++) {
					scan.next();
				}
				point.G = scan.nextDouble();   // G
				point.BP = scan.nextDouble();  // BP
				point.RP = scan.nextDouble();  // RP
				
				points.add(point);
				
				scan.close();
			}
			in.close();
		}
		
		System.out.println("Got " + points.size() + " point to compute transformation");
		
		// Now compute transformation for the G, BP and RP bands
		//
		// The model is e.g. BP-V = a0 + a1(B-V) + a2(B-V)^2 + (higher order terms in B-V) + aX(V-R) + aX(V-R)^2 + (higher order terms in V-R) + (R-I terms)
		
		// The order of the B-V, V-R and R-I terms to be used to fit each of G, BP and RP
		final int[] orderBV = new int[]{3, 3, 3};
		final int[] orderVR = new int[]{3, 3, 3};
		final int[] orderRI = new int[]{3, 3, 3};
		
//		final int[] orderBV = new int[]{3, 3, 0};
//		final int[] orderVR = new int[]{0, 0, 0};
//		final int[] orderRI = new int[]{0, 0, 3};
		
//		final int[] orderBV = new int[]{1, 1, 0};
//		final int[] orderVR = new int[]{0, 0, 0};
//		final int[] orderRI = new int[]{0, 0, 1};
		
		
		
		// Build design matrix and observation vector
		double[][][] a = new double[3][points.size()][];
		double[][][] b = new double[3][points.size()][1];
		
		for(int i=0; i<3; i++) {
			int terms = 1 + orderBV[i] + orderVR[i] + orderRI[i];
			for(int j=0; j<points.size(); j++) {
				a[i][j] = new double[terms];
			}
		}
		
		int idx=0;
		for(Point point : points) {
			
			// Loop over G, BP and RP models
			for(int i=0; i<3; i++) {
				
				// Constant term
				a[i][idx][0] = 1.0;
				
				// B-V terms
				double tmp = (point.B - point.V);
				for(int k=0; k<orderBV[i]; k++) {
					a[i][idx][k+1] = tmp;
					tmp *= (point.B - point.V);
				}
				// V-R terms
				tmp = (point.V - point.R);
				for(int k=0; k<orderVR[i]; k++) {
					a[i][idx][k+1+orderBV[i]] = tmp;
					tmp *= (point.V - point.R);
				}
				// R-I terms
				tmp = (point.R - point.I);
				for(int k=0; k<orderRI[i]; k++) {
					a[i][idx][k+1+orderBV[i]+orderVR[i]] = tmp;
					tmp *= (point.R - point.I);
				}
				
			}
			
			b[0][idx][0] = point.G - point.V;
			b[1][idx][0] = point.BP - point.V;
			b[2][idx][0] = point.RP - point.I;
			
			idx++;
		}
		
		// Solve for G, BP and RP in turn
		for(int i=0; i<3; i++) {
			
			final int fi = i;
		
			Matrix A = new Matrix(a[i]);
			Matrix B = new Matrix(b[i]);
			
			// Solve for coefficients a
			final Matrix X = A.solve(B);
			
			// Create the colour transformation
			ColourTransformation trans = new ColourTransformation() {
	
				@Override
				public double transformColour(double u, double b, double v, double r, double i) {
					
					double gaia_band = 0.0;
					
					// Add constant term
					gaia_band += X.get(0, 0);
					
					// Add B-V terms
					double tmp = b-v;
					for(int k=0; k<orderBV[fi]; k++) {
						gaia_band += X.get(k+1, 0) * tmp;
						tmp *= (b-v);
					}
					// Add V-R terms
					tmp = v-r;
					for(int k=0; k<orderVR[fi]; k++) {
						gaia_band += X.get(k+1+orderBV[fi], 0) * tmp;
						tmp *= (v-r);
					}

					// Add R-I terms
					tmp = r-i;
					for(int k=0; k<orderRI[fi]; k++) {
						gaia_band += X.get(k+1+orderBV[fi]+orderVR[fi], 0) * tmp;
						tmp *= (r-i);
					}
					
					if(fi == 0) {
						// G transformation
						gaia_band += v;
					}
					else if(fi == 1) {
						// BP transformation
						gaia_band += v;
					}
					else {
						// RP transformation
						gaia_band += i;
					}
					
					return gaia_band;
				}
				
			};
			colTrans[i] = trans;
		}
					
		// Print the fit results in terms of two-colour plots
		for(int i=0; i<3; i++) {
			
			// Sort points into ascending order of B-V or R-I as appropriate to the colour transformation model
			
			if(i == 0) {
				
				// Sort on B-V
				Collections.sort(points, new Comparator<Point>() {
					@Override
					public int compare(Point o1, Point o2) {
						return Double.compare(o1.B-o1.V, o2.B-o2.V);
					}
				});
			}
			else if (i == 1) {

				// Sort on B-V
				Collections.sort(points, new Comparator<Point>() {
					@Override
					public int compare(Point o1, Point o2) {
						return Double.compare(o1.B-o1.V, o2.B-o2.V);
					}
				});
			}
			else {
				// Sort on R-I
				Collections.sort(points, new Comparator<Point>() {
					@Override
					public int compare(Point o1, Point o2) {
						return Double.compare(o1.R-o1.I, o2.R-o2.I);
					}
					
				});
			}
			
			String[] xlabel = new String[]{"B - V", "B - V", "R - I"};
			String[] ylabel = new String[]{"G - V", "BP - V", "RP - I"};	
			
			StringBuilder script = new StringBuilder();
			script.append("set terminal pngcairo enhanced color size 640,640").append(OSChecker.newline);
			script.append("set xrange [*:*]").append(OSChecker.newline);
			script.append("set yrange [*:*]").append(OSChecker.newline);
			script.append("set key off").append(OSChecker.newline);
			script.append("set xtics out").append(OSChecker.newline);
			script.append("set ytics out").append(OSChecker.newline);
			script.append("set title '"+atm+" atmosphere WDs'").append(OSChecker.newline);
			script.append("set xlabel '"+xlabel[i]+"'").append(OSChecker.newline);
			script.append("set ylabel '"+ylabel[i]+"'").append(OSChecker.newline);
			
			script.append("plot '-' u 1:2 w p pt 7 ps 0.25 lc rgbcolor 'black' notitle, ");
			script.append("     '-' u 1:2 w l lc rgbcolor 'red' title 'fit'").append(OSChecker.newline);
			
			// Temporary storage for observed and fitted colours
			double[][] array = new double[points.size()][3];
			int j=0;
			for(Point point : points) {
				
				double gaia_band_obs;
				double band;
				double colour;
				
				if(i == 0) {
					gaia_band_obs = point.G;
					band = point.V;
					colour = point.B - point.V;
				}
				else if(i == 1) {
					gaia_band_obs = point.BP;
					band = point.V;
					colour = point.B - point.V;
				}
				else {
					gaia_band_obs = point.RP;
					band = point.I;
					colour = point.R - point.I;
				}
				
				// Fitted quantity
				double gaia_band_fit = colTrans[i].transformColour(point.U, point.B, point.V, point.R, point.I);
				
				array[j][0] = colour;
				array[j][1] = gaia_band_obs - band;
				array[j++][2] = gaia_band_fit - band;
			}
			
			for(j=0; j<points.size(); j++) {
				script.append(array[j][0] + " " + array[j][1]).append(OSChecker.newline);
			}
			script.append("e").append(OSChecker.newline);
			for(j=0; j<points.size(); j++) {
				script.append(array[j][0] + " " + array[j][2]).append(OSChecker.newline);
			}
			script.append("e").append(OSChecker.newline);
			
			try {
				Gnuplot.displayImage(Gnuplot.executeScript(script.toString()));
			} catch (IOException e) {
				System.out.println("Problem plotting colour transformation");
			}
		}
		
		// Print the fit results in terms of correlation plots
		for(int i=0; i<3; i++) {
			
			String[] xlabel = new String[]{"G (model)", "BP (model)", "RP (model)"};
			String[] ylabel = new String[]{"G (transformed)", "BP (transformed)", "RP (transformed)"};		
			
			StringBuilder script = new StringBuilder();
			script.append("set terminal pngcairo enhanced color size 640,640").append(OSChecker.newline);
			script.append("set xrange [*:*]").append(OSChecker.newline);
			script.append("set yrange [*:*]").append(OSChecker.newline);
			script.append("set key off").append(OSChecker.newline);
			script.append("set xtics out").append(OSChecker.newline);
			script.append("set ytics out").append(OSChecker.newline);
			script.append("set xlabel '"+xlabel[i]+"'").append(OSChecker.newline);
			script.append("set ylabel '"+ylabel[i]+"'").append(OSChecker.newline);
			script.append("f(x) = x").append(OSChecker.newline);
			
			
			// Temporary storage for observed and fitted colours
			double[][] array = new double[points.size()][3];
			int j=0;
			// Compute RMS scatter
			double rms = 0.0;
			for(Point point : points) {
				
				double gaia_band_obs;
				double colour;
				
				if(i == 0) {
					gaia_band_obs = point.G;
					colour = point.B - point.V;
				}
				else if(i == 1) {
					gaia_band_obs = point.BP;
					colour = point.B - point.V;
				}
				else {
					gaia_band_obs = point.RP;
					colour = point.R - point.I;
				}
				
				// Fitted quantity
				double gaia_band_fit = colTrans[i].transformColour(point.U, point.B, point.V, point.R, point.I);
				
				array[j][0] = colour;
				array[j][1] = gaia_band_obs;
				array[j++][2] = gaia_band_fit;
				
				rms += (gaia_band_obs - gaia_band_fit)*(gaia_band_obs - gaia_band_fit);
			}
			
			rms = Math.sqrt(rms/points.size());
			
			
			script.append("set title '"+atm+" atmosphere WDs'").append(OSChecker.newline);
			script.append("set label 'RMS scatter = "+rms+"' font 'Helvetica,14' at graph 0.3,0.1").append(OSChecker.newline);
			
			script.append("plot '-' u 1:2 w p pt 7 ps 0.25 lc rgbcolor 'black' notitle, f(x) w l lt 1 lc rgb 'red'").append(OSChecker.newline);
			for(j=0; j<points.size(); j++) {
				script.append(array[j][1] + " " + array[j][2]).append(OSChecker.newline);
			}
			script.append("e").append(OSChecker.newline);
			
			try {
				Gnuplot.displayImage(Gnuplot.executeScript(script.toString()));
			} catch (IOException e) {
				System.out.println("Problem plotting colour transformation");
			}
			
		}
		
		return colTrans;
	}
	
	private static class Point {

		public double U;
		public double B;
		public double V;
		public double R;
		public double I;
		public double G;
		public double BP;
		public double RP;
		
		public Point() {
			
		}
	}
}
