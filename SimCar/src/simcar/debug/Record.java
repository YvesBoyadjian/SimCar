/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package simcar.debug;

/**
 *
 * @author YvesFabienne
 */
public class Record {
    double time_sec;
    double rot_diff;
    double braq;

    public Record(double time_secArg, double rot_diffArg,double braqArg) {
        time_sec = time_secArg;
        rot_diff = rot_diffArg;
        braq = braqArg;
    }
}
