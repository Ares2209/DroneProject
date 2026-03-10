# Crazyflie Mission Planner

Application interactive pour planifier des missions pour le drone Crazyflie.

## Description

Cette application permet aux développeurs de planifier des trajectoires pour le drone Crazyflie en dessinant sur une interface graphique. Elle génère ensuite un script Python utilisant la bibliothèque crazyflie-lib-python pour exécuter la mission.

## Fonctionnalités

- Dessin de trajectoires en 2D avec spécification d'altitude
- Sauvegarde et chargement de missions au format JSON
- Génération de scripts Python pour le contrôle du drone
- Visualisation de la trajectoire planifiée et effective
- Interface utilisateur avec JavaFX

## Prérequis

- Java 11 ou supérieur
- Maven
- Bibliothèque Python crazyflie-lib-python (pour l'exécution des missions)

## Installation

1. Cloner le repository
2. Compiler avec Maven : `mvn clean compile`
3. Lancer l'application : `mvn javafx:run`

## Structure du projet

- `src/main/java/com/enac/crazyflie/` : Code source Java
- `src/main/resources/` : Ressources FXML et autres
- `pom.xml` : Configuration Maven

## Utilisation

1. Lancer l'application
2. Cliquer sur "Start Drawing" pour commencer à dessiner la trajectoire
3. Cliquer sur le canvas pour ajouter des waypoints
4. Spécifier l'altitude et la vitesse
5. Générer le script Python via le menu Mission
6. Exécuter la mission (nécessite un drone Crazyflie connecté)

## Améliration à faire 

Rajouter la distance sur les traits entre les différents waypoints
Rajouter la possibilité d'une séléction de point et de leur déplacement. 
Rajouter la possibilité de zoomer
Rajouter la modification de notre type de drone. 