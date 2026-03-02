package com.example.demo.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;  // ← Ajouter cet import
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "utilisateurs")
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String motDePasse;

    private String nom;

    @JsonIgnore  // ← Ajouter cette ligne
    @OneToMany(mappedBy = "utilisateur", cascade = CascadeType.ALL)
    private List<Favori> favoris = new ArrayList<>();

    // Getters et setters (inchangés)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public List<Favori> getFavoris() { return favoris; }
    public void setFavoris(List<Favori> favoris) { this.favoris = favoris; }
}