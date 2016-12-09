
package wd.wdlf.modelling.exec;


import infra.gui.IPanel;
import infra.io.Gnuplot;
import infra.os.OSChecker;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;

import sfr.gui.SfrPanel;
import wd.wdlf.algoimpl.ModelWDLF;
import wd.wdlf.infra.EntryFormResult;
import wd.wdlf.infra.ModellingParametersForm;
import wd.wdlf.modelling.gui.SurveyTypeForm;
import wd.wdlf.modelling.gui.WDLFBinsForm;
import wd.wdlf.modelling.infra.ModellingState;
import wd.wdlf.modelling.infra.MonteCarloWDLFSolver;
import wd.wdlf.modelling.infra.WDLFSolver;

/**
 * Main application entry point for WDLF modelling.
 * 
 * TODO: make WDLF generation utility class that takes all the inputs and returns a WDLF
 * TODO: instead of each form manipulating the ModellingState fields by reference, they should keep
 *       their own instances which can be read back from the forms and assigned to the ModellingState
 * 		 internal fields when the simulation button is pressed.
 * 
 * Notes:
 *  - A 'save to disk' button may seem like a good idea. At the moment, the simulation results (the WDLF)
 *    and all inputs (SFR, metallicity, ...) are saved to disk every time a new simulation is performed
 *    by hitting the 'Calculate WDLF >>' button. This checks all fields of the ModellingState, and if a
 *    valid set of entries is found it proceeds to compute the WDLF. The problem with a 'save to disk'
 *    button is that the contents of ModellingState can be out of sync what's displayed in the GUI if the
 *    user has changed some options since the last simulation. So if a button is to be added then the structure
 *    must be changed so that the fields of ModellingState are only updated after the button is clicked and
 *    before going into a simulation. This would also require that the various forms don't keep references to
 *    the ModellingState that they can manipulate, but instead maintain their own instances of the specific
 *    fields in ModellingState that they are being used to manipulate. Note that this would probably make
 *    the code more flexible and reusable, so may be a good idea regardless of the 'save to disk' possibility.
 *
 *  - Implements PropertyChangeListener so that it can receive progress updates from the WDLF solver,
 *    which are passed as PropertyChangeEvents.
 *
 * @author nrowell
 * @version $Id$
 */
public class WdlfModeller extends JFrame implements PropertyChangeListener
{
	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The version string.
	 */
	public static String version = "1.02";
	
    /**
     * Main ModellingState object. This contains all parameters required to perform a WDLF simulation.
     */
    ModellingState modellingState;
    
    /**
     * GUI frontend to the star formation rate parameter.
     */
    SfrPanel sfrPanel;
    
    /**
     * GUI frontend to various input physics parameters.
     */
    ModellingParametersForm modellingParametersForm;
    
    /**
     * GUI frontend to the survey type ({volume|magnitude} limited).
     */
    SurveyTypeForm surveyTypeForm;
    
    /**
     * GUI frontend to the configuration of WDLF magnitude bins.
     */
    WDLFBinsForm wdlfBinsForm;
    
    
    IPanel luminosityFunctionPanel = new IPanel();
    IPanel ageLuminosityRelationPanel = new IPanel();
    IPanel massLuminosityRelationPanel = new IPanel();
    JProgressBar progressBar = new JProgressBar(0, 100);
    
    /** 
     * Main constructor. 
     */
    public WdlfModeller()
    {
    	// Get location we launched from as the default working directory for temporary files
    	File workingDir = new File("./");
    	
    	// Check that platform is supported...
        switch(OSChecker.getOS())
        {
            case UNIX: break;
            case WINDOWS: break;
            case MAC: break;
            case SOLARIS:
            case UNKNOWN:
                JOptionPane.showMessageDialog(this, "This software has not been "
                        + "tested on "+OSChecker.getOS().toString(),
                        "Input Error", JOptionPane.ERROR_MESSAGE);      
                System.exit(1);
        }
        
        // Check that Gnuplot works...
        if(!Gnuplot.isGnuplotWorking(workingDir))
        {
        	JOptionPane.showMessageDialog(this, "Could not find working GNUplot installation! Check logs.",
                    "Input Error", JOptionPane.ERROR_MESSAGE);      
            System.exit(1);
        }
        
        
    	modellingState = new ModellingState();
    	
        // Get default working directory from location that application was launched from
        modellingState.outputDirectory = new File("./");
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Simulated WDLF Generator "+version);
        setLayout(new BorderLayout());
        
        // Contains GUI elements related to the star formation rate
        sfrPanel = new SfrPanel();
        add(sfrPanel, BorderLayout.NORTH);
        
        // Contains GUI elements relating to
        JPanel wdlfPanel = new JPanel();
        
        modellingParametersForm = new ModellingParametersForm(modellingState);
        surveyTypeForm = new SurveyTypeForm(modellingState);
        wdlfBinsForm = new WDLFBinsForm(modellingState);
        
        // Add button to click when form is complete
        final JButton button = new JButton("Calculate WDLF >>");  
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                calculateWDLF();
            }
        });
        
        // Use a tabbed panel to display various simulation results
        JTabbedPane wdlfModelTabbedPane = new JTabbedPane();
        wdlfModelTabbedPane.setBorder(BorderFactory.createTitledBorder("Simulation Results"));
        wdlfModelTabbedPane.add(luminosityFunctionPanel, "Luminosity Function");
        wdlfModelTabbedPane.add(ageLuminosityRelationPanel, "Age-luminosity relation");
        wdlfModelTabbedPane.add(massLuminosityRelationPanel, "Mass-luminosity relation");
        
        // Progress bar to display WDLF calculation progress
        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        
        // This sub-panel hosts the remaining forms etc and is the second
        // of the two major panels in the main frame. Use gridbag layout
        // to achieve desired appearance.
        JPanel panel1 = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.gridheight = 3;
        c.weighty = 0.0;
        c.anchor = GridBagConstraints.PAGE_START;
        panel1.add(modellingParametersForm, c);

        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weighty = 0.0;
        panel1.add(surveyTypeForm, c);

        c.fill = GridBagConstraints.BOTH;
        c.gridx = 1;
        c.gridy = 3;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weighty = 0.0;
        panel1.add(wdlfBinsForm, c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 2;
        c.gridheight = 1;
        c.weighty = 0.0;
        panel1.add(button, c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 5;
        c.gridwidth = 2;
        c.gridheight = 1;
        c.weighty = 0.1;
        c.anchor = GridBagConstraints.PAGE_END;
        panel1.add(progressBar, c);
            
        // Add two sub-panels to main frame
        wdlfPanel.setLayout(new FlowLayout());
        wdlfPanel.add(panel1);
        wdlfPanel.add(wdlfModelTabbedPane);
        
        add(wdlfPanel, BorderLayout.SOUTH);
        
        

        // Calculate initial WDLF based on default parameters before showing the GUI. This allows
        // GUI components to be sized correctly. Note that we must compute this synchronously so that
        // frame is only packed once the simulation is complete.
     	modellingState.syntheticSFR = sfrPanel.selectedSfrModel.getSfr();
        WDLFSolver solver = new MonteCarloWDLFSolver();
        modellingState.syntheticWDLF = solver.calculateWDLF(modellingState);
        plotWDLF(modellingState.syntheticWDLF);
        
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    /**
     * Listens for changes to progress property of WDLFSolver and updates
     * progress bar in main GUI.
     * @param evt 
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) 
    {
        if ("progress".equals(evt.getPropertyName())) 
        {
            int progress = (Integer) evt.getNewValue();
            progressBar.setValue(progress);
        } 
    }
    
    /**
     * Check user input and commence WDLF simulation.
     */
    private void calculateWDLF()
    {
    
		// Collate error messages from all forms
		StringBuilder message = new StringBuilder();
		 
		// Combine validity results of all form entries
		boolean valid = true;
		 
		// Validate ModellingParametersForm
		EntryFormResult modellingParams = modellingParametersForm.verify();
		valid = valid && modellingParams.valid;
		message.append(modellingParams.message);
		 
		// Validate survey type form (number of stars requested)
		EntryFormResult surveyType = surveyTypeForm.verify();
		valid = valid && surveyType.valid;
		message.append(surveyType.message);
		 
		// Check entries in WDLF text area - magnitude bins.
		EntryFormResult wdlfBins = wdlfBinsForm.verify();
		valid = valid && wdlfBins.valid;
		message.append(wdlfBins.message);
        
		if(valid)
		{	 
			// Get the current active SFR instance and assign it the the SFR field of modellingState
			modellingState.syntheticSFR = sfrPanel.selectedSfrModel.getSfr();
			
			// Get suitable solver:
			final WDLFSolver solver = new MonteCarloWDLFSolver();
			solver.addPropertyChangeListener(this);
			
			// Run the WDLF simulation in a seperate thread (not the event dispatch thread; leave
			// that clear to update the GUI).
			final WdlfModeller temp = this;
			Thread thread = new Thread()
			{
				@Override
				public void run() {
					modellingState.syntheticWDLF = solver.calculateWDLF(modellingState);
					plotWDLF(modellingState.syntheticWDLF);
					// reset progress
					progressBar.setValue(0);
					
		            // Save simulation results to disk.
		            try 
		            {
						modellingState.saveToDisk();
					} 
		            catch (IOException e) 
		            {
		            	JOptionPane.showMessageDialog(temp, e.getMessage(),
								"Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			};
			thread.start();
		}
		else
		{
			// Display error message.
			JOptionPane.showMessageDialog(this, message.toString(),
							"Input Error", JOptionPane.ERROR_MESSAGE);
		}
         
    }
    
    /**
     * Plot the WDLF in the GUI forms.
     */
    private void plotWDLF(ModelWDLF syntheticWDLF)
    {
        try
        {
            // Write Gnuplot script file
            File script = File.createTempFile("obsWDLF", null, modellingState.outputDirectory);
            BufferedWriter out = new BufferedWriter(new FileWriter(script));
            out.write(syntheticWDLF.getLuminosityFunctionGnuplotScript());
            out.close();
            luminosityFunctionPanel.plotGnuplot(script);
            script.delete();
            
            // Plot age-luminosity relation
            script = File.createTempFile("obsAgeLuminosity", null, modellingState.outputDirectory);
            out = new BufferedWriter(new FileWriter(script));
            out.write(syntheticWDLF.getAgeLuminosityRelationGnuplotScript());
            out.close();
            ageLuminosityRelationPanel.plotGnuplot(script);
            script.delete();
            
            // Plot mass-luminosity relation
            script = File.createTempFile("obsMassLuminosity", null, modellingState.outputDirectory);
            out = new BufferedWriter(new FileWriter(script));
            out.write(syntheticWDLF.getMassLuminosityRelationGnuplotScript());
            out.close();
            massLuminosityRelationPanel.plotGnuplot(script);
            script.delete();        
            
            repaint();
            
        }
        catch(IOException ioe)
        { 
        	JOptionPane.showMessageDialog(this, ioe.getMessage(),
					"Error", JOptionPane.ERROR_MESSAGE);
        }
        
        
    }
    
    /** 
     * Main entry point for WDLF modelling program.
     */
    public static void main(String args[]) 
    {
        // Create and display the application main GUI
        java.awt.EventQueue.invokeLater(
                new Runnable() {
                    @Override
                    public void run() {
                        new WdlfModeller();
                    }
                });
    }
}
