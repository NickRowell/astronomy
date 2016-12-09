/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Diagrams;
import Kinematics.DiscoveryFractions;
import flanagan.interpolation.*;
import java.io.*;
/**
 *
 * @author nickrowell
 */
public class DisksAndSpheroidVTan {


    public static void main(String args[]) throws IOException{

        CubicSpline thin  = DiscoveryFractions.getP_vt(362, "S", "ThinDisk");
        CubicSpline thick = DiscoveryFractions.getP_vt(362, "S", "ThickDisk");
        CubicSpline sph   = DiscoveryFractions.getP_vt(362, "S", "Halo");


        BufferedWriter out = new BufferedWriter(new FileWriter(new File("/spare/Publications/SSSWDI/figs/Pvt/data")));

        for(double vt = 0; vt < 598; vt++){

            out.write(vt+"\t"+thin.interpolate(vt)+"\t"+ thick.interpolate(vt)+"\t"+ sph.interpolate(vt)+"\n");

        }

        out.close();

    }


}



