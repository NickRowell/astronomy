package wd.wdlf.modelling.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import wd.wdlf.algo.BaseWdlf;
import wd.wdlf.infra.EntryForm;
import wd.wdlf.infra.EntryFormResult;
import wd.wdlf.modelling.infra.ModellingState;

/**
 * Class provides a GUI interface to set the magnitude bin centres and widths
 * parameters of a ModellingState object. This adjusts the resolution and
 * magnitude range of the calculated WDLF.
 */
public class WDLFBinsForm extends EntryForm 
{
    /**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = -1481997904962101103L;

	/**
	 * Enum type indicates which convention for WDLF bins is currently
	 * selected.
	 */
	private static enum Type{REGULAR, FREEFORM};
	
	/**
	 * Handle to  main {@link ModellingState} object.
	 */
    ModellingState modellingState;
    
    /**
     * Indicates which WDLF bins style is currently selected.
     */
    private Type binsType = Type.FREEFORM;
    
    /**
     * Text field used to specify the lower limit on the magnitude range, in the
     * case of regular magnitude bins.
     */
    JTextField wdlfBinsStartField;
    
    /**
     * Text field used to specify the upper limit on the magnitude range, in the
     * case of regular magnitude bins.
     */
    JTextField wdlfBinsEndField;
    
    /**
     * Text field used to specify the constant magnitude bin width, in the
     * case of regular magnitude bins.
     */
    JTextField wdlfBinsStepField;
    
    /**
     * Text area used to specify the freeform magnitude bins.
     */
    JTextArea wdlfBinTextArea;
    
    /**
     * Main constructor.
     * 
     * @param modellingState
     * 	Reference to the main {@link ModellingState} object. The {@link ModellingState#wdlfBinCentres}
     * and {@link ModellingState#wdlfBinWidths} fields of this instance will be updated by this.
     */
    public WDLFBinsForm(ModellingState modellingState)
    {
        this.modellingState = modellingState;
        
    	setBorder(javax.swing.BorderFactory.createTitledBorder("Set magnitude bins"));
    	
        ButtonGroup binTypeButtonGroup = new ButtonGroup();
        JRadioButton regularRadioButton = new JRadioButton();
        JRadioButton freeformRadioButton = new JRadioButton();
        
        binTypeButtonGroup.add(regularRadioButton);
        regularRadioButton.setText("Regular:");
        regularRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
            	binsType = Type.REGULAR;
            }
        });

        binTypeButtonGroup.add(freeformRadioButton);
        freeformRadioButton.setText("Freeform:");
        freeformRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
            	binsType = Type.FREEFORM;
            }
        });
    	
        // Initialise radio button selection
        switch(binsType)
        {
            case REGULAR: 
                regularRadioButton.setSelected(true);
                freeformRadioButton.setSelected(false);
                break;
            case FREEFORM:
            	regularRadioButton.setSelected(false);
            	freeformRadioButton.setSelected(true); 
                break;
        }
    	
        // Small sub-panel to hold text fields and labels for regular bins input
        JLabel every = new JLabel("Width ");
        JLabel from = new JLabel(" Mbol, from ");
        JLabel to = new JLabel(" to ");
        wdlfBinsStartField = new JTextField("5.0");
        wdlfBinsEndField = new JTextField("16.0");
        wdlfBinsStepField = new JTextField("0.5");
        
        JPanel subPanel = new JPanel();
        subPanel.add(every);
        subPanel.add(wdlfBinsStepField);
        subPanel.add(from);
        subPanel.add(wdlfBinsStartField);
        subPanel.add(to);
        subPanel.add(wdlfBinsEndField);
        
        // Initialise WDLF bin centre & width text box
        // Text entry box for WDLF bin centres & widths
        JScrollPane jScrollPane1 = new JScrollPane();
        wdlfBinTextArea = new JTextArea();
        wdlfBinTextArea.setColumns(20);
        wdlfBinTextArea.setRows(5);
        jScrollPane1.setViewportView(wdlfBinTextArea);
        
        // Write header explaining format of input text
        wdlfBinTextArea.setText("# Bin centre / width");
        
        // Write current bin centres & widths    
        for(int i=0; i<modellingState.wdlfBinCentres.length; i++)
        {
            wdlfBinTextArea.append("\n"+modellingState.wdlfBinCentres[i] + "\t" +
                    modellingState.wdlfBinWidths[i]);
        }
        
        JPanel subSubPanel = new JPanel(new GridLayout(3,1));
        subSubPanel.add(regularRadioButton);
        subSubPanel.add(subPanel);
        subSubPanel.add(freeformRadioButton);
        
        this.setLayout(new BorderLayout());
        add(subSubPanel, BorderLayout.NORTH);
        add(jScrollPane1, BorderLayout.CENTER);
        this.validate();
    }
    
    /**
     * Check the entered values for the magnitude bin centres and widths, and update the
     * {@link ModellingState#wdlfBinCentres} and {@link ModellingState#wdlfBinWidths} fields of
     * the {@link #modellingState} instance.
     */
    @Override
    public EntryFormResult verify()
    {
    	WDLFBinsVerifier input = null;
    	
    	switch(binsType) {
	    	case REGULAR:
	    		input = new WDLFBinsVerifier(wdlfBinsStartField.getText(), wdlfBinsEndField.getText(),
	    												wdlfBinsStepField.getText());
	    		break;
	    	case FREEFORM:
	    		input = new WDLFBinsVerifier(wdlfBinTextArea.getText());
	    		break;
    	}
    	
    	// Verify input
        if (input.valid) 
        {
            // Read parsed parameters back.
             modellingState.wdlfBinCentres = input.wdlfBinCentres;
             modellingState.wdlfBinWidths  = input.wdlfBinWidths;
        }
        return input;
    }

	@Override
	public void initialise() {
		// Nothing to initialise
	}
}

/**
 * Implementation of {@link EntryFormResult} used to parse magnitude bin centres & widths
 * from text input of different types.
 *
 * @author nrowell
 * @version $Id$
 */
class WDLFBinsVerifier extends EntryFormResult {
	
	/**
	 * Parsed and checked values of the magnitude bin centres.
	 */
    public double[] wdlfBinCentres;
    /**
     * Parsed and checked values of the magnitude bin widths.
     */
    public double[] wdlfBinWidths;
    
	/**
	 * Constructor used to parse regular magnitude bins.
	 * 
	 * @param wdlfBinsStartStr
	 * 	String containing the lower (bright) magnitude limit.
	 * @param wdlfBinsEndStr
	 * 	String containing the upper (faint) magnitude limit.
	 * @param wdlfBinsStepStr
	 * 	String containing the magnitude bin width.
	 */
    public WDLFBinsVerifier(String wdlfBinsStartStr, String wdlfBinsEndStr, String wdlfBinsStepStr)
    {
        StringBuilder msg_builder = new StringBuilder();

    	// Parsed & checked value of the lower magnitude limit.
    	double wdlfBinsStart = Double.NaN;
    	
    	// Parsed & checked value of the upper magnitude limit.
    	double wdlfBinsEnd = Double.NaN;
    	
    	// Parsed & checked value of the magnitude step.
    	double wdlfBinsStep = Double.NaN;
    	
        // Attempt to parse bins start
        try
        {
            wdlfBinsStart = Double.parseDouble(wdlfBinsStartStr);
        }
        catch(NumberFormatException nfe)
        {
            msg_builder.append("Could not parse WDLF bins start: ").append(wdlfBinsStartStr).append("\n");
            valid = false;
        }
        // Attempt to parse bins end
        try
        {
            wdlfBinsEnd = Double.parseDouble(wdlfBinsEndStr);
        }
        catch(NumberFormatException nfe)
        {
            msg_builder.append("Could not parse WDLF bins end: ").append(wdlfBinsEndStr).append("\n");
            valid = false;
        }
        // Attempt to parse bins step
        try
        {
            wdlfBinsStep = Double.parseDouble(wdlfBinsStepStr);
            if(wdlfBinsStep<=0)
            {
                msg_builder.append("WDLF bin step size must be positive\n");
                valid = false;               
            }
        }
        catch(NumberFormatException nfe)
        {
            msg_builder.append("Could not parse WDLF bins step: ").append(wdlfBinsStepStr).append("\n");
            valid = false;
        }
        
        // Higher level checks
        if(wdlfBinsEnd <= wdlfBinsStart) {
        	msg_builder.append("Illegal WDLF bin range! ["+wdlfBinsStart+":"+wdlfBinsEnd+"]").append("\n");
            valid = false;
        }
        
        // Safe to compute bins from form inputs
        if(valid) {
        	
        	int nBins = (int)Math.ceil((wdlfBinsEnd - wdlfBinsStart)/wdlfBinsStep);
        	
        	wdlfBinCentres = new double[nBins];
        	wdlfBinWidths  = new double[nBins];
        	
        	for(int i=0; i<nBins; i++) {
        		wdlfBinCentres[i] = wdlfBinsStart + i*wdlfBinsStep + (wdlfBinsStep/2.0);
        		wdlfBinWidths[i]  = wdlfBinsStep;
        	}
        	
        }
        message = msg_builder.toString();
    }

    /**
     * Constructor used to parse freeform magnitude bins.
     * 
     * @param wdlfBinsString
     * 	String containing the magnitude bin centres and widths specified in freeform format.
     */
    public WDLFBinsVerifier(String wdlfBinsString)
    {
    
        StringBuilder msg_builder = new StringBuilder();
    
        double[][] wdlfBins = null;
        
        try {
        	wdlfBins = BaseWdlf.parseWdlfDataFromString(wdlfBinsString, true);
        	// Check for empty form
            if(wdlfBins[0].length==0)
            {
                valid = false;
                msg_builder.append("No data!\n");
            }
        }
        catch(IllegalArgumentException e) {
        	valid = false;
            msg_builder.append(e.getMessage()+"\n");
        }
        
        if(valid) {
            wdlfBinCentres = wdlfBins[0];
            wdlfBinWidths  = wdlfBins[1];
        }
            
        message = msg_builder.toString();
    }
}
