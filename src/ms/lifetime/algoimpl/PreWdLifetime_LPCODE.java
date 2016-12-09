package ms.lifetime.algoimpl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Logger;

import ifmr.algoimpl.Ifmr_Renedo2010_Z0p01;
import ms.lifetime.algo.PreWdLifetime;
import numeric.functions.Linear;
import numeric.functions.MonotonicFunction1D;
import numeric.functions.MonotonicLinear;
import numeric.minimisation.nllsq.algoimpl.LevenbergMarquardtExponentialFitter;
import util.ParseUtil;

/**
 * Class provides an implementation of the {@link PreWdLifetime} that encapsulates the pre-WD lifetimes as
 * a function of mass published in Renedo et al (2010). Only a single metallicity is supported, and the lifetimes
 * themselves are not that accurate due to the use of an inappropriate equation of state for the WD progenitors.
 * This implementation is ONLY provided in order to subtract off the pre-WD lifetime from the total stellar age
 * in the Renedo et al. WD cooling tracks, to obtain the WD cooling time.
 * 
 * @author nrowell
 * @version $Id$
 */
public class PreWdLifetime_LPCODE extends PreWdLifetime {

	/**
	 * The logger.
	 */
	private static final Logger logger = Logger.getLogger(PreWdLifetime_LPCODE.class.getName());

	/**
	 * This boolean flag controls whether we use an exponential model to smoothly fit the main sequence
	 * lifetimes as a function of mass (with the drawback that it doesn't pass directly through the data
	 * points), or whether we use a linear intepolation instead (which has the benefit of passing directly
	 * through the data points).
	 * 
	 * Testing shows that the exponential fit is not suitable within the WD inversion context, presumably because the
	 * error in the cooling age (introduced by the fact the smoothed function does not pass precisely through
	 * the data points) is significant for most masses.
	 */
	private static final boolean useExponentialFit = false;
	
	/**
	 * The relative path to the top level directory containing the LPCODE 'colours' files
	 */
	private static final String colourLoc = "resources/wd/cooling/LPCODE/da/z0p01/colours/";
	
	/**
	 * The relative path to the top level directory containing the LPCODE 'tracks' files
	 */
	private static final String tracksLoc = "resources/wd/cooling/LPCODE/da/z0p01/tracks/";

	// Each model of a certain mass has two corresponding data files with names in the format:
	//
	// 1) cox_052490.dat
	//     - The 'colours' files
	//
	// 2) wd0524_z001.trk
	//     - The 'tracks' files
	//
	// The following String arrays record the file names in order of increasing mass. Note that the
	// 'tracks' files include a high mass model that does not have a corresponding 'colours' model.
	
	private static final String[] colours = new String[]{"cox_052490.dat", "cox_057015.dat", "cox_059316.dat", "cox_060959.dat", 
			"cox_063229.dat", "cox_065988.dat", "cox_070511.dat", "cox_076703.dat", "cox_083731.dat", "cox_087790.dat"};
	
	private static final String[] tracks = new String[]{"wd0524_z001.trk", "wd0570_z001.trk", "wd0593_z001.trk", "wd0609_z001.trk",
			"wd0632_z001.trk", "wd0659_z001.trk", "wd0705_z001.trk", "wd0767_z001.trk", "wd0837_z001.trk", "wd0877_z001.trk", "wd0934_z001.trk"};
	
	/**
	 * The metallicity (Z).
	 */
	private static double z = 0.001;
	
	/**
	 * The Helium content (Y).
	 * From section 2.3 of the paper: "The initial He content of our
	 * starting models at the main sequence was provided by the relation
	 * Y = 0.23 + 2.41Z, as given by present determinations of
	 * the chemical evolution of the Galaxy (Flynn 2004; Casagrande et al. 2007)"
	 */
	private static double y = 0.232;
	
	/**
	 * The name of these models.
	 */
	private static final String name = "Renedo et al. (2010)";
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getName() {
		return name;
	}
	
	/**
	 * {@inheritDoc}}
	 */
	@Override
	protected void load() {
		
		// Get an instance of the Renedo et al (2010) IFMR, to obtain MS masses
		Ifmr_Renedo2010_Z0p01 ifmr = new Ifmr_Renedo2010_Z0p01();
		
		// List of main sequence mass points
		List<Double> masses = new LinkedList<>();
		// List of main sequence lifetime points
		List<Double> lifetimes = new LinkedList<>();
		
		List<String> comments = new LinkedList<>();
		comments.add("#");
		comments.add("Teff");
		
		// Loop over each distinct mass in turn and read the files
		for(int f=0; f<colours.length; f++) {
			
			// Open readers on each file
			InputStream colourIs = this.getClass().getClassLoader().getResourceAsStream(colourLoc + colours[f]);
			InputStream tracksIs = this.getClass().getClassLoader().getResourceAsStream(tracksLoc + tracks[f]);
	        
			BufferedReader colourIn = new BufferedReader(new InputStreamReader(colourIs));
			BufferedReader tracksIn = new BufferedReader(new InputStreamReader(tracksIs));
	        
			double[][] colourData = null, tracksData = null;
			
			try {
				colourData = ParseUtil.parseFile(colourIn, ParseUtil.whitespaceDelim, comments);
				tracksData = ParseUtil.parseFile(tracksIn, ParseUtil.whitespaceDelim, comments);
			}
			catch (IOException e) {
				logger.warning("Could not load the Renedo et al. (2010) model files!");
				return;
			}

			// Look up the mass for this pair of models
			double wdMass = tracksData[5][0];
			// Get the corresponding MS mass
			double msMass = ifmr.getMi(wdMass);
			
			// The elements of the colourData array are as follows:
			//
			// [0][i] 	- Effective temperature [k] (decreasing)
			// [1][i] 	- Surface gravity [Log(g)] (increasing)
			// [2][i] 	- Bolometric luminosity [Log(L/L_{Solar})] (decreasing)
			// [3][i] 	- Total stellar age [Gyr] (increasing)
			// [4][i] 	- Helium abundance [Y] (constant)
			// [5][i]	- Magnitudes in HST filters [F220W, F250W, F330W, F344N, F435W, F475W, F502N, 
			//   |                F550M, F555W, F606W, F625W, F658N, F660N, F775W, F814W, F850LP, F892N]
			// [21][i]
			// [22][i] 	- Bolometric correction [V-band]
			// [23][i]	- Magnitudes in standard filters [U, B, V, R, I, J, H, K, L]
			//   |
			// [31][i]
			
			// The elements of the tracksData array are as follows:
			//
			// [0][i]	- Bolometric luminosity [Log(L/L_{Solar})] (decreasing)
			// [1][i]	- Effective temperature [Log(T_{eff})]
			// [2][i]	- The logarithm of the central temperature (K)
			// [3][i]	- The logarithm of the central density (gr/cm3)
			// [4][i]	- WD cooling time (time from peak effective temperature) [Myr]
			// [5][i]	- Mass (fixed)
			// [6][i]	- The logarithm of luminosity (in solar unit) due to proton-proton burning
			// [7][i]	- The logarithm of luminosity (in solar unit) due to CNO burning
			// [8][i]	- The logarithm of luminosity (in solar unit) due to helium burning
			// [9][i]	- The logarithm of luminosity (in solar unit) due to neutrino losses
			// [10][i]	- The logarithm of hydrogen content in solar mass
			// [11][i]	- The logarithm of surface gravity (cm/s2)
			// [12][i]	- Stellar radius in solar unit
			
			// So, to compute the MS lifetime for a particular mass, we get the total stellar age from
			// the coloursData, and the corresponding WD cooling time to the same temperature from the 
			// tracksData, and subtract the WD cooling time from the total stellar age.
			
			// Colours data: interpolate the total stellar age as a function of WD effective temperature
			// Units:
			//  - X: effective temperature in K
			//  - Y: total stellar age in Gyr
			double[] coloursTeffK = new double[colourData.length];
			double[] totalStellarAgeGyr  = new double[colourData.length];
			// Read backwards through array in order to get increasing effective temperature
			for(int i=0; i<colourData.length; i++) {
				coloursTeffK[i] = colourData[0][(colourData.length-1)-i];
				totalStellarAgeGyr[i] = colourData[3][(colourData.length-1)-i];
			}
//			MonotonicLinear totalStellarAgeByTeff = new MonotonicLinear(coloursTeffK, totalStellarAgeGyr);
			
			// Tracks data: interpolate the effective temperature as a function of WD cooling time
			// Units:
			//  - X: WD cooling time in Myr
			//  - Y: effective temperature in log(K)
			Linear tEffByWdCoolingTime = new Linear(tracksData[4], tracksData[1]);
			
			// First method:
			// 1) Use tracks function to get the effective temperature at zero WD cooling time
			// 2) Put this into the colours function to get the total stellar age at this point
			// Drawback is that this may require the colours function to be extrapolated, resulting
			// in total stellar age that is slightly incorrect.
			
			// Interpolate effeective temperature at zero WD cooling time
//			double tEffZeroWdCoolingTime = tEffByWdCoolingTime.interpolateY(0.0);
//			// Convert to linear units
//			tEffZeroWdCoolingTime = Math.pow(10, tEffZeroWdCoolingTime);
//			// Interpolate the total stellar age
//			double totalStellarAge = totalStellarAgeByTeff.interpolateY(tEffZeroWdCoolingTime);
//			// Convert to years
//			totalStellarAge *= 1e9;
			
			// Second method:
			// 1) Interpolate the WD cooling time at the first (highest T_{eff}) point in the colours data
			// 2) Subtract this from the corresponding total stellar age to get the pre-WD lifetime
			//
			// This method provides values that agree with the values Leandro Althaus quoted in an email
			// on 03/12/15, so is superior to the first method.
			
			double tEffMaxColours = Math.log10(coloursTeffK[coloursTeffK.length - 1]);
			
			// Get the corresponding WD cooling time in years. The function is not monotonic so we may end up with
			// multiple values; by inspection, there are two values in this case and we want the largest
			double[] wdCoolingTimes = tEffByWdCoolingTime.interpolateX(tEffMaxColours)[0];
			double wdCoolingTime = Double.NaN;
			if(wdCoolingTimes.length == 1) {
				wdCoolingTime = wdCoolingTimes[0];
			}
			else if(wdCoolingTimes.length == 2) {
				wdCoolingTime = Math.max(wdCoolingTimes[0], wdCoolingTimes[1]);
			}
			else {
				throw new RuntimeException("Found "+wdCoolingTimes.length+" values for WD cooling time!");
			}
			wdCoolingTime *= 1e6;
			
			// Subtract from the total stellar age at the corresponding point
			double totalStellarAge = totalStellarAgeGyr[coloursTeffK.length - 1]*1e9 - wdCoolingTime;
			
			logger.finer("For MS/WD mass "+wdMass+" / "+msMass+", found pre-WD lifetime of: "+totalStellarAge);
			
			masses.add(msMass);
			lifetimes.add(totalStellarAge);
		}
		
		double[] mass = new double[masses.size()];
		double[] lifetime = new double[masses.size()];
		
		for(int i=0; i<masses.size(); i++) {
			mass[i] = masses.get(i);
			lifetime[i] = lifetimes.get(i);
		}
		
		MonotonicFunction1D fun = null;
		
		if(useExponentialFit) {
			// Fit an exponential function of the form f(x) = A * x^{B} to these points.
			LevenbergMarquardtExponentialFitter fitter = new LevenbergMarquardtExponentialFitter(mass, lifetime);
			// Give fitter some reasonable starting values
			fitter.A = 1e10;
			fitter.B = -2.5;
			// Perform the fit
			fitter.invoke();
			
			// Retrieve the fitted values
			final double A = fitter.A;
			final double B = fitter.B;
			
			// Implement a MonotonicFunction1D that encapsulates this
			fun = new MonotonicFunction1D() {
				@Override
				public double[] getY(double x) {
					// Get the pre-WD lifetime as a function of the main sequence mass
					return new double[]{A * Math.pow(x, B), B * A * Math.pow(x, B-1)};
				}
				@Override
				public double[] getX(double y) {
					// Get main sequence stellar mass as a function of pre-WD lifetime
					return new double[]{Math.pow(y/A, 1/B), (1/B) * Math.pow(1/A, 1/B) * Math.pow(y, (1/B) - 1)};
				}
			};
		}
		else {
			fun = new MonotonicLinear(mass, lifetime);
		}
		
		

    	// Initialise the map if this is the first time we've encountered a model with this Z
    	if(!lifetimeAsFnMassByMetallicity.containsKey(z)) {
    		lifetimeAsFnMassByMetallicity.put(z, new TreeMap<Double, MonotonicFunction1D>());
    	}
    	
    	// Install in the map
    	lifetimeAsFnMassByMetallicity.get(z).put(y, fun);
	}
}