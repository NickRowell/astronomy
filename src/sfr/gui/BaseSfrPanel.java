package sfr.gui;

import javax.swing.JPanel;

import sfr.algo.BaseSfr;

/**
 * 
 * Base class for all classes that provide a JPanel to configure a SFR model.
 *
 * @author nrowell
 * @version $Id$
 */
public abstract class BaseSfrPanel extends JPanel {
	
	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 4101014412052977520L;
	
	/**
	 * Get the star formation rate model.
	 */
	public abstract BaseSfr getSfr();
	
	/**
	 * Read the form and update the internal SFR model.
	 */
	public abstract void update();
	
	/**
	 * Get a suitable descriptive name for the SFR.
	 * @return
	 * 	Suitable (short) name for the SFR model type.
	 */
	public String getSfrModelName() {
		return getSfr().getName();
	}
}
