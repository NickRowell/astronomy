package projects.gaia.exec;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Logger;

import ifmr.infra.IFMR;
import imf.algo.BaseImf;
import imf.algoimpl.IMF_PowerLaw;
import infra.io.Gnuplot;
import infra.os.OSChecker;
import ms.lifetime.algo.PreWdLifetime;
import ms.lifetime.algoimpl.PreWdLifetime_Padova;
import numeric.minimisation.nllsq.algo.LevenbergMarquardt;
import numeric.minimisation.nllsq.algo.LevenbergMarquardt.STATUS;
import photometry.Filter;
import projects.gaia.dm.GaiaSdssSource;
import projects.gaia.dm.GaiaSource;
import projects.gaia.util.GaiaSourceUtil;
import projects.gaia.util.WdMassFitter;
import wd.models.algo.WdCoolingModelSet;
import wd.models.infra.WdAtmosphereType;
import wd.models.infra.WdCoolingModels;

/**
 * This class provides an application that analyses White Dwarfs in the Solar Neighbourhood.
 *
 *
 * @author nrowell
 * @version $Id$
 */
public class SolarNeighbourhoodWdsGaiaSdss {
	
	/**
	 * The logger.
	 */
	private static final Logger logger = Logger.getLogger(SolarNeighbourhoodWdsGaiaSdss.class.getName());
	
	/**
	 * The {@link File} containing the input data (subset of GaiaSource fields) in CSV format.
	 */
	private static File input = new File("/home/nrowell/Astronomy/gaia_WDs/dr2_work/dr2_samples/gaia_x_sdss/WDsXsdss-result.csv");
	
	/**
	 * Main application entry point.
	 * @param args
	 * 	The command line args (ignored)
	 */
	public static void main(String[] args) {
		
		// Load the input catalogue
		Collection<GaiaSdssSource> wds = GaiaSourceUtil.loadGaiaSdssSources(input);
		
		// Load the WD models
		WdCoolingModelSet wdModels = WdCoolingModels.MONTREAL.getWdCoolingModels();
		
		// Select IFMR
		IFMR ifmr = IFMR.KALIRAI_2009;
		
		// Select MS lifetime models
		PreWdLifetime preWdLifetimeModels = new PreWdLifetime_Padova();
		// Select progenitor metallicity
		double z = 0.0017;
		double y = 0.271;
		
		// Select IMF (Salpeter)
		BaseImf imf = new IMF_PowerLaw();
		
		displayHrDiagram(wds, wdModels);
		
		// Parameters of mass distribution plot
		double massMin = 0.0;
		double massMax = 1.4;
		double massStep = 0.01;
        int nMassBins = (int)Math.floor((massMax - massMin) / massStep);
        int[] massHist = new int[nMassBins];
        
        // Parameters of star formation plot
        double ageMin = 0;
		double ageMax = 13e9;
		double ageStep = 1e8;
        int nAgeBins = (int)Math.floor((ageMax - ageMin) / ageStep);
        double[] ageHist = new double[nAgeBins];
		
        // Relative weights according to atmosphere type
        Map<WdAtmosphereType, Double> atmWeights = new EnumMap<>(WdAtmosphereType.class);
        atmWeights.put(WdAtmosphereType.H, 0.65);
        atmWeights.put(WdAtmosphereType.He, 0.35);
        
        // Fit WD atmospheres of each type
        for(WdAtmosphereType atm : WdAtmosphereType.values()) {
        
	        // Track fitting outcomes
	        int n_success=0, n_fail=0, n_unrealistic=0;
	        
	        // Relative weight for this atmosphere type
	        double w = atmWeights.get(atm);
	        
			// Loop over each star
			for(GaiaSdssSource wd : wds) {
				
				// 1) Determine atmosphere type - for now, weight each star 35% He & 65% H
				
				// 2) Use appropriate WD cooling model set to fit mass of WD in U/U-G plane
				WdMassFitter fitter = new WdMassFitter(wdModels, Filter.SDSS_U, Filter.SDSS_U, Filter.SDSS_G, atm);
				
				double[] data = new double[]{wd.u - wd.g, wd.u};
				double[][] cov = new double[][]{{1.0, 0.0}, {0.0, 1.0}};
				// Initial guess parameters
				double initGuessMass = 0.5;
				double initGuessCoolingTime = wdModels.tcool(wd.u, initGuessMass, atm, Filter.SDSS_U);
				double[] initParams = new double[]{initGuessMass, initGuessCoolingTime};
				fitter.setData(data);
				fitter.setCovariance(cov);
				fitter.setInitialGuessParameters(initParams);
	
				// Perform the optimization
				STATUS status = fitter.fit(500, false);
				
				if(status == STATUS.SUCCESS) {
					
					// Extract the solution
					double[] solution = fitter.getParametersSolution();
					
					double wdMass = solution[0];
					double wdCoolingTime = solution[1];
					
					// Check for physically realistic solution
					if(wdMass < 0.2 || wdMass > 1.4) {
						n_unrealistic++;
						continue;
					}
					n_success++;
					
					// Add to the mass distribution
					int massBin = (int)Math.floor((wdMass - massMin) / massStep);
					massHist[massBin]++;
					
					// Get progenitor mass
					double ms_mass = ifmr.getIFMR().getMi(wdMass);
					
					// Get progenitor lifetime
					double preWdLifetime = preWdLifetimeModels.getPreWdLifetime(z, y, ms_mass)[0];
					
					// Total stellar age
					double age = preWdLifetime + wdCoolingTime;
					
					// Bin stars by age, applying relative weights
					int ageBin = (int)Math.floor((age - ageMin) / ageStep);
					if(ageBin >=0 && ageBin < nAgeBins) {
						ageHist[ageBin] += w;
					}
				}
				else {
					n_fail++;
				}
			}
			
			System.out.println("("+atm+" atmospheres) Proportion of success/unrealistic/fail = "+n_success + " / " + n_unrealistic + " / " + n_fail + " (of "+wds.size()+")");
			
			displayMassDistribution(massHist, massMin, massStep, "WD mass distribution (assuming "+atm+" atmosphere)");
        }
        
        displayAgeDistribution(ageHist, ageMin, ageStep, "Stellar age distribution");
        
        // Correct age distribution to get SFR
        for(int i=0; i<ageHist.length; i++) {
        	
        	// Compute the fraction of stars that form at this time that formed WDs at the present day
			double age = ageMin + i*ageStep + ageStep/2.0;
			
			// Get the MS turnoff mass at this age
			double mass_MSTO = preWdLifetimeModels.getStellarMass(z, y, age)[0];
            
            if(mass_MSTO < BaseImf.M_upper) {
                // Fraction of stars that have had time to form WDs
            	double wd_frac = 1.0 - imf.getIntegral(mass_MSTO);
            	
            	// Correct the number of WDs for the stars that haven't had time to form WDs
            	ageHist[i] *= (1.0/wd_frac);
            }
            else {
            	// Very recent times: no WDs yet formed, so constraint on the SFR
            	ageHist[i] = 0.0;
            }
        }
        
        displayAgeDistribution(ageHist, ageMin, ageStep, "Relative Star Formation Rate");
	}
	
	/**
	 * Plot and display the HR diagram for the set of {@link GaiaSource}s.
	 * @param wds
	 * 	The set of {@link GaiaSource}s to plot.
	 */
	private static void displayHrDiagram(Collection<GaiaSdssSource> wds, WdCoolingModelSet wdModels) {

		for(WdAtmosphereType atm : WdAtmosphereType.values()) {
			
			// Constant-mass tracks to plot in HR diagram
			double[] masses = wdModels.getMassGridPoints(atm);
			
			String colour = atm==WdAtmosphereType.H ? "blue" : "green";
			
			StringBuilder script = new StringBuilder();
			script.append("set terminal pngcairo enhanced color size 512,512").append(OSChecker.newline);
			script.append("set xrange [-0.5:2.5]").append(OSChecker.newline);
			script.append("set yrange [9:19] reverse").append(OSChecker.newline);
			
			script.append("set key top right").append(OSChecker.newline);
			script.append("set xtics out").append(OSChecker.newline);
			script.append("set ytics out").append(OSChecker.newline);
			script.append("unset colorbox").append(OSChecker.newline);
			script.append("set xlabel 'u - g'").append(OSChecker.newline);
			script.append("set ylabel 'M_{U}'").append(OSChecker.newline);
			
			script.append("plot '-' u 1:2 notitle w d lc rgb 'black', ");
			script.append("'-' u 1:2 w l lc rgb '"+colour+"' title '"+atm+" ["+masses[0]+" -> "+masses[masses.length-1]+" M_{☉}]'");
			for(int m=1; m<masses.length; m++) {
				script.append(", '-' u 1:2 w l lc rgb '"+colour+"' notitle");
			}
			script.append(OSChecker.newline);
			
			for(GaiaSdssSource wd : wds) {
				script.append((wd.u - wd.g) + " " + wd.u).append(OSChecker.newline);
			}
			script.append("e").append(OSChecker.newline);
			
			for(int m=0; m<masses.length; m++) {
				for(double tcool=0; tcool<12e9; tcool += 1e7) {
					double u = wdModels.quantity(tcool, masses[m], atm, Filter.SDSS_U);
					double g = wdModels.quantity(tcool, masses[m], atm, Filter.SDSS_G);
					script.append((u - g) + " " + u).append(OSChecker.newline);
				}
				script.append("e").append(OSChecker.newline);
			}
			
			try {
				Gnuplot.displayImage(Gnuplot.executeScript(script.toString()));
			} catch (IOException e) {
				System.out.println("Problem making HR diagram");
			}
		}
	}

	/**
	 * TODO: comment
	 */
	private static void displayMassDistribution(int[] massHist, double massMin, double massStep, String title) {

		StringBuilder script = new StringBuilder();
		script.append("set terminal pngcairo enhanced color size 512,512").append(OSChecker.newline);
		script.append("set xrange [*:*]").append(OSChecker.newline);
		script.append("set yrange [0:*]").append(OSChecker.newline);
//		script.append("set yrange [*:*] reverse").append(OSChecker.newline);
		
		script.append("set key off").append(OSChecker.newline);
		script.append("set xtics out").append(OSChecker.newline);
		script.append("set ytics out").append(OSChecker.newline);
		script.append("set xlabel 'WD mass [M_{☉}]'").append(OSChecker.newline);
		script.append("set ylabel 'Number'").append(OSChecker.newline);
		
		script.append("set title '"+title+"'").append(OSChecker.newline);
		
		script.append("set style fill transparent solid 0.5 noborder").append(OSChecker.newline);
		script.append("set boxwidth 0.95 relative").append(OSChecker.newline);
		
		script.append("plot '-' with boxes lc rgb 'green' notitle").append(OSChecker.newline);
		
		for(int i=0; i<massHist.length; i++) {
			double mass = massMin + i*massStep + massStep/2.0;
			script.append(String.format("%f\t%d\n", mass, massHist[i]));
		}
		script.append("e").append(OSChecker.newline);
		
		
		try {
			Gnuplot.displayImage(Gnuplot.executeScript(script.toString()));
		} catch (IOException e) {
			System.out.println("Problem making HR diagram");
		}
	}

	/**
	 * TODO: comment
	 */
	private static void displayAgeDistribution(double[] ageHist, double ageMin, double ageStep, String title) {

		StringBuilder script = new StringBuilder();
		script.append("set terminal pngcairo enhanced color size 512,512").append(OSChecker.newline);
		script.append("set xrange [*:*]").append(OSChecker.newline);
		script.append("set yrange [0:*]").append(OSChecker.newline);
		
		script.append("set key off").append(OSChecker.newline);
		script.append("set xtics out").append(OSChecker.newline);
		script.append("set ytics out").append(OSChecker.newline);
		script.append("set xlabel 'Lookback time [Gyr]'").append(OSChecker.newline);
		script.append("set ylabel 'Number'").append(OSChecker.newline);
		
		script.append("set title '"+title+"'").append(OSChecker.newline);
		
		script.append("set style fill transparent solid 0.5 noborder").append(OSChecker.newline);
		script.append("set boxwidth 0.95 relative").append(OSChecker.newline);
		
		script.append("plot '-' with l lc rgb 'red' notitle").append(OSChecker.newline);
		
		for(int i=0; i<ageHist.length; i++) {
			double age = ageMin + i*ageStep + ageStep/2.0;
			// Plot as Gyr
			age /= 1e9;
			
			script.append(String.format("%f\t%f\n", age, ageHist[i]));
		}
		script.append("e").append(OSChecker.newline);
		
		
		try {
			Gnuplot.displayImage(Gnuplot.executeScript(script.toString()));
		} catch (IOException e) {
			System.out.println("Problem making age distribution");
		}
	}
	
	
	
	
	
}
