/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package simcar;

import java.awt.event.KeyEvent;
import javax.swing.JOptionPane;
import simcar.voiture.ArriveeAtteinte;
import simcar.voiture.RoueNeTouchePasSol;

/**
 *
 * @author YvesFabienne
 */
public class IdleThread extends Thread {
    MainFrame mjf;
    long start_time;

    @Override
    public void run() {
        start_time = System.currentTimeMillis()/1000;
        try {
        while( true) {
            synchronized(mjf) {
            if ( Graph256.lpvBits != null) {
                    mjf.simcar.IdleProc();
                        if ( mjf.simcar.keyb[KeyEvent.VK_ESCAPE]) {
                            mjf.setVisible(false);
                            break;
                        }
            }
            }
            yield();
            //mjf.repaint();
        }
        } catch( RoueNeTouchePasSol e) {
            JOptionPane.showMessageDialog(mjf, "perdu");
        } catch( ArriveeAtteinte a) {
            long sec = System.currentTimeMillis()/1000 - start_time;
            String message = "Bravo, vous ètes arrivés, vous avez mis "+sec+" secondes.";
            JOptionPane.showMessageDialog(mjf, message);
        }
    }

    public IdleThread(MainFrame mjfArg) {
        mjf = mjfArg;
    }
}
