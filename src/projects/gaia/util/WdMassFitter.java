package projects.gaia.util;

import numeric.minimisation.nllsq.algo.LevenbergMarquardt;
import photometry.Filter;
import wd.models.algo.WdCoolingModelSet;
import wd.models.infra.WdAtmosphereType;

/**
 * Reusable class for fitting WD mass and cooling time given colour and absolute magnitude.
 *
 * @author nrowell
 * @version $Id$
 */
public class WdMassFitter extends LevenbergMarquardt {

	private WdCoolingModelSet wdModels;
	private WdAtmosphereType atm;
	private Filter magFilter;
	private Filter col1Filter;
	private Filter col2Filter;
	
	public WdMassFitter(WdCoolingModelSet wdModels, Filter magFilter, Filter col1Filter, Filter col2Filter, WdAtmosphereType atm) {
		this.wdModels = wdModels;
		this.magFilter = magFilter;
		this.col1Filter = col1Filter;
		this.col2Filter = col2Filter;
		this.atm = atm;
	}
	
	@Override
	public double[] getModel(double[] params) {
		
		double mass = params[0];
		double tcool = params[1];
		
		// Get the colour and absolute magnitude in the given bands
		double mag = wdModels.mag(tcool, mass, atm, magFilter);
		double colour = wdModels.mag(tcool, mass, atm, col1Filter) - wdModels.mag(tcool, mass, atm, col2Filter);
		
		double[] model = new double[]{colour, mag};
		return model;
	}

	@Override
	public boolean useFiniteDifferencesJacobian() {
    	return true;
    }
	
	@Override
	public double[] finiteDifferencesStepSizePerParam() {
		double stepMass = 0.001;
		double stepCoolingTime = 1e6;
		return new double[]{stepMass, stepCoolingTime};
    }

	@Override
	public double[][] getJacobian(double[] params) {
		throw new RuntimeException("Attempted to use analytic Jacobian!");
	}
}
