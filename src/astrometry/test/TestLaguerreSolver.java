package astrometry.test;

import org.apache.commons.math3.analysis.solvers.LaguerreSolver;
import org.apache.commons.math3.complex.Complex;

/**
 * Class tests the Apache Commons Math Laguerre Solver method.
 *
 *
 * @author nrowell
 * @version $Id$
 */
public class TestLaguerreSolver {

	public static void main(String[] args) {
		
		// Coefficients of the polynomial a3*x^3 + a2*x^2 + a1*x + a0
		double a3 = 3;
		double a2 = 2;
		double a1 = 1;
		double a0 = 1;
		
		LaguerreSolver laguerreSolver = new LaguerreSolver();
		
		Complex[] roots = laguerreSolver.solveAllComplex(new double[]{a0,a1,a2,a3}, 0.0);
		
		// Loop over the roots and verify that they are roots of the original polynomial
		for(Complex root : roots) {
			
			Complex a3x3 = root.pow(3).multiply(a3);
			Complex a2x2 = root.pow(2).multiply(a2);
			Complex a1x1 = root.pow(1).multiply(a1);
			Complex a0x0 = root.pow(0).multiply(a0);
			
			Complex f = a3x3.add(a2x2).add(a1x1).add(a0x0);
			
			System.out.println("Root:       "+root.toString());
			System.out.println("Polynomial: "+f.toString());
			
		}
	}
	
}