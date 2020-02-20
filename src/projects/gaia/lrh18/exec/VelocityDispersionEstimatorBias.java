package projects.gaia.lrh18.exec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import Jama.Matrix;
import astrometry.util.AstrometryUtils;
import constants.Galactic;
import kinematics.dm.AstrometricStar;
import kinematics.util.ProperMotionDeprojection;
import numeric.data.RangeMap;
import numeric.stats.StatUtil;
import projections.Aitoff;
import projections.util.ProjectionUtil;
import projects.gaia.lrh18.dm.GaiaSource;

/**
* This class provides an application that is used to estimate the bias in the velocity dispersion estimator
* that assumes zero radial velocity. It generates a synthetic population of stars with known
* kinematics, simulates a catalogue with distance & proper motion values then compares how well the
* known velocity dispersion is recovered using deprojection of proper motions and by assuming zero
* radial velocity.
* 
* @author nickrowell
*/
public class VelocityDispersionEstimatorBias {

	/**
	 * Ground truth mean motion in Galactic frame [km/s].
	 */
    static Matrix meanThinDisk = new Matrix(new double[][]{{-8.62},{-20.04},{-7.1}});

    /**
     * Ground truth velocity ellipsoid in Galactic frame.
     */
    static Matrix ellipsoidThinDisk = new Matrix(new double[][]{{32.4*32.4, 0,         0},
                                                                {0,         23.0*23.0, 0},
                                                                {0,         0,         18.1*18.1}});
    
    /**
     * Maximum distance [parsecs].
     */
    static double dmax = 1000;
    
    /**
     * Minimum distance [parsecs].
     */
    static double dmin = 10;

    /**
     * Random number generator.
     */
    static Random random = new Random();

    /**
     * Main application entry point.
     * 
     * @param args
     * 	The command line arguments (ignored)
     */
    public static void main(String[] args) {

        // Number of stars to be used in simulation
        int N = 100000;

        // Store the Stars
        List<Star> stars = new ArrayList<Star>();
        
        // Populate array with stars with properties assigned randomly
        for(int s=0; s<N; s++){

        	if(10*(s+1)%N==0) {
        		int nHash = 10*(s+1)/N/ + 1;
        		StringBuilder str = new StringBuilder();
        		str.append("Progress: [");
        		for(int i=0; i<nHash; i++) {
        			str.append("#");
        		}
        		for(int i=nHash; i<10; i++) {
        			str.append(".");
        		}
        		str.append("]\r");
        		System.out.print(str.toString());
        	}
        	
        	// Random 3D space motion in Galactic coordinates
        	Matrix uvw = StatUtil.drawRandomVector(ellipsoidThinDisk, meanThinDisk);
        	
        	// Transform this to equatorial coordinate frame
        	Matrix xyz = Galactic.r_E_G.times(uvw);
        	
            // Random 3D position in equatorial coordinates
        	double ra = AstrometryUtils.getRandomRa();
        	double dec = AstrometryUtils.getRandomDec();
        	double dist = AstrometryUtils.getRandomDistance(dmin, dmax);
        	
        	// Get the tangential velocity
        	Matrix vtan = AstrometryUtils.getTangentialVelocityVector(ra, dec, xyz);
            
        	// Project equatorial 3D space motion to get proper motion
        	double[] mu = AstrometryUtils.getProperMotionsFromTangentialVelocity(dist, ra, dec, vtan);
        	double mu_acosd = mu[0];
        	double mu_d = mu[1];
        	
            stars.add(new Star(dist, ra, dec, mu_acosd, mu_d, uvw));
        }
        
        displaySkyMap(stars);
        
        // Estimate velocity disperion by deprojection
		Matrix meanVelocity = ProperMotionDeprojection.computeMeanVelocity(stars)[0];
		double velocityDispersion = ProperMotionDeprojection.computeScalarVelocityDispersion(stars)[0];
		Matrix velocityEllipsoid =  ProperMotionDeprojection.reshape6x1To3x3(ProperMotionDeprojection.computeTensorVelocityDispersion(stars)[0]);

		// Estimate the velocity dispersion by assuming zero radial velocity
		double[][] x = new double[stars.size()][3];
		int idx = 0;
		for(AstrometricStar star : stars) {
			x[idx][0] = star.getP().get(0, 0);
			x[idx][1] = star.getP().get(1, 0);
			x[idx][2] = star.getP().get(2, 0);
			idx++;
		}
		double[] meanVelocityZeroVr = StatUtil.getSampleMean(x);
		
		// Subtract the mean velocity from each star
		for(idx = 0; idx < stars.size(); idx++) {
			
			// Projection of the mean velocity for this star
			Matrix projMeanVel = stars.get(idx).A.times(meanVelocity);
			
			x[idx][0] -= meanVelocityZeroVr[0];
			x[idx][1] -= meanVelocityZeroVr[1];
			x[idx][2] -= meanVelocityZeroVr[2];
//			x[idx][0] -= projMeanVel.get(0, 0);
//			x[idx][1] -= projMeanVel.get(1, 0);
//			x[idx][2] -= projMeanVel.get(2, 0);
		}
		
		double velocityDispZeroVr = StatUtil.getSampleDispersion(x);
		Matrix velocityEllipsoidZeroVr  = new Matrix(StatUtil.getSampleCovariance(x));

		// Print mean velocity
		System.out.println("\n\nMean velocity (zero vr)      = " + String.format("%.3f\t%.3f\t%.3f", meanVelocityZeroVr[0], meanVelocityZeroVr[1], meanVelocityZeroVr[2]));
		System.out.println("Mean velocity (deprojection) = " + String.format("%.3f\t%.3f\t%.3f", meanVelocity.get(0, 0), meanVelocity.get(1, 0), meanVelocity.get(2, 0)));
		
		double v0 = Math.sqrt(meanVelocityZeroVr[0]*meanVelocityZeroVr[0] + meanVelocityZeroVr[1]*meanVelocityZeroVr[1] + meanVelocityZeroVr[2]*meanVelocityZeroVr[2]);
		double v1 = meanVelocity.normF();
		
		System.out.println("Ratio of mean velocity magnitudes (deproj/zerovr) = " + (v1/v0));
		
		// Print scalar velocity dispersion
		System.out.println("\n\nVelocity dispersion (zero vr)      = " + velocityDispZeroVr);
		System.out.println("Velocity dispersion (deprojection) = " + velocityDispersion);
		
		// Print tensor velocity dispersion
		System.out.println("\n\nVelocity ellipsoid (zero vr)      = ");
		velocityEllipsoidZeroVr.print(5, 5);
		System.out.println("Velocity ellipsoid (deprojection) = ");
		velocityEllipsoid.print(5, 5);
		
		// Estimate bias in velocity dispersion by comparing trace of the velocity dispersion tensors
		System.out.println("Trace of velocity dispersion tensor (zero vr)      = " + velocityEllipsoidZeroVr.trace());
		System.out.println("Trace of velocity dispersion tensor (deprojection) = " + velocityEllipsoid.trace());
		System.out.println("Ratio of velocity ellipsoid traces (deproj/zerovr) = " + (velocityEllipsoid.trace()/velocityEllipsoidZeroVr.trace()));
    }
    
    /**
	 * Displays an all-sky map of the star positions.
	 * @param stars
	 * 	A {@link RangeMap} containing all of the {@link GaiaSource}s to plot.
	 */
	private static void displaySkyMap(Collection<? extends AstrometricStar> stars) {
		
		List<double[]> points = new LinkedList<>();
		
		for(AstrometricStar star : stars) {
			// Rotate equatorial coordinates to Galactic coordinates
			points.add(new double[]{star.getLong(), star.getLat()});
		}
	
		ProjectionUtil.makeAndDisplayJFreeChartPlot(points, "Sky distribution of simulated stars", new Aitoff());
	}
}

/**
 * Private class used to represent star objects in the simulations.
 *
 * @author nickrowell
 */

class Star implements AstrometricStar {
	
	/**
	 * Main constructor for the {@link Star}.
	 * 
	 * @param d
	 * 	Distance [parsecs]
	 * @param ra
	 * 	Right ascension [radians]
	 * @param dec
	 * 	Declination [radians]
	 * @param mu_acosd
	 * 	Proper motion parallel to equator [radians/yr]
	 * @param mu_d
	 * 	Proper motion perpendicualar to equator [radians/yr]
	 * @param uvw
	 * 	3D space motion in Galactic frame [km/s]
	 */
	public Star(double d, double ra, double dec, double mu_acosd, double mu_d, Matrix uvw) {
		
		// Get the distance to the star [parsecs]
		this.d = d;
		
		// Retrieve some fields for convenience
		
		// 1) Convert proper motion to Galactic coordinates
		double[] mu_lb = AstrometryUtils.convertPositionAndProperMotionEqToGal(ra, dec, mu_acosd, mu_d);
		l = mu_lb[0];
		b = mu_lb[1];
		mu_lcosb = mu_lb[2];
		mu_b = mu_lb[3];
		
		// We haven't added a component to the motion to account for differential rotation, so don't subtract it off.
//		mu_lcosb = mu_lcosb - (A_RadYr * Math.cos(2 * l) + B_RadYr) * Math.cos(b);
//		mu_b = mu_b + A_RadYr * Math.sin(2 * l) * Math.cos(b) * Math.sin(b);
		
		// 3) Convert to proper motion velocity vector
		p = AstrometryUtils.getTangentialVelocityVector(this.d, l, b, mu_lcosb, mu_b);
		
		// 4) Compute the projection matrix A along the line of sight towards this star
		A = AstrometryUtils.getProjectionMatrixA(l, b);
		
		// This will be computed later
		pPrime = new Matrix(3,1);
		
		u = uvw.get(0, 0);
		v = uvw.get(1, 0);
		w = uvw.get(2, 0);
	}
	
	/**
	 * U component of the Galactic space velocity [km/s].
	 */
	public final double u;

	/**
	 * V component of the Galactic space velocity [km/s].
	 */
	public final double v;

	/**
	 * W component of the Galactic space velocity [km/s].
	 */
	public final double w;
	
	/**
	 * The Galactic longitude [radians]
	 */
	public double l;
	
	/**
	 * The Galactic latitude [radians]
	 */
	public double b;
	
	/**
	 * The proper motion in Galactic longitude, including cosine factor [radians/yr]
	 */
	public double mu_lcosb;
	
	/**
	 * The proper motion in Galactic latitude [radians/yr]
	 */
	public double mu_b;
	
	/**
	 * The distance to the star [parsecs]
	 */
	public double d;
	
	/**
	 * The proper motion velocity vector (i.e. tangential velocity).
	 */
	public Matrix p;
	
	/**
	 * The proper motion velocity vector (i.e. tangential velocity) minus the mean motion, i.e.
	 * the peculiar motion for this star from which the velocity dispersion is measured.
	 */
	public Matrix pPrime;
	
	/**
	 * The projection matrix A that projects the 3D velocity onto the celestial sphere.
	 */
	public Matrix A;

	@Override
	public double getLong() {
		return l;
	}

	@Override
	public double getLat() {
		return b;
	}

	@Override
	public double getMuLCosB() {
		return mu_lcosb;
	}

	@Override
	public double getMuB() {
		return mu_b;
	}

	@Override
	public double getDistance() {
		return d;
	}

	@Override
	public Matrix getP() {
		return p;
	}

	@Override
	public void setP(Matrix p) {
		this.p = p;
	}

	@Override
	public Matrix getPPrime() {
		return pPrime;
	}

	@Override
	public void setPPrime(Matrix pPrime) {
		this.pPrime = pPrime;
	}

	@Override
	public Matrix getA() {
		return A;
	}

	@Override
	public void setA(Matrix a) {
		this.A = a;
	}
	
	@Override
	public Matrix getU() {
		
		// Compute the vector u for this star
		double[][] u = new double[6][1];
		
		// Loop over mixed products of the peculiar velocity components
		int n=3;
		for(int i=0; i<n; i++) {
			for(int k=i; k<n; k++) {
				
				// Index into 1D array
				int t = (n*(n-1)/2) - (n-i-1)*(n-i)/2 + k;
				
				u[t][0] += pPrime.get(i, 0) * pPrime.get(k, 0);
			}
		}
		
		return new Matrix(u);
	}
	
	@Override
	public Matrix getB() {
		
		// Compute the matrix B for this star
		double[][] b = new double[6][6];
		
		// Loop over mixed products of the projection matrix components
		int n=3;
		for(int i=0; i<n; i++) {
			for(int k=i; k<n; k++) {
				
				// Index into 1D array
				int t = (n*(n-1)/2) - (n-i-1)*(n-i)/2 + k;
				
				for(int j=0; j<n; j++) {
					for(int l=j; l<n; l++) {
						
						// Second index into 2D array
						int v = (n*(n-1)/2) - (n-j-1)*(n-j)/2 + l;
						
						b[t][v] += (A.get(i, j) * A.get(k, l) + A.get(k, j) * A.get(i, l)) / 2;
					}
				}
			}
		}
		
		return new Matrix(b);
	}
}