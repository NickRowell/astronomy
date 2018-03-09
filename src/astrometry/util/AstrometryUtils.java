package astrometry.util;

import java.util.Random;

import constants.Galactic;
import constants.Units;
import Jama.EigenvalueDecomposition;
import Jama.Matrix;

/**
 * Utilities for astrometry.
 *
 * TODO: test the proper motion conversion method against known values
 *
 * @author nrowell
 * @version $Id$
 */
public class AstrometryUtils
{
	// Random number generator
    static Random random = new Random();
	
    /**
	 * Equation relating proper motion, distance and tangential velocity.
	 * @param mu	Proper motion [arcseconds per year]
	 * @param d		Distance [parsecs]
	 * @return		Tangential velocity [km/s]
	 */
	public static double getVtFromMuAndD(double mu, double d)
	{
		return 4.74 * mu * d;
	}
	
	/**
	 * Equation relating proper motion, distance and tangential velocity.
	 * @param mu	Proper motion [arcseconds per year]
	 * @param vt	Tangential velocity [km/s]
	 * @return		Distance [parsecs]
	 */
	public static double getDFromMuAndVt(double mu, double vt)
	{
		return vt / (4.74 * mu);
	}
	
	/**
	 * Equation relating proper motion, distance and tangential velocity.
	 * @param d		Distance [pc]
	 * @param vt	Tangential velocity [km/s]
	 * @return		Proper motion [arcseconds per year]
	 */
	public static double getMuFromDAndVt(double d, double vt)
	{
		return vt / (4.74 * d);
	}
	
	/**
	 * Returns a unit vector pointing in the direction towards the point defined in 
	 * spherical polar coordinates by (ra,dec). This assumes that declination is
	 * measured from the equator, and that right ascension is measured anticlockwise
	 * from the x axis (looking down from +z).
	 * @param r		Radial distance [arbitrary; returned vector has same units]
	 * @param ra	Right ascension (or Galactic longitude) [radians]
	 * @param dec   Declination (or Galactic latitude) [radians]
	 * @return		A JAMA Matrix (3x1) containing x,y,z.
	 */
	public static Matrix sphericalPolarToCartesian(double r, double ra, double dec)
	{
		double x = r*Math.cos(ra)*Math.cos(dec);
		double y = r*Math.sin(ra)*Math.cos(dec);
		double z = r*Math.sin(dec);
		
		return new Matrix(new double[][]{{x},{y},{z}});
	}
	
	/**
	 * Alternative interface to sphericalPolarToCartesian(double r, double ra, double dec),
	 * where the r, ra and dec components are stored in a 3x1 or 1x3 Matrix.
	 * @param cart	Matrix of dimension 1x3 or 3x1 containing r, ra and dec components.
	 * @return		A JAMA Matrix containing a 3x1 column vector
	 */
	public static Matrix sphericalPolarToCartesian(Matrix cart)
	{
		double r,ra,dec;
		
		if(cart.getRowDimension()==3 && cart.getColumnDimension()==1)
		{
			// 3x1 column vector
			r = cart.get(0, 0);
			ra = cart.get(1, 0);
			dec = cart.get(2, 0);
		}
		else if(cart.getRowDimension()==1 && cart.getColumnDimension()==3)
		{
			// 1x3 row vector
			r = cart.get(0, 0);
			ra = cart.get(0, 1);
			dec = cart.get(0, 2);
		}
		else
		{
			throw new RuntimeException("Vector must be 3x1 or 1x3!");
		}
		
		return sphericalPolarToCartesian(r, ra, dec);
	}
	
	/**
	 * Converts a vector from the cartesian to spherical coordinate representation,
	 * with declination/latitude measured from the xy plane, and right ascension/longitude
	 * measured clockwise from +x about +z.
	 * @param x		X component of vector in cartesian coordinates.
	 * @param y		Y component of vector in cartesian coordinates.
	 * @param z		Z component of vector in cartesian coordinates.
	 * @return		Matrix (3x1) containing r, ra, dec [radians].
	 */
	public static Matrix cartesianToSphericalPolar(double x, double y, double z)
	{
		// Length of vector
		double r = Math.sqrt(x*x + y*y + z*z);
		
		// declination/latitude/elevation...
		double dec = Math.asin(z/r);
		
		// right ascension/longitude/azimuth...
		double ra  = Math.atan2(y, x);
		
		// Shift ra to 0:2pi range
		ra = translateToRangeZeroToTwoPi(ra);
		
		return new Matrix(new double[][]{{r},{ra},{dec}});
	}
	
	/**
	 * Alternative interface to cartesianToSpherical(double x, double y, double z),
	 * where the x, y and z components are stored in a 3x1 or 1x3 Matrix.
	 * @param cart	Matrix of dimension 1x3 or 3x1 containing x, y and z components.
	 * @return		Matrix (3x1) containing r, ra, dec [radians].
	 */
	public static Matrix cartesianToSphericalPolar(Matrix cart)
	{
		double x,y,z;
		
		if(cart.getRowDimension()==3 && cart.getColumnDimension()==1)
		{
			// 3x1 column vector
			x = cart.get(0, 0);
			y = cart.get(1, 0);
			z = cart.get(2, 0);
		}
		else if(cart.getRowDimension()==1 && cart.getColumnDimension()==3)
		{
			// 1x3 row vector
			x = cart.get(0, 0);
			y = cart.get(0, 1);
			z = cart.get(0, 2);
		}
		else
		{
			throw new RuntimeException("Vector must be 3x1 or 1x3!");
		}
		
		return cartesianToSphericalPolar(x, y, z);
	}
	
	/**
	 * General method for converting angular coordinates from one frame to another.
	 * 
	 * @param ra	The angular coordinate around the equator [radians]
	 * @param dec	The angular coordinate perpendicular to the equator [radians]
	 * @param A		The rotation matrix that rotates vectors from the frame of the input coordinates
	 * 				to the desired frame of the output coordinates
	 * @return		The angular coordinates in the desired frame [radians]
	 */
	public static double[] convertPosition(double ra, double dec, Matrix A) {

		// Get unit vector in initial frame pointing along direction to ra,dec
		Matrix r_E = sphericalPolarToCartesian(1.0, ra, dec);
		
		// Rotate this to the Galactic frame
		Matrix r_G = A.times(r_E);
		
		// Get corresponding spherical coordinate representation
		Matrix r_G_sph = cartesianToSphericalPolar(r_G);
		
		// Extract longitude and latitude
		double longitude = r_G_sph.get(1,0);
		double latitude  = r_G_sph.get(2,0);
		
		return new double[]{longitude, latitude};
	}
	
	/**
	 * Converts right ascension and declination in equatorial coordinates to the
	 * equivalent Galactic latitude and longitude.
	 * @param ra	Right ascension [radians]
	 * @param dec	Declination [radians]
	 * @return		Array containing Galactic longitude and latitude [radians]
	 */
	public static double[] convertPositionEqToGal(double ra, double dec)
	{
		return convertPosition(ra, dec, Galactic.r_G_E);
	}

	/**
	 * Converts longitude and latitude in Galactic coordinates to the
	 * equivalent equatorial right ascension and declination.
	 * @param lon	Galactic longitude [radians]
	 * @param lat	Galactic latitude [radians]
	 * @return		Array containing Equatorial right ascension and declination [radians]
	 */
	public static double[] convertPositionGalToEq(double lon, double lat)
	{
		return convertPosition(lon, lat, Galactic.r_E_G);
	}
	
	/**
	 * General method for converting angular coordinates and velocities from one frame to another. The
	 * variable names and comments refer to right ascension and declination only for convenience.
	 * 
	 * @param ra	The angular coordinate around the equator [radians]
	 * @param dec	The angular coordinate perpendicular to the equator [radians]
	 * @param mu_acosd
	 * 				The angular velocity component parallel to the equator [radians/yr]
	 * @param mu_d	The angular velocity component perpendicular to the equator [radians/yr]
	 * @param A		The rotation matrix that rotates vectors from the frame of the input coordinates
	 * 				to the desired frame of the output coordinates
	 * @return		The angular coordinates [radians] and proper motions [radians/yr] in the desired frame
	 */
	public static double[] convertPositionAndProperMotion(double ra, double dec, double mu_acosd, double mu_d, Matrix A)
	{
		// 0) Transform the angular coordinates to the desired frame
		double[] pos_out = convertPosition(ra, dec, A);
		
		// 1) Convert proper motion to proper motion velocity vector by assuming arbitrary distance.
		Matrix vt_in = getTangentialVelocityVector(1.0, ra, dec, mu_acosd, mu_d);
		
		// 2) Rotate the proper motion vector to the desired frame
		Matrix vt_out = A.times(vt_in);
		
		// 3) Project onto the sky to get the angular motion
		double[] mu_out = getProperMotions(1.0, pos_out[0], pos_out[1], vt_out);
		
		return new double[]{pos_out[0], pos_out[1], mu_out[0], mu_out[1]};
	}
	
	/**
	 * Converts position and proper motion from Equatorial frame to Galactic frame.
	 * 
	 * Equivalent to the method presented in 
	 * 
	 * "Transformation of the equatorial proper motion to the galactic system"
	 * Radoslaw Poleski (2013) [arXiv:1306.2945v2]
	 * 
	 * @param ra	Right ascension (Equatorial) [radians]
	 * @param dec	Declination (Equatorial) [radians]
	 * @param mu_acosd	Angular (proper) motion parallel to equator [radians/yr]
	 * @param mu_d	Angular (proper) motion perpendicular to equator [radians/yr]
	 * @return		Array containing components of the position [radians] and proper motion in the Galactic frame [radians/yr]
	 */
	public static double[] convertPositionAndProperMotionEqToGal(double ra, double dec, double mu_acosd, double mu_d)
	{
		double l, b, mu_lcosb, mu_b;
		
		// Transform position via 3D rotation of unit vectors
		double[] posAndMuGal = convertPositionAndProperMotion(ra, dec, mu_acosd, mu_d, Galactic.r_G_E);
		
		l = posAndMuGal[0];
		b = posAndMuGal[1];
		mu_lcosb = posAndMuGal[2];
		mu_b = posAndMuGal[3];
		
		// Alternative method from the Radoslaw Poleski (2013) paper:
		
//		// 1) Transform positions:
//		
//		// NOTE that there is an error in the equations 2 & 3: the "Galactic longitude of the ascending node of the galactic plane"
//		// (which in itself doesn't make sense - it's the ascending node of the Equatorial plane) should be replaced with the
//		// Galactic longitude of the North Celestial Pole.
//		double sinb = Math.cos(dec)*Math.cos(Galactic.NGPdec)*Math.cos(ra - Galactic.NGPra) + Math.sin(dec)*Math.sin(Galactic.NGPdec);
//		b = Math.asin(sinb);
//		double cosb = Math.cos(b);
//		double sinLoL = (1.0/cosb) * Math.cos(dec)*Math.sin(ra - Galactic.NGPra);
//		double cosLoL = (1.0/cosb) * (Math.sin(dec)*Math.cos(Galactic.NGPdec) - Math.cos(dec)*Math.sin(Galactic.NGPdec)*Math.cos(ra - Galactic.NGPra));
//		
//		// Galactic longitude of the ascending node of the Equatorial plane (incorrect quantity to use)
//		double l_OMEGA = Math.toRadians(32.93192);
//		// Galactic longitude of the north celestial pole
//		double l_NCP = Math.toRadians(122.932);
//		
//		l = l_NCP - Math.atan2(sinLoL, cosLoL);
//		
//		// 2) Transform proper motions:
//		
//		double c1 = Math.sin(Galactic.NGPdec) * Math.cos(dec) - Math.cos(Galactic.NGPdec) * Math.sin(dec) * Math.cos(ra - Galactic.NGPra);
//		double c2 = Math.cos(Galactic.NGPdec) * Math.sin(ra - Galactic.NGPra);
//		double cos_b = Math.sqrt(c1*c1 + c2*c2);
//		
//		mu_lcosb = (c1 * mu_acosd + c2 * mu_d)/cos_b;
//		mu_b = (-c2 * mu_acosd + c1 * mu_d)/cos_b;
		
		return new double[]{l, b, mu_lcosb, mu_b};
	}
	
	/**
	 * Converts position and proper motion from Galactic frame to Equatorial frame.
	 * 
	 * @param l			Galactic longitude [radians]
	 * @param b			Galactic latitude [radians]
	 * @param mu_lcosb	Angular (proper) motion parallel to Galactic equator  [radians/yr]
	 * @param mu_b		Angular (proper) motion perpendicular to Galactic equator [radians/yr]
	 * @return			Array containing components of the position [radians] and proper motion in the Equatorial frame [radians/yr]
	 */
	public static double[] convertPositionAndProperMotionGalToEq(double l, double b, double mu_lcosb, double mu_b)
	{
		return convertPositionAndProperMotion(l, b, mu_lcosb, mu_b, Galactic.r_E_G);
	}
	
	/**
	 * Get the proper motion vector; this is the 3D vector of the velocity in the plane of the sky.
	 * Note that the angular coordinates can be in any suitable frame: the comments refer to the
	 * Equatorial frame just for convenience.
	 * 
	 * Uses the following relations between the angular and cartesian coordinates:
	 * <p><ul>
	 * <li> x^{dot} = -cos(ra)*sin(dec)*mu_d - sin(ra)*mu_acosd
	 * <li> y^{dot} = -sin(ra)*sin(dec)*mu_d + cos(ra)*mu_acosd
	 * <li> z^{dot} = cos(dec)*mu_d
	 * </ul><p>
	 * @param d		Distance [pc]
	 * @param ra	Right ascension (Equatorial) [radians]
	 * @param dec	Declination (Equatorial) [radians]
	 * @param mu_acosd	Angular (proper) motion parallel to equator [radians/yr]
	 * @param mu_d	Angular (proper) motion perpendicular to equator [radians/yr]
	 * @return		A 3x1 JAMA Matrix containing components of the proper motion vector [km/s]
	 */
	public static Matrix getTangentialVelocityVector(double d, double ra, double dec, double mu_acosd, double mu_d) {
		
		// NOTE that the cos(dec) terms have been incorporated into mu_acosd.
		// These expressions are derived from e.g. dx/dt where x = cos(ra)cos(dec) etc.
		double x = -Math.sin(ra)*mu_acosd - Math.cos(ra)*Math.sin(dec)*mu_d;
		double y =  Math.cos(ra)*mu_acosd - Math.sin(ra)*Math.sin(dec)*mu_d;
		double z =  Math.cos(dec)*mu_d;
		Matrix vt = new Matrix(new double[][]{{d*x},{d*y},{d*z}});
		
		// Tangential velocity is currently in units of parsecs per year.
		// Convert to kilometres per second.
		vt.timesEquals(Units.PARSECS_PER_YEAR_TO_KILOMETRES_PER_SECOND);
		
		return vt;
	}
	
	/**
	 * Converts the tangential velocity of an object to the equivalent proper motion, viewed at the
	 * given distance.
	 * 
	 * Uses the following relations between the angular and cartesian coordinates:
	 * <p><ul>
	 * <li> x^{dot} = -cos(ra)*sin(dec)*mu_d - sin(ra)*mu_acosd
	 * <li> y^{dot} = -sin(ra)*sin(dec)*mu_d + cos(ra)*mu_acosd
	 * <li> z^{dot} = cos(dec)*mu_d
	 * </ul><p>
	 * so:
	 * <p><ul>
	 * <li> mu_d = z^{dot} / cos(dec)
	 * <li> mu_acosd = y^{dot} * cos(ra) - x^{dot} * sin(ra)
	 * </ul><p>
	 * @param d		Distance [pc]
	 * @param ra	Right ascension of the target position [radians]
	 * @param dec	Declination of the target position [radians]
	 * @param vtan	Tangential velocity vector [km/s]
	 * @return		The angular velocity (proper motion) parallel and perpendicular to the equator (i.e.
	 * 				includes the cos(dec) factor in the motion parallel to the equator) [radians/yr]
	 */
	public static double[] getProperMotions(double d, double ra, double dec, Matrix vtan) {
		
		// Retrieve the components and divide out the distance
		double x_dot = vtan.get(0, 0) / d;
		double y_dot = vtan.get(1, 0) / d;
		double z_dot = vtan.get(2, 0) / d;
		
		// Convert to parsecs per year
		x_dot *= Units.KILOMETRES_PER_SECOND_TO_PARSECS_PER_YEAR;
		y_dot *= Units.KILOMETRES_PER_SECOND_TO_PARSECS_PER_YEAR;
		z_dot *= Units.KILOMETRES_PER_SECOND_TO_PARSECS_PER_YEAR;
		
		double mu_d = z_dot / Math.cos(dec);
		double mu_acosd = y_dot * Math.cos(ra) - x_dot * Math.sin(ra);
		
		return new double[]{mu_acosd, mu_d};
	}
	
	/**
	 * The 'Normal' frame is defined for a particular line of sight, which in this 
	 * case is parameterised by the spherical coordinates of the LOS. The Normal
	 * coordinate frame is constructed by basis vectors pqr with the following 
	 * definitions:
	 *  - p is parallel to the equator, positive east
	 *  - q is perpendicular to the equator, positive north
	 *  - r points along the LOS towards the tangent point
	 *  - pq plane coincides with the tangent plane
	 *  - pqr form a right handed set
	 * 
	 * The matrix returned by this method contains the Normal frame basis vectors
	 * expressed in the input frame as it's rows. It is used to transform 3x1 column
	 * vectors from the input frame (r_E) to the Normal frame (r_N) according to:
	 * 
	 * r_N = r_N_E * r_E
	 * 
	 * @param ra	Right ascension (or Galactic longitude) in radians
	 * @param dec   Declination (or Galactic latitude) in radians
	 * @return		A JAMA Matrix containing a 3x3 orthonormal matrix that defines the
	 * 				transformation of vectors from the frame in which the right ascension
	 * 				and declination are defined, to the Normal frame at that line of sight.
	 * 				It's rows consist of the Normal frame basis vectors expressed in the
	 * 				input frame. It transforms 3x1 vectors according to r_N = r_N_E * r_E.
	 */
	public static Matrix getNormalFrame(double ra, double dec)
	{
		
		// Transformation from Equatorial -> Normal frame
		
		// Basis vectors of Normal frame, expressed in input frame (Equatorial, Galactic, ...)
		double[] p = new double[]{-Math.sin(ra), Math.cos(ra), 0};
		double[] q = new double[]{-Math.sin(dec)*Math.cos(ra),-Math.sin(dec)*Math.sin(ra), Math.cos(dec)};
		double[] r = new double[]{ Math.cos(dec)*Math.cos(ra), Math.cos(dec)*Math.sin(ra), Math.sin(dec)};
		
		// Rows consist of the basis vectors of the Normal frame, expressed in the
		// input frame.
		double[][] rne = new double[][]{p,q,r};
		
		// This Matrix transforms 3x1 column vectors FROM the input frame TO the Normal
		// frame by right-multiplication.
		Matrix r_N_E = new Matrix(rne);
		return r_N_E;
		
	}
	
	/**
	 * Get the matrix that projects velocities and other vectors onto the celestial sphere. Note
	 * that the angular coordinates can be in any frame; the labels right ascension and declination are
	 * used for convenience.
	 * 
	 * @param ra
	 * 	Angular coordinate about the equator (e.g. Right Ascension, longitude) [radians]
	 * @param dec
	 * 	Angular coordinate measured from the equator (e.g. Declination, latitude) [radians]
	 * @return
	 * 	The matrix that projects velocities and other vectors onto the celestial sphere - usually
	 * denoted with a capital A, e.g. in Dehnen & Binney (1998)
	 */
	public static Matrix getProjectionMatrixA(double ra, double dec) {
		// Unit vector pointing towards the point
		Matrix r = sphericalPolarToCartesian(1.0, ra, dec);
		return Matrix.identity(3, 3).minus(r.times(r.transpose()));
	}
	
	/**
	 * Computes the angular separation of two points specified in angular coordinates.
	 * @param RA1
	 * 	The right ascension/longitude of the first point [radians]
	 * @param dec1
	 * 	The declination/latitude of the first point [radians]
	 * @param RA2
	 * 	The right ascension/longitude of the second point [radians]
	 * @param dec2
	 * 	The declination/latitude of the second point [radians]
	 * @return
	 * 	The anglur separation of the two points [radians]
	 */
	public static double angularSeparation(double RA1, double dec1, double RA2, double dec2) {
		double sep = Math.cos(dec1)*Math.cos(dec2)*Math.cos(RA1 - RA2) + Math.sin(dec1)*Math.sin(dec2);
		return Math.acos(sep);
	}
	
	/**
	 * Utility to generate realizations of a random vector described by
	 * a mean and covariance matrix.
	 * 
	 * @param covar		NxN covariance matrix on vector elements.
	 * @param mean		Nx1 column vector containing mean of random vector.
	 * @return			Random vector drawn from input distribution.
	 */
	public static Matrix getRandomVector(Matrix covar, Matrix mean)
	{

        // How many elements to draw?
        int N = covar.getColumnDimension();     
        
        // Basic checks on matrix sizes and shapes etc.
        if(covar.getColumnDimension()!=covar.getRowDimension())
            throw new RuntimeException();
        if(mean.getColumnDimension()!=1 || mean.getRowDimension()!=N)
            throw new RuntimeException();
                
        // Get Eigenvalue decomposition of covariance matrix
        EigenvalueDecomposition eig = covar.eig();
        
        // Get dispersions along the principal axes of the covariance hyper-ellipsoid
        Matrix evals = eig.getD();
        
        // Random vector in principal axes frame
        Matrix rand = new Matrix(N,1);
        
        // Draw elements from unit Gaussian and scale according to axis sigma
        for(int d=0; d<N; d++)
            rand.set(d, 0, Math.sqrt(evals.get(d, d))*random.nextGaussian());
        
        // Transform this back to original frame using eigenvectors
        // of covariance matrix
        rand = eig.getV().times(rand);    
                
        // Add random error onto mean and return
        return mean.plus(rand);
        
	}
	
	/**
	 * Compute and return a random coordinate distributed uniformly on the celestial sphere.
	 * @return
	 * 	Array of two doubles containing the angular coordinates of the point [radians].
	 */
	public static double[] getRandomRaDec() {
        return new double[]{getRandomRa(), getRandomDec()};
	}
	
	/**
	 * Compute a random right ascension / longitude coordinate for points uniformly
	 * distributed on the unit sphere.
	 * @return
	 * 	A random right ascension / longitude coordinate for points uniformly
	 * distributed on the unit sphere [radians]
	 */
	public static double getRandomRa() {
		return 2*Math.PI*Math.random();
	}
	
	/**
	 * Compute a random declination / latitude coordinate for points uniformly
	 * distributed on the unit sphere.
	 * @return
	 * 	A random declination / latitude coordinate for points uniformly
	 * distributed on the unit sphere [radians]
	 */
	public static double getRandomDec() {
		return Math.asin(2*Math.random() - 1);
	}
	
	/**
	 * Converts an angle in the range [0:2*PI] to the hours, minutes, seconds representation.
	 * @param angle
	 * 	The angle to convert [radians]. If it doesn't lie in the range [0:2*PI] then it is
	 * first translated to this range by adding or subtracting multiples of 2*PI.
	 * @return
	 * 	Array of doubles containing the hours [0], minutes [1] and seconds [2]. The hours
	 * and minutes are whole numbers, the seconds have numbers after the decimal place.
	 */
	public static double[] radiansToHMS(double angle) {

		angle = translateToRangeZeroToTwoPi(angle);
		
		double deg = Math.toDegrees(angle);

        double hours = Math.floor(deg/15);
        double remainder = (deg/15) - hours;
        double mins = Math.floor(remainder*60);
        remainder = (remainder*60) - mins;
        double secs = remainder*60;

        return new double[]{hours, mins, secs};
	}

	/**
	 * Converts an angle in radians to the equivalent degrees, arcminutes, arcseconds
	 * representation. The angle can be positive or negative. If it lies outside the
	 * range [-2*PI : 2*PI] then it is first translated to this range by adding or
	 * subtracting multiples of 2*PI.
	 * @param angle
	 * 	The angle to convert [radians]
	 * @return
	 * 	Array of doubles containing the degrees [0], arcminutes [1] and arcseconds [2]. The
	 * degrees and arcminutes are whole numbers, the arcseconds have numbers after the decimal place.
	 */
	public static double[] radiansToDMS(double angle) {
		
		angle = translateToRangeMinusTwoPiToTwoPi(angle);
		angle = Math.toDegrees(angle);
		
		double deg, arcmin, arcsec;
		
		if(angle > 0){
		    deg = Math.floor(angle);
		    double remainder = angle - deg;
		    arcmin = Math.floor(remainder*60.0);
		    remainder = remainder*60.0 - arcmin;
		    arcsec = remainder*60.0;
        }
        else{
            deg = Math.ceil(angle);
            double remainder = deg - angle;
            arcmin = Math.floor(remainder*60);
            remainder = remainder*60 - arcmin;
            arcsec = remainder*60;
        }
		
		return new double[]{deg, arcmin, arcsec};
	}
	
	/**
	 * Converts an angle from HH:MM:SS.s format to decimal radians.
	 * @param hour
	 * 	The hours (a whole number).
	 * @param min
	 * 	The minutes (a whole number).
	 * @param sec
	 * 	The seconds.
	 * @return
	 * 	The equivalent angle in deciman radians.
	 */
	public static double hmsToRadians(int hour, int min, double sec) {
		
		// Sanity check on inputs
		if(hour < 0) {
			throw new RuntimeException("Hours must be positive! hour = " + hour);
		}
		return Math.toRadians(hour*15.0 + (min*15.0)/60.0 + (sec*15.0)/3600.0);
	}
	
	/**
	 * Converts an angle from +/-DD:MM:SS.s format to decimal radians.
	 * @param sign
	 * 	Indicates the sign of the angle; must be +/-1. This is required as a separate
	 * argument in order to handle angles between [-1:0], which would otherwise be 
	 * impossible to distinguish from [0:1] (because the integer -0 does not exist in Java).
	 * The sign of the deg argument is ignored.
	 * @param deg
	 * 	The degrees; a whole number. Note that the sign is ignored (the absolute value
	 * is taken): the sign of the angle must be indicated using the sign argument.
	 * @param arcmin
	 * 	The arcminutes; a whole number.
	 * @param arcsec
	 * 	The arcseconds; a decimal
	 * @return
	 * 	The equivalent angle in radians.
	 */
	public static double dmsToRadians(int sign, int deg, int arcmin, double arcsec) {
		
		if(sign != -1 && sign != 1) {
			throw new RuntimeException("Expected sign of +1 or -1, found "+sign);
		}
		
		double degrees = Math.abs(deg) + (arcmin/60.0) + (arcsec/3600.0);

		return Math.toRadians(degrees * sign);
	}
	
	/**
	 * Translate an angle to the equivalent angle in the range [0:2*PI]
	 * @param angle
	 * 	The angle [radians]
	 * @return
	 * 	The equivalent angle in the range [0:2*PI]
	 */
	public static double translateToRangeZeroToTwoPi(double angle) {
		
		if(angle < 0) {
			// Add multiples of 2*pi
			double multiplesOfTwoPiToAdd = Math.floor(-angle/(2*Math.PI)) + 1;
			return angle + multiplesOfTwoPiToAdd*2*Math.PI;
		}
		else if(angle > 2*Math.PI) {
			// Subtract multiples of 2*pi
			double multiplesOfTwoPiToSubtract = Math.floor(angle/(2*Math.PI));
			return angle - multiplesOfTwoPiToSubtract*2*Math.PI;
		}
		else {
			// Angle is in the desired range
			return angle;
		}
	}

	/**
	 * Translate an angle to the equivalent angle in the range [-2*PI:2*PI]. The
	 * sign of the angle is not changed.
	 * @param angle
	 * 	The angle [radians]
	 * @return
	 * 	The equivalent angle in the range [-2*PI:2*PI]
	 */
	public static double translateToRangeMinusTwoPiToTwoPi(double angle) {
		
		if(angle < -2*Math.PI) {
			// Add multiples of 2*pi
			double multiplesOfTwoPiToAdd = Math.floor(-angle/(2*Math.PI));
			return angle + multiplesOfTwoPiToAdd*2*Math.PI;
		}
		else if(angle > 2*Math.PI) {
			// Subtract multiples of 2*pi
			double multiplesOfTwoPiToSubtract = Math.floor(angle/(2*Math.PI));
			return angle - multiplesOfTwoPiToSubtract*2*Math.PI;
		}
		else {
			// Angle is in the desired range
			return angle;
		}
	}
	
	/**
	 * Computes the stellar parallax for stars at a distance d. Note that the inverse problem,
	 * distance from parallax, is tricky and utilities are available at {@link astrometry.DistanceFromParallax}.
	 * 
	 * @param d
	 * 	The distance to the star along the line of sight [parsecs]
	 * @return
	 * 	The annual parallax [arcseconds]
	 */
	public static double getParallaxFromDistance(double d) {
		return 1.0 / d;
	}
	
	/**
	 * Takes the appropriate action if a user tries to naively invert the parallax to get the distance.
	 * @param pi
	 * 	The stellar parallax [arcseconds]
	 * @return
	 * 	The stellar distance [parsecs]
	 */
	public static double getDistanceFromParallax(double pi) {
		throw new RuntimeException("Oh no you don't - see astrometry.DistanceFromParallax for this");
	}
	
}
