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

import projections.Aitoff;
import projections.util.ProjectionUtil;
import projects.tycho2.dm.Tycho2Star;
import projects.tycho2.util.Tycho2Utils;

/**
 * This class provides an application that processes the Tycho-2 catalogue to generate a reference
 * star catalogue, stored either as an ASCII or binary file, that is suitable for use with Asteria
 * for use in calibrating the camera.
 *
 * @author nrowell
 * @version $Id$
 */
public class CreateReferenceStarCatalogue {

	/**
	 * The Logger
	 */
    protected static Logger logger = Logger.getLogger(CreateReferenceStarCatalogue.class.getCanonicalName());
    
    /**
     * The output directory to store the reference star catalogue and sky projection
     */
    static File outputDir = new File("/home/nrowell/Temp/");
    
    /**
     * Faint VT magnitude limit
     */
    static double magLimit = 7.0;
    
    /**
     * A {@link Comparator} used to sort the stars in the reference catalogue according to
     * declination or right ascension as desired.
     */
    static Comparator<Tycho2Star> tycStarComparator = new Comparator<Tycho2Star>() {
		@Override
		public int compare(Tycho2Star o1, Tycho2Star o2) {
			// Sort according to right ascension
			return Double.compare(o1.RAdeg, o2.RAdeg);
		}};
    
	/**
	 * Main application entry point.
	 * @param args
	 * 	The command line arguments (ignored)
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		// Load the Tycho-2 catalogue stars
		Collection<Tycho2Star> allTycho2Stars = Tycho2Utils.loadTycho2Catalogue();
		
		logger.info("Got "+allTycho2Stars.size()+" Tycho2Stars in total");
		
		// Filter the stars according to magnitude and any other fields that we need
		Set<Tycho2Star> selectedTycho2Stars = new TreeSet<>(tycStarComparator);
		
		for(Tycho2Star tyc2Star : allTycho2Stars) {
			
			// Select stars with valid mean RA and dec
			if(Double.isNaN(tyc2Star.RAmdeg) || Double.isNaN(tyc2Star.DEmdeg)) {
				continue;
			}
			
			// Select stars with both BT and VT magnitudes
			if(Double.isNaN(tyc2Star.BTmag) || Double.isNaN(tyc2Star.VTmag)) {
				continue;
			}
			
			// Select stars brighter that the magnitude limit
			if(tyc2Star.VTmag > magLimit) {
				continue;
			}
			
			selectedTycho2Stars.add(tyc2Star);
		}

		logger.info("Selected "+selectedTycho2Stars.size()+" Tycho2Stars for the reference catalogue");
		
		// Write the reference star catalogue
		BufferedWriter out = new BufferedWriter(new FileWriter( new File(outputDir, "RefStarCat.dat")));
		
		out.write("# This is the reference star catalogue for use by the Asteria software. It is derived from\n");
		out.write("# the Tycho-2 catalogue (Hog+ 2000) by selecting stars with both BT and VT magnitudes and\n");
		out.write("# applying a faint VT magnitude limit of "+magLimit+". The columns are as follows:\n");
		out.write("# (1) Right ascension at epoch J2000, ICRS, (the RAmdeg field from Tycho-2) [deg]\n");
		out.write("# (2) Declination at epoch J2000, ICRS, (the DEmdeg field from Tycho-2) [deg]\n");
		out.write("# (3) VT apparent magnitude [mag]\n");
		List<double[]> coords = new LinkedList<>();
		for(Tycho2Star tyc2Star : selectedTycho2Stars) {
			out.write(String.format("%.8f\t%.8f\t%.3f\n", tyc2Star.RAmdeg, tyc2Star.DEmdeg, tyc2Star.VTmag));
			coords.add(new double[]{Math.toRadians(tyc2Star.RAmdeg), Math.toRadians(tyc2Star.DEmdeg)});
		}
		out.close();
		
		// Generate a sky projection of the catalogue stars
		JFreeChart projection = ProjectionUtil.makeJFreeChartPlot(coords, "Asteria Reference Star Catalogue", new Aitoff());
		

		ChartUtilities.saveChartAsPNG(new File(outputDir, "RefStarCat.png"), projection, 1280, 960);
		
		
	}
	
}