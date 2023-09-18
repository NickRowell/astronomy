package sfr.exec;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import infra.io.Gnuplot;
import sfr.algo.BaseSfr;
import sfr.gui.SfrForm;
import wd.wdlf.modelling.infra.ModellingState;

/**
 * Class provides a simple application to display the star formation rate model GUIs.
 *
 * @author nrowell
 * @version $Id$
 */
public class ShowSfrPanel extends JFrame implements ActionListener {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 7801914039090477747L;
	
	/**
	 * The {@link ModellingState} instance that stores the {@link BaseSfr} instance we're plotting.
	 */
	ModellingState state = new ModellingState();
	
	/**
	 * The {@link SfrForm} instance.
	 */
	SfrForm sfrForm = new SfrForm(state);
	
	/**
	 * An {@link ImageIcon} used to display the SFR.
	 */
	ImageIcon icon = new ImageIcon();
	
	/**
	 * Default constructor.
	 * @throws IOException 
	 */
	public ShowSfrPanel() throws IOException {
		
		super(SfrForm.class.getSimpleName() + " Demo");
    	
    	setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        add(sfrForm, BorderLayout.WEST);
        
        // Get the initial plot
        String script = state.syntheticSFR.getGnuplotScript();
        BufferedImage img = Gnuplot.executeScript(script);
        icon = new ImageIcon(img);
        
        JLabel imgLabel = new JLabel(icon);
        add(imgLabel, BorderLayout.EAST);
        
        sfrForm.addActionListener(this);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		// Plot the current SFR
        String script = state.syntheticSFR.getGnuplotScript();
        
		try {
			icon.setImage(Gnuplot.executeScript(script));
			repaint();
		}
		catch (IOException e1) {
			e1.printStackTrace();
		}
	}
		
    /**
     * Method used to test construction & layout of the JPanel.
     * @param args
     * 	The args [not used]
     */
    public static void main(String[] args)
    {
    	SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
				try {
					ShowSfrPanel f = new ShowSfrPanel();
					f.pack();
	                f.setLocationRelativeTo(null);
	                f.setVisible(true);
				} catch (IOException e) {
					e.printStackTrace();
				}
                
            }
        });
    }
}
