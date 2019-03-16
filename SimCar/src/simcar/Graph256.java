/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package simcar;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import simcar.windows.POINT;

/**
 *
 * @author YvesFabienne
 */
public class Graph256 {

static int scan;
static BufferedImage lpvBits;

public static void Triangle( 	int x0, int y0, int x1, int y1, int x2, int y2, int coul)
{
    Graphics2D gr2d = lpvBits.createGraphics();

    Polygon p = new Polygon();
    p.addPoint(x0, y0);
    p.addPoint(x1, y1);
    p.addPoint(x2, y2);
    gr2d.setColor(new Color(coul));
    gr2d.fillPolygon(p);
    gr2d.dispose();
}


public static void Polygone( int coul, POINT[] xye, int np) {

    Graphics2D gr2d = lpvBits.createGraphics();
    Polygon p = new Polygon();
    for ( int i=0; i<np; i++) {
        p.addPoint(xye[i].x, xye[i].y);
    }
    gr2d.setColor(new Color(coul));
    gr2d.fillPolygon(p);
    gr2d.dispose();

}
}
