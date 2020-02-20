package constants;

import Jama.Matrix;

/**
 * 
 * Constants and quantities associated with Galactic structure.
 * 
 * Notation used in this class:
 * 
 *  - <r>_E
 *  	subscript E indicates a quantity expressed in the Equatorial frame.
 *  
 *  - <r>_G
 *  	subscript G indicates a quantity expressed in the Galactic frame.
 * 
 *  - r_E_G
 *  	is a 3x3 matrix that transforms 3x1 vectors from frame G (Galactic)
 *  	to frame E (Equatorial) by right multiplication, e.g:
 *  		
 *  		r_E = r_E_G * r_G
 *  
 *  
 *  Galactic Coordinates [G]
 *  ------------------------
 *  X: Towards the Galactic centre
 *  Y: Perpendicular to X, in the Galactic plane (in direction of rotation)
 *  Z: Points towards north Galactic pole
 *  
 * 	Equatorial Coordinates [E]
 * 	--------------------------
 * 	X: Towards the Vernal Equinox
 *  Y: Perpendicular to X, in the equatorial plane
 *  Z: Points towards north rotational pole
 * 
 * 
 * 
 * 
 * @author nrowell
 *
 */
public class Galactic
{
	/**
	 * Equatorial right ascension of the Galactic Centre [radians]
	 * From Galactic Astronomy (pp30-31).
	 */
    public static final double GCra   = Math.toRadians(266.4050);

	/**
	 * Equatorial declination of the Galactic Centre [radians]
	 * From Galactic Astronomy (pp30-31).
	 */
    public static final double GCdec  = Math.toRadians(-28.9362);

	/**
	 * Equatorial right ascension of the North Galactic Pole [radians]
	 * From Galactic Astronomy (pp30-31).
	 */
    public static final double NGPra  = Math.toRadians(192.85948);

	/**
	 * Equatorial declination of the North Galactic Pole [radians]
	 * From Galactic Astronomy (pp30-31).
	 */
    public static final double NGPdec = Math.toRadians(27.12825);

    
    // Basis vectors of Galactic frame, expressed in Equatorial frame
    public static final double[] u = new double[]{Math.cos(GCra)*Math.cos(GCdec), Math.sin(GCra)*Math.cos(GCdec), Math.sin(GCdec)};
    public static final double[] v = new double[]{Math.sin(GCdec)*Math.sin(NGPra)*Math.cos(NGPdec) - Math.sin(NGPdec)*Math.sin(GCra)*Math.cos(GCdec), Math.sin(NGPdec)*Math.cos(GCra)*Math.cos(GCdec)-Math.cos(NGPra)*Math.cos(NGPdec)*Math.sin(GCdec), Math.cos(GCdec)*Math.cos(NGPdec)*Math.sin(GCra - NGPra)};
    public static final double[] w = new double[]{Math.cos(NGPra)*Math.cos(NGPdec), Math.sin(NGPra)*Math.cos(NGPdec), Math.sin(NGPdec)};
	
    // 3x1 basis vectors of Galactic frame, expressed in Equatorial frame
    public static final Matrix U = new Matrix(u,3);
    public static final Matrix V = new Matrix(v,3);
    public static final Matrix W = new Matrix(w,3);
    
    
    
	// Rows consist of the basis vectors of the Galactic frame, expressed in the
	// Equatorial frame.
    private static final double[][] rge = new double[][]{u,v,w};
    
    /**
     *  This Matrix transforms 3x1 column vectors from Equatorial to Galactic
     *  coordinates, like so:
     *  
     *  r_G = r_G_E * r_E
     */
    public static final Matrix r_G_E = new Matrix(rge);
    
    /**
     * This Matrix transforms 3x1 column vectors from Galactic to Equatorial
     * coordinates, like so:
     *
     * r_E = r_E_G * r_G
     */
    public static final Matrix r_E_G = r_G_E.transpose();

	
    //+++ Disks scaleheight [parsecs] +++//
    public static final double thinDiskScaleheight  = 250.0;
    public static final double thickDiskScaleheight = 1500.0;
    
    
    //+++++++++++++++++ Oort Constants +++++++++++++++++++++//
    
    /**
     * Sources for the Oort Constants:
     * 
     * Feast & Whitelock (1997) MNRAS 291,683-693
     *  - Based on 3D space motions of Hipparcos Cepheids out to ~8kpc
     *  - A =  14.82 ± 0.84
     *  - B = -12.37 ± 0.64
     *  
     * Olling & Dehnen (2003) ApJ 599:275-296
     *  - Based on proper motions of 10^6 Tycho-2 stars
     *  - A = 
     *  - B = 
     *  - C = 
     *  
     * Bovy (2015) [arXiv:1610.07610]
     *  - Based on 3D space motions of 300,000 Gaia TGAS stars
     *  - A =  15.3 ± 0.64 km/s/kpc [combination of 0.4 random & 0.5 systematic]
     *  - B = -11.9 ± 0.4  km/s/kpc
     *  - C = -3.2  ± 0.4  km/s/kpc
     *  - K = -3.3  ± 0.6  km/s/kpc
     */
    
    /**
     * Oort constant A, quantifying the azimuthal shear of the velocity field.
     * Units: km s^{-1} kpc^{-1}  [kilometres per second per kiloparsec]
     * Source: Bovy (2017) [arXiv:1610.07610]
     */
    public static final double A = 15.3;
    /**
     * See {@link Galactic#A}
     */
    public static final double sigma_A = 0.64;
    
    /**
     * Oort constant B, quantifying the vorticity of the velocity field.
     * Units: km s^{-1} kpc^{-1}  [kilometres per second per kiloparsec]
     * Source: Bovy (2017) [arXiv:1610.07610]
     */
    public static final double B = -11.9;
    /**
     * See {@link Galactic#B}
     */
    public static final double sigma_B = 0.4;

    /**
     * Oort constant C, quantifying the radial shear of the velocity field.
     * Units: km s^{-1} kpc^{-1}  [kilometres per second per kiloparsec]
     * Source: Bovy (2017) [arXiv:1610.07610]
     */
    public static final double C = -3.2;
    /**
     * See {@link Galactic#C}
     */
    public static final double sigma_C = 0.4;

    /**
     * Oort constant K, quantifying the divergence of the velocity field.
     * Units: km s^{-1} kpc^{-1}  [kilometres per second per kiloparsec]
     * Source: Bovy (2017) [arXiv:1610.07610]
     */
    public static final double K = -3.3;
    /**
     * See {@link Galactic#K}
     */
    public static final double sigma_K = 0.6;
    
    //+++++++++++++ Angular velocity of circular rotation at the Sun +++++++++++++//
    
    // Units: km s^{-1} kpc^{-1}  [kilometres per second per kiloparsec]
 
    // Source: Feast & Whitelock (1997) MNRAS 291,683-693
    public static final double omega = 27.19;
    public static final double sigma_omega = 0.87;
    
    
    //++++++++++++++++ Solar distance from Galactic plane ++++++++++++++++//
    
    // Source: B. Cameron Reed 2006  JRASC 100:146-148
    public static final int Z_solar = 20;

    //+++++++++++++++++ Solar motion wrt LSR +++++++++++++++++++++++//
    
    /**
     * U component of the Solar motion with respect to the Local Standard of Rest.
     * Source: Aumer & Binney (2009) MNRAS 397(3),1286-1301.
     * Units: km s^{-1}
     */
    public static final double S_U = 9.96;
    /**
     * See {@link Galactic#S_U}
     */
    public static final double sigma_S_U = 0.33;
    /**
     * See {@link Galactic#S_U}
     */
    public static final double S_V = 5.25;
    /**
     * See {@link Galactic#S_U}
     */
    public static final double sigma_S_V = 0.54;
    /**
     * See {@link Galactic#S_U}
     */
    public static final double S_W = 7.07;
    /**
     * See {@link Galactic#S_U}
     */
    public static final double sigma_S_W = 0.34;
    
    
    // Source: Dehnen & Binney 1998
    //public static double S_U = 10.0;
    //public static double S_V = 5.25;
    //public static double S_W = 7.17;
    
    // Source: Schonrich, Binney & Dehnen 2009
    //public static double U = 11.1;
    //public static double V = 12.24;
    //public static double W = 7.25;
    
    public static final Matrix solar_G = new Matrix(new double[][]{{S_U},{S_V},{S_W}});
    
    //+++++++++++++ Velocity ellipsoids in the Solar neighbourhood +++++++++++++++++//
    
    // Velocities are expressed in the Galactic reference frame, in km/s.
    // The mean velocity is relative to the Sun (NOT local standard of rest)
    
    // Other sources:
    // Reid, Hawley & Gizis (1995) AJ 110(4)

    // THIN DISK
	//  From Fuchs et al. 2009 study of M dwarfs in the Solar cylinder.
	//  Values taken from 0 - 100 pc bin that is least affected by problems associated with
	//  deprojection of proper motions. This is also the bin least contaminated by thick
    //  disk stars and corresponds to the distance range in which most of my WDs lie.
	public static final double thin_disk_sigma_U = 32.4;
	public static final double thin_disk_sigma_V = 23.0;
	public static final double thin_disk_sigma_W = 18.1;

	public static final double thin_disk_mean_U =  -8.62;
	public static final double thin_disk_mean_V = -20.04;
	public static final double thin_disk_mean_W =  -7.10;
	
	public static final Matrix thin_disk_mean = 
			new Matrix(new double[][]{{thin_disk_mean_U},{thin_disk_mean_V},{thin_disk_mean_W}});
	public static final Matrix thin_disk_covariance = 
			new Matrix(new double[][]{{thin_disk_sigma_U*thin_disk_sigma_U,0,0},
                                      {0,thin_disk_sigma_V*thin_disk_sigma_V,0},
                                      {0,0,thin_disk_sigma_W*thin_disk_sigma_W}});
	
	// THICK DISK
	//  "Kinematics of metal poor stars in the Galaxy III"   Chiba & Beers 2000
	//   Taken from table 1 line 1, -0.6 < [Fe/H]  -0.8, a metallicity range expected to represent
	//   the thick disk according to Binney & Merrifield (Gal. Astronomy pp 655)
    //
    //   Note that Chiba & Beers correct their mean velocities to the LSR by adopting a value
    //   for the Solar motion. I've removed this correction here.
	public static final double thick_disk_sigma_U = 50.0;
	public static final double thick_disk_sigma_V = 56.0;
	public static final double thick_disk_sigma_W = 34.0;

	public static final double thick_disk_mean_U  = -11.0;
	public static final double thick_disk_mean_V  = -42.0;              
	public static final double thick_disk_mean_W  = -12.0;

	public static final Matrix thick_disk_mean = 
			new Matrix(new double[][]{{thick_disk_mean_U},{thick_disk_mean_V},{thick_disk_mean_W}});
	public static final Matrix thick_disk_covariance = 
			new Matrix(new double[][]{{thick_disk_sigma_U*thick_disk_sigma_U,0,0},
                                      {0,thick_disk_sigma_V*thick_disk_sigma_V,0},
                                      {0,0,thick_disk_sigma_W*thick_disk_sigma_W}});
	
	
	// SPHEROID
	//  "Kinematics of metal poor stars in the Galaxy III"   Chiba & Beers 2000
	//   Taken from table 1 line 5, [Fe/H] <= -2.2, which they say 'likely represents a pure
	//   halo component'
    //
    //   Same correction applied as above.
	public static final double spheroid_sigma_U = 141.0;
	public static final double spheroid_sigma_V = 106.0;
	public static final double spheroid_sigma_W = 94.0;
	
	public static final double spheroid_mean_U  = -26;            
	public static final double spheroid_mean_V  = -199;           
	public static final double spheroid_mean_W  = -12;	        
	
	public static final Matrix spheroid_mean = 
			new Matrix(new double[][]{{spheroid_mean_U},{spheroid_mean_V},{spheroid_mean_W}});
	public static final Matrix spheroid_covariance = 
			new Matrix(new double[][]{{spheroid_sigma_U*spheroid_sigma_U,0,0},
                                      {0,spheroid_sigma_V*spheroid_sigma_V,0},
                                      {0,0,spheroid_sigma_W*spheroid_sigma_W}});
	
}
