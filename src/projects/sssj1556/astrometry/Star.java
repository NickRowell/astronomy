package projects.sssj1556.astrometry;

import java.util.LinkedList;
import java.util.List;

import time.Epoch;

/**
 * This class is used to represent stars in the parallax investigation, both
 * parallax targets and the reference stars used to place all of the target
 * observations onto the same reference system.
 * 
 * @author nickrowell
 */
public class Star
{
    
    /** Equatorial coordinate (right ascension) at nominal epoch [radians]. */
    public double ra;
    
    /** Equatorial coordinate (declination) at nominal epoch [radians]. */
    public double dec;
    
    /** Nominal epoch corresponding to coordinates. */
    public Epoch nom_epoch;
    
    /** Component of proper motion parallel to equator (arcseconds/year). */
    public double mu_acosd;
    /** Uncertainty. */
    public double sigma_mu_acosd;
    
    /** Component of proper motion perpendicular to equator (arcseconds/year). */
    public double mu_d;
    /** Uncertainty. */
    public double sigma_mu_d;
    
    /** List of StackedImages that this star appears on. */
    List<StackedImage> images;
    
    /** List of Detections of this Star. */
    List<Detection> detections;
    
    public Star(double pra, double pdec, Epoch pnom_epoch, double pmu_acosd,
            double psigma_mu_acosd, double pmu_d, double psigma_mu_d)
    {
        ra             = pra;
        dec            = pdec;
        nom_epoch      = pnom_epoch;
        mu_acosd       = pmu_acosd;
        sigma_mu_acosd = psigma_mu_acosd;
        mu_d           = pmu_d;
        sigma_mu_d     = psigma_mu_d;
        
        images = new LinkedList<StackedImage>();
        detections = new LinkedList<Detection>();
    }
    
    /**
     * Get the proper-motion-corrected celestial coordinates at the reference 
     * epoch.
     * @param ref_epoch Reference epoch
     * @return          Proper motion corrected equatorial coordinates [radians]
     */
    public double[] getCoordinatesAtEpoch(Epoch ref_epoch)
    {
        // Number of Julian days passed between reference and nominal epochs
        double tdiff = (ref_epoch.mjd - nom_epoch.mjd)/Epoch.JULIAN_DAYS_PER_YEAR;
        
        // Remember to include cos(dec) factor when correcting right ascension.
        double ref_ra  = ra + Math.toRadians(mu_acosd/(60*60))*tdiff/Math.cos(dec);
        
        double ref_dec = dec + Math.toRadians(mu_d/(60*60))*tdiff;
        
        return new double[]{ref_ra,ref_dec};
    }
    
        
    /**
     * Get total proper motion (arcseconds per year) from components.
     * @return
     */
    public double getMu()
    {
        return Math.sqrt(mu_acosd*mu_acosd + mu_d*mu_d);
    }
    
    
    /**
     * Projects the celestial coordinates (ra,dec) of the star into the tangent 
     * plane to get the standard coordinates (eta,xsi). Assumes that the RefStar
     * passed in as the argument is the tangent point.
     * @param tangent_point Star to use as tangent point
     * @param epoch         Epoch at which to determine standard coordinates (will
     *                      be proper motion corrected to this epoch).
     * 
     * @return 
     */
    public Coordinates2D getTangentPlaneCoordinates(Star tangent_point, Epoch epoch)
    {
        // Proper motion correct both the active and reference star equatorial
        // coordinates to the desired epoch.
        double[] ra_dec_tp = tangent_point.getCoordinatesAtEpoch(epoch);
        double[] ra_dec    = getCoordinatesAtEpoch(epoch);
        
        return Coordinates2D.getStandardCoordinates(ra_dec, ra_dec_tp);
    }
    
    
    
}
