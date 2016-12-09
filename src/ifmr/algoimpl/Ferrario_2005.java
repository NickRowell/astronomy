package ifmr.algoimpl;

import ifmr.algo.BaseIfmr;

/**
 * 
 * Initial-Final mass relation from:
 * 
 * Ferrario, Wickramasinghe, Liebert & Williams MNRAS 361:1131-1135 (2005).
 * 
 * This class implements the linear version (not the sixth order polynomial fit).
 * 
 * @author nickrowell
 */
public class Ferrario_2005 extends BaseIfmr {
	
	
	/**
	 * Parameter A of linear model mf = A*(mi) + B
	 */
	private double A = 0.10038;
	
	/**
	 * Parameter B of linear model mf = A*(mi) + B
	 */
	private double B = 0.43443;
	
	
	
	/**
	 * {@inheritDoc}
	 */
    @Override
    public double getMf(double mi) { return A*mi + B;}
    
    /**
	 * {@inheritDoc}
	 */
    @Override
    public double getMi(double mf) { return (mf - B)/A;}
    
    /**
     * From the solution of A * mi + B = mi; i.e. getMf(mi) = mi.
	 * {@inheritDoc}
	 */
    @Override
    public double getBreakdownInitialMass(){ return -B/(A-1);}
    
    /**
	 * {@inheritDoc}
	 */
    @Override
    public String toString(){ return "Ferrario (2005) linear";}
    
    
    
    
    // The following code implemented the sixth order polynomial from the paper.
    // It requires the Flanagan Java maths library that was removed from
    // the public domain.
    
  //class Ferrario_2005_Polynomial extends IFMR
  //{
  //
//      private final double a0 = 0.46575;
//      private final double a1 = 0.19022;
//      private final double a2 = -0.21550;
//      private final double a3 = 0.12350;
//      private final double a4 = -0.02960;
//      private final double a5 = 0.003160;
//      private final double a6 = -0.00012336;
  //    
//      @Override
//      public double getMf(double mi) 
//      {
//          return ( a0 * Math.pow(mi,0) + 
//                   a1 * Math.pow(mi,1) + 
//                   a2 * Math.pow(mi,2) + 
//                   a3 * Math.pow(mi,3) + 
//                   a4 * Math.pow(mi,4) + 
//                   a5 * Math.pow(mi,5) +  
//                   a6 * Math.pow(mi,6));
//      }
  //
//      @Override
//      public double getMi(double mf) 
//      {
//          
//          /**
//           * This is tricky, given that the relation is a sixth order polynomial.
//           * Use the Flanagan maths library to solve this.
//           * We first subtract the constant mf from the forward polynomial to get a
//           * sixth order polynomial that has a root at the desired initial mass.
//           */
//          Polynomial shifted = new Polynomial(new double[]{a0-mf,a1,a2,a3,a4,a5,a6});
//                  
//          // We then use the inbuilt functions of the flanagan library to find
//          // all the roots of this polynomial.
//          //Complex[] roots = shifted.laguerreAll();
//          Complex[] roots = shifted.roots();
//          
//          // Now search these roots for a) real roots with b) values in the
//          // range 1-8 (solar masses)
//          
//          // Store all possible solutions (should be one, but checking for
//          // multiple solutions allows a sanity check on inversion procedure).
//          List<Complex> solutions = new LinkedList<Complex>();
//          
//          for(Complex root : roots)
//          {
//              // Threshold of 1E-3 on imaginary part for real root finding
//              if (root.isReal(1E-3))
//              {
//                  if(root.getReal() > IMF.M_lower &&
//                     root.getReal() < IMF.M_upper)
//                  {
//                      solutions.add(root);
//                  }
//              }
//          }
//          
//          // Now check number of possible solutions...
//          if(solutions.size()>1)
//          {
//              System.err.print("More than one initial mass solution!");
//              System.exit(1);
//          }
//          if(solutions.isEmpty())
//          {
//              System.err.print("Zero initial mass solutions!");
//              System.err.println("Complex roots for mi:");
//              for(Complex root : roots)
//              {
//                  System.err.println(root.toString());
//              }
//              System.exit(1);
//          }        
//          
//          // If only one, return the real part as the solution for the initial mass
//          return solutions.get(0).getReal();
  //   
//      }
  //
//      /*
//       * As this initial-final mass relation is a sixth order polynomial, there
//       * is no single solution for the point m_i = m_f. The solutions are the
//       * roots of the polynomial:
//       * 
//       * a6*M^6 + a5*M^5 + a4*M^4 + a3*M^3 + a2*M^2 + (a1-1)*M + a0 = 0
//       * 
//       * which are:
//       * 
//       *   10.4796 + 2.9531i
//       *   10.4796 - 2.9531i
//       *    2.9980 + 4.8798i
//       *    2.9980 - 4.8798i
//       *   -1.8609          
//       *    0.5218  
//       * 
//       * Only the final one is physically meaningful. This is the breakdown mass.
//       * 
//       */
//      @Override
//      public double getBreakdownMass(){ return 0.5218;}
  //    
//      @Override
//      public String toString(){ return "Ferrario (2005) polynomial";}
  //    
  //}

    
    
    
}
