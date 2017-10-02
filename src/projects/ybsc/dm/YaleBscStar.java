package projects.ybsc.dm;

import java.util.Arrays;

/**
 * Class represents a single star in the Tycho-2 catalogue.
 *
 * @author nrowell
 * @version $Id$
 */
public class YaleBscStar {
	
	/**
	 * Harvard Revised Number = Bright Star Number
	 */
	public final int hr;

	/**
	 * Name, generally Bayer and/or Flamsteed name
	 */
	public final String name;

	/**
	 * Durchmusterung Identification (zone in bytes 17-19)
	 */
	public final String dm;
	
	/**
	 * Henry Draper Catalog Number
	 */
	public final int hd;
	
	/**
	 * SAO Catalog Number
	 */
	public final int sao;

	/**
	 * FK5 star Number
	 */
	public final int fk5;
	
	/**
	 * I if infrared source
	 */
	public final String IRflag;

	/**
	 * Coded reference for infrared source
	 */
	public final String r_IRflag;

	/**
	 * Double or multiple-star code
	 */
	public final String Multiple;

	/**
	 * Aitken's Double Star Catalog (ADS) designation
	 */
	public final String ADS;

	/**
	 * ADS number components
	 */
	public final String ADScomp;

	/**
	 * Variable star identification
	 */
	public final String VarID;

	/**
	 * Hours RA, equinox B1900, epoch 1900.0
	 */
	public final int RAh1900;

	/**
	 * Minutes RA, equinox B1900, epoch 1900.0
	 */
	public final int RAm1900;

	/**
	 * Seconds RA, equinox B1900, epoch 1900.0
	 */
	public final double RAs1900;

	/**
	 * Sign Dec, equinox B1900, epoch 1900.0
	 */
	public final String DE_1900;

	/**
	 * Degrees Dec, equinox B1900, epoch 1900.0
	 */
	public final int DEd1900;

	/**
	 * Minutes Dec, equinox B1900, epoch 1900.0
	 */
	public final int DEm1900;

	/**
	 * Seconds Dec, equinox B1900, epoch 1900.0
	 */
	public final int DEs1900;

	/**
	 * Hours RA, equinox J2000, epoch 2000.0
	 */
	public final int RAh2000;

	/**
	 * Minutes RA, equinox J2000, epoch 2000.0
	 */
	public final int RAm2000;

	/**
	 * Seconds RA, equinox J2000, epoch 2000.0
	 */
	public final double RAs2000;

	/**
	 * Sign Dec, equinox J2000, epoch 2000.0
	 */
	public final String DE_2000;

	/**
	 * Degrees Dec, equinox J2000, epoch 2000.0
	 */
	public final int DEd2000;

	/**
	 * Minutes Dec, equinox J2000, epoch 2000.0
	 */
	public final int DEm2000;

	/**
	 * Seconds Dec, equinox J2000, epoch 2000.0
	 */
	public final int DEs2000;

	/**
	 * Galactic longitude [degrees]
	 */
	public final double GLON;

	/**
	 * Galactic latitude [degrees]
	 */
	public final double GLAT;

	/**
	 * Visual magnitude
	 */
	public final double Vmag;
	
	/**
	 * Visual magnitude code
	 */
	public final String n_Vmag;

	/**
	 * Uncertainty flag on V
	 */
	public final String u_Vmag;

	/**
	 * B-V color in the UBV system
	 */
	public final double BV;
	
	/**
	 * Uncertainty flag on B-V
	 */
	public final String uBV;

	/**
	 * U-B color in the UBV system
	 */
	public final double UB;
	
	/**
	 * Uncertainty flag on U-B
	 */
	public final String uUB;

	/**
	 * R-I in system specified by n_R-I
	 */
	public final double RI;
	
	/**
	 * Code for R-I system (Cousin, Eggen)
	 */
	public final String nRI;

	/**
	 * Spectral type
	 */
	public final String SpType;

	/**
	 * Spectral type code
	 */
	public final String n_SpType;

	/**
	 * Annual proper motion in RA J2000, FK5 system
	 */
	public final double pmRA;

	/**
	 * Annual proper motion in Dec J2000, FK5 system
	 */
	public final double pmDE;

	/**
	 * D indicates a dynamical parallax, otherwise a trigonometric parallax
	 */
	public final String n_Parallax;

	/**
	 * Parallax [arcsec]
	 */
	public final double Parallax;
	
	/**
	 * Heliocentric Radial Velocity [km/s]
	 */
	public final int RadVel;

	/**
	 * Radial velocity comments
	 */
	public final String n_RadVel;

	/**
	 * Rotational velocity limit characters
	 */
	public final String l_RotVel;

	/**
	 * Rotational velocity, v sin i [km/s]
	 */
	public final int RotVel;

	/**
	 * uncertainty and variability flag on RotVel
	 */
	public final String u_RotVel;

	/**
	 * Magnitude difference of double, or brightest multiple
	 */
	public final double Dmag;

	/**
	 * Separation of components in Dmag if occultation binary [arcsec]
	 */
	public final double Sep;

	/**
	 * Identifications of components in Dmag
	 */
	public final String MultID;

	/**
	 * Number of components assigned to a multiple
	 */
	public final int MultCnt;

	/**
	 * a star indicates that there is a note (see file notes)
	 */
	public final String NoteFlag;
	
	/**
	 * Main constructor for the {@link YaleBscStar}.
	 * 
	 * @param hr
	 * 	Harvard Revised Number = Bright Star Number
	 * @param name
	 * 	Name, generally Bayer and/or Flamsteed name
	 * @param dm
	 * 	Durchmusterung Identification (zone in bytes 17-19)
	 * @param hd
	 * 	Henry Draper Catalog Number
	 * @param sao
	 * 	SAO Catalog Number
	 * @param fk5
	 * 	FK5 star Number
	 * @param IRflag
	 * 	I if infrared source
	 * @param r_IRflag
	 * 	Coded reference for infrared source
	 * @param Multiple
	 * 	Double or multiple-star code
	 * @param ADS
	 * 	Aitken's Double Star Catalog (ADS) designation
	 * @param ADScomp
	 * 	ADS number components
	 * @param VarID
	 * 	Variable star identification
	 * @param RAh1900
	 * 	Hours RA, equinox B1900, epoch 1900.0
	 * @param RAm1900
	 * 	Minutes RA, equinox B1900, epoch 1900.0
	 * @param RAs1900
	 * 	Seconds RA, equinox B1900, epoch 1900.0
	 * @param DE_1900
	 * 	Sign Dec, equinox B1900, epoch 1900.0
	 * @param DEd1900
	 * 	Degrees Dec, equinox B1900, epoch 1900.0
	 * @param DEm1900
	 * 	Minutes Dec, equinox B1900, epoch 1900.0
	 * @param DEs1900
	 * 	Seconds Dec, equinox B1900, epoch 1900.0
	 * @param RAh2000
	 * 	Hours RA, equinox J2000, epoch 2000.0
	 * @param RAm2000
	 * 	Minutes RA, equinox J2000, epoch 2000.0
	 * @param RAs2000
	 * 	Seconds RA, equinox J2000, epoch 2000.0
	 * @param DE_2000
	 * 	Sign Dec, equinox J2000, epoch 2000.0
	 * @param DEd2000
	 * 	Degrees Dec, equinox J2000, epoch 2000.0
	 * @param DEm2000
	 * 	Minutes Dec, equinox J2000, epoch 2000.0
	 * @param DEs2000
	 * 	Seconds Dec, equinox J2000, epoch 2000.0
	 * @param GLON
	 * 	Galactic longitude [degrees]
	 * @param GLAT
	 * 	Galactic latitude [degrees]
	 * @param Vmag
	 * 	Visual magnitude
	 * @param n_Vmag
	 * 	Visual magnitude code
	 * @param u_Vmag
	 * 	Uncertainty flag on V
	 * @param BV
	 * 	B-V color in the UBV system
	 * @param uBV
	 * 	Uncertainty flag on B-V
	 * @param UB
	 * 	U-B color in the UBV system
	 * @param uUB
	 * 	Uncertainty flag on U-B
	 * @param RI
	 * 	R-I in system specified by n_R-I
	 * @param nRI
	 * 	Code for R-I system (Cousin, Eggen)
	 * @param SpType
	 * 	Spectral type
	 * @param n_SpType
	 * 	Spectral type code
	 * @param pmRA
	 * 	Annual proper motion in RA J2000, FK5 system
	 * @param pmDE
	 * 	Annual proper motion in Dec J2000, FK5 system
	 * @param n_Parallax
	 * 	D indicates a dynamical parallax, otherwise a trigonometric parallax
	 * @param Parallax
	 * 	Parallax [arcsec]
	 * @param RadVel
	 * 	Heliocentric Radial Velocity [km/s]
	 * @param n_RadVel
	 * 	Radial velocity comments
	 * @param l_RotVel
	 * 	Rotational velocity limit characters
	 * @param RotVel
	 * 	Rotational velocity, v sin i [km/s]
	 * @param u_RotVel
	 * 	uncertainty and variability flag on RotVel
	 * @param Dmag
	 * 	Magnitude difference of double, or brightest multiple
	 * @param Sep
	 * 	Separation of components in Dmag if occultation binary [arcsec]
	 * @param MultID
	 * 	Identifications of components in Dmag
	 * @param MultCnt
	 * 	Number of components assigned to a multiple
	 * @param NoteFlag
	 * 	a star indicates that there is a note (see file notes)
	 */
	public YaleBscStar(int hr, String name, String dm, int hd, int sao, int fk5, String IRflag, String r_IRflag, 
			String Multiple, String ADS, String ADScomp, String VarID, int RAh1900, int RAm1900, double RAs1900, 
			String DE_1900, int DEd1900, int DEm1900, int DEs1900, int RAh2000, int RAm2000, double RAs2000, 
			String DE_2000, int DEd2000, int DEm2000, int DEs2000, double GLON, double GLAT, double Vmag, String n_Vmag, 
			String u_Vmag, double BV, String uBV, double UB, String uUB, double RI, String nRI, String SpType, 
			String n_SpType, double pmRA, double pmDE, String n_Parallax, double Parallax, int RadVel, String n_RadVel, 
			String l_RotVel, int RotVel, String u_RotVel, double Dmag, double Sep, String MultID, int MultCnt, String NoteFlag) {
		
		this.hr = hr;
		this.name = name;
		this.dm = dm;
		this.hd = hd;
		this.sao = sao;
		this.fk5 = fk5;
		this.IRflag = IRflag;
		this.r_IRflag = r_IRflag;
		this.Multiple = Multiple;
		this.ADS = ADS;
		this.ADScomp = ADScomp;
		this.VarID = VarID;
		this.RAh1900 = RAh1900;
		this.RAm1900 = RAm1900;
		this.RAs1900 = RAs1900;
		this.DE_1900 = DE_1900;
		this.DEd1900 = DEd1900;
		this.DEm1900 = DEm1900;
		this.DEs1900 = DEs1900;
		this.RAh2000 = RAh2000;
		this.RAm2000 = RAm2000;
		this.RAs2000 = RAs2000;
		this.DE_2000 = DE_2000;
		this.DEd2000 = DEd2000;
		this.DEm2000 = DEm2000;
		this.DEs2000 = DEs2000;
		this.GLON = GLON;
		this.GLAT = GLAT;
		this.Vmag = Vmag;
		this.n_Vmag = n_Vmag;
		this.u_Vmag = u_Vmag;
		this.BV = BV;
		this.uBV = uBV;
		this.UB = UB;
		this.uUB = uUB;
		this.RI = RI;
		this.nRI = nRI;
		this.SpType = SpType;
		this.n_SpType = n_SpType;
		this.pmRA = pmRA;
		this.pmDE = pmDE;
		this.n_Parallax = n_Parallax;
		this.Parallax = Parallax;
		this.RadVel = RadVel;
		this.n_RadVel = n_RadVel;
		this.l_RotVel = l_RotVel;
		this.RotVel = RotVel;
		this.u_RotVel = u_RotVel;
		this.Dmag = Dmag;
		this.Sep = Sep;
		this.MultID = MultID;
		this.MultCnt = MultCnt;
		this.NoteFlag = NoteFlag;
	}
	
	/**
	 * Parse a {@link YaleBscStar} from the given entry in the Yale Bright Star Catalogue.
	 * 
	 * @param data
	 * 	A String containing a single record in the Tycho-2 catalogue.
	 * @return
	 * 	A {@link YaleBscStar}.
	 */
	public static YaleBscStar parseYaleBscStar(String data) {
		
		// Convert to byte array for extraction of fields
		byte[] bytes = data.getBytes();
		
		int hr = Integer.parseInt(new String(Arrays.copyOfRange(bytes, 0, 4)).trim());
		String name = new String(Arrays.copyOfRange(bytes, 4, 14)).trim();
		
		String dm = new String(Arrays.copyOfRange(bytes, 14, 25)).trim();
		
		String hdStr = new String(Arrays.copyOfRange(bytes, 25, 31)).trim();
		int hd = Integer.MIN_VALUE;
		if(!hdStr.isEmpty()) {
			hd = Integer.parseInt(hdStr);
		}
		
		String saoStr = new String(Arrays.copyOfRange(bytes, 31, 37)).trim();
		int sao = Integer.MIN_VALUE;
		if(!saoStr.isEmpty()) {
			sao = Integer.parseInt(saoStr);
		}
		
		String fk5Str = new String(Arrays.copyOfRange(bytes, 37, 41)).trim();
		int fk5 = Integer.MIN_VALUE;
		if(!fk5Str.isEmpty()) {
			fk5 = Integer.parseInt(fk5Str);
		}
		
		String IRflag = new String(Arrays.copyOfRange(bytes, 41, 42)).trim();
		String r_IRflag = new String(Arrays.copyOfRange(bytes, 42, 43)).trim();
		String Multiple = new String(Arrays.copyOfRange(bytes, 43, 44)).trim();
		String ADS = new String(Arrays.copyOfRange(bytes, 44, 49)).trim();
		String ADScomp = new String(Arrays.copyOfRange(bytes, 49, 51)).trim();
		String VarID = new String(Arrays.copyOfRange(bytes, 51, 60)).trim();
		
		String RAh1900Str = new String(Arrays.copyOfRange(bytes, 60, 62)).trim();
		int RAh1900 = Integer.MIN_VALUE;
		if(!RAh1900Str.isEmpty()) {
			RAh1900 = Integer.parseInt(RAh1900Str);
		}
		
		String RAm1900Str = new String(Arrays.copyOfRange(bytes, 62, 64)).trim();
		int RAm1900 = Integer.MIN_VALUE;
		if(!RAm1900Str.isEmpty()) {
			RAm1900 = Integer.parseInt(RAm1900Str);
		}
		
		String RAs1900Str = new String(Arrays.copyOfRange(bytes, 64, 68)).trim();
		double RAs1900 = Double.MIN_VALUE;
		if(!RAs1900Str.isEmpty()) {
			RAs1900 = Double.parseDouble(RAs1900Str);
		}
		
		String DE_1900 = new String(Arrays.copyOfRange(bytes, 68, 69)).trim();
		
		String DEd1900Str = new String(Arrays.copyOfRange(bytes, 69, 71)).trim();
		int DEd1900 = Integer.MIN_VALUE;
		if(!DEd1900Str.isEmpty()) {
			DEd1900 = Integer.parseInt(DEd1900Str);
		}
		
		String DEm1900Str = new String(Arrays.copyOfRange(bytes, 71, 73)).trim();
		int DEm1900 = Integer.MIN_VALUE;
		if(!DEm1900Str.isEmpty()) {
			DEm1900 = Integer.parseInt(DEm1900Str);
		}
		
		String DEs1900Str = new String(Arrays.copyOfRange(bytes, 73, 75)).trim();
		int DEs1900 = Integer.MIN_VALUE;
		if(!DEs1900Str.isEmpty()) {
			DEs1900 = Integer.parseInt(DEs1900Str);
		}
		
		String RAh2000Str = new String(Arrays.copyOfRange(bytes, 75, 77)).trim();
		int RAh2000 = Integer.MIN_VALUE;
		if(!RAh2000Str.isEmpty()) {
			RAh2000 = Integer.parseInt(RAh1900Str);
		}
		
		String RAm2000Str = new String(Arrays.copyOfRange(bytes, 77, 79)).trim();
		int RAm2000 = Integer.MIN_VALUE;
		if(!RAm2000Str.isEmpty()) {
			RAm2000 = Integer.parseInt(RAm1900Str);
		}
		
		String RAs2000Str = new String(Arrays.copyOfRange(bytes, 79, 83)).trim();
		double RAs2000 = Double.MIN_VALUE;
		if(!RAs2000Str.isEmpty()) {
			RAs2000 = Double.parseDouble(RAs1900Str);
		}
		
		String DE_2000 = new String(Arrays.copyOfRange(bytes, 83, 84)).trim();
		
		String DEd2000Str = new String(Arrays.copyOfRange(bytes, 84, 86)).trim();
		int DEd2000 = Integer.MIN_VALUE;
		if(!DEd2000Str.isEmpty()) {
			DEd2000 = Integer.parseInt(DEd1900Str);
		}
		
		String DEm2000Str = new String(Arrays.copyOfRange(bytes, 86, 88)).trim();
		int DEm2000 = Integer.MIN_VALUE;
		if(!DEm2000Str.isEmpty()) {
			DEm2000 = Integer.parseInt(DEm1900Str);
		}
		
		String DEs2000Str = new String(Arrays.copyOfRange(bytes, 88, 90)).trim();
		int DEs2000 = Integer.MIN_VALUE;
		if(!DEs2000Str.isEmpty()) {
			DEs2000 = Integer.parseInt(DEs1900Str);
		}
		
		String GLONStr = new String(Arrays.copyOfRange(bytes, 90, 96)).trim();
		double GLON = Double.MIN_VALUE;
		if(!GLONStr.isEmpty()) {
			GLON = Double.parseDouble(GLONStr);
		}
		
		String GLATStr = new String(Arrays.copyOfRange(bytes, 96, 102)).trim();
		double GLAT = Double.MIN_VALUE;
		if(!GLATStr.isEmpty()) {
			GLAT = Double.parseDouble(GLATStr);
		}
		
		String VmagStr = new String(Arrays.copyOfRange(bytes, 102, 107)).trim();
		double Vmag = Double.MIN_VALUE;
		if(!VmagStr.isEmpty()) {
			Vmag = Double.parseDouble(VmagStr);
		}
		
		String n_Vmag = new String(Arrays.copyOfRange(bytes, 107, 108)).trim();
		String u_Vmag = new String(Arrays.copyOfRange(bytes, 108, 109)).trim();
		
		String BVStr = new String(Arrays.copyOfRange(bytes, 109, 114)).trim();
		double BV = Double.MIN_VALUE;
		if(!BVStr.isEmpty()) {
			BV = Double.parseDouble(BVStr);
		}
		
		String uBV = new String(Arrays.copyOfRange(bytes, 114, 115)).trim();
		
		String UBStr = new String(Arrays.copyOfRange(bytes, 115, 120)).trim();
		double UB = Double.MIN_VALUE;
		if(!UBStr.isEmpty()) {
			UB = Double.parseDouble(UBStr);
		}
		
		String uUB = new String(Arrays.copyOfRange(bytes, 120, 121)).trim();
		
		String RIStr = new String(Arrays.copyOfRange(bytes, 121, 126)).trim();
		double RI = Double.MIN_VALUE;
		if(!RIStr.isEmpty()) {
			RI = Double.parseDouble(RIStr);
		}
		
		String nRI = new String(Arrays.copyOfRange(bytes, 126, 127)).trim();
		
		String SpType = new String(Arrays.copyOfRange(bytes, 127, 147)).trim();
		
		String n_SpType = new String(Arrays.copyOfRange(bytes, 147, 148)).trim();		
		
		String pmRAStr = new String(Arrays.copyOfRange(bytes, 148, 154)).trim();
		double pmRA = Double.MIN_VALUE;
		if(!pmRAStr.isEmpty()) {
			pmRA = Double.parseDouble(pmRAStr);
		}
		
		String pmDEStr = new String(Arrays.copyOfRange(bytes, 154, 160)).trim();
		double pmDE = Double.MIN_VALUE;
		if(!pmDEStr.isEmpty()) {
			pmDE = Double.parseDouble(pmDEStr);
			
			
		}
		
		
		String n_Parallax = new String(Arrays.copyOfRange(bytes, 160, 161)).trim();
		
		
		String ParallaxStr = "";
		try {
			ParallaxStr = new String(Arrays.copyOfRange(bytes, 161, 166)).trim();
		}
		catch(ArrayIndexOutOfBoundsException e) {
			// Leave variable at the default value
		}
		double Parallax = Double.MIN_VALUE;
		if(!ParallaxStr.isEmpty()) {
			Parallax = Double.parseDouble(ParallaxStr);
		}
		
		
		String RadVelStr = "";
		try {
			RadVelStr = new String(Arrays.copyOfRange(bytes, 166, 170)).trim();
		}
		catch(ArrayIndexOutOfBoundsException e) {
			// Leave variable at the default value
		}
		int RadVel = Integer.MIN_VALUE;
		if(!RadVelStr.isEmpty()) {
			RadVel = Integer.parseInt(RadVelStr);
		}		
		
		
		String n_RadVel = "";
		try {
			n_RadVel = new String(Arrays.copyOfRange(bytes, 170, 174)).trim();
		}
		catch(ArrayIndexOutOfBoundsException e) {
			// Leave variable at the default value
		}
		
		
		String l_RotVel = "";
		try {
			l_RotVel = new String(Arrays.copyOfRange(bytes, 174, 176)).trim();
		}
		catch(ArrayIndexOutOfBoundsException e) {
			// Leave variable at the default value
		}
		
		
		String RotVelStr = "";
		try {
			RotVelStr = new String(Arrays.copyOfRange(bytes, 176, 179)).trim();
		}
		catch(ArrayIndexOutOfBoundsException e) {
			// Leave variable at the default value
		}
		int RotVel = Integer.MIN_VALUE;
		if(!RotVelStr.isEmpty()) {
			RotVel = Integer.parseInt(RotVelStr);
		}
		
		
		String u_RotVel = "";
		try {
			u_RotVel = new String(Arrays.copyOfRange(bytes, 179, 180)).trim();
		}
		catch(ArrayIndexOutOfBoundsException e) {
			// Leave variable at the default value
		}
		
		
		String DmagStr = "";
		try {
			DmagStr = new String(Arrays.copyOfRange(bytes, 180, 184)).trim();
		}
		catch(ArrayIndexOutOfBoundsException e) {
			// Leave variable at the default value
		}
		double Dmag = Double.MIN_VALUE;
		if(!DmagStr.isEmpty()) {
			Dmag = Double.parseDouble(DmagStr);
		}
		
		
		String SepStr = "";
		try {
			SepStr = new String(Arrays.copyOfRange(bytes, 184, 190)).trim();
		}
		catch(ArrayIndexOutOfBoundsException e) {
			// Leave variable at the default value
		}
		double Sep = Double.MIN_VALUE;
		if(!SepStr.isEmpty()) {
			Sep = Double.parseDouble(SepStr);
		}
		
		
		String MultID = "";
		try {
			MultID = new String(Arrays.copyOfRange(bytes, 190, 194)).trim();
		}
		catch(ArrayIndexOutOfBoundsException e) {
			// Leave variable at the default value
		}
		
		
		String MultCntStr = "";
		try {
			MultCntStr = new String(Arrays.copyOfRange(bytes, 194, 196)).trim();
		}
		catch(ArrayIndexOutOfBoundsException e) {
			// Leave variable at the default value
		}
		int MultCnt = Integer.MIN_VALUE;
		if(!MultCntStr.isEmpty()) {
			MultCnt = Integer.parseInt(MultCntStr);
		}
		
		
		String NoteFlag = "";
		try {
			NoteFlag = new String(Arrays.copyOfRange(bytes, 196, 197)).trim();
		}
		catch(ArrayIndexOutOfBoundsException e) {
			// Leave variable at the default value
		}
		
		
		return new YaleBscStar(hr, name, dm, hd, sao, fk5, IRflag, r_IRflag, Multiple, ADS, ADScomp, VarID, RAh1900, RAm1900, RAs1900, 
				DE_1900, DEd1900, DEm1900, DEs1900, RAh2000, RAm2000, RAs2000, DE_2000, DEd2000, DEm2000, DEs2000, GLON, GLAT, Vmag, n_Vmag, 
				u_Vmag, BV, uBV, UB, uUB, RI, nRI, SpType, n_SpType, pmRA, pmDE, n_Parallax, Parallax, RadVel, n_RadVel, 
				l_RotVel, RotVel, u_RotVel, Dmag, Sep, MultID, MultCnt, NoteFlag);
	}
	
}