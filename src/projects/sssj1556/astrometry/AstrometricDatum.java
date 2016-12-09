package projects.sssj1556.astrometry;

/**
 * Class provides a convenient wrapper for individual reference star data used 
 * in astrometric fit. The astrometric fit is performed on lists of instances of
 * this class, which makes it easy to implement outlier rejection in a recursive
 * manner.
 * 
 * @author nickrowell
 */
public class AstrometricDatum
{
    /** Parallax factor, parallel to equator. */
    public double F_xsi;
    /** Parallax factor, perpendicular to equator. */
    public double F_eta;
    /** Time baseline to master frame [decimal years]. */
    public double t;
    /** Standard coordinate at observation epoch. */
    public Coordinates2D tp;
    
    public AstrometricDatum(double pF_xsi, double pF_eta, double pt, Coordinates2D ptp)
    {
        F_xsi = pF_xsi;
        F_eta = pF_eta;
        t     = pt;
        tp    = ptp;
    }
    
}