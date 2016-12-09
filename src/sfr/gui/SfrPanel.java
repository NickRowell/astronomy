package sfr.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import infra.gui.IPanel;
import infra.io.Gnuplot;
import sfr.infra.SFR;

/**
 * Class provides a handy {@link JPanel} that presents all of the {@link sfr#algo#BaseSfr} implementations
 * available for stellar population modelling, provides forms to set the parameters of each model, and a plot
 * of the currently selected model.
 * 
 * @author nrowell
 * @version $Id$
 */
public class SfrPanel extends JPanel {
	
	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = -7891603542079477716L;

	// Panel with plot of WDLF
    private IPanel sfrModelPlotPanel;
    
    // Components of SFR parameter entry form
    private JPanel sfrParametersPanel;
    
    // Currently selected panel
    public SFR selectedSfrModel;
	
    /**
     * Main constructor.
     * @param workingDir
     * 		The working directory where temporary plot scripts will be saved.
     */
    public SfrPanel() {
        
    	// Right hand panel containing SFR plot
        sfrModelPlotPanel  = new IPanel();
        
        // Panel containing all SFR parameter stuff
        JPanel sfrModelEntryPanel = new JPanel(new BorderLayout());
        
        // Holds entry froms for each basic SFR type
        sfrParametersPanel = new JPanel(new CardLayout());
        
        selectedSfrModel = SFR.CONSTANT;
        
        // Configure SFR type combo box
        final JComboBox<SFR> basicTypeComboBox = new JComboBox<SFR>(SFR.values());
        
        basicTypeComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) 
            {
                selectedSfrModel = (SFR)basicTypeComboBox.getSelectedItem();
                // Show selected SFR parameter form
                CardLayout cl = (CardLayout) (sfrParametersPanel.getLayout());
                cl.show(sfrParametersPanel, selectedSfrModel.toString());
                plotSFR();
            }
        });
        
        // Configure SFR update button
        JButton updateButton = new JButton();
        updateButton.setText("Update SFR model");
        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
            	
            	// Read new SFR parameters
            	selectedSfrModel.getSfrPanel().update();
            	
                // Draw updated SFR model into panel
                plotSFR();  
            }
        });       
        
        // Add all basic SFR types to panel.
        for(SFR sfrType : SFR.values()) {
        	sfrParametersPanel.add(sfrType.getSfrPanel(), sfrType.toString());
        }
        
        // Configure lower SFR entry panel
        sfrModelEntryPanel.setBorder(BorderFactory.createTitledBorder("Select basic SFR model type"));
        sfrModelEntryPanel.add(basicTypeComboBox, BorderLayout.NORTH);
        sfrModelEntryPanel.add(sfrParametersPanel, BorderLayout.CENTER);
        sfrModelEntryPanel.add(updateButton, BorderLayout.SOUTH);

        // Configure upper plot panel
        sfrModelPlotPanel.setBorder(BorderFactory.createTitledBorder("Current SFR model"));
        
        // Initial plot of WDLF.
        plotSFR();
        
        sfrModelPlotPanel.setPreferredSize();
        
        // Add to main panel
        setLayout(new FlowLayout());
        add(sfrModelEntryPanel);
        add(sfrModelPlotPanel);
    }
	
    /**
     * Use Gnuplot to plot the current active (selected) star formation rate model.
     */
    private void plotSFR() {
    	try {
			BufferedImage img = Gnuplot.executeScript(selectedSfrModel.getSfr().getGnuplotScript());
			sfrModelPlotPanel.setImage(img);
		}
    	catch (IOException e1) {
            JOptionPane.showMessageDialog(this, e1.getMessage(), "Plotting Error", JOptionPane.ERROR_MESSAGE);
		}
    }
    
}