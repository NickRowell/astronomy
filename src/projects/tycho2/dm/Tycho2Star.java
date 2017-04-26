package projects.tycho2.dm;

import java.util.Arrays;

/**
 * Class represents a single star in the Tycho-2 catalogue.
 *
 * @author nrowell
 * @version $Id$
 */
public class Tycho2Star {
	
	/**
	 * First component of the TYC identifier:
	 * "The TYC identifier is constructed from the GSC region number
	 * (TYC1), the running number within the region (TYC2) and a component
	 * identifier (TYC3) which is normally 1. Some non-GSC running numbers
	 * were constructed for the first Tycho Catalogue and for Tycho-2.
	 * The recommended star designation contains a hyphen between the
	 * TYC numbers, e.g. TYC 1-13-1."
	 */
	public final int tyc1;

	/**
	 * Second component of the TYC identifier: 
	 * "The TYC identifier is constructed from the GSC region number
	 * (TYC1), the running number within the region (TYC2) and a component
	 * identifier (TYC3) which is normally 1. Some non-GSC running numbers
	 * were constructed for the first Tycho Catalogue and for Tycho-2.
	 * The recommended star designation contains a hyphen between the
	 * TYC numbers, e.g. TYC 1-13-1."
	 */
	public final int tyc2;

	/**
	 * Third component of the TYC identifier: 
	 * "The TYC identifier is constructed from the GSC region number
	 * (TYC1), the running number within the region (TYC2) and a component
	 * identifier (TYC3) which is normally 1. Some non-GSC running numbers
	 * were constructed for the first Tycho Catalogue and for Tycho-2.
	 * The recommended star designation contains a hyphen between the
	 * TYC numbers, e.g. TYC 1-13-1."
	 */
	public final int tyc3;

	/**
	 *  Mean position flag:
	 *  "' ' = normal mean position and proper motion.
	 *   'P' = the mean position, proper motion, etc., refer to the
	 *         photocentre of two Tycho-2 entries, where the BT magnitudes
	 *         were used in weighting the positions.
	 *   'X' = no mean position, no proper motion."
	 */
	public final String pflag;
	
	/**
	 * Mean Right Asc, ICRS, epoch=J2000, or Double.NaN if this is not available [degrees]
	 * "The mean position is a weighted mean for the catalogues contributing
	 * to the proper motion determination. This mean has then been brought to
	 * epoch 2000.0 by the computed proper motion. See Note(2) above for
	 * details. Tycho-2 is one of the several catalogues used to determine
	 * the mean position and proper motion. The observed Tycho-2 position is
	 * given in the fields RAdeg and DEdeg."
	 */
	public final double RAmdeg;

	/**
	 * Mean Decl, ICRS, at epoch=J2000, or Double.NaN if this is not available [degrees]
	 * "The mean position is a weighted mean for the catalogues contributing
	 * to the proper motion determination. This mean has then been brought to
	 * epoch 2000.0 by the computed proper motion. See Note(2) above for
	 * details. Tycho-2 is one of the several catalogues used to determine
	 * the mean position and proper motion. The observed Tycho-2 position is
	 * given in the fields RAdeg and DEdeg."
	 */
	public final double DEmdeg;
	
	/**
	 * Proper motion in RA*cos(dec), or Double.NaN if this is not available [mas/yr]
	 */
	public final double pmRA;
	
	/**
	 * Proper motion in Dec, or Double.NaN if this is not available [mas/yr]
	 */
	public final double pmDE;
	
	/**
	 * Standard error in RA*cos(dec) at mean epoch, or Integer.MIN_VALUE if this is not available [mas]
	 */
	public final int e_RAmdeg;
	
	/**
	 * Standard error in Dec at mean epoch, or Integer.MIN_VALUE if this is not available [mas]
	 */
	public final int e_DEmdeg;
	
	/**
	 * Standard error in proper motion in RA*cos(dec), or Double.NaN if this is not available [mas/yr]
	 */
	public final double e_pmRA;
	
	/**
	 * Standard error in proper motion in Dec, or Double.NaN if this is not available [mas/yr]
	 */
	public final double e_pmDE;
	
	/**
	 * Mean epoch of RA, or Double.NaN if this is not available [yr]
	 */
	public final double EpRAm;
	
	/**
	 * Mean epoch of Dec, or Double.NaN if this is not available [yr]
	 */
	public final double EpDEm;
	
	/**
	 * Number of positions used, or Integer.MIN_VALUE if this is not available [-]
	 */
	public final int Num;
	
	/**
	 * Goodness of fit for mean RA, or Double.NaN if this is not available [-]
	 * "This goodness of fit is the ratio of the scatter-based and the
	 * model-based error. It is only defined when Num > 2. Values
	 * exceeding 9.9 are truncated to 9.9."
	 */
	public final double q_RAmdeg;
	
	/**
	 * Goodness of fit for mean Dec, or Double.NaN if this is not available [-]
	 * "This goodness of fit is the ratio of the scatter-based and the
	 * model-based error. It is only defined when Num > 2. Values
	 * exceeding 9.9 are truncated to 9.9."
	 */
	public final double q_DEmdeg;
	
	/**
	 * Goodness of fit for pmRA, or Double.NaN if this is not available [-]
	 * "This goodness of fit is the ratio of the scatter-based and the
	 * model-based error. It is only defined when Num > 2. Values
	 * exceeding 9.9 are truncated to 9.9."
	 */
	public final double q_pmRA;
	
	/**
	 * Goodness of fit for pmDE, or Double.NaN if this is not available [-]
	 * "This goodness of fit is the ratio of the scatter-based and the
	 * model-based error. It is only defined when Num > 2. Values
	 * exceeding 9.9 are truncated to 9.9."
	 */
	public final double q_pmDE;
	
	/**
	 * Tycho-2 BT magnitude, or Double.NaN if this is not available [mag]
	 * "Blank when no magnitude is available. Either BTmag or VTmag is
	 * always given. Approximate Johnson photometry may be obtained as:
	 * V   = VT -0.090*(BT-VT)
	 * B-V = 0.850*(BT-VT)
	 * Consult Sect 1.3 of Vol 1 of "The Hipparcos and Tycho Catalogues",
	 * ESA SP-1200, 1997, for details."
	 */
	public final double BTmag;
	
	/**
	 * Standard error on the BT magnitude, or Double.NaN if this is not available [mag]
	 * "Blank when no magnitude is available. Either BTmag or VTmag is
	 * always given. Approximate Johnson photometry may be obtained as:
	 * V   = VT -0.090*(BT-VT)
	 * B-V = 0.850*(BT-VT)
	 * Consult Sect 1.3 of Vol 1 of "The Hipparcos and Tycho Catalogues",
	 * ESA SP-1200, 1997, for details."
	 */
	public final double e_BTmag;
	
	/**
	 * Tycho-2 VT magnitude, or Double.NaN if this is not available [mag]
	 * "Blank when no magnitude is available. Either BTmag or VTmag is
	 * always given. Approximate Johnson photometry may be obtained as:
	 * V   = VT -0.090*(BT-VT)
	 * B-V = 0.850*(BT-VT)
	 * Consult Sect 1.3 of Vol 1 of "The Hipparcos and Tycho Catalogues",
	 * ESA SP-1200, 1997, for details."
	 */
	public final double VTmag;
	
	/**
	 * Standard error on the VT magnitude, or Double.NaN if this is not available [mag]
	 * "Blank when no magnitude is available. Either BTmag or VTmag is
	 * always given. Approximate Johnson photometry may be obtained as:
	 * V   = VT -0.090*(BT-VT)
	 * B-V = 0.850*(BT-VT)
	 * Consult Sect 1.3 of Vol 1 of "The Hipparcos and Tycho Catalogues",
	 * ESA SP-1200, 1997, for details."
	 */
	public final double e_VTmag;
	
	/**
	 * Proximity indicator [0.1 arcsec]
	 * "Distance in units of 100 mas to the nearest entry in the Tycho-2
	 * main catalogue or supplement. The distance is computed for the
	 * epoch 1991.25. A value of 999 (i.e. 99.9 arcsec) is given if the
	 * distance exceeds 99.9 arcsec."
	 */
	public final int prox;
	
	/**
	 * Tycho-1 star [-]
	 * "' ' = no Tycho-1 star was found within 0.8 arcsec (quality 1-8)
	 *        or 2.4 arcsec (quality 9).
	 *  'T' = this is a Tycho-1 star. The Tycho-1 identifier is given in the
	 *        beginning of the record. For Tycho-1 stars, resolved in
	 *        Tycho-2 as a close pair, both components are flagged as
	 *        a Tycho-1 star and the Tycho-1 TYC3 is assigned to the
	 *        brightest (VT) component.
	 *  The HIP-only stars given in Tycho-1 are not flagged as Tycho-1 stars."
	 */
	public final String TYC;
	
	/**
	 * Hipparcos number, or Integer.MIN_VALUE if the star is not in the Hipparcos catalogue [-]
	 */
	public final int HIP;
	
	/**
	 * CCDM component identifier for HIP stars [-]
	 * "The CCDM component identifiers for double or multiple Hipparcos stars
	 * contributing to this Tycho-2 entry. For photocentre solutions, all
	 * components within 0.8 arcsec contribute. For double star solutions any
	 * unresolved component within 0.8 arcsec contributes. For single star
	 * solutions, the predicted signal from close stars were normally
	 * subtracted in the analysis of the photon counts and such stars
	 * therefore do not contribute to the solution. The components are given
	 * in lexical order."
	 */
	public final String CCDM;
	
	/**
	 * Observed Tycho-2 Right Ascension, ICRS [degrees]
	 */
	public final double RAdeg;
	
	/**
	 * Observed Tycho-2 Declination, ICRS [degrees]
	 */
	public final double DEdeg;
	
	/**
	 * Epoch-1990 of RAdeg [yr]
	 */
	public final double EpRA_1990;
	
	/**
	 * Epoch-1990 of DEdeg [yr]
	 */
	public final double EpDE_1990;
	
	/**
	 * Standard error in RA*cos(dec) of observed Tycho-2 RA [mas]
	 */
	public final double e_RAdeg;
	
	/**
	 * Standard error in observed Tycho-2 Dec [mas]
	 */
	public final double e_DEdeg;
	
	/**
	 * Type of Tycho-2 solution [-]
	 * "' ' = normal treatment, close stars were subtracted when possible.
	 *  'D' = double star treatment. Two stars were found. The companion is
	 *        normally included as a separate Tycho-2 entry, but may have
	 *        been rejected.
	 *  'P' = photocentre treatment, close stars were not subtracted. This
	 *        special treatment was applied to known or suspected doubles
	 *        which were not successfully (or reliably) resolved in the
	 *        Tycho-2 double star processing."
	 */
	public final String posflg;
	
	/**
	 * Correlation (RAdeg,DEdeg) [-]
	 */
	public final double corr;
	
	
	/**
	 * Main constructor for the {@link Tycho2Star}.
	 * 
	 * @param tyc1
	 * 	First component of the TYC identifier
	 * @param tyc2
	 * 	Second component of the TYC identifier
	 * @param tyc3
	 * 	Third component of the TYC identifier
	 * @param pflag
	 * 	Mean position flag
	 * @param RAmdeg
	 * 	Mean Right Asc, ICRS, epoch=J2000, or Double.NaN if this is not available [degrees]
	 * @param DEmdeg
	 * 	Mean Decl, ICRS, at epoch=J2000, or Double.NaN if this is not available [degrees]
	 * @param pmRA
	 * 	Proper motion in RA*cos(dec), or Double.NaN if this is not available [mas/yr]
	 * @param pmDE
	 * 	Proper motion in Dec, or Double.NaN if this is not available [mas/yr]
	 * @param e_RAmdeg
	 * 	Standard error in RA*cos(dec) at mean epoch, or Integer.MIN_VALUE if this is not available [mas]
	 * @param e_DEmdeg
	 * 	Standard error in Dec at mean epoch, or Integer.MIN_VALUE if this is not available [mas]
	 * @param e_pmRA
	 * 	Standard error in proper motion in RA*cos(dec), or Double.NaN if this is not available [mas/yr]
	 * @param e_pmDE
	 * 	Standard error in proper motion in Dec, or Double.NaN if this is not available [mas/yr]
	 * @param EpRAm
	 * 	Mean epoch of RA, or Double.NaN if this is not available [yr]
	 * @param EpDEm
	 * 	Mean epoch of Dec, or Double.NaN if this is not available [yr]
	 * @param Num
	 * 	Number of positions used, or Integer.MIN_VALUE if this is not available [-]
	 * @param q_RAmdeg
	 * 	Goodness of fit for mean RA, or Double.NaN if this is not available [-]
	 * @param q_DEmdeg
	 * 	Goodness of fit for mean Dec, or Double.NaN if this is not available [-]
	 * @param q_pmRA
	 * 	Goodness of fit for pmRA, or Double.NaN if this is not available [-]
	 * @param q_pmDE
	 * 	Goodness of fit for pmDE, or Double.NaN if this is not available [-]
	 * @param BTmag
	 * 	Tycho-2 BT magnitude, or Double.NaN if this is not available [mag]
	 * @param e_BTmag
	 * 	Standard error on the BT magnitude, or Double.NaN if this is not available [mag]
	 * @param VTmag
	 * 	Tycho-2 VT magnitude, or Double.NaN if this is not available [mag]
	 * @param e_VTmag
	 * 	Standard error on the VT magnitude, or Double.NaN if this is not available [mag]
	 * @param prox
	 * 	Proximity indicator [0.1 arcsec]
	 * @param TYC
	 * 	Tycho-1 star [-]
	 * @param HIP
	 * 	Hipparcos number, or Integer.MIN_VALUE if the star is not in the Hipparcos catalogue [-]
	 * @param CCDM
	 * 	CCDM component identifier for HIP stars [-]
	 * @param RAdeg
	 * 	Observed Tycho-2 Right Ascension, ICRS [degrees]
	 * @param DEdeg
	 * 	Observed Tycho-2 Declination, ICRS [degrees]
	 * @param EpRA_1990
	 * 	Epoch-1990 of RAdeg [yr]
	 * @param EpDE_1990
	 * 	Epoch-1990 of DEdeg [yr]
	 * @param e_RAdeg
	 * 	Standard error in RA*cos(dec) of observed Tycho-2 RA [mas]
	 * @param e_DEdeg
	 * 	Standard error in observed Tycho-2 Dec [mas]
	 * @param posflg
	 * 	Type of Tycho-2 solution [-]
	 * @param corr
	 * 	Correlation (RAdeg,DEdeg) [-]
	 */
	public Tycho2Star(int tyc1, int tyc2, int tyc3, String pflag, double RAmdeg, double DEmdeg, double pmRA, double pmDE, int e_RAmdeg,
			int e_DEmdeg, double e_pmRA, double e_pmDE, double EpRAm, double EpDEm, int Num, double q_RAmdeg, double q_DEmdeg, double q_pmRA,
			double q_pmDE, double BTmag, double e_BTmag, double VTmag, double e_VTmag, int prox, String TYC, int HIP, String CCDM,
			double RAdeg, double DEdeg, double EpRA_1990, double EpDE_1990, double e_RAdeg, double e_DEdeg, String posflg, double corr) {
		this.tyc1 = tyc1;
		this.tyc2 = tyc2;
		this.tyc3 = tyc3;
		this.pflag = pflag;
		this.RAmdeg = RAmdeg;
		this.DEmdeg = DEmdeg;
		this.pmRA = pmRA;
		this.pmDE = pmDE;
		this.e_RAmdeg = e_RAmdeg;
		this.e_DEmdeg = e_DEmdeg;
		this.e_pmRA = e_pmRA;
		this.e_pmDE = e_pmDE;
		this.EpRAm = EpRAm;
		this.EpDEm = EpDEm;
		this.Num = Num;
		this.q_RAmdeg = q_RAmdeg;
		this.q_DEmdeg = q_DEmdeg;
		this.q_pmRA = q_pmRA;
		this.q_pmDE = q_pmDE;
		this.BTmag = BTmag;
		this.e_BTmag = e_BTmag;
		this.VTmag = VTmag;
		this.e_VTmag = e_VTmag;
		this.prox = prox;
		this.TYC = TYC;
		this.HIP = HIP;
		this.CCDM = CCDM;
		this.RAdeg = RAdeg;
		this.DEdeg = DEdeg;
		this.EpRA_1990 = EpRA_1990;
		this.EpDE_1990 = EpDE_1990; 
		this.e_RAdeg = e_RAdeg; 
		this.e_DEdeg = e_DEdeg; 
		this.posflg = posflg;
		this.corr = corr;
	}
	
	/**
	 * Parse a {@link Tycho2Star} from the given entry in the Tycho-2 catalogue.
	 * 
	 * @param data
	 * 	A String containing a single record in the Tycho-2 catalogue.
	 * @return
	 * 	A {@link Tycho2Star}.
	 */
	public static Tycho2Star parseTycho2Star(String data) {
		
		// Convert to byte array for extraction of fields
		byte[] bytes = data.getBytes();
		
		int tyc1 = Integer.parseInt(new String(Arrays.copyOfRange(bytes,  0,  4)).trim());
		int tyc2 = Integer.parseInt(new String(Arrays.copyOfRange(bytes,  5, 10)).trim());
		int tyc3 = Integer.parseInt(new String(Arrays.copyOfRange(bytes, 11, 12)).trim());
		String pflag = new String(Arrays.copyOfRange(bytes, 13, 14));
		
		// Some records contain no RAmdeg field
		String RAmdegStr = new String(Arrays.copyOfRange(bytes, 15, 27)).trim();
		double RAmdeg = Double.NaN;
		if(!RAmdegStr.isEmpty()) {
			RAmdeg = Double.parseDouble(RAmdegStr);
		}
		
		// Some records contain no DEmdeg field
		String DEmdegStr = new String(Arrays.copyOfRange(bytes, 28, 40)).trim();
		double DEmdeg = Double.NaN;
		if(!DEmdegStr.isEmpty()) {
			DEmdeg = Double.parseDouble(DEmdegStr);
		}
		
		// Some records contain no pmRA field
		String pmRAStr = new String(Arrays.copyOfRange(bytes, 41, 48)).trim();
		double pmRA = Double.NaN;
		if(!pmRAStr.isEmpty()) {
			pmRA = Double.parseDouble(pmRAStr);
		}
		
		// Some records contain no pmDE field
		String pmDEStr = new String(Arrays.copyOfRange(bytes, 49, 56)).trim();
		double pmDE = Double.NaN;
		if(!pmDEStr.isEmpty()) {
			pmDE = Double.parseDouble(pmDEStr);
		}
		
		// Some records contain no e_RAmdeg field
		String e_RAmdegStr = new String(Arrays.copyOfRange(bytes,  57,  60)).trim();
		int e_RAmdeg = Integer.MIN_VALUE;
		if(!e_RAmdegStr.isEmpty()) {
			e_RAmdeg = Integer.parseInt(e_RAmdegStr);
		}
		
		// Some records contain no e_DEmdeg field
		String e_DEmdegStr = new String(Arrays.copyOfRange(bytes,  61,  64)).trim();
		int e_DEmdeg = Integer.MIN_VALUE;
		if(!e_DEmdegStr.isEmpty()) {
			e_DEmdeg = Integer.parseInt(e_DEmdegStr);
		}
		
		// Some records contain no e_pmRA field
		String e_pmRAStr = new String(Arrays.copyOfRange(bytes, 65, 69)).trim();
		double e_pmRA = Double.NaN;
		if(!e_pmRAStr.isEmpty()) {
			e_pmRA = Double.parseDouble(e_pmRAStr);
		}
		
		// Some records contain no e_pmDE field
		String e_pmDEStr = new String(Arrays.copyOfRange(bytes, 70, 74)).trim();
		double e_pmDE = Double.NaN;
		if(!e_pmDEStr.isEmpty()) {
			e_pmDE = Double.parseDouble(e_pmDEStr);
		}
		
		// Some records contain no EpRAm field
		String EpRAmStr = new String(Arrays.copyOfRange(bytes, 75, 82)).trim();
		double EpRAm = Double.NaN;
		if(!EpRAmStr.isEmpty()) {
			EpRAm = Double.parseDouble(EpRAmStr);
		}
		
		// Some records contain no EpDEm field
		String EpDEmStr = new String(Arrays.copyOfRange(bytes, 83, 90)).trim();
		double EpDEm = Double.NaN;
		if(!EpDEmStr.isEmpty()) {
			EpDEm = Double.parseDouble(EpDEmStr);
		}
		
		// Some records contain no Num field
		String NumStr = new String(Arrays.copyOfRange(bytes,  91, 93)).trim();
		int Num = Integer.MIN_VALUE;
		if(!NumStr.isEmpty()) {
			Num = Integer.parseInt(NumStr);
		}
		
		// Some records contain no q_RAmdeg field
		String q_RAmdegStr = new String(Arrays.copyOfRange(bytes, 94, 97)).trim();
		double q_RAmdeg = Double.NaN;
		if(!q_RAmdegStr.isEmpty()) {
			q_RAmdeg = Double.parseDouble(q_RAmdegStr);
		}
		
		// Some records contain no q_DEmdeg field
		String q_DEmdegStr = new String(Arrays.copyOfRange(bytes, 98, 101)).trim();
		double q_DEmdeg = Double.NaN;
		if(!q_DEmdegStr.isEmpty()) {
			q_DEmdeg = Double.parseDouble(q_DEmdegStr);
		}
		
		// Some records contain no q_pmRA field
		String q_pmRAStr = new String(Arrays.copyOfRange(bytes, 102, 105)).trim();
		double q_pmRA = Double.NaN;
		if(!q_pmRAStr.isEmpty()) {
			q_pmRA = Double.parseDouble(q_pmRAStr);
		}
		
		// Some records contain no q_pmDE field
		String q_pmDEStr = new String(Arrays.copyOfRange(bytes, 106, 109)).trim();
		double q_pmDE = Double.NaN;
		if(!q_pmDEStr.isEmpty()) {
			q_pmDE = Double.parseDouble(q_pmDEStr);
		}
		
		
		// Some records contain no BTmag field
		String BTmagStr = new String(Arrays.copyOfRange(bytes, 110, 116)).trim();
		double BTmag = Double.NaN;
		if(!BTmagStr.isEmpty()) {
			BTmag = Double.parseDouble(BTmagStr);
		}
		
		// Some records contain no e_BTmag field
		String e_BTmagStr = new String(Arrays.copyOfRange(bytes, 117, 122)).trim();
		double e_BTmag = Double.NaN;
		if(!e_BTmagStr.isEmpty()) {
			e_BTmag = Double.parseDouble(e_BTmagStr);
		}
		
		// Some records contain no VTmag field
		String VTmagStr = new String(Arrays.copyOfRange(bytes, 123, 129)).trim();
		double VTmag = Double.NaN;
		if(!VTmagStr.isEmpty()) {
			VTmag = Double.parseDouble(VTmagStr);
		}
		
		// Some records contain no e_VTmag field
		String e_VTmagStr = new String(Arrays.copyOfRange(bytes, 130, 135)).trim();
		double e_VTmag = Double.NaN;
		if(!e_VTmagStr.isEmpty()) {
			e_VTmag = Double.parseDouble(e_VTmagStr);
		}
		
		int prox = Integer.parseInt(new String(Arrays.copyOfRange(bytes,  136, 139)).trim());
		String TYC = new String(Arrays.copyOfRange(bytes, 140, 141));
		
		// Some records contain no HIP field
		String hipStr = new String(Arrays.copyOfRange(bytes,  142, 148)).trim();
		int HIP = Integer.MIN_VALUE;
		if(!hipStr.isEmpty()) {
			HIP = Integer.parseInt(hipStr);
		}
		
		String CCDM = new String(Arrays.copyOfRange(bytes, 148, 151));
		double RAdeg = Double.parseDouble(new String(Arrays.copyOfRange(bytes, 152, 164)));
		double DEdeg = Double.parseDouble(new String(Arrays.copyOfRange(bytes, 165, 177)));
		double EpRA_1990 = Double.parseDouble(new String(Arrays.copyOfRange(bytes, 178, 182)));
		double EpDE_1990 = Double.parseDouble(new String(Arrays.copyOfRange(bytes, 183, 187)));
		double e_RAdeg = Double.parseDouble(new String(Arrays.copyOfRange(bytes, 188, 193)));
		double e_DEdeg = Double.parseDouble(new String(Arrays.copyOfRange(bytes, 194, 199)));
		String posflg = new String(Arrays.copyOfRange(bytes, 200, 201));
		double corr = Double.parseDouble(new String(Arrays.copyOfRange(bytes, 202, 206)));
		
		return new Tycho2Star(tyc1, tyc2, tyc3, pflag, RAmdeg, DEmdeg, pmRA, pmDE, e_RAmdeg, e_DEmdeg, e_pmRA,
				e_pmDE, EpRAm, EpDEm, Num, q_RAmdeg, q_DEmdeg, q_pmRA, q_pmDE, BTmag, e_BTmag, VTmag, e_VTmag,
				prox, TYC, HIP, CCDM, RAdeg, DEdeg, EpRA_1990, EpDE_1990, e_RAdeg, e_DEdeg, posflg, corr);
	}
	
}