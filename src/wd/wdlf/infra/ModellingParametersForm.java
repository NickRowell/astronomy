package wd.wdlf.infra;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import ifmr.infra.IFMR;
import ms.lifetime.infra.PreWdLifetimeModels;
import photometry.Filter;
import util.CharUtil;
import util.ParseUtil;
import wd.models.infra.WdCoolingModels;
import wd.wdlf.dm.State;
import wd.wdlf.dm.WdlfModellingParameters;

/**
 * 
 * This class provides a convenient wrapper for a basic set of input parameters
 * used to perform WDLF simulations. It also provides a JPanel interface for 
 * selecting different options for the various parameters. Instances of this 
 * class are used by modelling code to generate stars and synthesise stellar
 * populations.
 * 
 */
public class ModellingParametersForm extends EntryForm 
{
    /**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = -2694133455919772196L;

	/** 
	 * Reference to main ModellingParameters object. 
	 */
    WdlfModellingParameters params;
    
    private final JComboBox<PreWdLifetimeModels> msModelComboBox;
    private final JComboBox<IFMR> ifmrComboBox;
    private final JComboBox<WdCoolingModels> wdModelComboBox;
    private final JComboBox<Filter> filterComboBox;
    private final JTextField heHAtmosphereField;
    private final JTextField imfExponentTextField;
    private final JTextField magErrorField;
    private final JTextField zField;
    private final JTextField sigmaZField;
    private final JTextField yField;
    private final JTextField sigmaYField;
    
    /** 
     * Default constructor that creates a default {@link WdlfModellingParameters} instance
     * to manipulate via the GUI.
     */
    public ModellingParametersForm()
    {
    	this(new WdlfModellingParameters());
    }
    
    /**
     * Alternative constructor that accepts a full {@link State} instance.
     * The encapsulated {@link WdlfModellingParameters} instance will be manipulated.
     * 
     * @param state
     * 	The {@link State} instance.
     */
    public ModellingParametersForm(State state)
    {
    	this(state.params);
    }
    
    /**
     * Main constructor, taking a reference to an existing {@link WdlfModellingParameters}
     * instance that will be manipulated by selections made in the GUI.
     * 
     * @param params
     * 	The {@link WdlfModellingParameters} instance.
     */
    public ModellingParametersForm(WdlfModellingParameters paramsIn) 
    {
        params = paramsIn;
        
        msModelComboBox = new JComboBox<PreWdLifetimeModels>(PreWdLifetimeModels.values());
        zField = new JTextField();
        sigmaZField = new JTextField();
        yField = new JTextField();
        sigmaYField = new JTextField();
        ifmrComboBox = new JComboBox<IFMR>(IFMR.values());
        wdModelComboBox = new JComboBox<WdCoolingModels>(WdCoolingModels.values());
        filterComboBox = new JComboBox<Filter>(Filter.values());
        heHAtmosphereField = new JTextField();
        magErrorField = new JTextField();
        imfExponentTextField = new JTextField();
        
        msModelComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) 
            {
            	params.setPreWdLifetimeModels((PreWdLifetimeModels)msModelComboBox.getSelectedItem());
            }
        });
        
        ifmrComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) 
            {
                params.setIFMR((IFMR)ifmrComboBox.getSelectedItem());
            }
        });
         
        wdModelComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) 
            {
            	// Update the WD cooling models (also resets the filter to the default)
                params.setWdCoolingModels((WdCoolingModels)wdModelComboBox.getSelectedItem());
                // Note that this call has effect of triggering the ActionListener attached to the filterComboBox, with
                // the result that the filter is set twice: once by the setWdCoolingModels(...) method and once by the
                // filterComboBox ActionListener.
                filterComboBox.setModel(new DefaultComboBoxModel<Filter>(params.getBaseWdCoolingModels().getPassbands()));
                filterComboBox.setSelectedItem(params.getFilter());
            }
        });
        
        filterComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) 
            {
            	Filter filter = (Filter)filterComboBox.getSelectedItem();
                params.setFilter(filter);
            }
        });
        
        // Set up GUI
        setBorder(BorderFactory.createTitledBorder("Set input physics parameters"));
        setPreferredSize(new java.awt.Dimension(300, 250));
        setLayout(new java.awt.GridLayout(11,2));
        add(new JLabel("IMF exponent:"));
        add(imfExponentTextField);
        add(new JLabel("Pre-WD lifetime models:"));
        add(msModelComboBox);
        add(new JLabel("   "+CharUtil.bullet+" Mean Z = "));
        add(zField);
        add(new JLabel("   "+CharUtil.bullet+" Sigma-Z = "));
        add(sigmaZField);
        add(new JLabel("   "+CharUtil.bullet+" Mean Y = "));
        add(yField);
        add(new JLabel("   "+CharUtil.bullet+" Sigma-Y = "));
        add(sigmaYField);
        add(new JLabel("IFMR:"));
        add(ifmrComboBox);
        add(new JLabel("WD cooling models:"));
        add(wdModelComboBox);
        add(new JLabel("Passband:"));
        add(filterComboBox);
        add(new JLabel("H fraction (0:1):"));
        add(heHAtmosphereField);
        add(new JLabel("Magnitude error:"));
        add(magErrorField);
        
        initialise();
    }
    
    @Override
    public EntryFormResult verify()
    {
        ModellingFormInputVerifier input = new ModellingFormInputVerifier(
                heHAtmosphereField.getText(),
                magErrorField.getText(),
                imfExponentTextField.getText(),
                zField.getText(),
                sigmaZField.getText(),
                yField.getText(),
                sigmaYField.getText());

        // Verify input forms....
        if (input.valid) 
        {
            // Read parsed parameters back.
            params.setIMF(input.imf);
            params.setSigM(input.s_m);
            params.setW_H(input.w);
            params.setMetallicity(input.z);
            params.setMetallicitySigma(input.sigmaZ);
            params.setHeliumContent(input.y);
            params.setHeliumContentSigma(input.sigmaY);
        }
        
        return input;
    }

	@Override
	public void initialise() {
		msModelComboBox.setSelectedItem(params.getPreWdLifetimeModelsEnum());
        zField.setText(String.format("%f",params.getMeanMetallicity()));
        sigmaZField.setText(String.format("%f",params.getMetallicitySigma()));
        yField.setText(String.format("%f",params.getMeanHeliumContent()));
        sigmaYField.setText(String.format("%f",params.getHeliumContentSigma()));
        ifmrComboBox.setSelectedItem(params.getIfmrEnum());
        wdModelComboBox.setSelectedItem(params.getWdCoolingModelsEnum());
        filterComboBox.setModel(new DefaultComboBoxModel<Filter>(params.getBaseWdCoolingModels().getPassbands()));
        filterComboBox.setSelectedItem(params.getFilter());
        heHAtmosphereField.setText(String.format("%f",params.getW_H()));
        magErrorField.setText(String.format("%f",params.getSigM()));
        imfExponentTextField.setText(String.format("%f",params.getIMF().getExponent()));
	}
	
    /**
     * Test function for this class.
     */
    public static void main(String[] args)
    {
        
        // Create and display the form
        java.awt.EventQueue.invokeLater(
                new Runnable() 
                    {
                        @Override
                        public void run() {
                            
                            JFrame tester = new JFrame();
                            tester.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                            tester.add(new ModellingParametersForm());
                            tester.setVisible(true);
                            tester.pack();
                            tester.validate();
                            
                        }
                    });
    }  
}

/**
 * Class used to validate entries in the {@link ModellingParametersForm}.
 *
 * @author nrowell
 * @version $Id$
 */
class ModellingFormInputVerifier extends EntryFormResult 
{
    
    // Parsed and checked values of parameters.
    public double w;
    public double s_m;
    public double imf;
    public double z;
    public double sigmaZ;
    public double y;
    public double sigmaY;
    
    /**
     * Main constructor.
     * @param w
     * 	String containing the hydrogen-to-helium atmosphere type ratio.
     * @param s_m
     * 	String containing the magnitude error.
     * @param imf
     * 	String containing the IMF exponent.
     * @param z
     * 	String containing the mean metallicity (Z).
     * @param sigmaZ
     * 	String containing the metallicity standard deviation.
     * @param y
     * 	String containing the Helium content (Y).
     * @param sigmaY
     * 	String containing the Helium content standard deviation.
     */
    public ModellingFormInputVerifier(String w, String s_m, String imf, String z, String sigmaZ, String y, String sigmaY)
    {
            
        StringBuilder msg_builder = new StringBuilder();
        
        // Attempt to parse w
        try {
            this.w = ParseUtil.parseAndCheckWithinRangeInclusive(w, "alpha", 0.0, 1.0);
        }
        catch(Exception e) {
        	msg_builder.append(e.getMessage());
            valid = false;
        }      
        
        // Attempt to parse s_m
        try {
            this.s_m = ParseUtil.parseAndCheckGreaterThanOrEqualTo(s_m, "Magnitude error", 0.0);
        }
        catch(Exception e) {
            msg_builder.append(e.getMessage());
            valid = false;
        }
        
        // Attempt to parse imf exponent
        try {
            this.imf = ParseUtil.parseAndCheckLessThan(imf, "IMF exponent", 0.0);
        }
        catch(Exception e) {
        	msg_builder.append(e.getMessage());
            valid = false;
        }
        
        // Attempt to parse z
        try {
            this.z = ParseUtil.parseAndCheckGreaterThan(z, "Mean metallicity (Z)", 0.0);
        }
        catch(Exception e) {
            msg_builder.append(e.getMessage());
            valid = false;
        }
        
        // Attempt to parse y
        try {
            this.y = ParseUtil.parseAndCheckGreaterThan(y, "Mean Helium content (Y)", 0.0);
        }
        catch(Exception e) {
            msg_builder.append(e.getMessage());
            valid = false;
        }
        
        // Attempt to parse sigmaZ
        try {
            this.sigmaZ = ParseUtil.parseAndCheckGreaterThanOrEqualTo(sigmaZ, "Metallicity standard deviation ("+CharUtil.greek_s+"_{Z})", 0.0);
        }
        catch(Exception e) {
            msg_builder.append(e.getMessage());
            valid = false;
        }
        
        // Attempt to parse sigmaY
        try {
            this.sigmaY = ParseUtil.parseAndCheckGreaterThanOrEqualTo(sigmaY, "Helium content standard deviation ("+CharUtil.greek_s+"_{Y})", 0.0);
        }
        catch(Exception e) {
            msg_builder.append(e.getMessage());
            valid = false;
        }
        
        message = msg_builder.toString();
    }
}