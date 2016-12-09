
package sss.util;

/*
This class contains all the methods used in sample
selection routines that return properties specific to
each survey field, e.g. field centres, field solid
angles, field proper motion and mangitude limits. Makes
selection routines more compact and easier to read.

Proper motion and mag limit methods are used in sample
selection programs, Vmax program uses these as well as
survey field centres, solid angles and discovery
fractions.


*/

import java.io.*;

public class Misc {


    //+++ Parameters of data files - number of lines in each +++//
    public static double lines_North = 2790834;       // from->  nr@debian:$ wc -l North.txt etc.
    public static double lines_South = 1756598;       // subtracting two lines for headers
    public static double lines_newpm = 936109;
    public static double lines_extrpm = 905085;

    //+++ Photometric uncertainties +++//

    // Uncertainty in SSS colours. From interpolation of two points given in SSS literature
    public static double photoError(double B) {
    	double error=0;
    	if(B<16.5) {
    		error=0.07;
    	}
    	else {
    		error = (0.025714)*B - (0.3542857);
    	}
    	return error;
    }

    // Single passband photometric errors, from Table 12 in Hambly et al. 2001b
    public static double photoErrorB(double B) {
    	if(B<14)
    		return 0.33;
    	if((B>=14)&&(B<15))
    		return 0.38;
    	if((B>=15)&&(B<16))
    		return 0.33;
        if((B>=16)&&(B<17))
        	return 0.22;
        if((B>=17)&&(B<18))
        	return 0.14;
        if((B>=18)&&(B<19))
        	return 0.10;
        if((B>=19)&&(B<20))
        	return 0.09;
        if((B>=20)&&(B<21))
        	return 0.11;
        if((B>=21)&&(B<22))
        	return 0.23;
        if(B>=22)
        	return 0.22;
        return -1.0;
    }

    public static double photoErrorR1(double R1){
        if(R1<11)             return 0.77;
        if((R1>=11)&&(R1<12)) return 0.96;
        if((R1>=12)&&(R1<13)) return 1.13;
        if((R1>=13)&&(R1<14)) return 1.11;
        if((R1>=14)&&(R1<15)) return 0.85;
        if((R1>=15)&&(R1<16)) return 0.73;
        if((R1>=16)&&(R1<17)) return 0.16;
        if((R1>=17)&&(R1<18)) return 0.17;
        if((R1>=18)&&(R1<19)) return 0.22;
        if((R1>=19)&&(R1<20)) return 0.29;
        if(R1>=20)            return 1.31;
         return -1.0;
    }


    public static double photoErrorR2(double R2){
        if(R2<12)             return 0.66;
        if((R2>=12)&&(R2<13)) return 0.69;
        if((R2>=13)&&(R2<14)) return 0.66;
        if((R2>=14)&&(R2<15)) return 0.54;
        if((R2>=15)&&(R2<16)) return 0.40;
        if((R2>=16)&&(R2<17)) return 0.15;
        if((R2>=17)&&(R2<18)) return 0.08;
        if((R2>=18)&&(R2<19)) return 0.07;
        if((R2>=19)&&(R2<20)) return 0.12;
        if(R2>=20)            return 0.25;
        return -1.0;
    }

    public static double photoErrorI(double I){
        if(I<11)            return 0.97;
        if((I>=11)&&(I<12)) return 0.37;
        if((I>=12)&&(I<13)) return 1.01;
        if((I>=13)&&(I<14)) return 0.93;
        if((I>=14)&&(I<15)) return 0.72;
        if((I>=15)&&(I<16)) return 0.21;
        if((I>=16)&&(I<17)) return 0.10;
        if((I>=17)&&(I<18)) return 0.09;
        if((I>=18)&&(I<19)) return 0.17;
        if(I>=19)           return 0.84;
        return -1.0;
    }



    public static String surveyID(int id){

	switch(id){
	case 1: return "SERC-J";
	case 2: return "SERC-R";
	case 3: return "SERC-I";
	case 4: return "ESO-R";
	case 5: return "POSSI-En";
	case 6: return "POSSII-B";
	case 7: return "POSSII-R";
	case 8: return "POSSII-I";
	case 9: return "POSSI-Es";

	}

	return "Error";
    }



    //+++ Methods used in d_max/min selection, for finding max/min of 3,4 or 5 quantities +++//

    public static double min(double a, double b, double c) {
    	return Math.min(Math.min(a, b), c);
    }
    
    public static double max(double a, double b, double c) {
    	return Math.max(Math.max(a, b), c);
    }

    public static double min(double a, double b, double c, double d) {
    	return Math.min(min(a, b, c), d);
    }
    
    public static double max(double a, double b, double c, double d) {
    	return Math.max(max(a, b, c), d);
    }
    
    public static double min(double a, double b, double c, double d, double e) {
    	return Math.min(min(a, b, c, d), e);
    }
    
    public static double max(double a, double b, double c, double d, double e) {
    	return Math.max(max(a, b, c, d), e);
    }


    /* Formula for generalised volume used widely in literature, but which assumes |z| constant across annuli
     * perpendicular to LOS. This has been superseded by the use of tabulated functions giving volume enclosed
     * as a function of LOS distance, taking variation in |z| across annulus into account.
     *
     */

    public static double generalV(double b, double d, double h){

		// b - galactic latitude in radians
		// d - distance (max/min) of observation limit
		// h - assumed scaleheight
	
		double xi = (d*Math.abs(Math.sin(b)))/h;
	
		double V = ((h*h*h)/(Math.abs(Math.sin(b)*Math.sin(b)*Math.sin(b))))*(2 - (xi*xi + (2*xi) + 2)*Math.exp(-1.0*xi));
	
		return V;
    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
    //
    //  Methods used to process strings of column-based data
    //
    //


    public static String columns(String data, int colnum){

	int col = 0;
	String out = "";
	int status = 0;

	//zero = Line starts with text, one = line starts with column break
	if(data.substring(0,1).equals(" ")||data.substring(0,1).equals("\t")){status = 1;}
	else{status = 0; col = 1;}


	for(int j = 0; j < data.length(); j++){

	    //+++ When new column is encountered increment column number and record text if column is desired one +++//
	    if((status==1)&&(!data.substring(j,j+1).equals(" "))&&(!data.substring(j,j+1).equals("\t"))){
		col++;
		if(colnum==col) out += data.substring(j,j+1);
		status=0;
	    }

	    //+++ When moving from text to text, record characters if current column is desired one +++//

	    else if((status==0)&&(!data.substring(j,j+1).equals(" "))&&(!data.substring(j,j+1).equals("\t"))){

		if(colnum==col) out += data.substring(j,j+1);

	    }


	    //+++ Moving from text to white space, return 'out' if column just finished was desired one +++//

	    else if((status==0)&&(data.substring(j,j+1).equals(" ")||data.substring(j,j+1).equals("\t"))){

		if(colnum==col) return out;

		status=1;
	    }

	    //+++ moving from white space to white space; do nothing +++//

	    else{}



	}

	//+++ If desired column is at the end of a line with no white space to the right, it will end up here +++//

	return out;

    }



	//Get number of columns in a string:

    public static int columnsN(String data){

	//length of string...
	int i = data.length();

	int N = 0;  //N increments when new column is found

	String begin = data.substring(0,1);

	// 'status' -> zero = reading text, one = reading white space
	int status = 0;

	if(begin.equals(" ")){status = 1;}
	else{status = 0; N += 1;}



	//variable 'status' set to 1 if line starts with white space, 0 if not:

	for(int j = 0; j < i; j++){

	    String a = data.substring(j,j+1);



	    //moving from text to white space
	    if((a.equals(" ")||a.equals("\t"))&&(status==0)){status=1;}

	    //moving from white space to white space:
	    else if(a.equals(" ")&&(status==1)){}

	    //moving from text to text:
	    else if((a.equals(" ")!=true)&&(status==0)){}

	    //moving from white space to new text:
	    else if((a.equals(" ")!=true)&&(status==1)){
		N += 1;
	       	status = 0;
	    }

	}

	//Return counted number of columns:
	return N;

    }




    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
    //
    // Parameters unique to specific fields; solid angle, drill fraction, discovery fractions etc
    //


    //+++ Check if given field is contained in SSA at all +++//

    public static boolean checkField(int fieldNum, String hemi){
	boolean fieldOK = true;
	if((hemi.equals("N"))&&((fieldNum<2)||((fieldNum>822)&&(fieldNum<895))||(fieldNum>897))) fieldOK = false;
	if((hemi.equals("S"))&&((fieldNum>894)||(fieldNum<1))) fieldOK = false;
	return fieldOK;
    }


    //+++ Get number of lines in a file, by counting '\n' characters +++//

    public static int lines(File file) throws IOException
    {
        Reader reader = new InputStreamReader(new FileInputStream(file));

        int lineCount = 0;
        char[] buffer = new char[4096];
        for (int charsRead = reader.read(buffer); charsRead >= 0; charsRead = reader.read(buffer))
        {
            for (int charIndex = 0; charIndex < charsRead ; charIndex++)
            {
                if (buffer[charIndex] == '\n')
                    lineCount++;
            }
        }
        reader.close();
        return lineCount;
    }


    //overloaded method for case where lines of non-data are to be skipped

    public static int lines(File input, String skip) throws IOException{

        //'skip' is a character at the start of lines that identifies lines to be skipped

        BufferedReader in = new BufferedReader(new FileReader(input));

        String line;
        int n = 0;

        while((line = in.readLine())!=null){

            if(line.substring(0,1).equals(skip)){}
            else{n+=1;}

        }

        in.close();
        
        return n;
    }


}
