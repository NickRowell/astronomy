package sss.util;

/**
 * Utilities associated with the SSA.
 *
 *
 * @author nrowell
 * @version $Id$
 */
public class SsaUtil {
	
	/**
	 * Reads the field number from the object ID. Note that a similar system of field numbers is
	 * used in the north and south hemispheres, so you need to additionally check which hemisphere
	 * the field is in.
	 * @param objID
	 * 	The object ID.
	 * @return
	 * 	The field on which the object was observed.
	 */
	public static long objid2ssafield(long objID) {
		return ((objID & 0xffff00000000L) / 0x100000000L);
	}
	
	/**
	 * Reads the field number from the object ID encoded as a string. Note that a similar system of
	 * field numbers is used in the north and south hemispheres, so you need to additionally check
	 * which hemisphere the field is in.
	 * @param objID
	 * 	The object ID as a string.
	 * @return
	 * 	The field on which the object was observed.
	 */
	public static long objid2ssafield(String objID) {
		return objid2ssafield(Long.parseLong(objID));
	}

}
