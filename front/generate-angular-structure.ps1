#  Définition du bon chemin (évite app/app)
$rootPath = "src/app"

#  Création des dossiers
New-Item -Path "$rootPath/services" -ItemType Directory -Force
New-Item -Path "$rootPath/models" -ItemType Directory -Force
New-Item -Path "$rootPath/components/championnat" -ItemType Directory -Force
New-Item -Path "$rootPath/components/rencontre" -ItemType Directory -Force
New-Item -Path "$rootPath/components/terrain" -ItemType Directory -Force
New-Item -Path "$rootPath/components/joueur" -ItemType Directory -Force

#  Génération des modèles
ng g class src/app/models/championnat.model --type=model
ng g class src/app/models/rencontre.model --type=model
ng g class src/app/models/feuille-de-match.model --type=model
ng g class src/app/models/joueur.model --type=model
ng g class src/app/models/stat-championnat.model --type=model
ng g class src/app/models/terrain.model --type=model
ng g class src/app/models/cloture-rencontre.model --type=model

#  Génération des services
ng g service src/app/services/championnat
ng g service src/app/services/rencontre
ng g service src/app/services/feuille-de-match
ng g service src/app/services/joueur
ng g service src/app/services/stat-championnat

#  Génération des composants
ng g component src/app/components/championnat/championnat-list
ng g component src/app/components/championnat/championnat-detail
ng g component src/app/components/rencontre/rencontre-list
ng g component src/app/components/rencontre/rencontre-detail
ng g component src/app/components/terrain
ng g component src/app/components/joueur/joueur-list
ng g component src/app/components/joueur/joueur-detail
