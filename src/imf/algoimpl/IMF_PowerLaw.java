package imf.algoimpl;

import java.util.Random;

import imf.algo.BaseImf;

/**
 * Simple one-parameter power law IMF.
 *
 * @author nrowell
 * @version $Id$
 */
public class IMF_PowerLaw extends BaseImf
{

    /**
     * Instance of {@link Random} used to provide random number generation.
     */
	private static final Random random = new Random(System.currentTimeMillis());
	
    /**
     * Power law exponent.
     */
    private double exponent;

    /**
     * Normalisation constant.
     */
    private double A;
    
    /**
     * Default constructor. The IMF exponent if initialised to -2.35.
     */
    public IMF_PowerLaw()
    {
        this(-2.35);
    }
    
    /**
     * Main constructor.
     * @param exp
     * 	The power law exponent.
     */
    public IMF_PowerLaw(double exp)
    {
        setExponent(exp);
    }
    
    /**
     * Set the power law exponent. The normalisation constant is automatically updated.
     * @param exp
     * 	The power law exponent to set.
     */
    public final void setExponent(double exp)
    {
        exponent = exp;
        // calculate normalisation constant
        A = 1/(exponent+1) * (Math.pow(M_upper, exponent+1) - Math.pow(M_lower, exponent+1));
        A = 1/A;
    }
    
    /**
     * Get the power law exponent.
     * @return
     * 	The power law exponent.
     */
    public double getExponent() {
    	return exponent;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public double getIMF(double M) {
        //assert (M>=M_lower)&&(M<=M_upper);
        if(!((M>=M_lower)&&(M<=M_upper)))
            throw new RuntimeException("getIMF() error: Mass = "+M);
        
        return A*Math.pow(M,exponent);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public double getIntegral(double M) {
        return A * (1/(exponent+1)) * (Math.pow(M,exponent+1)-Math.pow(M_lower,exponent+1));
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public double drawMass() {
        double x = random.nextDouble();        
        return Math.pow((x/A)*(exponent+1) + Math.pow(M_lower,exponent+1), 1/(exponent+1));
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
    	return "Power law (exp = "+exponent+")";
    }
    
}