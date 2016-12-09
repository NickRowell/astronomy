package Kinematics;


import Field.*;
import java.io.*;
import flanagan.interpolation.*;

/**
 *  This class provides discovery fractions at any input tangential velocity
 *  by interpolating discrete discovery fraction tables using cubicsplines.
 *
 *
 * @author nickrowell
 */
public class DiscoveryFractions {



    //+++ Main class data member +++//
    CubicSpline[][] discoveryFractions  = new CubicSpline[900][2];



    //+++ Main Constructor +++//
    public DiscoveryFractions(String population) throws IOException{

    	try{
            for(int f = 1; f<900; f++){
                discoveryFractions[f][0] = getDFs(f,"N",population); //north
                discoveryFractions[f][1] = getDFs(f,"S",population); //south
            }
	}
	catch(IOException ioe){System.out.println("Error");}

    }

    //+++ Get a single discovery fraction from array +++//
    public double getDiscoveryFraction(int f, String hemi, double vtan){

            if(!Misc.checkField(f,hemi)) return 0.0;

            return this.discoveryFractions[f][(hemi.equals("N") ? 0 : 1)].interpolate(vtan);
    }



    //+++ Used to generate discovery fractions at arbitrary v_{tan} by interpolation +++//

    public static CubicSpline getDFs(int fieldNum, String hemi, String pop) throws IOException{

	//+++ null CubicSpline to return if field is not in SSA +++//
	CubicSpline empty = new CubicSpline(5);
	if(!Misc.checkField(fieldNum,hemi)) return empty;

	File limit = new File("/spare/SSS/Resources/LookupTables/VtanDistributions/"+pop+"/"+fieldNum+""+hemi);
      	BufferedReader in = new BufferedReader(new FileReader(limit));
	in.readLine();   //skip header

	// How many rows of data are there?
	int N = 0;
	while(in.readLine()!=null){N+=1;}

	//set up arrays of correct length:
	double[] vtan   = new double[N];
	double[] frac = new double[N];


	// get data:
	in = new BufferedReader(new FileReader(limit));  //re-initialise file
	in.readLine();                                   //skip header again...
	String data;
	int index=0;

	//scroll through file and read in all points:
	while((data = in.readLine())!=null){
	    vtan[index]   = Double.parseDouble(Misc.columns(data,1));            // v_{tan}
	    frac[index]   = (1.0-Double.parseDouble(Misc.columns(data,3)));      //Discovery fraction

	    index++;
	}

	in.close();

	return new CubicSpline(vtan,frac);
    }







    //+++ Get tangential velocity distribution in terms of probability density +++//
    //+++ rather than cumulative fraction of stars. At present, only used to   +++//
    //+++ generate a figure demonstrating kinematics of disks and spheroid     +++//

    public static CubicSpline getP_vt(int fieldNum, String hemi, String pop) throws IOException{

	//+++ null CubicSpline to return if field is not in SSA +++//
	CubicSpline empty = new CubicSpline(5);
	if(!Misc.checkField(fieldNum,hemi)) return empty;

	File limit = new File("/spare/SSS/Resources/LookupTables/VtanDistributions/"+pop+"/"+fieldNum+""+hemi);
      	BufferedReader in = new BufferedReader(new FileReader(limit));

	// How many rows of data are there?
	int N = 0; in.readLine();   //skip header
	while(in.readLine()!=null){N+=1;}

	//set up arrays of correct length:
	double[] vtan   = new double[N];
	double[] p_vtan = new double[N];


	// get data:
	in = new BufferedReader(new FileReader(limit));  //re-initialise file
	in.readLine();                                   //skip header again...
	String data;
	int index=0;

	//scroll through file and read in all points:
	while((data = in.readLine())!=null){
	    vtan[index]   = Double.parseDouble(Misc.columns(data,1));            // v_{tan}
	    p_vtan[index] = Double.parseDouble(Misc.columns(data,2));            // probability density at v_{tan}
	    index++;
	}

	CubicSpline Pvt = new CubicSpline(vtan,p_vtan);

	in.close();

	return Pvt;
    }




}
