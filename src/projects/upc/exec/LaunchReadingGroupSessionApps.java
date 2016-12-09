package projects.upc.exec;

public class LaunchReadingGroupSessionApps {
	
	
	public static void main(String[] args) {
		
		// Introduction to survey
		SkyPlot.main(new String[]{});
		
		// Discussion of parallax error calibration
		PlotParallaxHistograms.main(new String[]{});
		ParallaxExternalErrorCalibrationLindegren.main(new String[]{});
		
		// Discussion of distance-from-parallax methods
		HrDiagram.main(new String[]{});
		
	}
	
}