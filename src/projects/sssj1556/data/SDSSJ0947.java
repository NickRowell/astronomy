package projects.sssj1556.data;

import java.io.File;
import java.util.LinkedList;

import projects.sssj1556.astrometry.StackedImage;
import projects.sssj1556.astrometry.Star;

/**
 * 0262  9 47 24.4362 +45  0  1.664 1995.242    0.081   52.872 18477 18534 19697  17849  163495  145623  186895  101058  0.0981 0.0950       0       0   -1039 0.0807       0       0   -2319 0.1120       0       0    -790 0.1645       0       0    1667  0.0653  0.0060  0.0482  0.0070 1953.125 1991.952 1989.894 0 0-1 0-1-1 0-1-1-1-1-1   0.811412E-01   0.981965E-01 4   0.811412E-01   0.981965E-01 4   0.811412E-01   0.981965E-01 4   0.811412E-01   0.981965E-01 4
 * @author nickrowell
 */
public class SDSSJ0947
extends ParallaxDataset
{
    
    public SDSSJ0947()
    {
        designation = "SDSSJ0947";
        path        = new File("/home/nickrowell/Astronomy/WD_Parallax_Study_2014/data/LHS_3250");
        
        slaves   = new LinkedList<StackedImage>();
        allstars = new LinkedList<Star>();
        
        
//        ra             = ParallaxDataset.hmsToRadians(9, 47, 24.4362);
//        dec            = ParallaxDataset.dmsToRadians(45, 0,  1.664);
//        nom_epoch      = new Epoch(1995.242);
//        mu_acosd       = 0.0653;
//        sigma_mu_acosd = 0.0060;
//        mu_d           = 0.0482;
//        sigma_mu_d     = 0.0070;
        
//        epochs = new Epoch[3];
//        epochs[0]  = new Epoch(2007, 2,  7);
//        epochs[1]  = new Epoch(2007, 4, 22);
//        epochs[2]  = new Epoch(2007, 4, 27);

    }
}
