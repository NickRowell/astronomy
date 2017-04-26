package wd.wdlf.dm;

import java.util.logging.Logger;

import ifmr.algo.BaseIfmr;
import ifmr.infra.IFMR;
import imf.algoimpl.IMF_PowerLaw;
import infra.os.OSChecker;
import ms.lifetime.algo.PreWdLifetime;
import ms.lifetime.infra.PreWdLifetimeModels;
import photometry.Filter;
import wd.models.algo.WdCoolingModelSet;
import wd.models.infra.WdCoolingModels;

/**
 * Class encapsulates all the parameters and objects necessary for the modelling of white dwarf
 * populations.
 *
 * @author nrowell
 * @version $Id$
 */
public class WdlfModellingParameters {

	/**
	 * The logger.
	 */
	private static final Logger logger = Logger.getLogger(WdlfModellingParameters.class.getName());
	
    /**
     * Initial mass function.
     */
    private IMF_PowerLaw imf;
    
    /**
     * Pre-WD lifetime models
     */
    private PreWdLifetimeModels preWdLifetimeModels;
    
    /**
     * Mean progenitor metallicity (Z)
     */
    private double z;
    
    /**
     * Mean progenitor Helium content (Y)
     */
    private double y;
    
    /**
     * Standard deviation in progenitor metallicity (Z).
     */
    private double sigmaZ;
    
    /**
     * Standard deviation in progenitor Helium content (Y).
     */
    private double sigmaY;
    
    /**
     * Initial-Final Mass Relation.
     */
    private IFMR ifmr;
    
    /** 
     * WD cooling models.
     */
    private WdCoolingModels wdCoolingModels;
    
    /**
     * Primary passband.
     */
    private Filter filter;
    
    /**
     * Fraction of Hydrogen atmospheres (w_H = n_H/(n_H + n_He)).
     */
    private double w_H;
    
    /**
     * Observational error on magnitude.
     * 
     * TODO: replace this with a function that allows fainter objects to have larger magnitude errors.
     * 
     */
    private double sigM;
    
    /**
     * Default constructor, setting appropriate default parameter values.
     */
    public WdlfModellingParameters() {
        imf  = new IMF_PowerLaw(-2.3);
        ifmr = IFMR.KALIRAI_2008;
        w_H  = 1.0;
        sigM = 0.1;
        // Solar value
        z = 0.003; //0.017;
        // From Y = 0.23 + 2.41Z (Flynn 2004; Casagrande et al. 2007)
        y = 0.26; //0.271;
        sigmaZ = 0.001;
        sigmaY = 0.001;
        setPreWdLifetimeModels(PreWdLifetimeModels.PADOVA);
        setWdCoolingModels(WdCoolingModels.RENEDO);
    }
    
    /**
     * Set the {@link IFMR}.
     * @param ifmr
     * 	The {@link IFMR} enumerated type to set.
     */
    public void setIFMR(IFMR ifmr) { 
    	logger.info("Setting IFMR to "+ifmr);
    	this.ifmr = ifmr;
    }
    
    /** 
     * Get the enumerated type of the current {@link BaseIfmr}.
     * @return
     * 	The enumerated type of the current {@link BaseIfmr}.
     */
    public IFMR getIfmrEnum() { 
    	return ifmr;
    }
    
    /** 
     * Get the current {@link BaseIfmr}.
     * @return
     * 	The current {@link BaseIfmr}.
     */
    public BaseIfmr getIFMR() { 
    	return ifmr.getIFMR();
    }
    
    /**
     * Set the current IMF slope.
     */
    public void setIMF(double exp) {
    	logger.info("Setting IMF exponent to "+imf);
    	imf.setExponent(exp);
    }

    /**
     * Get the current IMF object.
     */
    public IMF_PowerLaw getIMF() { 
    	return imf;
    }
    
    /**
     * Set the {@link PreWdLifetimeModels}
     * @param preWdLifetimeModels
     *  The {@link PreWdLifetimeModels} to set.
     */
    public void setPreWdLifetimeModels(PreWdLifetimeModels preWdLifetimeModels) {
    	logger.info("Setting pre-WD lifetime models source to "+preWdLifetimeModels);
    	this.preWdLifetimeModels = preWdLifetimeModels;
    }
    
    /**
     * Get the {@link PreWdLifetime} corresponding to the current {@link PreWdLifetimeModels}.
     * @return
     * 	The {@link PreWdLifetime} corresponding to the current {@link PreWdLifetimeModels}.
     */
    public PreWdLifetime getPreWdLifetime() { 
   		return this.preWdLifetimeModels.getPreWdLifetimeModels();
    }
   
    /**
     * Get the enumerated type of the {@link PreWdLifetimeModels} models.
     * @return
     * 	The enumerated type of the {@link PreWdLifetimeModels} models.
     */
    public PreWdLifetimeModels getPreWdLifetimeModelsEnum() {
   		return this.preWdLifetimeModels;
    }
    
    /**
     * Set the mean progenitor metallicity (Z).
     * @param z
     * 	The metallicity value to set.
     */
    public void setMetallicity(double z) {
    	logger.info("Setting metallicity to "+z);
    	this.z = z;
    }

    /**
     * Get the mean progenitor metallicity (Z).
     * @return
     * 	The mean progenitor metallicity (Z).
     */
    public double getMeanMetallicity() {
    	return this.z;
    }
    
    /**
     * Set the standard deviation of the progenitor metallicity (Z).
     * @param sigmaZ
     * 	The standard deviation of the metallicity value to set.
     */
    public void setMetallicitySigma(double sigmaZ) {
    	logger.info("Setting metallicity standard deviation to "+sigmaZ);
    	this.sigmaZ = sigmaZ;
    }

    /**
     * Get the standard deviation of the progenitor metallicity (Z).
     * @return
     * 	The standard deviation of the progenitor metallicity (Z).
     */
    public double getMetallicitySigma() {
    	return this.sigmaZ;
    }
    
    /**
     * Set the mean progenitor Helium content (Y).
     * @param y
     * 	The Helium content value to set.
     */
    public void setHeliumContent(double y) {
    	logger.info("Setting Helium content to "+y);
    	this.y = y;
    }
    
    /**
     * Get the mean progenitor Helium content (Y).
     * @return
     * 	The mean progenitor Helium content (Y).
     */
    public double getMeanHeliumContent() {
    	return this.y;
    }
    
    /**
     * Set the standard deviation of the progenitor Helium content (Y).
     * @param sigmaY
     * 	The standard deviation of the progenitor Helium content (Y) value to set.
     */
    public void setHeliumContentSigma(double sigmaY) {
    	logger.info("Setting Helium content standard deviation to "+sigmaY);
    	this.sigmaY = sigmaY;
    }

    /**
     * Get the standard deviation of the progenitor Helium content (Y).
     * @return
     * 	The standard deviation of the progenitor Helium content (Y).
     */
    public double getHeliumContentSigma() {
    	return this.sigmaY;
    }
    
    /**
     * Set the {@link WdCoolingModels}, and initialise the {@link Filter} to
     * the first available in the set.
     * @param preWdLifetimeModels
     *  The {@link WdCoolingModels} to set.
     */
    public void setWdCoolingModels(WdCoolingModels wdCoolingModels) {
    	logger.info("Setting WD cooling models source to "+wdCoolingModels);
    	this.wdCoolingModels = wdCoolingModels;
    	setFilter(wdCoolingModels.getWdCoolingModels().getPassbands()[0]);
    }

    /**
     * Get the {@link WdCoolingModelSet} corresponding to the current {@link WdCoolingModels}.
     * @return
     * 	The {@link WdCoolingModelSet} corresponding to the current {@link WdCoolingModels}.
     */
    public WdCoolingModelSet getBaseWdCoolingModels() { 
    	return wdCoolingModels.getWdCoolingModels();
    }
    
    /**
     * Get the enumerated type of the {@link BaseWdCoolingModels} models.
     * @return
     * 	The enumerated type of the {@link BaseWdCoolingModels} models.
     */
    public WdCoolingModels getWdCoolingModelsEnum() { 
    	return wdCoolingModels;
    }
    
    /**
     * Set the {@link Filter} to be used as the primary passband.
     * @param filter
     *  The {@link Filter} to set.
     */
    public void setFilter(Filter filter) {
    	logger.info("Setting Filter to "+filter);
    	this.filter = filter;
    }

    /**
     * Get the current {@link Filter}.
     * @return
     * 	The current {@link Filter}.
     */
    public Filter getFilter() { 
    	return filter;
    }
    
    
    
    /**
     * Set value of {@link WdlfModellingParameters#w_H}, the fraction of H atmosphere types
     * to the total H + He.
     * @param w_H
     * 	The fraction of H atmosphere types to the total H + He to set.
     */
    public void setW_H(double w_H) {
    	logger.info("Setting w_H to "+w_H);
    	this.w_H = w_H;
    }
    
    /**
     * Get value of {@link WdlfModellingParameters#w_H}, the fraction of H atmosphere types
     * to the total H + He.
     * @return
     * 	The fraction of H atmosphere types to the total H + He.
     */
    public double getW_H() { 
    	return w_H;
    }
    
    /**
     * Set value of {@link WdlfModellingParameters#sigM}, the observational error on the magnitude.
     * This is a zero-mean Gaussian with standard deviation {@link WdlfModellingParameters#sigM}.
     * @param sigM
     * 	The value of {@link WdlfModellingParameters#sigM} to set.
     */
    public void setSigM(double sigM) {
    	logger.info("Setting sigM to "+sigM);
    	this.sigM = sigM;
    }
    
    /**
     * Get value of {@link WdlfModellingParameters#sigM}, the observational error on the magnitude.
     * This is a zero-mean Gaussian with standard deviation {@link WdlfModellingParameters#sigM}.
     * @return
     * 	The value of {@link WdlfModellingParameters#sigM}.
     */
    public double getSigM() { 
    	return sigM;
    }
    
    @Override
    public String toString()
    {
        return "# IMF exponent     = " + imf.getExponent() + OSChecker.newline +
        	   "# MS models        = " + preWdLifetimeModels.toString() + OSChecker.newline +
               "# Metallicity Z    = " + z + " +/- " + sigmaZ + OSChecker.newline +
               "# Helium content Y = " + y + " +/- " + sigmaY + OSChecker.newline + 
               "# IFMR             = " + ifmr.toString() + OSChecker.newline +
               "# Alpha parameter  = " + w_H + OSChecker.newline +
               "# WD models        = " + wdCoolingModels.toString() + OSChecker.newline +
               "# Filter           = " + filter.toString() + OSChecker.newline +
               "# Mag. error       = " + sigM + OSChecker.newline;
    }
}