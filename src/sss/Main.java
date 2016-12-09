package sss;

import java.util.Scanner;

import sss.util.SsaUtil;

/**
 * Main class for the SSS project.
 *
 *
 * @author nrowell
 * @version $Id$
 */
public class Main {
	
	
	public static void main(String[] args) {
		
		// Check field number parsing
//		long fieldId = SsaUtil.objid2ssafield(281728380072151L);
//		System.out.println("Field ID = "+fieldId);
//		int field = (int)((Long.parseLong("281728380072151") & Long.decode("0xffff00000000"))/Long.decode("0x100000000"));
//		System.out.println("Field = "+field);
		
		String test = "+345";
		
		int i = Integer.parseInt(test);
		
		System.out.println("i = "+i);
		
	}
	
}
