package astrometry;

import java.util.LinkedList;
import java.util.List;

import constants.Galactic;
import Jama.Matrix;

/**
 * Implements the proper motion deprojection from 
 * 
 * Dehnen & Binney (1998) "Local stellar kinematics from Hipparcos data", MNRAS 298:2 387-394
 *
 *
 * @author nrowell
 * @version $Id$
 */
public class ProperMotionDeprojection {

	
	/**
	 * Input data loaded into the calculation.
	 */
	List<double[]> inputs = new LinkedList<>();
	
	/**
	 * Boolean used to indicate whether new objects have been added to the sample since the last
	 * computation of the proper motion deprojection. This is to reduce unneccesary recalculations.
	 */
	boolean ready;
	
	/**
	 * The mean velocity components.
	 */
	Matrix meanVelocity;
	
	/**
	 * The velocity dispersion tensor.
	 */
	Matrix velDisp;
	
	/**
	 * Default constructor.
	 */
	public ProperMotionDeprojection(){}
	
	/**
	 * 
	 * 
	 * @param ra		Right ascension [radians]
	 * @param dec		Declination [radians]
	 * @param ra_dot	Proper motion component parallel to celestial equator [radians/year]
	 * @param dec_dot	Proper motion component perpendicular to celestial equator [radians/year]
	 * @param dist		Distance to object [parsecs]
	 * @param sigDist	Error (standard deviation) on distance [parsecs]
	 * @return
	 */
	public boolean addObject(double ra, double dec, double ra_dot, double dec_dot, double dist, double sigDist)
	{
		return addObject(ra, dec, ra_dot, dec_dot, dist, 1.0);
	}
	
	/**
	 * 
	 * 
	 * @param ra		Right ascension [radians]
	 * @param dec		Declination [radians]
	 * @param ra_dot	Proper motion component parallel to celestial equator [radians/year]
	 * @param dec_dot	Proper motion component perpendicular to celestial equator [radians/year]
	 * @param dist		Distance to object [parsecs]
	 * @param sigDist	Error (standard deviation) on distance [parsecs]
	 * @param weight	Weight to be applied to this object in the calculation [dimensionless]
	 * @return
	 */
	public boolean addObject(double ra, double dec, double ra_dot, double dec_dot, double dist, double sigDist,
			double weight)
	{
		ready=false;
		return inputs.add(new double[]{ra, dec, ra_dot, dec_dot, dist, weight});
	}
	
	/**
	 * 
	 * Performs proper motion deprojection on the inputs currently loaded into the class.
	 * 
	 * TODO: add computation of velocity dispersion tensor, and covariance matrices on mean and
	 * dispersion elements.
	 * 
	 * TODO: implement weighted calculation.
	 * 
	 */
	private void deprojectProperMotions()
	{
		
		Matrix I = Matrix.identity(3,3);
	
        // Individual quantities
        Matrix p   = new Matrix(3,1);
        Matrix ep2 = new Matrix(3,1);
        Matrix A   = new Matrix(3,3);

        // Sum quantities
        Matrix meanP   = new Matrix(3,1);
        Matrix meanEP2 = new Matrix(3,1);
        Matrix meanA   = new Matrix(3,3);

        // Total weight of all stars added to 
        double n=0;

        for(double[] object : inputs){

        	double ra      = object[0];
        	double dec     = object[1];
        	double ra_dot  = object[2];
        	double dec_dot = object[3];
        	double d       = object[4];
        	double sig_d   = object[5];
        	double weight  = object[6];
        	
            //+++ get unit position vector +++//
        	// TODO: is there an astro utils method for this?
            Matrix r = new Matrix(new double[][]{{Math.cos(ra) * Math.cos(dec)},
                                                 {Math.sin(ra) * Math.cos(dec)},
                                                 {Math.sin(dec)}});

            //+++ Calculate components of projection matrix relative to Equatorial triad +++//
            A = I.minus(r.times(r.transpose()));

            //+++ Transform A to Galactic coordinate frame +++//
            A = Galactic.r_G_E.times(A.times(Galactic.r_E_G));

            //+++ Calculate proper motion vector components relative to Equatorial triad +++//
            p.set(0, 0, 4.74 * d * (-Math.sin(ra) * Math.cos(dec) * ra_dot - Math.cos(ra) * Math.sin(dec) * dec_dot));
            p.set(1, 0, 4.74 * d * (Math.cos(ra) * Math.cos(dec) * ra_dot - Math.sin(ra) * Math.sin(dec) * dec_dot));
            p.set(2, 0, 4.74 * d * (Math.cos(dec) * dec_dot));

            //+++ Transform proper motion vector to Galactic frame +++//
            p = Galactic.r_G_E.times(p);

            //+++ Get proper motion velocity error in Galactic frame +++//
            ep2.set(0, 0, Math.pow(p.get(0, 0) * sig_d / d, 2.0));
            ep2.set(1, 0, Math.pow(p.get(1, 0) * sig_d / d, 2.0));
            ep2.set(2, 0, Math.pow(p.get(2, 0) * sig_d / d, 2.0));

            //+++ Weight components and add to sum total for ensemble +++//
            // FIXME: add weighting here
            meanP = meanP.plus(p.times(weight));
            meanA = meanA.plus(A.times(weight));
            meanEP2 = meanEP2.plus(ep2.times(weight*weight));
            n+=weight;
        }

        //+++ Divide A, p & ep2 component-wise by number of stars to get mean +++//
        meanP   = meanP.times(1.0 / n);
        meanEP2 = meanEP2.times(1.0 / (n*n));
        meanA   = meanA.times(1.0 / n);

        //+++ Deprojection operation using mean values +++//

        //+++ Invert <A> +++//
        Matrix invA = meanA.inverse();

        //+++ Get errors on mean velocities from distance uncertainties alone +++//
        double sig2U_d = ((invA.get(0,0)*invA.get(0,0))*meanEP2.get(0,0)) +
                         ((invA.get(0,1)*invA.get(0,1))*meanEP2.get(1,0)) +
                         ((invA.get(0,2)*invA.get(0,2))*meanEP2.get(2,0));

        double sig2V_d = ((invA.get(1,0)*invA.get(1,0))*meanEP2.get(0,0)) +
                         ((invA.get(1,1)*invA.get(1,1))*meanEP2.get(1,0)) +
                         ((invA.get(1,2)*invA.get(1,2))*meanEP2.get(2,0));
        
        double sig2W_d = ((invA.get(2,0)*invA.get(2,0))*meanEP2.get(0,0)) +
                         ((invA.get(2,1)*invA.get(2,1))*meanEP2.get(1,0)) +
                         ((invA.get(2,2)*invA.get(2,2))*meanEP2.get(2,0));

        //+++ Get statistical uncertainty on mean velocities due to Poisson counting errors +++//
        double sig2U_N = (1.0/n)*
                         (((invA.get(0,0)*invA.get(0,0))*meanP.get(0,0)*meanP.get(0,0)) +
                          ((invA.get(0,1)*invA.get(0,1))*meanP.get(1,0)*meanP.get(1,0)) +
                          ((invA.get(0,2)*invA.get(0,2))*meanP.get(2,0)*meanP.get(2,0)));
        double sig2V_N = (1.0/n)*
                         (((invA.get(1,0)*invA.get(1,0))*meanP.get(0,0)*meanP.get(0,0)) +
                          ((invA.get(1,1)*invA.get(1,1))*meanP.get(1,0)*meanP.get(1,0)) +
                          ((invA.get(1,2)*invA.get(1,2))*meanP.get(2,0)*meanP.get(2,0)));
        double sig2W_N = (1.0/n)*
                         (((invA.get(2,0)*invA.get(2,0))*meanP.get(0,0)*meanP.get(0,0)) +
                          ((invA.get(2,1)*invA.get(2,1))*meanP.get(1,0)*meanP.get(1,0)) +
                          ((invA.get(2,2)*invA.get(2,2))*meanP.get(2,0)*meanP.get(2,0)));


        //+++ Combine errors on mean velocities +++//
        double sig2U = sig2U_d + sig2U_N;
        double sig2V = sig2V_d + sig2V_N;
        double sig2W = sig2W_d + sig2W_N;

        //+++ Multiply <A>^-1 by <p> to get <v> +++//
        Matrix meanV = invA.times(meanP);

        //+++ Write header +++//
        System.out.println("\nDeprojection of proper motions");
        System.out.println("------------------------------");
        System.out.println("\nUncertainties are due to errors on distance and Poisson noise on star counts.");
        System.out.println("No uncertainty on position is considered, so matrix A has no errors.");
        System.out.println("\nMoments of the velocity distribution\n------------------------------------");

        //+++ Display mean velocities and errors due to distance uncertainties +++//
        System.out.println("\nMean velocities:");
        System.out.println("<U> = " + meanV.get(0, 0) + "\t+/- "+Math.sqrt(sig2U));
        System.out.println("<V> = " + meanV.get(1, 0) + "\t+/- "+Math.sqrt(sig2V));
        System.out.println("<W> = " + meanV.get(2, 0) + "\t+/- "+Math.sqrt(sig2W));

        System.out.println("\nNumber of stars = " + n);
        
		ready=true;
	}
	
	
	
	
	public Matrix getMean()
	{
		if(!ready)
			deprojectProperMotions();
		return meanVelocity;
	}
	
	public Matrix getVelocityDispersion()
	{
		if(!ready)
			deprojectProperMotions();
		return velDisp;
	}
	
	
	


}
