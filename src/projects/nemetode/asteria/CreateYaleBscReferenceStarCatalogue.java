package projects.nemetode.asteria;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;

import astrometry.util.AstrometryUtils;
import projections.Aitoff;
import projections.util.ProjectionUtil;
import projects.tycho2.dm.Tycho2Star;
import projects.tycho2.util.Tycho2Utils;
import projects.ybsc.dm.YaleBscStar;
import projects.ybsc.util.YaleBscUtils;

/**
 * This class provides an application that processes the Yale Bright Star Catalogue to generate a reference
 * star catalogue, stored either as an ASCII or binary file, that is suitable for use with Asteria
 * for use in calibrating the camera.
 *
 * @author nrowell
 * @version $Id$
 */
public class CreateYaleBscReferenceStarCatalogue {

	/**
	 * The Logger
	 */
    protected static Logger logger = Logger.getLogger(CreateYaleBscReferenceStarCatalogue.class.getCanonicalName());
    
    /**
     * The output directory to store the reference star catalogue and sky projection
     */
    static File outputDir = new File("/home/nrowell/Temp/");
    
    /**
     * A {@link Comparator} used to sort the stars in the reference catalogue according to
     * declination or right ascension as desired.
     */
    static Comparator<YaleBscStar> yaleBscStarComparator = new Comparator<YaleBscStar>() {
		@Override
		public int compare(YaleBscStar o1, YaleBscStar o2) {
			
			double ra1 = AstrometryUtils.hmsToRadians(o1.RAh2000, o1.RAm2000, o1.RAs2000);
			double ra2 = AstrometryUtils.hmsToRadians(o2.RAh2000, o2.RAm2000, o2.RAs2000);
			
			// Sort according to right ascension
			return Double.compare(ra1, ra2);
		}
	};
    
	/**
	 * Main application entry point.
	 * @param args
	 * 	The command line arguments (ignored)
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {

		// Load the Yale Bright Star Catalogue objects
		Collection<YaleBscStar> yaleBscStars = YaleBscUtils.loadYaleBrightStarCatalogue();
		
		logger.info("Got "+yaleBscStars.size()+" YaleBscStars in total");
		
		// Filter the stars according to magnitude and any other fields that we need
		Set<YaleBscStar> selectedTycho2Stars = new TreeSet<>(yaleBscStarComparator);
		
		for(YaleBscStar yaleBrightStar : yaleBscStars) {
			
			// Select objects with a valid coordinates. This rules out a few novae, 47 Tuc etc.
			if(yaleBrightStar.RAh2000 == Integer.MIN_VALUE) {
				continue;
			}
			
			selectedTycho2Stars.add(yaleBrightStar);
		}

		logger.info("Selected "+selectedTycho2Stars.size()+" Tycho2Stars for the reference catalogue");
		
		// Write the reference star catalogue
		BufferedWriter out = new BufferedWriter(new FileWriter( new File(outputDir, "RefStarCat.dat")));
		
		out.write("# This is the reference star catalogue for use by the Asteria software. It is derived from\n");
		out.write("# the Yale Bright Star Catalogue. The columns are as follows:\n");
		out.write("# (1) Right ascension at epoch 2000.0, equinox J2000 [deg]\n");
		out.write("# (2) Declination at epoch 2000.0, equinox J2000 [deg]\n");
		out.write("# (3) Apparent visual magnitude (V band) [mag]\n");
		List<double[]> coords = new LinkedList<>();
		for(YaleBscStar yaleBrightStar : selectedTycho2Stars) {
			
			double ra = AstrometryUtils.hmsToRadians(yaleBrightStar.RAh2000, yaleBrightStar.RAm2000, yaleBrightStar.RAs2000);
			int sign = yaleBrightStar.DE_2000.equals("-") ? -1 : 1;
			double dec = AstrometryUtils.dmsToRadians(sign, yaleBrightStar.DEd2000, yaleBrightStar.DEm2000, yaleBrightStar.DEs2000);
			
			double v = yaleBrightStar.Vmag;
			
			out.write(String.format("%.8f\t%.8f\t%.3f\n", Math.toDegrees(ra), Math.toDegrees(dec), v));
			coords.add(new double[]{ra, dec});
		}
		out.close();
		
		// Generate a sky projection of the catalogue stars
		JFreeChart projection = ProjectionUtil.makeJFreeChartPlot(coords, "Asteria Reference Star Catalogue", new Aitoff());
		

		ChartUtilities.saveChartAsPNG(new File(outputDir, "RefStarCat.png"), projection, 1280, 960);
		
		
	}
	
}