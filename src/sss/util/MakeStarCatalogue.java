package Catalogues;

import Star.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;

/**
 *
 * This class reads the WD catalogues compiled by the three surveys, and writes them into a form
 * alright for publication, i.e. strips out all redundant morphological information and housekeeping
 * info, and equalises stars from low and high proper motion sources.
 *
 * @author nickrowell
 */
public class MakeStarCatalogue {


    public static void main(String[] args) throws Exception{

  	//+++ Get array of input files corrsponding to WD candidates from each survey +++//
	File input[] = {new File("/spare/SSS/Catalogues/LowPM/WDs_fit.txt"),
			new File("/spare/SSS/Catalogues/ExtraPM/WDs_fit.txt"),
			new File("/spare/SSS/Catalogues/HighPM/WDs_fit.txt")};

        //+++ Now create an ArrayList to store all star objects +++//
        ArrayList<WhiteDwarf> stars = new ArrayList<WhiteDwarf>();

        //+++ Store numbers of stars in various v_{tan} ranges +++//
        double[][] RPM  = new double[][] {{20,0},{30,0},{40,0},{160,0},{200,0},{240,0}};      // RPM selected velocity subsamples
        double[][] vtan = new double[][] {{20,0},{30,0},{40,0},{160,0},{200,0},{240,0}};      // Photo-PI selected velocity subsamples

        //+++ Read all catalogue stars into array +++//
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

                //+++ Add star to velocity subsample counters +++//
                for(int r=0; r<RPM.length; r++)
                    if(star.getRPM() > PhotoPI.getRpmThreshold(star.b - star.r2, RPM[r][0]))
                        RPM[r][1]++;

                for(int v=0; v<vtan.length; v++){
                    if(4.74 * star.dH * star.mu > vtan[v][0])
                        vtan[v][1]+=star.fracH;
                    if(4.74 * star.dHe * star.mu > vtan[v][0])
                        vtan[v][1]+=star.fracHe;
                }


            }

        }

        //+++ Now, sort stars into order of increasing right ascension +++//
        System.out.println("Ordering stars on right ascension...");

        //+++ Use instance of Comparator class to sort on right ascension +++//
        byRightAscension sort = new byRightAscension();

        Collections.sort(stars,sort);

        //+++ Now write out to file +++//
        System.out.println("Writing stars out to file...");

        File cat = new File("/spare/SSS/Catalogues/Release/ssawdcat");
        BufferedWriter out = new BufferedWriter(new FileWriter(cat));

        out.write(WhiteDwarf.getCatalogueHeader()+"\n");

        for(int s=0; s<stars.size(); s++){
                out.write(stars.get(s).getCatalogueEntry()+"\n");
                out.flush();
        }

        //+++ Print out velocity subsample counts +++//
        for(int r=0; r<RPM.length; r++)
            System.out.println("RPM threshold of "+RPM[r][0]+" kms^{-1}:  "+RPM[r][1]);

        for(int v=0; v<vtan.length; v++)
            System.out.println("vtan threshold of "+vtan[v][0]+" kms^{-1}: "+vtan[v][1]);


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