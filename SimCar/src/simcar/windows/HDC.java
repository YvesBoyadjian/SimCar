/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package simcar.windows;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 *
 * @author YvesFabienne
 */
public class HDC {
    public Graphics2D g2d;
    public BufferedImage bi;

    public HDC(Graphics2D gr2d) {
        g2d = gr2d;
    }

    public HDC() {
        
    }
}
