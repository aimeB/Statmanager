import { Component, OnInit, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { ChangeDetectorRef } from '@angular/core';
import { TerrainService } from '../../services/terrain.service';
import { TerrainDTO } from '../../../rencontre/models/terrain.model';
import { FeuilleDeMatchDTO } from '../../../feuille-de-match/models/feuille-de-match.model';
import { JoueurSelectionDialogComponent } from '../../components/../../joueur/components/joueur-selection-dialog/joueur-selection-dialog.component';
import { MatSelectModule } from '@angular/material/select';
import { MatSelectChange } from '@angular/material/select'; // ✅ Vérifie que c'est bien importé

@Component({
  selector: 'app-terrain',
  standalone: true,
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
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
  templateUrl: './terrain.component.html',
  styleUrls: ['./terrain.component.scss'],
})
export class TerrainComponent implements OnInit {
  terrain: TerrainDTO | null = null;
  joueursDisponibles: FeuilleDeMatchDTO[] = [];
  joueurSelectionne: FeuilleDeMatchDTO | null = null;
  posteSelectionne: string | null = null;
  idRencontre!: number;
  terrainJoueurs: { [poste: string]: FeuilleDeMatchDTO | null } = {}; // 🔥 Associe les joueurs aux postes

  constructor(
    private terrainService: TerrainService,
    private router: Router,
    private route: ActivatedRoute,
    private dialog: MatDialog,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.idRencontre = Number(this.route.snapshot.paramMap.get('idRencontre'));

    if (!this.idRencontre) {
      console.error('❌ ID de rencontre introuvable.');
      return;
    }

    // 🔄 Charge le terrain modifiable (nouvelle méthode)
    this.chargerTerrainModifiable();
  }

  /**
   * 📌 Charge un terrain modifiable pour l'utilisateur
   */
  private chargerTerrainModifiable(): void {
    console.log(`📡 Préparation du terrain modifiable pour la rencontre ID=${this.idRencontre}`);

    this.terrainService.construireTerrain(this.idRencontre).subscribe({
      next: (terrainData) => {
        if (!terrainData || !terrainData.terrainJoueurs) {
          console.error('❌ TerrainDTO est vide ou mal formaté !');
          return;
        }

        // Vérification si les titulaires et remplaçants existent
        if (!terrainData.titulaires || !terrainData.remplacants) {
          console.error('❌ Problème : titulaires ou remplaçants absents !');
          return;
        }

        this.terrain = terrainData;
        this.initialiserTerrain();
      },
      error: (err) => {
        console.error('❌ Erreur lors du chargement du terrain modifiable :', err);
      },
    });
  }

  initialiserTerrain(): void {
    if (!this.terrain || !this.terrain.terrainJoueurs) return;

    console.log('📌 TerrainDTO reçu :', this.terrain);

    this.terrainJoueurs = {};
    let joueursSansPoste: FeuilleDeMatchDTO[] = [];

    const formation = this.getPostesFormation();
    let postesRestants = [...formation.defense, ...formation.milieu, ...formation.attaque];

    // 🥅 ✅ **Forcer l'affectation du gardien**
    let gardien = this.terrain.titulaires.find((j) => j.poste === 'GB');
    if (gardien) {
      this.terrainJoueurs['GB'] = gardien;
      console.log(`🥅 Gardien assigné : ${gardien.nom}`);
    } else {
      console.warn('⚠️ Aucun gardien titulaire détecté !');
    }

    // ✅ Placer les autres titulaires
    for (let joueur of this.terrain.titulaires) {
      if (joueur.poste === 'GB') continue; // ⚽ Le gardien a déjà été géré
      console.log(`🔍 Placement titulaire : ${joueur.nom} (${joueur.poste})`);

      if (postesRestants.includes(joueur.poste)) {
        this.terrainJoueurs[joueur.poste] = joueur;
        postesRestants = postesRestants.filter((p) => p !== joueur.poste);
      } else {
        let posteCompatible = postesRestants.find((p) =>
          this.getPostesCompatibles(joueur.poste).includes(p),
        );
        if (posteCompatible) {
          console.log(`⚠️ Placement alternatif pour ${joueur.nom} en ${posteCompatible}`);
          this.terrainJoueurs[posteCompatible] = joueur;
          postesRestants = postesRestants.filter((p) => p !== posteCompatible);
        } else {
          joueursSansPoste.push(joueur);
        }
      }
    }

    // 🚨 **Assurer que tous les postes de la formation sont bien définis**
    [...formation.defense, ...formation.milieu, ...formation.attaque].forEach((poste) => {
      if (!(poste in this.terrainJoueurs)) {
        console.warn(`⚠️ Poste ${poste} absent de terrainJoueurs, initialisation à null.`);
        this.terrainJoueurs[poste] = null;
      }
    });

    this.joueursDisponibles = [...this.terrain.remplacants, ...joueursSansPoste];

    console.log('📌 TITULAIRE après mise à jour de initialiserTerrain :', this.terrainJoueurs);
    console.log(
      '📌 REMPLACANT disponibles après ajustement de initialiserTerrain :',
      this.joueursDisponibles,
    );

    this.cdr.detectChanges();
  }

  /**
   * 📌 Retourne la liste des postes compatibles avec un poste donné
   */
  private getPostesCompatibles(posteKey: string): string[] {
    const mappingFlexible: Record<string, string[]> = {
      GB: ['GB'],
      DC_CENTRAL: ['DC_DROIT', 'DC_GAUCHE', 'DC_CENTRAL', 'DD', 'DG', 'MDF'],
      DC_DROIT: ['DC_DROIT', 'DC_CENTRAL', 'DD', 'DG', 'DC_GAUCHE'],
      DC_GAUCHE: ['DC_DROIT', 'DC_GAUCHE', 'DC_CENTRAL', 'DG', 'DD'],
      DD: ['DD', 'DC_DROIT', 'MR', 'DC_CENTRAL', 'DG', 'DC_GAUCHE'],
      DG: ['DG', 'DC_GAUCHE', 'MO', 'DC_CENTRAL', 'DD', 'DC_DROIT'],
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
      defense: ['DD', 'DC_DROIT', 'DC_GAUCHE', 'DG'],
      milieu: ['MR', 'MDF', 'MO'],
      attaque: ['AID', 'AC', 'AIG'],
    },
    '532': {
      defense: ['DD', 'DC_DROIT', 'DC_CENTRAL', 'DC_GAUCHE', 'DG'],
      milieu: ['MR', 'MDF', 'MO'],
      attaque: ['AC_DROIT', 'AC_GAUCHE'],
    },
    '343': {
      defense: ['DC_DROIT', 'DC_CENTRAL', 'DC_GAUCHE'],
      milieu: ['DD', 'MR', 'MDF', 'DG'],
      attaque: ['AIG', 'AC', 'AID'],
    },
    '433-2': {
      defense: ['DD', 'DC_DROIT', 'DC_GAUCHE', 'DG'],
      milieu: ['MR', 'MDF', 'MO'],
      attaque: ['AID', 'AC', 'SA'],
    },
  };

  // 📌 Formation active (modifiable par l’utilisateur)
  public formationActive: string = '433';

  public getPostesFormation(): { defense: string[]; milieu: string[]; attaque: string[] } {
    return (
      this.formationsDisponibles[this.formationActive] || { defense: [], milieu: [], attaque: [] }
    );
  }

  trackByPoste(
    index: number,
    poste: { key: string; label: string; joueur: FeuilleDeMatchDTO | null },
  ): string {
    return poste.key;
  }

  trackByJoueur(index: number, joueur: FeuilleDeMatchDTO): number {
    return joueur.id;
  }

  /**
   * 📌 Réinitialise un poste et remet le joueur dans la liste des disponibles
   */
  reinitialiserPoste(posteKey: string): void {
    if (!this.terrain || !this.terrainJoueurs || !this.terrainJoueurs[posteKey]) return;

    const joueurSortant = this.terrainJoueurs[posteKey];

    if (!joueurSortant) {
      console.warn(`⚠️ Aucun joueur trouvé au poste ${posteKey} à réinitialiser.`);
      return;
    }

    console.log(`♻️ Réinitialisation du poste ${posteKey} : ${joueurSortant.nom}`);

    // ✅ Ajouter le joueur aux disponibles UNIQUEMENT s'il n'y est pas déjà
    if (!this.joueursDisponibles.some((j) => j.jid === joueurSortant.jid)) {
      this.joueursDisponibles = [...this.joueursDisponibles, joueurSortant];
    }

    // ❌ Supprimer correctement le joueur du terrain
    delete this.terrainJoueurs[posteKey];

    // ✅ Remettre `null` explicitement pour éviter les références fantômes
    this.terrainJoueurs = { ...this.terrainJoueurs, [posteKey]: null };

    console.log(
      `📌 [DEBUG] Après suppression, état actuel de terrainJoueurs :`,
      JSON.stringify(this.terrainJoueurs, null, 2),
    );

    // 🔄 Forcer la mise à jour Angular
    this.cdr.detectChanges();
  }

  /**
   * 📌 Ouvrir la sélection de joueurs pour un poste donné
   */
  ouvrirSelectionJoueur(posteKey: string): void {
    this.posteSelectionne = posteKey;

    // ✅ Récupérer les joueurs qui peuvent jouer à ce poste
    const joueursProposables = [
      ...this.joueursDisponibles.filter((j) =>
        this.getPostesCompatibles(posteKey).includes(j.poste),
      ),
      ...Object.values(this.terrainJoueurs).filter(
        (j) => j && this.getPostesCompatibles(posteKey).includes(j.poste),
      ),
    ].filter((j): j is FeuilleDeMatchDTO => j !== null); // Filtre pour éviter les valeurs null

    console.log(`📌 Joueurs disponibles pour ${posteKey} :`, joueursProposables);

    // 📌 Ouvrir la boîte de dialogue avec la liste des joueurs disponibles
    const dialogRef = this.dialog.open(JoueurSelectionDialogComponent, {
      data: {
        joueurs:
          joueursProposables.length > 0
            ? joueursProposables
            : [{ id: -1, nom: 'Aucun joueur disponible', poste: '' }],
      },
    });

    dialogRef.afterClosed().subscribe((joueurSelectionne: FeuilleDeMatchDTO) => {
      console.log(`📌 Joueur sélectionné après fermeture :`, joueurSelectionne);

      if (joueurSelectionne && joueurSelectionne.id !== -1) {
        this.echangerJoueurs(joueurSelectionne, posteKey);
      }
    });
  }

  /**
   * 📌 Retourne la liste des formations disponibles
   */
  public getFormationsDisponibles(): string[] {
    return Object.keys(this.formationsDisponibles);
  }

  /**
   * 📌 Possibilité de réorganiser les joueurs sur le terrain
   */
  echangerJoueurs(joueurEntrant: FeuilleDeMatchDTO, posteCibleKey: string): void {
    if (!this.terrain || !this.terrainJoueurs) return;

    console.log(
      '📌 [DEBUG] État de terrainJoueurs AVANT échange :',
      JSON.stringify(this.terrainJoueurs, null, 2),
    );

    // 🔍 Vérifie qui est déjà au poste cible
    const joueurRemplace = this.terrainJoueurs[posteCibleKey] || null;

    // 🔍 Trouver où joue actuellement le joueur entrant
    const posteActuelKey = Object.keys(this.terrainJoueurs).find(
      (key) => this.terrainJoueurs[key]?.id === joueurEntrant.id,
    );

    console.log(
      `🔄 Échange : ${joueurEntrant.nom} (${posteActuelKey || 'remplaçants'}) ↔ ${joueurRemplace?.nom || 'aucun'} (${posteCibleKey})`,
    );

    // ✅ Retirer le joueur entrant de la liste des disponibles
    this.joueursDisponibles = this.joueursDisponibles.filter((j) => j.id !== joueurEntrant.id);

    if (posteActuelKey) {
      // 🛠 **Si le joueur entrant était déjà sur le terrain, on échange les deux joueurs**
      this.terrainJoueurs[posteActuelKey] = joueurRemplace; // L'ancien joueur prend la place du joueur entrant
    } else if (joueurRemplace) {
      // 📌 **Si le joueur remplacé était déjà sur le terrain, il va en disponibles**
      this.joueursDisponibles.push(joueurRemplace);
    }

    // ✅ **Placer le joueur entrant au poste sélectionné**
    this.terrainJoueurs[posteCibleKey] = joueurEntrant;

    // 🔥 LOG après modification locale
    console.log(
      '📌 [DEBUG] État de terrainJoueurs APRÈS échange :',
      JSON.stringify(this.terrainJoueurs, null, 2),
    );

    // 🔄 **Copier l'état local dans `this.terrain.terrainJoueurs` avant envoi**
    this.terrain.terrainJoueurs = { ...this.terrainJoueurs };

    console.log(
      '✅ Terrain mis à jour apres  affectation  :',
      JSON.stringify(this.terrain.terrainJoueurs, null, 2),
    );

    // 🔄 Forcer Angular à détecter les changements
    this.cdr.detectChanges();
  }

  changerFormation(event: MatSelectChange): void {
    console.log('📌 Événement capté :', event);

    const nouvelleFormation = event.value; // ✅ C'est ici que l'erreur se corrige !
    if (!nouvelleFormation || !this.formationsDisponibles[nouvelleFormation]) return;

    console.log(`⚽ Changement de formation vers : ${nouvelleFormation}`);

    // ✅ Mise à jour de la formation active
    this.formationActive = nouvelleFormation;

    // ✅ Met à jour les postes en fonction de la nouvelle formation
    this.majPostesFormation();
  }

  private majPostesFormation(): void {
    console.log(`🔄 Mise à jour des postes pour ${this.formationActive}`);

    const formation = this.getPostesFormation();
    const nouveauxJoueursTerrain: { [poste: string]: FeuilleDeMatchDTO | null } = {};
    let joueursSansPoste: FeuilleDeMatchDTO[] = [];

    // 🥅 ✅ **Conserver le gardien**
    if (this.terrainJoueurs['GB']) {
      nouveauxJoueursTerrain['GB'] = this.terrainJoueurs['GB'];
      console.log(`🥅 Gardien conservé : ${nouveauxJoueursTerrain['GB']?.nom}`);
    }

    // ✅ Étape 1 : Identifier les postes vides et les joueurs sans poste
    let postesVides = [...formation.defense, ...formation.milieu, ...formation.attaque];

    Object.keys(this.terrainJoueurs).forEach((poste) => {
      if (poste === 'GB') return; // Exclure le gardien

      const joueur = this.terrainJoueurs[poste];
      if (joueur) {
        if (postesVides.includes(poste)) {
          nouveauxJoueursTerrain[poste] = joueur;
          postesVides = postesVides.filter((p) => p !== poste);
        } else {
          joueursSansPoste.push(joueur);
        }
      }
    });

    console.log('📌 [DEBUG] Postes vides :', postesVides);
    console.log('📌 [DEBUG] Joueurs sans poste :', joueursSansPoste);

    // 🔄 **Étape 2 : Compléter la défense avec des milieux pouvant descendre**
    let defenseCompletee = false;
    while (!defenseCompletee) {
      let mouvement = false;
      for (let poste of formation.defense) {
        if (!nouveauxJoueursTerrain[poste]) {
          let milieuDescendant = Object.keys(nouveauxJoueursTerrain).find(
            (pos) =>
              formation.milieu.includes(pos) &&
              this.getPostesCompatibles(nouveauxJoueursTerrain[pos]?.poste || '').includes(poste),
          );

          if (milieuDescendant) {
            console.log(
              `🔄 [DESCENTE] ${nouveauxJoueursTerrain[milieuDescendant]?.nom} descend de ${milieuDescendant} à ${poste}`,
            );
            nouveauxJoueursTerrain[poste] = nouveauxJoueursTerrain[milieuDescendant];
            delete nouveauxJoueursTerrain[milieuDescendant];
            mouvement = true;
          }
        }
      }
      if (!mouvement) defenseCompletee = true;
    }
    console.log('📌 [DEBUG] Défense verrouillée :', nouveauxJoueursTerrain);

    // 🔄 **Étape 3 : Tester immédiatement les joueurs hors terrain pour les postes libérés en milieu**
    let postesMilieuxLibres = formation.milieu.filter((p) => !nouveauxJoueursTerrain[p]);
    joueursSansPoste = joueursSansPoste.filter((joueur) => {
      let posteMilieu = postesMilieuxLibres.find((p) =>
        this.getPostesCompatibles(joueur.poste).includes(p),
      );

      if (posteMilieu) {
        console.log(`✅ [PLACEMENT DIRECT] ${joueur.nom} prend le poste de ${posteMilieu}`);
        nouveauxJoueursTerrain[posteMilieu] = joueur;
        postesMilieuxLibres = postesMilieuxLibres.filter((p) => p !== posteMilieu);
        return false;
      }
      return true;
    });

    console.log('📌 [DEBUG] Milieux après tentative directe :', nouveauxJoueursTerrain);

    // 🔄 **Étape 4 : Si des postes de milieu restent vides, on bouge un milieu du terrain**
    let milieuCompletee = false;
    while (!milieuCompletee) {
      let mouvement = false;
      for (let poste of formation.milieu) {
        if (!nouveauxJoueursTerrain[poste]) {
          let autreMilieu = Object.keys(nouveauxJoueursTerrain).find(
            (pos) =>
              formation.milieu.includes(pos) &&
              this.getPostesCompatibles(nouveauxJoueursTerrain[pos]?.poste || '').includes(poste),
          );

          if (autreMilieu) {
            console.log(
              `🔄 [RÉAGENCEMENT] ${nouveauxJoueursTerrain[autreMilieu]?.nom} passe de ${autreMilieu} à ${poste}`,
            );
            nouveauxJoueursTerrain[poste] = nouveauxJoueursTerrain[autreMilieu];
            delete nouveauxJoueursTerrain[autreMilieu];
            mouvement = true;

            // ✅ Vérifier immédiatement si un joueur hors terrain peut prendre la place laissée par le milieu
            let joueurHorsTerrain = joueursSansPoste.find((j) =>
              this.getPostesCompatibles(j.poste).includes(autreMilieu),
            );
            if (joueurHorsTerrain) {
              console.log(
                `✅ [RÉINTRODUCTION] ${joueurHorsTerrain.nom} prend le poste libéré ${autreMilieu}`,
              );
              nouveauxJoueursTerrain[autreMilieu] = joueurHorsTerrain;
              joueursSansPoste = joueursSansPoste.filter((j) => j !== joueurHorsTerrain);
            }
          }
        }
      }
      if (!mouvement) milieuCompletee = true;
    }
    console.log('📌 [DEBUG] Milieux verrouillés :', nouveauxJoueursTerrain);

    // 🔄 **Étape 5 : Tester immédiatement les joueurs hors terrain pour l'attaque**
    let postesAttaqueLibres = formation.attaque.filter((p) => !nouveauxJoueursTerrain[p]);
    joueursSansPoste = joueursSansPoste.filter((joueur) => {
      let posteAttaque = postesAttaqueLibres.find((p) =>
        this.getPostesCompatibles(joueur.poste).includes(p),
      );

      if (posteAttaque) {
        console.log(`✅ [PLACEMENT FINAL] ${joueur.nom} prend le poste de ${posteAttaque}`);
        nouveauxJoueursTerrain[posteAttaque] = joueur;
        postesAttaqueLibres = postesAttaqueLibres.filter((p) => p !== posteAttaque);
        return false;
      }
      return true;
    });

    console.log('📌 [DEBUG] Attaque finalisée :', nouveauxJoueursTerrain);

    // ✅ Ajouter les joueurs sans poste aux disponibles
    this.joueursDisponibles = [...this.joueursDisponibles, ...joueursSansPoste];

    // ✅ Appliquer la mise à jour du terrain
    this.terrainJoueurs = nouveauxJoueursTerrain;

    console.log('📌 [DEBUG] Nouvelle disposition des joueurs :', this.terrainJoueurs);
    console.log('📌 [DEBUG] Joueurs restants sans poste (remplaçants) :', joueursSansPoste);

    this.cdr.detectChanges();
  }

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

  /**
   * 📌 Valide la formation et enregistre le terrain
   */
  validerTerrain(): void {
    console.log('✅ Bouton cliqué !');
    if (!this.terrain) {
      console.error('❌ Terrain non initialisé.');
      return;
    }

    if (!this.estTerrainComplet()) {
      alert('⚠️ Tous les postes doivent être remplis avant de valider !');
      return;
    }

    // 🔄 **Met à jour `titulaires` AVANT l'envoi au backend**
    this.terrain.titulaires = Object.values(this.terrain.terrainJoueurs).filter(
      (joueur): joueur is FeuilleDeMatchDTO => joueur !== null,
    );

    this.terrain.remplacants = [...this.joueursDisponibles];

    console.log(
      '📌 [DEBUG] TITULAIRES envoyés au backend :',
      JSON.stringify(this.terrain.titulaires, null, 2),
    );

    this.terrainService.initialiserTerrain(this.terrain.idRencontre, this.terrain).subscribe({
      next: (terrainConfirme) => {
        console.log('✅ Terrain validé et mis à jour en base :', terrainConfirme);
        this.terrain = terrainConfirme; // 🔄 Mise à jour locale

        // ✅ **Redirection après confirmation**
        console.log(
          '📌 [DEBUG] Redirection vers le détail du terrain :',
          `/championnats/${this.terrain?.idChampionnat}/rencontres/${this.terrain?.idRencontre}/terrain/detail`,
        );
        this.router.navigate(
          [
            `/championnats/${this.terrain?.idChampionnat}/rencontres/${this.terrain?.idRencontre}/terrain/detail`,
          ],
          {
            state: { terrain: terrainConfirme },
          },
        );
      },
      error: (err) => {
        console.error('❌ Erreur lors de la validation du terrain :', err);
      },
    });
  }
}
