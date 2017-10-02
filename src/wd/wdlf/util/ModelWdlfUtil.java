package wd.wdlf.util;

import numeric.data.Range;
import numeric.data.RangeMap;
import wd.wdlf.algoimpl.ModelWDLF;
import wd.wdlf.dm.Star;

/**
 * Utilities associated with the model WDLF.
 *
 * @author nrowell
 * @version $Id$
 */
public class ModelWdlfUtil {
	
	/**
	 * Gets the luminosity function for the {@link RangeMap<Star>}; assumes the input rangemap
	 * contains {@link Star}s that are mapped according to their magnitude.
	 * 
	 * @param starsPerMag
	 * 	If true, then the density units are scaled to per-magnitude (mag^{-1}). If false,
	 * then the density units are per-bin-width.
	 * @param stars
	 * 	The {@link RangeMap<Star>} containing the stars to process.
	 * @return
	 * 	The {@link ModelWDLF} derived from the given stellar population.
	 */
	public static ModelWDLF getLF(boolean starsPerMag, RangeMap<Star> stars) {
		
		// XXX: I updated this to return a WDLF with the same number of magnitude bins as
		// the input RangeMap; previously empty bins were skipped but this was affecting
		// efforts to fit model WDLFs to observations. In reality if there are no simulated
		// WDs in a bin then the density is zero and the uncertainty is very large. (Although
		// formally we could place an upper limit on the density consistent with observing no
		// stars).
		
		int n = stars.size();
		
        double[] centres    = new double[n];
        double[] widths     = new double[n];        
        double[] lf         = new double[n];
        double[] lf_STD     = new double[n];
        double[] mass       = new double[n];
        double[] mass_STD   = new double[n];
        double[] age        = new double[n];
        double[] age_STD    = new double[n];       
        
        // Loop over all magnitude bins
        for(int bin=0; bin<stars.size(); bin++) {
        	
        	centres[bin] = stars.getRange(bin).mid();
	        widths[bin] = stars.getRange(bin).width();
	        
            if(stars.get(bin).isEmpty()) {
            	// No simulated WDs. Density is zero and uncertainty is very large
            	lf[bin] = 0.0;
	            lf_STD[bin] = 1e9;
	            mass[bin] = 0.0;
	            mass_STD[bin] = 0.0;
	            age[bin] = 0.0;
	            age_STD[bin] = 0.0;
            }
            else {
	            
	            double[] lf_bin = getNumberDensity(stars, bin, starsPerMag);
	            double[] mass_bin = getMeanWdMass(stars, bin);
	            double[] age_bin = getMeanAge(stars, bin);
	
	            lf[bin] = lf_bin[0];
	            lf_STD[bin] = lf_bin[1];
	            mass[bin] = mass_bin[0];
	            mass_STD[bin] = mass_bin[1];
	            age[bin] = age_bin[0];
	            age_STD[bin] = age_bin[1];
            }
        }
        
        return new ModelWDLF(centres, widths, lf, lf_STD, mass, mass_STD, age, age_STD);
	}
	
	/**
	 * Gets the mean and standard error of the number density of stars in the given bin.
	 * @param stars
	 * 	The {@link RangeMap<Star>} containing the stars to process.
	 * @param bin
	 * 	The index of the bin in the {@link RangeMap<Star>} containing the stars to process.
	 * @param starsPerUnit
	 * 	If true, then the density units are scaled to the inverse of whatever the units of the
	 * bin width are (e.g. per-magnitude mag^{-1} or per-year yr^{-1}). If false, then the density
	 * units are per-bin-width.
	 * @return
	 * 	The number density in N-per-bin (if stars_per_unit=false) or N-per-unit (if stars_per_unit=true),
	 * and the associated uncertainty (standard deviation).
	 */
	public static double[] getNumberDensity(RangeMap<Star> stars, int bin, boolean starsPerUnit) {
        
        // Check for no simulated stars in bin
        if(stars.get(bin).isEmpty()) {
        	return null;
        }

        Range range = stars.getRange(bin);
        double width = range.width();
        
        // Sum numbers of stars and uncertainty in quadrature.
        double density = 0;
        double sigma2_density = 0;
        
        for (Star star : stars.get(bin)) {
            density         += star.getNumber();
            sigma2_density  += star.getSigma2Number();
        }
        
        // Optionally convert this into units of stars-per-magnitude
        if(starsPerUnit) {
            density        /= width;
            sigma2_density /= (width*width);            
        }
        
        return new double[]{density, Math.sqrt(sigma2_density)};
    }
	
	/**
	 * Gets the mean and standard error on the white dwarf mass for stars in the given bin.
	 * @param stars
     * 	The {@link RangeMap<Star>} containing the stars to process.
	 * @param bin
	 * 	The index of the bin in the {@link RangeMap<Star>} containing the stars to process.
	 * @return
	 */
    public static double[] getMeanWdMass(RangeMap<Star> stars, int bin) {
        
        // Check for no simulated stars in bin
        if(stars.get(bin).isEmpty()) {
        	return null;
        }
        
        // Get average mass and average mass squared
        double sum_mass    = 0;
        double sum_mass_2  = 0;
        double n_stars     = 0;
        
        for (Star star : stars.get(bin)) {
            n_stars    += star.getNumber();
            sum_mass   += star.getNumber()*star.getWhiteDwarfMass();
            sum_mass_2 += star.getNumber()*star.getWhiteDwarfMass()*star.getWhiteDwarfMass();
        }
        
        // Mean mass
        double mean_mass   = sum_mass / n_stars;
        double mean_mass_2 = sum_mass_2 / n_stars;
        
        // Variance is equal to mean of square minus square of mean
        double var_mass = mean_mass_2 - mean_mass * mean_mass;
        
        return new double[]{mean_mass, Math.sqrt(var_mass)};
    }   
	
    /**
     * Gets the mean and standard error on the total stellar age for stars in the given bin.
     * @param stars
     * 	The {@link RangeMap<Star>} containing the stars to process.
     * @param bin
	 * 	The index of the bin in the {@link RangeMap<Star>} containing the stars to process.
     * @return
     */
    public static double[] getMeanAge(RangeMap<Star> stars, int bin) {
    	
        // Check for no simulated stars in bin
        if(stars.get(bin).isEmpty()) {
        	return null;
        }
        
        // Get average age and average age squared
        double sum_age    = 0;
        double sum_age_2  = 0;
        double n_stars     = 0;
        
        for (Star star : stars.get(bin)) {
            n_stars   += star.getNumber();
            sum_age   += star.getNumber()*star.getTotalAge();
            sum_age_2 += star.getNumber()*star.getTotalAge()*star.getTotalAge();
        }
        
        // Mean age
        double mean_age   = sum_age / n_stars;
        double mean_age_2 = sum_age_2 / n_stars;
        
        // Variance is equal to mean of square minus square of mean
        double var_age = mean_age_2 - mean_age * mean_age;
        
        return new double[]{mean_age, Math.sqrt(var_age)};
    }
	
}