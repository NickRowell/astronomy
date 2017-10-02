package spectroscopy.util;

import constants.Physical;

/**
 * Class provides various static methods useful for manipulating spectroscopic data.
 * 
 * 
 * @author nrowell
 */
public class SpectroscopyUtils {
	
	/**
	 * Angstroms -> metres conversion factor.
	 */
	public static double ANGSTROMS_TO_METRES = 1E-10;

	/**
	 * Metres -> Angstroms conversion factor.
	 */
	public static double METRES_TO_ANGSTROMS = 1E10;
	
	/**
	 * Convert a wavelength in Angstroms to a frequency in Hertz.
	 * @param angstroms
	 * 	Wavelength in Angstroms.
	 * @return
	 * 	Frequency in Hertz.
	 */
	public static double angstromsToHz(double angstroms) {
		double hz = (Physical.C)/(angstroms*ANGSTROMS_TO_METRES);
		return hz;
	}
	
	/**
	 * Convert a frequency in Hertz to a wavelength in Angstroms.
	 * @param hz
	 * 	Frequency in Hertz.
	 * @return
	 * 	Wavelength in Angstroms.
	 */
	public static double hzToAngstroms(double hz) {
		double angstroms = METRES_TO_ANGSTROMS * (Physical.C)/hz;
		return angstroms;
	}
	
	/**
	 * Converts a flux measurement from frequency flux density units [Hz^{-1}] to
	 * wavelength flux density units [A^{-1}]
	 * 
	 * @param f_nu
	 * 	The frequency flux density (flux-per-Hertz) [Hz^{-1}]
	 * @param nu
	 * 	The frequency [Hz]
	 * @return
	 * 	The wavelength flux density (flux-per-Angstrom) [A^{-1}]
	 */
	public static double frequencyFluxDensityToWavelengthFluxDensity(double f_nu, double nu) {
		double f_lambda = (nu/hzToAngstroms(nu)) * f_nu;
		return f_lambda;
	}
	
	/**
	 * Converts a flux measurement from wavelength flux density units [A^{-1}] to
	 * frequency flux density units [Hz^{-1}]
	 * 
	 * @param f_lambda
	 * 	The wavelength flux density (flux-per-Angstrom) [A^{-1}]
	 * @param lambda
	 * 	The wavelength [A]
	 * @return
	 * 	The frequency flux density (flux-per-Hertz) [Hz^{-1}]
	 */
	public static double wavelengthFluxDensityToFrequencyFluxDensity(double f_lambda, double lambda) {
		double f_nu = (lambda/angstromsToHz(lambda)) * f_lambda;
		return f_nu;
	}
	
}
