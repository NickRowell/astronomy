package projects.gaia.exec;

import infra.os.OSChecker;
import photometry.Filter;
import wd.models.algo.WdCoolingModelSet;
import wd.models.infra.WdAtmosphereType;
import wd.models.infra.WdCoolingModels;

public class PrintWdCoolingTracks {
	
	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		
		WdCoolingModelSet wdCoolingModelSet = WdCoolingModels.MONTREAL_NEW_2020.getWdCoolingModels();
		
		System.out.println("\n# 0.6 M_{solar} H atmosphere white dwarf (DA type)");
		System.out.println("# Column 1: Gaia G band magnitude");
		System.out.println("# Column 2: Gaia BP-RP magnitude");
		System.out.println("# Column 3: cooling time (since WD formation) in years");
		for(double tCool = 0; tCool < 10e9; tCool += 0.1e9) {
			
			double g = wdCoolingModelSet.quantity(tCool, 0.6, WdAtmosphereType.H, Filter.G_DR3);
			double bp = wdCoolingModelSet.quantity(tCool, 0.6, WdAtmosphereType.H, Filter.BP_DR3);
			double rp = wdCoolingModelSet.quantity(tCool, 0.6, WdAtmosphereType.H, Filter.RP_DR3);
			
			System.out.println((bp-rp) + "\t" + g + "\t" + tCool);
		}
		
		System.out.println("\n# 0.6 M_{solar} He atmosphere white dwarf (DB type)");
		System.out.println("# Column 1: Gaia G band magnitude");
		System.out.println("# Column 2: Gaia BP-RP magnitude");
		System.out.println("# Column 3: cooling time (since WD formation) in years");
		for(double tCool = 0; tCool <= 10e9; tCool += 0.1e9) {
			
			double g = wdCoolingModelSet.quantity(tCool, 0.6, WdAtmosphereType.He, Filter.G_DR3);
			double bp = wdCoolingModelSet.quantity(tCool, 0.6, WdAtmosphereType.He, Filter.BP_DR3);
			double rp = wdCoolingModelSet.quantity(tCool, 0.6, WdAtmosphereType.He, Filter.RP_DR3);
			
			System.out.println((bp-rp) + "\t" + g + "\t" + tCool);
		}

		System.out.println("\n# 1.0 M_{solar} H atmosphere white dwarf (DA type)");
		System.out.println("# Column 1: Gaia G band magnitude");
		System.out.println("# Column 2: Gaia BP-RP magnitude");
		System.out.println("# Column 3: cooling time (since WD formation) in years");
		for(double tCool = 0; tCool <= 10e9; tCool += 0.1e9) {
			
			double g = wdCoolingModelSet.quantity(tCool, 1.0, WdAtmosphereType.H, Filter.G_DR3);
			double bp = wdCoolingModelSet.quantity(tCool, 1.0, WdAtmosphereType.H, Filter.BP_DR3);
			double rp = wdCoolingModelSet.quantity(tCool, 1.0, WdAtmosphereType.H, Filter.RP_DR3);
			
			System.out.println((bp-rp) + "\t" + g + "\t" + tCool);
		}
		
		System.out.println("\n# 1.0 M_{solar} He atmosphere white dwarf (DB type)");
		System.out.println("# Column 1: Gaia G band magnitude");
		System.out.println("# Column 2: Gaia BP-RP magnitude");
		System.out.println("# Column 3: cooling time (since WD formation) in years");
		for(double tCool = 0; tCool < 10e9; tCool += 0.1e9) {
			
			double g = wdCoolingModelSet.quantity(tCool, 1.0, WdAtmosphereType.He, Filter.G_DR3);
			double bp = wdCoolingModelSet.quantity(tCool, 1.0, WdAtmosphereType.He, Filter.BP_DR3);
			double rp = wdCoolingModelSet.quantity(tCool, 1.0, WdAtmosphereType.He, Filter.RP_DR3);
			
			System.out.println((bp-rp) + "\t" + g + "\t" + tCool);
		}

		System.out.println("\n# 1.2 M_{solar} H atmosphere white dwarf (DA type)");
		System.out.println("# Column 1: Gaia G band magnitude");
		System.out.println("# Column 2: Gaia BP-RP magnitude");
		System.out.println("# Column 3: cooling time (since WD formation) in years");
		for(double tCool = 0; tCool <= 10e9; tCool += 0.1e9) {
			
			double g = wdCoolingModelSet.quantity(tCool, 1.2, WdAtmosphereType.H, Filter.G_DR3);
			double bp = wdCoolingModelSet.quantity(tCool, 1.2, WdAtmosphereType.H, Filter.BP_DR3);
			double rp = wdCoolingModelSet.quantity(tCool, 1.2, WdAtmosphereType.H, Filter.RP_DR3);
			
			System.out.println((bp-rp) + "\t" + g + "\t" + tCool);
		}

		System.out.println("\n# 1.2 M_{solar} He atmosphere white dwarf (DB type)");
		System.out.println("# Column 1: Gaia G band magnitude");
		System.out.println("# Column 2: Gaia BP-RP magnitude");
		System.out.println("# Column 3: cooling time (since WD formation) in years");
		for(double tCool = 0; tCool < 10e9; tCool += 0.1e9) {
			
			double g = wdCoolingModelSet.quantity(tCool, 1.2, WdAtmosphereType.He, Filter.G_DR3);
			double bp = wdCoolingModelSet.quantity(tCool, 1.2, WdAtmosphereType.He, Filter.BP_DR3);
			double rp = wdCoolingModelSet.quantity(tCool, 1.2, WdAtmosphereType.He, Filter.RP_DR3);
			
			System.out.println((bp-rp) + "\t" + g + "\t" + tCool);
		}
		
	}
}
