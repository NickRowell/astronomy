package projects.gaia.lrh18.dm;

import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Class represents a single star from Gaia DR2 source table with a subset of the fields.
 * 
 * TODO: make the fields final and set them in the constructor.
 * TODO: add additional fields as necessary: sourceID, excess noise, position errors
 * 
 * @author nrowell
 * @version $Id$
 */
public class GaiaSource {
	
	/**
	 * The equatorial right ascension [deg]
	 */
	public double ra;
	
	/**
	 * The equatorial declination [deg]
	 */
	public double dec;
	
	/**
	 * The stellar parallax [mas]
	 */
	public double parallax;

	/**
	 * The error on the stellar parallax [mas]
	 */
	public double parallax_error;
	
	/**
	 * Proper motion in right ascension [mas/yr]
	 */
	public double pmra;

	/**
	 * Error on the proper motion in right ascension [mas/yr]
	 */
	public double pmra_error;

	/**
	 * Proper motion in declination [mas/yr]
	 */
	public double pmdec;

	/**
	 * Error on the proper motion in declination [mas/yr]
	 */
	public double pmdec_error;
	
	/**
	 * G band mean magnitude [mag]
	 */
	public double phot_g_mean_mag;

	/**
	 * BP band mean magnitude [mag]
	 */
	public double phot_bp_mean_mag;

	/**
	 * RP band mean magnitude [mag]
	 */
	public double phot_rp_mean_mag;
	
	/**
	 * G-band mean flux divided by its error
	 */
	public double phot_g_mean_flux_over_error;

	/**
	 * BP-band mean flux divided by its error
	 */
	public double phot_bp_mean_flux_over_error;

	/**
	 * RP-band mean flux divided by its error
	 */
	public double phot_rp_mean_flux_over_error;
	
	/**
	 * Astrometric excess noise.
	 */
	public double astrometric_excess_noise;
	
	/**
	 * Chi-squared for the astrometric solution.
	 */
	public double astrometric_chi2_al;
	
	/**
	 * Number of good AL observations in the astrometric solution.
	 */
	public int astrometric_n_good_obs_al;
	
	/**
	 * Main constructor for the {@link GaiaSource}.
	 * 
	 */
	private GaiaSource() {
	}
	
	/**
	 * Copy constructor for the {@link GaiaSource}.
	 * 
	 * @param copyme
	 * 	The {@link GaiaSource} to copy.
	 */
	public GaiaSource(GaiaSource copyme) {
		ra = copyme.ra;
		dec = copyme.dec;
		parallax = copyme.parallax;
		parallax_error = copyme.parallax_error;
		pmra = copyme.pmra;
		pmra_error = copyme.pmra_error;
		pmdec = copyme.pmdec;
		pmdec_error = copyme.pmdec_error;
		phot_g_mean_mag = copyme.phot_g_mean_mag;
		phot_bp_mean_mag = copyme.phot_bp_mean_mag;
		phot_rp_mean_mag = copyme.phot_rp_mean_mag;
		phot_g_mean_flux_over_error = copyme.phot_g_mean_flux_over_error;
		phot_bp_mean_flux_over_error = copyme.phot_bp_mean_flux_over_error;
		phot_rp_mean_flux_over_error = copyme.phot_rp_mean_flux_over_error;
		astrometric_excess_noise = copyme.astrometric_excess_noise;
		astrometric_chi2_al = copyme.astrometric_chi2_al;
		astrometric_n_good_obs_al = copyme.astrometric_n_good_obs_al;
	}
	
	/**
	 * Parse a {@link GaiaSource} from the String.
	 * 
	 * @param data
	 * 	A string containing the fields of the {@link GaiaSource}
	 * @return
	 * 	A {@link GaiaSource}.
	 */
	public static GaiaSource parseGaiaSource(String data) {
		
		try(Scanner scan = new Scanner(data)) {
			
			// Parsing from csv file
			scan.useDelimiter(",");
			
			GaiaSource gaiaSource = new GaiaSource();
			
			gaiaSource.ra = scan.nextDouble();
			gaiaSource.dec = scan.nextDouble();
			gaiaSource.parallax = scan.nextDouble();
			gaiaSource.parallax_error = scan.nextDouble();
			gaiaSource.pmra = scan.nextDouble();
			gaiaSource.pmra_error = scan.nextDouble();
			gaiaSource.pmdec = scan.nextDouble();
			gaiaSource.pmdec_error = scan.nextDouble();
			gaiaSource.phot_g_mean_mag = scan.nextDouble();
			gaiaSource.phot_bp_mean_mag = scan.nextDouble();
			gaiaSource.phot_rp_mean_mag = scan.nextDouble();
			gaiaSource.phot_g_mean_flux_over_error = scan.nextDouble();
			gaiaSource.phot_bp_mean_flux_over_error = scan.nextDouble();
			gaiaSource.phot_rp_mean_flux_over_error = scan.nextDouble();
			gaiaSource.astrometric_excess_noise = scan.nextDouble();
			
			// XXX: purge the bp_rp field for the time being
			scan.next();
			gaiaSource.astrometric_chi2_al = scan.nextDouble();
			gaiaSource.astrometric_n_good_obs_al = scan.nextInt();
			
			return gaiaSource;
		}
		catch(IllegalStateException | NoSuchElementException e) {
			// Illegal input data format. In practise this happens because the Tycho-2 Bt or Vt magnitudes 
			// are in some cases missing and replaced with "" (two double quotes)
			return null;
		}
	}
	
}