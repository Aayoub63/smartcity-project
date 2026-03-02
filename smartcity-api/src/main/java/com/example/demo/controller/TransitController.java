package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.listener.NatsTransitListener;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/transit")
public class TransitController {

    private final NatsTransitListener transitListener;

    public TransitController(NatsTransitListener transitListener) {
        this.transitListener = transitListener;
    }

    // Retourne tous les arrets
    @GetMapping("/arrets")
    public List<String> getAllArrets() {
        return new ArrayList<>(transitListener.getAllArrets().values());
    }

    // Retourne tous les temps d'attente
    @GetMapping("/attentes")
    public List<String> getAllAttentes() {
        return new ArrayList<>(transitListener.getAllAttentes().values());
    }

    // Retourne les arrets d'une ligne specifique (ex: /api/transit/arrets/ligne/A)
    @GetMapping("/arrets/ligne/{ligne}")
    public List<String> getArretsByLigne(@PathVariable String ligne) {
        return new ArrayList<>(transitListener.getArretsByLigne(ligne).values());
    }

    // Retourne les attentes d'une ligne specifique (ex: /api/transit/attentes/ligne/A)
    @GetMapping("/attentes/ligne/{ligne}")
    public List<String> getAttentesByLigne(@PathVariable String ligne) {
        return new ArrayList<>(transitListener.getAttentesByLigne(ligne).values());
    }

    // Retourne tout en une fois (arrets + attentes)
    @GetMapping("/all")
    public Object getAll() {
        return new Object() {
            public List<String> arrets = new ArrayList<>(transitListener.getAllArrets().values());
            public List<String> attentes = new ArrayList<>(transitListener.getAllAttentes().values());
        };
    }
}