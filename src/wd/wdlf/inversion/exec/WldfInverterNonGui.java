package wd.wdlf.inversion.exec;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Logger;

import ifmr.infra.IFMR;
import imf.algo.BaseImf;
import imf.infra.IMF;
import ms.lifetime.infra.PreWdLifetimeModels;
import numeric.data.DiscreteFunction1D;
import numeric.data.FloatList;
import photometry.Filter;
import sfr.algoimpl.InitialGuessSFR;
import wd.models.algo.WdCoolingModelSet;
import wd.models.infra.WdAtmosphereType;
import wd.models.infra.WdCoolingModels;
import wd.wdlf.algo.BaseWdlf;
import wd.wdlf.inversion.infra.Convergence;
import wd.wdlf.inversion.infra.InversionState;
import wd.wdlf.inversion.infra.MonteCarloInverter;

public class WldfInverterNonGui {

	/**
	 * The {@link Logger}.
	 */
	private static final Logger logger = Logger.getLogger(WldfInverterNonGui.class.getName());
	
	/**
	 * 
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		// Create an InversionState to encapsulate all inputs and config
		InversionState inversionState = new InversionState();
		
		// Set output location
//		inversionState.outputDirectory = new File("/home/nrowell/Projects/Astronomy/gaia_WDs/Gaia_EDR3_WDLF/results/ex_hyades");
//		inversionState.outputDirectory = new File("/home/nrowell/Projects/Astronomy/gaia_WDs/Gaia_EDR3_WDLF/results/full_wdlf_BASTI");
//		inversionState.outputDirectory = new File("/home/nrowell/Projects/Astronomy/2023.08.07_MarcoLam_WDLF_SFH/results/run3");
//		inversionState.outputDirectory = new File("/home/nrowell/Projects/Astronomy/2023.08.07_MarcoLam_WDLF_SFH/results/parsec_v1.2");
//		inversionState.outputDirectory = new File("/home/nrowell/Projects/Astronomy/2023.08.07_MarcoLam_WDLF_SFH/results/parsec_v2.0");
		inversionState.outputDirectory = new File("/home/nrowell/Projects/Astronomy/2023.08.07_MarcoLam_WDLF_SFH/results/hurley_2000");
		inversionState.writeOutput = true;
		
		// Set input physics
		
		// IMF
//		inversionState.params.setIMF(IMF.POWER_LAW_SALPETER);
		inversionState.params.setIMF(IMF.CHABRIER03);
		
		
		// Pre-WD lifetime models
//		inversionState.params.setPreWdLifetimeModels(PreWdLifetimeModels.PARSECV1p2s);
//		inversionState.params.setPreWdLifetimeModels(PreWdLifetimeModels.PARSECV2p0);
		inversionState.params.setPreWdLifetimeModels(PreWdLifetimeModels.HURLEY);
		
		// Cause discontinuity in WD mass distribution due to noncontinuous first derivative
		inversionState.params.setIFMR(IFMR.CATALAN_2008);
//		inversionState.params.setIFMR(IFMR.KALIRAI_2008);
//		inversionState.params.setIFMR(IFMR.CUMMINGS_2018);
		
		inversionState.params.setWdCoolingModels(WdCoolingModels.MONTREAL_EVOL);
		inversionState.params.setFilter(Filter.M_BOL);
		// Fraction of WDs that have H atmospheres
		inversionState.params.setW_H(1.0);
		// Uncertainty on the magnitude
		inversionState.params.setSigM(0.0);
		// Solar value
		inversionState.params.setMeanMetallicity(0.017);
//		inversionState.params.setMetallicitySigma(0.001);
		inversionState.params.setMetallicitySigma(0.0);
		// From Y = 0.23 + 2.41Z (Flynn 2004; Casagrande et al. 2007)
		inversionState.params.setMeanHeliumContent(0.279);
//		inversionState.params.setHeliumContentSigma(0.001);
		inversionState.params.setHeliumContentSigma(0.0);
		
		// Set parameters of initial guess star formation rate
		double zMean = inversionState.params.getMeanMetallicity();
		double yMean = inversionState.params.getMeanHeliumContent();
		double tMin = inversionState.params.getPreWdLifetime().getPreWdLifetime(zMean, yMean, BaseImf.M_upper)[0];
		
    	// Set input WDLF
//    	File wdlfIn = new File("/home/nrowell/Projects/Astronomy/gaia_WDs/Gaia_EDR3_WDLF/wdlf/equalised-WDLF-256-hp5-maglimp80-vgen-h366pc-grp-nohyads_PROCESSED.txt");
    	File wdlfIn = new File("/home/nrowell/Projects/Astronomy/2023.08.07_MarcoLam_WDLF_SFH/wdlf/wdlf.txt");
    	// Read entire WDLF file into a single String
    	String content = new Scanner(wdlfIn).useDelimiter("\\Z").next();
    	double[][] wdlfData = BaseWdlf.parseWdlfDataFromString(content, false);
    	BaseWdlf wdlfToInvert = new BaseWdlf(wdlfData[0], wdlfData[1], wdlfData[2], wdlfData[3]);
    	wdlfToInvert.setName("GCNS WDLF");
    	wdlfToInvert.setFilter(Filter.M_BOL);

    	inversionState.wdlf_obs = wdlfToInvert;
    	
    	// Set initial guess SFR
    	InitialGuessSFR initialGuessSfr = new InitialGuessSFR(tMin, 1.45E10, 100, 1.5E-12);
    	initialGuessSfr.setLookbackTimeBins();
    	
    	inversionState.currentSfr = initialGuessSfr.copy();
    	
    	// Write inversion state to file
        File output = new File(inversionState.outputDirectory, "wdlf_inversion_config.txt");
        try (BufferedWriter out = new BufferedWriter(new FileWriter(output))) {
            out.write(inversionState.toString());
        }
        catch(IOException ioe) {
            ioe.printStackTrace();
        }
    	
    	// Perform inversion
        performInversion(inversionState);
        
        // Retrieve the final converged star formation rate
        InitialGuessSFR convergedSFR = inversionState.currentSfr.copy();

        // Final output products:
        //  - Data file containing star formation rate and error per time bin, for publishing
        //  - Plot file containing sequence of points for plotting bins
        DiscreteFunction1D data = convergedSFR.data;

        File sfhDataFile = new File(inversionState.outputDirectory, "sfh.txt");
        File sfhPlotFile = new File(inversionState.outputDirectory, "sfhPlotData.txt");
        
        BufferedWriter outData = new BufferedWriter(new FileWriter(sfhDataFile));
        outData.write("# Columns: \n");
        outData.write("# 1) Lookback time bin centre [yr] \n");
        outData.write("# 2) Lookback time bin width  [yr] \n");
        outData.write("# 3) Star formation rate in bin, obtained from original inversion [N yr^{-1} pc^{-3}] \n");
        outData.write("# 4) One-sigma standard deviation on star formation rate in bin, obtained from original inversion [N yr^{-1} pc^{-3}] \n");

        BufferedWriter outPlot = new BufferedWriter(new FileWriter(sfhPlotFile));
        outPlot.write("# Columns: \n");
        outPlot.write("# 1) Lookback time bin alternating lower/upper edge [yr] \n");
        outPlot.write("# 2) Star formation rate in bin, obtained from original inversion [N yr^{-1} pc^{-3}] \n");
        outPlot.write("# 3) One-sigma standard deviation on star formation rate in bin, obtained from original inversion [N yr^{-1} pc^{-3}] \n");
        // Two extra header lines so that it's the same length as the header for the Monte Carlo plot data file
        outPlot.write("#\n");
        outPlot.write("#\n");
        
        // Point at zero at the lower edge of the SFR histogram
        outPlot.write(data.getBinLowerEdge(0) + "\t0\t0\n");
        
        double[] sfrIntegral = {0.0, 0.0};
        
        for(int i=0; i<data.size(); i++) {
        	
        	double binLower = data.getBinLowerEdge(i);
        	double binMid = data.getBinCentre(i);
        	double binUpper = data.getBinUpperEdge(i);
        	double binWidth = data.getBinWidth(i);
        	
        	// SFH value and uncertainty from inversion
        	double val = data.getBinContents(i);
        	double sigma = data.getBinUncertainty(i);
        	
        	// Write data in format for dissemination [one line per bin]
        	outData.write(String.format("%s\t%s\t%s\t%s\n", Double.toString(binMid), Double.toString(binWidth), Double.toString(val), Double.toString(sigma)));
        	
        	// Write data in format for plotting [two lines per bin, point at lower and upper edges]
        	outPlot.write(String.format("%s\t%s\t%s\n", Double.toString(binLower), Double.toString(val), Double.toString(sigma)));
            outPlot.write(String.format("%s\t%s\t%s\n", Double.toString(binUpper), Double.toString(val), Double.toString(sigma)));
        	
        	// Integrate SFR histogram
        	sfrIntegral[0] += data.getBinWidth(i) * data.getBinContents(i);
        	// Uncertainty from propagation of errors inside inversion algorithm
        	sfrIntegral[1] += (data.getBinWidth(i) * data.getBinWidth(i) * data.getBinUncertainty(i) * data.getBinUncertainty(i));
        }
        
        // Include integrated SFR in the output file
        outData.write("Integral of SFR: " + sfrIntegral[0] + "\n");
        outData.write("One-sigma uncertainty from error propagation inside inversion algorithm: " + sfrIntegral[1] + "\n");
        outData.close();
        
        // Point at zero at the upper edge of the SFR histogram
        outPlot.write(data.getBinUpperEdge(data.size()-1) + "\t0\t0\n");
        outPlot.close();
        
        // Plot a simulated white dwarf HRD using the converged star formation rate and other simulation parameters
//		plotHrd(inversionState, WdCoolingModels.MONTREAL_NEW_2020.getWdCoolingModels(), 70000, Filter.G_DR3, Filter.BP_DR3, Filter.RP_DR3);
		
        // Obtain robust estimate of the star formation rate uncertainty from bootstrap resampling of the input WDLF
        resampling(inversionState, initialGuessSfr, wdlfToInvert, 200);
	}
	
	/**
	 * Perform the inversion of the WDLF given the inputs and configuration stored in the {@link InversionState} instance.
	 * 
	 * @param inversionState
	 * 	The {@link InversionState} instance. On exit this will contain the final converged results of the inversion.
	 * @throws IOException 
	 * 	
	 */
	private static void performInversion(InversionState inversionState) throws IOException {
		
		boolean converged = false;
    	
    	// List of the relative change in chi-square with each iteration, for writing out
    	List<Double> relativeChangeAtEachIteration = new LinkedList<>();
    	
    	while(!converged) {
    		
            // Generate a new {@link MonteCarloInverter} with current SFR model.
    		final MonteCarloInverter inversion = new MonteCarloInverter(inversionState);
            
			try {
				inversion.performSingleIteration();
			} catch (IOException e) {
				logger.severe("Encountered IOException when performing single iteration: " + e.getLocalizedMessage());
			}
			
			// Update SFR
        	inversionState.currentSfr = (InitialGuessSFR)inversionState.updatedSfr.copy();
        	
            // Finished iteration. Check for convergence.
            if(inversionState.iterations < inversionState.iterations_min) {
            	String preConvStr = "Iteration %d: don't check for convergence within the first %d iterations.";
            	logger.info(String.format(preConvStr, inversionState.iterations, inversionState.iterations_min));
            }
            else {
            	
            	// Chi-square list now has inversionState.iterations_min items
            	
                Convergence convergence = Convergence.factory(Convergence.Type.SLIDINGLINEAR, inversionState.chi2);
                
                // Get relative change in chi-square with latest iteration
                double chiSqChange = convergence.getRelativeAbsChangeAtLatestIteration();
                relativeChangeAtEachIteration.add(chiSqChange);
                
                String iterUpdStr = "Iteration %d: chi2 = %4.2f - fractional change of %5.5f %% in smoothed chi^2 function";
                logger.info(String.format(iterUpdStr, inversionState.iterations, inversionState.getLastChi2(), chiSqChange*100));
        
                // Test for convergence and reset flag.
                if(convergence.hasConverged(inversionState.chi2Threshold)) {
                	
                	logger.info("Converged!\n");
                	
                	if(inversionState.writeOutput) {
	                	// Write out chi-squared trend and the convergence detection function
	                    File output = new File(inversionState.outputDirectory, "chi2.txt");
	                    BufferedWriter out = new BufferedWriter(new FileWriter(output));
	                    
	                    convergence.toString();
	                    
	                    out.write("# ITERATION\tCHI2\tFIT\tREL_CHANGE\n");
	                    for(int i=0; i<inversionState.iterations; i++) {
	                    	if(i<inversionState.iterations_min-1) {
	                    		// Points two to inversionState.iterations_min-1: the relative change list doesn't know about them
	                    		out.write(String.format("%d\t%.3f\t%.3f\t0.0\n", i, inversionState.chi2.get(i), convergence.getChiSquare(i)));
	                    	}
	                    	else {
	                    		out.write(String.format("%d\t%.3f\t%.3f\t%.3f\n", i, inversionState.chi2.get(i), convergence.getChiSquare(i), 
	                    				relativeChangeAtEachIteration.get(i-inversionState.iterations_min+1)));
	                    	}
	                    }
	                    out.close();
                	}
                	
                    converged = true;
                }
            }
    	}
    	
    	return;
	}
	
	/**
	 * Plot the white dwarf HRD produced from the star formation rate and other parameters stored in the
	 * {@link InversionState}.
	 * 
	 * @param inversionState
	 * @throws IOException 
	 */
	private static void plotHrd(InversionState inversionState, WdCoolingModelSet wdColours, int nWds, Filter f1, Filter f2, Filter f3) throws IOException {

    	// Simulate 70000 WDs and plot HRD
        logger.info("Simulating HRD for "+nWds+" WDs...");
        File output = new File(inversionState.outputDirectory, "hrd.txt");
        BufferedWriter out = new BufferedWriter(new FileWriter(output));
        
        Random random = new Random(4398379L);
        for(int nWd=0; nWd<nWds; ) {
        	
        	// Draw progenitor formation time and mass
        	double totalAge = inversionState.currentSfr.drawCreationTime();
    		double progenitorMass = inversionState.params.getIMF().drawMass();
    		double z = -Double.MAX_VALUE;
    		double y = -Double.MAX_VALUE;
    		while(z<=0.0) {
    			// Redraw until we get a positive value
    			z = inversionState.params.getMeanMetallicity() + random.nextGaussian() * inversionState.params.getMetallicitySigma();
    		}
    		while(y<=0.0) {
    			// Redraw until we get a positive value
    			y = inversionState.params.getMeanHeliumContent() + random.nextGaussian() * inversionState.params.getHeliumContentSigma();
    		}
    		
        	double msLifetime = inversionState.params.getPreWdLifetime().getPreWdLifetime(z, y, progenitorMass)[0];

        	double coolingTimeWD = totalAge - msLifetime;
        	
        	if(coolingTimeWD > 0) {
        		
        		// Star has formed a WD at the present day
        		nWd++;
        	
        		// Get final WD mass
        		double wdMass = inversionState.params.getIFMR().getMf(progenitorMass);
        		
        		// Assign WD atmosphere type randomly
        		WdAtmosphereType wdAtmType = (random.nextDouble() < inversionState.params.getW_H()) ? WdAtmosphereType.H : WdAtmosphereType.He;
        		
        		// Get G,BP,RP magnitudes
                double g = wdColours.quantity(coolingTimeWD, wdMass, wdAtmType, f1);
                double bp = wdColours.quantity(coolingTimeWD, wdMass, wdAtmType, f2);
                double rp = wdColours.quantity(coolingTimeWD, wdMass, wdAtmType, f3);
        		
        		out.write(g + "\t" + bp + "\t" + rp + "\t" + wdAtmType + "\t" + wdMass + "\n");
        	}
        }
        out.close();
    }
	
	/**
	 * This method is used to make robust estimates of the uncertainty on the star formation history obtained
	 * from an inversion of the input {@link BaseWdlf}. This gives a measure of the uncertainty that is an alternative
	 * to the one obtained within the inversion algorithm itself by propagation the variance.
	 * 
	 * 
	 * This is achieved by making many resamplings of the luminosity function to obtain many alternative realisations
	 * of the input WDLF; these are then inverted, and the distribution of the resulting star formation rates are used
	 * to derive robust estimates of the spread in star formation rate as a function of time given the uncertainty on
	 * the input WDLF.
	 * 
	 * @param inversionState
	 * 	The {@link InversionState} instance containing all the configuration and modelling parameters etc.
	 * @param initialGuessSfr
	 * 	The {@link InitialGuessSFR} containing the chosen initial guess star formation rate.
	 * @param wdlfToInvert
	 * 	The {@link BaseWdlf} containing the white dwarf luminosity function (WDLF) to be inverted.
	 * @param numMonteCarlo
	 * 	The number of Monte Carlo resamplings of the WDLF to be drawn and inverted to derive statistics for the resulting
	 * star formation rate.
	 * @throws IOException
	 * 	If there's a problem writing the outputs.
	 */
	private static void resampling(InversionState inversionState, InitialGuessSFR initialGuessSfr, BaseWdlf wdlfToInvert, int numMonteCarlo) throws IOException {

        // Create directory to store the Monte Carlo products
    	File monteCarloDir = new File(inversionState.outputDirectory, "monteCarloSfh");
    	monteCarloDir.mkdir();
    	
    	BufferedWriter outSfh = new BufferedWriter(new FileWriter(new File(monteCarloDir, "MonteCarlo_SFHs.txt")));
    	BufferedWriter outWdlf = new BufferedWriter(new FileWriter(new File(monteCarloDir, "MonteCarlo_WDLFs.txt")));
    	
    	// Don't write all the intermediate outputs; we'll just keep the converged SFH
    	inversionState.writeOutput = false;
        
        InitialGuessSFR[] sfrs = new InitialGuessSFR[numMonteCarlo];
        
        for(int i=0; i<numMonteCarlo; i++) {
        	
        	logger.info("Monte Carlo realisation " + (i+1));
        	
	        // Reset InversionState
	    	inversionState.currentSfr = initialGuessSfr.copy();
	    	inversionState.iterations = 0;
	    	inversionState.chi2.clear();
			
	    	// Resample the input WDLF
	    	BaseWdlf resampledWdlf = new BaseWdlf(wdlfToInvert, true);
	    	inversionState.wdlf_obs = resampledWdlf;
			
	    	// Perform inversion
	        performInversion(inversionState);
			
	        sfrs[i] = inversionState.currentSfr.copy();
	        
			// Save the resampled WDLF and resulting SFH to file
	        for(int j=0; j<sfrs[i].N; j++) {
	        	outSfh.write(sfrs[i].data.getBinCentre(j) + "\t" + sfrs[i].data.getBinContents(j) + "\n");
	        }
	        outSfh.write("\n\n");
	        outSfh.flush();
	        
	        for(int j=0; j<resampledWdlf.size(); j++) {
	        	outWdlf.write(resampledWdlf.density.getBinCentre(j) + "\t" + resampledWdlf.density.getBinContents(j) + "\n");
	        }
	        outWdlf.write("\n\n");
	        outWdlf.flush();
        }
        outSfh.close();
        outWdlf.close();
        
        // Estimates of uncertainty on SFH from spread in values in the Monte Carlo realisations:
        // 1) Scaled median-of-absolute-deviations
        double[] mcMad = new double[initialGuessSfr.N];
        // 2) Sample standard deviation
        double[] mcStd = new double[initialGuessSfr.N];
        
        // Mean and median values of the SFH from the Monte Carlo run
        double[] mcMeanSfh = new double[initialGuessSfr.N];
        double[] mcMedianSfh = new double[initialGuessSfr.N];
        
        // Loop over each lookback time bin
        for(int j=0; j<initialGuessSfr.N; j++) {
        	
        	FloatList values = new FloatList();
        	
        	// Loop over each Monte Carlo realisation
        	for(int i=0; i<numMonteCarlo; i++) {
        		values.add((float) sfrs[i].data.getBinContents(j));
        	}
        	
        	// Now estimate one-sigma standard deviation using MAD
        	float[] mad = values.getMAD();
        	mcMedianSfh[j] = mad[0];
        	mcMad[j] = 1.4826 * mad[1];
        	mcMeanSfh[j] = values.getMean();
        	mcStd[j] = values.getStd();
        }
        
        // Final output products:
        //  - Data file containing star formation rate and error per time bin, for publishing
        //  - Plot file containing sequence of points for plotting bins
        DiscreteFunction1D data = initialGuessSfr.data;
        
        File sfhDataFile = new File(monteCarloDir, "MonteCarlo_sfh.txt");
        File sfhPlotFile = new File(monteCarloDir, "MonteCarlo_sfhPlotData.txt");

        BufferedWriter outData = new BufferedWriter(new FileWriter(sfhDataFile));
        outData.write("# Columns: \n");
        outData.write("# 1) Lookback time bin centre [yr] \n");
        outData.write("# 2) Lookback time bin width  [yr] \n");
        outData.write("# 3) Median star formation rate obtained from " + numMonteCarlo + " resamplings of the WDLF [N yr^{-1} pc^{-3}] \n");
        outData.write("# 4) Mean star formation rate obtained from " + numMonteCarlo + " resamplings of the WDLF [N yr^{-1} pc^{-3}] \n");
        outData.write("# 5) Median-of-absolute-deviations (* 1.4826) of the star formation rate obtained from " + numMonteCarlo + " resamplings of the WDLF [N yr^{-1} pc^{-3}] \n");
        outData.write("# 6) Sample standard deviation of the star formation rate obtained from " + numMonteCarlo + " resamplings of the WDLF [N yr^{-1} pc^{-3}] \n");

        BufferedWriter outPlot = new BufferedWriter(new FileWriter(sfhPlotFile));
        outPlot.write("# Columns: \n");
        outPlot.write("# 1) Lookback time bin alternating lower/upper edge [yr] \n");
        outPlot.write("# 2) Median star formation rate obtained from " + numMonteCarlo + " resamplings of the WDLF [N yr^{-1} pc^{-3}] \n");
        outPlot.write("# 3) Mean star formation rate obtained from " + numMonteCarlo + " resamplings of the WDLF [N yr^{-1} pc^{-3}] \n");
        outPlot.write("# 4) Median-of-absolute-deviations (* 1.4826) of the star formation rate obtained from " + numMonteCarlo + " resamplings of the WDLF [N yr^{-1} pc^{-3}] \n");
        outPlot.write("# 5) Sample standard deviation of the star formation rate obtained from " + numMonteCarlo + " resamplings of the WDLF [N yr^{-1} pc^{-3}] \n");
        
        // Point at zero at the lower edge of the SFR histogram
        outPlot.write(data.getBinLowerEdge(0) + "\t0\t0\t0\t0\n");
        
        for(int i=0; i<data.size(); i++) {
        	
        	double binLower = data.getBinLowerEdge(i);
        	double binMid = data.getBinCentre(i);
        	double binUpper = data.getBinUpperEdge(i);
        	double binWidth = data.getBinWidth(i);
        	
        	// Write data in format for dissemination [one line per bin]
        	outData.write(String.format("%s\t%s\t%s\t%s\t%s\t%s\n", Double.toString(binMid), Double.toString(binWidth), Double.toString(mcMedianSfh[i]), Double.toString(mcMeanSfh[i]), Double.toString(mcMad[i]), Double.toString(mcStd[i])));
        	
        	// Write data in format for plotting [two lines per bin, point at lower and upper edges]
        	outPlot.write(String.format("%s\t%s\t%s\t%s\t%s\n", Double.toString(binLower), Double.toString(mcMedianSfh[i]), Double.toString(mcMeanSfh[i]), Double.toString(mcMad[i]), Double.toString(mcStd[i])));
            outPlot.write(String.format("%s\t%s\t%s\t%s\t%s\n", Double.toString(binUpper), Double.toString(mcMedianSfh[i]), Double.toString(mcMeanSfh[i]), Double.toString(mcMad[i]), Double.toString(mcStd[i])));
        }
        
        outData.close();
        
        // Point at zero at the upper edge of the SFR histogram
        outPlot.write(data.getBinUpperEdge(data.size()-1) + "\t0\t0\t0\t0\n");
        outPlot.close();
	}
}
