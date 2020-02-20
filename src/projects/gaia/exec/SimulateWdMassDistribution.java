package projects.gaia.exec;

import java.io.IOException;
import java.util.logging.Logger;

import ifmr.algo.BaseIfmr;
import ifmr.infra.IFMR;
import imf.algo.BaseImf;
import imf.algoimpl.IMF_PowerLaw;
import infra.io.Gnuplot;
import infra.os.OSChecker;
import ms.lifetime.algo.PreWdLifetime;
import ms.lifetime.algoimpl.PreWdLifetime_Padova;
import numeric.stats.Gaussian;
import projects.gaia.util.Gaussian1DMixtureModelFitter;
import sfr.algo.BaseSfr;
import sfr.algoimpl.ConstantSFR;
import sfr.algoimpl.SingleBurstSFR;
import wd.models.algo.WdCoolingModelSet;
import wd.models.algoimpl.WdCoolingModelSet_Montreal;
import wd.models.infra.WdCoolingModels;

/**
 * Class provides an application to simulate the mass distribution for solar neighbourhood white dwarfs.
 *
 *
 * @author nrowell
 * @version $Id$
 */
public class SimulateWdMassDistribution {

	/**
	 * The logger.
	 */
	private static final Logger logger = Logger.getLogger(SimulateWdMassDistribution.class.getName());
	
	/**
	 * Main application entry point.
	 * @param args
	 * 	The command line arguments (ignored)
	 */
	public static void main(String[] args) {
		
		/////////////// Configurable parameters:
		
		// Load the WD models
		WdCoolingModelSet wdModels = new WdCoolingModelSet_Montreal();
		
		// Select IFMR
		BaseIfmr ifmr = IFMR.KALIRAI_2009.getIFMR();
		
		// Select MS lifetime models
		PreWdLifetime preWdLifetimeModels = new PreWdLifetime_Padova();
		
		// Thin disk progenitor metallicity
		double z_thin = 0.0017;
		double y_thin = 0.271;
		
		// Thick disk progenitor metallicity
		double z_thick = 0.0017;
		double y_thick = 0.271;
		
		// Select IMF (Salpeter)
		BaseImf imf = new IMF_PowerLaw();
		
		// Star formation history for the thin disk
		BaseSfr thinDiskSfr = new ConstantSFR(0, 10e9, 1e-12);

		// Star formation history for the thick disk
//		BaseSfr thickDiskSfr = new ConstantSFR(0, 10e9, 1e-12);
		BaseSfr thickDiskSfr = new SingleBurstSFR(10e9, 1e9, 1e-12);
		
		
		// Fraction of thin disk WDs
		double normThinThick = 0.8;
		
		// XXX: Fraction of He atmosphere WDs (not required)
//		double normHeH = 0.36;
		
		// Total number of WDs to simulate
		int nWds = 13100;
		
		// Parameters of the mass distribution
		double massMin = 0.0;
		double massMax = 1.4;
		double massStep = 0.01;
        int nMassBins = (int)Math.floor((massMax - massMin) / massStep);
        int[] massHistThin = new int[nMassBins];
        int[] massHistThick = new int[nMassBins];
		
		////////////////// Derived parameters:
		
		// Number of thin disk & thick disk WDs to simulate:
		int nWdsThin = (int)(nWds * normThinThick);
		int nWdsThick = nWds - nWdsThin;
		
		logger.info("Simulating " + nWdsThin + " thin disk white dwarfs...");
		
		// Generate thin disk stars until we have nWdsThin WDs
		for(int n = 0; n<nWdsThin; ) {
			
			// Draw initial mass
			double ms_mass = imf.drawMass();
			
			// Draw lookback time of formation (present day = 0)
			double t_form = thinDiskSfr.drawCreationTime();
			
			// Progenitor lifetime
			double ms_lifetime = preWdLifetimeModels.getPreWdLifetime(z_thin, y_thin, ms_mass)[0];
			
			// Has the star formed a WD at the present day?
			if(ms_lifetime < t_form) {
				// Yes - WD has formed
				n++;
				
				// Get final mass of WD
				double wd_mass = ifmr.getMf(ms_mass);
				
				int massBin = (int)Math.floor((wd_mass - massMin) / massStep);
				massHistThin[massBin]++;
			}
		}

		logger.info("Simulating " + nWdsThick + " thick disk white dwarfs...");
		
		// Generate thick disk stars until we have nWdsThick WDs
		for(int n = 0; n<nWdsThick; ) {
			
			// Draw initial mass
			double ms_mass = imf.drawMass();
			
			// Draw lookback time of formation (present day = 0)
			double t_form = thickDiskSfr.drawCreationTime();
			
			// Progenitor lifetime
			double ms_lifetime = preWdLifetimeModels.getPreWdLifetime(z_thick, y_thick, ms_mass)[0];
			
			// Has the star formed a WD at the present day?
			if(ms_lifetime < t_form) {
				// Yes - WD has formed
				n++;
				
				// Get final mass of WD
				double wd_mass = ifmr.getMf(ms_mass);
				
				int massBin = (int)Math.floor((wd_mass - massMin) / massStep);
				massHistThick[massBin]++;
			}
		}
		
		displayMassDistribution(massHistThin, massHistThick, massMin, massStep, "WD mass distribution");
	}
	
	/**
	 * TODO: comment
	 */
	private static void displayMassDistribution(int[] massHistThin, int[] massHistThick, double massMin, double massStep, String title) {
		
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
		
		script.append("plot '-' with boxes lc rgb 'green' title 'Thin disk',"
				+ " '-' with boxes lc rgb 'red' title 'Thick disk', "
				+ " '-' w l lc rgb 'black' t 'Total'").append(OSChecker.newline);
		
		for(int i=0; i<massHistThin.length; i++) {
			double mass = massMin + i*massStep + massStep/2.0;
			script.append(String.format("%f\t%d\n", mass, massHistThin[i]));
		}
		script.append("e").append(OSChecker.newline);

		for(int i=0; i<massHistThick.length; i++) {
			double mass = massMin + i*massStep + massStep/2.0;
			script.append(String.format("%f\t%d\n", mass, massHistThick[i]));
		}
		script.append("e").append(OSChecker.newline);

		for(int i=0; i<massHistThick.length; i++) {
			double mass = massMin + i*massStep + massStep/2.0;
			script.append(String.format("%f\t%d\n", mass, massHistThick[i] + massHistThin[i]));
		}
		script.append("e").append(OSChecker.newline);
		
		
		
		try {
			Gnuplot.displayImage(Gnuplot.executeScript(script.toString()));
		} catch (IOException e) {
			System.out.println("Problem making HR diagram");
		}
	}
}
