/*
 * Gaia CU5 DU10
 *
 * (c) 2005-2020 Gaia Data Processing and Analysis Consortium
 *
 *
 * CU5 photometric calibration software is free software; you can redistribute
 * it and/or modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * CU5 photometric calibration software is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this CU5 software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *
 *-----------------------------------------------------------------------------
 */

package projects.wdlf47tuc;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jfree.chart.ChartColor;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYPolygonAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYErrorRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.Range;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.xy.YIntervalSeries;
import org.jfree.data.xy.YIntervalSeriesCollection;

import photometry.Filter;
import util.GuiUtil;

/**
 * 
 * @author nrowell
 * @version $Id$
 */
public class ProcessAllSwathcal extends JPanel {
	
	/**
	 * The logger.
	 */
	private static final Logger logger = Logger.getLogger(ProcessAllSwathcal.class.getName());
	
	/**
	 * The serial version UID
	 */
	private static final long serialVersionUID = 7362072105533140618L;

	/**
	 * Available colour filters
	 */
	static Filter[] filters = new Filter[]{Filter.F110W_WFC3_IR, Filter.F160W_WFC3_IR, Filter.F390W_WFC3_UVIS, Filter.F606W_WFC3_UVIS};

	/**
	 * Range on chi-squared slider
	 */
	static final double chi2RangeMin = 0.0, chi2RangeMax = 10.0;
	
	/**
	 * Range on sharp slider
	 */
	static final double sharpRangeMin = -0.5, sharpRangeMax = 0.5;
	
	/**
	 * Range on mag error slider
	 */
	static final double magErrorRangeMin = 0.0, magErrorRangeMax = 1.0;
	
	/**
	 * Upper threshold on chi-square
	 */
	double chi2Max = 1.3;
	
	/**
	 * Lower threshold on 'sharp' statistic.
	 * LOW values suggest galaxy, HIGH values suggest noise (?)
	 */
	double sharpMin = -0.02;
	
	/**
	 * Upper threshold on 'sharp' statistic.
	 * LOW values suggest galaxy, HIGH values suggest noise (?)
	 */
	double sharpMax = 0.06;
	
	/**
	 * Upper limit on photometric error for selection [mag]
	 */
	double magErrMax = 0.1;
	
	/**
	 * The adopted distance modulus.
	 * 
	 * The default value is from Hansen et al. (2013)
	 */
	double mu=13.32;
	
	/**
	 * Filter to use for magnitude axis
	 */
	Filter magFilter = Filter.F390W_WFC3_UVIS;
	
	/**
	 * First filter to use for colour axis; colour = {@link #col1Filter} - {@link #col2Filter}.
	 */
	Filter col1Filter = Filter.F390W_WFC3_UVIS;
	
	/**
	 * Second filter to use for colour axis; colour = {@link #col1Filter} - {@link #col2Filter}.
	 */
	Filter col2Filter = Filter.F606W_WFC3_UVIS;
	
	/**
	 * List of all loaded {@link Source}s.
	 */
	List<Source> allSources = new LinkedList<>();
	
	/**
	 * List of all {@link Source}s that passed the current selection criteria and are being plotted.
	 * This is reset whenever the selection criteria OR the colour/magnitude combination changes.
	 */
	List<Source> selectedSources = new LinkedList<>();
	
	/**
	 * List of all {@link Source}s that are inside current boxed area. This is reset whenever the selection
	 * criteria change OR the colour/magnitude combinatino changes OR the box region changes.
	 */
	List<Source> boxedSources = new LinkedList<>();
	
	/**
	 * Panel containing CMD plot
	 */
	ChartPanel cmdPanel;
	
	/**
	 * Arbitrary box used to define WD selection region.
	 */
	List<double[]> points = new LinkedList<>();
	
	/**
	 * Main entry point.
	 * 
	 * @param args
	 * 
	 * @throws IOException 
	 * 
	 */
	public ProcessAllSwathcal() {
		
		// Path to AllSwathcal.dat file
		File allSwathcal = new File("/home/nrowell/Astronomy/Data/47_Tuc/Kalirai_2012/UVIS/www.stsci.edu/~jkalirai/47Tuc/AllSwathcal.dat");
		
		// Read file contents into the List
		try (BufferedReader in = new BufferedReader(new FileReader(allSwathcal))) {
			String sourceStr;
			while((sourceStr=in.readLine())!=null) {
				Source source = Source.parseSource(sourceStr);
				if(source != null) {
					allSources.add(source);
				}
			}
		}
		catch(IOException e) {
		}
		
		logger.info("Parsed "+allSources.size()+" Sources from AllSwathcal.dat");
		
		// Initialise chart
		cmdPanel = new ChartPanel(updateDataAndPlotCmd(allSources));
		cmdPanel.addChartMouseListener(new ChartMouseListener() {
            @Override
            public void chartMouseClicked(ChartMouseEvent e) {
            	// Capture mouse click location, transform to graph coordinates and add
            	// a point to the polygonal selection box.
            	Point2D p = cmdPanel.translateScreenToJava2D(e.getTrigger().getPoint());
            	Rectangle2D plotArea = cmdPanel.getScreenDataArea();
            	XYPlot plot = (XYPlot) cmdPanel.getChart().getPlot();
            	double chartX = plot.getDomainAxis().java2DToValue(p.getX(), plotArea, plot.getDomainAxisEdge());
            	double chartY = plot.getRangeAxis().java2DToValue(p.getY(), plotArea, plot.getRangeAxisEdge());
            	points.add(new double[]{chartX, chartY});
            	cmdPanel.setChart(plotCmd());
            }

            @Override
            public void chartMouseMoved(ChartMouseEvent arg0) {
            }
        });
		
		// Create colour combo boxes
		final JComboBox<Filter> magComboBox = new JComboBox<Filter>(filters);
		final JComboBox<Filter> col1ComboBox = new JComboBox<Filter>(filters);
		final JComboBox<Filter> col2ComboBox = new JComboBox<Filter>(filters);
		
		// Set initial values
		magComboBox.setSelectedItem(magFilter);
		col1ComboBox.setSelectedItem(col1Filter);
		col2ComboBox.setSelectedItem(col2Filter);
		
		// Create an action listener for these
		ActionListener al = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) 
            {
            	if(evt.getSource()==magComboBox)
            	{
            		magFilter = (Filter)magComboBox.getSelectedItem();
            	}
            	if(evt.getSource()==col1ComboBox)
            	{
            		col1Filter = (Filter)col1ComboBox.getSelectedItem();
            	}
            	if(evt.getSource()==col2ComboBox)
            	{
            		col2Filter = (Filter)col2ComboBox.getSelectedItem();
            	}
            	// Changed colour(s), so reset selection box coordinates
            	points.clear();
				cmdPanel.setChart(updateDataAndPlotCmd(allSources));
            }
        };
        magComboBox.addActionListener(al);
        col1ComboBox.addActionListener(al);
        col2ComboBox.addActionListener(al);
        // Add a bit of padding to space things out
		magComboBox.setBorder(new EmptyBorder(5,5,5,5));
		col1ComboBox.setBorder(new EmptyBorder(5,5,5,5));
		col2ComboBox.setBorder(new EmptyBorder(5,5,5,5));
		
		// Set up statistic sliders
		final JSlider magErrMaxSlider = GuiUtil.buildSlider(magErrorRangeMin, magErrorRangeMax, 3, "%3.3f");
		final JSlider chi2MaxSlider = GuiUtil.buildSlider(chi2RangeMin, chi2RangeMax, 3, "%3.3f");
		final JSlider sharpMinSlider = GuiUtil.buildSlider(sharpRangeMin, sharpRangeMax, 3, "%3.3f");
		final JSlider sharpMaxSlider = GuiUtil.buildSlider(sharpRangeMin, sharpRangeMax, 3, "%3.3f");
		
		// Set intial values
        magErrMaxSlider.setValue((int)Math.rint(100.0*(magErrMax-magErrorRangeMin)/(magErrorRangeMax - magErrorRangeMin)));
        chi2MaxSlider.setValue((int)Math.rint(100.0*(chi2Max-chi2RangeMin)/(chi2RangeMax - chi2RangeMin)));
        sharpMinSlider.setValue((int)Math.rint(100.0*(sharpMin-sharpRangeMin)/(sharpRangeMax - sharpRangeMin)));
        sharpMaxSlider.setValue((int)Math.rint(100.0*(sharpMax-sharpRangeMin)/(sharpRangeMax - sharpRangeMin)));
        
        // Set labels & initial values
		final JLabel magErrMaxLabel = new JLabel(getMagErrMaxLabel());
		final JLabel chi2MaxLabel = new JLabel(getChi2MaxLabel());
		final JLabel sharpMinLabel = new JLabel(getSharpMinLabel());
		final JLabel sharpMaxLabel = new JLabel(getSharpMaxLabel());
		
		// Create a change listener fot these
		ChangeListener cl = new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent e)
			{
				JSlider source = (JSlider)e.getSource();
				
				if(source==magErrMaxSlider) {
					// Compute max mag error from slider position
					double newMagErrMax = magErrorRangeMin + (magErrorRangeMax - magErrorRangeMin)*(source.getValue()/100.0);
					magErrMax = newMagErrMax;
					magErrMaxLabel.setText(getMagErrMaxLabel());
				}
				if(source==chi2MaxSlider) {
					// Compute Chi2 max from slider position
					double newChi2Max = chi2RangeMin + (chi2RangeMax - chi2RangeMin)*(source.getValue()/100.0);
					chi2Max = newChi2Max;
					chi2MaxLabel.setText(getChi2MaxLabel());
				}
				if(source==sharpMinSlider) {
					// Compute sharp min from slider position
					double newSharpMin = sharpRangeMin + (sharpRangeMax - sharpRangeMin)*(source.getValue()/100.0);
					sharpMin = newSharpMin;
					sharpMinLabel.setText(getSharpMinLabel());
				}
				if(source==sharpMaxSlider) {
					// Compute sharp max from slider position
					double newSharpMax = sharpRangeMin + (sharpRangeMax - sharpRangeMin)*(source.getValue()/100.0);
					sharpMax = newSharpMax;
					sharpMaxLabel.setText(getSharpMaxLabel());
				}
				cmdPanel.setChart(updateDataAndPlotCmd(allSources));
			}
		};
		magErrMaxSlider.addChangeListener(cl);
		chi2MaxSlider.addChangeListener(cl);
		sharpMinSlider.addChangeListener(cl);
		sharpMaxSlider.addChangeListener(cl);
		// Add a bit of padding to space things out
		magErrMaxSlider.setBorder(new EmptyBorder(5,5,5,5));
		chi2MaxSlider.setBorder(new EmptyBorder(5,5,5,5));
		sharpMinSlider.setBorder(new EmptyBorder(5,5,5,5));
		sharpMaxSlider.setBorder(new EmptyBorder(5,5,5,5));
		
		// Text field to store distance modulus
		final JTextField distanceModulusField = new JTextField(Double.toString(mu));
		distanceModulusField.setBorder(new EmptyBorder(5,5,5,5));
		
		Border compound = BorderFactory.createCompoundBorder(
				new LineBorder(this.getBackground(), 5), BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		
		final JButton lfButton = new JButton("Luminosity function for selection");
		lfButton.setBorder(compound);
		lfButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
				// Read distance modulus field
				try {
					double mu_new = Double.parseDouble(distanceModulusField.getText());
					mu = mu_new;
				}
				catch(NullPointerException | NumberFormatException ex) {
					JOptionPane.showMessageDialog(lfButton, "Error parsing the distance modulus: "+ex.getMessage(),
		                    "Distance Modulus Error", JOptionPane.ERROR_MESSAGE);  
					return;
				}
				
				if(boxedSources.isEmpty()) {
					JOptionPane.showMessageDialog(lfButton, "No sources are currently selected!",
		                    "Selection Error", JOptionPane.ERROR_MESSAGE);  
				}
				else {
					computeAndPlotLuminosityFunction(boxedSources);
				}
			}
		});
		final JButton clearSelectionButton = new JButton("Clear selection");
		clearSelectionButton.setBorder(compound);
		clearSelectionButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				points.clear();
				cmdPanel.setChart(plotCmd());
			}
		});
		
		JPanel controls = new JPanel(new GridLayout(9,2));
		controls.setBorder(new EmptyBorder(10, 10, 10, 10));
		controls.add(new JLabel("Magnitude = "));
		controls.add(magComboBox);
		controls.add(new JLabel("Colour 1 = "));
		controls.add(col1ComboBox);
		controls.add(new JLabel("Colour 2 = "));
		controls.add(col2ComboBox);
		controls.add(magErrMaxLabel);
		controls.add(magErrMaxSlider);
		controls.add(chi2MaxLabel);
		controls.add(chi2MaxSlider);
		controls.add(sharpMinLabel);
		controls.add(sharpMinSlider);
		controls.add(sharpMaxLabel);
		controls.add(sharpMaxSlider);
		controls.add(new JLabel("Adopted distance modulus = "));
		controls.add(distanceModulusField);
		controls.add(lfButton);
		controls.add(clearSelectionButton);
		
		this.setLayout(new BorderLayout());
		this.add(cmdPanel, BorderLayout.CENTER);
		this.add(controls, BorderLayout.SOUTH);
		
		this.validate();
	}
	
	/**
	 * Get a label appropriate for the maximum magnitude error field.
	 * @return
	 * 	A label appropriate for the maximum magnitude error field.
	 */
	private String getMagErrMaxLabel() {
		return String.format("Maximum magnitude error [%3.3f]:", magErrMax);
	}

	/**
	 * Get a label appropriate for the maximum chi-square field.
	 * @return
	 * 	A label appropriate for the maximum chi-square field.
	 */
	private String getChi2MaxLabel() {
		return String.format("Maximum chi-square [%3.3f]:", chi2Max);
	}

	/**
	 * Get a label appropriate for the minimum sharp field.
	 * @return
	 * 	A label appropriate for the minimum sharp field.
	 */
	private String getSharpMinLabel() {
		return String.format("Minimum sharp [%3.3f]:", sharpMin);
	}
	
	/**
	 * Get a label appropriate for the maximum sharp field.
	 * @return
	 * 	A label appropriate for the maximum sharp field.
	 */
	private String getSharpMaxLabel() {
		return String.format("Maximum sharp [%3.3f]:", sharpMax);
	}
	
	/**
	 * Get an appropriate range for the Y axis based on the particular filter chosen.
	 * 
	 * @return
	 * 	A range suitable for plotting the data
	 */
	private Range getYRange() {
		switch(magFilter) {
			case F110W_WFC3_IR:
				return new Range(13.0, 29.0);
			case F160W_WFC3_IR:
				return new Range(13.0, 29.0);
			case F390W_WFC3_UVIS:
				return new Range(16.0, 30.0);
			case F606W_WFC3_UVIS:
				return new Range(15.0, 30.0);
			default:
				throw new RuntimeException("Unrecognized Filter: "+magFilter);
		}
	}
	
	/**
	 * Get an appropriate range for the X axis based on the particular combination of
	 * filters chosen.
	 * 
	 * @return
	 * 	A range suitable for plotting the data
	 */
	private Range getXRange() {
		
		if(col1Filter==Filter.F110W_WFC3_IR) {
			switch(col2Filter) {
				case F110W_WFC3_IR: return new Range(0,0);
				case F160W_WFC3_IR: return new Range(-0.25, 1.25);
				case F390W_WFC3_UVIS: return new Range(-7.5, 2.5);
				case F606W_WFC3_UVIS: return new Range(-4.0, 1.0);
				default:
					throw new RuntimeException("Unrecognized Filter: "+col2Filter);
			}
		}
		else if(col1Filter==Filter.F160W_WFC3_IR) {
			switch(col2Filter) {
				case F110W_WFC3_IR: return new Range(-1.25, 0.25);
				case F160W_WFC3_IR: return new Range(0,0);
				case F390W_WFC3_UVIS: return new Range(-8, 2);
				case F606W_WFC3_UVIS: return new Range(-7, 2);
				default:
					throw new RuntimeException("Unrecognized Filter: "+col2Filter);
			}
		}
		else if(col1Filter==Filter.F390W_WFC3_UVIS) {
			switch(col2Filter) {
				case F110W_WFC3_IR: return new Range(-2.5, 7.5);
				case F160W_WFC3_IR: return new Range(-2, 8);
				case F390W_WFC3_UVIS: return new Range(0,0);
				case F606W_WFC3_UVIS: return new Range(-2, 4);
				default:
					throw new RuntimeException("Unrecognized Filter: "+col2Filter);
			}
		}
		else if(col1Filter==Filter.F606W_WFC3_UVIS) {
			switch(col2Filter) {
				case F110W_WFC3_IR: return new Range(-1.0, 4.0);
				case F160W_WFC3_IR: return new Range(-2, 7);
				case F390W_WFC3_UVIS: return new Range(-4, 2);
				case F606W_WFC3_UVIS: return new Range(0,0);
				default:
					throw new RuntimeException("Unrecognized Filter: "+col2Filter);
			}
		}
		return null;
	}
	
	/**
	 * Determines if the given {@link Source} passes the current selection criteria.
	 * @param source
	 * 	The {@link Source}
	 * @return
	 * 	Boolean stating whether the {@link Source} passes the current selection criteria.
	 */
	private boolean passedSelection(Source source) {
		
		if(source.sharp < sharpMin || source.sharp > sharpMax) {
			// Source fails selection on sharp statistic
			return false;
		}
		
		if(source.chi2 > chi2Max) {
			// Source fails selection on chi-square statistic
			return false;
		}
		
		if(source.getMag(magFilter) > 50 || source.getMag(col1Filter) > 50 || source.getMag(col2Filter) > 50) {
			// Source is undetected at one or more bands
			return false;
		}
		
		if(source.getMagError(magFilter) > magErrMax || source.getMagError(col1Filter) > magErrMax
				|| source.getMagError(col2Filter) > magErrMax) {
			// Source is undetected at one or more bands
			return false;
		}
		
		// All selection criteria passed
		return true;
	}
	
	/**
	 * Update the plot data, then plot the CMD and return the resulting chart.
	 * Called whenever the underlying dataset changes - adjustments to the selection criteria
	 * and/or the particular colour filters used.
	 * @param sources
	 * 	The {@link Source}s to plot
	 * @return
	 * 	A JFreeChart presenting the colour-magnitude diagram for the current selection criteria and colours.
	 */
	private JFreeChart updateDataAndPlotCmd(List<Source> sources) {
		
		selectedSources.clear();
		for(Source source : sources) {
			if(passedSelection(source)) {
				selectedSources.add(source);
			}
		}
		return plotCmd();
	}
	
	/**
	 * Plots the CMD using the existing dataset. Used whenever chart annotations change, without the
	 * underlying plot data changing. This method identifies all sources lying within the boxed region
	 * and loads them into the secondary list {@link #boxedSources}.
	 * 
	 * @param allSources
	 * 	The {@link Source}s to plot
	 * @return
	 * 	A JFreeChart presenting the colour-magnitude diagram for the current selection criteria and colours.
	 */
	private JFreeChart plotCmd() {
		
		XYSeries outside = new XYSeries("Outside");
		XYSeries inside  = new XYSeries("Inside");
		
		// Use a Path2D.Double instance to determine polygon intersection
		Path2D.Double path = new Path2D.Double();
		boxedSources.clear();
		
		boolean performBoxSelection = (points.size()>2);
		
		if(performBoxSelection) {
			// Initialise Path2D object
			path.moveTo(points.get(0)[0], points.get(0)[1]);
			for(double[] point : points) {
				path.lineTo(point[0], point[1]);
			}
		}
		
		for(Source source : selectedSources) {
			double magnitude = source.getMag(magFilter);
			double col1 = source.getMag(col1Filter);
			double col2 = source.getMag(col2Filter);
			
			double x = col1 - col2;
			double y = magnitude;
			
			if(performBoxSelection){
				Point2D.Double point = new Point2D.Double(x,y);
				if(path.contains(point)) {
					inside.add(x, y);
					boxedSources.add(source);
				}
				else {
					outside.add(x, y);
				}
			}
			else {
				outside.add(x,y);
			}
		}
		
		final XYSeriesCollection data = new XYSeriesCollection();
		data.addSeries(outside);
		data.addSeries(inside);
		
    	XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesLinesVisible(0, false);
        renderer.setSeriesShapesVisible(0, true);
        renderer.setSeriesShape(0, new Ellipse2D.Float(-0.5f, -0.5f, 1, 1));
        renderer.setSeriesPaint(0, ChartColor.BLACK);
        
        renderer.setSeriesLinesVisible(1, false);
        renderer.setSeriesShapesVisible(1, true);
        renderer.setSeriesShape(1, new Ellipse2D.Float(-0.5f, -0.5f, 1, 1));
        renderer.setSeriesPaint(1, ChartColor.RED);
        
        NumberAxis xAxis = new NumberAxis(col1Filter.toString()+" - "+col2Filter.toString());
        xAxis.setRange(getXRange());
        
        NumberAxis yAxis = new NumberAxis(magFilter.toString());
        yAxis.setRange(getYRange());
        yAxis.setInverted(true);
        
        // Configure plot
        XYPlot xyplot = new XYPlot(data, xAxis, yAxis, renderer);
        xyplot.setBackgroundPaint(Color.lightGray);
        xyplot.setDomainGridlinePaint(Color.white);
        xyplot.setDomainGridlinesVisible(true);
        xyplot.setRangeGridlinePaint(Color.white);
        
        // Specify selection box, if points have been specified
        if(!points.isEmpty()) {
        	
        	double[] coords = new double[points.size()*2];
        	
        	for(int i=0; i<points.size(); i++) {
        		double[] point = points.get(i);
        		coords[2*i + 0] = point[0];
        		coords[2*i + 1] = point[1];
        	}
	        XYPolygonAnnotation box = new XYPolygonAnnotation(coords, new BasicStroke(2.0f), Color.BLUE);
	        xyplot.addAnnotation(box);
        }
        
        // Configure chart
        JFreeChart chart = new JFreeChart("47 Tuc CMD", xyplot);
        chart.setBackgroundPaint(Color.white);
		chart.setTitle("47 Tuc colour-magnitude diagram");
		chart.removeLegend();
		
		return chart;
	}
	
	
	/**
	 * Computes the luminosity function for the current boxed region and plots it in a JFrame.
	 * Also prints out the coordinates of the selection box vertices and the luminosity function
	 * quantities.
	 * 
	 * @param sources
	 * 	The {@link Source}s to compute the luminosity function for.
	 */
	private void computeAndPlotLuminosityFunction(List<Source> sources) {
		
		// Print out coordinates of selection box corners
		System.out.println("# Coordinates of selection box corners:");
		System.out.println("# ("+col1Filter+"-"+col2Filter+")\t"+magFilter);
		for(double[] point : points) {
			System.out.println("# "+point[0] + "\t" + point[1]);
		}
		System.out.println("# Luminosity function:");
		System.out.println("# Mag.\tN\tsigN");
		
		
		double magBinWidth = 0.5;
		
		// Get the range of the data
		double mMin = Double.MAX_VALUE;
		double mMax = -Double.MAX_VALUE;
		for(Source source : sources) {
			double mag = source.getMag(magFilter) - mu;
			mMin = Math.min(mMin, mag);
			mMax = Math.max(mMax, mag);
		}
		
		// Quantize this to a whole number
		mMin = Math.floor(mMin);
		mMax = Math.ceil(mMax);
		
		int nBins = (int)Math.rint((mMax - mMin)/magBinWidth);
		
		// Array to accumulate all objects in each bin
		int[] n = new int[nBins];
		
		for(Source source : sources) {
			double mag = source.getMag(magFilter) - mu;
			// Bin number
			int bin = (int)Math.floor((mag-mMin)/magBinWidth);
			n[bin]++;
		}
		
		YIntervalSeries luminosityFunction = new YIntervalSeries("Luminosity Function");
		
		for(int i=0; i<nBins; i++) {
			// Bin centre
			double x = mMin + i*magBinWidth + 0.5*magBinWidth;
			double y = n[i];
			double yErr = n[i]>0 ? Math.sqrt(y) : 0;
			luminosityFunction.add(x, y, y-yErr, y+yErr);
			System.out.println(x+"\t"+y+"\t"+yErr);
		}
		
		final YIntervalSeriesCollection data = new YIntervalSeriesCollection();
		data.addSeries(luminosityFunction);
		
    	XYErrorRenderer renderer = new XYErrorRenderer();
        renderer.setSeriesLinesVisible(0, true);
        renderer.setSeriesShapesVisible(0, true);
        renderer.setSeriesShape(0, new Ellipse2D.Float(-1f, -1f, 2, 2));
        renderer.setSeriesPaint(0, ChartColor.BLACK);
        
        NumberAxis xAxis = new NumberAxis("Absolute Magnitude (" + magFilter.toString()+")");
        xAxis.setAutoRange(true);
        xAxis.setAutoRangeIncludesZero(false);
        
        NumberAxis yAxis = new NumberAxis("N");
        yAxis.setAutoRange(true);
        yAxis.setAutoRangeIncludesZero(true);
        
        // Configure plot
        XYPlot xyplot = new XYPlot(data, xAxis, yAxis, renderer);
        xyplot.setBackgroundPaint(Color.lightGray);
        xyplot.setDomainGridlinePaint(Color.white);
        xyplot.setDomainGridlinesVisible(true);
        xyplot.setRangeGridlinePaint(Color.white);
        
        // Configure chart
        JFreeChart chart = new JFreeChart("Luminosity Function", xyplot);
        chart.setBackgroundPaint(Color.white);
		chart.setTitle("47 Tuc luminosity function");
		chart.removeLegend();
		
		
		final ChartPanel lfChartPanel = new ChartPanel(chart);
		
		java.awt.EventQueue.invokeLater(
		        new Runnable() 
		            {
		                @Override
		                public void run() 
		                {
		                    JFrame tester = new JFrame();
		                    tester.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		                    tester.setLayout(new BorderLayout());
		                    tester.add(lfChartPanel, BorderLayout.CENTER);
		                    tester.pack();
		                    tester.setVisible(true);
		                }
		            });
		
	}
	
	
	
	/**
	 * Inner class representing a single source in the AllSwathcal.dat data file.
	 *
	 *
	 * @author nrowell
	 * @version $Id$
	 */
	static class Source {
		
		// Raw data
		int sourceId, fieldId;
		double x, y, f390w, f390w_err, f606w, f606w_err, f110w, f110w_err, f160w, f160w_err, chi2, sharp;
		
		public Source(int sourceId, double x, double y, double f390w, double f390w_err, 
				double f606w, double f606w_err, double f110w, double f110w_err, 
				double f160w, double f160w_err, double chi2, double sharp, int fieldId) {
			this.sourceId = sourceId;
			this.x = x;
			this.y = y;
			this.f390w = f390w;
			this.f390w_err = f390w_err;
			this.f606w = f606w;
			this.f606w_err = f606w_err;
			this.f110w = f110w;
			this.f110w_err = f110w_err;
			this.f160w = f160w;
			this.f160w_err = f160w_err;
			this.chi2 = chi2;
			this.sharp = sharp;
			this.fieldId = fieldId;
			
		}
		
		/**
		 * Determine if the {@link Source} is detected in all of the wave bands.
		 * @return
		 * 	True if the {@link Source} is detected in all of the wave bands, false otherwise.
		 */
		public boolean isDetectedInAllBands() {
			return !(this.f390w > 100.0 || this.f606w > 100.0 || this.f110w > 100.0 || this.f160w < 100.0);
		}
		
		/**
		 * Get the magnitude in the given {@link Filter}
		 * @param filter
		 * 	The {@link Filter}
		 * @return
		 * 	The magnitude in the given {@link Filter}
		 */
		public double getMag(Filter filter) {
			switch(filter) {
				case F110W_WFC3_IR:
					return this.f110w;
				case F160W_WFC3_IR:
					return this.f160w;
				case F390W_WFC3_UVIS:
					return this.f390w;
				case F606W_WFC3_UVIS:
					return this.f606w;
				default:
					throw new RuntimeException("Unrecognized Filter: "+filter);
			}
		}
		
		/**
		 * Get the magnitude uncertainty in the given {@link Filter}
		 * @param filter
		 * 	The {@link Filter}
		 * @return
		 * 	The magnitude uncertainty in the given {@link Filter}
		 */
		public double getMagError(Filter filter) {
			switch(filter) {
				case F110W_WFC3_IR:
					return this.f110w_err;
				case F160W_WFC3_IR:
					return this.f160w_err;
				case F390W_WFC3_UVIS:
					return this.f390w_err;
				case F606W_WFC3_UVIS:
					return this.f606w_err;
				default:
					throw new RuntimeException("Unrecognized Filter: "+filter);
			}
		}
		
		/**
		 * Parse a {@link Source} from a String.
		 * 
		 * @param sourceStr
		 * 	String containing the fields for a Source.
		 * @return
		 * 	The parsed Source, or null if a Source could not be parsed from the String.
		 */
		static Source parseSource(String sourceStr) {
			
			try(Scanner scan = new Scanner(sourceStr)) {
				int id = scan.nextInt();
				double x = scan.nextDouble();
				double y = scan.nextDouble();
				double f390w = scan.nextDouble();
				double f390w_err = scan.nextDouble();
				double f606w = scan.nextDouble();
				double f606w_err = scan.nextDouble();
				double f110w = scan.nextDouble();
				double f110w_err = scan.nextDouble();
				double f160w = scan.nextDouble();
				double f160w_err = scan.nextDouble();
				double chi2 = scan.nextDouble();
				double sharp = scan.nextDouble();
				int fieldId = scan.nextInt();
				
				return new Source(id, x, y, f390w, f390w_err, f606w, f606w_err, f110w, f110w_err, f160w, f160w_err, chi2, sharp, fieldId);
			}
			catch(NoSuchElementException | IllegalStateException e) {
				System.out.println("");
				return null;
			}
		}
		
	}

	/**
	 * Main application entry point
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		
		java.awt.EventQueue.invokeLater(
	        new Runnable() 
	            {
	                @Override
	                public void run() 
	                {
	                    JFrame tester = new JFrame();
	                    tester.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	                    tester.setLayout(new BorderLayout());
	                    tester.add(new ProcessAllSwathcal(), BorderLayout.CENTER);
	                    tester.pack();
	                    tester.setVisible(true);
	                }
	            });
	}
}
