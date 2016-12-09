package projects.sssj1556.data;

import java.io.File;
import java.util.LinkedList;

import projects.sssj1556.astrometry.StackedImage;
import projects.sssj1556.astrometry.Star;

/**
 * #Target77 675N/674N overlap, CTIO
 * 0675 22 30 55.3494 +15 23 51.948 1991.706    0.531   67.713 17287 17309 18379  17022  275254  198744  179410  118174  0.2381 0.1180       0       0     -38 0.0830       0       0     680 0.0451       0       0    -500 0.2158       0       0     534  0.4942  0.0056  0.1947  0.0062 1953.833 1988.538 1992.719 0 0 0 0 1 1 0 1 1 0 1 1   0.206244E+15   0.999900E+09 1   0.554206E+00   0.129746E+00 3   0.554206E+00   0.129746E+00 3   0.554206E+00   0.129746E+00 3
 *
 * @author nickrowell
 */
public class SSSJ2230p1523
extends ParallaxDataset
{
    
    public SSSJ2230p1523()
    {
        designation = "SSSJ2230p1523";
        path        = new File("/home/nickrowell/Astronomy/WD_Parallax_Study_2014/data/LHS_3250");
        
        slaves   = new LinkedList<StackedImage>();
        allstars = new LinkedList<Star>();
        
//        ra             = ParallaxDataset.hmsToRadians(22, 30, 55.3494);
//        dec            = ParallaxDataset.dmsToRadians(15, 23, 51.948);
//        nom_epoch      = new Epoch(1991.706);
//        mu_acosd       = 0.4942;
//        sigma_mu_acosd = 0.0056;
//        mu_d           = 0.1947;
//        sigma_mu_d     = 0.0062;
        
//        epochs = new Epoch[7];
//        epochs[0] = new Epoch(2007, 11, 16);
//        epochs[1] = new Epoch(2008,  7,  4);
//        epochs[2] = new Epoch(2008,  7, 23);
//        epochs[3] = new Epoch(2008, 10, 17);
//        epochs[4] = new Epoch(2008, 10, 20);
//        epochs[5] = new Epoch(2009,  6, 12);
//        epochs[6] = new Epoch(2009,  6, 27);
            
    }
}
