/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package simcar;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import simcar.struct.Double3D;
import simcar.struct.Point3D;
import simcar.struct.RECT;
import simcar.voiture.ArriveeAtteinte;
import simcar.voiture.RoueEnum;
import simcar.voiture.RoueNeTouchePasSol;
import simcar.voiture.Voiture;
import simcar.windows.HDC;
import simcar.windows.POINT;
import simcar.windows.WinGDI;
import simcar.windows.WinUser;

/**
 *
 * @author YvesFabienne
 */
public class Simcar {

    public static final double DIST_MIN = 0.0;

    public static final int NB_PLAQUES = 5000;	// Nbre de plaques mémorisées.
    public static final int NB_COUL_SOL	= 16;	// Nombre de niveaux de couleurs pour l'herbe.
    public static final int NB_VOLANT =	45;	// Nombre de positions différentes du volant.
    public static final int NVISI = 250;	// Nombre de plaques visibles en même temps.
    public static final int VISI_MIN = 200;	// n° à partir duquel on distingue les plaques < NVISI
    public static final float INTER_PLAQUE = 4.0f;	// Nbre de mètres entre chaque plaque.
    public static final int NB_FILTRE =	10;	// Nombre d'échantillons dans le filtrage de la route.

    public static final long FR_ECH  = 44100L;
    public static final float PI = 3.14159265359f;

    public static final int FOND	=128;
    public static final int R_CIEL	=185;
    public static final int V_CIEL	=185;
    public static final int B_CIEL	=255;
    public static final int ROUTE1	=46;
    public static final int ROUTE2	=36;
    public static final int R_SOL	=100;
    public static final int V_SOL	=220;
    public static final int B_SOL	=30;
    public static final int R_FLANC	=230;
    public static final int V_FLANC	=180;
    public static final int B_FLANC	=120;

    public static final boolean FALSE = false;
    public static final float FLT_EPSILON = 1.192092896e-07F;        /* smallest such that 1.0+FLT_EPSILON != 1.0 */

    public static Point3D[][] hglb;//, hBits, hCoul[256];

    public static int joy_x_min, joy_x_max;
    public static int joy_y_min, joy_y_max, joy_y_med;

    public static boolean[] keyb = new boolean[1024];
    public static boolean[] keybtr = new boolean[1024];

    public static BufferedImage hbm_ct_buffer=null, hbm_vit_buffer=null, hbm_ct, hbm_vit;
    public static BufferedImage[] hbm_vol = new BufferedImage[NB_VOLANT];
    public static HDC hDCMem_vit_buffer= new HDC(), hDCMem_ct_buffer = new HDC(), hDCEcr= new HDC(), hDC_ct= new HDC();
    public static Color  hb_Rouge, hb_Fond, hb_old;
    
    public static int[] flanc = new int[16], Sol = new int[16];
    public static int Ciel, Route1, Route2;

    public static Font hf_Vitesse, hf_old;
    public static	int hsyspal=0;

    public static RECT TheRect = new RECT(), ct_Rect = new RECT(), vit_Rect = new RECT();	// Dimensions de la fenètre (Chiffre pair), du compte-tours.

    public static double cosa, cosb, cosc, sina, sinb, sinc;
    public static int xmax, ymax, xymax_x, xymax_y;	// Dimensions du bitmap interne
    public static int zoomx=1, zoomy=1;	// Finesse du graphisme
    public static int yaccel, yfreins;	// Hauteur de la visualisation de l'accélérateur/frein.

    public static boolean vitauto = true;

    public static Point3D[][]  route = new Point3D[NB_PLAQUES][7];	// Coordonnées du bord de la route en m.

    //public static int boucle=0;
    public static final double SOUND_PERIOD =0.05;
    public static double soundTime = 0.0;

    public static boolean fast=false;
    public static double g=9.81;

    public static Voiture car;
    public static double[] source = { 1500.0, 3000.0, 4000.0 };

    public static short[] lpBruitRose  = new short[55000];

    //public static short[] lpData = null, lpOldData = null;
    public static byte[] bbData = null;

    public static SourceDataLine sdl;
    
    public Simcar() {

        AudioFormat af = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED, // encoding
                44100.0f, //sampleRate
                16,          //sampleSizeInBits
                2,          // channels
                4,          // frameSize
                44100.0f, // frameRate
                true
                );
        
        DataLine.Info di = new DataLine.Info(SourceDataLine.class,af);
        try {
            sdl = AudioSystem.getSourceDataLine(af);
            sdl.open(af);
            sdl.start();
        } catch (LineUnavailableException ex) {
            Logger.getLogger(Simcar.class.getName()).log(Level.SEVERE, null, ex);
        }

        for ( int i=0; i<20;i++) {
            xye[i] = new POINT();
        }
        for ( int i=0; i<NVISI*8; i++) {
            xy[i] = new POINT(); // Points projetés à l'écran
        }
    }

    public void dispose() {
        sdl.close();
        sdl = null;
    }
    
    public static double CHRONO() {
        return System.nanoTime()/1.0e9;
    }

    static double domaine( double a, double b, double c){
        return Math.min( Math.max(a,b),c);
    }

    public static int flanc( int X) {
        return WinGDI.PALETTERGB( R_FLANC*(X+1)/16, V_FLANC*(X+1)/16, B_FLANC*(X+1)/16);
    }
    public static int sol( int X) {
        return WinGDI.PALETTERGB( R_SOL*(X+17)/32, V_SOL*(X+17)/32, B_SOL*(X+17)/32);
    }
    public static int COUL_FOND() {
        return WinGDI.PALETTERGB( FOND, FOND, FOND);	// Couleur du fond du tableau de bord.
    }
    public static int COUL_SOL() {
        return WinGDI.PALETTERGB( R_SOL, V_SOL, B_SOL);	// Couleur du sol
    }
    public static int COUL_CIEL() {
        return WinGDI.PALETTERGB( R_CIEL, V_CIEL, B_CIEL);	// Couleur du ciel
    }
    public static int COUL_ROUTE1() {
        return WinGDI.PALETTERGB( ROUTE1, ROUTE1, ROUTE1);	// Couleur de la route
    }
    public static int COUL_ROUTE2() {
        return WinGDI.PALETTERGB( ROUTE2,ROUTE2,ROUTE2);	// Couleur de la route
    }
    public static int COUL_TRAIT() {
        return WinGDI.PALETTERGB( 0, 0, 0);	// Couleur des segments graphiques.
    }
/*##################### Calcul du vecteur normal #######################*/
static void CalcNorm(	Point3D[][] route, int no, int j, Double3D normale)
{
	double x1, y1, z1; // Vecteurs parallèles à la surface.
	double x2, y2, z2;

	x1 =route[no][j].x - route[no][j+1].x;
	y1 =route[no][j].y - route[no][j+1].y;
	z1 =route[no][j].z - route[no][j+1].z;
	x2 =route[no][j].x - route[no+1][j].x;
	y2 =route[no][j].y - route[no+1][j].y;
	z2 =route[no][j].z - route[no+1][j].z;
	normale.x = y2*z1 - y1*z2;
	normale.y = z2*x1 - z1*x2;
	normale.z = x2*y1 - x1*y2;
}

static double CalcLumi( Point3D[][] route, int no, int j)
{
	double lum;	// Luminosité de la surface.
	Double3D norm = new Double3D();

	CalcNorm(route, no, j, norm);
	lum = (source[0]*norm.x + source[1]*norm.y + source[2]*norm.z )
		/Math.sqrt((source[0]*source[0]+source[1]*source[1]+source[2]*source[2])
		*(norm.x*norm.x+norm.y*norm.y+norm.z*norm.z));
	return domaine( 0.0, Math.abs(lum), 0.999);
}

    public static void InitApplication() {
	double angle, montee, virage, vir2, dv, zr, dz, br_dz, br_zr;
	double	vx, vy;
	double	larg_route, larg_flanc, larg_cote, v_norme, devers;
	double[] coef = new double[NB_FILTRE];
        double somme;
    int i;
    int j;
	//Point3D  ( _huge* route)[7];	// Coordonnées du bord de la route en mm.
	double[] alti = new double[NB_PLAQUES];

        for ( i=0; i < NB_PLAQUES; i++) {
            for ( j=0; j < 7; j++) {
                route[i][j] = new Point3D();
            }
        }

	//hsyspal = RegisterPalette();

	joy_x_min = 65535; joy_x_max = 0;
	joy_y_min = 65535; joy_y_max = 0;

//		MessageBox( NULL, "Repere 1", "Simcar", MB_OK);
		//srand( unsigned(time(NULL)));
                Random srand = new Random( System.currentTimeMillis());
		for ( i=0; i<7;i++)
		{
			route[0][i].x = route[0][i].y = route[0][i].z = 0.0f;
			route[1][i].x =  route[1][i].z = 0.0f;
			route[1][i].y = INTER_PLAQUE;
		}
//		MessageBox( NULL, "Repere 2", "Simcar", MB_OK);
		//_______________________________________Route dirigée vers l'axe des y
		for ( i=1, angle=1.57, virage= 0.0, vir2 = 0.0; i<NB_PLAQUES; i++) {
			dv = srand.nextDouble() - 0.5- angle/30.0;
			dv = dv*dv*dv;
			virage += dv;
			virage -= virage/20.0;
//			if (virage > 0.39) virage=0.39;
//			if (virage < -0.39) virage=-0.39;
			if ( Math.abs(vir2 - virage) > 0.1) vir2 = virage;
			angle += vir2*vir2*vir2;
			route[i][3].x = route[i-1][3].x + (float)(INTER_PLAQUE*Math.cos(angle));
			route[i][3].y = route[i-1][3].y + (float)(INTER_PLAQUE*Math.sin(angle));
		}
//		MessageBox( NULL, "Repere 3", "Simcar", MB_OK);
		for ( i=0, somme=0.0; i<NB_FILTRE; i++) {
			coef[i] = 0.54+(1.0 - 0.54)*Math.cos(2.0*PI*( i - NB_FILTRE/2)/NB_FILTRE);
			somme += coef[i];
		}
//		MessageBox( NULL, "Repere 4", "Simcar", MB_OK);
		//srand( unsigned(time(NULL)));
		//hglbAlti = GlobalAlloc( GMEM_FIXED, sizeof(double)*long(NB_PLAQUES));
		//(double (_huge*)) GlobalLock(hglbAlti);

//		if ( alti) MessageBox( NULL, "Repere 5", "Simcar", MB_OK);
		for ( i=1, dz = zr = alti[0] = 0.0 ; i<NB_PLAQUES; i++) {
			br_dz = srand.nextDouble() - 0.5;	// Irrégularités de la pente
			dz += br_dz*0.18;
			//________________________________Limitation de la pente moyenne
			dz -= dz/25.0;
			//______________________________Limitation de l'altitude de la route
			dz -= zr/3.0e5;
			zr += dz;
			montee = zr - alti[i-1];
			//______________________________________Pente maximum de 13 %
			montee = domaine( -0.13*INTER_PLAQUE, montee, 0.13*INTER_PLAQUE);
			alti[i] = alti[i-1] + montee;
		}
//		MessageBox( NULL, "Repere 6", "Simcar", MB_OK);
		//srand( unsigned(time(NULL)));
		for (i=0; i<NB_PLAQUES;i++) {
			br_zr = srand.nextDouble() - 0.45 ;	// Irrégularités de la route
			for ( j=0; j<4; j++)
				br_zr *=3.3*br_zr*br_zr;
			//______________________________Irrégularités de 2.5 m max. sur la chaussée
			alti[i] += br_zr*5.0;
		}
//		MessageBox( NULL, "Repere 7", "Simcar", MB_OK);
		for ( i=NB_PLAQUES - NB_FILTRE; i != 0; i--) {
			route[i][3].z = 0.f;
			for ( j=NB_FILTRE-1; j != 0; j--)
				route[i][3].z += (float)(alti[i+j]*coef[j]/somme);
		}
		//GlobalUnlock( hglbAlti);
		//GlobalFree( hglbAlti);
		//srand( unsigned(time(NULL)));
		larg_route=3.5;	// Route de 7m de large
		larg_cote=10.0;	// Bas-côtés larges de 7m
//		MessageBox( NULL, "Repere 8", "Simcar", MB_OK);
		for ( i=0, devers=0.0; i< NB_PLAQUES-NB_FILTRE; i++, devers += ( srand.nextDouble() - 0.52)/10.0) {
			if ( devers > 0.1) devers = 0.1;
			if ( devers < -0.1) devers = - 0.1;
			vx = route[i+1][3].x - route[i][3].x;
			vy = route[i+1][3].y - route[i][3].y;
			v_norme = Math.sqrt(vx*vx +vy*vy);
			if ( v_norme == 0.0)
			WinUser.MessageBox( null, "vnorme=0", "Simcar", WinUser.MB_OK);
			route[i][2].x =(float)( -larg_route*vy/v_norme) + route[i][3].x;	// Bord gauche de la route
			route[i][2].y = (float)(larg_route*vx/v_norme) + route[i][3].y;
			route[i][4].x = (float)(larg_route*vy/v_norme) + route[i][3].x;	// Bord droit de la route
			route[i][4].y = (float)(-larg_route*vx/v_norme) + route[i][3].y;
			route[i][2].z =  route[i][3].z - (float)(devers);
			route[i][4].z = route[i][3].z + (float)(devers);
			route[i][1].x =  (float)(-larg_cote*vy/v_norme) + route[i][3].x;
			route[i][1].y =  (float)(larg_cote*vx/v_norme) + route[i][3].y;
			route[i][5].x =  (float)(larg_cote*vy/v_norme) + route[i][3].x;
			route[i][5].y =  (float)(-larg_cote*vx/v_norme) + route[i][3].y;
			route[i][1].z = route[i][3].z +(float)( srand.nextDouble() - 0.7);
			route[i][5].z = route[i][3].z + (float)( srand.nextDouble() - 0.7);
			larg_flanc=20.0;	// Flancs  à 25 m
			route[i][0].x = (float)(-larg_flanc*vy/v_norme) + route[i][3].x;	// Limite du flanc gauche
			route[i][0].y = (float)(larg_flanc*vx/v_norme) + route[i][3].y;
			larg_flanc=20.0;	// Flancs  à 25 m
			route[i][6].x = (float)(larg_flanc*vy/v_norme) + route[i][3].x;	// Limite du flanc droit
			route[i][6].y = (float)(-larg_flanc*vx/v_norme) + route[i][3].y;
			route[i][0].z = route[i][1].z+(float)(8.0+ srand.nextDouble()*2.0);
			route[i][6].z = route[i][5].z +(float)(8.0+ srand.nextDouble()*2.0);
		}
		//WinUser.MessageBox( null, "Repere 9", "Simcar", WinUser.MB_OK);
		for (i = NB_PLAQUES-NB_FILTRE-2;i!=0;i--) {
			route[i][0].coul = (int)(256.0*CalcLumi(route, i, 0));
			route[i][1].coul = (int)(256.0*CalcLumi(route, i, 1));
			route[i][4].coul = (int)(256.0*CalcLumi(route, i, 4));
			route[i][5].coul = (int)( 256.0*CalcLumi(route, i, 5));
		}
		//GlobalUnlock( hglb); route= NULL;
		//WinUser.MessageBox( null, "Repere 10", "Simcar", WinUser.MB_OK);

		/* Register the window class and return success/failure code. */
		return;
    }

    /****************************************************************************

    FUNCTION:  InitInstance(HANDLE, int)

    PURPOSE:  Saves instance handle and creates main window

    COMMENTS:

        This function is called at initialization time for every instance of
        this application.  This function performs initialization tasks that
        cannot be shared by multiple instances.

        In this case, we save the instance handle in a static variable and
        create and display the main program window.

****************************************************************************/

    void InitInstance()
{
	int i;

    /* Save the instance handle in static variable, which will be used in  */
    /* many subsequence calls from this application to Windows.            */


    /* Create a main window for this application instance.  */

    /* If window could not be created, return "failure" */


		for (i=1, lpBruitRose[0]=0; i<5500l; i++) {
			lpBruitRose[i] = (short)((1.0 - 200.0/FR_ECH)*lpBruitRose[i-1]+3000.0*( Math.random() - 0.5));
		}

        //lpData = new short[10000];
                bbData = new byte[99999];

	car = new  Voiture();
        car.route = route;

	if ( car == null)
		WinUser.MessageBox( null, "Alloc ratee", "Voiture", WinUser.MB_OK);
	car.Make305SR();

	//joyReleaseCapture(JOYSTICKID1);
	//joySetCapture(hwndApp, JOYSTICKID1, 10, FALSE);

	car.Nouveau();

	return ;               /* Returns the value from PostQuitMessage */
}

static void Son(Voiture voitu ) {

	int i, nb_ech, lambda;

	lambda = (int)(FR_ECH*PI/voitu.rot_mot);	// Longueur d'onde
	if ( lambda<=0 || lambda > 1200) {
            lambda = 1200;
        }

/*	hData = GlobalAlloc( GMEM_MOVEABLE | GMEM_SHARE, 10000l);
	if ( !(lpData = ( short _huge*) GlobalLock( hData)))
		MessageBox( hwndApp, "Erreur 1", 0, MB_OK);
*/
        /*
	hWaveHdr = GlobalAlloc(GMEM_MOVEABLE | GMEM_SHARE, sizeof(WAVEHDR));
   	lpWaveHdr = (LPWAVEHDR) GlobalLock(hWaveHdr);
         *
         */

	nb_ech = (int)(FR_ECH*SOUND_PERIOD);// (int)Math.floor(12000.0*voitu.rot_mot/FR_ECH/PI)*lambda;	// nbre maxi d'échantillons 16 bits
        /*
	if (nb_ech<=0 || nb_ech>12000) {
            nb_ech=12000;
        }
         * 
         */
        /*
	lpWaveHdr.lpData = ( LPSTR) lpData;
	lpWaveHdr.dwUser = (DWORD) hWaveHdr;
	lpWaveHdr.dwBufferLength = nb_ech*4l;
	lpWaveHdr.dwFlags = WHDR_BEGINLOOP | WHDR_ENDLOOP;
	lpWaveHdr.dwLoops = 100l;
*/
        ByteBuffer bb = ByteBuffer.wrap(bbData);
        ShortBuffer lpData = bb.asShortBuffer();
        
	for ( i=0; i< lambda; i++) {
		lpData.put(2*i, (short)(1.0e6/(voitu.rot_mot+100.0) * (
							  ((int)(4.0*i*voitu.rot_mot/PI/FR_ECH)%2)*(2.0+voitu.accel)*0.1 // Explosions
							 + Math.sin( 4.0*i*voitu.rot_mot/FR_ECH)*(1.5+voitu.accel)*1.5	// Déséquilibre vilebrequin
							 + Math.sin( 2.0*i*voitu.rot_mot/FR_ECH)*(1.0+6.0*voitu.accel)*0.7	// Déséquilibre vilebrequin+explosion
		)));
		lpData.put(2*i+1, lpData.get(2*i));
	}
	for ( ; i<nb_ech; i++) {
		lpData.put(2*i, lpData.get(2*(i%lambda)));
                lpData.put(2*i+1,lpData.get(2*(i%lambda)));
	}
	if ( voitu.avd.glis ^ voitu.ard.glis)
		for ( i=0; i< nb_ech; i++) lpData.put(2*i+1, (short)(lpData.get(2*i+1) +(short)(lpBruitRose[i]/1.5)));
	else if ( voitu.avd.glis & voitu.ard.glis)
		for ( i=0; i< nb_ech; i++) lpData.put(2*i+1, (short)(lpData.get(2*i+1) + lpBruitRose[i]));
	if ( voitu.avg.glis ^ voitu.arg.glis)
		for ( i=0; i< nb_ech; i++) lpData.put(2*i, (short)(lpData.get(2*i) + lpBruitRose[i+400]/1.5));
	else if ( voitu.avg.glis & voitu.arg.glis)
		for ( i=0; i< nb_ech; i++) lpData.put(2*i, (short)(lpData.get(2*i) + lpBruitRose[i+400]));
        
        
        sdl.write(bbData, 0, nb_ech * 2 * 2);
    /*
        waveOutPrepareHeader( hWaveOut, lpWaveHdr, sizeof(WAVEHDR));
            waveOutReset( hWaveOut);
            waveOutWrite( hWaveOut, lpWaveHdr, sizeof(WAVEHDR));
     *
     */
}

/*******************************************************************

	FUNCTION: IdlePROC(HWWND)

	PURPOSE: routine principale du jeu, affichage et calcul temps réel

*******************************************************************/

	POINT[] xye = new POINT[20];
	POINT[] xy = new POINT[NVISI*8]; // Points projetés à l'écran

public void  IdleProc() throws RoueNeTouchePasSol,ArriveeAtteinte
{
	double tj;

	tj = Simcar.CHRONO() - car.td;
	if ( tj > 0.25)
	{
		tj=0.25;
		car.td = Simcar.CHRONO() - tj;
	}

	if ( car.Key(tj)) {
            if ( SimCarDisplayer.getInstance() != null)
                SimCarDisplayer.getInstance().drawRapport(car.rapport);
                    /*
        	WinGDI.SelectObject(hDCEcr, hf_Vitesse);
		Pr_at.fpr_at( hDCEcr, (TheRect.right*4)/5, (TheRect.bottom*11)/11, car.rapport);
                     *
                     */
        }
        car.td += tj;
        double divisor = 10000.0;
        if ( tj > 0.0) {
            if ( tj < 1.0/divisor) {
                car.calcu_force( tj);
            }
            else {
                int nb = (int)(1+tj*divisor);
                for ( int i=0; i<nb;i++) {
                    car.calcu_force(tj/nb);
                }
            }
/*
            if ( SimCarDisplayer.getInstance() != null)
                SimCarDisplayer.getInstance().drawFrequency((int)(1.0/tj));
 * 
 */
        }
    }
public void drawProc()
    {
    if ( Graph256.lpvBits == null) {
        return;
    }
    Simcar.hDCEcr.g2d = SimCarDisplayer.getInstance().screen.createGraphics();

                //System.out.println("nv="+car.getNV());

	//Point3D[][]  route = Simcar.route;	// Coordonnées du bord de la route en m.
   	int no,  nv2, yaccel_old, yfreins_old;
	int i;
	double x, y, z;

	nv2 = car.CalcuPoints(fast, xy);

	car.CalcuHori( xye);

/*
    i=100;
    if ( xmax>50)
	while(i--) {
		Triangle( 1,1, 1, ymax/2, ymax/2,ymax, 2);
		Triangle( 1,1, ymax/2, ymax/3, ymax/2,ymax, 2);
	}

*/
	xye[2].x=xye[1].x; xye[2].y= xye[1].y-10000;
	xye[3].x= xye[0].x; xye[3].y= xye[0].y-10000;
	Graph256.Polygone(Ciel, xye, 4);
	xye[2].y = xye[1].y+10000;
	xye[3].y= xye[0].y+10000;
	Graph256.Polygone( Sol[13], xye, 4);

	for ( no=NVISI-2; no >=VISI_MIN; no--) {		// Projection des segments.
		i= 6*no;
		xye[0].x =xy[i+1].x; xye[0].y=xy[i+1].y;
		xye[3].x =xy[i+2].x; xye[3].y=xy[i+2].y;
		xye[1].x =xy[i+7].x; xye[1].y=xy[i+7].y;
		xye[2].x =xy[i+8].x; xye[2].y=xy[i+8].y;
		Graph256.Polygone(Route1, xye, 4);	// Route
		xye[0].x =xy[i+1].x; xye[0].y=xy[i+1].y;
		xye[1].x =xy[i+4].x; xye[1].y=xy[i+4].y;
		xye[3].x =xy[i+7].x; xye[3].y=xy[i+7].y;
		xye[2].x =xy[i+10].x; xye[2].y=xy[i+10].y;
		Graph256.Polygone(Sol[9], xye, 4);	// Bas-coté gauche
		xye[0].x =xy[i+2].x; xye[0].y=xy[i+2].y;
		xye[1].x =xy[i+5].x; xye[1].y=xy[i+5].y;
		xye[3].x =xy[i+8].x; xye[3].y=xy[i+8].y;
		xye[2].x =xy[i+11].x; xye[2].y=xy[i+11].y;
		Graph256.Polygone(Sol[9], xye, 4);	// Bas-coté droit
		if ( !fast) {
			xye[0].x =xy[i].x; xye[0].y=xy[i].y;
			xye[1].x =xy[i+4].x; xye[1].y=xy[i+4].y;
			xye[3].x =xy[i+6].x; xye[3].y=xy[i+6].y;
			xye[2].x =xy[i+10].x; xye[2].y=xy[i+10].y;
			Graph256.Polygone(flanc[9], xye, 4);	// Flanc gauche
			xye[0].x =xy[i+3].x; xye[0].y=xy[i+3].y;
			xye[3].x =xy[i+5].x; xye[3].y=xy[i+5].y;
			xye[1].x =xy[i+9].x; xye[1].y=xy[i+9].y;
			xye[2].x =xy[i+11].x; xye[2].y=xy[i+11].y;
			Graph256.Polygone(flanc[9], xye, 4);	// Flanc droit
		}
	}
/*
	nv2 = (int)(car.getNV()+DIST_MIN);	// Affichage à partir de 6m de distance
	if ( nv2<0)
            nv2=0;
*/
	for ( no= VISI_MIN-1; no >= 0; no--) {		// Projection des segments.
		i = 6*no;
		xye[0].x =xy[i+1].x; xye[0].y=xy[i+1].y;
		xye[3].x =xy[i+2].x; xye[3].y=xy[i+2].y;
		xye[1].x =xy[i+7].x; xye[1].y=xy[i+7].y;
		xye[2].x =xy[i+8].x; xye[2].y=xy[i+8].y;
		if (( no+nv2)%2==0)
			Graph256.Polygone(Route1, xye, 4);
		else
			Graph256.Polygone(Route2, xye, 4);
		xye[0].x =xy[i+1].x; xye[0].y=xy[i+1].y;
		xye[1].x =xy[i+4].x; xye[1].y=xy[i+4].y;
		xye[3].x =xy[i+7].x; xye[3].y=xy[i+7].y;
		xye[2].x =xy[i+10].x; xye[2].y=xy[i+10].y;
		Graph256.Polygone(Sol[route[no+nv2][1].coul/16], xye, 4);	// Bas-coté gauche
		xye[0].x =xy[i+2].x; xye[0].y=xy[i+2].y;
		xye[1].x =xy[i+5].x; xye[1].y=xy[i+5].y;
		xye[3].x =xy[i+8].x; xye[3].y=xy[i+8].y;
		xye[2].x =xy[i+11].x; xye[2].y=xy[i+11].y;
		Graph256.Polygone(Sol[route[no+nv2][4].coul/16], xye, 4);	// Bas-coté droit
if ( !fast) {
		xye[0].x =xy[i].x; xye[0].y=xy[i].y;
		xye[1].x =xy[i+4].x; xye[1].y=xy[i+4].y;
		xye[3].x =xy[i+6].x; xye[3].y=xy[i+6].y;
		xye[2].x =xy[i+10].x; xye[2].y=xy[i+10].y;
		Graph256.Polygone(flanc[route[no+nv2][0].coul/16], xye, 4);	// Flanc gauche
		xye[0].x =xy[i+3].x; xye[0].y=xy[i+3].y;
		xye[3].x =xy[i+5].x; xye[3].y=xy[i+5].y;
		xye[1].x =xy[i+9].x; xye[1].y=xy[i+9].y;
		xye[2].x =xy[i+11].x; xye[2].y=xy[i+11].y;
		Graph256.Polygone(flanc[route[no+nv2][5].coul/16], xye, 4);	// Flanc droit
}
	}

	WinGDI.SelectObject(hDCEcr, hf_old);

        SimCarDisplayer.getInstance().drawVitRoue(car.ard.v_rot,RoueEnum.ARD);
        SimCarDisplayer.getInstance().drawVitRoue(car.arg.v_rot,RoueEnum.ARG);
	//Pr_at.pr_at( hDCEcr, 200, TheRect.bottom+10, " ard=" + car.ard.v_rot);
	//Pr_at.pr_at( hDCEcr, 200, TheRect.bottom+20, " arg=" + car.arg.v_rot);
        SimCarDisplayer.getInstance().drawVitRoue(car.avd.v_rot,RoueEnum.AVD);
        SimCarDisplayer.getInstance().drawVitRoue(car.avg.v_rot,RoueEnum.AVG);

	SimCarDisplayer.getInstance().drawGlissement(RoueEnum.AVG, car.avg.glis);//if (car.avg.glis) Pr_at.pr_at(hDCEcr, 0, TheRect.bottom, "*"); else Pr_at.pr_at(hDCEcr,0,TheRect.bottom,"_");
	SimCarDisplayer.getInstance().drawGlissement(RoueEnum.AVD, car.avd.glis);//if (car.avd.glis) Pr_at.pr_at(hDCEcr, 10, TheRect.bottom, "*");else Pr_at.pr_at(hDCEcr,10,TheRect.bottom,"_");
	SimCarDisplayer.getInstance().drawGlissement(RoueEnum.ARG, car.arg.glis);//if (car.arg.glis) Pr_at.pr_at(hDCEcr, 0, TheRect.bottom+20, "*");else Pr_at.pr_at(hDCEcr,0,TheRect.bottom+20,"_");
	SimCarDisplayer.getInstance().drawGlissement(RoueEnum.ARD, car.ard.glis);//if (car.ard.glis) Pr_at.pr_at(hDCEcr, 10, TheRect.bottom+20, "*");else Pr_at.pr_at(hDCEcr,10,TheRect.bottom+20,"_");


	WinGDI.SetDIBitsToDevice( hDCEcr, 0, 0, xmax, ymax,0,0, 0, ymax, Graph256.lpvBits);
/*
	if (!WinGStretchBlt( hDCEcr, 0, 0, xmax*zoomx, ymax*zoomy,
								hWinGDC, 0, 0, xmax, ymax))
		MessageBox( NULL, "Erreur WinG", "SIMCAR", MB_ICONHAND |MB_OK);
*/

	//hf_old=(HFONT)SelectObject(hDCEcr, hf_old);

	//if ( boucle==0) {
//___________________________________________________Affichage du compte_tours.
	WinGDI.SelectObject( hDC_ct, hbm_ct);
	WinGDI.BitBlt( hDCMem_ct_buffer, 0, 0, ct_Rect.right, ct_Rect.bottom, hDC_ct, 0,0);
	WinGDI.SelectObject( hDCMem_ct_buffer, Color.WHITE);
	x = PI*( 0.5 - car.rot_mot/837.7)*1.5 + PI/2.0;
	if ( x < -0.785) x = -0.785;
	y = Math.sin(x);
	z = Math.cos(x);
	xy[0].x = (int)(ct_Rect.right*( 0.5 - z/9.0 - y/25.0));
	xy[0].y = (int)(ct_Rect.right*( 0.5+ y/9.0 - z/25.0));
	xy[1].x = (int)(ct_Rect.right*( 0.5 +z/2.2));
	xy[1].y = (int)(ct_Rect.right*( 0.5 - y/2.2));
	xy[2].x = (int)(ct_Rect.right*( 0.5 - z/9.0 + y/25.0));
	xy[2].y = (int)(ct_Rect.right*( 0.5+ y/9.0 + z/25.0));
	WinGDI.Polygon( hDCMem_ct_buffer, xy, 3);
	WinGDI.BitBlt( hDCEcr, TheRect.right/8,TheRect.bottom*4/5,
	 ct_Rect.right, ct_Rect.bottom, hDCMem_ct_buffer, 0,0);
	//}
	//else if ( boucle==1) {
//__________________________________________Affichage du compteur de vitesse.
	WinGDI.SelectObject( hDC_ct, hbm_vit);
	WinGDI.BitBlt( hDCMem_vit_buffer, 0,0, vit_Rect.right, vit_Rect.bottom, hDC_ct, 0, 0);
	WinGDI.SelectObject( hDCMem_vit_buffer, Color.WHITE);
	x = - PI*car.vg.y*3.6/200.0*2.0 - PI/2.0;
	y = Math.sin(x);
	z = Math.cos(x);
	xy[0].x = (int)(vit_Rect.right*( 0.5 - z/11.0 - y/35.0));
	xy[0].y = (int)(vit_Rect.right*( 0.5+ y/11.0 - z/35.0));
	xy[1].x = (int)(vit_Rect.right*( 0.5 +z/2.2));
	xy[1].y = (int)(vit_Rect.right*( 0.5 - y/2.2));
	xy[2].x = (int)(vit_Rect.right*( 0.5 - z/11.0 + y/35.0));
	xy[2].y = (int)(vit_Rect.right*( 0.5+ y/11.0 + z/35.0));
	WinGDI.Polygon( hDCMem_vit_buffer, xy, 3);
	WinGDI.BitBlt ( hDCEcr, TheRect.right*3/11, TheRect.bottom*3/4, vit_Rect.right, vit_Rect.bottom, hDCMem_vit_buffer,0,0);
	//}
	//else if ( boucle==2) {
//_________________________________________________________Affichage du volant.
	i = (int)(2*NB_VOLANT+(1.0 - car.angle_vol*3.6)*NB_VOLANT/2)%NB_VOLANT;
	WinGDI.SelectObject(hDC_ct, hbm_vol[i]);
	WinGDI.BitBlt( hDCEcr, TheRect.right*5/11, TheRect.bottom*3/4, vit_Rect.right, vit_Rect.bottom, hDC_ct,0,0);
	//}
	//else if ( boucle==3) {
//____________________________________Affichage des pédales de frein et accélérateur.
	yaccel_old = yaccel;
	yfreins_old = yfreins;
	yaccel = (int)(TheRect.bottom - ((TheRect.bottom-1)/5)*car.accel + 0.5);
	yfreins = (int)(TheRect.bottom - ((TheRect.bottom-1)/5)*car.freins + 0.5);
	//SelectObject( hDCEcr, GetStockObject(NULL_PEN));
	if ( yaccel < yaccel_old) {
		WinGDI.SelectObject( hDCEcr, hb_Rouge);
		WinGDI.Rectangle( hDCEcr, TheRect.right*16/24, yaccel-1, TheRect.right*17/24, yaccel_old);
	}
	else if ( yaccel > yaccel_old) {
		WinGDI.SelectObject( hDCEcr, hb_Fond);
		WinGDI.Rectangle( hDCEcr, TheRect.right*16/24, yaccel_old-1, TheRect.right*17/24, yaccel);
	}
	if ( yfreins < yfreins_old) {
		WinGDI.SelectObject( hDCEcr, hb_Rouge);
		WinGDI.Rectangle( hDCEcr, TheRect.right*18/24, yfreins-1, TheRect.right*19/24, yfreins_old);
	}
	else if ( yfreins > yfreins_old) {
		WinGDI.SelectObject( hDCEcr, hb_Fond);
		WinGDI.Rectangle( hDCEcr, TheRect.right*18/24, yfreins_old-1, TheRect.right*19/24, yfreins);
	}
	//}

	double delta = CHRONO() - soundTime;
	if ( delta > SOUND_PERIOD) {
		soundTime = CHRONO();
		Son( car);
	}
        Simcar.hDCEcr.g2d.dispose();
}

public static void paint(SimCarDisplayer sd, Component frame) {

    if ( car==null) {
        return;
    }
    BufferedImage screen = sd.screen;
    HDC hDCEc;

        hDCEc = new HDC(screen.createGraphics());

	RECT BckgrdRect = new RECT();
	int i, j, d_vol;
	double angle_vol;
//	unsigned short* indice;
	POINT[] xy = POINT.alloc(20); // Points projetés à l'écran

        Component hwndApp = frame;

	WinUser.GetClientRect(hwndApp, TheRect);
	WinUser.GetClientRect(hwndApp,BckgrdRect);
	if ( TheRect.bottom*3 > TheRect.right*2)
		TheRect.bottom = (TheRect.right*2)/3;
	if ( TheRect.bottom*3 < TheRect.right*2)
		TheRect.right = (TheRect.bottom*3)/2;
	xmax= TheRect.right/zoomx;
	ymax= (TheRect.bottom*3)/4/zoomy;

	Graph256.scan = ((xmax+3)/4)*4;

	ct_Rect.top = ct_Rect.left = 0;
	ct_Rect.right = ct_Rect.bottom = TheRect.bottom/5;

	vit_Rect.top = vit_Rect.left = 0;
	d_vol = vit_Rect.right = vit_Rect.bottom = TheRect.bottom/4;



	xymax_x = Toolkit.getDefaultToolkit().getScreenSize().width/zoomx;
	xymax_y = Toolkit.getDefaultToolkit().getScreenSize().width/zoomy;

    hbm_vit_buffer = WinGDI.CreateCompatibleBitmap(hDCEc, vit_Rect.right, vit_Rect.bottom);
    hbm_ct_buffer = WinGDI.CreateCompatibleBitmap(hDCEc, ct_Rect.right, ct_Rect.bottom);


		Route1 = WinGDI.GetNearestPaletteIndex( hsyspal, COUL_ROUTE1());
		Route2= WinGDI.GetNearestPaletteIndex( hsyspal, COUL_ROUTE2());
		Ciel = WinGDI.GetNearestPaletteIndex( hsyspal, COUL_CIEL());

		for ( i=0; i< 16; i++ ) {
				flanc[i] = flanc(i);
				Sol[i] = sol(i);
		}
/*
		pinfo->bmiColors[Route1].rgbBlue = GetBValue(COUL_ROUTE1);
		pinfo->bmiColors[Route1].rgbGreen = GetGValue(COUL_ROUTE1);
		pinfo->bmiColors[Route1].rgbRed = GetRValue(COUL_ROUTE1);
		pinfo->bmiColors[Route1].rgbReserved = 0;

		pinfo->bmiColors[Route2].rgbBlue = GetBValue(COUL_ROUTE2);
		pinfo->bmiColors[Route2].rgbGreen = GetGValue(COUL_ROUTE2);
		pinfo->bmiColors[Route2].rgbRed = GetRValue(COUL_ROUTE2);
		pinfo->bmiColors[Route2].rgbReserved  = 0;

		pinfo->bmiColors[Ciel].rgbBlue = GetBValue(COUL_CIEL);
		pinfo->bmiColors[Ciel].rgbGreen = GetGValue(COUL_CIEL);
		pinfo->bmiColors[Ciel].rgbRed = GetRValue(COUL_CIEL);
		pinfo->bmiColors[Ciel].rgbReserved = 0;
*/
                /*
		for ( i=0; i<256; i++)
		{
//			indice[i]=i;
			couleur[i] = 257*i;
		}
                 *
                 */
		//hBits = GlobalAlloc(GPTR, long(scan*4)*long(ymax));
		//lpvBits = (char _huge*) GlobalLock( hBits);
                Graph256.lpvBits = new BufferedImage(Graph256.scan,ymax,BufferedImage.TYPE_INT_RGB);
/*
		hDC_ct=CreateCompatibleDC(ps.hdc);
 */
		WinGDI.SelectObject( hDCMem_vit_buffer, hbm_vit_buffer);
		WinGDI.SelectObject( hDCMem_ct_buffer, hbm_ct_buffer);
                /*
		hp_old=(HPEN)SelectObject(hDCMem, GetStockObject(NULL_PEN));
		hb_old=(HBRUSH)SelectObject(hDCMem, GetStockObject(NULL_BRUSH));

		if ( hf_Vitesse=CreateFont( TheRect.bottom/3, 0, 0, 0, 0, 0,
							 0, 0, 0, OUT_TT_ONLY_PRECIS,
							 CLIP_DEFAULT_PRECIS,DEFAULT_QUALITY,
							 DEFAULT_PITCH | FF_DONTCARE,"Arial"))
			hf_old = (HFONT)SelectObject( hDCEcr, hf_Vitesse);
		else
			hf_old = NULL;
*/
                hf_Vitesse = new Font(Font.SANS_SERIF,Font.PLAIN,TheRect.bottom/3);
                hf_old =  new Font(Font.SANS_SERIF,Font.PLAIN,10);
                hDCEc.g2d.setFont(hf_Vitesse);
		hb_Rouge = WinGDI.CreateSolidBrush( WinGDI.PALETTERGB(255,0,0));
		hb_Fond = WinGDI.CreateSolidBrush( COUL_FOND());

//______________________________________________________Dessin du compte-tours.
		if ( (hbm_ct = WinGDI.CreateCompatibleBitmap( ct_Rect.right, ct_Rect.bottom))!=null) {
                    HDC hdcct = new HDC();
			WinGDI.SelectObject( hdcct, hbm_ct);
			WinUser.FillRect( hdcct, ct_Rect, hb_Fond);
			WinGDI.SelectObject( hdcct, Color.BLACK);
			WinGDI.Ellipse( hdcct, 0,0, ct_Rect.right, ct_Rect.bottom);
			WinGDI.SelectObject( hdcct, hb_Rouge);
			WinGDI.Pie( hdcct, ct_Rect.left, ct_Rect.top,
			  ct_Rect.right, ct_Rect.bottom, 45,
			  -30);
			WinGDI.SelectObject( hdcct, Color.BLACK);
			WinGDI.Ellipse( hdcct, ct_Rect.right/5, ct_Rect.bottom/5,
			 ct_Rect.right*4/5, ct_Rect.bottom*4/5);
                        hdcct.g2d.dispose();
		}
//_________________________________________________Dessin du compteur de vitesse.
                
		if ( (hbm_vit = WinGDI.CreateCompatibleBitmap( vit_Rect.right, vit_Rect.bottom))!=null) {
                    HDC hdcvit = new HDC();
			WinGDI.SelectObject( hdcvit, hbm_vit);
			WinUser.FillRect( hdcvit, vit_Rect, hb_Fond);
			WinGDI.SelectObject( hdcvit, Color.BLACK);
			WinGDI.Ellipse( hdcvit, 0,0, vit_Rect.right, vit_Rect.bottom);
                        hdcvit.g2d.dispose();
		}               
//___________________________________________________________Dessin du volant.
		for ( i=0; i< NB_VOLANT; i++) {
			hbm_vol[i] = WinGDI.CreateCompatibleBitmap( d_vol, d_vol);
                        HDC hdcvol = new HDC();
			WinGDI.SelectObject( hdcvol, hbm_vol[i]);
			WinUser.FillRect( hdcvol, vit_Rect, hb_Fond);
			WinGDI.SelectObject( hdcvol, Color.BLACK);
			WinGDI.Ellipse( hdcvol, 0,0, d_vol, d_vol);
			angle_vol= Math.PI/2.0*(1.0 +(double)(4*(i-NB_VOLANT/2))/NB_VOLANT);
			WinGDI.SelectObject( hdcvol, hb_Fond);
			WinGDI.Ellipse( hdcvol, (int)(d_vol*0.08+.5),(int)( d_vol*0.08+.5),
			(int) (d_vol*0.92+.5), (int)(d_vol*0.92+.5));
			WinGDI.SelectObject( hdcvol, Color.BLACK);
			xy[0].x = (int)(d_vol/2.0*( 1.0 + Math.sin( angle_vol)*.85)+ d_vol*Math.cos( angle_vol)/10.0);
			xy[0].y = (int)(d_vol/2.0*( 1.0 - Math.cos( angle_vol)*.85)+ d_vol*Math.sin( angle_vol)/10.0);
			xy[1].x = xy[0].x + (int)(d_vol*Math.cos( angle_vol)/12.0);
			xy[1].y = xy[0].y + (int)(d_vol*Math.sin( angle_vol)/12.0);
			xy[3].x = (int) ( d_vol/2.0*( 1.0 - Math.sin( angle_vol)*.85)+ d_vol*Math.cos( angle_vol)/10.0);
			xy[3].y = (int)( d_vol/2.0*( 1.0 + Math.cos( angle_vol)*.85)+ d_vol*Math.sin( angle_vol)/10.0);
			xy[2].x = xy[3].x + (int)( d_vol*Math.cos( angle_vol)/12.0);
			xy[2].y = xy[3].y + (int)( d_vol*Math.sin( angle_vol)/12.0);
			WinGDI.Polygon( hdcvol, xy, 4);
			for ( j= -1; j<2; j++)
				WinGDI.SetPixel( hdcvol, (int)(d_vol/2.0*( 1.0+Math.sin( angle_vol+j*2-Math.PI/2.0)*0.88)),
				 (int)(d_vol/2.0*(1.0-Math.cos( angle_vol+j*2-Math.PI/2.0)*0.88)), WinGDI.RGB(255,255,255));
                        hdcvol.g2d.dispose();
		}

		WinGDI.SelectObject( hDCEc, hb_Fond);
		WinUser.FillRect( hDCEc, BckgrdRect, hb_Fond);
		WinGDI.SetBkColor( hDCEc,COUL_FOND());
                sd.drawRapport(car.rapport);
//		Pr_at.fpr_at( hDCEcr, (TheRect.right*4)/5, (TheRect.bottom*11)/11, car.rapport);
		yaccel = (int)(TheRect.bottom - ((TheRect.bottom-1)/5)*car.accel + 0.5);
		yfreins = (int)(TheRect.bottom - ((TheRect.bottom-1)/5)*car.freins + 0.5);
		WinGDI.SelectObject( hDCEc, Color.BLACK);
		WinGDI.Rectangle( hDCEc, TheRect.right*16/24-1, TheRect.bottom*4/5-1,
		 TheRect.right*17/24, TheRect.bottom);
		WinGDI.Rectangle( hDCEc, TheRect.right*18/24-1, TheRect.bottom*4/5-1,
		 TheRect.right*19/24, TheRect.bottom);

                hDCEc.g2d.dispose();
}


}
