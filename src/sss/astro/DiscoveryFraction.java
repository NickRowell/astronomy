/*
 * A discovery fraction is the fraction of stars with tangential velocities above
 * a certain value, according to the kinematic model chosen.
 *
 */
package Kinematics;

import java.io.*;


/**
 *
 * @author nickrowell
 */
public class DiscoveryFraction {


    //+++ Main instance data member +++//
    double[][] discoveryFraction  = new double[900][2];



    //+++ Main Constructor +++//
    public DiscoveryFraction(double vtan, String population) throws IOException{

        //+++ New DiscoveryFractions object +++//
        DiscoveryFractions dfs = new DiscoveryFractions(population);

        //+++ Now interpolate these at desired tangential velocity and store in array +++//
	for(int f = 1; f<900; f++){
	    this.discoveryFraction[f][0] = dfs.getDiscoveryFraction(f, "N", vtan); //north
	    this.discoveryFraction[f][1] = dfs.getDiscoveryFraction(f, "S", vtan); //south
	}

    }


     //+++ Constructor with upper and lower tangential velocity limits +++//
    public DiscoveryFraction(double vtan_lower, double vtan_upper, String population) throws IOException{

        //+++ New DiscoveryFractions object +++//
        DiscoveryFractions dfs = new DiscoveryFractions(population);

        //+++ Now interpolate these at desired tangential velocity and store in array +++//
	for(int f = 1; f<900; f++){
	    this.discoveryFraction[f][0] = dfs.getDiscoveryFraction(f, "N", vtan_lower) -
                                      dfs.getDiscoveryFraction(f, "N", vtan_upper);   //north
	    this.discoveryFraction[f][1] = dfs.getDiscoveryFraction(f, "S", vtan_lower) -
                                      dfs.getDiscoveryFraction(f, "S", vtan_upper);   //south
	}

    }




    //+++ Get a single discovery fraction from array +++//
    public double getDiscoveryFraction(int f, String hemi){
            return discoveryFraction[f][(hemi.equals("N") ? 0 : 1)];
    }


}
