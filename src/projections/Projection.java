package projections;

import java.util.LinkedList;
import java.util.List;

/**
 * Supertype for all types of map and sky projection.
 * 
 * TODO: set up common structure with algo/algoimpl/infra etc
 * TODO: add central meridian field, update constant latitude lines
 * TODO: add inverse projection
 * TODO: classify projections according to equal area etc?
 *
 * @author nrowell
 * @version $Id$
 */
public abstract class Projection {
	
    /**
     * Forward projection from angular coordinates to projection plane.
     * @param in 	Anguler coordinates e.g. (RA,dec), (lat,long) [radians]
     * @return		The projection coordinates
     */
    public abstract double[] getForwardProjection(double[] in);
    
    /**
     * Name of the projection.
     */
    public abstract String toString();
    
    /**
     * Get the range of the Y projection coordinate.
     * @return
     * 	The maximal values of the projected Y coordinate.
     */
    public double[] getRangeY()
    {
		double[] northPole = new double[]{0, Math.PI/2.0};
		double[] southPole = new double[]{0,-Math.PI/2.0};
		
		double[] northProj = getForwardProjection(northPole);
		double[] southProj = getForwardProjection(southPole);
		
		double y1 = southProj[1];
		double y2 = northProj[1];
		
		return new double[]{Math.min(y1, y2), Math.max(y1, y2)};
	}
    
    /**
     * Get the range of the X projection coordinate.
     * @return
     * 	The maximal values of the projected X coordinate.
     */
    public double[] getRangeX()
    {
		double[] eastHorizon = new double[]{ Math.PI, 0};
		double[] westHorizon = new double[]{-Math.PI, 0};
		
		double[] eastProj = getForwardProjection(eastHorizon);
		double[] westProj = getForwardProjection(westHorizon);
		
		double x1 = eastProj[0];
		double x2 = westProj[0];
		
		return new double[]{Math.min(x1, x2), Math.max(x1, x2)};
	}
    
    /**
     * Creates and returns lines of constant longitude in the projected space. Each line starts
     * at the south pole and ends at the north pole. Coordinates are in projected coordinates,
     * with each line stored as a 2D array of doubles, and the full set contained in the List.
     * 
     * @param n
     * 	Number of constant longitude lines (must be at least 2). The lines are spaced uniformly
     * between -PI and +PI (inclusive), which form the boundary of the projection as the standard
     * parallel is at 0.
     * @return
     * 	List of 2D double arrays; each array encodes one line of constant longitude as follows:
     * <ul>
     *  <li>array[i][0] - contains the first coordinate of point i</li>
     *  <li>array[i][1] - contains the second coordinate of point i</li>
     * </ul>
     */
    public List<double[][]> getConstantLongitudeLinesProjected(int n) {
    	
    	// Get un-projected lines (plain longitude/latitude)
    	List<double[][]> lines = getConstantLongitudeLines(n);
    	
    	// Project the coordinates
    	for(double[][] line : lines) {
    		
    		for(double[] point : line) {
    			
    			double[] projected = getForwardProjection(point);
    			point[0] = projected[0];
    			point[1] = projected[1];
    		}
    	}
    	
    	return lines;
    }
    
    /**
     * Creates and returns lines of constant longitude. Each line starts at the south pole and ends
     * at the north pole. Coordinates are in unprojected (plain longitude/latitude),
     * with each line stored as a 2D array of doubles, and the full set contained in the List.
     * 
     * @param n
     * 	Number of constant longitude lines (must be at least 2). The lines are spaced uniformly
     * between -PI and +PI (inclusive), which form the boundary of the projection as the standard
     * parallel is at 0.
     * @return
     * 	List of 2D double arrays; each array encodes one line of constant longitude as follows:
     * <ul>
     *  <li>array[i][0] - contains the first coordinate of point i</li>
     *  <li>array[i][1] - contains the second coordinate of point i</li>
     * </ul>
     */
    public List<double[][]> getConstantLongitudeLines(int n) {
    	
    	if(n<2) {
    		throw new RuntimeException("Number of constant longitude lines must be greater than one!");
    	}
    	
    	// Longitude step between lines
    	double longStep = 2.0d*Math.PI / (n-1);
    	
    	// Number of points to plot along each line of constant longitude (including poles)
    	int nP = 90;

    	// Latitude step between points on the same line of constant longitude
    	double latStep = Math.PI / (nP-1);
    	
    	List<double[][]> lines = new LinkedList<>();
    	
    	// Loop over lines
    	for(int i=0; i<n; i++) {
    		
    		// Longitude coordinate
    		double longitude = -Math.PI + i*longStep;
    		
    		double[][] line = new double[nP][2];
    		
    		for(int p=0; p<nP; p++) {
    			
    			// Latitude coordinate
    			double latitude = -Math.PI/2.0 + p*latStep;
    			
    			// Projected coordinate
    			line[p] = new double[]{longitude, latitude};
    		}
    		lines.add(line);
    	}
    	
    	return lines;
    }
    
    /**
     * Creates and returns lines of constant latitude. Lines range in longitude [-PI:PI].
     * Coordinates are in the projected space, with each line stored as a 2D array of 
     * doubles, and the full set contained in the List.
     * 
     * TODO: when the central meridian field is added, the line start and end points will
     * need to be updated.
     * 
     * @param n
     * 	Number of constant latitude lines (must be at least 1). The lines are spaced uniformly
     * between -PI/2 and +PI/2 (inclusive).
     * @return
     * 	List of 2D double arrays; each array encodes one line of constant latitude as follows:
     * <ul>
     *  <li>array[i][0] - contains the first coordinate of point i</li>
     *  <li>array[i][1] - contains the second coordinate of point i</li>
     * </ul>
     */
    public List<double[][]> getConstantLatitudeLinesProjected(int n) {
    	
    	// Get un-projected lines (plain longitude/latitude)
    	List<double[][]> lines = getConstantLatitudeLines(n);
    	
    	// Project the coordinates
    	for(double[][] line : lines) {
    		
    		for(double[] point : line) {
    			
    			double[] projected = getForwardProjection(point);
    			point[0] = projected[0];
    			point[1] = projected[1];
    		}
    	}
    	
    	return lines;
    }
    
    /**
     * Creates and returns lines of constant latitude. Lines range in longitude [-PI:PI].
     * Coordinates are in the unprojected space (plain longitude/latitude), with each line
     * stored as a 2D array of doubles, and the full set contained in the List.
     * 
     * TODO: when the central meridian field is added, the line start and end points will
     * need to be updated.
     * 
     * @param n
     * 	Number of constant latitude lines (must be at least 1). The lines are spaced uniformly
     * between -PI/2 and +PI/2 (inclusive).
     * @return
     * 	List of 2D double arrays; each array encodes one line of constant latitude as follows:
     * <ul>
     *  <li>array[i][0] - contains the first coordinate of point i</li>
     *  <li>array[i][1] - contains the second coordinate of point i</li>
     * </ul>
     */
    public List<double[][]> getConstantLatitudeLines(int n) {
    	
    	if(n<1) {
    		throw new RuntimeException("Number of constant latitude lines must be greater than zero!");
    	}
    	
    	// Latitude step between lines
    	double latStep = Math.PI / (n+1);
    	
    	// Number of points to plot along each line of constant latitude
    	int nP = 180;
    	
    	// Longitude step between points on the same line of constant latitude
    	double longStep = 2.0d*Math.PI / (nP-1);
    	
    	List<double[][]> lines = new LinkedList<>();
    	
    	// Loop over lines
    	for(int i=0; i<n; i++) {
    		
    		// Latitude coordinate
    		double latitude = -Math.PI/2d + (i+1)*latStep;
    		
    		double[][] line = new double[nP][2];
    		
    		for(int p=0; p<nP; p++) {
    			
    			// Longitude coordinate
    			double longitude = -Math.PI + p*longStep;
    			
    			// Projected coordinate
    			line[p] = new double[]{longitude, latitude};
    		}
    		lines.add(line);
    	}
    	
    	return lines;
    }
    
}