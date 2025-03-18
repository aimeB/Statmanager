import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

// ✅ Importation des modules Angular Material (OBLIGATOIRE dans un standalone component)
import { MatMenuModule } from '@angular/material/menu';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-menu',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatMenuModule, // ✅ Nécessaire pour afficher <mat-menu>
    MatButtonModule, // ✅ Nécessaire pour les boutons Angular Material
    MatIconModule, // ✅ Nécessaire pour les icônes
  ],
  templateUrl: './menu.component.html',
  styleUrls: ['./menu.component.scss'],
})
export class MenuComponent {}
