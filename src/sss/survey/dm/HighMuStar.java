package sss.survey.dm;

import java.io.*;
import java.util.Scanner;

import astrometry.util.AstrometryUtils;

/**
 * Implementation of {@link WhiteDwarf} specialized for the high proper motion sample.

 * @author nrowell
 * @version $Id$
 */
public class HighMuStar extends WhiteDwarf {

	/**
	 * Epochs of the R1, B and I observations.
	 */
    public double epochR1;
    public double epochB;
    public double epochI;
    
	/**
	 * Blend numbers.
	 */
    public double br1;
    public double br2;
    public double bb;
    public double bi;
    
    /**
     * Record pointers.
     */
    public int recPointerR1;
    public int recPointerR2;
    public int recPointerB;
    public int recPointerI;

    /**
     * Number of plates (max 4) on which the star was detected.
     */
    public int Np;

    /**
     * Position angle [degrees]
     */
    public double theta;

    /**
     * Main constructor.
     * @param data
     * 	The record fora single {@link HighMuStar}
     * @throws IOException
     * @throws Exception
     */
    public HighMuStar(String data) throws IOException, Exception {

        super(data);
    	
    	// Parse all the fields out of the record
    	Scanner scan = new Scanner(data);
    	
    	// This is what a single record in the high proper motion catalogue looks like:
    	// 0002  1 29 40.1061 +81 44 36.029 1996.645    9.594   40.439 17917 18169 19925  17501    9237       7      15      83  2.8350 0.0614       0       0     161 0.1180       0       0    -411 0.0889       0       0    -926 0.2440       0       0     710  3.0854  0.0050  9.0848  0.0042 1954.728 1996.629 1996.719 0 0 0 0 1 1 0 1 1 0 1 1   0.206244E+15   0.999900E+09 1   0.316402E+01   0.115703E+00 3   0.316402E+01   0.115703E+00 3   0.316402E+01   0.115703E+00 3

    	this.f = scan.nextInt();
    	
    	int hour = scan.nextInt();
    	int min  = scan.nextInt();
    	double sec  = scan.nextDouble();
    	this.ra = Math.toDegrees(AstrometryUtils.hmsToRadians(hour, min, sec));
    	
    	String degString = scan.next();
    	// Inspect the first character to determine sign of angle
    	int sign = 0;
    	if(degString.startsWith("+")) {
    		sign = 1;
    	}
    	else if(degString.startsWith("-")) {
    		sign = -1;
    	}
    	int deg = Integer.parseInt(degString.substring(1));
    	int arcmin = scan.nextInt();
    	double arcsec = scan.nextDouble();
    	this.dec = Math.toDegrees(AstrometryUtils.dmsToRadians(sign, deg, arcmin, arcsec));
    	
    	this.epochR2 = scan.nextDouble();
    	this.theta = scan.nextDouble();
    	this.mu = scan.nextDouble();
    	
    	// Convert [mmag] -> [mag]
        this.r1 = scan.nextDouble() / 1000.0;
        this.r2 = scan.nextDouble() / 1000.0;
        this.b = scan.nextDouble() / 1000.0;
        this.i = scan.nextDouble() / 1000.0;
    	
        // Record pointers
        this.recPointerR1 = scan.nextInt();
        this.recPointerR2 = scan.nextInt();
        this.recPointerB = scan.nextInt();
        this.recPointerI = scan.nextInt();
    	
        this.redChi2 = scan.nextDouble();
        
        this.er1 = scan.nextDouble();
        this.br1 = scan.nextDouble();
        this.qr1 = scan.nextDouble();
        // Scale profile statistic to sigmas
        this.pr1 = scan.nextDouble() / 1000.0; 
    	
        this.er2 = scan.nextDouble();
        this.br2 = scan.nextDouble();
        this.qr2 = scan.nextDouble();
        // Scale profile statistic to sigmas
        this.pr2 = scan.nextDouble() / 1000.0; 
        
        this.eb = scan.nextDouble();
        this.bb = scan.nextDouble();
        this.qb = scan.nextDouble();
        // Scale profile statistic to sigmas
        this.pb = scan.nextDouble() / 1000.0; 
        
        // NOTE: there appear to be a very small number of I ellipticities with the value nan:
        // "the intermediate proper motion catalogue gives 'nan' for the I band ellipticity for 
        //  a few obejcts in field 263S. Ideally do away with this once source of problem is identified."
        this.ei = scan.nextDouble();
        this.bi = scan.nextDouble();
        this.qi = scan.nextDouble();
        // Scale profile statistic to sigmas
        this.pi = scan.nextDouble() / 1000.0; 
        
        this.mu_acosd = scan.nextDouble();
        this.sig_muacosd = scan.nextDouble();
    	this.mu_d = scan.nextDouble();
    	this.sig_mud = scan.nextDouble();
        
    	this.epochR1 = scan.nextDouble();
    	this.epochB = scan.nextDouble();
    	this.epochI = scan.nextDouble();
    	
    	
    	// Rest of the record contains the epoch of each observation, and some additional numbers that
    	// I don't know what they mean.
    	
    	scan.close();
    	
      	this.sig_mu = (1.0/this.mu) * Math.sqrt(Math.pow((this.mu_d*this.sig_mud),2) +
						Math.pow((this.mu_acosd*this.sig_muacosd),2));
        
        this.setNP();

        this.setHemi();
    }

    private void setNP(){

		Np=0;
	
		// Magnitude value that indicates non-detection is 99.999
		if(this.b < 90) {
			Np++;
		}
		if(this.r1 < 90) {
			Np++;
		}
		if(this.r2 < 90) {
			Np++;
		}
		if(this.i < 90)  {
			Np++;
		}
        
    }
    
    
    /**
     * {@inheritDoc}
     */
    public boolean isKnownUCWD() {

             //SDSSJ0947
            if ((this.f == 262) && this.hemi.equals("N")
                    && (this.recPointerR1 == 163495)
                    && (this.recPointerR2 == 145623)
                    && (this.recPointerB == 186895)
                    && (this.recPointerI == 101058)) {
                this.ucwdAtmosphere = new File("/spare/SSS/Resources/LocateUCWDs/Gates2004/SDSSJ0947");
                this.ucwd = true;
                this.ucwdName = "Gates2004/SDSSJ0947";
                return true;
            }

            //SDSSJ1452+45
            if ((this.f == 273) && this.hemi.equals("N")
                    && (this.recPointerR1 == 253199)
                    && (this.recPointerR2 == 227616)
                    && (this.recPointerB == 241248)
                    && (this.recPointerI == 145234)) {
                this.ucwdAtmosphere = new File("/spare/SSS/Resources/LocateUCWDs/Harris2008/SDSSJ1452+45");
                this.ucwd = true;
                this.ucwdName = "Harris2008/SDSSJ1452+45";
                return true;
            }

            //SDSSJ1632+24
            if ((this.f == 517) && this.hemi.equals("N")
                    && (this.recPointerR1 == 326720)
                    && (this.recPointerR2 == 209113)
                    && (this.recPointerB == 204253)
                    && (this.recPointerI == 187542)) {
                this.ucwdAtmosphere = new File("/spare/SSS/Resources/LocateUCWDs/Harris2008/SDSSJ1632+24");
                this.ucwd = true;
                this.ucwdName = "Harris2008/SDSSJ1632+24";
                return true;
            }

            //CE 51
            if ((this.f == 496) && this.hemi.equals("S")
                    && (this.recPointerR1 == 436882)
                    && (this.recPointerR2 == 743545)
                    && (this.recPointerB == 708192)
                    && (this.recPointerI == 534286)) {
                this.ucwdAtmosphere = new File("/spare/SSS/Resources/LocateUCWDs/RuizBergeron2001/CE51");
                this.ucwd = true;
                this.ucwdName = "RuizBergeron2001/CE51";
                return true;
            }

            //LHS 1402
            if ((this.f == 415) && this.hemi.equals("S")
                    && (this.recPointerR1 == 105431)
                    && (this.recPointerR2 == 148899)
                    && (this.recPointerB == 191392)
                    && (this.recPointerI == 106632)) {
                this.ucwdAtmosphere = new File("/spare/SSS/Resources/LocateUCWDs/OHDHS/LHS1402");
                this.ucwd = true;
                this.ucwdName = "OHDHS/LHS1402";
                return true;
            }

            // From Harris et al 1999, "A very low luminosity, very cool, DC white dwarf"

            //LHS 3250
            if ((this.f == 101) && this.hemi.equals("N")
                    && (this.recPointerR1 == 58024)
                    && (this.recPointerR2 == 50242)
                    && (this.recPointerB == 53852)
                    && (this.recPointerI == 40844)) {
                this.ucwdAtmosphere = new File("/spare/SSS/Resources/LocateUCWDs/Harris1999/LHS3250");
                this.ucwd = true;
                this.ucwdName = "Harris1999/LHS3250";
                return true;
            }

            //From Rowell et al 2008

            //SSS J1556
            if ((this.f == 727) && this.hemi.equals("S")
                    && (this.recPointerR1 == 205182)
                    && (this.recPointerR2 == 415718)
                    && (this.recPointerB == 486115)
                    && (this.recPointerI == 305360)) {
                this.ucwdAtmosphere = new File("/spare/SSS/Resources/LocateUCWDs/Rowell2008/SSSJ1556");
                this.ucwd = true;
                this.ucwdName = "Rowell2008/SSSJ1556";
                return true;
            }

            //Stars from OHDHS 2001 paper, with atmosphere parameters from Bergeron et al 2005:

            //J0014-3937
	/*if((this.f==293)&&this.hemi.equals("S")&&
            (this.recPointerR1==85413)&&
            (this.recPointerR2==171805)&&
            (this.recPointerB==201362)&&
            (this.recPointerI==84635)){this.ucwdAtmosphere = new File("/spare/SSS/Resources/LocateUCWDs/OHDHS/J0014-3937"); this.ucwd = true; this.ucwdName = "OHDHS/J0014-3937"; return true;}*/
            //WD0044-284
	/*if((this.f==411)&&this.hemi.equals("S")&&
            (this.recPointerR1==135115)&&
            (this.recPointerR2==179524)&&
            (this.recPointerB==249403)&&
            (this.recPointerI==129893)){this.ucwdAtmosphere = new File("/spare/SSS/Resources/LocateUCWDs/OHDHS/WD0044-284"); this.ucwd = true; this.ucwdName = "OHDHS/WD0044-284"; return true;}*/
            //WD 0045-061
	/*if((this.f==753)&&this.hemi.equals("S")&&
            (this.recPointerR1==41476)&&
            (this.recPointerR2==72231)&&
            (this.recPointerB==62900)&&
            (this.recPointerI==33934)){this.ucwdAtmosphere = new File("/spare/SSS/Resources/LocateUCWDs/OHDHS/WD0045-061"); this.ucwd = true; this.ucwdName = "OHDHS/WD 0045-061"; return true;}*/
            //F351-50
	/*if((this.f==351)&&this.hemi.equals("S")&&
            (this.recPointerR1==76550)&&
            (this.recPointerR2==248781)&&
            (this.recPointerB==202593)&&
            (this.recPointerI==132432)){this.ucwdAtmosphere = new File("/spare/SSS/Resources/LocateUCWDs/OHDHS/F351-50"); this.ucwd = true; this.ucwdName = "OHDHS/F351-50"; return true;}*/
            //WD 0100-645
	/*if((this.f==79)&&this.hemi.equals("S")&&
            (this.recPointerR1==112675)&&
            (this.recPointerR2==242229)&&
            (this.recPointerB==249112)&&
            (this.recPointerI==161285)){this.ucwdAtmosphere = new File("/spare/SSS/Resources/LocateUCWDs/OHDHS/WD0100-645"); this.ucwd = true; this.ucwdName = "OHDHS/WD0100-645"; return true;}*/
            //LP 586-51
	/*if((this.f==826)&&this.hemi.equals("S")&&
            (this.recPointerR1==63259)&&
            (this.recPointerR2==92726)&&
            (this.recPointerB==111570)&&
            (this.recPointerI==40863)){this.ucwdAtmosphere = new File("/spare/SSS/Resources/LocateUCWDs/OHDHS/LP586-51"); this.ucwd = true; this.ucwdName = "OHDHS/LP586-51"; return true;}*/
            //WD 0117-268
	/*if((this.f==475)&&this.hemi.equals("S")&&
            (this.recPointerR1==20178)&&
            (this.recPointerR2==41766)&&
            (this.recPointerB==39497)&&
            (this.recPointerI==29280)){this.ucwdAtmosphere = new File("/spare/SSS/Resources/LocateUCWDs/OHDHS/WD0117-268"); this.ucwd = true; this.ucwdName = "OHDHS/WD0117-268"; return true;}*/
            //WD 0135-546
	/*if((this.f==152)&&this.hemi.equals("S")&&
            (this.recPointerR1==167486)&&
            (this.recPointerR2==140759)&&
            (this.recPointerB==198787)&&
            (this.recPointerI==113956)){this.ucwdAtmosphere = new File("/spare/SSS/Resources/LocateUCWDs/OHDHS/WD0135-546"); this.ucwd = true; this.ucwdName = "OHDHS/WD0135-546"; return true;}*/
            //LHS 1274
	/*if((this.f==353)&&this.hemi.equals("S")&&
            (this.recPointerR1==96345)&&
            (this.recPointerR2==198656)&&
            (this.recPointerB==194324)&&
            (this.recPointerI==114913)){this.ucwdAtmosphere = new File("/spare/SSS/Resources/LocateUCWDs/OHDHS/LHS1274"); this.ucwd = true; this.ucwdName = "OHDHS/LHS1274"; return true;}*/
            //WD 0205-053
	/*if((this.f==757)&&this.hemi.equals("S")&&
            (this.recPointerR1==55635)&&
            (this.recPointerR2==81526)&&
            (this.recPointerB==132680)&&
            (this.recPointerI==72875)){this.ucwdAtmosphere = new File("/spare/SSS/Resources/LocateUCWDs/OHDHS/WD0205-053"); this.ucwd = true; this.ucwdName = "OHDHS/WD0205-053"; return true;}*/
            //WD 0214-419
	/*if((this.f==298)&&this.hemi.equals("S")&&
            (this.recPointerR1==23093)&&
            (this.recPointerR2==48958)&&
            (this.recPointerB==45449)&&
            (this.recPointerI==29466)){this.ucwdAtmosphere = new File("/spare/SSS/Resources/LocateUCWDs/OHDHS/WD0214-419"); this.ucwd = true; this.ucwdName = "OHDHS/WD0214-419"; return true;}*/

            //WD 0227-444
	/*if((this.f==246)&&this.hemi.equals("S")&&
            (this.recPointerR1==118985)&&
            (this.recPointerR2==155434)&&
            (this.recPointerB==217373)&&
            (this.recPointerI==115204)){this.ucwdAtmosphere = new File("/spare/SSS/Resources/LocateUCWDs/OHDHS/WD0227-444"); this.ucwd = true; this.ucwdName = "OHDHS/WD0227-444"; return true;}*/
            //LHS 1447
	/*if((this.f==416)&&this.hemi.equals("S")&&
            (this.recPointerR1==67456)&&
            (this.recPointerR2==153667)&&
            (this.recPointerB==152341)&&
            (this.recPointerI==81671)){this.ucwdAtmosphere = new File("/spare/SSS/Resources/LocateUCWDs/OHDHS/LHS1447"); this.ucwd = true; this.ucwdName = "OHDHS/LHS1447"; return true;}*/
            //LP 651-74
	/*if((this.f==760)&&this.hemi.equals("S")&&
            (this.recPointerR1==10840)&&
            (this.recPointerR2==28858)&&
            (this.recPointerB==20398)&&
            (this.recPointerI==13990)){this.ucwdAtmosphere = new File("/spare/SSS/Resources/LocateUCWDs/OHDHS/LP651-74"); this.ucwd = true; this.ucwdName = "OHDHS/LP651-74"; return true;}*/
            //WD 0340-330
	/*if((this.f==358)&&this.hemi.equals("S")&&
            (this.recPointerR1==199000)&&
            (this.recPointerR2==201093)&&
            (this.recPointerB==376355)&&
            (this.recPointerI==167548)){this.ucwdAtmosphere = new File("/spare/SSS/Resources/LocateUCWDs/OHDHS/WD0340-330"); this.ucwd = true; this.ucwdName = "OHDHS/WD0340-330"; return true;}*/
            //WD 2214-390
	/*if((this.f==344)&&this.hemi.equals("S")&&
            (this.recPointerR1==132160)&&
            (this.recPointerR2==240479)&&
            (this.recPointerB==244622)&&
            (this.recPointerI==178620)){this.ucwdAtmosphere = new File("/spare/SSS/Resources/LocateUCWDs/OHDHS/WD2214-390"); this.ucwd = true; this.ucwdName = "OHDHS/WD2214-390"; return true;}*/
            //WD 2242-197
	/*if((this.f==603)&&this.hemi.equals("S")&&
            (this.recPointerR1==93090)&&
            (this.recPointerR2==140587)&&
            (this.recPointerB==170695)&&
            (this.recPointerI==113297)){this.ucwdAtmosphere = new File("/spare/SSS/Resources/LocateUCWDs/OHDHS/WD2242-197"); this.ucwd = true; this.ucwdName = "OHDHS/WD2242-197"; return true;}*/
            //WD 2259-465
	/*if((this.f==290)&&this.hemi.equals("S")&&
            (this.recPointerR1==43600)&&
            (this.recPointerR2==66534)&&
            (this.recPointerB==90257)&&
            (this.recPointerI==42903)){this.ucwdAtmosphere = new File("/spare/SSS/Resources/LocateUCWDs/OHDHS/WD2259-465"); this.ucwd = true; this.ucwdName = "OHDHS/WD2259-465"; return true;}*/
            //LHS 542
	/*if((this.f==821)&&this.hemi.equals("S")&&
            (this.recPointerR1==35198)&&
            (this.recPointerR2==59786)&&
            (this.recPointerB==64238)&&
            (this.recPointerI==49795)){this.ucwdAtmosphere = new File("/spare/SSS/Resources/LocateUCWDs/OHDHS/LHS542"); this.ucwd = true; this.ucwdName = "OHDHS/LHS542"; return true;}*/
            //WD 2324-595
	/*if((this.f==148)&&this.hemi.equals("S")&&
            (this.recPointerR1==124937)&&
            (this.recPointerR2==211918)&&
            (this.recPointerB==174725)&&
            (this.recPointerI==133211)){this.ucwdAtmosphere = new File("/spare/SSS/Resources/LocateUCWDs/OHDHS/WD2324-595"); this.ucwd = true; this.ucwdName = "OHDHS/WD2324-595"; return true;}*/
            //WD 2346-478
	/*if((this.f==240)&&this.hemi.equals("S")&&
            (this.recPointerR1==155570)&&
            (this.recPointerR2==216636)&&
            (this.recPointerB==303693)&&
            (this.recPointerI==172757)){this.ucwdAtmosphere = new File("/spare/SSS/Resources/LocateUCWDs/OHDHS/WD2346-478"); this.ucwd = true; this.ucwdName = "OHDHS/WD2346-478"; return true;}*/
            //WD 2348-548
	/*if((this.f==192)&&this.hemi.equals("S")&&
            (this.recPointerR1==135914)&&
            (this.recPointerR2==141239)&&
            (this.recPointerB==181482)&&
            (this.recPointerI==105020)){this.ucwdAtmosphere = new File("/spare/SSS/Resources/LocateUCWDs/OHDHS/WD2348-548"); this.ucwd = true; this.ucwdName = "OHDHS/WD2348-548"; return true;}*/
            //LHS 4042
	/*if((this.f==349)&&this.hemi.equals("S")&&
            (this.recPointerR1==180388)&&
            (this.recPointerR2==342335)&&
            (this.recPointerB==290308)&&
            (this.recPointerI==152839)){this.ucwdAtmosphere = new File("/spare/SSS/Resources/LocateUCWDs/OHDHS/LHS4042"); this.ucwd = true; this.ucwdName = "OHDHS/LHS4042"; return true;}*/






            // From Gates et al 2005, "Discovery of new UCWDs in the SDSS"

            //SDSS J1220
	/*if((this.f==716)&&this.hemi.equals("N")&&
            (this.recPointerR1==135569)&&
            (this.recPointerR2==117028)&&
            (this.recPointerB==185616)&&
            (this.recPointerI==110231)){this.ucwdAtmosphere = new File("/spare/SSS/Resources/LocateUCWDs/Gates2004/SDSSJ1220"); this.ucwd = true; this.ucwdName = "Gates2004/SDSSJ1220"; return true;}*/
            //SDSS J1403
	/*if((this.f==271)&&this.hemi.equals("N")&&
            (this.recPointerR1==286881)&&
            (this.recPointerR2==275523)&&
            (this.recPointerB==270318)&&
            (this.recPointerI==196049)){this.ucwdAtmosphere = new File("/spare/SSS/Resources/LocateUCWDs/Gates2004/SDSSJ1403"); this.ucwd = true; this.ucwdName = "Gates2004/SDSSJ1403"; return true;}*/




            //From Hambly et al 1999

            //WD 0346+246
	/*if((this.f==482)&&this.hemi.equals("N")&&
            (this.recPointerR1==402208)&&
            (this.recPointerR2==167838)&&
            (this.recPointerB==387706)&&
            (this.recPointerI==131603)){this.ucwdAtmosphere = new File("/spare/SSS/Resources/LocateUCWDs/Hambly1999/WD0346+246"); this.ucwd = true; this.ucwdName = "Hambly1999/WD0346+246"; return true;}*/


        this.ucwdAtmosphere = null;
		this.ucwd = false;
		this.ucwdName = "Not an identified ucwd";
	
		return false;

    }

    

}
