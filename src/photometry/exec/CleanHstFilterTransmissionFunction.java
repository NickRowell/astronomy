package photometry.exec;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Scanner;
import java.util.TreeMap;

/**
 * This class is used to clean a table of filter transmission values in order to remove duplicated
 * consecutive wavelength points in the first column. This turned out to be a significant issue with
 * HST filter transmission files.
 *
 * @author nrowell
 * @version $Id$
 */
public class CleanHstFilterTransmissionFunction {

	
	/**
	 * Main entry point for the {@link CleanHstFilterTransmissionFunction} application.
	 * @param args
	 * @throws URISyntaxException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws URISyntaxException, IOException {
		
		new CleanHstFilterTransmissionFunction("hst/wfc_F814W.dat", "hst/wfc_F814W_cleaned.dat");
	}
	
	/**
	 * 
	 * @param input
	 * @param output
	 * @throws URISyntaxException 
	 * @throws IOException 
	 */
	CleanHstFilterTransmissionFunction(String input, String output) throws URISyntaxException, IOException {
		
		InputStream is = this.getClass().getClassLoader().getResourceAsStream("resources/filters/"+input);
		
        // Read in data from file
        String line;
        
        // Store values parsed from each line. Use a TreeMap to map the wavelength to a list of
        // transmission values for that wavelength, representing the possibly duplicated values
        // for the given wavelength. This means the file does not need to be in order.
        NavigableMap<Double, List<Double>> records = new TreeMap<>();
        
        List<Double> duplicatedRecords = new LinkedList<>();
        
        try(BufferedReader in = new BufferedReader(new InputStreamReader(is)))
        {
	        while((line=in.readLine())!=null)
	        {
	            // Skip commented lines
	            if(line.substring(0, 1).equals("#"))
	                continue;
	            // Open Scanner on line
	            Scanner scan = new Scanner(line);
	            // Read wavelength (Angstroms)
	            double lambda = scan.nextDouble();
	            // Read transmission
	            double throughput = scan.nextDouble();
	            
	            // If we don't already contain this wavelength, initialise the map entry
	            if(!records.containsKey(lambda)) {
	            	records.put(lambda, new LinkedList<Double>());
	            }
	            else {
	            	// List already exists - this wavelength has duplicated entries
	            	duplicatedRecords.add(lambda);
	            }
	            // Add to the map
	            records.get(lambda).add(throughput);
	            
	            scan.close();
	        }
        }
        catch(IOException e )
        {
        	System.out.println("Unable to read filter transmission function!");
        }
        
        // Now we navigate over the map and write out the values for each wavelength, taking the
        // average of any duplicated values.
        File file = new File("src/resources/filters/"+output);
        BufferedWriter os = new BufferedWriter(new FileWriter(file));
		
        os.write("# Cleaned filter transmission function. The following wavelengths had duplicated\n");
        os.write("# transmission values that have been averaged:\n");
        for(Double lambda : duplicatedRecords) {
        	os.write(String.format("# %.3f \n",lambda));
        }
        
        for(Entry<Double, List<Double>> entry : records.entrySet()) {
        	
        	double lambda = entry.getKey();
        	List<Double> transmissionRecords = entry.getValue();
        	
        	double meanTransmission = 0;
        	for(Double transmissionRecord : transmissionRecords) {
        		meanTransmission += transmissionRecord;
        	}
        	meanTransmission /= transmissionRecords.size();
        	
        	os.write(lambda + "\t" + meanTransmission + "\n");
        }
        
        os.close();
	}
}
