package sdss.footprint.exec;



import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import projections.Aitoff;
import projections.util.ProjectionUtil;
import sdss.footprint.SdssFootprintUtils;
import sdss.footprint.Stripe;
import utils.AstrometryUtils;

/**
 * Test the SDSS footprint area code.
 *
 *
 * @author nrowell
 * @version $Id$
 */
public class TestSdssFootprint {
	
	/**
	 * Main application entry point.
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		
		// Loop over all data releases
		for(int dr=2; dr<8; dr++) {
			
			// Get the footprint area as a list of Stripes
			List<Stripe> footprint = SdssFootprintUtils.parseDataReleaseFootprint(dr);
			
			List<double[]> pointsInFootprint = new LinkedList<>();
			
			// Test a whole bunch of randomly distributed points for inclusion in footprint area
			for(int p=0; p<10000; p++) {
				
				double ra  = AstrometryUtils.getRandomRa();
				double dec = AstrometryUtils.getRandomDec();
				
				if(SdssFootprintUtils.isInSDSS(footprint, ra, dec)) {
					pointsInFootprint.add(new double[]{ra, dec});
				}
			}
			
			ProjectionUtil.makeAndDisplayJFreeChartPlot(pointsInFootprint, "SDSS DR"+dr+" Footprint", new Aitoff());
		}
	}
	
}
