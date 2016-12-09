package Diagrams;

/*
 * This short program bins SSS fields areas (allsky) into a frequency histogram which can
 * be put in thesis/papers to give some idea of solid angle range of fields in seamless
 * catalogue.
 *
 * Direct output to a file 'areaFrequencyPlot' (lubnaig> java areaFrequency > areaFrequencyPlot)
 * Gnuplot script in /THESIS/Figures/areaFrequency.p reads this and prints a graph that 
 * gets imported into thesis chapter 2.
 * 
 */

import java.io.*;
import Field.*;


class areaFrequency{


    public static void main(String args[]) throws IOException{

	
	/*
	 * Set width of omega bins in steradians (resolution of histogram)
	 *
	 */


	double bin_width = 0.0001;

	/*
	 * Specify range of omega, i.e. set highest and lowest omega values so that program 
	 * knows what range to expect and can set up an array of a suitable size.
	 * 
	 */

	double omega_min = 0;
	double omega_max = 0.01;

	// Get required array length to cover all possible omega points:
	int n_p = (int)Math.rint((omega_max - omega_min)/bin_width);


	/*
	 * Set up an array to store omega-number points
	 *
	 */


	int frequency[] = new int[n_p];


	/*
	 * Set up files containing all-sky solid angles to be binned, a reader
	 * to open on these and a string variable to store line-by-line data from
	 * each file.
	 */


	File north = new File("/spare/SSS/Resources/SurveyVolume/SurveyFieldAreas/allsky/solidAnglesN.txt");
	File south = new File("/spare/SSS/Resources/SurveyVolume/SurveyFieldAreas/allsky/solidAnglesS.txt");

	BufferedReader input;

	String data;


	// Loop over hemispheres:

	for(int h = 0; h<2; h++){

	    // Open reader on correct file. North first (h=0), south second (h=1).

	    input = (h==0) ? new BufferedReader(new FileReader(north)) :
		             new BufferedReader(new FileReader(south));

	    
	    /*
	     * Scroll through input file and bin omega values:
	     *
	     */
	     
	    while((data=input.readLine())!=null){
		frequency[(int)Math.floor((Double.parseDouble(Misc.columns(data,2))  - omega_min)/bin_width)]++;
	    }

	}


	/*
	 * Print out omega-frequency values
	 *
	 */

	double omega_mid;


	for(int o = 0; o< frequency.length; o++){
	    
	    // translate index o into mid-point of corresponding omega bin.

	    omega_mid = (double)o * bin_width + omega_min + (bin_width/2.0);

	    System.out.println(omega_mid + "\t" + frequency[o]);


	}
	



    }


}
