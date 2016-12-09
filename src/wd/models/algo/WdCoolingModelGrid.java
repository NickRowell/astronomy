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

package wd.models.algo;

import java.util.NavigableMap;

import numeric.functions.MonotonicLinear;
import photometry.Filter;
import wd.models.infra.WdAtmosphereType;

/**
 * Class represents a single grid of WD cooling models: multiple cooling tracks of different mass,
 * which can be used to interpolate the cooling time to a particular magnitude.
 * 
 * The grid correponds to a single {@link Filter} and {@link WdAtmosphereType}. Multiple instances
 * of this are used to compile a {@link WdCoolingModelSet}.
 *
 *
 * @author nrowell
 * @version $Id$
 */
public class WdCoolingModelGrid {
	
	/**
	 * The {@link Filter}.
	 */
	protected Filter filter;
	
	/**
	 * The {@link WdAtmosphereType}.
	 */
	protected WdAtmosphereType atm;
	
	/**
	 * Map of WD cooling track (represented by a {@link MonotonicLinear}) by mass.
	 */
    public final NavigableMap<Double, MonotonicLinear> mbolAsFnTcoolByMass;
	
    /**
     * Main constructor for the {@link WdCoolingModelGrid}.
     * 
     * @param filter
     * 	The {@link Filter}.
     * @param atm
     * 	The {@link WdAtmosphereType}.
     * @param mbolAsFnTcoolByMass
     * 	The {@link NavigableMap} of WD cooling track (represented as a {@link MonotonicLinear}) by
     * mass [M_{Solar}].
     */
    public WdCoolingModelGrid(final Filter filter, final WdAtmosphereType atm,
    		final NavigableMap<Double, MonotonicLinear> mbolAsFnTcoolByMass) {
    	this.filter = filter;
    	this.atm = atm;
    	this.mbolAsFnTcoolByMass = mbolAsFnTcoolByMass;
    }
    
    /**
     * Check if desired mass is within range of models.
     * @param mass
     * 	The mass to check [M_{solar}]
     * @return
     * 	True if the mass lies within the range covered by the model grid; false otherwise.
     */
    public boolean massIsWithinRangeOfModels(double mass){ 
    	
    	// Mass of lowest mass cooling track
    	double lowestMass = mbolAsFnTcoolByMass.firstKey();
    	
    	// Mass of highest mass cooling track
    	double highestMass = mbolAsFnTcoolByMass.lastKey();
    	
        return (mass>=lowestMass && mass<=highestMass);
    }

    /**
     * Do cooling models need to be extrapolated at this age & atmosphere type?
     *
     * @param age
     * 	The WD cooling time [yr]
     * @param M mass   
     * 	WD mass [M_{solar}]
     * @return		
     * 	True if the models need to be extrapolated
     */
    public boolean isExtrapolated(double age, double mass) {
        
        // Check if mass is outside range of models
        if(!massIsWithinRangeOfModels(mass))
        {
        	return true;
        }
        
        // If either upper or lower CoolingTrack is extrapolated, then this point is considered to be extrapolated.
        return mbolAsFnTcoolByMass.floorEntry(mass).getValue().isXExtrapolated(age)||
               mbolAsFnTcoolByMass.ceilingEntry(mass).getValue().isXExtrapolated(age);
    }
    
    /**
     * Interpolate/extrapolate magnitude at arbitrary cooling time, mass and atmosphere type.
     *
     * @param age
     * 	WD cooling time [yr]
     * @param mass
     * 	WD mass [M_{solar}]
     * @return
     * 	The magitude in the requested {@link Filter}
     */
    public double mag(double age, double mass) {
    	
    	if(mbolAsFnTcoolByMass.containsKey(mass)) {
    		// Mass coincides exactly with one of the constant mass cooling tracks.
    		// No inter-mass interpolation is necessary
    		return mbolAsFnTcoolByMass.get(mass).interpolateY(age)[0];
    	}
    	
    	double lowerMass = Double.NaN;
    	double higherMass = Double.NaN;
    	MonotonicLinear lowerMassTrack = null;
    	MonotonicLinear higherMassTrack = null;
    	
    	if(massIsWithinRangeOfModels(mass)) {
    		// Linear interpolation between two neighbouring constant-mass cooling tracks
    		lowerMass = mbolAsFnTcoolByMass.floorKey(mass);
    		higherMass = mbolAsFnTcoolByMass.ceilingKey(mass);
    		// Get the cooling tracks immediately above and below this mass.
    		lowerMassTrack = mbolAsFnTcoolByMass.get(lowerMass);
    		higherMassTrack = mbolAsFnTcoolByMass.get(higherMass);
    	}
    	else {
    		// Linear extrapolation beyond extreme range of mass. We use the two constant mass
    		// tracks closest to the edge of the range to perform a linear extrapolation.
    		if(mass < mbolAsFnTcoolByMass.firstKey()) {
    			// Extrapolation to masses lower than the lowest mass track: extract the
    			// masses of the lowest two tracks
    			lowerMass = mbolAsFnTcoolByMass.firstKey();
    			higherMass = mbolAsFnTcoolByMass.higherKey(lowerMass);
    		}
    		else {
    			// Extrapolation to masses higher than the highest mass track: extract the
    			// masses of the highest two tracks
    			higherMass = mbolAsFnTcoolByMass.lastKey();
    			lowerMass = mbolAsFnTcoolByMass.lowerKey(higherMass);
    		}
    		lowerMassTrack = mbolAsFnTcoolByMass.get(lowerMass);
    		higherMassTrack = mbolAsFnTcoolByMass.get(higherMass);
    	}
    	
    	// Data points at (x0,y0) and (x1,y1) where x=mass; y=magnitude
		double x0 = lowerMass;
		double y0 = lowerMassTrack.interpolateY(age)[0];
		double x1 = higherMass;
		double y1 = higherMassTrack.interpolateY(age)[0];
		// Mass at which to interpolate the magnitude
		double x = mass;
		// Interpolated magnitude
		double y = y0 + (y1-y0)*(x-x0)/(x1-x0);
		
		return y;
    }
    
    /**
     * Interpolate/extrapolate cooling time at arbitrary magnitude, mass and atmosphere
     * type.
     *
     * @param mag
     * 	Magnitude in the requested {@link Filter}
     * @param mass 
     * 	WD mass [M_{solar}]
     * @return
     * 	Cooling time [yr] to the given magnitude in the given {@link Filter}, for the given WD mass, atmosphere type.
     */
    public double tcool(double mag, double mass) {

    	if(mbolAsFnTcoolByMass.containsKey(mass)) {
    		// Mass coincides exactly with one of the constant mass cooling tracks.
    		// No inter-mass interpolation is necessary
    		return mbolAsFnTcoolByMass.get(mass).interpolateUniqueX(mag)[0];
    	}

    	double lowerMass = Double.NaN;
    	double higherMass = Double.NaN;
    	MonotonicLinear lowerMassTrack = null;
    	MonotonicLinear higherMassTrack = null;
    	
    	if(massIsWithinRangeOfModels(mass)) {
    		// Linear interpolation between two neighbouring constant-mass cooling tracks
    		lowerMass = mbolAsFnTcoolByMass.floorKey(mass);
    		higherMass = mbolAsFnTcoolByMass.ceilingKey(mass);
    		
    		// Get the cooling tracks immediately above and below this mass.
    		lowerMassTrack = mbolAsFnTcoolByMass.get(lowerMass);
    		higherMassTrack = mbolAsFnTcoolByMass.get(higherMass);
    	}
    	else {
    		// Linear extrapolation beyond extreme range of mass. We use the two constant mass
    		// tracks closest to the edge of the range to perform a linear extrapolation.
    		
    		if(mass < mbolAsFnTcoolByMass.firstKey()) {
    			// Extrapolation to masses lower than the lowest mass track: extract the
    			// masses of the lowest two tracks
    			lowerMass = mbolAsFnTcoolByMass.firstKey();
    			higherMass = mbolAsFnTcoolByMass.higherKey(lowerMass);
    		}
    		else {
    			// Extrapolation to masses higher than the highest mass track: extract the
    			// masses of the highest two tracks
    			higherMass = mbolAsFnTcoolByMass.lastKey();
    			lowerMass = mbolAsFnTcoolByMass.lowerKey(higherMass);
    		}
    		lowerMassTrack = mbolAsFnTcoolByMass.get(lowerMass);
    		higherMassTrack = mbolAsFnTcoolByMass.get(higherMass);
    	}
    	
    	// Data points at (x0,y0) and (x1,y1) where x=mass; y=cooling time
		double x0 = lowerMass;
		double y0 = lowerMassTrack.interpolateUniqueX(mag)[0];
		double x1 = higherMass;
		double y1 = higherMassTrack.interpolateUniqueX(mag)[0];
		// Mass at which to interpolate the cooling time
		double x = mass;
		// Interpolated cooling time
		double y = y0 + (y1-y0)*(x-x0)/(x1-x0);
		
		return y;
    }
    
}