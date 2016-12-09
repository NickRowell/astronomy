package Diagrams;

import java.io.*;
import java.text.DecimalFormat;
import java.awt.*;
import java.awt.event.*;
import Star.*;

/**
 * This program uses cumulative proper motion number counts of high proper motion sample
 * to find an appropriate upper proper motion limit as a function of apparent magnitude.
 *
 * @author nickrowell
 */
public class ProperMotionCounts {



    //start of main program
    public static void main(String args[]) throws IOException, Exception{


        //+++ Input binning parameters +++//
        double lower = 0.0;
        double upper = 10.0;
        double bin_width = 0.01;


        //+++ Set up binning process +++//

        //set up array to store binned data, and initialise all elements to zero:
        int Nbins = (int) ((upper - lower) / bin_width);
        double binning[] = new double[Nbins];
        for (int i = 0; i < Nbins; i++) binning[i] = 0;
        
        //cumulative number counts:
        double cumulative[] = new double[Nbins];
        for (int i = 0; i < Nbins; i++) cumulative[i] = 0;
        
        int bin;

        //+++ output file - histogram data +++//
        BufferedWriter out = new BufferedWriter(new FileWriter("/spare/Publications/SSSWDI/figs/HighProperMotionLimit/data"));

        File[] input = new File[]{new File("/spare/SSS/Catalogues/HighPM/AllStars/NonWDs.txt"),
            new File("/spare/SSS/Catalogues/HighPM/AllStars/WDsRejected.txt"),
            new File("/spare/SSS/Catalogues/HighPM/WDs_raw.txt"),
            new File("/spare/SSS/Catalogues/ExtraPM/AllStars/NonWDs.txt"),
            new File("/spare/SSS/Catalogues/ExtraPM/AllStars/WDsRejected.txt"),
            new File("/spare/SSS/Catalogues/ExtraPM/WDs_raw.txt"),
            new File("/spare/SSS/Catalogues/LowPM/AllStars/NonWDs.txt"),
            new File("/spare/SSS/Catalogues/LowPM/AllStars/WDsRejected.txt"),
            new File("/spare/SSS/Catalogues/LowPM/WDs_raw.txt")};

        //+++ loop over all input files +++//
        for (int j = 0; j < input.length; j++) {

            System.out.println("File "+input[j].getAbsolutePath());

            //+++ store input data +++//
            String data;
            double quantity;
            BufferedReader in = new BufferedReader(new FileReader(input[j]));

            while ((data = in.readLine()) != null) {

                //+++ Create a new WhiteDwarf object using this input line +++//
                WhiteDwarf star = new WhiteDwarf(data, ((j<6) ? 1 : 0));

                //+++ Extract quantity to be binned from input line +++//
                quantity = star.mu;

                //get corresponding bin:
                bin = (int) Math.floor((quantity - lower) / bin_width);


                //+++ Non-cumulative binning +++//
                try {
                    binning[bin] += 1;
                } catch (ArrayIndexOutOfBoundsException aioobe1) {
                }

                //+++ Cumulative binning     +++//
                //add 1 to all elements associated with values lower than 'quantity'
                for (int i = 0; i < Nbins; i++) {
                    if (i > bin) {
                    } else {
                        try {
                            cumulative[i] += 1;
                        } catch (ArrayIndexOutOfBoundsException aioobe1) {
                        }
                    }
                }






            }
        }


	//+++ Print out number count histograms +++//
	for(int i = 0; i < Nbins; i++){
	    out.write((i*bin_width + lower + (bin_width/2)) + "\t" + binning[i] + "\t" + cumulative[i]);
	    out.newLine();
	}

        System.exit(0);


    }


}
