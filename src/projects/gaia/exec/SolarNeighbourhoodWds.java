package projects.gaia.exec;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
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
import numeric.stats.Gaussian;
import photometry.Filter;
import projects.gaia.dm.GaiaSource;
import projects.gaia.util.GaiaSourceUtil;
import projects.gaia.util.Gaussian1DMixtureModelFitter;
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
public class SolarNeighbourhoodWds {
	
	/**
	 * The logger.
	 */
	private static final Logger logger = Logger.getLogger(SolarNeighbourhoodWds.class.getName());
	
	/**
	 * The {@link File} containing the input data (subset of GaiaSource fields) in CSV format.
	 */
	private static File input = new File("/home/nrowell/Astronomy/gaia_WDs/dr2_work/dr2_samples/gaia_only/1524751657269O-result.csv");
	
	/**
	 * Main application entry point.
	 * @param args
	 * 	The command line args (ignored)
	 * @throws IOException
	 * 	If there's a problem writing the results to text files.
	 */
	public static void main(String[] args) throws IOException {
		
		// Load the input catalogue
		Collection<GaiaSource> wds = GaiaSourceUtil.loadGaiaSources(input);
		
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
        
        	// Create output file containing mass and age of each star
        	BufferedWriter out = new BufferedWriter(new FileWriter(new File("/home/nrowell/Astronomy/gaia_WDs/dr2_work/results/" + atm + ".txt")));
        	
	        // Track fitting outcomes
	        int n_success=0, n_fail=0, n_unrealistic=0;
	        
	        // Store masses of each WD
	        List<Double> wdMasses = new LinkedList<>();
	        
	        // Relative weight for this atmosphere type
	        double w = atmWeights.get(atm);
	        
			// Loop over each star
			for(GaiaSource wd : wds) {
				
				// 1) Determine atmosphere type - for now, weight each star 35% He & 65% H
				
				// 2) Use appropriate WD cooling model set to fit mass of WD in G/BP-RP plane
				WdMassFitter fitter = new WdMassFitter(wdModels, Filter.G_REV_DR2, Filter.BP_REV_DR2, Filter.RP_REV_DR2, atm);
				
				double bmr = wd.phot_bp_mean_mag - wd.phot_rp_mean_mag;
				
				// TODO: need to estimate WD absolute G magnitude
				
				double[] data = new double[]{bmr, wd.phot_g_mean_mag};
				double[][] cov = new double[][]{{1.0, 0.0}, {0.0, 1.0}};
				// Initial guess parameters
				double initGuessMass = 0.5;
				double initGuessCoolingTime = wdModels.tcool(wd.m_g, initGuessMass, atm, Filter.G_REV_DR2);
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
					
					wdMasses.add(wdMass);
					
					// Write the results to file
					out.write(wd.m_g + "\t" + bmr + "\t" + wdMass + "\t" + wdCoolingTime + "\n");
					
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
			
			// Fit 2 component Gaussian mixture model to WD masses
			double[] wdMassesArr = new double[wdMasses.size()];
			for(int h=0; h<wdMasses.size(); h++) {
				wdMassesArr[h] = wdMasses.get(h);
			}
			
			Gaussian1DMixtureModelFitter gausFit = new Gaussian1DMixtureModelFitter(wdMassesArr);
			gausFit.invoke();
			
			
			out.close();
			
			System.out.println("("+atm+" atmospheres) Proportion of success/unrealistic/fail = "+n_success + " / " + n_unrealistic + " / " + n_fail + " (of "+wds.size()+")");
			
			displayMassDistribution(gausFit, massHist, massMin, massStep, "WD mass distribution (assuming "+atm+" atmosphere)");
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
	private static void displayHrDiagram(Collection<GaiaSource> wds, WdCoolingModelSet wdModels) {

		for(WdAtmosphereType atm : WdAtmosphereType.values()) {
			
			// Constant-mass tracks to plot in HR diagram
			double[] masses = wdModels.getMassGridPoints(atm);
			
			String colour = atm==WdAtmosphereType.H ? "blue" : "green";
			
			StringBuilder script = new StringBuilder();
			script.append("set terminal pngcairo enhanced color size 512,512").append(OSChecker.newline);
			script.append("set xrange [-0.5:1.75]").append(OSChecker.newline);
			script.append("set yrange [9:17] reverse").append(OSChecker.newline);
			
			script.append("set key top right").append(OSChecker.newline);
			script.append("set xtics out").append(OSChecker.newline);
			script.append("set ytics out").append(OSChecker.newline);
			script.append("unset colorbox").append(OSChecker.newline);
			script.append("set xlabel 'BP - RP'").append(OSChecker.newline);
			script.append("set ylabel 'M_{G}'").append(OSChecker.newline);
			
			script.append("plot '-' u 1:2 notitle w d lc rgb 'black', ");
			script.append("'-' u 1:2 w l lc rgb '"+colour+"' title '"+atm+" ["+masses[0]+" -> "+masses[masses.length-1]+" M_{☉}]'");
			for(int m=1; m<masses.length; m++) {
				script.append(", '-' u 1:2 w l lc rgb '"+colour+"' notitle");
			}
			script.append(OSChecker.newline);
			
			for(GaiaSource wd : wds) {

				double bmr = wd.phot_bp_mean_mag - wd.phot_rp_mean_mag;
				
				script.append(bmr + " " + wd.m_g).append(OSChecker.newline);
			}
			script.append("e").append(OSChecker.newline);
			
			for(int m=0; m<masses.length; m++) {
				for(double tcool=0; tcool<12e9; tcool += 1e7) {
					double g = wdModels.quantity(tcool, masses[m], atm, Filter.G_REV_DR2);
					double bp = wdModels.quantity(tcool, masses[m], atm, Filter.BP_REV_DR2);
					double rp = wdModels.quantity(tcool, masses[m], atm, Filter.RP_REV_DR2);
					script.append((bp - rp) + " " + g).append(OSChecker.newline);
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
	private static void displayMassDistribution(Gaussian1DMixtureModelFitter gausFit, int[] massHist, double massMin, double massStep, String title) {

		// Determine the appropriate scale factor to apply to the Gaussians for the plot
		double fiducialMass = 0.5;
		
		// Sum of the Gaussians at the fiducial mass
		double pdf = gausFit.weight * Gaussian.phi(fiducialMass, gausFit.mean_1, gausFit.std_1) + 
				(1.0 - gausFit.weight) * Gaussian.phi(fiducialMass, gausFit.mean_2, gausFit.std_2);
		
		// Histogram level at the fiducial mass
		double hist = massHist[(int)Math.floor((fiducialMass - massMin) / massStep)];
		
		// Scale factor to apply to Gaussians
		double s = hist / pdf;
		
		StringBuilder script = new StringBuilder();
		script.append("set terminal pngcairo enhanced color size 512,512").append(OSChecker.newline);
		script.append("set xrange [0:*]").append(OSChecker.newline);
		script.append("set yrange [0:*]").append(OSChecker.newline);
		
		script.append("set key top right").append(OSChecker.newline);
		script.append("set xtics out").append(OSChecker.newline);
		script.append("set ytics out").append(OSChecker.newline);
		script.append("set xlabel 'WD mass [M_{☉}]'").append(OSChecker.newline);
		script.append("set ylabel 'Number'").append(OSChecker.newline);
		
		script.append("set title '"+title+"'").append(OSChecker.newline);
		
		script.append("set style fill transparent solid 0.5 noborder").append(OSChecker.newline);
		script.append("set boxwidth 0.95 relative").append(OSChecker.newline);
		
		
		
		script.append("plot '-' with boxes lc rgb 'green' notitle,"
				+ " '-' w l lc rgb 'red' t '"+String.format("w = %.3f <x> = %.3f std = %.3f", gausFit.weight, gausFit.mean_1, gausFit.std_1)+"',"
				+ " '-' w l lc rgb 'blue' t '"+String.format("w = %.3f <x> = %.3f std = %.3f", 1.0 - gausFit.weight, gausFit.mean_2, gausFit.std_2)+"'").append(OSChecker.newline);
		
		for(int i=0; i<massHist.length; i++) {
			double mass = massMin + i*massStep + massStep/2.0;
			script.append(String.format("%f\t%d\n", mass, massHist[i]));
		}
		script.append("e").append(OSChecker.newline);
		
		// Gaussian component 1
		for(int i=0; i<massHist.length; i++) {
			double mass = massMin + i*massStep + massStep/2.0;
			double p = s * gausFit.weight * Gaussian.phi(mass, gausFit.mean_1, gausFit.std_1);
			script.append(String.format("%f\t%f\n", mass, p));
		}
		script.append("e").append(OSChecker.newline);
		
		// Gaussian component 2
		for(int i=0; i<massHist.length; i++) {
			double mass = massMin + i*massStep + massStep/2.0;
			double p = s * (1.0 - gausFit.weight) * Gaussian.phi(mass, gausFit.mean_2, gausFit.std_2);
			script.append(String.format("%f\t%f\n", mass, p));
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
