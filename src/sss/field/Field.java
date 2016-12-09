package sss.field;

/**
 * Encapsulates the parameters for a single field in the SSS, and 
 * provides access to parameters for the set of all fields.
 * 
 * Parameters to record:
 *  -) Coordinates of centre
 *  -) Drill fraction
 *  -) ...?
 * 
 */
public enum Field
{
	S_1(40.0, 50.0),
	S_2(40.0, 30.0);
	
	// Coordinates of centre [radians]
	double ra;
	double dec;
	
	// Constructor
	Field(double pra, double pdec)
	{
		ra  = pra;
		dec = pdec;
	}
	
}
