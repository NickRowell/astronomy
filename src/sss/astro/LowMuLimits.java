package sss.astro;

import flanagan.interpolation.*;
import java.io.*;


/**
 *  For the intermediate and high proper motion surveys, the proper motion limits are fixed.
 *  For the low proper motion survey howeverm they are a function of apparent magnitude. This
 *  class provides those functions.
 *
 * @author nickrowell
 */

public class LowMuLimits {

    //+++ Data member for class - array of CubicSplines for proper motion uncertainty +++//
    CubicSpline[][] sigMu = new CubicSpline[900][2];

    //+++ Offset applied to sigma_mu prior to threshold being applied +++//
    double offset = 2.0;

    //+++ Defines detection threshold for proper motions in sigmas, with above offset +++//
    double factor = 5.0;


    //+++ Main constructor +++//
    public LowMuLimits(){

      	try{
            for(int f = 1; f<sigMu.length; f++){

                sigMu[f][0] = loadLowMuLimit(f,"N");
                sigMu[f][1] = loadLowMuLimit(f,"S");
            }
        }
	catch(IOException ioe){System.out.println("Error");}
    }

    //+++ Data access methods +++//
    public double getLowMuLimit(double b, int field, String hemisphere){
        return ((sigMu[field][(hemisphere.equals("N") ? 0 : 1)].interpolate(b) + (offset/1000.0)) * factor);
    }
    
    public double getLowMuOffset(){return this.offset;}
    public double getLowMuFactor(){return this.factor;}



    //+++ Get proper motion uncertainty for a field, and return it stored in a CubicSpline +++//
    private static CubicSpline loadLowMuLimit(int fieldNum, String hemi) throws IOException{

	//+++ In case specified field is not in SSS, return empty cubic spline +++//
	if(!Misc.checkField(fieldNum,hemi)) return new CubicSpline(3);

	//+++ Get hemisphere +++//
	String hemisphere = (hemi.equals("N")) ? "North" : "South";


	//+++ Initialise & open reader on file containing desired histogram +++//
	File limit = new File("/spare/SSS/Resources/MuLimits/SSA/LowerLimits/AllFields/Fields"+hemisphere+"/"+fieldNum+""+hemi+"/muLimM");

      	BufferedReader in = new BufferedReader(new FileReader(limit));


	//+++ number of points in histogram +++//
	int n_points = Misc.lines(limit);

        
	//+++ set up arrays to store proper motion limit points+++//
	double[] mag = new double[n_points+2];    //an extra point at each end is added so that interpolated magnitude
	double[] lim = new double[n_points+2];    //is never outside range of data points
        
       
	String data;
	int index=1;

	//+++ Read in all points and store in arrays +++//
	while((data=in.readLine())!=null){
	    mag[index] = Double.parseDouble(Misc.columns(data,1));
            lim[index] = (Double.parseDouble(Misc.columns(data,2))/1000.0);    // (in mas/yr)
            
	    index++;
	}


	//add points at B=12 and B=25 so that calls to interpolation method never try to find
	//points outside the magnitude range covered by histogram:

	double gradient,intercept;

	mag[0]=12.0; mag[n_points+1]=25.0;

        //+++ first point +++//
	gradient = (lim[2]-lim[1])/(mag[2]-mag[1]);
	intercept = lim[1] - gradient*mag[1];
	lim[0] = gradient*mag[0] + intercept;

	//+++ last point +++//
	gradient = (lim[n_points] - lim[n_points-1])/(mag[n_points] - mag[n_points-1]);
	intercept = lim[n_points] - gradient*mag[n_points];
	lim[n_points+1] = gradient*mag[n_points+1] + intercept;

	CubicSpline muLimHistogram = new CubicSpline(mag,lim);

	in.close();

	return muLimHistogram;

    }

}
