package projects.gaia.exec;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import numeric.minimisation.nllsq.algo.LevenbergMarquardt;
import photometry.Filter;
import sfr.algoimpl.ConstantSFR;
import sfr.algoimpl.SingleBurstSFR;
import wd.models.infra.WdCoolingModels;
import wd.wdlf.algo.BaseWdlf;
import wd.wdlf.algoimpl.ModelWDLF;
import wd.wdlf.algoimpl.ObservedWdlf;
import wd.wdlf.modelling.infra.ModellingState;
import wd.wdlf.modelling.infra.MonteCarloWDLFSolver;
import wd.wdlf.modelling.infra.WDLFSolver;

/**
 * This class provides an application that is used to fit a simulated WDLF to an observed
 * one by varying the parameters of the star formation rate model.
 *
 *
 * @author nrowell
 * @version $Id$
 */
public class WdlfFitter {
	
	/**
	 * Main application entry point.
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		// Top level directory for output WDLFs
		File outputDir = new File("/home/nrowell/Temp/tmp");
		
		fitThinDisk(outputDir);
		fitThickDisk(outputDir);
		fitSpheroid(outputDir);
	}
	
	/**
	 * Method to fit the thin disk WDLF
	 * @throws IOException
	 */
	public static void fitThinDisk(File outputDir) throws IOException {
		
		BufferedWriter out = new BufferedWriter(new FileWriter(new File(outputDir, "thin_disk_model.txt")));
		
		// The observed WDLF to be fitted
		BaseWdlf obsWdlf = ObservedWdlf.Rowell2011_Veff_ThinDisk.wdlf;
		
		// ModellingState object used to encapsulate the data required for modelling the WDLF
		final ModellingState state = new ModellingState();
		
		state.params.setFilter(Filter.M_BOL);
		state.params.setWdCoolingModels(WdCoolingModels.RENEDO);
		
		// Set the bins of the simulated WDLF equal to the bins of the observed WDLF
		state.wdlfBinCentres = obsWdlf.density.getBinCentres();
		state.wdlfBinWidths = obsWdlf.density.getBinWidths();
		state.n_WDs = 200000;
		
		// Create the star formation rate model to be fitted
		final ConstantSFR sfr = new ConstantSFR(0.0, 9.0e9, 1.5E-12);
		
		state.syntheticSFR = sfr;
		
		final WDLFSolver solver = new MonteCarloWDLFSolver();
		
		// Compute the initial guess model
        ModelWDLF syntheticWDLF = solver.calculateWDLF(state);
		
        // Specify units per bin, as the WDLF is computed in those units already
        out.write(syntheticWDLF.density.print(false));
		out.newLine();
		out.newLine();
		
		// Build an implementation of the LevenbergMarquardt
        LevenbergMarquardt lma = new LevenbergMarquardt() {

			@Override
			public double[] getModel(double[] params) {
				
				state.syntheticSFR.setParams(params);
				
				ModelWDLF syntheticWDLF = solver.calculateWDLF(state);
				
				// Extract the model WDLF points
				double[] model = new double[syntheticWDLF.density.getBinCentres().length];
				
				for(int bin=0; bin<model.length; bin++) {
					model[bin] = syntheticWDLF.density.getBinContents(bin);
				}
				
				return model;
			}

			@Override
			public double[][] getJacobian(double[] params) {
				throw new RuntimeException("Naughty naughty!");
			}
			
			@Override
			public boolean useFiniteDifferencesJacobian() {
				return true;
			}
			
			@Override
			public double[] finiteDifferencesStepSizePerParam() {
				return new double[]{1e-14, 1e9};
			}
        };
        
        int n = obsWdlf.density.getBinCentres().length;
        double[] data = new double[n];
        double[][] cov = new double[n][n];
        for(int d=0; d<n; d++) {
        	data[d] = obsWdlf.density.getBinContents(d);
        	cov[d][d] = obsWdlf.density.getBinUncertainty(d) * obsWdlf.density.getBinUncertainty(d);
        }
        
        lma.setData(data);
        lma.setCovariance(cov);
        lma.setInitialGuessParameters(sfr.getParams());
        
        // Perform the optimization
        lma.fit(500, true);
        
        // Extract the solution
        double[] solution = lma.getParametersSolution();
        
        out.write("# Final converged SFR parameters:\n");
        out.write(String.format("# Age  = %6.3e\n", solution[1]));
        out.write(String.format("# Rate = %6.3e\n", solution[0]));
        
        // Set the SFR params and generate the final model
        sfr.setParams(solution);
		syntheticWDLF = solver.calculateWDLF(state);

        out.write(syntheticWDLF.density.print(false));
		out.newLine();
		out.newLine();
		
		// Regenerate the final model at high resolution
		double magMin = 2.0;
		double magMax = 17.0;
		double magStep = 0.1;
		int magBins = (int)((magMax - magMin)/magStep);
		
		double[] highResBinCentres = new double[magBins];
		double[] highResBinWidths = new double[magBins];
		for(int i=0; i<magBins; i++) {
			double mag = magMin + i*magStep + magStep/2.0;
			highResBinCentres[i] = mag;
			highResBinWidths[i] = magStep;
		}
		
		state.wdlfBinCentres = highResBinCentres;
		state.wdlfBinWidths = highResBinWidths;
		// Increase number of WDs
		state.n_WDs = 2000000;
		
		syntheticWDLF = solver.calculateWDLF(state);

        out.write(syntheticWDLF.density.print(false));
		out.newLine();
		out.newLine();
		
		// Now regenerate the model for the G band
		state.params.setFilter(Filter.G_NOM_DR2);

		syntheticWDLF = solver.calculateWDLF(state);

        out.write(syntheticWDLF.density.print(false));
		
        out.close();
	}
	
	/**
	 * Method to fit the thick disk WDLF
	 * @throws IOException
	 */
	public static void fitThickDisk(File outputDir) throws IOException {
		
		BufferedWriter out = new BufferedWriter(new FileWriter(new File(outputDir, "thick_disk_model.txt")));
		
		// The observed WDLF to be fitted
		BaseWdlf obsWdlf = ObservedWdlf.Rowell2011_Veff_ThickDisk.wdlf;
		
		// ModellingState object used to encapsulate the data required for modelling the WDLF
		final ModellingState state = new ModellingState();
		
		state.params.setFilter(Filter.M_BOL);
		state.params.setWdCoolingModels(WdCoolingModels.RENEDO);
		
		// Set the bins of the simulated WDLF equal to the bins of the observed WDLF
		state.wdlfBinCentres = obsWdlf.density.getBinCentres();
		state.wdlfBinWidths = obsWdlf.density.getBinWidths();
		state.n_WDs = 200000;
		
		// Create the star formation rate model to be fitted
		final ConstantSFR sfr = new ConstantSFR(0.0, 11.0e9, 1.5E-12);
		
		state.syntheticSFR = sfr;
		
		final WDLFSolver solver = new MonteCarloWDLFSolver();
		
		// Compute the initial guess model
        ModelWDLF syntheticWDLF = solver.calculateWDLF(state);
		
        // Specify units per bin, as the WDLF is computed in those units already
        out.write(syntheticWDLF.density.print(false));
		out.newLine();
		out.newLine();
		
		// Build an implementation of the LevenbergMarquardt
        LevenbergMarquardt lma = new LevenbergMarquardt() {

			@Override
			public double[] getModel(double[] params) {
				
				state.syntheticSFR.setParams(params);
				
				ModelWDLF syntheticWDLF = solver.calculateWDLF(state);
				
				// Extract the model WDLF points
				double[] model = new double[syntheticWDLF.density.getBinCentres().length];
				
				for(int bin=0; bin<model.length; bin++) {
					model[bin] = syntheticWDLF.density.getBinContents(bin);
				}
				
				return model;
			}

			@Override
			public double[][] getJacobian(double[] params) {
				throw new RuntimeException("Naughty naughty!");
			}
			
			@Override
			public boolean useFiniteDifferencesJacobian() {
				return true;
			}
			
			@Override
			public double[] finiteDifferencesStepSizePerParam() {
				return new double[]{1e-14, 1e9};
			}
        };
        
        int n = obsWdlf.density.getBinCentres().length;
        double[] data = new double[n];
        double[][] cov = new double[n][n];
        for(int d=0; d<n; d++) {
        	data[d] = obsWdlf.density.getBinContents(d);
        	cov[d][d] = obsWdlf.density.getBinUncertainty(d) * obsWdlf.density.getBinUncertainty(d);
        }
        
        lma.setData(data);
        lma.setCovariance(cov);
        lma.setInitialGuessParameters(sfr.getParams());
        
        // Perform the optimization
        lma.fit(500, true);
        
        // Extract the solution
        double[] solution = lma.getParametersSolution();
        
        out.write("# Final converged SFR parameters:\n");
        out.write(String.format("# Age  = %6.3e\n", solution[1]));
        out.write(String.format("# Rate = %6.3e\n", solution[0]));
        
        // Set the SFR params and generate the final model
        sfr.setParams(solution);
		syntheticWDLF = solver.calculateWDLF(state);

        out.write(syntheticWDLF.density.print(false));
		out.newLine();
		out.newLine();
		
		// Regenerate the final model at high resolution
		double magMin = 2.0;
		double magMax = 17.0;
		double magStep = 0.1;
		int magBins = (int)((magMax - magMin)/magStep);
		
		double[] highResBinCentres = new double[magBins];
		double[] highResBinWidths = new double[magBins];
		for(int i=0; i<magBins; i++) {
			double mag = magMin + i*magStep + magStep/2.0;
			highResBinCentres[i] = mag;
			highResBinWidths[i] = magStep;
		}
		
		state.wdlfBinCentres = highResBinCentres;
		state.wdlfBinWidths = highResBinWidths;
		// Increase number of WDs
		state.n_WDs = 2000000;
		
		syntheticWDLF = solver.calculateWDLF(state);

        out.write(syntheticWDLF.density.print(false));
		out.newLine();
		out.newLine();
		
		// Now regenerate the model for the G band
		state.params.setFilter(Filter.G_NOM_DR2);

		syntheticWDLF = solver.calculateWDLF(state);

        out.write(syntheticWDLF.density.print(false));
		
        out.close();
	}
	
	/**
	 * Method to fit the thick disk WDLF
	 * @throws IOException
	 */
	public static void fitSpheroid(File outputDir) throws IOException {
		
		BufferedWriter out = new BufferedWriter(new FileWriter(new File(outputDir, "spheroid_model.txt")));
		
		// The observed WDLF to be fitted
		BaseWdlf obsWdlf = ObservedWdlf.Rowell2011_Veff_Spheroid.wdlf;
		
		// ModellingState object used to encapsulate the data required for modelling the WDLF
		final ModellingState state = new ModellingState();
		
		state.params.setFilter(Filter.M_BOL);
		state.params.setWdCoolingModels(WdCoolingModels.RENEDO);
		
		// Set the bins of the simulated WDLF equal to the bins of the observed WDLF
		state.wdlfBinCentres = obsWdlf.density.getBinCentres();
		state.wdlfBinWidths = obsWdlf.density.getBinWidths();
		state.n_WDs = 200000;
		
		// Create the star formation rate model to be fitted
		final SingleBurstSFR sfr = new SingleBurstSFR(12e9, 5e8, 1.5E-13);
		
		state.syntheticSFR = sfr;
		
		final WDLFSolver solver = new MonteCarloWDLFSolver();
		
		// Compute the initial guess model
        ModelWDLF syntheticWDLF = solver.calculateWDLF(state);
		
        // Specify units per bin, as the WDLF is computed in those units already
        out.write(syntheticWDLF.density.print(false));
		out.newLine();
		out.newLine();
		
		// Build an implementation of the LevenbergMarquardt
        LevenbergMarquardt lma = new LevenbergMarquardt() {

			@Override
			public double[] getModel(double[] params) {
				
				state.syntheticSFR.setParams(params);
				
				ModelWDLF syntheticWDLF = solver.calculateWDLF(state);
				
				// Extract the model WDLF points
				double[] model = new double[syntheticWDLF.density.getBinCentres().length];
				
				for(int bin=0; bin<model.length; bin++) {
					model[bin] = syntheticWDLF.density.getBinContents(bin);
				}
				
				return model;
			}

			@Override
			public double[][] getJacobian(double[] params) {
				throw new RuntimeException("Naughty naughty!");
			}
			
			@Override
			public boolean useFiniteDifferencesJacobian() {
				return true;
			}
			
			@Override
			public double[] finiteDifferencesStepSizePerParam() {
				return new double[]{1e-14, 5e7, 5e7};
			}
        };
        
        int n = obsWdlf.density.getBinCentres().length;
        double[] data = new double[n];
        double[][] cov = new double[n][n];
        for(int d=0; d<n; d++) {
        	data[d] = obsWdlf.density.getBinContents(d);
        	cov[d][d] = obsWdlf.density.getBinUncertainty(d) * obsWdlf.density.getBinUncertainty(d);
        }
        
        lma.setData(data);
        lma.setCovariance(cov);
        lma.setInitialGuessParameters(sfr.getParams());
        
        // Perform the optimization
        lma.fit(500, true);
        
        // Extract the solution
        double[] solution = lma.getParametersSolution();
        
        out.write("# Final converged SFR parameters:\n");
        out.write(String.format("# Start = %6.3e\n", solution[0]));
        out.write(String.format("# End   = %6.3e\n", solution[1]));
        out.write(String.format("# Rate  = %6.3e\n", solution[2]));
        
        // Set the SFR params and generate the final model
        sfr.setParams(solution);
		syntheticWDLF = solver.calculateWDLF(state);

        out.write(syntheticWDLF.density.print(false));
		out.newLine();
		out.newLine();
		
		// Regenerate the final model at high resolution
		double magMin = 2.0;
		double magMax = 17.0;
		double magStep = 0.1;
		int magBins = (int)((magMax - magMin)/magStep);
		
		double[] highResBinCentres = new double[magBins];
		double[] highResBinWidths = new double[magBins];
		for(int i=0; i<magBins; i++) {
			double mag = magMin + i*magStep + magStep/2.0;
			highResBinCentres[i] = mag;
			highResBinWidths[i] = magStep;
		}
		
		state.wdlfBinCentres = highResBinCentres;
		state.wdlfBinWidths = highResBinWidths;
		// Increase number of WDs
		state.n_WDs = 2000000;
		
		syntheticWDLF = solver.calculateWDLF(state);

        out.write(syntheticWDLF.density.print(false));
		out.newLine();
		out.newLine();
		
		// Now regenerate the model for the G band
		state.params.setFilter(Filter.G_NOM_DR2);

		syntheticWDLF = solver.calculateWDLF(state);

        out.write(syntheticWDLF.density.print(false));
		
        out.close();
	}
}
