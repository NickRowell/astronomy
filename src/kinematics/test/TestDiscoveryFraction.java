package kinematics.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import kinematics.TangentialVelocityDistribution;
import constants.Galactic;
import Jama.Matrix;
import astrometry.util.AstrometryUtils;

public class TestDiscoveryFraction
{
	
	
	
	public static void main(String args[]) throws IOException
	{
		
		double step = 1.0;  // tangential velocity distribution tabulated at 1km/s intervals
		
		double[] pdf = new double[1000];
		double[] cdf = new double[1000];
		
		// Handles to velocity moments for selected population
		Matrix cov  = new Matrix(new double[][]{{10, 0.0, 0.0},
												{0.0, 10, 0.0},
												{0.0, 0.0, 10}});
		Matrix mean = new Matrix(new double[][]{{0},
												{50},
												{0}});
		
		// Vtan distribution in direction towards Galactic centre
		TangentialVelocityDistribution.getVtanDistributionTowardsLos(cov, mean, Galactic.U, step, pdf, cdf);
		
		// Vtan distribution in direction of rotation
		TangentialVelocityDistribution.getVtanDistributionTowardsLos(cov, mean, Galactic.V, step, pdf, cdf);
		
		// Vtan distribution in direction towards north Galactic pole
		TangentialVelocityDistribution.getVtanDistributionTowardsLos(cov, mean, Galactic.W, step, pdf, cdf);
		
		File outf = new File("/home/nrowell/Astronomy/tangential_velocity_distributions/20_50_NICK.txt");
		
		BufferedWriter out = new BufferedWriter(new FileWriter(outf));
		
		
		for(int i=0; i<pdf.length; i++)
		{
			// Translate index to tangential velocity at the centre of this bin
			double vt = i*step + (step/2.0);
			
			out.write(vt+"\t"+pdf[i]+"\t"+cdf[i]+"\n");
		}
		
		out.close();
	}

	
}
