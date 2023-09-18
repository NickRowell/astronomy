package photometry;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import infra.Quantity;
import numeric.functions.Linear;
import util.ParseUtil;

/**
 * This enum represents magnitudes in particular photometric passbands. It also contains the filter transmission
 * functions in most cases.
 * 
 * The transmission functions are read from plain text files containing two columns. The
 * first column gives the (increasing) wavelength in Angstroms, the second gives the transmission in the
 * [0:1] range.
 * 
 * @author nrowell
 * @version $Id$
 */
public enum Filter implements Quantity<Filter> {
	
	/** Bolometric magnitude: a special class of filter */
	M_BOL(null, "M_{bol}"),
	
	// SuperCOSMOS/Schmidt photometric bands
	
	/** SuperCOSMOS/Schmidt B_{J} band */
	B_J("sss/B_J.dat", "B_{J}"),
	/** SuperCOSMOS/Schmidt R_{59F} band */
	R_59F("sss/R_59F.dat", "R_{59F}"),
	/** SuperCOSMOS/Schmidt R_{63F} band */
	R_63F("sss/R_63F.dat", "R_{63F}"),
	/** SuperCOSMOS/Schmidt I_{N} band */
	I_N("sss/I_N.dat", "I_{N}"),
	
	// SDSS bands
	
	/** SDSS u band */
	SDSS_U("sdss/u.dat", "u"),
	/** SDSS g band */
	SDSS_G("sdss/g.dat", "g"),
	/** SDSS r band */
	SDSS_R("sdss/r.dat", "r"),
	/** SDSS i band */
	SDSS_I("sdss/i.dat", "i"),
	/** SDSS z band */
	SDSS_Z("sdss/z.dat", "z"),
	
	// Johnson-Cousins bands
	
	/** Johnson-Cousins U band */
	U("johnson/Uj.dat", "U"),
	/** Johnson-Cousins B band */
	B("johnson/Bj.dat", "B"),
	/** Johnson-Cousins V band */
	V("johnson/Vj.dat", "V"),
	/** Johnson-Cousins R band */
	R("johnson/Rj.dat", "R"),
	/** Johnson-Cousins I band */
	I("johnson/Ij.dat", "I"),
	
	// Stromgren bands
	
	/** Stromgren u band */
	u(null, "u"),
	/** Stromgren b band */
	b(null, "b"),
	/** Stromgren v band */
	v(null, "v"),
	/** Stromgren y band */
	y(null, "y"),
	
	// IR bands
	
	/** Near-infrared J band */
	J(null, "J"),
	/** Near-infrared H band */
	H(null, "H"),
	/** Near-infrared K band */
	K(null, "K"),
	
	// HST: WFC3 IR bands
	F110W_WFC3_IR("hst/wfc_f110w.IR.tab_proc","F110W"),
	F160W_WFC3_IR("hst/wfc_f160w.IR.tab_proc","F160W"),
	// HST: WFC3 UVIS bands
	F390W_WFC3_UVIS("hst/f390w.UVIS1.tab_proc", "F390W"),
	F606W_WFC3_UVIS("hst/f606w.UVIS1.tab_proc", "F606W (WFC3)"),
	
	// HST: ACS bands
	F435W_ACS(null, "F435W (ACS)"),
	F475_ACS(null, "F475 (ACS)"),
	F502N_ACS(null, "F502N (ACS)"),
	F550M_ACS(null, "F550M (ACS)"),
	F555W_ACS(null, "F555W (ACS)"),
	F606W_ACS("hst/acs_F606W.dat", "F606W (ACS)"),
	F625W_ACS(null, "F625W (ACS)"),
	F658N_ACS(null, "F658N (ACS)"),
	F660N_ACS(null, "F660N (ACS)"),
	F775W_ACS(null, "F775W (ACS)"),
	F814W_ACS("hst/acs_F814W_cleaned.dat", "F814W (ACS)"),
	F850LP_ACS(null, "F850LP (ACS)"),
	F892N_ACS(null, "F892N (ACS)"),
	
	// Gaia nominal bands
	
	/** Nominal Gaia G band */
	G_NOM_DR2("gaia/G_energy.txt","G (DR2 nom)"),
	/** Nominal Gaia BP band */
	BP_NOM_DR2("gaia/BP_energy.txt","BP (DR2 nom)"),
	/** Nominal Gaia RP band */
	RP_NOM_DR2("gaia/RP_energy.txt","RP (DR2 nom)"),
	/** Revised DR2 Gaia G band */
	G_REV_DR2("gaia/G_energy.txt","G (DR2 rev)"),
	/** Revised DR2 Gaia BP band */
	BP_REV_DR2("gaia/BP_energy.txt","BP (DR2 rev)"),
	/** Revised DR2 Gaia RP band */
	RP_REV_DR2("gaia/RP_energy.txt","RP (DR2 rev)"),
	/** DR3 Gaia G band */
	G_DR3(null,"G (DR3)"),
	/** DR3 Gaia BP band */
	BP_DR3(null,"BP (DR3)"),
	/** DR3 Gaia RP band */
	RP_DR3(null,"RP (DR3)")
	;
	
	/**
	 * Johnson bandshttp://www.aip.de/en/research/facilities/stella/instruments/data/johnson-ubvri-filter-curves
	 */
	public static Filter[] johnson = new Filter[]{U, B, V, R, I};
	
	/**
	 * Stromgren bands.
	 */
	public static Filter[] Stromgren = new Filter[]{Filter.u, Filter.b, Filter.v, Filter.y};
	
	/**
	 * HST bands
	 */
	public static Filter[] hst = new Filter[]{Filter.F110W_WFC3_IR, Filter.F160W_WFC3_IR, Filter.F390W_WFC3_UVIS, Filter.F606W_WFC3_UVIS, Filter.F606W_ACS, Filter.F814W_ACS};
	
	/**
	 * SuperCOSMOS bands
	 */
	public static Filter[] sss = new Filter[]{Filter.B_J, Filter.R_59F, Filter.R_63F, Filter.I_N};
	
	/**
	 * Sloan Digital Sky Survey bands
	 */
	public static Filter[] sdss = new Filter[]{Filter.SDSS_U, Filter.SDSS_G, Filter.SDSS_R, Filter.SDSS_I, Filter.SDSS_Z};
	
	/**
	 * Gaia nominal passbands
	 */
	public static Filter[] gaia = new Filter[]{Filter.G_NOM_DR2, Filter.BP_NOM_DR2, Filter.RP_NOM_DR2};
	
	/**
	 * Contains subdirectory path within src/resources/filters where
	 * the data file for this filter is located.
	 */
	String filename;
	
	/**
	 * Human-readable filter name for use in plot titles etc.
	 */
	String filtername;
	
	/** 
	 * Linear interpolation object for filter transmission as function of wavelength.
	 */
    protected Linear transmission;
	
    /**
     * Start wavelength [Angstroms]
     */
    public double lambdaMin;
    
    /**
     * End wavelength [Angstroms]
     */
    public double lambdaMax;
    
    /**
     * The main  constructor.
     * 
     * @param filename
     * 	The name of the file containing the tabulated filter transmission function. If this is
     * not available then null is passed in.
     * @param filtername
     * 	The human-readable name of the filter
     */
	Filter(String filename, String filtername)
	{
		this.filtername = filtername;
		this.filename = filename;
		
		transmission=null;
		lambdaMin = 0;
        lambdaMax = 0;
		
		// Don't attempt to load the file if the transmission function is not available
		if(filename==null) {
			return;
		}
		
		InputStream is = this.getClass().getClassLoader().getResourceAsStream("resources/filters/"+getFilename());

        try(BufferedReader in = new BufferedReader(new InputStreamReader(is)))
        {
        	double[][] data = ParseUtil.parseFile(in, ParseUtil.whitespaceDelim, ParseUtil.hashComment);
        	
        	// First column contains the wavelength [Angstroms], second column contains the throughput
	        transmission = new Linear(data[0], data[1]);
	        lambdaMin = data[0][0];
	        lambdaMax = data[0][data[0].length-1];
        }
        catch(IOException e )
        {
        	System.out.println("Unable to read filter transmission function!");
        }
	}
	
	/**
	 * Get the name of the {@link File} containing the transmission function data for
	 * this {@link Filter}.
	 * @return
	 * 	The name of the {@link File} containing the transmission function data for
	 * this {@link Filter}.
	 */
	public String getFilename()
	{
		return filename;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Enum#toString()
	 */
	public String toString() {
		return filtername;
	}
	
	/**
	 * Get the transmission function for this {@link Filter}.
	 * @return
	 *  The transmission function for this {@link Filter}
	 */
	public Linear getTransmission() {
		if(transmission==null) {
			throw new RuntimeException("Filter "+this+" does not have transmission function data associated with it.");
		}
		return transmission;
	}
	
    /** 
     * Interpolate the filter transmission at wavelength <code>lambda</code>.
     * @param lambda
     * 	The wavelength [Angstroms]
     * @return 
     * 	The filter transmission
     */
    public double interpolate(double lambda)
    {
        return getTransmission().interpolateY(lambda)[0];
    }
    
}