/**
 * 
 * Name:
 *  WDLFSolver.java
 * 
 * Purpose:
 *  WDLFSolver is the abstract parent class that specifies the interface for
 * different types of WDLF integration methods. It extends SwingWorker class 
 * so that the lengthy integration calculation can run in a separate thread 
 * and avoid the GUI freezing up.
 * 
 * Language:
 * Java
 *
 * Author:
 * Nicholas Rowell
 * 
 */
package wd.wdlf.modelling.infra;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;
import java.util.List;

import wd.wdlf.algoimpl.ModelWDLF;

/**
 * Base type for classes that provide WDLF calculations. This amounts to defining the name of
 * the method used to compute the WDLF, and putting some machinery in place to allow classes invoking
 * WDLFSolvers to get some feedback on the progress of the simulation via a progress variable and
 * PropertyChangeListener interface. This is a lightweight implementation of a property change
 * listener intended to seperate as much as possible the WDLF code from any GUI or other infrastructure.
 *
 * @author nrowell
 * @version $Id$
 */
public abstract class WDLFSolver
{
    /**
     * Perform the WDLF calculation.
     */
    public abstract ModelWDLF calculateWDLF(ModellingState modellingState);
    
    /**
     * List of registered listeners.
     */
    List<PropertyChangeListener> listeners = new LinkedList<>();
    
    /**
     * The current progress of the simulation.
     */
    int progress;
    
    /**
     * Registers a PropertyChangeListener to receive notifications when properties
     * (i.e. the progress) are updated.
     * @param listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
    	listeners.add(listener);
    }
    
    /**
     * Set a value for the progress, and if it corresponds to a change from the existing
     * value then we fire a PropertyChangeEvent to all the registered listeners.
     * @param progress
     */
    protected void setProgress(int progress) 
    {
    	if(this.progress == progress)
    		// No change
    		return;
    	
    	// Change: update value and fire property change event on listeners
    	int oldValue = this.progress;
    	this.progress = progress;
    	
    	PropertyChangeEvent event = new PropertyChangeEvent(this, "progress", oldValue, progress);
    	
    	for(PropertyChangeListener listener : listeners)
    	{
    		listener.propertyChange(event);
    	}
    }
    
}
