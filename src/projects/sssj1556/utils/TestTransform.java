package projects.sssj1556.utils;

import java.io.File;

import projects.sssj1556.astrometry.Coordinates2D;
import projects.sssj1556.astrometry.Detection;
import projects.sssj1556.astrometry.StackedImage;
import projects.sssj1556.astrometry.Star;
import time.Epoch;

/**
 *
 * @author nickrowell
 */
public class TestTransform
{
    
    
    public static void main(String[] args)
    {
        File path = new File("/home/nickrowell/Astronomy/WD_Parallax_Study_2014/data/LHS_3250");
        
        
        // Set up reference stars:
        Star lhs3250     = new Star(Coordinates2D.hmsToRadians(16, 54, 1.7603), Coordinates2D.dmsToRadians(62, 53, 54.113), new Epoch(1993.618), -0.5433, 0.0051, 0.1301, 0.0062);
        Star lhs3250_r1  = new Star(4.424353803269561, 1.0974472772093984, new Epoch(1993.306), -0.038299999999999994, 0.004148, 0.1097, 0.005679999999999999);
        Star lhs3250_r2  = new Star(4.4237327569440605, 1.0975290167960332, new Epoch(1993.306), -0.020980000000000002, 0.004293, 0.05045, 0.005719999999999999);
        Star lhs3250_r3  = new Star(4.423334603708449, 1.0976282581565566, new Epoch(1993.306), 0.02052, 0.004399, 0.01307, 0.005878);
        Star lhs3250_r4  = new Star(4.425329733209635, 1.0982354872921463, new Epoch(1993.615), 0.005543, 0.004123, -0.006598, 0.006241);
        Star lhs3250_r5  = new Star(4.424115129494352, 1.0971314695775234, new Epoch(1993.306), -0.02169, 0.004091999999999999, -0.004091, 0.005726);
        Star lhs3250_r6  = new Star(4.425480486023775, 1.0978630049409497, new Epoch(1993.615), -0.002218, 0.004216, -0.004114, 0.005968);
        Star lhs3250_r7  = new Star(4.423813696588121, 1.097814184203262, new Epoch(1993.306), -0.002921, 0.0043170000000000005, 6.412E-4, 0.005881);
        Star lhs3250_r8  = new Star(4.424766210027397, 1.0971986162723573, new Epoch(1993.306), 0.002541, 0.004398, -0.005974999999999999, 0.005709);
        Star lhs3250_r9  = new Star(4.425368275897283, 1.0981180654185814, new Epoch(1993.615), -0.0324, 0.0047220000000000005, -0.00114, 0.005984);
        Star lhs3250_r10 = new Star(4.425081096513278, 1.0970887090108496, new Epoch(1993.306), 0.009288000000000001, 0.004601, -0.00851, 0.005802);
        Star lhs3250_r11 = new Star(4.425223922623732, 1.0982246759470573, new Epoch(1993.615), -0.004783000000000001, 0.0057599999999999995, -7.183000000000001E-4, 0.006746);
        Star lhs3250_r12 = new Star(4.424412999020025, 1.0984395938518932, new Epoch(1993.615), -0.01255, 0.005707, -0.0027890000000000002, 0.006654);
        Star lhs3250_r13 = new Star(4.425742067245419, 1.097874882876137, new Epoch(1993.615), -0.001618, 0.006801, 0.006646, 0.007847999999999999);
        Star lhs3250_r14 = new Star(4.425269737516598, 1.0976587044557302, new Epoch(1993.615), 0.001968, 0.006622, 0.004568, 0.007677);
        Star lhs3250_r15 = new Star(4.425160218106035, 1.0976482809615864, new Epoch(1993.615), -0.0045579999999999996, 0.0067599999999999995, -0.009046, 0.007807000000000001);
        Star lhs3250_r16 = new Star(4.425181961999633, 1.0982489166311131, new Epoch(1993.615), -0.11320000000000001, 0.006766, 0.1308, 0.007690000000000001);
        Star lhs3250_r17 = new Star(4.423714285542809, 1.0982099860925199, new Epoch(1993.615), 0.01701, 0.1331, 0.1885, 0.1386);
        Star lhs3250_r18 = new Star(4.424138473273097, 1.098061536143364, new Epoch(1993.615), -0.0054020000000000006, 0.009666000000000001, -0.006142000000000001, 0.01035);
        
        
        
        
        // Create master image:
        StackedImage master = new StackedImage(new Epoch(2009, 7, 17, 21, 41, 05), new File("2009.07.17/grp1"), 2038, 1983);
        master.add(new Detection(lhs3250_r1, 571.255, 927.854, 1.5699464674E-4, 1.1475977219E-4, 1.2280209084E-6));
        master.add(new Detection(lhs3250_r2, 689.173, 511.859, 2.0161284942E-4, 1.5029857689E-4, 1.3417168932E-6));
        master.add(new Detection(lhs3250_r3, 832.543, 249.427, 1.9425029202E-4, 1.4689630851E-4, 2.0747110759E-6));
        master.add(new Detection(lhs3250_r4, 1717.031, 1600.214, 2.6199819367E-4, 1.9068495645E-4, -4.0999584974E-6));
        master.add(new Detection(lhs3250_r5, 92.747, 766.29, 2.6550175597E-4, 1.9715345631E-4, 7.3636597883E-6));
        master.add(new Detection(lhs3250_r6, 1166.08, 1697.654, 3.1457797605E-4, 2.2867155394E-4, -1.7952487491E-6));
        master.add(new Detection(lhs3250_r7, 1104.309, 572.738, 3.2968673826E-4, 2.4974462944E-4, 4.6309601448E-7));
        master.add(new Detection(lhs3250_r8, 188.884, 1208.151, 5.5903768176E-4, 4.0008594369E-4, 1.9804857404E-5));
        master.add(new Detection(lhs3250_r9, 1543.648, 1620.362, 7.9535005879E-4, 5.6743486199E-4, -5.6222275745E-6));
        master.add(new Detection(lhs3250_r10, 24.287, 1419.708, 7.3579607341E-4, 5.393550088E-4, 2.8885298275E-5));
        master.add(new Detection(lhs3250_r11, 1702.301, 1528.485, 0.0016387904529, 0.0010626395195, -1.8610645727E-6));
        master.add(new Detection(lhs3250_r12, 2023.294, 983.161, 0.0022467205129, 0.0014999929505, 3.3615335405E-5));
        master.add(new Detection(lhs3250_r13, 1182.865, 1873.536, 0.0040786305197, 0.0023993167636, -2.5789900942E-5));
        master.add(new Detection(lhs3250_r14, 865.563, 1553.258, 0.0040449791615, 0.0026745368325, 8.6301408335E-5));
        master.add(new Detection(lhs3250_r15, 849.574, 1479.441, 0.0042004254565, 0.00282311873, 6.3012652265E-5));
        master.add(new Detection(lhs3250_r16, 1740.925, 1493.878, 0.0042934856985, 0.0030098090553, -6.41791466E-5));
        master.add(new Detection(lhs3250_r17, 1687.901, 512.529, 0.0040026348583, 0.0029493649008, 1.9280765865E-4));
        master.add(new Detection(lhs3250_r18, 1465.727, 795.503, 0.0049756790947, 0.0035040301478, 3.6441559854E-4));
        master.add(new Detection(lhs3250,     1069.975, 999.132, 0.0020736709635, 0.0015052709717, 3.5703243629E-5));

        // Create slave image:
        StackedImage slave = new StackedImage(new Epoch(2007, 2, 7, 5, 51, 0), new File("2007.02.06/grp1"), 1965, 2045);
        slave.add(new Detection(lhs3250_r1, 1040.481, 1241.737, 8.2998130282E-4, 7.0726696187E-4, -2.9962748071E-6));
        slave.add(new Detection(lhs3250_r2, 633.109, 1096.277, 0.0011094052165, 9.6564030173E-4, 1.932528858E-5));
        slave.add(new Detection(lhs3250_r3, 380.131, 935.229, 0.0011065555153, 9.6214372569E-4, 1.027931324E-5));
        slave.add(new Detection(lhs3250_r4, 1784.53, 139.721, 0.0012809033671, 0.0010632915872, 6.8445654845E-5));
        slave.add(new Detection(lhs3250_r5, 848.007, 1706.776, 0.0013721002568, 0.0011183531056, 4.1081297514E-5));
        slave.add(new Detection(lhs3250_r6, 1846.362, 696.05, 0.0014653405052, 0.0011202412253, 1.0450825674E-5));
        slave.add(new Detection(lhs3250_r7, 720.608, 684.973, 0.0016251184442, 0.0013327779061, -8.3339049227E-6));
        slave.add(new Detection(lhs3250_r8, 1294.59, 1639.412, 0.0022704643632, 0.0019935589586, -8.6339318242E-5));
        slave.add(new Detection(lhs3250_r9, 1794.643, 314.535, 0.0034842996061, 0.0028237135768, 1.3987665198E-4));
        slave.add(new Detection(lhs3250_r10, 1495.116, 1817.0, 0.0032168727048, 0.0029255262935, -2.9808834493E-4));
        slave.add(new Detection(lhs3250_r11, 1712.648, 149.94, 0.0066115023102, 0.0050167138151, -5.715509845E-4));
        slave.add(new Detection(lhs3250,     1153.543, 750.114, 0.0073340435866, 0.0083725838518, -5.2111612051E-4));

        
        slave.computeSlaveToMasterImageTransform(master);
        
        System.out.println("Transformation parameters:");
        System.out.println("a = "+slave.s2m[0]);
        System.out.println("b = "+slave.s2m[1]);
        System.out.println("c = "+slave.s2m[2]);
        System.out.println("d = "+slave.s2m[3]);
        System.out.println("e = "+slave.s2m[4]);
        System.out.println("f = "+slave.s2m[5]);
        
        System.out.println("Ref star 1 coordinates in master = "+
                master.detections.get(0).x + ","+master.detections.get(0).y);
        
        Coordinates2D trans = slave.detections.get(0).transform(slave.s2m);
        
        System.out.println("Ref star 1 coordinates in slave, transformed to master = "+trans.x + ","+trans.y);
        
        
        Detection target = master.getDetection(lhs3250);
        
        int t_x = (int)Math.rint(target.x);
        int t_y = (int)Math.rint(master.height - target.y);
        System.out.println(slave.imageMagickTransformCommand(t_x, t_y));
        
    }
    
}
