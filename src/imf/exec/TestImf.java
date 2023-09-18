package imf.exec;

import java.io.IOException;

import imf.algo.BaseImf;
import imf.algoimpl.Chabrier03;
import infra.io.Gnuplot;
import infra.os.OSChecker;
import numeric.data.Histogram;
import numeric.integration.IntegrableFunction;
import numeric.integration.IntegrationUtils;
import util.CharUtil;

/**
 * This class provides an application used to test {@link BaseImf} implementations during development.
 * 
 * @author nrowell
 */
public class TestImf {
	
	
	/**
	 * Application main entry point.
	 * 
	 * @param args
	 * 	The command line arguments (ignored).
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		BaseImf imf = new Chabrier03();
		
		IntegrableFunction intFunc = (x) -> {return imf.getIMF(x);};
		
		// Numerical integration
		double h = 0.00001;
		for(double mass = BaseImf.M_lower; mass < BaseImf.M_upper; mass += 0.01) {
			
			double numericInt = IntegrationUtils.integrate(intFunc, BaseImf.M_lower, mass, h);
			double analyticInt = imf.getIntegral(mass);
			
			double diff = Math.abs(numericInt - analyticInt);
			
			// Check that difference is within step size
			if(diff / h > 0.5) {
				System.out.println("\nIntegral over [" + BaseImf.M_lower + ":" + mass + "]");
				System.out.println("Numerical integration = " + numericInt);
				System.out.println("Analytic integration  = " + analyticInt);
				System.out.println("Abs. diff / step h    = " + (diff / h));
			}
		}
		
		Histogram hist = new Histogram(0.0, 20.0, 0.01, false);
		
		for(int n=0; n<1000000; n++) {
			double mass = imf.drawMass();
			hist.add(mass);
		}
		
		hist = hist.getNormalised();
		
		String script = 
				"set terminal pngcairo enhanced color size 640,480" + OSChecker.newline + 
				"set xrange [0.5:8]" + OSChecker.newline +
				"set yrange [*:*]" + OSChecker.newline +
				"set key top right" + OSChecker.newline +
				"set xtics out" + OSChecker.newline +
				"set ytics out" + OSChecker.newline +
				"set logscale x" + OSChecker.newline +
				"set logscale y" + OSChecker.newline +
				"set xlabel 'Stellar mass [M_"+CharUtil.solar+"]'" + OSChecker.newline +
				"set ylabel 'Density [N M_"+CharUtil.solar+"]^{-1}'" + OSChecker.newline +
				"plot '-' w l lw 2 t 'FUNC', '-' w l notitle";
		script += OSChecker.newline;
		
		for(double mass = BaseImf.M_lower; mass <= BaseImf.M_upper; mass += 0.01) {
			script += mass + "\t" + imf.getIMF(mass) + "\n";
		}

		script += "e" + OSChecker.newline;
		
		// Lower left edge of first bin
		script += hist.getBinLowerEdge(0) + "\t0.0\n";
		
		for(int i=0; i<hist.getNumberOfBins(); i++) {
			
			double bin = hist.getBinContents(i);
			
			// Upper left edge of bin
			script += hist.getBinLowerEdge(i) + "\t" + bin + "\n";

			// Upper right edge of bin
			script += hist.getBinUpperEdge(i) + "\t" + bin + "\n";
		}

		// Lower right edge of final bin
		script += hist.getBinUpperEdge(hist.getNumberOfBins()-1) + "\t0.0\n";
		
		script += "e" + OSChecker.newline;
		
		Gnuplot.displayImage(Gnuplot.executeScript(script));
		
	}
}
