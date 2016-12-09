package sss.survey.dm;

import java.io.BufferedReader;
import java.io.File;
import java.text.DecimalFormat;

/**
 * Base class for stars drawn from the high and low proper motion samples.
 *
 *
 * @author nrowell
 * @version $Id$
 */
public abstract class WhiteDwarf {


    //+++ Class fields - > list of objects with spectroscopic follow ups +++//
    private static File high_mu_spectroscopic_followups = new File("/spare/SSS/Resources/Spectroscopic_followups/HighPM/objects");
    private static BufferedReader check;
    private static File low_mu_spectroscopic_followups = new File("/spare/SSS/Resources/Spectroscopic_followups/LowPM/objects");
	
    /**
     * Raw record from the input file.
     */
    public String data;

    /**
     * Field number
     */
    public int f;
    
    /**
     * Right ascension [degrees]
     */
    public double ra;
    
    /**
     * Declination [degrees]
     */
    public double dec;
    
    /**
     * Reference epoch [yr], which is that of the second-epoch R observation.
     */
    public double epochR2;
    
    // Proper motion components
    public double mu;
    public double sig_mu;
    public double mu_acosd;
    public double sig_muacosd;
    public double mu_d;
    public double sig_mud;
    
    // Magnitudes
    public double r1;
    public double r2;
    public double b;
    public double i;
    public double redChi2;
    // Image ellipticity
    public double er1;
    public double er2;
    public double eb;
    public double ei;
    // Image quality number
    public double qr1;
    public double qr2;
    public double qb;
    public double qi;
    // Profile classification statistic
    public double pr1;
    public double pr2;
    public double pb;
    public double pi;
    
    /**
     * Hemisphere [N/S]
     */
    public String hemi;

    
    // Derived parameters
    public double fracH;             // Atmosphere fractions
    public double fracHe;

    public double dH;            // Photometric parallaxes and uncertainty
    public double sig_dH;        // for hydrogen and helium atmospheres
    public double dHe;
    public double sig_dHe;

    // The following parameters are given null values on declaration of new object, and are set separately
    public String bestFitH;
    public String bestFitHe;
    public String oneSigmaUpperH;
    public String oneSigmaLowerH;
    public String oneSigmaUpperHe;
    public String oneSigmaLowerHe;

    // Fields of relevance to LF
    public double mBolH;
    public double mBolHe;

    // Does star have spectroscopic follow up?
    public boolean spec;

    // These deal with known UCWDs that have external atmosphere models
    public File ucwdAtmosphere;
    // true -> object has external atmosphere parameters from the literature
    public boolean ucwd;
    public String ucwdName;
    
    /**
     * Main constructor.
     * 
     * TODO: reintroduce tests for spectra, known UCWD etc.
     * 
     * @param data
     * 	The raw record from the data file.
     */
    public WhiteDwarf(String data) {

        this.data = data;

        //+++ Set atmosphere fractions according to b-r colour +++//
//        this.setAtmosphereFractions();
//
//		//+++ Initialise photometric parallaxes to null values +++//
//		this.setPhotoPI();
//
//        this.spectraCheck(flag);
    }

    /**
     * Sets the {@link WhiteDwarf#hemi} field.
     */
    protected final void setHemi() {
		if((this.dec<0)||((this.f>822)&&(this.f<895))) {
			this.hemi = "S";
		}
		else{
			this.hemi = "N";
		}
    }
    
    //+++ Get reduced proper motion value for star using b magnitude +++//
    public double getRPM() {
    	return this.b + 5.0*Math.log10(this.mu) + 5;
    }
    
    //+++ Check whether star has a spectroscopic follow up +++//
//    public void spectraCheck(int flag) throws IOException {
//
//		String new_record;
//		boolean spectra = false;
//	        if(flag==0){
//	            check = new BufferedReader(new FileReader(low_mu_spectroscopic_followups));
//	            while((new_record=check.readLine())!=null){
//		    if(columns(this.data,1).equals(columns(new_record,1))) spectra = true; //compare object IDs with record
//		    for(int j = 0; j<5; j++){check.readLine();}    //scroll through next 5 redundant lines ready for next record
//	
//	            }
//	        }
//	        else{
//	            check = new BufferedReader(new FileReader(high_mu_spectroscopic_followups));
//	            while((new_record=check.readLine())!=null){
//		     if(this.data.substring(6,33).equals(new_record.substring(6,33)))spectra = true; //compare coordinate strings
//		     for(int j = 0; j<5; j++){check.readLine();}    //scroll through next 5 redundant lines ready for next record
//	            }
//	        }
//	
//	        check.close();
//	
//		this.spec = spectra;
//
//    }

//    public void setModels(String models[]){
//
//		this.bestFitH=models[0];
//		this.oneSigmaLowerH=models[1];
//		this.oneSigmaUpperH=models[2];
//		this.bestFitHe=models[3];
//		this.oneSigmaLowerHe=models[4];
//		this.oneSigmaUpperHe=models[5];
//
//        this.mBolH = Double.parseDouble(columns(models[0],4));
//        this.mBolHe = Double.parseDouble(columns(models[3],4));
//
//    }

//    public void setPhotoPI(String models[]){
//
//		double photoPI[] = PhotoPI.photoParallax(this.b, this.r2, this.i, models);
//	
//	 	this.dH      = photoPI[0];
//		this.sig_dH  = photoPI[1];
//		this.dHe     = photoPI[2];
//		this.sig_dHe = photoPI[3];
//
//    }


//    public void fitSyntheticColours(){
//
//		String models[] = PhotoPI.photoFitting(this.b,this.r2,this.i);
//	
//		this.setModels(models);
//	
//		this.setPhotoPI(models);
//
//    }

//    public void setAtmosphereFractions(){
//		this.fracHe = PhotoPI.getHeWeight(this.b-this.r2);
//		this.fracH  = 1.0 - this.fracHe;
//    }

//    public void setPhotoPI(){
//
//		this.dH      = Double.NaN;
//		this.sig_dH  = Double.NaN;
//		this.dHe     = Double.NaN;
//		this.sig_dHe = Double.NaN;
//
//    }


//    public String designation(){
//
//        String RA  = this.raToHMS();
//        String DEC = this.decToDMS();
//
//        String Designation = "SSSJ"+columns(RA,1)+columns(RA,2)+columns(RA,3)+columns(DEC,1)+columns(DEC,2)+columns(DEC,3);
//
//        return Designation;
//
//    }


    /*
     *   Write WD catalogue entry for this object
     *
     */

//    public static String getCatalogueHeader(){
//
//        String header = "Designation                          Astrometric parameters                                   Photometry                           Photometric parallaxes" +
//                        "\n                         ra                dec           epoch        mu_acosd   mu_d     b       r       i     d_H     vt_H   Mbol_H   w_H     d_He    vt_He  Mbol_He  w_He" +
//                        "\n-------------------     --------------------------------------------------------------  ----------------------  ------------------------------------------------------------";
//
//        return header;
//    }

//    public String getCatalogueEntry(){
//
//        DecimalFormat xxpxxxx = new DecimalFormat("00.0000");
//        DecimalFormat xxxpxxxx = new DecimalFormat("000.0000");
//        DecimalFormat xpxxx = new DecimalFormat("0.000");
//        DecimalFormat xpxx  = new DecimalFormat("0.00");
//        DecimalFormat xpx  = new DecimalFormat("0.0");
//
//        return this.designation() + "\t" +
//               xxxpxxxx.format(this.ra) + "\t" + ((this.dec<0) ? xxpxxxx.format(this.dec) : " "+xxpxxxx.format(this.dec)) + "\t" +
//               xpxxx.format(this.epoch) + "\t" +
//               xpx.format(this.mu_acosd*1000.0) + "\t" +xpx.format(this.mu_d*1000.0) + "\t" +
//               xpxxx.format(this.b) + "\t" + xpxxx.format(this.r2) + "\t" + xpxxx.format(this.i) + "\t" +
//               xpx.format(this.dH) + "\t" + xpx.format(4.74 * this.dH * this.mu) + "\t" + xpx.format(Double.parseDouble(columns(this.bestFitH,4))) + "\t" + xpxx.format(this.fracH) + "\t" +
//               xpx.format(this.dHe) + "\t" + xpx.format(4.74 * this.dHe * this.mu) + "\t" + xpx.format(Double.parseDouble(columns(this.bestFitHe,4))) + "\t" + xpxx.format(this.fracHe);
//    }

    //+++ If any UCWDs are identified in proper motion data, add their details +++//
    //+++ here so that code can identify them and get preset atmosphere models +++//

//    public void setPublishedAtmosphere() throws IOException{
//
//		BufferedReader in = new BufferedReader(new FileReader(this.ucwdAtmosphere));
//	
//		//+++ Read in models from lines 2-7 in file +++//
//		in.readLine();
//		String[] models = {in.readLine(),in.readLine(),in.readLine(),in.readLine(),in.readLine(),in.readLine()};
//	
//		// Note - literature models don't give separate H/He atmosphere fits, so I've written the lit. values
//		//        to the entries for both types, and arbitrarily set H fraction to 1 and He fraction to 0.
//		//        Also don't have one-sigma upper and lower T_eff atmosphere fits, so have left these at flag values of -1
//	
//		//+++ Store these models to the star's internal parameters +++//
//		this.setModels(models);
//	
//		//+++ Distance and uncertainty recorded at the end of first model +++//
//	 	this.dH      = Double.parseDouble(columns(models[0],13));
//		this.sig_dH  = Double.parseDouble(columns(models[0],14));
//		this.dHe     = Double.parseDouble(columns(models[3],13));
//		this.sig_dHe = Double.parseDouble(columns(models[3],14));
//	
//        this.mBolH  = Double.parseDouble(columns(models[0],4));
//        this.mBolHe = Double.parseDouble(columns(models[3],4));
//	
//		//+++ Atmosphere fractions +++//
//		this.fracH  = 1.0;
//		this.fracHe = 0.0;
//
//        in.close();
//    }


    /**
     * Determines if this star is a known ultracool WD, by checking against a built-in list
     * of known UCWDs. These are identified based on statistics that vary between the high
     * and low proper motion samples, so it's left for the subclasses to implement the
     * determination.
     * @return
     * 	True of this star is a known ultracool white dwarf, for which the atmosphere solution
     * is taken from the literature rather than from model fitting.
     */
    public abstract boolean isKnownUCWD();

}
