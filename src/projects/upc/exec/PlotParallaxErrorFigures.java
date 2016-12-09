package projects.upc.exec;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import infra.io.Gnuplot;
import numeric.integration.IntegrableFunction;
import numeric.integration.IntegrationUtils;

/**
 * This class generates a couple of plots that demonstrate the origin of negative parallaxes
 * by convolution of a Gaussian error distribution with the distribution of true parallaxes.
 *
 *
 * @author nrowell
 * @version $Id$
 */
public class PlotParallaxErrorFigures {
	
	/**
	 * The output location.
	 */
	private static File output = new File("/home/nrowell/workspace/Astronomy/src/projects/upc/plots");
	
	/**
	 * Main application entry point.
	 * @param args
	 * 	The command line args (ignored)
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		
		// Parameters of Gaussian parallax error distribution
		final double sigma_pi = 0.5;
		final double mean = 0.0;
		IntegrableFunction pNoise = new IntegrableFunction(){
			@Override
			public double evaluate(double x) {
				return (1.0/(Math.sqrt(2.0*Math.PI)*sigma_pi)) * Math.exp((-1.0/2.0)*((x - mean)/sigma_pi)*((x - mean)/sigma_pi));
			}
		};
		
		// Parameters of true parallax distribution
		final double c=1.0;
		final double n = -1.0;
		IntegrableFunction pPiTrue = new IntegrableFunction(){
			@Override
			public double evaluate(double x) {
				return (x<=0) ? 0.0 : c*Math.pow(x, n);
			}
		};
		
		StringBuilder gaussPlotScript = new StringBuilder();
		gaussPlotScript.append("set term pngcairo enhanced size 320,240\n");
		gaussPlotScript.append("set key off\n");
		gaussPlotScript.append("set xrange [-3:3]\n");
		gaussPlotScript.append("set xtics 1.0 out nomirror\n");
		gaussPlotScript.append("set ylabel ''\n");
		gaussPlotScript.append("unset ytics\n");
		gaussPlotScript.append("set xlabel 'P({/Symbol p}_{error})'\n");
		gaussPlotScript.append("set border 1\n");
		gaussPlotScript.append("plot '-' w l lw 2 lc rgbcolor 'black'\n");
		for(double x0=-3; x0<3.0; x0+=0.01) {
			gaussPlotScript.append(x0 + "\t" + pNoise.evaluate(x0)+"\n");
		}
		gaussPlotScript.append("e");
		
		BufferedImage plot = Gnuplot.executeScript(gaussPlotScript.toString());
		ImageIO.write(plot, "png", new File(output,"gaussian.png"));
		

		StringBuilder piTruePlotScript = new StringBuilder();
		piTruePlotScript.append("set term pngcairo enhanced size 320,240\n");
		piTruePlotScript.append("set key off\n");
		piTruePlotScript.append("set xrange [0:3]\n");
		piTruePlotScript.append("set yrange [0:3]\n");
		piTruePlotScript.append("set xtics 1.0 out nomirror\n");
		piTruePlotScript.append("set ylabel ''\n");
		piTruePlotScript.append("unset ytics\n");
		piTruePlotScript.append("set xlabel 'P({/Symbol p}_{true})'\n");
		piTruePlotScript.append("set border 1\n");
		piTruePlotScript.append("plot '-' w l lw 2 lc rgbcolor 'black'\n");
		
		for(double x0=-1; x0<3.0; x0+=0.01) {
			piTruePlotScript.append(x0 + "\t" + pPiTrue.evaluate(x0)+"\n");
		}
		piTruePlotScript.append("e");
		
		BufferedImage piTruePlot = Gnuplot.executeScript(piTruePlotScript.toString());
		ImageIO.write(piTruePlot, "png", new File(output,"parallax_true.png"));
		
		
		
		StringBuilder piObsPlotScript = new StringBuilder();
		piObsPlotScript.append("set term pngcairo enhanced size 320,240\n");
		piObsPlotScript.append("set key off\n");
		piObsPlotScript.append("set xrange [-2:4]\n");
		piObsPlotScript.append("set yrange [0:*]\n");
		piObsPlotScript.append("set xtics 1.0 out nomirror\n");
		piObsPlotScript.append("set ylabel ''\n");
		piObsPlotScript.append("unset ytics\n");
		piObsPlotScript.append("set xlabel 'P({/Symbol p}_{obs})'\n");
		piObsPlotScript.append("set border 1\n");
		piObsPlotScript.append("plot '-' w l lw 2 lc rgbcolor 'black'\n");
		// Compute the numerical interation of the true parallax distribution and noide distribution
		for(double x0=-2; x0<4.0; x0+=0.01) {
			double conv = IntegrationUtils.convolve(pPiTrue, pNoise, -10.0, 10.0,  x0, 0.01);
			piObsPlotScript.append(x0 + "\t" + conv+"\n");
		}
		piObsPlotScript.append("e");
		
		BufferedImage piObsPlot = Gnuplot.executeScript(piObsPlotScript.toString());
		ImageIO.write(piObsPlot, "png", new File(output,"parallax_obs.png"));
		
		
		
		
		
		
		
	}
	
	
	
	
	
}
