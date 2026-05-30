package com.example.demo.controller;

import com.example.demo.listener.NatsMeteoListener;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MeteoController.class)
@AutoConfigureMockMvc(addFilters = false)
class MeteoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NatsMeteoListener natsMeteoListener;

    @Test
    void shouldReturnMeteoByVille() throws Exception {
        String mockJson = "{\"ville\":\"Clermont-Ferrand\",\"temperature\":15.5}";
        when(natsMeteoListener.getByVille("clermont")).thenReturn(mockJson);

        mockMvc.perform(get("/api/meteo/clermont"))
                .andExpect(status().isOk())
                .andExpect(content().json(mockJson));
    }
}