package photometry.exec;

import photometry.Filter;
import photometry.util.FilterUtils;

/**
 * Class examines the colours of blackbody sources of different temperatures in various bands.
 *
 *
 * @author nrowell
 * @version $Id$
 */
public class ExamineBlackbodyColours {

	/**
	 * Main application entry point.
	 * 
	 * @param args
	 * 	Ignored
	 */
	public static void main(String[] args) {
		
		Filter f1 = Filter.B_J;
		Filter f2 = Filter.R_59F;
		
		// Temperature range [K]
		double tmin = 3000;
		double tmax = 10000;
		
		// Step in temperature [K]
		double tstep = 10.0;
		
		for(double t=tmin; t<tmax; t+=tstep) {
			// Compute blackbody magnitude in each band
			double colF1F2 = FilterUtils.blackbodyColour(f1, f2, t);
			System.out.println(t + "\t" + colF1F2);
		}
		
		
	}
}
