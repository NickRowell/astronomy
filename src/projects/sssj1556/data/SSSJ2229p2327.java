package projects.sssj1556.data;

import java.io.File;
import java.util.LinkedList;

import projects.sssj1556.astrometry.StackedImage;
import projects.sssj1556.astrometry.Star;

/**
 * #Target67, CTIO
 * 0533 22 29 33.7621 +23 27 45.191 1987.747    0.306  133.485 17743 17858 18797  17428  177464  119726   96818   67351  0.4321 0.0736       0       0    -303 0.0434       0       0   -1512 0.0681       0       0     404 0.1175       0       0   -1444  0.2240  0.0051 -0.2085  0.0050 1953.708 1987.790 1995.554 0 0 0 0 1 1 0 1 1 0 1 1   0.206244E+15   0.999900E+09 1   0.321828E+00   0.361675E+00 3   0.321828E+00   0.361675E+00 3   0.321828E+00   0.361675E+00 3
 *
 * @author nickrowell
 */
public class SSSJ2229p2327
extends ParallaxDataset
{
    
    public SSSJ2229p2327()
    {
        designation = "SSSJ2229p2327";
        path        = new File("/home/nickrowell/Astronomy/WD_Parallax_Study_2014/data/LHS_3250");
        
        slaves   = new LinkedList<StackedImage>();
        allstars = new LinkedList<Star>();
        
//        ra             = ParallaxDataset.hmsToRadians(22, 29, 33.7621);
//        dec            = ParallaxDataset.dmsToRadians(23, 27, 45.191);
//        nom_epoch      = new Epoch(1987.747);
//        mu_acosd       =  0.2240;
//        sigma_mu_acosd =  0.0051;
//        mu_d           = -0.2085;
//        sigma_mu_d     =  0.0050;
        
//        epochs = new Epoch[9];
//        epochs[0] = new Epoch(2008,  6, 28);
//        epochs[1] = new Epoch(2008,  7,  4);
//        epochs[2] = new Epoch(2008,  7, 23);
//        epochs[3] = new Epoch(2008,  8, 28);
//        epochs[4] = new Epoch(2008, 11, 11);
//        epochs[5] = new Epoch(2008, 11, 18);
//        epochs[6] = new Epoch(2009,  6, 13);
//        epochs[7] = new Epoch(2009,  6, 27);
//        epochs[8] = new Epoch(2009,  7,  8);

    }
}
