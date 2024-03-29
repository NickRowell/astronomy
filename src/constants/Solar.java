package constants;

public class Solar {
	
	/**
	 * Bolometric magnitude of the Sun, used to convert log-luminosity
	 * to bolometric magnitude according to the formula:
	 * 
	 * M_bol_star = M_bol_solar - 2.5 * Log( L_star / L_solar)
	 * 
	 */
    public static final double mbol = 4.75;
    
    /**
     * Solar luminosity in cgs units.
     */
    public static final double lnu_cgs = 3.828e33;
	
}
