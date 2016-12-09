/**
 * 
 * Name:
 *  EntryFormResult.java
 * 
 * Purpose:
 *  Class used to pass results between entry forms & GUI.
 * 
 * Language:
 * Java
 *
 * Author:
 * Nicholas Rowell
 * 
 */
package wd.wdlf.infra;


public class EntryFormResult
{
    /**
     * Validity flag for entries in an EntryForm.
     */
    public boolean valid = true;
    
    /**
     * Contains any error message produced by checking form entries.
     */
    public String message = "";
    
    /**
     * Main constructor.
     */
    public EntryFormResult() {
    	
    }
    
}