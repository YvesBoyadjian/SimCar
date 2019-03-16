/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package simcar.voiture;

import java.awt.event.KeyEvent;
import org.la4j.Matrices;
import org.la4j.Matrix;
import org.la4j.Vector;
import org.la4j.Vectors;
import simcar.Simcar;
import simcar.windows.JOYINFO;
import simcar.windows.MMSystem;
import simcar.struct.Double3D;
import simcar.struct.Point3D;
import simcar.windows.POINT;
import simcar.windows.WinUser;

/**
***************************************************************************
		Paramètres dynamiques de l'état de la voiture.
***************************************************************************
 *
 * @author YvesFabienne
 */
public class Chassis extends Caracteristiques {

        double[][] rotat = new double[3][3];

	public double	rot_mot;	// Vitesse de rotation du moteur = rad.s-1
	public int	rapport;	// Rapport de boite engagé = -1<= ss dim <= 4
        public double   dateRapport;    // Date en secondes du dernier changement de rapport

        protected double	embr;	// Coefficient de transmission de couple de l'embrayage = 0 <= ss dim <= 1

	public double	rot_pont;	// Vitesse de rotation du pont avant.
	public double	rot_diff;	// Vitesse de rotation de la roue avant gauche/droite.
//	double	rot_ard, rot_arg;	// Vitesses de rotation des roues arrières.

	public double	accel;		// Position de la pédale d'accélérateur = 0 <= ss dim <= 1
	public double	freins;		// Position de la pédale de frein = 0<= ss dim <=1
	public double	angle_vol;	// Angle de braquage du volant ( négatif à droite)

	protected double	braq;	// Angle de braquage moyen des roues avant = ss dim

	public Double3D	cg = new Double3D();	// Coordonnées du centre d'inertie / Repère absolu = m
	public Double3D	vg = new Double3D();	// Composantes de la vitesse de G dans (I,J,K).
	//public double	rot00, rot11, rot22;
	//public double	rot01, rot02, rot10, rot12, rot20, rot21;
        
        public final Matrix rotMatrix = Matrices.DENSE.apply(3, 3);

	public Double3D	vit_g = new Double3D();	// Composantes de la vitesse absolue du centre d'inertie = m/s
	public Double3D	sigma = new Double3D();	// Moment cinétique de la voiture ds (G,I,J,K).
	public Double3D	om = new Double3D();	// Vecteur rotation de la voiture ds (G,I,J,K).

	public Point3D[][]  route;	// Adresse de la route.
	private double	nv;	// n° de la plaque sur laquelle se trouve la voiture. ( dans le tableau route[])
	public double	td;	// Horloge interne de la voiture.

        public Chassis() {

            for ( int i=0; i< Simcar.NVISI;i++) {
                for( int j=0; j< 7; j++) {
                    pointrot[i][j] = new Double3D();
                }
            }
        }

        public synchronized void setNV( double nvArg) throws ArriveeAtteinte {
            nv = nvArg;
            if ( nv > (Simcar.NB_PLAQUES - 1000)) {
                throw new ArriveeAtteinte();
            }
        }

        public synchronized double getNV() {
            return nv;
        }

/*******************************************************
FONCTION: KEY(double)
ROLE: Traitement des entrées clavier pour le controle de la voiture.
SORTIE: TRUE si caractère traité, FALSE sinon.
*******************************************************/
	public boolean Key(double dt) {

	JOYINFO joy = new JOYINFO();
	double delta_x, delta_y;
	double volant;

	freins -= 1.5*dt;
	if ( freins < 0.0) freins = 0.0;

	if( MMSystem.joyGetPos( MMSystem.JOYSTICKID1, joy)== MMSystem.JOYERR_NOERROR ) {
		if ( joy.wXpos< Simcar.joy_x_min) Simcar.joy_x_min=joy.wXpos;
		if ( joy.wXpos > Simcar.joy_x_max) Simcar.joy_x_max=joy.wXpos;
		if ( joy.wYpos < Simcar.joy_y_min) Simcar.joy_y_min = joy.wYpos;
		if ( joy.wYpos > Simcar.joy_y_max) Simcar.joy_y_max = joy.wYpos;
		if ( (delta_x = Simcar.joy_x_max - Simcar.joy_x_min)>20.0) {
			volant = 1.0 - 2.0*( joy.wXpos - Simcar.joy_x_min)/delta_x;	// Angle du volant compris entre -1 et 1.
			if ( volant >0.0)
				if ( volant > 0.9) {
					if (angle_vol < 0.225)
						angle_vol = 0.225;
					angle_vol += 0.4*dt;
					if ( angle_vol > 1.0)
						angle_vol = 1.0;
				}
				else
					angle_vol= volant*0.25;
			else
				if ( volant < - 0.9) {
					if ( angle_vol > - 0.225)
						angle_vol = - 0.225;
					angle_vol -= 0.4*dt;
					if ( angle_vol < -1.0)
						angle_vol = -1.0;
				}
				else
					angle_vol= volant*0.25;
		}
		else angle_vol=0.0;

		if ( (delta_y = Simcar.joy_y_max - Simcar.joy_y_min)>20.0) {
			volant = 1.0 - 2.5*( joy.wYpos - Simcar.joy_y_min)/delta_y;
			if ( volant >0.0) {
				accel = 3.0*volant;
				if (accel > 1.0) accel = 1.0;
				freins = 0.0;
			}
			else {
				freins = - volant;
				if ( freins > 1.0) freins = 1.0;
				accel = 0.0;
			}
		}
	}
	else {	// Cas où le joystick n'est pas connecté.
                double angle_max = 1.0/(1.0 + vg.norm()/10.0);
		if ( Simcar.keyb[KeyEvent.VK_RIGHT] )  {
			if ( angle_vol > 0.0 ){
                            angle_vol = 0.0;
                        }
			angle_vol -= dt;
			if (angle_vol <- angle_max) angle_vol= -angle_max;
		}
		if ( Simcar.keyb[ KeyEvent.VK_LEFT] ) {
			if (angle_vol < 0.0) {
                            angle_vol = 0.0;
                        }
			angle_vol += dt;
			if (angle_vol > angle_max) angle_vol= angle_max;
		}
                if ( !Simcar.keyb[ KeyEvent.VK_LEFT] && !Simcar.keyb[KeyEvent.VK_RIGHT]) {
        		if ( angle_vol>0.0) {
                            angle_vol -= dt;
                            if (angle_vol < 0.0) {
                                angle_vol = 0.0;
                            }
                        }
                	else if (angle_vol < 0.0) {
                            angle_vol += dt;
                            if ( angle_vol > 0.0) {
                                angle_vol = 0.0;
                            }
                        }
                }
		if ( Simcar.keyb[KeyEvent.VK_UP] ) {
			freins = 0.0;
			accel+= 10.0*dt;
			if (accel > 1.0) accel= 1.0;
		}
                else {
                    accel -= 5*dt;
                    if ( accel < 0 ) {
                        accel = 0;
                    }
                }
		if ( Simcar.keyb[KeyEvent.VK_DOWN] || Simcar.keyb[KeyEvent.VK_SPACE]) {
                    /*
			accel -= 2.0*dt;
			if (accel <0.0) accel=0.0;
                     *
                     */
                        accel = 0;
        		freins += 10.*dt;
                	if ( freins > 1.0) freins = 1.0;
		}
                 else {
                    freins -= 5*dt;
                    if ( freins < 0) {
                        freins = 0;
                    }
                 }
	}	// Fin du cas où le joystick n'est pas connecté.
	if ( Simcar.keyb[KeyEvent.VK_SPACE] ) {
		freins += 3.*dt;
		if ( freins > 1.0) freins = 1.0;
	}
        if ( Simcar.vitauto) {
            if ( rot_mot > 650.0) {
                if ( (System.nanoTime()/1.0e9 - dateRapport) > 1.0) {
                    if ( incRapport()) {
                        return true;
                    }
                }
            }
            if ( rot_mot < 300.0 && rapport > 1 && accel > 0.0) {
                if ( (System.nanoTime()/1.0e9 - dateRapport) > 1.0) {
                    if ( decRapport()) {
                        return true;
                    }
                }
            }
        }
	if (Simcar.keybtr['X'] ) {
		Simcar.keybtr['X']= Simcar.FALSE;
                return incRapport();
	}
	if ( Simcar.keybtr['W'] ){
		Simcar.keybtr['W']= Simcar.FALSE;
                return decRapport();
	}
	return false;
        }

        public boolean incRapport() {

		rapport ++;
		if (rapport > 4) rapport = 4;
		else {
                    dateRapport = System.nanoTime()/1.0e9;
                    return(true);
                }
                return false;
        }

        public boolean decRapport() {

		rapport --;
		if (rapport < -1) rapport = -1;
		else return(true);
                return false;
        }

        // Calcule la matrice de rotation.
/**********************************************************
FONCTION: CALCU_ROT(dt)
RÔLE: Itération de la matrice de rotation.

	 [rot] est l'inverse de la matrice P de passage de (O,i,j,k) à
	(G,I,J,K), repère mobile barycentrique.
	 Comme P est une rotation, [rot] est égale à la transposée de P.
	 Les colonnes de [rot] sont les coordonnées de i,j,k dans (G,I,J,K).
	 Les lignes de [rot] sont les coordonnées de I,J,K dans (O,i,j,k).

	 Calcu_rot itère la matrice de passage en effectuant les rotations
	 dans le repère barycentrique.

	 [rot] --> tr([rotat])* [rot]

**********************************************************/
	public void Calcu_rot(double dt) {


	double[][] rotnew = new double[3][3], rot = new double[3][3];
	int i, j, k;

	Simcar.sina=Math.sin( dt*sigma.z/mz); Simcar.cosa=Math.cos( dt*sigma.z/mz);
	Simcar.sinb=Math.sin( dt*sigma.y/my); Simcar.cosb=Math.cos( dt*sigma.y/my);
	Simcar.sinc=Math.sin( dt*sigma.x/mx); Simcar.cosc=Math.cos( dt*sigma.x/mx);

	rotat[0][0] = Simcar.cosa*Simcar.cosb;
	rotat[0][1] = - Simcar.sina*Simcar.cosc + Simcar.cosa*Simcar.sinb*Simcar.sinc;
	rotat[0][2] = Simcar.sina*Simcar.sinc + Simcar.cosa*Simcar.cosc*Simcar.sinb;
	rotat[1][0] = Simcar.sina*Simcar.cosb;
	rotat[1][1] = Simcar.cosa*Simcar.cosc + Simcar.sina*Simcar.sinb*Simcar.sinc;
	rotat[1][2] = - Simcar.cosa*Simcar.sinc + Simcar.sina*Simcar.sinb*Simcar.cosc;
	rotat[2][0] = - Simcar.sinb;
	rotat[2][1] = Simcar.cosb*Simcar.sinc;
	rotat[2][2] = Simcar.cosb*Simcar.cosc;
	rot[0][0]= rotMatrix.get(0, 0);//rot00;
	rot[0][1]= rotMatrix.get(0, 1);//rot01;
	rot[0][2]= rotMatrix.get(0, 2);//rot02;
	rot[1][0]= rotMatrix.get(1, 0);//rot10;
	rot[1][1]= rotMatrix.get(1, 1);//rot11;
	rot[1][2]= rotMatrix.get(1, 2);//rot12;
	rot[2][0]= rotMatrix.get(2, 0);//rot20;
	rot[2][1]= rotMatrix.get(2, 1);//rot21;
	rot[2][2]= rotMatrix.get(2, 2);//rot22;
	for ( i=0; i<3; i++)
		for ( j=0; j<3; j++)
			for ( k=0, rotnew[i][j]=0.0; k<3; k++)
			rotnew[i][j] += rotat[k][i]*rot[k][j];
        
	/*rot00*/rotMatrix.set(0,0,rotnew[0][0]);
	/*rot01*/ rotMatrix.set(0,1,rotnew[0][1]);
	/*rot02*/ rotMatrix.set(0,2,rotnew[0][2]);
	/*rot10*/ rotMatrix.set(1,0,rotnew[1][0]);
	/*rot11*/ rotMatrix.set(1,1,rotnew[1][1]);
	/*rot12*/ rotMatrix.set(1,2,rotnew[1][2]);
	/*rot20*/ rotMatrix.set(2,0,rotnew[2][0]);
	/*rot21*/ rotMatrix.set(2,1,rotnew[2][1]);
	/*rot22*/ rotMatrix.set(2,2,rotnew[2][2]);
        
        // anti-creep
        
        double kf = 0.25;
        
        Vector r1 = rotMatrix.getRow(0);
        Vector r2 = rotMatrix.getRow(1);
        Vector r3 = rotMatrix.getRow(2);
        
        Vector rp1 = r1.subtract(correct(kf,r1,r2)).subtract(correct(kf,r1,r3));
        Vector rp2 = r2.subtract(correct(kf,r2,r1)).subtract(correct(kf,r2,r3));
        Vector rp3 = r3.subtract(correct(kf,r3,r1)).subtract(correct(kf,r3,r2));
        
        rotMatrix.setRow(0, rp1);
        rotMatrix.setRow(1, rp2);
        rotMatrix.setRow(2, rp3);
        }
        
        public Vector correct(double k, Vector r1, Vector r2) {
            return r2.multiply(k*r1.innerProduct(r2)/r2.innerProduct(r2));
        }

        // Itère le régime du moteur.
/********************************************************
FONCTION: CALCU_MOT (FORCE_ROUES)
	Calcule la variation de régime moteur et le couple au differentiel.

	Entrées : Total des forces appliquées sur les roues avant.
					 Intervalle de temps.
	Sorties : Vitesse de rotation moteur.
					Force équivalente à la bande de roulement au differentiel.

********************************************************/
	protected double calcu_mot(double dt, double force_roues, double rotat) {
            
	double couple_mot, c_embr;

//_________________________________________________Frottements moteur
	couple_mot = (rotat - reg_ral)* coef_frein_mot;
//_____________________________________________________Force motrice
	if ( rotat < reg_cmot_max )	
		couple_mot += accel * (
			cmot_ral +
			( rotat - reg_ral ) 			* ( coef_mot1 - coef_frein_mot )
		);
	else if ( rotat < reg_max )
		couple_mot += accel * (
			cmot_max - ( reg_cmot_max - reg_ral) * coef_frein_mot +
			( rotat - reg_cmot_max) * ( coef_mot2 - coef_frein_mot )
		);
	else
		couple_mot += accel *(
			cmot 		- ( 		  reg_max - reg_ral) * coef_frein_mot +
			( rotat - reg_max)		 * ( coef_mot3 - coef_frein_mot )
		);
	c_embr = force_roues*rayon*rap_boite[rapport+1];
//________________________________________________Force d'inertie moteur
	rot_mot += dt * (couple_mot+c_embr)/(jmot+rap_boite[rapport+1]*rap_boite[rapport+1]*jpont);

	if ( rapport == 0)
		return 0.0;
	else
		return couple_mot/rayon/rap_boite[rapport+1];
        }

        // Calcule la force de freinage.
/*********************************************
		Force exercee par les freins
*********************************************/
	protected double calcu_fr(double fr_max, double v_rot) {

            double freins_coef = 0.1;
	if ( v_rot < -1.0/freins_coef)
		return -fr_max;
	else if ( Math.abs(v_rot) < 1.0/freins_coef)
		return fr_max*v_rot*freins_coef;
	else
		return fr_max;
        }

        // Calcule l'abscisse curviligne.
	protected double calcu_s( Double3D coord) {
            
	Double3D	r = new Double3D(), cg = new Double3D();
	double norme;
	int	no;

	no = (int) nv;
	if (no < 0)
		no = 0;

        double delta=0.0;
        do {
	// Vecteur parallèle à la route.
	r.x =  route[no+1][3].x - route[no][3].x;
	r.y =  route[no+1][3].y - route[no][3].y;
	r.z =  route[no+1][3].z - route[no][3].z;

	// Vecteur pavé arriere -> point.
	cg.x = coord.x - route[no][3].x;
	cg.y = coord.y - route[no][3].y;
	cg.z = coord.z - route[no][3].z;

	// Abscisse curviligne du point.
	if ( Math.abs(norme = r.x*r.x+r.y*r.y+r.z*r.z) > Simcar.FLT_EPSILON) {
            delta = (cg.x*r.x+cg.y*r.y+cg.z*r.z)/norme;
            }
	else {
		WinUser.MessageBox( null, "Repere 3", "Voiture", WinUser.MB_OK);
		return (double)no;
	}
        no = no + (int)delta;
        } while ( delta > 1);
		return  Math.max(1.0,(double)no+delta);
        }


        // Calcule la hauteur du sol en fonction de l'abscisse curviligne.
/**************************
 Calcul de l'altitude de la route
	entree: Abscisse curviligne
**************************/

	protected double calcu_z( double s) {

	int	nv2 = (int)s;

	if (nv2< 0l)
		return 0.0;
	return   route[nv2][3].z+( s - (double)nv2)*( route[nv2+1][3].z - route[nv2][3].z);
        }

/***********************************************************
	Convertit des coordonnées dans (O,i,j,k) en coordonnées dans (G,I,J,K)
***********************************************************/
	public void O_G( Double3D vect, Double3D sort) {
            /*
	Double3D buff = new Double3D();

	buff.x = vect.x;
	buff.y = vect.y;
	buff.z = vect.z;
        */
        Vector buffv = Vectors.DENSE.apply(3);
        buffv.set(0, vect.x);
        buffv.set(1, vect.y);
        buffv.set(2, vect.z);
        
        Vector sortv = rotMatrix.multiply(buffv);

	sort.x = sortv.get(0);//rot00*buff.x + rot01*buff.y + rot02*buff.z;
	sort.y = sortv.get(1);//rot10*buff.x + rot11*buff.y + rot12*buff.z;
	sort.z = sortv.get(2);//rot20*buff.x + rot21*buff.y + rot22*buff.z;
        
        
        }

/***********************************************************
	Convertit des coordonnées dans (G,I,J,K) en coordonnées dans (O,i,j,k)
***********************************************************/
	public void G_O( Double3D vect, Double3D sort) {

	Double3D buff = new Double3D();

	buff.x = vect.x;
	buff.y = vect.y;
	buff.z = vect.z;

        Vector buffv = Vectors.DENSE.apply(3);
        buffv.set(0, vect.x);
        buffv.set(1, vect.y);
        buffv.set(2, vect.z);
        
        Vector sortv = rotMatrix.transpose().multiply(buffv);

        
	sort.x = sortv.get(0);//rot00*buff.x + rot10*buff.y + rot20*buff.z;
	sort.y = sortv.get(1);//rot01*buff.x + rot11*buff.y + rot21*buff.z;
	sort.z = sortv.get(2);//rot02*buff.x + rot12*buff.y + rot22*buff.z;
        }

	Double3D[][] pointrot = new Double3D[Simcar.NVISI][7]; // Bords de route après rotation en mètres.
	public int CalcuPoints(  boolean fast, POINT xy[]) {

	int nv2, no;
	int i,j;
	double x, y, z;

	double	rot00 = rotMatrix.get(0,0), rot11 = rotMatrix.get(1,1), rot22 = rotMatrix.get(2,2);
	double	rot01 = rotMatrix.get(0,1), rot02 = rotMatrix.get(0,2), rot10 = rotMatrix.get(1,0), rot12 = rotMatrix.get(1,2), rot20 = rotMatrix.get(2,0), rot21 = rotMatrix.get(2,1);
        
	//Point3D  ( _huge* route)[7];	// Coordonnées du bord de la route en m.

	nv2 = (int)(getNV()+Simcar.DIST_MIN);	// Affichage à partir de 6m de distance
	if ( nv2<0) nv2=0;

	if ( !fast) for ( no = 0, j=nv2; no < Simcar.VISI_MIN; no++, j++) {	// Changement de repère pour les points.
		for ( i=0; i<7; i++) {
			x=route[j][i].x - cg.x;
			y=route[j][i].y - cg.y;
			z=route[j][i].z - cg.z;
			pointrot[no][i].x = rot00*x+rot01*y+rot02*z;
			pointrot[no][i].y = rot10*x+rot11*y+rot12*z;
			pointrot[no][i].z = rot20*x+rot21*y+rot22*z - alti_obs;
		}
	}
	else for ( no = 0, j=nv2; no < Simcar.VISI_MIN; no++, j++) {	// Changement de repère pour les points.
		for ( i=1; i<6; i++) {
			x=route[j][i].x - cg.x;
			y=route[j][i].y - cg.y;
			z=route[j][i].z - cg.z;
			pointrot[no][i].x = rot00*x+rot01*y+rot02*z;
			pointrot[no][i].y = rot10*x+rot11*y+rot12*z;
			pointrot[no][i].z = rot20*x+rot21*y+rot22*z - alti_obs;
		}
	}

	if ( !fast ) for ( no = 0, j= Simcar.VISI_MIN+nv2; no < Simcar.NVISI - Simcar.VISI_MIN; no++, j+=10) {	// Changement de repère pour les points.
		for ( i=0; i<7; i++) {
			x=route[j][i].x - cg.x;
			y=route[j][i].y - cg.y;
			z=route[j][i].z - cg.z;
			pointrot[ Simcar.VISI_MIN+no][i].x = rot00*x+rot01*y+rot02*z;
			pointrot[ Simcar.VISI_MIN+no][i].y = rot10*x+rot11*y+rot12*z;
			pointrot[ Simcar.VISI_MIN+no][i].z = rot20*x+rot21*y+rot22*z - alti_obs;
		}
	}
	else for ( no = 0, j= Simcar.VISI_MIN+nv2; no < Simcar.NVISI- Simcar.VISI_MIN; no++, j+=10) {	// Changement de repère pour les points.
		for ( i=1; i<6; i++) {
			x=route[j][i].x - cg.x;
			y=route[j][i].y - cg.y;
			z=route[j][i].z - cg.z;
			pointrot[ Simcar.VISI_MIN+no][i].x = rot00*x+rot01*y+rot02*z;
			pointrot[ Simcar.VISI_MIN+no][i].y = rot10*x+rot11*y+rot12*z;
			pointrot[ Simcar.VISI_MIN+no][i].z = rot20*x+rot21*y+rot22*z - alti_obs;
		}
	}

	if ( !fast ) for ( no= Simcar.NVISI-1; no >=0; no--) {
		i = 6*no;
		xy[i].x = projx( pointrot[no][0]);	// Flanc gauche
		xy[i].y = projy( pointrot[no][0]);
		i++;
		xy[i].x = projx( pointrot[no][2]);	// Route gauche
		xy[i].y = projy( pointrot[no][2]);
		i++;
		xy[i].x = projx( pointrot[no][4]);	// Route droite
		xy[i].y = projy( pointrot[no][4]);
		i++;
		xy[i].x = projx( pointrot[no][6]); // Flanc droit
		xy[i].y = projy( pointrot[no][6]);
		i++;
		xy[i].x = projx( pointrot[no][1]); // Bas-coté gauche
		xy[i].y = projy( pointrot[no][1]);
		i++;
		xy[i].x = projx( pointrot[no][5]); // Bas-coté droit
		xy[i].y = projy( pointrot[no][5]);
	}
	else for ( no= Simcar.NVISI-1; no >=0; no--) {
		i = 6*no;
		i++;
		xy[i].x = projx( pointrot[no][2]);	// Route gauche
		xy[i].y = projy( pointrot[no][2]);
		i++;
		xy[i].x = projx( pointrot[no][4]);	// Route droite
		xy[i].y = projy( pointrot[no][4]);
		i++;
		i++;
		xy[i].x = projx( pointrot[no][1]); // Bas-coté gauche
		xy[i].y = projy( pointrot[no][1]);
		i++;
		xy[i].x = projx( pointrot[no][5]); // Bas-coté droit
		xy[i].y = projy( pointrot[no][5]);
	}

        return nv2;
        }

    int projx( Double3D p)
    {
	if ( p.y > 0.5)
		return	(int)( 0.6*Simcar.xymax_x*p.x/p.y)+ (Simcar.xmax+1)/2;
	else
		return Integer.MAX_VALUE;
    }

    int projy( Double3D p)
    {
	if ( p.y> 0.5)
		return   -(int)( 0.6*Simcar.xymax_y*p.z/p.y)+ Simcar.ymax*10/18;
	else
		return Integer.MAX_VALUE;
    }

	public void CalcuHori( POINT[] xye) {
	double x, y;
	Double3D hori1 = new Double3D(), hori2 = new Double3D();

	double	rot00 = rotMatrix.get(0,0), rot11 = rotMatrix.get(1,1), rot22 = rotMatrix.get(2,2);
	double	rot01 = rotMatrix.get(0,1), rot02 = rotMatrix.get(0,2), rot10 = rotMatrix.get(1,0), rot12 = rotMatrix.get(1,2), rot20 = rotMatrix.get(2,0), rot21 = rotMatrix.get(2,1);
        
	x = rot10*.707+0.707*rot11;
	y = rot11*.707 - 0.707*rot10;
	hori1.x = rot00*x + rot01*y;
	hori1.y = rot10*x + rot11*y;
	hori1.z = rot20*x + rot21*y;
	x = rot10*.707 - 0.707*rot11;
	y =rot11*.707 + 0.707*rot10;
	hori2.x = rot00*x + rot01*y;
	hori2.y = rot10*x + rot11*y;
	hori2.z = rot20*x + rot21*y;

	Projette( hori1, hori2, hori2, hori2, xye);
        }

        /*************************************************************************
_________________________Projection d'un elt de route sur l'écran.
_________________________Retourne le nombre de points.

*************************************************************************/

int Projette( Double3D e1, Double3D e2, Double3D e3, Double3D e4, POINT xy[])
{
	xy[0].x = projx(e1);	// On projette le 1er point.
	xy[0].y = projy(e1);
	xy[1].x = projx(e2);	// On projette le 2eme point.
	xy[1].y = projy(e2);
	xy[2].x = projx(e3);
	xy[2].y = projy(e3);
	xy[3].x = projx(e4);
	xy[3].y = projy(e4);
	return(4);
}

}
