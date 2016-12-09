
package Catalogues;

import java.io.*;
import Field.*;
import Star.LowMuStar;
import Star.PhotoPI;
import Survey.LowMuSurvey;


class LowMu{



    public static void main(String args[]) throws IOException, Exception{


	//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
	//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
	//                                                                      //
	// Input data:
	//
	// Low proper motion catalogue files


	File north = new File("src/Data/North.txt");
	File south = new File("src/Data/South.txt");


	//+++ Minimum tangential velocity threshold for inclusion in WD catalogue, based on RPM +++//
	double vtan = 20;

        //+++ Integer array to count number of stars passing a series of reduced proper motion thresholds +++//
	int[] vtan_histogram = new int[] {0,0,0,0,0,0};


        //+++ Initialise all survey parameters +++//

        LowMuSurvey lowMuSurvey = new LowMuSurvey("b10_GC20");


	//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	//
	// Output data files:
	//

	System.out.println("Setting up output file streams...");

        String dir = "/spare/SSS/Catalogues/LowPM/";

        //Stars that fail RPM selection as WD candidates
        BufferedWriter outNonWDs = new BufferedWriter(new FileWriter(dir+"AllStars/NonWDs.txt"));

        //for stars that fail photometric parallax fitting
        BufferedWriter rejects =new BufferedWriter(new FileWriter(dir+"AllStars/WDsRejected.txt"));


	BufferedWriter ucwd = new BufferedWriter(new FileWriter(dir+"UCWDs_in_catalogue"));
	BufferedWriter out = new BufferedWriter(new FileWriter(dir+"WDs_raw.txt"));  //raw SSS data for WD candidates
	BufferedWriter out2 = new BufferedWriter(new FileWriter(dir+"WDs_fit.txt")); //as above, with photometric parallaxes
	BufferedWriter spectra = new BufferedWriter(new FileWriter(dir+"spectra"));  // objects in WDs_fit.txt that have
	                                                                         // spectroscopic follow ups


        //+++ Track rejects +++//
        int[] tracker = new int[6];

	//+++ Track progress of program +++//
	double Ntot = Misc.lines_North+Misc.lines_South;
	double N = 0;

	String data;

	BufferedReader in = new BufferedReader(new FileReader(north));
	//skip first two lines of data file:
	in.readLine();
	in.readLine();

	for(int j=0; j<2; j++){

	System.out.println("Reading SSA "+ ((j==0) ? "north" : "south") +" data file...");

	while((data=in.readLine())!=null){

	 
	    //+++ Progress report +++//
            if(((++N)%(Ntot/100))==0)
		System.out.println("Low proper motion catalogue is "+(Math.rint((N/Ntot)*100.0))+"% complete");

	    



	    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
	    //                                                            //
	    // Initialise new LowMuStar object using current record       //
	    //                                                            //
	    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//

	    LowMuStar star = new LowMuStar(data);

	    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
	    //                                                                      //
	    // Image morphology criteria. Cuts on quality and profile stat (blend   //
            // numbers all zero)                                                    //
	    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//

	    if       ((star.qb>lowMuSurvey.qcut)
		    ||(star.qr1>lowMuSurvey.qcut)
		    ||(star.qr2>lowMuSurvey.qcut)
		    ||(star.qi>lowMuSurvey.qcut)){}



	    else if  ((Math.abs(star.pb)>lowMuSurvey.pcscut)
		    ||(Math.abs(star.pr1)>lowMuSurvey.pcscut)
		    ||(Math.abs(star.pr2)>lowMuSurvey.pcscut)
		    ||(Math.abs(star.pi)>lowMuSurvey.pcscut)){}



	    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
	    //                                                                      //
	    // Reduced chi^2 cut, different limits in north and south hemispheres   //
            //                                                                      //
	    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//

	    else if(star.redChi2>lowMuSurvey.getChi2Limit(star.hemi)){}


	    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
	    //                                                                      //
	    // Reject objects that have fewer than 4 plate detections.              //
	    //                                                                      //
	    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//

	    else if((star.b==-99)||(star.r1==-99)||(star.r2==-99)||(star.i==-99)){}


	    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
	    //                                                                      //
	    // Remove fields centred on magellanic cloud cores,
	    // and any others that show high levels of contamination.
            //
	    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//

	    else if(!lowMuSurvey.includeField(star.f, star.hemi)){}

	    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
	    //                                                                      //
	    // Restriction on footprint area                                        //
            //                                                                      //
	    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//

            else if(!lowMuSurvey.footprint.isStarInRegion(star)){}



            //+++ Star passes morphological and astrometric constraints, magnitude/proper motion +++//
            //+++ cuts, lies in the selected footprint region and is in a reliable field.        +++//
            //+++ Now select white dwarf candidates from remaining stars based on RPM.           +++//

            else if(star.getRPM() < PhotoPI.getRpmThreshold(star.b - star.r2, vtan)){

                    //+++ All non - white dwarfs +++//
                    outNonWDs.write(star.data);
                    outNonWDs.newLine();
                    outNonWDs.flush();
            }


            else{

                //+++ Star passes survey selection as a WD candidate. Now apply limits +++//

                boolean pass = true;

                if((star.b  > lowMuSurvey.getFaintBLimit(star.f, star.hemi))||(star.b  < lowMuSurvey.getBrightBLimit(star.f, star.hemi))){tracker[0]++; pass=false;}
                if((star.r2 > lowMuSurvey.getFaintR2Limit(star.f, star.hemi))||(star.r2 < lowMuSurvey.getBrightR2Limit(star.f, star.hemi))){tracker[1]++; pass=false;}
                if((star.i  > lowMuSurvey.getFaintILimit(star.f, star.hemi))||(star.i  < lowMuSurvey.getBrightILimit(star.f, star.hemi))){tracker[2]++; pass=false;}
                if((star.r1 > lowMuSurvey.getFaintR1Limit(star.f, star.hemi))||(star.r1 < lowMuSurvey.getBrightR1Limit(star.f, star.hemi))){tracker[3]++; pass=false;}

                if(star.mu < lowMuSurvey.getLowerProperMotionLimit(star.b, star.f, star.hemi)){tracker[4]++; pass=false;}
                if(star.mu > lowMuSurvey.getUpperProperMotionLimit()){tracker[5]++; pass=false;}

                if(pass){

                //+++ All selected white dwarf candidates -> fit with photometric models +++//
                //+++ and write out to cataluges.                                        +++//

                    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
                    //                                                              //
                    // Check if star is a previously identified WD with atmospheric //
                    // parameters taken from the literature. If so, set internal    //
                    // parameter that specifies file containing parallax            //
                    // then get atmospheric parameters with method setPublishedAt.  //
                    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//

                    if (star.isKnownUCWD()) star.setPublishedAtmosphere();
                    else                    star.fitSyntheticColours();

                    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
                    //                                                              //
                    // Check that residuals to model atmosphere fits are acceptable //
                    //                                                              //
                    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//

                    if ((Double.parseDouble(Misc.columns(star.bestFitH, 12)) < PhotoPI.chi2lim)
                            || (Double.parseDouble(Misc.columns(star.bestFitHe, 12)) < PhotoPI.chi2lim)) {

                        //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
                        //                                                              //
                        // If so, write out to catalogue (vtan threshold applied at RPM //
                        // selection stage rather than here)                            //
                        //                                                              //
                        //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//

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
                        out2.write(star.data);
                        out2.newLine();
                        out2.write("#" + star.bestFitH);
                        out2.newLine();
                        out2.write("#" + star.oneSigmaLowerH);
                        out2.newLine();
                        out2.write("#" + star.oneSigmaUpperH);
                        out2.newLine();
                        out2.write("#" + star.bestFitHe);
                        out2.newLine();
                        out2.write("#" + star.oneSigmaLowerHe);
                        out2.newLine();
                        out2.write("#" + star.oneSigmaUpperHe);
                        out2.newLine();
                        out2.flush();


                        //+++ Record any UCWDs that make it into sample +++//
                        if (star.ucwd) {
                            ucwd.write("#" + star.ucwdAtmosphere.toString());
                            ucwd.newLine();
                            ucwd.write(star.data);
                            ucwd.newLine();
                            ucwd.flush();
                        }

                        //+++ For objects with spectroscopic followups, write out to another file +++//
                        if (star.spec) {
                            spectra.write("#" + star.designation());
                            spectra.newLine();
                            spectra.write(star.data);
                            spectra.newLine();
                            spectra.flush();
                        }

                    }
                    else {

                        //+++ Write out WD candidates that have high residuals to photometric models +++//
                        rejects.write(data);
                        rejects.newLine();
                        rejects.flush();

                    }

                }      // closes if(pass) for objects that pass survey completeness limits

	    }          // closes else{} block for all objects that pass basic survey cuts

	}


	//switch over to southern hemishere:

	in = new BufferedReader(new FileReader(south));
	//skip first two lines of data file:
	in.readLine();
	in.readLine();


	}

        System.out.println("WDs rejected on bj      = "+tracker[0]);
        System.out.println("WDs rejected on r2      = "+tracker[1]);
        System.out.println("WDs rejected on i       = "+tracker[2]);
        System.out.println("WDs rejected on r2      = "+tracker[3]);
        System.out.println("WDs rejected on low mu  = "+tracker[4]);
        System.out.println("WDs rejected on high mu = "+tracker[5]);

	System.out.print("Nstars with Vtan > (20,30,40,100,160,200) = ("+vtan_histogram[0]);
        for(int v=1; v<vtan_histogram.length; v++)
            System.out.print(", "+vtan_histogram[v]);
        System.out.print(")");

        System.exit(0);

    }




}