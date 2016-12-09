package sfr.gui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import sfr.algo.BaseSfr;
import sfr.algoimpl.FractalSFR;
import util.ParseUtil;

/**
 * Class provides a GUI for configuring the parameters of a {@link FractalSFR} instance.
 *
 * @author nrowell
 * @version $Id$
 */
public class FractalSfrPanel extends BaseSfrPanel {
	
	/**
	 * The serial verions UID.
	 */
	private static final long serialVersionUID = 1939315822733271834L;

	/**
	 * Reference to the SFR object that is being manipulated
	 */
	FractalSFR sfr;
	
    /**
	 * Text field containing user-specified minimum lookback time.
	 */
    private JTextField tMinField;
    
	/**
	 * Text field containing user-specified maximum lookback time.
	 */
    private JTextField tMaxField;
    
    /**
	 * Text field containing user-specified Hurst parameter.
	 */
    private JTextField hurstParamTextField;

    /**
	 * Text field containing user-specified initial standard deviation.
	 */
    private JTextField initVarTextField;

    /**
	 * Text field containing user-specified magnitude parameter.
	 */
    private JTextField magnitudeTextField;

    /**
	 * Text field containing user-specified rate at end points.
	 */
    private JTextField rateTextField;
	
    /**
	 * The default constructor.
	 */
    public FractalSfrPanel() {
    	this(new FractalSFR());
    }
    
    /**
	 * Main constructor.
	 * @param sfr
	 * 		The FractalSFR to present in the panel.
	 */
	public FractalSfrPanel(final FractalSFR sfr) {
		
		this.sfr = sfr;
		
		ButtonGroup clampToZeroGroup = new ButtonGroup();
		
		tMinField = new JTextField(String.format("%s", sfr.t_min));
		tMaxField = new JTextField(String.format("%s", sfr.t_max));
        magnitudeTextField = new JTextField(String.format("%s", sfr.magnitude));
        rateTextField = new JTextField(String.format("%s", sfr.r0));
        hurstParamTextField = new JTextField(String.format("%s", sfr.H));
        initVarTextField = new JTextField(String.format("%s", sfr.std));
        
        JRadioButton shiftToZeroButton = new JRadioButton("Shift rate to min. zero");
        JRadioButton clampToZeroButton = new JRadioButton("Clamp rate to zero");

		setBorder(BorderFactory.createTitledBorder("Parameters of "+sfr.getName()+" SFR model"));
        setLayout(new GridLayout(7, 2));

		add(new JLabel("Min lookback time [yr]:"));
	    add(tMinField);
	    add(new JLabel("Max lookback time [yr]:"));
	    add(tMaxField);
        add(new JLabel("Magnitude (2^N + 1):"));
        add(magnitudeTextField);
        add(new JLabel("Initial rate [N/yr]:"));
        add(rateTextField);
        add(new JLabel("Hurst parameter [0:1]:"));
        add(hurstParamTextField);
        add(new JLabel("Initial STD [N/yr]:"));
        add(initVarTextField);
        
        clampToZeroGroup.add(shiftToZeroButton);
        shiftToZeroButton.setHorizontalTextPosition(SwingConstants.LEADING);
        shiftToZeroButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
            	// Activate the 'shift to zero' option
                FractalSfrPanel.this.sfr.clamp_to_zero = false;
            }
        });
        add(shiftToZeroButton);

        clampToZeroGroup.add(clampToZeroButton);
        clampToZeroButton.setSelected(true);
        clampToZeroButton.setHorizontalTextPosition(SwingConstants.LEADING);
        clampToZeroButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
            	// Activate the 'clamp to zero' option
            	FractalSfrPanel.this.sfr.clamp_to_zero = true;
            }
        });
        add(clampToZeroButton);
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
    	int magnitude = -Integer.MAX_VALUE;
    	double rate = Double.NaN;
    	double H = Double.NaN;
    	double std = Double.NaN;

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
    	
    	// Attempt to parse magnitude
    	try {
            double fMag = ParseUtil.parseAndCheckGreaterThan(magnitudeTextField.getText(), "Fractal magnitude", 0.0);
            
            // Check that user has given a whole number
            if((fMag == Math.floor(fMag)) && !Double.isInfinite(fMag)) {
            	// fMag is an integer quantity
            	magnitude = (int)Math.floor(fMag);
            }
            else {
            	msg_builder.append("Magnitude parameter must be a positive whole number!");
                success = false;
            }
        }
        catch(Exception e) {
            msg_builder.append(e.getMessage());
            success = false;
        }
    	
        // Attempt to parse star formation rate
    	try {
            rate = ParseUtil.parseAndCheckGreaterThan(rateTextField.getText(), "Star Formation Rate [N/yr]", 0.0);
        }
        catch(Exception e) {
            msg_builder.append(e.getMessage());
            success = false;
        }
        
    	// Attempt to parse Hurst parameter
    	try {
            H =  ParseUtil.parseAndCheckWithinRangeInclusive(hurstParamTextField.getText(), "Hurst parameter", 0.0, 1.0);
        }
        catch(Exception e) {
            msg_builder.append(e.getMessage());
            success = false;
        }
    	
    	// Attempt to parse initial variance
    	try {
            std = ParseUtil.parseAndCheckGreaterThan(initVarTextField.getText(), "Initial STD [N/yr]:", 0.0);
        }
        catch(Exception e) {
            msg_builder.append(e.getMessage());
            success = false;
        }

        // Check if we parsed OK
        if(!success) {
        	// Display error message.
            JOptionPane.showMessageDialog(this, msg_builder.toString(), "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Read parsed parameters back.
    	sfr.t_min       = t_min;
        sfr.t_max       = t_max;
        sfr.magnitude   = magnitude;
        sfr.r0          = rate;
        sfr.H           = H;
        sfr.std         = std;
    	
    	sfr.init();
    }
}