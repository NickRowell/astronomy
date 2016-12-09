package sss.wdlf.util;

import java.io.*;
import Star.*;
import static Field.Misc.columns;
import Field.Misc;
import Survey.Survey;

/**
 *
 * @author nickrowell
 */
public class PhotometricParallax {

    public static void main(String args[]) throws Exception, IOException{



	//Initialise input files of BRL cool white dwarfs and their SSS counterparts:
	File BRL = new File("src/Data/BRL2001_cat");
	File SSS = new File("src/Data/BRL2001_ids");

	// ^ these catalogues have a one-to-one correspondence between lines.

     	BufferedReader brl = new BufferedReader(new FileReader(BRL));
     	BufferedReader sss = new BufferedReader(new FileReader(SSS));

	//output files
	BufferedWriter gnuplot = new BufferedWriter(new FileWriter("../../Resources/PhotoParallaxes/AccuracyTest/commands.p"));
	BufferedWriter out = new BufferedWriter(new FileWriter("../../Resources/PhotoParallaxes/AccuracyTest/output"));


	//+++ Two arrays for storing trig and phot distances and errors +++//

	double[][] trig_tmp = new double[200][2];
	double[][] phot_tmp = new double[200][2];

        double Mv=0,BR_model=0,BI_model=0,BR_model_up=-1.0; double BI_model_up=-1.0; double BR_model_lo=-1.0; double BI_model_lo=-1.0;
	int np=0;

	boolean fit = false;

	String trig="", phot="", wdname="";


	//skip header in BRL2001 catalogue:
	for(int i = 0; i< 37; i++){trig=brl.readLine();}

     	HighMuStar star;

	//read in one line at a time and get trig and photometric parallaxes:

	while((phot = sss.readLine()) != null){

	    if(phot.substring(0,1).equals("#"))	trig = brl.readLine(); //indicates unmatched star

	    else if(!check(star = new HighMuStar(phot))) trig = brl.readLine();  // SSS star fails morphology constraints

 	    else{
	    //+++ Get corresponding BLR2001 data +++//

	    trig = brl.readLine();

	    double PI     = Double.parseDouble(columns(trig,3))/1000.0; //in arcseconds
	    double sig_PI = Double.parseDouble(columns(trig,4))/1000.0; //in arcseconds
            double trig_distance       = 1.0/PI;
	    double trig_distance_error = sig_PI/(PI*PI);

	    trig_tmp[np][0] = trig_distance;
	    trig_tmp[np][1] = trig_distance_error;


	    //+++ Get photometric parallax by fitting models according to standard method used in survey +++//

	    star.fitSyntheticColours();

	    //+++ Is star H or He atmosphere objects according to BLR2001? +++//
	    double chi2H  = Double.parseDouble(columns(star.bestFitH,12));
            double chi2He = Double.parseDouble(columns(star.bestFitHe,12));

            double chi2=0;

	    fit = false;   // Note whether star passes fit OK

	    if(getAtmType(columns(trig,1)).equals("H")&&(chi2H<5.0)){
	    

                chi2 = chi2H;

		//+++ Assign hydrogen rich model fit +++//
    	        phot_tmp[np][0] = star.dH;
	        phot_tmp[np][1] = star.sig_dH;

	        Mv = Double.parseDouble(columns(star.bestFitH,6));

	        BR_model=Double.parseDouble(columns(star.bestFitH,7))-Double.parseDouble(columns(star.bestFitH,8));
	        BI_model=Double.parseDouble(columns(star.bestFitH,7))-Double.parseDouble(columns(star.bestFitH,10));
	        try{
	        BR_model_up=Double.parseDouble(columns(star.oneSigmaUpperH,7))-Double.parseDouble(columns(star.oneSigmaUpperH,8));
	        BI_model_up=Double.parseDouble(columns(star.oneSigmaUpperH,7))-Double.parseDouble(columns(star.oneSigmaUpperH,10));}
	        catch(NumberFormatException nfe1){}
	        try{
	        BR_model_lo=Double.parseDouble(columns(star.oneSigmaLowerH,7))-Double.parseDouble(columns(star.oneSigmaLowerH,8));
	        BI_model_lo=Double.parseDouble(columns(star.oneSigmaLowerH,7))-Double.parseDouble(columns(star.oneSigmaLowerH,10));}
	        catch(NumberFormatException nfe2){}

		fit = true;

	    }


            if(getAtmType(columns(trig,1)).equals("He")&&(chi2He<5.0)){
            

                chi2 = chi2He;

		phot_tmp[np][0] = star.dHe;
		phot_tmp[np][1] = star.sig_dHe;

		Mv = Double.parseDouble(columns(star.bestFitHe,6));

		BR_model=Double.parseDouble(columns(star.bestFitHe,7))-Double.parseDouble(columns(star.bestFitHe,8));
		BI_model=Double.parseDouble(columns(star.bestFitHe,7))-Double.parseDouble(columns(star.bestFitHe,10));
	    	try{
		BR_model_up=Double.parseDouble(columns(star.oneSigmaUpperHe,7))-Double.parseDouble(columns(star.oneSigmaUpperHe,8));
		BI_model_up=Double.parseDouble(columns(star.oneSigmaUpperHe,7))-Double.parseDouble(columns(star.oneSigmaUpperHe,10));}
		catch(NumberFormatException nfe1){}
		try{
		BR_model_lo=Double.parseDouble(columns(star.oneSigmaLowerHe,7))-Double.parseDouble(columns(star.oneSigmaLowerHe,8));
		BI_model_lo=Double.parseDouble(columns(star.oneSigmaLowerHe,7))-Double.parseDouble(columns(star.oneSigmaLowerHe,10));}
		catch(NumberFormatException nfe2){}

		fit = true;

	    }

	    if(fit){

	    //+++ Output - graphical representation of fit to synthetic models, and correlation of photometric and trig distances +++//

	    System.out.println(trig_tmp[np][0]+"\t"+phot_tmp[np][0]+"\t"+columns(trig,1) + "\t" + chi2);

	    out.write(""+phot_tmp[np][0]+"\t"
		        +trig_tmp[np][0]+"\t"
		        +phot_tmp[np][1]+"\t"
		        +trig_tmp[np][1]+"\t"
		        +(star.b-star.r2)+"\t"+(star.b-star.i)+"\t"+BR_model+"\t"+BI_model+"\t"
		        +BR_model_up+"\t"+BI_model_up+"\t"+BR_model_lo+"\t"
		        +BI_model_lo+"\t"+Misc.photoError(star.b)+"\t"+Misc.photoError(star.b));

	    out.newLine();
	    out.newLine();
	    out.newLine();
	    out.flush();

	    //+++ Gnuplot script for plotting fits etc. +++//

	    gnuplot.write("set size square 0.6,0.6");	    gnuplot.newLine();
	    gnuplot.write("set key noautotitles");	    gnuplot.newLine();
	    gnuplot.write("set multiplot");	            gnuplot.newLine();
	    gnuplot.write("set origin 0,0.2");	            gnuplot.newLine();
	    gnuplot.write("set key top left");	            gnuplot.newLine();
	    gnuplot.write("set yrange [-1:5]");	            gnuplot.newLine();
	    gnuplot.write("set ylabel \'B-I\'");	    gnuplot.newLine();
	    gnuplot.write("set bar 0");	                    gnuplot.newLine();
	    gnuplot.write("set xrange[-1:5]");	            gnuplot.newLine();
	    gnuplot.write("set xlabel \'B-R\'");	    gnuplot.newLine();
	    gnuplot.write("plot \'output\' index "+np+" using 5:6:13:14 with xyerrorbars title \'WD"+columns(trig,1)+", Mv = "+Mv+"\',\\");	    gnuplot.newLine();
	    gnuplot.write("\'output\' index "+np+" using 7:8 with points ps 1 title \'model\',\\");	    gnuplot.newLine();
	    gnuplot.write("\'output\' index "+np+" using 9:10 with points ps 1,\\");	                    gnuplot.newLine();
	    gnuplot.write("\'output\' index "+np+" using 11:12 with points ps 1,\\");	            gnuplot.newLine();
	    gnuplot.write("\'/spare/SSS/Resources/PhotoParallaxes/ModelRepository/PhotoDistances/Htrack\' using ($7-$8):($7-$10) with dots,\\");	    gnuplot.newLine();
	    gnuplot.write("\'/spare/SSS/Resources/PhotoParallaxes/ModelRepository/PhotoDistances/Hetrack\' using ($7-$8):($7-$10) with dots");	    gnuplot.newLine();
	    gnuplot.write("set origin 0.4,0.2");	    gnuplot.newLine();
	    gnuplot.write("set xlabel \'d_{phot}\'");	    gnuplot.newLine();
	    gnuplot.write("set ylabel \'d_{trig}\'");	    gnuplot.newLine();
	    gnuplot.write("set yrange [0:200]");	    gnuplot.newLine();
	    gnuplot.write("set xrange [0:200]");	    gnuplot.newLine();
	    gnuplot.write("f(x) = x");	                    gnuplot.newLine();
	    gnuplot.write("plot \'output\' index "+np+" using 1:2:3:4 with xyerrorbars notitle,f(x)");	    gnuplot.newLine();
	    gnuplot.write("unset multiplot");	            gnuplot.newLine();
	    gnuplot.write("pause -1 \"\\n\\rpush return to continue...\\n\"");	    gnuplot.newLine();
	    gnuplot.newLine();
	    gnuplot.flush();

	    np++;

	    }

	    }

	}


	//+++ Read partially filled distance arrays into new arrays of correct size +++//

	double[] trig_d = new double[np];
	double[] phot_d = new double[np];

	double mean_trig = 0;
	double mean_phot = 0;
	double mean_trph = 0;

	for(int i = 0; i < np; i++){
	    trig_d[i] = trig_tmp[i][0];
	    phot_d[i] = phot_tmp[i][0];

	    mean_trig += trig_d[i];
	    mean_phot += phot_d[i];
	    mean_trph += trig_d[i]/phot_d[i];


	}


	//+++ Get correlation coefficient for sample +++//
	mean_trig /= (double)np;
	mean_phot /= (double)np;

	double cov   = 0;
	double var_t = 0;
	double var_p = 0;

	for(int i = 0; i < np; i++){

	    cov   += (trig_d[i] - mean_trig)*(phot_d[i] - mean_phot);
	    var_t += (trig_d[i] - mean_trig)*(trig_d[i] - mean_trig);
	    var_p += (phot_d[i] - mean_phot)*(phot_d[i] - mean_phot);

	}

	double r = cov/Math.sqrt(var_t * var_p);


	//+++ Get sample standard deviation of ratio of trig to photo distances +++//

	//+++ Should they be scaled by uncertainty on trig/phot? +++//

	mean_trph /= (double)np;

	double std_trph = 0;

	for(int i = 0; i < np; i++) std_trph += ((trig_d[i]/phot_d[i]) - mean_trph) * ((trig_d[i]/phot_d[i]) - mean_trph);

	std_trph = (1.0/((double)np - 1.0))* std_trph;
	std_trph = Math.sqrt(std_trph);

	//+++ Get rms dispersion +++//

	double rms = 0;

        for(int i = 0; i < np; i++) rms += (trig_d[i] - phot_d[i]) * (trig_d[i] - phot_d[i]);

	rms = rms/(double)np;
	rms = Math.sqrt(rms);


	//+++ Print out statistics +++//

	System.out.println("Correlation coefficient = "+r);
	System.out.println();
	System.out.println("trig/photo ratio: Mean = "+mean_trph+"\tDeviation = "+std_trph);
	System.out.println();
	System.out.println("Total stars = "+np);
	System.out.println();
	System.out.println("rms = "+rms);
    }





    public static boolean check(HighMuStar star){

	//+++ Restrict on blend, quality and number of observations +++//

	    if((star.qb>127)||(star.qr1>127)||(star.qr2>127)||(star.qi>127)){return false;}
	    else if((star.bb!=0)||(star.br1!=0)||(star.br2!=0)||(star.bi!=0)){return false;}
	    else if((star.b>90)||(star.r2>90)||(star.i>90)||(star.r1>90)){return false;}
     	    else{}

	    return true;
    }

    public static String getAtmType(String name){

        if(name.equals("0000-345")) return "He";
        if(name.equals("0009+501")) return "H";
        if(name.equals("0011+000")) return "H";
        if(name.equals("0011-134")) return "H";
        if(name.equals("0029-032")) return "He";
        if(name.equals("0033+016")) return "H";
        if(name.equals("0038-226")) return "He";
        if(name.equals("0038+555")) return "He";
        if(name.equals("0046+051")) return "He";
        if(name.equals("0101+048")) return "H";
        if(name.equals("0115+159")) return "He";
        if(name.equals("0117-145")) return "H";
        if(name.equals("0121+401")) return "H";
        if(name.equals("0126+101")) return "H";
        if(name.equals("0135-052")) return "H";
        if(name.equals("0142+312")) return "H";
        if(name.equals("0208+396")) return "H";
        if(name.equals("0213+427")) return "H";
        if(name.equals("0222+648")) return "He";
        if(name.equals("0230-144")) return "H";
        if(name.equals("0243-026")) return "H";
        if(name.equals("0245+541")) return "H";
        if(name.equals("0257+080")) return "H";
        if(name.equals("0324+738")) return "He";
        if(name.equals("0326-273")) return "H";
        if(name.equals("0341+182")) return "He";
        if(name.equals("0357+081")) return "H";
        if(name.equals("0426+588")) return "He";
        if(name.equals("0433+270")) return "H";
        if(name.equals("0435-088")) return "He";
        if(name.equals("0440+510")) return "H";
        if(name.equals("0503-174")) return "H";
        if(name.equals("0518+333")) return "H";
        if(name.equals("0548-001")) return "He";
        if(name.equals("0551+468")) return "H";
        if(name.equals("0552-041")) return "He";
        if(name.equals("0553+053")) return "H";
        if(name.equals("0618+067")) return "H";
        if(name.equals("0644+025")) return "H";
        if(name.equals("0648+641")) return "H";
        if(name.equals("0651-479")) return "He";
        if(name.equals("0654+027")) return "He";
        if(name.equals("0657+320")) return "H";
        if(name.equals("0659-064")) return "H";
        if(name.equals("0706+377")) return "He";
        if(name.equals("0727+482A")) return "H";
        if(name.equals("0727+482B")) return "H";
        if(name.equals("0738-172")) return "He";
        if(name.equals("0743-340")) return "He";
        if(name.equals("0747+073A")) return "He";
        if(name.equals("0747+073B")) return "H";
        if(name.equals("0751+578")) return "He";
        if(name.equals("0752-676")) return "H";
        if(name.equals("0752+365")) return "H";
        if(name.equals("0802+386")) return "He";
        if(name.equals("0816+387")) return "H";
        if(name.equals("0827+328")) return "H";
        if(name.equals("0839-327")) return "H";
        if(name.equals("0856+331")) return "He";
        if(name.equals("0912+536")) return "He";
        if(name.equals("0913+442")) return "H";
        if(name.equals("0930+294")) return "H";
        if(name.equals("0946+534")) return "He";
        if(name.equals("0955+247")) return "H";
        if(name.equals("1012+083")) return "H";
        if(name.equals("1019+637")) return "H";
        if(name.equals("1039+145")) return "He";
        if(name.equals("1043-188")) return "He";
        if(name.equals("1055-072")) return "He";
        if(name.equals("1108+207")) return "H";
        if(name.equals("1115-029")) return "He";
        if(name.equals("1121+216")) return "H";
        if(name.equals("1124-296")) return "H";
        if(name.equals("1136-286")) return "He";
        if(name.equals("1142-645")) return "He";
        if(name.equals("1147+255")) return "H";
        if(name.equals("1154+186")) return "He";
        if(name.equals("1208+576")) return "H";
        if(name.equals("1215+323")) return "He";
        if(name.equals("1236-495")) return "H";
        if(name.equals("1244+149")) return "H";
        if(name.equals("1247+550")) return "H";
        if(name.equals("1257+037")) return "H";
        if(name.equals("1257+278")) return "H";
        if(name.equals("1300+263")) return "H";
        if(name.equals("1310-472")) return "H";
        if(name.equals("1313-198")) return "He";
        if(name.equals("1325+581")) return "H";
        if(name.equals("1328+307")) return "He";
        if(name.equals("1334+039")) return "H";
        if(name.equals("1344+106")) return "H";
        if(name.equals("1345+238")) return "H";
        if(name.equals("1418-088")) return "H";
        if(name.equals("1444-174")) return "He";
        if(name.equals("1455+298")) return "H";
        if(name.equals("1503-070")) return "H";
        if(name.equals("1606+422")) return "H";
        if(name.equals("1609+135")) return "H";
        if(name.equals("1625+093")) return "H";
        if(name.equals("1626+368")) return "He";
        if(name.equals("1633+433")) return "H";
        if(name.equals("1633+572")) return "He";
        if(name.equals("1635+137")) return "H";
        if(name.equals("1637+335")) return "H";
        if(name.equals("1639+537")) return "He";
        if(name.equals("1655+215")) return "H";
        if(name.equals("1656-062")) return "H";
        if(name.equals("1705+030")) return "He";
        if(name.equals("1716+020")) return "H";
        if(name.equals("1733-544")) return "H";
        if(name.equals("1736+052")) return "H";
        if(name.equals("1748+708")) return "He";
        if(name.equals("1756+827")) return "H";
        if(name.equals("1811+327A")) return "H";
        if(name.equals("1811+327B")) return "H";
        if(name.equals("1818+126")) return "H";
        if(name.equals("1820+609")) return "H";
        if(name.equals("1824+040")) return "H";
        if(name.equals("1826-045")) return "H";
        if(name.equals("1829+547")) return "H";
        if(name.equals("1831+197")) return "He";
        if(name.equals("1840+042")) return "H";
        if(name.equals("1855+338")) return "H";
        if(name.equals("1900+705")) return "He";
        if(name.equals("1917+386")) return "He";
        if(name.equals("1953-011")) return "H";
        if(name.equals("2002-110")) return "He";
        if(name.equals("2011+065")) return "He";
        if(name.equals("2048+263")) return "H";
        if(name.equals("2054-050")) return "He";
        if(name.equals("2059+190")) return "H";
        if(name.equals("2059+247")) return "H";
        if(name.equals("2059+316")) return "He";
        if(name.equals("2105-820")) return "H";
        if(name.equals("2107-216")) return "H";
        if(name.equals("2111+261")) return "H";
        if(name.equals("2136+229")) return "H";
        if(name.equals("2140+207")) return "He";
        if(name.equals("2154-512")) return "H";    // No atmosphere type in BLR2001 paper - I've given it H here but residuals are large (786/51) for either type
        if(name.equals("2207+142")) return "H";
        if(name.equals("2246+223")) return "H";
        if(name.equals("2248+293")) return "H";
        if(name.equals("2251-070")) return "He";
        if(name.equals("2253-081")) return "H";
        if(name.equals("2311-068")) return "He";
        if(name.equals("2312-024")) return "He";
        if(name.equals("2316-064")) return "He";
        if(name.equals("2329+267")) return "H";
        if(name.equals("2345-447")) return "He";
        if(name.equals("2347+292")) return "H";
        if(name.equals("2352+401")) return "He";

	return "error";

    }


}
