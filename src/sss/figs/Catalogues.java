package Diagrams;

import java.io.*;
import java.text.DecimalFormat;
import Star.*;

/**
 * This class writes data for the reduced proper motion and two colour diagrams.
 * Need to run gnuplot scripts in figs/ folders after this program has
 * completed.
 *
 *
 * @author nickrowell
 */
public class Catalogues {


    public static void main(String[] args) throws Exception{


      	//+++ Get input data files. Catalogue WDs, stars that fail photometric fit, and +++//
        //+++ stars that fail RPM selection.                                            +++//
	File WDs[] = {new File("/spare/SSS/Catalogues/LowPM/WDs_raw.txt"),
		      new File("/spare/SSS/Catalogues/ExtraPM/WDs_raw.txt"),
                      new File("/spare/SSS/Catalogues/HighPM/WDs_raw.txt")};

        File RPM_fails[] = {new File("/spare/SSS/Catalogues/LowPM/AllStars/NonWDs.txt"),
	  		    new File("/spare/SSS/Catalogues/ExtraPM/AllStars/NonWDs.txt"),
			    new File("/spare/SSS/Catalogues/HighPM/AllStars/NonWDs.txt")};

        File photo_fails[] = {new File("/spare/SSS/Catalogues/LowPM/AllStars/WDsRejected.txt"),
	  	   	      new File("/spare/SSS/Catalogues/ExtraPM/AllStars/WDsRejected.txt"),
			      new File("/spare/SSS/Catalogues/HighPM/AllStars/WDsRejected.txt")};

        BufferedWriter RPM    = new BufferedWriter(new FileWriter(new File("/spare/Publications/SSSWDI/figs/RPMDs/data")));
        BufferedWriter twoCol = new BufferedWriter(new FileWriter(new File("/spare/Publications/SSSWDI/figs/TwoColourPlots/data")));

        DecimalFormat xpxxx = new DecimalFormat("0.000");

        RPM.write("# First index is for all WD candidates, second is non-WDs.\n# B-R2\tRPM_B");
        twoCol.write("# First index is for all WD candidates that pass fitting, second is for failures.\n# B-R2\tB-I");



        //+++ First - write out all WD catalogue entries to each file +++//
        for(int f=0; f<3; f++){

            BufferedReader in = new BufferedReader(new FileReader(WDs[f]));

            //+++ Set flag determining whether low or high proper motion records are being read in +++//
            int flag = (f==0) ? 0:1;

            String data;

            while((data=in.readLine())!=null){
                WhiteDwarf star = new WhiteDwarf(data, flag);
                //+++ Print out RPM information +++//
                RPM.write("\n"+xpxxx.format(star.b-star.r2) + "\t" + xpxxx.format(star.getRPM()));
                //+++ Print out two colour information +++//
                twoCol.write("\n"+xpxxx.format(star.b-star.r2) + "\t" + xpxxx.format(star.b - star.i));
            }
        }

        System.out.println("Finished printing out WD data");

        //+++ Now write out photometric fitting failures. For the RPMD, these are included in +++//
        //+++ first data index with photometric fitting successes, but for two colour plot    +++//
        //+++ these are written to a second data index.                                       +++//

        twoCol.write("\n\n\n\n\n");

        for(int f=0; f<3; f++){

            BufferedReader in = new BufferedReader(new FileReader(photo_fails[f]));

            //+++ Set flag determining whether low or high proper motion records are being read in +++//
            int flag = (f==0) ? 0:1;

            String data;

            while((data=in.readLine())!=null){
                WhiteDwarf star = new WhiteDwarf(data, flag);
                //+++ Print out RPM information +++//
                RPM.write("\n"+xpxxx.format(star.b-star.r2) + "\t" + xpxxx.format(star.getRPM()));
                //+++ Print out two colour information +++//
                twoCol.write("\n"+xpxxx.format(star.b-star.r2) + "\t" + xpxxx.format(star.b - star.i));
            }
        }

        twoCol.flush();
        RPM.flush();
        System.out.println("Finished printing out WD fails data");

        //+++ Now write RPM selection failures out to RPM data file as a second index +++//

        RPM.write("\n\n\n");

        for(int f=0; f<3; f++){

            BufferedReader in = new BufferedReader(new FileReader(RPM_fails[f]));

            //+++ Set flag determining whether low or high proper motion records are being read in +++//
            int flag = (f==0) ? 0:1;

            String data;

            while((data=in.readLine())!=null){
                WhiteDwarf star = new WhiteDwarf(data, flag);
                //+++ Print out RPM information +++//
                RPM.write("\n"+xpxxx.format(star.b-star.r2) + "\t" + xpxxx.format(star.getRPM()));
            }
        }

        RPM.flush();
        twoCol.flush();

        RPM.close();
        twoCol.close();

    }
}
