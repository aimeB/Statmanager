
export enum CategoriePoste {
    GARDIEN = 'GARDIEN',
    DEFENSEUR = 'DEFENSEUR',
    MILIEU = 'MILIEU',
    ATTAQUANT = 'ATTAQUANT',
    INCONNU = 'INCONNU'
  }


  export class PosteUtils {
    static fromString(posteKey: string): CategoriePoste {
  
  
      if (!posteKey) {
        console.warn(`⚠️ Poste NULL ou UNDEFINED détecté, renvoyé en INCONNU.`);
        return CategoriePoste.INCONNU;
      }
  
      switch (posteKey.trim().toUpperCase()) { // 🔥 Normalisation des données
        case "GB":
          return CategoriePoste.GARDIEN;
        case "DC_GAUCHE":
        case "DC_DROIT":
        case "DG":
        case "DD":
          return CategoriePoste.DEFENSEUR;
        case "MDF":
        case "MO":
        case "MR":
        case "MLD":
        case "MLG":
          return CategoriePoste.MILIEU;
        case "AIG":
        case "AID":
        case "AC":
        case "SA":
          return CategoriePoste.ATTAQUANT;
        default:
          console.warn(`⚠️ Poste inconnu détecté : ${posteKey}, renvoyé en INCONNU.`);
          return CategoriePoste.INCONNU;
      }
    }
  
}



  