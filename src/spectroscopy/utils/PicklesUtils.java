package spectroscopy.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;

import numeric.functions.Linear;
import spectroscopy.LuminosityClass;
import spectroscopy.SpectralType;
import util.ParseUtil;

/**
 * Utilities related to the Pickles library of stellar spectra.
 *
 *
 * @author nrowell
 * @version $Id$
 */
public class PicklesUtils {
	
	/**
	 * The logger.
	 */
	protected static final Logger logger = Logger.getLogger(PicklesUtils.class.getName());
	
	/**
	 * Path to the directory containing the data files.
	 */
	public static final String picklesLibPath = "src/resources/spectro/pickles/";
	
	/**
	 * Enum type to represent the metallicities present among the contents of
	 * the Pickles library.
	 */
	public static enum PicklesMetallicity {RICH, WEAK, NORMAL};
	
	/**
	 * Class encapsulates the metadata fields for a single spectrum in the Pickles library.
	 */
	public static class PicklesMetadata implements Comparable<PicklesMetadata> {
		
		/**
		 * The {@link SpectralType}.
		 */
		public final SpectralType spectralType;

		/**
		 * The {@link LuminosityClass}.
		 */
		public final LuminosityClass luminosityClass;

		/**
		 * The {@link PicklesMetallicity}.
		 */
		public final PicklesMetallicity picklesMetallicity;
		
		/**
		 * Main constructor for the {@link PicklesMetadata}.
		 * @param spectralType
		 * 	The {@link SpectralType}.
		 * @param luminosityClass
		 * 	The {@link LuminosityClass}.
		 * @param picklesMetallicity
		 * 	The {@link PicklesMetallicity}.
		 */
		public PicklesMetadata(SpectralType spectralType, LuminosityClass luminosityClass, PicklesMetallicity picklesMetallicity) {
			this.spectralType = spectralType;
			this.luminosityClass = luminosityClass;
			this.picklesMetallicity = picklesMetallicity;
		}

		@Override
		public int compareTo(PicklesMetadata that) {
			
			// Compare spectral type
			if(this.spectralType == that.spectralType) {
				// Same spectral type: sort according to luminosity class
				if(this.luminosityClass == that.luminosityClass) {
					// Same spectral type and luminosity class: sort on metallicity
					return this.picklesMetallicity.ordinal() - that.picklesMetallicity.ordinal();
				}
				else {
					// Different luminosity classes
					return this.luminosityClass.ordinal() - that.luminosityClass.ordinal();
				}
				
			}
			else {
				// Different spectral types
				return this.spectralType.ordinal() - that.spectralType.ordinal();
			}
		}
	}
	
	/**
	 * Loads and returns all spectra in the Pickles library.
	 * @return
	 */
	public static Map<PicklesMetadata, Linear> loadPicklesSpectra() {
		
		// Initialise the top level Map
		Map<PicklesMetadata, Linear> spectra = new TreeMap<>();
		
		// Collection of all files found in the data directory
		@SuppressWarnings("unchecked")
		Collection<File> files = FileUtils.listFiles(new File(picklesLibPath), null, true);
		
		// Loop over each file
		for(File file : files) {
			
			// Parse the file name to determine what type of spectra it contains (if any)
			String name = file.getName();
			
			// Pickles library data file names have the format:
			//
			// uk{r|w}<spectral type><luminosity class>.dat
			
			// Names that start 'uk' and end '.dat' are library files
			if(name.startsWith("uk") && name.endsWith(".dat")) {
				
				SpectralType spectralType;
				LuminosityClass lumClass;
				PicklesMetallicity metallicity;
				
				// Determine if spectrum is for metal rich, weak or normal star
				metallicity = parsePicklesMetallicity(name.substring(2, 3));
				
				// Determine SpectralType
				if(metallicity==PicklesMetallicity.NORMAL) {
					spectralType = parsePicklesSpectralType(name.substring(2, 4));
				}
				else {
					// Additional {r|w} in name bumps spectral type to later position
					spectralType = parsePicklesSpectralType(name.substring(3, 5));
				}
				
				if(spectralType==null) {
					logger.warning("Couldn't determine SpectralType for file "+name);
					continue;
				}
				
				// Determine LuminosityClass
				int fullStopIdx = name.indexOf(".");
				if(metallicity==PicklesMetallicity.NORMAL) {
					lumClass = parsePicklesLuminosityClass(name.substring(4, fullStopIdx));
				}
				else {
					// Additional {r|w} in name bumps luminosity class to later position
					lumClass = parsePicklesLuminosityClass(name.substring(5, fullStopIdx));
				}
				
				if(lumClass==null) {
					logger.warning("Couldn't determine LuminosityClass for file "+name);
					continue;
				}
				
				// Have determined all the metadata for the spectrum
				PicklesMetadata metaData = new PicklesMetadata(spectralType, lumClass, metallicity);
				
				// Load the spectrum from the file
				InputStream is = (new PicklesUtils()).getClass().getClassLoader().getResourceAsStream("resources/spectro/pickles/"+name);
				
				double[][] specData = null;
				
				try(BufferedReader in = new BufferedReader(new InputStreamReader(is))) {
					specData = ParseUtil.parseFile(in, ParseUtil.whitespaceDelim, ParseUtil.hashComment);
				}
				catch (IOException e) {
					logger.warning("Could not load "+name+"!");
					return null;
				}
				
				Linear linear = new Linear(specData[0], specData[1]);
				spectra.put(metaData, linear);
				
				logger.info("Successfully parsed Pickles library data file "+name);
			}
		}
		
		return spectra;
	}
	
	/**
	 * Parses a single character to determine if the corresponding Pickles library data file
	 * contains metal rich, weak or normal spectrum.
	 * @param metallicityString
	 * 	The single character encoding the metallicity - r (RICH), w (WEAK) and otherwise (NORMAL)
	 * @return
	 * 	The corresponding {@link PicklesMetallicity}.
	 */
	public static PicklesMetallicity parsePicklesMetallicity(String metallicityString) {
		switch(metallicityString) {
			case "r": return PicklesMetallicity.RICH;
			case "w": return PicklesMetallicity.WEAK;
			default: return PicklesMetallicity.NORMAL;
		}
	}
	
	/**
	 * Parses the two-character string that encodes the spectral type of a star in the Pickles
	 * library.
	 * @param spectralTypeString
	 * 	Two character String encoding the spectral type.
	 * @return
	 * 	The corresponding {@link SpectralType}, or null if it could not be determined.
	 */
	public static SpectralType parsePicklesSpectralType(String spectralTypeString) {
		
		try {
			return SpectralType.valueOf(spectralTypeString.toUpperCase());
		}
		catch(IllegalArgumentException e) {
			logger.warning("Couldn't parse SpectralType from "+spectralTypeString);
			return null;
		}
	}
	
	/**
	 * Parses the multi-character string that encodes the luminosity class of a star in the
	 * Pickles library.
	 * @param lumClassString
	 * 	The multi-character string that encodes the luminosity class.
	 * @return
	 * 	The corresponding {@link LuminosityClass}, or null if it could not be determined.
	 */
	public static LuminosityClass parsePicklesLuminosityClass(String lumClassString) {

		try {
			return LuminosityClass.valueOf(lumClassString.toUpperCase());
		}
		catch(IllegalArgumentException e) {
			logger.warning("Couldn't parse LuminosityClass from "+lumClassString);
			return null;
		}
	}
	
}