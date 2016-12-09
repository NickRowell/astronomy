package astrometry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import constants.Astronomical;
import numeric.functions.Linear;

/**
 * Class has the sole purpose of reading the Earth ephemeris lookup table and
 * initialising a Vector3Interpolator.
 * @author nickrowell
 */
public class Ephemeris
{
    
    public static enum Body
    {
    	EARTH("earth.txt");
    	
    	String filename;
    	
    	Body(String filename)
    	{
    		this.filename = filename;
    	}
    	
    	String getFilename()
    	{
    		return filename;
    	}
    }
    
    
    /**
     * Linear interpolation object for each vector component.
     */
    protected Linear X1, X2, X3;
	
    /**
     * Start time [Julian day number]
     */
    public double tMin;
    
    /**
     * End time [Julian day number]
     */
    public double tMax;
    
    
    /**
     * Default constructor.
     */
    public Ephemeris()
    {}
    
    /**
     * Main constructor for Ephemeris.
     * @param t Ordered set of time values (min to max).
     * @param x1 Corresponding values for first vector component.
     * @param x2 Corresponding values for second vector component.
     * @param x3 Corresponding values for third vector component.
     */
    public Ephemeris(double[] t, double[] x1, double[] x2, double[] x3) 
    throws RuntimeException
    {
        X1 = new Linear(t,x1);
        X2 = new Linear(t,x2);
        X3 = new Linear(t,x3);
        
        tMin = t[0];
        tMax = t[t.length-1];
    }
    
    /** 
     * Main interpolation method. Function is monotonic so a unique solution
     * is returned, as long as X lies within range of data. An empty array is
     * returned if X is not within range of data.
     * @param t
     * @return 
     */
    public double[] interpolate(double t)
    {
        double X1_int = X1.interpolateY(t)[0]/Astronomical.AU;
        double X2_int = X2.interpolateY(t)[0]/Astronomical.AU;
        double X3_int = X3.interpolateY(t)[0]/Astronomical.AU;
        
        return new double[]{X1_int, X2_int, X3_int};
    }
    
    /**
     * Parallax factor for declination.
     * 
     * TODO: move to astrometry utils
     * 
     * @param jd    Julian Day Number
     * @param ra    Right Ascension [radians]
     * @param dec   Declination [radians]
     * @return 
     */
    public double getFd(double jd, double ra, double dec)
    {
        double[] XYZ = interpolate(jd);
        double X = XYZ[0];
        double Y = XYZ[1];
        double Z = XYZ[2];
        
        return (X*Math.cos(ra)*Math.sin(dec) + Y*Math.sin(ra)*Math.sin(dec) - Z*Math.cos(dec));
    }
    
    /**
     * Parallax factor for right ascension
     * 
     * TODO: move to astrometry utils
     * 
     * @param jd    Julian Day number
     * @param ra    Right Ascension [radians]
     * @param dec   Declination [radians]
     * @return 
     */
    public double getFa(double jd, double ra, double dec)
    {
        double[] XYZ = interpolate(jd);
        double X = XYZ[0];
        double Y = XYZ[1];
        
        return (X*Math.sin(ra) - Y*Math.cos(ra))/Math.cos(dec);
    }
    
    /**
     * Parse ephemeris for the given Body and return an Ephemeris object.
     * @return 
     */
    public static Ephemeris getEphemeris(Body body)
    throws IOException
    {
        InputStream is = (new Ephemeris()).getClass().getClassLoader().getResourceAsStream("resources/ephemerides/"+body.getFilename());
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        
        // Read in data from file
        String line;
        // Store values parsed from each line
        List<double[]> records = new LinkedList<double[]>();
            
        while((line=in.readLine())!=null)
        {
                
            // Skip commented lines
            if(line.substring(0, 1).equals("#"))
                continue;
                
            // Open Scanner on line
            Scanner scan = new Scanner(line);
            // Read Julian Day number
            double t = scan.nextDouble();
            // Read X,Y,Z components of Barycentric Position Vector [KM]
            double x = scan.nextDouble();
            double y = scan.nextDouble();
            double z = scan.nextDouble();
            // Add coordinate to List
            records.add(new double[]{t,x,y,z});
            scan.close();
        }
            
        // Now read out data to arrays
        double[] t  = new double[records.size()];
        double[] x1 = new double[records.size()];
        double[] x2 = new double[records.size()];
        double[] x3 = new double[records.size()];
            
        for(int i=0; i<records.size(); i++)
        {
            t[i]  = records.get(i)[0];
            x1[i] = records.get(i)[1];
            x2[i] = records.get(i)[2];
            x3[i] = records.get(i)[3];
        }
        
        return new Ephemeris(t,x1,x2,x3);
    }
}