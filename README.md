# StatManager

Application web permettant de gérer les statistiques des joueurs de football lors d’un match.  
Développée avec **Java Spring Boot** pour le backend et **Angular** pour le frontend.

---

## Fonctionnalités principales

- Gestion des joueurs, postes et remplacements
- Suivi en temps réel des buts, passes et arrêts
- Popup de clôture de match avec édition des stats (cotes, minutes jouées, etc.)
- Calcul automatique des points et classements par division
- Interface intuitive pour visualiser et modifier les données

---

## Architecture technique

- **Backend** : Java, Spring Boot, JPA, MySQL
- **Frontend** : Angular
- **Architecture** : Microservices, REST APIs
- **Modélisation** : UML pour l’analyse et la conception

---

## Aperçu du projet

*(Ajoute ici des captures d’écran ou GIF du tableau de stats, popup fin de match, etc.)*

---

## Installation

### Prérequis
- Java 17+
- Node.js + Angular CLI
- MySQL

### Étapes
1. Cloner le repository :
```bash
git clone https://github.com/aimeB/Statmanager.git


Lancer le backend (Spring Boot) :

cd BACK
./mvnw spring-boot:run

Lancer le frontend (Angular) :

cd front
npm install
ng serve

Accéder à l’application : http://localhost:4200


Objectif du projet
Ce projet a été conçu pour apprendre et démontrer une architecture full stack moderne (Spring Boot + Angular) appliquée à un cas concret : la gestion de statistiques sportives.

