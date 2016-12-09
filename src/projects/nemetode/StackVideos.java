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

package projects.nemetode;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

import infra.io.StreamGobbler;
import numeric.data.FloatList;

/**
 * 
 *
 *
 * @author nrowell
 * @version $Id$
 */
public class StackVideos {
	
	/**
	 * The Logger
	 */
    protected static Logger logger = Logger.getLogger(StackVideos.class.getCanonicalName());
    
	/**
	 * Main application entry point.
	 * @param args
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
		
		// Directory containing the video folders
		File videoDir = new File("/home/nrowell/Temp/videos");
		
		
		// Get all subdirectories (M20160812_225434_Gargunnock etc)
		String[] dirs = videoDir.list(new FilenameFilter() {
			  @Override
			  public boolean accept(File current, String name) {
			    return new File(current, name).isDirectory();
			  }
			});
		
		// Process each video: break it into single frames using avconv, then map the individual frames by frame number
		Map<Integer, List<BufferedImage>> framesMap = new HashMap<>();
		
		for(String dir : dirs) {
			
			File videoSubDir = new File(videoDir, dir);
			
			logger.info("Processing "+videoSubDir.getName());
			
			File avi = new File(videoSubDir, videoSubDir.getName()+"_NR.avi");
			
	        String[] command = new String[]{"avconv", "-i", avi.getAbsolutePath(), "-f",  "image2",  videoSubDir.getAbsolutePath()+"/frame%03d.png"};
	        
	        logger.info(String.format("Command: %s %s %s %s %s %s ", command));
	        
            final Process proc = Runtime.getRuntime().exec(command);
            StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), "AVCONV");
            errorGobbler.start();
            int exitCode = proc.waitFor();
            errorGobbler.join();
            
            // Now load all the images to the map
            for(File imFile : (Collection<File>)FileUtils.listFiles(videoSubDir, new String[]{"png"}, false)) {
            	// Read the frame number
            	String filename = imFile.getName();   // frame_XXX.png
            	int fNum = Integer.parseInt(filename.substring(5, 8));
            	BufferedImage frame = ImageIO.read(imFile);
            	if(!framesMap.containsKey(fNum)) {
            		framesMap.put(fNum, new LinkedList<BufferedImage>());
            	}
            	framesMap.get(fNum).add(frame);
            	imFile.delete();
            }
            
		}
		
		// Loop over the frame number
		for(Entry<Integer, List<BufferedImage>> entry : framesMap.entrySet()) {
			
			int fNum = entry.getKey();
			List<BufferedImage> frames = entry.getValue();
			
			// Generate single combined output image from all clips
			FloatList[] pixels = new FloatList[640*480];
			for(int p=0; p<pixels.length; p++) {
				pixels[p] = new FloatList();
			}
			
			// Loop over all the clips and produce a merged image of all of the individual frames
			for(BufferedImage frame : frames) {
				
				// Read all the pixels into the list
				for(int i=0; i<640; i++) {
					for(int j=0; j<480; j++) {
						
						// Index into flattened pixels array
						int idx = i*480 + j;
						
						// Extract the pixel value from the frame
						int pixel = frame.getRGB(i, j);
//						int a = (pixel >> 24) & 0xFF;
						// The RGB all contain the same value
						int r = (pixel >> 16) & 0xFF;
//						int g = (pixel >> 8) & 0xFF;
//						int b = pixel & 0xFF;
						
						pixels[idx].add((float)r);
					}
				}
			}
			
			BufferedImage composite = new BufferedImage(640, 480, BufferedImage.TYPE_INT_RGB);

	        // Draw integer array of pixel values into image as graylevels
	        for (int x = 0; x < 640; x++) {
	            for (int y = 0; y < 480; y++) {
	            	
	            	// Index into flattened pixels array
					int idx = x*480 + y;
	            	
					int gray = (int) pixels[idx].getMinMax()[1];
					int pixel = 0xFF000000 + (gray << 16) + (gray << 8) + gray;
					
	                composite.setRGB(x, y, pixel);
	            }
	        }
			
	        ImageIO.write(composite, "png", new File(videoDir, String.format("median_%03d.png", fNum)));
		}
		
		
		// AVCONV command to encode video:
		//  avconv -r 30 -i median_%03d.png -vcodec libx264 -crf 20 -vf transpose=1 movie.mp4
		
	}
	
}
