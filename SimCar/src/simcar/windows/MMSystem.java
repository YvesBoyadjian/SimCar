/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package simcar.windows;

/**
 *
 * @author YvesFabienne
 */
public class MMSystem {

    public static final int JOYERR_BASE = 160;

/* joystick ID constants */
    public static final int JOYSTICKID1 = 0;
    public static final int JOYSTICKID2 = 1;

    public static final int JOYERR_NOERROR = 0;                  /* no error */
    public static final int JOYERR_PARMS = (JOYERR_BASE+5);      /* bad parameters */
    public static final int JOYERR_NOCANDO =(JOYERR_BASE+6);     /* request not completed */
    public static final int JOYERR_UNPLUGGED =(JOYERR_BASE+7);   /* joystick is unplugged */


    public static int joyGetPos( int joyID, JOYINFO ji) {
        // TODO
        return JOYERR_UNPLUGGED;//JOYERR_NOERROR;
    }
}
