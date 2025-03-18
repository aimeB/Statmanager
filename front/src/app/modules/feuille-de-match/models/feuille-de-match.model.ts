/**
 * 📌 DTO représentant une Feuille de Match d'un joueur.
 */
export interface FeuilleDeMatchDTO {
  id: number;
  rid: number;
  jid: number;
  nom: string;
  poste: string;
  buts: number;
  passes: number;
  cote: number;
  minutesJouees: number;
  titulaire: boolean;
  ajoue: boolean;
  butArreter: number;
  butEncaisser: number;
  passeursId: number[];
  nomsPasseurs: string[];
}

/**
 * 📌 Convertit un objet brut en `FeuilleDeMatchDTO`.
 */
export function fromRawFeuilleDeMatch(data: any): FeuilleDeMatchDTO {
  return {
    id: data.id,
    rid: data.rid,
    jid: data.jid,
    nom: data.nom,
    poste: data.poste || 'INCONNU',
    buts: data.buts || 0,
    passes: data.passes || 0,
    cote: data.cote || 5.0,
    minutesJouees: data.minutesJouees || 0,
    titulaire: data.titulaire || false,
    ajoue: data.aJoue || false,
    butArreter: data.butArreter || 0,
    butEncaisser: data.butEncaisser || 0,
    passeursId: data.passeursId ? [...data.passeursId] : [],
    nomsPasseurs: data.nomsPasseurs ? [...data.nomsPasseurs] : [],
  };
}
