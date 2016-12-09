package projects.tgas.dm;

import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Class represents a single star in the TGAS catalogue.
 *
 * @author nrowell
 * @version $Id$
 */
public class TgasStar {
	
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
	 * Mean magnitude in the Gaia G band [mag]
	 */
	public final double phot_g_mean_mag;
	
	/**
	 * TYCHO-2
	 * Tycho-2 BT magnitude [mag]
	 */
	public final double bt_mag;
	
	/**
	 * TYCHO-2
	 * Error on the Tycho-2 BT magnitude [mag]
	 */
	public final double e_bt_mag;
	
	/**
	 * TYCHO-2
	 * Tycho-2 VT magnitude [mag]
	 */
	public final double vt_mag;
	
	/**
	 * TYCHO-2
	 * Error on the Tycho-2 VT magnitude [mag]
	 */
	public final double e_vt_mag;
	
	/**
	 * Quantifies the excess scatter of the observations about the astrometric model [mas]
	 */
	public final double astrometric_excess_noise;

	/**
	 * Total number of AL observations used in the astrometric solution of the source
	 */
	public final int astrometric_n_obs_al;
	
	/**
	 * Number of AL observations that were not strongly downweighted in the astrometric solution of the source
	 */
	public final int astrometric_n_good_obs_al;
	
	/**
	 * Total number of AC observations used in the astrometric solution of the source
	 */
	public final int astrometric_n_obs_ac;
	
	/**
	 * Number of AC observations that were not strongly downweighted in the astrometric solution of the source
	 */
	public final int astrometric_n_good_obs_ac;
	
	/**
	 * Tycho 2 identifier. The TYC identifier is constructed from the GSC region number (TYC1), the running 
	 * number within the region (TYC2) and a component identifier (TYC3) which is normally 1.
	 */
	public final String tycho2_id;

	/**
	 * Main constructor for the {@link TgasStar}.
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
	 * @param phot_g_mean_mag
	 * 	Mean magnitude in the Gaia G band [mag]
	 * @param bt_mag
	 * 	Tycho-2 BT magnitude [mag]
	 * @param e_bt_mag
	 * 	Error on the Tycho-2 BT magnitude [mag]
	 * @param vt_mag
	 * 	Tycho-2 VT magnitude [mag]
	 * @param e_vt_mag
	 * 	Error on the Tycho-2 VT magnitude [mag]
	 * @param astrometric_excess_noise
	 * 	Quantifies the excess scatter of the observations about the astrometric model [mas]
	 * @param astrometric_n_obs_al
	 * 	Total number of AL observations used in the astrometric solution of the source
	 * @param astrometric_n_good_obs_al
	 * 	Number of AL observations that were not strongly downweighted in the astrometric solution of the source
	 * @param astrometric_n_obs_ac
	 * 	Total number of AC observations used in the astrometric solution of the source
	 * @param astrometric_n_good_obs_ac
	 * 	Number of AC observations that were not strongly downweighted in the astrometric solution of the source
	 * @param tycho2_id
	 * 	Tycho 2 identifier. The TYC identifier is constructed from the GSC region number (TYC1), the running 
	 * number within the region (TYC2) and a component identifier (TYC3) which is normally 1.
	 */
	public TgasStar(long sourceId, double ra, double ra_error, double dec, double dec_error, double parallax, double parallax_error, double pmra, double pmra_error, 
			double pmdec, double pmdec_error, double phot_g_mean_mag, double bt_mag, double e_bt_mag, double vt_mag, double e_vt_mag, double astrometric_excess_noise, 
			int astrometric_n_obs_al, int astrometric_n_good_obs_al, int astrometric_n_obs_ac, int astrometric_n_good_obs_ac, String tycho2_id) {
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
		this.phot_g_mean_mag = phot_g_mean_mag;
		this.bt_mag = bt_mag;
		this.e_bt_mag = e_bt_mag;
		this.vt_mag = vt_mag;
		this.e_vt_mag = e_vt_mag;
		this.astrometric_excess_noise = astrometric_excess_noise;
		this.astrometric_n_obs_al = astrometric_n_obs_al;
		this.astrometric_n_good_obs_al = astrometric_n_good_obs_al;
		this.astrometric_n_obs_ac = astrometric_n_obs_ac;
		this.astrometric_n_good_obs_ac = astrometric_n_good_obs_ac;
		this.tycho2_id = tycho2_id;
	}
	
	
	/**
	 * Parse a {@link TgasStar} from the given entry in the TGAS catalogue.
	 * 
	 * @param data
	 * 	A string containing a single entry in the TGAS catalogue.
	 * @return
	 * 	A {@link TgasStar}.
	 */
	public static TgasStar parseTgasStar(String data) {
		
		try(Scanner scan = new Scanner(data)) {
			
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
			double phot_g_mean_mag = scan.nextDouble();
			
			double bt_mag = scan.nextDouble();
			double e_bt_mag = scan.nextDouble();
			
			double vt_mag = scan.nextDouble();
			double e_vt_mag = scan.nextDouble();
			double astrometric_excess_noise = scan.nextDouble();
			int astrometric_n_obs_al = scan.nextInt();
			int astrometric_n_good_obs_al = scan.nextInt();
			int astrometric_n_obs_ac = scan.nextInt();
			int astrometric_n_good_obs_ac = scan.nextInt();
			String tycho2_id = scan.next();
			
			return new TgasStar(sourceId, ra, ra_error, dec, dec_error, parallax, parallax_error, pmra, pmra_error, 
					pmdec, pmdec_error, phot_g_mean_mag, bt_mag, e_bt_mag, vt_mag, e_vt_mag, astrometric_excess_noise, 
					astrometric_n_obs_al, astrometric_n_good_obs_al, astrometric_n_obs_ac, astrometric_n_good_obs_ac, tycho2_id);
		}
		catch(IllegalStateException | NoSuchElementException e) {
			// Illegal input data format. In practise this happens because the Tycho-2 Bt or Vt magnitudes 
			// are in some cases missing and replaced with "" (two double quotes)
			return null;
		}
	}
	
}