import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./shared/components/menu/menu.component').then((m) => m.MenuComponent), // ✅ Chargement dynamique
    children: [
      { path: 'championnats', loadComponent: () => import('./modules/championnat/components/championnat-list/championnat-list.component').then(m => m.ChampionnatListComponent) },
      { path: 'championnats/create', loadComponent: () => import('./modules/championnat/components/championnat-create/championnat-create.component').then(m => m.ChampionnatCreateComponent) },
      { path: 'championnats/:idChampionnat', loadComponent: () => import('./modules/championnat/components/championnat-detail/championnat-detail.component').then(m => m.ChampionnatDetailComponent) },

      // 📌 Gestion des rencontres
      { path: 'championnats/:idChampionnat/rencontres/selection', loadComponent: () => import('./modules/rencontre/components/rencontre-selection/rencontre-selection.component').then(m => m.RencontreSelectionComponent) },
      { 
        path: 'championnats/:idChampionnat/rencontres/:idRencontre/terrain',
        loadComponent: () => import('./modules/rencontre/components/terrain/terrain.component').then(m => m.TerrainComponent),
        children: [
          { path: '', pathMatch: 'full', redirectTo: 'detail' },
          { path: 'detail', loadComponent: () => import('./modules/rencontre/components/terrain/detail-terrain/detail-terrain.component').then(m => m.DetailTerrainComponent) }
        ]
      },
      { path: 'championnats/:idChampionnat/rencontres/:idRencontre', loadComponent: () => import('./modules/rencontre/components/rencontre-detail/rencontre-detail.component').then(m => m.RencontreDetailComponent) },
      { path: 'championnats/:idChampionnat/rencontres/:idRencontre/fin-match', loadComponent: () => import('./modules/rencontre/components/fin-de-match/fin-de-match.component').then(m => m.FinMatchComponent) },

      // 📌 Gestion des joueurs et statistiques
      { path: 'joueurs/all', loadComponent: () => import('./modules/joueur/components/joueur-list/joueur-list.component').then(m => m.JoueurListComponent) },
      { path: 'joueurs/:id', loadComponent: () => import('./modules/joueur/components/joueur-detail/joueur-detail.component').then(m => m.JoueurDetailComponent) },

      // 📌 Statistiques avancées
      { path: 'statistiques/critere', loadComponent: () => import('./modules/statistiques/components/stat-critere/stat-critere.component').then(m => m.StatCritereComponent) },
      { path: 'statistiques/derniers-matchs', loadComponent: () => import('./modules/statistiques/components/stat-top-joueur/stat-top-joueur.component').then(m => m.StatTopJoueurComponent) },
      { path: 'statistiques/composite', loadComponent: () => import('./modules/statistiques/components/stat-composite/stat-composite.component').then(m => m.StatCompositeComponent) },
    ],
  },

  // 📌 Redirection en cas d'erreur
  { path: '**', redirectTo: 'championnats' },
];
