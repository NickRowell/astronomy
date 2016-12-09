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
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import numeric.data.FloatList;

/**
 * Class intended to process a single meteor video clip to produce a nice composite image of the event.
 *
 *
 * @author nrowell
 * @version $Id$
 */
public class VideoPostProcess {
	
	/**
	 * The Logger
	 */
    protected static Logger logger = Logger.getLogger(VideoPostProcess.class.getCanonicalName());
    
	/**
	 * Main applicatio entry point.
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		// TODO: somehow get the video file and run avconv -i M20160309_223711_Gargunnock_NR.avi -f image2 Out%00d.png
		// on it to write extracted images to temporary files.
		
		// Top level directory containing extracted image frames
		File dir = new File("/home/nrowell/Projects/NEMETODE/videos/commissioning");
		
		// Get a list of all the individual image files
		File[] frameFiles = dir.listFiles(new FrameFilenameFilter());
		
		BufferedImage[] frames = new BufferedImage[frameFiles.length];
		
		for(int i=0; i<frameFiles.length; i++) {
			logger.info("Reading "+frameFiles[i].getName());
			frames[i] = ImageIO.read(frameFiles[i]);
			
			int width = frames[i].getWidth();
			int height = frames[i].getHeight();
		
			if(width!=640 || height !=480) {
				logger.warning("Image "+frameFiles[i].getName()+" has unexpected size: "+width+"x"+height);
			}
			
		}
		
		// Generate single comined output images - median and sum
		FloatList[] pixels = new FloatList[640*480];
		for(int p=0; p<pixels.length; p++) {
			pixels[p] = new FloatList();
		}
		
		for(BufferedImage frame : frames) {
			
			Raster raster = frame.getRaster();
			
			for(int i=0; i<640; i++) {
				for(int j=0; j<480; j++) {
					
					// Index into flattened pixels array
					int idx = i*480 + j;
					
					// Extract the pixel value from the frame
					int rgb = raster.getSample(i, j, 0);
					
					pixels[idx].add((float)rgb);
				}
			}
		}
		
		// Compose output image
		BufferedImage image = new BufferedImage(640, 480, BufferedImage.TYPE_BYTE_GRAY);

        // Draw integer array of pixel values into image as graylevels
        for (int x = 0; x < 640; x++) {
            for (int y = 0; y < 480; y++) {
            	
            	// Index into flattened pixels array
				int idx = x*480 + y;
            	
				// Might need to apply a colour stretch
                image.setRGB(x, y, (byte)pixels[idx].getSum()/100);
            }
        }
		
        ImageIO.write(image, "png", new File("/home/nrowell/Projects/NEMETODE/videos/commissioning/median.png"));
		
		
	}
	
	
	
	private static class FrameFilenameFilter implements FilenameFilter {

		@Override
		public boolean accept(File dir, String name) {
			
			return name.startsWith("Out") && name.endsWith(".png");
		}
		
	}
	
	
}
