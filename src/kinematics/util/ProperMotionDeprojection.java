/*
 * Gaia CU5 DU10
 *
 * (c) 2005-2020 Gaia Data Processing and Analysis Consortium
 *
 *
 * CU5 photometric calibration software is free software; you can redistribute
 * it and/or modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * CU5 photometric calibration software is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this CU5 software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *
 *-----------------------------------------------------------------------------
 */

package kinematics.util;

import java.util.Collection;


import Jama.Matrix;
import kinematics.dm.AstrometricStar;

/**
 * This class provides utilities to compute the deprojection of proper motions and estimate the population mean
 * velocity and moments of the velocity distribution.
 *
 * @author nrowell
 * @version $Id$
 */
public final class ProperMotionDeprojection {

	/**
	 * Private constructor enforces non-instantiation.
	 */
	private ProperMotionDeprojection() { }
	
	/**
	 * Solve for the mean velocity relative to the Sun, and the covariance on that, for the {@link Collection} of stars.
	 * This method requires the projection matrix A and proper motion velocity vector P to have been set for each 
	 * {@link AstrometricStar}.
	 * 
	 * @param stars
	 * 	A {@link Collection} containing all the {@link AstrometricStar}s.
	 * @return
	 *   A two-element array of {@link Matrix}s: the first element contains the mean velocity relative to the sun (a 3x1 
	 *   {@link Matrix}), the second element contains the covariance on the mean velocity (a 3x3 {@link Matrix}) respectively.
	 *   All velocities are in kilometres per second.
	 */
	public static Matrix[] computeMeanVelocity(Collection<? extends AstrometricStar> stars) {

		// Mean projection matrix A
		Matrix meanA = new Matrix(3, 3);
		
		// Mean proper motion velocity vector p
		Matrix meanP = new Matrix(3, 1);
		
		// For each object:
		for(AstrometricStar star : stars) {
			meanP.plusEquals(star.getP());
			meanA.plusEquals(star.getA());
		}

		// Compute means
		int n = stars.size();
		meanP.timesEquals(1.0/n);
		meanA.timesEquals(1.0/n);
		
		// Solve for the mean Solar motion
		Matrix meanV = null;
		Matrix var_meanV = null;
		try {
			
			// Investigating what numpy dot function does if incorrectly used to multiply invA and meanP
//			Matrix invA = meanA.inverse();
//			double u = invA.get(0, 2) * meanP.get(0, 0);
//			double v = invA.get(1, 2) * meanP.get(1, 0);
//			double w = invA.get(2, 2) * meanP.get(2, 0);
//			meanV = new Matrix(new double[][] {{u}, {v}, {w}});
			
			meanV = meanA.solve(meanP);
			
			// Compute the error on the mean velocity
			double meanPprimeSquared = 0.0;
			for(AstrometricStar star : stars) {
				Matrix pPrime = star.getP().minus(star.getA().times(meanV));
				star.setPPrime(pPrime);
				meanPprimeSquared += pPrime.normF()*pPrime.normF();
			}
			meanPprimeSquared /= n;
			var_meanV = meanA.inverse().times(meanPprimeSquared/n);
		}
		catch(RuntimeException e) {
			// Singular matrix - leave results null
		}
		
		return new Matrix[]{meanV, var_meanV};
	}
	
	/**
	 * Compute the scalar velocity dispersion for the stars in each magnitude bin. This method requires the
	 * {@link AstrometricStar#setPPrime(Matrix)} method to have been called for each {@link AstrometricStar},
	 * by a previous call to {@link #computeMeanVelocity(Collection)}.
	 * 
	 * @param stars
	 * 	A {@link RangeMap} containing all the selected {@link ExtendedGaiaSource} partitioned by magnitude.
	 * @return
	 * 	The velocity dispersion relative to the mean for the stars in each magnitude bin, including the
	 * variance in the second element.
	 */
	public static double[] computeScalarVelocityDispersion(Collection<? extends AstrometricStar> stars) {
		
		if(stars.isEmpty()) {
			return new double[]{Double.NaN, Double.NaN};
		}
		
		// Compute mean p' to the second and fourth powers
		double meanP2 = 0.0, meanP4 = 0.0;
		
		for(AstrometricStar star : stars) {
			
			double p_prime = star.getPPrime().normF();
			meanP2 += p_prime * p_prime;
			meanP4 += p_prime * p_prime * p_prime * p_prime;
		}
		
		int n = stars.size();
		meanP2 /= n;
		meanP4 /= n;
		
		return new double[]{meanP2, (meanP4 - meanP2 * meanP2)/n};
	}
	
	/**
	 * Compute the velocity dispersion tensor for the stars in each magnitude bin. This method requires the
	 * {@link AstrometricStar#setPPrime(Matrix)} method to have been called for each {@link AstrometricStar},
	 * by a previous call to {@link #computeMeanVelocity(Collection)}.
	 * 
	 * @param magBinnedStars
	 * 	A {@link RangeMap} containing all the selected {@link ExtendedGaiaSource} partitioned by magnitude.
	 * @return
	 *   A two-element array of {@link Matrix}s: the first element contains the 6 independent components of the UVW velocity
	 *   dispersion matrix in a 6x1 column vector as [σ^2_U, σ_UV, σ_UW, σ^2_V, σ_VW, σ^2_W]; the second element contains the
	 *   covariance on the 6 independent components of the velocity dispersion (a 6x6  symmetric {@link Matrix}) respectively.
	 *   All velocities are in kilometres per second. If the velocity dispersion equations cannot be solved due to a
	 *   non-singular mean B matrix then both elements of the returned array are null.
	 */
	public static Matrix[] computeTensorVelocityDispersion(Collection<? extends AstrometricStar> magBinnedStars) {
		
		// This method simply solves for the sample mean peculiar velocity outer product, which does not
		// impose symmetry constraint on the result so is an inferior method:
//		for(int bin=0; bin<magBinnedStars.size(); bin++) {
//			
//			// Mean outer product of the projection matrices
//			Matrix meanAAt = new Matrix(3,3);
//			
//			// Mean outer product of the peculiar tangential velocity
//			Matrix meanPPprime = new Matrix(3,3);
//			
//			// For each object in given colour bin:
//			for(GaiaSourceWithVelocity star : magBinnedStars.get(bin)) {
//				
//				// Outer product of the projection matrices:
//				Matrix aAt = star.A.times(star.A.transpose());
//				
//				// Outer product of the peculiar tangential velocity
//				Matrix pPprime = star.pPrime.times(star.pPrime.transpose());
//				
//				meanAAt.plusEquals(aAt);
//				meanPPprime.plusEquals(pPprime);
//			}
//			
//			// Take mean
//			int n = magBinnedStars.get(bin).size();
//			meanAAt.times(1.0/n);
//			meanPPprime.times(1.0/n);
//			
//			// Solve for velocity dispersion tensor
//			Matrix v = meanAAt.solve(meanPPprime);
//			
//			System.out.println("\n\n\n\nSimple:\n\nBin " + bin + " (" + magBinnedStars.getRange(bin).toString() + ") - " + n + " stars");
//			
//			System.out.println("(σ_U, σ_V, σ_W) = " + "(" + Math.sqrt(v.get(0, 0)) + ", " + Math.sqrt(v.get(1, 1)) + ", "
//							+ Math.sqrt(v.get(2, 2)) + ")");
//			
//			v.print(5, 5);
//		}
		
		// Improved method that imposes symmetry on velocity dispersion tensor:
		Matrix meanU = new Matrix(6,1);
		Matrix meanB = new Matrix(6,6);
		
		// For each object in given colour bin:
		for(AstrometricStar star : magBinnedStars) {
			meanU.plusEquals(star.getU());
			meanB.plusEquals(star.getB());
		}
		
		int n = magBinnedStars.size();
		
		meanB.timesEquals(1.0/n);
		meanU.timesEquals(1.0/n);
		
		Matrix s = null;
		
		try {
			s = meanB.solve(meanU);
		}
		catch(RuntimeException e) {
			// Matrix is singular; no solution possible
			return new Matrix[2];
		}
		
		// Compute the covariance on the elements of S from Poisson noise
		double meanUPrimeSquared = 0.0;
		
		for(AstrometricStar star : magBinnedStars) {
			Matrix u = star.getU();
			Matrix b = star.getB();
			Matrix uPrime = u.minus(b.times(s));
			
			meanUPrimeSquared += uPrime.normF()*uPrime.normF();
		}
		meanUPrimeSquared /= n;
		
		Matrix var_s = meanB.inverse().times(meanUPrimeSquared/n);
		
		return new Matrix[]{s, var_s};
	}
	
	/**
	 * Reshapes the 6x1 column vector containing velocity ellipsoid elements to the standard 3x3 format.
	 * 
	 * @param s
	 * 	Column vector containing the 6 independent components of the UVW velocity dispersion matrix as 
	 * [σ^2_U, σ_UV, σ_UW, σ^2_V, σ_VW, σ^2_W]; this is the output of {@link #computeTensorVelocityDispersion(Collection)}
	 * @return
	 * 	The velocity dispersion tensor arranged as a standard 3x3 symmetric matrix.
	 */
	public static Matrix reshape6x1To3x3(Matrix s) {
		
		double sig2_U = s.get(0, 0);
		double sig_UV = s.get(1, 0);
		double sig_UW = s.get(2, 0);
		double sig2_V = s.get(3, 0);
		double sig_VW = s.get(4, 0);
		double sig2_W = s.get(5, 0);
		
		return new Matrix(new double[][]{{sig2_U, sig_UV, sig_UW},{sig_UV, sig2_V, sig_VW},{sig_UW, sig_VW, sig2_W}});
	}
}
