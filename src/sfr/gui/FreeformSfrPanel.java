package sfr.gui;

import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import infra.os.OSChecker;
import numeric.data.DiscreteFunction1D;
import sfr.algo.BaseSfr;
import sfr.algoimpl.FreeformSFR;
import util.ArrayUtil;
import util.ParseUtil;

public class FreeformSfrPanel extends BaseSfrPanel {
	
	/**
	 * The serial verions UID.
	 */
	private static final long serialVersionUID = 1939315822733271834L;

	/**
	 * Reference to the SFR object that is being manipulated
	 */
	FreeformSFR sfr;

	/**
	 * Text fields containing user specified SFR model parameters.
	 */
    private JTextArea timeRateTextArea;
	
    /**
	 * The default constructor.
	 */
    public FreeformSfrPanel() {
    	this(new FreeformSFR());
    }
    
    /**
	 * Main constructor.
	 * @param sfr
	 * 		The FreeformSFR to present in the panel.
	 */
	public FreeformSfrPanel(FreeformSFR sfr) {
		
		this.sfr = sfr;
		
		JScrollPane jScrollPane1 = new JScrollPane();
        JLabel timeRateLabel = new JLabel("bin centre [yr] / width [yr] / rate [N/yr] / error (optional) [N/yr] points:");
        
        timeRateTextArea = new JTextArea();

		setBorder(BorderFactory.createTitledBorder("Parameters of "+sfr.getName()+" SFR model"));
        setLayout(new BorderLayout());

        timeRateTextArea.setColumns(20);
        timeRateTextArea.setRows(5);
        jScrollPane1.setViewportView(timeRateTextArea);

        add(jScrollPane1, BorderLayout.CENTER);

        add(timeRateLabel, BorderLayout.PAGE_START);
		
		// Read initial freeform parameters to set up text field contents
        StringBuilder data = new StringBuilder();
        
        // Loop over all SFR points
        for(int i=0; i<sfr.data.size(); i++) {
        	data.append(String.format("%.3e %.3e %.3e %.3e", sfr.data.getBinCentre(i), sfr.data.getBinWidth(i), 
        			sfr.data.getBinContents(i), sfr.data.getBinUncertainty(i)));
        	data.append(OSChecker.newline);
        }
        
        timeRateTextArea.setText(data.toString());
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
    	
    	// Records success/failure of parsing
    	boolean success = true;
    	
    	StringBuilder msg_builder = new StringBuilder();
        
    	// Read the text from the String using a BufferedReader
    	String text = timeRateTextArea.getText();
    	InputStream is = new ByteArrayInputStream(text.getBytes());
    	BufferedReader br = new BufferedReader(new InputStreamReader(is));
    	
		List<String> comments = new LinkedList<>();
		comments.add("#");
    	
    	double[][] data = null;
    	try {
    		data = ParseUtil.parseFile(br, ParseUtil.whitespaceDelim, comments);
    	}
    	catch(Exception e) {
            msg_builder.append(e.getMessage());
            success = false;
    	}
    	
    	// Check if we parsed the text OK
        if(!success) {
        	// Display error message.
            JOptionPane.showMessageDialog(this, msg_builder.toString(), "Input Error", JOptionPane.ERROR_MESSAGE);
        	return;
        }
        
        // Read out points to arrays
        double[] centres = new double[data[0].length];
        double[] widths  = new double[data[0].length];
        double[] rates   = new double[data[0].length];
        double[] errors  = new double[data[0].length];
        
        for (int p = 0; p < data[0].length; p++) {
            centres[p] = data[0][p];
            widths[p] = data[1][p];
            rates[p] = data[2][p];
            // Read optional error field (is defined for all of the points or none of them)
            if(data.length>3) {
            	errors[p] = data[3][p];
            }
            else {
            	errors[p] = 0.0;
            }
            
            // Sanity checks on values
            if(centres[p] <= 0.0) {
            	msg_builder.append("Non-positive bin centre ("+centres[p]+") found!\n");
                success = false;
            }
            if(widths[p] <= 0.0) {
            	msg_builder.append("Non-positive bin width ("+widths[p]+") found!\n");
                success = false;
            }
            if(rates[p] < 0.0) {
            	msg_builder.append("Negative rate ("+rates[p]+") found!\n");
                success = false;
            }
            if(errors[p] < 0.0) {
            	msg_builder.append("Negative error ("+errors[p]+") found!\n");
                success = false;
            }
            if(!success) {
            	// Display error message.
                JOptionPane.showMessageDialog(this, msg_builder.toString(), "Input Error", JOptionPane.ERROR_MESSAGE);
            	return;
            }
        }
        
        // Sanity checks on array sizes and ordering
        if (!ArrayUtil.checkIncreasing(centres)) {
            msg_builder.append("SFR bin centres don't show monotonic increase!\n");
            success = false;
        }
        if (!ArrayUtil.checkNonOverlappingBins(centres, widths)) {
            msg_builder.append("Overlapping SFR bins!\n");
            success = false;
        }
        
        if(!success) {
        	// Display error message.
            JOptionPane.showMessageDialog(this, msg_builder.toString(), "Input Error", JOptionPane.ERROR_MESSAGE);
        	return;
        }
        
        sfr.data = new DiscreteFunction1D(centres, widths, rates, errors);

        // Set t_max from edge of earliest specified SFR bin
        int N = centres.length;
        sfr.t_max = centres[N - 1] + widths[N - 1] / 2.0;
        sfr.t_min = centres[0] - widths[0] / 2.0;
    }
}
