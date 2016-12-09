package wd.wdlf.inversion.util;

import java.util.logging.Logger;

import imf.algo.BaseImf;
import numeric.data.Range;
import numeric.data.RangeMap;
import wd.wdlf.algo.BaseWdlf;
import wd.wdlf.dm.Star;
import wd.wdlf.dm.WdlfModellingParameters;
import wd.wdlf.infra.NoSFRConstraintException;
import wd.wdlf.util.ModelWdlfUtil;

/**
 * Class provides utilities that implement some of the main inversion operations on the simulated stellar
 * population.
 *
 *
 * @author nrowell
 * @version $Id$
 */
public class InversionUtil {
	
	/**
	 * Tolerance used for testing equality of doubles.
	 */
	private static final double EPSILON = 1e-9;

	/**
	 * Number of rectangular strips to divide SFR bins into when computing the
	 * fraction of stars in a given lookback time range that are WDs at the present day.
	 */
	private static final int N_STRIP = 10;
    
	/**
	 * The logger.
	 */
	private static final Logger logger = Logger.getLogger(InversionUtil.class.getName());
	
    /**
     * Scales the weight of each simulation star so that the simulated WDLF matches the observed WDLF.
     * @param stars
     * 	The {@link RangeMap<Star>} containing the stars to process. This must represent the white dwarf
     * population binned in the map according to their magnitude, with the same binning as the 
     * observed WDLF.
     * @param obsWdlf
     * 	The {@link BaseWdlf} representing the observed WDLF; the simulated stars will be scaled such
     * that their number density in each bin matches that of the observed WDLF.
     * @return
     * 	The chi-square for the initial fit of the simulated WDLF to the observed WDLF.
     */
    public static double scaleToObservedDensity(RangeMap<Star> stars, BaseWdlf obsWdlf) {
    	
        // Sanity check: this ObservedWDLF should have the same number of
        // density bins as the simulation wdlf.
        if(stars.size() != obsWdlf.density.size()) {
            throw new RuntimeException("Observed WDLF and model WDLF have different number of"
            		+ " magnitude bins!"+stars.size() + " and "+obsWdlf.density.size());
        }
                    
        // Chi-square statistic for fit.
        double chi2 = 0;
        
        // Loop over all bins
        for (int bin=0; bin<stars.size(); bin++) {

            Range range = stars.getRange(bin);
            double width = range.width();
            double centre = range.mid();
        	
            // Sanity checks on bin configuration and density values
            if(Math.abs(width - obsWdlf.density.getBinWidth(bin)) > EPSILON) {
                throw new RuntimeException("Observed & model WDLF have different mbol bin widths! "+
                		width + " and "+obsWdlf.density.getBinWidth(bin)+" at bin "+bin);
            }
            if(Math.abs(centre - obsWdlf.density.getBinCentre(bin)) > EPSILON) {
                throw new RuntimeException("Observed & model WDLF have different mbol bin centres! "+
                		centre + " and "+obsWdlf.density.getBinCentre(bin)+" at bin "+bin);
            }
            if(obsWdlf.density.getBinContents(bin) <= 0) {
            	throw new RuntimeException("Observed WDLF density is "+obsWdlf.density.getBinContents(bin)+" in bin "+bin);
            }
            if(obsWdlf.density.getBinUncertainty(bin) <= 0) {
            	throw new RuntimeException("Observed WDLF error is "+obsWdlf.density.getBinUncertainty(bin)+" in bin "+bin);
            }
            
            // Get number of simulated stars and associated error in this magnitude bin.
            // This is the synthetic WDLF. Use per-magnitude units for comparison to observed WDLFs.
            double[] synthWdlf = ModelWdlfUtil.getNumberDensity(stars, bin, true);
            
            if(synthWdlf == null) {
                // If no simulated stars, this bin cannot be used to constrain SFR.
                logger.warning("Too few simulation stars! No simulated white dwarfs"
                		+ " lie in magnitude range ["+range.lower+":"+range.upper+"]");
                continue;
            }
            
            // Density ratio between simulated WDs and observed WDs, and errors.
            double phi_sim      = synthWdlf[0];
            double phi_sim_err  = synthWdlf[1];
            double phi_obs      = obsWdlf.density.getBinContents(bin);
            double phi_obs_err  = obsWdlf.density.getBinUncertainty(bin);
            
            // Calculate chi-square sum.
            chi2 += (phi_obs - phi_sim)*(phi_obs - phi_sim)/(phi_obs_err*phi_obs_err);
            
            // What factor must simulation stars be weighted by to give a number that matches observations?
            
            // CORRECTION FACTOR - correct simulated number of each WD to  match observed WD number.
            double w = phi_obs/phi_sim;
            
            // Uncertainty in correction factor, considering error in both simulated and observed star number.
            double sigma_w = Math.sqrt((phi_obs_err*phi_obs_err)/(phi_sim*phi_sim) + (phi_obs*phi_obs*phi_sim_err*phi_sim_err)/(Math.pow(phi_sim,4)));
            
            // Weight all simulated stars in this bin by this factor.
            for(Star star : stars.get(bin)) {
                star.reweight(w, sigma_w);
            }
            
            // Divide observational error among all simulation stars. Weight
            // according to number of real stars that each represents, so that
            // simulation stars representing more real stars carry a greater
            // fraction of the observational error.
            addObsError(stars, bin, phi_obs_err);
        }
        
        // At this point, the observed and simulated WDLFs are identical. The uncertainty on the simulated WDLF
        // is slightly larger due to the finite number of simulation stars; in the limit of an infinite number
        // of simulation stars, the error on the simulated WDLF converges on the observed WDLF.

        // Return chi-square statistic
        return chi2;
    }
    
    /**
     * Apply additive constant to the variance on the number of real stars. This is
     * intended to incorporate observational error in the number of stars in some e.g.
     * magnitude range.
     * 
     * @param stars
     * 	The {@link RangeMap<Star>} containing the stars to process.
     * @param bin
     * 	The index of the bin in the {@link RangeMap<Star>} containing the stars to process.
     * @param phiObsErr
     * 	Standard deviation on the observed WDLF in the given bin.
     */
    public static void addObsError(RangeMap<Star> stars, int bin, double phiObsErr) {
        
        // Get number of simulation stars in bin
        int N = stars.get(bin).size();

        Range range = stars.getRange(bin);
        double width = range.width();
        
        // Mean number of real stars that each simulated star represents
        double n_mean = 0;
        for (Star star : stars.get(bin)) {
            n_mean += star.getNumber();
        }
        n_mean /= N;
        
        // Variance per real star.
        double varPerRealStar = phiObsErr * phiObsErr * width * width / (N * n_mean);
        
        // Add this variance onto each simulation star.
        for(Star star : stars.get(bin)) {
            star.addObservationalUncertainty(varPerRealStar * star.getNumber());
        }
    }
    
    /**
     * Marginalise over main sequence mass for a single formation time bin.
     * Returns star formation rate in the given bin, corrected for stars that
     * have not had time to form white dwarfs. Also corrects for fraction of
     * stars that do form white dwarfs, but which are not observed at 
     * magnitudes covered by observed WDLF.
     * 
     * Low mass stars are accounted for by integrating over IMF. Unobserved
     * WDs are accounted for by a simple scaling.
     * 
     * @param stars
     * 	The {@link RangeMap<Star>} containing the stars to process.
     * @param bin
     * 	The index of the bin in the {@link RangeMap<Star>} containing the stars to process.
     * @param starsPerYear
	 * 	If true, then the density units are scaled to stars-per-year (N yr^{-1}). If false,
	 * then the density units are per-bin-width.
     * @param params
     * 	The {@link WdlfModellingParameters}
     * @return
     * 	The star formation rate and standard deviation [N/yr], and the two applied correction factors
     * for unobserved WDs and low mass stars.
     * @throws NoSFRConstraintException
     */
    public static double[] getSFR(RangeMap<Star> stars, int bin, boolean starsPerYear, WdlfModellingParameters params) throws NoSFRConstraintException {

        Range range = stars.getRange(bin);
        double lookBackTimeMin = range.lower;
        double lookBackTimeMax = range.upper;
        double width = range.width();
        
        // Sum total number of observed WDs that formed in this bin
        double density = 0;
        // Variance on total SFR
        double sigma2_density = 0;   
        
        // Sum number of real stars represented by observed WD progenitors 
        // in this bin; only these are used to constrain SFR.
        double nObsWds = 0;
        for(Star star : stars.get(bin)) {
            if(star.getIsObserved()) {
                // Sum densities
                density += star.getNumber();
                // Sum errors in quadrature
                sigma2_density += star.getSigma2Number();
                nObsWds++;
            }
        }
        
        // CORRECT FOR UNOBSERVED WDS
        if(nObsWds==0.0) {
        	// None of the WDs produced by stars that formed in this time range lie within the range of the observed WDLF
            throw new NoSFRConstraintException("SFR bin ["+lookBackTimeMin + ":"+lookBackTimeMax+"] - no observed WDs.");
        }
        double fracObsWds = nObsWds/stars.get(bin).size();
        double unobservedWdCorrection =  1.0 / fracObsWds;
        density = density * unobservedWdCorrection;
        // Correct sigma on total number
        sigma2_density = sigma2_density * unobservedWdCorrection * unobservedWdCorrection;
        
        // CORRECT FOR LOW MASS STARS / NON-WD-PROGENITORS
        double fracWdProgenitors = getFractionWDProgenitorsInTimeRange(lookBackTimeMin, lookBackTimeMax, params);
        if(fracWdProgenitors==0.0) {
        	// NO stars produced WDs (a very recent time range)
        	throw new NoSFRConstraintException("SFR bin ["+lookBackTimeMin + ":"+lookBackTimeMax+"] - no WD progenitors.");
        }
        
        double lowMassCorrection = 1.0 / fracWdProgenitors;
        
        density = density * lowMassCorrection;
        // Correct sigma on total number
        sigma2_density = sigma2_density * lowMassCorrection * lowMassCorrection;

        // Sanity check on density
        if (density < 0.0 || Double.isNaN(density)) {
            throw new RuntimeException("Error on marginalised SFR! SFR = "+density);
        }

        // Optionally convert this into units of stars-per-year
        if(starsPerYear) {
            density /= width;
            sigma2_density /= (width * width);
        }
        
        return new double[]{density, Math.sqrt(sigma2_density), unobservedWdCorrection, lowMassCorrection};
    }
    
    /**
     * Calculate the fraction of main sequence stars in that form in this lookback
     * time range that are white dwarfs at the present day. Only stars above
     * a certain mass have time to form white dwarfs.
     * 
     * @param lookBackTimeMin
     * 	The lower limit in the lookback time [yr]
     * @param lookBackTimeMax
     * 	The upper limit on the lookback time [yr]
     * @param params
     * 	The {@link WdlfModellingParameters} instance encapsulating all the relevant input
     * physics and modelling parameters.
     * @return
     * 	The fraction of main sequence stars in that form in this lookback
     * time range that are white dwarfs at the present day.
     */
    public static double getFractionWDProgenitorsInTimeRange(double lookBackTimeMin, double lookBackTimeMax,
    		WdlfModellingParameters params) {
        
        double width = lookBackTimeMax - lookBackTimeMin;
        
        // Numerically integrate over the width of the bin, and for each 
        // element of t_max determine the fraction of progenitors that have had
        // time to form white dwarfs.
        double dT = width/N_STRIP;
        
        // Fraction of stars that form in this SFR bin that DON'T produce
        // white dwarfs at the present day. Ranges from 0 (very old times; all
        // stars produce white dwarfs) to 1 (very recent times; no stars
        // produce white dwarfs).
        double A_j = 0;
        
        for(double t=lookBackTimeMin; (t+dT/2.0)<lookBackTimeMax; t+=dT) {
            
            // Centre of this formation time element
            double t_ms = t + dT/2.0;
            
            // Mass of stars formed at t_ms that are just forming WDs at the present time.
            // All stars that formed with lower masses have not yet produced WDs.
            double mass_MSTO = params.getPreWdLifetime().getStellarMass(params.getMeanMetallicity(), params.getMeanHeliumContent(), t_ms)[0];
            
            // MS mass is within WD formation range - integrate IMF to get
            // mean fraction of stars that are too low mass to form
            // WDs and are missed from simulation.
            if(mass_MSTO < BaseImf.M_upper) {
                // getIntegral(mass) returns fraction of all stars that form
                // below 'mass'. Normalised to range 0.6 -> 7 M_0.
            	double imf = params.getIMF().getIntegral(mass_MSTO);
                A_j += imf * dT;
            }
            // All stars greater than 7 Solar masses are lost. Fix IMF integral at 1.0.
            // TODO: we shouldn't encounter this situation, because the minimum lookback time
            // is set to the pre-WD lifetime of the most massive star, so all the time steps should
            // create WDs.
            else {
            	logger.warning("Reached unexpected branch for t_ms = "+t_ms +"; mass_MSTO = "+mass_MSTO); 
            	A_j += 1.0 * dT;
            }
            
        }
        
        // Normalise
        A_j /= width;
        
        return (1.0 - A_j);
    }
    
}