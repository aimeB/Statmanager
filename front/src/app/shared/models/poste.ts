import { JoueurDTO } from '../../modules/joueur/models/joueur.model';

export interface Poste {
    key: string;
    label: string;
    joueur: JoueurDTO | null;
  }
  

 
