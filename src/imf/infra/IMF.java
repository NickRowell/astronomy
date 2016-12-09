package imf.infra;

import imf.algo.BaseImf;
import imf.algoimpl.IMF_Kroupa;
import imf.algoimpl.IMF_PowerLaw;

/**
 * Enumerated type to represent the available Initial Mass Function implementations.
 *
 * @author nrowell
 * @version $Id$
 */
public enum IMF {
	
	/**
	 * All available {@link BaseImf} implementations encapsulated in a corresponding {@link IMF}
	 */
	KROUPA(new IMF_Kroupa()),
    POWER_LAW(new IMF_PowerLaw());

	/**
	 * The {@link BaseImf} that backs this {@link IMF}
	 */
    private BaseImf imf;
    
    /**
     * Main constructor
     * @param imf
     * 	The {@link BaseImf} that backs this {@link IMF}
     */
    IMF(BaseImf imf) {
    	this.imf = imf;
    }
    
    /**
     * Get the {@link BaseImf} that backs this {@link IFMF
     * @return
     *  The {@link BaseImf} that backs this {@link IMF}
     */
    public BaseImf getIMF() {
    	return imf;
    }
    
    /*
     * (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
    	return imf.toString();
    }
	
}
