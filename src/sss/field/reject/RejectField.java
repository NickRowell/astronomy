package sss.field.reject;

/**
 * This class lists fields rejected from surveys for various reasons.
 *
 * @author nickrowell
 */
public class RejectField {

    /**
     * Fields rejected from low and high PM surveys due to high level of contamination.
     * @param fieldNum
     * 	The field number
     * @param hemisphere
     * 	The hemisphere ["N"/"S"]
     * @return
     * 	True if the field is OK, false if is is to be rejected.
     */
    public static boolean contamination(int fieldNum, String hemisphere){
	if(hemisphere.equals("N")){
	    switch(fieldNum){
	    case 231: return false;
	    case 558: return false;
	    case 732: return false;
	    case 806: return false;
	    case 805: return false;
	    case 740: return false;
	    case 731: return false;
	    case 733: return false;
	    case 739: return false;
	    case 491: return false;
	    }
	}

	if(hemisphere.equals("S")){
	    switch(fieldNum){
                
            // These four were originally only excluded from extrapm sample, but I decided this made
            // the survey footprint too complicated. So by rejecting these fields from all surveys,
            // the sky footprint is now split into just two proper motion ranges, above and below
            // 0.18 arcseconds/yr
            case 53: return false;   // These threee may suffer from proximity to LMC
            case 58: return false;   //
            case 79: return false;   //
            case 271: return false;  // 5 WDs in this field pass proper motion limit but have <5sigma detections

		    //ESO R region:
		    case 330: return false;
		    //PAE region:
		    case 670: return false;
		    case 669: return false;
		    case 668: return false;
		    case 667: return false;
		    case 666: return false;
		    case 665: return false;
		    case 664: return false;
		    case 657: return false;
		    case 655: return false;
		    case 654: return false;
		    case 653: return false;
		    case 652: return false;
		    case 638: return false;
		    case 633: return false;
		    case 631: return false;
		    case 626: return false;
		    case 625: return false;
		    case 624: return false;
		    case 614: return false;
		    case 743: return false;
		    case 742: return false;
		    case 741: return false;
		    case 740: return false;
		    case 739: return false;
		    case 738: return false;
		    case 737: return false;
		    case 730: return false;
		    case 729: return false;
		    case 728: return false;
		    case 705: return false;
		    case 706: return false;
		    case 817: return false;
		    case 810: return false;
		    case 886: return false;
		    case 877: return false;
		    case 889: return false;
		    case 856: return false;
		    case 797: return false;
	    }
	}
	return true;
    }

    /**
     * Fields removed from low and high mu sample due to proximity to magellanic clouds.
     * @param fieldNum
     * 	The field number
     * @param hemisphere
     * 	The hemisphere ["N"/"S"]
     * @return
     * 	True if the field is OK, false if is is to be rejected.
     */
    public static boolean magellanicClouds(int fieldNum, String hemisphere){
	if(hemisphere.equals("S")){
	    switch(fieldNum){
		//SMC fields:
	    case 29: return false;
	    case 51: return false;
		//LMC fields:
	    case 33: return false;
	    case 56: return false;
	    case 57: return false;
	    case 85: return false;
	    case 86: return false;}}
	if(hemisphere.equals("N")){
	    switch(fieldNum){
            //fiducial entry to ensure program compiles:
	    case 900: return false;}
        }
	//if no entry for current field in list of those to avoid, return true.
	return true;
    }

    /**
     * Fields rejected from high pm sample due to poor spread in epochs, as identified
     * using the fieldEpochSpread application.
     * 
     * @param fieldNum
     * 	The field number
     * @param hemisphere
     * 	The hemisphere ["N"/"S"]
     * @return
     * 	True if the field is OK, false if is is to be rejected.
     */
    public static boolean epochSpread(int fieldNum, String hemisphere) {
		if(hemisphere.equals("S")){
		    switch(fieldNum){
			    case 375: return false;
			    case 618: return false;
			    case 633: return false;
			    case 703: return false;
			    case 742: return false;
			    case 840: return false;
			    case 862: return false;}}
		if(hemisphere.equals("N")){
		    switch(fieldNum){
			    case 2: return false;
			    case 3: return false;
			    case 4: return false;
			    case 9: return false;
			    case 10: return false;
			    case 11: return false;
			    case 12: return false;
			    case 15: return false;
			    case 21: return false;
			    case 25: return false;
			    case 29: return false;
			    case 57: return false;
			    case 64: return false;
			    case 68: return false;
			    case 72: return false;
			    case 77: return false;
			    case 83: return false;
			    case 84: return false;
			    case 92: return false;
			    case 103: return false;
			    case 104: return false;
			    case 106: return false;
			    case 109: return false;
			    case 125: return false;
			    case 133: return false;
			    case 161: return false;
			    case 168: return false;
			    case 179: return false;
			    case 233: return false;
			    case 274: return false;
			    case 277: return false;
			    case 358: return false;
			    case 384: return false;
			    case 386: return false;
			    case 456: return false;
			    case 513: return false;
			    case 579: return false;
			    case 615: return false;
			    case 624: return false;
			    case 639: return false;
			    case 661: return false;
			    case 679: return false;
			    case 680: return false;
			    case 751: return false;
			    case 754: return false;
			    case 765: return false;
			    case 774: return false;
			    case 820: return false;
			    case 895: return false;
			    case 896: return false;
			    case 897: return false;
		    }
        }
		// If no entry for current field in list of those to avoid, return true
		return true;
    }


    //+++ Fields rejected from extrpm survey because their proper motion uncertainty is large +++//
    //+++ and 5-sigma detections are not available at 80 mas/yr lower proper motion limit.    +++//
    //+++ Even with adoption of magnitude-dependent proper motion limits, some stars on these +++//
    //+++ fields pass proper motion limits but have less than 5-sigma proper motions.         +++//
    //
    // NOW DEPRECATED - these fields are now rejected from all surveys by contamination() method.
    public static boolean extrpmHighSigmaMu(int fieldNum, String hemisphere){
        System.err.println("Warning! - extrpmHighMuSigmaMu() is deprecated!");
	if(hemisphere.equals("N")){
	    switch(fieldNum){
	    case 999: return false;
	    }
	}
	if(hemisphere.equals("S")){
	    switch(fieldNum){
            case 53: return false;   // These threee may suffer from proximity to LMC
            case 58: return false;   //
            case 79: return false;   //
            case 271: return false;  // 5 WDs in this field pass proepr motion limit but have <5sigma detections
	    case 999: return false;
	    }

	}
	//if no entry for current field in list of those to avoid, return true.
	return true;
    }




}
