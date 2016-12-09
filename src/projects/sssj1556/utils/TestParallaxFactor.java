package projects.sssj1556.utils;

import java.io.IOException;

import projects.sssj1556.data.LHS3250;
import projects.sssj1556.data.ParallaxDataset;
import astrometry.Ephemeris;

/**
 *
 * @author nickrowell
 */
public class TestParallaxFactor
{
    
    public static void main(String[] args) throws IOException
    {
        // Get a test Star object
        ParallaxDataset lhs3250 = new LHS3250();
        
        // Get the Earth barycentric position vector interpolator
        Ephemeris ephemeris = Ephemeris.getEphemeris(Ephemeris.Body.EARTH);
        
        // Select an epoch (Julian Day number)
        double epoch = 2454642.041667;
        
        double[] raDec = lhs3250.target.getCoordinatesAtEpoch(lhs3250.master.epoch);
        double ra  = raDec[0];
        double dec = raDec[1];
        
        // Get parallax factors
        double fd = ephemeris.getFd(epoch, ra, dec);
        double fa = ephemeris.getFa(epoch, ra, dec);
        
        System.out.println("Fd = "+fd);
        System.out.println("Fa = "+fa);
        
    }
    
    
}
