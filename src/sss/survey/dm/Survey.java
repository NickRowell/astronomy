package sss.survey.dm;

/**
 * Abstract class that allows the three surveys to be referenced as the same type of object
 * in code.
 *
 *
 * @author nickrowell
 */
public abstract class Survey{

    //+++ Image quality criteria +++//
    public double qcut    = 127;
    public double ecut    = 1.0;
    public double pcscut  = 4;

    //+++ Set magnitude step to use in numerical vmax integration +++//
    public double dm = 0.01;

    public abstract double getChi2Limit(String hemi);
    
    public abstract double getChi2DiscoveryFraction(String hemi);
    
    public abstract double getLowerProperMotionLimit(double mag, int field, String hemisphere);
    
    public abstract double getUpperProperMotionLimit();

    public abstract double[][] getVMax(WhiteDwarf star, double distance);
    /**
     *
     * @param absmags
     * @param discoveryFractions
     * @param v_min
     * @param discrete Are overlapping v_{tan} ranges to be used to calculate survey volume? This increases the
     *                 signal at the expense of introducing correlated errors on star counts.
     * @return
     */
    public abstract double[][] getMarginalizedVmax(double[] absmags, DiscoveryFractions[] discoveryFractions, double[] v_min, boolean discrete);

    public GeneralizedVolume thinDisk;
    public GeneralizedVolume thickDisk;
    public Footprint         footprint;
    public MagnitudeLimits   magnitudeLimits;

    //+++ Constructor to initialise data members that are uniform across all surveys +++//
    public Survey(String FOOTPRINT) throws Exception{

        thinDisk = new GeneralizedVolume(FOOTPRINT,
                                         Disks.getThinDiskScaleheight(),
                                         Solar.Z_solar);

        thickDisk = new GeneralizedVolume(FOOTPRINT,
                                          Disks.getThickDiskScaleheight(),
                                          Solar.Z_solar);


        footprint = new Footprint(FOOTPRINT);
        magnitudeLimits = new MagnitudeLimits();

    }

    //+++ Concrete methods that are the same for each survey +++//
    public double getFaintBLimit(int field, String hemisphere) {
    	return magnitudeLimits.getBMax(field, ((hemisphere.equals("N")) ? 0:1));
    }
    public double getFaintR1Limit(int field, String hemisphere) {
    	return magnitudeLimits.getR1Max(field, ((hemisphere.equals("N")) ? 0:1));
    }
    public double getFaintR2Limit(int field, String hemisphere) {
    	return magnitudeLimits.getR2Max(field, ((hemisphere.equals("N")) ? 0:1));
    }
    public double getFaintILimit(int field, String hemisphere) {
    	return magnitudeLimits.getIMax(field, ((hemisphere.equals("N")) ? 0:1));
    }

    public double getBrightBLimit(int field, String hemisphere) {
    	return magnitudeLimits.getBMin(field, ((hemisphere.equals("N")) ? 0:1));
    }
    public double getBrightR1Limit(int field, String hemisphere) {
    	return magnitudeLimits.getR1Min(field, ((hemisphere.equals("N")) ? 0:1));
    }
    public double getBrightR2Limit(int field, String hemisphere) {
    	return magnitudeLimits.getR2Min(field, ((hemisphere.equals("N")) ? 0:1));
    }
    public double getBrightILimit(int field, String hemisphere) {
    	return magnitudeLimits.getIMin(field, ((hemisphere.equals("N")) ? 0:1));
    }

    public double getSurveyFieldArea(int field, String hemisphere) {
    	return footprint.getSolidAngle(field, hemisphere);
    }

}