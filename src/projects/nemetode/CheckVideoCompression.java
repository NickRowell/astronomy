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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import infra.io.StreamGobbler;
import util.ArrayUtil;

/**
 * Class used to check if videos compressed with a certain algorithm are lossy or not.
 *
 * Conclusions:
 *  - It's difficult to find a video compression algorithm that is not lossless. The best I could find with
 *    avconv results in video that looks lossless but seems to be misaligned with the original.
 *  - It's best to always process the raw video in UFOAnalyser, and use compressed videos for online viewing
 *    without worrying whether they are lossless or not.
 *
 *
 * @author nrowell
 * @version $Id$
 */
public class CheckVideoCompression {
	
	/**
	 * The Logger
	 */
    protected static Logger logger = Logger.getLogger(CheckVideoCompression.class.getCanonicalName());
    
	/**
	 * Main application entry point.
	 * @param args
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
		
		// Input raw video
		File rawVideo = new File("/home/nrowell/Temp/temp/M20160511_005727_Gargunnock/M20160511_005727_Gargunnock_NR.avi");
		
		
		// Extract path to containing folder
		File path = rawVideo.getParentFile();
		
		// Get file name stripped of extension
		String name = rawVideo.getName().substring(0, rawVideo.getName().length()-4);
		
		// Create file to contain compressed video
		File compressedVideo = new File(path, name + "_comp.avi");
		
		// Execute the video compression command
		executeVideoCompression(rawVideo, compressedVideo);
		
        // Now split both the raw and compressed videos into single frames
		File rawFramesDir = new File(path, "raw");
		File compFramesDir = new File(path, "comp");
		extractFrames(rawVideo, rawFramesDir);
		extractFrames(compressedVideo, compFramesDir);
		
		// Read all frames into two arrays
		File[] rawFrames = rawFramesDir.listFiles(new FrameFilenameFilter());
		File[] compFrames = compFramesDir.listFiles(new FrameFilenameFilter());
		
		List<File> rawFramesList = ArrayUtil.toList(rawFrames);
		List<File> compFramesList = ArrayUtil.toList(compFrames);
		
		// Check we found the same number of frames
		if(rawFrames.length!=compFrames.length) {
			throw new RuntimeException("Different number of raw ("+rawFrames.length+") and "
					+ "compressed ("+compFrames.length+") video frames!");
		}
		
		// Sort arrays
		Collections.sort(rawFramesList, new FrameComparator());
		Collections.sort(compFramesList, new FrameComparator());
		
		// Check each pair of images. Draw difference image.
		File diffFramesDir = new File(path, "diff");
		diffFramesDir.mkdir();
		
		boolean diffDetected = false;
		for(int i=0; i<rawFrames.length; i++) {
			
			BufferedImage rawImage = ImageIO.read(rawFramesList.get(i));
			BufferedImage compImage = ImageIO.read(compFramesList.get(i));
			
			// Check each image is the same width & height
			if(rawImage.getHeight() != compImage.getHeight() || rawImage.getWidth() != compImage.getWidth()) {
				throw new RuntimeException("Raw ("+rawImage.getWidth()+"x"+rawImage.getHeight()+") & compressed "
						+ "("+compImage.getWidth()+"x"+compImage.getHeight()+") image dimensions are different!");
			}
			
			// Make a difference image
			BufferedImage diffImage = new BufferedImage(rawImage.getWidth(), rawImage.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
			
			// TODO: check that the number of bands matches
			Raster rawRaster = rawImage.getData();
			Raster compRaster = compImage.getData();
			
			// Loop over all pixels
			for(int j=0; j<rawImage.getWidth(); j++) {
				for(int k=0; k<rawImage.getHeight(); k++) {
					
					for(int b=0; b<rawRaster.getNumBands(); b++) {
						
						int rawPixel = rawRaster.getSample(j, k, b);
						int compPixel = compRaster.getSample(j, k, b);
						
						int diff = rawPixel - compPixel;
						
						if(rawPixel!=compPixel) {
							diffDetected = true;
						}
						
						diffImage.setRGB(j, k, (byte) (128 + diff));
//						System.out.println(b+"\t"+rawPixel+"\t"+compPixel);
						
					}
				}
			}
			
			ImageIO.write(diffImage, "png", new File(diffFramesDir, "frame_"+i+".png"));
					
		}
		
		if(diffDetected) {
			System.out.println("Difference detected between raw and compressed images!");
		}
		
		// Cleanup
//		compressedVideo.delete();
//		for(int i=0; i<rawFrames.length; i++) {
//			rawFrames[i].delete();
//			compFrames[i].delete();
//		}
//		rawFramesDir.delete();
//		compFramesDir.delete();
		
	}
	
	private static void executeVideoCompression(File raw, File compressed) throws IOException, InterruptedException {

		final Process proc = Runtime.getRuntime().exec(
				"avconv -i "+raw.getAbsolutePath()+" -vcodec libx264 -crf 0 -an "+compressed.getAbsolutePath());
        StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), "AVCONV");
        errorGobbler.start();
        proc.waitFor();
        errorGobbler.join();
	}
	
	private static void extractFrames(File video, File dir) throws IOException, InterruptedException {

		// Make directory to contain frame files
		if(!dir.exists()) {
			dir.mkdirs();
		}
		
		final Process proc = Runtime.getRuntime().exec(
				"avconv -i "+video.getAbsolutePath()+" -f image2 "+dir+"/frame_%00d.png");
        StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), "AVCONV");
        errorGobbler.start();
        proc.waitFor();
        errorGobbler.join();
		
	}
	
	private static class FrameComparator implements Comparator<File> {

		@Override
		public int compare(File o1, File o2) {
			
			// Extract the frame numbers from each file name
			int f1 = Integer.parseInt(o1.getName().substring("frame_".length(), o1.getName().length() - ".png".length()));
			int f2 = Integer.parseInt(o2.getName().substring("frame_".length(), o2.getName().length() - ".png".length()));
			
			if(f1==f2) {
				return 0;
			}
			return f1<f2 ? -1 : 1;
		}
	}
	
	
	private static class FrameFilenameFilter implements FilenameFilter {

		@Override
		public boolean accept(File dir, String name) {
			
			return name.startsWith("frame_") && name.endsWith(".png");
		}
		
	}
	
	
}
