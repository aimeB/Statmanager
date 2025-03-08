#  Création des dossiers dans app/ directement (sans répéter "app/")
Write-Output " Création des dossiers..."
New-Item -Path "services" -ItemType Directory -Force
New-Item -Path "models" -ItemType Directory -Force
New-Item -Path "components/championnat" -ItemType Directory -Force
New-Item -Path "components/rencontre" -ItemType Directory -Force
New-Item -Path "components/terrain" -ItemType Directory -Force
New-Item -Path "components/joueur" -ItemType Directory -Force

#  Génération des modèles (sans répéter "app/")
Write-Output " Génération des modèles..."
ng g class models/championnat.model --type=model --skip-tests
ng g class models/rencontre.model --type=model --skip-tests
ng g class models/feuille-de-match.model --type=model --skip-tests
ng g class models/joueur.model --type=model --skip-tests
ng g class models/stat-championnat.model --type=model --skip-tests
ng g class models/terrain.model --type=model --skip-tests
ng g class models/cloture-rencontre.model --type=model --skip-tests

#  Génération des services (sans répéter "app/")
Write-Output " Génération des services..."
ng g service services/championnat --skip-tests
ng g service services/rencontre --skip-tests
ng g service services/feuille-de-match --skip-tests
ng g service services/joueur --skip-tests
ng g service services/stat-championnat --skip-tests

#  Génération des composants (sans répéter "app/")
Write-Output " Génération des composants..."
ng g component components/championnat/championnat-list --skip-tests
ng g component components/championnat/championnat-detail --skip-tests
ng g component components/rencontre/rencontre-list --skip-tests
ng g component components/rencontre/rencontre-detail --skip-tests
ng g component components/terrain --skip-tests
ng g component components/joueur/joueur-list --skip-tests
ng g component components/joueur/joueur-detail --skip-tests

Write-Output " Génération terminée avec succès ! "
