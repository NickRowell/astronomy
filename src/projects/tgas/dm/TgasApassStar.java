package projects.tgas.dm;

import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Class represents a single star in the TGAS catalogue with additional fields from APASS following
 * cross-match using CDS VizieR services.
 *
 * @author nrowell
 * @version $Id$
 */
public class TgasApassStar {
	
	/**
	 * The Gaia source ID
	 */
	public final long sourceId;
	
	/**
	 * The equatorial right ascension [deg]
	 */
	public final double ra;
	
	/**
	 * Error on the equatorial right ascension [mas]
	 */
	public final double ra_error;
	
	/**
	 * The equatorial declination [deg]
	 */
	public final double dec;
	
	/**
	 * Error on the equatorial declination [mas]
	 */
	public final double dec_error;
	
	/**
	 * The stellar parallax [mas]
	 */
	public final double parallax;
	
	/**
	 * The parallax error [mas]
	 */
	public final double parallax_error;
	
	/**
	 * Proper motion in right ascension (includes cos(dec) factor) [mas/yr]
	 */
	public final double pmra;

	/**
	 * Error on the proper motion in right ascension [mas/yr]
	 */
	public final double pmra_error;
	
	/**
	 * Proper motion in declination [mas/yr]
	 */
	public final double pmdec;
	
	/**
	 * Error on the proper motion in declination [mas/yr]
	 */
	public final double pmdec_error;
	
	/**
	 * Quantifies the excess scatter of the observations about the astrometric model [mas]
	 */
	public final double astrometric_excess_noise;
	
	/**
	 * Mean magnitude in the Gaia G band [mag]
	 */
	public final double phot_g_mean_mag;

	/**
	 * APASS
	 * APASS V magnitude [mag]
	 */
	public final double v_mag;
	
	/**
	 * APASS
	 * Error on the APASS V magnitude [mag]
	 */
	public final double e_v_mag;
	
	/**
	 * APASS
	 * APASS B magnitude [mag]
	 */
	public final double b_mag;
	
	/**
	 * APASS
	 * Error on the APASS B magnitude [mag]
	 */
	public final double e_b_mag;
	
	/**
	 * Default constructor for the {@link TgasApassStar}.
	 */
	public TgasApassStar() {
		this.sourceId = Long.MIN_VALUE;
		this.ra = Double.NaN;
		this.ra_error = Double.NaN;
		this.dec = Double.NaN;
		this.dec_error = Double.NaN;
		this.parallax = Double.NaN;
		this.parallax_error = Double.NaN;
		this.pmra = Double.NaN;
		this.pmra_error = Double.NaN;
		this.pmdec = Double.NaN;
		this.pmdec_error = Double.NaN;
		this.astrometric_excess_noise = Double.NaN;
		this.phot_g_mean_mag = Double.NaN;
		this.v_mag = Double.NaN;
		this.e_v_mag = Double.NaN;
		this.b_mag = Double.NaN;
		this.e_b_mag = Double.NaN;
	}
	
	/**
	 * Main constructor for the {@link TgasApassStar}.
	 * 
	 * @param sourceId
	 * 	The Gaia source ID
	 * @param ra
	 * 	The equatorial right ascension [deg]
	 * @param ra_error
	 * 	Error on the equatorial right ascension [mas]
	 * @param dec
	 * 	The equatorial declination [deg]
	 * @param dec_error
	 * 	Error on the equatorial declination [mas]
	 * @param parallax
	 * 	The stellar parallax [mas]
	 * @param parallax_error
	 * 	The parallax error [mas]
	 * @param pmra
	 * 	Proper motion in right ascension (includes cos(dec) factor) [mas/yr]
	 * @param pmra_error
	 * 	Error on the proper motion in right ascension [mas/yr]
	 * @param pmdec
	 * 	Proper motion in declination [mas/yr]
	 * @param pmdec_error
	 * 	Error on the proper motion in declination [mas/yr]
	 * @param astrometric_excess_noise
	 * 	Quantifies the excess scatter of the observations about the astrometric model [mas]
	 * @param phot_g_mean_mag
	 * 	Mean magnitude in the Gaia G band [mag]
	 * @param v_mag
	 * 	APASS V magnitude [mag]
	 * @param e_v_mag
	 * 	Error on the APASS V magnitude [mag]
	 * @param b_mag
	 * 	APASS B magnitude [mag]
	 * @param e_b_mag
	 * 	Error on the APASS B magnitude [mag]
	 */
	public TgasApassStar(long sourceId, double ra, double ra_error, double dec, double dec_error, double parallax, double parallax_error, double pmra, double pmra_error, 
			double pmdec, double pmdec_error, double astrometric_excess_noise, double phot_g_mean_mag, double v_mag, double e_v_mag, double b_mag, double e_b_mag) {
		this.sourceId = sourceId;
		this.ra = ra;
		this.ra_error = ra_error;
		this.dec = dec;
		this.dec_error = dec_error;
		this.parallax = parallax;
		this.parallax_error = parallax_error;
		this.pmra = pmra;
		this.pmra_error = pmra_error;
		this.pmdec = pmdec;
		this.pmdec_error = pmdec_error;
		this.astrometric_excess_noise = astrometric_excess_noise;
		this.phot_g_mean_mag = phot_g_mean_mag;
		this.v_mag = v_mag;
		this.e_v_mag = e_v_mag;
		this.b_mag = b_mag;
		this.e_b_mag = e_b_mag;
	}
	
	/**
	 * Parse a {@link TgasApassStar} from the given entry in the TGAS/APASS cross-matched catalogue.
	 * 
	 * @param data
	 * 	A string containing a single entry in the TGAS/APASS cross-matched catalogue.
	 * @return
	 * 	A {@link TgasApassStar}.
	 */
	public static TgasApassStar parseTgasApassStar(String data) {
		
		try(Scanner scan = new Scanner(data)) {
			
			// TGAS fields
			long sourceId = scan.nextLong();
			double ra = scan.nextDouble();
			double ra_error = scan.nextDouble();
			double dec = scan.nextDouble(); 
			double dec_error = scan.nextDouble();
			double parallax = scan.nextDouble();
			double parallax_error = scan.nextDouble();
			double pmra = scan.nextDouble();
			double pmra_error = scan.nextDouble();
			double pmdec = scan.nextDouble();
			double pmdec_error = scan.nextDouble();
			double astrometric_excess_noise = scan.nextDouble();
			double phot_g_mean_mag = scan.nextDouble();
			// APASS fields
			double v_mag = scan.nextDouble();
			double e_v_mag = scan.nextDouble();
			double b_mag = scan.nextDouble();
			double e_b_mag = scan.nextDouble();
			
			return new TgasApassStar(sourceId, ra, ra_error, dec, dec_error, parallax, parallax_error, pmra, pmra_error, 
					pmdec, pmdec_error, astrometric_excess_noise, phot_g_mean_mag, v_mag, e_v_mag, b_mag, e_b_mag);
		}
		catch(IllegalStateException | NoSuchElementException e) {
			// Illegal input data format. In practise this happens because the APASS B or V magnitudes 
			// are in some cases missing and replaced with "" (two double quotes)
			return null;
		}
	}
	
}