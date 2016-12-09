package wd.wdlf.inversion.infra;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import numeric.data.RangeMap;
import photometry.Filter;
import sfr.algoimpl.InitialGuessSFR;
import wd.models.algo.WdCoolingModelSet;
import wd.models.infra.WdAtmosphereType;
import wd.wdlf.dm.Star;
import wd.wdlf.infra.NoSFRConstraintException;
import wd.wdlf.inversion.util.InversionPlotUtil;
import wd.wdlf.inversion.util.InversionUtil;
import wd.wdlf.util.ModelWdlfUtil;

/**
 * Performs a single iteration of the WDLF inversion algorithm using a Monte Carlo method.
 *
 * @author nickrowell
 */
public class MonteCarloInverter {
	
	/**
	 * The logger.
	 */
	private static final Logger logger = Logger.getLogger(MonteCarloInverter.class.getName());
	
	/**
     * Used to add Gaussian observation sigma to bolometric magnitude.
     */
    private static Random error = new Random();
	
    // Simulation parameters & objects
    
    /**
     * Simulation stars binned according to WD magnitude.
     */
    private RangeMap<Star> whiteDwarfs;
    
    /**
     * Simulation stars binned according to formation time.
     */
    private RangeMap<Star> progenitors;
    
    /**
     * InversionState contains all WDLF inversion algorithm stuff.
     */
    private InversionState inversionState;
    
    /**
     * {@link P_WD} represents the plot of the joint distribution of white dwarf mass and magnitude.
     */
    public BufferedImage pwdPlot;
    
    /**
     * {@link P_MS} represents the plot of the joint distribution of progenitor mass and formation time.
     */
    public BufferedImage pmsPlot;
    
    /**
     * Main constructor.
     * 
     * @param inversionState
     * 	The {@link InversionState} object.
     */
    public MonteCarloInverter(InversionState inversionState) {
        this.inversionState = inversionState;
    }
    
    /** 
     * Implements a single iteration step of inversion algorithm.
     */
    public final void performSingleIteration() throws IOException {
    	
    	// Extract some fields to local variables for convenience
    	WdCoolingModelSet wdCoolingModelSet = inversionState.params.getBaseWdCoolingModels();
    	Filter filter = inversionState.params.getFilter();
        
        // Construct new objects to store simulation stars.
        whiteDwarfs = new RangeMap<Star>(inversionState.wdlf_obs.density.getBinCentres(), inversionState.wdlf_obs.density.getBinWidths());
        progenitors = new RangeMap<Star>(inversionState.currentSfr.data.getBinCentres(), inversionState.currentSfr.data.getBinWidths());
        
        // Distribute simulated WDs so that an equal number form in each lookback time bin.
        double nWdsPerBin = inversionState.n_WDs / progenitors.size();
        
        logger.info("Iteration "+inversionState.iterations+": Simulating "+inversionState.n_WDs+" white dwarfs...\n");
        
        // Loop over all lookback time bins.
        for(int lookbackTimeBin = 0; lookbackTimeBin < progenitors.size(); lookbackTimeBin++) {
            
            // Lower edge of formation time bin
            double t_lower = progenitors.getRange(lookbackTimeBin).lower;
            // Upper edge of formation time bin
            double t_upper = progenitors.getRange(lookbackTimeBin).upper;    
            
            // Count number of simulation stars created in this bin in order
            // to get n_WDs_per_bin WD progenitors
            double N_SIM_STARS = 0;

            // Number of real stars that form in this formation time bin.
            double N_REAL_STARS = inversionState.currentSfr.data.integrate(lookbackTimeBin)[0];
            
            // Continually create stars uniformly distributed in this formation
            // time bin until n_WD_s_per_bin WD progenitors have been made.
            for(int nWdsFormedInBin=0; nWdsFormedInBin<=nWdsPerBin; ) {
      
                // Create a new Star using current distributions
                Star star = new Star(t_lower, t_upper, inversionState.params);
            
                N_SIM_STARS++;
            
                if(star.getTotalAge() > star.getPreWdLifetime()) {

                    // Star has become a WD
                	double coolingTimeWD = star.getTotalAge() - star.getPreWdLifetime();
                	
                	double wdMass = star.getWhiteDwarfMass();
                	WdAtmosphereType atmType = star.getWhiteDwarfAtmph();
                
                    // Get magnitude at present day
                    double mag = wdCoolingModelSet.mag(coolingTimeWD, wdMass, atmType, filter);
                    
                    // Add (Gaussian) error to the magnitude to simulate observation error
                    mag += error.nextGaussian() * inversionState.params.getSigM();
                    
                    // Increment WD counter.
                    nWdsFormedInBin++;
            
                    // Set magnitude of simulation star.
                    star.setMag(mag);
                
                    // Determine if WD models had to be extrapolated in order to get this magnitude.
                    star.setExtrap(wdCoolingModelSet.isExtrapolated(coolingTimeWD, wdMass, atmType, filter));
                
                    // Now add star to whiteDwarfs and progenitors histograms. 
                    // Note that the star will not be added to either of the
                    // histograms if it lies outside their range. This will never
                    // happen for progenitors, because the progenitors number
                    // matches the star formation model used to generate simulation
                    // stars. However, the whiteDwarfs object range is based on 
                    // observed WDLF and there may be missing regions due to lack 
                    // of data, so some stars may be lost from whiteDwarf population.
                
                    // Record whether star falls in WDLF data bin or not. Only
                    // observed WDs can be used to constrain star formation history.
                    star.setIsObserved(whiteDwarfs.add(star.getMag(), star));
                    
                    progenitors.add(star.getTotalAge(), star);
                }
        
                // Star hasn't yet turned into a WD - take no action.

            }
            
            // The abundance of WDs must be scaled to reflect the true number of
            // stars formed during simulation time. This is the scale factor, and is
            // calculated in the main Monte Carlo simulation loop.
            
            // Loop keeps creating until the desired number of WDs have formed. 
            // The abundance of these must then be scaled according to the total
            // number of stars that were formed during the loop, and the total
            // number that should have formed given the integrated star formation
            // rate. The number factor is calculated here:
            
            // Set number of real stars that each simulation star represents,
            // and variance on that quantity.
            double n = N_REAL_STARS / N_SIM_STARS;
            
            // Re-scale number density of all stars in current formation time 
            // bin to units of real stars. Note that this doesn't account
            // for low mass stars that don't form WDs at the present day.
            for(Star star : progenitors.get(lookbackTimeBin)) {
                star.reweight(n, 0);
            }
            
        }
        
        // Calculate forward-modelled WDLF for plotting. It's important to
        // do this before the scaleToObservedDensity call, as after that the
        // model WDLF will exactly match the observed one and won't reflect the
        // WDLF obtained from the initial SFR.
        inversionState.wdlf_model = ModelWdlfUtil.getLF(true, whiteDwarfs);
        
        // Update step in Richardson-Lucy algorithm - correct modelled
        // values to observed values then back-propagate corrections to 
        // previous guess of SFR.
        logger.info("Iteration "+inversionState.iterations+": Scaling WD population...\n");
        
        double chi2 = InversionUtil.scaleToObservedDensity(whiteDwarfs, inversionState.wdlf_obs);
        
        inversionState.chi2.add(chi2);
        
        // Update SFR model at end of iteration step. This method corrects for
        // low mass stars that have not had time to form white dwarfs.
        logger.info("Iteration "+inversionState.iterations+": Calculating revised SFR model...\n");
        
        // Copy the initial guess SFR to the updated instance in order to get the lookback time bins;
        // the updated instance will then have the bin values set appropriately.
        inversionState.updatedSfr = (InitialGuessSFR)inversionState.currentSfr.copy();
    	
        for(int bin = 0; bin<progenitors.size(); bin++) {
            try {
            	double[] sfrAndError = InversionUtil.getSFR(progenitors, bin, true, inversionState.params);
                inversionState.updatedSfr.setSFRBin(bin, sfrAndError[0], sfrAndError[1]);
            }
            catch(NoSFRConstraintException e) {
            	logger.log(Level.WARNING, "NoSFRConstraintException in bin "+bin, e);
            }
        }

        // Make the diagnostic plots P_{MS} and P_{WD}
        logger.info("Iteration "+inversionState.iterations+": Writing output files...\n");
        
        File outputDir = new File(inversionState.outputDirectory, String.format("Iteration_%d",inversionState.iterations));
        
        if(!outputDir.mkdir()) {
        	logger.severe("Iteration "+inversionState.iterations+": Could not create output directory\n");
            throw new IOException("Could not make output directory " + outputDir.getAbsolutePath()+"!");
        }
        
        logger.info("Iteration "+inversionState.iterations+": Creating P_WD\n");
        
        pwdPlot = InversionPlotUtil.getPwd(outputDir, inversionState, whiteDwarfs);
        
        logger.info("Iteration "+inversionState.iterations+": Creating P_MS\n");
        
        pmsPlot = InversionPlotUtil.getPms(outputDir, inversionState, progenitors);
        
        // Print out chi-square for this iteration
        logger.info("Iteration "+inversionState.iterations+": Chi^2 = "+chi2+"\n");
        
        // Update iteration count
        inversionState.iterations++;
    }

}