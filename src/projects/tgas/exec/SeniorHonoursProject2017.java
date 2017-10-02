package projects.tgas.exec;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import Jama.Matrix;
import astrometry.util.AstrometryUtils;
import constants.Galactic;
import constants.Units;
import infra.io.Gnuplot;
import infra.os.OSChecker;
import numeric.data.Range;
import numeric.data.RangeMap;
import numeric.functions.Linear;
import photometry.util.PhotometryUtils;
import projects.tgas.dm.TgasApassStar;
import projects.tgas.util.TgasUtils;

/**
 * This class performs an analysis of the kinematics of Solar Neighbourhood stars using the TGAS data.
 * 
 * TODO: add velocity dispersion computation
 * TODO: decide on colour bins to use
 * TODO: outlier rejection?
 *
 * @author nrowell
 * @version $Id$
 */
public class SeniorHonoursProject2017 {

	/**
	 * The logger.
	 */
	private static final Logger logger = Logger.getLogger(SeniorHonoursProject2017.class.getName());
	
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
	static double f_max = 5;
	
	/**
	 * Threshold on the astrometric excess noise.
	 */
	static double excess_noise_max = 1.0;
	
	/**
	 * Threshold on the B-V colour error
	 */
	static double cerr_max = 0.5;
	
	/**
	 * Distance threshold (to restrict to nearby stars) [parsec]
	 */
	static double d_max = 250.0;
	
	/**
	 * Points along upper boundary of Main Sequence used for selection
	 */
	static double[] x_upper = new double[]{-0.1, 0.8, 1.0, 1.5, 1.7};
	static double[] y_upper = new double[]{-0.5, 4.3, 6.0, 8.3, 10.0};

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
	 * Local class used to endow the {@link TgasApassStar} with a {@link Matrix} to store
	 * the proper motion velocity vector, for convenience.
	 *
	 * @author nrowell
	 * @version $Id$
	 */
	static class TgasApassStarWithVelocity extends TgasApassStar {
		
		public TgasApassStarWithVelocity(TgasApassStar star) {
			super(star.sourceId, star.ra, star.ra_error, star.dec, star.dec_error, star.parallax, star.parallax_error, star.pmra, star.pmra_error, 
					star.pmdec, star.pmdec_error, star.astrometric_excess_noise, star.phot_g_mean_mag, star.v_mag, star.e_v_mag, star.b_mag, star.e_b_mag);
			p = new Matrix(3,1);
			A = new Matrix(3,3);
		}
		
		/**
		 * The proper motion velocity vector.
		 */
		public Matrix p;
		
		/**
		 * The projection matrix A that projects the 3D velocity onto the celestial sphere.
		 */
		public Matrix A;
	}
	
	/**
	 * Main application entry point.
	 * @param args
	 * 	The command line arguments [ignored]
	 */
	public static void main(String[] args) {
		
		// Load the TGASxAPASS stars
		Collection<TgasApassStar> tgasStars = TgasUtils.loadTgasApassCatalogue();
		
		// Perform selection on parallax & colour error, main sequence, and bin by colour
		RangeMap<TgasApassStarWithVelocity> colourPartitionedStars = selectStarsAndBinByColour(tgasStars);
		
		// Compute the proper motion velocity vector and projection matrix for each star, and
		// solve for the mean stellar motion in each colour bin.
		Matrix[] meanVelocities = computeVelocities(colourPartitionedStars);
		
		// Correct the stellar motion for the mean motion in each colour bin; compute the
		// scalar velocity dispersion in each colour bin.
		double[] velocityDisp = computeVelocityDispersion(colourPartitionedStars, meanVelocities);
		
		// Plot the mean velocities and velocity dispersion
		plotMeanVelocities(colourPartitionedStars.getRanges(), meanVelocities, velocityDisp);
		
		// Plot the mean velocity as a function of velocity dispersion
		plotVelocityVsS2(colourPartitionedStars.getRanges(), meanVelocities, velocityDisp);
		
		// Plot the astrometric excess noise as a function of B-V colour
		plotExcessNoise(colourPartitionedStars);
		
	}
	
	/**
	 * Plots the astrometric excess noise as a function of B-V colour for selected stars.
	 * 
	 * @param colourPartitionedStars
	 * 	A {@link RangeMap} containing the selected stars partitioned by colour.
	 */
	private static void plotExcessNoise(RangeMap<TgasApassStarWithVelocity> colourPartitionedStars) {
		
		StringBuilder script = new StringBuilder();
		script.append("set terminal pngcairo enhanced color size 640,480").append(OSChecker.newline);
		script.append("set xrange [*:*]").append(OSChecker.newline);
		script.append("set yrange [*:*]").append(OSChecker.newline);
		script.append("set key top left").append(OSChecker.newline);
		script.append("set xtics out").append(OSChecker.newline);
		script.append("set ytics out").append(OSChecker.newline);
		script.append("set xlabel 'B - V'").append(OSChecker.newline);
		script.append("set ylabel 'Excess Noise'").append(OSChecker.newline);
		script.append("plot '-' u 1:2 w p pt 5 ps 0.5 notitle").append(OSChecker.newline);
		
		for(int bin=0; bin<colourPartitionedStars.size(); bin++) {
			for(TgasApassStarWithVelocity star : colourPartitionedStars.get(bin)) {
				double bv = star.b_mag - star.v_mag;
				script.append(bv + " " + star.astrometric_excess_noise).append(OSChecker.newline);
			}
		}
		try {
			Gnuplot.displayImage(Gnuplot.executeScript(script.toString()));
		} catch (IOException e) {
			System.out.println("Problem plotting excess noise");
		}
	}
	
	/**
	 * Plots the components of the mean velocity as a function of velocity dispersion.
	 * 
	 * @param colours
	 * 	The colour bins.
	 * @param meanVelocities
	 * 	Mean velocity in each colour bin.
	 * @param velocityDisp
	 * 	Velocity dispersion in each colour bin.
	 */
	private static void plotVelocityVsS2(Range[] colours, Matrix[] meanVelocities, double[] velocityDisp) {
		
		String[] labels = {"U", "V", "W"};
		
		for(int comp=0; comp<3; comp++) {
		
			StringBuilder script = new StringBuilder();
			script.append("set terminal pngcairo enhanced color size 640,480").append(OSChecker.newline);
			script.append("set xrange [*:*]").append(OSChecker.newline);
			script.append("set yrange [*:*]").append(OSChecker.newline);
			script.append("set key top left").append(OSChecker.newline);
			script.append("set xtics out").append(OSChecker.newline);
			script.append("set ytics out").append(OSChecker.newline);
			script.append("set xlabel 'S^{2}'").append(OSChecker.newline);
			script.append("set ylabel '"+labels[comp]+" [km s^{-1}]'").append(OSChecker.newline);
			script.append("plot '-' u 1:(-$2) w lp pt 5 ps 0.5 notitle").append(OSChecker.newline);
			
			for(int i=0; i<colours.length; i++) {
				script.append(velocityDisp[i] + " " + meanVelocities[i].get(comp, 0)).append(OSChecker.newline);
			}
			
			try {
				Gnuplot.displayImage(Gnuplot.executeScript(script.toString()));
			} catch (IOException e) {
				System.out.println("Problem plotting "+labels[comp]+" vs velocity dispersion");
			}
		}
	}

	/**
	 * Plots the components of the mean velocity and velocity dispersion as a function of colour.
	 * @param colours
	 * 	The colour bins.
	 * @param meanVelocities
	 * 	Mean velocity in each colour bin.
	 * @param velocityDisp
	 * 	Velocity dispersion in each colour bin.
	 */
	private static void plotMeanVelocities(Range[] colours, Matrix[] meanVelocities, double[] velocityDisp) {
		StringBuilder script = new StringBuilder();
		script.append("set terminal pngcairo enhanced color size 640,480").append(OSChecker.newline);
		script.append("set xrange [*:*]").append(OSChecker.newline);
		script.append("set yrange [*:*]").append(OSChecker.newline);
		script.append("set key top left").append(OSChecker.newline);
		script.append("set xtics out").append(OSChecker.newline);
		script.append("set ytics out").append(OSChecker.newline);
		script.append("set xlabel 'B-V'").append(OSChecker.newline);
		script.append("set ylabel 'km s^{-1}'").append(OSChecker.newline);
		script.append("plot '-' u 1:(-$2) w lp pt 5 ps 0.5 title 'U',");
		script.append("     '-' u 1:(-$2) w lp pt 5 ps 0.5 title 'V',");
		script.append("     '-' u 1:(-$2) w lp pt 5 ps 0.5 title 'W',");
		script.append("     '-' u 1:2 w lp pt 5 ps 0.5 title 'S'").append(OSChecker.newline);
		
		for(int i=0; i<colours.length; i++) {
			script.append(colours[i].mid() + " " + meanVelocities[i].get(0, 0)).append(OSChecker.newline);
		}
		script.append("e").append(OSChecker.newline);

		for(int i=0; i<colours.length; i++) {
			script.append(colours[i].mid() + " " + meanVelocities[i].get(1, 0)).append(OSChecker.newline);
		}
		script.append("e").append(OSChecker.newline);

		for(int i=0; i<colours.length; i++) {
			script.append(colours[i].mid() + " " + meanVelocities[i].get(2, 0)).append(OSChecker.newline);
		}
		script.append("e").append(OSChecker.newline);
		
		for(int i=0; i<colours.length; i++) {
			script.append(colours[i].mid() + " " + Math.sqrt(velocityDisp[i])).append(OSChecker.newline);
		}
		script.append("e").append(OSChecker.newline);
		
		try {
			Gnuplot.displayImage(Gnuplot.executeScript(script.toString()));
		} catch (IOException e) {
			System.out.println("Problem making mean velocities plot");
		}
	}
	/**
	 * Perform selection on the full set of {@link TgasApassStar}s and bin by colour.
	 * @param tgasStars
	 * 	The full set of {@link TgasApassStar}s
	 * @return
	 * 	A {@link RangeMap<TgasApassStar>} containing the selected stars binned by colour.
	 */
	private static RangeMap<TgasApassStarWithVelocity> selectStarsAndBinByColour(Collection<TgasApassStar> tgasStars) {
		
		// Create Linear objects for use in delineating the main sequence
		Linear upperMs = new Linear(x_upper, y_upper);
		Linear lowerMs = new Linear(x_lower, y_lower);
		
		// 0) Map the stars by colour, and make selection on parallax fractional error etc
		RangeMap<TgasApassStarWithVelocity> colourPartitionedStars = new RangeMap<>(bv_min, bv_max, bv_step);

		List<double[]> selectedStars = new LinkedList<>();
		List<double[]> nonSelectedStars = new LinkedList<>();
		
		for(TgasApassStar star : tgasStars) {
			
			// APASS B-V colour
			double bv = star.b_mag - star.v_mag;
			// APASS B-V colour error
			double bvErr = Math.sqrt(star.e_b_mag * star.e_b_mag + star.e_v_mag * star.e_v_mag);
			
			// Data quality thresholds: selection on parallax, colour error and excess noise
			if(star.parallax/star.parallax_error < f_max) {
				continue;
			}
			
			if(bvErr > cerr_max) {
				continue;
			}
			
			if(star.astrometric_excess_noise > excess_noise_max) {
				continue;
			}
			
			// Absolute V magnitude
			double vMag = PhotometryUtils.getAbsoluteMagnitudeFromPi(star.parallax/1000.0, star.v_mag);
			
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
			colourPartitionedStars.add(bv, new TgasApassStarWithVelocity(star));
			selectedStars.add(new double[]{bv, vMag});
		}

		logger.info("Selected "+selectedStars.size()+" TGASxAPASS stars for analysis");
		
		// Display HR diagram of selected and unselected stars
		displayHrDiagram(selectedStars, nonSelectedStars, upperMs, lowerMs);
		
		return colourPartitionedStars;
	}
	
	/**
	 * Compute the proper motion velocity vector and projection matrix for each {@link TgasApassStarWithVelocity}.
	 * 
	 * @param colourPartitionedStars
	 * 	A {@link RangeMap} containing all the selected {@link TgasApassStarWithVelocity} partitioned by colour.
	 */
	private static Matrix[] computeVelocities(RangeMap<TgasApassStarWithVelocity> colourPartitionedStars) {

		// Compute the mean velocity of the stars in each colour bin
		Matrix[] meanVelocity = new Matrix[colourPartitionedStars.size()];
		
		for(int bin=0; bin<colourPartitionedStars.size(); bin++) {
			
			// Mean projection matrix A
			Matrix meanA = new Matrix(3, 3);
			
			// Mean proper motion velocity vector p
			Matrix meanP = new Matrix(3, 1);
			
			// 1.1) For each object in given colour bin:
			for(TgasApassStarWithVelocity star : colourPartitionedStars.get(bin)) {
				
				// Get the distance to the star [parsecs]
				double d = 1000.0 / star.parallax;
				
				// Retrieve some fields for convenience
				double ra  = Math.toRadians(star.ra);
				double dec = Math.toRadians(star.dec);
				double mu_acosd = star.pmra * Units.MILLIARCSEC_TO_RADIANS;
				double mu_d = star.pmdec * Units.MILLIARCSEC_TO_RADIANS;
				
				// 1.2) Convert proper motion to Galactic coordinates
				double[] mu_lb = AstrometryUtils.convertPositionAndProperMotionEqToGal(ra, dec, mu_acosd, mu_d);
				double l = mu_lb[0];
				double b = mu_lb[1];
				double mu_lcosb = mu_lb[2];
				double mu_b = mu_lb[3];
				
				// 1.3) Subtract off the contribution from Galactic rotation, to leave the peculiar velocity [adopt Oort constants]
				//      NOTE we include the cos(b) term in the mu_lcosb correction, which differs from equation (1) in Dehnen & Binney (1998)
				//      as they are correcting proper motion that does not include the cos(b) term, i.e. just mu_l.
				mu_lcosb = mu_lcosb - (A * Math.cos(2 * l) + B) * Math.cos(b);
				mu_b = mu_b + A * Math.sin(2 * l) * Math.cos(b) * Math.sin(b);
				
				// 1.4) Convert to proper motion velocity vector
				star.p =  AstrometryUtils.getTangentialVelocityVector(d, l, b, mu_lcosb, mu_b);
				
				// 1.5) Compute the projection matrix A along the line of sight towards this star
				star.A = AstrometryUtils.getProjectionMatrixA(l, b);
				
				meanP.plusEquals(star.p);
				meanA.plusEquals(star.A);
			}

			// 1.6) Compute means
			int n = colourPartitionedStars.get(bin).size();
			meanP.timesEquals(1.0/n);
			meanA.timesEquals(1.0/n);
			
			// 1.7) Solve for the mean Solar motion
			Matrix v = meanA.solve(meanP);
			meanVelocity[bin] = v;
		}

		return meanVelocity;
	}
	
	/**
	 * Correct the proper motion velocity vector of each star for the mean motion; compute the scalar
	 * velocity dispersion for the stars in each colour bin.
	 * 
	 * @param colourPartitionedStars
	 * 	A {@link RangeMap} containing all the selected {@link TgasApassStar} partitioned by colour.
	 * @param meanVelocities
	 * 	An array of {@link Matrix}s containing the mean motion for the stars in each colour partition.
	 * @return
	 * 	The (scalar) velocity dispersion relative to the mean for the stars in each colour bin.
	 */
	private static double[] computeVelocityDispersion(RangeMap<TgasApassStarWithVelocity> colourPartitionedStars, Matrix[] meanVelocities) {
		
		double[] velocityDisp = new double[colourPartitionedStars.size()];
		
		for(int bin=0; bin<colourPartitionedStars.size(); bin++) {
			
			Matrix meanVelocity = meanVelocities[bin];
			
			// 1.1) For each object in given colour bin:
			for(TgasApassStarWithVelocity star : colourPartitionedStars.get(bin)) {
				
				// Subtract the (projected) mean motion from the stellar proper motion velocity vector
				star.p = star.p.minus(star.A.times(meanVelocity));
				
				velocityDisp[bin] += star.p.normF() * star.p.normF();
			}
			int n = colourPartitionedStars.get(bin).size();
			velocityDisp[bin] /= n;
		}
		
		return velocityDisp;
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
