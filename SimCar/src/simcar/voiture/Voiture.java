/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/******************************************************************

	VOITURE.CPP	Implémentation de la classe 'Voiture' et 'par'

	Version utilisant le type 'DOUBLE' ( 64 bits)

	Sources: Sciences & Vie n° 765, 776, 785, 820

******************************************************************/

package simcar.voiture;

import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import simcar.Simcar;
import simcar.debug.Record;
import simcar.struct.Double3D;

/**
 *
 * @author YvesFabienne
 */
public class Voiture extends Chassis {

    public static double COEF_FREINS = 12.0;

    public	Roue	avd = new Roue(RoueEnum.AVD), avg = new Roue(RoueEnum.AVG), ard = new Roue(RoueEnum.ARD), arg = new Roue(RoueEnum.ARG);

    public Vector<Record> tape = new Vector<Record>();
    
    public void Nouveau() {

	coef_mot1 = (cmot_max - cmot_ral)/(reg_cmot_max - reg_ral);
	coef_mot2 = (cmot - cmot_max)/( reg_max - reg_cmot_max);
	coef_mot3 = - cmot /(reg_max_vide - reg_max);
	empat_av = empat*poids_ar/( poids_av+poids_ar);
	empat_ar = - empat*poids_av/( poids_av+poids_ar);
	poids = poids_av+poids_ar;
	elong_av = alti_g + Simcar.g/2.0*poids_av*flexi_av;
	elong_ar = alti_g + Simcar.g/2.0*poids_ar*flexi_ar;

	rot_mot = reg_ral; // Moteur au ralenti
	rapport = 0;
	embr = 1.0;
	rot_pont = rot_diff = 0.0;
	accel = 0.0;
	freins = 0.0;
	angle_vol = 0.0;
	braq = 0.0;
	cg.x = 0.0;
	cg.z= alti_g+0.5;
	cg.y = 5.0;
	//rot01 = rot02 =rot10 =rot20 =rot12 =rot21 =0.0;
	//rot00= rot11= rot22= 1.0;
        rotMatrix.set(0, 0, 1.0);
        rotMatrix.set(1, 1, 1.0);
        rotMatrix.set(2, 2, 1.0);
        

	vit_g.x= vit_g.y = vit_g.z= 0.0;

	avg.coor.x = arg.coor.x = - voie/2.0;
	avd.coor.x = ard.coor.x = voie/2.0;
	avg.coor.y = avd.coor.y = empat_av;
	arg.coor.y = ard.coor.y = empat_ar;
	avd.coor.z = avg.coor.z = ard.coor.z = arg.coor.z = - alti_g;
	avd.glis = avg.glis = ard.glis = arg.glis = false;
	avd.v_rot = avg.v_rot = ard.v_rot = arg.v_rot = 0.0;
	ard.angle = 0;//0.005;
	arg.angle = 0;//-0.005;
	avg.cha = avd.cha = arg.cha = ard.cha = this;

	sigma.x = sigma.y = sigma.z = 0.0;
        try {
            setNV(1.0);
        } catch (ArriveeAtteinte ex) {
            Logger.getLogger(Voiture.class.getName()).log(Level.SEVERE, null, ex);
        }

	td = Simcar.CHRONO();
    }

/********************************************************
FONCTION: CALCU_FORCE (dt)
	Calcule les forces exercées sur la voiture dans le repère
	(G,I,J,K) et itère la vitesse.
********************************************************/
    public void calcu_force(double dt) throws RoueNeTouchePasSol,ArriveeAtteinte { // Itère la vitesse en fonction de la force motrice.

	double	rot00 = rotMatrix.get(0,0), rot11 = rotMatrix.get(1,1), rot22 = rotMatrix.get(2,2);
	double	rot01 = rotMatrix.get(0,1), rot02 = rotMatrix.get(0,2), rot10 = rotMatrix.get(1,0), rot12 = rotMatrix.get(1,2), rot20 = rotMatrix.get(2,0), rot21 = rotMatrix.get(2,1);
        
	double force_fr, force_roues, force_ga, force_dr, dt2;
	//_________Altitude du point de contact des 4 roues dans (O,i,j,k)
	double zavd, zavg, zard, zarg;	// Hauteurs absolues des roues
	double sx, sy, sz; //_______________Vecteur moment des forces au point G
	double old_avd, old_avg, old_ard, old_arg;
	double k0, k1, k2, k3, y0;

	Double3D	roavga = new Double3D(), roavdr = new Double3D();		// Force exercée sur les roues avant dans (I,J,K)
	Double3D	roarga = new Double3D(), roardr = new Double3D();		// Force exercée sur les roues arrières
	Double3D	centre = new Double3D(), roue = new Double3D();
	double		frein_max;
	int		no, i;

		centre.x = cg.x;
		centre.y = cg.y;
		centre.z = cg.z;
		setNV( calcu_s( centre));
		G_O( avg.coor, roue);
		roue.x += cg.x;
		roue.y += cg.y;
		roue.z += cg.z;
		zavg = calcu_z( calcu_s( roue));
		G_O( avd.coor, roue);
		roue.x += cg.x;
		roue.y += cg.y;
		roue.z += cg.z;
		zavd = calcu_z( calcu_s( roue));
		G_O( arg.coor, roue);
		roue.x += cg.x;
		roue.y += cg.y;
		roue.z += cg.z;
		zarg = calcu_z( calcu_s( roue));
		G_O( ard.coor, roue);
		roue.x += cg.x;
		roue.y += cg.y;
		roue.z += cg.z;
		zard = calcu_z( calcu_s( roue));


//_______________________________Coordonnées des 4 roues dans (G,I,J,K).
if ( rot22 > Simcar.FLT_EPSILON) {
	//__________Anciennes hauteurs des roues dans (G,I,J,K).
	old_avd = avd.coor.z;
	old_avg = avg.coor.z;
	old_arg = arg.coor.z;
	old_ard = ard.coor.z;
	// Nouvelles hauteurs des roues dans ( G,I,J,K).
	avd.coor.z = ( zavd - cg.z - rot02*avd.coor.x - rot12*empat_av)/rot22;
	avg.coor.z = ( zavg - cg.z - rot02*avg.coor.x - rot12*empat_av)/rot22;
	arg.coor.z = ( zarg - cg.z - rot02*arg.coor.x - rot12*empat_ar)/rot22;
	ard.coor.z = ( zard - cg.z - rot02*ard.coor.x - rot12*empat_ar)/rot22;

//________________________Calcul du vecteur vitesse de G dans (I,J,K)
	vg.x = rot00*vit_g.x+rot01*vit_g.y+rot02*vit_g.z;
	vg.y = rot10*vit_g.x+rot11*vit_g.y+rot12*vit_g.z;
	vg.z = rot20*vit_g.x+rot21*vit_g.y+rot22*vit_g.z;

	om.x = sigma.x/mx;
	om.y = sigma.y/my;
	om.z = sigma.z/mz;

//___________________________________Calcul de l'angle de braquage de  direction.
	braq = braq_max*angle_vol;
        if ( braq != 0.0) {
            double a = empat;
            double b = a/Math.tan(braq);
            avd.angle = Math.atan(a/(b+voie/2.0));
            avg.angle = Math.atan(a/(b-voie/2.0));
        }
        else {
            avd.angle = 0.0;
            avg.angle = 0.0;
        }
//_______________________________________________Force des freins.
	force_fr = freins* poids* COEF_FREINS;
	force_fr += res_roul; // Résistance au roulement.

	dt2 = dt/1.0;
	no = 1;
//___________________________________________________________Roues Avant.
	if ( avd.coor.z < - elong_av)	// Cas où la roue droite quitte le sol
	{
		avd.coor.z = /*old_avd =*/ - elong_av;
		roavdr.x = roavdr.y = roavdr.z = 0.0;
		avd.glis = false;
	}
	else
		roavdr.z = ( elong_av + avd.coor.z)/flexi_av
					+( avd.coor.z - old_avd)/dt*amort_av
					+ ( avd.coor.z - avg.coor.z)/barre_av;

	if ( avg.coor.z < -elong_av) // Cas où la roue gauche quitte le sol
	{
		avg.coor.z = /*old_avg =*/ - elong_av;
		roavga.x = roavga.y = roavga.z = 0.0;
		avg.glis = false;
	}
	else
		// Forces exercees selon K.
		roavga.z = ( elong_av + avg.coor.z)/flexi_av
					+ ( avg.coor.z - old_avg)/dt*amort_av
					+ (  avg.coor.z - avd.coor.z)/barre_av;

	// Force maxi exercee par les freins avant.
	frein_max = force_fr*rep_frein/2.0;

	for ( i=0; i<no; i++)
	{
		avg.v_rot = rayon * (rot_pont + rot_diff);
		avd.v_rot = rayon * (rot_pont - rot_diff);
		force_ga = avg.calcu_roue(roavga, old_avg, dt) + calcu_fr( frein_max, avg.v_rot);
		force_dr = avd.calcu_roue(roavdr, old_avd, dt) + calcu_fr( frein_max, avd.v_rot);
		force_roues = force_ga + force_dr;

 //_________________________________________Variation de régime moteur
		calcu_mot( dt2, - force_roues, rot_mot);

		if (rapport != 0)
			rot_pont = rot_mot*rap_boite[rapport+1];
		else
			rot_pont -= dt2*force_roues*rayon/jpont;
		rot_pont *= 1.0 - 5.0e-3*dt2;

		rot_diff -= dt2*( force_ga - force_dr)*rayon/(jpont/10);
		rot_diff *= 1.0 - 1.0e-2*dt2;
	}

	// Force maxi des freins.
	frein_max = force_fr*( 1.0 - rep_frein)/2.0;

//________________________________________________________Roue Arrière Gauche.
	if ( arg.coor.z < - elong_ar)
	{
		arg.coor.z = /*old_arg =*/ - elong_ar;
		roarga.x = roarga.y = roarga.z = 0.0;
		arg.glis = false;
	}
	else
	{
		// Force exercée selon K.
		roarga.z = ( elong_ar + arg.coor.z)/flexi_ar
					+ ( arg.coor.z - old_arg)/dt*amort_ar
					+ ( arg.coor.z - ard.coor.z)/barre_ar;
/*
		y0 = arg.v_rot;
		k0 = - dt*( arg.calcu_roue(roarga, old_arg, dt) + calcu_fr(frein_max, y0))*rayon*rayon/jpont;
		arg.v_rot += k0/2.0;
		k1 = - dt*( arg.calcu_roue(roarga, old_arg, dt) + calcu_fr(frein_max, arg.v_rot))*rayon*rayon/jpont;
		arg.v_rot = y0 + k1/2.0;
		k2 = - dt*( arg.calcu_roue(roarga, old_arg, dt) + calcu_fr(frein_max, arg.v_rot))*rayon*rayon/jpont;
		arg.v_rot = y0 + k2;
		k3 = - dt*( arg.calcu_roue(roarga, old_arg, dt) + calcu_fr(frein_max, arg.v_rot))*rayon*rayon/jpont;

		arg.v_rot = y0 +( k0 +2.0*(k1+k2)+k3)/6.0;
 *
 */
                arg.v_rot -= dt*( arg.calcu_roue(roarga, old_arg, dt) + calcu_fr(frein_max, arg.v_rot))*rayon*rayon/jpont;
	}

//_________________________________________________________Roue Arrière Droite.
	if ( ard.coor.z < - elong_ar)
	{	// La suspension est à son élongation maximum
		ard.coor.z = /*old_ard =*/ - elong_ar;
		roardr.x = roardr.y = roardr.z = 0.0;
		ard.glis = false;
	}
	else
	{
		// Force exercée selon K.
		roardr.z = ( elong_ar + ard.coor.z)/flexi_ar
					+ ( ard.coor.z - old_ard)/dt*amort_ar
					+ ( ard.coor.z - arg.coor.z)/barre_ar;
/*
		y0 = ard.v_rot;
		k0 = -dt*( ard.calcu_roue( roardr, old_ard, dt) + calcu_fr( frein_max, ard.v_rot))*rayon*rayon/jpont;
		ard.v_rot += k0/2.0;
		k1 = -dt*( ard.calcu_roue( roardr, old_ard, dt) + calcu_fr( frein_max, ard.v_rot))*rayon*rayon/jpont;
		ard.v_rot = y0 +k1/2.0;
		k2 = -dt*( ard.calcu_roue( roardr, old_ard, dt) + calcu_fr( frein_max, ard.v_rot))*rayon*rayon/jpont;
		ard.v_rot = y0 + k2;
		k3 = -dt*( ard.calcu_roue( roardr, old_ard, dt) + calcu_fr( frein_max, ard.v_rot))*rayon*rayon/jpont;

		ard.v_rot = y0 +( k0 +2.0*(k1+k2)+k3)/6.0;
 *
 */
                ard.v_rot -= dt*( ard.calcu_roue( roardr, old_ard, dt) + calcu_fr( frein_max, ard.v_rot))*rayon*rayon/jpont;
	}	// Fin du cas ou la suspension n'est pas à son élongation maxi.


}	// Fin du cas ou la voiture n'est pas renversée.

	else
	{	// Cas où la voiture est renversée
		roavdr.x = roavdr.y = roavdr.z = 0.0;
		roavga.x = roavga.y = roavga.z = 0.0;
		roardr.x = roardr.y = roardr.z = 0.0;
		roarga.x = roarga.y = roarga.z = 0.0;
	}

	sx = roavdr.z * avd.coor.y + roavga.z * avg.coor.y + roarga.z * arg.coor.y + roardr.z * ard.coor.y
			-  roavdr.y * avd.coor.z - roavga.y * avg.coor.z - roardr.y * ard.coor.z - roarga.y * arg.coor.z;

	sy = roavdr.x*avd.coor.z + roavga.x*avg.coor.z + roardr.x*ard.coor.z + roarga.x*arg.coor.z
			 - roavdr.z*avd.coor.x - roavga.z*avg.coor.x - roardr.z*ard.coor.x - roarga.z*arg.coor.x;

	sz = voie/2.0*( roavdr.y + roardr.y - roavga.y - roarga.y)
	 		- (roavdr.x+roavga.x)*empat_av - (roardr.x+roarga.x)*empat_ar;

	sigma.x += dt*sx;	// Rotation selon l'axe (G,I)
	sigma.y += dt*sy;	// Rotation selon autre axe
	sigma.z += dt*sz;	// Rotation selon l'axe (O,k)

	//sigma.x *= 1.0 - 1.0e-2*dt; // Constante de temps d'amortissement de 100s
	//sigma.y *= 1.0 - 5.0e-2*dt; // Constante de temps d'amortissement de 20s
	//sigma.z *= 1.0 - 1.0e-2*dt;

//________________________________________________Forces selon K ( verticales)
	vg.z += dt*( roavdr.z+ roavga.z+ roardr.z+ roarga.z - vg.z*Math.abs(vg.z)*trainee*3.0)/poids;
//_________________________________________________Forces selon J
	vg.y += dt* ( roavdr.y + roavga.y + roardr.y + roarga.y - vg.y*Math.abs(vg.y)*trainee) / poids;
//_________________________________________________Forces selon I ( latérales)
	vg.x += dt* ( roavdr.x + roavga.x + roardr.x + roarga.x - vg.x*Math.abs(vg.x)*trainee*3.0) / poids;

//__________________________________Calcul du vecteur vitesse de G dans (i,j,k)
	vit_g.x = rot00*vg.x+rot10*vg.y+rot20*vg.z;
	vit_g.y = rot01*vg.x+rot11*vg.y+rot21*vg.z;
	vit_g.z = rot02*vg.x+rot12*vg.y+rot22*vg.z;

//_________________________________________________Force de la pesanteur
	vit_g.z += - dt*Simcar.g;

	Calcu_rot(dt);

	om.x =rotat[0][0]*sigma.x+rotat[1][0]*sigma.y+rotat[2][0]*sigma.z;
	om.y =rotat[0][1]*sigma.x+rotat[1][1]*sigma.y+rotat[2][1]*sigma.z;
	om.z =rotat[0][2]*sigma.x+rotat[1][2]*sigma.y+rotat[2][2]*sigma.z;
	sigma.x = om.x;
	sigma.y = om.y;
	sigma.z = om.z;

	cg.x += dt*vit_g.x;
	cg.y += dt*vit_g.y;
	cg.z += dt*vit_g.z;
        /*
	if ( cg.z <(zavd+zarg)/2.0+0.1)  {
            cg.z= (zavd+zarg)/2.0+0.1;
        }
*/
        //tape.add(new Record(System.nanoTime()/1.0e9, rot_diff,braq));
        if ( tape.size() == (10000 * 30)) {
            int ii=0;
        }
    }
}
