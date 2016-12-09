package sfr.infra;

import sfr.algo.BaseSfr;
import sfr.gui.BaseSfrPanel;
import sfr.gui.ConstantSfrPanel;
import sfr.gui.ExponentialDecaySfrPanel;
import sfr.gui.FractalSfrPanel;
import sfr.gui.FreeformSfrPanel;
import sfr.gui.SingleBurstSfrPanel;

/**
 * Enumerated type to represent available Star Formation Rate models.
 * 
 * The SFR encapsulates a BaseSfrPanel which in turn encapsulates a BaseSfr. This architecture allows
 * us to maintain linked instances of the BaseSfr and a panel used to manipulate it's internal parameters.
 *
 * @author nrowell
 * @version $Id$
 */
public enum SFR {

	/**
	 * All available {@link BaseSfrPanel} implementations encapsulated in a corresponding {@link SFR}
	 */
	CONSTANT(new ConstantSfrPanel()),
    EXPDECAY(new ExponentialDecaySfrPanel()),
    SINGLEBURST(new SingleBurstSfrPanel()),
    FRACTAL(new FractalSfrPanel()),
    FREEFORM(new FreeformSfrPanel());

	/**
	 * The {@link BaseSfrPanel} that backs this {@link SFR}
	 */
    private BaseSfrPanel sfrPanel;
    
    /**
     * Main constructor
     * @param sfr
     * 	The {@link BaseSfrPanel} that backs this {@link SFR}
     */
    SFR(BaseSfrPanel sfr) {
    	this.sfrPanel = sfr;
    }
    
    /**
     * Get the {@link BaseSfr} that backs this {@link SFR}
     * @return
     *  The {@link BaseSfr} that backs this {@link SFR}
     */
    public BaseSfr getSfr() {
    	return sfrPanel.getSfr();
    }
    
    /**
     * Get the {@link BaseSfrPanel} that backs this {@link SFR}
     * @return
     *  The {@link BaseSfrPanel} that backs this {@link SFR}
     */
    public BaseSfrPanel getSfrPanel() {
    	return sfrPanel;
    }
    
    /*
     * (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
    	return sfrPanel.getSfr().getName();
    }
}