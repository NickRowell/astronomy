package projects.sssj1556.astrometry;

/**
 * Represents a single observation of a Star in an image.
 * @author nickrowell
 */
public class Detection extends Coordinates2D
{
    
    /** Reference to the Star object that this is a detection of. */
    public Star star;
    
    /** Reference to the StackedImage that this Detection appears on. */
    public StackedImage image;
    
    public Detection(Star pstar, double px, double py, double pcxx, double pcxy, double pcyy)
    {
        super(px, py, pcxx, pcxy, pcyy);
        
        // Handle the additional information about which Star this is a 
        // Detection of
        star = pstar;
        star.detections.add(this);
    }
    
    
    
    /**
     * Used to identify observations of the same star in two images.
     * 
     * @param that
     * @return 
     */
    @Override
    public boolean equals(Object that)
    {
        if(!(that instanceof Detection))
            return false;
        if(this==that)
            return true;
        
        return star == ((Detection)that).star;
    }
    
    
}
