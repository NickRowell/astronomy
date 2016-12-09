package Diagrams;

import Field.*;
import Survey.*;
import java.io.*;
import flanagan.interpolation.*;

/**
 * Class prints mean lower proper motion limit as a function of magnitude for the northern
 * hemisphere and two regions of southern hemisphere.
 *
 * Also prints mean magnitude limits, once plate limit offsets are applied.
 *
 *
 * @author nickrowell
 */

class meanLims{


    public static void main(String args[]) throws IOException, Exception{

	LowMuSurvey lowMuSurvey = new LowMuSurvey("b10_GC20");
	
	//stats used to characterise proper motion limit at each magnitude:
	double north_mean=0, north_meansq=0, north_var=0;
	double south_esor_mean=0, south_esor_meansq=0, south_esor_var=0;
	double south_possie_mean=0, south_possie_meansq=0, south_possie_var=0;

	double lim;
        double[] N = new double[3];


        //+++ Magnitude limits in each field +++//
        double sercj=0, sercr=0, serci=0, esor=0, possiib=0, possiir=0, possiii=0, possie=0;


	
	//+++ Loop over small steps in apparent B magnitude +++//

	for(double B = 12; B <= 23; B += 0.1){

	    //+++ North lower proper motion limits +++//
	    N[0]=0;
	    
	    for(int f = 2;f<823; f++){

		if((Misc.checkField(f,"N"))&&(RejectField.contamination(f, "N"))&&(RejectField.epochSpread(f,"N"))){

		    lim = lowMuSurvey.getLowerProperMotionLimit(B, f, "N");

		    N[0] += 1.0;
		    north_mean   += lim;
		    north_meansq += lim*lim;
		}

	    }
	    
	    north_mean   = north_mean/N[0];
	    north_meansq = north_meansq/N[0];
	    
	    north_var = north_meansq - north_mean*north_mean;
	    
	    //+++ South ESO-R proper motion limits +++//
	    N[1]=0;
	    

	    for(int f = 1;f<607; f++){


		if((Misc.checkField(f,"S"))&&(RejectField.contamination(f, "S"))&&(RejectField.epochSpread(f,"S"))){

		    lim = lowMuSurvey.getLowerProperMotionLimit(B, f, "S");

		    N[1] += 1.0;
		    south_esor_mean   += lim;
		    south_esor_meansq += lim*lim;
		}

	    }

	    south_esor_mean   = south_esor_mean/N[1];
	    south_esor_meansq = south_esor_meansq/N[1];
	    
	    south_esor_var = south_esor_meansq - south_esor_mean*south_esor_mean;


	    //+++ South POSSI-E proper motion limits +++//
	    N[2]=0;

	    for(int f = 607;f<895; f++){

		if((Misc.checkField(f,"S"))&&(RejectField.contamination(f, "S"))&&(RejectField.epochSpread(f,"S"))){

		    lim = lowMuSurvey.getLowerProperMotionLimit(B, f, "S");

		    N[2] += 1.0;
		    south_possie_mean   += lim;
		    south_possie_meansq += lim*lim;
		}

	    }

	    south_possie_mean   = south_possie_mean/N[2];
	    south_possie_meansq = south_possie_meansq/N[2];
	    
	    south_possie_var = south_possie_meansq - south_possie_mean*south_possie_mean;

	    //write out results:
	    
	    System.out.println(B+"\t"+north_mean+"\t"+Math.sqrt(north_var)+"\t"+south_esor_mean+"\t"+Math.sqrt(south_esor_var)+"\t"+south_possie_mean+"\t"+Math.sqrt(south_possie_var));

	}





        //+++ North lower proper motion limits +++//
        N[0] = 0;

        for (int f = 2; f < 823; f++) {

            if ((Misc.checkField(f, "N")) && (RejectField.contamination(f, "N")) && (RejectField.epochSpread(f, "N"))) {

                N[0] += 1.0;

                possiib += lowMuSurvey.getFaintBLimit(f, "N");
                possiir += lowMuSurvey.getFaintR2Limit(f, "N");
                possiii += lowMuSurvey.getFaintILimit(f, "N");
                possie += lowMuSurvey.getFaintR1Limit(f, "N");
            }

        }
        //+++ South ESO-R proper motion limits +++//
        N[1] = 0;


        for (int f = 1; f < 607; f++) {


            if ((Misc.checkField(f, "S")) && (RejectField.contamination(f, "S")) && (RejectField.epochSpread(f, "S"))) {

                N[1] += 1.0;

                sercj += lowMuSurvey.getFaintBLimit(f, "S");
                sercr += lowMuSurvey.getFaintR2Limit(f, "S");
                serci += lowMuSurvey.getFaintILimit(f, "S");
                esor += lowMuSurvey.getFaintR1Limit(f, "S");
            }

        }
        //+++ South POSSI-E proper motion limits +++//
        N[2] = 0;

        for (int f = 607; f < 895; f++) {

            if ((Misc.checkField(f, "S")) && (RejectField.contamination(f, "S")) && (RejectField.epochSpread(f, "S"))) {

                N[2] += 1.0;

                sercj += lowMuSurvey.getFaintBLimit(f, "S");
                sercr += lowMuSurvey.getFaintR2Limit(f, "S");
                serci += lowMuSurvey.getFaintILimit(f, "S");
                possie += lowMuSurvey.getFaintR1Limit(f, "S");
            }

        }


        System.out.println("mean SERC-J = " + (sercj / (N[1] + N[2])));
        System.out.println("mean SERC-R = " + (sercr / (N[1] + N[2])));
        System.out.println("mean SERC-I = " + (serci / (N[1] + N[2])));
        System.out.println("mean ESO-R  = " + (esor / (N[1])));
        System.out.println("mean POSSII-B = " + possiib / (N[0]));
        System.out.println("mean POSSII-R = " + possiir / (N[0]));
        System.out.println("mean POSSII-I = " + possiii / (N[0]));
        System.out.println("mean POSSI-E  = " + possie / (N[0] + N[2]));



    }





}
