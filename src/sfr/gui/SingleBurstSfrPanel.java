package sfr.gui;

import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import sfr.algo.BaseSfr;
import sfr.algoimpl.SingleBurstSFR;
import util.ParseUtil;

/**
 * Class provides a GUI for configuring the parameters of a {@link SingleBurstSFR} instance.
 *
 * @author nrowell
 * @version $Id$
 */
public class SingleBurstSfrPanel extends BaseSfrPanel {
	
	/**
	 * The serial verions UID.
	 */
	private static final long serialVersionUID = 1939315822733271834L;

	/**
	 * Reference to the SFR object that is being manipulated
	 */
	SingleBurstSFR sfr;

    /**
	 * Text field containing user-specified burst duration.
	 */
    private JTextField burstDurationField;
    
	/**
	 * Text field containing user-specified lookback time at onset of star formation.
	 */
    private JTextField burstOnsetField;
    
    /**
	 * Text field containing user-specified star formation rate during the burst.
	 */
    private JTextField rateField;
	
	/**
	 * The default constructor.
	 */
	public SingleBurstSfrPanel() {
		this(new SingleBurstSFR());
	}
	
	/**
	 * Main constructor.
	 * @param sfr
	 * 		The SingleBurstSFR to present in the panel.
	 */
	public SingleBurstSfrPanel(SingleBurstSFR sfr) {
		
		this.sfr = sfr;
		
		burstOnsetField = new JTextField(String.format("%s", sfr.t_max));
		burstDurationField = new JTextField(String.format("%s", sfr.t_max - sfr.t_min));
		rateField = new JTextField(String.format("%s", sfr.rate));
		
		setBorder(BorderFactory.createTitledBorder("Parameters of "+sfr.getName()+" SFR model"));
		setLayout(new GridLayout(3, 2, 0, 5));
		
		add(new JLabel("Onset of star formation [yr]:"));
		add(burstOnsetField);
		add(new JLabel("Burst duration [yr]:"));
		add(burstDurationField);
		add(new JLabel("Rate during burst [N/yr]:"));
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
    	
		double burstDuration = Double.NaN;
    	double burstOnset = Double.NaN;
    	double rate = Double.NaN;

    	// Records success/failure of parsing
    	boolean success = true;
    	
    	// Build error message
    	StringBuilder msg_builder = new StringBuilder();

    	// Attempt to parse t_min
    	try {
            burstDuration = ParseUtil.parseAndCheckGreaterThan(burstDurationField.getText(), "Burst duration [yr]", 0.0);
        }
        catch(Exception e) {
            msg_builder.append(e.getMessage());
            success = false;
        }
    	
    	// Attempt to parse t_max
    	try {
            burstOnset = ParseUtil.parseAndCheckGreaterThan(burstOnsetField.getText(), "Onset of star formation [yr]", burstDuration);
        }
        catch(Exception e) {
            msg_builder.append(e.getMessage());
            success = false;
        }
    	
        // Attempt to parse star formation rate
    	try {
            rate = ParseUtil.parseAndCheckGreaterThan(rateField.getText(), "Star Formation Rate during burst [N/yr]", 0.0);
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
        	sfr.t_min = burstOnset - burstDuration;
	        sfr.t_max = burstOnset;
	        sfr.rate  = rate;
        }
    }
}