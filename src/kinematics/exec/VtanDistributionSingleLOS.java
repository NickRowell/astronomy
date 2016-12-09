package kinematics.exec;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import kinematics.TangentialVelocityDistribution;
import Jama.Matrix;
import constants.Galactic;

public class VtanDistributionSingleLOS
{
	
	public static void main(String args[]) throws IOException
	{
		
		// Line of sight coordinates
		double ra  = Math.toRadians(260.0);
		double dec = Math.toRadians(-30.0);
		
		double step = 1.0;  // tangential velocity distribution tabulated at 1km/s intervals
		
		// Handles to velocity moments for selected population
		double[] pdf_thindisk = new double[1000];
		double[] cdf_thindisk = new double[1001];
		Matrix cov  = Galactic.thin_disk_covariance;
		Matrix mean = Galactic.thin_disk_mean;
		System.out.println("Computing thin disk...");
		TangentialVelocityDistribution.getVtanDistributionTowardsLos(cov, mean, ra, dec, step, pdf_thindisk, cdf_thindisk);
		
		double[] pdf_thickdisk = new double[1000];
		double[] cdf_thickdisk = new double[1001];
		cov  = Galactic.thick_disk_covariance;
		mean = Galactic.thick_disk_mean;
		System.out.println("Computing thick disk...");
		TangentialVelocityDistribution.getVtanDistributionTowardsLos(cov, mean, ra, dec, step, pdf_thickdisk, cdf_thickdisk);
		
		double[] pdf_halo = new double[1000];
		double[] cdf_halo = new double[1001];
		cov  = Galactic.spheroid_covariance;
		mean = Galactic.spheroid_mean;
		System.out.println("Computing spheroid...");
		TangentialVelocityDistribution.getVtanDistributionTowardsLos(cov, mean, ra, dec, step, pdf_halo, cdf_halo);
		
		File outf = new File("/home/nrowell/Astronomy/tangential_velocity_distributions/NR/260_-30.txt");
		
		BufferedWriter out = new BufferedWriter(new FileWriter(outf));
		
		
		for(int i=0; i<pdf_thindisk.length; i++)
		{
			// Translate index to tangential velocity at the centre of this bin
			double vt = i*step + (step/2.0);
			
			out.write(vt+"\t"+pdf_thindisk[i]+"\t"+pdf_thickdisk[i]+"\t"+pdf_halo[i]+"\n");
		}
		
		out.close();
	}

	
}
