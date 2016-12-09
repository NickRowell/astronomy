package Diagrams;

import java.io.*;
import java.text.DecimalFormat;
import Star.*;

/**
 * This program uses cumulative proper motion number counts of high proper motion sample
 * to find an appropriate upper proper motion limit as a function of apparent magnitude.
 *
 * @author nickrowell
 */
public class ReducedChi2Counts {



    //start of main program
    public static void main(String args[]) throws IOException, Exception{


        //+++ Input binning parameters +++//
        double lower = 0.0;
        double upper = 2.5;
        double bin_width = 0.01;


        //+++ Set up binning process +++//
        int Nbins = (int) ((upper - lower) / bin_width);

        double binning_ssa[] = new double[Nbins];          // Frequency bins
        double binning_newpm[] = new double[Nbins];
        double binning_extrapm[] = new double[Nbins];

        double cumulative_ssa[] = new double[Nbins];       // Cumulative bins
        double cumulative_newpm[] = new double[Nbins];
        double cumulative_extrapm[] = new double[Nbins];

        int bin;

        //+++ output file - histogram data +++//
        BufferedWriter out = new BufferedWriter(new FileWriter("/spare/Publications/SSSWDI/figs/Chi2Dist/figs/data"));

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
                quantity = star.redChi2;

                //get corresponding bin:
                bin = (int) Math.floor((quantity - lower) / bin_width);

                //+++ Non-cumulative binning +++//

                try {
                    if(j<3)        binning_newpm[bin] += 1;
                    if(j<6 && j>2) binning_extrapm[bin] += 1;
                    else           binning_ssa[bin] += 1;
                }
                catch (ArrayIndexOutOfBoundsException aioobe1) {}

                //+++ Cumulative binning     +++//
                //add 1 to all elements associated with values higher than 'quantity'
                for (int i = 0; i < Nbins; i++) {
                    if (i < bin) {}
                    else {
                        try {
                            if(j<3)        cumulative_newpm[i] += 1;
                            if(j<6 && j>2) cumulative_extrapm[i] += 1;
                            else           cumulative_ssa[i] += 1;
                        }
                        catch (ArrayIndexOutOfBoundsException aioobe1) {}
                    }
                }


            }
        }

        //+++ normalisation factors to convert histogram to pdf +++//
        double norm_ssa = cumulative_ssa[Nbins-1]*bin_width;
        double norm_newpm = cumulative_newpm[Nbins-1]*bin_width;
        double norm_extrapm = cumulative_extrapm[Nbins-1]*bin_width;

        DecimalFormat xpxxx = new DecimalFormat("0.000");

	//+++ Print out number count histograms +++//
	for(int i = 0; i < Nbins; i++){
            String chi2 = xpxxx.format((i*bin_width + lower + (bin_width/2)));
	    out.write(chi2 + "\t" + binning_ssa[i]/norm_ssa + "\t" + cumulative_ssa[i]/norm_ssa + "\t" + binning_extrapm[i]/norm_extrapm + "\t" + cumulative_extrapm[i]/norm_extrapm+ "\t" + binning_newpm[i]/norm_newpm + "\t" + cumulative_newpm[i]/norm_newpm);
	    out.newLine();
	}
        out.flush();
        out.close();
        System.exit(0);


    }


}
