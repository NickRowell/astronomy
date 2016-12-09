package wd.wdlf.inversion.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import infra.io.Gnuplot;
import wd.wdlf.dm.State;
import wd.wdlf.infra.EntryForm;
import wd.wdlf.infra.EntryFormResult;
import wd.wdlf.inversion.infra.InversionState;

/**
 * Class implements the welcome form that is presented to the user first, when the
 * WDLF inversion algorithm application is launched.
 * 
 * @author nickrowell
 */
public class WelcomeForm extends EntryForm 
{
    
    /**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 7151748183747228660L;
	
	/**
	 * Reference to the main {@link InversionState} instance.
	 */
    InversionState inversionState;

    /**
     * Button allows user to change the output directory.
     */
    private JButton changeOutputDirButton;
    
    /**
     * Displays the location of the output directory
     */
    private JLabel outputLocLabel;
    
    /**
     * Main constructor
     * @param inversionState
     * 	Reference to the main {@link InversionState} instance.
     */
    public WelcomeForm(InversionState inversionState) 
    {
    	this.inversionState = inversionState;
    	
    	// Welcome message
    	JLabel message = new JLabel(
    			"<html>"
    			+ "<h2>White Dwarf Luminosity Function inversion algorithm</h2>"
    			+ "<br>"
    			+ "This application provides an implementation of the WDLF inversion algorithm "
    			+ "described in Rowell (2013) "
    			+ "<br>"
    			+ "<i>"
    			+ "\"The Star Formation History of the Solar Neighbourhood from the White Dwarf Luminosity Function\" </i>"
    			+ "<br>"
    			+ "You configure and run the algorithm by proceeding through a series of input forms "
    			+ "using the buttons below."
    			+ "<br>"
    			+ "</html>", SwingConstants.CENTER);
    	
    	// Initialise button
    	changeOutputDirButton = new JButton("Select directory");
    	changeOutputDirButton.addActionListener(new ActionListener() {
    		@Override
            public void actionPerformed(ActionEvent evt) {
                JFileChooser chooser = new JFileChooser();
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                chooser.setAcceptAllFileFilterUsed(false);

                int returnVal = chooser.showOpenDialog(WelcomeForm.this);
                if(returnVal == JFileChooser.APPROVE_OPTION) 
                {
                	File newOutputDir = chooser.getSelectedFile();
                	if(newOutputDir!=null) {
                		WelcomeForm.this.inversionState.outputDirectory = newOutputDir;
                		
                		// If the user specifies an alternative directory, then we write all outputs
                		WelcomeForm.this.inversionState.writeOutput = true;
                		initialise();
                	}
                }
            }
        });
    	
    	outputLocLabel = new JLabel("", SwingConstants.LEFT);
    	
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.black), 
        		BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        
        add(message, BorderLayout.CENTER);
        JPanel subPanel = new JPanel();
        subPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory
	            .createTitledBorder("Location for algorithm outputs"), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        subPanel.setLayout(new GridLayout(1,2));
        subPanel.add(changeOutputDirButton);
        subPanel.add(outputLocLabel);
        add(subPanel, BorderLayout.SOUTH);
        
        initialise();
    }
    
    @Override
    public EntryFormResult verify(){
        return new InputVerifier(inversionState);
    }
    
    @Override
    public void initialise() {
    	// Update the label stating the current output directory
    	outputLocLabel.setText("  Current: "+getAbbreviatedFilePath(this.inversionState.outputDirectory, 30));
    }
    
    /**
     * Get the abbreviated full path to a file, for displaying in finite width
     * space.
     * 
     * @param file
     * 	The File
     * @param width
     * 	The maximum width of the returned String in characters
     * @return 
     * 	String containing the abbreviated full path
     */
    private String getAbbreviatedFilePath(File file, int width){
    	
        String output_str = file.getAbsolutePath();
        // Truncate text if file path is too long
        if(output_str.length()>width)
             return "..." + output_str.substring(3 + output_str.length() - width);
        else
            return output_str;
    }
}

/**
 * Class performs verification on the {@link WelcomeForm}. This amounts to checking that
 * we have write access on the output directory, and that Gnuplot is installed & working.
 *
 * @author nrowell
 * @version $Id$
 */
class InputVerifier extends EntryFormResult
{
    /**
     * Main constructor.
     * @param state
     * 	Reference to the main {@link State} instance, whose fields we are verifying.
     */
    public InputVerifier(State state)
    {

        StringBuilder msg_builder = new StringBuilder();
        
        // Check that we can read & write to the specified parent directory.

        // Check that output directory specified
        if (state.outputDirectory == null) 
        {
            msg_builder.append("No output directory specified!\n");
            valid = false;
        } 
        // Check read & write access
        else if (!state.outputDirectory.canWrite()) 
        {
            msg_builder.append("Cannot write to directory ").append(state.outputDirectory.getAbsolutePath()).append("\n");
            valid = false;
        }
        
        // If validity is TRUE at this point, then we can go ahead and write
        // a gnuplot test script to the location and test whether gnuplot is
        // working or not.
        if(valid){
            
            // Write test script & check GNUplot works.
            // Check that Gnuplot works...
            if(!Gnuplot.isGnuplotWorking(state.outputDirectory))
            {
            	valid = false;
            	msg_builder.append("Could not find working GNUplot installation! Check logs.");
            }
        }
        
        message = msg_builder.toString();
    }
    
}