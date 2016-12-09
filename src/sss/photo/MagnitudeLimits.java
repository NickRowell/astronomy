package sss.photo;

import java.io.*;

/**
 *  Load all magnitude limits into array and wrap in a MagnitudeLimits object.
 *  There's probably a much more efficient way to read files into array but this works
 *  so leaving it as is for the time being.
 *
 * @author nickrowell
 */
public class MagnitudeLimits {

    //+++ Magnitude offsets used to set completeness limits +++//
    public static double SERCB_offset   = 0.4;
    public static double SERCR_offset   = 0.3;
    public static double ESOR_offset    = 0.7;
    public static double SERCI_offset   = 0.7;
    public static double POSSIIB_offset = 0.4;
    public static double POSSIIR_offset = 0.5;
    public static double POSSIII_offset = 0.7;
    public static double POSSIE_offset  = 1.9;    //1.0;

    public static double r2_max  = 19.75;         // both upper and lower proper motion catalogues limited to this.

    //+++ Bright magnitude limits +++//
    public static double b_min  = 12.0;
    public static double r1_min = 12.0;
    public static double r2_min = 12.0;
    public static double i_min  = 12.0;

    //+++ Data members for this class - array of magnitude limits for survey fields, given +++//
    //+++ the specified plate depth offsets.                                               +++//
    public static double[][][] magnitudeLimits = new double[900][2][4];

    //+++ Main constructor +++//
    public MagnitudeLimits() throws IOException{
        loadMagLimits(magnitudeLimits);
    }

    //+++ Data access methods +++//
    public double getBMax(int f, int h){ return magnitudeLimits[f][h][0];}
    public double getR1Max(int f, int h){ return magnitudeLimits[f][h][3];}
    public double getR2Max(int f, int h){ return magnitudeLimits[f][h][1];}
    public double getIMax(int f, int h){ return magnitudeLimits[f][h][2];}
    
    public double getBMin(int f, int h){ return b_min;}
    public double getR1Min(int f, int h){ return r1_min;}
    public double getR2Min(int f, int h){ return r2_min;}
    public double getIMin(int f, int h){ return i_min;}



    private static void loadMagLimits(double[][][] magLimits) throws IOException{

        for(int i = 1; i<900; i++){
            magLimits[i][0][0] = plateLim(i,"N","B")  - POSSIIB_offset;
            magLimits[i][0][1] = plateLim(i,"N","R2") - POSSIIR_offset;
            magLimits[i][0][2] = plateLim(i,"N","I")  - POSSIII_offset;
            magLimits[i][0][3] = plateLim(i,"N","R1") - POSSIE_offset;

            magLimits[i][1][0] = plateLim(i,"S","B") - SERCB_offset;
            magLimits[i][1][1] = plateLim(i,"S","R2") - SERCR_offset;
            magLimits[i][1][2] = plateLim(i,"S","I") - SERCI_offset;

            //fields 607->894 in south use POSSI-E plates
            if(i>=607) magLimits[i][1][3] = plateLim(i,"S","R1") - POSSIE_offset;
            else       magLimits[i][1][3] = plateLim(i,"S","R1") - ESOR_offset;

            //catalogues limited to R2 mag < 19.75 - reset limits if those given above are beyond this:
            if(magLimits[i][0][1] > r2_max){magLimits[i][0][1]=r2_max;}
            if(magLimits[i][1][1] > r2_max){magLimits[i][1][1]=r2_max;}

        }

	return;
    }



    public static double plateLim(int fieldNum, String hemisphere, String band) throws IOException{

	//+++ get column number for band in lookup table +++/
	int col = 0;
	if(band.equals("R1")){col = 2;}
	if(band.equals("R2")){col = 3;}
	if(band.equals("B")){col = 4;}
	if(band.equals("I")){col = 5;}

	//+++ initialise file containing lookup table of mag limits +++//
	String hemi = (hemisphere.equals("N")) ? "North" : "South";

	File limit = new File("/spare/SSS/Resources/LookupTables/PlateLimits/plateLimits"+hemi+".txt");
      	BufferedReader in = new BufferedReader(new FileReader(limit));

	String data="";

	//+++ Skip header line +++//
	in.readLine();

	while((data = in.readLine())!=null){
   	    if(Integer.parseInt(Misc.columns(data,1))==fieldNum){
		in.close();
		return Double.parseDouble(Misc.columns(data,col));
	    }
	}

	in.close();

	//+++ If field number/hemisphere combo does not exist, return flag value +++//
	return -9E9;

    }




}