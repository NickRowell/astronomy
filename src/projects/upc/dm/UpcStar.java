package projects.upc.dm;

import infra.os.OSChecker;
import util.ArrayUtil;

/**
 * Class represents a single star in the URAT Parallax Catalogue.
 *
 *
 * @author nrowell
 * @version $Id$
 */
public class UpcStar {

	/**
	 * URAT Parallax Catalog (UPC) number
	 */
	final public int upcId;
	
	/**
	 * Right ascension on ICRS, at Epoch J2014 [degrees]
	 * Positions are on the International Celestial Reference System
	 * (ICRS) as represented by the UCAC4 catalog. Mean observed positions
	 * are given at epoch J2014 (mean observed positions have been updated
 	 * to J2014 using the proper motions from column ('pmRA', 'pmDE').
	 */
	final public double ra;

	/**
	 * Declination on ICRS, at Epoch J2014 [degrees]
	 * Positions are on the International Celestial Reference System
	 * (ICRS) as represented by the UCAC4 catalog. Mean observed positions
	 * are given at epoch J2014 (mean observed positions have been updated
 	 * to J2014 using the proper motions from column ('pmRA', 'pmDE').
	 */
	final public double dec;
	
	/**
	 * Identifier.
	 * Identifier comes from the external source from which the URAT
 	 * position was matched column ('srcflg').  For Mearth the LSPM North
 	 * Catalog Designation is given. This is left blank if no identifier
 	 * was found.
	 */
	final public String name;
	
	/**
	 * URAT mean model fit magnitude
	 *  This is the mean, observed magnitude in the 680-750 nm URAT
 	 *  bandpass, calibrated by APASS photometry. This bandpass is between
 	 *  R and I, thus further into the red than UCAC. Observations in
 	 *  non-photometric nights *are* included thus the URAT magnitudes need
 	 *  to be taken with caution.
	 */
	final public double fMag;
	
	/**
	 * URAT relative parallax [mas]
	 */
	final public double relPi;
	
	/**
	 * Parallax correction [mas]
	 */
	final public double corPi;
	
	/**
	 * Parallax correction flag [0-2]
	 * The parallax correction flag is as follows:
	 * 0 = correction from the photometric parallaxes of UCAC4 ref.stars
	 * 1 = no correction so the mean of 1.3mas was used
	 * 2 = large correction so the cut off of 3.9mas was used
	 */
	final public int corFlag;
	
	/**
	 * URAT absolute parallax [mas]
	 */
	final public double absPi;
	
	/**
	 * Error on absolute parallax [mas]
	 */
	final public double absPiErr;
	
	/**
	 * Proper motion in right ascension [mas/yr]
	 */
	final public double muRa;

	/**
	 * Error proper motion in right ascension [mas/yr]
	 */
	final public double muRaErr;

	/**
	 * Proper motion in Declination [mas/yr]
	 */
	final public double muDec;

	/**
	 * Error proper motion in Declination [mas/yr]
	 */
	final public double muDecErr;
	
	/**
	 * Total number of epochs available.
	 * The number of epochs available in the URAT epoch data (ne) and
 	 * the number of epochs rejected (nr) during the fit solutions.
	 */
	final public int ne;
	
	/**
	 * Total number of epochs rejected.
	 * The number of epochs available in the URAT epoch data (ne) and
 	 * the number of epochs rejected (nr) during the fit solutions.
	 */
	final public int nr;
	
	/**
	 * Epoch span of data
	 */
	final public double epochSpan;
	
	/**
	 * Average elongation of image.
	 * Average image elongation from moment analysis, ratio of major
	 * to minor axis is given (1.0 = round)
	 */
	final public double elong;
	
	/**
	 * Fit sigma 1 (reduced chi-square).
	 * The error of unit weight (reduced chi-square) of the parallax fit
 	 * solution is given. Thus 1.0 means the scatter of the post-fit residuals
 	 * match the expected observational errors and assigned weights.
	 */
	final public double fsig1;
	
	/**
	 * Fit sigma 2 (mean error indiv.obs.).
	 */
	final public double fsig2;
	
	/**
	 * Source flag [0-5].
	 * The source flag gives the external source to which the URAT
 	 * position was matched and where the information for columns 'srcpi'
 	 * and e_srcpi are from.
  	 *  0 = no match to any of the following external catalogs found
   	 * 1 = matched to Hipparcos (Cat. I/239)
   	 * 2 = matched with The Yale Parallax Catalog
   	 *      (van Altena et al., 1995, Cat. I/238)
   	 * 3 = matched with (Finch & Zacharias, 2016, AJ, in press)
   	 * 4 = matched with MEarth parallaxes
   	 *       (Dittmann et.  al., 2014ApJ...784....2M)
   	 * 5 = matched with SIMBAD database (http://simbad.u-strasbg.fr/simbad/)
	 */
	final public int srcFlg;
	
	/**
	 * Parallax from external source. Due to the
 	 * limitations of the SIMBAD database automated search feature the
 	 * parallax error or the source of the parallax is not given so for
 	 * all srcflg = 5, the parallax error is reported as 0.0. For all
 	 * srcflg = 0, meaning no match to an external catalog the parallax
 	 * and error are reported as 0.0.
	 */
	final public double srcPi;
	
	/**
	 * Parallax error from external source. Due to the
 	 * limitations of the SIMBAD database automated search feature the
 	 * parallax error or the source of the parallax is not given so for
 	 * all srcflg = 5, the parallax error is reported as 0.0. For all
 	 * srcflg = 0, meaning no match to an external catalog the parallax
 	 * and error are reported as 0.0.
	 */
	final public double srcPiErr;
	
	/**
	 * Main constructor for {@link UpcStar}.
	 * @param upcId
	 * 	See {@link UpcStar#upcId}
	 * @param ra
	 * 	See {@link UpcStar#ra}
	 * @param dec
	 * 	See {@link UpcStar#dec}
	 * @param name
	 * 	See {@link UpcStar#name}
	 * @param fMag
	 * 	See {@link UpcStar#fMag}
	 * @param relPi
	 * 	See {@link UpcStar#relPi}
	 * @param corPi
	 * 	See {@link UpcStar#corPi}
	 * @param corFlag
	 * 	See {@link UpcStar#corFlag}
	 * @param absPi
	 * 	See {@link UpcStar#absPi}
	 * @param absPiErr
	 * 	See {@link UpcStar#absPiErr}
	 * @param muRa
	 * 	See {@link UpcStar#muRa}
	 * @param muRaErr
	 * 	See {@link UpcStar#muRaErr}
	 * @param muDec
	 * 	See {@link UpcStar#muDec}
	 * @param muDecErr
	 * 	See {@link UpcStar#muDecErr}
	 * @param ne
	 * 	See {@link UpcStar#ne}
	 * @param nr
	 * 	See {@link UpcStar#nr}
	 * @param epochSpan
	 * 	See {@link UpcStar#epochSpan}
	 * @param elong
	 * 	See {@link UpcStar#elong}
	 * @param fsig1
	 * 	See {@link UpcStar#fsig1}
	 * @param fsig2
	 * 	See {@link UpcStar#fsig2}
	 * @param srcFlg
	 * 	See {@link UpcStar#srcFlg}
	 * @param srcPi
	 * 	See {@link UpcStar#srcPi}
	 * @param srcPiErr
	 * 	See {@link UpcStar#srcPiErr}
	 */
	public UpcStar(int upcId, double ra, double dec, String name, double fMag, double relPi, double corPi,
			int corFlag, double absPi, double absPiErr, double muRa, double muRaErr, double muDec, double muDecErr,
			int ne, int nr, double epochSpan, double elong, double fsig1,  double fsig2, int srcFlg, double srcPi,
			double srcPiErr) {
		this.upcId = upcId;
		this.ra = ra;
		this.dec = dec;
		this.name = name;
		this.fMag = fMag;
		this.relPi = relPi;
		this.corPi = corPi;
		this.corFlag = corFlag;
		this.absPi = absPi;
		this.absPiErr = absPiErr;
		this.muRa = muRa;
		this.muRaErr = muRaErr;
		this.muDec = muDec;
		this.muDecErr = muDecErr;
		this.ne = ne;
		this.nr = nr;
		this.epochSpan = epochSpan;
		this.elong = elong;
		this.fsig1 = fsig1;
		this.fsig2 = fsig2;
		this.srcFlg = srcFlg;
		this.srcPi = srcPi;
		this.srcPiErr = srcPiErr;
	}
	
	/**
	 * A String representation of the {@link UpcStar}; the internal fields are formatted in the same way
	 * as the input catalogue.
	 * @return
	 * 	A String representation of the {@link UpcStar}.
	 */
	public String toRecord() {
		StringBuilder string = new StringBuilder();
		string.append(String.format("%6d ", this.upcId));
		string.append(String.format("%11.7f ", this.ra));
		string.append(String.format("%11.7f ", this.dec));
		string.append(String.format("%-31s ", this.name));
		string.append(String.format("%5.2f ", this.fMag));
		string.append(String.format("%6.1f ", this.relPi));
		string.append(String.format("%3.1f ", this.corPi));
		string.append(String.format("%1d ", this.corFlag));
		string.append(String.format("%6.1f ", this.absPi));
		string.append(String.format("%4.1f ", this.absPiErr));
		string.append(String.format("%7.1f ", this.muRa));
		string.append(String.format("%4.1f ", this.muRaErr));
		string.append(String.format("%7.1f ", this.muDec));
		string.append(String.format("%4.1f ", this.muDecErr));
		string.append(String.format("%2d ", this.ne));
		string.append(String.format("%2d ", this.nr));
		string.append(String.format("%4.2f ", this.epochSpan));
		string.append(String.format("%5.3f ", this.elong));
		string.append(String.format("%5.3f ", this.fsig1));
		string.append(String.format("%5.1f ", this.fsig2));
		string.append(String.format("%1d ", this.srcFlg));
		string.append(String.format("%7.2f ", this.srcPi));
		string.append(String.format("%5.2f ", this.srcPiErr));
		return string.toString();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		
		StringBuilder string = new StringBuilder();
		
		string.append("upcId\t\t= " + this.upcId).append(OSChecker.newline);
		string.append("ra\t\t= " + this.ra).append(OSChecker.newline);
		string.append("dec\t\t= " + this.dec).append(OSChecker.newline);
		string.append("name\t\t= " + this.name).append(OSChecker.newline);
		string.append("fMag\t\t= " + this.fMag).append(OSChecker.newline);
		string.append("relPi\t\t= " + this.relPi).append(OSChecker.newline);
		string.append("corPi\t\t= " + this.corPi).append(OSChecker.newline);
		string.append("corFlag\t\t= " + this.corFlag).append(OSChecker.newline);
		string.append("absPi\t\t= " + this.absPi).append(OSChecker.newline);
		string.append("absPiErr\t= " + this.absPiErr).append(OSChecker.newline);
		string.append("muRa\t\t= " + this.muRa).append(OSChecker.newline);
		string.append("muRaErr\t\t= " + this.muRaErr).append(OSChecker.newline);
		string.append("muDec\t\t= " + this.muDec).append(OSChecker.newline);
		string.append("muDecErr\t= " + this.muDecErr).append(OSChecker.newline);
		string.append("ne\t\t= " + this.ne).append(OSChecker.newline);
		string.append("nr\t\t= " + this.nr).append(OSChecker.newline);
		string.append("epochSpan\t= " + this.epochSpan).append(OSChecker.newline);
		string.append("elong\t\t= " + this.elong).append(OSChecker.newline);
		string.append("fsig1\t\t= " + this.fsig1).append(OSChecker.newline);
		string.append("fsig2\t\t= " + this.fsig2).append(OSChecker.newline);
		string.append("srcFlg\t\t= " + this.srcFlg).append(OSChecker.newline);
		string.append("srcPi\t\t= " + this.srcPi).append(OSChecker.newline);
		string.append("srcPiErr\t= " + this.srcPiErr).append(OSChecker.newline);
		
		return string.toString();
	}
	
	/**
	 * Parse a line from the UPC data file that contains the fields of a single star, read the
	 * fields and return a {@link UpcStar}.
	 * @param record
	 * 	String containing a single line from the UPC data file
	 * @return
	 * 	A {@link UpcStar} configured with the fields read from the String.
	 */
	public static UpcStar parseUpcStar(String record) {
		
		byte[] bytes = record.getBytes();
		
		int upcId 		=   Integer.parseInt(ArrayUtil.getStringFromBytes(bytes, 0, 6).trim());
		double ra 		= Double.parseDouble(ArrayUtil.getStringFromBytes(bytes, 7, 18).trim());
		double dec 		= Double.parseDouble(ArrayUtil.getStringFromBytes(bytes, 19, 30).trim());
		String name 	=                    ArrayUtil.getStringFromBytes(bytes, 31, 61).trim();
		double fMag 	= Double.parseDouble(ArrayUtil.getStringFromBytes(bytes, 62, 67).trim());
		double relPi 	= Double.parseDouble(ArrayUtil.getStringFromBytes(bytes, 68, 74).trim());
		double corPi 	= Double.parseDouble(ArrayUtil.getStringFromBytes(bytes, 75, 78).trim());
		int corFlag 	=   Integer.parseInt(ArrayUtil.getStringFromBytes(bytes, 79, 80).trim());
		double absPi 	= Double.parseDouble(ArrayUtil.getStringFromBytes(bytes, 81, 87).trim());
		double absPiErr = Double.parseDouble(ArrayUtil.getStringFromBytes(bytes, 88, 93).trim());
		double muRa 	= Double.parseDouble(ArrayUtil.getStringFromBytes(bytes, 94, 101).trim());
		double muRaErr 	= Double.parseDouble(ArrayUtil.getStringFromBytes(bytes, 102, 107).trim());
		double muDec 	= Double.parseDouble(ArrayUtil.getStringFromBytes(bytes, 108, 115).trim());
		double muDecErr = Double.parseDouble(ArrayUtil.getStringFromBytes(bytes, 116, 121).trim());
		int ne			=   Integer.parseInt(ArrayUtil.getStringFromBytes(bytes, 122, 125).trim());
		int nr          =   Integer.parseInt(ArrayUtil.getStringFromBytes(bytes, 126, 128).trim());
		double epochSpan= Double.parseDouble(ArrayUtil.getStringFromBytes(bytes, 129, 133).trim());
		double elong 	= Double.parseDouble(ArrayUtil.getStringFromBytes(bytes, 134, 139).trim());
		double fsig1 	= Double.parseDouble(ArrayUtil.getStringFromBytes(bytes, 140, 145).trim());
		double fsig2 	= Double.parseDouble(ArrayUtil.getStringFromBytes(bytes, 146, 151).trim());
		int srcFlg 		=   Integer.parseInt(ArrayUtil.getStringFromBytes(bytes, 152, 153).trim());
		double srcPi 	= Double.parseDouble(ArrayUtil.getStringFromBytes(bytes, 154, 161).trim());
		double srcPiErr = Double.parseDouble(ArrayUtil.getStringFromBytes(bytes, 162, 167).trim());
		
		return new UpcStar(upcId, ra, dec, name, fMag, relPi, corPi, corFlag, absPi, absPiErr, muRa,
				muRaErr, muDec, muDecErr, ne, nr, epochSpan, elong, fsig1, fsig2, srcFlg, srcPi, srcPiErr);
	}
	
	/**
	 * Checks if this {@link UpcStar} has been crossmatched with the Hipparcos catalogue. In this case,
	 * the {@link UpcStar#srcPi} and {@link UpcStar#srcPiErr} fields contains the parallax and error as
	 * measured in Hipparcos.
	 * @return
	 * 	True if this star is matched with a Hipparcos source.
	 */
	public boolean isHipparcosStar() {
		return srcFlg == 1;
	}
	
	/**
	 * Checks if this {@link UpcStar} has not been matched in any of the external parallax catalogues.
	 * @return
	 * 	True if this star is not matched in any of the external parallax catalogues.
	 */
	public boolean hasNoExternalCatalogueMatch() {
		return srcFlg == 0;
	}
	
}