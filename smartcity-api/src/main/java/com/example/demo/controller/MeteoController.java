package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.listener.NatsMeteoListener;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/meteo")
public class MeteoController {

    private final NatsMeteoListener meteoListener;

    public MeteoController(NatsMeteoListener meteoListener) {
        this.meteoListener = meteoListener;
    }

    @GetMapping
    public List<String> getAll() {
        return new ArrayList<>(meteoListener.getAll().values());
    }

    @GetMapping("/{ville}")
    public String getByVille(@PathVariable String ville) {
        return meteoListener.getByVille(ville);
    }
}