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

    @GetMapping("/arrets")
    public List<String> getAllArrets() {
        return new ArrayList<>(transitListener.getAllArrets().values());
    }

    @GetMapping("/attentes")
    public List<String> getAllAttentes() {
        return new ArrayList<>(transitListener.getAllAttentes().values());
    }

    @GetMapping("/arrets/ligne/{ligne}")
    public List<String> getArretsByLigne(@PathVariable String ligne) {
        return new ArrayList<>(transitListener.getArretsByLigne(ligne).values());
    }

    @GetMapping("/attentes/ligne/{ligne}")
    public List<String> getAttentesByLigne(@PathVariable String ligne) {
        return new ArrayList<>(transitListener.getAttentesByLigne(ligne).values());
    }

    public record TransitDataResponse(List<String> arrets, List<String> attentes) {}

    @GetMapping("/all")
    public TransitDataResponse getAll() {
        return new TransitDataResponse(
            new ArrayList<>(transitListener.getAllArrets().values()),
            new ArrayList<>(transitListener.getAllAttentes().values())
        );
    }
}