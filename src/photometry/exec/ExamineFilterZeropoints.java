package photometry.exec;

import photometry.Filter;
import photometry.FilterUtils;

/**
 * Prints out Vega magnitude zeropoints for a selection of filters in order to test the code.
 *
 * @author nrowell
 * @version $Id$
 */
public class ExamineFilterZeropoints {

	/**
	 * Main application entry point.
	 * 
	 * @param args
	 * 	Ignored
	 */
	public static void main(String[] args) {
		for(Filter filter : Filter.sss) {
			System.out.println("Vega zeropoint magnitude & L_{eff} for "+filter.toString()+": "+
					FilterUtils.getVegaMagZp(filter)+" / "+FilterUtils.getEffectiveWavelength(filter));
			
		}
	}
}
