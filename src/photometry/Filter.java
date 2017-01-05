package photometry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import numeric.functions.Linear;

/**
 * Class represents a photometric filter.
 * 
 * The transmission functions are read from plain text files containing two columns. The
 * first column gives the wavelength in Angstroms, the second gives the transmission in the
 * [0:1] range.
 * 
 *
 * @author nrowell
 * @version $Id$
 */
public enum Filter {
	
	// Bolometric magnitude is a special filter type
	M_BOL(null, "M_{bol}"),
	
	// SuperCOSMOS/Schmidt photometric bands
	B_J("sss/B_J.dat", "B_{J}"),
	R_59F("sss/R_59F.dat", "R_{59F}"),
	R_63F("sss/R_63F.dat", "R_{63F}"),
	I_N("sss/I_N.dat", "I_{N}"),
	
	// SDSS bands
	SDSS_U("sdss/u.dat", "u"),
	SDSS_G("sdss/g.dat", "g"),
	SDSS_R("sdss/r.dat", "r"),
	SDSS_I("sdss/i.dat", "i"),
	SDSS_Z("sdss/z.dat", "z"),
	
	// Johnson-Kron-Cousins bands
	U("johnson/U.dat","U"),
	B("johnson/B.dat","B"),
	V("johnson/V.dat","V"),
	R(null,"R"),
	I(null,"I"),
	
	// Stromgren bands
	u(null,"u"),
	b(null,"b"),
	v(null,"v"),
	y(null,"y"),
	
	// IR bands
	J(null,"J"),
	H(null,"H"),
	K(null,"K"),
	
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
	F892N_ACS(null, "F892N (ACS)")
	;
	
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
		
		// Don't attempt to load the file if the transmission function is not available
		if(filename==null) {
			transmission=null;
			lambdaMin = 0;
	        lambdaMax = 0;
			return;
		}
		
		InputStream is = this.getClass().getClassLoader().getResourceAsStream("resources/filters/"+getFilename());

        // Read in data from file
        String line;
        
        // Store values parsed from each line
        List<double[]> records = new LinkedList<double[]>();
        
        try(BufferedReader in = new BufferedReader(new InputStreamReader(is)))
        {
	        while((line=in.readLine())!=null)
	        {
	            // Skip commented lines
	            if(line.substring(0, 1).equals("#"))
	                continue;
	            // Open Scanner on line
	            Scanner scan = new Scanner(line);
	            // Read wavelength (Angstroms)
	            double lambda = scan.nextDouble();
	            // Read transmission
	            double throughput = scan.nextDouble();
	            // Add coordinate to List
	            records.add(new double[]{lambda,throughput});
	            scan.close();
	        }
        }
        catch(IOException e )
        {
        	System.out.println("Unable to read filter transmission function!");
        }
            
        // Now read out data to arrays
        double[] lambda  = new double[records.size()];
        double[] throughput = new double[records.size()];
            
        for(int i=0; i<records.size(); i++)
        {
            lambda[i]  = records.get(i)[0];
            throughput[i] = records.get(i)[1];
        }
        
        transmission = new Linear(lambda,throughput);
        
        lambdaMin = lambda[0];
        lambdaMax = lambda[lambda.length-1];
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
