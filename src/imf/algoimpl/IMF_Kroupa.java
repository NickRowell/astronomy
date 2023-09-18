package imf.algoimpl;

import imf.algo.BaseImf;

/**
 * Implementation of the Kroupa IMF.
 * 
 * TODO: fill in the other parts of the function.
 *
 *
 * @author nrowell
 * @version $Id$
 */
public class IMF_Kroupa extends BaseImf
{

    /**
     * Normalisation constant for Kroupa IMF.
     */
    private double A = 1.4330809;
    
    /**
     * Basic constructor.
     */
    public IMF_Kroupa(){}

    /**
     * {@inheritDoc}
     */
    @Override
    public double getIMF(double M) 
    {
        return Math.pow(M, -2.3) / A;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getIntegral(double M) 
    {
        return (1. / A) * (-1. / 1.3)
                * (Math.pow(M, -1.3) - Math.pow(M_lower, -1.3));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double drawMass() 
    {
        double x = Math.random();
        
        return Math.exp(Math.log(x * A * (-1.3) + Math.pow(M_lower, -1.3)) / -1.3);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() 
    {
        return "Kroupa";
    }

}