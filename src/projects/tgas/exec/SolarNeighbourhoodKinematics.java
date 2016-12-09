package projects.tgas.exec;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import Jama.Matrix;
import constants.Galactic;
import constants.Units;
import infra.io.Gnuplot;
import infra.os.OSChecker;
import numeric.data.RangeMap;
import numeric.functions.Linear;
import projects.tgas.dm.TgasApassStar;
import projects.tgas.util.TgasUtils;
import utils.AstrometryUtils;
import utils.MagnitudeUtils;

/**
 * This class performs an analysis of the kinematics of Solar Neighbourhood stars using the TGAS data.
 * 
 * TODO: double check the correction for Oort constants (especially inclusion of cos(b) term); include C & K in the correction?
 * TODO: add velocity dispersion computation
 * 
 * 
 * TODO: decide on colour bins to use
 * TODO: outlier rejection?
 *
 * @author nrowell
 * @version $Id$
 */
public class SolarNeighbourhoodKinematics {

	/**
	 * The logger.
	 */
	private static final Logger logger = Logger.getLogger(SolarNeighbourhoodKinematics.class.getName());
	
	/**
	 * Adopted value for the Oort Constant A, in units of radians per year.
	 */
	static double A = Galactic.A * Units.KM_PER_SEC_PER_KPC_TO_RAD_PER_YEAR;

	/**
	 * Adopted value for the Oort Constant B, in units of radians per year.
	 */
	static double B = Galactic.B * Units.KM_PER_SEC_PER_KPC_TO_RAD_PER_YEAR;
	
	/**
	 * Threshold on the fractional parallax error
	 */
	static double f_max = 10;
	
	/**
	 * Threshold on the B-V colour error
	 */
	static double cerr_max = 10.05;
	
	/**
	 * Distance threshold (to restrict to nearby stars) [parsec]
	 */
	static double d_max = 500.0;
	
	/**
	 * Points along upper boundary of Main Sequence used for selection
	 */
	static double[] x_upper = new double[]{-0.1, 0.8, 1.0, 1.5, 1.7};
	static double[] y_upper = new double[]{-1.7, 3.5, 6.0, 8.0, 10.0};

	/**
	 * Points along lower boundary of Main Sequence used for selection
	 */
	static double[] x_lower = new double[]{-0.1, 0.5, 1.3, 1.36};
	static double[] y_lower = new double[]{ 1.8, 5.6, 9.0, 10.0};
	
	/**
	 * Colour binning parameters
	 */
	static double bv_min = 0.0;
	static double bv_max = 1.5;
	static double bv_step = 0.02;
	
	/**
	 * Main application entry point.
	 * @param args
	 * 	The command line arguments [ignored]
	 */
	public static void main(String[] args) {
		
		// Load the TGASxAPASS stars
		Collection<TgasApassStar> tgasStars = TgasUtils.loadTgasApassCatalogue();
		
		// Create Linear objects for use in delineating the main sequence
		Linear upperMs = new Linear(x_upper, y_upper);
		Linear lowerMs = new Linear(x_lower, y_lower);
		
		// 0) Map the stars by colour, and make selection on parallax fractional error etc
		RangeMap<TgasApassStar> stars = new RangeMap<>(bv_min, bv_max, bv_step);

		List<double[]> selectedStars = new LinkedList<>();
		List<double[]> nonSelectedStars = new LinkedList<>();
		
		for(TgasApassStar star : tgasStars) {
			
			// APASS B-V colour
			double bv = star.b_mag - star.v_mag;
			// APASS B-V colour error
			double bvErr = Math.sqrt(star.e_b_mag * star.e_b_mag + star.e_v_mag * star.e_v_mag);
			
			// Data quality thresholds: selection on parallax and colour error
			// TODO: selection on astrometric excess noise?
			if(star.parallax/star.parallax_error < f_max || bvErr > cerr_max) {
				continue;
			}
			
			// Absolute V magnitude
			double vMag = MagnitudeUtils.getAbsoluteMagnitudeFromPi(star.parallax/1000.0, star.v_mag);
			
			// Distance [parsec]
			double d = 1000.0 / star.parallax;
			
			// Distance selection
			if(d > d_max) {
				nonSelectedStars.add(new double[]{bv, vMag});
				continue;
			}
			
			// Select main sequence. Note magnitude axis is inverted.
			if(vMag < upperMs.interpolateY(bv)[0] || vMag > lowerMs.interpolateY(bv)[0]) {
				nonSelectedStars.add(new double[]{bv, vMag});
				continue;
			}
			
			// Load selected star into colour map
			stars.add(bv, star);
			selectedStars.add(new double[]{bv, vMag});
		}
		
		logger.info("Selected "+selectedStars.size()+" TGASxAPASS stars for analysis");
		
		// Display HR diagram of selected and unselected stars
		displayHrDiagram(selectedStars, nonSelectedStars, upperMs, lowerMs);

		System.out.println(String.format("%s\t%s\t%s\t%s", "B-V", "U", "V", "W"));
		
		// 1) Process colour bins separately
		for(int bin=0; bin<stars.size(); bin++) {
			
			// Colour of the current bin centre
			double bv = stars.getRange(bin).mid();
		
			// Mean projection matrix A
			Matrix meanA = new Matrix(3, 3);
			
			// Mean proper motion velocity vector p
			Matrix meanP = new Matrix(3, 1);
			
			// 2) For each object in given colour bin:
			for(TgasApassStar star : stars.get(bin)) {
				
				// Get the distance to the star [parsecs]
				double d = (1000.0) * (1.0 / star.parallax);
				
				// Retrieve some fields for convenience
				double ra  = Math.toRadians(star.ra);
				double dec = Math.toRadians(star.dec);
				double mu_acosd = star.pmra * Units.MILLIARCSEC_TO_RADIANS;
				double mu_d = star.pmdec * Units.MILLIARCSEC_TO_RADIANS;
				
				// 2.1) Convert proper motion to Galactic coordinates
				double[] mu_lb = AstrometryUtils.convertPositionAndProperMotionEqToGal(ra, dec, mu_acosd, mu_d);
				double l = mu_lb[0];
				double b = mu_lb[1];
				double mu_lcosb = mu_lb[2];
				double mu_b = mu_lb[3];
				
				// 2.2) Subtract off the contribution from Galactic rotation, to leave the peculiar velocity [adopt Oort constants]
				mu_lcosb = mu_lcosb - (A * Math.cos(2 * l) + B) * Math.cos(b);
				mu_b = mu_b + A * Math.sin(2 * l) * Math.cos(b) * Math.sin(b);
				
				// 2.3) Convert to proper motion velocity vector
				Matrix p = AstrometryUtils.getTangentialVelocityVector(d, l, b, mu_lcosb, mu_b);
				meanP.plusEquals(p);
				
				// 2.4) Compute the projection matrix A along the line of sight towards this star
				Matrix A = AstrometryUtils.getProjectionMatrixA(l, b);
				meanA.plusEquals(A);
				
				// 2.5) Profit
				
			}
			
			// Compute means
			int n = stars.get(bin).size();
			meanP.timesEquals(1.0/n);
			meanA.timesEquals(1.0/n);
			
			// Solve for the mean Solar motion
			Matrix v = meanA.solve(meanP);
			
			System.out.println(String.format("%f\t%f\t%f\t%f", bv, v.get(0, 0), v.get(1, 0), v.get(2, 0)));
		}
		
	}
	
	/**
	 * Create and display an HR diagram using the loaded data.
	 * 
	 * @param selectedStars
	 * 	Selected stars, i.e. drawn in black
	 * @param nonSelectedStars
	 * 	Non selected stars, i.e. drawn in grey
	 * @param upper
	 * 	Upper (magnitude) boundary of the main sequence
	 * @param lower
	 * 	Lower (magnitude) boundary of the main sequence
	 */
	private static void displayHrDiagram(List<double[]> selectedStars, List<double[]> nonSelectedStars, Linear upper, Linear lower) {
		
		StringBuilder script = new StringBuilder();
		script.append("set terminal pngcairo enhanced color size 480,640").append(OSChecker.newline);
		script.append("set xrange [-0.1:2]").append(OSChecker.newline);
		script.append("set yrange [-2:10] reverse").append(OSChecker.newline);
		script.append("set key off").append(OSChecker.newline);
		script.append("set xtics out").append(OSChecker.newline);
		script.append("set ytics out").append(OSChecker.newline);
		script.append("set xlabel 'B-V'").append(OSChecker.newline);
		script.append("set ylabel 'M_{V}'").append(OSChecker.newline);
		script.append("plot '-' u 1:2 w d lc rgbcolor 'black' notitle, ");
		script.append("     '-' u 1:2 w d lc rgbcolor 'grey' notitle, ");
		script.append("     '-' u 1:2 w l lc rgbcolor 'green' notitle, ");
		script.append("     '-' u 1:2 w l lc rgbcolor 'green' notitle").append(OSChecker.newline);
		
		for(double[] data : selectedStars) {
			script.append(data[0] + " " + data[1]).append(OSChecker.newline);
		}
		script.append("e").append(OSChecker.newline);

		for(double[] data : nonSelectedStars) {
			script.append(data[0] + " " + data[1]).append(OSChecker.newline);
		}
		script.append("e").append(OSChecker.newline);
		
		for(double bv=-0.1; bv<2.1; bv+=0.01) {
			script.append(bv + " " + upper.interpolateY(bv)[0]).append(OSChecker.newline);
		}
		script.append("e").append(OSChecker.newline);
		
		for(double bv=-0.1; bv<2.1; bv+=0.01) {
			script.append(bv + " " + lower.interpolateY(bv)[0]).append(OSChecker.newline);
		}
		script.append("e").append(OSChecker.newline);
		
		try {
			Gnuplot.displayImage(Gnuplot.executeScript(script.toString()));
		} catch (IOException e) {
			System.out.println("Problem making HR diagram");
		}
	}

}
