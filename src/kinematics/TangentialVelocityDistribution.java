package kinematics;

import Jama.Matrix;
import astrometry.util.AstrometryUtils;
import constants.Galactic;
import numeric.stats.StatUtil;

/**
 * 
 * This class provides methods used to compute the distribution of tangential
 * velocities of stars along particular lines of sight.
 * 
 * Notation used here:
 * 
 * Subscript G indicates quantities in the Galactic reference frame
 *  - (basis vectors?)
 * Subscript E indicates quantities in the Equatorial reference frame
 *  - (basis vectors?)
 * Subscript N indicates quantities in the Normal reference frame at the given tangent point
 *  - Right handed set with basis vectors labelled p,q,r
 *  - p is parallel to the equator, positive east
 *  - q is perpendicular to the equator, positive north
 *  - r points along the LOS towards the tangent point
 *  - pq plane coincides with the tangent plane
 * 
 * @author nrowell
 *
 */
public class TangentialVelocityDistribution
{
	
	// Hard-coded parameters of numerical integration
	
	// Number of integration steps to take between tabulated PDF/CDF points.
	// Higher number = more accurate but slower.
	static int N_SUBSTEPS_VT = 10;
	
	// Step size when marginalising over position angle [radians].
	// Smaller = more accurate but slower.
	static double DELTA_POSITION_ANGLE = Math.toRadians(1);
	
	/**
	 * Computes the tangential velocity distribution for stars along a given LOS,
	 * using a numerical integration algorithm.
	 * 
	 * @param covariance_G		Population velocity ellipsoid in Galactic coordinates [(km/s)^2]
	 * @param mean_G			Population mean velocity relative to Sun, in Galactic coordinates [km/s]
	 * @param ra				Right ascension of LOS, equatorial coordinates [radians]
	 * @param dec				Declination of LOS, equatorial coordinates [radians]
	 * @param vt_major_step		Step size in tangential velocity between elements in PDF/CDF arrays [km/s]
	 * 
	 * @param pdf				On exit, contains tangential velocity PDF, tabulated at tangential
	 * 							velocity values from 0+step/2 to (pdf.length-1)*step+step/2 [km/s].
	 * 							Such that the element pdf[x] contains the probability (per unit vtan)
	 * 							that the tangential velocity lies in the range x*vt_major_step -> 
	 * 							(x+1)*vt_major_step.
	 * @param cdf				On exit, contains tangential velocity CDF, tabulated at tangential
	 * 							velocity values from 0 to (cdf.length-1)*step [km/s].
	 * 							Such that the element cdf[x] contains the probability that the
	 * 							tangential velocity lies in the range 0 -> x*vt_major_step.
	 * 							NOTE: cdf array must have one more element than pdf, to contain
	 * 							      initial point at 0. This forces to the user to be aware that
	 * 							      the CDF represents the integral over ranges in v_{tan} whereas
	 * 								  the PDF represents probability density at instantaneous points.
	 * 
	 * @return
	 */
	public static void getVtanDistributionTowardsLos(Matrix covariance_G, Matrix mean_G,
			     double ra, double dec, double vt_major_step, double[] pdf, double[] cdf)
	{
		
		assert(pdf.length+1==cdf.length) : "CDF array must have length = PDF.length+1!";
		
		// Transformation from Equatorial -> Normal frame
		Matrix r_N_E = AstrometryUtils.getNormalFrame(ra, dec);
		Matrix r_E_N = r_N_E.transpose();
		
		// Full transformation from Galactic -> Normal frame
		Matrix r_N_G = r_N_E.times(Galactic.r_E_G);
		Matrix r_G_N = r_N_G.transpose();
		
		// Population mean velocity in Normal frame
		Matrix mean_N = r_N_G.times(mean_G);
		
		// Population velocity covariance matrix in Normal frame
		Matrix covariance_N = r_N_G.times(covariance_G.times(r_G_N));
		
		// Extract components of population mean motion lying in tangent plane
		double mean_p = mean_N.get(0,0);
		double mean_q = mean_N.get(1,0);
		
		// Extract sub-matrix expressing covariance of peculiar velocity
		// components in the tangent plane (upper left 2x2 components).
		Matrix cov_pq = covariance_N.getMatrix(0, 1, 0, 1);
		double det_pq = cov_pq.det();
		// Bivariate Gaussian normalisation constant
		double N = (1.0/(2.0*Math.PI*Math.sqrt(det_pq)));
		
		double Cpp = covariance_N.get(0,0);
		double Cqq = covariance_N.get(1,1);
		double Cpq = covariance_N.get(1,0);
		
		// np = peculiar velocity parallel to equator, positive east,
		// nq = peculiar velocity perpendicular to equator, positive north.
		//
		// Bivariate probability density function of these peculiar velocity components given by:
		//
		// Fpq = (1.0/(2.0*Math.PI*Math.sqrt(sigdet*crr))) *
		//       Math.exp((-1.0/(2.0*crr*sigdet))*(Cqq*np*np   +   Cpp*nq*nq   -   2*Cpq*np*nq))
		//
		// This can be transformed to a probability density function for tangential velocity and
		// position angle using the transformations:
		//
		// np -> Vt*sin(position angle)      nq -> Vt*cos(position angle)
		//
		// The mean reflex motion is accounted for by adding a shift in each coordinate:
		//
		// np -> Vt*sin(position angle) + reflex_p      nq -> Vt*cos(position angle) + reflex_q
		//

		// Minor step size in V_tan (integration steps)
	    double vt_minor_step = (vt_major_step/N_SUBSTEPS_VT);
		
		// Loop over major steps in V_tan (tabulated points)
		for(int vt_major=0; vt_major<pdf.length; vt_major++)
		{
			// Lower boundary on V_tan for this step
			double vt_min = vt_major*vt_major_step;
			
			// Total integrated probability within this major step
			double pdf_step = 0;
			
			// Loop over minor steps in V_tan (integration steps)
			for(int vt_minor=0; vt_minor<N_SUBSTEPS_VT; vt_minor++)
			{
				
				// central V_tan for this integration step
				double vt = vt_min + (vt_minor*vt_minor_step) + (vt_minor_step/2.0);
				
				// Integrate over position angle
				
				// Peculiar velocity components stepped at three values of position angle
				double vp1,vq1,vp2,vq2,vp3,vq3;
				
				// Position angle step. Trapezium width = h*Vt
				double h = DELTA_POSITION_ANGLE;
				
			    double T_h  = 0;
			    double T_2h = 0;
			    
			    for(double pos = 0; pos<(2.0*Math.PI - h/2); pos+=h)
			    {
			    	// Area of the integration element in the (vp,vq) plane
			    	double A = vt * h * vt_minor_step;
			    	
			    	// transform tangential velocity and position angle
			    	// to components in tangent plane coordinates.
			    	// Include mean reflex motion in each direction
					vp1 = vt*Math.sin(pos)     + mean_p;
					vq1 = vt*Math.cos(pos)     + mean_q;
					vp2 = vt*Math.sin(pos+h/2) + mean_p;
					vq2 = vt*Math.cos(pos+h/2) + mean_q;
					vp3 = vt*Math.sin(pos+h)   + mean_p;
					vq3 = vt*Math.cos(pos+h)   + mean_q;
			    	
			    	// Compute Gaussian exponent terms, i.e.
			    	// v1' * cov_pq^{-1} * v1
					double v1_exp = (1.0/det_pq)*(Cqq*vp1*vp1 + Cpp*vq1*vq1 - 2*Cpq*vp1*vq1);
					double v2_exp = (1.0/det_pq)*(Cqq*vp2*vp2 + Cpp*vq2*vq2 - 2*Cpq*vp2*vq2);
					double v3_exp = (1.0/det_pq)*(Cqq*vp3*vp3 + Cpp*vq3*vq3 - 2*Cpq*vp3*vq3);
					
					//contributions to T(h) sum:
					T_h  += N*(Math.exp(-0.5*v1_exp) + Math.exp(-0.5*v2_exp))*0.5*(A/2.0);
					T_h  += N*(Math.exp(-0.5*v2_exp) + Math.exp(-0.5*v3_exp))*0.5*(A/2.0);
					
					//contribution to T(2h) sum:
					T_2h += N*(Math.exp(-0.5*v1_exp) + Math.exp(-0.5*v3_exp))*0.5*A;
					
			    }

			    // Contribution to PDF major step from this minor step.
			    // Integral is computed by Richardson's extrapolation.
			    pdf_step += (1.0/3.0) * (4.0 * T_h  - T_2h);
				
			}
			
			// Normalise to [(km s^{-1})^{-1}]
			pdf[vt_major] = pdf_step / vt_major_step;
			
			// Clamp tiny negative values to zero
			if(pdf[vt_major] < 0)
			{
				if(pdf[vt_major] < -1e-32)
					throw new RuntimeException("Tangential velocity distribution recorded a negative value "
							+ "at ("+vt_min+","+pdf[vt_major]+")");
				else
					pdf[vt_major] = 0;
			}
			
			
		}
		
		// Now compute CDF from PDF. Uses the following optimisation:
		//
		// CDF_i = SUM_{j=0}^i(PDF_j) = PDF_i + SUM_{j=0}^{i-1}(PDF_j) = PDF_i + CDF_{i-1}
		//
		cdf[0] = 0.0;
		for(int vt_major=0; vt_major<pdf.length; vt_major++)
		{
			cdf[vt_major+1] = pdf[vt_major]*vt_major_step + cdf[vt_major];
		}
		
		
	}
	
	/**
	 * See other method comments.
	 * 
	 * @param covariance_G		Population velocity ellipsoid in Galactic coordinates [(km/s)^2]
	 * @param mean_G			Population mean velocity relative to Sun, in Galactic coordinates [km/s]
	 * @param los				Line of sight (unit) vector in Equatorial frame [-]
	 * @param vt_major_step		Step size in tangential velocity between elements in PDF/CDF arrays [km/s]
	 * 
	 * @param pdf				On exit, contains tangential velocity PDF, tabulated at tangential
	 * 							velocity values from 0+step/2 to (pdf.length-1)*step+step/2 [km/s].
	 * 							Such that the element pdf[x] contains the probability (per unit vtan)
	 * 							that the tangential velocity lies in the range x*vt_major_step -> 
	 * 							(x+1)*vt_major_step.
	 * @param cdf				On exit, contains tangential velocity CDF, tabulated at tangential
	 * 							velocity values from 0 to (cdf.length-1)*step [km/s].
	 * 							Such that the element cdf[x] contains the probability that the
	 * 							tangential velocity lies in the range 0 -> x*vt_major_step.
	 * 							NOTE: cdf array must have one more element than pdf, to contain
	 * 							      initial point at 0. This forces to the user to be aware that
	 * 							      the CDF represents the integral over ranges in v_{tan} whereas
	 * 								  the PDF represents probability density at instantaneous points.
	 * @return
	 */
	public static void getVtanDistributionTowardsLos(Matrix covariance_G, Matrix mean_G,
		     Matrix los, double vt_major_step, double[] pdf, double[] cdf)
	{
		// Get right ascension and declination from line of sight vector
		Matrix r   = AstrometryUtils.cartesianToSphericalPolar(los);
		double ra  = r.get(1, 0);
		double dec = r.get(2, 0);
		
		getVtanDistributionTowardsLos(covariance_G, mean_G, ra, dec, vt_major_step, pdf, cdf);
	}
	
	
	
	/**
	 * Alternative interface to Monte Carlo tangential velocity distribution
	 * generator.
	 * 
	 * @param covariance_G		Population velocity ellipsoid in Galactic coordinates [(km/s)^2]
	 * @param mean_G			Population mean velocity relative to Sun, in Galactic coordinates [km/s]
	 * @param los				Line of sight (unit) vector in Equatorial frame [-]
	 * @param vt_major_step		Step size in tangential velocity between elements in PDF/CDF arrays [km/s]
	 * 
	 * @param pdf				On exit, contains tangential velocity PDF, tabulated at tangential
	 * 							velocity values from 0+step/2 to (pdf.length-1)*step+step/2 [km/s].
	 * 							Such that the element pdf[x] contains the probability (per unit vtan)
	 * 							that the tangential velocity lies in the range x*vt_major_step -> 
	 * 							(x+1)*vt_major_step.
	 * @param cdf				On exit, contains tangential velocity CDF, tabulated at tangential
	 * 							velocity values from 0 to (cdf.length-1)*step [km/s].
	 * 							Such that the element cdf[x] contains the probability that the
	 * 							tangential velocity lies in the range 0 -> x*vt_major_step.
	 * 							NOTE: cdf array must have one more element than pdf, to contain
	 * 							      initial point at 0. This forces to the user to be aware that
	 * 							      the CDF represents the integral over ranges in v_{tan} whereas
	 * 								  the PDF represents probability density at instantaneous points.
	 * @return
	 */
	public static void getVtanDistributionTowardsLosMonteCarlo
	(Matrix covariance_G, Matrix mean_G,
		     Matrix los, double vt_major_step, double[] pdf, double[] cdf)
	{
		// Get right ascension and declination from line of sight vector
		Matrix r   = AstrometryUtils.cartesianToSphericalPolar(los);
		double ra  = r.get(1, 0);
		double dec = r.get(2, 0);
		
		getVtanDistributionTowardsLosMonteCarlo(covariance_G, mean_G, ra, dec, vt_major_step, pdf, cdf);
	}
	
	
	/**
	 * Computes the tangential velocity distribution for stars along a given LOS,
	 * using a Monte Carlo algorithm.
	 * 
	 * 
	 * @param covariance_G		Population velocity ellipsoid in Galactic coordinates [(km/s)^2]
	 * @param mean_G			Population mean velocity relative to Sun, in Galactic coordinates [km/s]
	 * @param ra				Right ascension of LOS, equatorial coordinates [radians]
	 * @param dec				Declination of LOS, equatorial coordinates [radians]
	 * @param vt_major_step		Step size in tangential velocity between elements in PDF/CDF arrays [km/s]
	 * 
	 * @param pdf				On exit, contains tangential velocity PDF, tabulated at tangential
	 * 							velocity values from 0+step/2 to (pdf.length-1)*step+step/2 [km/s].
	 * 							Such that the element pdf[x] contains the probability (per unit vtan)
	 * 							that the tangential velocity lies in the range x*vt_major_step -> 
	 * 							(x+1)*vt_major_step.
	 * @param cdf				On exit, contains tangential velocity CDF, tabulated at tangential
	 * 							velocity values from 0 to (cdf.length-1)*step [km/s].
	 * 							Such that the element cdf[x] contains the probability that the
	 * 							tangential velocity lies in the range 0 -> x*vt_major_step.
	 * 							NOTE: cdf array must have one more element than pdf, to contain
	 * 							      initial point at 0. This forces to the user to be aware that
	 * 							      the CDF represents the integral over ranges in v_{tan} whereas
	 * 								  the PDF represents probability density at instantaneous points.
	 * @return
	 */
	public static void getVtanDistributionTowardsLosMonteCarlo(Matrix covariance_G, Matrix mean_G,
			     double ra, double dec, double vt_major_step, double[] pdf, double[] cdf)
	{
		
		assert(pdf.length+1==cdf.length) : "CDF array must have length = PDF.length+1!";
		
		// Zero PDF array elements (necessary in Monte Carlo case because we only
		// ever add to them, and never assign them directly).
		for(int i=0; i<pdf.length; i++)
		{
			pdf[i]=0.0;
		}
		
		// Transformation from Equatorial -> Normal frame
		Matrix r_N_E = AstrometryUtils.getNormalFrame(ra, dec);
		Matrix r_E_N = r_N_E.transpose();
		
		// Full transformation from Galactic -> Normal frame
		Matrix r_N_G = r_N_E.times(Galactic.r_E_G);
		
		// Seems possible Marco has the Equatorial/Normal frame transformation
		// the wrong way round
		
		// Number of random stars to generate
		int N = 1000000;
		
		// This is the contribution that each star makes to the pdf, such that the
		// final pdf is normalised.
		double c = (1.0/((double)N*vt_major_step));
		
		for(int i=0; i<N; i++)
		{
			// Draw random velocity for star in Galactic frame
			Matrix v_G = StatUtil.drawRandomVector(covariance_G, mean_G);
			
			// Transform to Normal frame
			Matrix v_N = r_N_G.times(v_G);
			
			// Get tangential velocity from pq components
			double p = v_N.get(0, 0);
			double q = v_N.get(1, 0);
			
			double vtan = Math.sqrt(p*p + q*q);
			
			// Add to tangential velocity distribution array
			int bin = (int)Math.floor(vtan/vt_major_step);
			
			pdf[bin] += c;
			
		}
		
		// Now compute CDF from PDF. Uses the following optimisation:
		//
		// CDF_i = SUM_{j=0}^i(PDF_j) = PDF_i + SUM_{j=0}^{i-1}(PDF_j) = PDF_i + CDF_{i-1}
		//
		cdf[0] = 0.0;
		for(int vt_major=0; vt_major<pdf.length; vt_major++)
		{
			cdf[vt_major+1] = pdf[vt_major]*vt_major_step+ cdf[vt_major];
		}
		
	}
	
}
