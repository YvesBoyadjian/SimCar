/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package simcar.voiture;

import simcar.struct.Double3D;
import simcar.windows.WinUser;

/**
 *
 * @author YvesFabienne
 */
public class Roue {

    public RoueEnum position;
    public Chassis cha;
    public boolean glis;	// Flag de glissement de la roue.
    public Double3D  coor = new Double3D(); // Coordonnées du point de contact de la roue ds (G,I,J,K).
    public double v_rot;	// Norme de la vitesse relative à I,J,K du pneu au point de contact de la roue au sol = m/s
    public double angle;	// Angle que fait la roue par rapport à l'axe de la voiture.


    public Roue(RoueEnum positionArg) {
        position = positionArg;
    }
/***********************************************************
FONCTION: CALCU_ROUE(...)
	Calcule la force exercee sur la roue.

		Sorties : forg = force exterieure appliquee sur la roue dans (I,J,K)
						calcu_roue = force exercee sur l'axe de la roue
***********************************************************/
    public double calcu_roue( Double3D forg, double old_roue, double dt) throws RoueNeTouchePasSol {	// Calcule alpha pour une roue.

	Double3D vit_glis = new Double3D(), vit_pneu = new Double3D(), vit_sol = new Double3D(), forc = new Double3D();
	double alpha, rap_glis, rap_2, glis_norm, pneu_norm, sol_norm;
	double buf=1.0, old_buf, dfz, old_fz, vz, fmax;
	double seuil_glis=0.2;
	int i=0;

	vz =  (coor.z - old_roue)/dt;
	//_____________________________________Vitesse de la bdr/sol dans ( I,J,K ).
	vit_glis.x = cha.vg.x + cha.om.y*coor.z - cha.om.z*coor.y + v_rot*Math.sin(angle);
	vit_glis.y = cha.vg.y + cha.om.z*coor.x - cha.om.x*coor.z - v_rot*Math.cos(angle);
	vit_glis.z = cha.vg.z + cha.om.x*coor.y - cha.om.y*coor.x + vz;

	//_________________________________Vitesse de la bdr/sol dans ( i,j,k ).
	cha.G_O( vit_glis, vit_glis);

	//__________________Vitesse absolue du point de contact roue/sol dans ( I,J,K ).
	vit_sol.x = cha.vg.x + cha.om.y*coor.z - cha.om.z*coor.y;
	vit_sol.y = cha.vg.y + cha.om.z*coor.x - cha.om.x*coor.z;
	vit_sol.z = cha.vg.z + cha.om.x*coor.y - cha.om.y*coor.x + vz;

	//_________________________________Vitesse absolue de la roue dans ( i,j,k ).
	cha.G_O( vit_sol, vit_sol);

	vit_pneu.x = vit_sol.x - vit_glis.x;
	vit_pneu.y = vit_sol.y - vit_glis.y;

	glis_norm = Math.sqrt( vit_glis.x*vit_glis.x + vit_glis.y*vit_glis.y);	// Vitesse de glissement
	sol_norm = Math.sqrt( vit_sol.x*vit_sol.x + vit_sol.y*vit_sol.y);		// Vitesse du point de contact/sol
	pneu_norm = Math.sqrt( vit_pneu.x*vit_pneu.x + vit_pneu.y*vit_pneu.y);// Vitesse du point de contact/pneu

	rap_glis = glis_norm /Math.max(pneu_norm+sol_norm, 0.02); // Rapport de glissement, compris entre 0 et 1
	rap_2 = glis_norm/Math.max(pneu_norm,0.001);

	// Coefficient d'adhérence
	alpha = Math.min(1.0, 20.0*rap_glis);

	if ( rap_2 > seuil_glis)	// Glissement
	{
		glis = true;
//		alpha = Math.min( alpha, 0.8+seuil_glis/rap_2);
    }
	else
		glis = false;

	if ( (forc.z= forg.z/cha.rotMatrix.get(2, 2)) > 0.01)
	{
		old_buf = forg.z; old_fz = 0.0;	// Force d'appui verticale nulle
		for ( ; Math.abs( buf)> 1e-6; forc.z += dfz )
		{
			fmax =  Math.max(forc.z,0.0)*Math.max(cha.coef_sec + ((position==RoueEnum.ARD || position==RoueEnum.ARG) ? 0.2:0.0)/*forc.z/20000.0*/,0.01);//*alpha;

                        double glis_norm_x_y = Math.sqrt(vit_glis.x*vit_glis.x+vit_glis.y*vit_glis.y);
                        glis = ( glis_norm_x_y*10.0 > 1.0);
                        if ( glis_norm_x_y > 0.0) {
                            double fglis = fmax /(1.0 + 1.0/(glis_norm_x_y*10.0));//Math.min(1, glis_norm_x_y*10.0);
                            forc.x = - fglis * vit_glis.x/glis_norm_x_y;///Math.max(glis_norm, 0.0001);
                            forc.y = - fglis * vit_glis.y/glis_norm_x_y;///Math.max(glis_norm, 0.0001);
                        }
                        else {
                            forc.x = 0.0;
                            forc.y = 0.0;
                        }
			buf = forg.z - cha.rotMatrix.get(2, 0)*forc.x - cha.rotMatrix.get(2, 1)*forc.y - cha.rotMatrix.get(2, 2)*forc.z;
			dfz = - buf *( forc.z - old_fz)/( buf - old_buf);
			old_fz = forc.z;
			old_buf = buf;
			i++;
			if ( i== 100)  {
                            throw new RoueNeTouchePasSol();
				//WinUser.MessageBox( null, "i = 100", "Simcar", WinUser.MB_OK);
				//break;
			}
		}
	}
	else {
		forg.x = forg.y = forg.z = 0.0;
		return 0.0;
	}
	//_____________________________________________Force dans ( i, j, k )
	if ( forc.z < 0.0)
		forc.z = 0.0;
	cha.O_G( forc, forg);

	return( forg.y*Math.cos(angle) - forg.x*Math.sin(angle));
    }
}
