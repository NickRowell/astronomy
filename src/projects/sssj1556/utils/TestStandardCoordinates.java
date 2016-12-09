package projects.sssj1556.utils;

import projects.sssj1556.astrometry.Coordinates2D;
import projects.sssj1556.astrometry.StackedImage;
import projects.sssj1556.data.LHS3250;
import projects.sssj1556.data.ParallaxDataset;


/**
 *
 * @author nickrowell
 */
public class TestStandardCoordinates
{
    
    public static void main(String[] args)
    {
        
        // Get a test Star object
        ParallaxDataset lhs3250 = new LHS3250();
        
        // Register slave images
        for(StackedImage slave : lhs3250.slaves)
        {
            slave.computeSlaveToMasterImageTransform(lhs3250.master);
        }
        
        // Compute conversion from image to standard coordinates, in master frame
        lhs3250.master.computeImageToTangentPlaneTransform(lhs3250.target);
        
        // Pbserved coordinates of target star in master frame
        double x = lhs3250.master.getDetection(lhs3250.target).x;
        double y = lhs3250.master.getDetection(lhs3250.target).y;
        System.out.println("Direct target star coordinates = "+x+", "+y);
        
        // Get standard coordinates of target star in master frame (should be zero)
        Coordinates2D im = lhs3250.master.imageCoordinatesToStandardCoordinates(lhs3250.master.getDetection(lhs3250.target));
        
        System.out.println("...after conversion to master frame = "+im.x+", "+im.y);
        
        // Standard coordinates of target, transformed from master frame
        Coordinates2D tan = lhs3250.master.imageCoordinatesToStandardCoordinates(im);
        
        System.out.println("standard coordinates = "+tan.x+", "+tan.y);
        
        
    }
    
}
