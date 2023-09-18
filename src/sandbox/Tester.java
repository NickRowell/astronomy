package sandbox;

import constants.Galactic;
import constants.Units;

/**
 * Class used for ad-hoc testing.
 * 
 * @author nrowell
 */
public class Tester {
	
	/**
	 * Main application entry point.
	 * 
	 * @param args
	 * 	The command line arguments (ignored).
	 */
	public static void main(String[] args) {
		
		System.out.println("A = " + Galactic.A + " km/s/kpc");
		System.out.println("B = " + Galactic.B + " km/s/kpc");
		
		double A_MasYr = Galactic.A * Units.KM_PER_SEC_PER_KPC_TO_MAS_PER_YEAR;
		double B_MasYr = Galactic.B * Units.KM_PER_SEC_PER_KPC_TO_MAS_PER_YEAR;
		
		System.out.println("A = " + A_MasYr + " mas/yr");
		System.out.println("B = " + B_MasYr + " mas/yr");
		
		double A_RadYr = Galactic.A * Units.KM_PER_SEC_PER_KPC_TO_RAD_PER_YEAR;
		double B_RadYr = Galactic.B * Units.KM_PER_SEC_PER_KPC_TO_RAD_PER_YEAR;

		System.out.println("A = " + A_RadYr + " rad/yr");
		System.out.println("B = " + B_RadYr + " rad/yr");
		
	}
}
