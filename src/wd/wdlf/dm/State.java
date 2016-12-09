/**
 * Parent class of the InversionState and ModellingState objects. This 
 * gathers the various parameters that these objects have in common, so that
 * certain forms can be used with either type of object.
 * 
 */
package wd.wdlf.dm;

import java.io.File;

public class State {
    /** 
     * Directory to save plots, data files and other diagnostics. This is initialised to the
     * default temporary directory for the system, and can be reset by the user.
     */
    public File outputDirectory = new File(System.getProperty("java.io.tmpdir"));

    /**
     * Input physics for simulations.
     */
    public WdlfModellingParameters params = new WdlfModellingParameters();
 
}
