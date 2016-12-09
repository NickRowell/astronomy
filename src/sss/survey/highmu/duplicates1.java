package Catalogues;

import java.io.*;
import Field.*;
import Star.HighMuStar;


/*
This program runs on the sample of WDs drawn from the high proper motion table. The first selection program
performs the survey, kinematic selection and morphology cuts in the same way as the low proper motion
selection program does. However a problem arises with the high proper motion data - in pairing objects out to
10 arcseconds, it is inevitable that for a given object at a given epoch, several different astrometric solutions
will be found by different combinations of pairing amongst the neighbouring objects. This leads to many spurious objects
in the results as some of these may pass all the survey etc. cuts.

These are 'type 1' duplicates in the survey list, and this program aims to remove these in the following manner:

It exploits the fact that these duplicates are found at close separations - smaller than 1 arcmin. If two objects share
the same R2 detection then their coordinates will be identical. If we assume that for any given splodge of crap detections
there is only one good proper motion object, then we simply look for the record with the lowest chi-squared statistic
from amongst all the possible detections. Use the fact that these always fall in the same field to separate this effect
from the 'type 2' duplicates.


'Type 2' duplicates are found in areas of plate overlap when the same object has been detected in two or more different
neighbouring fields.
These objects can be searched for after removing type one detections. They can be identified through their coordinates,
position angle and total proper motion.

*/



class duplicates1{






    public static void main(String args[]) throws IOException, Exception{


	//for tracking progress:
	File input = new File("/spare/SSS/Catalogues/"+args[0]+"/RawSample/WDs.txt");
	int Nstars = Misc.lines(input);


	//initial sample drawn from high proper motion table, opened as a random access file
	//for reading and writing - allows a marker to be placed on rows after they have been identified as duplicates,
	//so they are not checked a second time.
	RandomAccessFile raf = new RandomAccessFile(input, "rw");

	//record of objects removed from list
        BufferedWriter out1 = new BufferedWriter(new FileWriter("/spare/SSS/Catalogues/"+args[0]+"/RawSample/duplicate_WDs1.txt"));
	//Sample after removing these duplicates:
        BufferedWriter out2 = new BufferedWriter(new FileWriter("/spare/SSS/Catalogues/"+args[0]+"/RawSample/WDs1.txt"));

	//set integer 'pointer' to value at start of file:
	int pointer = (int)raf.getFilePointer();

	int start, end, counter;
	int Nstar=0;
	String marker = "d";

	//+++ Max possible number of duplicates for any given object +++//
	int Ndup = 300;

	//read in first line from sample (record_current), store in duplicates array, then sequentially read in
	//all others in same field (record_check) and write any identified duplicates to array as well, overwriting the
	//first few bytes so that the line can be ignored later. Once field
	//has been searched, take all records stored in duplicates array and decide which is the correct one.
	String record_current, record_check;

	HighMuStar star_current, star_check;

	//read each line in file sequentially:
	while((record_current = raf.readLine())!=null){

	    Nstar++;

	    //+++ Record location of file pointer for return later +++//
	    pointer = (int)raf.getFilePointer();


	    //+++ Set up array of duplicate objects, and counter +++//
	    HighMuStar[] duplicates = new HighMuStar[Ndup];
	    counter = 0;



	    //if current line was previously matched as a duplicate, skip it:
	    if(record_current.substring(0,marker.length()).equals(marker)){}

	    else{

		star_current = new HighMuStar(record_current);

		//+++ Load into first element of duplicates array and increment counter +++//
		duplicates[counter++] = star_current;

		//+++ Log current position, then proceed through remainder of file +++//
		start = (int)raf.getFilePointer();

		while((record_check = raf.readLine())!=null){

		    end = (int)raf.getFilePointer();

		    //+++ If previously flagged as duplicate, ignore +++//
		    if(record_check.substring(0,marker.length()).equals(marker)){}

		    else{

			star_check = new HighMuStar(record_check);

			//+++ Compare to star_current and check for a match +++//
			//if hemispheres, field numbers AND any pair of record pointers match, flag as a duplicate:

			if((star_current.hemi.equals(star_check.hemi))&&
			   (star_current.f==star_check.f)&&
			   ((star_current.recPointerB==star_check.recPointerB)||
			   (star_current.recPointerR1==star_check.recPointerR1)||
			   (star_current.recPointerR2==star_check.recPointerR2)||
			   (star_current.recPointerI==star_check.recPointerI))){

			    //record pointers have indicated a match - write to duplicates array and mark line.
			    duplicates[counter++] = star_check;

			    //skip back to start of line:
			    raf.seek(start);
			    //mark so that line is skipped in future:
			    raf.writeBytes(marker);
			    //return to original position ready to read in new lines:
			    raf.seek(end);

			}
		    }
		}

		start = (int)raf.getFilePointer();



	    //+++ Finished checking file for duplicates of star_current. If duplicates have been found,  +++//
	    //+++ write out to the catalogue that with the lowest chi^2 value                            +++//

	    if(duplicates[1]!=null){

		double chi = 999;
		HighMuStar chosen_object = duplicates[0];    // Initialise chosen star to current star

		int k = 0;

		//+++ Get object with lowest chi-square +++//
		while(duplicates[k]!=null){
		    if(duplicates[k].redChi2 < chi){
			chi = duplicates[k].redChi2;
			chosen_object = duplicates[k];
		    }
		    k++;
		}

		//write out most reliable duplicate to next sample catalogue:
		out2.write(chosen_object.data);
		out2.newLine();
		out2.flush();

		//record duplicates:
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
		    k++;
		}
		out1.newLine();
		out1.newLine();
		out1.write("-------------------------------------");
		out1.newLine();
		out1.flush();
	    }

	    else{
		out2.write(duplicates[0].data);
		out2.newLine();
		out2.flush();
	    }

	    }
	    //+++ Reset pointer to start of next line in main file +++//
	    raf.seek(pointer);

	}

	//+++ Finished duplicate removal +++//
	System.out.println("Finished removing type 1 duplicates");

    }


}
