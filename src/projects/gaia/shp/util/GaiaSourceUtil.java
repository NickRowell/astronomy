
package projects.gaia.shp.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import projects.gaia.shp.dm.GaiaSource;
import projects.gaia.shp.dm.GaiaSource_2020_2021;



/**
 * Utilities related to the Gaia DR2 source table.
 *
 * @author nrowell
 * @version $Id$
 */
public class GaiaSourceUtil {
	
	/**
	 * The logger.
	 */
	private static final Logger logger = Logger.getLogger(GaiaSourceUtil.class.getName());
	
	/**
	 * Loads {@link GaiaSource}s from the CSV file at the given location.
	 * 
	 * @param csv
	 * 	The CSV data {@link File}.
	 * @return
	 * 	List of {@link GaiaSource}s read from the data file.
	 */
	public static Collection<GaiaSource> loadGaiaSources(File csv) {
		
		// Read the file and load all the contents to a list of GaiaSource
		List<GaiaSource> gaiaSources = new LinkedList<>();
		
		int missing = 0;
		
		// Read file contents into the List
		try (BufferedReader in = new BufferedReader(new FileReader(csv))) {
			// Trim off the header CSV line
			String starStr = in.readLine();
			while((starStr=in.readLine())!=null) {
				
				// Avoid blank lines
	        	if (starStr.length()==0) {
	                continue;
	        	}
	        	
	            // Avoid any commented out lines
	            if (starStr.substring(0, 1).equals("#")) {
	                continue;
	            }
	            
				GaiaSource source = GaiaSource.parseGaiaSource(starStr);
				if(source != null) {
					gaiaSources.add(source);
				}
				else {
					missing++;
				}
			}
		}
		catch(IOException e) {
			logger.log(Level.SEVERE, "Could not load the "+GaiaSource.class.getSimpleName()+"s!");
		}
		
		logger.log(Level.INFO, "Loaded " + gaiaSources.size() + " " + GaiaSource.class.getSimpleName() + "s.");
		logger.log(Level.INFO, "Failed to load " + missing + " " + GaiaSource.class.getSimpleName() + "s.");
		
		return gaiaSources;
	}
	
	/**
	 * Loads {@link GaiaSource_2020_2021}s from the CSV file at the given location.
	 * 
	 * @param csv
	 * 	The CSV data {@link File}.
	 * @return
	 * 	List of {@link GaiaSource_2020_2021}s read from the data file.
	 */
	public static Collection<GaiaSource_2020_2021> loadGaiaSources_2020_2021(File csv) {
		
		// Read the file and load all the contents to a list of GaiaSource
		List<GaiaSource_2020_2021> gaiaSources = new LinkedList<>();
		
		int missing = 0;
		int logInterval = 1000;
		
		// Read file contents into the List
		try (BufferedReader in = new BufferedReader(new FileReader(csv))) {
			// Trim off the header CSV line
			String starStr = in.readLine();
			while((starStr=in.readLine())!=null) {
				
				if(gaiaSources.size()%logInterval==0) {
					logger.info("Loaded " + gaiaSources.size() + " GaiaSource_2020_2021");
				}
				
				// Avoid blank lines
	        	if (starStr.length()==0) {
	                continue;
	        	}
	        	
	            // Avoid any commented out lines
	            if (starStr.substring(0, 1).equals("#")) {
	                continue;
	            }
	            
				GaiaSource_2020_2021 source = GaiaSource_2020_2021.parseGaiaSource(starStr);
				if(source != null) {
					gaiaSources.add(source);
				}
				else {
					missing++;
				}
			}
		}
		catch(IOException e) {
			logger.log(Level.SEVERE, "Could not load the "+GaiaSource_2020_2021.class.getSimpleName()+"s!");
		}
		
		logger.log(Level.INFO, "Loaded " + gaiaSources.size() + " " + GaiaSource_2020_2021.class.getSimpleName() + "s.");
		logger.log(Level.INFO, "Failed to load " + missing + " " + GaiaSource_2020_2021.class.getSimpleName() + "s.");
		
		return gaiaSources;
	}
	
	
	/**
	 * Loads {@link GaiaSource_2020_2021}s from the CSV file at the given location.
	 * 
	 * @param csv
	 * 	The CSV data {@link File}.
	 * @return
	 * 	List of {@link GaiaSource_2020_2021}s read from the data file.
	 */
	public static Collection<GaiaSource_2020_2021> loadGaiaSources_2020_2021_MultiThread(File csv) {
		
		// Read the file into a list of strings, then parse the content in multiple threads
		List<String> lines = new LinkedList<>();
		
		// Read file contents into the List
		try (BufferedReader in = new BufferedReader(new FileReader(csv))) {
			// Trim off the header CSV line
			String starStr = in.readLine();
			while((starStr=in.readLine())!=null) {
				
				// Avoid blank lines
	        	if (starStr.length()==0) {
	                continue;
	        	}
	        	
	            // Avoid any commented out lines
	            if (starStr.substring(0, 1).equals("#")) {
	                continue;
	            }
	            
	            lines.add(starStr);
			}
		}
		catch(IOException e) {
			logger.log(Level.SEVERE, "Could not load the "+GaiaSource_2020_2021.class.getSimpleName()+"s!");
		}
		
		logger.log(Level.INFO, "Read " + lines.size() + " lines from data file");
		
		// Read the file and load all the contents to a list of GaiaSource
		List<GaiaSource_2020_2021> gaiaSources = new LinkedList<>();
		
		List<GaiaSource_2020_2021> gaiaSourcesSynced = Collections.synchronizedList(gaiaSources);
		
        final List<Future<Void>> futures = new LinkedList<Future<Void>>();
        final ExecutorService executor = Executors.newFixedThreadPool(7);
        
        AtomicInteger missing = new AtomicInteger(0);
		
        for(String line : lines) {

			final Callable<Void> worker = new Callable<Void>() {
				
                @Override
                public Void call() throws Exception {
                	
    				GaiaSource_2020_2021 source = GaiaSource_2020_2021.parseGaiaSource(line);
    				if(source != null) {
    					gaiaSourcesSynced.add(source);
    				}
    				else {
    					missing.incrementAndGet();
    				}
    				
                	return null;
                }
			};
			futures.add(executor.submit(worker));
        	
        }
        
 		// Shutdown the execution
		executor.shutdown();
		
		int loadedStars = 0;
		int logInterval = 10000;
		
		// Retrieve results
		for (final Future<Void> future : futures) {
			try {
				future.get();
				loadedStars++;

				if(loadedStars%logInterval==0) {
					logger.info("Loaded " + loadedStars + " GaiaSource_2020_2021");
				}
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
		
		logger.log(Level.INFO, "Loaded " + gaiaSources.size() + " " + GaiaSource_2020_2021.class.getSimpleName() + "s.");
		logger.log(Level.INFO, "Failed to load " + missing.get() + " " + GaiaSource_2020_2021.class.getSimpleName() + "s.");
		
		return gaiaSources;
	}
}
