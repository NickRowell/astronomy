package projects.sssj1556.data;

import Jama.Matrix;
import astrometry.Ephemeris;

import java.io.File;
import java.util.List;
import java.io.IOException;
import java.util.LinkedList;

import projects.sssj1556.astrometry.AstrometricDatum;
import projects.sssj1556.astrometry.Coordinates2D;
import projects.sssj1556.astrometry.Coordinates2D.STANDARD_COORDINATE;
import projects.sssj1556.astrometry.Detection;
import projects.sssj1556.astrometry.StackedImage;
import projects.sssj1556.astrometry.Star;
import time.Epoch;

/**
 * An instance of this class represents a single complete dataset for an
 * individual target in the parallax program.
 * 
 * @author nickrowell
 */
public class ParallaxDataset
{
    /** Array of Stars used in analysis. */
    public static ParallaxDataset[] datasets = new ParallaxDataset[]
    {
        new LHS3250()
//        new SDSSJ0854(),
//        new SDSSJ0947(),
//        new SSSJ0021p2640(),
//        new SSSJ0052p3450(),
//        new SSSJ0233p2125(),
//        new SSSJ1556m0805(),
//        new SSSJ2217p3707(),
//        new SSSJ2229p2327(),
//        new SSSJ2230p1523()
    };
    
    /** Official designation within this project. */
    public String designation;
    
    /** Path to top level data directory. */
    public File path;
    
    /** 
     * All StackedImages INCLUDING THE MASTER - useful to have a complete list
     * of images for star.
     */
    public List<StackedImage> slaves;
    /** The StackedImage selected as the master frame. */
    public StackedImage master;
    
    /** All reference stars including the target star. */
    public List<Star> allstars;
    
    /** The Star selected as the parallax target. */
    public Star target;
    
    /** 
     * Could specify a different star as the tangent point, so that we can
     * potentially measure parallax for other field stars that are off-centre
     * in the image.
     */
    
    
    /**
     * Main parallax calculation method.
     * @throws java.io.IOException
     */
    public void performAstrometryReductions() throws IOException
    {
        // Get Earth ephemeris, for computing parallax factors
        Ephemeris ephemeris = Ephemeris.getEphemeris(Ephemeris.Body.EARTH);
        
        // First, register all of the slave images with the master image. This
        // computes the affine transformation from slave image coordinates to master
        // image coordinates.
        for(StackedImage slave : slaves)
        {
            slave.computeSlaveToMasterImageTransform(master);
        }
        
        // Now print imagemagick script for generating warped slave images, for
        // visualisation purposes:
        
        // Get position of target star in master image
        Detection target_det = master.getDetection(target);    
        int t_x = (int)Math.rint(target_det.x);
        int t_y = (int)Math.rint(master.height - target_det.y);
        for(StackedImage slave : slaves)
        {
            System.out.println(slave.imageMagickTransformCommand(t_x, t_y));
        }
        
        // Now we use the master reference stars image and celestial coordinates
        // to compute the transformation from master image coordinates to 
        // tangent plane coordinates. Note that we proper-motion-correct the
        // reference star equatorial coordinates to the epoch of the master
        // frame before calculating the transformation.
        //
        // Note - RECONS do this part differently. They use the Guide Star Catalogue
        // to measure the rotation of their field wrt the celestial coordinates,
        // then fix the scale by multiplying by the plate scale. The zeropoint is
        // obtained by finding the right ascension & declination of their target
        // star in the 2MASS catalogue. A bit more long-winded and probably not
        // much better.
        master.computeImageToTangentPlaneTransform(target);
        
        // Now, get the tangent plane coordinates of the target star at each
        // epoch. Also compute the model coefficients (time baseline for proper
        // motion contribution, parallax factors for parallax contribution).
        
        List<AstrometricDatum> inliers = new LinkedList<AstrometricDatum>();
        
        for(StackedImage slave : slaves)
        {
            // Image coordinates of target, transformed to frame of master image
            Coordinates2D target_master = slave.imageCoordinatesInMasterFrame(slave.getDetection(target));
            
            // Standard coordinates of target, transformed from master frame
            Coordinates2D target_standard = master.imageCoordinatesToStandardCoordinates(target_master);
            
            // Get celestial coordinates of the target star at the slave epoch
            double[] raDec = target.getCoordinatesAtEpoch(slave.epoch);
            double ra  = raDec[0];
            double dec = raDec[1];
            
            // Parallax factors in celestial coordinates.
            double f_ra  = ephemeris.getFa(slave.epoch.jd(), ra, dec);
            double f_dec = ephemeris.getFd(slave.epoch.jd(), ra, dec);
            
            // Convert parallax factors to standard coordinates
            double f_xsi = f_ra * Math.cos(dec);
            double f_eta = f_dec;
            
            // Proper motion time baseline to master [Julian years]
            double t_baseline = (slave.epoch.jd() - master.epoch.jd())/Epoch.JULIAN_DAYS_PER_YEAR;
            
            inliers.add(new AstrometricDatum(f_xsi, f_eta, t_baseline, target_standard));
        }
        
        // Perform fit and print results
        double[] p     = new double[5];
        double[][] cov = new double[5][5];
        astrometricFit5Params(inliers, p, cov, 2.0);
        
        // Perform fit and print results
//        double[] p     = new double[3];
//        double[][] cov = new double[3][3];
//        astrometricFit3ParamsOneCoordinate(inliers, p, cov, 2.0, STANDARD_COORDINATE.ETA);
        
    }
    
    
    /**
     * Astrometric fit for parallax, proper motion and zeropoints.
     * @param inliers   List of inlying data points to be used in fit.
     * @param p         On exit, contains the fitted astrometric parameters.
     * @param cov       On exit, contains the parameter covariance matrix.
     * @param sig_clip  Threshold on residuals for outlier rejection.
     */
    public void astrometricFit5Params(List<AstrometricDatum> inliers,
                               double[] p, double[][] cov, double sig_clip)
    {
        
        // Number of points
        int N = inliers.size();
        
        // Check we have enough points to calculate the astrometric fit
        if(N<3)
            throw new RuntimeException("Too few epochs to determine astrometric fit!");
        
        // Create matrices to store components of astrometric fit.
        //
        // Cx = D
        
        // Design matrix [contains astrometric model]
        Matrix C = new Matrix(2*N,5);
        // Data matrix [contains observed tangent plane coordinates]
        Matrix D = new Matrix(2*N,1);
        // Covariance matrix [contains covariance on observed tan. plane coordinates]
        Matrix S = new Matrix(2*N,2*N);
        
        // The model we solve is:
        //
        // F_xsi*pi + xsi_0 + t*mu_xsi = xsi
        // F_eta*pi + eta_0 + t*mu_eta = eta
        //
        // Rearrange into the form:
        //
        // |F_xsi_1  1  t_1  0  0   |   |pi    |   |xsi_1|
        // |F_eta_1  0  0    1  t_1 | * |xsi_0 | = |eta_1|
        // |          .             |   |mu_xsi|   |  .  |
        // |          .             |   |eta_0 |   |  .  |
        // |          .             |   |mu_eta|   |  .  |
        // |F_xsi_N  1  t_N  0  0   |              |xsi_N|
        // |F_eta_N  0  0    1  t_N |              |eta_N|
        //
        //
        // Or: Cx = D
        //
        // where x contains the astrometric parameters:
        // 
        // x = (pi, xsi_0, mu_xsi, eta_0, mu_eta)^T
        //
        for(int i=0; i<inliers.size(); i++)
        {
            
            AstrometricDatum entry = inliers.get(i);
            
            // Load values into matrices
            C.set(2*i+0, 0, entry.F_xsi);
            C.set(2*i+0, 1, 1.0);
            C.set(2*i+0, 2, entry.t);
            C.set(2*i+0, 3, 0.0);
            C.set(2*i+0, 4, 0.0);
            
            C.set(2*i+1, 0, entry.F_eta);
            C.set(2*i+1, 1, 0.0);
            C.set(2*i+1, 2, 0.0);
            C.set(2*i+1, 3, 1.0);
            C.set(2*i+1, 4, entry.t);
            
            D.set(2*i+0, 0, entry.tp.x);     // xsi
            D.set(2*i+1, 0, entry.tp.y);     // eta
            
            S.set(2*i+0, 2*i+0, entry.tp.cxx);   // c_xx
            S.set(2*i+1, 2*i+0, entry.tp.cxy);   // c_xe
            S.set(2*i+0, 2*i+1, entry.tp.cxy);   // c_xe
            S.set(2*i+1, 2*i+1, entry.tp.cyy);   // c_ee
            
        }
        
        // Weighted least squares:
        Matrix M = C.transpose().times(S.solve(C));
        // Solve for astrometric parameters
        Matrix x = M.solve(C.transpose().times(S.solve(D)));
        // Parameter covariance
        Matrix S_x = M.inverse();
        // Model
        Matrix model = C.times(x);
        
        // Calculate sample covariance matrix
        Matrix Sc_tp = new Matrix(2,2);
        for(int i=0; i<N; i++)
        {
            // Residual vector
            Matrix R_tp = new Matrix(new double[][]{{model.get(2*i+0, 0)-D.get(2*i+0, 0)},
                                                    {model.get(2*i+1, 0)-D.get(2*i+1, 0)}});
            
            Sc_tp.plusEquals(R_tp.times(R_tp.transpose()));
        }
        // Normalise sample covariance
        Sc_tp.timesEquals(1.0/(N-1.0));
        
        // Now print residuals for each point and detect outliers
        System.out.println("\n\n#--- Astrometric fit residuals ---#");
        System.out.println("#");
        System.out.println("# Columns are:");
        System.out.println("# 1) Epoch [UTC]");
        System.out.println("# 2) Epoch [MJD]");
        System.out.println("# 3) Residuals, unweighted [pixels]");
        System.out.println("# 4) Residuals, unweighted [standard coordinates]");
        System.out.println("# 5) Residuals, weighted by formal covariance on data");
        System.out.println("# 6) Residuals, weighted by sample covariance on data");
        System.out.println("# 7) Asterisk indicates an outlier");
        
        // List of detected outliers
        List<AstrometricDatum> outliers = new LinkedList<AstrometricDatum>();
        
        for(int i=0; i<N; i++)
        {
            // Model standard coordinates
            Coordinates2D tp_model = new Coordinates2D(model.get(2*i+0, 0), model.get(2*i+1, 0), 1, 0, 1);
            
            // Observed standard coordinates
            Coordinates2D tp_obs = new Coordinates2D(D.get(2*i+0, 0), D.get(2*i+1, 0), 1, 0, 1);
            
            // Transform both to pixel coordinates in the master image
            Coordinates2D pix_model = tp_model.transform(master.tp2im);
            Coordinates2D pix_obs   = tp_obs.transform(master.tp2im);
            
            // Residual vectors
            Matrix R_tp = new Matrix(new double[][]{{tp_model.x-tp_obs.x},
                                                    {tp_model.y-tp_obs.y}});
            
            Matrix R_pix = new Matrix(new double[][]{{pix_model.x-pix_obs.x},
                                                     {pix_model.y-pix_obs.y}});
            
            // Residuals (non covariance-weighted)
            double resid_TP  = Math.sqrt(R_tp.transpose().times(R_tp).get(0, 0));
            double resid_PIX = Math.sqrt(R_pix.transpose().times(R_pix).get(0, 0));
            
            // Extract formal covariance on observed standard coordinates
            Matrix S_tp = S.getMatrix(2*i+0, 2*i+1, 2*i+0, 2*i+1);

            // Formal covariance-weighted residual
            double sigma_formal = Math.sqrt(R_tp.transpose().times(S_tp.solve(R_tp)).get(0, 0));
            
            // Sample covariance-weighted residual
            double sigma_sample = Math.sqrt(R_tp.transpose().times(Sc_tp.solve(R_tp)).get(0, 0));

            // Entry in outliers column
            String ol = "";
            
            // Detect outliers
            if(sigma_sample > sig_clip)
            {
                outliers.add(inliers.get(i));
                ol = "*";
            }
            
            // Print results
            System.out.printf("%s \t %7.7g \t %5.5g \t %5.5g \t %5.5g \t %5.5g \t %s\n",
                              slaves.get(i).epoch.toString(),
                              slaves.get(i).epoch.mjd,
                              resid_PIX,
                              resid_TP,
                              sigma_formal,
                              sigma_sample,
                              ol);
            
        }
        
        // If outliers were found, we remove them then call the fitting
        // function recursively.
        
        // Found outliers; remove them then refit
        if(outliers.size()>0)
        {
            System.out.println("\nFound "+outliers.size()+" outlier(s), recomputing...\n");
            
            // Remove outliers
            inliers.removeAll(outliers);
            
            // Call method recursively to implement outlier rejection
            astrometricFit5Params(inliers, p, cov, sig_clip);
            
        }
        // No outliers found; return results of fit.
        else
        {
            // Load parameters & covariance into output arrays
            p[0] = x.get(0,0);
            p[1] = x.get(1,0);
            p[2] = x.get(2,0);
            p[3] = x.get(3,0);
            p[4] = x.get(4,0);

            // Copy array data from covariance matrix
            for(int i=0; i<5; i++)
                System.arraycopy(S_x.getArray()[i], 0, cov[i], 0, 5);
            
            
            // Parallax in arcseconds
            double pi = Math.toDegrees(p[0])*60*60;
            // Parallax uncertainty in arcseconds
            double s_pi = Math.toDegrees(Math.sqrt(cov[0][0]))*60*60;

            // Zeropoint, xsi [arcsec]
            double xsi_0 = Math.toDegrees(p[1])*60*60;
            // Uncertainty xsi [arcsec]
            double s_xsi_0 = Math.toDegrees(Math.sqrt(cov[1][1]))*60*60;

            // Proper motion, xsi (parallel to equator) [arcsec/year]
            double mu_xsi = Math.toDegrees(p[2])*60*60;
            // Uncertainty
            double s_mu_xsi = Math.toDegrees(Math.sqrt(cov[2][2]))*60*60;

            // Zeropoint, eta [arcsec]
            double eta_0 = Math.toDegrees(p[3])*60*60;
            // Uncertainty eta [arcsec]
            double s_eta_0 = Math.toDegrees(Math.sqrt(cov[3][3]))*60*60;

            // Proper motion, eta (perpendicular to equator) [arcsec/year]
            double mu_eta = Math.toDegrees(p[4])*60*60;
            // Uncertainty
            double s_mu_eta = Math.toDegrees(Math.sqrt(cov[4][4]))*60*60;

            System.out.println("\n\nParallax [arcsec]                = "+pi+" +/- "+s_pi);
            System.out.println("Proper motion, xsi [arcsec/year] = "+mu_xsi+" +/- "+s_mu_xsi);
            System.out.println("Proper motion, eta [arcsec/year] = "+mu_eta+" +/- "+s_mu_eta);
            System.out.println("Xsi zeropoint [arcsec]           = "+xsi_0+" +/- "+s_xsi_0);
            System.out.println("Eta zeropoint [arcsec]           = "+eta_0+" +/- "+s_eta_0);

            System.out.flush();
            
        }
        
    }
    
    
    
    
    /**
     * Astrometric fit for parallax, proper motion and zeropoints.
     * 
     * Only one of the parallax factors is included in the model, in order to
     * test that we get the same results.
     * 
     * @param inliers   List of inlying data points to be used in fit.
     * @param p         On exit, contains the fitted astrometric parameters.
     * @param cov       On exit, contains the parameter covariance matrix.
     * @param sig_clip  Threshold on residuals for outlier rejection.
     * @param type      Indicates which coordinate to fit.
     */
    public void astrometricFit3ParamsOneCoordinate(List<AstrometricDatum> inliers,
                               double[] p, double[][] cov, double sig_clip,
                               STANDARD_COORDINATE type)
    {
        
        // Number of points
        int N = inliers.size();
        
        // Check we have enough points to calculate the astrometric fit
        if(N<3)
            throw new RuntimeException("Too few epochs to determine astrometric fit!");
        
        // Create matrices to store components of astrometric fit.
        //
        // Cx = D
        
        // Design matrix [contains astrometric model]
        Matrix C = new Matrix(N,3);
        // Data matrix [contains observed tangent plane coordinates]
        Matrix D = new Matrix(N,1);
        // Covariance matrix [contains covariance on observed tan. plane coordinates]
        Matrix S = new Matrix(N,N);
        
        // The model we solve is:
        //
        // F_xsi*pi + xsi_0 + t*mu_xsi = xsi
        //
        // or
        //
        // F_eta*pi + eta_0 + t*mu_eta = eta
        //
        // ...depending on which parallax factor we want to use.
        //
        // Rearrange into the form:
        //
        // |F_xsi_1  1  t_1 |   |pi    |   |xsi_1|
        // |          .     | * |xsi_0 | = |  .  |
        // |          .     |   |mu_xsi|   |  .  |
        // |F_xsi_N  1  t_N |              |xsi_N|
        //
        // or
        //
        // |F_eta_1  1  t_1 |   |pi    |   |eta_1|
        // |          .     | * |eta_0 | = |  .  |
        // |          .     |   |mu_eta|   |  .  |
        // |F_eta_N  1  t_N |              |eta_N|
        //
        // Or: Cx = D
        //
        // where x contains the astrometric parameters:
        // 
        // x = (pi, xsi_0, mu_xsi)^T
        //
        // or
        //
        // x = (pi, eta_0, mu_eta)^T
        //
        for(int i=0; i<inliers.size(); i++)
        {
            
            AstrometricDatum entry = inliers.get(i);
            
            switch(type)
            {
                case XSI:
                {
                    C.set(i, 0, entry.F_xsi);
                    C.set(i, 1, 1.0);
                    C.set(i, 2, entry.t);
                    D.set(i, 0, entry.tp.x);
                    S.set(i, i, entry.tp.cxx);
                    break;
                }
                case ETA:
                {
                    C.set(i, 0, entry.F_eta);
                    C.set(i, 1, 1.0);
                    C.set(i, 2, entry.t);
                    D.set(i, 0, entry.tp.y);
                    S.set(i, i, entry.tp.cyy);
                    break;
                }
            }
        }
        
        // Weighted least squares:
        Matrix M = C.transpose().times(S.solve(C));
        // Solve for astrometric parameters
        Matrix x = M.solve(C.transpose().times(S.solve(D)));
        // Parameter covariance
        Matrix S_x = M.inverse();
        // Model
        Matrix model = C.times(x);
        
        // Calculate sample covariance (just one term; error between predicted
        // and observed coordinate)
        double Sc_tp = 0.0;
        for(int i=0; i<N; i++)
        {
            // Residual
            double r = model.get(i, 0)-D.get(i, 0);
            Sc_tp   += r*r;
            
        }
        // Normalise sample covariance
        Sc_tp *= (1.0/(N-1.0));
        
        // Now print residuals for each point and detect outliers
        System.out.println("\n\n#--- Astrometric fit residuals ---#");
        System.out.println("#");
        System.out.println("# Columns are:");
        System.out.println("# 1) Epoch [UTC]");
        System.out.println("# 2) Epoch [MJD]");
        System.out.println("# 3) Residuals, unweighted [standard coordinates]");
        System.out.println("# 4) Residuals, weighted by formal covariance on data");
        System.out.println("# 5) Residuals, weighted by sample covariance on data");
        System.out.println("# 6) Asterisk indicates an outlier");
        
        // List of detected outliers
        List<AstrometricDatum> outliers = new LinkedList<AstrometricDatum>();
        
        for(int i=0; i<N; i++)
        {
            double tp_model = model.get(i, 0);                // Model standard coordinates
            double tp_obs = D.get(i, 0);                      // Observed standard coordinates
            double R_tp = tp_model - tp_obs;                  // Residual
            double resid_TP  = R_tp * R_tp;                   // Residuals (non covariance-weighted)
            double S_tp = S.get(i, i);                        // Variance on observed standard coordinates
            double sigma_formal = Math.sqrt(R_tp*R_tp/S_tp);  // Formal covariance-weighted residual
            double sigma_sample = Math.sqrt(R_tp*R_tp/Sc_tp); // Sample covariance-weighted residual

            // Entry in outliers column
            String ol = "";
            
            // Detect outliers
            if(sigma_sample > sig_clip)
            {
                outliers.add(inliers.get(i));
                ol = "*";
            }
            
            // Print results
            System.out.printf("%s \t %7.7g \t %5.5g \t %5.5g \t %5.5g \t %s\n",
                              slaves.get(i).epoch.toString(),
                              slaves.get(i).epoch.mjd,
                              resid_TP, 
                              sigma_formal, 
                              sigma_sample,
                              ol);
            
        }
        
        // If outliers were found, we remove them then call the fitting
        // function recursively.
        
        // Found outliers; remove them then refit
        if(outliers.size()>0)
        {
            System.out.println("\nFound "+outliers.size()+" outlier(s), recomputing...\n");
            
            // Remove outliers
            inliers.removeAll(outliers);
            
            // Call method recursively to implement outlier rejection
            astrometricFit3ParamsOneCoordinate(inliers, p, cov, sig_clip, type);
            
        }
        // No outliers found; return results of fit.
        else
        {
            // Load parameters & covariance into output arrays
            p[0] = x.get(0,0);
            p[1] = x.get(1,0);
            p[2] = x.get(2,0);

            // Copy array data from covariance matrix
            for(int i=0; i<3; i++)
                System.arraycopy(S_x.getArray()[i], 0, cov[i], 0, 3);
            
            // Parallax in arcseconds
            double pi = Math.toDegrees(p[0])*60*60;
            // Parallax uncertainty in arcseconds
            double s_pi = Math.toDegrees(Math.sqrt(cov[0][0]))*60*60;

            // Zeropoint, standard coordinate [arcsec]
            double tp_0 = Math.toDegrees(p[1])*60*60;
            // Uncertainty, standard coordinate [arcsec]
            double s_tp_0 = Math.toDegrees(Math.sqrt(cov[1][1]))*60*60;

            // Proper motion, standard coordinate [arcsec/year]
            double mu_tp = Math.toDegrees(p[2])*60*60;
            // Uncertainty, standard coordinate
            double s_mu_tp = Math.toDegrees(Math.sqrt(cov[2][2]))*60*60;


            switch(type)
            {
                case XSI:
                {
                    System.out.println("\n\nParallax [arcsec]                = "+pi+" +/- "+s_pi);
                    System.out.println("Proper motion, xsi [arcsec/year] = "+mu_tp+" +/- "+s_mu_tp);
                    System.out.println("Xsi zeropoint [arcsec]           = "+tp_0+" +/- "+s_tp_0);
                    break;
                }
                case ETA:
                {
                    System.out.println("\n\nParallax [arcsec]                = "+pi+" +/- "+s_pi);
                    System.out.println("Proper motion, eta [arcsec/year] = "+mu_tp+" +/- "+s_mu_tp);
                    System.out.println("Eta zeropoint [arcsec]           = "+tp_0+" +/- "+s_tp_0);
                    break;
                }
            }
            
            System.out.flush();
            
        }
        
    }  
    
    
    
}
