/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package simcar.windows;

import java.awt.Color;
import java.awt.Font;
import java.awt.Polygon;
import java.awt.image.BufferedImage;

/**
 *
 * @author YvesFabienne
 */
public class WinGDI {

    public static int RGB( int r, int g, int b) {
        return (r<<16)|(g<<8)|(b);
    }
    public static int PALETTERGB( int r,int g,int b) {
        return RGB(r,g,b);
    }

    public static int GetNearestPaletteIndex(int hsyspal, int color) {
        return color;
    }

    public static BufferedImage CreateCompatibleBitmap(int cx, int cy) {
        return new BufferedImage(cx,cy,BufferedImage.TYPE_INT_RGB);
    }

    public static Color CreateSolidBrush(int PALETTERGB) {
        return new Color(PALETTERGB);
    }

    public static BufferedImage SelectObject(HDC hDC_ct, BufferedImage hbm_ct) {
        BufferedImage old_bi = hDC_ct.bi;
        hDC_ct.g2d = hbm_ct.createGraphics();
        hDC_ct.bi = hbm_ct;
        return old_bi;
    }

    public static void SelectObject(HDC hdc, Color col) {
        hdc.g2d.setColor(col);
    }

    public static Font SelectObject(HDC hdc, Font font) {
        Font old_font = hdc.g2d.getFont();
        hdc.g2d.setFont(font);
        return old_font;
    }

    public static void Ellipse(HDC hDC_ct, int left, int top, int right, int bottom) {
        hDC_ct.g2d.fillOval(left, top, right-left, bottom - top);
    }

    public static void Pie(HDC hDC_ct, int left, int top, int right, int bottom, int startAngle, int arcAngle) {
        hDC_ct.g2d.fillArc(left, top, right-left, bottom - top, startAngle, arcAngle);
    }

    public static void Polygon(HDC hDC_ct, POINT[] xy, int nb) {
        Polygon p = new Polygon();
        for ( int i=0; i<nb; i++) {
            p.addPoint(xy[i].x, xy[i].y);
        }
        hDC_ct.g2d.fillPolygon(p);
    }

    public static void SetPixel(HDC hDC_ct, int x, int y, int color) {
        hDC_ct.g2d.setColor(new Color(color));
        hDC_ct.g2d.drawLine(x, y, x, y);
    }

    public static void SetBkColor(HDC dc, int COUL_FOND) {
        dc.g2d.setBackground( new Color(COUL_FOND));
    }

    public static void TextOut(HDC dc, int x_aff, int y_aff, String chaine) {
        dc.g2d.drawString(chaine, x_aff, y_aff);
    }

    public static void Rectangle(HDC dc, int left, int top, int right, int bottom) {
        dc.g2d.fillRect(left, top, right-left, bottom-top);
    }

    public static void SetDIBitsToDevice(
            HDC hdc,
            int XDest,
            int YDest,
            int dwWidth,
            int dwHeight,
            int Xsrc,
            int Ysrc,
            int uStartScan,
            int cScanLines,
            BufferedImage lpvBits) {
        hdc.g2d.drawImage(lpvBits, XDest, YDest, null);
    }

    public static void BitBlt(
            HDC hdcDest,
            int nXDest,
            int nYDest,
            int nWidth,
            int nHeight,
            HDC hdcSrc,
            int nXSrc,
            int nYSrc) {
        hdcDest.g2d.drawImage(hdcSrc.bi, nXDest, nYDest, null);
    }

    public static BufferedImage CreateCompatibleBitmap(
            HDC hdc,
            int nWidth,
            int nHeight
            ) {
        return new BufferedImage(nWidth,nHeight,BufferedImage.TYPE_INT_RGB);
    }
}
