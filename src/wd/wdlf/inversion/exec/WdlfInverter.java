package wd.wdlf.inversion.exec;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import infra.os.OSChecker;
import wd.wdlf.infra.EntryForm;
import wd.wdlf.infra.EntryFormResult;
import wd.wdlf.infra.ModellingParametersForm;
import wd.wdlf.inversion.gui.InitialSfrForm;
import wd.wdlf.inversion.gui.WelcomeForm;
import wd.wdlf.inversion.gui.InversionForm;
import wd.wdlf.inversion.gui.WdlfInputForm;
import wd.wdlf.inversion.infra.InversionState;

/**
 * TODO: compare mass distribution with observed distribution of field WD mass
 * TODO: add plot of residuals between WD model and observation
 * TODO: add plot of the chi-squared trend as a function of the iteration number
 * TODO: add tooltiptext for each set of WD/MS models etc for use in the GUI forms
 * 
 * 
 * 
 * 
 * 
 * To do:   
 *          Maybe do something with WhiteDwarfs.getAgeDistributions and
 *          WhiteDwarfs.getWDExtrapolatedFractionChart. These could be useful
 *          diagnostic function.
 *          
 * Things that could be improved:
 * Use of extrapolation scheme to improve accuracy of numerical integrations at various points.
 * 
 * @author nickrowell
 */
public class WdlfInverter extends JPanel {
	
    /**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 5831553151279215720L;

	/**
	 * Main {@link InversionState} instance that encapsulates all parameters of inversion algorithm.
	 */
    InversionState inversionState;
    
    /**
     * Array of all {@link EntryForm}s that are to be presented sequentially to
     * the user to allow configuration of the algorithm.
     */
    EntryForm[] forms;
    
    /**
     * Strings containing names for each form, for retrieving them from the card layout structure.
     */
    String[] names;
    
    /**
     * Index into forms array of current form.
     */
    int formIdx = 0;
    
    /**
     * JPanel presenting the entry forms.
     */
    JPanel formPanel;
    
    /** 
     * Main constructor.
     */
    public WdlfInverter()
    {
        
        // Initialise the main {@link InversionState} instance
        inversionState = new InversionState();
        
        // Intialise each form that will manipulate the inversion state in sequence
        final WelcomeForm welcomeForm = new WelcomeForm(inversionState);
        final ModellingParametersForm paramsForm = new ModellingParametersForm(inversionState);
        final InitialSfrForm sfrForm = new InitialSfrForm(inversionState);
        final WdlfInputForm obsWdlfForm =  new WdlfInputForm(inversionState);
        final InversionForm inversionForm = new InversionForm(inversionState);
        
        forms = new EntryForm[]{welcomeForm, paramsForm, sfrForm, obsWdlfForm, inversionForm};
        names = new String[]{"WelcomeForm", "ParamsForm", "SfrForm", "ObsWdlfForm", "InvForm"};
        
        formPanel = new JPanel(new CardLayout());
        
        // Add forms to the CardLayout like so:
        formPanel.add(forms[0], names[0]);
        formPanel.add(forms[1], names[1]);
        formPanel.add(forms[2], names[2]);
        formPanel.add(forms[3], names[3]);
        formPanel.add(forms[4], names[4]);
        
        // Add button to click when form is complete
        final JButton rightButton = new JButton("Next >>");
        final JButton leftButton = new JButton("<< Previous");
        
        rightButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
            	
            	// Verify the current form
            	EntryFormResult input = forms[formIdx].verify();
            	if(!input.valid){
            		// Display error message and take no action
                    JOptionPane.showMessageDialog(WdlfInverter.this, input.message, "Input Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            	
            	if(formIdx==4) {
            		// Start the inversion; disable both buttons
            		leftButton.setEnabled(false);
            		rightButton.setEnabled(false);
            		inversionForm.start();
            		return;
            	}
            	
            	formIdx++;
            	
            	// Initialise the next form
            	forms[formIdx].initialise();
            	
            	// Present the next form
            	CardLayout cl = (CardLayout)(formPanel.getLayout());
                cl.show(formPanel, names[formIdx]);
            	
            	if(formIdx==4) {
            		// Just advanced to the inversion form; change the text on the right button
            		// to reflect the changed behaviour
            		rightButton.setText("Start inversion!");
            	}
            	else if(formIdx==1) {
            		// Just advanced off the first form; enable the left button
            		leftButton.setEnabled(true);
            	}
            }
        });
        leftButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {

            	formIdx--;

            	// Present the previous form
            	CardLayout cl = (CardLayout)(formPanel.getLayout());
                cl.show(formPanel, names[formIdx]);
                
            	if(formIdx==0) {
            		// Returned to first form; disable left button
            		leftButton.setEnabled(false);
            	}
            	else if(formIdx==3) {
            		// Moved off the inversion form to the previous one; reset the text on the right button
            		rightButton.setText("Next >>");
            	}
            }
        });
        
        // Left button starts off at inactive
        leftButton.setEnabled(false);
        
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        buttonPanel.add(leftButton);
        buttonPanel.add(rightButton);
        
        this.setLayout(new BorderLayout());
        this.add(formPanel, BorderLayout.CENTER);
        this.add(buttonPanel, BorderLayout.SOUTH);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) 
    {
    	
        // Check that platform is supported
        OSChecker.checkOS();
    	
        final JFrame frame = new JFrame("WDLF inversion GUI");
    	
		SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                frame.setLayout(new FlowLayout());
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.add(new WdlfInverter());
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }
        });
    }
}