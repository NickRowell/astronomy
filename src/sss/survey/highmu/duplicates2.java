package Catalogues;

import java.io.*;
import Field.*;
import Star.HighMuStar;



/*
 *
 * This program removes duplicate observations of objects that have been measured twice on neighbouring plates
 *
 * Selects for catalogue object that is closest to it's respective field centre.
 *
 */



class duplicates2{

    public static void main(String args[]) throws IOException, Exception{

	//+++ Tolerances on object matching +++//
	double angSepLimit = 4;               // in arcseconds
	double PAdiffLimit = 40;              // degrees
	double muDiffLimit = 0.05;            // arcseconds per year

        //+++ Load Footprint object so that field centres can be obtained in code +++//
        Footprint footprint = new Footprint();

	//+++ For tracking progress +++//
	File input = new File("/spare/SSS/Catalogues/"+args[0]+"/RawSample/WDs1.txt");
	int Nstars = Misc.lines(input);

	//initial sample drawn from high proper motion table, opened as a random access file
	//for reading and writing - allows a marker to be placed on rows after they have been identified as duplicates,
	//so they are not checked a second time.
	RandomAccessFile raf = new RandomAccessFile(input, "rw");

	//+++ Record of objects removed from list +++//
        BufferedWriter out1 = new BufferedWriter(new FileWriter("/spare/SSS/Catalogues/"+args[0]+"/RawSample/duplicate_WDs2.txt"));

	//+++ Cleaned WD sample after removing these duplicates +++//
        BufferedWriter out2 = new BufferedWriter(new FileWriter("/spare/SSS/Catalogues/"+args[0]+"/RawSample/WDs2.txt"));



	//set integer pointer to value at start of file:
	int pointer = (int)raf.getFilePointer();

	int start, end, counter;
	int Nstar=0;
	String marker = "d";

	//+++ Max possible number of duplicates for any given object +++//
	int Ndup = 300;

	String     record_current, record_check;
	HighMuStar star_current,   star_check;

	//read through whole file over and over:
	while((record_current = raf.readLine())!=null){

	    Nstar++;


	    //+++ Record location of file pointer so can return later +++//
	    pointer = (int)raf.getFilePointer();

	    //+++ Set up array of duplicate objects, and counter +++//
	    HighMuStar[] duplicates = new HighMuStar[Ndup];
	    counter = 0;

	    //+++ If current line was previously matched as a duplicate, skip it +++//
	    if(record_current.substring(0,marker.length()).equals(marker)){}


	    else{

		star_current = new HighMuStar(record_current);

		//+++ Load into first element of duplicates array and increment counter +++//
		duplicates[counter++] = star_current;

		start = (int)raf.getFilePointer();

		while((record_check = raf.readLine())!=null){

		    end = (int)raf.getFilePointer();

		    //if this record previously matched as duplicate, ignore:
		    if(record_check.substring(0,marker.length()).equals(marker)){}

		    else{

			star_check = new HighMuStar(record_check);

			/* For object to be a duplicate, it must lie in a different field and be within
			   tolerances defined at start of code */


			//+++ Angular distance between objects, first in radians then in arcseconds +++//
			double angSep = Misc.angSep(Math.toRadians(star_current.ra),
						     Math.toRadians(star_current.dec),
						     Math.toRadians(star_check.ra),
						     Math.toRadians(star_check.dec));

			angSep = Math.toDegrees(angSep)*3600;


			//+++ Difference in position angle, accounting for differences across zero direction +++//
                        double PAdiff=0.0;
                        if(Math.abs(star_current.theta - star_check.theta) > 180)
                            PAdiff = 360 - Math.abs(star_current.theta - star_check.theta);
                        else
                            PAdiff = Math.abs(star_current.theta - star_check.theta);

			//+++ Difference in proper motion +++//
			double muDiff = Math.abs(star_current.mu - star_check.mu);

			//+++ If stars are within the tolerances given above, flag as a duplicate observation, +++//
			//+++ or if all R1 image statistics are the same, flag as a duplicate.                 +++//

			if(star_current.f!=star_check.f &&
			   angSep<angSepLimit &&
			   PAdiff<PAdiffLimit &&
			   muDiff<muDiffLimit)
			   {

			    //+++ Object is a duplicate -> write to array and mark line +++//
			    duplicates[counter++] = star_check;

			    //skip back to start of line:
			    raf.seek(start);
			    //mark so that line is skipped in future:
			    raf.writeBytes(marker);
			    //return to original position ready to read in new lines:
			    raf.seek(end);

			}
		    }
		   start = (int)raf.getFilePointer();
		}

		//Finished searching file. If more than one observation of object has been found, write out one
		//closest to it's respective field centre:



		if(duplicates[1]!=null){

		    HighMuStar chosen_object = duplicates[0];    // Initialise chosen star to current star

		    double lowestPitch = Math.PI;    //initialise to largest possible separation (in radians)
		    double pitch;
		    double field_centre[];

		    int k = 0;

		    while(duplicates[k]!=null){

			//+++ Coordinates of associated field centre +++//
                        field_centre = footprint.getFieldCentre(duplicates[k].fMax, duplicates[k].hemi);

			pitch = Misc.angSep(Math.toRadians(duplicates[k].ra),
						     Math.toRadians(duplicates[k].dec),
						     Math.toRadians(field_centre[0]),
						     Math.toRadians(field_centre[1]));

			if(pitch<lowestPitch){
			    lowestPitch=pitch;
			    chosen_object = duplicates[k];
                        }
			k++;
		    }


		    //+++ Write out selected object, i.e. that closest to it's field centre +++//
		    out2.write(chosen_object.data);
		    out2.newLine();
		    out2.flush();

		    //record details of any duplications found:
		    out1.newLine();
		    out1.newLine();
		    out1.write("Chosen object:");
		    out1.newLine();
		    out1.write(chosen_object.data);
		    out1.newLine();
		    out1.newLine();
		    out1.write("Duplicates:");
		    out1.newLine();

		    k = 0;
		    while(duplicates[k]!=null){
			out1.write(duplicates[k].data);
			out1.newLine();
			k++;}

		    out1.flush();


		}
		else{
		    //write out objects that have no duplicate observations:
		    out2.write(duplicates[0].data);
		    out2.newLine();
		    out2.flush();
		}



	    }     // If current object previously identified as a duplicate, program skips down to here

	    //reset pointer to start of next line:
	    raf.seek(pointer);

	}    //loops back to while((record_current = raf.readLine())!=null) to get the next line to check for duplicates


	//finished
	System.out.println("Finished removing type 2 duplicates");

	}


}
