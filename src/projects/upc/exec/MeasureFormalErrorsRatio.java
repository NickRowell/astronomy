package projects.upc.exec;

import java.util.List;

import projects.upc.dm.UpcStar;
import projects.upc.util.UpcUtils;

public class MeasureFormalErrorsRatio {
	
	
	
	
	public static void main(String[] args) {
		
		List<UpcStar> upcStars = UpcUtils.loadUpcCatalogue();
		List<UpcStar> hipStars = UpcUtils.getHipparcosSubset(upcStars);
		
		
		double sumHip = 0.0;
		double sumUpc = 0.0;
		double sumRatio = 0.0;
		
		for(UpcStar hipStar : hipStars) {
			
			sumUpc += hipStar.absPiErr;
			sumHip += hipStar.srcPiErr;
			
			sumRatio += hipStar.absPiErr / hipStar.srcPiErr;
		}
		
		System.out.println("Mean UPC error = "+(sumUpc/hipStars.size()));
		System.out.println("Mean Hip error = "+(sumHip/hipStars.size()));
		System.out.println("Mean err ratio = "+(sumRatio/hipStars.size()));
		
		
		
	}
	
	
	
	
}
