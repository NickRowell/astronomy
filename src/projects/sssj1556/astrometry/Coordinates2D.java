package projects.sssj1556.astrometry;

import Jama.Matrix;
import java.util.LinkedList;
import java.util.List;

/**
 * Encapsulates the data needed to specify a point in an image, including
 * uncertainty. Used in this project to represent both pixel coordinates and
 * standard coordinates.
 * 
 * @author nickrowell
 */
public class Coordinates2D
{
    
    /**
     * Enumerated type used to label each standard coordinate. Useful e.g. to
     * indicate which of the two we want to fit when only fitting one parallax
     * factor.
     */
    public static enum STANDARD_COORDINATE{XSI, ETA};
    
    
    /** Coordinates in the image [pixels]. */
    public double x,y;
    
    /** Covariance on pixel coordinates [pixels^2]. */
    public double cxx, cxy, cyy;
    
    public Coordinates2D(double px, double py, double pcxx, double pcxy, double pcyy)
    {
        x    = px;
        y    = py;
        cxx  = pcxx;
        cxy  = pcxy;
        cyy  = pcyy;
    }
    
    
    /**
     * Transform image position and covariance in pixels to another frame.
     * Performs first order propagation of covariance through the transformation
     * matrix.
     * 
     * @param a    Coefficients of transformation
     * @return 
     */
    public Coordinates2D transform(double[] a)
    {
        
        double x_prime = a[0]*x + a[1]*y + a[2];
        double y_prime = a[3]*x + a[4]*y + a[5];
        
        // First order propagation of position covariance
        Matrix A = new Matrix(new double[][]{{a[0],a[1]},{a[3],a[4]}});
        Matrix cov = new Matrix(new double[][]{{cxx, cxy},{cxy, cyy}});
        
        Matrix cov_prime = A.times(cov).times(A.transpose());
        
        double c_xx_prime = cov_prime.get(0,0);
        double c_xy_prime = cov_prime.get(0,1);
        double c_yy_prime = cov_prime.get(1,1);
        
        return new Coordinates2D(x_prime, y_prime, c_xx_prime, c_xy_prime, c_yy_prime);
    }
    
    
    // Everything else --> utility functions
    
    
    /**
     * Projects points on the celestial sphere to the tangent plane.
     * 
     * @param ra_dec    Right ascension and declination of star [radians]
     * @param ra_dec_tp Right ascension and declination of tangent point [radians]
     * @return
     */
    public static Coordinates2D getStandardCoordinates(double[] ra_dec,  double[] ra_dec_tp)
    {
        // Break out right ascension and declination of tangent point
        double ra_tp  = ra_dec_tp[0];
        double dec_tp = ra_dec_tp[1];
        
        // Break out right ascension and declination of star
        double ra  = ra_dec[0];
        double dec = ra_dec[1];
        
        // First get scalar quantity lambda
        double lambda = 1.0 / (Math.sin(dec)*Math.sin(dec_tp) + Math.cos(dec)*Math.cos(dec_tp)*Math.cos(ra - ra_tp));
        
        // Now calculate standard coordinates
        double xsi = lambda * Math.cos(dec) * Math.sin(ra - ra_tp);
        double eta = lambda * (Math.cos(dec_tp) * Math.sin(dec) - Math.sin(dec_tp) * Math.cos(dec) * Math.cos(ra - ra_tp));
        
        // Create new Coordinates2D, with identity covariance
        return new Coordinates2D(xsi, eta, 1, 0, 1);
    }
    
    /**
     * Method computes the linear transformation that transforms coordinates
     * from frame X to frame Y.
     * 
     * The inliers are used to compute the fit. Sigma-clipping is used to 
     * identify any outliers from within the set of inliers; if any are found,
     * they are removed from the inliers list, and the fit is recomputed from 
     * the smaller set of inliers.
     * 
     * 
     * @param inliers   Coordinates of inlying reference points
     * @param AtoB      On exit, contains coefficients of X->Y transform
     * @param BtoA      On exit, contains coefficients of Y->X transform
     * @param c_AtoB    Sample covariance of reference points in frame Y
     * @param sig_clip  Threshold for sigma clipping
     * @param verbose   Print statistics etc
     */
    public static void linTrans(List<Correspondence> inliers,
                                double[] AtoB, double[] BtoA, double[] c_AtoB,
                                double sig_clip, boolean verbose)
    {
        // Number of points
        int N = inliers.size();
        
        // Check we have enough points for the transformation to be non-degenerate
        if(N<3)
            throw new RuntimeException("Too few reference stars to calculate transformation!");
        
        // Cx=D
        
        // Set up design matrix
        Matrix C = new Matrix(2*N,6);
        // Set up observation matrix
        Matrix D = new Matrix(2*N,1);
        // Set up covariance matrix
        Matrix S = new Matrix(2*N,2*N);
        
        // Loop over all Coordinates2D
        for(int i=0; i<N; i++)
        {
            // Handles to points in each frame
            Coordinates2D A = inliers.get(i).A;
            Coordinates2D B = inliers.get(i).B;
            
            // Each point provides two rows in design matrix...
            double[][] rows = new double[][]{{A.x, A.y,  1.0, 0.0, 0.0,  0.0},
                                             {0.0, 0.0,  0.0, A.x, A.y,  1.0}};
            
            C.setMatrix(2*i+0, 2*i+1, 0, 5, new Matrix(rows));

            // ...and two rows in data vector
            D.set(2*i+0, 0, B.x);
            D.set(2*i+1, 0, B.y);

            // Covariance on un-transformed points is used to weight fit. This
            // corresponds to the slave image coordinates (for slave->master fit)
            // and master images coordinates (for master->tangent plane fit),
            // which is the best choice in both cases.
            S.set(2*i+0, 2*i+0, A.cxx);
            S.set(2*i+1, 2*i+0, A.cxy);
            S.set(2*i+0, 2*i+1, A.cxy);
            S.set(2*i+1, 2*i+1, A.cyy);
            
        }
        
        // Weighted linear least squares solution for transform coefficients
        Matrix M = C.transpose().times(S.solve(C));             // Intermediate quantity
        Matrix x = M.solve(C.transpose().times(S.solve(D)));    // Parameter solution
        Matrix S_x = M.inverse();                               // Parameter covariance
        
        // Coefficients of forward transformation
        double a = x.get(0, 0);
        double b = x.get(1, 0);
        double c = x.get(2, 0);
        double d = x.get(3, 0);
        double e = x.get(4, 0);
        double f = x.get(5, 0);
        
        // Coefficients of backward transformation
        double a_inv =          -e/(b*d - e*a);
        double b_inv =           b/(b*d - e*a);
        double c_inv = (e*c - b*f)/(b*d - e*a);
        double d_inv =           d/(b*d - e*a);
        double e_inv =          -a/(b*d - e*a);
        double f_inv = (a*f - c*d)/(b*d - e*a);
        
        // Now calculate sample covariance on transformed reference points, and
        // print residuals expressed in units of both frames.
        Matrix model = C.times(x);      // Model
        
        Matrix S_B = new Matrix(2,2);
        Matrix S_A = new Matrix(2,2);
        
        for(int i=0; i<N; i++)
        {
            Coordinates2D A = inliers.get(i).A;
            Coordinates2D B = inliers.get(i).B;
            
            // Sample covariance in frame A
            double Ai_model = a_inv*B.x + b_inv*B.y + c_inv;
            double Aj_model = d_inv*B.x + e_inv*B.y + f_inv;
            Matrix R_A = new Matrix(new double[][]{{Ai_model - A.x},
                                                   {Aj_model - A.y}});
            S_A.plusEquals(R_A.times(R_A.transpose()));
            
            
            // Sample covariance in frame B
            Matrix R_B = new Matrix(new double[][]{{model.get(2*i+0, 0) - B.x},
                                                   {model.get(2*i+1, 0) - B.y}});
            S_B.plusEquals(R_B.times(R_B.transpose()));
            
        }
        
        S_A.timesEquals(1.0/(N-1.0));
        S_B.timesEquals(1.0/(N-1.0));
        
        if(verbose)
        {
            System.out.println("Sample covariance matrix [A->B]:");
            System.out.printf("%5g %5g \n %5g %5g \n", S_B.get(0,0), S_B.get(0,1), S_B.get(1,0), S_B.get(1,1));
            System.out.println("Sample covariance matrix [B->A]:");
            System.out.printf("%5g %5g \n %5g %5g \n", S_A.get(0,0), S_A.get(0,1), S_A.get(1,0), S_A.get(1,1));
        }
        
        // Now measure residuals for each point, covariance weighted residual,
        // and detect outliers.
        List<Correspondence> outliers = new LinkedList<Correspondence>();
        
        // Calculate RMS error for the forward and backward transforms
        double rms_A=0, rms_B=0;
        
        for(int i=0; i<N; i++)
        {
            
            Coordinates2D A = inliers.get(i).A;
            Coordinates2D B = inliers.get(i).B;
            
            // Transform reference points from frame Y to X using the inverse
            // transform, in order to express the residuals in frame X as well.
            double Ai_model = a_inv*B.x + b_inv*B.y + c_inv;
            double Aj_model = d_inv*B.x + e_inv*B.y + f_inv;
            
            // Residual vector for this point in frame A
            Matrix R_A = new Matrix(new double[][]{{Ai_model - A.x},
                                                   {Aj_model - A.y}});
            
            // Residual vector for this point in frame B
            Matrix R_B = new Matrix(new double[][]{{model.get(2*i+0, 0) - B.x},
                                                   {model.get(2*i+1, 0) - B.y}});
            
            // Sum squared error
            rms_A += R_A.transpose().times(R_A).get(0, 0);
            rms_B += R_B.transpose().times(R_B).get(0, 0);
            
            // Residuals (non covariance-weighted)
            double resid_A = Math.sqrt(R_A.transpose().times(R_A).get(0, 0));
            double resid_B = Math.sqrt(R_B.transpose().times(R_B).get(0, 0));
            
            // Contributions to chi-square
            double sig2_A = R_A.transpose().times(S_A.solve(R_A)).get(0, 0);
            double sig2_B = R_B.transpose().times(S_B.solve(R_B)).get(0, 0);
            
            // Residuals (covariance weighted). These are the same in each frame.
            double sigmas_A = Math.sqrt(sig2_A);
            double sigmas_B = Math.sqrt(sig2_B);
            
            if(verbose)
            {
                System.out.printf("Reference point "+i+" : %5.4g %5.4g [%5.4g]",resid_A, resid_B, sigmas_B);
                if(sigmas_B > sig_clip)
                    System.out.println("*");
                else
                    System.out.println("");
                
            }
            
            if(sigmas_B > sig_clip)
            {
                // Add point to outliers list
                outliers.add(inliers.get(i));
            }
            
        }
        
        if(verbose)
        {
            // Print rms errors
            System.out.println("RMS error [A->B] = "+Math.sqrt(rms_B/N));
            System.out.println("RMS error [B->A] = "+Math.sqrt(rms_A/N));
        }
        
        // Found outliers; remove them then refit
        if(outliers.size()>0)
        {
            if(verbose)
                System.out.println("\nFound "+outliers.size()+" outlier(s), recomputing...\n");
            
            // Remove outliers
            inliers.removeAll(outliers);
            
            // Call method recursively to implement outlier rejection
            linTrans(inliers, AtoB, BtoA, c_AtoB, sig_clip, verbose);
            
        }
        // No outliers found; return results of fit.
        else
        {
            // Pass results out by reference
            AtoB[0] = a;
            AtoB[1] = b;
            AtoB[2] = c;
            AtoB[3] = d;
            AtoB[4] = e;
            AtoB[5] = f;

            BtoA[0] = a_inv;
            BtoA[1] = b_inv;
            BtoA[2] = c_inv;
            BtoA[3] = d_inv;
            BtoA[4] = e_inv;
            BtoA[5] = f_inv;

            c_AtoB[0] = S_B.get(0,0);
            c_AtoB[1] = S_B.get(1,0);
            c_AtoB[2] = S_B.get(1,1);
        }
    }
    
    
    
    
    
    /**
     * Convert angle from hours-minutes-seconds to radians.
     * @param h
     * @param m
     * @param s
     * @return 
     */
    public static double hmsToRadians(double h, double m, double s)
    {
        double degrees = h*15 + (m*15)/60.0 + (s*15)/3600.0;
        return Math.toRadians(degrees);
    }
    
    /**
     * Convert angle from degrees-minutes-seconds to radians.
     * @param d
     * @param m
     * @param s
     * @return  
     */
    public static double dmsToRadians(double d, double m, double s)
    {
	if(d<0)
        {
            return Math.toRadians( d - m/60 - s/3600 );
        }
	else
        {
            return Math.toRadians( d + m/60 + s/3600 );
        }
    }
    
    
    public static double[] radsToHms(double rads)
    {
        // Values must be positive!
        if(rads<0)
            throw new RuntimeException("Radians must be positive!");
        
        
        // Floating point values
        double fhours, fmins, fsecs;
        
        fhours = Math.toDegrees(rads)/15.0;
        
        // Whole number of hours
        double ihours = Math.floor(fhours);
        
        fmins = (fhours-ihours)*60.0;
        
        double imins = Math.floor(fmins);
        
        fsecs = (fmins - imins)*60.0;
        
        return new double[]{ihours, imins, fsecs};
        
        
    }
    
    
    public static double[] radsToDms(double rads)
    {
        // Check sign
        double sign = rads > 0 ? 1 : -1;
        
        // Get absolute value
        rads = Math.abs(rads);
        
        // Floating point values
        double fdeg, farcmins, farcsecs;
        
        fdeg = Math.toDegrees(rads);
        
        // Whole number of degrees
        double ideg = Math.floor(fdeg);
        
        farcmins = (fdeg-ideg)*60.0;
        
        double imins = Math.floor(farcmins);
        
        farcsecs = (farcmins - imins)*60.0;
        
        return new double[]{ideg*sign, imins, farcsecs};
        
        
    }
    
}
