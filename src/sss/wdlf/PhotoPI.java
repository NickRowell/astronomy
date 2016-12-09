/*
 * This class contains various methods to handle photometric parallaxes and
 * synthetic models.
 *
 *
 */


package Star;

import java.io.*;
import flanagan.interpolation.*;

import static Field.Misc.columns;
import Field.Misc;

public class PhotoPI{
 
    /**
     * Fraction n_He/n_H of helium atmosphere to hydrogen atmosphere white dwarfs in the Solar neighbourhood.
     * Value taken from Tremblay & Bergeron 2008. Used in assigning atmosphere fractions of stars.
     */
    public static double C = 0.5;

    //+++ Cut on residuals to best fit models +++//
    public static double chi2lim = 5.0;

    //+++ Get R_{59F} / B-R colour magnitude relations. Used in RPMD selection and atmosphere type weights +++//

    public static double[][] Htrack;
    public static double[][] Hetrack;

    static{
	try{getHtrack();}
	catch(Exception e){e.printStackTrace();}
        try{getHetrack();}
        catch(Exception e){e.printStackTrace();}
    }

    public static CubicSpline colMagH  = new CubicSpline(Htrack[1], Htrack[0]);
    public static CubicSpline colMagHe = new CubicSpline(Hetrack[1], Hetrack[0]);

    //+++ Blue and red edges of these tracks, to avoid trying to interpolate values outwith range of models +++//

    public static double redEdgeH   = colMagH.getXmax();  // about  1.677;
    public static double blueEdgeH  = colMagH.getXmin();  // about -0.448;
    public static double redEdgeHe  = colMagHe.getXmax(); // about  2.541;
    public static double blueEdgeHe = colMagHe.getXmin(); // about -0.265;

    //+++ Method to provide RPM threshold at a given stellar colour +++//
    public static double getRpmThreshold(double br, double vtan){

        //+++ If star is very blue, use bluest point of H col-mag relation to set RPM threshold +++//
        if ((br) < PhotoPI.blueEdgeH)
            return colMagH.interpolate(PhotoPI.blueEdgeH) + 5 * Math.log10(vtan) - 3.379;

        //+++ If star is very red, discard. WD locus merges with disk main sequence.   +++//
        //+++ Set large threshold so that no stars are selected from this colour range +++//
        else if ((br) > PhotoPI.redEdgeH)
            return 9E9;

        //+++ At intermediate colours, interpolate model cooling track to get RPM threshold +++//
        else
            return colMagH.interpolate(br) + 5 * Math.log10(vtan) - 3.379;
        
    }




    //+++ Get synthetic models interpolated at 10K intervals for use in photoparallaxes +++//

    private static String[] DAmodels;
    private static String[] DBmodels;

    static{
	try{getHmodels();}
	catch(Exception e){e.printStackTrace();}
	try{getHemodels();}
	catch(Exception e){e.printStackTrace();}
    }


    //+++ arrays to store the residuals for each model fit +++//
    private static double[] DAresiduals = new double[DAmodels.length];
    private static double[] DBresiduals = new double[DBmodels.length];






    /*
     * Notes on fitting routine.
     *
     * This routine will always find best fitting hydrogen and helium rich atmospheres, but not
     * necessarily the corresponding one-sigma boundary models. If data is a very poor fit to model
     * tracks, or is at either extreme, one-sigma boundary locating method will fail and return
     * "-1 -1 -1 -1" i.e. flag values instead of model details.
     *
     * Limit on chi^2 has been removed from this code so that models will always be fitted, and main program
     * can decide whether to use them or not based on chi^2 values returned.
     */


    public static String[] photoFitting(double B, double R2, double I){

	// set all residuals to zero:
	for(int i =0; i<DAmodels.length; i++){DAresiduals[i]=0;}
	for(int i =0; i<DBmodels.length; i++){DBresiduals[i]=0;}


	//+++ Read in photometric data for one star, and get variance +++//

	double starBR = B-R2;
	double starBI = B-I;
	double starRI = R2-I;

	//get colour variance at B mag:
	double starBRvar = Misc.photoError(B)*Misc.photoError(B);
	double starBIvar = starBRvar;
	double starRIvar = starBRvar;

	double modelBR,modelBI,modelRI;


	//+++ Fit against all DA and DB models +++//
	for(int i = 0; i<DAmodels.length; i++){

	    //Measure residuals to current DA model and store:
	    modelBR = Double.parseDouble(columns(DAmodels[i],7)) - Double.parseDouble(columns(DAmodels[i],8));
	    modelBI = Double.parseDouble(columns(DAmodels[i],7)) -Double.parseDouble(columns(DAmodels[i],10));
            modelRI = Double.parseDouble(columns(DAmodels[i],8)) - Double.parseDouble(columns(DAmodels[i],10));

	    /* Uncomment whichever these lines is desired. However, tests have shown that B-R,B-I gives
	     * better photometric parallaxes.
	     */

	    //+++ Fitting to B-R,B-I data +++//
	    DAresiduals[i] = (1.0/starBRvar)*(starBR-modelBR)*(starBR-modelBR)+(1.0/starBIvar)*(starBI-modelBI)*(starBI-modelBI);

            //+++ Fitting to B-R,R-I data +++//
            //DAresiduals[i] = (1.0/starBRvar)*(starBR-modelBR)*(starBR-modelBR) + (1.0/starRIvar)*(starRI-modelRI)*(starRI-modelRI);

	}
	for(int i = 0; i<DBmodels.length; i++){
	    //Measure residuals to current DB model and store:
	    modelBR = Double.parseDouble(columns(DBmodels[i],7)) - Double.parseDouble(columns(DBmodels[i],8));
	    modelBI = Double.parseDouble(columns(DBmodels[i],7)) - Double.parseDouble(columns(DBmodels[i],10));
            modelRI = Double.parseDouble(columns(DBmodels[i],8)) - Double.parseDouble(columns(DBmodels[i],10));

	    //+++ Fitting to B-R,B-I data +++//
	    DBresiduals[i] = (1.0/starBRvar)*(starBR-modelBR)*(starBR-modelBR) + (1.0/starBIvar)*(starBI-modelBI)*(starBI-modelBI);

            //+++ Fitting to B-R,R-I data +++//
            //DBresiduals[i] = (1.0/starBRvar)*(starBR-modelBR)*(starBR-modelBR) + (1.0/starRIvar)*(starRI-modelRI)*(starRI-modelRI);

	}


	//+++ Locate best fitting DA and DB model for each star +++//

	String bestFitDA="";   //stores best fitting models and corresponding residuals
	String bestFitDB="";
	String upperOneSigmaDA="-1\t-1\t-1\t-1\t-1\t-1\t-1\t-1\t-1\t-1\t-1";   //initialise to error values that will
	String lowerOneSigmaDA="-1\t-1\t-1\t-1\t-1\t-1\t-1\t-1\t-1\t-1\t-1";   //be reassigned on successful fitting.
	String upperOneSigmaDB="-1\t-1\t-1\t-1\t-1\t-1\t-1\t-1\t-1\t-1\t-1";
	String lowerOneSigmaDB="-1\t-1\t-1\t-1\t-1\t-1\t-1\t-1\t-1\t-1\t-1";


	//variables to store lowest chi^2 value for each model type, initialised to first value:
	double minChiDA = DAresiduals[0];
	double minChiDB = DBresiduals[0];

	//integers to record the array index of the best fitting models, initialised to first model:
	int bestFitDAmodel=0;
	int bestFitDBmodel=0;


	for(int i = 0; i<DAmodels.length; i++){
	    if(DAresiduals[i]<minChiDA){
		//if fit is better than current stored one, record this:
		minChiDA = DAresiduals[i];
		bestFitDAmodel = i;}
	}


	for(int i = 0; i<DBmodels.length; i++){
	    if(DBresiduals[i]<minChiDB){
		//found a fit that is better than current stored one, record this:
		minChiDB = DBresiduals[i];
		bestFitDBmodel = i;}
	}

	//Fitted WD models plus residuals. These are returned at end of routine, once one-sigma models have been found.
       	bestFitDA = DAmodels[bestFitDAmodel]+"\t"+DAresiduals[bestFitDAmodel];
	bestFitDB = DBmodels[bestFitDBmodel]+"\t"+DBresiduals[bestFitDBmodel];






	//+++ Now, read through model array again and locate models one-sigma away from best fit on both sides +++//


	double oneSigmaDA = minChiDA + 1;
	double oneSigmaDB = minChiDB + 1;

	int oneSigmaUpperDA = -1;
	int oneSigmaLowerDA = -1;
	int oneSigmaUpperDB = -1;
	int oneSigmaLowerDB = -1;

	/*  Read through array again and find one-sigma confidence intervals about best fit model:
	 *  models run from low to high temperature, first confidence boundary encountered
	 *  is lower T bound.
	 *  This method for locating confidence bound insists that 'previous point has residuals above confidence bound
	 *  AND current point has residuals below confidence bound'
	 */

	for(int i = 1; i<DAmodels.length; i++){

	    //get lower Teff bound from falling region of chi^2, Teff distribution
	    if((DAresiduals[i]<oneSigmaDA) && (DAresiduals[i-1]>oneSigmaDA) && (Double.parseDouble(columns(DAmodels[i],1))<Double.parseDouble(columns(DAmodels[bestFitDAmodel],1)))){

		lowerOneSigmaDA = DAmodels[i]+"\t"+DAresiduals[i];
	    }

	    //get upper Teff bound from rising region of chi^2, Teff distribution
	    if((DAresiduals[i]>oneSigmaDA) && (DAresiduals[i-1]<oneSigmaDA) && (Double.parseDouble(columns(DAmodels[i],1))>Double.parseDouble(columns(DAmodels[bestFitDAmodel],1)))){

		upperOneSigmaDA = DAmodels[i]+"\t"+DAresiduals[i];
	    }

	}




	for(int i = 1; i<DBmodels.length; i++){

	    //get lower Teff bound from falling region of chi^2, Teff distribution
	    if((DBresiduals[i]<oneSigmaDB) && (DBresiduals[i-1]>oneSigmaDB) && (Double.parseDouble(columns(DBmodels[i],1))<Double.parseDouble(columns(DBmodels[bestFitDBmodel],1)))){

		lowerOneSigmaDB = DBmodels[i]+"\t"+DBresiduals[i];
	    }

	    //get upper Teff bound from rising region of chi^2, Teff distribution
	    if((DBresiduals[i]>oneSigmaDB) && (DBresiduals[i-1]<oneSigmaDB) && (Double.parseDouble(columns(DBmodels[i],1))>Double.parseDouble(columns(DBmodels[bestFitDBmodel],1)))){

		upperOneSigmaDB = DBmodels[i]+"\t"+DBresiduals[i];
	    }
	}


	String[] output = {bestFitDA,lowerOneSigmaDA,upperOneSigmaDA,bestFitDB,lowerOneSigmaDB,upperOneSigmaDB};



	return output;


    }



    //+++ Returns errors obtained by averaging upper and lower confidence regions, and if one of these
    //+++ hasn't been found by model fitting procedure then the other is used.

    public static double[] photoParallax(double B, double R2, double I,String[] fittingOutput){


	double BDA = Double.parseDouble(columns(fittingOutput[0],7));     //absolute B Mag for best fit DA model
	double R2DA = Double.parseDouble(columns(fittingOutput[0],8));     //absolute R2 Mag for best fit DA model
	double IDA = Double.parseDouble(columns(fittingOutput[0],10));     //absolute I Mag for best fit DA model

	double BDB = Double.parseDouble(columns(fittingOutput[3],7));     //absolute B Mag for best fit DB model
	double R2DB = Double.parseDouble(columns(fittingOutput[3],8));     //absolute R2 Mag for best fit DB model
	double IDB = Double.parseDouble(columns(fittingOutput[3],10));     //absolute I Mag for best fit DB model

	//+++ Detect whether one-sigma models have been found or not using these flags:
	boolean da_upper = false, da_lower = false, db_upper = false, db_lower = false;

	//+++ If confidence boundary was not located, model parameters will be "-1\t-1\t-1\t-1\t-1\t-1\t-1\t-1\t-1\t-1\t-1" +++//
	//+++ Identify missed boundary by negative surface gravity

	double BDA_up=0,R2DA_up=0,IDA_up=0;
	double BDA_lo=0,R2DA_lo=0,IDA_lo=0;
	double BDB_up=0,R2DB_up=0,IDB_up=0;
	double BDB_lo=0,R2DB_lo=0,IDB_lo=0;

	//+++ upper one sigma H model +++//

	if(Double.parseDouble(columns(fittingOutput[2],2)) > 0.0){
	    BDA_up = Double.parseDouble(columns(fittingOutput[2],7));     //absolute mags for upper 1-sig DA model
	    R2DA_up = Double.parseDouble(columns(fittingOutput[2],8));
	    IDA_up = Double.parseDouble(columns(fittingOutput[2],10));
	    da_upper = true;
	}

	//+++ Lower one sigma H model +++//

	if(Double.parseDouble(columns(fittingOutput[1],2)) > 0.0){
	    BDA_lo = Double.parseDouble(columns(fittingOutput[1],7));     //absolute mags for lower 1-sig DA model
	    R2DA_lo = Double.parseDouble(columns(fittingOutput[1],8));
	    IDA_lo = Double.parseDouble(columns(fittingOutput[1],10));
	    da_lower = true;
	}

	//+++ Upper one sigma He model +++//

	if(Double.parseDouble(columns(fittingOutput[5],2)) > 0.0){
	    BDB_up = Double.parseDouble(columns(fittingOutput[5],7));     //absolute mags for upper 1-sig DB model
	    R2DB_up = Double.parseDouble(columns(fittingOutput[5],8));
	    IDB_up = Double.parseDouble(columns(fittingOutput[5],10));
	    db_upper = true;
	}

	//+++ Lower one sigma He model +++//

	if(Double.parseDouble(columns(fittingOutput[4],2)) > 0.0){
	    BDB_lo = Double.parseDouble(columns(fittingOutput[4],7));     //absolute mags for lower 1-sig DB model
	    R2DB_lo = Double.parseDouble(columns(fittingOutput[4],8));
	    IDB_lo = Double.parseDouble(columns(fittingOutput[4],10));
	    db_lower = true;
	}


	//+++ Single-passband photometric uncertainties +++//

        double sigma_bj = Misc.photoErrorB(B);
        double sigma_r2 = Misc.photoErrorR2(R2);
        double sigma_in = Misc.photoErrorI(I);



	//+++ Confidence limits in fitted absolute magnitudes +++//
	double sigma_BDA=0,sigma_R2DA=0,sigma_IDA=0;
	double sigma_BDB=0,sigma_R2DB=0,sigma_IDB=0;

	//+++ if both boundaries have been found, average confidence limits on absolute magnitudes +++//
	//+++ If only one has been found, just use this for overall uncerainty on fitted magnitude +++//

	if(da_upper&&da_lower){
	    sigma_BDA = (Math.abs(BDA-BDA_up) + Math.abs(BDA-BDA_lo))/2.0;
	    sigma_R2DA = (Math.abs(R2DA-R2DA_up) + Math.abs(R2DA-R2DA_lo))/2.0;
	    sigma_IDA = (Math.abs(IDA-IDA_up) + Math.abs(IDA-IDA_lo))/2.0;
	}
	else if(da_upper&&(!da_lower)){
            sigma_BDA = Math.abs(BDA-BDA_up);
            sigma_R2DA = Math.abs(R2DA-R2DA_up);
            sigma_IDA = Math.abs(IDA-IDA_up);
	}
        else if((!da_upper)&&da_lower){
            sigma_BDA = Math.abs(BDA-BDA_lo);
            sigma_R2DA = Math.abs(R2DA-R2DA_lo);
            sigma_IDA = Math.abs(IDA-IDA_lo);
	}
	else{
	    System.err.println("Neither upper nor lower one-sigma H photometric model found");
            sigma_BDA = sigma_R2DA = sigma_IDA = -9E9;
	}

	if(db_upper&&db_lower){
	    sigma_BDB = (Math.abs(BDB-BDB_up) + Math.abs(BDB-BDB_lo))/2.0;
	    sigma_R2DB = (Math.abs(R2DB-R2DB_up) + Math.abs(R2DB-R2DB_lo))/2.0;
	    sigma_IDB = (Math.abs(IDB-IDB_up) + Math.abs(IDB-IDB_lo))/2.0;
	}
	else if(db_upper&&(!db_lower)){
	    sigma_BDB = Math.abs(BDB-BDB_up);
	    sigma_R2DB = Math.abs(R2DB-R2DB_up);
	    sigma_IDB = Math.abs(IDB-IDB_up);
	}
	else if((!db_upper)&&db_lower){
	    sigma_BDB = Math.abs(BDB-BDB_lo);
	    sigma_R2DB = Math.abs(R2DB-R2DB_lo);
	    sigma_IDB = Math.abs(IDB-IDB_lo);
	}
	else{
	    System.err.println("Neither upper nor lower one-sigma He photometric model found");
            sigma_BDB = sigma_R2DB = sigma_IDB = -9E9;
	}




	//+++ Photometric distance to star, DA case +++//
	double dB_DA  = Math.pow(10,((B-BDA+5.0)/5.0));
	double dR2_DA = Math.pow(10,((R2-R2DA+5.0)/5.0));
	double dI_DA  = Math.pow(10,((I-IDA+5.0)/5.0));

	//+++ Uncertainties on these by combining photometric error and uncertainty in model fit +++//
	double sig_dB_DA  = (Math.log(10.0)*dB_DA/5.0)*Math.sqrt(sigma_bj*sigma_bj + sigma_BDA*sigma_BDA);
	double sig_dR2_DA = (Math.log(10.0)*dR2_DA/5.0)*Math.sqrt(sigma_r2*sigma_r2 + sigma_R2DA*sigma_R2DA);
	double sig_dI_DA  = (Math.log(10.0)*dI_DA/5.0)*Math.sqrt(sigma_in*sigma_in + sigma_IDA*sigma_IDA);


	//...minimum variance combination of individual distance estimates, using mean upper/lower variance:
	double dDA = ((dB_DA/(sig_dB_DA*sig_dB_DA))+
		      (dR2_DA/(sig_dR2_DA*sig_dR2_DA))+
		      (dI_DA/(sig_dI_DA*sig_dI_DA)))
	    /
	    ((1.0/(sig_dB_DA*sig_dB_DA))+
	     (1.0/(sig_dR2_DA*sig_dR2_DA))+
	     (1.0/(sig_dI_DA*sig_dI_DA)));

	//confidence boundaries on this distance:

	double sig_dDA = Math.sqrt(1.0
				      /
				      ((1.0/(sig_dB_DA*sig_dB_DA))+
				       (1.0/(sig_dR2_DA*sig_dR2_DA))+
				       (1.0/(sig_dI_DA*sig_dI_DA))));


	//+++ Photometric distance to star, DB case +++//
	double dB_DB  = Math.pow(10,((B-BDB+5.0)/5.0));
	double dR2_DB = Math.pow(10,((R2-R2DB+5.0)/5.0));
	double dI_DB  = Math.pow(10,((I-IDB+5.0)/5.0));

        //+++ Uncertainties on these by combining photometric error and uncertainty in model fit +++//
 	double sig_dB_DB  = (Math.log(10.0)*dB_DB/5.0)*Math.sqrt(sigma_bj*sigma_bj + sigma_BDB*sigma_BDB);
	double sig_dR2_DB = (Math.log(10.0)*dR2_DB/5.0)*Math.sqrt(sigma_r2*sigma_r2 + sigma_R2DB*sigma_R2DB);
	double sig_dI_DB  = (Math.log(10.0)*dI_DB/5.0)*Math.sqrt(sigma_in*sigma_in + sigma_IDB*sigma_IDB);


	//...minimum variance combination of individual estimates, using mean upper/lower variance:
	double dDB = ((dB_DB/(sig_dB_DB*sig_dB_DB))+
		      (dR2_DB/(sig_dR2_DB*sig_dR2_DB))+
		      (dI_DB/(sig_dI_DB*sig_dI_DB)))
	    /
	    ((1.0/(sig_dB_DB*sig_dB_DB))+
	     (1.0/(sig_dR2_DB*sig_dR2_DB))+
	     (1.0/(sig_dI_DB*sig_dI_DB)));

	//upper and lower confidence boundaries on this distance:

	double sig_dDB = Math.sqrt(1.0
				      /
				      ((1.0/(sig_dB_DB*sig_dB_DB))+
				       (1.0/(sig_dR2_DB*sig_dR2_DB))+
				       (1.0/(sig_dI_DB*sig_dI_DB))));




		    double[] output = {dDA,sig_dDA,dDB,sig_dDB};

		    return output;


    }



    //+++ New weights for H and He atmosphere types +++//

    public static double getHeWeight(double BR){

	//+++ check if BR colour is within range of H and He models +++//

	if(BR<blueEdgeH){return 0.0;}     // Set very blue objects to pure H
	if(BR<blueEdgeHe){return 0.0;}

	if(BR>redEdgeH){return 1.0;}      // As no objects redder than redEdgeH are selected by survey, this bit of code should
	if(BR>redEdgeHe){return 1.0;}     // never be reached. Included for completeness.


	if(BR < 0.8) {return 0.0;}        // Bluer than ~0.8, atmosphere type has little effect, so set w_He = 0

	//+++ Get abolute R_{59F} for each atmosphere type +++//

	double R_He = colMagHe.interpolate(BR);
	double R_H  = colMagH.interpolate(BR);


	double w_he = 1.0 / (1 + (1.0/C) * Math.pow(10,(3.0/5.0)*(R_He - R_H)));

	return w_he;

    }



    //+++ Get appropriate colour-magnitude relations for doing RPM selection +++//

    public static void getHtrack() throws IOException{

	File model = new File("/spare/SSS/Resources/PhotoParallaxes/ModelRepository/RPMselection/Htrack");
      	BufferedReader CMR = new BufferedReader(new FileReader(model));

	Htrack = new double[2][Misc.lines(model,"#")];
	String data;
	int element=0;
	//read in colour magnitude relation...
	while((data=CMR.readLine())!=null){
	    if(data.substring(0,1).equals("#")){}  //avoid commented lines at start of file.
	    else{
		//B magnitude:
		Htrack[0][element] = Double.parseDouble(columns(data,7));
		//B-R2 colour
		Htrack[1][element] = Double.parseDouble(columns(data,7))-Double.parseDouble(columns(data,8));
		element++;
	    }
	}

	return;
    }

    public static void getHetrack() throws IOException{

	File model = new File("/spare/SSS/Resources/PhotoParallaxes/ModelRepository/RPMselection/Hetrack");
        
      	BufferedReader CMR = new BufferedReader(new FileReader(model));

	Hetrack = new double[2][Misc.lines(model,"#")];

	String data;
	int element=0;
	//read in colour magnitude relation...
	while((data=CMR.readLine())!=null){
	    if(data.substring(0,1).equals("#")){}  //avoid commented lines at start of file.
	    else{
		//B magnitude:
		Hetrack[0][element] = Double.parseDouble(columns(data,7));
		//B-R2 colour
		Hetrack[1][element] = Double.parseDouble(columns(data,7))-Double.parseDouble(columns(data,8));
		element++;
	    }
	}

	return ;
    }

    public static void getHmodels() throws IOException{

	File Table_DA = new File("/spare/SSS/Resources/PhotoParallaxes/ModelRepository/PhotoDistances/Htrack");
	BufferedReader DAin = new BufferedReader(new FileReader(Table_DA));
	int NumDA = 10850;
	DAmodels = new String[NumDA];

	String DA;
	int element =0;
	while((DA=DAin.readLine())!=null){DAmodels[element]=DA;element++;}
	return;
    }

    public static void getHemodels() throws IOException{

	File Table_DB = new File("/spare/SSS/Resources/PhotoParallaxes/ModelRepository/PhotoDistances/Hetrack");
	BufferedReader DBin = new BufferedReader(new FileReader(Table_DB));
	int NumDB = 2650;
	DBmodels = new String[NumDB];
	String DB;
	int element =0;
	while((DB=DBin.readLine())!=null){DBmodels[element]=DB;element++;}
	return;
    }






    //corrects to bolometric magnitude for arbitrary input absolute magnitude for arbitrary input band, for a range
    //of gravities and for either atmosphere type:

    public static double getMbol(String type, double gravity, String band, double mag) throws IOException{

	//construct file containing necessary models:
	File models = new File("/spare/SSS/Resources/PhotoParallaxes/Tables/Table_"+type);
	//open reader on this new file:
	BufferedReader in = new BufferedReader(new FileReader(models));
	//read through file and count number of lines corresponding to desired gravity model:
	int N = 0;
	String line;
	in.readLine(); in.readLine();  //skip header
	while((line=in.readLine())!=null){
	    try{
	    if(Double.parseDouble(columns(line,2))==gravity){N+=1;}
	    }
	    catch(StringIndexOutOfBoundsException sioobe){} //to avoid screwups reading blank lines between gravity models
	}

	//initialise two arrays of correct length to store BC and M_band
	double Mbol[]  = new double[N];
	double Mband[] = new double[N];

	//now get column number for absolute magnitude in specified band:
	int column=0;
	if(band.equals("V")){column=6;}
	if(band.equals("B")){column=7;}
	if(band.equals("R2")){column=8;}
	if(band.equals("R1")){column=9;}
	if(band.equals("I")){column=10;}

	//re-open file reader at start of file:
	in = new BufferedReader(new FileReader(models));
	in.readLine(); in.readLine();  //skip header

	//read through and store bolometric correction as a function of magnitude in data arrays:
	int counter=0;
	while((line=in.readLine())!=null){
	    try{
	    if(Double.parseDouble(columns(line,2))==gravity){
		Mband[counter] = Double.parseDouble(columns(line,column));
		Mbol[counter]    = Double.parseDouble(columns(line,4));
		counter += 1;
	    }
	    }
	    catch(StringIndexOutOfBoundsException sioobe){} //to avoid screwups reading blank lines between gravity models


	}

	//now, initialise new CubicSpline object using these data arrays:
	CubicSpline mbol = new CubicSpline(Mband,Mbol);

	//interpolate BC at desired magnitude and return value:

	return mbol.interpolate(mag);
    }


    // Method returns mean B,R1,R2,I and variance B,R1,R2,I for a specified model type and gravity,
    // for a specified M_{bol} bin centre and width.

    public static double[] getMeanMags(String binning, double centre, String type, String gravity) throws IOException{

	File mean = new File("/spare/SSS/Resources/PhotoParallaxes/ModelRepository/MeanAbsoluteMags/"+binning+"_mag_bins/"+type+"_g="+gravity);

	//+++ set up reader on file "mean" +++//

	BufferedReader in = new BufferedReader(new FileReader(mean));

	String data;

	//+++ skip headers +++//

	while((data = in.readLine()).substring(0,1).equals("#")){}
	do{
	    if(Double.parseDouble(columns(data,1))==centre){break;}
	}
	while((data = in.readLine())!=null);

	double mags[] = {Double.parseDouble(columns(data,2)),
			 Double.parseDouble(columns(data,4)),
			 Double.parseDouble(columns(data,3)),
			 Double.parseDouble(columns(data,5)),
			 Double.parseDouble(columns(data,6)),
			 Double.parseDouble(columns(data,8)),
			 Double.parseDouble(columns(data,7)),
			 Double.parseDouble(columns(data,9))};

	return mags;

    }


}
