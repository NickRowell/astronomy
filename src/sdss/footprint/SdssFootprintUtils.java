package sdss.footprint;

import Jama.*;
import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 * This class provides SDSS survey utilities. Currently the only function is to handle the survey
 * footprint area for different data releases.
 *
 * TODO: document what each field of the survey stripes means
 * TODO: more robust method to determine if a given string contains a Stripe record
 * 
 * @author nrowell
 * @version $Id$
 */
public class SdssFootprintUtils{
	
	/**
	 * 
	 * @param DR
	 * 	Data release number
	 * @param ra
	 * 	Equatorial right ascension [radians]
	 * @param dec
	 * 	Equatorial declination [radians]
	 * @return
	 * @throws IOException 
	 * 
	 */
    public static boolean isInSDSS(int DR, double ra, double dec) throws IOException {

		// Read the footprint area.
		List<Stripe> footprint = parseDataReleaseFootprint(DR);
		
		return isInSDSS(footprint, ra, dec);
    }
    
    /**
     * 
     * @param footprint
     * 	
     * @param ra
	 * 	Equatorial right ascension [radians]
     * @param dec
	 * 	Equatorial declination [radians]
     * @return
     */
    public static boolean isInSDSS(List<Stripe> footprint, double ra, double dec) {

    	double startMu, endMu;

    	// Position vector in Equatorial coordinates
    	double r[][] = new double[3][1];
    	r[0][0] = Math.cos(dec)*Math.cos(ra);
    	r[1][0] = Math.cos(dec)*Math.sin(ra);
    	r[2][0] = Math.sin(dec);
        Matrix R = new Matrix(r);

    	double cosNucosMu,cosNusinMu,sinNu,Nu;
    	double cosMu,sinMu,Mu;

    	// Check all Stripes in the given data release.
    	for(Stripe stripe : footprint){
    		
    		// Position vector in Great Circle basis for this Stripe
    	    Matrix R_prime = stripe.GTN.times(R);
    	    
    	    //+++ Get great circle basis latitude by analysis of elements of R_prime +++//
    	    cosNucosMu = R_prime.get(0,0);
    	    cosNusinMu = R_prime.get(1,0);
    	    sinNu      = R_prime.get(2,0);

            // Latitude in Great circle frame - star must lie within a few
            // degrees of equator to be included in survey.
    	    Nu = Math.asin(sinNu);

            // Skip rest of loop if latitude places star outside this stripe
            if(Math.abs(Nu)>Stripe.stripe_halfwidth)
                continue;

    	    cosMu = cosNucosMu/Math.cos(Nu);
    	    sinMu = cosNusinMu/Math.cos(Nu);

            // Longitude in Great Circle basis - star must lie within longitude
            // range covered by this stripe in order to be included in survey.
            Mu = Math.toDegrees(Math.atan2(sinMu,cosMu));
            Mu += (Mu<0) ? 360.0 : 0;    // Translate Mu from -180:180 to 0:360
    	    Mu *= 3600.0;                // Scale to arcseconds

    	    // Beginning and end of data range, in terms of great circle longitude coordinate mu
    	    startMu = stripe.startMu;
    	    endMu   = stripe.endMu;

    	    // startMu and endMu sometimes go past 360 degrees (1296000 arcsec):
            if(startMu<1296000 && endMu<=1296000){
                if(Mu>startMu && Mu<endMu)
                    return true;
            }
            // endMu rolled over
            else if (startMu<1296000 && endMu>1296000){
                if(Mu>startMu || (Mu+1296000 < endMu))
                    return true;
            }
            // both rolled over
            else{
                Mu += 1296000;
                if(Mu>startMu && Mu<endMu)
                    return true;
            }

    	}

    	// Didn't find any Stripe containing the point: it's not within footprint area
    	return false;
    }
    
    /**
     * Parse the imaging area footprint for a given SDSS data release.
     * @param dr
     * @return
     * @throws IOException
     */
    public static List<Stripe> parseDataReleaseFootprint(int dr) throws IOException {
    	
    	List<Stripe> stripes = new LinkedList<>();

        // Open reader on file containing survey footprint for desired data release
        InputStream is = (new SdssFootprintUtils()).getClass().getClassLoader().getResourceAsStream("sdss/footprint/res/tsChunk.dr"+dr+".best.par");
        
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
    	
        String stripeStr;
        
        // Read all lines from footprint file
        while((stripeStr=in.readLine())!=null) {
        	
        	// Lines that define a Stripe start with TSSEGLONG
        	Scanner scan = new Scanner(stripeStr);
        	
        	if(scan.hasNext()) {
        		if(scan.next().equalsIgnoreCase("TSSEGLONG")) {
        			stripes.add(Stripe.parse(stripeStr));
        		}
        	}
        	scan.close();
        }
    	
    	return stripes;
    }
}
