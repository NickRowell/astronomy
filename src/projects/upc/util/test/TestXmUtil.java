package projects.upc.util.test;

import java.util.Map;

import projects.upc.dm.SsaCrossMatch;
import projects.upc.util.XmUtil;

/**
 * Tests associated with the {@link projects.upc.util.XmUtil}
 *
 *
 * @author nrowell
 * @version $Id$
 */
public class TestXmUtil {
	
	
	
	public static void main(String[] args) {
		
		
		Map<Integer, SsaCrossMatch> ssaCrossMatches = XmUtil.loadSsaCrossMatches();
		
		System.out.println("Got "+ssaCrossMatches.size()+" SSA cross matches");
		
	}
	
}
