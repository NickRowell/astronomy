package ms.lifetime.exec;

import numeric.minimisation.nllsq.algoimpl.LevenbergMarquardtExponentialFitter;

/**
 * This class provides an application to fit an exponential function through a
 * set of data points, intended for use in deriving a functional fit to main
 * sequence (or pre-WD) lifetime data.
 *
 * @author nrowell
 * @version $Id$
 */
public class FitExponentialModel {
	
	/**
	 * Main application entry point.
	 * 
	 * NOTE that the fit can be performed using Gnuplot via the following script:
	 * 
	 * > f(x) = A * x ** B
	 * > A = 1e10
	 * > B = -2.5
	 * > FIT_LIMIT = 1e-6
	 * > fit f(x) 'data.txt' via A, B
	 * 
	 * ...where data.txt contains the data values below entered in two columns.
	 * Note that in this problem, Gnuplot does not adjust the value of A at all.
	 * 
	 * 
	 * 
	 * @param args
	 * 	The command line arguments (ignored)
	 */
	public static void main(String[] args) {
		
		// The input data (Renedo et al. 2010 pre-WD lifetimes as a function of mass)
		double[] x = {1.0, 1.5, 1.75, 2.0, 2.25, 2.5, 3.0, 3.5, 4.0, 5.0};
		double[] y = { 1.1113639294916761E10, 2.694704969130745E9, 1.6922729519988887E9, 1.2043908122328372E9, 9.812294497817005E8, 7.334557563276579E8, 4.332254445064522E8, 2.8035178932861906E8, 1.8753804317388174E8, 1.0362295551132117E8};
		
		// Reasonable starting values for the paramaters A and B, where the model that is
		// fitted to the data is of the form f(x) = A * x^{B}
		double A = 1e10;
		double B = -2.5;
		
		System.out.println("Starting A = "+A);
		System.out.println("Starting B = "+B);
		
		LevenbergMarquardtExponentialFitter fitter = new LevenbergMarquardtExponentialFitter(x, y);
		
		fitter.A = A;
		fitter.B = B;
		
		fitter.invoke();
		
		// Retrieve the fitted values
		A = fitter.A;
		B = fitter.B;
		
		System.out.println("Fitted A = "+A);
		System.out.println("Fitted B = "+B);
		
		
	}
	
}