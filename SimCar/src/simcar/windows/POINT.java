/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package simcar.windows;

/**
 *
 * @author YvesFabienne
 */
public class POINT {

    public static POINT[] alloc(int nb) {
        POINT[] array = new POINT[nb];
        for ( int i=0; i<nb;i++) {
            array[i] = new POINT();
        }
        return array;
    }

    public int  x;
    public int  y;
}
