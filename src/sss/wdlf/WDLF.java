
package LuminosityFunction;

import java.io.*;
import java.text.*;
import java.util.*;
import java.text.DecimalFormat;
import Star.*;
import Field.*;
import Survey.*;
import Kinematics.*;
import Constants.*;

/**
 * This class applies the traditional 1/v_{max} technique to measure the luminosity function
 * for white dwarfs. The calculation is complicated somewhat by the non-analytic lower proper
 * motion limits, which makes the volume calculation a discrete integration. This is all done
 * behind the scenes in the Survey classes. For each star, the entire survey volume over all
 * proper motion ranges is determined.
 * @author nickrowell
 */
class WDLF{

    //+++ Set tangential velocity range for white dwarfs +++//
    static double vtan_lower  = 240;
    static double vtan_upper  = 550;

    //+++ Luminosity function resolution. Per-magnitude densities +++//
    //+++ are recovered by dividing by width of magnitude bin.    +++//
    static double bin_width = 0.5;
    static double Mbol_min = 0.0;
    static double Mbol_max = 20.0;

    public static void main(String arg[]) throws IOException, Exception{

	//+++ Get precise date and time of program run, for output file names and logging  +++//
        Date date = new Date();

        //+++ Select survey footprint to be used +++//
        Footprint FOOTPRINT = new Footprint("b10_GC20");

        //+++ Initialise each survey object +++//
        LowMuSurvey lowMuSurvey = new LowMuSurvey(FOOTPRINT.toString());
        IntermediateMuSurvey intermediateMuSurvey = new IntermediateMuSurvey(FOOTPRINT.toString());
        HighMuSurvey highMuSurvey = new HighMuSurvey(FOOTPRINT.toString());

        //+++ Load these into an array +++//
        Survey[] survey = {lowMuSurvey,intermediateMuSurvey,highMuSurvey};

	//+++ Get array of input files corrsponding to WD candidates from each survey +++//
	File input[] = {new File("/spare/SSS/Catalogues/LowPM/WDs_fit.txt"),
			new File("/spare/SSS/Catalogues/ExtraPM/WDs_fit.txt"),
			new File("/spare/SSS/Catalogues/HighPM/WDs_fit.txt")};

        //+++ Get discovery fractions for each kinematic population in each field, given vtan range +++//
        DiscoveryFraction[] discoveryFraction  = {new DiscoveryFraction(vtan_lower, vtan_upper, "ThinDisk"),
                                                  new DiscoveryFraction(vtan_lower, vtan_upper, "ThickDisk"),
                                                  new DiscoveryFraction(vtan_lower, vtan_upper, "Halo")};
        


	//+++ Temporary files for recording what UCWDs and stars with spectroscopic follow ups +++//
	//+++ are included in the LF. These will be appended to each file  +++//
	//+++ after LF calculation, then the temporary files will be deleted. Set up file writers too.   +++//
	File UCWD    = new File("/spare/SSS/LuminosityFunction/Total/UCWD_"+filename.format(date));
	File SPECTRA = new File("/spare/SSS/LuminosityFunction/Total/SPECTRA_"+filename.format(date));
	BufferedWriter ucwd    = new BufferedWriter(new FileWriter(UCWD));
	BufferedWriter spectra = new BufferedWriter(new FileWriter(SPECTRA));

	//+++ Set up output file for details of stars that contribute to LF +++//
	File stars = new File("/spare/SSS/LuminosityFunction/Total/"+filename.format(date)+"_stars");
	BufferedWriter out_STARS = new BufferedWriter(new FileWriter(stars));

	//+++ Write headers +++//
        writeHeaders(out_STARS,date,FOOTPRINT);

	//+++ Array to store luminosity function information +++//
	double N_bins = (Mbol_max - Mbol_min)/bin_width;
	double LF[][][] = new double[3][(int)Math.rint(N_bins)][7];

        //+++ Leading element specifies kinematic population, 0=thin disk, 1=thick disk, 2=halo +++//
        //+++ Quantities used to measure luminosity function in bin +++//
        //LF[pop][m][0]=0;    // sum 1/Vmax
        //LF[pop][m][1]=0;    // sum 1/Vmax^2
        //+++ Quantities used for statistical tests and normalization +++//
        //LF[pop][m][2]=0;    // Sum of (fractional) stars in bin, i.e. weights of stars
        //LF[pop][m][3]=0;    // Sum of (weighted) v/vmax in bin
        //+++ Quantities handling horizontal error bars +++//
        //LF[pop][m][4]=0;    // Sum of upper M_{bol} sigmas
        //LF[pop][m][5]=0;    // Sum of lower M_{bol} sigmas
        //LF[pop][m][6]=0;    // Sum of M_{bol} in bin, for plotting LF point at mean M_{bol}


	//+++ Non-symmetric horizontal error bars on each star +++//
	double sigH_up,sigH_lo,sigHe_up,sigHe_lo;

	int binDA,binDB;             // Magnitude bin that stars fall into

	//+++ Track running of code by counting stars read in and stars added to LF +++//
	int N_read  = 0;
        int N_added = 0;

        //+++ Record distribution of v/vmax statistic +++//
        double vvmax_bin_width = 0.1;
        double[][] P_vvmax = new double[3][(int)Math.rint(1/vvmax_bin_width)];
        //+++ Record mean for each population +++//
        double[][] vvmax = new double[3][2];


	//+++ Read in all stellar data by looping over all input catalogues +++//
	String stellarData;

        for(int s=0; s<3; s++){

            BufferedReader in = new BufferedReader(new FileReader(input[s]));

            //+++ Set flag determining whether low or high proper motion records are being read in +++//
            int flag = (s==0) ? 0:1;

            while ((stellarData = in.readLine()) != null) {

                System.out.println("Star " + (N_read++));

                WhiteDwarf star = new WhiteDwarf(stellarData, flag);

                //+++ Get corresponding photometric models +++//
                String[] models = {in.readLine(), in.readLine(), in.readLine(), in.readLine(), in.readLine(), in.readLine()};

                //+++ Check this star lies in footprint area set for surveys. It is possible that    +++//
                //+++ input WD candidates catalogues might be all-sky, and footprint region set here +++//
                if (FOOTPRINT.isStarInRegion(star) &&
                    FOOTPRINT.getSolidAngle(star.f,star.hemi) != 0.0) {


                    //+++ Set distance to star using either fitted colours or values from literature +++//
                    if (star.isKnownUCWD()) {
                        star.setPublishedAtmosphere();
                    }
                    else {
                        star.setModels(models);
                        star.setAtmosphereFractions();
                        star.setPhotoPI(models);
                    }


                    //+++ Each atmosphere type may or may not contribute to LF depending on whether +++//
                    //+++ or not it gives a solution that passes survey tangential velocity limit.  +++//
                    boolean H_ok = ((4.74 * star.dH * star.mu > vtan_lower) && (4.74 * star.dH * star.mu < vtan_upper));
                    boolean He_ok = ((4.74 * star.dHe * star.mu > vtan_lower) && (4.74 * star.dHe * star.mu < vtan_upper));

                    // Only proceed if one or the other type passes v_{tan} limits. This speeds things up.
                    if(H_ok || He_ok){


                        //+++ Get survey volumes for star, assuming H and He atmosphere solutions +++//

                        //+++ Initialise arrays to sum survey volumes +++//
                        double[][] V_H = {{0, 0}, {0, 0}, {0, 0}}, v_H;
                        double[][] V_He = {{0, 0}, {0, 0}, {0, 0}}, v_He;

                        //+++ Loop over all surveys and sum volume for each +++//
                        for (int i = 0; i < 3; i++) {

                            //+++ Get v_{max} for survey 'i', for all kinematic populations +++//
                            v_H = survey[i].getVMax(star, star.dH);
                            v_He = survey[i].getVMax(star, star.dHe);

                            //+++ Add volumes to total for all surveys +++//
                            for (int pop = 0; pop < 3; pop++) {
                                for (int vol = 0; vol < 2; vol++) {
                                    V_H[pop][vol] += v_H[pop][vol];
                                    V_He[pop][vol] += v_He[pop][vol];
                                }
                            }
                        }

                        //+++ Get correct LF bin for this object +++//
                        binDA = (int) Math.floor((star.mBolH - Mbol_min) / bin_width);
                        binDB = (int) Math.floor((star.mBolHe - Mbol_min) / bin_width);

                        //+++ Matrix V_x now contains V and V_{max} values for star in all surveys, +++//
                        //+++ for each kinematic population and for each atmosphere type.           +++//

                        //+++ Adjust all volumes by reduced chi^2 correction factor for survey of original detection +++//
                        for (int pop = 0; pop < 3; pop++) {
                            for (int vol = 0; vol < 2; vol++) {
                                V_H[pop][vol] *= survey[s].getChi2DiscoveryFraction(star.hemi);
                                V_He[pop][vol] *= survey[s].getChi2DiscoveryFraction(star.hemi);
                            }
                        }

                        //+++ Adjust volumes by discovery fraction appropriate for each kinematic population +++//
                        for (int pop = 0; pop < 3; pop++) {
                            for (int vol = 0; vol < 2; vol++) {
                                V_H[pop][vol] *= discoveryFraction[pop].getDiscoveryFraction(star.f, star.hemi);
                                V_He[pop][vol] *= discoveryFraction[pop].getDiscoveryFraction(star.f, star.hemi);
                            }
                        }

                        //+++ Add weighted inverse v_{max}'s to luminosity function array +++//
                        for (int pop = 0; pop < 3; pop++) {
                            if (H_ok) {
                                LF[pop][binDA][0] += (1.0 / V_H[pop][1]) * star.fracH;
                                LF[pop][binDA][1] += (1.0 / V_H[pop][1]) * (1.0 / V_H[pop][1]) * star.fracH * star.fracH;
                            }
                            if (He_ok) {
                                LF[pop][binDB][0] += (1.0 / V_He[pop][1]) * star.fracHe;
                                LF[pop][binDB][1] += (1.0 / V_He[pop][1]) * (1.0 / V_He[pop][1]) * star.fracHe * star.fracHe;
                            }
                        }

                        //+++ Add stellar weights to sum of stars in each bin +++//
                        for (int pop = 0; pop < 3; pop++) {
                            if (H_ok) {
                                LF[pop][binDA][2] += star.fracH;
                            }
                            if (He_ok) {
                                LF[pop][binDB][2] += star.fracHe;
                            }
                        }

                        //+++ Now deal with v/vmax statistic +++//
                        for (int pop = 0; pop < 3; pop++) {

                            //+++ Index of P_vvmax array to add vvmax to +++//
                            int indexH = (int) Math.floor((V_H[pop][0] / V_H[pop][1]) / vvmax_bin_width);
                            int indexHe = (int) Math.floor((V_He[pop][0] / V_He[pop][1]) / vvmax_bin_width);

                            //+++ Clamp vvmax so that when vvmax = 1.0 the star is added to final element of array +++//
                            if (indexH >= P_vvmax[pop].length) {
                                indexH = P_vvmax.length - 1;
                                System.err.println("VVmax H clamped");
                            }
                            if (indexHe >= P_vvmax[pop].length) {
                                indexHe = P_vvmax.length - 1;
                                System.err.println("VVmax He clamped");
                            }

                            if (H_ok) {
                                //+++ Add star to v/vmax histogram +++//
                                P_vvmax[pop][indexH] += star.fracH;
                                //+++ Add to sum for mean of all stars +++//
                                vvmax[pop][0] += (V_H[pop][0] / V_H[pop][1]) * star.fracH;
                                vvmax[pop][1] += star.fracH;
                                //+++ Add to luminosity function bin +++//
                                LF[pop][binDA][3] += (V_H[pop][0] / V_H[pop][1]) * star.fracH;
                            }
                            if (He_ok) {
                                //+++ Add star to v/vmax histogram +++//
                                P_vvmax[pop][indexHe] += star.fracHe;
                                //+++ Add to sum for mean of all stars +++//
                                vvmax[pop][0] += (V_He[pop][0] / V_He[pop][1]) * star.fracHe;
                                vvmax[pop][1] += star.fracHe;
                                //+++ Add to luminosity function bin +++//
                                LF[pop][binDB][3] += (V_He[pop][0] / V_He[pop][1]) * star.fracHe;
                            }

                        }

                        //+++ Now deal with contribution to horizontal error bars on LF bin +++//
                        sigH_up = Double.parseDouble(Misc.columns(star.oneSigmaLowerH, 4)) - star.mBolH;
                        sigH_lo = star.mBolH - Double.parseDouble(Misc.columns(star.oneSigmaUpperH, 4));

                        sigHe_up = Double.parseDouble(Misc.columns(star.oneSigmaLowerHe, 4)) - star.mBolHe;
                        sigHe_lo = star.mBolHe - Double.parseDouble(Misc.columns(star.oneSigmaUpperHe, 4));

                        //+++ If either upper or lower one-sigma photometric model was not +++//
                        //+++ found, use the error on the one that was found to set error  +++//
                        if (Misc.columns(star.oneSigmaLowerH, 2).equals("-1")) {
                            sigH_up = sigH_lo;
                        }
                        if (Misc.columns(star.oneSigmaUpperH, 2).equals("-1")) {
                            sigH_lo = sigH_up;
                        }

                        if (Misc.columns(star.oneSigmaLowerHe, 2).equals("-1")) {
                            sigHe_up = sigHe_lo;
                        }
                        if (Misc.columns(star.oneSigmaUpperHe, 2).equals("-1")) {
                            sigHe_lo = sigHe_up;
                        }

                        //+++ Known UCWDs have neither upper nor lower models. Set sigmas to 0.5M +++//
                        if (star.ucwd) {
                            sigH_up = sigH_lo = sigHe_up = sigHe_lo = 0.5;
                        }

                        //+++ Add these to sum. Identical for each population because calculated only +++//
                        //+++ from uncertainty in atmosphere fit and not on assumed kinematic type.   +++//
                        for (int pop = 0; pop < 3; pop++) {
                            if (H_ok) {
                                LF[pop][binDA][4] += star.fracH * sigH_up * sigH_up;
                                LF[pop][binDA][5] += star.fracH * sigH_lo * sigH_lo;
                                LF[pop][binDA][6] += star.fracH * star.mBolH;
                            }
                            if (He_ok) {
                                LF[pop][binDB][4] += star.fracHe * sigHe_up * sigHe_up;
                                LF[pop][binDB][5] += star.fracHe * sigHe_lo * sigHe_lo;
                                LF[pop][binDB][6] += star.fracHe * star.mBolHe;
                            }
                        }



                        /**  For either atmoshpere type, if star passes tangential velocity selection AND
                         *   has non-zero atmosphere weight for given colour, then it has contributed to
                         *   luminosity function. Write out the details of it's contribution to a file so that
                         *   errors can be checked for. Also write out details of spectroscopic follow ups and UCWDs
                         */
                        if ((H_ok && (star.fracH > 0.0)) || (He_ok && (star.fracHe > 0.0))) {
                            writeStar(H_ok, He_ok, (N_added++), out_STARS, star, V_H, V_He);
                            writeSpec(spectra, star);
                            writeUCWD(ucwd, star);
                        }

                    }

                }

            }


        }



	//+++ Finished processing all stars - write out v/vmax histogram and calculate means and variances +++//

        //+++ Sum all vvmax histogram entries to get normalisation constant +++//
        double N=0;
        for(int v=0; v<P_vvmax[0].length; v++) N += P_vvmax[0][v];

	File Pvvmax = new File("/spare/SSS/LuminosityFunction/Total/"+filename.format(date)+"_vvmax");
	BufferedWriter out_VVMAX = new BufferedWriter(new FileWriter(Pvvmax));

        out_VVMAX.write("v/vmax\tdisk1\tdisk2\tsph.\n");

        //+++ Sums used to calculate mean and variance of v/vmax for each population +++//
        //+++ by integrating distribution.                                           +++//
        double[] mean_vvmax  = {0.0,0.0,0.0};
        double[] mean_vvmax2 = {0.0,0.0,0.0};

        for(int v=0; v<P_vvmax[0].length; v++){

            //+++ V/V_{max} value at bin centre - use this for integration +++//
            double vvmax_mid = v*vvmax_bin_width + (vvmax_bin_width/2.0);

            //+++ Note - (P_vvmax / N) gives probability in each vvmax bin. Dividing this +++//
            //+++ by the vvmax bin width gives the probability per unit vvmax, which is    +++//
            //+++ the normal quantity for probability density functions.                  +++//

            //+++ Simple rectangular integration elements +++//
            for(int pop=0; pop<3; pop++){
                mean_vvmax[pop]  += (vvmax_mid * ((P_vvmax[pop][v]/N)/vvmax_bin_width) * vvmax_bin_width);
                mean_vvmax2[pop] += (vvmax_mid * vvmax_mid * ((P_vvmax[pop][v]/N)/vvmax_bin_width) * vvmax_bin_width);
            }

            //+++ Write out this vvmax bin +++//
            out_VVMAX.write(xpxx.format(vvmax_mid) + "\t" + xpxxx.format((P_vvmax[0][v]/N)/vvmax_bin_width) + "\t" + xpxxx.format((P_vvmax[1][v]/N)/vvmax_bin_width) + "\t" + xpxxx.format((P_vvmax[2][v]/N)/vvmax_bin_width) + "\n");

        }
        
        //+++ Now append mean vvmax values to end of histogram +++//
        out_VVMAX.write("\nmean");
        for(int pop=0; pop<3; pop++){ out_VVMAX.write("\t" + xpxxx.format(mean_vvmax[pop]));}
        out_VVMAX.write("\nsigma");
        for(int pop=0; pop<3; pop++){
            out_VVMAX.write("\t" + xpxxx.format(Math.sqrt(mean_vvmax2[pop] - mean_vvmax[pop]*mean_vvmax[pop])));
        }
        out_VVMAX.write("\nN = " + N);
        out_VVMAX.flush();

        //+++ Write out luminosity function details +++//

	File wdlf = new File("/spare/SSS/LuminosityFunction/Total/"+filename.format(date)+"_lf");
	BufferedWriter out_LF = new BufferedWriter(new FileWriter(wdlf));

	//+++ Write headers for output LF file +++//
	out_LF.write("# Date and time of run: "+header.format(date)+"\n");
	out_LF.write("# Selected sky area: "+FOOTPRINT.toString()+"\n");
	out_LF.write("# v_{tan} range  = "+vtan_lower + " to " + vtan_upper+"\n#\n#\n");

        /*
         *  Each bin has a unique mean bolometric magnitude, horizontal error and number of stars.
         *  Within each bin, each kinematic population has it's own luminosity function, uncertainty
         *  and <v/vmax> calculated using the corresponding density profiles.
         */

	out_LF.write("#    \t      \t      \t      \t \t Thin disk \t\t Thick disk \t\t Spheroid \n");
	out_LF.write("#Mbol\t<Mbol>\tsigMlo\tsigMhi\tN\tPhi\tSig_Phi\t<v/vm>\tPhi\tSig_Phi\t<v/vm>\tPhi\tSig_Phi\t<v/vm>\n");

	out_LF.flush();


        //+++ Parameters constant between kinematic populations +++//
	double meanMbol, sig_lo, sig_up;

        //+++ Parameters calculated separately for each kinematic population +++//
        double lf, var, meanvvmax;

        //+++ Integrated quantities for each kinematic population +++//
	double[] totalSpaceDensity    = {0.0,0.0,0.0};
	double[] totalSpaceDensityVar = {0.0,0.0,0.0};

        //+++ Total number of stars in LF +++//
	double totalStars = 0.0;

	//+++ Loop over all bolometric magnitude bins +++//
	for(int m=0;m<LF[0].length;m++){

            //+++ Sum total number of stars. Should be same for each population. +++//
            totalStars += LF[0][m][2];

            //+++ Write out details of magnitude bin, mean magnitude and error. +++//
            //+++ These are identical for each population - use thin disk here. +++//

	    //+++ Get M_{bol} corresponding to centre of current bin +++//
	    String M = ""+xpxx.format((m*bin_width)+Mbol_min+(bin_width/2.0));

            //+++ Get mean bolometric magnitude for stars that fall in this bin +++//
	    meanMbol = (LF[0][m][6]/LF[0][m][2]);

            //+++ Horizontal error bars +++//
	    sig_up = Math.sqrt(LF[0][m][4]/LF[0][m][2]);
	    sig_lo = Math.sqrt(LF[0][m][5]/LF[0][m][2]);

            out_LF.write(M + "\t" + xpxx.format(meanMbol) + "\t" + xpxx.format(sig_lo) + "\t" + xpxx.format(sig_up) + "\t" + xpx.format(LF[0][m][2]));

            //+++ Now loop over each population and write out population specific density etc. +++//
            for(int pop=0; pop<3; pop++){

                //+++ Density per unit magnitude, and uncertainty from propagating Poisson errors +++//
	        lf  = LF[pop][m][0]/bin_width;
	        var = LF[pop][m][1]*(1.0/bin_width)*(1.0/bin_width);
                meanvvmax = LF[pop][m][3]/LF[pop][m][2];

                out_LF.write("\t" + xpxxEx.format(lf) + "\t" + xpxEx.format(Math.sqrt(var)) + "\t" + xpxxx.format(meanvvmax));

                totalSpaceDensity[pop]    += LF[pop][m][0];
                totalSpaceDensityVar[pop] += LF[pop][m][1];
            }

	    out_LF.newLine();
            out_LF.flush();

	}


        //+++ Now write out quantities integrated over LF +++//
        out_LF.write("\n\n\n# Integrated densities\n#\n#\n");
        out_LF.write("# Thin disk:\n");
	out_LF.write("# Total space density = "+totalSpaceDensity[0]+" +/- "+Math.sqrt(totalSpaceDensityVar[0]) + "\n");
        out_LF.write("# Thick disk:\n");
	out_LF.write("# Total space density = "+totalSpaceDensity[1]+" +/- "+Math.sqrt(totalSpaceDensityVar[1]) + "\n");
        out_LF.write("# Spheroid:\n");
	out_LF.write("# Total space density = "+totalSpaceDensity[2]+" +/- "+Math.sqrt(totalSpaceDensityVar[2]) + "\n");

        //+++ Now write out mean VVmax stats +++//
        out_LF.write("\n# Mean vvmax assuming all stars thin disk  = "+(vvmax[0][0]/vvmax[0][1])+" +/-"+(1.0/Math.sqrt(12.0*vvmax[0][1])));
        out_LF.write("\n# Mean vvmax assuming all stars thick disk = "+(vvmax[1][0]/vvmax[1][1])+" +/-"+(1.0/Math.sqrt(12.0*vvmax[1][1])));
        out_LF.write("\n# Mean vvmax assuming all stars spheroid   = "+(vvmax[2][0]/vvmax[2][1])+" +/-"+(1.0/Math.sqrt(12.0*vvmax[2][1])));

        //+++ Total number of stars contribution to each LF +++//
	out_LF.write("\n\n# Total number of stars = "+xpx.format(totalStars)+"\n\n");
        
	out_LF.write("List of objects used to calculate this LF that have spectroscopic follow ups:\n");


	//+++ Append lists of stars with spectra and any UCWDs present in this selected sky region +++//

	BufferedReader append_in = new BufferedReader(new FileReader(SPECTRA));

        String append;

	while((append=append_in.readLine())!=null) out_LF.write(append+"\n");

	out_LF.write("\n\n");
	
	out_LF.write("List of identified ultracool WDs used to calculate this LF:\n");

	append_in = new BufferedReader(new FileReader(UCWD));

	while((append=append_in.readLine())!=null) out_LF.write(append+"\n");

	//+++ Flush output stream +++//

	out_LF.flush();

	//+++ Remove temporary list files +++//

	SPECTRA.delete();
	UCWD.delete();


	System.exit(0);

    }

    public static void writeHeaders(BufferedWriter out_STARS, Date date, Footprint FOOTPRINT) throws IOException{
    	out_STARS.write("# Date and time of run: "+header.format(date)+"\n");
	out_STARS.write("# Selected sky area: "+FOOTPRINT.toString()+"\n");
	out_STARS.write("# v_{tan} range  = "+vtan_lower + " to " + vtan_upper+"\n# Thin disk H = "+Disks.getThinDiskScaleheight()+"\n# Thick disk H = "+Disks.getThickDiskScaleheight()+"\n");
        out_STARS.write("# Columns 3 & 4 give H and He atmosphere solution weights. A value \"----\" indicates that\n# a solution that does not pass tangential velocity selection, and is not included in calculation.\n#\n#\n");
        out_STARS.write("#    \t     \t   \t    \t    \t     \t      \t       \t Thin disk vmax^{-1} \t Thick disk vmax^{-1} \t Spheroid vmax^{-1}\n");
	out_STARS.write("#star\tfield\tw_H\tw_He\tVt_H\tVt_He\tMbol_H\tMbol_He\tH\tHe\tv/vmax\tH\tHe\tv/vmax\tH\tHe\tv/vmax" +"\n");
	out_STARS.flush();
    }


    public static void writeStar(boolean H_ok, boolean He_ok, int N_added, BufferedWriter out_STARS, WhiteDwarf star, double[][] V_H, double[][]V_He) throws IOException{

        //+++ Indexing, field number and hemisphere +++//
        out_STARS.write(N_added + "\t" + star.f + "" + star.hemi + "\t");

        //+++ Atmosphere weights, including kinematical selection flag +++//
        if (H_ok) {
            out_STARS.write(xpxx.format(star.fracH) + "\t");
        } else {
            out_STARS.write("----" + "\t");
        }
        if (He_ok) {
            out_STARS.write(xpxx.format(star.fracHe) + "\t");
        } else {
            out_STARS.write("----" + "\t");
        }

        //+++ Tangential velocity for each solution +++//
        out_STARS.write((int) Math.rint(4.74 * star.dH * star.mu) + "\t" + (int) Math.rint(4.74 * star.dHe * star.mu) + "\t");

        //+++ Bolometric magnitude for each solution +++//
        out_STARS.write(Misc.columns(star.bestFitH, 4) + "\t" + Misc.columns(star.bestFitH, 4) + "\t");

        //+++ Thin disk inverse volumes and v/vmax +++//
        out_STARS.write(xpxEx.format(1.0 / V_H[0][1]) + "\t" + xpxEx.format(1.0 / V_He[0][1]) + "\t" + xpxxx.format((V_H[0][0] / V_H[0][1]) * star.fracH + (V_He[0][0] / V_He[0][1]) * star.fracHe) + "\t");

        //+++ Thick disk inverse volumes and v/vmax +++//
        out_STARS.write(xpxEx.format(1.0 / V_H[1][1]) + "\t" + xpxEx.format(1.0 / V_He[1][1]) + "\t" + xpxxx.format((V_H[1][0] / V_H[1][1]) * star.fracH + (V_He[1][0] / V_He[1][1]) * star.fracHe) + "\t");

        //+++ Spheroid inverse volumes and v/vmax +++//
        out_STARS.write(xpxEx.format(1.0 / V_H[2][1]) + "\t" + xpxEx.format(1.0 / V_He[2][1]) + "\t" + xpxxx.format((V_H[2][0] / V_H[2][1]) * star.fracH + (V_He[2][0] / V_He[2][1]) * star.fracHe));

        out_STARS.newLine();
        out_STARS.flush();

    }

    public static void writeSpec(BufferedWriter spectra, WhiteDwarf star) throws IOException{
        if (star.spec) {
            //+++ Star has spectroscopic follow up +++//
            spectra.write(star.designation());
            if (star.ucwd) {
                spectra.write("\t -> " + star.ucwdName);
            }
            spectra.newLine();
            spectra.write(star.data);
            spectra.newLine();
            spectra.flush();
        }
    }

    public static void writeUCWD(BufferedWriter ucwd, WhiteDwarf star) throws IOException{

        if (star.ucwd) {
            //+++ Star has atmosphere parameters taken from literature +++//
            ucwd.write(star.ucwdName);
            ucwd.newLine();
            ucwd.flush();
        }
    }


    static DateFormat filename = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
    static DateFormat header = new SimpleDateFormat("HH:mm dd/MM/yyyy");
    //+++ Set up number and date formatters for output files and quantities +++//
    static DecimalFormat xpxEx = new DecimalFormat("0.0E0");
    static DecimalFormat xpxxEx = new DecimalFormat("0.00E0");
    static DecimalFormat xpx = new DecimalFormat("0.0");
    static DecimalFormat xpxx = new DecimalFormat("0.00");
    static DecimalFormat xpxxx = new DecimalFormat("0.000");


}