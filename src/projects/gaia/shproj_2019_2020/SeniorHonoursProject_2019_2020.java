package projects.gaia.shproj_2019_2020;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

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
import photometry.util.PhotometryUtils;
import projections.Aitoff;
import projections.util.ProjectionUtil;
import projects.gaia.shproj_2019_2020.dm.GaiaSource;
import projects.gaia.shproj_2019_2020.util.GaiaSourceUtil;
import util.ArrayUtil;
import utils.PlotUtil;

/**
 * 
 *
 * @author nrowell
 * @version $Id$
 */
public class SeniorHonoursProject_2019_2020 {

	/**
	 * The logger.
	 */
	private static final Logger logger = Logger.getLogger(SeniorHonoursProject_2019_2020.class.getName());
	
	/**
	 * Location of input data file: the exact CSV file as downloaded from the Gaia archive.
	 */
	private static final File data = new File("/home/nrowell/Projects/SeniorHonoursProjects/2019-2020/data/sample/100pc/all_good_stars_within_100pc.csv");
	
	/**
	 * Location for outputs.
	 */
	private static final File outputDir = new File("/home/nrowell/Projects/SeniorHonoursProjects/2019-2020/results/");
	
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
	static double d_max = 100.0;

	/**
	 * Tangential velocity threshold (to restrict to disk stars) [km/s]
	 */
	static double vtan_max = 150.0;
	static double vtan_min = 0.0;
	
	/**
	 * BP-RP binning parameters
	 */
	static double bprp_min = 0.0;
	static double bprp_max = 4.0;
	
	/**
	 * Magnitude bin step size for CONSTANT_BPRP_STEP
	 */
	static double bprp_step = 0.125;
	
	/**
	 * Number of stars per bin for CONSTANT_STARS
	 */
	static int nStarsPerBin = 2000;
	
	/**
	 * Enumerates the types of binning available.
	 */
	static enum BIN_SCHEME {CONSTANT_BPRP_STEP, CONSTANT_STARS};
	
	/**
	 * The chosen type of binning.
	 */
//	static BIN_SCHEME binScheme = BIN_SCHEME.CONSTANT_STARS;
	static BIN_SCHEME binScheme = BIN_SCHEME.CONSTANT_BPRP_STEP;
	
	// For consistency of plotting, use these colours for all UVW plots
	private static final String colour_U = "#1b9e77";
	private static final String colour_V = "#d95f02";
	private static final String colour_W = "#7570b3";
	
	// For consistency of plotting, use these point types for all UVW plots
	private static final int pt_U = 5;
	private static final int pt_V = 7;
	private static final int pt_W = 9;
	
	/**
	 * Lower limit on BP-RP colour to use in the determination of LSR motion etc. This marks the point brighter than
	 * which there is no correlation between the colour and the average age of main sequence stars.
	 */
	static double bprp_ageCorrelation_min = 2.0;
	
	/**
	 * Upper limit on BP-RP colour to use in the determination of LSR motion etc. Points fainter than this are judged
	 * to be too noisy to include.
	 */
	static double bprp_ageCorrelation_max = 4.0;
	
	/**
	 * Threshold on astrometric excess noise [mas]
	 */
	static double excessNoiseLimit = 2.5;
	
	/**
	 * Main application entry point.
	 * @param args
	 * 	The command line arguments [ignored]
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		// Load the Gaia sample
		Collection<GaiaSource> rawSample = GaiaSourceUtil.loadGaiaSources(data);
		
		// Compute various derived astrometric quantities for each star
		Collection<ExtendedGaiaSource> allStars = computePandA(rawSample);
		
		// XXX
		System.exit(0);
		
		// Apply quality criteria, distance and tangential velocity cuts
		Collection<ExtendedGaiaSource> cleanStars = applyInitialSelections(allStars);
		
		// Display HR diagram
		displayHrDiagram(cleanStars);
		
		// Apply WD selection in HRD
		Collection<ExtendedGaiaSource> wds = selectWds(cleanStars);
		
		logger.info("Got " + wds.size() + " WDs.");
		
		// Bin according to BP-RP
		// TODO - switch binning from G to BP-RP
		RangeMap<ExtendedGaiaSource> magBinnedWds = binByAbsG(wds);
		
		logger.info("Got " + magBinnedWds.numberOfObjects() + " binned WDs for analysis.");
		
		// Display the stars projected on the sky
		displaySkyMap(magBinnedWds);
		
		// Plot and display distributions of various statistics for selected stars, for quality checking
		List<Double> distances = new LinkedList<>();
		for(int bin=0; bin<magBinnedWds.size(); bin++) {
			for(ExtendedGaiaSource star : magBinnedWds.get(bin)) {
				distances.add(star.d);
			}
		}
		Gnuplot.displayImage(PlotUtil.plotHistogram(ArrayUtil.toArray(distances), 0, 500, 10, true, "Distance", "pc", 640, 480));
		
		// Plot the colour distribution for each magnitude bin
		for(int bin=0; bin<magBinnedWds.size(); bin++) {
			
			double mag = magBinnedWds.getRange(bin).mid();
			
			BufferedImage plot = PlotUtil.plotCdf(magBinnedWds.get(bin), (a) -> a.phot_bp_mean_mag - a.phot_rp_mean_mag, 
					-0.5, 2.0, 0.05, true, false, "G_{BP} - G_{RP}", "mag", 640, 480);
			
			ImageIO.write(plot, "png", new File(outputDir, "figures/cdf/"+String.format("%.5f.png", mag)));
		}
		
		// Now compute the various quantities for the proper motion deprojection
		int n = magBinnedWds.size();
		
		// Mean 3D velocity (3x1) relative to the Sun, and the covariance matrix (symmetric 3x3) on that.
		Matrix[][] meanVelocities = new Matrix[n][2];
		
		// Scalar velocity dispersion and variance
		double[][] velocityDisp = new double[n][2];
		
		// Velocity dispersion tensor (symmetric 3x3) and the covariance matrix (symmetric 6x6) for the 6 independent elements
		Matrix[][] velocityDispTensor = new Matrix[n][2];
		
		for(int i=0; i<n; i++) {
			
			Collection<ExtendedGaiaSource> stars = magBinnedWds.get(i);
			
			Matrix[] meanVelocity = ProperMotionDeprojection.computeMeanVelocity(stars);
			
			double[] disp = ProperMotionDeprojection.computeScalarVelocityDispersion(stars);
			
			Matrix[] velocityEllipsoid = ProperMotionDeprojection.computeTensorVelocityDispersion(stars);
			
			meanVelocities[i] = meanVelocity;
			velocityDisp[i] = disp;
			velocityDispTensor[i] = velocityEllipsoid;
		}
		
		// Compute the solar motion with respect to the Local Standard of Rest
		Polynomial[] polys = new Polynomial[3];
		
		// Extract the datapoints
		int nPoints = 0;
		
		for(int i=0; i<magBinnedWds.size(); i++) {
			
			double mag = magBinnedWds.getRange(i).mid();
			
			// Skip ranges of G that don't show age/G correlation
			if(mag < bprp_ageCorrelation_min || mag > bprp_ageCorrelation_max) {
				continue;
			}
			
			// Skip ranges with too few stars
			if(magBinnedWds.get(i).size() < 2 || velocityDisp[i][0] == Double.NaN) {
				continue;
			}
			
			nPoints++;
		}
		
		for(int comp=0; comp<3; comp++) {
		
			double[] x = new double[nPoints];
			double[] y = new double[nPoints];
			double[] yerrs = new double[nPoints];
			int idx = 0;
			for(int i=0; i<magBinnedWds.size(); i++) {
				
				double mag = magBinnedWds.getRange(i).mid();
				
				// Skip ranges of G that don't show age/G correlation
				if(mag < bprp_ageCorrelation_min || mag > bprp_ageCorrelation_max) {
					continue;
				}

				// Skip ranges with too few stars
				if(magBinnedWds.get(i).size() < 2 || velocityDisp[i][0] == Double.NaN) {
					continue;
				}
				
				
				x[idx] = velocityDisp[i][0];
				y[idx] = -meanVelocities[i][0].get(comp, 0);
				yerrs[idx] = Math.sqrt(meanVelocities[i][1].get(comp, comp));
				idx++;
			}
			
			// Fit a straight line to the mean velocity as a function of velocity dispersion
			polys[comp] = PolynomialFitting.fitPoly(1, x, y, yerrs);
		}
		
		// Print a Latex formatted table presenting the data for each magnitude bin
		printDataTable(magBinnedWds, meanVelocities, velocityDispTensor);
		
		// Plot the mean velocities and velocity dispersion as a function of absolute G magnitude
		plotMeanVelocities(magBinnedWds.getRanges(), meanVelocities, velocityDisp);
		
		// Plot the mean velocity as a function of velocity dispersion for LSR motion
		plotVelocityVsS2(magBinnedWds, meanVelocities, velocityDisp, polys);

		// Plot of the velocity dispersion as a function of magnitude
		plotVelocityDispersion(magBinnedWds.getRanges(), velocityDispTensor);
		
		// Plot of the vertex deviation as a function of magnitude
		plotVertexDeviation(magBinnedWds.getRanges(), velocityDispTensor);
		
		// Plot of the UV-plane velocity dispersion as a function of G
		plotVelocityEllipsoidProjections(magBinnedWds.getRanges(), velocityDispTensor);
	}
	
	/**
	 * Print a Latex formatted table presenting the results.
	 * 
	 * @param magBinnedWds
	 * 	The magnitude bined WDs.
	 * @param meanVelocities
	 * 	The mean velocity in each magnitude bin.
	 * @param velocityDispTensor
	 * 	The UVW Velocity dispersion tensor for each magnitude bin.
	 */
	private static void printDataTable(RangeMap<ExtendedGaiaSource> magBinnedWds, Matrix[][] meanVelocities, Matrix[][] velocityDispTensor) {
		
		List<String> source = new LinkedList<>();
		
		source.add("\\begin{landscape}");
		source.add("\\begin{table*}");
		source.add("\\label{tab:covar}");
		source.add("\\begin{tabular}{|c|cccccc|ccccccccccccccccccccc|}");
		source.add("\\hline");
		source.add(" Bin centre & \\multicolumn{6}{|c|}{Covariance on Mean Velocity [km$^2$ s$^{-2}$]} & \\multicolumn{21}{|c|}{Covariance on Velocity dispersion [km$^4$ s$^{-4}$]} \\\\");
		
		source.add(" G [mag] & $\\sigma^2_{U}$ & $\\sigma^2_{V}$ & $\\sigma^2_{W}$ & $\\sigma_{UV}$ & $\\sigma_{UW}$ & $\\sigma_{VW}$ & "
				+ "$\\sigma^2_{UU}$ & $\\sigma_{UU,UV}$ & $\\sigma_{UU,UW}$ & $\\sigma_{UU,VV}$ & $\\sigma_{UU,VW}$ & $\\sigma_{UU,WW}$ & "
				+ "$\\sigma^2_{UV}$ & $\\sigma_{UV,UW}$ & $\\sigma_{UV,VV}$ & $\\sigma_{UV,VW}$ & $\\sigma_{UV,WW}$ & "
				+ "$\\sigma^2_{UW}$ & $\\sigma_{UW,VV}$ & $\\sigma_{UW,VW}$ & $\\sigma_{UW,WW}$ & "
				+ "$\\sigma^2_{VV}$ & $\\sigma_{VV,VW}$ & $\\sigma_{VV,WW}$ & "
				+ "$\\sigma^2_{VW}$ & $\\sigma_{VW,WW}$ & "
				+ "$\\sigma^2_{WW}$ \\\\");
		
		source.add("\\hline");
		source.add("\\hline");
		
		Range[] magBins = magBinnedWds.getRanges();
		
		for(int i=0; i<magBins.length; i++) {
			
			if(meanVelocities[i][0]==null || velocityDispTensor[i][1] == null) {
				continue;
			}
			
			double s2_u = meanVelocities[i][1].get(0, 0);
			double s2_v = meanVelocities[i][1].get(1, 1);
			double s2_w = meanVelocities[i][1].get(2, 2);
			double s_uv = meanVelocities[i][1].get(0, 1);
			double s_uw = meanVelocities[i][1].get(0, 2);
			double s_vw = meanVelocities[i][1].get(1, 2);
			
			double s_uu_uu = velocityDispTensor[i][1].get(0, 0);
			double s_uu_uv = velocityDispTensor[i][1].get(0, 1);
			double s_uu_uw = velocityDispTensor[i][1].get(0, 2);
			double s_uu_vv = velocityDispTensor[i][1].get(0, 3);
			double s_uu_vw = velocityDispTensor[i][1].get(0, 4);
			double s_uu_ww = velocityDispTensor[i][1].get(0, 5);
			
			double s_uv_uv = velocityDispTensor[i][1].get(1, 1);
			double s_uv_uw = velocityDispTensor[i][1].get(1, 2);
			double s_uv_vv = velocityDispTensor[i][1].get(1, 3);
			double s_uv_vw = velocityDispTensor[i][1].get(1, 4);
			double s_uv_ww = velocityDispTensor[i][1].get(1, 5);
			
			double s_uw_uw = velocityDispTensor[i][1].get(2, 2);
			double s_uw_vv = velocityDispTensor[i][1].get(2, 3);
			double s_uw_vw = velocityDispTensor[i][1].get(2, 4);
			double s_uw_ww = velocityDispTensor[i][1].get(2, 5);
			
			double s_vv_vv = velocityDispTensor[i][1].get(3, 3);
			double s_vv_vw = velocityDispTensor[i][1].get(3, 4);
			double s_vv_ww = velocityDispTensor[i][1].get(3, 5);
			
			double s_vw_vw = velocityDispTensor[i][1].get(4, 4);
			double s_vw_ww = velocityDispTensor[i][1].get(4, 5);
			
			double s_ww_ww = velocityDispTensor[i][1].get(5, 5);
			
			source.add(String.format("%.3f & %.2f & %.2f & %.2f & %.2f & %.2f & %.2f & "
					+ "%.2f & %.2f & %.2f & %.2f & %.2f & %.2f & %.2f & %.2f & %.2f & %.2f & %.2f & %.2f & %.2f & %.2f & %.2f & %.2f & %.2f & %.2f & %.2f & %.2f & %.2f \\\\",
					magBins[i].mid(), s2_u, s2_v, s2_w, s_uv, s_uw, s_vw, s_uu_uu, s_uu_uv, s_uu_uw, s_uu_vv, s_uu_vw, s_uu_ww, s_uv_uv, s_uv_uw, s_uv_vv, s_uv_vw,
					s_uv_ww, s_uw_uw, s_uw_vv, s_uw_vw, s_uw_ww, s_vv_vv, s_vv_vw, s_vv_ww, s_vw_vw, s_vw_ww, s_ww_ww));
		}
		
		source.add("\\hline");
		source.add("\\end{tabular}");
		source.add("\\end{table*}");
		source.add("\\end{landscape}");
		
		
		
		
		source.add("\\begin{table*}");
		source.add("\\begin{tabular}{|cc|ccc|cccccc|}");
		source.add("\\hline");
		source.add(" Bin centre & N & \\multicolumn{3}{|c|}{Mean velocity [kms$^{-1}$]} & \\multicolumn{6}{|c|}{Velocity dispersion [km$^2$ s$^{-2}$]} \\\\");
		source.add(" G [mag] & [stars] & V$_U$ & V$_V$ & V$_W$ & $\\Sigma^2_{U}$ & $\\Sigma^2_{V}$ & $\\Sigma^2_{W}$ & $\\Sigma_{UV}$ & $\\Sigma_{UW}$ & $\\Sigma_{VW}$ \\\\");
		source.add("\\hline");
		source.add("\\hline");
		
		for(int i=0; i<magBins.length; i++) {
			
			if(meanVelocities[i][0]==null || velocityDispTensor[i][0] == null) {
				continue;
			}
			
			int n = magBinnedWds.get(i).size();
			
			double u = meanVelocities[i][0].get(0, 0);
			double v = meanVelocities[i][0].get(1, 0);
			double w = meanVelocities[i][0].get(2, 0);
			
			double s2_u = velocityDispTensor[i][0].get(0, 0);
			double s_uv = velocityDispTensor[i][0].get(1, 0);
			double s_uw = velocityDispTensor[i][0].get(2, 0);
			double s2_v = velocityDispTensor[i][0].get(3, 0);
			double s_vw = velocityDispTensor[i][0].get(4, 0);
			double s2_w = velocityDispTensor[i][0].get(5, 0);
			
			source.add(String.format("%.3f & %d & %.2f & %.2f & %.2f & %.2f & %.2f & %.2f & %.2f & %.2f & %.2f \\\\",
					magBins[i].mid(), n, u, v, w, s2_u, s2_v, s2_w, s_uv, s_uw, s_vw));
		}
		
		source.add("\\hline");
		source.add("\\end{tabular}");
		source.add("\\end{table*}");
		
		for(String str : source) {
			System.out.println(str);
		}
	}
	
	/**
	 * Plots the velocity dispersion diagonal terms as a function of G magnitude.
	 * 
	 * @param magBins
	 * 	The magnitude bins.
	 * @param velocityDispTensor
	 * 	The UVW Velocity dispersion tensor for each magnitude bin.
	 */
	private static void plotVelocityDispersion(Range[] magBins, Matrix[][] velocityDispTensor) {

		StringBuilder script = new StringBuilder();
		script.append("set terminal pngcairo enhanced color size 640,480").append(OSChecker.newline);
		
		script.append("set style line 1 lt 1 lw 1 pt "+pt_U+" ps 0.75 lc rgbcolor '"+colour_U+"'\n");
		script.append("set style line 2 lt 1 lw 1 pt "+pt_V+" ps 0.75 lc rgbcolor '"+colour_V+"'\n");
		script.append("set style line 3 lt 1 lw 1 pt "+pt_W+" ps 0.75 lc rgbcolor '"+colour_W+"'\n");
		
		script.append("set style line 20 lc rgb '#ddccdd' lt 1 lw 1.5").append(OSChecker.newline);
		script.append("set style line 21 lc rgb '#ddccdd' lt 1 lw 0.5").append(OSChecker.newline);
		script.append("set grid xtics mxtics ytics mytics back ls 20, ls 21").append(OSChecker.newline);
		
		script.append("set xrange ["+bprp_min+":"+bprp_max+"]").append(OSChecker.newline);
		script.append("set yrange [0:*]").append(OSChecker.newline);
		script.append("set key top left").append(OSChecker.newline);
		script.append("set xtics out").append(OSChecker.newline);
		script.append("set ytics out").append(OSChecker.newline);
		script.append("set mxtics 2").append(OSChecker.newline);
		script.append("set mytics 2").append(OSChecker.newline);
		script.append("set xlabel 'M_{G}'").append(OSChecker.newline);
		script.append("set ylabel 'km s^{-1}'").append(OSChecker.newline);
		script.append("plot '-' u 1:2 w l ls 1 notitle,");
		script.append("     '-' u 1:2 w l ls 2 notitle,");
		script.append("     '-' u 1:2 w l ls 3 notitle,");
		script.append("     '-' u 1:2:3:4 w yerrorbars ls 1 title 'σ_U',");
		script.append("     '-' u 1:2:3:4 w yerrorbars ls 2 title 'σ_V',");
		script.append("     '-' u 1:2:3:4 w yerrorbars ls 3 title 'σ_W'").append(OSChecker.newline);
		
		
		for(int i=0; i<magBins.length; i++) {
			if(velocityDispTensor[i][0]==null) {
				continue;
			}
			script.append(magBins[i].mid() + " " + Math.sqrt(velocityDispTensor[i][0].get(0, 0))).append(OSChecker.newline);
		}
		script.append("e").append(OSChecker.newline);

		for(int i=0; i<magBins.length; i++) {
			if(velocityDispTensor[i][0]==null) {
				continue;
			}
			script.append(magBins[i].mid() + " " + Math.sqrt(velocityDispTensor[i][0].get(3, 0))).append(OSChecker.newline);
		}
		script.append("e").append(OSChecker.newline);

		for(int i=0; i<magBins.length; i++) {
			if(velocityDispTensor[i][0]==null) {
				continue;
			}
			script.append(magBins[i].mid() + " " + Math.sqrt(velocityDispTensor[i][0].get(5, 0))).append(OSChecker.newline);
		}
		script.append("e").append(OSChecker.newline);
		
		for(int i=0; i<magBins.length; i++) {
			if(velocityDispTensor[i][0]==null) {
				continue;
			}
			
			// Velocity dispersion
			double s2 = velocityDispTensor[i][0].get(0, 0);
			double s = Math.sqrt(s2);
			// Standard deviation of the velocity dispersion
			double sig_s2 = Math.sqrt(velocityDispTensor[i][1].get(0, 0));
			
			// One sigma confidence boundaries
			double lowerPerc = 15.87;
			double upperPerc = 84.13;
			
			Function f = new Function(1, 1) {
				@Override
				public double[] compute(double[] inputs) {
					double s2 = inputs[0];
					if(s2 < 0.0) {
						s2 = Math.abs(s2);
					}
					double s = Math.sqrt(s2);
					return new double[]{s};
				}};
				
			double[] confLims = StatUtil.computeConfidenceLimits(f, new double[]{s2}, new double[]{sig_s2}, lowerPerc, upperPerc);
			double lower = confLims[0];
			double upper = confLims[1];
			
			script.append(magBins[i].mid() + " " + s + " " + lower + " " + upper).append(OSChecker.newline);
		}
		script.append("e").append(OSChecker.newline);
		
		for(int i=0; i<magBins.length; i++) {
			if(velocityDispTensor[i][0]==null) {
				continue;
			}
			// Velocity dispersion
			double s2 = velocityDispTensor[i][0].get(3, 0);
			double s = Math.sqrt(s2);
			// Standard deviation of the velocity dispersion
			double sig_s2 = Math.sqrt(velocityDispTensor[i][1].get(3, 3));
			
			// One sigma confidence boundaries
			double lowerPerc = 15.87;
			double upperPerc = 84.13;
			
			Function f = new Function(1, 1) {
				@Override
				public double[] compute(double[] inputs) {
					double s2 = inputs[0];
					if(s2 < 0.0) {
						s2 = Math.abs(s2);
					}
					double s = Math.sqrt(s2);
					return new double[]{s};
				}};
				
			double[] confLims = StatUtil.computeConfidenceLimits(f, new double[]{s2}, new double[]{sig_s2}, lowerPerc, upperPerc);
			double lower = confLims[0];
			double upper = confLims[1];
			
			script.append(magBins[i].mid() + " " + s + " " + lower + " " + upper).append(OSChecker.newline);
		}
		script.append("e").append(OSChecker.newline);

		for(int i=0; i<magBins.length; i++) {
			if(velocityDispTensor[i][0]==null) {
				continue;
			}
			// Velocity dispersion
			double s2 = velocityDispTensor[i][0].get(5, 0);
			double s = Math.sqrt(s2);
			// Standard deviation of the velocity dispersion
			double sig_s2 = Math.sqrt(velocityDispTensor[i][1].get(5, 5));
			
			// One sigma confidence boundaries
			double lowerPerc = 15.87;
			double upperPerc = 84.13;
			
			Function f = new Function(1, 1) {
				@Override
				public double[] compute(double[] inputs) {
					double s2 = inputs[0];
					if(s2 < 0.0) {
						s2 = Math.abs(s2);
					}
					double s = Math.sqrt(s2);
					return new double[]{s};
				}};
				
			double[] confLims = StatUtil.computeConfidenceLimits(f, new double[]{s2}, new double[]{sig_s2}, lowerPerc, upperPerc);
			double lower = confLims[0];
			double upper = confLims[1];
			
			script.append(magBins[i].mid() + " " + s + " " + lower + " " + upper).append(OSChecker.newline);
		}
		script.append("e").append(OSChecker.newline);
		
		try {
			Gnuplot.displayImage(Gnuplot.executeScript(script.toString()));
		} catch (IOException e) {
			System.out.println("Problem making velocity dispersion plot");
		}
		
	}
	
	/**
	 * Plots the vertex deviation as a function of G magnitude.
	 * 
	 * @param magBins
	 * 	The magnitude bins.
	 * @param velocityDispTensor
	 * 	The UVW Velocity dispersion tensor for each magnitude bin.
	 */
	private static void plotVertexDeviation(Range[] magBins, Matrix[][] velocityDispTensor) {

		StringBuilder script = new StringBuilder();
		script.append("set terminal pngcairo enhanced dashed color size 640,640").append(OSChecker.newline);
		
		script.append("set style line 1 lt 1 lw 1 pt "+pt_U+" ps 0.75 lc rgbcolor '"+colour_U+"'\n");
		script.append("set style line 2 lt 1 lw 1 pt "+pt_V+" ps 0.75 lc rgbcolor '"+colour_V+"'\n");
		script.append("set style line 3 lt 1 lw 1 pt "+pt_W+" ps 0.75 lc rgbcolor '"+colour_W+"'\n");
		
		script.append("set style line 20 lc rgb '#ddccdd' lt 1 lw 1.5").append(OSChecker.newline);
		script.append("set style line 21 lc rgb '#ddccdd' lt 1 lw 0.5").append(OSChecker.newline);
		script.append("set grid xtics mxtics ytics mytics back ls 20, ls 21").append(OSChecker.newline);
		
		script.append("set xrange ["+bprp_min+":"+bprp_max+"]").append(OSChecker.newline);
		script.append("set xtics out scale 1.0 nomirror").append(OSChecker.newline);
		script.append("set mxtics 2").append(OSChecker.newline);
		
		script.append("set xlabel ''").append(OSChecker.newline);
		script.append("set format x ''").append(OSChecker.newline);
		
		script.append("set key off").append(OSChecker.newline);
		// Works in Gnuplot 5.0:
//		script.append("set xzeroaxis lt 1 lw 1 dt 2 lc rgbcolor 'black'").append(OSChecker.newline);
		// Works in Gnuplot 4.6:
		script.append("set xzeroaxis lt 2 lw 1 lc rgbcolor 'black'").append(OSChecker.newline);
		
		
		script.append("set bmargin 0.0").append(OSChecker.newline);
		script.append("set tmargin 0.0").append(OSChecker.newline);
		
		script.append("set multiplot").append(OSChecker.newline);
		script.append("set size 1.0, 0.25").append(OSChecker.newline);
		
		for(int comp=0; comp<3; comp++) {
			
			script.append("set origin 0.0, "+(0.7 - 0.28*comp)).append(OSChecker.newline);
		
			if(comp==0) {
				script.append("set ylabel 'l_{UV} [deg]'").append(OSChecker.newline);
				
				script.append("set yrange [-20:30]").append(OSChecker.newline);
				script.append("set ytics 10 out").append(OSChecker.newline);
				script.append("set mytics 2").append(OSChecker.newline);
			}
			else if(comp==1) {
				script.append("set ylabel 'l_{UW} [deg]'").append(OSChecker.newline);

				script.append("set yrange [-20:20]").append(OSChecker.newline);
				script.append("set ytics 10 out").append(OSChecker.newline);
				script.append("set mytics 2").append(OSChecker.newline);
			}
			else {
				script.append("set format x '%g'").append(OSChecker.newline);
				script.append("set ylabel 'l_{VW} [deg]'").append(OSChecker.newline);

				script.append("set yrange [-60:60]").append(OSChecker.newline);
				script.append("set ytics 20 out").append(OSChecker.newline);
				script.append("set mytics 2").append(OSChecker.newline);
				
				script.append("set label 'M_G' at screen 0.45,screen 0.05").append(OSChecker.newline);
			}
			
			script.append("plot '-' u 1:2 w l ls "+(comp+1)+" notitle,");
			script.append("     '-' u 1:2:3:4 w yerrorbars ls "+(comp+1)+" notitle").append(OSChecker.newline);
			
			for(int t=0; t<2; t++) {
				for(int i=0; i<magBins.length; i++) {
					if(velocityDispTensor[i][0]==null) {
						continue;
					}
					
					// Components of the velocity dispersion tensor
					double sig2_00 = Double.NaN, sig_01 = Double.NaN, sig2_11 = Double.NaN;
					
					// Standard deviations on the estimates of velocity dispersion tensor components. Covariances ignored.
					double sig_sig2_00, sig_sig_01, sig_sig2_11;
					
					if(comp==0) {
						sig2_00 = velocityDispTensor[i][0].get(0, 0);
						sig_01  = velocityDispTensor[i][0].get(1, 0);
						sig2_11 = velocityDispTensor[i][0].get(3, 0);
						
						sig_sig2_00 = Math.sqrt(velocityDispTensor[i][1].get(0, 0));
						sig_sig_01 = Math.sqrt(velocityDispTensor[i][1].get(1, 1));
						sig_sig2_11 = Math.sqrt(velocityDispTensor[i][1].get(3, 3));
					}
					else if(comp==1) {
						sig2_00 = velocityDispTensor[i][0].get(0, 0);
						sig_01  = velocityDispTensor[i][0].get(2, 0);
						sig2_11 = velocityDispTensor[i][0].get(5, 0);
						
						sig_sig2_00 = Math.sqrt(velocityDispTensor[i][1].get(0, 0));
						sig_sig_01 = Math.sqrt(velocityDispTensor[i][1].get(2, 2));
						sig_sig2_11 = Math.sqrt(velocityDispTensor[i][1].get(5, 5));
					}
					else {
						sig2_00 = velocityDispTensor[i][0].get(3, 0);
						sig_01  = velocityDispTensor[i][0].get(4, 0);
						sig2_11 = velocityDispTensor[i][0].get(5, 0);
						
						sig_sig2_00 = Math.sqrt(velocityDispTensor[i][1].get(3, 3));
						sig_sig_01 = Math.sqrt(velocityDispTensor[i][1].get(4, 4));
						sig_sig2_11 = Math.sqrt(velocityDispTensor[i][1].get(5, 5));
					}
					
					double l_01 = Math.toDegrees(0.5 * Math.atan2(2*sig_01, sig2_00 - sig2_11));
					
					if(t==0) {
						script.append(magBins[i].mid() + " " + l_01).append(OSChecker.newline);
					}
					else {
						// One sigma confidence boundaries
						double lowerPerc = 15.87;
						double upperPerc = 84.13;
						
						Function f = new Function(3, 1) {
							@Override
							public double[] compute(double[] inputs) {
								
								double a11 = inputs[0];
								double a12 = inputs[1];
								double a22 = inputs[2];
								
								double l = Math.toDegrees(0.5 * Math.atan2(2*a12, a11 - a22));
								
								return new double[]{l};
							}};
							
						double[] confLims = StatUtil.computeConfidenceLimits(f, new double[]{sig2_00, sig_01, sig2_11}, 
								new double[]{sig_sig2_00, sig_sig_01, sig_sig2_11}, lowerPerc, upperPerc);
						
						double lower = confLims[0];
						double upper = confLims[1];
						
						script.append(magBins[i].mid() + " " + l_01 + " " + lower + " " + upper).append(OSChecker.newline);
					}
				}
				script.append("e").append(OSChecker.newline);
			}
		}
		
		try {
			Gnuplot.displayImage(Gnuplot.executeScript(script.toString()));
		} catch (IOException e) {
			System.out.println("Problem making velocity dispersion plot");
		}
		
	}
	
	/**
	 * Plots the velocity dispersion ellipse in the UV plane as a function of G magnitude.
	 * 
	 * @param magBins
	 * 	The magnitude bins.
	 * @param velocityDispTensor
	 * 	The UVW Velocity dispersion tensor for each magnitude bin.
	 */
	private static void plotVelocityEllipsoidProjections(Range[] magBins, Matrix[][] velocityDispTensor) {
		
		double[] rangesUVW = new double[]{50.0, 50.0, 35.0};
		String[][] labels = new String[][]{{"U","V"},{"U","W"},{"V","W"}};
		
		for(int k=0; k<3; k++) {
			
			StringBuilder script = new StringBuilder();
			script.append("set terminal pngcairo enhanced color size 512,512").append(OSChecker.newline);
		
			script.append("set size square").append(OSChecker.newline);
			script.append("set key off").append(OSChecker.newline);
			
			script.append("set style line 1 lt 1 lw 1 palette").append(OSChecker.newline);
			
			script.append("set style line 20 lc rgb '#ddccdd' lt 1 lw 1.5").append(OSChecker.newline);
			script.append("set style line 21 lc rgb '#ddccdd' lt 1 lw 0.5").append(OSChecker.newline);
			script.append("set grid xtics mxtics ytics mytics back ls 20, ls 21").append(OSChecker.newline);
			
			script.append("set cbrange ["+bprp_min+":"+bprp_max+"]").append(OSChecker.newline);
			script.append("set cblabel 'M_G'").append(OSChecker.newline);
			
			script.append("set xlabel '"+labels[k][0]+" [km s^{-1}]'").append(OSChecker.newline);
			script.append("set xrange ["+(-rangesUVW[k])+":"+rangesUVW[k]+"]").append(OSChecker.newline);
			script.append("set xtics out").append(OSChecker.newline);
			script.append("set mxtics 2").append(OSChecker.newline);
			
			script.append("set ylabel '"+labels[k][1]+" [km s^{-1}] '").append(OSChecker.newline);
			script.append("set yrange ["+(-rangesUVW[k])+":"+rangesUVW[k]+"]").append(OSChecker.newline);
			script.append("set ytics out").append(OSChecker.newline);
			script.append("set mytics 2").append(OSChecker.newline);
			
			script.append("plot '-' u 1:2:3 w l ls 1 notitle");
			
			for(int i=0; i<magBins.length; i++) {
				if(velocityDispTensor[i][0] != null) {
					script.append(", '-' u 1:2:3 w l ls 1 notitle");
				}
			}
			script.append(OSChecker.newline);
			
			int nPoints = 100;
			
			for(int i=0; i<magBins.length; i++) {
				if(velocityDispTensor[i][0] == null) {
					continue;
				}
				double mag = magBins[i].mid();
				
				double sig2_U = velocityDispTensor[i][0].get(0, 0);
				double sig_UV = velocityDispTensor[i][0].get(1, 0);
				double sig_UW = velocityDispTensor[i][0].get(2, 0);
				double sig2_V = velocityDispTensor[i][0].get(3, 0);
				double sig_VW = velocityDispTensor[i][0].get(4, 0);
				double sig2_W = velocityDispTensor[i][0].get(5, 0);
				
				Matrix disp = null;
				if(k==0) {
					disp = new Matrix(new double[][]{{sig2_U, sig_UV},{sig_UV, sig2_V}});
				}
				else if(k==1) {
					disp = new Matrix(new double[][]{{sig2_U, sig_UW},{sig_UW, sig2_W}});
				}
				else {
					disp = new Matrix(new double[][]{{sig2_V, sig_VW},{sig_VW, sig2_W}});
				}
				
				double[][] pointsUv = StatUtil.getConfidenceEllipsePoints(new double[]{0.0, 0.0}, disp, 1.0, nPoints);
				
				for(double[] pointUv : pointsUv) {
					script.append(pointUv[0] + "\t" + pointUv[1] + "\t" + mag).append(OSChecker.newline);
				}
				script.append("e").append(OSChecker.newline);
			}
			
			try {
				Gnuplot.displayImage(Gnuplot.executeScript(script.toString()));
			} catch (IOException e) {
				System.out.println("Problem making velocity dispersion plot");
			}
		}
        
	}
	
	/**
	 * Plots the components of the mean velocity as a function of velocity dispersion.
	 * 
	 * @param magBinnedWds
	 * 	The magnitude bined WDs.
	 * @param meanVelocities
	 * 	Mean velocity in each magnitude bin.
	 * @param velocityDisp
	 * 	Velocity dispersion in each magnitude bin.
	 * @param polys
	 * 	{@link Polynomial} fits to the mean velocity as a function of velocity dispersion
	 */
	private static void plotVelocityVsS2(RangeMap<ExtendedGaiaSource> magBinnedWds, Matrix[][] meanVelocities, double[][] velocityDisp, Polynomial[] polys) {
		
		String[] labels = {"U", "V", "W"};
		
		int[] mytics = {5, 5, 2};
		
		for(int comp=0; comp<3; comp++) {
			
			// Value of LSR motion
			double[] lsr = polys[comp].getFnWithError(0.0);
			
			StringBuilder script = new StringBuilder();
			script.append("set terminal pngcairo enhanced color size 640,480").append(OSChecker.newline);
			script.append("set xrange [0:*]").append(OSChecker.newline);
			script.append("set yrange [0:*]").append(OSChecker.newline);
			
			script.append("set style line 1 lt 1 lw 1 pt "+pt_U+" ps 0.75 lc rgbcolor '"+colour_U+"'\n");
			script.append("set style line 2 lt 1 lw 1 pt "+pt_V+" ps 0.75 lc rgbcolor '"+colour_V+"'\n");
			script.append("set style line 3 lt 1 lw 1 pt "+pt_W+" ps 0.75 lc rgbcolor '"+colour_W+"'\n");
			
			script.append("set key off").append(OSChecker.newline);
			script.append("set xtics out").append(OSChecker.newline);
			script.append("set mxtics 5").append(OSChecker.newline);
			script.append("set ytics out").append(OSChecker.newline);
			script.append("set mytics "+mytics[comp]+"").append(OSChecker.newline);
			script.append("set xlabel 'S^{2} [km^2 s^{-2}]'").append(OSChecker.newline);
			script.append("set ylabel '"+labels[comp]+" [km s^{-1}]'").append(OSChecker.newline);
			script.append("set title '"+labels[comp]+"_0 = "+String.format("%.2f +/- %.2f", lsr[0], Math.sqrt(lsr[1]))+" kms^{-1}'").append(OSChecker.newline);
			script.append("plot '-' u 1:2 w l lt 1 lw 2 lc rgb 'black' notitle,");
			script.append("     '-' u 1:2 w l lt 1 lw 1 lc rgb 'gray60' notitle,");
			script.append("     '-' u 1:2 w l lt 1 lw 1 lc rgb 'gray60' notitle,");
			script.append("     '-' u 1:(-$2) w p ls "+(comp+1)+" notitle,");
			script.append("     '-' u 1:(-$2):3 w yerrorbars ls "+(comp+1)+" notitle").append(OSChecker.newline);

			// Cache largest x coordinate, for help in plotting regression line
			double max = -Double.MAX_VALUE;
			for(int i=0; i<magBinnedWds.size(); i++) {
				
				double mag = magBinnedWds.getRange(i).mid();
				
				// Skip ranges of G that don't show age/G correlation
				if(mag < bprp_ageCorrelation_min || mag > bprp_ageCorrelation_max) {
					continue;
				}

				// Skip ranges with too few stars
				if(magBinnedWds.get(i).size() < 2 || velocityDisp[i][0] == Double.NaN) {
					continue;
				}
				
				max = Math.max(max, velocityDisp[i][0]);
			}
			int nPoints = 100;
			double xStep = max / (nPoints-1);
			
			// Plot regression line
			for(int i=0; i<nPoints; i++) {
				double x = i*xStep;
				double y = polys[comp].getFn(x);
				script.append(x + " " + y).append(OSChecker.newline);
			}
			script.append("e").append(OSChecker.newline);

			// Plot upper confidence limit regression line
			for(int i=0; i<nPoints; i++) {
				double x = i*xStep;
				double[] y = polys[comp].getFnWithError(x);
				script.append(x + " " + (y[0] + Math.sqrt(y[1]))).append(OSChecker.newline);
			}
			script.append("e").append(OSChecker.newline);

			// Plot lower confidence limit regression line
			for(int i=0; i<nPoints; i++) {
				double x = i*xStep;
				double[] y = polys[comp].getFnWithError(x);
				script.append(x + " " + (y[0] - Math.sqrt(y[1]))).append(OSChecker.newline);
			}
			script.append("e").append(OSChecker.newline);
			
			// Plot the points
			for(int i=0; i<magBinnedWds.size(); i++) {

				double mag = magBinnedWds.getRange(i).mid();
				
				// Skip ranges of G that don't show age/G correlation
				if(mag < bprp_ageCorrelation_min || mag > bprp_ageCorrelation_max) {
					continue;
				}

				// Skip ranges with too few stars
				if(magBinnedWds.get(i).size() < 2 || velocityDisp[i][0] == Double.NaN) {
					continue;
				}
				
				script.append(velocityDisp[i][0] + " " + meanVelocities[i][0].get(comp, 0)).append(OSChecker.newline);
				
				max = Math.max(max, velocityDisp[i][0]);
			}
			script.append("e").append(OSChecker.newline);
			
			// Plot the errors
			for(int i=0; i<magBinnedWds.size(); i++) {

				double mag = magBinnedWds.getRange(i).mid();
				
				// Skip ranges of G that don't show age/G correlation
				if(mag < bprp_ageCorrelation_min || mag > bprp_ageCorrelation_max) {
					continue;
				}

				// Skip ranges with too few stars
				if(magBinnedWds.get(i).size() < 2 || velocityDisp[i][0] == Double.NaN) {
					continue;
				}
				
				script.append(velocityDisp[i][0] + " " + meanVelocities[i][0].get(comp, 0) + " " + Math.sqrt(meanVelocities[i][1].get(comp, comp))).append(OSChecker.newline);
			}
			script.append("e").append(OSChecker.newline);
			
			try {
				Gnuplot.displayImage(Gnuplot.executeScript(script.toString()));
			} catch (IOException e) {
				System.out.println("Problem plotting "+labels[comp]+" vs velocity dispersion");
			}
		}
	}

	/**
	 * Plots the components of the mean velocity and velocity dispersion as a function of absolute magnitude.
	 * @param magBins
	 * 	The magnitude bins.
	 * @param meanVelocities
	 * 	Mean velocity in each magnitude bin.
	 * @param velocityDisp
	 * 	Velocity dispersion in each magnitude bin.
	 */
	private static void plotMeanVelocities(Range[] magBins, Matrix[][] meanVelocities, double[][] velocityDisp) {
		
		StringBuilder script = new StringBuilder();
		script.append("set terminal pngcairo enhanced color size 640,480").append(OSChecker.newline);
		script.append("set xrange [*:*]").append(OSChecker.newline);
		script.append("set yrange [*:*]").append(OSChecker.newline);

		script.append("set style line 1 lt 1 lw 1 pt "+pt_U+" ps 0.75 lc rgbcolor '"+colour_U+"'\n");
		script.append("set style line 2 lt 1 lw 1 pt "+pt_V+" ps 0.75 lc rgbcolor '"+colour_V+"'\n");
		script.append("set style line 3 lt 1 lw 1 pt "+pt_W+" ps 0.75 lc rgbcolor '"+colour_W+"'\n");
		// Style for velocity dispersion plot
		script.append("set style line 4 lt 1 lw 1 pt 3 ps 0.75 lc rgbcolor 'black'\n");
		
		script.append("set style line 20 lc rgb '#ddccdd' lt 1 lw 1.5").append(OSChecker.newline);
		script.append("set style line 21 lc rgb '#ddccdd' lt 1 lw 0.5").append(OSChecker.newline);
		script.append("set grid xtics mxtics ytics mytics back ls 20, ls 21").append(OSChecker.newline);
		script.append("set key top left").append(OSChecker.newline);
		script.append("set xtics out").append(OSChecker.newline);
		script.append("set ytics out").append(OSChecker.newline);
		script.append("set mxtics 2").append(OSChecker.newline);
		script.append("set mytics 2").append(OSChecker.newline);
		
		script.append("set xlabel 'M_G'").append(OSChecker.newline);
		script.append("set ylabel 'km s^{-1}'").append(OSChecker.newline);
		script.append("plot '-' u 1:(-$2) w l ls 1 notitle,");
		script.append("     '-' u 1:(-$2) w l ls 2 notitle,");
		script.append("     '-' u 1:(-$2) w l ls 3 notitle,");
		script.append("     '-' u 1:(-$2):3 w yerrorbars ls 1 title 'U',");
		script.append("     '-' u 1:(-$2):3 w yerrorbars ls 2 title 'V',");
		script.append("     '-' u 1:(-$2):3 w yerrorbars ls 3 title 'W',");
		script.append("     '-' u 1:2:3:4 w yerrorbars ls 4 title 'S'").append(OSChecker.newline);
		
		for(int i=0; i<magBins.length; i++) {
			script.append(magBins[i].mid() + " " + meanVelocities[i][0].get(0, 0)).append(OSChecker.newline);
		}
		script.append("e").append(OSChecker.newline);

		for(int i=0; i<magBins.length; i++) {
			script.append(magBins[i].mid() + " " + meanVelocities[i][0].get(1, 0)).append(OSChecker.newline);
		}
		script.append("e").append(OSChecker.newline);

		for(int i=0; i<magBins.length; i++) {
			script.append(magBins[i].mid() + " " + meanVelocities[i][0].get(2, 0)).append(OSChecker.newline);
		}
		script.append("e").append(OSChecker.newline);
		
		
		// Errors
		for(int i=0; i<magBins.length; i++) {
			script.append(magBins[i].mid() + " " + meanVelocities[i][0].get(0, 0) + " " + Math.sqrt(meanVelocities[i][1].get(0, 0))).append(OSChecker.newline);
		}
		script.append("e").append(OSChecker.newline);

		for(int i=0; i<magBins.length; i++) {
			script.append(magBins[i].mid() + " " + meanVelocities[i][0].get(1, 0) + " " + Math.sqrt(meanVelocities[i][1].get(1, 1))).append(OSChecker.newline);
		}
		script.append("e").append(OSChecker.newline);

		for(int i=0; i<magBins.length; i++) {
			script.append(magBins[i].mid() + " " + meanVelocities[i][0].get(2, 0) + " " + Math.sqrt(meanVelocities[i][1].get(2, 2))).append(OSChecker.newline);
		}
		script.append("e").append(OSChecker.newline);
		
		
		// Velocity dispersion
		for(int i=0; i<magBins.length; i++) {
			
			double s2 = velocityDisp[i][0];
			double s = Math.sqrt(s2);
			double sig_s2 = Math.sqrt(velocityDisp[i][1]);
			
			// Transform variance on S2 to variance on S
			
			// Error estimation by first order Taylor expansion (not robust for nonlinear sqrt transformation)
			// double sig_s = (1.0/(2.0 * s)) * sig_s2;
			
			// One sigma confidence boundaries
			double lowerPerc = 15.87;
			double upperPerc = 84.13;
			
			Function f = new Function(1, 1) {
				@Override
				public double[] compute(double[] inputs) {
					double s2 = inputs[0];
					if(s2 < 0.0) {
						s2 = Math.abs(s2);
					}
					double s = Math.sqrt(s2);
					return new double[]{s};
				}};
				
			double[] confLims = StatUtil.computeConfidenceLimits(f, new double[]{s2}, new double[]{sig_s2}, lowerPerc, upperPerc);
			double lower = confLims[0];
			double upper = confLims[1];
			
			script.append(magBins[i].mid() + " " + s + " " + lower + " " + upper).append(OSChecker.newline);
		}
		script.append("e").append(OSChecker.newline);
		
		try {
			Gnuplot.displayImage(Gnuplot.executeScript(script.toString()));
		} catch (IOException e) {
			System.out.println("Problem making mean velocities plot");
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
		int[] count = new int[3];
		
		// Stars that failed quality criteria; leaving only good MS and WDs
		List<ExtendedGaiaSource> accepted = new LinkedList<>();
		
		for(ExtendedGaiaSource star : allStars) {
			
			// BP-RP colour index
			double bpRp = star.phot_bp_mean_mag - star.phot_rp_mean_mag;
			
			// Apply quality criteria
			boolean reject = false;
			
			// Filtering on astrometric quality:
			
			if(star.astrometric_excess_noise > excessNoiseLimit) {
				reject = true;
				count[0]++;
			}
			
			// Distance selection
			if(star.d > d_max) {
				reject = true;
				count[1]++;
			}
			// Tangential velocity selection
			double vtan = star.p.normF();
			
			if(vtan > vtan_max || vtan < vtan_min) {
				reject = true;
				count[2]++;
			}
			
			if(!reject) {
				accepted.add(star);
			}
			
		}
		
		int rejected = allStars.size() - accepted.size();
		
		logger.info("Purged "+rejected+" objects; retained "+accepted.size()+" well-measured Solar neighbourhood disk stars for analysis");
		logger.info("Failed excess noise threshold: " + count[0]);
		logger.info("Failed distance threshold:     " + count[1]);
		logger.info("Failed vtan threshold:         " + count[2]);
		
		return accepted;
	}
	
	/**
	 * Apply WD selection in the HRD.
	 * 
	 * @param allStars
	 * 	The set of {@link ExtendedGaiaSource}s.
	 * @return
	 * 	A {@link RangeMap<ExtendedGaiaSource>} containing the selected WDs binned by absolute G magnitude.
	 */
	private static Collection<ExtendedGaiaSource> selectWds(Collection<ExtendedGaiaSource> allStars) {
		
		// Count number of stars that fail each threshold
		int[] count = new int[1];
		
		List<ExtendedGaiaSource> wds = new LinkedList<>();
		
		for(ExtendedGaiaSource star : allStars) {
			
			// Absolute G magnitude
			double gMag = PhotometryUtils.getAbsoluteMagnitudeFromPi(star.parallax/1000.0, star.phot_g_mean_mag);
			
			// BP-RP colour index
			double bpRp = star.phot_bp_mean_mag - star.phot_rp_mean_mag;
			
			// Apply quality criteria
			boolean reject = false;
			
			// Selection in HRD
			if(!(gMag > 5 && gMag > 5.93 + 5.047*bpRp && gMag > 6*bpRp*bpRp*bpRp - 21.77*bpRp*bpRp + 27.91*bpRp + 0.897 && bpRp < 1.7)) {
				count[0]++;
				reject = true;
			}
			
			if(!reject) {
				wds.add(star);
			}
		}

		logger.info("Retained "+wds.size()+" WDs for analysis");
		logger.info("Failed HRD selection:      " + count[0]);
		
		return wds;
	}
	
	/**
	 * Apply WD selection in the HRD and bin resulting WDs by magnitude.
	 * 
	 * @param allStars
	 * 	The set of {@link ExtendedGaiaSource}s.
	 * @return
	 * 	A {@link RangeMap<ExtendedGaiaSource>} containing the selected WDs binned by absolute G magnitude.
	 */
	private static RangeMap<ExtendedGaiaSource> binByAbsG(Collection<ExtendedGaiaSource> wds) {
		
		// Now bin the selected stars on G magnitude according to the chosen bin scheme
		RangeMap<ExtendedGaiaSource> magBinnedWds;
		
		if(binScheme == BIN_SCHEME.CONSTANT_BPRP_STEP) {
			// Map the stars by magnitude
			magBinnedWds = new RangeMap<>(bprp_min, bprp_max, bprp_step);
		}
		else if(binScheme == BIN_SCHEME.CONSTANT_STARS) {
			
			// Read the G magnitudes into a sorted list
			List<Double> gMags = new LinkedList<>();
			for(ExtendedGaiaSource star : wds) {
				// Absolute G magnitude
				double gMag = PhotometryUtils.getAbsoluteMagnitudeFromPi(star.parallax/1000.0, star.phot_g_mean_mag);
				
				if(gMag >= bprp_min && gMag <= bprp_max) {
					gMags.add(gMag);
				}
			}
			
			Collections.sort(gMags);
			
			// Find the bin edges
			List<Double> binEdges = new LinkedList<>();
			
			// Lower edge of first bin
			binEdges.add(gMags.get(0));
			
			for(int bin=1; ; bin++) {
				
				// Index of star that lies at upper boundary of the bin
				int upperIdx = nStarsPerBin*bin - 1;
				
				if(upperIdx > gMags.size() - 1) {
					// Final bin and there are fewer than nStarsPerBin in it; merge it with the previous bin.
					// Remove upper edge of previous bin
					binEdges.remove(binEdges.size() - 1);
					// Add upper edge of final bin (the magnitude of the faintest star)
					double binEdge = gMags.get(gMags.size() - 1);
					binEdges.add(binEdge);
					break;
				}
				else if(upperIdx == gMags.size() - 1) {
					// Final bin and there's nStarsPerBin in it; don't merge it with previous bin.
					double binEdge = gMags.get(upperIdx);
					binEdges.add(binEdge);
					break;
				}
				else {
					// Not yet reached the final bin; place bin edge at midpoint between two stars
					double binEdge = (gMags.get(upperIdx) + gMags.get(upperIdx + 1)) / 2.0f;
					binEdges.add(binEdge);
				}
			}
			
			// Convert bin edges list to arrays of bin centres and widths
			double[] binCentres = new double[binEdges.size()-1];
			double[] binWidths = new double[binEdges.size()-1];
			
			for(int bin = 0; bin < binEdges.size()-1; bin++) {
				
				// Bin lower edge & upper edge
				double lower = binEdges.get(bin);
				double upper = binEdges.get(bin+1);
				
				binCentres[bin] = (lower + upper) / 2.0;
				binWidths[bin] = (upper - lower);
			}
			magBinnedWds = new RangeMap<>(binCentres, binWidths);
		}
		else {
			throw new RuntimeException("Unsupported bin scheme: " + binScheme);
		}
		
		for(ExtendedGaiaSource star : wds) {
			// Absolute G magnitude
			double gMag = PhotometryUtils.getAbsoluteMagnitudeFromPi(star.parallax/1000.0, star.phot_g_mean_mag);
			magBinnedWds.add(gMag, star);
		}
		
		return magBinnedWds;
	}
	
	
	
	
	/**
	 * Promote each {@link GaiaSource} to a {@link ExtendedGaiaSource}.
	 * 
	 * @param stars
	 * 	A {@link Collection} of all the {@link GaiaSource}s.
	 * @return
	 * 	A {@link Collection} of {@link ExtendedGaiaSource}.
	 */
	private static Collection<ExtendedGaiaSource> computePandA(Collection<GaiaSource> stars) {
		
		Collection<ExtendedGaiaSource> starsWithVelocity = new LinkedList<>();
		
		for(GaiaSource star : stars) {
			starsWithVelocity.add( new ExtendedGaiaSource(star));
		}
		
		return starsWithVelocity;
	}
	
	/**
	 * Displays an all-sky map of the star positions.
	 * @param stars
	 * 	A {@link RangeMap} containing all of the {@link GaiaSource}s to plot.
	 */
	private static void displaySkyMap(RangeMap<? extends GaiaSource> stars) {
		
		List<double[]> points = new LinkedList<>();
		
		for(int bin=0; bin<stars.size(); bin++) {
			for(GaiaSource star : stars.get(bin)) {
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
		
		for(GaiaSource star : allStars) {
			
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
		script.append("set terminal pngcairo enhanced color size 540,640").append(OSChecker.newline);
		
		script.append("set xlabel 'G_{BP}-G_{RP}'").append(OSChecker.newline);
		script.append("set xrange [-0.7:5]").append(OSChecker.newline);
		script.append("set xtics out nomirror").append(OSChecker.newline);
		script.append("set mxtics 2").append(OSChecker.newline);
		
		script.append("set ylabel 'M_G'").append(OSChecker.newline);
		script.append("set yrange [17:-4]").append(OSChecker.newline);
		script.append("set ytics out nomirror").append(OSChecker.newline);
		script.append("set mytics 5").append(OSChecker.newline);
		
		script.append("set cbtics 1").append(OSChecker.newline);
		script.append("set cblabel 'Log N [mag^{-2}]'").append(OSChecker.newline);
		
		script.append("set key off").append(OSChecker.newline);
		
		script.append("min(x,y) = (x < y) ? x : y").append(OSChecker.newline);
		script.append("max(x,y) = (x > y) ? x : y").append(OSChecker.newline);
		script.append("f(x) = (x <= 1.75) ? max(max(5, 5.93 + 5.047*x), 6*x**3 - 21.77*x**2 + 27.91*x + 0.897) : 1/0").append(OSChecker.newline);
		
		script.append("set size 0.9,1.0").append(OSChecker.newline);
		
		script.append("set style line 12 lc rgb '#ddccdd' lt 1 lw 1.5").append(OSChecker.newline);
		script.append("set style line 13 lc rgb '#ddccdd' lt 1 lw 0.5").append(OSChecker.newline);
		script.append("set grid xtics mxtics ytics mytics back ls 12, ls 13").append(OSChecker.newline);
		
		script.append("set multiplot").append(OSChecker.newline);
		script.append("set view map").append(OSChecker.newline);
		
		script.append("splot '"+plotDataTmp1.getAbsolutePath()+"' u 1:2:(1.0) w d lc rgbcolor 'black' notitle").append(OSChecker.newline);
		
		script.append("set pm3d map").append(OSChecker.newline);
		script.append("unset grid").append(OSChecker.newline);
		script.append("set arrow from 1.7,17 to 1.7,14.9067 nohead front lw 2 lc rgb 'red'").append(OSChecker.newline);
		
		script.append("splot '"+plotDataTmp1.getAbsolutePath()+"' u 1:2:(1.0) w d lc rgbcolor 'black' notitle").append(OSChecker.newline);
		script.append("splot '"+plotDataTmp2.getAbsolutePath()+"' u 1:2:(($3 * "+(magStep*bpRpStep)+") < 5 ? 1/0 : log10($3)) notitle,");
		script.append("      '+' u 1:(f($1)):(1.0) w l lw 2 lc rgb 'red' notitle").append(OSChecker.newline);
		
		try {
			Gnuplot.displayImage(Gnuplot.executeScript(script.toString()));
		} catch (IOException e) {
			System.out.println("Problem making HR diagram");
		}
	}
	

	/**
	 * Local class used to endow the {@link GaiaSource} with {@link Matrix}s to store
	 * the proper motion velocity vector and projection matrices etc, for convenience.
	 *
	 * @author nrowell
	 * @version $Id$
	 */
	static class ExtendedGaiaSource extends GaiaSource implements AstrometricStar {
		
		public ExtendedGaiaSource(GaiaSource star) {
			
			super(star);
			
			// Get the distance to the star [parsecs]
			d = 1000.0 / star.parallax;
			
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
					6293246906873373312L, 3845384108163674240L, 1517698613271363840L, 4555529565849768576L, 6619460878574794880L}));
			
			if(srcIdSet.contains(star.sourceId)) {
				System.out.println("\nFound source " + star.sourceId);
				System.out.println("ra       = " + star.ra);
				System.out.println("dec      = " + star.dec);
				System.out.println("pmra     = " + star.pmra);
				System.out.println("pmdec    = " + star.pmdec);
				
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
