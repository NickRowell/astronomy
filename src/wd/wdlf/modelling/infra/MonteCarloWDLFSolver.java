package wd.wdlf.modelling.infra;

import java.util.Collection;
import java.util.Random;

import numeric.data.RangeMap;
import photometry.Filter;
import wd.models.algo.WdCoolingModelSet;
import wd.wdlf.algoimpl.ModelWDLF;
import wd.wdlf.dm.Star;
import wd.wdlf.util.ModelWdlfUtil;

/**
 * Instances of this class are used to calculate synthetic WDLF using Monte Carlo integration method.
 *
 * @author nrowell
 * @version $Id$
 */
public class MonteCarloWDLFSolver extends WDLFSolver {
	
    // Used to add Gaussian error to bolometric magnitude
    private static Random error = new Random();
    
    /**
     * Default constructor.
     */
    public MonteCarloWDLFSolver() {
    	
    }
    
    /**
     * Monte Carlo LF integration code.
     */
    @Override
    public final ModelWDLF calculateWDLF(ModellingState modellingState) {
        
        // Get WhiteDwarfs object for accumulating simulated stars.
        RangeMap<Star> whiteDwarfs = new RangeMap<Star>(modellingState.wdlfBinCentres, modellingState.wdlfBinWidths);
        
        // Counter for current number of WDs formed in a given run
        long wd;
    
        // The abundance of WDs must be scaled to reflect that true number of
        // stars formed during simulation time. This is the scale factor, and is
        // calculated in the main Monte Carlo simulation loop.
        double WEIGHT;
        
        // Integrate SFR to get total number of real stars created.
        double N_REAL_STARS = modellingState.syntheticSFR.integrateSFR()[0];

        // Count number of simulation stars created
        double N_SIM_STARS = 0;
        
        // A few variables...
        double coolingTimeWD;
        
        // Get the WD cooling models
        WdCoolingModelSet wdCoolingModels = modellingState.params.getBaseWdCoolingModels();
        
        // Get the Filter
        Filter filter = modellingState.params.getFilter();
        
        // Now generate stars until desired number of WDs has been produced
        for(wd=0; wd<modellingState.n_WDs; ) {
        
            // Create a new Star using current distributions
            Star star = new Star(modellingState.syntheticSFR, modellingState.params);
            
            N_SIM_STARS++;
            
            // Determine time that star has been cooling as a WD
            if((coolingTimeWD = (star.getTotalAge() - star.getPreWdLifetime())) > 0.0) {
            	
                // Star HAS become a WD
                // Get bolometric magnitude at present day
                double mbol = wdCoolingModels.mag(coolingTimeWD, star.getWhiteDwarfMass(), star.getWhiteDwarfAtmph(), filter);
                
                // Add Gaussian noise
                mbol += error.nextGaussian() * modellingState.params.getSigM();
                
                // Set bolometric magnitude of simulation star.
                star.setMag(mbol);
                
                // Determine if WD models had to be extrapolated in order
                // to get this bolometric magnitude.
                star.setExtrap(wdCoolingModels.isExtrapolated(coolingTimeWD, star.getWhiteDwarfMass(),
                                           star.getWhiteDwarfAtmph(), filter));
                
                // In volume limited survey, all stars are observed and each
                // carries a weight of 1.
                
                switch(modellingState.surveyType) {
                    case VOLUME_LIMITED: {
                                
                        // Add star to simulated WDLF, if it lies within bolometric
                        // magnitude range of bins.
                        star.setIsObserved(whiteDwarfs.add(star.getMag(), star));
                        
                        // Increment WD counter, if star was added to WDLF
                        if(star.getIsObserved()) wd++;
                        
                        // If ints are used to cound WD numbers, this calculation
                        // will overflow at about 20 million WDs. Use longs
                        // then cast percentage to int.
                        setProgress((int)(100l * wd / modellingState.n_WDs));

                        break;
                    }
                        
                    // In magnitude limited survey, stars are observed with a 
                    // probability derived from their absolute magnitude and
                    // carry a weight derived from the survey apparent magnitude
                    // limit.
                    case MAGNITUDE_LIMITED: {
                    
                        // Get total generalised survey volume, in which any given
                        // survey star could reside. Use a sufficiently bright
                        // absolute magnitude that no real star could lie beyond
                        // this distance.
                        double survey_edge = Star.getDmax(modellingState.apparentMagLimit, -5);
                        double v_gen_tot   = modellingState.surveyVolume.interpolateY(survey_edge)[0];
                    
                        // Get maximum observable distance for the present star.
                        double d_max = star.getDmax(modellingState.apparentMagLimit);
                        // Get maximum generalized survey volume for star.
                        double v_gen_max = modellingState.surveyVolume.interpolateY(d_max)[0];
                    
                        // Probability that this star is detected is equal to the
                        // ratio of the volume in which it is detectable to the
                        // total volume in which it could reside. This assumes that
                        // probability of lying in any given generalised volume
                        // is constant.
                        double obs_probability = v_gen_max/v_gen_tot;
                                        
                        if(Math.random() < obs_probability) {
                            // Star IS contained in survey volume
                        
                            // We use a variation on the 1/Vmax estimator, because
                            // I want to keep the density dimensionless wrt volume.
                            // We multiply by the total survey volume to achieve
                            // this.
                            star.reweight(v_gen_tot/v_gen_max, 0);
                        
                            // Add star to simulated WDLF, if it lies within bolometric
                            // magnitude range of bins.
                            star.setIsObserved(whiteDwarfs.add(star.getMag(), star));
                        
                            // Increment WD counter, if star was added to WDLF
                            if(star.getIsObserved()) wd++;
                                                
                            setProgress((int)(100l * wd /modellingState.n_WDs));
                            
                            break;
                        }
                    
                    }
                
                }
                
            }
        
        }
        
        // Required number of simulation stars created. Scale number of stars
        // that each represents to units of real stars.
        
        // Loop keeps creating until the desired number of WDs have formed. 
        // The abundance of these must then be scaled according to the total
        // number of stars that were formed during the loop, and the total
        // number that should have formed given the integrated star formation
        // rate. The weight factor is calculated here:
        WEIGHT = N_REAL_STARS / N_SIM_STARS;

        for(int i=0; i<whiteDwarfs.size(); i++) {
        	Collection<Star> stars = whiteDwarfs.get(i);
	        for(Star star : stars) {
	            star.reweight(WEIGHT, 0);
	        }
        }
        
        // Derive WDLF from simulated population, in per-mbol units.
        
        // Get only non-zero density bins from whiteDwarfs
        ModelWDLF modelWdlf = ModelWdlfUtil.getLF(true, whiteDwarfs);
        modelWdlf.setTarget("Simulated WDLF");
        modelWdlf.setFilter(modellingState.params.getFilter());
        
        return modelWdlf;
    }
    
}