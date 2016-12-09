package projects.kaleidoscopes;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import numeric.geom.dim3.Vector3d;

/**
 * This application is used to generate OBJ models describing 2- and 3-mirror kaleidoscope designs that
 * can be loaded and viewed using e.g. Blender.
 *
 *
 * @author nrowell
 * @version $Id$
 */
public class MakeMirrorDesignModel {
	
	
	
	public static void main(String[] args) throws IOException {
		
		File topLevel = new File("/home/nrowell/Projects/Kaleidoscope/designs");
		
		File mtl = new File(topLevel, "mirror.mtl");
		File obj = new File(topLevel, "mirror.obj");
		
		writeMaterialLibrary(mtl);
		writeSquare(obj);
	}
	
	
	private static void writeSquare(File obj) throws IOException {
		
		Vector3d a = new Vector3d(-1,-1, 0);
		Vector3d b = new Vector3d( 1,-1, 0);
		Vector3d c = new Vector3d(-1, 1, 0);
		Vector3d d = new Vector3d( 1, 1, 0);
		Vector3d n = new Vector3d( 0, 0, 1);
		
		writeMirrorPolygon(a, b, c, d, n, obj);
		
	}
	
	
	
	
	private static void writeMirrorPolygon(Vector3d v1, Vector3d v2, Vector3d v3, Vector3d v4, Vector3d n, File obj) throws IOException {
		
		BufferedWriter out = new BufferedWriter(new FileWriter(obj));
		
		out.write("# Blender v2.76 (sub 0) OBJ File: ''\n");
		out.write("# www.blender.org\n");
		out.write("mtllib mirror2.mtl\n");
		
		// Declare a named object
		out.write("o Plane.001\n");
		// Vertex spatial coordinates
		out.write(String.format("v\t%f\t%f\t%f\n", v1.getX(), v1.getY(), v1.getZ()));
		out.write(String.format("v\t%f\t%f\t%f\n", v2.getX(), v2.getY(), v2.getZ()));
		out.write(String.format("v\t%f\t%f\t%f\n", v3.getX(), v3.getY(), v3.getZ()));
		out.write(String.format("v\t%f\t%f\t%f\n", v4.getX(), v4.getY(), v4.getZ()));
		// Normal vectors
		out.write(String.format("vn\t%f\t%f\t%f\n", n.getX(), n.getY(), n.getZ()));
		
		out.write("usemtl Material.002 \n");
		// Smooth shading disabled
		out.write("s off \n");
		
		// Face declaration. Format is:
		// 
		// 	f v1/vt1/vn1 v2/vt2/vn2 v3/vt3/vn3 ...
		//
		// ...where v1 is the spatial coordinate of vertex1, vt1 is the texture coordinate of vertex 1, and
		// vn1 is the normal vector for vertex 1.
		//
		// Note that the spatial coordinate, normal vector etc indices are derived from the order of the types of
		// vector in the file, i.e. vn1 is the index of the normal vector.
		out.write("f 1//1 2//1 4//1 3//1 \n");
		
		
			
			
			
			
			
			
			
			
			
			
			
		out.close();
		
		
		
		
	}
	
	
	
	
	/**
	 * Writes an OBJ material library file that describes a mirrored surface that can
	 * be used to create mirrors in the 3D model.
	 * 
	 * @param mtl
	 * 	The File to write to.
	 * @throws IOException 
	 */
	private static void writeMaterialLibrary(File mtl) throws IOException {
		
		BufferedWriter out = new BufferedWriter(new FileWriter(mtl));
		
		out.write("# Blender MTL File: 'None' \n");
		out.write("# Material Count: 1 \n");
		out.write("\n");
		out.write("newmtl Material.002 \n");
		
		out.write("Ni 1.000000 \n");
		
		// Ambient colour
		out.write("Ka 1.000000 0.233606 0.110362 \n");
		
		// Diffuse colour
		out.write("Kd 0.640000 0.640000 0.640000 \n");
		
		// Specular colour
		out.write("Ks 0.500000 0.500000 0.500000 \n");
		// Specular exponent [0-1000]
		out.write("Ns 96.078431 \n");
		
		// Emmissivity
		out.write("Ke 0.000000 0.000000 0.000000 \n");
		
		// Refers to transparency, i.e. 'dissolved'; 1.0 == fully opaque
		out.write("d 1.000000 \n");
		
		// This is the 'Illumination Model'. The values are:
		// 0. Color on and Ambient off
		// 1. Color on and Ambient on
		// 2. Highlight on
		// 3. Reflection on and Ray trace on
		// 4. Transparency: Glass on, Reflection: Ray trace on
		// 5. Reflection: Fresnel on and Ray trace on
		// 6. Transparency: Refraction on, Reflection: Fresnel off and Ray trace on
		// 7. Transparency: Refraction on, Reflection: Fresnel on and Ray trace on
		// 8. Reflection on and Ray trace off
		// 9. Transparency: Glass on, Reflection: Ray trace off
		// 10. Casts shadows onto invisible surfaces
		out.write("illum 3 \n");

		out.close();
	}
	
	
	
}
