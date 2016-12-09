package projects.sssj1556.data;

import java.io.File;
import java.util.LinkedList;

import projects.sssj1556.astrometry.StackedImage;
import projects.sssj1556.astrometry.Star;

/**
 * #Target73, CTIO
 * 0405 22 17 47.1291 +37  7 49.635 1986.753    0.469   78.928 16658 16629 18142  16149  709119  673214  491976  517810  0.5492 0.0677       0       0      54 0.0400       0       0   -1587 0.1005       0       0     401 0.0741       0       0     295  0.4620  0.0041  0.0798  0.0038 1953.613 1989.686 1995.708 0 0 0 0 1 0 0 1 1 0 1 0   0.206244E+15   0.999900E+09 1   0.463099E+00   0.662944E+00 3   0.463099E+00   0.662944E+00 3   0.463099E+00   0.662944E+00 3
 *
 * @author nickrowell
 */
public class SSSJ2217p3707
extends ParallaxDataset
{
    
    public SSSJ2217p3707()
    {
        designation = "SSSJ2217p3707";
        path        = new File("/home/nickrowell/Astronomy/WD_Parallax_Study_2014/data/LHS_3250");
        
        slaves   = new LinkedList<StackedImage>();
        allstars = new LinkedList<Star>();
        
//        ra             = ParallaxDataset.hmsToRadians(22, 17, 47.1291);
//        dec            = ParallaxDataset.dmsToRadians(37,  7, 49.635);
//        nom_epoch      = new Epoch(1986.753);
//        mu_acosd       = 0.4620;
//        sigma_mu_acosd = 0.0041;
//        mu_d           = 0.0798;
//        sigma_mu_d     = 0.0038;
        
//        epochs = new Epoch[3];
//        epochs[0] = new Epoch(2008, 6, 23);
//        epochs[1] = new Epoch(2008, 7, 23);
//        epochs[2] = new Epoch(2008, 7, 26);
            
    }
}
