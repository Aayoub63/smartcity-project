const Dashboard = {
    velosData: [],
    transportsData: {
        arrets: [],
        attentes: []
    },
    meteoData: [],
    favorisData: [],
    filtreActuel: 'all',
    ongletActuel: 'velos',
    token: null,
    utilisateur: null,

    init: function() {
        this.chargerVelos();
        this.chargerTransports();
        this.chargerMeteo();
        
        setInterval(() => this.chargerVelos(), 5000);
        setInterval(() => this.chargerTransports(), 30000);
        setInterval(() => this.chargerMeteo(), 600000);
    },

    changerOnglet: function(onglet) {
        this.ongletActuel = onglet;
        document.querySelectorAll('.onglet').forEach(b => b.classList.remove('actif'));
        event.target.classList.add('actif');
        
        document.getElementById('contenu-velos').style.display = 'none';
        document.getElementById('contenu-transports').style.display = 'none';
        document.getElementById('contenu-meteo').style.display = 'none';
        document.getElementById('contenu-favoris').style.display = 'none';
        
        document.getElementById('contenu-' + onglet).style.display = 'block';
        
        if (onglet === 'velos') {
            this.afficherVelos();
        } else if (onglet === 'transports') {
            this.afficherTransports();
        } else if (onglet === 'meteo') {
            this.afficherMeteoDetail();
        } else if (onglet === 'favoris') {
            this.chargerFavoris();
            this.afficherFavoris();
        }
    },

    chargerVelos: function() {
        fetch('/api/bikes')
            .then(response => response.json())
            .then(data => {
                this.velosData = data;
                if (this.ongletActuel === 'velos') {
                    this.afficherVelos();
                }
                this.mettreAJourTimestamp();
            })
            .catch(error => {
                console.error('Erreur velos:', error);
            });
    },

    afficherVelos: function() {
        let totalVelos = 0;
        let totalPlaces = 0;
        
        this.velosData.forEach(s => { 
            totalVelos += s.bikesAvailable; 
            totalPlaces += s.docksAvailable; 
        });
        
        document.getElementById('total-stations').textContent = this.velosData.length;
        document.getElementById('total-velos').textContent = totalVelos;
        document.getElementById('total-places').textContent = totalPlaces;
        
        let html = '<table><thead><tr><th>ID</th><th>Nom</th><th>Velos</th><th>Places</th><th>Statut</th>';
        
        if (this.utilisateur) {
            html += '<th>Favori</th>';
        }
        
        html += '</tr></thead><tbody>';
        
        this.velosData.forEach(s => {
            let classe = 'vert';
            let texte = 'Disponible';
            
            if (s.bikesAvailable === 0) { 
                classe = 'rouge'; 
                texte = 'Vide'; 
            } else if (s.docksAvailable === 0) { 
                classe = 'orange'; 
                texte = 'Plein'; 
            }
            
            html += '<tr>';
            html += '<td>' + s.stationId + '</td>';
            html += '<td>' + (s.name || 'Inconnu') + '</td>';
            html += '<td>' + s.bikesAvailable + '</td>';
            html += '<td>' + s.docksAvailable + '</td>';
            html += '<td class="' + classe + '">' + texte + '</td>';
            
            if (this.utilisateur) {
                html += '<td><button class="bouton-favori" onclick="Dashboard.ajouterFavori(\'' + s.stationId + '\', \'VELO\', \'' + (s.name || 'Inconnu') + '\')">⭐</button></td>';
            }
            
            html += '</tr>';
        });
        
        html += '</tbody></table>';
        document.getElementById('tableau-velos').innerHTML = html;
    },

    chargerTransports: function() {
        fetch('/api/transit/arrets')
            .then(response => response.json())
            .then(data => {
                this.transportsData.arrets = data.map(a => JSON.parse(a));
                if (this.ongletActuel === 'transports') {
                    this.afficherTransports();
                }
            })
            .catch(error => {
                console.error('Erreur arrets:', error);
            });
        
        fetch('/api/transit/attentes')
            .then(response => response.json())
            .then(data => {
                this.transportsData.attentes = data.map(a => JSON.parse(a));
                if (this.ongletActuel === 'transports') {
                    this.afficherTransports();
                }
            })
            .catch(error => {
                console.error('Erreur attentes:', error);
            });
    },

    filtrerTransports: function() {
        this.filtreActuel = document.getElementById('filtre-ligne').value;
        this.afficherTransports();
    },

   afficherTransports: function() {
    console.log("Donnees arrets:", this.transportsData.arrets);
    console.log("Donnees attentes:", this.transportsData.attentes);
    
    let arrets = this.transportsData.arrets;
    let attentes = this.transportsData.attentes;
    
    if (this.filtreActuel !== 'all') {
        arrets = arrets.filter(a => a.ligne === this.filtreActuel);
        attentes = attentes.filter(a => a.ligne === this.filtreActuel);
    }
    
    console.log("Arrets apres filtre:", arrets);
    console.log("Attentes apres filtre:", attentes);
    
    let html = '<table><thead><tr><th>Ligne</th><th>Arret</th><th>Type</th><th>Commune</th><th>Prochain</th>';
    
    if (this.utilisateur) {
        html += '<th>Favori</th>';
    }
    
    html += '</tr></thead><tbody>';
    
    arrets.forEach(a => {
        let att = attentes.find(at => at.arret_id === a.arret_id);
        let prochain = att ? att.minutes + ' min' : '--';
        
        console.log("Arret:", a.arret_id, "Attente trouvee:", att);
        
        html += '<tr>';
        html += '<td>' + a.ligne + '</td>';
        html += '<td>' + a.nom + '</td>';
        html += '<td>' + a.type + '</td>';
        html += '<td>' + a.commune + '</td>';
        html += '<td>' + prochain + '</td>';
        
        if (this.utilisateur) {
            html += '<td><button class="bouton-favori" onclick="Dashboard.ajouterFavori(\'' + a.arret_id + '\', \'' + a.type.toUpperCase() + '\', \'' + a.nom + '\')">⭐</button></td>';
        }
        
        html += '</tr>';
    });
    
    html += '</tbody></table>';
    document.getElementById('tableau-transports').innerHTML = html;
},

    chargerMeteo: function() {
        fetch('/api/meteo')
            .then(response => response.json())
            .then(data => {
                this.meteoData = data.map(m => JSON.parse(m));
                this.afficherBandeauMeteo();
                if (this.ongletActuel === 'meteo') {
                    this.afficherMeteoDetail();
                }
            })
            .catch(error => {
                console.error('Erreur meteo:', error);
                document.getElementById('bandeau-meteo').innerHTML = '<div class="meteo-loading">Meteo indisponible</div>';
            });
    },

    afficherBandeauMeteo: function() {
        if (this.meteoData.length === 0) {
            document.getElementById('bandeau-meteo').innerHTML = '<div class="meteo-loading">Meteo non disponible</div>';
            return;
        }
        
        let html = '<div class="meteo-liste">';
        
        this.meteoData.forEach(m => {
            html += '<div class="meteo-item">';
            html += '<div class="meteo-ville">' + m.ville + '</div>';
            html += '<div class="meteo-temp">' + m.temperature + '°C</div>';
            html += '<div class="meteo-desc">' + m.description + '</div>';
            html += '</div>';
        });
        
        html += '</div>';
        document.getElementById('bandeau-meteo').innerHTML = html;
    },

    afficherMeteoDetail: function() {
        if (this.meteoData.length === 0) {
            document.getElementById('meteo-detail').innerHTML = '<p class="erreur">Aucune donnee meteo disponible</p>';
            return;
        }
        
        let html = '<div class="meteo-grille">';
        
        this.meteoData.forEach(m => {
            let date = new Date(m.timestamp * 1000);
            let heures = date.getHours().toString().padStart(2, '0');
            let minutes = date.getMinutes().toString().padStart(2, '0');
            
            html += '<div class="carte-meteo">';
            html += '<h3>' + m.ville + '</h3>';
            html += '<div class="temperature">' + m.temperature + '°C</div>';
            html += '<div class="description">' + m.description + '</div>';
            html += '<div class="humidite">Humidite: ' + m.humidite + '%</div>';
            html += '<div class="timestamp">' + heures + ':' + minutes + '</div>';
            html += '</div>';
        });
        
        html += '</div>';
        document.getElementById('meteo-detail').innerHTML = html;
    },

    mettreAJourTimestamp: function() {
        let d = new Date();
        let heures = d.getHours().toString().padStart(2, '0');
        let minutes = d.getMinutes().toString().padStart(2, '0');
        let secondes = d.getSeconds().toString().padStart(2, '0');
        document.getElementById('timestamp').textContent = heures + ':' + minutes + ':' + secondes;
    },

    ouvrirPopup: function() {
        document.getElementById('popup-connexion').style.display = 'flex';
    },

    fermerPopup: function() {
        document.getElementById('popup-connexion').style.display = 'none';
    },

    changerOngletPopup: function(onglet) {
        document.querySelectorAll('.onglet-popup').forEach(b => b.classList.remove('actif'));
        event.target.classList.add('actif');
        
        if (onglet === 'connexion') {
            document.getElementById('form-connexion').style.display = 'block';
            document.getElementById('form-inscription').style.display = 'none';
        } else {
            document.getElementById('form-connexion').style.display = 'none';
            document.getElementById('form-inscription').style.display = 'block';
        }
    },

    seConnecter: function() {
        let email = document.getElementById('login-email').value;
        let password = document.getElementById('login-password').value;

        fetch('/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email: email, motDePasse: password })
        })
        .then(response => response.json())
        .then(data => {
            if (data.token) {
                this.token = data.token;
                this.utilisateur = data;
                this.fermerPopup();
                this.afficherInfoUtilisateur();
                this.rafraichirAffichage();
            } else {
                this.afficherMessagePopup(data, 'error');
            }
        })
        .catch(error => {
            this.afficherMessagePopup('Erreur de connexion', 'error');
        });
    },

    sInscrire: function() {
        let nom = document.getElementById('register-nom').value;
        let email = document.getElementById('register-email').value;
        let password = document.getElementById('register-password').value;

        fetch('/api/auth/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ 
                nom: nom,
                email: email, 
                motDePasse: password 
            })
        })
        .then(response => response.json())
        .then(data => {
            if (data.token) {
                this.token = data.token;
                this.utilisateur = data;
                this.fermerPopup();
                this.afficherInfoUtilisateur();
                this.rafraichirAffichage();
            } else {
                this.afficherMessagePopup(data, 'error');
            }
        })
        .catch(error => {
            this.afficherMessagePopup('Erreur inscription', 'error');
        });
    },

    afficherMessagePopup: function(message, type) {
        let div = document.getElementById('message-popup');
        div.textContent = message;
        div.className = 'message-popup ' + type;
        setTimeout(() => {
            div.textContent = '';
            div.className = 'message-popup';
        }, 3000);
    },

    afficherInfoUtilisateur: function() {
        if (this.utilisateur) {
            document.getElementById('bouton-connexion').style.display = 'none';
            let info = document.getElementById('info-utilisateur');
            info.style.display = 'block';
            info.innerHTML = 'Bonjour ' + (this.utilisateur.nom || this.utilisateur.email) + 
                ' <button onclick="Dashboard.deconnexion()">Deconnexion</button>';
        }
    },

    deconnexion: function() {
        this.token = null;
        this.utilisateur = null;
        document.getElementById('bouton-connexion').style.display = 'block';
        document.getElementById('info-utilisateur').style.display = 'none';
        this.rafraichirAffichage();
    },

    rafraichirAffichage: function() {
        if (this.ongletActuel === 'velos') {
            this.afficherVelos();
        } else if (this.ongletActuel === 'transports') {
            this.afficherTransports();
        } else if (this.ongletActuel === 'favoris') {
            this.chargerFavoris();
        }
    },

    ajouterFavori: function(stationId, type, nom) {
        if (!this.utilisateur) {
            alert("Vous devez etre connecte pour ajouter des favoris");
            this.ouvrirPopup();
            return;
        }

        console.log("Ajout favori:", stationId, type, nom);
        console.log("Token:", this.token);

        fetch('/api/favoris', {
            method: 'POST',
            headers: { 
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + this.token
            },
            body: JSON.stringify({ 
                stationId: stationId,
                type: type,
                nom: nom
            })
        })
        .then(response => {
            console.log("Status:", response.status);
            if (response.ok) {
                alert('Station ajoutee aux favoris');
            } else if (response.status === 401) {
                alert('Session expiree, veuillez vous reconnecter');
                this.deconnexion();
            } else {
                response.text().then(text => alert('Erreur: ' + text));
            }
        })
        .catch(error => {
            console.error('Erreur:', error);
            alert('Erreur de connexion');
        });
    },

    chargerFavoris: function() {
        if (!this.utilisateur) return;
        
        fetch('/api/favoris', {
            headers: { 'Authorization': 'Bearer ' + this.token }
        })
        .then(response => response.json())
        .then(data => {
            this.favorisData = data;
            if (this.ongletActuel === 'favoris') {
                this.afficherFavoris();
            }
        })
        .catch(error => {
            console.error('Erreur chargement favoris:', error);
        });
    },

    afficherFavoris: function() {
        if (!this.favorisData || this.favorisData.length === 0) {
            document.getElementById('tableau-favoris').innerHTML = '<p>Aucun favori pour le moment</p>';
            return;
        }

        let html = '<table><thead><tr><th>Type</th><th>Station</th><th>Nom</th><th>Action</th></tr></thead><tbody>';
        
        this.favorisData.forEach(f => {
            html += '<tr>';
            html += '<td>' + f.type + '</td>';
            html += '<td>' + f.stationId + '</td>';
            html += '<td>' + f.nom + '</td>';
            html += '<td><button class="bouton-favori" onclick="Dashboard.supprimerFavori(\'' + f.id + '\', \'' + f.stationId + '\')">🗑️</button></td>';
            html += '</tr>';
        });
        
        html += '</tbody></table>';
        document.getElementById('tableau-favoris').innerHTML = html;
    },

    supprimerFavori: function(id, stationId) {
        if (!confirm('Supprimer ce favori ?')) return;

        fetch('/api/favoris/' + stationId, {
            method: 'DELETE',
            headers: { 'Authorization': 'Bearer ' + this.token }
        })
        .then(response => {
            if (response.ok) {
                alert('Favori supprime');
                this.chargerFavoris();
                if (this.ongletActuel === 'velos') this.afficherVelos();
                if (this.ongletActuel === 'transports') this.afficherTransports();
            } else {
                alert('Erreur lors de la suppression');
            }
        })
        .catch(error => {
            console.error('Erreur:', error);
            alert('Erreur de connexion');
        });
    }
};

function changerOnglet(onglet) { 
    Dashboard.changerOnglet(onglet); 
}

function filtrerTransports() { 
    Dashboard.filtrerTransports(); 
}

function ouvrirPopup() {
    Dashboard.ouvrirPopup();
}

function fermerPopup() {
    Dashboard.fermerPopup();
}

function changerOngletPopup(onglet) {
    Dashboard.changerOngletPopup(onglet);
}

function seConnecter() {
    Dashboard.seConnecter();
}

function sInscrire() {
    Dashboard.sInscrire();
}

document.addEventListener('DOMContentLoaded', function() {
    Dashboard.init();
});