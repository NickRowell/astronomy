package sss.survey.dm;

import java.io.*;
import java.util.Scanner;

/**
 * Implementation of {@link WhiteDwarf} specialized for the low proper motion sample.

 * @author nrowell
 * @version $Id$
 */
public class LowMuStar extends WhiteDwarf {

    //+++ The following instance fields are set when a new LowMuStar object is declared +++//

    public long objID;

    //+++ main constructor +++//

    public LowMuStar(String data) throws IOException, Exception{

        super(data);

        Scanner scan = new Scanner(data);
        
        this.objID = scan.nextLong();
        this.ra    = scan.nextDouble();
        this.dec   = scan.nextDouble();
        this.epochR2 = scan.nextDouble();
        
        // Convert to [mas] -> [as]
        this.mu_acosd = scan.nextDouble() / 1000.0;
        this.mu_d  = scan.nextDouble() / 1000.0;
        this.sig_muacosd = scan.nextDouble() / 1000.0;
        this.sig_mud = scan.nextDouble() / 1000.0;
        
        this.b     = scan.nextDouble();
        this.r1    = scan.nextDouble();
        this.r2    = scan.nextDouble();
        this.i     = scan.nextDouble();
        
        this.qb    = scan.nextDouble();
        this.qr1   = scan.nextDouble();
        this.qr2   = scan.nextDouble();
        this.qi    = scan.nextDouble();

        this.pb    = scan.nextDouble();
        this.pr1   = scan.nextDouble();
        this.pr2   = scan.nextDouble();
        this.pi    = scan.nextDouble();
        
        this.eb    = scan.nextDouble();
        this.er1   = scan.nextDouble();
        this.er2   = scan.nextDouble();
        this.ei    = scan.nextDouble();
        
        this.redChi2 = scan.nextDouble();
        this.f     = scan.nextInt();
        
        scan.close();
        
        this.mu = Math.sqrt(this.mu_acosd*this.mu_acosd + this.mu_d*this.mu_d);
      	this.sig_mu = (1.0/this.mu) * Math.sqrt(Math.pow((this.mu_d*this.sig_mud),2) +
						Math.pow((this.mu_acosd*this.sig_muacosd),2));
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean isKnownUCWD() {
    	// There are no known UCWDs in the low proper motion sample.
    	return false;
    }
}