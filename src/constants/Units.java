package constants;

public class Units
{
	// Convert parsecs to other units
	public static double PC_TO_M   = 3.0857e16;
	public static double KPC_TO_M   = 3.0857e19;
	public static double PC_TO_KM  = PC_TO_M/1000.0;
	public static double KPC_TO_KM  = KPC_TO_M/1000.0;
	public static double PC_TO_AU  = 206264.81;
	public static double PC_TO_LYR = 3.2615638;
	
	// Reverse conversions
	public static double M_TO_PC   = 1.0/PC_TO_M;
	public static double M_TO_KPC   = 1.0/KPC_TO_M;
	public static double KM_TO_PC  = 1.0/PC_TO_KM;
	public static double KM_TO_KPC  = 1.0/KPC_TO_KM;
	public static double AU_TO_PC  = 1.0/PC_TO_AU;
	public static double LYR_TO_PC = 1.0/PC_TO_LYR;

    /**
     * Converts angles from radians to arcseconds.
     */
    public static final double RADIANS_TO_ARCSEC = Math.toDegrees(1.0)*3600.0;
    
    /**
     * Converts angles from radians to milliarcseconds.
     */
    public static final double RADIANS_TO_MILLIARCSEC = RADIANS_TO_ARCSEC * 1e3;
    
    /**
     * Converts angles from arcseconds to radians.
     */
    public static final double ARCSEC_TO_RADIANS = Math.toRadians(1.0/3600.0);
    
    /**
     * Converts angles from milliarcseconds to radians.
     * 4.84813681109536E-9
     */
    public static final double MILLIARCSEC_TO_RADIANS = ARCSEC_TO_RADIANS / 1e3;

    /**
     * Converts durations from Julian years to seconds.
     */
    public static final double JULIAN_YEARS_TO_SECONDS = 365.25 * 86400;
	
	/**
     * Converts durations from seconds to Julian years.
     */
    public static final double SECONDS_TO_JULIAN_YEARS = 1.0 / JULIAN_YEARS_TO_SECONDS;

    /**
     * Converts velocities from parsecs per year to kilometres per second.
     */
    public static final double PARSECS_PER_YEAR_TO_KILOMETRES_PER_SECOND =  Units.PC_TO_KM / JULIAN_YEARS_TO_SECONDS;
	
    /**
     * Converts velocities from kilometres per second to parsecs per year.
     */
    public static final double KILOMETRES_PER_SECOND_TO_PARSECS_PER_YEAR =  Units.KM_TO_PC / SECONDS_TO_JULIAN_YEARS;
	
    /**
     * Converts angular velocities from km/s/kpc to rad/yr.
     * 1.0227047347441424E-9
     */
    public static final double KM_PER_SEC_PER_KPC_TO_RAD_PER_YEAR = Units.JULIAN_YEARS_TO_SECONDS * Units.KM_TO_KPC;
	
	/**
     * Converts angular velocities from km/s/kpc to mas/yr.
     * 0.21094799395998862
     */
    public static final double KM_PER_SEC_PER_KPC_TO_MAS_PER_YEAR = KM_PER_SEC_PER_KPC_TO_RAD_PER_YEAR * Units.RADIANS_TO_MILLIARCSEC;
	
	/**
	 * Converts angular velocities from rad/yr to km/s/kpc.
	 * 9.777993256774913E8
	 */
    public static final double RAD_PER_YEAR_TO_KM_PER_SEC_PER_KPC = 1.0 / KM_PER_SEC_PER_KPC_TO_RAD_PER_YEAR;
	
	/**
	 * Converts angular velocities from mas/yr to km/s/kpc.
	 * 4.7405049046812655
	 */
    public static final double MAS_PER_YEAR_TO_KM_PER_SEC_PER_KPC = 1.0 / KM_PER_SEC_PER_KPC_TO_MAS_PER_YEAR;
    
    
    
	
	
}
