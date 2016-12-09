package sss.photo;

import java.io.*;
import java.util.Scanner;
import java.util.ArrayList;
import Field.*;
import flanagan.interpolation.*;


/**
 * This class is used to interpolate WD models at 10K intervals for use in fitting photometric models.
 *
 * @author nickrowell
 */
public class InterpolateWDModels {

    public static double[][] Htrack;
    public static double[][] Hetrack;

    public static void main(String[] args) throws IOException{

        //+++ Loop over each of two input files +++//
        for(int f=0; f<2; f++){
        
            File input  = (f==0) ? new File("/spare/SSS/Resources/PhotoParallaxes/ModelRepository/BaseModel/DA") : new File("/spare/SSS/Resources/PhotoParallaxes/ModelRepository/BaseModel/DB");
            File output = (f==0) ? new File("/spare/SSS/Resources/PhotoParallaxes/ModelRepository/PhotoDistances/DA") : new File("/spare/SSS/Resources/PhotoParallaxes/ModelRepository/PhotoDistances/DB");


            BufferedReader CMR = new BufferedReader(new FileReader(input));
            
            //+++ Set up ArrayList for each datum +++//
            ArrayList<Double> T    = new ArrayList<Double>();
            ArrayList<Double> logg = new ArrayList<Double>();
            ArrayList<Double> M    = new ArrayList<Double>();
            ArrayList<Double> Mbol = new ArrayList<Double>();
            ArrayList<Double> BC   = new ArrayList<Double>();
            ArrayList<Double> V    = new ArrayList<Double>();
            ArrayList<Double> Bj   = new ArrayList<Double>();
            ArrayList<Double> R2   = new ArrayList<Double>();
            ArrayList<Double> R1   = new ArrayList<Double>();
            ArrayList<Double> I    = new ArrayList<Double>();
            ArrayList<Double> age  = new ArrayList<Double>();
                       
            //+++ Now read all data in to ArrayLists +++//
            String data;
            while ((data = CMR.readLine()) != null) {
                        
                if (data.substring(0, 1).equals("#")) {} //avoid commented lines at start of file.
                else {
                    
                    Scanner scan = new Scanner(data);
                    
                    T.add(scan.nextDouble());
                    logg.add(scan.nextDouble());
                    M.add(scan.nextDouble());
                    Mbol.add(scan.nextDouble());
                    BC.add(scan.nextDouble());
                    V.add(scan.nextDouble());
                    Bj.add(scan.nextDouble());
                    R2.add(scan.nextDouble());
                    R1.add(scan.nextDouble());
                    I.add(scan.nextDouble());
                    age.add(scan.nextDouble());
                }
            }
            
            //+++ Get get data into regular array objects +++//
            double[] t = new double[T.size()];
            double[] Logg = new double[T.size()];
            double[] m = new double[T.size()];
            double[] mbol = new double[T.size()];
            double[] bc = new double[T.size()];
            double[] v = new double[T.size()];
            double[] bj = new double[T.size()];
            double[] r2 = new double[T.size()];
            double[] r1 = new double[T.size()];
            double[] i = new double[T.size()];
            double[] Age = new double[T.size()];

            //+++ Now read all data into Arrays +++//
            for(int e=0; e<T.size(); e++){
                t[e] = T.get(e);
                Logg[e] = logg.get(e);
                m[e] = M.get(e);
                mbol[e] = Mbol.get(e);
                bc[e] = BC.get(e);
                v[e] = V.get(e);
                bj[e] = Bj.get(e);
                r2[e] = R2.get(e);
                r1[e] = R1.get(e);
                i[e] = I.get(e);
                Age[e] = age.get(e);
            }

            //+++ Now create 10 CubicSplines, one in each T/variable pair +++//
            CubicSpline tLogg = new CubicSpline(t,Logg);
            CubicSpline tm    = new CubicSpline(t,m);
            CubicSpline tmbol = new CubicSpline(t,mbol);
            CubicSpline tbc   = new CubicSpline(t,bc);
            CubicSpline tv    = new CubicSpline(t,v);
            CubicSpline tbj   = new CubicSpline(t,bj);
            CubicSpline tr2   = new CubicSpline(t,r2);
            CubicSpline tr1   = new CubicSpline(t,r1);
            CubicSpline ti    = new CubicSpline(t,i);
            CubicSpline tAge  = new CubicSpline(t,Age);

            BufferedWriter out = new BufferedWriter(new FileWriter(output));


            //+++ Now interpolate these in 10K steps from coolest to hottest model +++//
            for(double Teff = t[0]; Teff<t[t.length-1]; Teff += 10){

                out.write(Teff+"\t"+tm.interpolate(Teff) + "\t"+ tLogg.interpolate(Teff)+"\t" + tmbol.interpolate(Teff) + "\t"
                          + tbc.interpolate(Teff) + "\t" + tv.interpolate(Teff)+"\t"+tbj.interpolate(Teff) + "\t"
                          +tr2.interpolate(Teff)+"\t"+tr1.interpolate(Teff)+"\t"+ti.interpolate(Teff)+"\t"
                          +tAge.interpolate(Teff)+"\n");

            }

            out.flush();
        }


    }




    
    //+++ Get appropriate colour-magnitude relations for doing RPM selection +++//

    public static void getHtrack() throws IOException{

	File model = new File("/spare/SSS/Resources/PhotoParallaxes/ModelRepository/RPMselection/Htrack");
      	BufferedReader CMR = new BufferedReader(new FileReader(model));

	Htrack = new double[2][Misc.lines(model,"#")];
	String data;
	int element=0;
	//read in colour magnitude relation...
	while((data=CMR.readLine())!=null){
	    if(data.substring(0,1).equals("#")){}  //avoid commented lines at start of file.
	    else{
		//B magnitude:
		Htrack[0][element] = Double.parseDouble(Misc.columns(data,7));
		//B-R2 colour
		Htrack[1][element] = Double.parseDouble(Misc.columns(data,7))-Double.parseDouble(Misc.columns(data,8));
		element++;
	    }
	}

	return;
    }

    public static void getHetrack() throws IOException{

	File model = new File("/spare/SSS/Resources/PhotoParallaxes/ModelRepository/RPMselection/Hetrack");

      	BufferedReader CMR = new BufferedReader(new FileReader(model));

	Hetrack = new double[2][Misc.lines(model,"#")];

	String data;
	int element=0;
	//read in colour magnitude relation...
	while((data=CMR.readLine())!=null){
	    if(data.substring(0,1).equals("#")){}  //avoid commented lines at start of file.
	    else{
		//B magnitude:
		Hetrack[0][element] = Double.parseDouble(Misc.columns(data,7));
		//B-R2 colour
		Hetrack[1][element] = Double.parseDouble(Misc.columns(data,7))-Double.parseDouble(Misc.columns(data,8));
		element++;
	    }
	}

	return ;
    }


}
