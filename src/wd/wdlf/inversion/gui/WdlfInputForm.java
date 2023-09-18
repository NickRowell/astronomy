package wd.wdlf.inversion.gui;

import infra.io.Gnuplot;
import photometry.Filter;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import wd.wdlf.algo.BaseWdlf;
import wd.wdlf.algoimpl.ObservedWdlf;
import wd.wdlf.infra.EntryForm;
import wd.wdlf.infra.EntryFormResult;
import wd.wdlf.inversion.infra.InversionState;

/**
 * Class provides GUI interface for selecting WDLF to be inverted.
 * 
 * @author nickrowell
 */
public class WdlfInputForm extends EntryForm {

    /**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 7142000639949823426L;

	/**
	 * Reference to the main {@link InversionState} instance.
	 */
    private final InversionState inversionState;
    
    /**
     * The {@link JLabel} presenting the WDLF plot.
     */
    final JLabel wdlfPlotPanel;
    
    /**
     * The {@link JComboBox} used to select built in {@link ObservedWdlf}s.
     */
    final JComboBox<ObservedWdlf> obsWDLFComboBox;
    
    /**
     * Initial default entry in the WDLF text entry area.
     */
    private static String defaultWdlfStr = "# Each line corresponds to one WDLF point\n"
    		+ "# and consists of 4 columns.\n#\n"
    		+ "# Column 1: M_{bol} bin centre\n"
    		+ "# Column 2: M_{bol} bin width\n"
    		+ "# Column 3: LF value [N / M_{bol}]\n"
    		+ "# Column 4: one-sigma uncertainty on LF\n"
    		+ "# e.g.\n6.25  0.5 2.318E-7 2.356E-7\n"
    		+ "6.75  0.5 5.127E-7 3.862E-7\n"
    		+ "7.25  0.5 2.469E-6 5.822E-7\n"
    		+ "7.75  0.5 5.133E-6 7.891E-7\n"
    		+ "8.25  0.5 7.335E-6 8.920E-7\n"
    		+ "8.75  0.5 1.329E-5 1.183E-6\n"
    		+ "9.25  0.5 1.866E-5 1.440E-6\n"
    		+ "9.75  0.5 3.520E-5 2.012E-6\n"
    		+ "10.25 0.5 5.122E-5 2.583E-6\n"
    		+ "10.75 0.5 5.306E-5 2.944E-6\n"
    		+ "11.25 0.5 6.979E-5 3.676E-6\n"
    		+ "11.75 0.5 1.108E-4 4.798E-6\n"
    		+ "12.25 0.5 1.450E-4 6.426E-6\n"
    		+ "13.25 0.5 2.956E-4 1.292E-5\n"
    		+ "13.75 0.5 4.260E-4 1.956E-5\n"
    		+ "14.25 0.5 5.675E-4 2.843E-5\n"
    		+ "14.75 0.5 8.668E-4 4.687E-5\n"
    		+ "15.25 0.5 8.846E-4 7.255E-5";
    
    /**
     * Creates new form ObsWDLFInputForm
     */
    public WdlfInputForm(InversionState inversionStateIn) 
    {
    	setLayout(new BorderLayout());
    	
        this.inversionState = inversionStateIn;
        
    	setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Select input WDLF to be inverted"),
    			BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        wdlfPlotPanel = new JLabel(new ImageIcon());
        wdlfPlotPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Loaded WDLF"), 
        		BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        
        // Set up combo box used to select observed WDLFs
    	obsWDLFComboBox = new JComboBox<ObservedWdlf>(ObservedWdlf.values());
    	obsWDLFComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (evt.getActionCommand().equals("comboBoxChanged")) 
                {
                    // Get currently selected item
                    inversionState.wdlf_obs = ((ObservedWdlf)obsWDLFComboBox.getSelectedItem()).wdlf;
                }
                // Plot WDLF
                plotWDLF();
            }
        });
    	
    	// Set up sub-panel used to specify WDLF manually
    	final JTextArea wdlfTextArea = new JTextArea(defaultWdlfStr);
    	wdlfTextArea.setColumns(20);
        wdlfTextArea.setRows(15);
    	final JComboBox<Filter> filterComboBox = new JComboBox<Filter>(Filter.values());
    	final JTextField distModField = new JTextField("0.0");
    	final JButton loadWDLFButton = new JButton("Load user specified WDLF");
    	
        loadWDLFButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
            	
            	// Read WDLF data from text area.
                ObsWDLFInputVerifier input = new ObsWDLFInputVerifier(wdlfTextArea.getText(), distModField.getText(),
                		(Filter)filterComboBox.getSelectedItem());
                
                if(input.valid)
                {
                    // WDLF entered correctly: read back from input verifier
                    inversionState.wdlf_obs = input.wdlf_obs;
                    // Plot WDLF
                    plotWDLF();
                }
                else
                {
                    // Display error message.
                    JOptionPane.showMessageDialog(null, input.message, 
                            "Input Error",JOptionPane.ERROR_MESSAGE);      
                }
            }
        });
        
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Inputs"), 
        		BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        
        GridBagConstraints c = new GridBagConstraints();
        c.weighty = 1.0;
        
        c.gridx = 0;
        c.gridy = 0;
        c.gridheight = 1;
        c.gridwidth = 3;
        c.fill = GridBagConstraints.HORIZONTAL;
        inputPanel.add(new JLabel("Select an observed WDLF:"), c);
        
        c.gridx = 0;
        c.gridy = 1;
        c.gridheight = 1;
        c.gridwidth = 3;
        c.fill = GridBagConstraints.HORIZONTAL;
        inputPanel.add(obsWDLFComboBox, c);
        
        c.gridx = 0;
        c.gridy = 2;
        c.gridheight = 2;
        c.gridwidth = 3;
        c.fill = GridBagConstraints.HORIZONTAL;
        inputPanel.add(new JLabel("...or specify one manually:"), c);
        
        c.gridx = 0;
        c.gridy = 4;
        c.gridheight = 12;
        c.gridwidth = 3;
        c.fill = GridBagConstraints.HORIZONTAL;
        inputPanel.add(new JScrollPane(wdlfTextArea), c);
        
        c.gridx = 0;
        c.gridy = 16;
        c.gridheight = 1;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        inputPanel.add(new JLabel("Distance Modulus:"), c);
        
        c.gridx = 2;
        c.gridy = 16;
        c.gridheight = 1;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.0;
        inputPanel.add(distModField, c);
        
        c.gridx = 0;
        c.gridy = 17;
        c.gridheight = 1;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        inputPanel.add(new JLabel("Filter:"), c);
        
        c.gridx = 2;
        c.gridy = 17;
        c.gridheight = 1;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.0;
        inputPanel.add(filterComboBox, c);
        
        c.gridx = 0;
        c.gridy = 18;
        c.gridheight = 1;
        c.gridwidth = 3;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        inputPanel.add(loadWDLFButton, c);
        
        add(inputPanel, BorderLayout.WEST);
        add(wdlfPlotPanel, BorderLayout.EAST);
        
        initialise();
    }

    /** 
     * Parameters are always valid by construction, because they are either
     * loaded from memory or validated when the 'load' button is pressed.
     */
    @Override
    public EntryFormResult verify(){
    	
    	EntryFormResult entryFormResult = new EntryFormResult();
    	
    	// Get the Filter specified in the modelling parameters...
    	if(inversionState.params.getFilter() != inversionState.wdlf_obs.getFilter()) {
    		
    		entryFormResult.valid = false;
    		entryFormResult.message = "Observed WDLF uses band "+inversionState.wdlf_obs.getFilter()+
    				" but the band selected for modelling is "+inversionState.params.getFilter()+"!";
    	}
    	
        return entryFormResult;
    }
    
    /**
     * Plot the WDLF currently loaded to the {@link InversionState} instance and
     * display it in the {@link IPanel}.
     */
    private void plotWDLF()
    {
        try
        {
            // Write Gnuplot script file
            File script = new File(inversionState.outputDirectory,"obsWDLFScript.p");
            BufferedWriter out = new BufferedWriter(new FileWriter(script));
            out.write(inversionState.wdlf_obs.getLuminosityFunctionGnuplotScript());
            out.close();
            
            BufferedImage img = Gnuplot.executeScript(script);
            
            ((ImageIcon)wdlfPlotPanel.getIcon()).setImage(img);
            wdlfPlotPanel.repaint();
        }
        catch(IOException ioe)
        {
            // Display error message.
            JOptionPane.showMessageDialog(this, ioe.getMessage(), 
                    "Input Error",JOptionPane.ERROR_MESSAGE);  
        }
    }
    
	@Override
	public void initialise() {
		// What initialisation to make? We could think about restricting the range of WDLFs available
		// based on the {@link Filter} that the user selected.
		
        inversionState.wdlf_obs = ((ObservedWdlf)obsWDLFComboBox.getSelectedItem()).wdlf;
		
		// Plot the WDLF
		plotWDLF();
	}
	
    /**
     * Simple application to display form for debugging & design purposes.
     * @param args
     * 	Ignored.
     */
    public static void main(String[] args) {
    	javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
            	JFrame frame = new JFrame();
            	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            	frame.add(new WdlfInputForm(new InversionState()));
            	frame.pack();
            	frame.setLocationRelativeTo(null);
            	frame.setVisible(true);
            }
        });
    }
}

/**
 * Implementation of {@link EntryFormResult} used to parse the user-specified WDLF fields
 * from the GUI.
 */
class ObsWDLFInputVerifier extends EntryFormResult
{
	/**
	 * Parsed and checked {@link BaseWdlf}
	 */
    public BaseWdlf wdlf_obs;
    
    /**
     * Main constructor.
     * @param wdlf_obs
     * 	String containing WDLF bin centres, widths, LF values and associated errors, formatted with one
     * row per line.
     * @param distModStr
     * 	String containing the distance modulus.
     * @param filter
     * 	The associated {@link Filter}
     */
    public ObsWDLFInputVerifier(String wdlf_obs, String distModStr, Filter filter)
    {
        StringBuilder msg_builder = new StringBuilder();
        
        // Try to parse numbers from WDLF entry table
        double[][] data = null;
        
        try {
        	data = BaseWdlf.parseWdlfDataFromString(wdlf_obs, false);
        	// Check for empty form
            if(data[0].length==0)
            {
                valid = false;
                msg_builder.append("No WDLF data!\n");
            }
        }
        catch(IllegalArgumentException e) {
        	valid = false;
            msg_builder.append(e.getMessage()+"\n");
        }
        
        // Attempt to parse distance modulus
        double distMod = Double.NaN;
        try
        {
            distMod = Double.parseDouble(distModStr);
            if(distMod<0.0)
            {
                msg_builder.append("Distance modulus ["+distMod+"] must be positive!\n");
                valid = false;               
            }
        }
        catch(NumberFormatException nfe)
        {
            msg_builder.append("Could not parse distance modulus: ").append(distModStr).append("\n");
            valid = false;
        }
        
        if(valid) {
            // Values all ok - initialise WDLF
            this.wdlf_obs = new BaseWdlf(data[0], data[1], data[2], data[3], distMod);
            this.wdlf_obs.setName("User WDLF");
            this.wdlf_obs.setFilter(filter);
        }
        
        message = msg_builder.toString();
    }
}