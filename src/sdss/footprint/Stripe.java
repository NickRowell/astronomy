package sdss.footprint;

import java.util.Scanner;

import Jama.Matrix;

/**
 * Class represents a single stripe in the SDSS footprint.
 *
 * TODO: document/comment each field
 * TODO: document the SDSS survey coordinate system
 *
 * @author nrowell
 * @version $Id$
 */
public class Stripe {
	
	/**
	 * The half-width of a Stripe [radians].
	 */
	public static final double stripe_halfwidth = Math.toRadians(1.25);
	
	public final String tsseglong;
	public final int int0;
	public final int int1;
	public final int int2;
	
	/**
	 * Stripe number
	 */
	public final int stripeNum;
	
	public final String hemi;
	
	/**
	 * Start Mu
	 */
	public final int startMu;
	
	/**
	 * End Mu
	 */
	public final int endMu;
	
	public final int int6;
	public final int int7;
	public final String primary;
	public final int int8;
	public final int int9;
	
	
	// Derived quantities
	public final double eta;
	
	/**
	 * Transformation matrix that rotates vectors from the SDSS survey basis to
	 * the Great Circle basis appropriate for this {@link Stripe}.
	 */
	public final Matrix GTS;
	
	/**
	 * Transformation matrix that rotates vectors from the Equatorial basis to
	 * Great Circle basis appropriate for this {@link Stripe}.
	 */
	public final Matrix GTN;
	
	
	/**
	 * Main constructor for the {@link Stripe}.
	 * @param tsseglong
	 * @param int0
	 * @param int1
	 * @param int2
	 * @param stripeNum
	 * @param hemi
	 * @param startMu
	 * @param endMu
	 * @param int6
	 * @param int7
	 * @param primary
	 * @param int8
	 * @param int9
	 */
	public Stripe(String tsseglong, int int0, int int1, int int2, int stripeNum, String hemi,
			int startMu, int endMu, int int6, int int7, String primary, int int8, int int9) {
		
		this.tsseglong = tsseglong;
		this.int0 = int0;
		this.int1 = int1;
		this.int2 = int2;
		this.stripeNum = stripeNum;
		this.hemi = hemi;
		this.startMu = startMu;
		this.endMu = endMu;
		this.int6 = int6;
		this.int7 = int7;
		this.primary = primary;
		this.int8 = int8;
		this.int9 = int9;
		
		// Derived quantities
		eta = computeEta();
		GTS = computeGTS();
		GTN = computeGTN();
	}
	
	/**
	 * Compute the eta coordinate that defines the coordinate frame for this {@link Stripe}.
	 * @return
	 * 	The eta coordinate that defines the coordinate frame for this {@link Stripe} [radians]
	 */
	private final double computeEta() {
		return (stripeNum<76) ? Math.toRadians((stripeNum - 10)*2.5 - 32.5) : Math.toRadians((stripeNum - 82)*2.5 - 32.5);
	}
	
	/**
	 * Compute the transformation matrix that rotates vectors from the survey basis to
	 * the Great Circle basis appropriate for this {@link Stripe}.
	 * @return
	 * 	Matrix that rotates vectors from the survey basis to
	 * the Great Circle basis appropriate for this {@link Stripe}.
	 */
	private final Matrix computeGTS() {
		double[][] gts = {{-1.0*Math.cos(eta), -1.0*Math.sin(eta),0},
	              {0,0,1},
	              {-1.0*Math.sin(eta),Math.cos(eta),0}};
		return new Matrix(gts);
	}
	
	/**
	 * Compute the transformation matrix that rotates vectors from the Equatorial basis to
	 * the Great Circle basis appropriate for this {@link Stripe}.
	 * @return
	 * 	Matrix that rotates vectors from the Equatorial basis to
	 * the Great Circle basis appropriate for this {@link Stripe}.
	 */
	private final Matrix computeGTN() {
		return GTS.times(sdss.footprint.Constants.STN);
	}
	
	/**
	 * Parse a {@link Stripe} from the String.
	 * @param stripeStr
	 * 	A String containing the {@link Stripe} fields in the standard SDSS footprint format.
	 * @return
	 * 	A {@link Stripe} parsed from the String.
	 */
	public static Stripe parse(String stripeStr) {
		
		Scanner scan = new Scanner(stripeStr);
		
		String tsseglong = scan.next();
		int int0 = scan.nextInt();
		int int1 = scan.nextInt();
		int int2 = scan.nextInt();
		int int3 = scan.nextInt();
		String hemi = scan.next();
		int int4 = scan.nextInt();
		int int5 = scan.nextInt();
		int int6 = scan.nextInt();
		int int7 = scan.nextInt();
		String primary = scan.next();
		int int8 = scan.nextInt();
		int int9 = scan.nextInt();
		
		scan.close();
		
		return new Stripe(tsseglong, int0, int1, int2, int3, hemi, int4, int5, int6, int7, primary, int8, int9);
	}
	
}
