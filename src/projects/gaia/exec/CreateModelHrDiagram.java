/*
 * Gaia CU5 DU10
 *
 * (c) 2005-2020 Gaia Data Processing and Analysis Consortium
 *
 *
 * CU5 photometric calibration software is free software; you can redistribute
 * it and/or modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * CU5 photometric calibration software is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this CU5 software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *
 *-----------------------------------------------------------------------------
 */

package projects.gaia.exec;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

import ifmr.algo.BaseIfmr;
import ifmr.algoimpl.Ifmr_Kalirai2008;
import imf.algo.BaseImf;
import imf.algoimpl.IMF_PowerLaw;
import infra.io.Gnuplot;
import infra.os.OSChecker;
import ms.lifetime.algo.PreWdLifetime;
import ms.lifetime.algoimpl.PreWdLifetime_Padova;
import photometry.Filter;
import sfr.algo.BaseSfr;
import sfr.algoimpl.ConstantSFR;
import wd.models.algo.WdCoolingModelSet;
import wd.models.algoimpl.WdCoolingModelSet_BaSTI;
import wd.models.algoimpl.WdCoolingModelSet_LPCODE;
import wd.models.algoimpl.WdCoolingModelSet_Montreal;
import wd.models.infra.WdAtmosphereType;

/**
 * Application used to generate model HR diagram of the WD cooling sequence for a given
 * star formation history.
 *
 *
 * @author nrowell
 * @version $Id$
 */
public class CreateModelHrDiagram {

    
	/**
	 * Main application entry point.
	 * @param args
	 * 	The command line arguments (ignored).
	 */
	public static void main(String[] args) {

		// Star formation rate
		BaseSfr sfr = new ConstantSFR(0.0, 10e9, 1.5e-12);
		
	    // Initial mass function.
		BaseImf imf = new IMF_PowerLaw();
	    
	    // Pre-WD lifetime models
	    PreWdLifetime preWdLifetimeModels = new PreWdLifetime_Padova();
	    
	    // Mean progenitor metallicity (Z)
	    double meanZ = 0.0019;
	    
	    // Mean progenitor Helium content (Y)
	    double meanY = 0.03;
	    
	    // Standard deviation in progenitor metallicity (Z).
	    double sigmaZ = 0.0001;
	    
	    // Standard deviation in progenitor Helium content (Y).
	    double sigmaY = 0.0001;
	    
	    // Initial-Final Mass Relation.
	    BaseIfmr ifmr = new Ifmr_Kalirai2008();
	    
	    // WD cooling models.
	    WdCoolingModelSet wdCoolingModels = new WdCoolingModelSet_Montreal();
//	    WdCoolingModelSet wdCoolingModels = new WdCoolingModelSet_LPCODE();
//	    WdCoolingModelSet wdCoolingModels = new WdCoolingModelSet_BaSTI(true);
	    
	    // Fraction of Hydrogen atmospheres (w_H = n_H/(n_H + n_He)).
	    double w_H = 1.0;
	    
	    Random rand = new Random(System.currentTimeMillis());
	    
	    // Build gnuplot script
	    StringBuilder script = new StringBuilder();
		script.append("set terminal pngcairo enhanced color size 540,840").append(OSChecker.newline);
		script.append("set xrange [-0.7:2]").append(OSChecker.newline);
		script.append("set yrange [8:20] reverse").append(OSChecker.newline);
		script.append("set key off").append(OSChecker.newline);
		script.append("set xtics in").append(OSChecker.newline);
		script.append("set ytics in").append(OSChecker.newline);
		script.append("set xlabel 'BP - RP'").append(OSChecker.newline);
		script.append("set ylabel 'G'").append(OSChecker.newline);
//		script.append("plot '-' u ($2-$3):1 w p pt 5 ps 0.25").append(OSChecker.newline);
		script.append("plot '-' u ($2-$3):1 w d").append(OSChecker.newline);
	    
		// Generate stars
	    for(int i = 0; i<100000; i++) {
	    	
	    	// Draw creation time; this is equal to the total stellar age
	    	double t = sfr.drawCreationTime();
	    	
	    	// Draw progenitor mass
	    	double mass_ms = imf.drawMass();
	    	
	    	// Draw metallicity
	    	double z = meanZ + rand.nextGaussian() * sigmaZ;
	    	double y = meanY + rand.nextGaussian() * sigmaY;
	    	
	    	// Get pre-WD lifetime
	    	double t_ms = preWdLifetimeModels.getPreWdLifetime(z, y, mass_ms)[0];
	    	
	    	// Check if star has formed a WD at the present day
	    	if(t > t_ms) {
	    		
	    		// White dwarf cooling time
	    		double t_wd = t - t_ms;
	    		
	    		// White dwarf mass
	    		double mass_wd = ifmr.getMf(mass_ms);
	    		
	    		// Draw WD atmosphere type
	    		WdAtmosphereType wd_atm = rand.nextFloat() > w_H ? WdAtmosphereType.He : WdAtmosphereType.H;
	    		
	    		// Get white dwarf magnitudes at the present day
	    		double g = wdCoolingModels.mag(t_wd, mass_wd, wd_atm, Filter.G_NOM_DR2);
	    		double bp = wdCoolingModels.mag(t_wd, mass_wd, wd_atm, Filter.BP_NOM_DR2);
	    		double rp = wdCoolingModels.mag(t_wd, mass_wd, wd_atm, Filter.RP_NOM_DR2);
	    		
//	    		double g = wdCoolingModels.mag(t_wd, mass_wd, wd_atm, Filter.B);
//	    		double bp = wdCoolingModels.mag(t_wd, mass_wd, wd_atm, Filter.B);
//	    		double rp = wdCoolingModels.mag(t_wd, mass_wd, wd_atm, Filter.V);
	    		
	    		
	    		// Plot colour-magnitude diagram
				script.append(g + "\t" + bp + "\t" + rp).append(OSChecker.newline);

	    	}
	    }
	    
		script.append("e").append(OSChecker.newline);
	    
	    // Make plot
		try {
			BufferedImage plot = Gnuplot.executeScript(script.toString());
			Gnuplot.displayImage(plot);
			
		} catch (IOException e) {
//			logger.log(Level.SEVERE, "Exception during plotting!", e);
		}
		
	}
}
