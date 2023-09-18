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

import infra.Quantity;
import numeric.functions.MonotonicLinear;
import wd.models.infra.WdAtmosphereType;

/**
 * Class represents a grid of WD cooling models for a single {@link Quantity} and {@link WdAtmosphereType},
 * which can be used to interpolate the cooling time to a particular value of the quantity. Multiple instances of this
 * are used to compile a {@link WdCoolingModelSet}.
 *
 * @author nrowell
 * @version $Id$
 */
public class WdCoolingModelGrid {
	
	/**
	 * The {@link Quantity} that is provided by the grid.
	 */
	protected Quantity<?> quantity;
	
	/**
	 * The {@link WdAtmosphereType}.
	 */
	protected WdAtmosphereType atm;
	
	/**
	 * Map of WD cooling track (represented by a {@link MonotonicLinear}) by mass.
	 */
    public final NavigableMap<Double, MonotonicLinear> quantityAsFnTcoolByMass;
	
    /**
     * Main constructor for the {@link WdCoolingModelGrid}.
     * 
     * @param quantity
     * 	The {@link Quantity}.
     * @param atm
     * 	The {@link WdAtmosphereType}.
     * @param quantityAsFnTcoolByMass
     * 	The {@link NavigableMap} of WD cooling track (represented as a {@link MonotonicLinear}) by
     * mass [M_{Solar}].
     */
    public WdCoolingModelGrid(final Quantity<?> quantity, final WdAtmosphereType atm,
    		final NavigableMap<Double, MonotonicLinear> quantityAsFnTcoolByMass) {
    	this.quantity = quantity;
    	this.atm = atm;
    	this.quantityAsFnTcoolByMass = quantityAsFnTcoolByMass;
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
    	double lowestMass = quantityAsFnTcoolByMass.firstKey();
    	
    	// Mass of highest mass cooling track
    	double highestMass = quantityAsFnTcoolByMass.lastKey();
    	
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
        return quantityAsFnTcoolByMass.floorEntry(mass).getValue().isXExtrapolated(age)||
               quantityAsFnTcoolByMass.ceilingEntry(mass).getValue().isXExtrapolated(age);
    }
    
    /**
     * Interpolate/extrapolate the {@link Quantity} at arbitrary cooling time, mass and atmosphere type.
     *
     * @param tcool
     * 	WD cooling time [yr]
     * @param mass
     * 	WD mass [M_{solar}]
     * @return
     * 	The value of the {@link Quantity} at the given cooling time and mass.
     */
    public double quantity(double tcool, double mass) {
    	
    	if(quantityAsFnTcoolByMass.containsKey(mass)) {
    		// Mass coincides exactly with one of the constant mass cooling tracks.
    		// No inter-mass interpolation is necessary
    		return quantityAsFnTcoolByMass.get(mass).interpolateY(tcool)[0];
    	}
    	
    	double lowerMass = Double.NaN;
    	double higherMass = Double.NaN;
    	MonotonicLinear lowerMassTrack = null;
    	MonotonicLinear higherMassTrack = null;
    	
    	if(massIsWithinRangeOfModels(mass)) {
    		// Linear interpolation between two neighbouring constant-mass cooling tracks
    		lowerMass = quantityAsFnTcoolByMass.floorKey(mass);
    		higherMass = quantityAsFnTcoolByMass.ceilingKey(mass);
    		// Get the cooling tracks immediately above and below this mass.
    		lowerMassTrack = quantityAsFnTcoolByMass.get(lowerMass);
    		higherMassTrack = quantityAsFnTcoolByMass.get(higherMass);
    	}
    	else {
    		// Linear extrapolation beyond extreme range of mass. We use the two constant mass
    		// tracks closest to the edge of the range to perform a linear extrapolation.
    		if(mass < quantityAsFnTcoolByMass.firstKey()) {
    			// Extrapolation to masses lower than the lowest mass track: extract the
    			// masses of the lowest two tracks
    			lowerMass = quantityAsFnTcoolByMass.firstKey();
    			higherMass = quantityAsFnTcoolByMass.higherKey(lowerMass);
    		}
    		else {
    			// Extrapolation to masses higher than the highest mass track: extract the
    			// masses of the highest two tracks
    			higherMass = quantityAsFnTcoolByMass.lastKey();
    			lowerMass = quantityAsFnTcoolByMass.lowerKey(higherMass);
    		}
    		lowerMassTrack = quantityAsFnTcoolByMass.get(lowerMass);
    		higherMassTrack = quantityAsFnTcoolByMass.get(higherMass);
    	}
    	
    	// Data points at (x0,y0) and (x1,y1) where x=mass; y=magnitude
		double x0 = lowerMass;
		double y0 = lowerMassTrack.interpolateY(tcool)[0];
		double x1 = higherMass;
		double y1 = higherMassTrack.interpolateY(tcool)[0];
		// Mass at which to interpolate the magnitude
		double x = mass;
		// Interpolated magnitude
		double y = y0 + (y1-y0)*(x-x0)/(x1-x0);
		
		return y;
    }
    
    /**
     * Interpolate/extrapolate cooling time at arbitrary value of the {@link Quantity}, mass and atmosphere
     * type.
     *
     * @param value
     * 	Value of the {@link Quantity} at which the cooling time if to be interpolated.
     * @param mass 
     * 	WD mass [M_{solar}]
     * @return
     * 	Cooling time [yr] to the given value of the {@link Quantity}, for the given WD mass, atmosphere type.
     */
    public double tcool(double value, double mass) {

    	if(quantityAsFnTcoolByMass.containsKey(mass)) {
    		// Mass coincides exactly with one of the constant mass cooling tracks.
    		// No inter-mass interpolation is necessary
    		return quantityAsFnTcoolByMass.get(mass).interpolateUniqueX(value)[0];
    	}

    	double lowerMass = Double.NaN;
    	double higherMass = Double.NaN;
    	MonotonicLinear lowerMassTrack = null;
    	MonotonicLinear higherMassTrack = null;
    	
    	if(massIsWithinRangeOfModels(mass)) {
    		// Linear interpolation between two neighbouring constant-mass cooling tracks
    		lowerMass = quantityAsFnTcoolByMass.floorKey(mass);
    		higherMass = quantityAsFnTcoolByMass.ceilingKey(mass);
    		
    		// Get the cooling tracks immediately above and below this mass.
    		lowerMassTrack = quantityAsFnTcoolByMass.get(lowerMass);
    		higherMassTrack = quantityAsFnTcoolByMass.get(higherMass);
    	}
    	else {
    		// Linear extrapolation beyond extreme range of mass. We use the two constant mass
    		// tracks closest to the edge of the range to perform a linear extrapolation.
    		
    		if(mass < quantityAsFnTcoolByMass.firstKey()) {
    			// Extrapolation to masses lower than the lowest mass track: extract the
    			// masses of the lowest two tracks
    			lowerMass = quantityAsFnTcoolByMass.firstKey();
    			higherMass = quantityAsFnTcoolByMass.higherKey(lowerMass);
    		}
    		else {
    			// Extrapolation to masses higher than the highest mass track: extract the
    			// masses of the highest two tracks
    			higherMass = quantityAsFnTcoolByMass.lastKey();
    			lowerMass = quantityAsFnTcoolByMass.lowerKey(higherMass);
    		}
    		lowerMassTrack = quantityAsFnTcoolByMass.get(lowerMass);
    		higherMassTrack = quantityAsFnTcoolByMass.get(higherMass);
    	}
    	
    	// Data points at (x0,y0) and (x1,y1) where x=mass; y=cooling time
		double x0 = lowerMass;
		double y0 = lowerMassTrack.interpolateUniqueX(value)[0];
		double x1 = higherMass;
		double y1 = higherMassTrack.interpolateUniqueX(value)[0];
		// Mass at which to interpolate the cooling time
		double x = mass;
		// Interpolated cooling time
		double y = y0 + (y1-y0)*(x-x0)/(x1-x0);
		
		return y;
    }
    
}