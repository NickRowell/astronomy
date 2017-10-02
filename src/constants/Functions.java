package constants;

import spectroscopy.util.SpectroscopyUtils;

/**
 * Class contains implementations of various common functions.
 *
 * @author nrowell
 * @version $Id$
 */
public class Functions {


    /**
     * The Planck function (per unit wavelength) for blackbody emission.
     * 
     * @param T
     * 	Temperature [K]
     * @param L
     * 	Wavelength [angstroms]
     * @return
     * 	Spectral radiance [power emitted per unit area of the body, per unit solid angle that the 
     * radiation is measured over, per unit wavelength]
     */
    public static double planckFunction(double T, double L){
    
    	//Planck constant
		double h = Physical.H;
		
		//Speed of light
		double c = Physical.C;
		
		//Boltzmann constant
		double k = Physical.Kb;
		
		// Wavelength in metres
		double Lm = L*SpectroscopyUtils.ANGSTROMS_TO_METRES;
	
		return (2.0*h*c*c/Math.pow(Lm,5))/(Math.exp(h*c/(Lm*k*T)) - 1);
    }
	
}
