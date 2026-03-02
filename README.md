# Smart City Clermont-Ferrand

Plateforme de supervision en temps réel pour la métropole clermontoise. Le système collecte et affiche les données des vélos C.velo, des transports T2C et de la météorologie, avec gestion d'utilisateurs et de favoris.

## Technologies utilisées

- **Backend** : Java 17, Spring Boot 4.0.3, Spring Security, JWT, JPA/Hibernate
- **Base de données** : PostgreSQL 16
- **Middleware** : NATS Server 2.10 avec JetStream (messaging asynchrone et persistant)
- **Ingestion** : Go 1.24, Protocol Buffers (GTFS-RT), OpenWeatherMap API
- **Frontend** : HTML5, CSS3, JavaScript vanilla
- **Déploiement** : Docker, Docker Compose

## Architecture

```
[APIs Externes] -> [Workers Go] -> [NATS JetStream] -> [Spring Boot] -> [REST API] -> [Dashboard]
     C.velo         cvelo_worker      city.bikes.*      Listeners        /api/bikes
     T2C            t2c_worker        city.transit.*    Controllers      /api/transit
     Meteo          meteo_worker      city.weather.*    JPA              /api/meteo
                                                          Security         /api/auth
                                                                           /api/favoris
```

## Prérequis

- Docker Desktop (avec intégration WSL2 activée sous Windows)
- Git

## Installation et lancement

### 1. Cloner le projet

```bash
git clone https://github.com/Aayoub63/smartcity-project.git  
cd smartcity-project
```

### 2. Configurer la clé API météo (gratuite)

Le projet utilise OpenWeatherMap pour les données météorologiques.

1. Créez un compte gratuit sur [OpenWeatherMap](https://openweathermap.org/api)
2. Dans votre tableau de bord, allez dans "API Keys" et générez une clé
3. Copiez la clé (l'activation peut prendre 1 à 2 heures)

### 3. Configurer les variables d'environnement

```bash
# Copier le fichier d'exemple
cp .env.example .env

# Éditer le fichier avec votre clé
nano .env
```

Dans le fichier `.env`, remplacez `votre_cle_api_ici` par votre vraie clé OpenWeatherMap.

### 4. Lancer le projet

```bash
docker compose up -d
```

### 5. Accéder au dashboard

Ouvrez votre navigateur à l'adresse : http://localhost:8080

### 6. Créer un compte utilisateur

- Cliquez sur "Connexion" en haut à droite
- Allez dans l'onglet "Inscription"
- Remplissez le formulaire
### 7. Initialisation JetStream (optionnel)

Si les streams JetStream venaient à être supprimés, vous pouvez les recréer avec :

```bash
cd scripts
./init-jetstream.sh

## Fonctionnalités

### Vélos
- Affichage en temps réel des stations C.velo
- Nombre de vélos disponibles et places libres
- Code couleur : vert (disponible), orange (plein), rouge (vide)

### Transports
- Arrêts de bus et tramways du réseau T2C
- Temps d'attente en temps réel
- Filtrage par ligne

### Météo
- Température par ville
- Description (ciel dégagé, nuageux, etc.)
- Humidité
- Bandeau permanent en haut du dashboard

### Utilisateurs et favoris
- Inscription et connexion sécurisée par JWT
- Ajout de stations en favori
- Liste personnalisée des favoris
- Suppression des favoris

## Commandes utiles

```bash
# Voir l'état des services
docker compose ps

# Voir les logs
docker compose logs -f

# Voir les logs d'un service spécifique
docker compose logs api
docker compose logs worker-meteo

# Redémarrer un service
docker compose restart api

# Arrêter tous les services
docker compose down

# Arrêter et supprimer les volumes (supprime les données)
docker compose down -v
```

## Tests des APIs

```bash
# Inscription
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"nom":"Jean","email":"jean@email.com","motDePasse":"password"}'

# Connexion
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"jean@email.com","motDePasse":"password"}'

# Vélos
curl http://localhost:8080/api/bikes

# Transports
curl http://localhost:8080/api/transit/arrets

# Météo
curl http://localhost:8080/api/meteo
```

## Dépannage

### La météo n'affiche pas de données
```bash
# Vérifier que la clé API est bien configurée
docker exec smartcity-worker-meteo env | grep OPENWEATHER

# Vérifier les logs du worker météo
docker compose logs worker-meteo
```

### Les transports n'affichent pas de données
Les flux T2C sont disponibles uniquement en journée (6h-22h).

### Erreur de connexion à la base de données
```bash
# Vérifier que PostgreSQL est bien lancé
docker compose ps postgres

# Voir les logs PostgreSQL
docker compose logs postgres
```

## Version actuelle

**Version 1.0.0** - Mars 2026
- Première version fonctionnelle
- Collecte des données C.velo, T2C et météo
- Messaging persistant avec NATS JetStream
- Dashboard temps réel
- Gestion des utilisateurs et favoris

## Améliorations prévues
- Tests unitaires supplémentaires
- Ajout de graphiques d'évolution
- Support mobile

## Auteur

**Ayoub** - Master Informatique
- GitHub : [@Aayoub63](https://github.com/Aayoub63)
