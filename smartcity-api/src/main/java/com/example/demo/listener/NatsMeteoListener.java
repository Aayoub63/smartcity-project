package com.example.demo.listener;

import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.JetStream;
import io.nats.client.JetStreamSubscription;
import io.nats.client.Nats;
import io.nats.client.PushSubscribeOptions;
import io.nats.client.api.ConsumerConfiguration;
import io.nats.client.api.DeliverPolicy;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class NatsMeteoListener {

    private final Map<String, String> meteoCache = new ConcurrentHashMap<>();
    private Connection nc;
    private Dispatcher dispatcher;
    private JetStream js;

    @PostConstruct
    public void init() {
        try {
            String natsUrl = System.getenv("NATS_URL");
            if (natsUrl == null || natsUrl.isEmpty()) {
                natsUrl = "nats://127.0.0.1:4222";
                System.out.println("[INFO] Utilisation de l'URL NATS par defaut pour la meteo: " + natsUrl);
            } else {
                System.out.println("[INFO] Utilisation de l'URL NATS depuis l'environnement pour la meteo: " + natsUrl);
            }

            io.nats.client.Options options = new io.nats.client.Options.Builder()
                    .server(natsUrl)
                    .connectionName("meteo-listener")
                    .reconnectWait(Duration.ofSeconds(2))
                    .maxReconnects(-1)
                    .build();

            nc = Nats.connect(options);
            dispatcher = nc.createDispatcher(msg -> {});
            js = nc.jetStream();
            
            System.out.println("Connecte a NATS JetStream pour la meteo");

            String consumerName = "meteo-consumer-durable";
            
            ConsumerConfiguration consumerConfig = ConsumerConfiguration.builder()
                    .durable(consumerName)
                    .deliverPolicy(DeliverPolicy.New)
                    .ackWait(Duration.ofSeconds(30))
                    .build();
            
            PushSubscribeOptions pushOptions = PushSubscribeOptions.builder()
                    .stream("weather_stream")
                    .durable(consumerName)
                    .configuration(consumerConfig)
                    .build();
            
            JetStreamSubscription sub = js.subscribe(
                "city.weather.*", 
                dispatcher, 
                msg -> {
                    try {
                        String messageJson = new String(msg.getData(), StandardCharsets.UTF_8);
                        
                        String sujet = msg.getSubject();
                        String[] parties = sujet.split("\\.");
                        String ville = parties[parties.length - 1];
                        
                        meteoCache.put(ville, messageJson);
                        msg.ack();
                        
                        System.out.println("Meteo recue pour " + ville);
                    } catch (Exception e) {
                        System.err.println("Erreur traitement meteo: " + e.getMessage());
                    }
                },
                false,
                pushOptions
            );

            System.out.println("En ecoute sur city.weather.* via JetStream avec consumer: " + consumerName);

        } catch (Exception e) {
            System.err.println("Erreur connexion NATS meteo: " + e.getMessage());
        }
    }

    public Map<String, String> getAll() {
        return meteoCache;
    }

    public String getByVille(String ville) {
        return meteoCache.get(ville);
    }
}