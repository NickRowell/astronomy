package projections.exec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import astrometry.util.AstrometryUtils;
import projections.Aitoff;
import projections.Projection;
import projections.util.ProjectionUtil;

/**
 * Simple application to test the generation of sky projection plots using utilities in {@link ProjectionUtil}.
 *
 *
 * @author nrowell
 * @version $Id$
 */
public class TestProjection {

	/**
	 * Application interface provides a test function.
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		// Generate a set of points randomly distributed.
		int N=1000;
		
		List<double[]> points = new ArrayList<>(N);
		
		for(int n=0; n<N; n++) {
			points.add(AstrometryUtils.getRandomRaDec());
		}
		
		Projection proj = new Aitoff();
		
		ProjectionUtil.makeAndDisplayJFreeChartPlot(points, "Projection Plot Tester", proj);
	}
	
}
