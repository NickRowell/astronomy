/**
 * This class is used to handle cases where particular formation time bins
 * in SFR model do not produce any white dwarfs in observed range of bolometric
 * magnitude. In these cases the star formation rate is completely 
 * unconstrained. Note that this isn't relevant to simulations of the WDLF, and
 * is only used when inverting the WDLF.
 * 
 * Providing an exception class to deal with these cases allows better feedback
 * between modelling functions and output.
 * 
 */
package wd.wdlf.infra;

public class NoSFRConstraintException extends Exception
{

    /**
	 * The serial version UID
	 */
	private static final long serialVersionUID = -4590125882729111152L;

	/**
     * Creates a new instance of
     * <code>NoSFRConstraintException</code> without detail message.
     */
    public NoSFRConstraintException() {}

    /**
     * Constructs an instance of
     * <code>NoSFRConstraintException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public NoSFRConstraintException(String msg) 
    {
        super(msg);
    }
}
