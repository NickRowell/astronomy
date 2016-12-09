/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Diagrams;

import Star.*;
import Field.Misc;
import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;

/**
 *
 * This class writes data for distance and tangential velocity distributions,
 * and galactic coordinates for all stars for aitoff projection.
 *
 * @author nickrowell
 */
public class Distributions {

    public static void main(String[] args) throws Exception{

  	//+++ Get array of input files corrsponding to WD candidates from each survey +++//
	File input[] = {new File("/spare/SSS/Catalogues/LowPM/WDs_fit.txt"),
			new File("/spare/SSS/Catalogues/ExtraPM/WDs_fit.txt"),
			new File("/spare/SSS/Catalogues/HighPM/WDs_fit.txt")};

        //+++ Now create an ArrayList to store all star objects +++//
        ArrayList<WhiteDwarf> stars = new ArrayList<WhiteDwarf>();

        //+++ Second pass - read all catalogue stars into array +++//
        System.out.println("Reading all stars into internal data array...");

        for(int f=0; f<3; f++){

            BufferedReader in = new BufferedReader(new FileReader(input[f]));

            //+++ Set flag determining whether low or high proper motion records are being read in +++//
            int flag = (f==0) ? 0:1;

            String data;

            while((data=in.readLine())!=null){

                WhiteDwarf star = new WhiteDwarf(data, flag);

                //+++ Get corresponding photometric models +++//
                String[] models = {in.readLine(), in.readLine(), in.readLine(), in.readLine(), in.readLine(), in.readLine()};

                //+++ Set distance to star using either fitted colours or values from literature +++//
                if (star.isKnownUCWD()) {
                    star.setPublishedAtmosphere();
                } else {
                    star.setModels(models);
                    star.setAtmosphereFractions();
                    star.setPhotoPI(models);
                }

                stars.add(star);

            }

        }

        //+++ Now, sort stars into order of increasing right ascension +++//
        System.out.println("Ordering stars on right ascension...");

        //+++ Use instance of Comparator class to sort on right ascension +++//
        byRightAscension sort = new byRightAscension();

        Collections.sort(stars,sort);

        //+++ Now write out to file +++//
        System.out.println("Calculating distributions...");

	//+++ Set up histograms +++//

	double vt_min  = 0;
	double vt_max  = 1000;
	double vt_step = 10;

	double vtan[] = new double[(int)Math.rint((vt_max - vt_min)/vt_step)];

	double d_min  = 0;
	double d_max  = 2000;
	double d_step = 20;

	double d[] = new double[(int)Math.rint((d_max - d_min)/d_step)];


        for(int s=0; s<stars.size(); s++){
            vtan[(int)Math.floor((4.74*stars.get(s).dH*stars.get(s).mu  - vt_min)/vt_step)] += stars.get(s).fracH;
            vtan[(int)Math.floor((4.74*stars.get(s).dHe*stars.get(s).mu - vt_min)/vt_step)] += stars.get(s).fracHe;

            d[(int)Math.floor((stars.get(s).dH  - d_min)/d_step)] += stars.get(s).fracH;
            d[(int)Math.floor((stars.get(s).dHe - d_min)/d_step)] += stars.get(s).fracHe;
        }

        System.out.println("Writing distribution data...");
        File cat = new File("/spare/Publications/SSSWDI/figs/Distributions/distance");
        BufferedWriter out = new BufferedWriter(new FileWriter(cat));
        for(int s=0; s<d.length; s++){out.write(((double)s*(d_step)+(d_step/2.0))+"\t"+d[s] +"\n");}

        out.flush();

        cat = new File("/spare/Publications/SSSWDI/figs/Distributions/vtan");
        out = new BufferedWriter(new FileWriter(cat));
        for(int s=0; s<vtan.length; s++){out.write(((double)s*(vt_step)+(vt_step/2.0))+"\t"+vtan[s] + "\n");}

        out.flush();

        System.out.println("Writing sky coordinates for aitoff projection plot...");


        cat = new File("/spare/Publications/SSSWDI/figs/SkyProjection/coords");
        out = new BufferedWriter(new FileWriter(cat));

        for(int s=0; s<stars.size(); s++){
            double[] galacticCoordinates = Misc.eqGalCoordinates(Math.toRadians(stars.get(s).ra), Math.toRadians(stars.get(s).dec));
            out.write(Math.toDegrees(galacticCoordinates[0]) +"\t" + Math.toDegrees(galacticCoordinates[1]) + "\n");
            out.flush();
        }

    }
}


class byRightAscension implements Comparator{

    @Override
    public int compare(Object o1, Object o2){

        WhiteDwarf star1 = (WhiteDwarf)o1;
        WhiteDwarf star2 = (WhiteDwarf)o2;

        return (int)((star1.ra - star2.ra)*100000.0);

    }


}