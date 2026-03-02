package com.example.demo.service;

import com.example.demo.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    public void init() {
        jwtService = new JwtService();
    }

    @Test
    public void genererTokenRetourneUneChaine() {
        String token = jwtService.generateToken("test@email.com");
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    @Test
    public void extraireEmailDuTokenRetourneLeBonEmail() {
        String email = "test@email.com";
        String token = jwtService.generateToken(email);
        String emailExtrait = jwtService.extractUsername(token);
        assertThat(emailExtrait).isEqualTo(email);
    }

    @Test
    public void tokenValidePourLeBonUtilisateur() {
        String email = "test@email.com";
        String token = jwtService.generateToken(email);
        boolean valide = jwtService.validateToken(token, email);
        assertThat(valide).isTrue();
    }
}