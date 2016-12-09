package astrometry.test;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;

import astrometry.ProperMotionDeprojection;
import constants.Galactic;
import numeric.geom.dim3.Vector3d;
import Jama.EigenvalueDecomposition;
import Jama.Matrix;

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

    //+++ Set kinematics of population used in simulation +++//
    //
    //  Mean motion in Galactic coordinate frame relative to Sun
    static Matrix meanThinDisk = new Matrix(new double[][]{{-8.62},{-20.04},{-7.1}});
    //
    //  Velocity ellipsoid in Galactic coordinate frame - can be non-diagonal
    static Matrix ellipsoidThinDisk = new Matrix(new double[][]{{32.4*32.4, 0,         0},
                                                                {0,         23.0*23.0, 0},
                                                                {0,         0,         18.1*18.1}});
    
    //+++ Radius of spherical volume in which to generate stars +++//
    static double dmax = 1000;
    static double dmin = 10;

    //+++ Size of Gaussian relative error to add to star distances to simulate measurement error +++//
    static double sigma = 0.5;

    //+++ Used to draw random velocities +++//
    static Random normal = new Random();

    static Matrix I = Matrix.identity(3,3);

    public static void main(String[] args){

        //+++ Lower tangential velocity threshold for stars included in deprojection +++//
        double vt_min = 0;

        //+++ Calculate fraction of stars that pass this +++//
        double N_pass = 0, N_fail = 0;

        //+++ Get Eigenvalue decomposition of input velocity ellipsoid +++//
        EigenvalueDecomposition evd = new EigenvalueDecomposition(ellipsoidThinDisk);

        //+++ Use eigenvalues to draw random motion in frame defined by eigenvectors of velocity ellipsoid +++//
        Matrix D = evd.getD();
        Matrix V = evd.getV();

        double diag1 = Math.sqrt(D.get(0, 0));
        double diag2 = Math.sqrt(D.get(1, 1));
        double diag3 = Math.sqrt(D.get(2, 2));


        //+++ Set number of stars to be used in deprojection +++//
        int N = 1000000;

        //+++ Create ArrayList to store these +++//
        ArrayList<Star> stars = new ArrayList<Star>();

        //+++ Populate array with stars with properties assigned randomly +++//
        for(int s=0; s<N;){

            //+++ Randomly draw a 3D position +++//
            Matrix r = Vector3d.getRandVecOnUnitSphere().mult(Math.random() * dmax).toColumnMatrix();


            //+++ Draw random motion in frame composed of eigenvectors of velocity ellipsoid +++//
            //+++ Each component is independant and normally distributed about zero, with    +++//
            //+++ variance set by velocity dispersion.                                       +++//
            double e1 = normal.nextGaussian() * diag1,
                   e2 = normal.nextGaussian() * diag2,
                   e3 = normal.nextGaussian() * diag3;

            Matrix diag123 = new Matrix(new double[][]{{e1},{e2},{e3}});

            //+++ Rotate this vector to Galactic coordinates +++//
            Matrix UVW = V.times(diag123);

            //+++ Add mean motion of population +++//
            UVW.plusEquals(meanThinDisk);

            Star star = new Star(r,UVW);

            if(star.vt > vt_min){
                stars.add(star);
                N_pass++;
                s++;
                System.out.println("star "+s);
            }
            else N_fail++;
            
        }
        
        System.out.println("Fraction of thin disk stars with velocities higher than "+vt_min+" kms^{-1}:"+
                            "\n\n N_pass/(N_pass + N_fail) = "+N_pass+"/"+(N_pass+N_fail)+" = "+(N_pass/(N_pass+N_fail)));
        
        //+++ Verify velocity moments using intrinsic properties +++//
        double u=0.0, v=0.0, w=0.0;
        double uu=0.0, vv=0.0, ww=0.0;
        double uv=0.0, uw=0.0, vw=0.0;

        for(int s=0; s<stars.size(); s++){

            u += stars.get(s).getU(); uu += stars.get(s).getU()*stars.get(s).getU();
            v += stars.get(s).getV(); vv += stars.get(s).getV()*stars.get(s).getV();
            w += stars.get(s).getW(); ww += stars.get(s).getW()*stars.get(s).getW();

            uv += stars.get(s).getU()*stars.get(s).getV();
            uw += stars.get(s).getU()*stars.get(s).getW();
            vw += stars.get(s).getV()*stars.get(s).getW();
        }

        u  /= (double)stars.size();  v /= (double)stars.size();  w /= (double)stars.size();
        uu /= (double)stars.size(); vv /= (double)stars.size(); ww /= (double)stars.size();        
        uv /= (double)stars.size(); uw /= (double)stars.size(); vw /= (double)stars.size();
        
        DecimalFormat xpxx = new DecimalFormat("0.00");
        DecimalFormat xpx    = new DecimalFormat("0.0");

        System.out.println("\nIntrinsic velocity moments for sampled synthetic population");
        System.out.println("-------------------------------------------------------------");
        System.out.println("\n<U> = "+xpxx.format(u)+"\t\n<V> = "+xpxx.format(v)+"\t\n<W> = "+xpxx.format(w));
        System.out.println("\nVelocity dispersion tensor:");
        System.out.println("[ "+xpx.format(Math.signum(uu - u*u)*Math.sqrt(Math.abs(uu - u*u)))+", "+xpx.format(Math.signum(uv - u*v)*Math.sqrt(Math.abs(uv - u*v)))+", "+xpx.format(Math.signum(uw - u*w)*Math.sqrt(Math.abs(uw - u*w))));
        System.out.println("  "+xpx.format(Math.signum(uv - u*v)*Math.sqrt(Math.abs(uv - u*v)))+", "+xpx.format(Math.signum(vv - v*v)*Math.sqrt(Math.abs(vv - v*v)))+", "+xpx.format(Math.signum(vw - v*w)*Math.sqrt(Math.abs(vw - v*w))));
        System.out.println("  "+xpx.format(Math.signum(uw - u*w)*Math.sqrt(Math.abs(uw - u*w)))+", "+xpx.format(Math.signum(vw - v*w)*Math.sqrt(Math.abs(vw - v*w)))+", "+xpx.format(Math.signum(ww - w*w)*Math.sqrt(Math.abs(ww - w*w)))+" ]");



        //+++ Now do deprojection of proper motions on remaining stars +++//

        ProperMotionDeprojection deprojection = new ProperMotionDeprojection();
        
        for(int s=0; s<stars.size(); s++){

        	double ra  = stars.get(s).ra;
        	double dec = stars.get(s).dec;
        	double ra_dot = stars.get(s).ra_dot;
        	double dec_dot = stars.get(s).dec_dot;
        	double dist = stars.get(s).d;
        	double sigDist = sigma;
        	
        	
        	deprojection.addObject(ra, dec, ra_dot, dec_dot, dist, sigDist);
        	
        }
        
        Matrix mean = deprojection.getMean();
        
        System.out.println("Mean velocity = "+mean.toString());
        
    }
}

/**
 * Internal class used to represent star objects in main code. These have a very restricted set of
 * parameters relative to the star objects used in the main code.
 *
 * @author nickrowell
 */


class Star{


    /*
     * Observational quantities, calculated from randomly assigned intrinsic properties.
     *
     */

    //+++ Position on sky +++//
    double ra, dec;

    //+++ Rate of change of each coordinate +++//
    double ra_dot, dec_dot;

    //+++ Proper motion, arcseconds per year +++//
    double mu = 0.0;

    //+++ Line of sight distance +++//
    double d;

    //+++ Distance error +++//
    double sigma_d;

    //+++ Tangential velocity +++//
    double vt = 0.0;

    /*
     * Intrinsic properties, hidden from main code.
     *
     */

    //+++ Position vector +++//
    Matrix r = new Matrix(3,1);

    //+++ Velocity vector referred to Galactic, equatorial and Normal triads +++//
    Matrix UVW = new Matrix(3,1);
    Matrix XYZ = new Matrix(3,1);
    Matrix PQR = new Matrix(3,1);

    public Star(Matrix R, Matrix uvw){

        //+++ Set intrinsic quantities +++//
        // Galactic velocity
        UVW.set(0,0,uvw.get(0, 0));
        UVW.set(1,0,uvw.get(1, 0));
        UVW.set(2,0,uvw.get(2, 0));
        // Celestial position vector
        r.set(0, 0, R.get(0, 0));
        r.set(1, 0, R.get(1, 0));
        r.set(2, 0, R.get(2, 0));

        //+++ Distance +++//
        d = Math.sqrt(r.get(0,0)*r.get(0,0) + r.get(1,0)*r.get(1,0) + r.get(2,0)*r.get(2,0));

        //+++ Calculate ra and dec +++//
        Matrix r_hat = r.times(1.0/r.normF());
        dec = Math.asin(r_hat.get(2,0));
        ra = Math.atan2(r_hat.get(1,0), r_hat.get(0,0));

        //+++ Refer velocity vector to equatorial triad +++//
        XYZ = Galactic.r_E_G.times(UVW);

        //+++ Now refer velocity to Normal triad, and get components perp. and par. to equator +++//
		double[][] ntr = {{-1*Math.sin(ra), -1*Math.sin(dec)*Math.cos(ra),Math.cos(dec)*Math.cos(ra)},
	                          {Math.cos(ra),-1*Math.sin(dec)*Math.sin(ra),Math.cos(dec)*Math.sin(ra)},
	                          {0,Math.cos(dec),Math.sin(dec)}};
		Matrix NTR = new Matrix(ntr);
		Matrix RTN = NTR.transpose();

        //+++ Velocity referred to Normal triad +++//
        PQR = RTN.times(XYZ);

        //+++ Get component of proper motion perpendicular to equator, i.e. declination +++//
        // Q component is corresponding velocity
        dec_dot = PQR.get(1,0)/(4.74*d);
        //+++ Get component parallel to equator, i.e. right ascension +++//
        ra_dot = PQR.get(0,0)/(4.74*d);

        //+++ Get total angular motion +++//
        mu = Math.sqrt(dec_dot*dec_dot + ra_dot*ra_dot);

        //+++ Convert this to tangential velocity +++//
        vt = mu * 4.74 * d;

        //+++ Convert ra to a rate of change in the coordinate. This is used in deprojection. +++//
        ra_dot = ra_dot / Math.cos(dec);

        //+++ Add error to distance +++//
        sigma_d = TestProperMotionDeprojection.normal.nextGaussian()*(TestProperMotionDeprojection.sigma*d);
        d = d + sigma_d;

    }

    public double getU(){ return UVW.get(0,0);}
    public double getV(){ return UVW.get(1,0);}
    public double getW(){ return UVW.get(2,0);}

}