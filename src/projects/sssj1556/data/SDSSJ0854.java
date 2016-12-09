package projects.sssj1556.data;


import java.io.File;
import java.util.LinkedList;

import projects.sssj1556.astrometry.StackedImage;
import projects.sssj1556.astrometry.Star;

/**
 *
 * #      RA            DEC        Epoch      MU_ACOSD      MU_D      SIGMU_A      SIGMU_D    B(J)     R_1      R_2       I           Area       Ellipse (A, B, PA)   Class   N(0,1)    Blend      Quality  Field
 * #      (equinox J2000.0)         (J)            (mas/yr)                 (mas/yr)                                           (0.67" pix)      (mas       mas  degs)         (sigma)                         No.
 * #
       8 54 43.405   +35  3 53.41  1998.075 -0.3959E+02 -0.1052E+03  0.2680E+02  0.2786E+02   20.687   99.999   18.962   18.730          31      2038      1796   112    2      1.724        0            0    371
 * 
 * @author nickrowell
 */
public class SDSSJ0854
extends ParallaxDataset
{
    
    public SDSSJ0854()
    {
        designation = "SDSSJ0854";
        path        = new File("/home/nickrowell/Astronomy/WD_Parallax_Study_2014/data/LHS_3250");
        
        slaves   = new LinkedList<StackedImage>();
        allstars = new LinkedList<Star>();
        
//        ra             = ParallaxDataset.hmsToRadians(8, 54, 43.405);
//        dec            = ParallaxDataset.dmsToRadians(35, 3, 53.41);
//        nom_epoch      = new Epoch(1998.075);
//        mu_acosd       = -0.03959;
//        sigma_mu_acosd =  0.02680;
//        mu_d           = -0.1052;
//        sigma_mu_d     =  0.02786;    
        
//        epochs = new Epoch[2];
//        epochs[0]  = new Epoch(2007, 2,  8);
//        epochs[1]  = new Epoch(2007, 4, 27);

    }
}
