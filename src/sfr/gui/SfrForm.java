package sfr.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import sfr.infra.SFR;
import wd.wdlf.modelling.infra.ModellingState;

/**
 * This class provides a GUI element used to select between different {@link SFR} instances and
 * configure their parameters.
 * 
 * @author nrowell
 */
public class SfrForm extends JPanel {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 5276984865834118126L;
	
	/**
	 * List of registered listeners.
	 */
	List<ActionListener> listeners = new LinkedList<>();
	
    /**
     * Main constructor.
     * 
     * @param state
     * 	The {@link ModellingState} instance whose internal {@link BaseSfr} instance will be updated by
     * this form.
     */
    public SfrForm(ModellingState state) {
    	
    	if(state.syntheticSFR == null) {
        	state.syntheticSFR = SFR.CONSTANT.getSfr();
        }
        
        // Holds entry forms for each basic SFR type
        JPanel sfrParametersPanel = new JPanel(new CardLayout());
        
        // Add all basic SFR types to panel.
        for(SFR sfrType : SFR.values()) {
        	sfrParametersPanel.add(sfrType.getSfrPanel(), sfrType.toString());
        }

        // Configure SFR type combo box
        final JComboBox<SFR> basicTypeComboBox = new JComboBox<SFR>(SFR.values());
        
        basicTypeComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) 
            {
                SFR selectedSfrModel = (SFR)basicTypeComboBox.getSelectedItem();
                
                state.syntheticSFR = selectedSfrModel.getSfr();
                
                // Show selected SFR parameter form
                CardLayout cl = (CardLayout) (sfrParametersPanel.getLayout());
                cl.show(sfrParametersPanel, selectedSfrModel.toString());
                
            	ActionEvent ae = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "SFR function changed");
            	for(ActionListener al : listeners) {
            		al.actionPerformed(ae);
            	}
            }
        });
        
        // Configure SFR update button
        JButton updateButton = new JButton();
        updateButton.setText("Update SFR model");
        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
            	
            	// Read new SFR parameters
            	SFR selectedSfrModel = (SFR)basicTypeComboBox.getSelectedItem();
            	selectedSfrModel.getSfrPanel().update();
            	
            	ActionEvent ae = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "SFR parameters updated");
            	for(ActionListener al : listeners) {
            		al.actionPerformed(ae);
            	}
            }
        });       
        
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Select basic SFR model type"));
        add(basicTypeComboBox, BorderLayout.NORTH);
        add(sfrParametersPanel, BorderLayout.CENTER);
        add(updateButton, BorderLayout.SOUTH);
    }
    
    /**
     * Register the {@link ActionListener} instance.
     * 
     * @param al
     * 	The {@link ActionListener} instance.
     */
    public void addActionListener(ActionListener al) {
    	listeners.add(al);
    }
    
    /**
     * Remove the {@link ActionListener} instance.
     * 
     * @param al
     * 	The {@link ActionListener} instance.
     */
    public void removeActionListener(ActionListener al) {
    	listeners.remove(al);
    }
}
