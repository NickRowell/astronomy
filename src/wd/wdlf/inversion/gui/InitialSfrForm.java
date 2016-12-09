package wd.wdlf.inversion.gui;

import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTextField;

import imf.algo.BaseImf;
import util.ParseUtil;
import wd.wdlf.infra.EntryForm;
import wd.wdlf.infra.EntryFormResult;
import wd.wdlf.inversion.infra.InversionState;

/**
 * Entry form for parameters of initial guess star formation rate model.
 * 
 * @author nickrowell
 */
public class InitialSfrForm extends EntryForm 
{
    /**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = -596886091970662337L;
	
	/**
	 * Reference to the main {@link InversionState} object.
	 */
	private final InversionState inversionState;
    
    /**
     * The {@link JTextField} specifying the star formation rate of the initial guess model.
     */
    private final JTextField initSFRField;
    
    /**
     * The {@link JLabel} displaying the minimum lookback time. This is not configurable by
     * the form and is instead set by the lifetime of the most massive star, which is determined
     * by the metallicity and pre-WD lifetime models chosen in previous forms.
     */
    private final JLabel minLookbackTimeField;
    
    /**
     * The {@link JTextField} specifying the maximum lookback time.
     */
    private final JTextField maxLookbackTimeField;
    
    /**
     * The {@link JTextField} specifying the number of (regular) time bins to divide the time range into.
     */
    private final JTextField nBinsField;
    
    /**
     * Main constructor for the {@link InitialSfrForm}
     * @param inversionState
     * 	Handle to the main {@link InversionState}
     */
    public InitialSfrForm(InversionState inversionState) {
    	
    	this.inversionState = inversionState;
    	
    	minLookbackTimeField = new JLabel();
        maxLookbackTimeField = new JTextField();
        nBinsField = new JTextField();
        initSFRField = new JTextField();

        setBorder(BorderFactory.createTitledBorder("Set parameters of initial guess SFR"));
        setPreferredSize(new Dimension(400, 350));

        setLayout(new GridLayout(4,2));
        add(new JLabel("Minimum lookback time [yr]:"));
        add(minLookbackTimeField);
        add(new JLabel("Maximum lookback time [yr]:"));
        add(maxLookbackTimeField);
        add(new JLabel("Initial SFR value [N/yr]:"));
        add(initSFRField);
        add(new JLabel("Number of SFR bins:"));
        add(nBinsField);
        
        initialise();
    }
    
    @Override
    public EntryFormResult verify() {
 
        InitSFRInputVerifier input = new InitSFRInputVerifier(
                maxLookbackTimeField.getText(),
                nBinsField.getText(),
                initSFRField.getText(),
                inversionState.currentSfr.t_min);
        
        // Verify input forms....
        if(input.valid) {
            // Read all values to initial guess SFR model
        	inversionState.currentSfr.t_max = input.tmax;
        	inversionState.currentSfr.N = input.n;
        	inversionState.currentSfr.init_SFR = input.sfr;
            // Configure SFR bins using selected parameters.
        	inversionState.currentSfr.setLookbackTimeBins();
        }             
        
        return input;
    }
    
	@Override
	public void initialise() {

        // Set minimum lookback time for SFR; this is equal to the lifetime of the most massive
        // stars (adopting the mean metallicity value)
        double z = inversionState.params.getMeanMetallicity();
        double y = inversionState.params.getMeanHeliumContent();
        double tMin = inversionState.params.getPreWdLifetime().getPreWdLifetime(z, y, BaseImf.M_upper)[0];
        inversionState.currentSfr.t_min = tMin;
        
        // Update text fields in GUI etc.
    	minLookbackTimeField.setText(String.format("%f", inversionState.currentSfr.t_min));
        maxLookbackTimeField.setText(ParseUtil.expNumFormat.format(inversionState.currentSfr.t_max));
        nBinsField.setText(String.format("%d", inversionState.currentSfr.N));
        initSFRField.setText(ParseUtil.expNumFormat.format(inversionState.currentSfr.init_SFR));
	}
}

/**
 * Class used to validate entries in the {@link InitialSfrForm}.
 *
 * @author nrowell
 * @version $Id$
 */
class InitSFRInputVerifier extends EntryFormResult {

    // Parsed and checked values of parameters.
    public double tmax;
    public int n;
    public double sfr;
    
    /**
     * Main constructor.
     * @param tmax
     * 	String containing maximum lookback time.
     * @param n
     * 	String containing number of formation time bins.
     * @param sfr
     * 	String containing initial star formation rate.
     * @param tmin
     * 	The minimum lookback time.
     */
    public InitSFRInputVerifier(String tmax, String n, String sfr, double tmin) {
            
        StringBuilder msg_builder = new StringBuilder();
        
        // Attempt to parse tmax
        try {
            this.tmax = ParseUtil.parseAndCheckGreaterThan(tmax, "Maximum lookback time", tmin);
        }
        catch(Exception e) {
            msg_builder.append(e.getMessage());
            valid = false;
        }
        
        // Attempt to parse n
        try {
            this.n = (int)ParseUtil.parseAndCheckGreaterThan(n, "Number of bins", 0.0);
        }
        catch(Exception e) {
            msg_builder.append(e.getMessage());
            valid = false;
        }
        
        // Attempt to parse sfr
        try {
            this.sfr = ParseUtil.parseAndCheckGreaterThan(sfr, "Initial star formation rate", 0.0);
        }
        catch(Exception e) {
            msg_builder.append(e.getMessage());
            valid = false;
        }
        
        message = msg_builder.toString();
    }
}