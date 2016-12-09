package ifmr.algoimpl.test;

import ifmr.algo.BaseIfmr;
import ifmr.infra.IFMR;
import imf.algo.BaseImf;

public class TestIfmr
{
	
	
	
	public static void main(String[] args)
	{
		
		double mi_min = BaseImf.M_lower;
		double mi_max = BaseImf.M_upper;
		int nSteps = 100;
		double miStep = (mi_max - mi_min)/nSteps;
		
		for(IFMR ifmrType : IFMR.values())
		{
			// Retrieve IFMR instance
			BaseIfmr ifmr = ifmrType.getIFMR();
			
			for(int step = 0; step <=nSteps; step++)
			{
				// Translate index to initial mass
				double mi = mi_min + step*miStep;
				
				double mf = ifmr.getMf(mi);
				
				// Convert back to mi
				double mi_inverse = ifmr.getMi(mf);
				
				// Check IFMR for consistency
				if(Math.abs(mi_inverse - mi) > 1e-12)
				{
					System.out.println(ifmrType + ": inconsistent initial mass: "+mi+" -> "+mf+" -> "+mi_inverse);
				}
			}
		}
		
		// Get a MS instance, for determining MS ages.
//        MS ms = MS.Type.z017y23.getBaseMainSequenceLifetime();
//        
//        // Loop over each IFMR type:
//        for(IFMR ifmr_type : IFMR.values()){
//                    
//            BaseIfmr ifmr = ifmr_type.getIFMR();
//            
//            // Get MS age of stars of breakdown mass
//            double age = ms.getLifetime(ifmr.getBreakdownInitialMass());
//            
//            System.out.println("IFMR = "+ifmr.toString()+", breakdown age = "+age);
//        
//        }
		
		
	}
	
	

}
