package projects.sssj1556.data;

import java.io.File;
import java.util.LinkedList;

import projects.sssj1556.astrometry.StackedImage;
import projects.sssj1556.astrometry.Star;

/**
 * #Target63, CTIO
 * 0545  2 33 38.9412 +21 25 15.346 1990.785    0.222  163.189 16973 17056 18396  16773  313762  202934  171965  134065  0.7743 0.0697       0       0    -481 0.0387       0       0     728 0.0279       0       0    -280 0.0798       0       0    -582  0.0655  0.0048 -0.2123  0.0051 1951.919 1992.755 1995.650 0 0 0 0 1 1 0 1 1 0 1 1   0.206244E+15   0.999900E+09 1   0.210511E+00   0.125043E+01 3   0.210511E+00   0.125043E+01 3   0.210511E+00   0.125043E+01 3
 *
 * @author nickrowell
 */
public class SSSJ0233p2125
extends ParallaxDataset
{
    
    public SSSJ0233p2125()
    {
        designation = "SSSJ0233p2125";
        path        = new File("/home/nickrowell/Astronomy/WD_Parallax_Study_2014/data/LHS_3250");
        
        slaves   = new LinkedList<StackedImage>();
        allstars = new LinkedList<Star>();
        
//        ra             = ParallaxDataset.hmsToRadians( 2, 33, 38.9412);
//        dec            = ParallaxDataset.dmsToRadians(21, 25, 15.346);
//        nom_epoch      = new Epoch(1990.785);
//        mu_acosd       =  0.0655;     
//        sigma_mu_acosd =  0.0048;
//        mu_d           = -0.2123;
//        sigma_mu_d     =  0.0051;
        
//        epochs = new Epoch[2];
//        epochs[0] = new Epoch(2008, 8, 24);
//        epochs[1] = new Epoch(2008, 8, 28);
            
    }
}
