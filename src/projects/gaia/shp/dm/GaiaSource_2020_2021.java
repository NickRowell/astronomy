package projects.gaia.shp.dm;

import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Class represents a single star from Gaia EDR3 source table with a subset of the fields.
 * 
 * @author nrowell
 * @version $Id$
 */
public class GaiaSource_2020_2021 {
	
	/**
	 * The source ID.
	 */
	public long sourceId;
	
	/**
	 * The equatorial right ascension [deg]
	 */
	public double ra;
	
	/**
	 * The error on the equatorial right ascension [mas]
	 */
	public double ra_error;
	
	/**
	 * The equatorial declination [deg]
	 */
	public double dec;
	
	/**
	 * The error on the equatorial declination [mas]
	 */
	public double dec_error;
	
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
	 * Renormalised unit weight error.
	 */
	public double ruwe;

	/**
	 * Photometric BP-RP flux excess factor.
	 */
	public double phot_bp_rp_excess_factor;
	
	/**
	 * Main constructor for the {@link GaiaSource_2020_2021}.
	 * 
	 */
	private GaiaSource_2020_2021() {
	}
	
	/**
	 * Copy constructor for the {@link GaiaSource_2020_2021}.
	 * 
	 * @param copyme
	 * 	The {@link GaiaSource_2020_2021} to copy.
	 */
	public GaiaSource_2020_2021(GaiaSource_2020_2021 copyme) {
		sourceId = copyme.sourceId;
		ra = copyme.ra;
		ra_error = copyme.ra_error;
		dec = copyme.dec;
		dec_error = copyme.dec_error;
		parallax = copyme.parallax;
		parallax_error = copyme.parallax_error;
		pmra = copyme.pmra;
		pmra_error = copyme.pmra_error;
		pmdec = copyme.pmdec;
		pmdec_error = copyme.pmdec_error;
		phot_g_mean_mag = copyme.phot_g_mean_mag;
		phot_bp_mean_mag = copyme.phot_bp_mean_mag;
		phot_rp_mean_mag = copyme.phot_rp_mean_mag;
		ruwe = copyme.ruwe;
		phot_bp_rp_excess_factor = copyme.phot_bp_rp_excess_factor;
	}
	
	/**
	 * Parse a {@link GaiaSource_2020_2021} from the String.
	 * 
	 * @param data
	 * 	A string containing the fields of the {@link GaiaSource_2020_2021}
	 * @return
	 * 	A {@link GaiaSource_2020_2021}.
	 */
	public static GaiaSource_2020_2021 parseGaiaSource(String data) {
		
		try(Scanner scan = new Scanner(data)) {
			
			// Parsing from csv file
			scan.useDelimiter(",");
			
			GaiaSource_2020_2021 gaiaSource = new GaiaSource_2020_2021();
			
			gaiaSource.sourceId = scan.nextLong();
			gaiaSource.ra = scan.nextDouble();
			gaiaSource.ra_error = scan.nextDouble();
			gaiaSource.dec = scan.nextDouble();
			gaiaSource.dec_error = scan.nextDouble();
			gaiaSource.parallax = scan.nextDouble();
			gaiaSource.parallax_error = scan.nextDouble();
			gaiaSource.pmra = scan.nextDouble();
			gaiaSource.pmra_error = scan.nextDouble();
			gaiaSource.pmdec = scan.nextDouble();
			gaiaSource.pmdec_error = scan.nextDouble();
			gaiaSource.phot_g_mean_mag = scan.nextDouble();
			gaiaSource.phot_bp_mean_mag = scan.nextDouble();
			gaiaSource.phot_rp_mean_mag = scan.nextDouble();
			gaiaSource.ruwe = scan.nextDouble();
			gaiaSource.phot_bp_rp_excess_factor = scan.nextDouble();
			
			return gaiaSource;
		}
		catch(IllegalStateException | NoSuchElementException e) {
			// Illegal input data format. In practise this happens because the Tycho-2 Bt or Vt magnitudes 
			// are in some cases missing and replaced with "" (two double quotes)
			return null;
		}
	}
	
}