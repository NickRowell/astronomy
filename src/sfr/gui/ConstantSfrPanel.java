package sfr.gui;

import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import sfr.algo.BaseSfr;
import sfr.algoimpl.ConstantSFR;
import util.ParseUtil;

/**
 * Class provides a GUI for configuring the parameters of a {@link ConstantSFR} instance.
 *
 * @author nrowell
 * @version $Id$
 */
public class ConstantSfrPanel extends BaseSfrPanel {
	
	/**
	 * The serial verions UID.
	 */
	private static final long serialVersionUID = 1939315822733271834L;

	/**
	 * Reference to the SFR object that is being manipulated
	 */
	ConstantSFR sfr;
	
    /**
	 * Text field containing user-specified minimum lookback time.
	 */
    private JTextField tMinField;
    
	/**
	 * Text field containing user-specified maximum lookback time.
	 */
    private JTextField tMaxField;
    
    /**
	 * Text field containing user-specified star formation rate.
	 */
    private JTextField rateField;
	
	/**
	 * The default constructor.
	 */
	public ConstantSfrPanel() {
		this(new ConstantSFR());
	}
	
	/**
	 * Main constructor.
	 * 
	 * @param sfr
	 * 	The ConstantSFR to present in the panel.
	 */
	public ConstantSfrPanel(ConstantSFR sfr) {
		
		this.sfr = sfr;
		
		tMinField = new JTextField(String.format("%s", sfr.t_min));
		tMaxField = new JTextField(String.format("%s", sfr.t_max));
		rateField = new JTextField(String.format("%s", sfr.rate));
		
		setBorder(BorderFactory.createTitledBorder("Parameters of "+sfr.getName()+" SFR model"));
		
		setLayout(new GridLayout(3, 2, 0, 5));

		add(new JLabel("Min lookback time [yr]:"));
	    add(tMinField);
	    add(new JLabel("Max lookback time [yr]:"));
	    add(tMaxField);
		add(new JLabel("Rate [N/yr]:"));
	    add(rateField);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public BaseSfr getSfr() {
		return sfr;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update() {
		
		double t_min = Double.NaN;
    	double t_max = Double.NaN;
    	double rate = Double.NaN;

    	// Records success/failure of parsing
    	boolean success = true;
    	
    	// Build error message
    	StringBuilder msg_builder = new StringBuilder();

    	// Attempt to parse t_min
    	try {
            t_min = ParseUtil.parseAndCheckGreaterThanOrEqualTo(tMinField.getText(), "Minimum lookback time [yr]", 0.0);
        }
        catch(Exception e) {
            msg_builder.append(e.getMessage());
            success = false;
        }
    	
    	// Attempt to parse t_max
    	try {
            t_max = ParseUtil.parseAndCheckGreaterThan(tMaxField.getText(), "Maximum lookback time [yr]", t_min);
        }
        catch(Exception e) {
            msg_builder.append(e.getMessage());
            success = false;
        }
    	
        // Attempt to parse star formation rate
    	try {
            rate = ParseUtil.parseAndCheckGreaterThan(rateField.getText(), "Star Formation Rate [N/yr]", 0.0);
        }
        catch(Exception e) {
            msg_builder.append(e.getMessage());
            success = false;
        }
        
        // Check if we parsed OK
        if(!success) {
        	// Display error message.
            JOptionPane.showMessageDialog(this, msg_builder.toString(), "Input Error", JOptionPane.ERROR_MESSAGE);
        }
        else {
	        // Set parsed values to internal fields.
        	sfr.t_min = t_min;
	        sfr.t_max = t_max;
	        sfr.rate  = rate;
        }
	}
    
}
