package ifmr.exec;

import ifmr.algoimpl.Ifmr_Renedo2010_Z0p01;
import ifmr.util.IfmrUtil;

/**
 * Simple application to compute the breakdow mass associated with the Renedo et al (2010) IFMR.
 *
 *
 * @author nrowell
 * @version $Id$
 */
public class ComputeBreakdownMass {

	
	public static void main(String[] args) {
		
		// Get instance
		Ifmr_Renedo2010_Z0p01 ifmr = new Ifmr_Renedo2010_Z0p01();
		
		double m_breakdown = IfmrUtil.getBreakdownMass(ifmr, false);
		
		System.out.println("Breakdown mass = "+m_breakdown);
		System.out.println("Final mass at this initial mass = "+ifmr.getMf(m_breakdown));
		
		
		
	}
	
}
