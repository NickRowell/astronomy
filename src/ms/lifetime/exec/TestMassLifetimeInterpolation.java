package ms.lifetime.exec;

import ms.lifetime.algo.PreWdLifetime;
import ms.lifetime.infra.PreWdLifetimeModels;

/**
 * Class used to test and develop the pre-WD lifetime vs stellar mass interpolation.
 *
 *
 * @author nrowell
 * @version $Id$
 */
public class TestMassLifetimeInterpolation {
	
	/**
	 * Main appication entry point
	 * @param args
	 * 	The command line arguments (ignored)
	 */
	public static void main(String[] args) {
		
		PreWdLifetime preWd = PreWdLifetimeModels.PADOVA.getPreWdLifetimeModels();
		
		double z = 0.00435456;
		double y = 0.25645;
		
		double lifetime = 5e7;
		
		double mass = preWd.getStellarMass(z, y, lifetime)[0];
		
		System.out.println("Input lifetime = "+lifetime);
		System.out.println("Forward-interpolated mass = "+mass);
		lifetime = preWd.getPreWdLifetime(z, y, mass)[0];
		System.out.println("Reverse-interpolated lifetime  = "+lifetime);
	}
	
}