package projects.sssj1556.utils;

import time.Epoch;



/**
 * Test class for Epoch.
 * @author nickrowell
 */
public class TestEpoch
{
    
    public static void main(String[] args)
    {
        
        // Test constructor
        Epoch test = new Epoch(2007,12,25, 12, 0, 0);
        
        System.out.println(test.toString());
        System.out.println("JD = "+test.jd());
        
        
        
        
    }
    
    
}
