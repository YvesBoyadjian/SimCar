/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package simcar.voiture;

/**
**********************************************************************
		Caractéristiques techniques constantes représentatives de la voiture.
**********************************************************************
 *
 * @author YvesFabienne
 */
public class Caracteristiques {

	public double	jmot;		// Moment d'inertie du moteur = m.N.s2
	public double	cmot_ral;	// Couple moteur max au régime de ralenti = m.N
	public double	cmot_max;	// Couple moteur maximum = m.N
	public double	cmot;		// Couple moteur maximum au régime maximum = m.N
	public double	reg_ral;		// Régime de ralenti = rad/s
	public double	reg_cmot_max;	// Régime de couple maximum = rad/s
	public double	reg_max;		// Régime de puissance maximum = rad/s
	public double	reg_max_vide;	// Régime maximum à vide, accélérateur à fond = rad/s

	public double	coef_frein_mot;	// Coefficient de frottement moteur = m.N.s
	public double	coef_mot1;	// Coefficient de motricité à bas régime ( Usage interne)
	public double	coef_mot2;	// Coefficient de motricité à haut régime ( Usage interne)
	public double	coef_mot3;	// Coefficient de motricité en surrégime ( Usage interne)

	public double[]	rap_boite = new double[8];		// Tableau des rapports de boite, rap_boite[1] correspond au point mort
	public double	rayon;	// Rayon de la bande de roulement = m
	public double	jpont;	// Moment d'inertie du pont avant = m.N.s2

	public double	flexi_av;	// Flexibilité à la roue avant = m/N
	public double	flexi_ar;	// Flexibilité à la roue arrière = m/N
	public double	elong_av, elong_ar;	// Elongation maximum ( Usage interne) = m
	public double	barre_av, barre_ar;	// Flexibilité de la barre anti-roulis = m/N
	public double	amort_av, amort_ar;	// Efficacité des amortisseurs = N.s/m
	public double	rep_frein;	// Répartition de freinage sur les roues avant = 0< ss dim <1
	public double	coef_sec;	// Coefficient d'adhérence sur sol sec
	public double	coef_mouil;	// Coefficient d'adhérence sur sol mouillé
	public double	coef_herbe;	// Coefficient d'adhérence sur les bords de la route
	public double	poids_av;		// Poids sur l'essieu avant = kg
	public double	poids_ar;		// Poids sur l'essieu arrière = kg
	public double	mx, my, mz;		// Moments principaux d'inertie en Kg.m2
	public double	poids;			// Poids total au repos ( Usage interne) = kg
	public double	braq_max;	// Angle de braquage maximum des roues avant ( Positif)
	public double	trainee;	// Trainée aérodynamique = N.s2/m2
	public double	res_roul;	// Résistance de roulement = N

	public double	alti_obs;		// Altitude de l'oeil du conducteur par rapport au centre d'inertie = m

        protected double	alti_g;			// Altitude du centre d'inertie au repos = m
	protected double	voie;		// Voies = m
	protected double	empat;		// Empattement = m
	protected double	empat_av;	// Distance essieu avant - centre d'inertie.	( Usage interne)
	protected double	empat_ar;	// Distance essieu arrière - centre d'inertie.	( Usage interne)

        public	void Make305SR() {
	jmot = 0.2;
	jpont = 1.0;	// Valeur estimee
	cmot_ral= 88;
	cmot_max=114.5;	// 1 m.kg = 9.7 N.m
	cmot = 86.58;
	reg_ral= 94.25; // 900 tr/min
	reg_cmot_max=314.2;
	reg_max=628.3;
	reg_max_vide=800.0;

	coef_frein_mot= 1.6 * ( cmot_max - cmot) / (reg_cmot_max - reg_max);

	rap_boite[0] = - 0.0693;
	rap_boite[1] = 0.0;
	rap_boite[2] = 0.0714;
	rap_boite[3] = 0.1234;
	rap_boite[4] = 0.1882;
	rap_boite[5] = 0.2772;

	rayon = 0.295;
	flexi_av = 8.144e-5;
	flexi_ar = 6.495e-5;
	barre_av = 8.0e-5;	// Valeur estimee
	barre_ar = 12.0e-5;	// Valeur estimee
	amort_av = 1000.0;	// Valeur estime
	amort_ar = 1000.0;	// Valeur estimee
	rep_frein = 0.7;
//_________________Source : Science & Vie N°776
	coef_sec = 0.9;
	coef_mouil = 0.4;
	coef_herbe = 0.2;
	poids_av = 593.0;
	poids_ar = 464.0;
	mx = (poids_av + poids_ar)*4.2*4.2/12.0;
	my = (poids_av + poids_ar)*1.9*1.9/12.0;
	mz = (poids_av + poids_ar)*4.4*4.4/12.0;

	braq_max= 0.672;
	alti_g = 0.55;	// Valeur estimee
	alti_obs = 1.16 - alti_g;	// Oeil à 1m16 du sol
	empat = 2.62;
	voie = 1.37;
	trainee = 0.5;
	res_roul = 100.0;
        }
}
