package sss.astro;

import Jama.*;
import java.util.Random;
import java.text.DecimalFormat;

/**
 * This class is designed to investigate the effect of correlated errors on the proper
 * motion solutions and reduced chi-square statistics supplied by the SSA.
 * 
 * Proper motion model has four parameters: the zeropoint and rate of change of
 * each of two tangent plane coordinates. There are either 6 or 8 data points
 * depending on whether three or four epochs of detection exist. So, the
 * resulting weighted sum-of-squares distribution should follow that of either 
 * a two or four degree of freedom chi-square. Code here is hard-wired for 
 * four epochs, so four degrees of freedom is the order of the day.
 *
 * @author nickrowell
 */
public class SyntheticProperMotions {

    //+++ Used to draw random errors +++//
    static Random normal = new Random();

    public static void main(String[] args){

        //+++ Adopt intrinsic parameters of proper motion object +++//
        //+++ in tangent plane coordinates.                      +++//
        double eta_0 = 2;        // position at epoch = 0;
        double eta_dot = 10;
        double xi_0 = 3;
        double xi_dot = 20;

        //+++ Adopt covariance matrix for (independent) observed positions +++//
        Matrix covariance = new Matrix(new double[][]{{1,0},
                                                      {0,1}});

        //+++ Input binning parameters +++//
        double lower = 0.0;
        double upper = 100.0;
        double bin_width = 0.02;

        //+++ Set up binning process +++//
        int Nbins = (int) ((upper - lower) / bin_width);

        double Pchi2[] = new double[Nbins];          // Frequency bins
        double Cchi2[] = new double[Nbins];       // Cumulative bins

        int bin;
        
        //+++ Get Eigendecomposition of covariance matrix +++//
        EigenvalueDecomposition eig = new EigenvalueDecomposition(covariance);

        //+++ Use eigenvalues to draw random errors in frame defined +++//
        //+++ by eigenvectors of covariance matrix.                  +++//
        Matrix D = eig.getD();
        Matrix V = eig.getV();

        double diag1 = Math.sqrt(D.get(0, 0));     // Errors (sigma) along principal axes
        double diag2 = Math.sqrt(D.get(1, 1));

        //+++ Now select four epochs of observation +++//
        double[] epoch = {0.0,10.0,20.0,40.0};

        //+++ Set up design matrix for observations +++//
        Matrix A = new Matrix(new double[][]{{1,epoch[0],0,0},
                                             {1,epoch[1],0,0},
                                             {1,epoch[2],0,0},
                                             {1,epoch[3],0,0},
                                             {0,0,1,epoch[0]},
                                             {0,0,1,epoch[1]},
                                             {0,0,1,epoch[2]},
                                             {0,0,1,epoch[3]}});

        //+++ Set up full covariance matrix for all observations +++//
        //+++ and restricted matrix of just variance terms.      +++//
        Matrix M = new Matrix(8,8);
        Matrix W = new Matrix(8,8);

        for(int m=0; m<4; m++){
            M.set(m, m, covariance.get(0, 0));          // variance on eta
            W.set(m, m, covariance.get(0, 0));

            M.set(m+4, m+4, covariance.get(1, 1));      // variance on xi
            W.set(m+4, m+4, covariance.get(1, 1));

            M.set(m, m+4, covariance.get(0,1));         // covariance terms
            M.set(m+4, m, covariance.get(1,0));
        }

        //+++ Get covariance matrix for parameters using full covariance of observations +++//
        Matrix m = (A.transpose().times(M.inverse()).times(A)).inverse();
        //+++ ...or just variances on observations.                                      +++//
        //Matrix m = (A.transpose().times(W.inverse()).times(A)).inverse();

        //+++ Loop over many realisations of the observations +++//
        for(int r=0; r<1000000; r++){

            //+++ Get intrinsic eta,xi positions at each epoch and add noise +++//
            Matrix y = new Matrix(8,1);

            for(int ep=0; ep<4; ep++){

                //+++ Draw position errors along principal axes of covariance matrix +++//
                // Note that the extra factor of root 2 here is to simulate underestimated
                // errors.
                double e1 = normal.nextGaussian() * (diag1/Math.sqrt(2.0)),
                       e2 = normal.nextGaussian() * (diag2/Math.sqrt(2.0));

                //+++ Error vector in eigenbasis +++//
                Matrix error_eigen = new Matrix(new double[][]{{e1}, {e2}});

                //+++ Use eigenbasis of covariance matrix to rotate this to +++//
                //+++ align with tangent plane coordinates.                 +++//
                Matrix error_tangent = V.times(error_eigen);

                //+++ Now add error terms onto observed positions +++//
                y.set(ep,   0, eta_0 + eta_dot*epoch[ep] + error_tangent.get(0,0));
                y.set(ep+4, 0, xi_0  + xi_dot*epoch[ep]  + error_tangent.get(1,0));

            }

            //+++ Now solve for proper motion using full covariance matrix +++//
            Matrix x = (A.transpose().times(M.inverse()).times(A)).solve(A.transpose().times(M.inverse()).times(y));
            //+++ ... or restricted variance-only matrix +++//
            //Matrix x = (A.transpose().times(W.inverse()).times(A)).solve(A.transpose().times(W.inverse()).times(y));


            //System.out.println(x.get(0,0)+"\t"+x.get(1,0)+"\t"+x.get(2,0)+"\t"+x.get(3,0));



            //+++ Use solution to get column vector of model points +++//
            Matrix mu = new Matrix(8,1);

            for(int ep=0; ep<4; ep++){
                mu.set(ep,0,x.get(0, 0) + x.get(1,0)*epoch[ep]);
                mu.set(ep+4,0,x.get(2, 0) + x.get(3,0)*epoch[ep]);
            }

            //+++ Now get chi^2 for this realisation using full covariance matrix +++//
            Matrix chi2 = (y.minus(mu).transpose()).times(M.inverse()).times(y.minus(mu));
            //+++ ... or restricted variance-only matrix +++//
            //Matrix chi2 = (y.minus(mu).transpose()).times(W.inverse()).times(y.minus(mu));

            //+++ Now get chi^2 +++//
            double chi_squared = chi2.get(0,0)/4.0;

            bin = (int) Math.floor((chi_squared - lower) / bin_width);

            Pchi2[(bin < Nbins) ? bin : Nbins-1]++;                    // Frequency
            for (int i = 0; i < Nbins; i++) if (i >= bin) Cchi2[i]++;   // Cumulative
        }

        //+++ normalisation factors to convert histogram to pdf +++//
        double norm = Cchi2[Nbins-1]*bin_width;

        DecimalFormat xpxxx = new DecimalFormat("0.000");

	//+++ Print out Chi^2 histograms +++//
	for(int i = 0; i < Nbins; i++){
            String chi2 = xpxxx.format((i*bin_width + lower + (bin_width/2)));
	    System.out.println(chi2 + "\t" + Pchi2[i]/norm + "\t" + Cchi2[i]/Cchi2[Nbins-1]);

	}

        



    }




}
