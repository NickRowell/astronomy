package Field;

import java.io.*;
import flanagan.interpolation.CubicSpline;

/**
 *
 * @author nickrowell
 */
public class GeneralizedVolume {

    //+++ Data member for class - array of CubicSplines for generalized volume along the line of sight +++//
    CubicSpline[][] vGen = new CubicSpline[900][2];

    //+++ Label for footprint used to derive generalized volume functions +++//
    String surveyFootprint;

    //+++ Scaleheight used to derive generalized volume functions +++//
    double scaleheight;

    //+++ Distance of Sun from Galactic plane used to derive generalized volume functions +++//
    int z_solar;

    //+++ Main constructor +++//
    public GeneralizedVolume(String footprint, double H, int z_s) throws Exception{

        //+++ Test footprint is included in survey models +++//
        if(!Footprint.checkFootprint(footprint)) throw new Exception("Footprint "+footprint+" not recognized!");

        //+++ Test Z_{solar} against values considered in models +++//
        if(z_s!=0 && z_s!=20) throw new Exception("Z_{solar} "+z_s+" not available!");

        //+++ Test scaleheight against values considered in models +++//
        if(H!=200 &&
           H!=250 &&
           H!=300 &&
           H!=350 &&
           H!=1000 &&
           H!=1500)  throw new Exception("Scaleheight "+H+" not available!");


        //+++ Generalized volume models are available for this set of paramaters; load models +++//
        try{
            for(int f = 1; f<vGen.length; f++){

                //+++ Load Generalized volume models as CubicSpline objects +++//
                vGen[f][0] = loadVGenD(footprint,f,"N",H,z_s);
                vGen[f][1] = loadVGenD(footprint,f,"S",H,z_s);            }
        }
	catch(IOException ioe){System.out.println("Error");}

        //+++ Store parameters for models to other data members +++//
        surveyFootprint = footprint;
        scaleheight     = H;
        z_solar         = z_s;

    }

    //+++ Data access methods +++//
    public String getFootprint(){return surveyFootprint;}
    public double getScaleheight(){return scaleheight;}
    public int getZSolar(){return z_solar;}

    //+++ Method that will be used most often +++//
    public double getVGen(double d, int field, String hemisphere){
        return vGen[field][(hemisphere.equals("N") ? 0 : 1)].interpolate(d);
    }

    //+++ get generalised volume as a function of distance and scaleheight for field +++//
    public static CubicSpline loadVGenD(String footprint, int fieldNum, String hemi, double H, int z_s) throws IOException{

	//+++ In case specified field is not in SSS, return empty cubic spline +++//
	CubicSpline empty = new CubicSpline(3);
	if(!Misc.checkField(fieldNum,hemi)) return empty;


	String hemisphere = (hemi.equals("N")) ? "North" : "South";

	//+++ Get file containing relevant data +++//
	File vgen = new File("/spare/SSS/Resources/SurveyVolume/Vgen_along_LOS/" +
			     "z_solar_"+z_s+"pc/" +
			     footprint+"/" +
			     hemisphere+"/" +
			     fieldNum+""+hemi+"/"+(int)H+"pc");

	BufferedReader in = new BufferedReader(new FileReader(vgen));

	//+++ Set up arrays to store all points +++//
	double[] d = new double[(Misc.lines(vgen) - 3)];      // (three header lines)
	double[] v = new double[d.length];

	//+++ skip headers... +++//
	in.readLine();in.readLine();in.readLine();

	//+++ ... then read in all generalised volume points +++//
	int tracker = 0;
	String data;

	while((data=in.readLine())!=null){
	    d[tracker]   = Double.parseDouble(Misc.columns(data,1));
	    v[tracker++] = Double.parseDouble(Misc.columns(data,2));
	}

	//+++ generate a CubicSpline object using these arrays +++//
	CubicSpline vgenDH = new CubicSpline(d,v);

	in.close();

	return vgenDH;

    }

}
