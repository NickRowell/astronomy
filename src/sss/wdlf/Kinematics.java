package sss.wdlf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;

import Jama.Matrix;
import astrometry.ProperMotionDeprojection;


/**
 * Class performs kinematic analysis on WD candidates.
 *
 *
 * @author nrowell
 * @version $Id$
 */
public class Kinematics {
	

    public static void main(String args[]) throws IOException, Exception{

		//+++ Get array of input files corrsponding to WD candidates from each survey +++//
		File input[] = {new File("/home/nrowell/Astronomy/software/sss_wdlf/Catalogues/LowPM/WDs_fit.txt"),
						new File("/home/nrowell/Astronomy/software/sss_wdlf/Catalogues/HighPM/WDs_fit.txt")};
	
		//+++ Set tangential velocity range. Stars in this range will be included in deprojection +++//
		double vtan_lower = 200.0;
        double vtan_upper = 600.0;


	    /*
	     * Quantities concerning first moments of velocity distribution:
	     *
	     */
	
		//+++ Identity matrix +++//
		Matrix I = Matrix.identity(3,3);
	
	    //+++ Position vector +++//
		Matrix r = new Matrix(3,1);
	
	        //+++ Projection matrix A +++//
		Matrix A = new Matrix(3,3);
	
		//+++ Proper motion velocity +++//
		Matrix p = new Matrix(3,1);
	
		//+++ Proper motion velocity error due to distance errors +++//
		Matrix ep2 = new Matrix(3,1);
	
	    //+++ Quantities averaged over all stars +++//
		Matrix meanP = new Matrix(3,1);
		Matrix meanA = new Matrix(3,3);
        Matrix meanEP2 = new Matrix(3,1);


        String data;         // To store each successive line from catalogues
        double N = 0.0;      // To sum all (weighted contributions from) stars that go into mean quantities

        ProperMotionDeprojection deprojection = new ProperMotionDeprojection();
        
        
        //+++ Read in all stellar data by looping over all input catalogues +++//
        for (int s = 0; s < 3; s++) {

            BufferedReader in = new BufferedReader(new FileReader(input[s]));

            //+++ Set flag determining whether low or high proper motion records are being read in +++//
            int flag = (s == 0) ? 0 : 1;

            while ((data = in.readLine()) != null) {

                WhiteDwarf star = new WhiteDwarf(data, flag);

                //+++ Get corresponding photometric models +++//
                String[] models = {in.readLine(), in.readLine(), in.readLine(), in.readLine(), in.readLine(), in.readLine()};

                //+++ Set distance to star using either fitted colours or values from literature +++//
                if (star.isKnownUCWD()) {
                    star.setPublishedAtmosphere();
                } else {
                    star.setModels(models);
                    star.setAtmosphereFractions();
                    star.setPhotoPI(models);
                }


                //+++ Check H and He atmosphere solutions pass kinematic constraints +++//
                boolean H_ok  = ((4.74 * star.dH * star.mu > vtan_lower) && (4.74 * star.dH * star.mu < vtan_upper));
                boolean He_ok = ((4.74 * star.dHe * star.mu > vtan_lower) && (4.74 * star.dHe * star.mu < vtan_upper));


                double ra = Math.toRadians(star.ra);
                double dec = Math.toRadians(star.dec);
                double dec_dot = star.mu_d;
                // Rate of change of coordinates, in arcsecs per year
                double ra_dot = star.mu_acosd / Math.cos(dec);


                //+++ Unit position vector and projection operator are the same for each atmosphere type +++//
//                r.set(0, 0, Math.cos(ra) * Math.cos(dec));
//                r.set(1, 0, Math.sin(ra) * Math.cos(dec));
//                r.set(2, 0, Math.sin(dec));

                //+++ Calculate components of projection matrix relative to Equatorial triad +++//
//                A = I.minus(r.times(r.transpose()));

                //+++ Transform A to Galactic coordinate frame +++//
//                A = GalacticCoordinates.GTN.times(A.times(GalacticCoordinates.NTG));



                /*    Deprojection of proper motion
                 *    for each atmosphere type.
                 *
                 */

                //+++ Check if star passes tangential velocity threshold +++//
                if (H_ok && star.fracH > 0.0) {

                	double dist = star.dH;
                	double sigDist = star.sig_dH;
                	
                	
                	
//                    //+++ Calculate proper motion vector components relative to Equatorial triad +++//
//                    p.set(0, 0, 4.74 * star.dH * (-Math.sin(ra) * Math.cos(dec) * ra_dot - Math.cos(ra) * Math.sin(dec) * dec_dot));
//                    p.set(1, 0, 4.74 * star.dH * (Math.cos(ra) * Math.cos(dec) * ra_dot - Math.sin(ra) * Math.sin(dec) * dec_dot));
//                    p.set(2, 0, 4.74 * star.dH * (Math.cos(dec) * dec_dot));
//
//                    //+++ Transform proper motion vector to Galactic frame +++//
//                    p = GalacticCoordinates.GTN.times(p);
//
//                    //+++ Get proper motion velocity error in Galactic frame +++//
//                    ep2.set(0,0,Math.pow(p.get(0,0)*star.sig_dH/star.dH, 2.0));
//                    ep2.set(1,0,Math.pow(p.get(1,0)*star.sig_dH/star.dH, 2.0));
//                    ep2.set(2,0,Math.pow(p.get(2,0)*star.sig_dH/star.dH, 2.0));

                    //+++ Place a distance error threshold on stars +++//
                    if(star.dH/star.sig_dH > 10){
                    
                    	deprojection.addObject(ra, dec, ra_dot, dec_dot, dist, sigDist, star.fracH);
                    	
//                        //+++ Weight components and add to sum total for ensemble +++//
//                        meanP    = meanP.plus(p.times(star.fracH));
//                        meanA    = meanA.plus(A.times(star.fracH));
//                        meanEP2  = meanEP2.plus(ep2.times(star.fracH*star.fracH));
//
//                        N  += star.fracH;
                    }
                    
                }

                //+++ Check if star passes tangential velocity threshold +++//
                if (He_ok && star.fracHe > 0.0) {
                
                	double dist = star.dHe;
                	double sigDist = star.sig_dHe;
                	
//                    //+++ Calculate proper motion vector components relative to Equatorial triad +++//
//                    p.set(0, 0, 4.74 * star.dHe * (-Math.sin(ra) * Math.cos(dec) * ra_dot - Math.cos(ra) * Math.sin(dec) * dec_dot));
//                    p.set(1, 0, 4.74 * star.dHe * (Math.cos(ra) * Math.cos(dec) * ra_dot - Math.sin(ra) * Math.sin(dec) * dec_dot));
//                    p.set(2, 0, 4.74 * star.dHe * (Math.cos(dec) * dec_dot));
//
//                    //+++ Transform proper motion vector to Galactic frame +++//
////                    p = GalacticCoordinates.GTN.times(p);
//
//                    //+++ Get proper motion velocity error in Galactic frame +++//
//                    ep2.set(0,0,Math.pow(p.get(0,0)*star.sig_dHe/star.dHe, 2.0));
//                    ep2.set(1,0,Math.pow(p.get(1,0)*star.sig_dHe/star.dHe, 2.0));
//                    ep2.set(2,0,Math.pow(p.get(2,0)*star.sig_dHe/star.dHe, 2.0));

                    //+++ Place a distance error threshold on stars +++//
                    if(star.dHe/star.sig_dHe > 10){
                    	
                    	deprojection.addObject(ra, dec, ra_dot, dec_dot, dist, sigDist, star.fracHe);
 
//                        //+++ Weight components and add to sum total for ensemble +++//
//                        meanP = meanP.plus(p.times(star.fracHe));
//                        meanA = meanA.plus(A.times(star.fracHe));
//                        meanEP2 = meanEP2.plus(ep2.times(star.fracHe*star.fracHe));
//
//                        N  += star.fracHe;
                    }
                }

            }

        }
        
        Matrix mean = deprojection.getMean();
        
        

//        //+++ Divide A, B, p & pp component-wise by number of stars to get mean +++//
//        meanP  = meanP.times(1.0 / N);
//        meanEP2 = meanEP2.times(1.0 / (N*N));
//        meanA  = meanA.times(1.0 / N);
//
// 
//        //+++ Invert <A> +++//
//        Matrix invA = meanA.inverse();
//
//        DecimalFormat xpxxEx = new DecimalFormat("0.00E0");
//        DecimalFormat xpx    = new DecimalFormat("0.0");
//
//
//        //+++ Get errors on mean velocities from distance uncertainties alone +++//
//        double sig2U_d = ((invA.get(0,0)*invA.get(0,0))*meanEP2.get(0,0)) +
//                         ((invA.get(0,1)*invA.get(0,1))*meanEP2.get(1,0)) +
//                         ((invA.get(0,2)*invA.get(0,2))*meanEP2.get(2,0));
//        double sig2V_d = ((invA.get(1,0)*invA.get(1,0))*meanEP2.get(0,0)) +
//                         ((invA.get(1,1)*invA.get(1,1))*meanEP2.get(1,0)) +
//                         ((invA.get(1,2)*invA.get(1,2))*meanEP2.get(2,0));
//        double sig2W_d = ((invA.get(2,0)*invA.get(2,0))*meanEP2.get(0,0)) +
//                         ((invA.get(2,1)*invA.get(2,1))*meanEP2.get(1,0)) +
//                         ((invA.get(2,2)*invA.get(2,2))*meanEP2.get(2,0));
//
//        //+++ Get statistical uncertainty on mean velocities due to Poisson counting errors +++//
//        double sig2U_N = (1.0/N)*
//                         (((invA.get(0,0)*invA.get(0,0))*meanP.get(0,0)*meanP.get(0,0)) +
//                          ((invA.get(0,1)*invA.get(0,1))*meanP.get(1,0)*meanP.get(1,0)) +
//                          ((invA.get(0,2)*invA.get(0,2))*meanP.get(2,0)*meanP.get(2,0)));
//        double sig2V_N = (1.0/N)*
//                         (((invA.get(1,0)*invA.get(1,0))*meanP.get(0,0)*meanP.get(0,0)) +
//                          ((invA.get(1,1)*invA.get(1,1))*meanP.get(1,0)*meanP.get(1,0)) +
//                          ((invA.get(1,2)*invA.get(1,2))*meanP.get(2,0)*meanP.get(2,0)));
//        double sig2W_N = (1.0/N)*
//                         (((invA.get(2,0)*invA.get(2,0))*meanP.get(0,0)*meanP.get(0,0)) +
//                          ((invA.get(2,1)*invA.get(2,1))*meanP.get(1,0)*meanP.get(1,0)) +
//                          ((invA.get(2,2)*invA.get(2,2))*meanP.get(2,0)*meanP.get(2,0)));
//
// 
//        //+++ Combine errors on mean velocities +++//
//        double sig2U = sig2U_N + sig2U_d;
//        double sig2V = sig2V_N + sig2V_d;
//        double sig2W = sig2W_N + sig2W_d;
//
//        //+++ Multiply <A>^-1 by <p> to get <v> +++//
//        Matrix meanV = invA.times(meanP);
//
//        //+++ Write header +++//
//        System.out.println("Deprojection of proper motions");
//        System.out.println("------------------------------");
//        System.out.println("\nUncertainties are due to errors on distance and Poisson noise on star counts.");
//        System.out.println("\nPoisson error may be calculated wrongly - check this out.");
//        System.out.println("No uncertainty on position is considered, so matrix A has no error.");
//        System.out.println("\nMoments of the velocity distribution\n------------------------------------");
//
//        //+++ Display mean velocities and errors due to distance uncertainties +++//
//        System.out.println("\nMean velocities:");
//        System.out.println("<U> = " + xpx.format(meanV.get(0, 0)) + "\t+/- "+xpx.format(Math.sqrt(sig2U)));
//        System.out.println("<V> = " + xpx.format(meanV.get(1, 0)) + "\t+/- "+xpx.format(Math.sqrt(sig2V)));
//        System.out.println("<W> = " + xpx.format(meanV.get(2, 0)) + "\t+/- "+xpx.format(Math.sqrt(sig2W)));
//
//        
//        System.out.println("\nNumber of stars = " + xpx.format(N));
    }


	
	
	
}
