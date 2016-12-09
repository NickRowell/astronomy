package kinematics.exec;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import utils.MagnitudeUtils;
import kinematics.SampleTangentialVelocityDistribution;
import kinematics.TangentialVelocityDistribution;
import Jama.Matrix;
import constants.Galactic;
import density.DensityProfile;
import density.Uniform;

public class SampleVtanDistributionSingleLOS
{
	
	public static void main(String args[]) throws IOException
	{
		
		// Line of sight coordinates
		double ra  = Math.toRadians(260.0);
		double dec = Math.toRadians(-30.0);
		
		// Survey parameters
		double mu_min = 0.1;          // Proper motion limits [arcsec/yr]
		double mu_max = 1.0;
		double m_min  = 10.0;		  // Apparent magnitude limits [mags]
		double m_max  = 20.0;
		double omega = 0.01;          // Survey footprint size [steradians]. All-sky has omega = 4*pi.
		
		// Object parameters
		double M = 12;				  // Absolute magnitude (used to restrict distance range)
		
		// Distance limits [parsecs].
		double d_min = MagnitudeUtils.getDistance(m_min, M);
		double d_max = MagnitudeUtils.getDistance(m_max, M);
		
		System.out.println("d_min/d_max = "+d_min+"/"+d_max);
		
		// Steller population parameters
		Matrix cov  = Galactic.spheroid_covariance;
		Matrix mean = Galactic.spheroid_mean;
		DensityProfile density = new Uniform();
		
		// Output file for sample tangential velocity distribution
		File outf1 = new File("/home/nrowell/Astronomy/sample_vtan_distributions/260_-30.txt");
		// Output file for number of objects as function of distance and tangential velocity
		File outf2 = new File("/home/nrowell/Astronomy/sample_vtan_distributions/260_-30_range.txt");
		
		BufferedWriter out1 = new BufferedWriter(new FileWriter(outf1));
		BufferedWriter out2 = new BufferedWriter(new FileWriter(outf2));
		
		double step = 1.0;  // tangential velocity distribution tabulated at 1km/s intervals
		
		// True tangential velocity distribution
		double[] pdf_true = new double[2000];
		double[] cdf_true = new double[2001];
		System.out.println("Computing true v_{tan} distribution...");
		TangentialVelocityDistribution.getVtanDistributionTowardsLos(cov, mean, ra, dec, step, pdf_true, cdf_true);
		
		// Sample tangential velocity distribution
		double[] pdf_sample = new double[2000];
		double[] cdf_sample = new double[2001];
		System.out.println("Computing sample v_{tan} distribution...");
		SampleTangentialVelocityDistribution.getSampleVtanDistributionTowardsLos(cov, mean, ra, dec, mu_min, mu_max, d_min, d_max, omega, density, step, pdf_sample, cdf_sample, out2);
		
		for(int i=0; i<pdf_sample.length; i++)
		{
			// Translate index to tangential velocity at the centre of this bin
			double vt = i*step + (step/2.0);
			
			out1.write(vt+"\t"+pdf_true[i]+"\t"+pdf_sample[i]+"\n");
		}
		
		out1.close();
	}

	
}
