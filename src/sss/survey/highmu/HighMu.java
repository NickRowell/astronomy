
package Catalogues;

import java.io.*;
import Field.*;
import Star.HighMuStar;
import Survey.HighMuSurvey;
import Star.PhotoPI;


class HighMu{

    public static void main(String args[]) throws IOException, Exception{


	//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
	//                                                                      //
	// Set up data file from which WD catalogue is to be drawn              //
	//                                                                      //
	//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//

 	File input = new File("src/Data/newpm_everything.dat");

	BufferedReader in = new BufferedReader(new FileReader(input));


	//+++ Minimum tangential velocity threshold for inclusion in WD catalogue, based on RPM +++//
	double vtan = 20;

        //+++ Initialise all survey parameters +++//
        HighMuSurvey highMuSurvey = new HighMuSurvey("b10_GC20");

	//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
	//                                                                      //
	// Output WD catalogues                                                 //
	//                                                                      //
	//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//

	//output file for all white dwarfs, to be checked later for duplicates:
	BufferedWriter out = new BufferedWriter(new FileWriter("/spare/SSS/Catalogues/HighPM/RawSample/WDs.txt"));

	//and for all stars that fail WD selection:
	BufferedWriter outNonWDs = new BufferedWriter(new FileWriter("/spare/SSS/Catalogues/HighPM/AllStars/NonWDs.txt"));

	//+++ time stamp to record how long program is taking to run +++//
	double timeStamp = System.currentTimeMillis();

	//+++ Track progress +++//
	double N=0, Npass=0;

	String data;

        //+++ Track rejects +++//
        int[] tracker = new int[6];

	System.out.println("Reading newpm_everything.dat");

	while((data=in.readLine())!=null){

	    //+++ Progress report +++//
	    if(((++N)%(Misc.lines_newpm/100))==0){
		System.out.println("High proper motion WD selection is "+((int)Math.rint((N/Misc.lines_newpm)*100.0))+"% complete after "+((System.currentTimeMillis() - timeStamp)/(1000.0 * 60.0))+ " minutes.");
		timeStamp = System.currentTimeMillis();
	    }

	    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
	    //                                                            //
	    // Initialise new HighMuStar object using current record      //
	    //                                                            //
	    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//

	    HighMuStar star = new HighMuStar(data);

	    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
	    //                                                                      //
	    // Image morphology criteria. Cuts on quality, blend and profile stat   //
            // Only restrict R1 if object is detected at this epoch                 //
	    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//

	    if       ((star.qb>highMuSurvey.qcut)
		      ||(star.qr1>highMuSurvey.qcut)
		      ||(star.qr2>highMuSurvey.qcut)
		      ||(star.qi>highMuSurvey.qcut)){}

	    else if  ((star.bb!=0)
		      ||(star.br1!=0)
		      ||(star.br2!=0)
		      ||(star.bi!=0)){}
 // closes if{} block for all objects that have already
                    // passed survey etc. cuts and that
                    // also pass cut on residuals of model fitting.
	    else if  ((Math.abs(star.pb)>highMuSurvey.pcscut)
		      ||(Math.abs(star.pr1)>highMuSurvey.pcscut)
		      ||(Math.abs(star.pr2)>highMuSurvey.pcscut)
		      ||(Math.abs(star.pi)>highMuSurvey.pcscut)){}


	    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
	    //                                                                      //
	    // Reduced chi^2 cut, different limits in north and south hemispheres   //
            //                                                                      //
	    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//

	    else if(star.redChi2>highMuSurvey.getChi2Limit(star.hemi)){}



	    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
	    //                                                                        //
	    // Reject objects that have fewer  than B,R2,I plate detections, or whose //
	    // r1 and r2 magnitudes are inconsistent                                  //
	    //                                                                        //
	    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//

	    else if((star.b>90)||(star.r2>90)||(star.i>90)||(star.r1>90)){}
 	    else if(Math.abs(star.r1 - star.r2)>highMuSurvey.delRLim){}


	    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
	    //                                                                      //
	    // Reject objects if they are observed in fields selected for exclusion //
	    // from survey                                                          //
	    // i.e. three of four epochs within 2 years, on magellanic cloud cores, //
            // or significantly contaminated.                                       //
            //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//

	    else if(!highMuSurvey.includeField(star.f, star.hemi)){}


	    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
	    //                                                                      //
	    // Restriction on footprint area                                        //
            //                                                                      //
	    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//

            else if(!highMuSurvey.footprint.isStarInRegion(star)){}


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

	    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
	    //                                                                      //
	    // Magnitude and proper motion limits for survey                        //
	    //                                                                      //
	    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//

                boolean pass=true;

                if((star.b  > highMuSurvey.getFaintBLimit(star.f, star.hemi))||(star.b  < highMuSurvey.getBrightBLimit(star.f, star.hemi))){tracker[0]++; pass=false;}
                if((star.r2 > highMuSurvey.getFaintR2Limit(star.f, star.hemi))||(star.r2 < highMuSurvey.getBrightR2Limit(star.f, star.hemi))){tracker[1]++; pass=false;}
                if((star.i  > highMuSurvey.getFaintILimit(star.f, star.hemi))||(star.i  < highMuSurvey.getBrightILimit(star.f, star.hemi))){tracker[2]++; pass=false;}
                if((star.r1 > highMuSurvey.getFaintR1Limit(star.f, star.hemi))||(star.r1 < highMuSurvey.getBrightR1Limit(star.f, star.hemi))){tracker[3]++; pass=false;}

                if(star.mu < highMuSurvey.getLowerProperMotionLimit(star.b, star.f, star.hemi)){tracker[4]++; pass=false;}
                if(star.mu > highMuSurvey.getUpperProperMotionLimit()){tracker[5]++; pass=false;}




                if(pass){

                    //+++ All WD candidates identified by kinematical selection +++//
                    Npass += 1;
                    out.write(star.data);
                    out.newLine();
                    out.flush();
                }

	    }





	}

	out.close();
	outNonWDs.close();

        /*
         *  Finished initial catalogue selection. Now, purge of duplicate observations in
         *  overlapping field regions, and any objects that share record pointers which indicates
         *  pairing integrity has been compromised.
         *
         */
        
	System.out.println("WDs in raw sample = "+Npass);

        System.out.println("WDs rejected on bj      = "+tracker[0]);
        System.out.println("WDs rejected on r2      = "+tracker[1]);
        System.out.println("WDs rejected on i       = "+tracker[2]);
        System.out.println("WDs rejected on r2      = "+tracker[3]);
        System.out.println("WDs rejected on low mu  = "+tracker[4]);
        System.out.println("WDs rejected on high mu = "+tracker[5]);

        String[] label = {"HighPM",""};

	System.out.println("Removing type 1 duplicates from WD catalogue...");
	duplicates1.main(label);


	System.out.println("Removing type 2 duplicates from WD catalogue...");
	duplicates2.main(label);

        /*
         *  Now fit all remaining WD candidates with photometric models. Record statistics for
         *  objects that pass this stage of selection.
         *
         */


	System.out.println("Fitting photometric models to cleaned WD catalogues...");

	photoParallaxHighMu.main(label);

	System.exit(0);

    }

}