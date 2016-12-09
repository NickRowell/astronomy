package sss.field.drillfractions;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

import util.ParseUtil;

/**
 * Class encapsulates the drill fractions of SSA survey fields, i.e. the proportion of the
 * fields lost to 'drilling' around bright stars.
 * 
 * @author nickrowell
 */
public class DrillFraction {

	/**
	 * The drill fractions for each field in each hemisphere, stored as fraction of survey field
	 * remaining after drilling.
	 */
    static double[][] drillFraction = new double[900][2];

    /**
     * Main constructor.
     * @throws IOException 
     */
    public DrillFraction() throws IOException {
    	
    	// Initialise all drill fractions to NaN
    	for(int i=0; i<drillFraction.length; i++) {
    		drillFraction[i][0] = Double.NaN;
    		drillFraction[i][1] = Double.NaN;
    	}
    	
    	// Now load the drill fractions from file
    	List<String> comments = new LinkedList<>();
    	comments.add("#");
    	
    	File df = new File("/spare/SSS/Resources/LookupTables/DrillFractions/drillFractionsN.txt");
      	BufferedReader in = new BufferedReader(new FileReader(df));
      	
    	double[][] data = ParseUtil.parseFile(in, ParseUtil.whitespaceDelim, comments);
    	for(int i=0; i<data.length; i++) {
    		// data[i] contains field number & drill fraction
    		int f = (int)data[i][0];
    		drillFraction[f][0] = ((100.0 - data[i][1])/100.0);
    	}
    	
    	df = new File("/spare/SSS/Resources/LookupTables/DrillFractions/drillFractionsS.txt");
      	in = new BufferedReader(new FileReader(df));
      	
    	data = ParseUtil.parseFile(in, ParseUtil.whitespaceDelim, comments);
    	for(int i=0; i<data.length; i++) {
    		// data[i] contains field number & drill fraction
    		int f = (int)data[i][0];
    		drillFraction[f][1] = ((100.0 - data[i][1])/100.0);
    	}
    	
	}

    /**
     * Get the fraction of the survey field NOT lost to drilling.
     * @param field
     * 	The field number
     * @param hemisphere
     * 	The hemisphere ["N"/"S"]
     * @return
     * 	The fraction of the survey field NOT lost to drilling [0:1]
     */
    public double getDrillFraction(int field, String hemisphere){
       return drillFraction[field][(hemisphere.equals("N") ? 0 : 1)];
    }

}