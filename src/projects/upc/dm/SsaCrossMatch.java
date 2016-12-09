package projects.upc.dm;

/**
 * This class encapsulates the results of the cross matching of one UPC star
 * with the SuperCOSMOS Science Archive.
 *
 * @author nrowell
 * @version $Id$
 */
public class SsaCrossMatch {
	
	/**
	 * URAT Parallax Catalog (UPC) number
	 */
	final public int upcId;
	
	/**
	 * Right ascension of the SSA crossmatched source [deg]
	 */
	final public double ssaRa;

	/**
	 * Declination of the SSA crossmatched source [deg]
	 */
	final public double ssaDec;

	/**
	 * Cross match distance [arcmin]
	 */
	final public double delta;
	
	/**
	 * Epoch of the SSA cross matched source [yr]
	 */
	final public double ssaEpoch;

	/**
	 * SSA B magnitude [mags]
	 */
	final public double ssaB;

	/**
	 * SSA R1 magnitude [mags]
	 */
	final public double ssaR1;

	/**
	 * SSA R2 magnitude [mags]
	 */
	final public double ssaR2;

	/**
	 * SSA I magnitude [mags]
	 */
	final public double ssaI;

	/**
	 * Ellipticity of the SSA B detection [-]
	 */
	final public double ellipB;

	/**
	 * Ellipticity of the SSA R1 detection [-]
	 */
	final public double ellipR1;

	/**
	 * Ellipticity of the SSA R2 detection [-]
	 */
	final public double ellipR2;

	/**
	 * Ellipticity of the SSA I detection [-]
	 */
	final public double ellipI;

	/**
	 * Quality flag of the SSA B detection [-]
	 */
	final public int qualB;

	/**
	 * Quality flag of the SSA R1 detection [-]
	 */
	final public int qualR1;

	/**
	 * Quality flag of the SSA R2 detection [-]
	 */
	final public int qualR2;

	/**
	 * Quality flag of the SSA I detection [-]
	 */
	final public int qualI;
	
	/**
	 * Main constructor for a {@link SsaCrossMatch}.
	 * 
	 * @param upcId
	 * 	See {@link SsaCrossMatch#upcId}.
	 * @param ssaRa
	 * 	See {@link SsaCrossMatch#ssaRa}.
	 * @param ssaDec
	 * 	See {@link SsaCrossMatch#ssaDec}.
	 * @param delta
	 * 	See {@link SsaCrossMatch#delta}.
	 * @param ssaEpoch
	 * 	See {@link SsaCrossMatch#ssaEpoch}.
	 * @param ssaB
	 * 	See {@link SsaCrossMatch#ssaB}.
	 * @param ssaR1
	 * 	See {@link SsaCrossMatch#ssaR1}.
	 * @param ssaR2
	 * 	See {@link SsaCrossMatch#ssaR2}.
	 * @param ssaI
	 * 	See {@link SsaCrossMatch#ssaI}.
	 * @param ellipB
	 * 	See {@link SsaCrossMatch#ellipB}.
	 * @param ellipR1
	 * 	See {@link SsaCrossMatch#ellipR1}.
	 * @param ellipR2
	 * 	See {@link SsaCrossMatch#ellipR2}.
	 * @param ellipI
	 * 	See {@link SsaCrossMatch#ellipI}.
	 * @param qualB
	 * 	See {@link SsaCrossMatch#qualB}.
	 * @param qualR1
	 * 	See {@link SsaCrossMatch#qualR1}.
	 * @param qualR2
	 * 	See {@link SsaCrossMatch#qualR2}.
	 * @param qualI
	 * 	See {@link SsaCrossMatch#qualI}.
	 */
	public SsaCrossMatch(int upcId, double ssaRa, double ssaDec, double delta, double ssaEpoch, double ssaB, double ssaR1, double ssaR2,
			double ssaI, double ellipB, double ellipR1, double ellipR2, double ellipI, int qualB, int qualR1, int qualR2, int qualI) {
		
		this.upcId = upcId;
		this.ssaRa = ssaRa;
		this.ssaDec = ssaDec;
		this.delta = delta;
		this.ssaEpoch = ssaEpoch;
		this.ssaB = ssaB;
		this.ssaR1 = ssaR1;
		this.ssaR2 = ssaR2;
		this.ssaI = ssaI;
		this.ellipB = ellipB;
		this.ellipR1 = ellipR1;
		this.ellipR2 = ellipR2;
		this.ellipI = ellipI;
		this.qualB = qualB;
		this.qualR1 = qualR1;
		this.qualR2 = qualR2;
		this.qualI = qualI;
	}
	
	/**
	 * Parse a single {@link SsaCrossMatch} from a line in the cross match file.
	 * @param record
	 * 	String containing comma-seperated-value representation of a {@link SsaCrossMatch}
	 * @return
	 * 	A {@link SsaCrossMatch} constructed form the fields parsed from the record.
	 */
	public static SsaCrossMatch parseSsaCrossMatch(String record) {
		
		// Split the String on commas, removing all white space
		String[] values = record.trim().split("\\s*,\\s*");
		
		int upcId = Integer.parseInt(values[0].replace("upc_id_", ""));
		double delta = Double.parseDouble(values[4].replace("+", ""));
		double ssaRa = Double.parseDouble(values[5].replace("+", ""));
		double ssaDec = Double.parseDouble(values[6].replace("+", ""));
		double ssaEpoch = Double.parseDouble(values[9].replace("+", ""));
		double ssaB = Double.parseDouble(values[15].replace("+", ""));
		double ssaR1 = Double.parseDouble(values[16].replace("+", ""));
		double ssaR2 = Double.parseDouble(values[17].replace("+", ""));
		double ssaI = Double.parseDouble(values[18].replace("+", ""));
		double ellipB = Double.parseDouble(values[24].replace("+", ""));
		double ellipR1 = Double.parseDouble(values[25].replace("+", ""));
		double ellipR2 = Double.parseDouble(values[26].replace("+", ""));
		double ellipI = Double.parseDouble(values[27].replace("+", ""));
		int qualB = Integer.parseInt(values[28]);
		int qualR1 = Integer.parseInt(values[29]);
		int qualR2 = Integer.parseInt(values[30]);
		int qualI = Integer.parseInt(values[31]);
		
		return new SsaCrossMatch(upcId, delta, ssaRa, ssaDec, ssaEpoch, ssaB, ssaR1, ssaR2, ssaI, ellipB, ellipR1,
				ellipR2, ellipI, qualB, qualR1, qualR2, qualI);
	}
	
	/**
	 * Implements validation checks on the image quality parameters etc to decide if we want to use
	 * this cross ID.
	 * @return
	 * 	true if the cross ID is acceptable; false otherwise.
	 */
	public boolean isAcceptable() {
		
		// Require that object is detected in B,R2,I bands
		if(this.ssaB < -90 || this.ssaR2 < -90 || this.ssaI < -90 ) {
			return false;
		}
		return true;
	}
	
}
