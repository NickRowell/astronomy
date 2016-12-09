package projects.sssj1556.astrometry;

/**
 * Class is a convenient wrapper for pairs of Coordinates2D that correspond to
 * the same reference star observed in two frames. Lists of instances of this
 * class are used to measure linear transformations between frames.
 * @author nickrowell
 */
public class Correspondence
{
    public Coordinates2D A;
    public Coordinates2D B;
    
    public Correspondence(Coordinates2D pA, Coordinates2D pB)
    {
        A = pA;
        B = pB;
    }
    
    
}
