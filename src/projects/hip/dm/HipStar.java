package projects.hip.dm;

import java.util.Scanner;

/**
 * Class represents a single star in the Hipparcos catalogue.
 *
 * @author nrowell
 * @version $Id$
 */
public class HipStar {
	
	/**
	 * Hipparcos identifier
	 */
	public final int HIP;
	
	/**
	 * [0,159] Solution type new reduction (1)
	 */
	public final int Sn;
	
	/**
	 * [0,5] Solution type old reduction (2)
	 */
	public final int So;
	
	/**
	 * Number of components
	 */
	public final int Nc;
	
	/**
	 * Right Ascension in ICRS, Ep=1991.25 [radians]
	 */
	public final double RArad;
	
	/**
	 * Declination in ICRS, Ep=1991.25 [radians]
	 */
	public final double DErad;
	
	/**
	 * Parallax [mas]
	 */
	public final double Plx;
	
	/**
	 * Proper motion in Right Ascension [mas/yr]
	 */
	public final double pmRA;
	
	/**
	 * Proper motion in Declination [mas/yr]
	 */
	public final double pmDE;
	
	/**
	 * Formal error on RArad [mas]
	 */
	public final double e_RArad;
	
	/**
	 * Formal error on DErad [mas]
	 */
	public final double e_DErad;
	
	/**
	 * Formal error on Plx [mas]
	 */
	public final double e_Plx;
	
	/**
	 * Formal error on pmRA [mas/yr]
	 */
	public final double e_pmRA;
	
	/**
	 * Formal error on pmDE [mas/yr]
	 */
	public final double e_pmDE;
	
	/**
	 * Number of field transits used
	 */
	public final int Ntr;
	
	/**
	 * Goodness of fit
	 */
	public final double F2;
	
	/**
	 * Percentage rejected data [%]
	 */
	public final double F1;
	
	/**
	 * Cosmic dispersion added (stochastic solution)
	 */
	public final double var;
	
	/**
	 * Entry in one of the suppl.catalogues
	 */
	public final int ic;
	
	/**
	 * Hipparcos magnitude [mag]
	 */
	public final double Hpmag;
	
	/**
	 * Error on mean Hpmag [mag]
	 */
	public final double e_Hpmag;
	
	/**
	 * Scatter of Hpmag [mag]
	 */
	public final double sHp;
	
	/**
	 * [0,2] Reference to variability annex
	 */
	public final int VA;
	
	/**
	 * B-V colour index [mag]
	 */
	public final double bv;
	
	/**
	 * Formal error on colour B-V index [mag]
	 */
	public final double e_bv;
	
	/**
	 * V-I colour index [mag]
	 */
	public final double vi;
	
	/**
	 * Upper-triangular weight matrix (G1)
	 */
	public final double[] UW;
	
	/**
	 * Main constructor for the {@link HipStar}
	 * @param HIP
	 * 	Hipparcos identifier
	 * @param Sn
	 * 	[0,159] Solution type new reduction (1)
	 * @param So
	 * 	[0,5] Solution type old reduction (2)
	 * @param Nc
	 * 	Number of components
	 * @param RArad
	 * 	Right Ascension in ICRS, Ep=1991.25 [radians]
	 * @param DErad
	 * 	Declination in ICRS, Ep=1991.25 [radians]
	 * @param Plx
	 * 	Parallax [mas]
	 * @param pmRA
	 * 	Proper motion in Right Ascension [mas/yr]
	 * @param pmDE
	 * 	Proper motion in Declination [mas/yr]
	 * @param e_RArad
	 * 	Formal error on RArad [mas]
	 * @param e_DErad
	 * 	Formal error on DErad [mas]
	 * @param e_Plx
	 * 	Formal error on Plx [mas]
	 * @param e_pmRA
	 * 	Formal error on pmRA [mas/yr]
	 * @param e_pmDE
	 * 	Formal error on pmDE [mas/yr]
	 * @param Ntr
	 * 	Number of field transits used
	 * @param F2
	 * 	Goodness of fit
	 * @param F1
	 * 	Percentage rejected data [%]
	 * @param var
	 * 	Cosmic dispersion added (stochastic solution)
	 * @param ic
	 * 	Entry in one of the suppl.catalogues
	 * @param Hpmag
	 * 	Hipparcos magnitude [mag]
	 * @param e_Hpmag
	 * 	Error on mean Hpmag [mag]
	 * @param sHp
	 * 	Scatter of Hpmag [mag]
	 * @param VA
	 * 	[0,2] Reference to variability annex
	 * @param bv
	 * 	B-V colour index [mag]
	 * @param e_bv
	 * 	Formal error on colour B-V index [mag]
	 * @param vi
	 * 	V-I colour index [mag]
	 * @param UW
	 * 	Upper-triangular weight matrix (G1)
	 */
	public HipStar(int HIP, int Sn, int So, int Nc, double RArad, double DErad, double Plx, double pmRA, double pmDE, double e_RArad,
			double e_DErad, double e_Plx, double e_pmRA, double e_pmDE, int Ntr, double F2, double F1, double var, int ic, double Hpmag,
			double e_Hpmag, double sHp, int VA, double bv, double e_bv, double vi, double[] UW) {
		this.HIP = HIP;
		this.Sn = Sn;
		this.So = So;
		this.Nc = Nc;
		this.RArad = RArad;
		this.DErad = DErad;
		this.Plx = Plx;
		this.pmRA = pmRA;
		this.pmDE = pmDE;
		this.e_RArad = e_RArad;
		this.e_DErad = e_DErad;
		this.e_Plx = e_Plx;
		this.e_pmRA = e_pmRA;
		this.e_pmDE = e_pmDE;
		this.Ntr = Ntr;
		this.F2 = F2;
		this.F1 = F1;
		this.var = var;
		this.ic = ic;
		this.Hpmag = Hpmag;
		this.e_Hpmag = e_Hpmag;
		this.sHp = sHp;
		this.VA = VA;
		this.bv = bv;
		this.e_bv = e_bv;
		this.vi = vi;
		this.UW = UW;
	}
	
	/**
	 * Parse a {@link HipStar} from the given entry in the Hipparcos catalogue.
	 * @param data
	 * 	A string containing a single entry in the Hipparcos catalogue.
	 * @return
	 * 	A {@link HipStar}.
	 */
	public static HipStar parseHipStar(String data) {
		
		Scanner scan = new Scanner(data);
		
		int HIP = scan.nextInt();
		int Sn = scan.nextInt();
		int So = scan.nextInt();
		int Nc = scan.nextInt();
		double RArad = scan.nextDouble();
		double DErad = scan.nextDouble();
		double Plx = scan.nextDouble();
		double pmRA = scan.nextDouble();
		double pmDE = scan.nextDouble();
		double e_RArad = scan.nextDouble();
		double e_DErad = scan.nextDouble();
		double e_Plx = scan.nextDouble();
		double e_pmRA = scan.nextDouble();
		double e_pmDE = scan.nextDouble();
		int Ntr = scan.nextInt();
		double F2 = scan.nextDouble();
		double F1 = scan.nextDouble();
		double var = scan.nextDouble();
		int ic = scan.nextInt();
		double Hpmag = scan.nextDouble();
		double e_Hpmag = scan.nextDouble();
		double sHp = scan.nextDouble();
		int VA = scan.nextInt();
		double bv = scan.nextDouble();
		double e_bv = scan.nextDouble();
		double vi = scan.nextDouble();
		double[] UW = new double[15];
		for(int i=0; i<15; i++) {
			UW[i] = scan.nextDouble();
		}
		
		scan.close();
		
		return new HipStar(HIP, Sn, So, Nc, RArad, DErad, Plx, pmRA, pmDE, e_RArad,	e_DErad, e_Plx, e_pmRA, 
				e_pmDE, Ntr, F2, F1, var, ic, Hpmag, e_Hpmag, sHp, VA, bv, e_bv, vi, UW);
	}
	
}