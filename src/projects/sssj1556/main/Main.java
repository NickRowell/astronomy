package projects.sssj1556.main;


import java.io.IOException;

import projects.sssj1556.data.LHS3250;

/**
 *
 * @author nickrowell
 */
public class Main
{
    
    
    public static void main(String[] args) throws IOException
    {
        LHS3250 lhs3250 = new LHS3250();
        
        lhs3250.performAstrometryReductions();
    }
    
}
