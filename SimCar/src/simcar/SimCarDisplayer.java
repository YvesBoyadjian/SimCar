/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package simcar;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.text.NumberFormat;
import javax.swing.JComponent;
import simcar.voiture.RoueEnum;
import simcar.windows.WinGDI;

/**
 *
 * @author YvesFabienne
 */
public class SimCarDisplayer {

    static SimCarDisplayer displayer;

    static SimCarDisplayer getInstance() {
        return displayer;
    }

    static void draw(Graphics g, JComponent rootPane) {
        g.drawImage(displayer.screen, 0, 0, rootPane);
    }

    void drawGlissement(RoueEnum no, boolean glis) {

        Graphics2D gr2d = screen.createGraphics();

        gr2d.setColor(new Color(WinGDI.PALETTERGB(Simcar.FOND, Simcar.FOND, Simcar.FOND)));

	if (glis_avg) gr2d.drawString(  "*",0, Simcar.TheRect.bottom); else gr2d.drawString(  "_",0,Simcar.TheRect.bottom);
	if (glis_avd) gr2d.drawString(  "*", 10, Simcar.TheRect.bottom);else gr2d.drawString(  "_",10,Simcar.TheRect.bottom);
	if (glis_arg) gr2d.drawString(  "*", 0, Simcar.TheRect.bottom+20);else gr2d.drawString(  "_",0,Simcar.TheRect.bottom+20);
	if (glis_ard) gr2d.drawString(  "*", 10, Simcar.TheRect.bottom+20);else gr2d.drawString(  "_",10,Simcar.TheRect.bottom+20);

        switch(no) {
            case AVG:
                glis_avg = glis; break;
            case AVD:
                glis_avd = glis; break;
            case ARG:
                glis_arg = glis; break;
            case ARD:
                glis_ard = glis; break;
        }

        gr2d.setColor(Color.BLACK);

	if (glis_avg) gr2d.drawString(  "*", 0, Simcar.TheRect.bottom); else gr2d.drawString(  "_",0,Simcar.TheRect.bottom);
	if (glis_avd) gr2d.drawString(  "*", 10, Simcar.TheRect.bottom);else gr2d.drawString(  "_",10,Simcar.TheRect.bottom);
	if (glis_arg) gr2d.drawString(  "*", 0, Simcar.TheRect.bottom+20);else gr2d.drawString(  "_",0,Simcar.TheRect.bottom+20);
	if (glis_ard) gr2d.drawString(  "*", 10, Simcar.TheRect.bottom+20);else gr2d.drawString(  "_",10,Simcar.TheRect.bottom+20);

    }
    void drawRapport(int rapportArg) {

        Graphics2D gr2d = screen.createGraphics();
        gr2d.setFont(Simcar.hf_Vitesse);

        gr2d.setColor(new Color(WinGDI.PALETTERGB(Simcar.FOND, Simcar.FOND, Simcar.FOND)));
        gr2d.drawString(Integer.toString(rapport), (Simcar.TheRect.right*4)/5, (Simcar.TheRect.bottom*11)/11);
        rapport = rapportArg;

        gr2d.setColor(Color.black);
        gr2d.drawString(Integer.toString(rapport), (Simcar.TheRect.right*4)/5, (Simcar.TheRect.bottom*11)/11);
        gr2d.dispose();
    }

    void drawFrequency(int frequencyArg) {

        Graphics2D gr2d = screen.createGraphics();

	//WinGDI.SelectObject(Simcar.hDCEcr, Simcar.hf_old);

        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumIntegerDigits(6);

        gr2d.setColor(new Color(WinGDI.PALETTERGB(Simcar.FOND, Simcar.FOND, Simcar.FOND)));

	//Pr_at.pr_at( Simcar.hDCEcr, 0,Simcar.TheRect.bottom+10," Fr = "+nf.format(frequency)+" Hz" );
        gr2d.drawString(" Fr = "+nf.format(frequency)+" Hz", 0, Simcar.TheRect.bottom+10);

        frequency = frequencyArg;

        gr2d.setColor(Color.BLACK);

        //Pr_at.pr_at( Simcar.hDCEcr, 0,Simcar.TheRect.bottom+10," Fr = "+nf.format(frequencyArg)+" Hz" );
        gr2d.drawString(" Fr = "+nf.format(frequency)+" Hz", 0, Simcar.TheRect.bottom+10);
        gr2d.dispose();
    }

    void drawVitRoue(double vit, RoueEnum roue) {

        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(3);
        nf.setMinimumFractionDigits(3);
        nf.setMinimumIntegerDigits(3);
        Simcar.hDCEcr.g2d.setColor(new Color(WinGDI.PALETTERGB(Simcar.FOND, Simcar.FOND, Simcar.FOND)));

	Pr_at.pr_at( Simcar.hDCEcr,
                200 + ((roue == RoueEnum.AVD || roue == RoueEnum.ARD)?50:0),
                Simcar.TheRect.bottom+10 + ((roue == RoueEnum.ARD || roue == RoueEnum.ARG)?20:0),
                nf.format(getVit(roue))
                );
        switch( roue) {
            case AVG:
                avg = vit;
                break;
            case AVD:
                avd = vit;
                break;
            case ARG:
                arg = vit;
                break;
            case ARD:
                ard = vit;
        }

        Simcar.hDCEcr.g2d.setColor(Color.BLACK);

	Pr_at.pr_at( Simcar.hDCEcr,
                200 + ((roue == RoueEnum.AVD || roue == RoueEnum.ARD)?50:0),
                Simcar.TheRect.bottom+10 + ((roue == RoueEnum.ARD || roue == RoueEnum.ARG)?20:0),
                nf.format(getVit(roue))
                );
    }
/*
    void drawArd(double ardArg) {

        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(3);
        nf.setMinimumFractionDigits(3);
        nf.setMinimumIntegerDigits(3);
        Simcar.hDCEcr.g2d.setColor(new Color(WinGDI.PALETTERGB(Simcar.FOND, Simcar.FOND, Simcar.FOND)));

	Pr_at.pr_at( Simcar.hDCEcr, 200, Simcar.TheRect.bottom+10, " ard=" + nf.format(getArd()));
        setArd(ardArg);

        Simcar.hDCEcr.g2d.setColor(Color.BLACK);

	Pr_at.pr_at( Simcar.hDCEcr, 200, Simcar.TheRect.bottom+10, " ard=" + nf.format(ardArg));
    }

    void drawArg( double argArg) {

        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(3);
        nf.setMinimumFractionDigits(3);
        nf.setMinimumIntegerDigits(3);
        Simcar.hDCEcr.g2d.setColor(new Color(WinGDI.PALETTERGB(Simcar.FOND, Simcar.FOND, Simcar.FOND)));

	Pr_at.pr_at( Simcar.hDCEcr, 200, Simcar.TheRect.bottom+20, " arg=" + nf.format(arg));
        arg = argArg;

        Simcar.hDCEcr.g2d.setColor(Color.BLACK);

	Pr_at.pr_at( Simcar.hDCEcr, 200, Simcar.TheRect.bottom+20, " arg=" + nf.format(argArg));
    }
*/
    BufferedImage screen;
    int rapport;
    int frequency;
    double ard,arg,avd,avg;
    boolean glis_ard,glis_arg,glis_avd,glis_avg;

    private double getVit(RoueEnum roue) {
        switch(roue) {
            case AVD:
                return avd;
            case AVG:
                return avg;
            case ARD:
                return ard;
            case ARG:
                return arg;
        }
        return 0.0;
    }
    private synchronized double getArd() {
        return ard;
    }

    private synchronized void setArd(double ardArg) {
        ard = ardArg;
    }

    private SimCarDisplayer(int width, int height, Component frame) {
            screen = new BufferedImage(width, height,BufferedImage.TYPE_INT_RGB);
            Simcar.paint(this, frame);
    }

    static void checkDimensions(int width, int height, Component frame) {
        if (displayer == null || displayer.getHeight()!= height || displayer.getWidth() != width) {
            displayer = new SimCarDisplayer(width, height, frame);
        }
    }

    private int getHeight() {
        return screen.getHeight();
    }

    private int getWidth() {
        return screen.getWidth();
    }

}
