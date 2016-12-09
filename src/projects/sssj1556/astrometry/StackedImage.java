package projects.sssj1556.astrometry;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import time.Epoch;

/**
 *
 * @author nickrowell
 */
public class StackedImage
{
    
    /** Epoch of observation. */
    public Epoch epoch;
    
    /** Path from parent to directory containing this image. */
    public File child;
    
    /** Image size [pixels]. */
    public int width, height;
    
    /**
     * List of Detections of reference stars. StackedImages don't know which the
     * target is.
     */
    public List<Detection> detections;
    
    /**
     * Transformation that maps pixel (i,j) in this image to pixel (i',j') in 
     * master image is:
     * 
     * i' = a[0]*i + a[1]*j + a[2]
     * j' = a[3]*i + a[4]*j + a[5]
     * 
     * Note that origin for reference star coordinates is at the bottom left
     * of the image.
     * 
     */
    public double[] s2m = new double[6];
    
    /** Coefficients of inverse transform (master -> slave). */
    public double[] m2s = new double[6];
    
    /** Sample covariance of transformed reference points. */
    public double[] c_s2m = new double[3];
    
    /**
     * Coefficients of the linear transformation from image coordinates to
     * standard coordinates in the tangent plane.
     * The transformation has the form:
     * 
     * xsi = p[0]*i + p[1]*j + p[2]
     * eta = p[3]*i + p[4]*j + p[5]
     * 
     */
    public double[] im2tp = new double[6];
    
    /** Coefficients of inverse transform (standard coordinates -> master). */
    public double[] tp2im = new double[6];
    
    /** Sample covariance of transformed reference points. */
    public double[] c_im2tp = new double[3];
    
    
    /**
     * Coefficients of transformation with the image origin at the top left.
     * This is important when we want to use some other image processing 
     * tool such as ImageMagick to actually perform the affine transformation to
     * the slave image to align it with the master. Note that this is not 
     * required for further astrometric analysis because all we need are the
     * transformation coefficients, but it is useful for visualisation purposes
     * to have a set of co-aligned images so that the target star motion is
     * clear.
     */
    public double a_im, b_im, c_im, d_im, e_im, f_im;
    
    public StackedImage(Epoch pepoch, File pchild, int pwidth, int pheight)
    {
        epoch    = pepoch;
        child    = pchild;
        width    = pwidth;
        height   = pheight;
        detections = new LinkedList<Detection>();
        
        // Initialise all transforms to identity
        s2m[0]=1.0; s2m[1]=0.0; s2m[2]=0.0; s2m[3]=0.0; s2m[4]=1.0; s2m[5]=0.0;
        m2s[0]=1.0; m2s[1]=0.0; m2s[2]=0.0; m2s[3]=0.0; m2s[4]=1.0; m2s[5]=0.0;
        
        // 
        a_im = s2m[0];
        b_im = -s2m[1];
        c_im = s2m[2] + s2m[1]*height;
        d_im = -s2m[3];
        e_im = s2m[4];
        f_im = -s2m[5] - s2m[4]*height + height;
        
    }
    
    /**
     * Add this Detection to the parallax data structure.
     * 
     * @param detection 
     */
    public void add(Detection detection)
    {
        detection.image = this;
        detection.star.images.add(this);
        detections.add(detection);
    }
    
    
    /**
     * Image magick requires the backwards transformation. In addition, the
     * origin is at the top left whereas the reference star coordinates have the
     * origin at the bottom left so we need to flip the y coordinate.
     * @param cen_x
     * @param cen_y
     * @return
     */
    public String imageMagickTransformCommand(int cen_x, int cen_y)
    {
        
        // cen_x, cen_y is the coordinate of the target star in the master frame
        // (flipped to image magick frame, which has the origin at top left),
        // which is where we want to centre our warped image
        
        // Diameter of shifted & cropped warped image - can change this value.
        int diameter = 750;
        
        String in = (new File(child,"stacked.fits")).getPath();
        
        String out = epoch.year+"."+epoch.month+"."+epoch.day+"."+epoch.hour+"_"
                     +epoch.minute+"_"+epoch.second+".png";
        
        
        return "convert "+in+" -matte -virtual-pixel Transparent "+
               " -affine "+a_im+","+d_im+","+b_im+","+e_im+","+c_im+","+f_im+
               " -transform "+" -crop "+diameter+"x"+diameter+"+"+(cen_x-diameter/2)+"+"+(cen_y-diameter/2)
                + " -negate -contrast-stretch 0x0 -auto-level -auto-gamma "+" "+out;

    }
    
    
    
    /**
     * Compute the linear transformation that maps the coordinates of stars in 
     * this image to the given master image. Also estimates the error on 
     * transformed positions by calculating the residuals of the observed
     * transformed coordinates wrt the model. The sample covariance matrix is
     * used to estimate the error.
     * 
     * Note that the freedom to choose arbitrarily which frame is the master
     * means that we cannot guarantee a reference star observed in the slave
     * image will also be observed in the master.
     * 
     * @param master_image 
     */
    public void computeSlaveToMasterImageTransform(StackedImage master_image)
    {
        
        // Check for registering image with itself - if so, leave transformation
        // at identity
        if(this==master_image)
        {
            return;
        }
        
        // Load Coordinates2D objects into two lists containing coordinates in
        // the slave and master frames. There must be a one-to-one correspondence
        // between entries in each list.
        
        List<Correspondence> points = new LinkedList<Correspondence>();
        
        // Loop over all detections in the master frame.
        for(Detection master_star : master_image.detections)
        {
            // Has the star also been observed on this slave image?
            if(detections.contains(master_star))
            {
                // Load detections into the lists
                Coordinates2D A = detections.get(detections.indexOf(master_star));
                Coordinates2D B = master_star;
                points.add(new Correspondence(A,B));                
            }
        }
        
        // Compute the linear transformation that transforms coordinates from the
        // slave image to the master image.
        System.out.println("\n\n"+child.getPath()+": computing slave->master transform");
        System.out.println("Residuals           [pixels]  [pixels]");
        Coordinates2D.linTrans(points, s2m, m2s, c_s2m, 2.5, true);
        
        // Flip y axis and shift origin
        a_im = s2m[0];
        b_im = -s2m[1];
        c_im = s2m[2] + s2m[1]*height;
        d_im = -s2m[3];
        e_im = s2m[4];
        f_im = -s2m[5] - s2m[4]*height + master_image.height;
        
    }
     
    /**
     * This method computes the linear transformation from image coordinates
     * to tangent plane coordinates. It assumes the target star lies at the
     * tangent point. It uses the celestial coordinates for the reference stars
     * in this image. Thus the transformation is self-contained and does not
     * need a master image or anything.
     * 
     * Note that in the final reductions, only the tangent plane transformation
     * for one image (the master) is actually used.
     * 
     * The transformation has this form:
     * 
     * xsi = p*x + q*y + r
     * eta = s*x + t*y + u
     * 
     * 
     * @param tangent_point    Star to use as tangent point.
     */
    public void computeImageToTangentPlaneTransform(Star tangent_point)
    {
        
        // Load Coordinates2D objects into two lists containing coordinates in
        // the master frame and corresponding standard coordinates. There must 
        // be a one-to-one correspondence between entries in each list.
        List<Correspondence> points = new LinkedList<Correspondence>();
        
        // Loop over all detections
        for(Detection detection : detections)
        {
            
            Coordinates2D A = detection;
            Coordinates2D B = detection.star.getTangentPlaneCoordinates(tangent_point, epoch);
            
            points.add(new Correspondence(A,B));
        }
        
        // Compute the linear transformation that transforms coordinates from the
        // slave image to the master image.
        System.out.println(child.getPath()+": computing master->tangent plane transform");
        System.out.println("Residuals           [pixels]  [standard coordinates]");
        Coordinates2D.linTrans(points, im2tp, tp2im, c_im2tp, 2.5, true);
        
    }
    
    /**
     * Returns the Detection associated with the given Star in this StackedImage.
     * Returns null if the Star hasn't been detected in this image.
     * @param star  Star that we want to look up the detection for.
     * @return      The Detection associated with the Star in this StackedImage.
     */
    public Detection getDetection(Star star)
    {
        for(Detection detection : detections)
        {
            if(detection.star==star)
            {
                return detection;
            }
        }
        // Star not detected in this image.
        return null;
    }
    
    
    
    /**
     * Use current slave-to-master transformation to transform the Detection to 
     * the master frame.
     * @param slave
     * @return 
     */
    public Coordinates2D imageCoordinatesInMasterFrame(Coordinates2D slave)
    {
        // Initial transformation, that estimates covariance by propagating
        // target covariance through transform equation.
        Coordinates2D master = slave.transform(s2m);
        
        // Now add sample covariance on slave->master transform to get a robust
        // estimate of the true position error.
        master.cxx += c_s2m[0];
        master.cxy += c_s2m[1];
        master.cyy += c_s2m[2];
        
        return master;
    }
    
    /**
     * Transform the input from image coordinates to standard coordinates,
     * 
     * @param image
     * @return 
     */
    public Coordinates2D imageCoordinatesToStandardCoordinates(Coordinates2D image)
    {
        // Initial transformation, that estimates covariance by propagating
        // target covariance through transform equation.
        Coordinates2D standard = image.transform(im2tp);
        
        // Now add sample covariance on image->tangent plane transform to get a
        // robust estimate of the true position error.
        standard.cxx += c_im2tp[0];
        standard.cxy += c_im2tp[1];
        standard.cyy += c_im2tp[2];
        
        return standard;
    }
    
    
    /**
     * This method return the transformation matrix T that transforms the
     * reference star coordinates so that their centre of mass is at the origin
     * and their average distance from the origin is sqrt(2).
     * 
     * The transformation matrix is 3x3, and is used to calculate the shifted
     * and scaled coordinates (x',y') from the originals (x,y) by:
     * 
     *  |x'|       |x|
     *  |y'| = T * |y|
     *  |1 |       |1|
     * 
     * This was used initially to improve numerical stability of solution for
     * slave->master transformation, but I found the effect was actually 
     * negligible: the solution for the transformation coefficients is not
     * numerically unstable in the way that other machine vision calculations
     * are. I guess this is because this is an inhomogenous equation set that
     * we solve using standard linear algebra, whereas unstable equations in
     * machine vision tend to be homogenous and require singular value
     * decomposition and null-space finding in order to get best solution.
     * 
     * @return 
     */
//    public Matrix getNormT()
//    {
//        
//        // Number of reference stars
//        int N = refStars.size();
//        
//        // Intermediate
//        double[][] x   = new double[N][2];
//
//        // Get mean position of points in each frame
//        double x_COM=0, y_COM=0;
//        for(int i=0; i<N; i++)
//        {
//            x_COM   += refStars.get(i).i;
//            y_COM   += refStars.get(i).j;
//        }
//        
//        x_COM   /= (double)N;
//        y_COM   /= (double)N;
//
//        // Get centroid-relative coordinates for each point
//        for(int i=0; i<N; i++)
//        {
//            x[i][0] = refStars.get(i).i - x_COM;
//            x[i][1] = refStars.get(i).j- y_COM;
//        }
//
//        // Get mean distance from origin:
//        //
//        // 1) Get sum distances from origin
//        double D=0;
//        for(int i=0; i<N; i++)
//        {
//            D   += Math.sqrt(x[i][0] * x[i][0] + x[i][1] * x[i][1]);
//        }
//        // 2) Take average
//        D   /= (double)N;
//
//        // Scale factor
//        double s   = Math.sqrt(2.0)/D;
//
//        // Calculate transformation matrix
//        Matrix T = new Matrix(new double[][]{{s, 0, -s*x_COM},
//                                             {0, s, -s*y_COM},
//                                             {0, 0,        1}});
//
//        return T;
//        
//    }
    
    
    
}
