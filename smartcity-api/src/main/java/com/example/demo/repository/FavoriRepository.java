package com.example.demo.repository;

import com.example.demo.model.Favori;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface FavoriRepository extends JpaRepository<Favori, Long> {
    
    // Trouver tous les favoris d'un utilisateur
    List<Favori> findByUtilisateurId(Long utilisateurId);
    
    // Verifier si une station est deja en favori pour un utilisateur
    boolean existsByUtilisateurIdAndStationId(Long utilisateurId, String stationId);
    
    // Supprimer un favori par utilisateur et station
    @Transactional
    @Modifying
    @Query("DELETE FROM Favori f WHERE f.utilisateur.id = :userId AND f.stationId = :stationId")
    void deleteByUtilisateurIdAndStationId(@Param("userId") Long userId, @Param("stationId") String stationId);
}