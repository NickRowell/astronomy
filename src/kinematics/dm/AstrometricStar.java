/*
 * Gaia CU5 DU10
 *
 * (c) 2005-2020 Gaia Data Processing and Analysis Consortium
 *
 *
 * CU5 photometric calibration software is free software; you can redistribute
 * it and/or modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * CU5 photometric calibration software is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this CU5 software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *
 *-----------------------------------------------------------------------------
 */

package kinematics.dm;

import Jama.Matrix;

/**
 * Represents a star with astrometric measurements.
 *
 * @author nrowell
 * @version $Id$
 */
public interface AstrometricStar {
	
	/**
	 * Get the Galactic longitude [radians]
	 * 
	 * @return
	 * 	The Galactic longitude [radians]
	 */
	public double getLong();
	
	/**
	 * Get the Galactic latitude [radians]
	 * 
	 * @return
	 * 	The Galactic latitude [radians]
	 */
	public double getLat();
	
	/**
	 * Get the proper motion in Galactic longitude, including cosine factor [radians/yr]
	 * 
	 * @return
	 * 	The proper motion in Galactic longitude, including cosine factor [radians/yr]
	 */
	public double getMuLCosB();
	
	/**
	 * Get the proper motion in Galactic latitude [radians/yr]
	 * 
	 * @return
	 * 	The proper motion in Galactic latitude [radians/yr]
	 */
	public double getMuB();
	
	/**
	 * Get the distance to the star [parsecs]
	 * 
	 * @return
	 * 	The distance to the star [parsecs]
	 */
	public double getDistance();
	
	/**
	 * Get the tangential velocity vector [km/s]
	 * 
	 * @return
	 * 	The tangential velocity vector [km/s]
	 */
	public Matrix getP();

	/**
	 * Set the tangential velocity vector [km/s]
	 * 
	 * @param p The tangential velocity vector to set [km/s]
	 */
	public void setP(Matrix p);
	
	/**
	 * Get the peculiar tangential velocity vector [km/s]. This is the
	 * tangential velocity vector minus the mean.
	 * 
	 * @return
	 * 	The peculiar tangential velocity vector [km/s]
	 */
	public Matrix getPPrime();
	
	/**
	 * Set the peculiar tangential velocity vector [km/s]. This is the
	 * tangential velocity vector minus the mean.
	 * 
	 * @param pPrime The peculiar tangential velocity vector to set [km/s]
	 */
	public void setPPrime(Matrix pPrime);
	
	/**
	 * Get the 6x1 matrix containing the mixed products of the peculiar tangential velocity
	 * components. This requires that {@link #setPPrime(Matrix)} hasd previously been called
	 * to set the peculiar tangential velocity vector.
	 * @return
	 * 	The 6x1 matrix containing the mixed products of the peculiar tangential velocity
	 * components [km^2/s^2]
	 */
	public Matrix getU();
	
	/**
	 * Get the 6x6 matrix containing the mixed products of the projection matrix A.
	 * @return
	 * 	The 6x6 matrix containing the mixed products of the projection matrix A [-].
	 */
	public Matrix getB();
	
	/**
	 * Get the projection matrix.
	 * 
	 * @return
	 * 	The projection matrix.
	 */
	public Matrix getA();
	
	/**
	 * Set the projection matrix.
	 * 
	 * @param a The projection matrix to set.
	 */
	public void setA(Matrix a);

}
