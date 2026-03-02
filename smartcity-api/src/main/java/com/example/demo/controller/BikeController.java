package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.listener.NatsBikeListener;
import com.example.demo.model.StationData;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/bikes")
public class BikeController {

    private final NatsBikeListener natsListener;

    public BikeController(NatsBikeListener natsListener) {
        this.natsListener = natsListener;
    }

    @GetMapping
    public List<StationData> getBikes() {
        // Retourne directement les objets StationData (Spring les convertit en JSON)
        return new ArrayList<>(natsListener.getAllStations().values());
    }
}