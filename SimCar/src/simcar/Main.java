/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package simcar;

import java.awt.Frame;

/**
 *
 * @author YvesFabienne
 */
public class Main {

    static IdleThread it;
    static MainFrame mf;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {        
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                mf = new MainFrame();
                mf.setVisible(true);
                mf.setExtendedState(Frame.MAXIMIZED_BOTH);
        it = new IdleThread(mf);
        //it.setPriority(Thread.MIN_PRIORITY);
        it.start();

            }
        });
    }

}
