package projects.sssj1556.data;

import java.io.File;
import java.util.LinkedList;

import projects.sssj1556.astrometry.StackedImage;
import projects.sssj1556.astrometry.Star;

/**
 * #Target75, CTIO observing
 *  0473  0 21 47.3257 +26 40 36.347 1998.863    0.316  194.867 17101 17029 18376  16473  515103  242363  240094  211390  0.4137 0.0907       0       0    1048 0.0455       0       0   -1956 0.1048       0       0      38 0.0728       0       0    -231 -0.0828  0.0053 -0.3054  0.0054 1954.667 1993.708 1993.856 0 0 0 0 1 1 0 1 1 0 1 1   0.206244E+15   0.999900E+09 1   0.329980E+00   0.629138E+00 3   0.329980E+00   0.629138E+00 3   0.329980E+00   0.629138E+00 3
 *
 * @author nickrowell
 */
public class SSSJ0021p2640
extends ParallaxDataset
{
    
    public SSSJ0021p2640()
    {
        designation = "SSSJ0021p2640";
        path        = new File("/home/nickrowell/Astronomy/WD_Parallax_Study_2014/data/LHS_3250");
        
        slaves   = new LinkedList<StackedImage>();
        allstars = new LinkedList<Star>();
        
//        ra             = ParallaxDataset.hmsToRadians( 0, 21, 47.3257);
//        dec            = ParallaxDataset.dmsToRadians(26, 40, 36.347);
//        nom_epoch      = new Epoch(1998.863);
//        mu_acosd       = -0.0828;
//        sigma_mu_acosd =  0.0053;
//        mu_d           = -0.3054;
//        sigma_mu_d     =  0.0054;
        
//        epochs = new Epoch[2];
//        epochs[0] = new Epoch(2008, 8, 3);
//        epochs[1] = new Epoch(2008, 8, 4);
            
    }
}
