package com.example.demo.controller;

import com.example.demo.model.Favori;
import com.example.demo.model.Utilisateur;
import com.example.demo.repository.FavoriRepository;
import com.example.demo.repository.UtilisateurRepository;
import com.example.demo.security.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/favoris")
public class FavoriController {

    private final FavoriRepository favoriRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final JwtService jwtService;

    public FavoriController(
            FavoriRepository favoriRepository,
            UtilisateurRepository utilisateurRepository,
            JwtService jwtService) {
        this.favoriRepository = favoriRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.jwtService = jwtService;
    }

    private Long getUserIdFromToken(String token) {
        try {
            String email = jwtService.extractUsername(token.replace("Bearer ", ""));
            Optional<Utilisateur> user = utilisateurRepository.findByEmail(email);
            return user.map(Utilisateur::getId).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    @GetMapping
    public ResponseEntity<?> getMesFavoris(@RequestHeader("Authorization") String token) {
        Long userId = getUserIdFromToken(token);
        if (userId == null) {
            return ResponseEntity.status(401).body("Non authentifie");
        }

        List<Favori> favoris = favoriRepository.findByUtilisateurId(userId);
        return ResponseEntity.ok(favoris);
    }

    @PostMapping
    public ResponseEntity<?> ajouterFavori(
            @RequestHeader("Authorization") String token,
            @RequestBody Favori favori) {

        Long userId = getUserIdFromToken(token);
        if (userId == null) {
            return ResponseEntity.status(401).body("Non authentifie");
        }

        Optional<Utilisateur> user = utilisateurRepository.findById(userId);
        if (user.isEmpty()) {
            return ResponseEntity.badRequest().body("Utilisateur non trouve");
        }

        boolean exists = favoriRepository.existsByUtilisateurIdAndStationId(userId, favori.getStationId());
        if (exists) {
            return ResponseEntity.badRequest().body("Deja en favori");
        }

        favori.setUtilisateur(user.get());
        Favori saved = favoriRepository.save(favori);

        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{stationId}")
    public ResponseEntity<?> supprimerFavori(
            @RequestHeader("Authorization") String token,
            @PathVariable String stationId) {

        Long userId = getUserIdFromToken(token);
        if (userId == null) {
            return ResponseEntity.status(401).body("Non authentifie");
        }

        try {
            favoriRepository.deleteByUtilisateurIdAndStationId(userId, stationId);
            return ResponseEntity.ok("Favori supprime");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur lors de la suppression: " + e.getMessage());
        }
    }
}