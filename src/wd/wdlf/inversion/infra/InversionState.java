package wd.wdlf.inversion.infra;


import java.util.LinkedList;
import java.util.List;

import infra.os.OSChecker;
import sfr.algoimpl.InitialGuessSFR;
import wd.wdlf.algo.BaseWdlf;
import wd.wdlf.algoimpl.ObservedWdlf;
import wd.wdlf.dm.State;

/**
 * Class contains all variables related to inversion algorithm.
 * 
 * @author nickrowell
 */
public class InversionState extends State
{
    
    // Parameters set by WelcomeForm
    
    /**
     * Should all inversion results be written to disk?.
     */
    public boolean writeOutput = false;
    
    // Parameters set by ObsWDLFInputForm
    
    /**
     * The observed WDLF to invert.
     */
    public BaseWdlf wdlf_obs = ObservedWdlf.KrzesinskiAndHarris.wdlf;
    
    /**
     * Initial guess star formation rate model.
     */
    public InitialGuessSFR currentSfr = new InitialGuessSFR(0, 14.5E9, 50, 1.5E-12);
    
    /**
     * This stores the updated SFR following each iteration. It is then copied to the
     * {@link #currentSfr} prior to the next iteration.
     */
    public InitialGuessSFR updatedSfr = null;
    
    /**
     * Set number of simulation white dwarfs.
     */
    public int n_WDs = 2000000;
    
    /** 
     * This is the function p(M_{bol}) calculated from the input star formation
     * rate. Initially set to null, then initialised when the first iteration 
     * is completed.
     */
    public BaseWdlf wdlf_model = null;       
    
    /**
     * List of chi-square statistic for each iteration of the algorithm.
     */
    public List<Double> chi2 = new LinkedList<Double>();   
    
    /**
     * Count number of iterations.
     */
    public int iterations = 0;      
    
    /**
     * Minimum number of iterations for convergence.
     */
    public int iterations_min = 5;
    
    /** 
     * Relative changes in chi-square from one iteration to the next lower
     * than this amount indicate that the algorithm has converged.
     * 
     * 0.05 = 5% change
     * 
     */
    public double chi2Threshold = 0.01;
    
    /**
     * Get most recent chi-square.
     * @return
     * 	The chi-squared value from the most recent iteration.
     */
    public double getLastChi2() {
    	return chi2.get(chi2.size()-1);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString(){
    
        StringBuilder out = new StringBuilder();
        
        out.append("platform = ").append(OSChecker.getOS().toString()).append("\n");
        out.append("parentOutputDirectory = ").append((outputDirectory!=null) ? outputDirectory.toString() : "null").append("\n");
        out.append("Writing output to disk? ").append(writeOutput ? "yes\n" : "no\n");
        out.append("Modelling Parameters:\n").append(params.toString());
        out.append("Observed WDLF = ").append(wdlf_obs.name).append("\n");
        out.append("Initial guess SFR parameters:\n").append(currentSfr.printParameters());
        
        return out.toString();
    }
    
}