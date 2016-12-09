package wd.wdlf.modelling.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import javax.swing.JFrame;

import wd.wdlf.modelling.infra.ModellingState;
import wd.wdlf.modelling.infra.ModellingState.SolverType;

/**
 * 
 * Name:
 *  SolverTypeForm.java
 * 
 * Purpose:
 *  Class provides a GUI interface to set the solver type parameter of a
 * ModellingState object {MONTECARLO|TRAPEZIUM}.
 * 
 * Note that this class isn't used currently, because only Monte Carlo solver
 * is implemented.
 * 
 * Language:
 * Java
 *
 * Author:
 * Nicholas Rowell
 * 
 */
public class SolverTypeForm extends javax.swing.JPanel 
{
   
    /**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = -6291437213219176590L;

	/** Handle to main ModellingState object.  */
    ModellingState modellingState;
    
    /** GUI components. */                  
    private javax.swing.JRadioButton monteCarloRadioButton;
    private javax.swing.ButtonGroup surveyTypeButtonGroup;
    private javax.swing.JRadioButton trapeziumRadioButton;
    
    /** Default constructor. */
    public SolverTypeForm()
    {
        modellingState = new ModellingState();
        initComponents();
    }
    
    /** Creates new form surveyTypeForm. */
    public SolverTypeForm(ModellingState _modellingState) 
    {
        modellingState = _modellingState;
        initComponents();
    }
    
    /** Set up the GUI components. */
    public final void initComponents()
    {
        surveyTypeButtonGroup = new javax.swing.ButtonGroup();
        monteCarloRadioButton = new javax.swing.JRadioButton();
        trapeziumRadioButton = new javax.swing.JRadioButton();
        
        setBorder(javax.swing.BorderFactory.createTitledBorder("Select solver type"));
        
        surveyTypeButtonGroup.add(monteCarloRadioButton);
        monteCarloRadioButton.setText("Monte Carlo integrator");
        monteCarloRadioButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                monteCarloRadioButtonActionPerformed(evt);
            }
        });

        surveyTypeButtonGroup.add(trapeziumRadioButton);
        trapeziumRadioButton.setText("Trapezium integrator");
        trapeziumRadioButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                trapeziumRadioButtonActionPerformed(evt);
            }
        });        
        
        
        switch(modellingState.SOLVER)
        {
            case MONTECARLO: 
                monteCarloRadioButton.setSelected(true);
                trapeziumRadioButton.setSelected(false);
                break;
            case TRAPEZIUM:
                monteCarloRadioButton.setSelected(false);
                trapeziumRadioButton.setSelected(true); 
                break;
        }
        
        this.setLayout(new BorderLayout());
        
        add(monteCarloRadioButton, BorderLayout.NORTH);
        add(trapeziumRadioButton, BorderLayout.SOUTH);
        
    }
                          

    private void monteCarloRadioButtonActionPerformed(ActionEvent evt) 
    {
        modellingState.SOLVER = SolverType.MONTECARLO;
    }

    private void trapeziumRadioButtonActionPerformed(ActionEvent evt) 
    {
        modellingState.SOLVER = SolverType.TRAPEZIUM;
    }

    /** Used to disable buttons while computing WDLF. */
    public void disableComponents()
    {
        monteCarloRadioButton.setEnabled(false);
        trapeziumRadioButton.setEnabled(false);
    }
    /** Used to enable buttons after WDLF computation finished. */
    public void enableComponents()
    {
        monteCarloRadioButton.setEnabled(true);
        trapeziumRadioButton.setEnabled(true);
    }
    
    /** Test function to check appearance of form. */
    public static void main(String[] args)
    {
        
        // Create and display the form
        java.awt.EventQueue.invokeLater(
                new Runnable() 
                    {
                        @Override
                        public void run() 
                        {
                            
                            JFrame tester = new JFrame();
                            tester.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                            tester.add(new SolverTypeForm(new ModellingState()));
                            tester.setVisible(true);
                            tester.pack();
                            tester.validate();
                            
                        }
                    });
    
    }
    
}
