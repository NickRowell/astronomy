package projects.sssj1556.data;

import java.io.File;
import java.util.LinkedList;

import projects.sssj1556.astrometry.StackedImage;
import projects.sssj1556.astrometry.Star;

/**
 * 0727 15 56 47.3181 -08  5 59.713 1992.480    0.421  117.913 17866 17840 18800  18058  205182  415718  486115  305360  1.7931 0.0731       0       0     681 0.0846       0       0     384 0.1073       0       0     871 0.1281       0       0     347  0.3724  0.0055 -0.1955  0.0055 1954.000 1982.608 1992.317 0 0 0 0 0 1 0 0 0 0 0 1   0.206244E+15   0.999900E+09 1   0.445086E+00   0.308862E+00 3   0.445086E+00   0.308862E+00 3   0.445086E+00   0.308862E+00 3
 * @author nickrowell
 */
public class SSSJ1556m0805
extends ParallaxDataset
{
    
    public SSSJ1556m0805()
    {
        designation = "SSSJ1556m0805";
        path        = new File("/home/nickrowell/Astronomy/WD_Parallax_Study_2014/data/LHS_3250");
        
        slaves   = new LinkedList<StackedImage>();
        allstars = new LinkedList<Star>();
        
        
//        ra             = ParallaxDataset.hmsToRadians( 15, 56, 47.3181);
//        dec            = ParallaxDataset.dmsToRadians(-8,  5, 59.713);
//        nom_epoch      = new Epoch(1992.480);
//        mu_acosd       =  0.3724;
//        sigma_mu_acosd =  0.0055;
//        mu_d           = -0.1955;
//        sigma_mu_d     =  0.0055;
        
//        epochs = new Epoch[12];
//        epochs[0]  = new Epoch(2007, 2,  6);
//        epochs[1]  = new Epoch(2007, 2,  7);
//        epochs[2]  = new Epoch(2007, 8,  1);
//        epochs[3]  = new Epoch(2008, 8,  7);
//        epochs[4]  = new Epoch(2009, 2, 22);
//        epochs[5]  = new Epoch(2009, 3, 13);
//        epochs[6]  = new Epoch(2009, 3, 22);
//        epochs[7]  = new Epoch(2009, 6,  2);
//        epochs[8]  = new Epoch(2009, 7, 14);
//        epochs[9]  = new Epoch(2009, 7, 17);
//        epochs[10] = new Epoch(2009, 7, 22);
//        epochs[11] = new Epoch(2009, 7, 23);
            
    }
}
