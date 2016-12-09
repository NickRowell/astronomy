package Archive;

import Kinematics.*;
import Jama.*;
import Constants.GalacticCoordinates;
import java.util.ArrayList;
import java.util.Random;
import java.text.DecimalFormat;

/**
 *
 * This is a copy of a class from main repository. I couldn't figure out how to calculate second moments
 * of velocity distribution properly, so decided to defer this to a later more thorough analysis of the
 * kinematics. The original code was stripped down so that only first moments were calculated. I thought
 * it would be good to keep a copy of the original in case it came in handy at a later date.
 *
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
 * TODO: I've not quite cracked the deprojection formalism. It seems to either underestimate or
 * overestimate the errors on the velocity moments. I suspect that my calculation of the Poisson
 * counting error on the moments is wrong - Dehnen & Binney 1998 get a different value but I'm not
 * sure how. Should also check over the calculation of the observed stellar properties from the intrinsic
 * quantities, in particular the ra, dec, proper motion components and transformation between frames
 * necessary to get these values in Equatorial coordinates from their Galactic frame components.
 *
 *
 * @author nickrowell
 */
public class VelocityEllipsoidSimulator {

    //+++ Set kinematics of population used in simulation +++//
    //
    //  Mean motion in Galactic coordinate frame
    static Matrix mean = new Matrix(new double[][]{{11},{-23},{15}});
    //
    //  Velocity ellipsoid in Galactic coordinate frame - can be non-diagonal
    static Matrix ellipsoid = new Matrix(new double[][]{{36*36,10*10,    0},
                                                        {10*10,    25*25,0},
                                                        {0,    0,    36*36}});
    
    //+++ Radius of spherical volume in which to generate stars +++//
    static double dmax = 1000;
    static double dmin = 10;

    //+++ Size of Gaussian relative error to add to star distances to simulate measurement error +++//
    static double sigma = 0.0;

    //+++ Used to draw random velocities +++//
    static Random normal = new Random();

    static Matrix I = Matrix.identity(3,3);

    public static void main(String[] args){

        //+++ Get Eigenvalue decomposition of input velocity ellipsoid +++//
        EigenvalueDecomposition evd = new EigenvalueDecomposition(ellipsoid);

        //+++ Use eigenvalues to draw random motion in frame defined by eigenvectors of velocity ellipsoid +++//
        Matrix D = evd.getD();
        Matrix V = evd.getV();

        double diag1 = Math.sqrt(D.get(0, 0));
        double diag2 = Math.sqrt(D.get(1, 1));
        double diag3 = Math.sqrt(D.get(2, 2));


        //+++ Set number of stars to be used in simulation +++//
        int N = 100000;

        //+++ Create ArrayList to store these +++//
        ArrayList<Star> stars = new ArrayList<Star>();

        //+++ Populate array with stars with properties assigned randomly +++//
        for(int s=0; s<N; s++){

            //+++ Randomly draw a 3D position +++//
            Matrix r = getRandomPosition();

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
            UVW.plusEquals(mean);

            stars.add(new Star(r,UVW));

        }


        //+++ Now remove from list a certain selection of stars +++//
        for(int s=0; s<stars.size(); ){

            if(stars.get(s).vt < 30) stars.remove(s);
            else s++;
        }

        System.out.println("Number of stars removed by tangential velocity constraint = "+(N-stars.size()));

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
        
        DecimalFormat xpxxEx = new DecimalFormat("0.00E0");
        DecimalFormat xpx    = new DecimalFormat("0.0");

        System.out.println("\nIntrinsic velocity moments for sampled synthetic population");
        System.out.println("-------------------------------------------------------------");
        System.out.println("\n<U> = "+u+"\t\n<V> = "+v+"\t\n<W> = "+w);
        System.out.println("\nVelocity dispersion tensor:");
        System.out.println("[ "+xpx.format(Math.signum(uu - u*u)*Math.sqrt(Math.abs(uu - u*u)))+", "+xpx.format(Math.signum(uv - u*v)*Math.sqrt(Math.abs(uv - u*v)))+", "+xpx.format(Math.signum(uw - u*w)*Math.sqrt(Math.abs(uw - u*w))));
        System.out.println("  "+xpx.format(Math.signum(uv - u*v)*Math.sqrt(Math.abs(uv - u*v)))+", "+xpx.format(Math.signum(vv - v*v)*Math.sqrt(Math.abs(vv - v*v)))+", "+xpx.format(Math.signum(vw - v*w)*Math.sqrt(Math.abs(vw - v*w))));
        System.out.println("  "+xpx.format(Math.signum(uw - u*w)*Math.sqrt(Math.abs(uw - u*w)))+", "+xpx.format(Math.signum(vw - v*w)*Math.sqrt(Math.abs(vw - v*w)))+", "+xpx.format(Math.signum(ww - w*w)*Math.sqrt(Math.abs(ww - w*w)))+" ]");



        //+++ Now do deprojection of proper motions on remaining stars +++//

        // Individual quantities
        Matrix p   = new Matrix(3,1);
        Matrix ep2 = new Matrix(3,1);
        Matrix A   = new Matrix(3,3);
        Matrix pp   = new Matrix(6,1);
        Matrix epp2 = new Matrix(6,1);
        Matrix B    = new Matrix(6,6);

        // Sum quantities
        Matrix meanP   = new Matrix(3,1);
        Matrix meanEP2 = new Matrix(3,1);
        Matrix meanA   = new Matrix(3,3);
        Matrix meanPP   = new Matrix(6,1);
        Matrix meanEPP2 = new Matrix(6,1);
        Matrix meanB    = new Matrix(6,6);

        double n=0;

        for(int s=0; s<stars.size(); s++){

            //+++ get unit position vector +++//
            Matrix r = new Matrix(new double[][]{{Math.cos(stars.get(s).ra) * Math.cos(stars.get(s).dec)},
                                                 {Math.sin(stars.get(s).ra) * Math.cos(stars.get(s).dec)},
                                                 {Math.sin(stars.get(s).dec)}});

            //+++ Calculate components of projection matrix relative to Equatorial triad +++//
            A = I.minus(r.times(r.transpose()));

            //+++ Transform A to Galactic coordinate frame +++//
            A = GalacticCoordinates.GTN.times(A.times(GalacticCoordinates.NTG));


            /*
             *  This calculation is taken from equation 10 of Fuchs (2009) study
             *  of kinematics of SDSS M dwarfs. ALso appendix B.
             *
             * Main equation is <p_i p_j> = 1/2 SUM_{k,m}<(A_{ik}A_{jm} + A_{jk}A_{im})><v_k v_m>
             *
             * where i,j,k,m = UVW
             *
             * and don't take all combinations of values. pUpV = pVpU due to symmetry, so this value is
             * omitted from column vectors, which are consequently six elements long. B is a 6x6 matrix,
             * with rows corresponding to 6 p_i p_j combinations, and columns corresponding to 6 v_k v_m
             * combinations.
             *
             * Note that A is already transformed to Galactic coordinate frame, so B doesn't need to be.
             *
             */
            for (int nc = 0; nc < 6; nc++) {
                //+++ UU, VV, WW components +++//
                if (nc == 0 || nc == 3 || nc == 5) {
                    for (int nr = 0; nr < 6; nr++) {
                        B.set(nr, nc,
                                0.5 * (A.get(irow(nr, 0), icol(nc, 0)) * A.get(irow(nr, 1), icol(nc, 1))
                                + A.get(irow(nr, 2), icol(nc, 2)) * A.get(irow(nr, 3), icol(nc, 3))));
                    }
                }

                //+++ Mixed components UV, UW VW +++//
                if (nc == 1 || nc == 2 || nc == 4) {
                    for (int nr = 0; nr < 6; nr++) {
                        B.set(nr, nc,
                                0.5 * (A.get(irow(nr, 0), icol(nc, 0)) * A.get(irow(nr, 1), icol(nc, 1))
                                + A.get(irow(nr, 2), icol(nc, 2)) * A.get(irow(nr, 3), icol(nc, 3))
                                + A.get(irow(nr, 0), icol(nc, 4)) * A.get(irow(nr, 1), icol(nc, 5))
                                + A.get(irow(nr, 2), icol(nc, 6)) * A.get(irow(nr, 3), icol(nc, 7))));
                    }
                }
            }


            //+++ Calculate proper motion vector components relative to Equatorial triad +++//
            p.set(0, 0, 4.74 * stars.get(s).d * (-Math.sin(stars.get(s).ra) * Math.cos(stars.get(s).dec) * stars.get(s).ra_dot - Math.cos(stars.get(s).ra) * Math.sin(stars.get(s).dec) * stars.get(s).dec_dot));
            p.set(1, 0, 4.74 * stars.get(s).d * (Math.cos(stars.get(s).ra) * Math.cos(stars.get(s).dec) * stars.get(s).ra_dot - Math.sin(stars.get(s).ra) * Math.sin(stars.get(s).dec) * stars.get(s).dec_dot));
            p.set(2, 0, 4.74 * stars.get(s).d * (Math.cos(stars.get(s).dec) * stars.get(s).dec_dot));

            //+++ Transform proper motion vector to Galactic frame +++//
            p = GalacticCoordinates.GTN.times(p);

            //+++ Calculate components of mixed proper motion velocity vector from p +++//
            int index = 0;
            for (int i = 0; i < 3; i++) {
                for (int j = i; j < 3; j++) {
                    pp.set(index++, 0, p.get(i, 0) * p.get(j, 0));
                }
            }

            //+++ Get proper motion velocity error in Galactic frame +++//
            ep2.set(0, 0, Math.pow(p.get(0, 0) * sigma / stars.get(s).d, 2.0));
            ep2.set(1, 0, Math.pow(p.get(1, 0) * sigma / stars.get(s).d, 2.0));
            ep2.set(2, 0, Math.pow(p.get(2, 0) * sigma / stars.get(s).d, 2.0));

            //+++ Get errors on combined components of proper motion velocity +++//
            epp2.set(0, 0, Math.pow(p.get(0, 0), 2) * ep2.get(0, 0));
            epp2.set(1, 0, Math.pow(p.get(1, 0), 2) * ep2.get(0, 0) + Math.pow(p.get(0, 0), 2) * ep2.get(1, 0));
            epp2.set(2, 0, Math.pow(p.get(2, 0), 2) * ep2.get(0, 0) + Math.pow(p.get(0, 0), 2) * ep2.get(2, 0));
            epp2.set(3, 0, Math.pow(p.get(1, 0), 2) * ep2.get(1, 0));
            epp2.set(4, 0, Math.pow(p.get(1, 0), 2) * ep2.get(2, 0) + Math.pow(p.get(2, 0), 2) * ep2.get(1, 0));
            epp2.set(5, 0, Math.pow(p.get(2, 0), 2) * ep2.get(2, 0));


            //+++ Weight components and add to sum total for ensemble +++//
            meanP = meanP.plus(p);
            meanA = meanA.plus(A);
            meanEP2 = meanEP2.plus(ep2);
            meanPP = meanPP.plus(pp);
            meanB = meanB.plus(B);
            meanEPP2 = meanEPP2.plus(epp2);

            n++;
        }

        //+++ Divide A, B, p & pp component-wise by number of stars to get mean +++//
        meanP  = meanP.times(1.0 / n);
        meanPP = meanPP.times(1.0 / n);
        meanEP2 = meanEP2.times(1.0 / (n*n));
        meanA  = meanA.times(1.0 / n);
        meanB  = meanB.times(1.0 / n);

        //+++ Errors on mean combined proper motion components are found by normalization with varying factors +++//
        meanEPP2.set(0, 0, meanEPP2.get(0, 0)*4.0/(n*n));
        meanEPP2.set(1, 0, meanEPP2.get(1, 0)*1.0/(n*n));
        meanEPP2.set(2, 0, meanEPP2.get(2, 0)*1.0/(n*n));
        meanEPP2.set(3, 0, meanEPP2.get(3, 0)*4.0/(n*n));
        meanEPP2.set(4, 0, meanEPP2.get(4, 0)*1.0/(n*n));
        meanEPP2.set(5, 0, meanEPP2.get(5, 0)*4.0/(n*n));
        
        //+++ Deprojection operation using mean values +++//

        //+++ Invert <A> +++//
        Matrix invA = meanA.inverse();
        Matrix invB = meanB.inverse();

        //+++ Get errors on mean velocities from distance uncertainties alone +++//
        double sig2U_d = ((invA.get(0,0)*invA.get(0,0))*meanEP2.get(0,0)) +
                         ((invA.get(0,1)*invA.get(0,1))*meanEP2.get(1,0)) +
                         ((invA.get(0,2)*invA.get(0,2))*meanEP2.get(2,0));

        double sig2V_d = ((invA.get(1,0)*invA.get(1,0))*meanEP2.get(0,0)) +
                         ((invA.get(1,1)*invA.get(1,1))*meanEP2.get(1,0)) +
                         ((invA.get(1,2)*invA.get(1,2))*meanEP2.get(2,0));
        
        double sig2W_d = ((invA.get(2,0)*invA.get(2,0))*meanEP2.get(0,0)) +
                         ((invA.get(2,1)*invA.get(2,1))*meanEP2.get(1,0)) +
                         ((invA.get(2,2)*invA.get(2,2))*meanEP2.get(2,0));

        //+++ Get statistical uncertainty on mean velocities due to Poisson counting errors +++//
        double sig2U_N = (1.0/n)*
                         (((invA.get(0,0)*invA.get(0,0))*meanP.get(0,0)*meanP.get(0,0)) +
                          ((invA.get(0,1)*invA.get(0,1))*meanP.get(1,0)*meanP.get(1,0)) +
                          ((invA.get(0,2)*invA.get(0,2))*meanP.get(2,0)*meanP.get(2,0)));
        double sig2V_N = (1.0/n)*
                         (((invA.get(1,0)*invA.get(1,0))*meanP.get(0,0)*meanP.get(0,0)) +
                          ((invA.get(1,1)*invA.get(1,1))*meanP.get(1,0)*meanP.get(1,0)) +
                          ((invA.get(1,2)*invA.get(1,2))*meanP.get(2,0)*meanP.get(2,0)));
        double sig2W_N = (1.0/n)*
                         (((invA.get(2,0)*invA.get(2,0))*meanP.get(0,0)*meanP.get(0,0)) +
                          ((invA.get(2,1)*invA.get(2,1))*meanP.get(1,0)*meanP.get(1,0)) +
                          ((invA.get(2,2)*invA.get(2,2))*meanP.get(2,0)*meanP.get(2,0)));

        //+++ Get errors on mean combined proper motion components from distance uncertainties alone +++//
        double sig2UU_d = invB.get(0,0)*invB.get(0,0)*meanEPP2.get(0,0)+
                          invB.get(0,1)*invB.get(0,1)*meanEPP2.get(1,0)+
                          invB.get(0,2)*invB.get(0,2)*meanEPP2.get(2,0)+
                          invB.get(0,3)*invB.get(0,3)*meanEPP2.get(3,0)+
                          invB.get(0,4)*invB.get(0,4)*meanEPP2.get(4,0)+
                          invB.get(0,5)*invB.get(0,5)*meanEPP2.get(5,0);

        double sig2UV_d = invB.get(1,0)*invB.get(1,0)*meanEPP2.get(0,0)+
                          invB.get(1,1)*invB.get(1,1)*meanEPP2.get(1,0)+
                          invB.get(1,2)*invB.get(1,2)*meanEPP2.get(2,0)+
                          invB.get(1,3)*invB.get(1,3)*meanEPP2.get(3,0)+
                          invB.get(1,4)*invB.get(1,4)*meanEPP2.get(4,0)+
                          invB.get(1,5)*invB.get(1,5)*meanEPP2.get(5,0);

        double sig2UW_d = invB.get(2,0)*invB.get(2,0)*meanEPP2.get(0,0)+
                          invB.get(2,1)*invB.get(2,1)*meanEPP2.get(1,0)+
                          invB.get(2,2)*invB.get(2,2)*meanEPP2.get(2,0)+
                          invB.get(2,3)*invB.get(2,3)*meanEPP2.get(3,0)+
                          invB.get(2,4)*invB.get(2,4)*meanEPP2.get(4,0)+
                          invB.get(2,5)*invB.get(2,5)*meanEPP2.get(5,0);

        double sig2VV_d = invB.get(3,0)*invB.get(3,0)*meanEPP2.get(0,0)+
                          invB.get(3,1)*invB.get(3,1)*meanEPP2.get(1,0)+
                          invB.get(3,2)*invB.get(3,2)*meanEPP2.get(2,0)+
                          invB.get(3,3)*invB.get(3,3)*meanEPP2.get(3,0)+
                          invB.get(3,4)*invB.get(3,4)*meanEPP2.get(4,0)+
                          invB.get(3,5)*invB.get(3,5)*meanEPP2.get(5,0);

        double sig2VW_d = invB.get(4,0)*invB.get(4,0)*meanEPP2.get(0,0)+
                          invB.get(4,1)*invB.get(4,1)*meanEPP2.get(1,0)+
                          invB.get(4,2)*invB.get(4,2)*meanEPP2.get(2,0)+
                          invB.get(4,3)*invB.get(4,3)*meanEPP2.get(3,0)+
                          invB.get(4,4)*invB.get(4,4)*meanEPP2.get(4,0)+
                          invB.get(4,5)*invB.get(4,5)*meanEPP2.get(5,0);

        double sig2WW_d = invB.get(5,0)*invB.get(5,0)*meanEPP2.get(0,0)+
                          invB.get(5,1)*invB.get(5,1)*meanEPP2.get(1,0)+
                          invB.get(5,2)*invB.get(5,2)*meanEPP2.get(2,0)+
                          invB.get(5,3)*invB.get(5,3)*meanEPP2.get(3,0)+
                          invB.get(5,4)*invB.get(5,4)*meanEPP2.get(4,0)+
                          invB.get(5,5)*invB.get(5,5)*meanEPP2.get(5,0);

        //+++ Statistical errors on mean combined velocity components due to Poisson counting errors +++//
        double sig2UU_N=  (1.0/n)*(
                          invB.get(0,0)*invB.get(0,0)*meanPP.get(0,0)*meanPP.get(0,0)+
                          invB.get(0,1)*invB.get(0,1)*meanPP.get(1,0)*meanPP.get(1,0)+
                          invB.get(0,2)*invB.get(0,2)*meanPP.get(2,0)*meanPP.get(2,0)+
                          invB.get(0,3)*invB.get(0,3)*meanPP.get(3,0)*meanPP.get(3,0)+
                          invB.get(0,4)*invB.get(0,4)*meanPP.get(4,0)*meanPP.get(4,0)+
                          invB.get(0,5)*invB.get(0,5)*meanPP.get(5,0)*meanPP.get(5,0));

        double sig2UV_N=(1.0/n)*(
                          invB.get(1,0)*invB.get(1,0)*meanPP.get(0,0)*meanPP.get(0,0)+
                          invB.get(1,1)*invB.get(1,1)*meanPP.get(1,0)*meanPP.get(1,0)+
                          invB.get(1,2)*invB.get(1,2)*meanPP.get(2,0)*meanPP.get(2,0)+
                          invB.get(1,3)*invB.get(1,3)*meanPP.get(3,0)*meanPP.get(3,0)+
                          invB.get(1,4)*invB.get(1,4)*meanPP.get(4,0)*meanPP.get(4,0)+
                          invB.get(1,5)*invB.get(1,5)*meanPP.get(5,0)*meanPP.get(5,0));


        double sig2UW_N=(1.0/n)*(
                          invB.get(2,0)*invB.get(2,0)*meanPP.get(0,0)*meanPP.get(0,0)+
                          invB.get(2,1)*invB.get(2,1)*meanPP.get(1,0)*meanPP.get(1,0)+
                          invB.get(2,2)*invB.get(2,2)*meanPP.get(2,0)*meanPP.get(2,0)+
                          invB.get(2,3)*invB.get(2,3)*meanPP.get(3,0)*meanPP.get(3,0)+
                          invB.get(2,4)*invB.get(2,4)*meanPP.get(4,0)*meanPP.get(4,0)+
                          invB.get(2,5)*invB.get(2,5)*meanPP.get(5,0)*meanPP.get(5,0));

        double sig2VV_N=(1.0/n)*(
                          invB.get(3,0)*invB.get(3,0)*meanPP.get(0,0)*meanPP.get(0,0)+
                          invB.get(3,1)*invB.get(3,1)*meanPP.get(1,0)*meanPP.get(1,0)+
                          invB.get(3,2)*invB.get(3,2)*meanPP.get(2,0)*meanPP.get(2,0)+
                          invB.get(3,3)*invB.get(3,3)*meanPP.get(3,0)*meanPP.get(3,0)+
                          invB.get(3,4)*invB.get(3,4)*meanPP.get(4,0)*meanPP.get(4,0)+
                          invB.get(3,5)*invB.get(3,5)*meanPP.get(5,0)*meanPP.get(5,0));

        double sig2VW_N=(1.0/n)*(
                          invB.get(4,0)*invB.get(4,0)*meanPP.get(0,0)*meanPP.get(0,0)+
                          invB.get(4,1)*invB.get(4,1)*meanPP.get(1,0)*meanPP.get(1,0)+
                          invB.get(4,2)*invB.get(4,2)*meanPP.get(2,0)*meanPP.get(2,0)+
                          invB.get(4,3)*invB.get(4,3)*meanPP.get(3,0)*meanPP.get(3,0)+
                          invB.get(4,4)*invB.get(4,4)*meanPP.get(4,0)*meanPP.get(4,0)+
                          invB.get(4,5)*invB.get(4,5)*meanPP.get(5,0)*meanPP.get(5,0));

        double sig2WW_N=(1.0/n)*(
                          invB.get(5,0)*invB.get(5,0)*meanPP.get(0,0)*meanPP.get(0,0)+
                          invB.get(5,1)*invB.get(5,1)*meanPP.get(1,0)*meanPP.get(1,0)+
                          invB.get(5,2)*invB.get(5,2)*meanPP.get(2,0)*meanPP.get(2,0)+
                          invB.get(5,3)*invB.get(5,3)*meanPP.get(3,0)*meanPP.get(3,0)+
                          invB.get(5,4)*invB.get(5,4)*meanPP.get(4,0)*meanPP.get(4,0)+
                          invB.get(5,5)*invB.get(5,5)*meanPP.get(5,0)*meanPP.get(5,0));



        //+++ Combine errors on mean velocities +++//
        double sig2U = sig2U_d + sig2U_N;
        double sig2V = sig2V_d + sig2V_N;
        double sig2W = sig2W_d + sig2W_N;

        //+++ Combine errors on mean combined velocites +++//
        double sig2UU = sig2UU_d + sig2UU_N;
        double sig2UV = sig2UV_d + sig2UV_N;
        double sig2UW = sig2UW_d + sig2UW_N;
        double sig2VV = sig2VV_d + sig2VV_N;
        double sig2VW = sig2VW_d + sig2VW_N;
        double sig2WW = sig2WW_d + sig2WW_N;



        //+++ Multiply <A>^-1 by <p> to get <v> +++//
        Matrix meanV = invA.times(meanP);

        //+++ Multiply <B>^-1 by <pp> to get <vv> +++//
        Matrix meanVV = invB.times(meanPP);


        //+++ Get velocity dispersions. Use sigma_U^2 = <UU> - <U>^2   +++//
        //+++                               sigma_UV  = <UV> - <U><V>  +++//

        double UU = meanVV.get(0,0) - meanV.get(0,0)*meanV.get(0,0);
        double UV = meanVV.get(1,0) - meanV.get(0,0)*meanV.get(1,0);
        double UW = meanVV.get(2,0) - meanV.get(0,0)*meanV.get(2,0);
        double VV = meanVV.get(3,0) - meanV.get(1,0)*meanV.get(1,0);
        double VW = meanVV.get(4,0) - meanV.get(1,0)*meanV.get(2,0);
        double WW = meanVV.get(5,0) - meanV.get(2,0)*meanV.get(2,0);

        //+++ Get square roots of velocity dispersions +++//
        //+++ Covariances can be negative, so need to use sqrt(|vv|) +++//
        double sqrtUU = Math.signum(UU)*Math.sqrt(Math.abs(UU));
        double sqrtUV = Math.signum(UV)*Math.sqrt(Math.abs(UV));
        double sqrtUW = Math.signum(UW)*Math.sqrt(Math.abs(UW));
        double sqrtVV = Math.signum(VV)*Math.sqrt(Math.abs(VV));
        double sqrtVW = Math.signum(VW)*Math.sqrt(Math.abs(VW));
        double sqrtWW = Math.signum(WW)*Math.sqrt(Math.abs(WW));

        //+++ Get variance on velocity dispersions +++//
        double sig2sig2UU = sig2UU + Math.pow(2.0*meanV.get(0,0), 2.0)*sig2U;
        double sig2sig2UV = sig2UV + meanV.get(1,0)*meanV.get(1,0)*sig2U + meanV.get(0,0)*meanV.get(0,0)*sig2V;
        double sig2sig2UW = sig2UW + meanV.get(2,0)*meanV.get(2,0)*sig2U + meanV.get(0,0)*meanV.get(0,0)*sig2W;
        double sig2sig2VV = sig2VV + Math.pow(2.0*meanV.get(1,0), 2.0)*sig2V;
        double sig2sig2VW = sig2VW + meanV.get(2,0)*meanV.get(2,0)*sig2V + meanV.get(1,0)*meanV.get(1,0)*sig2W;
        double sig2sig2WW = sig2WW + Math.pow(2.0*meanV.get(2,0), 2.0)*sig2W;

        //+++ Get variance on square roots of velocity dispersions +++//
        double sig2sigUU = (1.0/(4.0*sqrtUU*sqrtUU))*sig2sig2UU;
        double sig2sigUV = (1.0/(4.0*sqrtUV*sqrtUV))*sig2sig2UV;
        double sig2sigUW = (1.0/(4.0*sqrtUW*sqrtUW))*sig2sig2UW;
        double sig2sigVV = (1.0/(4.0*sqrtVV*sqrtVV))*sig2sig2VV;
        double sig2sigVW = (1.0/(4.0*sqrtVW*sqrtVW))*sig2sig2VW;
        double sig2sigWW = (1.0/(4.0*sqrtWW*sqrtWW))*sig2sig2WW;

        //+++ Write header +++//
        System.out.println("Deprojection of proper motions");
        System.out.println("------------------------------");
        System.out.println("\nUncertainties are due to errors on distance and Poisson noise on star counts.");
        System.out.println("No uncertainty on position is considered, so matrices A and B have no errors.");
        System.out.println("\nMoments of the velocity distribution\n------------------------------------");

        //+++ Display mean velocities and errors due to distance uncertainties +++//
        System.out.println("\nMean velocities:");
        System.out.println("<U> = " + xpx.format(meanV.get(0, 0)) + "\t+/- "+xpx.format(Math.sqrt(sig2U)));
        System.out.println("<V> = " + xpx.format(meanV.get(1, 0)) + "\t+/- "+xpx.format(Math.sqrt(sig2V)));
        System.out.println("<W> = " + xpx.format(meanV.get(2, 0)) + "\t+/- "+xpx.format(Math.sqrt(sig2W)));

        //+++ Print out velocity dispersion tensor. Note that mixed moments can be  +++//
        System.out.println("\nVelocity dispersion tensor:");
        System.out.println("[ "+xpx.format(sqrtUU)+", "+xpx.format(sqrtUV)+", "+xpx.format(sqrtUW));
        System.out.println("  "+xpx.format(sqrtUV)+", "+xpx.format(sqrtVV)+", "+xpx.format(sqrtVW));
        System.out.println("  "+xpx.format(sqrtUW)+", "+xpx.format(sqrtVW)+", "+xpx.format(sqrtWW)+" ]");

        System.out.println("\nand errors:");
        System.out.println("[ "+xpx.format(Math.sqrt(sig2sig2UU))+", "+xpx.format(Math.sqrt(sig2sig2UV))+", "+xpx.format(Math.sqrt(sig2sig2UW)));
        System.out.println("  "+xpx.format(Math.sqrt(sig2sig2UV))+", "+xpx.format(Math.sqrt(sig2sig2VV))+", "+xpx.format(Math.sqrt(sig2sig2VW)));
        System.out.println("  "+xpx.format(Math.sqrt(sig2sig2UW))+", "+xpx.format(Math.sqrt(sig2sig2VW))+", "+xpx.format(Math.sqrt(sig2sig2WW))+" ]");

        System.out.println("\nNumber of stars = " + xpx.format(n));

    }



    //+++ Method to randomly assign a distance to a star, assuming a uniform population +++//
    public static Matrix getRandomPosition(){

        Matrix R = new Matrix(3,1);

        double r;

        //+++ Iterate over random positions within cube centred on origin until a position +++//
        //+++ lying within sphere centred on origin is obtained.                           +++//

        do{
            //+++ Randomize coordinates within [-dmax:dmax] range +++//
            R.set(0,0, (Math.random() - 0.5)*2.0*dmax);
            R.set(1,0, (Math.random() - 0.5)*2.0*dmax);
            R.set(2,0, (Math.random() - 0.5)*2.0*dmax);

            //+++ Get distance from origin +++//
            r = Math.sqrt(R.get(0,0)*R.get(0,0) + R.get(1,0)*R.get(1,0) + R.get(2,0)*R.get(2,0));
        }
        //+++ If this position lies outside sphere, repeat. Corresponds to positions in eight corners of cube +++//
        while(r > dmax || r < dmin);

        return R;
    }
    //+++ Tables used to look up matrix element indices for calculating B from A +++//
    public static int irow(int nr, int j){

        if(nr==0) return 0;
        if(nr==1) return (j==0 || j==3) ? 0:1;
        if(nr==2) return (j==0 || j==3) ? 0:2;
        if(nr==3) return 1;
        if(nr==4) return (j==0 || j==3) ? 1:2;
        if(nr==5) return 2;

        return -1;
    }

    public static int icol(int nc, int j){

        if(nc==0) return (j<4) ? 0 : -1;
        if(nc==1) return (j==0 || j==2 || j==5 || j==7) ? 0 : 1;
        if(nc==2) return (j==0 || j==2 || j==5 || j==7) ? 0 : 2;
        if(nc==3) return (j<4) ? 1 : -1;
        if(nc==4) return (j==0 || j==2 || j==5 || j==7) ? 1 : 2;
        if(nc==5) return (j<4) ? 2 : -1;

        return -1;
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
        XYZ = GalacticCoordinates.NTG.times(UVW);

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


        mu = Math.sqrt(dec_dot*dec_dot + ra_dot*ra_dot);

        vt = mu * 4.74 * d;

        //+++ Convert ra to a rate of change in the coordinate +++//
        ra_dot = ra_dot / Math.cos(dec);

        //+++ Add error to distance +++//
        sigma_d = VelocityEllipsoidSimulator.normal.nextGaussian()*(VelocityEllipsoidSimulator.sigma*d);
        d = d + sigma_d;

    }

    public double getU(){ return UVW.get(0,0);}
    public double getV(){ return UVW.get(1,0);}
    public double getW(){ return UVW.get(2,0);}

}