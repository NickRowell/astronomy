package ms.lifetime.algo;

import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.logging.Logger;

import numeric.functions.MonotonicFunction1D;

/**
 * Base class for implementations of {@link PreWdLifetime} that are based on tabulated models of
 * stellar evolution.
 * 
 * Bilinear interpolation of mass/lifetime within the grid of different metallicity models is performed.
 *
 * NOTE: the mapping and bilinear interpolation of the models is hardwired for a grid of models at regular intervals
 * in metallicity (Z,Y) space. This class will therefore give poor results if model sets at irregular
 * coordinates in (Z,Y) are used. An improved technique for handling these cases would be to perform
 * Delaunay Triangulation of the grid points, so that for any arbitrary coordinate (z,y) we can find the
 * three closest models, which can then be used to perform linear interpolation by fitting a plane through
 * them.
 * 
 * @author nrowell
 * @version $Id$
 */
public abstract class PreWdLifetimeTabulated implements PreWdLifetime {

	/**
	 * The {@link Logger}.
	 */
	protected static final Logger logger = Logger.getLogger(PreWdLifetimeTabulated.class.getName());
	
	/**
	 * Mapping of the {@link MonotonicFunction1D} of pre-WD lifetime as a function of mass,
	 * by metallicity. The outer key is Z, the inner key is Y.
	 */
	public NavigableMap<Double, NavigableMap<Double, MonotonicFunction1D>> lifetimeAsFnMassByMetallicity;
	
	/**
	 * Main constructor.
	 */
	public PreWdLifetimeTabulated() {
		
		lifetimeAsFnMassByMetallicity = new TreeMap<Double, NavigableMap<Double, MonotonicFunction1D>>();
		
		// Populate the map
		load();
	}
	
	/**
	 * Extending classes must implement the {@link PreWdLifetimeTabulated#load()} method to populate the
	 * map on construction.
	 */
	protected abstract void load();
	
	/**
	 * Determines if the given value for the metallicity (Z) is within the range covered
	 * by the models set or not.
	 * @param z
	 * 	The Z value.
	 * @return
	 * 	True if this value lies within the range covered by the model set. False otherwise.
	 */
	private boolean zIsWithinRangeOfModels(double z) {

    	// Z of lowest Z model
    	double lowestZ = lifetimeAsFnMassByMetallicity.firstKey();
    	
    	// Z of highest Z model
    	double highestZ = lifetimeAsFnMassByMetallicity.lastKey();
    	
        return (z>lowestZ && z<highestZ);
	}
	
	/**
	 * Determines if the given value for the helium content (Y) is within the range covered
	 * by the given models set or not.
	 * @param y
	 * 	The Y value.
	 * @param yModels
	 * 	The Map of {@link MonotonicFunction1D} by Y value that constitutes the model set.
	 * @return
	 * 	True if this value lies within the range covered by the model set. False otherwise.
	 */
	private boolean yIsWithinRangeOfModels(double y, NavigableMap<Double, MonotonicFunction1D> yModels) {

    	// Y of lowest Y model
    	double lowestY = yModels.firstKey();
    	
    	// Y of highest Y model
    	double highestY = yModels.lastKey();
    	
        return (y>lowestY && y<highestY);
	}
	
	/**
	 * Get the total pre-WD lifetime for the given metallicity and stellar mass, and the first derivative
	 * with respect to the mass.
	 * @param z
	 * 	The metallicity Z.
	 * @param y
	 * 	The helium content Y.
	 * @param mass
	 * 	The stellar mass [M_{Solar}]
	 * @return
	 * 	The pre-WD lifetime [yr] for the given metallicity and stellar mass, and the first derivative
	 * with respect to the mass.
	 */
	public double[] getPreWdLifetime(double z, double y, double mass) {
		
		// Retrieve the two Z model sets to be used to interpolate/extrapolate the lifetime
		Entry<Double, NavigableMap<Double, MonotonicFunction1D>> zLower = getZlower(z);
		Entry<Double, NavigableMap<Double, MonotonicFunction1D>> zUpper = getZupper(z);
		
		// Now retrieve the two surrounding Y values for each constant-Z set of models
		Entry<Double, MonotonicFunction1D> yLowerZLower = getYlower(y, zLower);
		Entry<Double, MonotonicFunction1D> yUpperZLower = getYupper(y, zLower);
		Entry<Double, MonotonicFunction1D> yLowerZUpper = getYlower(y, zUpper);
		Entry<Double, MonotonicFunction1D> yUpperZUpper = getYupper(y, zUpper);
		
		// Get the (Z,Y) coordinates of each lifetime(mass) sequence
		double[] zy_00 = {zLower.getKey(), yLowerZLower.getKey()};
		double[] zy_01 = {zLower.getKey(), yUpperZLower.getKey()};
		double[] zy_10 = {zUpper.getKey(), yLowerZUpper.getKey()};
		double[] zy_11 = {zUpper.getKey(), yUpperZUpper.getKey()};
		
		// Extract the lifetimes at the given mass, for each sequence
		double[] f_00 = yLowerZLower.getValue().getY(mass);
		double[] f_01 = yUpperZLower.getValue().getY(mass);
		double[] f_10 = yLowerZUpper.getValue().getY(mass);
		double[] f_11 = yUpperZUpper.getValue().getY(mass);
		
		// Interpolate lifetime at (z0,y), and first derivative wrt mass
		double[] f_z0y = f_00;
		if((zy_01[1] - zy_00[1])!=0.0) {
			double s = (y - zy_00[1]) / (zy_01[1] - zy_00[1]);
			f_z0y[0] = f_00[0] + s * (f_01[0] - f_00[0]);
			f_z0y[1] = f_00[1] + s * (f_01[1] - f_00[1]);
		}
		
		// Interpolate lifetime at (z1,y)
		double[] f_z1y = f_10;
		if((zy_11[1] - zy_10[1])!=0.0) {
			double s = (y - zy_10[1]) / (zy_11[1] - zy_10[1]);
			f_z1y[0] = f_10[0] + s * (f_11[0] - f_10[0]);
			f_z1y[1] = f_10[1] + s * (f_11[1] - f_10[1]);
		}
		
		// Interpolate the lifetime at (z,y)
		double[] f_zy = f_z0y;
		if((zy_10[0] - zy_00[0])!=0.0) {
			double s = (z - zy_00[0]) / (zy_10[0] - zy_00[0]);
			f_zy[0] = f_z0y[0] + s * (f_z1y[0] - f_z0y[0]);
			f_zy[1] = f_z0y[1] + s * (f_z1y[1] - f_z0y[1]);
		}
		
		return f_zy;
	}
	
	/**
	 * Get the stellar mass for the given metallicity and pre-WD lifetime, and the first derivative
	 * with respect to the pre-WD lifetime.
	 * @param z
	 * 	The metallicity.
	 * @param y
	 * 	The helium content Y.
	 * @param lifetime
	 * 	The pre-WD lifetime [yr]
	 * @return
	 * 	The stellar mass for the given metallicity and pre-WD lifetime [M_{Solar}], and the first
	 * derivative with respect to the pre-WD lifetime.
	 */
	public double[] getStellarMass(double z, double y, double lifetime) {
		
		////////////////////////////////////////////////////////////////////////////
		//                                                                        //
		//                    By direct linear interpolation                      //
		//                                                                        //
		// NOTE: produces a value inconsistent with getPreWdLifetime(z, y, mass)  //
		// We then compute a correction to the mass to restore consistency.       //
		//                                                                        //
		////////////////////////////////////////////////////////////////////////////
		
		// Retreive the two Z model sets to be used to interpolate/extrapolate the lifetime
		Entry<Double, NavigableMap<Double, MonotonicFunction1D>> zLower = getZlower(z);
		Entry<Double, NavigableMap<Double, MonotonicFunction1D>> zUpper = getZupper(z);
		
		// Now retrieve the two surrounding Y values for each constant-Z set of models
		Entry<Double, MonotonicFunction1D> yLowerZLower = getYlower(y, zLower);
		Entry<Double, MonotonicFunction1D> yUpperZLower = getYupper(y, zLower);
		Entry<Double, MonotonicFunction1D> yLowerZUpper = getYlower(y, zUpper);
		Entry<Double, MonotonicFunction1D> yUpperZUpper = getYupper(y, zUpper);
		
		// Get the (Z,Y) coordinates of each lifetime(mass) sequence
		double[] zy_00 = {zLower.getKey(), yLowerZLower.getKey()};
		double[] zy_01 = {zLower.getKey(), yUpperZLower.getKey()};
		double[] zy_10 = {zUpper.getKey(), yLowerZUpper.getKey()};
		double[] zy_11 = {zUpper.getKey(), yUpperZUpper.getKey()};
		
		// Extract the mass at the given lifetime, for each sequence
		double[] f_00 = yLowerZLower.getValue().getX(lifetime);
		double[] f_01 = yUpperZLower.getValue().getX(lifetime);
		double[] f_10 = yLowerZUpper.getValue().getX(lifetime);
		double[] f_11 = yUpperZUpper.getValue().getX(lifetime);
		
		// Interpolate mass at (z0,y)
		double[] f_z0y = f_00;
		if((zy_01[1] - zy_00[1])!=0.0) {
			double s = (y - zy_00[1]) / (zy_01[1] - zy_00[1]);
			f_z0y[0] = f_00[0] + s * (f_01[0] - f_00[0]);
			f_z0y[1] = f_00[1] + s * (f_01[1] - f_00[1]);
		}
		// Interpolate mass at (z1,y)
		double[] f_z1y = f_10;
		if((zy_11[1] - zy_10[1])!=0.0) {
			double s = (y - zy_10[1]) / (zy_11[1] - zy_10[1]);
			f_z1y[0] = f_10[0] + s * (f_11[0] - f_10[0]);
			f_z1y[1] = f_10[1] + s * (f_11[1] - f_10[1]);
		}
		
		// Interpolate the mass at (z,y)
		double[] f_zy = f_z0y;
		if((zy_10[0] - zy_00[0])!=0.0) {
			double s = (z - zy_00[0]) / (zy_10[0] - zy_00[0]);
			f_zy[0] = f_z0y[0] + s * (f_z1y[0] - f_z0y[0]);
			f_zy[1] = f_z0y[1] + s * (f_z1y[1] - f_z0y[1]);
		}
		
		
		////////////////////////////////////////////////////////////
		//                                                        //
		//                   Correction step                      //
		//                                                        //
		////////////////////////////////////////////////////////////
		
		double[] lPrime = getPreWdLifetime(z, y, f_zy[0]);
		
		// Corrected mass
		f_zy[0] = f_zy[0] - ((lPrime[0] - lifetime) / lPrime[1]);
		
		return f_zy;
	}
	
	/**
	 * For the given value of Z, retrieve the {@link Entry<Double, NavigableMap<Double, MonotonicFunction1D>>}
	 * corresponding to the lower Z key for interpolation.
	 * 
	 * @param z
	 * 	The value of the mean metallicity Z at which the interpolation is to be performed.
	 * @return
	 * 	The {@link Entry<Double, NavigableMap<Double, MonotonicFunction1D>>} containing the set of models
	 * that lie at the lower Z value for use in the interpolation.
	 */
	private Entry<Double, NavigableMap<Double, MonotonicFunction1D>> getZlower(double z) {
		
		// Retrieve the two Z model sets to be used to interpolate/extrapolate the lifetime
		Entry<Double, NavigableMap<Double, MonotonicFunction1D>> zLower = null;
		Entry<Double, NavigableMap<Double, MonotonicFunction1D>> zUpper = null;
		
		if(zIsWithinRangeOfModels(z)) {
			// We're interpolating wrt Z
			zLower = lifetimeAsFnMassByMetallicity.lowerEntry(z);
			zUpper = lifetimeAsFnMassByMetallicity.higherEntry(z);
		}
		else {
			// We're extrapolating wrt Z
			if(z <= lifetimeAsFnMassByMetallicity.firstKey()) {
				// Z is below the lowest value available: get the lowest two models
				zLower = lifetimeAsFnMassByMetallicity.firstEntry();
				zUpper = lifetimeAsFnMassByMetallicity.higherEntry(lifetimeAsFnMassByMetallicity.firstKey());
				if(zUpper==null) {
					// There's only ONE Z value available in the model set: set both extracted sets equal
					// to this to enable extrapolation (which will result in constant value).
					zUpper = zLower;
				}
			}
			else{
				// Z is above the highest value available: get the highest two models
				zLower = lifetimeAsFnMassByMetallicity.lowerEntry(lifetimeAsFnMassByMetallicity.lastKey());
				zUpper = lifetimeAsFnMassByMetallicity.lastEntry();
				if(zLower==null) {
					// See comment above
					zLower = zUpper;
				}
			}
		}
		
		return zLower;
	}
	
	/**
	 * For the given value of Z, retrieve the {@link Entry<Double, NavigableMap<Double, MonotonicFunction1D>>}
	 * corresponding to the upper Z key for interpolation.
	 * 
	 * @param z
	 * 	The value of the mean metallicity Z at which the interpolation is to be performed.
	 * @return
	 * 	The {@link Entry<Double, NavigableMap<Double, MonotonicFunction1D>>} containing the set of models
	 * that lie at the upper Z value for use in the interpolation.
	 */
	private Entry<Double, NavigableMap<Double, MonotonicFunction1D>> getZupper(double z) {
		
		// Retrieve the two Z model sets to be used to interpolate/extrapolate the lifetime
		Entry<Double, NavigableMap<Double, MonotonicFunction1D>> zLower = null;
		Entry<Double, NavigableMap<Double, MonotonicFunction1D>> zUpper = null;
		
		if(zIsWithinRangeOfModels(z)) {
			// We're interpolating wrt Z
			zLower = lifetimeAsFnMassByMetallicity.lowerEntry(z);
			zUpper = lifetimeAsFnMassByMetallicity.higherEntry(z);
		}
		else {
			// We're extrapolating wrt Z
			if(z <= lifetimeAsFnMassByMetallicity.firstKey()) {
				// Z is below the lowest value available: get the lowest two models
				zLower = lifetimeAsFnMassByMetallicity.firstEntry();
				zUpper = lifetimeAsFnMassByMetallicity.higherEntry(lifetimeAsFnMassByMetallicity.firstKey());
				if(zUpper==null) {
					// There's only ONE Z value available in the model set: set both extracted sets equal
					// to this to enable extrapolation (which will result in constant value).
					zUpper = zLower;
				}
			}
			else{
				// Z is above the highest value available: get the highest two models
				zLower = lifetimeAsFnMassByMetallicity.lowerEntry(lifetimeAsFnMassByMetallicity.lastKey());
				zUpper = lifetimeAsFnMassByMetallicity.lastEntry();
				if(zLower==null) {
					// See comment above
					zLower = zUpper;
				}
			}
		}
		
		return zUpper;
	}
	
	/**
	 * For the given Y value and set of constant-Z models, retrieve the {@link Entry<Double, MonotonicFunction1D>} that
	 * is suitable for forming the lower Y value for use in the interpolation.
	 * 
	 * @param y
	 * 	The value of the mean metallicity parameter Y
	 * @param map
	 * 	The set of constant Z models
	 * @return
	 * 	The {@link Entry<Double, MonotonicFunction1D>} that is suitable for forming the lower Y value for
	 * use in the interpolation.
	 */
	private Entry<Double, MonotonicFunction1D> getYlower(double y, Entry<Double, NavigableMap<Double, MonotonicFunction1D>> map) {
		
		Entry<Double, MonotonicFunction1D> yLower = null;
		Entry<Double, MonotonicFunction1D> yUpper = null;

		if(yIsWithinRangeOfModels(y, map.getValue())) {
			// Interpolation wrt Y at the lower Z value
			yLower = map.getValue().lowerEntry(y);
			yUpper = map.getValue().higherEntry(y);
		}
		else {
			// Extrapolation wrt Y at the lower Z value
			if(y <= map.getValue().firstKey()) {
				// Y is below the lowest value available: get the lowest two models
				yLower = map.getValue().firstEntry();
				yUpper = map.getValue().higherEntry(map.getValue().firstKey());
				if(yUpper == null) {
					// See comment above
					yUpper = yLower;
				}
			}
			else {
				// Y is above the highest value available: get the highest two models
				yLower = map.getValue().lowerEntry(map.getValue().lastKey());
				yUpper = map.getValue().lastEntry();
				if(yLower == null) {
					// See comment above
					yLower = yUpper;
				}
			}
		}
		
		return yLower;
	}
	
	/**
	 * For the given Y value and set of constant-Z models, retrieve the {@link Entry<Double, MonotonicFunction1D>} that
	 * is suitable for forming the upper Y value for use in the interpolation.
	 * 
	 * @param y
	 * 	The value of the mean metallicity parameter Y
	 * @param map
	 * 	The set of constant Z models
	 * @return
	 * 	The {@link Entry<Double, MonotonicFunction1D>} that is suitable for forming the upper Y value for
	 * use in the interpolation.
	 */
	private Entry<Double, MonotonicFunction1D> getYupper(double y, Entry<Double, NavigableMap<Double, MonotonicFunction1D>> map) {
		
		Entry<Double, MonotonicFunction1D> yLower = null;
		Entry<Double, MonotonicFunction1D> yUpper = null;

		if(yIsWithinRangeOfModels(y, map.getValue())) {
			// Interpolation wrt Y at the lower Z value
			yLower = map.getValue().lowerEntry(y);
			yUpper = map.getValue().higherEntry(y);
		}
		else {
			// Extrapolation wrt Y at the lower Z value
			if(y <= map.getValue().firstKey()) {
				// Y is below the lowest value available: get the lowest two models
				yLower = map.getValue().firstEntry();
				yUpper = map.getValue().higherEntry(map.getValue().firstKey());
				if(yUpper == null) {
					// See comment above
					yUpper = yLower;
				}
			}
			else {
				// Y is above the highest value available: get the highest two models
				yLower = map.getValue().lowerEntry(map.getValue().lastKey());
				yUpper = map.getValue().lastEntry();
				if(yLower == null) {
					// See comment above
					yLower = yUpper;
				}
			}
		}
		
		return yUpper;
	}
	
}