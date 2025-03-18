import {
  Component,
  OnInit,
  CUSTOM_ELEMENTS_SCHEMA,
  ChangeDetectorRef,
  ChangeDetectionStrategy,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { TerrainService } from '../../../services/terrain.service';
import { FeuilleDeMatchDTO } from '../../../../feuille-de-match/models/feuille-de-match.model';
import { TerrainDTO } from '../../../models/terrain.model';
import { JoueurSelectionDialogComponent } from '../../../components/../../joueur/components/joueur-selection-dialog/joueur-selection-dialog.component';
import { PosteUtils, CategoriePoste } from '../../../../../shared/models/posteUtils';
import { EvenementMatchDTO, RemplacementDTO } from '../../../models/rencontre.model';
import {
  TimePlay,
  fromPercentage,
  getTimePlayLabel,
} from '../../../../../shared/models/time-play.enum';
import { CoteAttributionDialogComponent } from '../../../../../shared/components/cote/cote-attribution-dialog.component';
import { ClotureRencontreDTO } from '../../../../../modules/rencontre/models/rencontre.model';
import { Division } from '../../../../../shared/models/division.enum';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { RouterModule } from '@angular/router';
import { TimePlaySelectionDialogComponent } from '../../../../../shared/components/time-play-selection-dialog/time-play-selection-dialog.component';
import { SelectionPasseurDialogComponent } from '../../../../../shared/components/selection-passeur-dialog/selection-passeur-dialog.component';
import { MatSelectModule } from '@angular/material/select';
import { MatSelectChange } from '@angular/material/select'; // ✅ Vérifie que c'est bien importé
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { Client, Message } from '@stomp/stompjs';
import { Injectable, OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs';
import { StompSubscription } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-detail-terrain',
  standalone: true,
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    CommonModule,
    FormsModule,
    MatChipsModule,
    MatIconModule,
    RouterModule,
    MatCardModule,
    MatDialogModule,
    MatSelectModule, // ✅ Assure-toi qu'il est bien là
  ],
  templateUrl: './detail-terrain.component.html',
  styleUrls: ['./detail-terrain.component.scss'],
})
export class DetailTerrainComponent implements OnInit {
  // ===============================
  // 1️⃣ Initialisation et Chargement des données
  // ===============================
  public terrain: TerrainDTO | null = null;
  // ✅ Identifiants et noms des équipes

  public idRencontre!: number;
  public idChampionnat!: number;
  public nomEquipe: string = ''; // ✅ Nom de l'équipe principale
  public nomAdversaire: string = ''; // ✅ Nom de l'adversaire
  public divisionAdversaire: string = ''; // ✅ Division de l'adversaire

  // ✅ Score du match
  public butEquipe: number = 0;
  public butAdversaire: number = 0;

  // ✅ Joueurs du match
  public titulaires: FeuilleDeMatchDTO[] = []; // ✅ Liste des titulaires
  public remplacants: FeuilleDeMatchDTO[] = []; // ✅ Liste des remplaçants
  public terrainJoueurs: { [poste: string]: FeuilleDeMatchDTO | null } = {}; // ✅ Position des joueurs

  // ✅ Statistiques modifiées
  public butsModifies: { [key: number]: number } = {}; // ✅ Buts enregistrés manuellement
  public passesModifies: { [key: number]: number } = {}; // ✅ Passes enregistrées manuellement
  public minutesJouees: { [key: number]: number } = {}; // ✅ Minutes jouées pour chaque joueur

  // ✅ Autres variables nécessaires
  public butsMarques: EvenementMatchDTO[] = []; // ✅ Historique des buts marqués
  public postesFormation: { defense: string[]; milieu: string[]; attaque: string[] } = {
    defense: [],
    milieu: [],
    attaque: [],
  }; // ✅ Postes de la formation active
  public formationActive: string = '433'; // ✅ Formation en cours
  public formationPrecedente: string | null = null; // ✅ Sauvegarde de la formation précédente
  public posteSelectionne: string | null = null; // ✅ Poste sélectionné pour modification
  public joueurSelectionne: FeuilleDeMatchDTO | null = null; // ✅ Joueur sélectionné pour modification
  public postesLibresCount: number = 0; // ✅ Variable pour stocker le nombre de postes libres
  afficherHistorique: boolean = false;


  // ✅ Variables nécessaires aux remplacement remplacementDTO
  joueurEntrant: FeuilleDeMatchDTO | null = null;
  joueurSortant: FeuilleDeMatchDTO | null = null;
  minuteRemplacement: number | null = null;

  // ✅ Variables nécessaires aux passe
  passeursDisponibles: FeuilleDeMatchDTO[] = [];
  passeurSelectionne: FeuilleDeMatchDTO | null = null;

  // ✅ Methode pour Enum TimePlay
  timePlayValues: number[] = Object.values(TimePlay)
    .filter((value) => typeof value === 'number') // Exclure les clés de l'enum
    .sort((a, b) => b - a) as number[]; // Trier de 1.0 à 0.0

  getTimePlayLabel(value: number): string {
    return getTimePlayLabel(value as TimePlay);
  }

  private stompClient!: Client;
  private terrainSubscription!: StompSubscription;
  private historiqueSubscription!: StompSubscription;
  private statsSubscription!: StompSubscription;
  private championnatSubscription!: StompSubscription;

  constructor(
    public route: ActivatedRoute,
    private terrainService: TerrainService,
    private dialog: MatDialog,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.idRencontre = Number(this.route.snapshot.paramMap.get('idRencontre'));
    if (!this.idRencontre) {
      console.error('❌ ID de rencontre introuvable.');
      return;
    }

    console.log('📌 Chargement du composant...');

    // 📌 Définir une formation par défaut si aucune n'est définie
    if (!this.formationActive) {
      this.formationActive = '433';
    }

    console.log('📌 Formation par défaut chargée :', this.formationActive);

    // ✅ Assurer que l'ancienne formation est bien NULL au premier chargement
    this.formationPrecedente = null;

    // ✅ Récupère les données du terrain
    this.chargerTerrain();

    // ✅ Connexion WebSocket
    this.connecterWebSocket();

    // ✅ Mise à jour de l'ancienne formation APRES la première répartition des joueurs
    setTimeout(() => {
      this.disposerJoueursNaturellement();
      this.formationPrecedente = this.formationActive; // ✅ Mise à jour après premier affichage
      this.cdr.detectChanges();
    }, 100);
  }

  /**
   * 📌 Charge toutes les données du terrain depuis l'API et met à jour l'affichage.
   * ✅ Récupère toutes les données nécessaires : titulaires, remplaçants, historique des buts, minutes jouées...
   * ✅ Vérifie que `nomEquipe`, `nomAdversaire`, et `divisionAdversaire` sont bien assignés.
   */
  private chargerTerrain(): void {
    console.log(`📡 Chargement des données pour ID=${this.idRencontre}`);

    forkJoin({
      terrain: this.terrainService.getTerrain(this.idRencontre),
      historique: this.terrainService.getHistoriqueEvenements(this.idRencontre),
    }).subscribe({
      next: ({ terrain, historique }) => {
        console.log('✅ Données complètes reçues :', { terrain, historique });

        // ✅ Vérification des données reçues avant assignation
        if (!terrain) {
          console.error("❌ Terrain non reçu depuis l'API.");
          return;
        }

        // ✅ **Assignation des données du terrain**
        this.terrain = terrain;
        this.terrainJoueurs = { ...terrain.terrainJoueurs };
        this.butAdversaire = terrain.butAdversaire;
        this.butEquipe = terrain.butEquipe;
        this.nomEquipe = terrain.nomEquipe; // ✅ Ajout récupération du nom de l'équipe
        this.nomAdversaire = terrain.nomAdversaire; // ✅ Ajout récupération du nom de l'adversaire
        this.divisionAdversaire = terrain.divisionAdversaire; // ✅ Ajout récupération de la division de l'adversaire
        this.remplacants = [...terrain.remplacants];
        this.butsMarques = historique;

        // ✅ **Assurer que la formation est bien définie**
        if (!this.formationActive) {
          this.formationActive = '433'; // 🔥 Définit la formation par défaut si ce n'est pas encore fait
        }

        // ✅ **Vérification et assignation des statistiques modifiées (buts, passes, minutes)**
        this.terrain.butsModifies ??= {};
        this.terrain.passesModifies ??= {};
        this.terrain.minutesJouees ??= {};

        console.log('📌 📌 Mise à jour des statistiques :', {
          butsModifies: this.terrain.butsModifies,
          passesModifies: this.terrain.passesModifies,
          minutesJouees: this.terrain.minutesJouees,
        });

        console.log(`📌 📌 Informations du terrain récupérées : 
        🏆 Équipe: ${this.nomEquipe} 
        ⚔️ Adversaire: ${this.nomAdversaire} (Division: ${this.divisionAdversaire})
        🥅 Score: ${this.butEquipe} - ${this.butAdversaire}`);

        // ✅ **Mise à jour des postes après récupération des données**
        this.mettreAJourPostesFormation();

        // 🔥 **Attendre un cycle Angular avant de répartir les joueurs**
        setTimeout(() => {
          this.disposerJoueursNaturellement();
          this.cdr.detectChanges(); // 🔄 Mise à jour forcée après disposition
        }, 500);

        console.log('📌 📌 Terrain et joueurs mis à jour avec succès !');
      },
      error: (err) => console.error('❌ Erreur lors du chargement du terrain :', err),
    });
  }

  connecterWebSocket(): void {
    // 🔥 Utilisation de SockJS
    const socket = new SockJS('http://localhost:8080/ws');

    this.stompClient = new Client({
      webSocketFactory: () => socket, // ✅ Connexion via SockJS
      debug: (msg: string) => console.log('📡 WebSocket:', msg),
      reconnectDelay: 5000, // 🚀 Reconnexion automatique
    });

    this.stompClient.onConnect = () => {
      console.log('✅ Connecté au WebSocket STOMP via SockJS');

      // 🔥 Écoute des mises à jour du terrain
      this.terrainSubscription = this.stompClient.subscribe(
        `/topic/terrain/${this.idRencontre}`,
        (message) => {
          const terrainMisAJour: TerrainDTO = JSON.parse(message.body);
          console.log('📡 Mise à jour du terrain reçue :', terrainMisAJour);
          this.terrain = { ...terrainMisAJour };
          this.terrainJoueurs = { ...terrainMisAJour.terrainJoueurs };
          this.cdr.detectChanges();
        },
      );
    };

    this.stompClient.onStompError = (frame) => {
      console.error('❌ Erreur STOMP', frame);
    };

    this.stompClient.activate();
  }

  ngOnDestroy(): void {
    if (this.terrainSubscription) {
      this.terrainSubscription.unsubscribe();
    }
    if (this.historiqueSubscription) {
      this.historiqueSubscription.unsubscribe();
    }
    if (this.statsSubscription) {
      this.statsSubscription.unsubscribe();
    }
    if (this.championnatSubscription) {
      this.championnatSubscription.unsubscribe();
    }
    if (this.stompClient) {
      this.stompClient.deactivate();
    }
  }

  // ===============================
  // 2️⃣ Gestion du score et des passes
  // ===============================

  /**
   * 📌 Incrémente le nombre de buts d’un joueur et ouvre la sélection du passeur.
   */
  incrementerBut(event: Event, joueur: FeuilleDeMatchDTO): void {
    event.stopPropagation(); // ✅ Empêche le clic d'affecter d'autres éléments

    if (!joueur || !joueur.jid) {
      console.error("❌ Impossible d'incrémenter le but : joueur null ou inexistant.");
      return;
    }

    if (!this.terrain) {
      console.error("❌ Erreur : Terrain non chargé, impossible d'incrémenter le but.");
      return;
    }

    // ✅ Initialiser `butsModifies` si ce n'est pas encore fait
    this.terrain.butsModifies ??= {};

    // ✅ Incrémente le nombre de buts du joueur
    this.terrain.butsModifies[joueur.jid] = (this.terrain.butsModifies[joueur.jid] || 0) + 1;

    console.log(
      `⚽ But ajouté pour ${joueur.nom} (Total : ${this.terrain.butsModifies[joueur.jid]})`,
    );

    this.joueurSelectionne = joueur;
    this.cdr.detectChanges();

    // ✅ Affiche directement la sélection du passeur
    this.selectionnerPasseur();
    
  }

  /**
   * 📌 Sélectionne un passeur après un but marqué (optionnel).
   */
  selectionnerPasseur(): void {
    if (!this.joueurSelectionne) {
      console.error('❌ Aucun joueur sélectionné pour le but.');
      return;
    }

    const joueurBut = this.joueurSelectionne; // ✅ Stocke la référence locale
    const joueursProposables = this.getJoueursDisponiblesPourPasse(joueurBut.jid);

    const dialogRef = this.dialog.open(SelectionPasseurDialogComponent, {
      data: { joueurs: joueursProposables },
      width: '400px',
    });

    dialogRef.afterClosed().subscribe((passeurSelectionne: FeuilleDeMatchDTO | null) => {
      if (passeurSelectionne) {
        this.envoyerButEtPasseur(passeurSelectionne); // ✅ Envoie avec passeur
      } else {
        console.log(`⚽ But validé pour ${joueurBut.nom} sans passeur.`);
        this.envoyerButEtPasseur(null); // ✅ Envoie sans passeur
      }
    });
  }

  envoyerButEtPasseur(passeur: FeuilleDeMatchDTO | null): void {
    if (!this.terrain) {
      console.error("❌ Erreur : Impossible d'envoyer le but car le terrain est null.");
      return;
    }
    if (!this.joueurSelectionne) return;

    console.log('📡 Envoi requête updateStatsEnTempsReel :', {
      idRencontre: this.terrain.idRencontre,
      idFeuilleMatch: this.joueurSelectionne.jid,
      buts: 1,
      idPasseurFeuille: passeur ? passeur.jid : null,
    });

    // ✅ Met à jour immédiatement le score localement
    this.terrain.butEquipe += 1;

    // ✅ Simule une minute fictive en fonction du nombre d’événements
    const minuteSimulee = (this.butsMarques.length + 1) * 5; // Exemple : 5min par but fictif

    // ✅ Ajoute immédiatement l'événement au tableau `butsMarques`
    this.butsMarques.push({
      idRencontre: this.terrain.idRencontre,
      idJoueur: this.joueurSelectionne.jid,
      nomJoueur: this.joueurSelectionne.nom,
      minute: minuteSimulee,
      typeEvenement: 'BUT',
      idPasseur: passeur ? passeur.jid : undefined,
      nomPasseur: passeur ? passeur.nom : '❌ Aucun passeur',
    });

    this.cdr.markForCheck(); // ✅ Met à jour l'affichage immédiatement

    // ✅ Envoi au backend pour la mise à jour officielle
    this.terrainService
      .updateStatsEnTempsReel(
        this.terrain.idRencontre,
        this.joueurSelectionne.jid,
        1,
        passeur ? passeur.jid : null,
      )
      .subscribe({
        next: (terrainDTO) => {
          console.log('✅ But et passe mis à jour dans le backend :', terrainDTO);
          this.terrain = terrainDTO; // ✅ Met à jour l'objet principal
          this.chargerTerrain(); // ✅ Recharge le terrain après mise à jour
          this.cdr.markForCheck(); // ✅ Rafraîchit l'affichage
        },
        error: (err) => console.error('❌ Erreur enregistrement but :', err),
      });
  }

  /**
   * 📌 Met à jour le score adverse.
   */
  mettreAJourButAdversaire(nouveauScore: number): void {
    if (!this.terrain) {
      console.error('❌ Erreur : Impossible de mettre à jour le score, le terrain est null.');
      return;
    }

    this.butAdversaire = nouveauScore;

    this.terrainService.mettreAJourButAdversaire(this.terrain.idRencontre, nouveauScore).subscribe({
      next: () => {
        
        // ✅ Envoi automatique des stats gardien dès qu'un but est encaissé
        this.envoyerStatsGardien();
        console.log('✅ Stats but adverse et but encaisser mise à jour.');
        this.chargerTerrain(); // ✅ Recharge le terrain après mise à jour
      },
      error: (err) => console.error('❌ Erreur envoi stats gardien :', err),
    });

    this.cdr.detectChanges();
  }

  // ===============================
  // 3️⃣ Gestion des remplacements
  // ===============================

  /**
   * 📌 Ouvre la sélection des joueurs pouvant remplacer un joueur sur le terrain.
   * ✅ `posteKey` est traduit en **poste réel** avant de chercher un joueur.
   * ✅ On utilise `joueurActuel.poste` pour la recherche.
   * ✅ Ne prend que les joueurs SUR LE TERRAIN.
   * ✅ Vérifie la compatibilité via `getPostesCompatibles()`.
   * ❌ Exclut le joueur actuel.
   */
  ouvrirSelectionJoueur(posteKey: string): void {
    this.posteSelectionne = posteKey;

    // ✅ Vérifier que le terrain est bien chargé
    if (!this.terrain || !this.terrain.terrainJoueurs) {
      console.error("❌ Impossible d'ouvrir la sélection : terrain ou terrainJoueurs est null.");
      return;
    }

    // ✅ **Récupérer le joueur actuellement en `posteKey`**
    const joueurActuel = this.terrainJoueurs[posteKey] ?? null;

    if (!joueurActuel) {
      console.warn(`⚠️ Aucun joueur trouvé pour ${posteKey}`);
      return;
    }

    console.log(`📌 [DEBUG] Joueur en ${posteKey} : ${joueurActuel.nom} (${joueurActuel.poste})`);

    // 🔍 **1️⃣ Trouver les postes compatibles avec son poste réel**
    const postesCompatibles = this.getPostesCompatibles(joueurActuel.poste);
    console.log(`📌 [DEBUG] Postes compatibles pour ${joueurActuel.poste} :`, postesCompatibles);

    // 🔄 **2️⃣ Récupérer les joueurs sur le terrain pouvant être compatibles**
    const joueursProposables = Object.values(this.terrain.terrainJoueurs).filter(
      (joueur) =>
        joueur !== null &&
        joueur !== undefined &&
        joueur.jid !== joueurActuel.jid && // ✅ Exclure uniquement le joueur actuel
        postesCompatibles.includes(joueur.poste), // ✅ Vérifier la compatibilité
    );

    console.log(
      `📌 [DEBUG] Joueurs compatibles pour remplacer ${joueurActuel.nom} (${joueurActuel.poste}) :`,
      joueursProposables,
    );

    // 📌 Si aucun joueur disponible, afficher une alerte
    if (joueursProposables.length === 0) {
      alert(`❌ Aucun joueur disponible pour remplacer ${joueurActuel.nom}`);
      return;
    }

    // 📌 Ouvrir la boîte de dialogue avec la liste des joueurs disponibles
    const dialogRef = this.dialog.open(JoueurSelectionDialogComponent, {
      data: { joueurs: joueursProposables },
    });

    dialogRef.afterClosed().subscribe((joueurSelectionne: FeuilleDeMatchDTO) => {
      if (joueurSelectionne && joueurSelectionne.id !== -1) {
        console.log(`📌 [DEBUG] Joueur sélectionné : ${joueurSelectionne.nom}`);
        this.echangerJoueurs(joueurSelectionne, posteKey); // ✅ On garde posteKey
      }
    });
  }

  /**
   * 📌 Possibilité de réorganiser les joueurs sur le terrain
   */
  echangerJoueurs(joueurEntrant: FeuilleDeMatchDTO, posteCibleKey: string): void {
    if (!this.terrain || !this.terrainJoueurs) return;

    // 🔍 Vérifie qui est déjà au poste cible
    const joueurRemplace = this.terrainJoueurs[posteCibleKey] || null;

    console.log('📌  echangerJoueurs Debug: TerrainJoueurs actuel', this.terrain.terrainJoueurs);
    // 🔍 Trouver où joue actuellement le joueur entrant
    const posteActuelKey = Object.keys(this.terrainJoueurs).find(
      (key) => this.terrainJoueurs[key]?.id === joueurEntrant.id,
    );

    console.log(
      `🔄 Échange : ${joueurEntrant.nom} (${posteActuelKey || 'remplaçants'}) ↔ ${joueurRemplace?.nom || 'aucun'} (${posteCibleKey})`,
    );

    // ✅ Retirer le joueur entrant de la liste des disponibles
    this.remplacants = this.remplacants.filter((j) => j.id !== joueurEntrant.id);

    if (posteActuelKey) {
      // 🛠 **Si le joueur entrant était déjà sur le terrain, on échange les deux joueurs**
      this.terrainJoueurs[posteActuelKey] = joueurRemplace; // L'ancien joueur prend la place du joueur entrant
    } else if (joueurRemplace) {
      // 📌 **Si le joueur remplacé était déjà sur le terrain, il va en disponibles**
      this.remplacants.push(joueurRemplace);
    }

    // ✅ **Placer le joueur entrant au poste sélectionné**
    this.terrainJoueurs[posteCibleKey] = joueurEntrant;

    // 🔄 Forcer Angular à détecter les changements
    this.cdr.detectChanges();
  }

  /**
   * 📌 Gère le processus de remplacement en ouvrant la boîte de dialogue avec les joueurs compatibles.
   * ✅ Seuls les **remplaçants** sont proposés.
   * ✅ Ils doivent avoir **0 minutes jouées**.
   * ✅ La compatibilité est vérifiée avec le **poste réel** du joueur sortant.
   */
  ouvrirSelectionRemplacement(joueurSortant: FeuilleDeMatchDTO): void {
    if (!joueurSortant) {
      alert('❌ Sélectionnez un joueur titulaire à remplacer !');
      return;
    }

    // 🔥 **ASSIGNATION DU JOUEUR SORTANT**
    this.joueurSortant = joueurSortant;
    console.log(`✅ [DEBUG] Joueur sortant stocké :`, this.joueurSortant);

    console.log(`📌 [DEBUG] Tentative de remplacement du joueur : ${joueurSortant.nom}`);

    // ✅ **Récupérer le poste réel du joueur sortant**
    const posteReel = this.terrainJoueurs[joueurSortant.poste]?.poste ?? joueurSortant.poste;

    if (!posteReel) {
      console.warn(`⚠️ Impossible de récupérer le poste réel de ${joueurSortant.nom}`);
      return;
    }

    console.log(`📌 [DEBUG] Poste réel de ${joueurSortant.nom} → ${posteReel}`);

    // 🔍 **1️⃣ Trouver les postes compatibles avec ce poste réel**
    const postesCompatibles = this.getPostesCompatibles(posteReel);
    console.log(`📌 [DEBUG] Postes compatibles pour ${posteReel} :`, postesCompatibles);

    // 🔄 **2️⃣ Filtrer uniquement les remplaçants compatibles ET qui n'ont pas encore joué**
    const joueursProposables = this.remplacants.filter(
      (joueur) =>
        joueur !== null &&
        joueur !== undefined &&
        joueur.minutesJouees === 0 && // ✅ Vérification que le joueur n'a PAS encore joué
        postesCompatibles.includes(joueur.poste), // ✅ Vérification de compatibilité
    );

    console.log(
      `📌 [DEBUG] Joueurs remplaçants compatibles pour ${joueurSortant.nom} (${posteReel}) :`,
      joueursProposables,
    );

    if (joueursProposables.length === 0) {
      alert(`❌ Aucun remplaçant disponible pour ce poste.`);
      return;
    }

    // 📌 **Ouvrir la boîte de dialogue avec la liste des joueurs disponibles**
    const dialogRef = this.dialog.open(JoueurSelectionDialogComponent, {
      data: { joueurs: joueursProposables },
    });

    dialogRef.afterClosed().subscribe((joueurSelectionne: FeuilleDeMatchDTO) => {
      if (joueurSelectionne) {
        console.log(`📌 [DEBUG] Remplaçant sélectionné : ${joueurSelectionne.nom}`);
        this.ouvrirSelectionTimePlay(joueurSelectionne); // 🔄 Étape suivante
      } else {
        console.log('❌ Aucun remplaçant sélectionné');
      }
    });
}


  ouvrirSelectionTimePlay(joueurEntrant: FeuilleDeMatchDTO): void {
    const dialogRef = this.dialog.open(TimePlaySelectionDialogComponent, {});

    dialogRef.afterClosed().subscribe((timePlaySelectionne: number) => {
      if (timePlaySelectionne !== undefined) {
        console.log(`⏳ TimePlay sélectionné : ${timePlaySelectionne}`);

        this.minuteRemplacement = timePlaySelectionne;
        this.validerRemplacement(joueurEntrant);
      } else {
        console.log('❌ Aucun TimePlay sélectionné, remplacement annulé.');
      }
    });
  }

  /**
   * 📌 Valide le remplacement en envoyant les données au backend.
   */
  validerRemplacement(joueurEntrant: FeuilleDeMatchDTO): void {
    if (!this.terrain) {
      console.error('❌ Erreur : Impossible de valider le remplacement, le terrain est null.');
      return;
    }






    console.log("📌 Vérification des variables avant remplacement :");
console.log("🔹 Joueur sortant:", this.joueurSortant);
console.log("🔹 Joueur entrant:", joueurEntrant);
console.log("🔹 Minute de remplacement:", this.minuteRemplacement);

if (!this.joueurSortant || !joueurEntrant || this.minuteRemplacement === null) {
  console.error("❌ Une ou plusieurs variables sont nulles :");
  console.error("Joueur sortant:", this.joueurSortant);
  console.error("Joueur entrant:", joueurEntrant);
  console.error("Minute de remplacement:", this.minuteRemplacement);
  
  alert('❌ Erreur : Informations de remplacement incomplètes !');
  return;
}


    if (!this.joueurSortant || !joueurEntrant || this.minuteRemplacement === null) {
      alert('❌ Erreur : Informations de remplacement incomplètes !');
      return;
    }

    console.log(
      `🔄 Validation remplacement : ${this.joueurSortant.nom} ➡ ${joueurEntrant.nom} à ${this.minuteRemplacement * 90} min`,
    );

    // ✅ 1️⃣ Mettre à jour la liste des joueurs localement AVANT l'envoi au backend
    this.joueurEntrant = joueurEntrant;

    console.log('📌 Liste des titulaires après mise à jour locale :', this.terrain.titulaires);
    console.log('📌 Liste des remplaçants après mise à jour locale :', this.terrain.remplacants);

    // ✅ 2️⃣ Construire l'objet à envoyer au backend
    const remplacementDTO: RemplacementDTO = {
      idRencontre: this.terrain.idRencontre,
      idRemplacantEntrant: joueurEntrant.jid,
      idTitulaireSortant: this.joueurSortant.jid,
      minuteEntree: this.minuteRemplacement,
    };

    console.log('📡 Envoi du remplacement au backend :', remplacementDTO);

    // ✅ 3️⃣ Envoyer uniquement les minutes jouées et `aJoue = true`
    this.terrainService.effectuerRemplacement(this.terrain.idRencontre, remplacementDTO).subscribe({
      next: (terrainDTO) => {
        if (!terrainDTO) {
          console.error('❌ Erreur : TerrainDTO est null après remplacement !');
          alert('❌ Erreur lors du remplacement !');
          return;
        }

        console.log('✅ Remplacement validé.', terrainDTO);
        
        // ✅ **Met à jour `terrain` avec la nouvelle version reçue du backend**
        this.terrain = { ...terrainDTO };

        // ✅ **Appelle `actualiserListeJoueurs()` pour bien gérer les titulaires/remplaçants**
        this.actualiserListeJoueurs();

        // ✅ Recharge l'historique et met à jour l'affichage
        this.chargerTerrain(); // ✅ Recharge le terrain après mise à jour
        this.cdr.markForCheck();

        alert('✅ Remplacement confirmé !');
      },
      error: (err) => {
        console.error('❌ Erreur lors du remplacement :', err);
        alert('❌ Erreur lors du remplacement !');
      },
    });
  }

  /**
   * 📌 Met à jour `terrainJoueurs` après un remplacement :
   *    - 🔄 Utilise `echangerJoueurs()` pour assurer la cohérence
   *    - ✅ Ajoute le joueur entrant à `titulaires`
   *    - ❌ Retire le joueur remplacé de `titulaires` et l’ajoute à `remplaçants`
   */
  actualiserListeJoueurs(): void {
    if (
      !this.terrain ||
      !this.terrain.titulaires ||
      !this.terrain.remplacants ||
      !this.joueurSortant ||
      !this.joueurEntrant
    ) {
      console.error('❌ Erreur : Données incomplètes pour le remplacement.');
      return;
    }

    console.log(
      `🔄 Début du remplacement : ${this.joueurSortant.nom} ➡ ${this.joueurEntrant.nom}`,
    );

    // ✅ **Ne plus chercher le poste, simplement mettre à jour les titulaires/remplaçants**
    this.terrainJoueurs[this.joueurSortant.poste] = this.joueurEntrant;
    this.terrainJoueurs[this.joueurEntrant.poste] = null;

    console.log(
      '📌 Nouvelle liste des titulaires :',
      this.terrain.titulaires.map((j) => j.nom),
    );
    console.log(
      '📌 Nouvelle liste des remplaçants :',
      this.terrain.remplacants.map((j) => j.nom),
    );

    // ✅ **Mettre à jour directement `terrainJoueurs` avec la version du backend**
    this.terrain = { ...this.terrain };
    console.log('📌 Nouvelle liste des remplaçants :', this.terrain);
    // ✅ Ne plus chercher le poste, mais juste rafraîchir l'affichage
    this.cdr.markForCheck();

    // ✅ Réinitialisation des variables après traitement
    setTimeout(() => {
      this.joueurSortant = null;
      this.joueurEntrant = null;
      this.minuteRemplacement = null;
    }, 0);
  }

  // ===============================
  // 4️⃣ Mise à jour des statistiques gardien
  // ===============================

  /**
   * 📌 Ajoute un arrêt au gardien.
   */
 ajouterArretGardien(): void {
  const gardien = this.terrain?.terrainJoueurs?.['GB'];
  if (!gardien) return console.error('❌ Aucun gardien trouvé sur le terrain.');

  // 🧤 Augmenter le nombre d'arrêts
  gardien.butArreter = (gardien.butArreter || 0) + 1;
  console.log(`🧤 Arrêt ajouté au gardien ${gardien.nom} (Total : ${gardien.butArreter})`);

  // ✅ Envoi automatique des stats gardien dès qu'un arrêt est ajouté
  this.envoyerStatsGardien();
  console.log(`🧤 Stat gardien mise à jour ${gardien.nom} (Total : ${gardien.butArreter})`);

  // ✅ Mettre à jour l'affichage après modification
  this.chargerTerrain();
  this.cdr.markForCheck();
}


  /**
   * 📌 Envoie les statistiques mises à jour du gardien au backend.
   */
  envoyerStatsGardien(): void {
    if (!this.terrain) {
      console.error("❌ Erreur : Impossible d'envoyer les stats du gardien, le terrain est null.");
      return;
    }
  
    const gardien = this.terrain.terrainJoueurs?.['GB'];
    if (!gardien) {
      console.error("❌ Aucun gardien disponible pour l'envoi des stats.");
      return;
    }
  
    // ✅ On récupère maintenant les vrais buts encaissés depuis `terrain.butAdversaire`
    const butsEncaisses = this.terrain.butAdversaire || 0;
    const arrets = gardien.butArreter || 0;
  
    console.log(
      `📡 Envoi stats gardien ${gardien.nom} → Buts encaissés : ${butsEncaisses}, Arrêts : ${arrets}`,
    );
  
    this.terrainService
      .mettreAJourStatsGardien(
        this.terrain.idRencontre,
        gardien.jid,
        butsEncaisses,  // ✅ Maintenant, on prend `this.terrain.butAdversaire`
        arrets
      )
      .subscribe({
        next: () => {
          console.log('✅ Stats gardien envoyées avec succès.');
          this.chargerTerrain(); // ✅ Recharge le terrain après mise à jour
        },
        error: (err) => console.error('❌ Erreur envoi stats gardien :', err),
      });
  
    this.cdr.markForCheck(); // ✅ Mise à jour optimisée
  }
  

  // ===============================
  // 5️⃣ Gestion de la fin de match
  // ===============================

/**
 * 📌 Ouvre la pop-up pour attribuer les cotes en fin de match.
 */
ouvrirPopupCotes(): void {
  if (!this.terrain) {
    console.error("❌ Erreur : Impossible d'ouvrir la pop-up, le terrain est null.");
    return;
  }

  // ✅ Récupère **tous** les titulaires + les remplaçants qui ont joué
  const joueursAvecCotes = [
    ...(this.terrain.titulaires || []),
    ...(this.terrain.remplacants?.filter(joueur => joueur.ajoue) || []) // ✅ Ajoute uniquement ceux qui ont joué
  ];

  if (joueursAvecCotes.length === 0) {
    alert("❌ Aucun joueur à noter !");
    return;
  }

  console.log("📌 Liste des joueurs pour attribution des cotes :", joueursAvecCotes);

  const dialogRef = this.dialog.open(CoteAttributionDialogComponent, {
    data: { joueurs: joueursAvecCotes }
  });

  dialogRef.afterClosed().subscribe((result) => {
    if (result) {
      console.log("📌 Résultat des cotes attribuées :", result);
      this.majCotesJoueurs(result); // 🔥 Appel de la méthode pour mettre à jour en base
    }
  });
}




/**
 * 📌 Met à jour les cotes des joueurs en base et enchaîne avec la clôture de la rencontre.
 */
majCotesJoueurs(cotes: { [joueurId: number]: number }): void {
  if (!this.terrain || !this.terrain.idRencontre) {
    console.error("❌ Erreur : Impossible de mettre à jour les cotes, `terrain` ou `idRencontre` est null/undefined.");
    return;
  }

  this.terrainService.mettreAJourCotes(this.terrain.idRencontre, cotes)
    .subscribe({
      next: () => {
        console.log("✅ Cotes mises à jour avec succès !");
        
        // 🔹 Après mise à jour des cotes, on clôture la rencontre
        this.cloturerRencontre();
      },
      error: (err) => console.error("❌ Erreur lors de la mise à jour des cotes :", err)
    });
}





/**
 * 📌 Vérifie que tout est prêt et clôture la rencontre.
 */
cloturerRencontre(): void {
  if (!this.terrain || !this.terrain.idRencontre) {
    console.error("❌ Erreur : Terrain ou ID rencontre introuvable.");
    return;
  }

  if (this.butAdversaire === undefined || this.butAdversaire === null) {
    alert("⚠️ Vous devez entrer le score de l'adversaire avant de clôturer.");
    return;
  }

  console.log("📌 Vérification de `this.terrain` et lancement de la clôture...");

  // 🔹 Préparation du DTO de clôture
  const clotureDTO: ClotureRencontreDTO = {
    idRencontre: this.terrain.idRencontre,
    nomAdversaire: this.terrain.nomAdversaire || "Adversaire inconnu",
    butAdversaire: this.butAdversaire,
    divisionAdversaire: this.terrain.divisionAdversaire as Division,

    // ✅ Récupération des cotes qui sont déjà en base
    cotes: this.terrain.titulaires.concat(this.terrain.remplacants ?? [])
      .reduce((acc, joueur) => {
        if (joueur.cote !== undefined) acc[joueur.jid] = joueur.cote;
        return acc;
      }, {} as Record<number, number>),

    // ✅ Détection automatique des buts arrêtés par les gardiens
    butsArretes: this.terrain.titulaires.concat(this.terrain.remplacants ?? [])
      .filter(joueur => joueur.poste === 'GB' && joueur.butArreter !== undefined)
      .reduce((acc, gardien) => {
        acc[gardien.jid] = gardien.butArreter || 0;
        return acc;
      }, {} as Record<number, number>),
  };

  console.log('📌 Données envoyées pour la clôture :', clotureDTO);

  // ✅ Envoi de la clôture directement
  this.terrainService.cloturerRencontre(this.terrain.idRencontre, clotureDTO).subscribe({
    next: () => alert('🏆 Rencontre clôturée avec succès !'),
    error: (err) => console.error('❌ Erreur lors de la clôture de la rencontre :', err),
  });
}





  // ===============================
  // 6️⃣ Gestion des formations et du terrain
  // ===============================

  /**
   * 📌 Retourne la liste des formations disponibles
   */
  public getFormationsDisponibles(): string[] {
    return Object.keys(this.formationsDisponibles);
  }

  /**
   * 📌 Change la formation et met à jour les postes.
   */
  changerFormation(event: MatSelectChange): void {
    console.log('🔄 Changement de formation détecté :', event.value);
    console.log('📌 Ancienne formation :', this.formationPrecedente);

    // ✅ Au tout premier appel, on ne bloque pas la mise à jour (ancienne formation = null)
    if (this.formationPrecedente !== null && this.formationPrecedente === event.value) {
      console.warn('⚠️ Formation inchangée, on ne refait pas le calcul.');
      return;
    }

    // ✅ Met à jour la formation
    this.formationActive = event.value;
    this.mettreAJourPostesFormation();

    // ✅ Met à jour l'ancienne formation pour la prochaine comparaison
    this.formationPrecedente = event.value;

    console.log('📌 Nouvelle formation enregistrée :', this.formationActive);

    setTimeout(() => {
      this.disposerJoueursNaturellement();
      this.cdr.detectChanges();
    }, 100);
  }

  /**
   * 📌 Détermine si un terrain comprends bien les 11 joueurs minimum requis
   */
  estTerrainComplet(): boolean {
    if (!this.terrain || !this.terrainJoueurs) {
      console.warn(
        '⚠️ Vérification de `estTerrainComplet()` impossible : terrain ou terrainJoueurs est NULL.',
      );
      return false;
    }

    console.log('📌 TITULAIRE SUR LE TERRAIN :', this.terrainJoueurs);

    // 📌 Étape 1 : Récupérer tous les joueurs non null
    const joueursActifs = Object.values(this.terrainJoueurs).filter((joueur) => joueur !== null);

    // 📌 Étape 2 : Extraire les `jid` des joueurs (ID unique)
    const idsJoueurs = joueursActifs.map((joueur) => joueur!.jid);

    // 📌 Étape 3 : Supprimer les doublons avec un `Set`
    const joueursUniques = new Set(idsJoueurs);

    // 📌 Vérification finale
    const complet = joueursUniques.size === 11;

    console.log(`🔍 [RESULTAT] estTerrainComplet() → ${complet ? '✅ OUI' : '❌ NON'}`);

    return complet;
  }

  // ===============================
  // 7️⃣ FORMATION
  // ===============================

  /**
   * 📌 Retourne la liste des postes compatibles avec un poste donné
   */
  private getPostesCompatibles(posteKey: string): string[] {
    const mappingFlexible: Record<string, string[]> = {
      GB: ['GB'],
      DC_CENTRAL: ['DC_DROIT', 'DC_GAUCHE', 'DC_CENTRAL', 'DD', 'DG', 'MDF'],
      DC_DROIT: ['DC_DROIT', 'DC_CENTRAL', 'DD', 'DG', 'DC_GAUCHE', 'MDF'],
      DC_GAUCHE: ['DC_DROIT', 'DC_GAUCHE', 'DC_CENTRAL', 'DG', 'DD', 'MDF'],
      DD: ['DD', 'DC_DROIT', 'MDF', 'DC_CENTRAL', 'DG', 'DC_GAUCHE'],
      DG: ['DG', 'DC_GAUCHE', 'MDF', 'DC_CENTRAL', 'DD', 'DC_DROIT'],
      MDF: ['MDF', 'MR', 'MO', 'DG', 'DD', 'DC_CENTRAL', 'DC_GAUCHE', 'DC_GAUCHE'],
      MR: ['MR', 'MO', 'MDF'],
      MO: ['MO', 'MR', 'MDF', 'AIG', 'AID', 'SA', 'AC_DROIT', 'AC_GAUCHE'],
      AIG: ['AIG', 'AID', 'AC', 'SA', 'AC_GAUCHE', 'AC_DROIT'],
      AID: ['AIG', 'AID', 'AC', 'SA', 'AC_GAUCHE', 'AC_DROIT'],
      AC: ['AIG', 'AID', 'AC', 'AC_DROIT', 'AC_GAUCHE', 'SA'],
      SA: ['AIG', 'AID', 'AC', 'SA', 'AC_DROIT', 'AC_GAUCHE', 'MO'],
      AC_DROIT: ['AIG', 'AID', 'AC', 'SA', 'AC_DROIT', 'AC_GAUCHE', 'MO'],
      AC_GAUCHE: ['AIG', 'AID', 'AC', 'SA', 'AC_DROIT', 'AC_GAUCHE', 'MO'],
    };

    const compatibles = mappingFlexible[posteKey] || [posteKey];

    console.log(`📌 DEBUG : ${posteKey} est compatible avec :`, compatibles);

    return compatibles;
  }

  public formationsDisponibles: Record<
    string,
    { defense: string[]; milieu: string[]; attaque: string[] }
  > = {
    '433': {
      defense: ['DD', 'DC1', 'DC2', 'DG'],
      milieu: ['MR', 'MDF', 'MO'],
      attaque: ['AID', 'AC', 'AIG'],
    },
    '532': {
      defense: ['DD', 'DC1', 'DC2', 'DC3', 'DG'],
      milieu: ['MR', 'MDF', 'MO'],
      attaque: ['AC1', 'AC2'],
    },
    '343': {
      defense: ['DC1', 'DC2', 'DC3'],
      milieu: ['MLD', 'MR', 'MDF', 'MLG'],
      attaque: ['AID', 'AC', 'AIG'],
    },
  };

  /**
   * 📌 Répartit les joueurs sur le terrain en fonction de la formation active.
   * ✅ Applique le Friendly Movement :
   *    - Un défenseur peut monter en milieu si besoin.
   *    - Un attaquant peut descendre en milieu si besoin.
   * ✅ Ignore complètement les postes spécifiques (ex: DC1, DC2) et ne considère que les LIGNES.
   */
  public disposerJoueursNaturellement(): void {
    if (!this.terrain || !this.terrain.terrainJoueurs) {
      console.error(
        '❌ Impossible de redistribuer les joueurs : Terrain ou terrainJoueurs est null.',
      );
      return;
    }

    console.log('🔄 Mise à jour de la répartition des joueurs...');

    // **1️⃣ Réinitialisation du terrain (on vide tout sauf le gardien)**
    this.terrainJoueurs = {};

    // **2️⃣ Vérification et assignation du gardien en premier**
    let gardiens = Object.values(this.terrain.terrainJoueurs).filter(
      (j): j is FeuilleDeMatchDTO =>
        j !== null && j !== undefined && PosteUtils.fromString(j.poste) === CategoriePoste.GARDIEN,
    );

    if (gardiens.length > 0) {
      this.terrainJoueurs['GB'] = gardiens[0]; // ✅ On garde le gardien
    } else {
      console.warn('⚠️ Aucun gardien trouvé !');
    }

    // **3️⃣ Récupération des joueurs restants (sans le gardien)**
    let tousJoueurs = Object.values(this.terrain.terrainJoueurs)
      .filter(
        (joueur): joueur is FeuilleDeMatchDTO =>
          joueur !== null && joueur !== undefined && joueur !== this.terrainJoueurs['GB'],
      )
      .sort((a, b) => (a?.jid ?? 0) - (b?.jid ?? 0)); // 🔄 Tri pour éviter un ordre aléatoire et gérer les `undefined`

    console.log('📌 Joueurs disponibles après suppression du gardien :', tousJoueurs);

    // **4️⃣ Répartition des joueurs sur 3 lignes (DEF, MIL, ATT)**
    const formation = this.postesFormation; // ✅ Utilisation des lignes

    let index = 0;

    // **🛡️ 4.1 Défense**
    formation.defense.forEach((poste) => {
      if (index < tousJoueurs.length) {
        this.terrainJoueurs[poste] = tousJoueurs[index++] ?? null;
      }
    });

    // **⚖️ 4.2 Milieu**
    formation.milieu.forEach((poste) => {
      if (index < tousJoueurs.length) {
        this.terrainJoueurs[poste] = tousJoueurs[index++] ?? null;
      } else {
        console.warn(`⚠️ Manque de milieux pour ${poste}, descente d'un défenseur.`);
        let defenseur = Object.entries(this.terrainJoueurs).find(
          ([key, j]) =>
            j !== null &&
            j !== undefined &&
            PosteUtils.fromString(j.poste) === CategoriePoste.DEFENSEUR,
        );

        if (defenseur) {
          this.terrainJoueurs[poste] = defenseur[1];
          delete this.terrainJoueurs[defenseur[0]];
        }
      }
    });

    // **🔥 4.3 Attaque**
    formation.attaque.forEach((poste) => {
      if (index < tousJoueurs.length) {
        this.terrainJoueurs[poste] = tousJoueurs[index++] ?? null;
      } else {
        console.warn(`⚠️ Manque d'attaquants pour ${poste}, descente d'un milieu.`);
        let milieu = Object.entries(this.terrainJoueurs).find(
          ([key, j]) =>
            j !== null &&
            j !== undefined &&
            PosteUtils.fromString(j.poste) === CategoriePoste.MILIEU,
        );

        if (milieu) {
          this.terrainJoueurs[poste] = milieu[1];
          delete this.terrainJoueurs[milieu[0]];
        }
      }
    });

    console.log('✅ Répartition finale (sans doublons) :', this.terrainJoueurs);
    this.cdr.detectChanges();
  }

  /**
   * 📌 Selectionne les joueur eligible pour un assist
   */
  getJoueursDisponiblesPourPasse(buteurFeuilleId: number): FeuilleDeMatchDTO[] {
    if (!this.terrain || !this.terrain.terrainJoueurs) {
      console.warn('⚠️ Impossible de récupérer les joueurs disponibles : terrain non chargé.');
      return [];
    }

    return Object.values(this.terrain.terrainJoueurs).filter(
      (joueur): joueur is FeuilleDeMatchDTO => joueur !== null && joueur.jid !== buteurFeuilleId,
    );
  }

  /**
   * 📌 Optimisation Angular : Utilisé pour identifier chaque poste de manière unique et éviter le re-render inutile.
   */
  trackByPoste(index: number, posteKey: string): string {
    return posteKey;
  }

  /**
   * 📌 Optimisation Angular : Utilisé pour identifier chaque joueur par son ID et éviter le re-render inutile.
   */
  trackByJoueur(index: number, joueur: FeuilleDeMatchDTO): number {
    return joueur.id;
  }

  /**
   * 📌 Convertit minute en TimePlay
   */
  convertirMinuteEnTimePlay(minute: number): TimePlay {
    return fromPercentage(minute);
  }

  /**
   * 📌 Retourne la structure de la formation active.
   */
  public getPostesFormation(): { defense: string[]; milieu: string[]; attaque: string[] } {
    return (
      this.formationsDisponibles[this.formationActive] || { defense: [], milieu: [], attaque: [] }
    );
  }

  /**
   * 📌 Met à jour la structure des postes de la formation active pour éviter les appels inutiles dans le HTML.
   */
  public mettreAJourPostesFormation(): void {
    this.postesFormation = this.getPostesFormation(); // Stocke la formation active une seule fois
    console.log('✅ Postes de la formation active mis à jour :', this.postesFormation);
  }

  // ===============================
  // 7️⃣ Optimisation et Utilitaires
  // ===============================
}
