package kinematics.test;

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
*
* This class produces a synthetic population of stars randomly distributed in a spherical volume
* about the origin, and endowed with UVW Galactic velocities drawn from Gaussian distribution
* functions defined at the start of the code. The velocity ellipsoid can be non-diagonal, but must
* be symmetric. If non-diagonal, velocities are obtained by first diagonalising it, randomly drawing
* velocities along each of the principal axes then rotating the resulting vector back to the Galactic
* frame. The mean motion is then added.
*
* Star objects are created with observed properties calculated from these intrinsic ones. The synthetic
* population can then be used to test how well the underlying distribution functions can be recovered
* by deprojection of proper motions. It is expected that for a WD sample drawn by RPM methods, a certain
* tangential velocity range will be excluded. This code can test by Monte Carlo methods what effect this
* has on the derived kinematical quantities.
*
* Only the first moments of the velocity distribution - the means - are calculated by deprojection here.
* This is sufficient for the purposes of my thesis work.
*
*
*
* @author nickrowell
*/
public class TestProperMotionDeprojection {

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
     * Standard deviation on distance errors [parsecs]
     */
    static double sigma = 0.0;

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
    public static void main(String[] args){

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
        		
        // Verify velocity moments using intrinsic properties
        double u=0.0, v=0.0, w=0.0;
        double uu=0.0, vv=0.0, ww=0.0;
        double uv=0.0, uw=0.0, vw=0.0;

        for(Star star : stars){
            u += star.u;
            v += star.v;
            w += star.w;
            
            uu += star.u*star.u;
            vv += star.v*star.v;
            ww += star.w*star.w;
            
            uv += star.u*star.v;
            uw += star.u*star.w;
            vw += star.v*star.w;
        }

        u /= N;
        v /= N;
        w /= N;
        uu /= N;
        vv /= N;
        ww /= N;        
        uv /= N;
        uw /= N;
        vw /= N;
        
        System.out.println("Ground truth mean velocity and velocity dispersion:");
        meanThinDisk.print(5, 5);
        ellipsoidThinDisk.print(5, 5);
        
        System.out.println("\nIntrinsic velocity moments for sampled synthetic population:");
        System.out.println(String.format("%.3f\n%.3f\n%.3f", u, v, w));
        System.out.println("");
        System.out.println(String.format("%.3f\t%.3f\t%.3f", (uu - u*u), (uv - u*v), (uw - u*w)));
        System.out.println(String.format("%.3f\t%.3f\t%.3f", (uv - u*v), (vv - v*v), (vw - v*w)));
        System.out.println(String.format("%.3f\t%.3f\t%.3f", (uw - u*w), (vw - v*w), (ww - w*w)));
        
        // Now do deprojection of proper motions on remaining stars
		Matrix[] meanVelocity = ProperMotionDeprojection.computeMeanVelocity(stars);
		
//		double[] disp = ProperMotionDeprojection.computeScalarVelocityDispersion(stars);
		
		Matrix[] velocityEllipsoid = ProperMotionDeprojection.computeTensorVelocityDispersion(stars);
		
		System.out.println("\n\nMeasured velocity moments from proper motion deprojection:");
		
		double estU = meanVelocity[0].get(0, 0);
		double sigma_estU = Math.sqrt(meanVelocity[1].get(0, 0));
		double estV = meanVelocity[0].get(1, 0);
		double sigma_estV = Math.sqrt(meanVelocity[1].get(1, 1));
		double estW = meanVelocity[0].get(2, 0);
		double sigma_estW = Math.sqrt(meanVelocity[1].get(2, 2));
		
		System.out.println(String.format("<U> = %.2f +/- %.6f", estU, sigma_estU));
		System.out.println(String.format("<V> = %.2f +/- %.6f", estV, sigma_estV));
		System.out.println(String.format("<W> = %.2f +/- %.6f", estW, sigma_estW));
		
		System.out.println("\nMeasured velocity dispersion matrix from proper motion deprojection:");
		
		double sig2_U = velocityEllipsoid[0].get(0, 0);
		double sig_UV = velocityEllipsoid[0].get(1, 0);
		double sig_UW = velocityEllipsoid[0].get(2, 0);
		double sig2_V = velocityEllipsoid[0].get(3, 0);
		double sig_VW = velocityEllipsoid[0].get(4, 0);
		double sig2_W = velocityEllipsoid[0].get(5, 0);
		
		Matrix mat = new Matrix(new double[][]{{sig2_U, sig_UV, sig_UW},{sig_UV, sig2_V, sig_VW},{sig_UW, sig_VW, sig2_W}});
		
		mat.print(5, 5);
		
		System.out.println("\nStandard deviation on the elements of this:");
		double sig2_sig2_U = Math.sqrt(velocityEllipsoid[1].get(0, 0));
		double sig2_sig_UV = Math.sqrt(velocityEllipsoid[1].get(1, 1));
		double sig2_sig_UW = Math.sqrt(velocityEllipsoid[1].get(2, 2));
		double sig2_sig2_V = Math.sqrt(velocityEllipsoid[1].get(3, 3));
		double sig2_sig_VW = Math.sqrt(velocityEllipsoid[1].get(4, 4));
		double sig2_sig2_W = Math.sqrt(velocityEllipsoid[1].get(5, 5));

		Matrix mat2 = new Matrix(new double[][]{{sig2_sig2_U, sig2_sig_UV, sig2_sig_UW},{sig2_sig_UV, sig2_sig2_V, sig2_sig_VW},{sig2_sig_UW, sig2_sig_VW, sig2_sig2_W}});
		
		mat2.print(5, 5);
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