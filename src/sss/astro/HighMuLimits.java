/**
 * NOW DEPRECATED
 *
 * This class provides upper proper motion limits for low proper motion survey. The limits differ
 * from field to field but are constant within eacn field. The lowest limit in each hemispere is
 * 97 mas/yr (north) and 91 mas/yr (south), so with the inclusion of the intermediate proper motion
 * catalogue that extends down to 80 mas/yr these variable limits are redundant. The upper proper motion
 * limit on the low proper motion survey is fixed at 80 mas/yr.
 */
package Field;

import java.io.*;

/**
 *  This class provides upper proper motion limits for low proper motion survey
 *  on a field by field basis.
 *
 *
 * @author nickrowell
 */
public class HighMuLimits {

    static double[][] highMuLim = new double[900][2];

    public HighMuLimits() throws IOException{

        try{
            for(int f = 1; f<highMuLim.length; f++){

                highMuLim[f][0] = muUpper(f,"N"); //north
                highMuLim[f][1] = muUpper(f,"S"); //south
            }
        }
	catch(IOException ioe){System.out.println("Error");}


    }

    //+++ Data access methods +++//
    public double getHighMuLimit(int field, String hemisphere){
        return highMuLim[field][(hemisphere.equals("N") ? 0 : 1)];
    }



    //+++ Upper proper motion limits in all fields in low proper motion survey +++//
    public static double muUpper(int fieldNum, String hemi) throws IOException{

	//file containing relevant proper motion limits:
	File limit = new File("/spare/SSS/Resources/LookupTables/ProperMotionLimits/mu_upper"+hemi);
      	BufferedReader in = new BufferedReader(new FileReader(limit));

	String data;

	//skip commented lines at start of file:
	while((data = in.readLine()).substring(0,1).equals("#")){}

	do{
	    if(Integer.parseInt(Misc.columns(data,1))==fieldNum){
		in.close();
		return (Double.parseDouble(Misc.columns(data,2))/1000.0);
	    }
	}
	while((data = in.readLine())!=null);

	in.close();

	return -99999.0;

    }


}
