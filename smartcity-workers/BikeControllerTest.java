package com.example.demo.controller;

import com.example.demo.listener.NatsBikeListener;
import com.example.demo.model.StationData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BikeController.class)
@AutoConfigureMockMvc(addFilters = false)
class BikeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NatsBikeListener natsBikeListener;

    @Test
    void shouldReturnBikesList() throws Exception {
        StationData mockStation = new StationData("1", "Jaude", 10, 5);
        when(natsBikeListener.getAllStations()).thenReturn(Map.of("1", mockStation));

        mockMvc.perform(get("/api/bikes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Jaude"))
                .andExpect(jsonPath("$[0].bikesAvailable").value(10));
    }
}