package com.example.demo.controller;

import com.example.demo.model.Utilisateur;
import com.example.demo.repository.UtilisateurRepository;
import com.example.demo.security.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(
            UtilisateurRepository utilisateurRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Utilisateur utilisateur) {
        if (utilisateurRepository.findByEmail(utilisateur.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email deja utilise");
        }

        utilisateur.setMotDePasse(passwordEncoder.encode(utilisateur.getMotDePasse()));
        Utilisateur saved = utilisateurRepository.save(utilisateur);

        String token = jwtService.generateToken(saved.getEmail());

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("id", saved.getId());
        response.put("email", saved.getEmail());
        response.put("nom", saved.getNom());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Utilisateur utilisateur) {
        Optional<Utilisateur> existing = utilisateurRepository.findByEmail(utilisateur.getEmail());

        if (existing.isEmpty()) {
            return ResponseEntity.badRequest().body("Email ou mot de passe incorrect");
        }

        Utilisateur user = existing.get();

        if (!passwordEncoder.matches(utilisateur.getMotDePasse(), user.getMotDePasse())) {
            return ResponseEntity.badRequest().body("Email ou mot de passe incorrect");
        }

        String token = jwtService.generateToken(user.getEmail());

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("id", user.getId());
        response.put("email", user.getEmail());
        response.put("nom", user.getNom());

        return ResponseEntity.ok(response);
    }
}
