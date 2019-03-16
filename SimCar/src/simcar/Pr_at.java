/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package simcar;

import simcar.windows.HDC;
import simcar.windows.WinGDI;

/**
 *
 * @author YvesFabienne
 */
public class Pr_at {

public static void pr_at(HDC dc, int x_aff, int y_aff, String chaine)
//	Entrées :
//				chaine	: Texte à afficher
//	Fonction : Affiche la chaine à l'écran avec espacement constant.
{
    WinGDI.TextOut( dc, x_aff, y_aff, chaine);
}
public static void fpr_at(HDC dc, int x_aff, int y_aff, double nombre)	{

    String buffer = Double.toString(nombre);
    pr_at( dc, x_aff, y_aff, buffer);
}
public static void fpr_at(HDC dc, int x_aff, int y_aff, int nombre)	{

    String buffer = Integer.toString(nombre);
    pr_at( dc, x_aff, y_aff, buffer);
}
}
