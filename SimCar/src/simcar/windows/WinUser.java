/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package simcar.windows;

import java.awt.Color;
import java.awt.Component;
import java.awt.Window;
import javax.swing.JOptionPane;
import simcar.struct.RECT;

/**
 *
 * @author YvesFabienne
 */
public class WinUser {

/*
 * MessageBox() Flags
 */
    public static final int MB_OK =                      0x00000000;
    public static final int MB_OKCANCEL =                  0x00000001;
    public static final int MB_ABORTRETRYIGNORE =        0x00000002;
    public static final int MB_YESNOCANCEL =             0x00000003;
    public static final int MB_YESNO =                   0x00000004;
    public static final int MB_RETRYCANCEL =             0x00000005;

    public static void MessageBox( Component hWnd, String lpText, String lpCaption, int uType) {
        JOptionPane.showMessageDialog(hWnd, lpText, lpCaption, uType);
    }

    public static void GetClientRect( Component jf, RECT r) {
        r.left = 0;
        r.top = 0;
        r.right = jf.getWidth();
        r.bottom = jf.getHeight();
    }

    public static void FillRect(HDC hDC_ct, RECT ct_Rect, Color hb_Fond) {
        hDC_ct.g2d.setColor(hb_Fond);
        hDC_ct.g2d.fillRect(
                Math.min(ct_Rect.left,ct_Rect.right),
                Math.min(ct_Rect.top,ct_Rect.bottom),
                Math.abs(ct_Rect.left - ct_Rect.right), Math.abs(ct_Rect.top - ct_Rect.bottom));
    }
}
