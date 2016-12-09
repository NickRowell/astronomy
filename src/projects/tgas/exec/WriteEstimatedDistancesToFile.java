package projects.tgas.exec;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import astrometry.DistanceFromParallax;
import astrometry.DistanceFromParallax.METHOD;
import projects.tgas.dm.TgasStar;
import projects.tgas.util.TgasUtils;
import utils.MagnitudeUtils;

/**
 * This class processes the TGAS preliminary data file and produces a file containing the distances and absolute
 * magnitudes assuming different distance priors. This is useful for plotting the HR for the different priors
 * for use in the Gaia DR1 workshops.
 *
 *
 * @author nrowell
 * @version $Id$
 */
public class WriteEstimatedDistancesToFile {

	/**
	 * The logger.
	 */
	private static final Logger logger = Logger.getLogger(WriteEstimatedDistancesToFile.class.getName());
	
	/**
	 * The output File.
	 */
	private static final File output = new File("/home/nrowell/Temp/tgas.txt");
	
	/**
	 * The main application entry point.
	 * @param args
	 * 	The command line arguments [ignored]
	 */
	public static void main(String[] args) {
		
		// Load the TGAS stars
		Collection<TgasStar> tgasStars = TgasUtils.loadTgasCatalogue();
		
		try(BufferedWriter out = new BufferedWriter(new FileWriter(output))) {
			
			// Write file header
			out.write("# 'B - V [mag]' 'Error on B - V [mag]' 'Fractional G flux error' 'Fractional parallax error' 'd (uniform prior) [pc]' 'd (constant volume density) [pc]' 'd (exponentially truncated) [pc]'"
					+ " 'G (uniform prior) [mag]' 'G (constant volume density) [mag]' 'G (exponentially truncated) [mag]'"
					+ " 'B (uniform prior) [mag]' 'B (constant volume density) [mag]' 'B (exponentially truncated) [mag]' 'G (simple) [mag]' 'HIP ID'\n");
			
			for(TgasStar tgasStar : tgasStars) {
				
				// G magnitude
				double g = tgasStar.phot_g_mean_mag;
				
				// Fractional G flux error
				double g_f_err = 1.0; //tgasStar.Gflux_err / tgasStar.Gflux;
				
				// B magnitude
				double b = tgasStar.bt_mag;
				
				// B-V magnitude
				double bv = tgasStar.bt_mag - tgasStar.vt_mag;
				
				// Error on B-V magnitude
				double e_bv = Math.sqrt(tgasStar.e_bt_mag*tgasStar.e_bt_mag + tgasStar.e_vt_mag*tgasStar.e_vt_mag);
				
				// Parallax and parallax error [arcseconds]
				double pi = tgasStar.parallax / 1000.0;
				double sigma_pi = tgasStar.parallax_error / 1000.0;
				
				// Fractional parallax error
				double f = Math.abs(sigma_pi / pi);
				
				// Distances assuming different types of distance prior
				double d_uniform = DistanceFromParallax.getDistance(pi, sigma_pi, METHOD.NAIVE);
				double d_cv = DistanceFromParallax.getDistance(pi, sigma_pi, METHOD.CONSTANT_VOLUME_DENSITY_PRIOR);
				double d_expdecay = DistanceFromParallax.getDistance(pi, sigma_pi, METHOD.EXP_DEC_VOLUME_DENSITY_PRIOR);
				
				
				// Absolute G magnitudes assuming different distance priors
				double g_uniform = MagnitudeUtils.getAbsoluteMagnitude(d_uniform, g);
				double g_cv = MagnitudeUtils.getAbsoluteMagnitude(d_cv, g);
				double g_expdecay = MagnitudeUtils.getAbsoluteMagnitude(d_expdecay, g);
				double g_simple = g + 5.0 * Math.log10(pi) + 5;
				
				double b_uniform = MagnitudeUtils.getAbsoluteMagnitude(d_uniform, b);
				double b_cv = MagnitudeUtils.getAbsoluteMagnitude(d_cv, b);
				double b_expdecay = MagnitudeUtils.getAbsoluteMagnitude(d_expdecay, b);
				
				
				out.write(String.format("%f\t%f\t%f\t%f\t%f\t%f\t%f\t%f\t%f\t%f\t%f\t%f\t%f\t%f\t%d\n", bv, e_bv, g_f_err, f, d_uniform, d_cv, d_expdecay, g_uniform, g_cv, g_expdecay, b_uniform, b_cv, b_expdecay, g_simple, tgasStar.tycho2_id));
			}
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Exception writing TGAS stars to file", e);
		}
	}
}