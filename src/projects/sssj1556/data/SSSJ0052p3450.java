package projects.sssj1556.data;

import java.io.File;
import java.util.LinkedList;

import projects.sssj1556.astrometry.StackedImage;
import projects.sssj1556.astrometry.Star;

/**
 * #Target37, CTIO
 * 0351  0 52 46.6172 +34 50 55.709 1989.750    0.426  111.305 18297 18224 19921  17770  552581  202549  132984  135829  0.1742 0.1302       0       0     309 0.1100       0       0    -655 0.0648       0       0   -1785 0.2751       0       0     136  0.3981  0.0058 -0.1529  0.0055 1951.842 1993.711 1994.664 0 0 0 0 1 1 0 1 1 0 1 1   0.206244E+15   0.999900E+09 1   0.428759E+00   0.302847E+00 3   0.428759E+00   0.302847E+00 3   0.428759E+00   0.302847E+00 3
 *
 * @author nickrowell
 */
public class SSSJ0052p3450
extends ParallaxDataset
{
    
    public SSSJ0052p3450()
    {
        designation = "SSSJ0052p3450";
        path        = new File("/home/nickrowell/Astronomy/WD_Parallax_Study_2014/data/LHS_3250");
        
        slaves   = new LinkedList<StackedImage>();
        allstars = new LinkedList<Star>();
        
//        ra             = ParallaxDataset.hmsToRadians( 0, 52, 46.6172);
//        dec            = ParallaxDataset.dmsToRadians(34, 50, 55.709);
//        nom_epoch      = new Epoch(1989.750);
//        mu_acosd       =  0.3981;
//        sigma_mu_acosd =  0.0058;
//        mu_d           = -0.1529;
//        sigma_mu_d     =  0.0055;
        
//        epochs = new Epoch[3];
//        epochs[0] = new Epoch(2008, 7, 31);
//        epochs[1] = new Epoch(2008, 8,  1);
//        epochs[2] = new Epoch(2008, 8,  3);

    }
}
