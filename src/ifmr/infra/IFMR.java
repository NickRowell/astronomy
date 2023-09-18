package ifmr.infra;

import ifmr.algo.BaseIfmr;
import ifmr.algoimpl.Ifmr_Catalan2008;
import ifmr.algoimpl.Ifmr_Ferrario2005;
import ifmr.algoimpl.Ifmr_Kalirai2008;
import ifmr.algoimpl.Ifmr_Kalirai2009;
import ifmr.algoimpl.Ifmr_Renedo2010_Z0p01;
import ifmr.algoimpl.Ifmr_Cummings2018;;

/**
 * Enumerated type to represent the available Initial-Final Mass Relations.
 *
 * @author nrowell
 * @version $Id$
 */
public enum IFMR {
	
	/**
	 * All available {@link BaseIfmr} implementations encapsulated in a corresponding {@link IFMR}
	 */
	KALIRAI_2009(new Ifmr_Kalirai2009()),
    KALIRAI_2008(new Ifmr_Kalirai2008()),
    FERRARIO_2005(new Ifmr_Ferrario2005()),
    CATALAN_2008(new Ifmr_Catalan2008()),
    RENEDO_2010(new Ifmr_Renedo2010_Z0p01()),
    CUMMINGS_2018(new Ifmr_Cummings2018());

	/**
	 * The {@link BaseIfmr} that backs this {@link IFMR}
	 */
    private BaseIfmr ifmr;
    
    /**
     * Main constructor
     * @param ifmr
     * 	The {@link BaseIfmr} that backs this {@link IFMR}
     */
    IFMR(BaseIfmr ifmr) {
    	this.ifmr = ifmr;
    }
    
    /**
     * Get the {@link BaseIfmr} that backs this {@link IFMR}
     * @return
     *  The {@link BaseIfmr} that backs this {@link IFMR}
     */
    public BaseIfmr getIFMR() {
    	return ifmr;
    }
    
    /*
     * (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
    	return ifmr.toString();
    }
}
