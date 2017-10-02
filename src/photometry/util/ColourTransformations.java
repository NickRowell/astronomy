package photometry.util;

/**
 * This class provides implementations of various colour transformation laws.
 * 
 * TODO: a more generic interface would be useful, where the user provides all the magnitudes they
 * have and the band they want, and the class interprets this and uses whatever transformations it has
 * available to provide the best estimate for the magnitude in the requested band.
 * 
 *
 * @author nrowell
 * @version $Id$
 */
public final class ColourTransformations {
	
	/**
	 * Implementation of the Gaia G band colour transformation described in
	 * the Gaia DR1 online documentation at:
	 * 
	 * https://gaia.esac.esa.int/documentation/GDR1/Data_processing/chap_cu5phot/sec_phot_calibr.html
	 * 
	 * @param v
	 * 	Johnson V band magnitude
	 * @param i
	 * 	Johnson I band magnitude
	 * @return
	 * 	Gaia G band magnitude
	 */
	public static double getGaiaGFromJohnsonCousinsVI(double v, double i) {
		
		double vi = v - i;
		
		if(vi <= -0.25 || vi >= 3.25) {
			throw new RuntimeException("V-I ("+vi+") out of range [-0.25:3.25] for transformation to G!");
		}
		
		double gv = 0.02266 - 0.27125*vi - 0.11207*vi*vi;
		double g = gv + v;
		return g;
	}
	
	/**
	 * Implementation of the Gaia G band colour transformation described in
	 * the Gaia DR1 online documentation at:
	 * 
	 * https://gaia.esac.esa.int/documentation/GDR1/Data_processing/chap_cu5phot/sec_phot_calibr.html
	 * 
	 * This relation is suitable for dwarf stars (affects the red end only).
	 * 
	 * @param v
	 * 	Johnson V band magnitude
	 * @param r
	 * 	Johnson R band magnitude
	 * @return
	 * 	Gaia G band magnitude
	 */
	public static double getGaiaGFromJohnsonCousinsVRDwarfs(double v, double r) {
		
		double vr = v - r;
		
		if(vr <= -0.2 || vr >= 1.4) {
			throw new RuntimeException("V-R ("+vr+") out of range [-0.2:1.4] for transformation to G!");
		}
		
		// 
		double gv = 0.0;
		int n = 0;
		
		if(vr > -0.2 && vr < 0.8) {
			// Blue end transformation
			gv += -0.0076783 - 0.35193*vr - 0.7834*vr*vr + 0.302*vr*vr*vr;
			n++;
		}
		
		if(vr > 0.7 && vr < 1.4) {
			// Red end transformation for dwarfs
			gv += -0.41187 + 1.0915*vr - 2.3259*vr*vr + 0.68516*vr*vr*vr;
			n++;
		}
		
		// Take average of all the transformation we applied
		gv /= n;
		double g = gv + v;
		return g;
	}

	/**
	 * Implementation of the Gaia G band colour transformation described in
	 * the Gaia DR1 online documentation at:
	 * 
	 * https://gaia.esac.esa.int/documentation/GDR1/Data_processing/chap_cu5phot/sec_phot_calibr.html
	 * 
	 * This relation is suitable for dwarf stars (affects the red end only).
	 * 
	 * @param b
	 * 	Johnson B band magnitude
	 * @param v
	 * 	Johnson V band magnitude
	 * @return
	 * 	Gaia G band magnitude
	 */
	public static double getGaiaGFromJohnsonCousinsBVDwarfs(double b, double v) {
		
		double bv = b - v;
		
		if(bv <= -0.2 || bv >= 1.8) {
			throw new RuntimeException("B-V ("+bv+") out of range [-0.2:1.8] for transformation to G!");
		}
		
		// 
		double gv = 0.0;
		int n = 0;
		
		if(bv > -0.2 && bv <= 1.25) {
			// Blue end transformation
			gv += -0.034281 - 0.084107*bv - 0.46201*bv*bv + 0.17151*bv*bv*bv;
			n++;
		}
		
		if(bv >= 1.25 && bv < 1.8) {
			// Red end transformation for dwarfs
			gv += -4.2544 + 9.6997*bv - 7.1348*bv*bv + 1.3729*bv*bv*bv;
			n++;
		}
		
		// Take average of all the transformation we applied
		gv /= n;
		double g = gv + v;
		return g;
	}
	
	
	
	
	
}
