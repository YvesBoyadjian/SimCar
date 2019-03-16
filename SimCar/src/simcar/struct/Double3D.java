/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package simcar.struct;

/**
 *
 * @author YvesFabienne
 */
public class Double3D {

	public double x;
	public double y;
	public double z;

        public double norm() {
            return Math.sqrt(x*x+y*y+z*z);
        }
}
