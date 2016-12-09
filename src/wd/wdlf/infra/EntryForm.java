package wd.wdlf.infra;

import javax.swing.JPanel;

/**
 * Base class for GUI entry forms. Defines the contract that implementations must adhere to.
 *
 * @author nrowell
 * @version $Id$
 */
public abstract class EntryForm extends JPanel
{
    /**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = -1815009588407609434L;

	/**
     * Check that all configurable fields have valid values.
     * @return
     * 	An {@link EntryFormResult} that encapsulates the parsed values of any relevant fields,
     * as well as a flag that indicates whether the values are valid or not and a message containing
     * a description of any invalid fields.
     */
    public abstract EntryFormResult verify();
	
    /**
     * This method is called when a form is presented to the user; it tells the form to load the
     * contents of any displayed/plots in order to present the current values to the user. Because
     * of the backwards dependency model (where the configurable values of a form determine certain
     * fixed values etc for later forms only) this means that the later forms must be intialised when
     * they are presented to the user (and not e.g. on construction). This also allows the user to
     * navigate backwards, change a value, then navigate forwards and see the relevant fields updated.
     */
	public abstract void initialise();
}
