package projects.hip.exec;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;

import astrometry.DistanceFromParallax;
import astrometry.DistanceFromParallax.METHOD;
import projects.hip.dm.HipStar;
import projects.hip.util.HipUtils;

/**
 * Sandbox for experimenting with Hipparcos catalogue analyses.
 *
 *
 * @author nrowell
 * @version $Id$
 */
public class Sandpit {

	/**
	 * Main application entry point.
	 * @param args
	 * 	The command line args (ignored)
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		Collection<HipStar> hipStars = HipUtils.loadHipCatalogue();
		
		BufferedWriter out = new BufferedWriter(new FileWriter(new File("/home/nrowell/Gaia/ELSF/2021.03.01_SIF_FARPSF/input/bv_by_hip_id.txt")));
		
		
		for(HipStar hipStar : hipStars) {
			
//			double p = hipStar.Plx/1000.0;
//			double sigma_p = hipStar.e_Plx/1000.0;
//			
//			// Distance using naive method
//			double d_naive = DistanceFromParallax.getDistance(p, sigma_p, METHOD.NAIVE);
//			double d_cv = DistanceFromParallax.getDistance(p, sigma_p, METHOD.CONSTANT_VOLUME_DENSITY_PRIOR);
//			double d_exp = DistanceFromParallax.getDistance(p, sigma_p, METHOD.EXP_DEC_VOLUME_DENSITY_PRIOR);
			
			
			
			out.write(hipStar.HIP+"\t"+hipStar.bv+"\n");
		}
		out.close();
	}
	
}
