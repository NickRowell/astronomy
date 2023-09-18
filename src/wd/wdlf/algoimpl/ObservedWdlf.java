package wd.wdlf.algoimpl;

import photometry.Filter;
import wd.wdlf.algo.BaseWdlf;

/**
 * Enumerated type to represent various specific observed WDLFs.
 * 
 * There should be a file with the same name as the enum declaration that contains the data
 * for the luminosity function as a table with four columns consisting of the luminsity bin
 * centre, bin width, density/number, and uncertainty.
 *
 *
 * @author nrowell
 * @version $Id$
 */
public enum ObservedWdlf {
	
	Harris2006_Vmax_vt30("Harris et al. (2006)", "Disk [v_{max}>30kms^{-1}]", Filter.M_BOL, 0.0),
	Harris2006_Vmax_vt160("Harris et al. (2006)", "Disk [v_{max}>160kms^{-1}]", Filter.M_BOL, 0.0),
	Harris2006_Vmax_vt200("Harris et al. (2006)", "Spheroid [v_{max}>200kms^{-1}]", Filter.M_BOL, 0.0),
	Krzesinski2009("Krzesinski et al. (2009)", "Disk", Filter.M_BOL, 0.0),
	KrzesinskiAndHarris("K09 + H06", "Disk", Filter.M_BOL, 0.0),
	Rowell2011_Vmax_vt30_ThinDisk("Rowell & Hambly (2011)", "Disk [v_{max}>30kms^{-1}]", Filter.M_BOL, 0.0),
	Rowell2011_Vmax_vt200_Spheroid("Rowell & Hambly (2011)", "Spheroid [v_{max}>200kms^{-1}]", Filter.M_BOL, 0.0),
	Rowell2011_Vmax_vt240_Spheroid("Rowell & Hambly (2011)", "Spheroid [v_{max}>240kms^{-1}]", Filter.M_BOL, 0.0),
	Rowell2011_Veff_ThinDisk("Rowell & Hambly (2011)", "Thin Disk", Filter.M_BOL, 0.0),
	Rowell2011_Veff_ThickDisk("Rowell & Hambly (2011)", "Thick Disk", Filter.M_BOL, 0.0),
	Rowell2011_Veff_Spheroid("Rowell & Hambly (2011)", "Spheroid", Filter.M_BOL, 0.0),
	Munn_et_al_2016_fig15("Munn et al (2016)", "Disk", Filter.M_BOL, 0.0),
	Hansen2013_47Tuc("Hansen et al. (2013)", "47 Tuc", Filter.F606W_ACS, 13.32);
    
	public BaseWdlf wdlf;
	
	/**
	 * Main constructor.
	 * @param reference
	 * 	The reference for the WDLF observations (e.g. 'Harris et al (2013)')
	 * @param target
	 * 	The target of the WDLF (e.g. 'Spheroid', '47 Tuc')
	 * @param filter
	 * 	The {@link Filter} in which the luminosity is measured
	 * @param mu
	 * 	The distance modulus
	 */
	ObservedWdlf(String reference, String target, Filter filter, double mu)
	{
		String resourceLocation = "resources/wd/wdlf/"+this.name();
		
		double[][] data = BaseWdlf.parseWdlfDataFromFile(resourceLocation);
		
		wdlf = new BaseWdlf(data[0], data[1], data[2], data[3], mu);
		wdlf.setName(target);
		wdlf.setReference(reference);
		wdlf.setFilter(filter);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String toString()
	{ 
		return wdlf.reference + " - " + wdlf.name;
	}
}
