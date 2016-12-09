package Calibration;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;
import Field.SDSS;

/**
 * This class is used to compare my SSS WD catalogue with other catalogues.
 * It reads in both catalogues, then looks for incidences of the same star
 * in both.
 *
 * @author nickrowell
 */
public class CompareCatalogues {

    public static void main(String[] args) throws IOException{

        File myCat    = new File("/spare/SSS/Catalogues/Release/v2/ssawdcat");
        // number of header lines to skip
        int NHEAD1    = 3;
        // Last line of data section
        int NDATA1    = 9752;

        File otherCat = new File("/spare/SSS/Catalogues/Harris2006/tabdata.dat");
        // number of header lines to skip
        int NHEAD2    = 3;
        // Last line of data section
        int NDATA2    = 7143;

        // ArrayList to hold stars from first catalogue
        ArrayList<Star> cat1 = new ArrayList<Star>();
        // Open reader on catalogue
        BufferedReader in = new BufferedReader(new FileReader(myCat));
        // Purge header
        for(int i=0; i<NHEAD1; i++) in.readLine();
        // For every remaining line, create one star object
        for(int i=NHEAD1; i<NDATA1; i++){
            Star star = new SSS_Star(in.readLine());
            if(star.vtan>30.0)
                cat1.add(star);
        }

        // ArrayList to hold stars from second catalogue
        ArrayList<Star> cat2 = new ArrayList<Star>();
        // Open reader on catalogue
        in = new BufferedReader(new FileReader(otherCat));
        // Purge header
        for(int i=0; i<NHEAD2; i++) in.readLine();
        // For every remaining line, create one star object
        for(int i=NHEAD2; i<NDATA2; i++){
            Star star = new SDSS_Star(in.readLine());
            if(star.vtan>30.0)
                cat2.add(star);
        }
        System.out.println(cat1.size()+" SSS stars with vt>30");
        System.out.println(cat2.size()+" SDSS stars with vt>30");

        // Get SDSS footprint data
        double[][] sdss_foot = SdssFootprintUtils.getSDSSFootprint(3);


        // Bin difference between g and b photometry
        Histogram bMinusG = new Histogram(-1.0, 1.0, 0.1);

        // Bin distance ratio
        Histogram photDist = new Histogram(0.0, 1.0, 0.05);
        Correlation photDistR = new Correlation();

        // Bin bolometric mag difference
        Histogram mBolDiff = new Histogram(-1.0, 1.0, 0.1);


        int[] STATUS = new int[6];

        // Now do pair-wise matching
        for(int sss = 0; sss<cat1.size(); sss++){

            // Exclude SSS stars that are not in SDSS footprint area
            double[] ra_dec = cat1.get(sss).getCoordinates(2000);

            if(!SdssFootprintUtils.isInSDSS(sdss_foot, ra_dec[0], ra_dec[1])){
                //System.out.println(Math.toDegrees(ra_dec[0])+" "+Math.toDegrees(ra_dec[1]));
                continue;
            }
            System.out.println(Math.toDegrees(ra_dec[0])+" "+Math.toDegrees(ra_dec[1]));

            // Add one to number of SSS stars in SDSS area.
            STATUS[4]++;


            // Check for matches against all SDSS stars:
            for(int sdss=0; sdss<cat2.size(); sdss++){

                if(Star.angSep(cat1.get(sss), cat2.get(sdss)) < 4){

                    // Add SDSS match to SSS star:
                    cat1.get(sss).MATCHES.add(sdss);

                    // Add SSS match to SDSS star:
                    cat2.get(sdss).MATCHES.add(sss);

                }
            }

            // Now evaluate matches and record stats

            // No matches to this star
            if(cat1.get(sss).MATCHES.size()==0)
                STATUS[0]++;
            // One match SSS->SDSS
            else if(cat1.get(sss).MATCHES.size()==1){

                // Index of SDSS star
                int SDSS = cat1.get(sss).MATCHES.get(0);

                // One match SDSS->SSS
                if(cat2.get(SDSS).MATCHES.size()==1){
                    STATUS[1]++;

                    /**
                     * At this point, a single SSS star has been matched to a
                     * single SDSS star. Concentrate on these objects, and use
                     * them to derive any completeness statistics or compare
                     * stellar parameters.
                     */

                    // Histogram of b-g values
                    double bg = ((SSS_Star)cat1.get(sss)).b - ((SDSS_Star)cat2.get(SDSS)).g;
                    bMinusG.add(bg);

                    // Histogram of bolometric magnitude difference
                    mBolDiff.add(cat1.get(sss).mbolH - cat2.get(SDSS).mbolH);

                    // Histgram of distance ratio
                    double maxDist = Math.max(cat1.get(sss).dH, cat2.get(SDSS).dH);
                    double minDist = Math.min(cat1.get(sss).dH, cat2.get(SDSS).dH);
                    photDist.add(minDist/maxDist);

                    photDistR.add(cat1.get(sss).dH, cat2.get(SDSS).dH);

                }
                // More than one match SDSS->SSS
                else if(cat2.get(SDSS).MATCHES.size()>1)
                    STATUS[2]++;
                else
                    System.err.println("Error 0!");
            }

            // More than one SSS->SDSS match
            else
                STATUS[3]++;


        }

        System.out.println("Finished matching:");
        System.out.println("Total number of SSS stars with vt>30:    "+cat1.size());
        System.out.println("Total number of SDSS stars with vt>30:   "+cat2.size());
        System.out.println("Number of SSS stars in SDSS area:        "+STATUS[4]);
        System.out.println("SSS stars with no SDSS counterpart:      "+STATUS[0]);
        System.out.println("SSS stars with one SDSS counterpart:     "+(STATUS[1]+STATUS[2]));
        System.out.println("Of these, SDSS star has one SSS match:   "+STATUS[1]);
        System.out.println("Of these, SDSS star has >1 SSS match:    "+STATUS[2]);
        System.out.println("SSS stars with more than one SDSS match: "+STATUS[3]);

        System.out.println("\n\nHistogram of b-g colour:");
        bMinusG.print();
        System.out.println("\n\nHistogram of distance ratio:");
        photDist.print();
        System.out.println("Mean distance ratio = "+photDist.getMean());

        System.out.println("Correlation between distance estimates = "+photDistR.getR());

        System.out.println("Bolometric magnitude difference = ");
        mBolDiff.print();

    }



}

/**
 * Class used to represent Star objects for comparison between surveys.
 * Identification is made based on Star objects; i.e. Stars should have enough
 * information to do identification, and derived classes (SSS_Star etc) keep
 * extra information specific to survey etc.
 */
abstract class Star{

    /** Record indices of conjugate catalogue of any matching stars */
    ArrayList<Integer> MATCHES = new ArrayList<Integer>();

    /** Only match vtan>30kms^{-1} stars */
    double vtan;

    /** Compare photometric distances of all stars based on H atmospheres */
    double dH;

    /** Compare bolometric magnitudes */
    double mbolH;


    /** Derived classes must provide this method */
    public abstract double[] getCoordinates(double epoch);

    /** Matching implemented here - all that is required is getCoordinates() */
    public static boolean isMatch(Star SSS, Star SDSS, double TOL){

        // Correct SSS coordinates to epoch 2000 to match SDSS
        if(angSep(SSS.getCoordinates(2000), SDSS.getCoordinates(0)) < TOL)
            return true;

        return false;
    }

    /** Gget angular separation between two stars in arcseconds */
    public static double angSep(Star SSS, Star SDSS){

        return angSep(SSS.getCoordinates(2000), SDSS.getCoordinates(0));
    }


    /**
     * Angular separation between two points on sphere. Coordinates are
     * inserted in array as ra,dec pair. Returned value is in arcseconds.
     */
    public static double angSep(double[] A, double[] B){

        // Turn ra,dec into unit vector:
        double Az = Math.sin(A[1]);
        double Ay = Math.cos(A[1])*Math.sin(A[0]);
        double Ax = Math.cos(A[1])*Math.cos(A[0]);

        double Bz = Math.sin(B[1]);
        double By = Math.cos(B[1])*Math.sin(B[0]);
        double Bx = Math.cos(B[1])*Math.cos(B[0]);

        // Now use dot product to get cosine of internal angle:
        double cosAng = Ax*Bx + Ay*By + Az*Bz;

        // Angle in degrees
        double ang = Math.toDegrees(Math.acos(cosAng));

        // Return angle in arcseconds
        return ang*60*60;

    }

}

/** Represent objects from SSS */
class SSS_Star extends Star{

    public double ra,dec,b,r,i,vtH,wH,dHe,vtHe,mbolHe,wHe,mu_ra,mu_dec,epoch;



    public SSS_Star(String data){
        
        // Open a Scanner on the data:
        Scanner scan = new Scanner(data);

        scan.next();                 // designation
        ra  = Math.toRadians(scan.nextDouble());     // ra
        dec = Math.toRadians(scan.nextDouble());     // dec
        epoch = scan.nextDouble();   // epoch
        mu_ra = Math.toRadians(scan.nextDouble()/(1000*60*60));   // mu_acosd
        mu_dec = Math.toRadians(scan.nextDouble()/(1000*60*60));  // mu_d
        b = scan.nextDouble();   // b
        r = scan.nextDouble();   // r
        i = scan.nextDouble();   // i
        dH = scan.nextDouble();   // d_H
        vtH = scan.nextDouble();   // vt_H
        vtan = vtH;                 // Set tangential velocity for matching
        mbolH = scan.nextDouble();   // Mbol_H
        wH = scan.nextDouble();   // w_H
        dHe = scan.nextDouble();   // d_He
        vtHe = scan.nextDouble();   // vt_He
        mbolHe = scan.nextDouble();   // Mbol_He
        wHe = scan.nextDouble();   // w_He

    }

    public double[] getCoordinates(double ep){

        double TDIFF = ep - epoch;

        // Coordinates in radians
        double ra_corr  = ra  + mu_ra  * TDIFF;
        double dec_corr = dec + mu_dec * TDIFF;

        return new double[]{ra_corr,dec_corr};

    }


}

/** Represent objects from SDSS */
class SDSS_Star extends Star{

    public double ra,dec,g,ug,gr,ri,iz,mg,dmin,dmax,ag,fracA,pm,glat,wt;
    int IF;
    // Make Chi a string, because at least one of the values can't be parsed as
    // a double, and it isn't used for matching anyway.
    String chi;

    public SDSS_Star(String data){

        // Open a Scanner on the data:
        Scanner scan = new Scanner(data);
        
        ra = Math.toRadians(scan.nextDouble());   // ra
        dec = Math.toRadians(scan.nextDouble());   // dec
        g = scan.nextDouble();   // g
        ug = scan.nextDouble();   // u-g
        gr = scan.nextDouble();   // g-r
        ri = scan.nextDouble();   // r-i
        iz = scan.nextDouble();   // i-z
        mbolH = scan.nextDouble();   // M_bol
        mg = scan.nextDouble();   // M_g
        dH = scan.nextDouble();   // D
        dmin = scan.nextDouble();   // D_min
        dmax = scan.nextDouble();   // D_max
        ag = scan.nextDouble();   // Ag
        fracA = scan.nextDouble();   // FracA
        pm = scan.nextDouble();   // PM
        vtan = scan.nextDouble();   // Vtan
        glat = scan.nextDouble();   // Glat
        chi = scan.next();          // Chi
        IF = scan.nextInt();   // IF
        wt = scan.nextDouble();   // Wt

    }

    /** No proper motion correction possible for these */
    public double[] getCoordinates(double ep){

        return new double[]{ra,dec};

    }

}

/** Class for making histograms */
class Histogram{

    // Histogram parameters
    double min;
    double max;
    double step;

    // Histogram data
    double[] hist;
    double sum=0;


    public Histogram(double MIN, double MAX, double STEP){
        min = MIN;
        max = MAX;
        step = STEP;
        int N_BOXES = (int)Math.ceil((max-min)/step);
        hist = new double[N_BOXES];
    }

    public void add(double value){
        int BOX = (int)Math.floor((value-min)/step);
        if(BOX >= 0 && BOX < hist.length){
            hist[BOX]++;
            sum++;
        }
    }

    public void print(){
        for(int b=0; b<hist.length; b++){
            // Histogram points at bin centres
            double BIN_CENTRE = b*step + min + step/2.0;
            System.out.println(BIN_CENTRE + " " + hist[b]/(sum*step));
        }
    }

    /**
     * Mean using rectangular strips;
     * 
     * <x> = INT x*pdf(x)
     *
     */
    public double getMean(){

        double mean = 0;

        for(int b=0; b<hist.length; b++){
            // Histogram points at bin centres
            double BIN_CENTRE = b*step + min + step/2.0;
            mean += BIN_CENTRE * (hist[b]/(sum*step) * step);
        }

        return mean;

    }


}



class Correlation{

    /** Values of variable 1 */
    ArrayList<Double> var1 = new ArrayList<Double>();
    /** Values of variable 2 */
    ArrayList<Double> var2 = new ArrayList<Double>();
    /** Sum of variable 1 */
    double sum1 = 0;
    /** Sum of variable 2 */
    double sum2 = 0;

    public Correlation(){}


    public void add(double val1, double val2){
        var1.add(val1);
        var2.add(val2);
        sum1 += val1;
        sum2 += val2;
    }

    public double getR(){

        double mean1 = sum1 / (double)var1.size();
        double mean2 = sum2 / (double)var2.size();

        double cov=0,std1=0,std2=0;

        for(int v=0; v<var1.size(); v++){

            cov  += (var1.get(v)-mean1)*(var2.get(v)-mean2);
            std1 += (var1.get(v)-mean1)*(var1.get(v)-mean1);
            std2 += (var2.get(v)-mean2)*(var2.get(v)-mean2);

        }

        return cov/Math.sqrt(std1*std2);

    }

}