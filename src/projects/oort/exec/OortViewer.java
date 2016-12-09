package projects.oort.exec;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import infra.gui.IPanel;
import infra.io.Gnuplot;
import infra.os.OSChecker;
import util.GuiUtil;

/**
 * This class provides an application to visualise the effect of the Oort constants
 * on the streaming motion of stars in the Solar neighbourhood.
 *
 *
 * @author nrowell
 * @version $Id$
 */
public class OortViewer extends JPanel {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = -3866665796677873150L;
	
	/**
	 * The logger.
	 */
	private static final Logger logger = Logger.getLogger(OortViewer.class.getName());
	
	/**
	 * Ranges of the Oort Constants for plotting.
	 */
	private static double min = -20;
	private static double max = 20;
	
	/**
	 * Current value of the Oort Constant A (azimuthal shear).
	 */
	private double A=0.0;

	/**
	 * Current value of the Oort Constant B (vorticity).
	 */
	private double B=0.0;

	/**
	 * Current value of the Oort Constant C (radial shear).
	 */
	private double C=0.0;

	/**
	 * Current value of the Oort Constant K (divergence).
	 */
	private double K=0.0;
	
	/**
	 * Main constructor for the {@link OortViewer}.
	 */
	public OortViewer() {

		final JSlider aSlider = GuiUtil.buildSlider(min, max, 2, "%4.2f");
		final JSlider bSlider = GuiUtil.buildSlider(min, max, 2, "%4.2f");
		final JSlider cSlider = GuiUtil.buildSlider(min, max, 2, "%4.2f");
		final JSlider kSlider = GuiUtil.buildSlider(min, max, 2, "%4.2f");
		aSlider.setValue(50);
		bSlider.setValue(50);
		cSlider.setValue(50);
		kSlider.setValue(50);

        final JLabel aLabel = new JLabel("<html>A "+String.format("(%4.2f) [km s<sup>-1</sup> kpc<sup>-1</sup>]", A)+":</html>");
        final JLabel bLabel = new JLabel("<html>B "+String.format("(%4.2f) [km s<sup>-1</sup> kpc<sup>-1</sup>]", B)+":</html>");
        final JLabel cLabel = new JLabel("<html>C "+String.format("(%4.2f) [km s<sup>-1</sup> kpc<sup>-1</sup>]", C)+":</html>");
        final JLabel kLabel = new JLabel("<html>K "+String.format("(%4.2f) [km s<sup>-1</sup> kpc<sup>-1</sup>]", K)+":</html>");
		
        aLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        bLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        cLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        kLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        aLabel.setHorizontalAlignment(JLabel.CENTER);
        bLabel.setHorizontalAlignment(JLabel.CENTER);
        cLabel.setHorizontalAlignment(JLabel.CENTER);
        kLabel.setHorizontalAlignment(JLabel.CENTER);
        
        JPanel subPanel = new JPanel();
        subPanel.setLayout(new GridLayout(4,2));
        subPanel.add(aLabel);
        subPanel.add(aSlider);
        subPanel.add(bLabel);
        subPanel.add(bSlider);
        subPanel.add(cLabel);
        subPanel.add(cSlider);
        subPanel.add(kLabel);
        subPanel.add(kSlider);
        
		final IPanel ipanel = new IPanel(plotStreamingMotions());
		
		ChangeListener cl = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent evt) {
				JSlider source = (JSlider)evt.getSource();
				
				if(source==aSlider) {
					A = (min + (max - min)*(source.getValue()/100.0));
					aLabel.setText("<html>A "+String.format("(%4.2f) [km s<sup>-1</sup> kpc<sup>-1</sup>]", A)+":</html>");
				}
				else if(source==bSlider) {
					B = (min + (max - min)*(source.getValue()/100.0));
					bLabel.setText("<html>B "+String.format("(%4.2f) [km s<sup>-1</sup> kpc<sup>-1</sup>]", B)+":</html>");
				}
				else if(source==cSlider) {
					C = (min + (max - min)*(source.getValue()/100.0));
					cLabel.setText("<html>C "+String.format("(%4.2f) [km s<sup>-1</sup> kpc<sup>-1</sup>]", C)+":</html>");
				}
				else if(source==kSlider) {
					K = (min + (max - min)*(source.getValue()/100.0));
					kLabel.setText("<html>K "+String.format("(%4.2f) [km s<sup>-1</sup> kpc<sup>-1</sup>]", K)+":</html>");
				}
				
				ipanel.setImage(plotStreamingMotions());
			}
 
        };
        
        // Add right-click menu to allow saving of image to disk etc.
        JPopupMenu menuPopup = new JPopupMenu();
        menuPopup.getAccessibleContext().setAccessibleDescription("File Menu");
        final JMenuItem saveImageMenuItem = new JMenuItem("Save image to file");
        final JMenuItem exitAppMenuItem = new JMenuItem("Exit", KeyEvent.VK_X);
        menuPopup.add(saveImageMenuItem);
        menuPopup.add(exitAppMenuItem);
        
        exitAppMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                System.exit(0);
            }
        });
        
        saveImageMenuItem.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				
				// Use file chooser dialog to select file save location:
				JFileChooser chooser = new JFileChooser();
				chooser.setDialogTitle("Set output file...");
				chooser.addChoosableFileFilter(new FileFilter() {
					public boolean accept(File file) {
			    		String filename = file.getName();
			    		return filename.endsWith(".png");
			    	}
			    	public String getDescription() {
			    		return "*.png";
			    	}
				});
				
				int userSelection = chooser.showSaveDialog(OortViewer.this);
				 
				if (userSelection == JFileChooser.APPROVE_OPTION) {
					File file = chooser.getSelectedFile();
					// Add extension '.png' if this was not given by the user
					String extension = "";
					int i = file.getName().lastIndexOf('.');
					if (i > 0) {
					    extension = file.getName().substring(i+1);
					}
					if (!extension.equalsIgnoreCase("png")) {
					    file = new File(file.toString() + ".png");
					}
					
					try {
						ImageIO.write(ipanel.image, "PNG", file);
					} catch (IOException e1) {
						JOptionPane.showMessageDialog(OortViewer.this, 
								"Error saving to file "+file.getAbsolutePath()+"!",
								"Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
        });
        
        ipanel.setComponentPopupMenu(menuPopup);
        
        aSlider.addChangeListener(cl);
        bSlider.addChangeListener(cl);
        cSlider.addChangeListener(cl);
        kSlider.addChangeListener(cl);
        
		this.setLayout(new GridLayout(1,2));
		this.add(ipanel);
		this.add(subPanel);
	}
	
	/**
	 * Creates and returns a {@link BufferedImage} containing a plot of the streaming motions of
	 * nearby stars given the currently selected values of the Oort Constants.
	 * @return
	 * 	A {@link BufferedImage} containing a plot of the streaming motions of
	 * nearby stars given the currently selected values of the Oort Constants.
	 */
	public BufferedImage plotStreamingMotions() {
		
		// Extent of the region plotted [pc]
		double xmin = -200;
		double xmax =  200;
		double ymin = -200;
		double ymax =  200;
		
		// Steps between points at which to draw velocity vectors [pc]
		double xstep = 20;
		double ystep = 20;
		
		StringBuilder script = new StringBuilder();
		script.append("set terminal pngcairo enhanced color size 512,512").append(OSChecker.newline);
		script.append("set xrange ["+xmin+":"+xmax+"]").append(OSChecker.newline);
		script.append("set yrange ["+ymin+":"+ymax+"]").append(OSChecker.newline);
		script.append("set key off").append(OSChecker.newline);
		script.append("set xtics in").append(OSChecker.newline);
		script.append("set ytics in").append(OSChecker.newline);
		script.append("set xlabel 'X [pc]'").append(OSChecker.newline);
		script.append("set ylabel 'Y [pc]'").append(OSChecker.newline);
		
		script.append("set style arrow 1 head filled ls 1 lc rgbcolor 'black'").append(OSChecker.newline);

		// kpc -> pc
		double kpc_to_pc = 1000.0;
		
		// Velocity scale factor
		double s = 0.00001;
		
		for(double x=xmin+xstep; x<xmax; x+=xstep) {
			for(double y=ymin+ystep; y<ymax; y+=ystep) {
				
				// x & y components of the velocity vector at this point
				double vx = (K+C)*kpc_to_pc*x + (A-B)*kpc_to_pc*y;
				double vy = (A+B)*kpc_to_pc*x + (K-C)*kpc_to_pc*y;
				
				// Start point of the arrow
				double xa = x - (vx*s/2.0);
				double ya = y - (vy*s/2.0);

				// End point of the arrow
				double xb = x + (vx*s/2.0);
				double yb = y + (vy*s/2.0);

				script.append("set arrow from "+xa+","+ya+" to "+xb+","+yb+" as 1").append(OSChecker.newline);

			}
		}
		
		script.append("plot '-' w p pt 4 ps 0.25, '-' w p pt 6 ps 1.0 lc rgbcolor 'black'").append(OSChecker.newline);
		for(double x=xmin+xstep; x<xmax; x+=xstep) {
			for(double y=ymin+ystep; y<ymax; y+=ystep) {
				script.append(x+" "+y).append(OSChecker.newline);
			}
		}
		script.append("e").append(OSChecker.newline);
		script.append("0 0").append(OSChecker.newline);
		script.append("e").append(OSChecker.newline);
		
		BufferedImage plot = null; 
		try {
			plot = Gnuplot.executeScript(script.toString());
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Exception during plotting!", e);
		}
		
		return plot;
	}
	
	/**
	 * Main application entry point.
	 * @param args
	 * 	The args - ignored.
	 */
	public static void main(String[] args) {
		
		final JFrame frame = new JFrame("Oort Constants Visualisation");
		
		SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
            	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setLayout(new BorderLayout());
                frame.add(new OortViewer(), BorderLayout.CENTER);
                frame.setSize(1500, 750);
                frame.pack();
                frame.setVisible(true);
            }
        });
	}
}
