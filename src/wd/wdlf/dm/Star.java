package wd.wdlf.dm;

import java.util.Random;

import sfr.algo.BaseSfr;
import utils.MagnitudeUtils;
import wd.models.infra.WdAtmosphereType;

/**
 * Class represents a simulation star and encapsulates all fields relevant to both modelling and
 * inversion of the White Dwarf Luminosity Function using Monte Carlo methods.
 *
 * @author nrowell
 * @version $Id$
 */
public class Star {
	
    /**
     * Instance used to assign stochastic fields of a new {@link Star}
     */
	private static final Random random = new Random(System.currentTimeMillis());
	
    // Parameters of main sequence progenitor
    
    /** 
     * Total stellar age (time since formation) [yr]
     */
    double totalAge;
    
    /** 
     * Mass of main sequence progenitor star [M_{Solar}]
     */
    double progenitorMass;
    
    /**
     * Metallicity (Z).
     */
    double z;
    
    /**
     * Helium content (Y).
     */
    double y;
    
    /** 
     * Total pre-WD lifetime of progenitor (MS + HB) [yr]
     */
    double preWdLifetime;
    
    // Parameters of white dwarf
    
    /** 
     * Mass of resulting WD star [M_{Solar}]
     */
    double whiteDwarfMass;
    
    /** 
     * Atmosphere type of resulting WD (H/He)
     */
    WdAtmosphereType wdAtmType;
    
    /** 
     * Present day magnitude of white dwarf. 
     */
    double mag;
    
    /** 
     * Variable is true if WD cooling models need to be extrapolated to
     * find this stars bolometric magnitude at the present day. 
     */
    boolean extrap;
    
    // Simulation stuff
    
    /** 
     * Number of real stars represented by this simulation object. 
     */
    double number = 1.0;
    
    /** 
     * Variance on the number of real stars represented by this simulation object.
     */
    double sigma2_number = 1.0;

    /** 
     * Does star fall in a WDLF data bin?. 
     */
    boolean isObserved;
    
    /**
     * Set the fields that are assignable from the {@link WdlfModellingParameters} alone.
     * Basically everything except the formation time.
     * @param params
     * 	The {@link WdlfModellingParameters}.
     */
    public Star(WdlfModellingParameters params) {
    	
		// Draw MS mass for star
		this.progenitorMass = params.getIMF().drawMass();
		
		// Draw metallicity for the star. Must avoid assigning non-positive values in cases where the sigma is large.
		double z = -Double.MAX_VALUE;
		double y = -Double.MAX_VALUE;
		while(z<=0.0) {
			// Redraw until we get a positive value
			z = params.getMeanMetallicity() + random.nextGaussian() * params.getMetallicitySigma();
		}
		while(y<=0.0) {
			// Redraw until we get a positive value
			y = params.getMeanHeliumContent() + random.nextGaussian() * params.getHeliumContentSigma();
		}
		this.z = z;
		this.y = y;
		
		// Get total pre-WD lifetime for star
		this.preWdLifetime = params.getPreWdLifetime().getPreWdLifetime(z, y, progenitorMass)[0];
		
		// Get final WD mass
		this.whiteDwarfMass = params.getIFMR().getMf(progenitorMass);
		
		// Assign WD atmosphere type randomly
		this.wdAtmType = (random.nextDouble() < params.getW_H()) ? WdAtmosphereType.H : WdAtmosphereType.He;
    }
    
    /**
     * Construct a Star where the total age is drawn from the SFR.
     * 
     * @param sfr
     * 	The {@link BaseSfr}
     * @param params
     * 	The WdlfModellingParameters
     */
    public Star(BaseSfr sfr, WdlfModellingParameters params) {
    	
    	this(params);
    	
		// Draw random lookback time of creation of star
		this.totalAge = sfr.drawCreationTime();
    }
    
    /**
     * Constructor for Star where formation time is drawn uniformly between specified limits.
     * 
     * @param t_min
     * 	Minimum lookback time [yr].
     * @param t_max
     * 	Maximum lookback time [yr].
     * @param params
     * 	The {@link WdlfModellingParameters}.
     */
    public Star(double t_min, double t_max, WdlfModellingParameters params) {
    	
    	this(params);
    	
        // Draw random lookback time of creation of star uniformly between specified limits
		this.totalAge = t_min + random.nextDouble()*(t_max-t_min);
    }  
    
    /**
     * Set lookback time at which this star formed. 
     * @param totalAge
     * 	The lookback time at which this star formed [yr].
     */
    public final void setTotalAge(double totalAge) {
    	this.totalAge = totalAge;
    }
    
    /**
     * Get lookback time at which this star formed. 
     * @return
     * 	The lookback time at which this star formed. 
     */
    public double getTotalAge() {
    	return totalAge;
    }
    
    /**
     * Set mass of main sequence progenitor star.
     * @param progenitorMass
     * 	Mass of main sequence progenitor star [M_{Solar}]
     */
    public final void setProgenitorMass(double progenitorMass) {
    	this.progenitorMass = progenitorMass;
    }
    
    /**
     * Get mass of main sequence progenitor star. 
     * @return
     * 	Mass of main sequence progenitor star [M_{Solar}]
     */
    public double getProgenitorMass() {
    	return progenitorMass;
    }
    
    /**
     * Set total pre-WD lifetime of progenitor (MS + HB).
     * @param preWdLifetime
     * 	The total pre-WD lifetime of progenitor (MS + HB) [yr]
     */
    public final void setPreWdLifetime(double preWdLifetime) {
    	this.preWdLifetime = preWdLifetime;
    }
    
    /**
     * Get total pre-WD lifetime of progenitor (MS + HB). 
     * @return
     * 	The total pre-WD lifetime of progenitor (MS + HB) [yr]
     */
    public double getPreWdLifetime() {
    	return preWdLifetime;
    }
    
    /**
     * Set mass of White Dwarf. 
     * @param whiteDwarfMass
     * 	Mass of the White Dwarf [M_{Solar}]
     */
    public final void setWhiteDwarfMass(double whiteDwarfMass) {
    	this.whiteDwarfMass = whiteDwarfMass;
    }

    /**
     * Get mass of White Dwarf. 
     * @return
     * 	Mass of the White Dwarf [M_{Solar}]
     */
    public double getWhiteDwarfMass() {
    	return whiteDwarfMass;
    }
    
    /**
     * Set the {@link WdAtmosphereType}
     * @param wdAtmType
     * 	The {@link WdAtmosphereType} to set.
     */
    public final void setWhiteDwarfAtmph(WdAtmosphereType wdAtmType) {
    	this.wdAtmType = wdAtmType;
    }

    /**
     * Get the {@link WdAtmosphereType}
     * @return
     * 	The {@link WdAtmosphereType}.
     */
    public WdAtmosphereType getWhiteDwarfAtmph() {
    	return wdAtmType;
    }
    
    // Quantities that depend on when WD is observed
    
    /**
     * Set present day magnitude of white dwarf.
     * @param m
     * 	The present day magnitude of white dwarf to set.
     */
    public void setMag(double mag) {
    	this.mag = mag;
    }
    
    /**
     * Get present day magnitude of white dwarf. 
     * @return
     * 	The present day magnitude of white dwarf. 
     */
    public double getMag() {
    	return mag;
    }
    
    /** 
     * Set stars extrapolation status variable. 
     */
    /**
     * Set stars extrapolation status variable. 
     * @param _extrap
     * 	The extrapolation status (true=is extrapolated)
     */
    public void setExtrap(boolean extrap) {
    	this.extrap = extrap;
    }
    
    /**
     * Get stars extrapolation status.
     * @return
     * 	The extrapolation status (true=is extrapolated)
     */
    public boolean getExtrap() {
    	return extrap;
    }
    
    /**
     * Set observed/not observed boolean flag.
     * @param isObserved
     * 	The observed status to set, i.e. true means the simulation star lies within the range of
     * the observed WDLF.
     */
    public void setIsObserved(boolean isObserved) {
    	this.isObserved = isObserved;
    }
    
    /**
     * Get observed/not observed boolean flag. 
     * @return
     * 	The observed status, i.e. true means the simulation star lies within the range of
     * the observed WDLF.
     */
    public boolean getIsObserved() {
    	return isObserved;
    }
    
    /**
     * Get the number of real stars represented by this simulation object. 
     * @return
     * 	The number of real stars represented by this simulation object. 
     */
    public double getNumber() {
    	return number;
    }
    
    /**
     * Get the uncertainty (variance) on the number of real stars
     * represented by this simulation object. 
     * @return
     * 	The uncertainty (variance) on the number of real stars
     * represented by this simulation object. 
     */
    public double getSigma2Number() {
    	return sigma2_number;
    }
    
    /**
     * Get hydrogen atmosphere status.
     * 
     * @return
     * 	True if this {@link Star} has a {@link WdAtmosphereType.H} atmosphere
     * in the white dwarf phase.
     */
    public boolean isHydrogen() {
    	return wdAtmType == WdAtmosphereType.H;
    }
    
    /**
     * Get helium atmosphere status.
     * 
     * @return
     * 	True if this {@link Star} has a {@link WdAtmosphereType.He} atmosphere
     * in the white dwarf phase.
     */
    public boolean isHelium() {
    	return wdAtmType == WdAtmosphereType.He;
    }
    
    /**
     * Updates the number of real stars represented by this simulation star
     * by applying a scale factor and associated uncertainty.
     * 
     * @param w
     * 	Scale factor to apply to the number of real stars represented by this
     * simulated star.
     * @param sigma_w
     * 	Uncertainty (standard deviation) on the scale factor.
     */
    public void reweight(double w, double sigma_w) {
        
        // Standard deviation of new number by propagation of variance
        sigma2_number = number*number*sigma_w*sigma_w + w*w*sigma2_number;
        
        // New number
        number = w * number;
    }
    
    /**
     * Apply additive constant to the variance on the number of real stars
     * represented by this simulated star. This is intended to incorporate
     * observational error in the number of stars in some e.g. magnitude
     * range.
     * 
     * @param obs_error
     * 	Variance on the observed star numbers; added in quadrature with the
     * existing uncertainty.
     */
    public void addObservationalUncertainty(double obs_error) {
        sigma2_number = sigma2_number + obs_error;
    }
    
    /**
     * Get maximum survey distance for star, given current absolute magnitude and
     * the apparent magnitude limit.
     * @param m
     * 	Apparent magnitude limit.
     * @return
     * 	The maximum survey distance for star of absolute magnitude M [pc].
     */
    public double getDmax(double m) {
        return MagnitudeUtils.getDistance(m, getMag());
    }
    
    /**
     * Get maximum survey distance for star of absolute magnitude M.
     * @param m
     * 	Apparent magnitude limit.
     * @param M
     * 	Absolute magnitude for star.
     * @return
     * 	The maximum survey distance for star of absolute magnitude M [pc].
     */
    public static double getDmax(double m, double M) {
        return MagnitudeUtils.getDistance(m, M);
    }
    
}