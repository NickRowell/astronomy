package wd.wdlf.inversion.gui;

import java.awt.BorderLayout;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;

import sfr.algoimpl.InitialGuessSFR;
import wd.wdlf.infra.EntryForm;
import wd.wdlf.infra.EntryFormResult;
import wd.wdlf.inversion.infra.Convergence;
import wd.wdlf.inversion.infra.InversionState;
import wd.wdlf.inversion.infra.MonteCarloInverter;
import wd.wdlf.inversion.util.InversionPlotUtil;

/**
 * Main form that performs inversion algorithm and displays results of each iteration in GUI.
 * 
 * @author nickrowell
 */
public class InversionForm extends EntryForm {
    /**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 4533462954383185768L;

	/**
	 * The logger.
	 */
	private static final Logger logger = Logger.getLogger(InversionForm.class.getName());
	
	/**
	 * Templates for log messages.
	 */
	private static final String iterUpdStr = "Iteration %d: chi2 = %4.2f - reduction of %5.5f %% in smoothed chi^2 function";
	private static final String preConvStr = "Iteration %d: don't check for convergence within the first %d iterations.";
	
	/**
     * Reference to the main {@link InversionState} instance that encapsulates all the
     * relevent inputs and parameters for the inversion.
	 */
    private final InversionState inversionState;
    
    /**
     * The {@link IPanel} presenting the P_{MS} plot: the joint distribution of progenitor
     * mass and formation time.
     */
    private final JLabel pmsPlotPanel;
    
    /**
     * The {@link IPanel} presenting the P_{WD} plot: the joint distribution of white dwarf
     * mass and magnitude.
     */
    private final JLabel pwdPlotPanel;
    
    /**
     * The {@link IPanel} presenting the chi-square statistic for each iteration.
     */
    private final JLabel chi2Panel;
    
    /**
     * The {@link IPanel} presenting the residuals of the WDLF model fit.
     */
    private final JLabel residualPanel;
    
    /**
     * Main constructor.
     * @param inversionState
     * 	The main {@link InversionState} instance that encapsulates all the relevent inputs and
     * parameters for the inversion.
     */
    public InversionForm(InversionState inversionState) {
    	
    	this.inversionState = inversionState;
    	
        // Set up GUI
        pwdPlotPanel = new JLabel(new ImageIcon());
        pmsPlotPanel = new JLabel(new ImageIcon());
        chi2Panel = new JLabel(new ImageIcon());
        residualPanel = new JLabel(new ImageIcon());
        
        pwdPlotPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        pmsPlotPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        chi2Panel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        residualPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        
        // Set up main panel
        setBorder(BorderFactory.createTitledBorder("Inversion progress... iteration 0"));
        
        setLayout(new BorderLayout());
        
        JPanel pwdPanel = new JPanel(new BorderLayout());
        pwdPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JLabel pwdLabel = new JLabel("<html>"
    			+ "<h3><u>Joint distribution of white dwarf luminosity and mass</u></h3>"
    			+ "</html>", SwingConstants.CENTER);
        
        pwdPanel.add(pwdLabel, BorderLayout.NORTH);
        pwdPanel.add(pwdPlotPanel, BorderLayout.SOUTH);
        
        JPanel pmsPanel = new JPanel(new BorderLayout());
        pmsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JLabel pmsLabel = new JLabel("<html>"
    			+ "<h3><u>Joint distribution of progenitor formation time and mass</u></h3>"
    			+ "</html>", SwingConstants.CENTER);
        
        pmsPanel.add(pmsLabel, BorderLayout.NORTH);
        pmsPanel.add(pmsPlotPanel, BorderLayout.SOUTH);
        
        add(pwdPanel, BorderLayout.WEST);
        add(pmsPanel, BorderLayout.EAST);
        
        initialise();
    }
    
    /**
     * Executes the inversion algorithm iterations in a separate thread.
     */
    public void start() {

    	// Log inversion state
        logger.info("Commencing inversion with configuration:\n"+this.inversionState.toString());
        
        // Write inversion state to file
        File output = new File(inversionState.outputDirectory, "wdlf_inversion_config.txt");
        try (BufferedWriter out = new BufferedWriter(new FileWriter(output))) {
            out.write(this.inversionState.toString());
        }
        catch(IOException ioe) {
            ioe.printStackTrace();
        }
        
    	Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
					performInversion();
				} catch(IOException e) {
					JOptionPane.showMessageDialog(InversionForm.this, e.getMessage(),
							"Error", JOptionPane.ERROR_MESSAGE);
				}
			}
    		
    	};
    	Thread thread = new Thread(runnable);
    	thread.start();
    }
    
    /** 
     * Execute the inversion algorithm iterations.
     */
    public void performInversion() throws IOException {
    	
    	boolean converged = false;
    	Convergence convergence = null;
    	
    	while(!converged) {
    		
            // Generate a new {@link MonteCarloInverter} with current SFR model.
    		final MonteCarloInverter inversion = new MonteCarloInverter(inversionState);
            
			inversion.performSingleIteration();
			
			// TODO: need to update the convergence detection and chi-squared plotting code here to reflect the changes
			// that were made during developments for 2023 paper with Marco.
			
            // List of chi-square statistics to examine for convergence
            List<Double> chi2 = inversionState.iterations==1 ? new LinkedList<Double>() : inversionState.chi2.subList(1,inversionState.chi2.size());
            		
            convergence = Convergence.factory(Convergence.Type.POWERLAW, chi2);
			
			// Update SFR
        	inversionState.currentSfr = (InitialGuessSFR)inversionState.updatedSfr.copy();
        	
            // Plot the diagnostics
            ((ImageIcon)chi2Panel.getIcon()).setImage(convergence.getGnuplotPlot());
            chi2Panel.repaint();
            ((ImageIcon)pwdPlotPanel.getIcon()).setImage(inversion.pwdPlot);
            pwdPlotPanel.repaint();
            ((ImageIcon)pmsPlotPanel.getIcon()).setImage(inversion.pmsPlot);
            pmsPlotPanel.repaint();
            
            // Finished iteration. Check for convergence.
            if(inversionState.iterations==1) {
            	// Don't check on first iteration; this is mainly updating the normalisation
            	// and the Chi-square value is an outlier
            	logger.info("Iteration "+inversionState.iterations+": don't check for convergence");
            }
            // Don't converge within the first {@link InversionState#iterations_min} iterations.
            else if(inversionState.iterations < inversionState.iterations_min) {
            	logger.info(String.format(preConvStr, inversionState.iterations, inversionState.iterations_min));
            }
            else {
                // Get relative change in chi-square with latest iteration
                double fitImprovement = convergence.getRelativeAbsChangeAtLatestIteration();
                
                logger.info(String.format(iterUpdStr, inversionState.iterations, inversionState.getLastChi2(), fitImprovement*100));
        
                // Test for convergence and reset flag.
                if(convergence.hasConverged(inversionState.chi2Threshold)) {
                	logger.info("Converged!\n");
                    converged = true;
                }
            }
            
            ((TitledBorder)getBorder()).setTitle("Inversion progress... iteration " + inversionState.iterations);
            
            // Redraw the GUI
            try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						repaint();
					}});
			}
            catch (InvocationTargetException | InterruptedException e) {
				e.printStackTrace();
			}
    	}
    	
    	// Algorithm has converged
    	((TitledBorder)getBorder()).setTitle("Inversion progress... converged in "+inversionState.iterations+" iterations");
    	
        // Write evolution of chi-square statistic to output file,
        // if one has been specified.
        if (inversionState.writeOutput) {
        	File output = new File(inversionState.outputDirectory, "chisquare");
            try (BufferedWriter out = new BufferedWriter(new FileWriter(output))) {
                out.write(convergence.toString());
                out.write("\n\nAlgorithm converged in "+ inversionState.iterations + " steps.");
                double[] total_stars = inversionState.currentSfr.integrateSFR();
                out.write("Integral of SFR: " + total_stars[0] + " +/- " + total_stars[1]);
            }
            catch(IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

	@Override
	public EntryFormResult verify() {
		// No fields to verify in this case
		return new EntryFormResult();
	}

	@Override
	public void initialise() {
		// Plot the empty P_{MS} and P_{WD} figures.
        try {
        	((ImageIcon)pwdPlotPanel.getIcon()).setImage(InversionPlotUtil.getPwd(inversionState.outputDirectory, inversionState, null));
        	pwdPlotPanel.repaint();
        	((ImageIcon)pmsPlotPanel.getIcon()).setImage(InversionPlotUtil.getPms(inversionState.outputDirectory, inversionState, null));
        	pmsPlotPanel.repaint();
        }
        catch(IOException e) {
        	logger.log(Level.SEVERE, "IOException initialising the "+this.getClass().getSimpleName(), e);
        }
	}
    
}