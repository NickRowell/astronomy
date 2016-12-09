package Catalogues;

import java.io.*;
import Field.*;
import Star.HighMuStar;
import Star.PhotoPI;
//+++ Import model colour-magnitude relation as static to ease syntax in RPM selection code +++//
import static Star.PhotoPI.colMagH;

/*
This program runs on the clean sample of WD candidates drawn by sampleHigh.java and then cleaned
of duplicate observations by duplicates1 & 2 programs.

This program fits synthetic WD models to those stars supplied by procedure described above,
and writes the details out to survey field separated text files for use in the vmax calculator
to derive the luminosity function.

This is done separately to main sample selection program (unlike in the low mu sample where these are combined)
due to the resulting table of WD candidates requiring another two stages of processing before a clean list
can be defined. These stages purge the list of duplicate observations due to 1) several possible pairings with
neighbouring objects and 2) duplicate observations in overlap regions of survey fields.
*/

class photoParallaxHighMu{

    public static void main(String args[]) throws IOException, Exception{

	//+++ input file of cleaned WD catalogue +++//
	File input = new File("/spare/SSS/Catalogues/"+args[0]+"/RawSample/WDs2.txt");
	BufferedReader in = new BufferedReader(new FileReader(input));

	double Ntot = Misc.lines(input);

        //+++ Integer array to count number of stars passing a series of reduced proper motion thresholds +++//
	int[] vtan_histogram = new int[] {0,0,0,0,0,0};

	BufferedWriter out     = new BufferedWriter(new FileWriter("/spare/SSS/Catalogues/"+args[0]+"/WDs_raw.txt"));
	BufferedWriter out2    = new BufferedWriter(new FileWriter("/spare/SSS/Catalogues/"+args[0]+"/WDs_fit.txt"));
	BufferedWriter ucwd    = new BufferedWriter(new FileWriter("/spare/SSS/Catalogues/"+args[0]+"/UCWDs_in_catalogue"));
	BufferedWriter spectra = new BufferedWriter(new FileWriter("/spare/SSS/Catalogues/"+args[0]+"/spectra"));
        BufferedWriter rejects = new BufferedWriter(new FileWriter("/spare/SSS/Catalogues/"+args[0]+"/AllStars/WDsRejected.txt"));

	String data;
  	double N=0,C=0;

	while((data = in.readLine())!=null){

	    //+++ Progress report +++//
	    if(((++N)%(Ntot/100))==0)
		System.out.println("photoParallaxHighMu.java is "+((int)((N/Ntot)*100.0))+"% complete");

	    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
	    //                                                            //
	    // Initialise new HighMuStar object using current record      //
	    //                                                            //
	    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//

	    HighMuStar star = new HighMuStar(data);

	    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
	    //                                                              //
	    // Check if star is a previously identified WD with atmospheric //
	    // parameters taken from the literature. If so, set internal    //
	    // HighMuStar parameter that specifies file containing parallax //
	    // then get atmospheric parameters with method setPublishedAt.  //
	    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//

	    if(star.isKnownUCWD()) star.setPublishedAtmosphere();
	    else                   star.fitSyntheticColours();

	    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
	    //                                                              //
	    // Check that residuals to model atmosphere fits are acceptable //
	    //                                                              //
	    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//


	    if((Double.parseDouble(Misc.columns(star.bestFitH,12))  < PhotoPI.chi2lim)
	       ||(Double.parseDouble(Misc.columns(star.bestFitHe,12)) < PhotoPI.chi2lim)){

                //+++ Count stars that pass a series of tangential velocity thresholds +++//
                if (star.getRPM() > PhotoPI.getRpmThreshold(star.b - star.r2, 20)) vtan_histogram[0]++;
                if (star.getRPM() > PhotoPI.getRpmThreshold(star.b - star.r2, 30)) vtan_histogram[1]++;
                if (star.getRPM() > PhotoPI.getRpmThreshold(star.b - star.r2, 40)) vtan_histogram[2]++;
                if (star.getRPM() > PhotoPI.getRpmThreshold(star.b - star.r2, 100)) vtan_histogram[3]++;
                if (star.getRPM() > PhotoPI.getRpmThreshold(star.b - star.r2, 160)) vtan_histogram[4]++;
                if (star.getRPM() > PhotoPI.getRpmThreshold(star.b - star.r2, 200)) vtan_histogram[5]++;

		//+++ Write out raw SSS data for all WD candidates +++//
		out.write(star.data);
		out.newLine();
		out.flush();

		//+++ As above, but with photoparallax details included over following six lines +++//
		out2.write(star.data);  		out2.newLine();
		out2.write("#"+star.bestFitH);		out2.newLine();
		out2.write("#"+star.oneSigmaLowerH);	out2.newLine();
		out2.write("#"+star.oneSigmaUpperH);	out2.newLine();
		out2.write("#"+star.bestFitHe);		out2.newLine();
		out2.write("#"+star.oneSigmaLowerHe);	out2.newLine();
		out2.write("#"+star.oneSigmaUpperHe);	out2.newLine();
		out2.flush();

		//+++ Record any UCWDs that make it into sample +++//
		if(star.ucwd){
		    ucwd.write("#"+star.ucwdAtmosphere.toString());
		    ucwd.newLine();
		    ucwd.write(star.data);
		    ucwd.newLine();
		    ucwd.flush();
		}

		//+++ For objects with spectroscopic followups, write out to another file +++//
		if(star.spec){
		    spectra.write("#"+star.designation());
		    spectra.newLine();
		    spectra.write(star.data);
		    spectra.newLine();
		    spectra.flush();
		}

	    }
            else{
                //+++ Write out WD candidates with large residuals to photometric parallax fit +++//
		rejects.write(star.data);
		rejects.newLine();
		rejects.flush();
	    }





	}

	System.out.println("Finished fitting photometric models");

	System.out.print("Nstars with Vtan > (20,30,40,100,160,200) = ("+vtan_histogram[0]);
        for(int v=1; v<vtan_histogram.length; v++)
            System.out.print(", "+vtan_histogram[v]);
        System.out.print(")");

    }

}