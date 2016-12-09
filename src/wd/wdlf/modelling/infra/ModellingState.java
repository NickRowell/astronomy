package wd.wdlf.modelling.infra;


import infra.os.OSChecker;
import numeric.functions.MonotonicLinear;
import sfr.algo.BaseSfr;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import survey.SurveyVolume;
import wd.wdlf.algoimpl.ModelWDLF;
import wd.wdlf.dm.State;

/**
 * ModellingState objects contain state variables of WDLF modelling algorithm.
 *
 * @author nrowell
 * @version $Id$
 */
public class ModellingState extends State {
    
    // A couple of enumerated type definitions for basic survey options
    
    /**
     * Types of WDLF solver. This isn't used currently - only Monte Carlo supported.
     */
    public static enum SolverType {
    	TRAPEZIUM,
    	MONTECARLO
    };
    
    /**
     * Types of WDLF survey.
     */
    public static enum SurveyType {
    	VOLUME_LIMITED,
    	MAGNITUDE_LIMITED
    };
    
    // A few fields that are not user configurable
    
    /**
     * MonotonicLinear object used to compute cumulative survey volume as a function of distance, generalised
     * for the density profile of an exponential disk. Used to calculate magnitude limited samples.
     */
    public MonotonicLinear surveyVolume = SurveyVolume.getSurveyVolume(250, Math.toRadians(30));
    
    /**
     * Apparent magnitude limit, used to derive V_max for simulated stars.
     */
    public double apparentMagLimit = 20;
    
    // The remaining parameters are set interactively through GUI
    
    /**
     * Synthetic WDLF object.
     */
    public ModelWDLF syntheticWDLF;     
    
    /**
     * Input star formation rate function.
     */
    public BaseSfr syntheticSFR = null;
    
    /**
     * Solver type {MONTECARLO|TRAPEZIUM}. Not used at present (Monte Carlo).
     */
    public SolverType SOLVER = SolverType.MONTECARLO;
    
    /**
     * Number of simulation WDs used in Monte Carlo simulations.
     */
    public long n_WDs = 20000;
    
    /**
     * Survey Type {MAGNITUDE_LIMITED|VOLUME_LIMITED}.
     */
    public SurveyType surveyType = SurveyType.VOLUME_LIMITED;
    
    /**
     * Centres of magnitude bins.Note that these are the desired bins,
     * if during modelling no stars fall in a given bin, then it will be
     * omitted from the ObservedWDLF.
     */
    public double[] wdlfBinCentres = new double[]{ 4.0, 4.5, 5.0, 5.5, 6.0,
                                                6.5, 7.0, 7.5, 8.0, 8.5,
                                                9.0, 9.5,10.0,10.5,11.0,
                                               11.5,12.0,12.5,13.0,13.5,
                                               14.0,14.5,15.0,15.5,16.0,
                                               16.5,17.0};
    /**
     * Widths of magnitude bins.
     */
    public double[] wdlfBinWidths  = new double[]{0.5, 0.5, 0.5, 0.5, 0.5,
                                               0.5, 0.5, 0.5, 0.5, 0.5,
                                               0.5, 0.5, 0.5, 0.5, 0.5,
                                               0.5, 0.5, 0.5, 0.5, 0.5,
                                               0.5, 0.5, 0.5, 0.5, 0.5,
                                               0.5, 0.5};
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        
        // Get date & time for header
        Date       date   = new Date();
        DateFormat header = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
    
        StringBuilder out = new StringBuilder();
        
        out.append("# WDLF simulation output.").append(OSChecker.newline);
        out.append("# ").append(header.format(date)).append(OSChecker.newline);
        out.append("# platform = ").append(OSChecker.getOS().toString()).append(OSChecker.newline);
        out.append("# Output directory = ").append((outputDirectory!=null) ? outputDirectory.toString() : "null").append(OSChecker.newline);
        out.append("# Modelling Parameters:").append(OSChecker.newline).append(params.toString());
        out.append("# Survey Type: ").append(surveyType.toString()).append(OSChecker.newline);
        out.append("# Number of stars: ").append(n_WDs).append(OSChecker.newline);
        out.append("# Simulated WDLF:").append(OSChecker.newline);
        out.append("#   column 1: bin centre [bolometric magnitude]").append(OSChecker.newline);
        out.append("#   column 2: bin width [bolometric magnitude]").append(OSChecker.newline);
        out.append("#   column 3: total number density [N/mag] (i.e. the luminosity function)").append(OSChecker.newline);
        out.append("#   column 4: standard deviation on total number density [N/mag]").append(OSChecker.newline);
        out.append("#   column 5: mean WD mass [M_{solar}]").append(OSChecker.newline);
        out.append("#   column 6: standard deviation of WD mass [M_{solar}]").append(OSChecker.newline);
        out.append("#   column 7: mean total stellar age [years] (sum of WD cooling time and MS lifetime)").append(OSChecker.newline);
        out.append("#   column 8: standard deviation of total stellar age [years]").append(OSChecker.newline);
        out.append(syntheticWDLF.toString()).append(OSChecker.newline);
        //out.append("Solver Type: ").append(SOLVER.toString()).append("\n");
        out.append("# SFR basic type: ").append(syntheticSFR.getName()).append(OSChecker.newline);
        out.append("# SFR function (time/rate/error):").append(OSChecker.newline).append(syntheticSFR.toString());
        
        return out.toString();
    }
    
    /**
     * Save current state to disk.
     */
    public void saveToDisk() throws IOException
    {
        BufferedWriter out = new BufferedWriter(new FileWriter(new File(outputDirectory,"wdlf_simulation.txt")));
        out.write(toString());
        out.close();
    }
}