package Calibration;


import java.io.*;
import flanagan.interpolation.*;
import Field.*;
import java.text.DecimalFormat;

/**
 *  This class integrates WD models over given bins in bolometric magnitude in order to estimate
 *  the mean absolute magnitude in each band in each bolometric magnitude bin. These are used
 *  in decomposed luminosity function calculations.
 *
 *  @author nickrowell
 */
public class MeanAbsoluteMagnitudes {


    public static void main(String args[]) throws IOException{

	//+++ integration parameters +++//

	double deltaM = 0.25;          // Bolometric mag bin width

	double N_strips = 1E5;        // number of trapezium strips to break each bin down into
	                              // (must be EVEN)

	double h = deltaM/N_strips;   // strip width used in trapezium rule


	String record;                    // sequentially store lines from table in String 'record'
	String[] table = new String[100]; // store all lines for desired model in String array 'table'
	int tabNum = 0;                   // index array table
	DecimalFormat xpointxxx = new DecimalFormat("0.000");

	//+++ Loop over model atmosphere type and magnitude range +++//
	String type=""; String logg=""; double M_min=-99; double M_max=-99;

	for(int i = 1; i< 12; i++){

	    switch(i){
	    case 1:  type = "DA"; logg = "7.0"; M_min = -0.5; M_max = 18.5; break;
	    case 2:  type = "DA"; logg = "7.5"; M_min = 0.5;  M_max = 19.5; break;
	    case 3:  type = "DA"; logg = "8.0"; M_min = 1.5;  M_max = 19.5; break;
	    case 4:  type = "DA"; logg = "8.5"; M_min = 2.5;  M_max = 20.5; break;
	    case 5:  type = "DA"; logg = "9.0"; M_min = 3.5;  M_max = 21.5; break;
	    case 6:  type = "DA"; logg = "9.5"; M_min = 4.5;  M_max = 22.5; break;
	    case 7:  type = "DB"; logg = "7.0"; M_min = 5.5;  M_max = 14.5; break;
	    case 8:  type = "DB"; logg = "7.5"; M_min = 6.5;  M_max = 15.5; break;
	    case 9:  type = "DB"; logg = "8.0"; M_min = 7.5;  M_max = 15.5; break;
	    case 10: type = "DB"; logg = "8.5"; M_min = 8.5;  M_max = 16.5; break;
	    case 11: type = "DB"; logg = "9.0"; M_min = 9.5;  M_max = 17.5; break;
	    }



	    //+++ Set up input file for reading +++//
	    File data = new File("/spare/SSS/Resources/PhotoParallaxes/Tables/Table_"+type);
	    BufferedReader in = new BufferedReader(new FileReader(data));

	    //+++ Set up output file for writing +++//
	    File output = new File("/spare/SSS/Resources/PhotoParallaxes/ModelRepository/MeanAbsoluteMags/"+deltaM+"_mag_bins/"+type+"_g="+logg);
	    BufferedWriter out = new BufferedWriter(new FileWriter(output));
	    out.write("#Mean and variance of absolute magnitudes in supercosmos passbands for "+type+" type\n#atmospheres, integrated over bolometric magnitude bins of width "+deltaM+".\n#\n#\n#bin\n#centre\t<B>\t<R59F>\t<R63F>\t<I>\tvar<B>\tvar<R2>\tvar<R1>\tvar<I>");
	    out.newLine();
	    out.flush();

	    //+++ Read in data from input file +++//
	    while((record = in.readLine()).substring(0,1).equals("#")){} //skip comments

	    tabNum=0; //reset array index counter

	    do{
		try{
		    if(Misc.columns(record,2).equals(logg)){
			table[tabNum] = record;
			tabNum+=1;}
		}
		catch(Exception e){}
	    }
	    while((record = in.readLine())!=null);


	    // All lines recorded, number of lines stored in 'tabNum' integer.
	    // Create arrays of this length and get absolute magnitudes


	    //+++ Initialise arrays to store (Mbol,B/R1/R2/I) points +++//
	    double[] mbol = new double[tabNum];

	    double[] bj   = new double[tabNum];
	    double[] r59f = new double[tabNum];
	    double[] r63f = new double[tabNum];
	    double[] ivn  = new double[tabNum];

	    double[] bj2   = new double[tabNum];
	    double[] r59f2 = new double[tabNum];
	    double[] r63f2 = new double[tabNum];
	    double[] ivn2  = new double[tabNum];


	    //+++ Get all values +++//
	    for(int j = 0; j<tabNum; j++){

		mbol[j] = Double.parseDouble(Misc.columns(table[j],4));
		bj[j]   = Double.parseDouble(Misc.columns(table[j],7));
		r59f[j] = Double.parseDouble(Misc.columns(table[j],8));
		r63f[j] = Double.parseDouble(Misc.columns(table[j],9));
		ivn[j]  = Double.parseDouble(Misc.columns(table[j],10));

		bj2[j]   = bj[j]*bj[j];
		r59f2[j] = r59f[j]*r59f[j];
		r63f2[j] = r63f[j]*r63f[j];
		ivn2[j]  = ivn[j]*ivn[j];

	    }


	    //+++ Initialise CubicSpline objects. These will be used to interpolate functions +++//
	    CubicSpline B  = new CubicSpline(mbol,bj);
	    CubicSpline R1 = new CubicSpline(mbol,r63f);
	    CubicSpline R2 = new CubicSpline(mbol,r59f);
	    CubicSpline I  = new CubicSpline(mbol,ivn);

	    // functions squared:
	    CubicSpline B2  = new CubicSpline(mbol,bj2);
	    CubicSpline R12 = new CubicSpline(mbol,r63f2);
	    CubicSpline R22 = new CubicSpline(mbol,r59f2);
	    CubicSpline I2  = new CubicSpline(mbol,ivn2);


	    // Test validity of interpolation
	    for(double m = 0; m< 19.0; m+=0.01){
                try{
                    System.out.println(""+m+"\t"+B.interpolate(m)+"\t"+R1.interpolate(m)+"\t"+R2.interpolate(m)+"\t"+I.interpolate(m));
                }
                catch(IllegalArgumentException iae){}

	    }
	    System.out.println("\n\n");

	    //+++                                                                                      +++//
	    //+++ B(Mbol), R(Mbol) etc functions stored in CubicSpline objects. Can now integrate them +++//
	    //+++                                                                                      +++//



	//+++ Loop over Mbol bins +++//


	for(double M = M_min; M<M_max; M+=deltaM){     // loop over Mbol bins

	    double bin_centre = M + (deltaM/2.0);  // for output file

	    // Integrate over current bin.
	    // Glyn James pages 570-2 describe the technique used here:
	    // 'Richardson's extrapolation', page 573

	    // For estimating mean:
	    double B_T_h   = 0;
	    double B_T_2h  = 0;
	    double R1_T_h  = 0;
	    double R1_T_2h = 0;
	    double R2_T_h  = 0;
	    double R2_T_2h = 0;
	    double I_T_h   = 0;
	    double I_T_2h  = 0;


	    for(double m = M; m<(M+deltaM-h); m+=2*h){


		// contributions to T(2h) sums:

		B_T_2h  += (B.interpolate(m)  + B.interpolate(m+(2*h)))*h;
		R1_T_2h += (R1.interpolate(m) + R1.interpolate(m+(2*h)))*h;
		R2_T_2h += (R2.interpolate(m) + R2.interpolate(m+(2*h)))*h;
		I_T_2h  += (I.interpolate(m)  + I.interpolate(m+(2*h)))*h;


		// contributions to T(h) sums - split region in half:

		B_T_h  += (B.interpolate(m)    + B.interpolate(m+h))*(h/2.0);
		B_T_h  += (B.interpolate(m+h)  + B.interpolate(m+(2*h)))*(h/2.0);
		R1_T_h += (R1.interpolate(m)   + R1.interpolate(m+h))*(h/2.0);
		R1_T_h += (R1.interpolate(m+h) + R1.interpolate(m+(2*h)))*(h/2.0);
		R2_T_h += (R2.interpolate(m)   + R2.interpolate(m+h))*(h/2.0);
		R2_T_h += (R2.interpolate(m+h) + R2.interpolate(m+(2*h)))*(h/2.0);
		I_T_h  += (I.interpolate(m)    + I.interpolate(m+h))*(h/2.0);
		I_T_h  += (I.interpolate(m+h)  + I.interpolate(m+(2*h)))*(h/2.0);


	    }


	    // For estimating mean of square:
	    double B2_T_h   = 0;
	    double B2_T_2h  = 0;
	    double R12_T_h  = 0;
	    double R12_T_2h = 0;
	    double R22_T_h  = 0;
	    double R22_T_2h = 0;
	    double I2_T_h   = 0;
	    double I2_T_2h  = 0;


	    for(double m = M; m<(M+deltaM-h); m+=2*h){


		// contributions to T(2h) sums:

		B2_T_2h  += (B2.interpolate(m)  + B2.interpolate(m+(2*h)))*h;
		R12_T_2h += (R12.interpolate(m) + R12.interpolate(m+(2*h)))*h;
		R22_T_2h += (R22.interpolate(m) + R22.interpolate(m+(2*h)))*h;
		I2_T_2h  += (I2.interpolate(m)  + I2.interpolate(m+(2*h)))*h;

		// contributions to T(h) sums - split region in half:

		B2_T_h  += (B2.interpolate(m)     + B2.interpolate(m+h))*(h/2.0);
		B2_T_h  += (B2.interpolate(m+h)  + B2.interpolate(m+(2*h)))*(h/2.0);
		R12_T_h += (R12.interpolate(m)    + R12.interpolate(m+h))*(h/2.0);
		R12_T_h += (R12.interpolate(m+h)+ R12.interpolate(m+(2*h)))*(h/2.0);
		R22_T_h += (R22.interpolate(m)    + R22.interpolate(m+h))*(h/2.0);
		R22_T_h += (R22.interpolate(m+h)+ R22.interpolate(m+(2*h)))*(h/2.0);
		I2_T_h  += (I2.interpolate(m)      + I2.interpolate(m+h))*(h/2.0);
		I2_T_h  += (I2.interpolate(m+h)  + I2.interpolate(m+(2*h)))*(h/2.0);

	    }



	    //Richardson's extrapolation:

	    double mean_B   = (1.0/3.0) * (4.0*B_T_h  - B_T_2h);
	    double mean_R1  = (1.0/3.0) * (4.0*R1_T_h - R1_T_2h);
	    double mean_R2  = (1.0/3.0) * (4.0*R2_T_h - R2_T_2h);
	    double mean_I   = (1.0/3.0) * (4.0*I_T_h  - I_T_2h);

	    double mean_B2  = (1.0/3.0) * (4.0*B2_T_h  - B2_T_2h);
	    double mean_R12 = (1.0/3.0) * (4.0*R12_T_h - R12_T_2h);
	    double mean_R22 = (1.0/3.0) * (4.0*R22_T_h - R22_T_2h);
	    double mean_I2  = (1.0/3.0) * (4.0*I2_T_h  - I2_T_2h);


	    //Mean value theorem:

	    mean_B   *= (1.0/deltaM);
	    mean_R1  *= (1.0/deltaM);
	    mean_R2  *= (1.0/deltaM);
	    mean_I   *= (1.0/deltaM);

	    mean_B2  *= (1.0/deltaM);
	    mean_R12 *= (1.0/deltaM);
	    mean_R22 *= (1.0/deltaM);
	    mean_I2  *= (1.0/deltaM);




	    // Variance = mean of square minus square of mean

	    double varB  = mean_B2  - mean_B*mean_B;
	    double varR1 = mean_R12 - mean_R1*mean_R1;
	    double varR2 = mean_R22 - mean_R2*mean_R2;
	    double varI  = mean_I2  - mean_I*mean_I;


	    //System.out.println("\nmean of square = "+mean_B2+"\tvariance = "+varB+"\tmag = "+M+"\nsquare of mean = "+mean_B*mean_B);

	    //+++ write out current point to file +++//

	    out.write(xpointxxx.format(bin_centre) +"\t"+xpointxxx.format(mean_B)+"\t"+xpointxxx.format(mean_R2)+"\t"+xpointxxx.format(mean_R1)+"\t"+xpointxxx.format(mean_I)+"\t"+xpointxxx.format(varB)+"\t"+xpointxxx.format(varR2)+"\t"+xpointxxx.format(varR1)+"\t"+xpointxxx.format(varI));

	    out.newLine();
	    out.flush();

	}



	}





    }





}
