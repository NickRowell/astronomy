package projects.gaia.dm;

import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Class represents a single star from Gaia DR2 source table with a subset of the fields.
 * 
 * TODO: make the fields final and set them in the constructor.
 *
 * @author nrowell
 * @version $Id$
 */
public class GaiaSdssSource {
	
	/**
	 * The equatorial right ascension [deg]
	 */
	public double ra;
	
	/**
	 * The equatorial declination [deg]
	 */
	public double dec;
	
	/**
	 * The BP- RP colour [mag]
	 */
	public double bmr;
	
	public double m_g;
	
	public double phot_g_mean_flux_over_error;
	
	public double phot_bp_mean_flux_over_error;
	
	public double phot_rp_mean_flux_over_error;
	
	public double parallax_over_error;
	
	public double u;
	public double u_err;

	public double g;
	public double g_err;

	public double r;
	public double r_err;

	public double i;
	public double i_err;
	
	public double z;
	public double z_err;
	
	public double parallax;
	
	/**
	 * Main constructor for the {@link GaiaSdssSource}.
	 * 
	 */
	private GaiaSdssSource() {
	}
	
	
	/**
	 * Parse a {@link GaiaSdssSource} from the String.
	 * 
	 * @param data
	 * 	A string containing the fields of the {@link GaiaSdssSource}
	 * @return
	 * 	A {@link GaiaSdssSource}.
	 */
	public static GaiaSdssSource parseGaiaSdssSource(String data) {
		
		try(Scanner scan = new Scanner(data)) {
			
			// Parsing from csv file
			scan.useDelimiter(",");
			
			GaiaSdssSource gaiaSource = new GaiaSdssSource();
			
			gaiaSource.ra = scan.nextDouble();
			gaiaSource.dec = scan.nextDouble();
			gaiaSource.bmr = scan.nextDouble();
			gaiaSource.m_g = scan.nextDouble();
			gaiaSource.phot_g_mean_flux_over_error = scan.nextDouble();
			gaiaSource.phot_bp_mean_flux_over_error = scan.nextDouble();
			gaiaSource.phot_rp_mean_flux_over_error = scan.nextDouble();
			gaiaSource.parallax_over_error = scan.nextDouble();
			
			gaiaSource.u = scan.nextDouble();
			gaiaSource.u_err = scan.nextDouble();
			
			gaiaSource.g = scan.nextDouble();
			gaiaSource.g_err = scan.nextDouble();
			
			gaiaSource.r = scan.nextDouble();
			gaiaSource.r_err = scan.nextDouble();
			
			gaiaSource.i = scan.nextDouble();
			gaiaSource.i_err = scan.nextDouble();
			
			gaiaSource.z = scan.nextDouble();
			gaiaSource.z_err = scan.nextDouble();
			
			// Parallax in milliarcseconds
			gaiaSource.parallax = scan.nextDouble();
			
			// Make the magnitudes absolute
			gaiaSource.u += 5.0 * Math.log10(gaiaSource.parallax/100.0);
			gaiaSource.g += 5.0 * Math.log10(gaiaSource.parallax/100.0);
			gaiaSource.r += 5.0 * Math.log10(gaiaSource.parallax/100.0);
			gaiaSource.i += 5.0 * Math.log10(gaiaSource.parallax/100.0);
			gaiaSource.z += 5.0 * Math.log10(gaiaSource.parallax/100.0);
			
			return gaiaSource;
		}
		catch(IllegalStateException | NoSuchElementException e) {
			// Illegal input data format. In practise this happens because the Tycho-2 Bt or Vt magnitudes 
			// are in some cases missing and replaced with "" (two double quotes)
			return null;
		}
	}
	
}