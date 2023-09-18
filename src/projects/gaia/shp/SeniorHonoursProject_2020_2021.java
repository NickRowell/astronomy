package projects.gaia.shp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.apache.commons.math3.util.Pair;

import Jama.Matrix;
import astrometry.util.AstrometryUtils;
import constants.Galactic;
import constants.Units;
import infra.io.Gnuplot;
import infra.os.OSChecker;
import kinematics.dm.AstrometricStar;
import kinematics.util.ProperMotionDeprojection;
import numeric.data.Range;
import numeric.data.RangeMap;
import numeric.fitting.PolynomialFitting;
import numeric.functions.Function;
import numeric.functions.Polynomial;
import numeric.stats.StatUtil;
import photometry.Filter;
import photometry.util.PhotometryUtils;
import projections.Aitoff;
import projections.util.ProjectionUtil;
import projects.gaia.shp.dm.GaiaSource_2020_2021;
import projects.gaia.shp.util.GaiaSourceUtil;
import util.CharUtil;
import wd.models.algo.WdCoolingModelSet;
import wd.models.infra.WdAtmosphereType;
import wd.models.infra.WdCoolingModels;

/**
 * 
 *
 * @author nrowell
 * @version $Id$
 */
public class SeniorHonoursProject_2020_2021 {

	/**
	 * The logger.
	 */
	private static final Logger logger = Logger.getLogger(SeniorHonoursProject_2020_2021.class.getName());
	
	/**
	 * Location of input data file: the exact CSV file as downloaded from the Gaia archive.
	 */
	private static final File data = new File("/home/nrowell/Projects/SeniorHonoursProjects/2020-2021/data/sample/100pc/shp_2020_2021-result.csv");
//	private static final File data = new File("/home/nrowell/Projects/SeniorHonoursProjects/2020-2021/data/sample/250pc/shp_2020_2021_250pc-result.csv");
	
	/**
	 * Location for outputs.
	 */
	private static final File outputDir = new File("/home/nrowell/Projects/SeniorHonoursProjects/2020-2021/results/");
	
	/**
	 * Adopted value for the Oort Constant A, in units of radians per year.
	 */
	static double A_RadYr = Galactic.A * Units.KM_PER_SEC_PER_KPC_TO_RAD_PER_YEAR;

	/**
	 * Adopted value for the Oort Constant B, in units of radians per year.
	 */
	static double B_RadYr = Galactic.B * Units.KM_PER_SEC_PER_KPC_TO_RAD_PER_YEAR;
	
	/**
	 * Distance threshold (to restrict to nearby stars) [parsec]
	 */
	static double d_max = 250.0;

	/**
	 * Tangential velocity threshold (to restrict to disk stars) [km/s]
	 */
	static double vtan_max = 150.0;
	static double vtan_min = 0.0;
	
	/**
	 * BP-RP binning parameters
	 */
	static double bprp_min = -0.7;
	static double bprp_max = 2.0;
	static double bprp_step = 0.0625;
	
	/**
	 * Absolute G magnitude binning parameters
	 */
	static double g_min = 4;
	static double g_max = 18;
	static double g_step = 0.125;

	static int g_steps = (int)Math.ceil((g_max - g_min) / g_step);
	static int bprp_steps = (int)Math.ceil((bprp_max - bprp_min) / bprp_step);
	
	
	// For consistency of plotting, use these colours for all UVW plots
	private static final String colour_U = "#1b9e77";
	private static final String colour_V = "#d95f02";
	private static final String colour_W = "#7570b3";
	
	// For consistency of plotting, use these point types for all UVW plots
	private static final int pt_U = 5;
	private static final int pt_V = 7;
	private static final int pt_W = 9;
	
	/**
	 * Threshold on RUWE [mas]
	 */
	static double ruweLimit = 1.4;
//	static double ruweLimit = 1.2;

	/**
	 * Thresholds on corrected flux excess [-]
	 */
	static double[] cStarLimits = {-0.1, 0.12};
//	static double[] cStarLimits = {-0.08, 0.1};
	
	/**
	 * Main application entry point.
	 * @param args
	 * 	The command line arguments [ignored]
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		// Load the Gaia sample
		Collection<GaiaSource_2020_2021> rawSample = GaiaSourceUtil.loadGaiaSources_2020_2021(data);
		
		// Compute various derived astrometric quantities for each star
		Collection<ExtendedGaiaSource> allStars = computePandA(rawSample);
		
		// Apply quality criteria, distance and tangential velocity cuts
		Collection<ExtendedGaiaSource> cleanStars = applyInitialSelections(allStars);
		
		// Display HR diagram
		displayHrDiagram(allStars);
		displayHrDiagram(cleanStars);
		
		// Apply WD selection in HRD
		Collection<ExtendedGaiaSource> wd = selectWd(cleanStars);
		
		displayHrDiagram(wd);
		
		logger.info("Got " + wd.size() + " WD stars.");
		
		// Bin according to (BP-RP,G) coordinate
		Map<Pair<Integer,Integer>, Collection<ExtendedGaiaSource>> binnedStars = bin2d(wd);
		
		int[] numBinnedWds = {0};
		binnedStars.forEach((k,v) -> {numBinnedWds[0] += v.size();});
		
		logger.info("Got " + numBinnedWds[0] + " binned WD stars for analysis.");
		
		// Display the stars projected on the sky
		displaySkyMap(binnedStars.values());
		
		// Plot and display distributions of various statistics for selected stars, for quality checking
//		List<Double> distances = new LinkedList<>();
//		for(int bin=0; bin<binnedStars.size(); bin++) {
//			for(ExtendedGaiaSource star : binnedStars.get(bin)) {
//				distances.add(star.d);
//			}
//		}
//		Gnuplot.displayImage(PlotUtil.plotHistogram(ArrayUtil.toArray(distances), 0, 500, 10, true, "Distance", "pc", 640, 480));
		
		// Mean 3D velocity (3x1) relative to the Sun, and the covariance matrix (symmetric 3x3) on that.
		Matrix[][] meanVelocities = new Matrix[bprp_steps * g_steps][2];
		
		// Scalar velocity dispersion and variance
		double[][] velocityDisp = new double[bprp_steps * g_steps][2];
		
		// Velocity dispersion tensor (symmetric 3x3) and the covariance matrix (symmetric 6x6) for the 6 independent elements
		Matrix[][] velocityDispTensor = new Matrix[bprp_steps * g_steps][2];
		
		for(int bprp_bin = 0; bprp_bin < bprp_steps; bprp_bin++) {
			for(int g_bin = 0; g_bin < g_steps; g_bin++) {
			
	        	Pair<Integer, Integer> binIdx = new Pair<>(bprp_bin, g_bin);
	        	
	        	if(!binnedStars.containsKey(binIdx)) {
	        		// No stars
	        		continue;
	        	}
	        	
	        	Collection<ExtendedGaiaSource> stars = binnedStars.get(binIdx);
	        	
	        	// Get 1D index
	        	int idx = bprp_bin * g_steps + g_bin;
	        	
	        	Matrix[] meanVelocity = ProperMotionDeprojection.computeMeanVelocity(stars);
				
				double[] disp = ProperMotionDeprojection.computeScalarVelocityDispersion(stars);
				
				Matrix[] velocityEllipsoid = ProperMotionDeprojection.computeTensorVelocityDispersion(stars);
				
				meanVelocities[idx] = meanVelocity;
				velocityDisp[idx] = disp;
				velocityDispTensor[idx] = velocityEllipsoid;
				
			}
		}
		
		// Plot the mean velocities and velocity dispersion as a function of BP-RP colour
		
		// TODO plot in 2D as a function of G and BP-RP
		plotMeanVelocities(meanVelocities, velocityDisp);
		
		// Compute 1D velocity dispersion etc as a function of G
		for(int g_bin = 0; g_bin < g_steps; g_bin++) {
			
			Set<ExtendedGaiaSource> stars = new HashSet<>();
			
			for(int bprp_bin = 0; bprp_bin < bprp_steps; bprp_bin++) {
			
			
	        	Pair<Integer, Integer> binIdx = new Pair<>(bprp_bin, g_bin);
	        	
	        	if(!binnedStars.containsKey(binIdx)) {
	        		// No stars
	        		continue;
	        	}
	        	
	        	stars.addAll(binnedStars.get(binIdx));
			}
			
			// Compute the average vector p
			Matrix meanP = new Matrix(3, 1);
			
			// For each object:
			for(AstrometricStar star : stars) {
				meanP.plusEquals(star.getP());
			}

			// Compute means
			int n = stars.size();
			meanP.timesEquals(1.0/n);
			
        	Matrix[] meanVelocity = ProperMotionDeprojection.computeMeanVelocity(stars);
			
			double[] disp = ProperMotionDeprojection.computeScalarVelocityDispersion(stars);
			
			double g = g_min + g_bin * g_step + g_step/2.0;
			
			double s2 = disp[0];
			double u = meanVelocity[0].get(0, 0);
			double v = meanVelocity[0].get(1, 0);
			double w = meanVelocity[0].get(2, 0);
			
			// Plot mean of vector p
//			double u = meanP.get(0, 0);
//			double v = meanP.get(1, 0);
//			double w = meanP.get(2, 0);
			
			System.out.println(g + "\t" + s2 + "\t" + u + "\t" + v + "\t" + w);
		}
		
		System.out.println("\n\n");
		
		
		
		
		
		
		
		
		
		
		
	}
	
	/**
	 * Plots the components of the mean velocity and velocity dispersion as a function of absolute magnitude.
	 * 
	 * @param meanVelocities
	 * 	Mean velocity in each magnitude bin.
	 * @param velocityDisp
	 * 	Velocity dispersion in each magnitude bin.
	 * @throws IOException 
	 */
	private static void plotMeanVelocities(Matrix[][] meanVelocities, double[][] velocityDisp) throws IOException {
		
		File plotDataTmp1 = File.createTempFile("plot", null);
		plotDataTmp1.deleteOnExit();
		
		BufferedWriter out = new BufferedWriter(new FileWriter(plotDataTmp1));
		
		for(int bprp_bin = 0; bprp_bin < bprp_steps; bprp_bin++) {
			for(int g_bin = 0; g_bin < g_steps; g_bin++) {
			
				double bprp = bprp_min + bprp_bin * bprp_step + (bprp_step/2.0);
				double g    = g_min + g_bin * g_step + (g_step/2.0);
				
	        	// Get 1D index
	        	int idx = bprp_bin * g_steps + g_bin;
	        	
	        	if(meanVelocities[idx][0] == null) {
	        		// Could not deproject proper motion
	        		out.write(bprp + "\t" + g + "\t-1000000.0\t-1000000.0\t-1000000\t-1000000\n");
	        		continue;
	        	}
	        	
	        	double v_u = -meanVelocities[idx][0].get(0, 0);
	        	double v_v = -meanVelocities[idx][0].get(1, 0);
	        	double v_w = -meanVelocities[idx][0].get(2, 0);
	        	
	        	double sig2_v_u = meanVelocities[idx][1].get(0, 0);
	        	double sig2_v_v = meanVelocities[idx][1].get(1, 1);
	        	double sig2_v_w = meanVelocities[idx][1].get(2, 2);
	        	
	        	double s2 = velocityDisp[idx][0];
	        	double sig2_s2 = velocityDisp[idx][1];
	        	
	        	out.write(bprp + "\t" + g + "\t" + v_u + "\t" + v_v + "\t" + v_w + "\t" + Math.sqrt(s2) + "\n");
	        	
			}
			out.newLine();
		}
		out.close();
		
		
		StringBuilder script = new StringBuilder();
		
		// <U>
		
		script.append("set terminal pngcairo enhanced color size 640,640").append(OSChecker.newline);
		
		script.append("set xlabel 'BP - RP [mag]'").append(OSChecker.newline);
		script.append("set xrange [-0.7:2]").append(OSChecker.newline);
		script.append("set xtics out nomirror").append(OSChecker.newline);
		script.append("set mxtics 2").append(OSChecker.newline);
		
		script.append("set ylabel 'M_G'").append(OSChecker.newline);
		script.append("set yrange [16.5:10]").append(OSChecker.newline);
		script.append("set ytics out nomirror").append(OSChecker.newline);
		script.append("set mytics 5").append(OSChecker.newline);
		
		script.append("set cbtics 10").append(OSChecker.newline);
		script.append("set cbrange [0:40]").append(OSChecker.newline);
		script.append("set cblabel '<U> [km s^{-1}]'").append(OSChecker.newline);
		
		script.append("set key off").append(OSChecker.newline);
		
		script.append("set size 0.9,1.0").append(OSChecker.newline);
		
		script.append("set style line 12 lc rgb '#ddccdd' lt 1 lw 1.5").append(OSChecker.newline);
		script.append("set style line 13 lc rgb '#ddccdd' lt 1 lw 0.5").append(OSChecker.newline);
		script.append("set grid xtics mxtics ytics mytics back ls 12, ls 13").append(OSChecker.newline);
		
		script.append("set multiplot").append(OSChecker.newline);
		script.append("set view map").append(OSChecker.newline);
		script.append("set pm3d map").append(OSChecker.newline);
		
		script.append("splot '"+plotDataTmp1.getAbsolutePath()+"' u 1:2:($3 < -10000 ? 1/0 : $3) notitle");
		
		try {
			Gnuplot.displayImage(Gnuplot.executeScript(script.toString()));
		} catch (IOException e) {
			System.out.println("Problem making plot");
		}
		

		// <V>
		
		script = new StringBuilder();
		
		script.append("set terminal pngcairo enhanced color size 640,640").append(OSChecker.newline);
		
		script.append("set xlabel 'BP - RP [mag]'").append(OSChecker.newline);
		script.append("set xrange [-0.7:2]").append(OSChecker.newline);
		script.append("set xtics out nomirror").append(OSChecker.newline);
		script.append("set mxtics 2").append(OSChecker.newline);
		
		script.append("set ylabel 'M_G'").append(OSChecker.newline);
		script.append("set yrange [16.5:10]").append(OSChecker.newline);
		script.append("set ytics out nomirror").append(OSChecker.newline);
		script.append("set mytics 5").append(OSChecker.newline);
		
		script.append("set cbtics 10").append(OSChecker.newline);
		script.append("set cbrange [0:50]").append(OSChecker.newline);
		script.append("set cblabel '<V> [km s^{-1}]'").append(OSChecker.newline);
		
		script.append("set key off").append(OSChecker.newline);
		
		script.append("set size 0.9,1.0").append(OSChecker.newline);
		
		script.append("set style line 12 lc rgb '#ddccdd' lt 1 lw 1.5").append(OSChecker.newline);
		script.append("set style line 13 lc rgb '#ddccdd' lt 1 lw 0.5").append(OSChecker.newline);
		script.append("set grid xtics mxtics ytics mytics back ls 12, ls 13").append(OSChecker.newline);
		
		script.append("set multiplot").append(OSChecker.newline);
		script.append("set view map").append(OSChecker.newline);
		script.append("set pm3d map").append(OSChecker.newline);
		
		script.append("splot '"+plotDataTmp1.getAbsolutePath()+"' u 1:2:($4 < -10000 ? 1/0 : $4) notitle");
		
		try {
			Gnuplot.displayImage(Gnuplot.executeScript(script.toString()));
		} catch (IOException e) {
			System.out.println("Problem making plot");
		}
		

		// <W>
		
		script = new StringBuilder();
		
		script.append("set terminal pngcairo enhanced color size 640,640").append(OSChecker.newline);
		
		script.append("set xlabel 'BP - RP [mag]'").append(OSChecker.newline);
		script.append("set xrange [-0.7:2]").append(OSChecker.newline);
		script.append("set xtics out nomirror").append(OSChecker.newline);
		script.append("set mxtics 2").append(OSChecker.newline);
		
		script.append("set ylabel 'M_G'").append(OSChecker.newline);
		script.append("set yrange [16.5:10]").append(OSChecker.newline);
		script.append("set ytics out nomirror").append(OSChecker.newline);
		script.append("set mytics 5").append(OSChecker.newline);
		
		script.append("set cbtics 10").append(OSChecker.newline);
		script.append("set cbrange [0:40]").append(OSChecker.newline);
		script.append("set cblabel '<W> [km s^{-1}]'").append(OSChecker.newline);
		
		script.append("set key off").append(OSChecker.newline);
		
		script.append("set size 0.9,1.0").append(OSChecker.newline);
		
		script.append("set style line 12 lc rgb '#ddccdd' lt 1 lw 1.5").append(OSChecker.newline);
		script.append("set style line 13 lc rgb '#ddccdd' lt 1 lw 0.5").append(OSChecker.newline);
		script.append("set grid xtics mxtics ytics mytics back ls 12, ls 13").append(OSChecker.newline);
		
		script.append("set multiplot").append(OSChecker.newline);
		script.append("set view map").append(OSChecker.newline);
		script.append("set pm3d map").append(OSChecker.newline);
		
		script.append("splot '"+plotDataTmp1.getAbsolutePath()+"' u 1:2:($5 < -10000 ? 1/0 : $5) notitle");
		
		try {
			Gnuplot.displayImage(Gnuplot.executeScript(script.toString()));
		} catch (IOException e) {
			System.out.println("Problem making plot");
		}
		
		// S^2
		
		script = new StringBuilder();
		
		script.append("set terminal pngcairo enhanced color size 640,640").append(OSChecker.newline);
		
		script.append("set xlabel 'BP - RP [mag]'").append(OSChecker.newline);
		script.append("set xrange [-0.7:2]").append(OSChecker.newline);
		script.append("set xtics out nomirror").append(OSChecker.newline);
		script.append("set mxtics 2").append(OSChecker.newline);
		
		script.append("set ylabel 'M_G'").append(OSChecker.newline);
		script.append("set yrange [16.5:10]").append(OSChecker.newline);
		script.append("set ytics out nomirror").append(OSChecker.newline);
		script.append("set mytics 5").append(OSChecker.newline);
		
		script.append("set cbtics 10").append(OSChecker.newline);
		script.append("set cbrange [0:60]").append(OSChecker.newline);
		script.append("set cblabel 'Velocity dispersion [km s^{-1}]'").append(OSChecker.newline);
		
		script.append("set key off").append(OSChecker.newline);
		
		script.append("set size 0.9,1.0").append(OSChecker.newline);
		
		script.append("set style line 12 lc rgb '#ddccdd' lt 1 lw 1.5").append(OSChecker.newline);
		script.append("set style line 13 lc rgb '#ddccdd' lt 1 lw 0.5").append(OSChecker.newline);
		script.append("set grid xtics mxtics ytics mytics back ls 12, ls 13").append(OSChecker.newline);
		
		script.append("set multiplot").append(OSChecker.newline);
		script.append("set view map").append(OSChecker.newline);
		script.append("set pm3d map").append(OSChecker.newline);
		
		script.append("splot '"+plotDataTmp1.getAbsolutePath()+"' u 1:2:($6 < 0 ? 1/0 : $6) notitle");
		
		try {
			Gnuplot.displayImage(Gnuplot.executeScript(script.toString()));
		} catch (IOException e) {
			System.out.println("Problem making plot");
		}
		
		
		
		
		
		
		
		
		
		
		
		
	}
	
	/**
	 * Applies quality criteria, distance and tangential velocity thresholds, leaving only well-measured Solar neighbourhood
	 * disk stars.
	 * 
	 * @param allStars
	 * 	The full set of stars. On exit this will contain only well-measured Solar neighbourhood disk stars.
	 */
	private static Collection<ExtendedGaiaSource> applyInitialSelections(Collection<ExtendedGaiaSource> allStars) {
		
		// Count number of stars that fail each quality threshold
		int[] count = new int[4];
		
		// Stars that failed quality criteria; leaving only good MS and WDs
		List<ExtendedGaiaSource> accepted = new LinkedList<>();
		
		for(ExtendedGaiaSource star : allStars) {
			
			// BP-RP colour index
			double bpRp = star.phot_bp_mean_mag - star.phot_rp_mean_mag;
			
			// Apply quality criteria
			boolean reject = false;
			
			// Filtering on astrometric quality:
			
			if(star.ruwe > ruweLimit) {
				reject = true;
				count[0]++;
			}
			
			// Compute the corrected flux excess factor
			double f = 0.0;
			if(bpRp < 0.5) {
				f = 1.154360 + 0.033772 * bpRp + 0.032277 * bpRp * bpRp;
			}
			else if(bpRp < 4.0) {
				f = 1.162004 + 0.011464 * bpRp + 0.049255 * bpRp * bpRp - 0.005879 * bpRp * bpRp * bpRp;
			}
			else {
				f = 1.057572 + 0.140537 * bpRp;
			}
			
			double cStar = star.phot_bp_rp_excess_factor - f;
			
			if(cStar < cStarLimits[0] || cStar > cStarLimits[1]) {
				reject = true;
				count[1]++;
			}
			
			// Distance selection
			if(star.d > d_max) {
				reject = true;
				count[2]++;
			}
			// Tangential velocity selection
			double vtan = star.p.normF();
			
			if(vtan > vtan_max || vtan < vtan_min) {
				reject = true;
				count[3]++;
			}
			
			if(!reject) {
				accepted.add(star);
			}
			
		}
		
		int rejected = allStars.size() - accepted.size();
		
		logger.info("Purged "+rejected+" objects; retained "+accepted.size()+" well-measured Solar neighbourhood disk stars for analysis");
		logger.info("Failed RUWE threshold:              " + count[0]);
		logger.info("Failed BP-RP flux excess threshold: " + count[1]);
		logger.info("Failed distance threshold:          " + count[2]);
		logger.info("Failed vtan threshold:              " + count[3]);
		
		return accepted;
	}
	
	/**
	 * Apply WD selection in the HRD.
	 * 
	 * @param allStars
	 * 	The set of {@link ExtendedGaiaSource}s.
	 * @return
	 * 	A {@link List<ExtendedGaiaSource>} containing the selected WDs.
	 */
	private static Collection<ExtendedGaiaSource> selectWd(Collection<ExtendedGaiaSource> allStars) {
		
		// Count number of stars that fail each threshold
		int[] count = new int[1];
		
		List<ExtendedGaiaSource> wd = new LinkedList<>();
		
		for(ExtendedGaiaSource star : allStars) {
			
			// Absolute G magnitude
			double gMag = PhotometryUtils.getAbsoluteMagnitudeFromPi(star.parallax/1000.0, star.phot_g_mean_mag);
			
			// BP-RP colour index
			double bpRp = star.phot_bp_mean_mag - star.phot_rp_mean_mag;
			
			// Apply quality criteria
			boolean reject = false;
			
			// TODO Rough selection in HRD
			double fiducial = 2.5 * bpRp + 8.25;
			
			if(gMag < fiducial) {
				count[0]++;
				reject = true;
			}
			
			if(!reject) {
				wd.add(star);
			}
		}

		logger.info("Retained "+wd.size()+" WD stars for analysis");
		logger.info("Failed HRD selection:      " + count[0]);
		
		return wd;
	}
	
	/**
	 * Bins the {@link Collection} of {@link ExtendedGaiaSource} in 2D according to the location in the (BP-RP,G) space.
	 * Each bin is indexed by a {@link Pair} of integers that individually index the BP-RP and G dimensions.
	 * 
	 * @param wds
	 * 	The {@link Collection} of {@link ExtendedGaiaSource} to bin.
	 * @return
	 * 	A {@link Map} containing the binned {@link ExtendedGaiaSource}s.
	 */
	private static Map<Pair<Integer,Integer>, Collection<ExtendedGaiaSource>> bin2d(Collection<ExtendedGaiaSource> wds) {
		
		Map<Pair<Integer,Integer>, Collection<ExtendedGaiaSource>> data = new HashMap<>();
		
		for(ExtendedGaiaSource wd : wds) {
			
			// Get indices of the bins in each dimension

        	int g_bin  = (int)Math.floor((wd.absG - g_min) / g_step);
        	int bprp_bin  = (int)Math.floor((wd.bprp - bprp_min) / bprp_step);
			
        	if(g_bin < 0 || g_bin >= g_steps || bprp_bin < 0 || bprp_bin >= bprp_steps) {
        		continue;
        	}
        	
        	Pair<Integer, Integer> binIdx = new Pair<>(bprp_bin, g_bin);
        	
        	if(!data.containsKey(binIdx)) {
        		data.put(binIdx, new HashSet<>());
        	}
        	
        	data.get(binIdx).add(wd);
		}
		
		return data;
	}
	
	
	/**
	 * Promote each {@link GaiaSource_2020_2021} to a {@link ExtendedGaiaSource}.
	 * 
	 * @param stars
	 * 	A {@link Collection} of all the {@link GaiaSource_2020_2021}s.
	 * @return
	 * 	A {@link Collection} of {@link ExtendedGaiaSource}.
	 */
	private static Collection<ExtendedGaiaSource> computePandA(Collection<GaiaSource_2020_2021> stars) {
		
		Collection<ExtendedGaiaSource> starsWithVelocity = new LinkedList<>();
		
		for(GaiaSource_2020_2021 star : stars) {
			starsWithVelocity.add( new ExtendedGaiaSource(star));
		}
		
		return starsWithVelocity;
	}
	
	/**
	 * Displays an all-sky map of the star positions.
	 * @param binnedStars
	 * 	A {@link RangeMap} containing all of the {@link GaiaSource_2020_2021}s to plot.
	 */
	private static void displaySkyMap(Collection<Collection<ExtendedGaiaSource>> binnedStars) {
		
		List<double[]> points = new LinkedList<>();
		
		for(Collection<ExtendedGaiaSource> stars : binnedStars) {
			for(ExtendedGaiaSource star : stars) {
				// Convert coordinates to radians
				double ra  = Math.toRadians(star.ra);
				double dec = Math.toRadians(star.dec);
				// Rotate equatorial coordinates to Galactic coordinates
				points.add(AstrometryUtils.convertPositionEqToGal(ra, dec));
			}
		}
		
//		ProjectionUtil.makeAndDisplayJFreeChartPlot(points, "Sky distribution of selected DR2 sources", new Aitoff());
		ProjectionUtil.makeAndDisplayJFreeChartPlot(points, null, new Aitoff());
	}
	
	/**
	 * Create and display an HR diagram using the loaded data.
	 * 
	 * @param allStars
	 * 	All stars.
	 * @throws IOException 
	 */
	private static void displayHrDiagram(Collection<ExtendedGaiaSource> allStars) throws IOException {
		
		// Bin config in magnitude & colour space
		double magMin = -4.0;
		double magMax = 17.0;
		double magStep = 0.15;
		int magSteps = (int)Math.ceil((magMax - magMin)/magStep);
		
		double bpRpMin = -0.6;
		double bpRpMax = 4.9;
		double bpRpStep = 0.05;
		int bpRpSteps = (int)Math.ceil((bpRpMax - bpRpMin)/bpRpStep);
		
		int[][] density = new int[magSteps][bpRpSteps];
		
		File plotDataTmp1 = File.createTempFile("plot", null);
		plotDataTmp1.deleteOnExit();
		File plotDataTmp2 = File.createTempFile("plot", null);
		plotDataTmp2.deleteOnExit();
		
		BufferedWriter out = new BufferedWriter(new FileWriter(plotDataTmp1));
		
		for(GaiaSource_2020_2021 star : allStars) {
			
			// Absolute G magnitude
			double gMag = PhotometryUtils.getAbsoluteMagnitudeFromPi(star.parallax/1000.0, star.phot_g_mean_mag);
			
			// BP - RP
			double bp_rp = star.phot_bp_mean_mag - star.phot_rp_mean_mag;
			
			int gMagBin = (int)Math.floor((gMag - magMin) / magStep);
	        int bpRpBin = (int)Math.floor((bp_rp - bpRpMin) / bpRpStep);
	        
	        if(gMagBin >= 0 && gMagBin < magSteps && bpRpBin >= 0 && bpRpBin < bpRpSteps) {
	        	density[gMagBin][bpRpBin]++;
	        }
	        
	        out.write(bp_rp + " " + gMag + "\n");
		}
		out.close();
		
		out = new BufferedWriter(new FileWriter(plotDataTmp2));
		
		for(int i=0; i<magSteps; i++) {
			double mag = magMin + i*magStep + magStep/2.0;
			for(int j=0; j<bpRpSteps; j++) {
				double bpRp = bpRpMin + j*bpRpStep + bpRpStep/2.0;
				out.write(bpRp + " " + mag + " " + (density[i][j]/(magStep*bpRpStep)) + "\n");
			}
			out.write("\n");
		}
		out.close();
		
		StringBuilder script = new StringBuilder();
//		script.append("set terminal pngcairo enhanced color size 540,640").append(OSChecker.newline);
		// XXX
		script.append("set terminal pngcairo enhanced color size 640,640").append(OSChecker.newline);
		
		
		
		script.append("set xlabel 'G_{BP}-G_{RP}'").append(OSChecker.newline);
		
		
//		script.append("set xrange [-0.7:5]").append(OSChecker.newline);
		script.append("set xrange [-0.7:2]").append(OSChecker.newline);
		
		
		
		script.append("set xtics out nomirror").append(OSChecker.newline);
		script.append("set mxtics 2").append(OSChecker.newline);
		
		script.append("set ylabel 'M_G'").append(OSChecker.newline);
		
		
		
//		script.append("set yrange [17:-4]").append(OSChecker.newline);
		script.append("set yrange [16.5:10]").append(OSChecker.newline);
		
		
		
		script.append("set ytics out nomirror").append(OSChecker.newline);
		script.append("set mytics 5").append(OSChecker.newline);
		
		script.append("set cbtics 1").append(OSChecker.newline);
		script.append("set cblabel 'Log N [mag^{-2}]'").append(OSChecker.newline);
		
		script.append("set key off").append(OSChecker.newline);
		// XXX
		script.append("set key top right").append(OSChecker.newline);
		
		
		
		script.append("min(x,y) = (x < y) ? x : y").append(OSChecker.newline);
		script.append("max(x,y) = (x > y) ? x : y").append(OSChecker.newline);
		
		// Clever WD selection line
//		script.append("f(x) = (x <= 1.75) ? max(max(5, 5.93 + 5.047*x), 6*x**3 - 21.77*x**2 + 27.91*x + 0.897) : 1/0").append(OSChecker.newline);
		
		// Simple WD selection line
		script.append("f(x) = 2.5 * x + 8.25").append(OSChecker.newline);
		
		script.append("set size 0.9,1.0").append(OSChecker.newline);
		
		script.append("set style line 12 lc rgb '#ddccdd' lt 1 lw 1.5").append(OSChecker.newline);
		script.append("set style line 13 lc rgb '#ddccdd' lt 1 lw 0.5").append(OSChecker.newline);
		script.append("set grid xtics mxtics ytics mytics back ls 12, ls 13").append(OSChecker.newline);
		
		script.append("set multiplot").append(OSChecker.newline);
		script.append("set view map").append(OSChecker.newline);
		
		script.append("splot '"+plotDataTmp1.getAbsolutePath()+"' u 1:2:(1.0) w d lc rgbcolor 'black' notitle").append(OSChecker.newline);
		
		script.append("set pm3d map").append(OSChecker.newline);
		script.append("unset grid").append(OSChecker.newline);
		
		// Part of clever WD selection line
//		script.append("set arrow from 1.7,17 to 1.7,14.9067 nohead front lw 2 lc rgb 'red'").append(OSChecker.newline);
		
		script.append("splot '"+plotDataTmp1.getAbsolutePath()+"' u 1:2:(1.0) w d lc rgbcolor 'black' notitle").append(OSChecker.newline);
		script.append("splot '"+plotDataTmp2.getAbsolutePath()+"' u 1:2:(($3 * "+(magStep*bpRpStep)+") < 5 ? 1/0 : log10($3)) notitle,");
		
		// XXX
//		script.append("      '+' u 1:(f($1)):(1.0) w l lw 2 lc rgb 'red' notitle,");
		
		script.append("      '-' u 1:2:(1.0) w l lw 2 lc rgb 'blue' title '0.6 M_{"+CharUtil.solar+"}',");
		script.append("      '-' u 1:2:(1.0) w p pt 7 ps 1.0 lc rgb 'blue' notitle,");
		script.append("      '-' u 1:2:(1.0) w l lw 2 lc rgb 'green' title '1.0 M_{"+CharUtil.solar+"}',");
		script.append("      '-' u 1:2:(1.0) w p pt 7 ps 1.0 lc rgb 'green' notitle").append(OSChecker.newline);
		
		// TODO: overplot WD cooling tracks
		WdCoolingModelSet wdCoolingModelSet = WdCoolingModels.MONTREAL_NEW_2020.getWdCoolingModels();
		for(double tCool = 0; tCool < 10e9; tCool += 0.01e9) {
			
			double g = wdCoolingModelSet.quantity(tCool, 0.6, WdAtmosphereType.H, Filter.G_DR3);
			double bp = wdCoolingModelSet.quantity(tCool, 0.6, WdAtmosphereType.H, Filter.BP_DR3);
			double rp = wdCoolingModelSet.quantity(tCool, 0.6, WdAtmosphereType.H, Filter.RP_DR3);
			script.append((bp-rp) + "\t" + g).append(OSChecker.newline);
		}
		script.append("e").append(OSChecker.newline);
		for(double tCool = 0; tCool <= 10e9; tCool += 1e9) {
			
			double g = wdCoolingModelSet.quantity(tCool, 0.6, WdAtmosphereType.H, Filter.G_DR3);
			double bp = wdCoolingModelSet.quantity(tCool, 0.6, WdAtmosphereType.H, Filter.BP_DR3);
			double rp = wdCoolingModelSet.quantity(tCool, 0.6, WdAtmosphereType.H, Filter.RP_DR3);
			script.append((bp-rp) + "\t" + g).append(OSChecker.newline);
		}
		script.append("e").append(OSChecker.newline);
		
		for(double tCool = 0; tCool < 10e9; tCool += 0.01e9) {
			
			double g = wdCoolingModelSet.quantity(tCool, 1.0, WdAtmosphereType.H, Filter.G_DR3);
			double bp = wdCoolingModelSet.quantity(tCool, 1.0, WdAtmosphereType.H, Filter.BP_DR3);
			double rp = wdCoolingModelSet.quantity(tCool, 1.0, WdAtmosphereType.H, Filter.RP_DR3);
			script.append((bp-rp) + "\t" + g).append(OSChecker.newline);
		}
		script.append("e").append(OSChecker.newline);
		
		for(double tCool = 0; tCool <= 10e9; tCool += 1e9) {
			
			double g = wdCoolingModelSet.quantity(tCool, 1.0, WdAtmosphereType.H, Filter.G_DR3);
			double bp = wdCoolingModelSet.quantity(tCool, 1.0, WdAtmosphereType.H, Filter.BP_DR3);
			double rp = wdCoolingModelSet.quantity(tCool, 1.0, WdAtmosphereType.H, Filter.RP_DR3);
			script.append((bp-rp) + "\t" + g).append(OSChecker.newline);
		}
		script.append("e").append(OSChecker.newline);
		
		
		
		
		
		try {
			Gnuplot.displayImage(Gnuplot.executeScript(script.toString()));
		} catch (IOException e) {
			System.out.println("Problem making HR diagram");
		}
	}
	

	/**
	 * Local class used to endow the {@link GaiaSource_2020_2021} with {@link Matrix}s to store
	 * the proper motion velocity vector and projection matrices etc, for convenience.
	 *
	 * @author nrowell
	 * @version $Id$
	 */
	static class ExtendedGaiaSource extends GaiaSource_2020_2021 implements AstrometricStar {
		
		public ExtendedGaiaSource(GaiaSource_2020_2021 star) {
			
			super(star);
			
			// Get the distance to the star [parsecs]
			d = 1000.0 / star.parallax;
			
			// Absolute G magnitude
			absG = PhotometryUtils.getAbsoluteMagnitudeFromPi(star.parallax/1000.0, star.phot_g_mean_mag);
			
			bprp = star.phot_bp_mean_mag - star.phot_rp_mean_mag;
			
			// Retrieve some fields for convenience
			double ra  = Math.toRadians(star.ra);
			double dec = Math.toRadians(star.dec);
			double mu_acosd = star.pmra * Units.MILLIARCSEC_TO_RADIANS;
			double mu_d = star.pmdec * Units.MILLIARCSEC_TO_RADIANS;
			
			// 1) Convert proper motion to Galactic coordinates
			double[] mu_lb = AstrometryUtils.convertPositionAndProperMotionEqToGal(ra, dec, mu_acosd, mu_d);
			
			l = mu_lb[0];
			b = mu_lb[1];
			mu_lcosb = mu_lb[2];
			mu_b = mu_lb[3];
			
			// XXX Check values that student gets
			Set<Long> srcIdSet = new TreeSet<>(Arrays.asList(new Long[]{
					4004185576130620288L}));
//			Set<Long> srcIdSet = new TreeSet<>();

			if(srcIdSet.contains(star.sourceId)) {
				System.out.println("\nFound source " + star.sourceId);
				System.out.println("ra       = " + star.ra);
				System.out.println("dec      = " + star.dec);
				System.out.println("pmra     = " + star.pmra);
				System.out.println("pmdec    = " + star.pmdec);

				Matrix r = AstrometryUtils.sphericalPolarToCartesian(1.0, l, b);
				System.out.println("r_hat = " + r.get(0, 0) + " , " + r.get(1, 0) + " , " + r.get(2, 0));
				System.out.println("l        = " + Math.toDegrees(l));
				System.out.println("b        = " + Math.toDegrees(b));
				System.out.println("mu_lcosb (pre) = " + mu_lcosb / Units.MILLIARCSEC_TO_RADIANS);
				System.out.println("mu_b (pre)     = " + mu_b / Units.MILLIARCSEC_TO_RADIANS);
			}
			
			// 2) Subtract off the contribution from Galactic rotation, to leave the peculiar velocity [adopt Oort constants]
			//      NOTE we include the cos(b) term in the mu_lcosb correction, which differs from equation (1) in Dehnen & Binney (1998)
			//      as they are correcting proper motion that does not include the cos(b) term, i.e. just mu_l.
			mu_lcosb = mu_lcosb - (A_RadYr * Math.cos(2 * l) + B_RadYr) * Math.cos(b);
			mu_b = mu_b + A_RadYr * Math.sin(2 * l) * Math.cos(b) * Math.sin(b);
			
			// 3) Convert to proper motion velocity vector
			p = AstrometryUtils.getTangentialVelocityVector(d, l, b, mu_lcosb, mu_b);
			
			// 4) Compute the projection matrix A along the line of sight towards this star
			A = AstrometryUtils.getProjectionMatrixA(l, b);
			
			// This will be computed later
			pPrime = new Matrix(3,1);
			
			// XXX Check values that student gets
			if(srcIdSet.contains(star.sourceId)) {
				System.out.println("mu_lcosb (post) = " + mu_lcosb / Units.MILLIARCSEC_TO_RADIANS);
				System.out.println("mu_b (post)     = " + mu_b / Units.MILLIARCSEC_TO_RADIANS);
				
				System.out.println("p:");
				p.print(5, 5);
				System.out.println("A:");
				A.print(5, 5);
			}
			
			
		}
		
		/**
		 * The Galactic longitude [radians]
		 */
		public double l;
		
		/**
		 * The Galactic latitude [radians]
		 */
		public double b;
		
		/**
		 * The proper motion in Galactic longitude, including cosine factor [radians/yr]
		 */
		public double mu_lcosb;
		
		/**
		 * The proper motion in Galactic latitude [radians/yr]
		 */
		public double mu_b;
		
		/**
		 * The distance to the star [parsecs]
		 */
		public double d;
		
		/**
		 * Absolute G magnitude.
		 */
		public double absG;

		/**
		 * BP-RP colour.
		 */
		public double bprp;
		
		/**
		 * The proper motion velocity vector (i.e. tangential velocity).
		 */
		public Matrix p;
		
		/**
		 * The proper motion velocity vector (i.e. tangential velocity) minus the mean motion, i.e.
		 * the peculiar motion for this star from which the velocity dispersion is measured.
		 */
		public Matrix pPrime;
		
		/**
		 * The projection matrix A that projects the 3D velocity onto the celestial sphere.
		 */
		public Matrix A;

		@Override
		public double getLong() {
			return l;
		}

		@Override
		public double getLat() {
			return b;
		}

		@Override
		public double getMuLCosB() {
			return mu_lcosb;
		}

		@Override
		public double getMuB() {
			return mu_b;
		}

		@Override
		public double getDistance() {
			return d;
		}

		@Override
		public Matrix getP() {
			return p;
		}

		@Override
		public void setP(Matrix p) {
			this.p = p;
		}

		@Override
		public Matrix getPPrime() {
			return pPrime;
		}

		@Override
		public void setPPrime(Matrix pPrime) {
			this.pPrime = pPrime;
		}

		@Override
		public Matrix getA() {
			return A;
		}

		@Override
		public void setA(Matrix a) {
			this.A = a;
		}
		
		@Override
		public Matrix getU() {
			
			// Compute the vector u for this star
			double[][] u = new double[6][1];
			
			// Loop over mixed products of the peculiar velocity components
			int n=3;
			for(int i=0; i<n; i++) {
				for(int k=i; k<n; k++) {
					
					// Index into 1D array
					int t = (n*(n-1)/2) - (n-i-1)*(n-i)/2 + k;
					
					u[t][0] += pPrime.get(i, 0) * pPrime.get(k, 0);
				}
			}
			
			return new Matrix(u);
		}
		
		@Override
		public Matrix getB() {
			
			// Compute the matrix B for this star
			double[][] b = new double[6][6];
			
			// Loop over mixed products of the projection matrix components
			int n=3;
			for(int i=0; i<n; i++) {
				for(int k=i; k<n; k++) {
					
					// Index into 1D array
					int t = (n*(n-1)/2) - (n-i-1)*(n-i)/2 + k;
					
					for(int j=0; j<n; j++) {
						for(int l=j; l<n; l++) {
							
							// Second index into 2D array
							int v = (n*(n-1)/2) - (n-j-1)*(n-j)/2 + l;
							
							b[t][v] += (A.get(i, j) * A.get(k, l) + A.get(k, j) * A.get(i, l)) / 2;
						}
					}
				}
			}
			
			return new Matrix(b);
		}
	}
	
}
