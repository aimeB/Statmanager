import { Component, OnInit, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { TerrainService } from '../../../services/terrain.service';
import { FeuilleDeMatchDTO } from '../../../../feuille-de-match/models/feuille-de-match.model';
import { TerrainDTO } from '../../../models/terrain.model';
import { MatDialog } from '@angular/material/dialog';
import { ChangeDetectorRef } from '@angular/core';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { JoueurSelectionDialogComponent } from '../../../../joueur/components/joueur-selection-dialog/joueur-selection-dialog.component';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { RouterModule } from '@angular/router';
import { EvenementMatchDTO, RemplacementDTO } from '../../../models/rencontre.model';
import { TimePlay, fromPercentage, getTimePlayLabel, } from '../../../../../shared/models/time-play.enum';
import {CoteAttributionDialogComponent } from '../../../../../shared/components/cote/cote-attribution-dialog.component';
import {ClotureRencontreDTO} from '../../../../../modules/rencontre/models/rencontre.model';
import { Division } from '../../../../../shared/models/division.enum';



@Component({
  selector: 'app-detail-terrain',
  standalone: true,
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  imports: [
    CommonModule,
    FormsModule,
    MatChipsModule,
    MatIconModule,
    RouterModule,
    MatCardModule,

  ],
  templateUrl: './detail-terrain.component.html',
  styleUrls: ['./detail-terrain.component.scss'],
})

export class DetailTerrainComponent implements OnInit {

  butEquipe: number = 0;
  butAdversaire: number = 0;
  joueurSelectionne: FeuilleDeMatchDTO | null = null;
  passeursDisponibles: FeuilleDeMatchDTO[] = [];
  joueurEntrant: FeuilleDeMatchDTO | null = null;
  joueurSortant: FeuilleDeMatchDTO | null = null;
  minuteRemplacement: number | null = null;
  private stompClient!: Client;
  butsMarques: EvenementMatchDTO[] = [];
  timePlayValues = Object.values(TimePlay).filter(v => typeof v === "number") as number[];

  getTimePlayLabel(value: number): string {
    return getTimePlayLabel(value as TimePlay);
  }
  



  // ✅ Propriétés de terrain
  terrain: TerrainDTO = {} as TerrainDTO;


  constructor(
    private route: ActivatedRoute,
    private terrainService: TerrainService,
    private dialog: MatDialog,
    private cdr: ChangeDetectorRef
  ) {}





  ngOnInit(): void {
    const idRencontre = Number(this.route.snapshot.paramMap.get('idRencontre'));
  
    if (!idRencontre) {
      console.error('❌ ID de rencontre manquant.');
      return;
    }
  
    // ✅ Récupération du terrain
    this.terrainService.getTerrain(idRencontre).subscribe({
      next: (terrainData) => {
        console.log("📥 [Front] Terrain reçu depuis backend :", terrainData);
        this.terrain = this.mapperTerrainDTO(terrainData);
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error("❌ Erreur lors de la récupération du terrain :", err);
      }
    });
  
    // ✅ Récupération de l’historique des événements (buts + remplacements)
    this.terrainService.getHistoriqueEvenements(idRencontre).subscribe({
      next: (historique) => {
        console.log("📥 Historique des événements reçu :", historique);
        this.butsMarques = historique;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error("❌ Erreur lors de la récupération de l'historique des événements :", err);
      }
    });
  
    this.initialiserWebSocket();
  }
  










  private initialiserWebSocket(): void {


    this.stompClient.subscribe('/topic/stats', (message) => {
      const update = JSON.parse(message.body);
      console.log('🔄 Mise à jour des stats reçue via WebSocket:', update);
      this.actualiserStats(update);
    });
    
    this.stompClient.subscribe('/topic/remplacements', (message) => {
      const update = JSON.parse(message.body);
      console.log('🔄 Remplacement reçu via WebSocket:', update);
      this.actualiserRemplacement(update);
    });
  

    this.stompClient = new Client({
      brokerURL: undefined,
      webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      debug: (str) => console.log('STOMP Debug:', str)
    });

    this.stompClient.onConnect = () => {
      console.log('🟢 WebSocket STOMP connecté');
      this.stompClient.subscribe('/topic/remplacements', (message) => {
        const update = JSON.parse(message.body);
        console.log('🔄 Remplacement reçu via WebSocket:', update);
        this.actualiserRemplacement(update);
      });

      this.stompClient.subscribe('/topic/stats', (message) => {
        const update = JSON.parse(message.body);
        console.log('🔄 Mise à jour des stats reçue via WebSocket:', update);
        this.actualiserStats(update);
      });
    };

    this.stompClient.onStompError = (frame) => {
      console.error('❌ Erreur STOMP :', frame);
    };

    this.stompClient.activate();
  }

  private mapperTerrainDTO(terrainData: any): TerrainDTO {
    return { ...terrainData } as TerrainDTO;
}




mettreAJourButAdversaire(nouveauScore: number): void {
  this.butAdversaire = nouveauScore;
  this.terrain.butAdversaire = nouveauScore; // ✅ Mise à jour du terrain
  this.terrain.titulaires.forEach(joueur => {
    if (joueur.poste === "GB") {
      joueur.butEncaisser = nouveauScore; // ✅ Mise à jour automatique du gardien
    }
  });

  console.log(`🔄 Score de l’adversaire mis à jour : ${this.butAdversaire}`);
  this.cdr.detectChanges();
}



ajouterArretGardien(): void {
  const gardien = this.terrain.titulaires.find(j => j.poste === "GB");
  if (gardien) {
    gardien.butArreter = (gardien.butArreter || 0) + 1;
    console.log(`🧤 Sauvetage ajouté pour ${gardien.nom} : ${gardien.butArreter}`);
    this.cdr.detectChanges();
  }
}








  actualiserStats(update: any): void {
    if (!this.terrain) {
      console.error("❌ Erreur : Le terrain n'est pas initialisé.");
      return;
    }

    const { idJoueur, buts, idPasseur, butEquipe, butAdversaire } = update;

    // ✅ Mettre à jour les buts et passes des joueurs
    Object.keys(this.terrain.terrainJoueurs).forEach(poste => {
      if (this.terrain.terrainJoueurs[poste]?.jid === idJoueur) {
        this.terrain.terrainJoueurs[poste].buts = buts;
      }
      if (idPasseur && this.terrain.terrainJoueurs[poste]?.jid === idPasseur) {
        this.terrain.terrainJoueurs[poste].passes = (this.terrain.terrainJoueurs[poste].passes || 0) + 1;
      }
    });

    // ✅ Mise à jour du score global
    this.butEquipe = butEquipe;
    this.butAdversaire = butAdversaire;

    this.cdr.detectChanges();
  }





  actualiserRemplacement(update: any): void {
    const { idRemplacant, idSortant } = update;

    const remplacant = this.terrain.remplacants.find(j => j.jid === idRemplacant);
    const sortant = this.terrain.titulaires.find(j => j.jid === idSortant);

    if (!remplacant || !sortant) {
      console.warn("⚠️ Remplacement impossible : joueur non trouvé", { remplacant, sortant });
      return;
    }

    // ✅ Mise à jour des listes titulaires/remplaçants
    this.terrain.titulaires = this.terrain.titulaires.filter(j => j.jid !== sortant.jid);
    this.terrain.remplacants.push(sortant);

    this.terrain.remplacants = this.terrain.remplacants.filter(j => j.jid !== remplacant.jid);
    this.terrain.titulaires.push(remplacant);

    // ✅ Mise à jour du terrain
    Object.keys(this.terrain.terrainJoueurs).forEach(poste => {
      if (this.terrain.terrainJoueurs[poste]?.jid === sortant.jid) {
        this.terrain.terrainJoueurs[poste] = remplacant;
      }
    });

    console.log("🔄 Mise à jour du terrain après remplacement:", this.terrain.terrainJoueurs);
    this.cdr.detectChanges();
  }






  

  ouvrirPopupCotes(): void {
    const joueursAvecCotes = [...this.terrain.titulaires, ...this.terrain.remplacants]
      .filter(joueur => joueur.titulaire || joueur.aJoue) // ✅ Seuls les joueurs ayant joué
  
    const dialogRef = this.dialog.open(CoteAttributionDialogComponent, {
      data: { joueurs: joueursAvecCotes }
    });
  
    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        console.log("✅ Cotes mises à jour :", result);
        this.cloturerRencontre(result);
      }
    });
  }
  




  incrementerBut(joueur: FeuilleDeMatchDTO): void {
    if (!joueur || !joueur.jid) {
      console.error("❌ Erreur : Joueur invalide !");
      return;
    }
  
    // ✅ Vérifier si terrain.butsModifies existe et mettre à jour
    if (!this.terrain.butsModifies) {
      this.terrain.butsModifies = {};
    }
  
    // ✅ Incrémenter le nombre de buts du joueur
    this.terrain.butsModifies[joueur.jid] = (this.terrain.butsModifies[joueur.jid] || 0) + 1;
  
    console.log(`⚽ But ajouté pour ${joueur.nom} (Total : ${this.terrain.butsModifies[joueur.jid]})`);
  
    this.joueurSelectionne = joueur; // ✅ Définit le joueur ayant marqué comme sélectionné
  
    this.cdr.detectChanges();
  
    // ✅ Ouvre immédiatement la fenêtre de sélection du passeur
    this.selectionnerPasseur();
  }
  
  










  getJoueursDisponiblesPourPasse(buteurFeuilleId: number): FeuilleDeMatchDTO[] {
    return Object.values(this.terrain.terrainJoueurs).filter(joueur => joueur.id !== buteurFeuilleId);
  }






  cloturerRencontre(cotes: Record<number, number>): void {
    if (!this.terrain) {
      console.error('❌ Erreur: Terrain non disponible.');
      return;
    }
  
    if (this.butAdversaire === 0) {
      alert('⚠️ Vous devez entrer le score de l\'adversaire avant de clôturer.');
      return;
    }
  
    const clotureDTO: ClotureRencontreDTO = {
      idRencontre: this.terrain.idRencontre,
      nomAdversaire: this.terrain.nomAdversaire, // ✅ Correction
      butAdversaire: this.butAdversaire,
      divisionAdversaire: this.terrain.divisionAdversaire as Division, // ✅ Conversion explicite

      cotes: cotes,
      butsArretes: this.terrain.titulaires
        .filter(j => j.poste === "GB" && j.butArreter !== undefined) // ✅ Filtrer uniquement les gardiens
        .reduce((acc, gardien) => {
          acc[gardien.jid] = gardien.butArreter || 0;
          return acc;
        }, {} as Record<number, number>), // ✅ Conversion en `Record<number, number>`
    };
  
    console.log("🏆 Envoi des données de clôture :", clotureDTO);
  
    this.terrainService.cloturerRencontre(this.terrain.idRencontre, clotureDTO).subscribe({ // ✅ Ajout de `idRencontre`
      next: () => {
        alert("🏆 Rencontre clôturée !");
      },
      error: (err) => {
        console.error("❌ Erreur lors de la clôture de la rencontre :", err);
      }
    });
  }
  
  







  selectionnerPasseur(): void {
    if (!this.joueurSelectionne) {
      console.error("❌ Aucun joueur sélectionné pour le but.");
      return;
    }
  
    // ✅ Exclure le buteur des joueurs pouvant être passeurs
    const joueursProposables: FeuilleDeMatchDTO[] = Object.values(this.terrain.terrainJoueurs)
      .filter(joueur => joueur.jid !== this.joueurSelectionne!.jid);
  
    if (joueursProposables.length === 0) {
      alert("❌ Aucun joueur disponible pour faire une passe !");
      return;
    }
  
    // ✅ Ouvrir la boîte de dialogue avec `FeuilleDeMatchDTO`
    const dialogRef = this.dialog.open(JoueurSelectionDialogComponent, {
      data: { joueurs: joueursProposables, type: 'FeuilleDeMatchDTO' }, // ✅ Indiquer le type
      width: '400px'
    });
  
    dialogRef.afterClosed().subscribe((passeurSelectionne: FeuilleDeMatchDTO) => {
      if (passeurSelectionne) {
        console.log(`🎯 Passeur sélectionné : ${passeurSelectionne.nom}`);
        this.envoyerButEtPasseur(passeurSelectionne);
      } else {
        console.log("❌ Aucun passeur sélectionné");
      }
    });
  }
  




  envoyerButEtPasseur(passeur: FeuilleDeMatchDTO): void {
    if (!this.joueurSelectionne) {
      console.error("❌ Aucun joueur sélectionné pour l'envoi du but.");
      return;
    }
  
    this.terrainService.updateStatsEnTempsReel(
      this.terrain.idRencontre,
      this.joueurSelectionne.jid, // ✅ ID du buteur
      (this.terrain.butsModifies[this.joueurSelectionne.jid] || 0) + 1, // ✅ Incrémente les buts
      passeur.jid // ✅ ID du passeur
    ).subscribe({
      next: (terrainDTO) => {
        console.log("✅ But et passe mis à jour avec succès :", terrainDTO);
        this.terrain = terrainDTO;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error("❌ Erreur lors de l'enregistrement du but :", err);
      }
    });
  }
  







  effectuerRemplacement(): void {
    if (!this.joueurSortant || !this.joueurEntrant || this.minuteRemplacement === null) {
      alert("❌ Veuillez sélectionner un titulaire, un remplaçant et une minute !");
      return;
    }
  
    console.log(`🔄 Tentative de remplacement : ${this.joueurSortant.nom} ➡ ${this.joueurEntrant.nom} à ${this.minuteRemplacement * 90} min`);
  
    // ✅ Conversion de la minute en TimePlay
    const minuteEntree = fromPercentage(this.minuteRemplacement);

  
    const remplacementDTO: RemplacementDTO = {
      idRencontre: this.terrain.idRencontre,
      idRemplacantEntrant: this.joueurEntrant.jid,
      idTitulaireSortant: this.joueurSortant.jid,
      minuteEntree: minuteEntree 
    };
  
    console.log("🔄 Envoi du remplacement :", remplacementDTO);
  
    this.terrainService.effectuerRemplacement(this.terrain.idRencontre, remplacementDTO).subscribe({
      next: (terrainDTO) => {
        if (!terrainDTO) {
          console.error("❌ Erreur : TerrainDTO est null après remplacement !");
          alert("❌ Erreur lors du remplacement !");
          return;
        }
        console.log('✅ Remplacement effectué avec succès.', terrainDTO);
  
        this.terrain = terrainDTO;
  
        // ✅ Mettre `aJoue = true` pour le joueur entrant
        this.terrain.titulaires.forEach(joueur => {
          if (joueur.jid === this.joueurEntrant!.jid) {
            joueur.aJoue = true;
          }
        });
  
        this.actualiserTempsDeJeu(minuteEntree);
        this.actualiserListeJoueurs();
        this.cdr.detectChanges();
        alert("✅ Remplacement validé !");
      },
      error: (err) => {
        console.error('❌ Erreur lors du remplacement:', err);
        alert("❌ Erreur lors du remplacement !");
      }
    });
  }
  
  











  actualiserTempsDeJeu(minuteEntree: number): void {
    if (!this.joueurSortant || !this.joueurEntrant) return;
  
    // ✅ Ajustement du temps de jeu
    const minutesSortant = this.terrain.terrainJoueurs[this.joueurSortant.poste]?.minutesJouees || 90;
    const minutesJoueesRemplacant = minutesSortant - (minuteEntree * 90);
  
    // ✅ Mettre à jour les valeurs
    this.terrain.terrainJoueurs[this.joueurSortant.poste].minutesJouees = minuteEntree * 90;
    this.terrain.terrainJoueurs[this.joueurEntrant.poste].minutesJouees = minutesJoueesRemplacant;
  
    console.log(`⏳ Mise à jour des minutes : ${this.joueurSortant.nom} → ${minuteEntree * 90} min, ${this.joueurEntrant.nom} → ${minutesJoueesRemplacant} min`);
  }
  
  





  actualiserListeJoueurs(): void {
    if (!this.terrain) {
      console.error("❌ Erreur : Terrain non initialisé.");
      return;
    }
  
    if (this.joueurSortant && this.joueurEntrant) {
      console.log("🔄 Mise à jour des listes après remplacement :", this.joueurSortant.nom, "sort et", this.joueurEntrant.nom, "entre");
  
      // ✅ Supprime le joueur sortant des titulaires et l'ajoute aux remplaçants
      this.terrain.titulaires = this.terrain.titulaires.filter(j => j.jid !== this.joueurSortant!.jid);
      this.terrain.remplacants.push(this.joueurSortant!);
  
      // ✅ Supprime le joueur entrant des remplaçants et l'ajoute aux titulaires
      this.terrain.remplacants = this.terrain.remplacants.filter(j => j.jid !== this.joueurEntrant!.jid);
      this.terrain.titulaires.push(this.joueurEntrant!);
  
      // ✅ Mise à jour du terrain en remplaçant le joueur sortant par le remplaçant
      Object.keys(this.terrain.terrainJoueurs).forEach(poste => {
        if (this.terrain.terrainJoueurs[poste]?.jid === this.joueurSortant!.jid) {
          this.terrain.terrainJoueurs[poste] = this.joueurEntrant!;
        }
      });
  
      // ✅ Mettre `aJoue = true` pour le joueur entrant
      this.joueurEntrant!.aJoue = true;
  
      console.log("🔄 Mise à jour du terrain après remplacement:", this.terrain.terrainJoueurs);
  
      // ✅ Réinitialisation des sélections
      this.joueurSortant = null;
      this.joueurEntrant = null;
      this.minuteRemplacement = null;
    }
  }
  





  ouvrirSelectionRemplacant(): void {
    const dialogRef = this.dialog.open(JoueurSelectionDialogComponent, {
      data: { joueurs: this.terrain.remplacants },
      width: '400px'
    });
  
    dialogRef.afterClosed().subscribe((joueurSelectionne: FeuilleDeMatchDTO) => {
      if (joueurSelectionne) {
        console.log(`🔄 Joueur remplaçant sélectionné : ${joueurSelectionne.nom}`);
        this.joueurEntrant = joueurSelectionne;
      }
    });
  }
  
}
