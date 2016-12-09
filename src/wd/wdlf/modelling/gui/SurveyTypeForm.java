/**
 * 
 * Name:
 *  SurveyTypeForm.java
 * 
 * Purpose:
 *  Class provides a GUI interface to set the survey type parameter of a
 * ModellingState object {VOLUME_LIMITED|MAGNITUDE_LIMITED}. Also allows user
 * to specify the number of simulated WDs used to determine the luminosity
 * function.
 * 
 * Language:
 * Java
 *
 * Author:
 * Nicholas Rowell
 * 
 */
package wd.wdlf.modelling.gui;


import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import wd.wdlf.infra.EntryForm;
import wd.wdlf.infra.EntryFormResult;
import wd.wdlf.modelling.infra.ModellingState;
import wd.wdlf.modelling.infra.ModellingState.SurveyType;

public class SurveyTypeForm extends EntryForm {
    
    /**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 2996204465692935869L;

	/**
	 * Handle to  main ModellingState object.
	 */
    ModellingState modellingState;
    
    /**
     * Test field used to enter the number of simulated WDs.
     */
    private JTextField nStarsField;
    
    /**
     * Creates new form surveyTypeForm.
     */
    public SurveyTypeForm(final ModellingState modellingState) 
    {
        this.modellingState = modellingState;
         
        ButtonGroup surveyTypeButtonGroup = new ButtonGroup();
        JRadioButton magLimRadioButton = new JRadioButton();
        JRadioButton volLimRadioButton = new JRadioButton();
        
        setBorder(BorderFactory.createTitledBorder("Select survey type"));
        
        surveyTypeButtonGroup.add(magLimRadioButton);
        magLimRadioButton.setText("Magnitude Limited");
        magLimRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
            	modellingState.surveyType = SurveyType.MAGNITUDE_LIMITED;
            }
        });

        surveyTypeButtonGroup.add(volLimRadioButton);
        volLimRadioButton.setText("Volume Limited");
        volLimRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                modellingState.surveyType = SurveyType.VOLUME_LIMITED;
            }
        });
        
        switch(modellingState.surveyType)
        {
            case MAGNITUDE_LIMITED: 
                magLimRadioButton.setSelected(true);
                volLimRadioButton.setSelected(false);
                break;
            case VOLUME_LIMITED:
                magLimRadioButton.setSelected(false);
                volLimRadioButton.setSelected(true); 
                break;
        }
        
        // Set up number of stars label and text field
        JLabel nStarsLabel = new JLabel("Number of WDs:");
        nStarsField = new JTextField(Long.toString(modellingState.n_WDs));
        nStarsField.setColumns(15);
        JPanel nStarsPanel = new JPanel(new GridLayout(2,1));
        nStarsPanel.add(nStarsLabel);
        nStarsPanel.add(nStarsField);
        
        this.setLayout(new BorderLayout());
        
        add(magLimRadioButton, BorderLayout.NORTH);
        add(volLimRadioButton, BorderLayout.CENTER);
        add(nStarsPanel, BorderLayout.SOUTH);
        
    }
    
    /**
     * Method checks the value entered for 'number of simulated WDs' parameter.
     */
    @Override
    public EntryFormResult verify()
    {
    
        SurveyTypeFormInputVerifier input = new SurveyTypeFormInputVerifier(
                nStarsField.getText());

        // Verify input forms....
        if (input.valid) {

            // Read parsed parameters back.
            modellingState.n_WDs = input.N;

        }
        
        return input;
    }
    
    /** Test function to check appearance of form. */
    public static void main(String[] args){
        
        // Create and display the form
        java.awt.EventQueue.invokeLater(
                new Runnable() 
                    {
                        @Override
                        public void run() 
                        {
                            
                            JFrame tester = new JFrame();
                            tester.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                            tester.add(new SurveyTypeForm(new ModellingState()));
                            tester.setVisible(true);
                            tester.pack();
                            tester.validate();
                            
                        }
                    });
    
    }

	@Override
	public void initialise() {
		// Nothing to initialise
	}  
}

/**
 * Class used to verify value entered in SurveyTypeForm.
 */
class SurveyTypeFormInputVerifier 
extends EntryFormResult 
{
    // Parsed and checked number of stars.
    public int N;
    
    public SurveyTypeFormInputVerifier(String _n)
    {
            
        StringBuilder msg_builder = new StringBuilder();
        
        // Attempt to parse _n
        try
        {
            N = Integer.parseInt(_n);
            if(N<=0)
            {
                msg_builder.append("Number of stars must be positive!\n");
                valid = false;               
            }
            
            // parameter N is good.
        }
        catch(NumberFormatException nfe)
        {
            msg_builder.append("Could not parse number of stars: ").append(_n).append("\n");
            valid = false;
        }      
        
        message = msg_builder.toString();
    
    }

}