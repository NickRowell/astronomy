package Diagrams;

import Field.*;
import Survey.*;
import java.io.*;

/**
 * The class adds up the sky area in each of the two proper motion ranges,
 * for table 5.
 *
 *
 * @author nickrowell
 */

class footprint{


    public static void main(String args[]) throws IOException, Exception{

	LowMuSurvey lowMuSurvey = new LowMuSurvey("b10_GC20");
	HighMuSurvey highMuSurvey = new HighMuSurvey("b10_GC20");

        double areaLow = 0, areaHigh = 0;

        //+++ Northern fields +++//
        for (int f = 2; f < 823; f++) {
            if ((Misc.checkField(f, "N")) && (RejectField.contamination(f, "N")) && (RejectField.magellanicClouds(f, "N")) && (RejectField.epochSpread(f, "N"))) {
                areaHigh += highMuSurvey.getSurveyFieldArea(f, "N");   // This includes drill fraction
            }

            //+++ Low mu survey - different set of rejected fields
            if ((Misc.checkField(f, "N")) && (RejectField.contamination(f, "N")) && (RejectField.magellanicClouds(f, "N"))) {
                areaLow += lowMuSurvey.getSurveyFieldArea(f, "N");   // This includes drill fraction
            }
        }

        //+++ Southern fields +++//
        for (int f = 1; f < 895; f++) {
            if ((Misc.checkField(f, "S")) && (RejectField.contamination(f, "S")) && (RejectField.magellanicClouds(f, "S")) && (RejectField.epochSpread(f, "S"))) {
                 areaHigh += highMuSurvey.getSurveyFieldArea(f, "S");   // This includes drill fraction
            }

            //+++ Low mu survey - different set of rejected fields
            if ((Misc.checkField(f, "S")) && (RejectField.contamination(f, "S")) && (RejectField.magellanicClouds(f, "S"))) {
                areaLow += lowMuSurvey.getSurveyFieldArea(f, "S");   // This includes drill fraction
            }
        }

        System.out.println("Total area low proper motion  = "+areaLow);
        System.out.println("Total area high proper motion = "+areaHigh);


    }





}
