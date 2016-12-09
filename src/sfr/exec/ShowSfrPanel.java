package sfr.exec;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import sfr.gui.SfrPanel;

/**
 * Class provides a simple application to display the star formation rate model GUIs.
 *
 *
 * @author nrowell
 * @version $Id$
 */
public class ShowSfrPanel {

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
            	JFrame f = new JFrame(SfrPanel.class.getSimpleName() + " Demo");
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                f.add(new SfrPanel());
                f.pack();
                f.setLocationRelativeTo(null);
                f.setVisible(true);
            }
        });
    }
	
}
