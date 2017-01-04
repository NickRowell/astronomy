package wd.wdlf.inversion.util;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import ifmr.algo.BaseIfmr;
import imf.algo.BaseImf;
import infra.io.Gnuplot;
import infra.os.OSChecker;
import ms.lifetime.algo.PreWdLifetime;
import numeric.data.Histogram;
import numeric.data.Histogram2D;
import numeric.data.Range;
import numeric.data.RangeMap;
import photometry.Filter;
import util.CharUtil;
import wd.models.algo.WdCoolingModelSet;
import wd.models.infra.WdAtmosphereType;
import wd.wdlf.dm.Star;
import wd.wdlf.dm.WdlfModellingParameters;
import wd.wdlf.inversion.infra.InversionState;

/**
 * Plotting utilities associated with the WDLF inversion algorithm.
 *
 *
 * @author nrowell
 * @version $Id$
 */
public class InversionPlotUtil {
	
	/**
	 * The logger.
	 */
	public static final Logger logger = Logger.getLogger(InversionPlotUtil.class.getName());
	
	/**
     * Names for temporary data files produced by P_MS.
     */
	
	// Data files associated with progenitors
    private static final String pmsFilename = "P_MS";
    private static final String progenitorMassDistFileName = "progenitor_mass_distribution";
    private static final String initialSfrFilename = "initialSFR";
    private static final String updatedSfrFilename = "updatedSFR";
    
    // Data files associated with white dwarfs
    private static final String pwdFileName = "P_WD";
    private static final String wdMassDistFileName = "WD_mass_distribution";
    private static final String obsWDLFFileName = "obsWDLF";
	private static final String modelWDLFFileName = "modelWDLF";
    
    // Plot annotations associated with progenitors
    private static final String msBoundaryFilename = "msBoundary";
    private static final String lowMsMassFilename = "lowMSMass";
    private static final String obsWdsFractionFilename = "observedWDsFraction";
    
	// Plot annotations associated with white dwarfs
	private static final String maxWDMassFileName = "maxWDMass";
	private static final String minHWDMassFileName = "minHWDMass";
	private static final String minHWDMbolFileName = "minHWDMbol";
	private static final String minHeWDMassFileName = "minHeWDMass";
	private static final String minHeWDMbolFileName = "minHeWDMbol";
	
	// Gnuplot scripts used to produce each plot, and the plots themselves
    private static final String pmsPlotScriptFilename = "pms.gnuplot";
    private static final String pwdPlotScriptFilename = "pwd.gnuplot";
    private static final String pmsPlotFilename = "pms.png";
    private static final String pwdPlotFilename = "pwd.png";
	
    /**
	 * Step size in WD mass function for plotting [M_{Solar}]. Note that the step size
	 * used is trimmed down to fit a whole number of steps within the defined range.
	 */
	private static final double wdMassFunctionStepSize = 0.01;
	
	/**
	 * Step size in WD luminosity function for plotting [mag]. Note that the step size
	 * used is trimmed down to fit a whole number of steps within the defined range.
	 */
	private static final double wdLuminosityFunctionStepSize = 0.1;
	
	/**
	 * Step size in progenitor mass function for plotting [M_{Solar}]. Note that the step size
	 * used is trimmed down to fit a whole number of steps within the defined range.
	 */
	private static final double progenitorMassFunctionStepSize = 0.05;
	
	/** 
	 * Step size in progenitor formation time function for plotting [yr]. Note that the step size
	 * used is trimmed down to fit a whole number of steps within the defined range.
	 */
	private static final double progenitorFormationTimeFunctionStepSize = 1e8;
	
	/**
     * Produces the diagnostic plot P_{MS}: the joint distribution of progenitor
     * mass and formation time.
     * 
	 * @param parent
	 * 	The path to the directory in which to save the output files.
	 * @param inversionState
     * 	The main {@link InversionState} instance that encapsulates all the modelling parameters and
     * other quantities.
	 * @param progenitors
     * 	The {@link RangeMap<Star>} containing the stars to process.
	 * @return
	 * 	A {@link BufferedImage} containing the diagnostic plot P_{MS}: the joint distribution of progenitor
     * mass and formation time.
	 * @throws IOException
	 * 	If there's an exception when writing data files.
	 */
    public static BufferedImage getPms(File parent, InversionState inversionState, RangeMap<Star> progenitors) throws IOException {
    	
    	// Get a reference to the {@link WdlfModellingParameters} for convenience
    	WdlfModellingParameters params = inversionState.params;
    	double tMax = inversionState.currentSfr.t_max;
    	double tMin = inversionState.currentSfr.t_min;
    	
        /////////////////////////////////////////////////////////////////////////
        //                                                                     //
        //        Write initial & updated Star Formation Rate functions        //
        //                                                                     //
        /////////////////////////////////////////////////////////////////////////
    	
        // Write initial SFR
        BufferedWriter out = new BufferedWriter(new FileWriter(new File(parent, initialSfrFilename)));
        out.write(inversionState.currentSfr.toString());
        double[] total_stars = inversionState.currentSfr.integrateSFR();
        out.write("\n\n# Integral = "+total_stars[0]+" +/- "+total_stars[1]);
        out.close();
        
        // Write updated SFR (won't exist until after inversion has started, so need to check
        // it's not null first or the initial pre-inversion plot will crash)
        out = new BufferedWriter(new FileWriter(new File(parent, updatedSfrFilename)));
        if(inversionState.updatedSfr != null) {
	        out.write(inversionState.updatedSfr.toString());
	        total_stars = inversionState.updatedSfr.integrateSFR();
	        out.write("\n\n# Integral = "+total_stars[0]+" +/- "+total_stars[1]);
        }
        out.close();
        
        /////////////////////////////////////////////////////////////////////////
        //                                                                     //
        //       Build 2D distribution of progenitor mass and magnitude,       //
        //             and 1D marginal distributions                           //
        //                                                                     //
        /////////////////////////////////////////////////////////////////////////
        
        double z = params.getMeanMetallicity();
        double y = params.getMeanHeliumContent();
        
        // Figure out low end of mass range:
        double low_mass_limit = params.getPreWdLifetime().getStellarMass(z, y, tMax)[0];
        
        double minX = tMin/1.0E9;
        double maxX = tMax/1.0E9;
        double dX = (maxX - minX) / Math.ceil((maxX - minX) / (progenitorFormationTimeFunctionStepSize/1.0E9));
        double minY = low_mass_limit;
        double maxY = BaseImf.M_upper;
        double dY = (maxY - minY) / Math.ceil((maxY - minY) / progenitorMassFunctionStepSize);
        
        
        // Configures normalisation of the mass distribution
        boolean normalise = true;
        double integral = 1.0;
        
        Histogram progenitorMassDistribution = new Histogram(minY, maxY, dY, true);
        double progenitorMassFunctionPeak = 1.0;
        Histogram2D msMassFormationTimeDistribution = new Histogram2D(minX, maxX, dX, minY, maxY, dY, true);
        double zmax = 1.0;
        
        if(progenitors!=null) {
        	for(int bin=0; bin<progenitors.size(); bin++) {
                // Loop over all stars in bin
                for (Star star : progenitors.get(bin)) {
                    msMassFormationTimeDistribution.add(star.getTotalAge()/1.0E9, star.getProgenitorMass(), star.getNumber()/(dX*dY));
                    progenitorMassDistribution.add(star.getProgenitorMass(), star.getNumber());
                }
            }
        	zmax = msMassFormationTimeDistribution.getMax();
        	progenitorMassFunctionPeak = progenitorMassDistribution.getMax();
        	integral = normalise ? progenitorMassDistribution.integrate() : 1.0;
        	progenitorMassFunctionPeak /= integral;
        }
        
        out = new BufferedWriter(new FileWriter(new File(parent, pmsFilename)));
        out.write(msMassFormationTimeDistribution.print(false));
        out.close();
        
        out = new BufferedWriter(new FileWriter(new File(parent, progenitorMassDistFileName)));
        
        for(int i=0; i<progenitorMassDistribution.getNumberOfBins(); i++) {
        	double binCentre = progenitorMassDistribution.getBinCentre(i);
        	double initMassDist = params.getIMF().getIMF(binCentre);
        	double updMassDist = progenitorMassDistribution.getBinContents(i)/(integral);
        	out.write(binCentre + "\t" + initMassDist + "\t" + updMassDist + "\n");
        }
        out.close();
        
        /////////////////////////////////////////////////////////////////////////
        //                                                                     //
        //                   Write plot annotations to file                    //
        //                                                                     //
        /////////////////////////////////////////////////////////////////////////
        
        // File contains a list of points all the way round the boundary of the populated
        // region of the progenitor mass/age plane; this is used to draw a blue line marking
        // the extent of this region.
        out = new BufferedWriter(new FileWriter(new File(parent, msBoundaryFilename)));
        for(double[] point : getMinMSMassRelation(params, tMax, tMin)) {
            out.write(point[0]+"\t"+point[1]+OSChecker.newline);
        }
        // Add point at tMax,M_{max}
        out.write(tMax/1.0E9 + "\t" + BaseImf.M_upper + OSChecker.newline);
        // Add point at tMin,M_{max} to complete loop
        out.write(tMin/1.0E9 + "\t" + BaseImf.M_upper + OSChecker.newline);
        out.close();
        
        // File contains a list of points lying along the low mass boundary of the populated region
        // of the Progenitor mass/age plane. This is used to draw a filled polygon in this region.
        out = new BufferedWriter(new FileWriter(new File(parent, lowMsMassFilename)));
        for(double[] point : getMinMSMassRelation(params, tMax, tMin)) {
            out.write(point[0]+"\t"+point[1]+OSChecker.newline);
        }
        out.close();
        
        out = new BufferedWriter(new FileWriter(new File(parent, obsWdsFractionFilename)));
        if(progenitors==null) {
        	out.write("0 0 0");
        }
        else {
        	for(int bin=0; bin<progenitors.size(); bin++) {
        		
        		// Get the fraction of observed WDs in this bin
        		double nObsWds = 0.0;
        		for(Star star : progenitors.get(bin)) {
        			if(star.getIsObserved()) {
        				nObsWds++;
        			}
        		}
        		double fracObsWds = nObsWds / progenitors.size();
        		
        		Range range = progenitors.getRange(bin);
        		
        		out.write(range.mid() + "\t" + fracObsWds + "\t" +
        				InversionUtil.getFractionWDProgenitorsInTimeRange(range.lower, range.upper, params)+"\n");
        	}
        }
        out.close();
        
        
		//////////////////////////////////////////////////////////////////////////////
		//                                                                          //
		//                  Construct Gnuplot script                                //
		//                                                                          //
		//////////////////////////////////////////////////////////////////////////////
        
        double ymax = inversionState.currentSfr.getMaxRate();
        if(inversionState.updatedSfr != null) {
        	// If inversion is underway and we have an updated SFR, then set axis range to fit this
        	ymax = inversionState.updatedSfr.getMaxRate();
        }
        int yexp = (int)Math.floor(Math.log10(ymax));
        
        // Maximum colour box. Set 5 contours evenly distributed in this range
        int zexp = (int)Math.floor(Math.log10(zmax));
        
        StringBuilder script = new StringBuilder();

        //////////////////////////////////////////////////////////////////////////////
        //                                                                          //
        //                  Set up the terminal                                     //
        //                                                                          //
        //////////////////////////////////////////////////////////////////////////////
        
        script.append("solarMass = \"M_{"+CharUtil.solar+"}\"").append(OSChecker.newline);
        script.append("set terminal pngcairo enhanced color crop size 1024,1024").append(OSChecker.newline);
        
        script.append("set style line 1 lt 2 lc rgb \"blue\" lw 1").append(OSChecker.newline);     // Style used to draw MS boundary line
        script.append("set style line 2 lt 1 lc rgb \"grey\" lw 1").append(OSChecker.newline);     // Style used to fill in grey region below low mass MS line
        script.append("set style line 3 lt 1 lc rgb \"purple\" lw 1").append(OSChecker.newline);   // Style for confidence region on inverted SFR
        // Style used for mass function (updated)
        script.append("set style line 4 lt 3 lc rgb \"blue\" lw 1").append(OSChecker.newline);
        // Style used for mass function (initial)
        script.append("set style line 5 lt 3 lc rgb \"grey\" lw 1").append(OSChecker.newline);
        
//        script.append("set size nosquare").append(OSChecker.newline);
        script.append("set multiplot").append(OSChecker.newline);
        script.append("set key off").append(OSChecker.newline);
        script.append("set tics scale 0.5").append(OSChecker.newline);
        
        //////////////////////////////////////////////////////////////////////////////
        //                                                                          //
        //                Plot the marginal mass distribution                       //
        //                                                                          //
        //////////////////////////////////////////////////////////////////////////////

    	double[] progenitor_mass_range = {0.0, BaseImf.M_upper};
    	
        // Margins for mass distribution plot
        script.append("set lmargin at screen 0.15").append(OSChecker.newline);
        script.append("set rmargin at screen 0.23").append(OSChecker.newline);
        script.append("set bmargin at screen 0.55").append(OSChecker.newline);
        script.append("set tmargin at screen 0.95").append(OSChecker.newline);
        
        // X axis
        script.append("set xrange [0:"+(progenitorMassFunctionPeak*1.2)+"] reverse").append(OSChecker.newline);
        script.append("set xtics "+getTicInterval(0, progenitorMassFunctionPeak*1.2)+" in").append(OSChecker.newline);
        script.append("set xtics format ''").append(OSChecker.newline);
        
        // Y axis
        script.append("set yrange ["+progenitor_mass_range[0]+":"+progenitor_mass_range[1]+"]").append(OSChecker.newline);
        script.append("set ytics 1").append(OSChecker.newline);
        script.append("set mytics 2").append(OSChecker.newline);
        script.append("set ylabel \"{/"+OSChecker.getFont()+"=14 Progenitor Mass [\".solarMass.\"]}\" offset 0,0").append(OSChecker.newline);
        
        // Plot annotations
        script.append("set label 1 'initial' at "+progenitorMassFunctionPeak+", graph 0.7 rotate font ',8' tc rgbcolor 'grey'").append(OSChecker.newline);
        script.append("set label 2 'updated' at "+0.8*progenitorMassFunctionPeak+", graph 0.7 rotate font ',8' tc rgbcolor 'blue'").append(OSChecker.newline);
        
        // Plot progenitor mass distribution
        script.append("plot '" + parent.getPath() + OSChecker.pathSep + progenitorMassDistFileName + "' u 2:1 w l ls 5,\\").append(OSChecker.newline);
        script.append("     '" + parent.getPath() + OSChecker.pathSep + progenitorMassDistFileName + "' u 3:1 w l ls 4").append(OSChecker.newline);
        
        // Tidy up
        script.append("unset label 1").append(OSChecker.newline);
        script.append("unset label 2").append(OSChecker.newline);
        
        //////////////////////////////////////////////////////////////////////////////
        //                                                                          //
        //             Plot the joint mass/formation time distribution              //
        //                                                                          //
        //////////////////////////////////////////////////////////////////////////////
        
        script.append("set view map").append(OSChecker.newline);
        
        // Margins for joint mass/formation time distribution plot
        script.append("set lmargin at screen 0.25").append(OSChecker.newline);
        script.append("set rmargin at screen 0.75").append(OSChecker.newline);
        script.append("set bmargin at screen 0.55").append(OSChecker.newline);
        script.append("set tmargin at screen 0.95").append(OSChecker.newline);
        
        // X axis
        script.append("set xrange [0:"+(tMax/1E9)+"] noreverse").append(OSChecker.newline);
        script.append("unset xtics").append(OSChecker.newline);
        script.append("unset xlabel").append(OSChecker.newline);
        
        // Y axis
        script.append("unset ytics").append(OSChecker.newline);
        script.append("unset ylabel").append(OSChecker.newline);
        
        // Z axis
        script.append("set cblabel \"Density\\n[N ({/Symbol \\264}10^{"+zexp+"}) yr^{-1} \".solarMass.\"^{-1}]\" font '"+OSChecker.getFont()+",14' offset 0,0").append(OSChecker.newline);
        script.append("set cbtics 1 font \""+OSChecker.getFont()+",12\" nomirror out").append(OSChecker.newline);
        script.append("set mcbtics 2").append(OSChecker.newline);
        script.append("set palette defined (0 \"white\", 0.0001 \"yellow\", 0.001 \"orange\", 0.01 \"red\", 0.1 \"black\")").append(OSChecker.newline);
        // Contour parameters
//        double cbmax = Math.ceil(zmax*Math.pow(10,-zexp));
//        script.append("set cont base").append(OSChecker.newline);
//        script.append("unset clabel").append(OSChecker.newline);
//        script.append("set cntrparam levels discrete "+0.1*cbmax+","+0.2*cbmax+","+0.5*cbmax+","+0.8*cbmax+"").append(OSChecker.newline);
//        script.append("set cntrparam linear").append(OSChecker.newline);
        
        // Use rectangle to fill unpopulated region of plane with grey background.
        script.append("set object 1 rectangle from graph 0,0 to graph 1,1 fillcolor rgb \"grey\" behind ").append(OSChecker.newline);
        script.append("splot '" + parent.getPath() + OSChecker.pathSep + pmsFilename + "' u 1:2:($3*1E"+(-zexp)+") notitle w pm3d").append(OSChecker.newline);
        script.append("unset object 1").append(OSChecker.newline);
        script.append("set xlabel \"\"").append(OSChecker.newline);
        script.append("set ylabel \"\"").append(OSChecker.newline);
        script.append("unset xtics").append(OSChecker.newline);
        script.append("unset ytics").append(OSChecker.newline);
        
        // Plot annotations...
        script.append("plot '" + parent.getPath() + OSChecker.pathSep + lowMsMassFilename + "' with filledcurves x1 fillstyle solid 1.0 noborder ls 2 notitle,\\").append(OSChecker.newline);
        script.append("     '" + parent.getPath() + OSChecker.pathSep + msBoundaryFilename + "' u 1:2:(0) w l ls 1 notitle ").append(OSChecker.newline);
        
        // Tidy up
        script.append("unset label 1").append(OSChecker.newline);
        script.append("unset label 2").append(OSChecker.newline);
        script.append("unset label 3").append(OSChecker.newline);

        //////////////////////////////////////////////////////////////////////////////
        //                                                                          //
        //              Plot the marginal formation time distribution               //
        //                                                                          //
        //////////////////////////////////////////////////////////////////////////////
        
        script.append("set key at "+(minX+0.45*(maxX-minX))+", graph 0.9").append(OSChecker.newline);
        
        // Margins for Star Formation Rate plot
        script.append("set lmargin at screen 0.25").append(OSChecker.newline);
        script.append("set rmargin at screen 0.75").append(OSChecker.newline);
        script.append("set bmargin at screen 0.2").append(OSChecker.newline);
        script.append("set tmargin at screen 0.53").append(OSChecker.newline);
        
        // X axis
        script.append("set xlabel \"{/"+OSChecker.getFont()+"=14 Lookback time [Gyr]_{}}\"").append(OSChecker.newline);
        script.append("set mxtics 2").append(OSChecker.newline);
        script.append("set xtics 1 font \""+OSChecker.getFont()+",12\" in").append(OSChecker.newline);
        script.append("set xtics format \"% g\"").append(OSChecker.newline);
        
        // Y2 axis
        script.append("set y2label \"{/"+OSChecker.getFont()+"=14 {/Symbol=16 \171}  [N ({/Symbol \\264}10^{"+yexp+"}) yr^{-1}]}\" offset 3,0").append(OSChecker.newline);
        script.append("set y2range [0:"+(ymax*1.2*Math.pow(10,-yexp))+"]").append(OSChecker.newline);
        script.append("set y2tics 1 font \""+OSChecker.getFont()+",12\" in mirror").append(OSChecker.newline);
        script.append("set my2tics 2").append(OSChecker.newline);

        
        
        if(inversionState.updatedSfr == null) {
        	// Pre-inversion: plot just the initial SFR
        	script.append("plot '" + parent.getPath() + OSChecker.pathSep + initialSfrFilename + "' u ($1/1E9):(($2+$3)*1E"+(-yexp)+"):(($2-$3)*1E"+(-yexp)+") axes x1y2 w filledcurves fillstyle transparent solid 0.5 ls 3 notitle,\\").append(OSChecker.newline);
        	script.append("     '" + parent.getPath() + OSChecker.pathSep + initialSfrFilename + "' u ($1/1E9):($2*1E"+(-yexp)+") axes x1y2 w l lw 2 lc rgb \"red\" title '{/"+OSChecker.getFont()+"=10 Initial SFR}'").append(OSChecker.newline);
	    }
        else {
        	// Inversion underway: plot initial and updated SFR for this iteration
        	script.append("plot '" + parent.getPath() + OSChecker.pathSep + updatedSfrFilename + "' u ($1/1E9):(($2+$3)*1E"+(-yexp)+"):(($2-$3)*1E"+(-yexp)+") axes x1y2 w filledcurves fillstyle transparent solid 0.5 ls 3 notitle,\\").append(OSChecker.newline);
        	script.append("     '" + parent.getPath() + OSChecker.pathSep + updatedSfrFilename + "' u ($1/1E9):($2*1E"+(-yexp)+") axes x1y2 w l lw 2 lc rgb \"red\" title '{/"+OSChecker.getFont()+"=12 Star Formation Rate}',\\").append(OSChecker.newline);
//        	script.append("     '" + parent.getPath() + OSChecker.pathSep + initialSfrFilename + "' u ($1/1E9):($2*1E"+(-yexp)+") axes x1y2 w l lt 0 lw 1 lc rgb \"black\" title '{/"+OSChecker.getFont()+"=10 Initial SFR}'").append(OSChecker.newline);
        	script.append("     '" + parent.getPath() + OSChecker.pathSep + initialSfrFilename + "' u ($1/1E9):($2*1E"+(-yexp)+") axes x1y2 w l lt 0 lw 1 lc rgb \"black\" notitle").append(OSChecker.newline);
        }
        
        // Tidy up
        script.append("unset multiplot").append(OSChecker.newline);
        
        // Save the plot script to file for replotting offline
        out = new BufferedWriter(new FileWriter(new File(parent, pmsPlotScriptFilename)));
        out.write(script.toString());
        out.close();

        //  Execute the plot script
        BufferedImage pms = Gnuplot.executeScript(script.toString());
        
        // Save the plot to file
        ImageIO.write(pms, "png", new File(parent, pmsPlotFilename));
        
        return pms;
    }
    
    /**
     * Produces the diagnostic plot P_{WD}: the joint distribution of white dwarf
     * mass and magnitude.
     * 
     * @param parent
	 * 	The path to the directory in which to save the output files.
     * @param inversionState
     * 	The main {@link InversionState} instance that encapsulates all the modelling parameters and
     * other quantities.
     * @param whiteDwarfs
     * 	The {@link RangeMap<Star>} containing the stars to process.
     * @return
	 * 	A {@link BufferedImage} containing the diagnostic plot P_{WD}: the joint distribution of white dwarf
     * mass and magnitude.
     * @throws IOException
	 * 	If there's an exception when writing data files.
     */
    public static BufferedImage getPwd(File parent, InversionState inversionState, RangeMap<Star> whiteDwarfs) throws IOException {

    	// Get a reference to the {@link WdlfModellingParameters} for convenience
    	WdlfModellingParameters params = inversionState.params;
    	double tMax = inversionState.currentSfr.t_max;
    	
		/////////////////////////////////////////////////////////////////////////
		//                                                                     //
		//                 Write observed & model WDLF to file                 //
		//                                                                     //
		/////////////////////////////////////////////////////////////////////////
    	
        BufferedWriter out = new BufferedWriter(new FileWriter(new File(parent, modelWDLFFileName)));
        if(inversionState.wdlf_model==null) {
        	out.write("0 1 1");
        }
        else {
        	out.write(inversionState.wdlf_model.toString());
        }
        out.close();
        
        out = new BufferedWriter(new FileWriter(new File(parent, obsWDLFFileName)));
        out.write(inversionState.wdlf_obs.toString());
        out.close();

        /////////////////////////////////////////////////////////////////////////
        //                                                                     //
        //           Build 2D distribution of WD mass and magnitude,           //
        //             and 1D marginal distributions                           //
        //                                                                     //
        /////////////////////////////////////////////////////////////////////////
        
        double z = params.getMeanMetallicity();
        double y = params.getMeanHeliumContent();
        
        // Get low mass limit for White Dwarfs
        double low_mass_limit_MS = params.getPreWdLifetime().getStellarMass(z, y, tMax)[0];
        double low_mass_limit_WD = params.getIFMR().getMf(low_mass_limit_MS);
        
        // Set magnitude axis range such that the observational WDLF lies in central 4/5
        double minObsMag = inversionState.wdlf_obs.getXRange()[0];
        double maxObsMag = inversionState.wdlf_obs.getXRange()[1];
        double marginMag = (maxObsMag - minObsMag) / 10.0;
        double minX = minObsMag - marginMag;
        double maxX = maxObsMag + marginMag;
        // Reduce the step size in order to fit in a whole number of bins
        double dX = (maxX - minX) / Math.ceil((maxX - minX) / wdLuminosityFunctionStepSize);
        double minY = low_mass_limit_WD;
        double maxY = params.getIFMR().getMf(BaseImf.M_upper);
        // Reduce the step size in order to fit in a whole number of bins
        double dY = (maxY - minY) / Math.ceil((maxY - minY) / wdMassFunctionStepSize);

        Histogram wdMassDistribution = new Histogram(minY, maxY, dY, true);
        double wdMassFunctionPeak = 0.5;
        double meanWdMass = Double.NaN;
        double medianWdMass = Double.NaN;
        
        Histogram2D wdMassMagJointDistribution = new Histogram2D(minX, maxX, dX, minY, maxY, dY, true);
        double zmax = 10.0;
        int zexp = 1;
        if(whiteDwarfs!=null) {
        	// Populate the histogram
        	for(int bin=0; bin<whiteDwarfs.size(); bin++) {
                for (Star star : whiteDwarfs.get(bin)) {
                    wdMassMagJointDistribution.add(star.getMag(), star.getWhiteDwarfMass(), star.getNumber()/(dX*dY));
                    wdMassDistribution.add(star.getWhiteDwarfMass(), star.getNumber());
                }
        	}
        	zmax = wdMassMagJointDistribution.getMax();
        	zexp = (int)Math.floor(Math.log10(zmax));
        	wdMassFunctionPeak = wdMassDistribution.getMax();
            meanWdMass = wdMassDistribution.getMean();
            medianWdMass = wdMassDistribution.getMedian();
        }
        
        out = new BufferedWriter(new FileWriter(new File(parent, pwdFileName)));
        out.write(wdMassMagJointDistribution.print(false));
        out.close();
        
        out = new BufferedWriter(new FileWriter(new File(parent, wdMassDistFileName)));
        out.write(wdMassDistribution.print(false));
        out.close();
        

        /////////////////////////////////////////////////////////////////////////
        //                                                                     //
        //                   Write plot annotations to file                    //
        //                                                                     //
        /////////////////////////////////////////////////////////////////////////
        
        // Upper mass limit of WDs
        out = new BufferedWriter(new FileWriter(new File(parent, maxWDMassFileName)));
        out.write(minX+"\t"+params.getIFMR().getMf(BaseImf.M_upper)+OSChecker.newline);
        out.write(maxX+"\t"+params.getIFMR().getMf(BaseImf.M_upper)+OSChecker.newline);
        out.close();
        
        // Low mass limit for WDs (H atmosphere): encloses the lower and right sides of the P_{WD} plane
        out = new BufferedWriter(new FileWriter(new File(parent, minHWDMassFileName)));
        for(double[] point : getLowMassWDLimit(params, tMax, WdAtmosphereType.H, maxX)) {
        	out.write(point[0]+"\t"+point[1]+OSChecker.newline);
        }
        out.close();
        
        // Low mass limit for WDs (He atmosphere): encloses the lower and right sides of the P_{WD} plane
        out = new BufferedWriter(new FileWriter(new File(parent, minHeWDMassFileName)));
        for(double[] point : getLowMassWDLimit(params, tMax, WdAtmosphereType.He, maxX)) {
        	out.write(point[0]+"\t"+point[1]+OSChecker.newline);
        }
        out.close();
        
        // Bright limit for WDs (H atmosphere): encloses the left side of the P_{WD} plane
        out = new BufferedWriter(new FileWriter(new File(parent, minHWDMbolFileName)));
        for(double[] point : getBrightWDLimit(params, tMax, WdAtmosphereType.H)) {
        	out.write(point[0]+"\t"+point[1]+OSChecker.newline);
        }
        out.close();
        
        // Bright limit for WDs (He atmosphere): encloses the left side of the P_{WD} plane
        out = new BufferedWriter(new FileWriter(new File(parent, minHeWDMbolFileName)));
        for(double[] point : getBrightWDLimit(params, tMax, WdAtmosphereType.He)) {
        	out.write(point[0]+"\t"+point[1]+OSChecker.newline);
        }
        out.close();
        
		//////////////////////////////////////////////////////////////////////////////
		//                                                                          //
		//                  Construct Gnuplot script                                //
		//                                                                          //
		//////////////////////////////////////////////////////////////////////////////

        double[] y_range = inversionState.wdlf_obs.getYRange();
        
    	String filterName = params.getFilter().toString();
        
        StringBuilder script = new StringBuilder();
        
        //////////////////////////////////////////////////////////////////////////////
        //                                                                          //
        //                  Set up the terminal                                     //
        //                                                                          //
        //////////////////////////////////////////////////////////////////////////////
        
        script.append("solarMass = \"M_{"+CharUtil.solar+"}\"").append(OSChecker.newline);
        script.append("set terminal pngcairo enhanced color crop size 1024,1024").append(OSChecker.newline);
        
        script.append("set style line 1 lt 2 lc rgb \"#00AA00\" lw 1").append(OSChecker.newline);
        script.append("set style line 2 lt 3 lc rgb \"blue\" lw 1").append(OSChecker.newline);
        script.append("set style line 3 lt 1 lc rgb \"black\" lw 1").append(OSChecker.newline);
        script.append("set style line 4 lt 1 lc rgb \"grey\" lw 1").append(OSChecker.newline);
        
//        script.append("set size 1024,1024").append(OSChecker.newline);
        script.append("set multiplot").append(OSChecker.newline);
        script.append("set key off").append(OSChecker.newline);
        script.append("set tics scale 0.5").append(OSChecker.newline);
        
        //////////////////////////////////////////////////////////////////////////////
        //                                                                          //
        //                Plot the marginal mass distribution                       //
        //                                                                          //
        //////////////////////////////////////////////////////////////////////////////

    	
        // Margins for mass distribution plot
        script.append("set lmargin at screen 0.15").append(OSChecker.newline);
        script.append("set rmargin at screen 0.23").append(OSChecker.newline);
        script.append("set bmargin at screen 0.55").append(OSChecker.newline);
        script.append("set tmargin at screen 0.95").append(OSChecker.newline);
        
        // X axis
        script.append("set xrange [0:"+(wdMassFunctionPeak*1.2)+"] reverse").append(OSChecker.newline);
        script.append("set xtics "+getTicInterval(0, wdMassFunctionPeak*1.2)+" in").append(OSChecker.newline);
        script.append("set xtics format ''").append(OSChecker.newline);
        
        // Y axis
        script.append("set yrange ["+minY+":"+maxY+"]").append(OSChecker.newline);
        script.append("set ytics 0.1 font \""+OSChecker.getFont()+",12\"").append(OSChecker.newline);
        script.append("set mytics 2").append(OSChecker.newline);
        script.append("set ylabel \"WD Mass [\".solarMass.\"]\" font '"+OSChecker.getFont()+",14' offset 0,0").append(OSChecker.newline);
        
        // Set annotations for WD mass distribution
        script.append("set arrow 1 from 0,"+meanWdMass+" to "+(wdMassFunctionPeak*1.2)+","+meanWdMass+ " nohead lc rgbcolor 'black'").append(OSChecker.newline);
        script.append("set arrow 2 from 0,"+medianWdMass+" to "+(wdMassFunctionPeak*1.2)+","+medianWdMass+ " nohead lc rgbcolor 'red'").append(OSChecker.newline);
        script.append("set label 1 'mean' at "+wdMassFunctionPeak+",1 rotate font ',8' tc rgbcolor 'black'").append(OSChecker.newline);
        script.append("set label 2 'median' at "+0.8*wdMassFunctionPeak+",1 rotate font ',8' tc rgbcolor 'red'").append(OSChecker.newline);
        
        // Plot WD mass distribution
        script.append("plot '" + parent.getPath() + OSChecker.pathSep + wdMassDistFileName + "' u 2:1 w l ls 2").append(OSChecker.newline);

        // Tidy up
        script.append("unset arrow 1").append(OSChecker.newline);
        script.append("unset arrow 2").append(OSChecker.newline);
        script.append("unset label 1").append(OSChecker.newline);
        script.append("unset label 2").append(OSChecker.newline);
        
        //////////////////////////////////////////////////////////////////////////////
        //                                                                          //
        //              Plot the joint mass/magnitude distribution                  //
        //                                                                          //
        //////////////////////////////////////////////////////////////////////////////

        script.append("set view map").append(OSChecker.newline);
        
        // Margins for joint mass/magnitude distribution plot
        script.append("set lmargin at screen 0.25").append(OSChecker.newline);
        script.append("set rmargin at screen 0.75").append(OSChecker.newline);
        script.append("set bmargin at screen 0.55").append(OSChecker.newline);
        script.append("set tmargin at screen 0.95").append(OSChecker.newline);
        
        // X axis
        script.append("set xrange ["+minX+":"+maxX+"] noreverse").append(OSChecker.newline);
        script.append("unset xtics").append(OSChecker.newline);
        script.append("unset xlabel").append(OSChecker.newline);
        
        // Y axis
        script.append("unset ytics").append(OSChecker.newline);
        script.append("unset ylabel").append(OSChecker.newline);
        
        // Z axis
        script.append("set cblabel \"Density\\n[N ({/Symbol \\264}10^{"+zexp+"}) Mag^{-1} \".solarMass.\"^{-1}]\" font '"+OSChecker.getFont()+",14'").append(OSChecker.newline);
        script.append("set cbtics 1 font \""+OSChecker.getFont()+",12\" nomirror out").append(OSChecker.newline);
        script.append("set mcbtics 2").append(OSChecker.newline);
        script.append("set palette defined (0 \"white\", 0.0001 \"yellow\", 0.001 \"orange\", 0.01 \"red\", 0.1 \"black\")").append(OSChecker.newline);
        
        // Contour parameters
//        script.append("set cont base").append(OSChecker.newline);
//        script.append("unset clabel").append(OSChecker.newline);
        // Plot 5 contours between zero and ztic/5
//        script.append("set cntrparam levels discrete "+Math.pow(10,-zexp-4)+","+Math.pow(10,-zexp-3.5)+","+Math.pow(10,-zexp-3)+","+Math.pow(10,-zexp-2.5)+","+Math.pow(10,-zexp-2)+"").append(OSChecker.newline);
//        script.append("set cntrparam linear").append(OSChecker.newline);
        
        script.append("set object 1 rectangle from graph 0,0 to graph 1,1 fillcolor rgb \"grey\" behind ").append(OSChecker.newline);
        script.append("splot '" + parent.getPath() + OSChecker.pathSep + pwdFileName + "' u 1:2:($3*1E"+(-zexp)+") notitle w pm3d").append(OSChecker.newline);
        
        // Plot annotations...
        script.append("unset object 1").append(OSChecker.newline);
        script.append("set xlabel \"\"").append(OSChecker.newline);
        script.append("set ylabel \"\"").append(OSChecker.newline);
        script.append("unset xtics").append(OSChecker.newline);
        script.append("unset ytics").append(OSChecker.newline);
        // Write labels for WD boundary lines:
        script.append("set label 1 \"H WD boundary\" at graph 0.05,0.97 front font '"+OSChecker.getFont()+",8' textcolor rgb \"#00AA00\"").append(OSChecker.newline);        
        script.append("set label 2 \"He WD boundary\" at graph 0.05,0.92  front font '"+OSChecker.getFont()+",8' textcolor rgb \"blue\"").append(OSChecker.newline);
        script.append("plot '" + parent.getPath() + OSChecker.pathSep + minHeWDMassFileName + "' with filledcurves x1 fillstyle solid 1.0 noborder ls 4,\\").append(OSChecker.newline);
        script.append("     '" + parent.getPath() + OSChecker.pathSep + minHWDMbolFileName + "' with filledcurves y1 fillstyle solid 1.0 noborder ls 4,\\").append(OSChecker.newline);
        script.append("     '" + parent.getPath() + OSChecker.pathSep + minHWDMassFileName + "' w l ls 1 ,\\").append(OSChecker.newline);
        script.append("     '" + parent.getPath() + OSChecker.pathSep + minHWDMbolFileName + "' w l ls 1 ,\\").append(OSChecker.newline);
        script.append("     '" + parent.getPath() + OSChecker.pathSep + minHeWDMassFileName + "' w l ls 2 ,\\").append(OSChecker.newline);        
        script.append("     '" + parent.getPath() + OSChecker.pathSep + minHeWDMbolFileName + "' w l ls 2 ,\\").append(OSChecker.newline);
        script.append("     '" + parent.getPath() + OSChecker.pathSep + maxWDMassFileName + "' w l ls 3").append(OSChecker.newline);
        
        // Tidy up
        script.append("unset label 1").append(OSChecker.newline);
        script.append("unset label 2").append(OSChecker.newline);
        
        //////////////////////////////////////////////////////////////////////////////
        //                                                                          //
        //              Plot the marginal magnitude distribution                    //
        //                                                                          //
        //////////////////////////////////////////////////////////////////////////////

        // Margins for WDLF plot
        script.append("set lmargin at screen 0.25").append(OSChecker.newline);
        script.append("set rmargin at screen 0.75").append(OSChecker.newline);
        script.append("set bmargin at screen 0.2").append(OSChecker.newline);
        script.append("set tmargin at screen 0.53").append(OSChecker.newline);
        
        script.append("set key at "+(minX + 0.45*(maxX-minX))+", graph 0.9").append(OSChecker.newline);
        script.append("set style line 4 lt 1 lw 2 pt 5 ps 0.5 lc rgb \"red\"").append(OSChecker.newline);
        script.append("set style line 5 lt 1 lw 1 pt 7 ps 0.75 lc rgb \"black\"").append(OSChecker.newline);
        // Width of bars on top & bottom of error bars
        script.append("set bar 0.5").append(OSChecker.newline);
        // Handle log plotting of lower error bars that lie at zero
        script.append("f(s,n) = (s-n>0) ? (s-n) : 9E-18").append(OSChecker.newline);
        
        // X axis
        script.append("set xlabel \"{/"+OSChecker.getFont()+"=14 Magnitude ["+filterName+"]}\"").append(OSChecker.newline);
        script.append("set mxtics 2").append(OSChecker.newline);
        script.append("set xtics 2 font \""+OSChecker.getFont()+",12\" in").append(OSChecker.newline);
        script.append("set xtics format \"% g\"").append(OSChecker.newline);
        
        // Y2 axis
        script.append("set y2label \"{/"+OSChecker.getFont()+"=14 Log {/Symbol \106} [N Mag^{-1}]}\" offset 3,0").append(OSChecker.newline);
        script.append("set y2range ["+y_range[0]+":"+y_range[1]+"]").append(OSChecker.newline);
        script.append("set y2tics "+getTicInterval(y_range[0], y_range[1])+" font \""+OSChecker.getFont()+",12\" in mirror").append(OSChecker.newline);
        script.append("set my2tics 2").append(OSChecker.newline);
        
        script.append("plot '" + parent.getPath() + OSChecker.pathSep + modelWDLFFileName + "' u 1:(log10($3)) axes x1y2 w l ls 4 title '{/"+OSChecker.getFont()+"=12 Model}',\\").append(OSChecker.newline);
//        script.append(" '" + parent.getPath() + OSChecker.pathSep + obsWDLFFileName + "' u 1:(log10($3)) axes x1y2 w l ls 5 notitle,\\").append(OSChecker.newline);
        script.append(" '" + parent.getPath() + OSChecker.pathSep + obsWDLFFileName + "' u 1:(log10($3)):(log10(f($3,$4))):(log10($3+$4)) axes x1y2 w yerrorbars ls 5 title '{/"+OSChecker.getFont()+"=12 "+inversionState.wdlf_obs.reference+"}'").append(OSChecker.newline);
        
        // Tidy up
        script.append("unset multiplot").append(OSChecker.newline);
        
        // Save the plot script to file for replotting offline
        out = new BufferedWriter(new FileWriter(new File(parent, pwdPlotScriptFilename)));
        out.write(script.toString());
        out.close();
        
        //  Execute the plot script
        BufferedImage pwd = Gnuplot.executeScript(script.toString());

        // Save the plot to file
        ImageIO.write(pwd, "png", new File(parent, pwdPlotFilename));
        
        return pwd;
    }
    
    /**
     * Returns a tic interval such that there will be between one and ten tics in the given plot range.
     * @param min
     * 	The lower end of the plot range.
     * @param max
     * 	The upper end of the plot range.
     * @return
     * 	Suitable interval such that there'll be between one and ten tics in the given interval.
     */
    public static double getTicInterval(double min, double max) {
    	return Math.pow(10, Math.floor(Math.log10(max - min)));
    }

    /**
     * Get low mass segment of boundary of populated region of (Mass_{MS}, t_{form}) plane.
     * 
     * @param params
     * 	The {@link WdlfModellingParameters}
     * @param age
     * 	The maximum age of any star, or, the time since the onset of star formation [yr]
     * @param tmin
     * 	The minimum age of any star; in WD modelling, this is set to the lifetime of the most
     * massive star: no star formed in the most recent tmin years has formed a WD yet.
     * @return
     * 	A {@link List<double[]>} containing the locus of points along the low mass boundary of
     * the populated region of (Mass_{MS}, t_{form}) plane. Each double[] contains the age [Gyr]
     * in the first element and the stellar mass [M_{Solar}] in the secon element.
     */
    public static List<double[]> getMinMSMassRelation(WdlfModellingParameters params, double age, double tmin) {
    	
    	double z = params.getMeanMetallicity();
    	double y = params.getMeanHeliumContent();
    	PreWdLifetime preWdLifetimes = params.getPreWdLifetime();
    	
    	// Number of points to plot between tmin and age
    	int nPoints = 50;

        // Set time step at which to plot points
        double dT = (age-tmin)/nPoints;
        
        List<double[]> boundary = new LinkedList<double[]>();
        
        // Plot initial point at t_min,M_{max}
        boundary.add(new double[]{tmin/1.0E9, preWdLifetimes.getStellarMass(z, y, tmin)[0]});
        
        // Now loop over rest of lookback time range
        for(int p=0; p<nPoints; p++) {
        	double time = tmin + p*dT + dT/2.0;
        	boundary.add(new double[]{time/1.0E9, preWdLifetimes.getStellarMass(z, y, time)[0]});
        }
        
        // Plot final point at age,M_{min}
        boundary.add(new double[]{age/1.0E9, preWdLifetimes.getStellarMass(z, y, age)[0]});
        
        return boundary;
    }
    
    /**
     * Get bright limit of WDs, i.e. locus of zero-cooling-times WDs truncated
     * at minimum WD mass.
     * 
     * @param params
     * 	The {@link WdlfModellingParameters}
     * @param age
     * 	The maximum age of any star, or, the time since the onset of star formation [yr]
     * @param atm
     * 	The {@link WdAtmosphereType}.
     */
    public static List<double[]> getBrightWDLimit(WdlfModellingParameters params, double age, WdAtmosphereType atm) {
    	
    	double z = params.getMeanMetallicity();
    	double y = params.getMeanHeliumContent();
    	PreWdLifetime preWdLifetimes = params.getPreWdLifetime();
    	Filter filter = params.getFilter();
    	BaseIfmr ifmr = params.getIFMR();
    	WdCoolingModelSet wdCoolingModelSet = params.getBaseWdCoolingModels();
    	
        List<double[]> points = new LinkedList<double[]>();
        
        // MS turnoff mass of oldest stars.
        double msTurnOffMass = preWdLifetimes.getStellarMass(z, y, age)[0];
        
        // Corresponding minimum WD mass
        double minWdMass = ifmr.getMf(msTurnOffMass);
        // Maximum WD mass
        double maxWdMass = ifmr.getMf(BaseImf.M_upper);
        
        double DMASS = 0.001;
        
        // Loop over all WD masses
        for(double wdMass = maxWdMass; wdMass>=minWdMass; wdMass-=DMASS)
        {
            // Get bolometric magnitude at cooling time of zero.
            points.add(new double[]{wdCoolingModelSet.mag(0, wdMass, atm, filter), wdMass});
        } 
        
        return points; 
    }
    
    /**
     * This method computes the boundary of the populated region of the (Mass_{WD}, Mag) plane
     * on the low mass side, i.e. as a function of magnitude it gives the mass below which WDs
     * have not had time to form.
     * 
     * It starts at the bright magnitude end where the Mass_{WD} point corresponds to newly formed
     * WDs that are the lowest mass WDs currently in existence. It then increases towards fainter
     * magnitudes until it either reaches the user-specified faint magnitude limit or the point
     * where the WD mass reaches the upper mass limit, in which case the function is truncated
     * there.
     * 
     * @param params
     * 	The {@link WdlfModellingParameters}
     * @param age
     * 	The maximum age of any star, or, the time since the onset of star formation [yr]
     * @param atm
     * 	The {@link WdAtmosphereType}
     * @param faintMagLimit
     * 	Magnitude limit out to which to compute the low WD mass limit relation. Note that the
     * relation may not exist out to this magnitude, i.e. if the magnitude is very faint and/or
     * the age too young then WDs will not have had time to reach it. In this case the function
     * is truncated.
     * @return
     */
    public static List<double[]> getLowMassWDLimit(WdlfModellingParameters params, double age, WdAtmosphereType atm, double faintMagLimit) {

    	double z = params.getMeanMetallicity();
    	double y = params.getMeanHeliumContent();
    	PreWdLifetime preWdLifetimes = params.getPreWdLifetime();
    	Filter filter = params.getFilter();
    	BaseIfmr ifmr = params.getIFMR();
    	WdCoolingModelSet wdCoolingModelSet = params.getBaseWdCoolingModels();
    	
        List<double[]> points = new LinkedList<double[]>();
        
        // What is brightest magnitude? This occurs at cooling time of zero, for
        // WDs that have just formed, which are the lowest mass WDs.
        
        // MS turnoff mass of oldest stars.
        double msTurnOffMass = preWdLifetimes.getStellarMass(z, y, age)[0];
        // Corresponding minimum WD mass
        double minWdMass = ifmr.getMf(msTurnOffMass);
        // Minimum magnitude, at zero cooling time
        double minWdMag = wdCoolingModelSet.mag(0, minWdMass, atm, filter);
        
        // Add this first point to List
        points.add(new double[]{minWdMag, minWdMass});
        
        // Upper limit on WD mass
        double maxWdMass = ifmr.getMf(BaseImf.M_upper);

        // Get minimum mass to within this tolerance
        double dm = 0.0000001;
        
        // Step in mbol
        double dMag = 0.1;
        
        // Now loop over rest of bolometric magnitude range
        
        // Loop over bolometric magnitude and get minimum WD mass
        for(double mag = minWdMag + dMag; mag<=faintMagLimit; mag+=dMag)
        {
            double[] point = new double[2];
            
            if(wdCoolingModelSet.tcool(mag, maxWdMass, atm, filter) > age) {
            	// At the current (faint) magnitude, no WDs have had time to form: truncate the 
            	// boundary here, after adding a final point at the high mass WD limit.
            	point[0] = mag;
                // WD mass in second element
                point[1] = maxWdMass;
                points.add(point);
            	break;
            }
            
            // Magnitude in first element
            point[0] = mag;
            
            // recursive interval halving method
            double mass = recursiveMinWDMass(params, minWdMass, maxWdMass, dm, atm, age, mag);
            
            // WD mass in second element
            point[1] = mass;
            
            points.add(point);
        }
        
        return points; 
    }
    
    /**
     * Get the minimum WD mass at a given bolometric magnitude. This involves
     * solving the transcendental equation for m0:
     * 
     * T_cool(mbol,m0) + T_MS(mass(m0)) = T_0
     * 
     * where T_cool is WD cooling time, T_MS is main sequence lifetime, and
     * T_0 is total time since the onset of star formation. An interval
     * halving algorithm is used.
     * 
     * It's possible that multiple solutions exist at a given bolometric
     * magnitude, depending on the rate of cooling of WDs of different mass.
     * In this case, a single low mass limit does not exist, and this method 
     * may fail.
     * 
     * Method is as follows:
     * 
     * Check that T_cool(mbol,m0,atm) + T_MS(mass(m0)) \ge T0
     *            T_cool(mbol,m1,atm) + T_MS(mass(m1)) \le T0
     * 
     *            - verifies that solution lies in [m0:m1]
     * 
     * @param params
     * 	The {@link WdlfModellingParameters}
     * @param m0
     * 		Lower boundary on MS mass range to search [M_{Solar}]
     * @param m1
     * 		Upper boundary on MS mass range to search [M_{Solar}]
     * @param dm
     * 		Tolerance on solution [M_{Solar}]
     * @param atm
     * 		The {@link  WdAtmosphereType}
     * @param age
     * 	The total age of population [yr]
     * @param mag
     * 	Magnitude (absolute or bolometric) at which to find minimum WD mass
     * @return
     * 	The minimum mass WD that has had time to form and cool to the
     * given magnitude [M_{Solar}]
     * @throws IllegalArgumentException
     * 	If there is no solution at the given magnitude.
     */
    public static double recursiveMinWDMass(WdlfModellingParameters params, double m0, double m1, double dm, 
    		WdAtmosphereType atm, double age, double mag) throws IllegalArgumentException {

    	double z = params.getMeanMetallicity();
    	double y = params.getMeanHeliumContent();
    	PreWdLifetime preWdLifetimes = params.getPreWdLifetime();
    	Filter filter = params.getFilter();
    	BaseIfmr ifmr = params.getIFMR();
    	WdCoolingModelSet wdCoolingModelSet = params.getBaseWdCoolingModels();
    	
        // Check that WDs of this magnitude have had time to form.
        // This assumes that high mass WDs cool the fastest.
        if(wdCoolingModelSet.tcool(mag, ifmr.getMf(BaseImf.M_upper), atm, filter) > age) {
            throw new IllegalArgumentException("Highest mass "+atm+" WDs not "
                    + "had time to cool to "+mag+ " in "+age+" years.");
        }
    	
    	// Get MS progenitor mass range
    	double M0 = ifmr.getMi(m0);
    	double M1 = ifmr.getMi(m1);
    	
    	// Check for numerical errors that cause M1 to be larger than IMF.upper, i.e.
    	// if we do getMi(getMf(IMF.upper)) we sometimes get 7.00000000001
    	if(M1 > BaseImf.M_upper)
    	{
//    		logger.warning("recursiveMinWDMass: overflowed M_upper ("+M1+")");
    		M1 = BaseImf.M_upper;
    	}
    	
        // WD cooling time @ m0
        double WD_cool_0 = wdCoolingModelSet.tcool(mag, m0, atm, filter);
        // MS lifetime @ m0
        double MS_lifetime_0 = preWdLifetimes.getPreWdLifetime(z, y, M0)[0];
        // Check total age of star at lower mass limit is older than total
        // time since onset of star formation.
        assert WD_cool_0 + MS_lifetime_0 > age;
        // WD cooling time @ m1
        double WD_cool_1 = wdCoolingModelSet.tcool(mag, m1, atm, filter);
        // MS lifetime @ m1
        double MS_lifetime_1 = preWdLifetimes.getPreWdLifetime(z, y, M1)[0];
        // Check total age of star at upper mass limit is less than total
        // time since onset of star formation.
        assert WD_cool_1 + MS_lifetime_1 < age;
        // Now check total age of star at midpoint of mass range
        double m_mid = (m0+m1)/2.0;
        
        // WD cooling time @ m_mid
        double WD_cool_mid = wdCoolingModelSet.tcool(mag, m_mid, atm, filter);
        // MS lifetime @ m_mid
        double MS_lifetime_mid = preWdLifetimes.getPreWdLifetime(z, y, ifmr.getMi(m_mid))[0];
        if(WD_cool_mid + MS_lifetime_mid < age)
        {
            // Solution lies in lower half of mass range
            if(m_mid - m0 < dm) 
                // Threshold reached - return mid point of lower half of mass
                // range as solution.
                return (m_mid + m0)/2.0;
            else
                // Threshold not reached - recurse
                return recursiveMinWDMass(params, m0, m_mid, dm, atm, age, mag);
        }
        else if (WD_cool_mid + MS_lifetime_mid > age)
        {
            // Solution lies in upper half of mass range
            if(m1 - m_mid < dm)
                // Threshold reached - return mid point of upper half of mass
                // range as solution.
                return (m1 + m_mid)/2.0;
            else
                // Threshold not reached - recurse
                return recursiveMinWDMass(params, m_mid, m1, dm, atm, age, mag);           
        }
        else
        {
            // Exact solution found to within machine precision
            return m_mid;
        }
    }
    
}