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
public class NatsTransitListener {

    private final Map<String, String> arretsCache = new ConcurrentHashMap<>();
    private final Map<String, String> attentesCache = new ConcurrentHashMap<>();
    private Connection nc;
    private Dispatcher dispatcher;
    private JetStream js;

    @PostConstruct
    public void init() {
        try {
            String natsUrl = System.getenv("NATS_URL");
            if (natsUrl == null || natsUrl.isEmpty()) {
                natsUrl = "nats://127.0.0.1:4222";
                System.out.println("[INFO] Utilisation de l'URL NATS par defaut pour T2C: " + natsUrl);
            } else {
                System.out.println("[INFO] Utilisation de l'URL NATS depuis l'environnement pour T2C: " + natsUrl);
            }

            io.nats.client.Options options = new io.nats.client.Options.Builder()
                    .server(natsUrl)
                    .connectionName("transit-listener")
                    .reconnectWait(Duration.ofSeconds(2))
                    .maxReconnects(-1)
                    .build();

            nc = Nats.connect(options);
            dispatcher = nc.createDispatcher(msg -> {});
            js = nc.jetStream();
            
            System.out.println("Connecte a NATS JetStream pour le service T2C");

            // Consumer pour les arrets
            String arretsConsumerName = "transit-arrets-consumer-durable";
            
            ConsumerConfiguration arretsConfig = ConsumerConfiguration.builder()
                    .durable(arretsConsumerName)
                    .deliverPolicy(DeliverPolicy.New)
                    .ackWait(Duration.ofSeconds(30))
                    .build();
            
            PushSubscribeOptions arretsOptions = PushSubscribeOptions.builder()
                    .stream("transit_stream")
                    .durable(arretsConsumerName)
                    .configuration(arretsConfig)
                    .build();
            
            JetStreamSubscription arretsSub = js.subscribe(
                "city.transit.arret.*.*", 
                dispatcher, 
                msg -> {
                    try {
                        String messageJson = new String(msg.getData(), StandardCharsets.UTF_8);
                        String subject = msg.getSubject();
                        
                        String[] parts = subject.split("\\.");
                        String arretId = parts[parts.length - 1];
                        
                        arretsCache.put(arretId, messageJson);
                        msg.ack();
                        
                        System.out.println("Arret T2C recu: " + arretId);
                    } catch (Exception e) {
                        System.err.println("Erreur traitement arret: " + e.getMessage());
                    }
                },
                false,
                arretsOptions
            );

            // Consumer pour les attentes
            String attentesConsumerName = "transit-attentes-consumer-durable";
            
            ConsumerConfiguration attentesConfig = ConsumerConfiguration.builder()
                    .durable(attentesConsumerName)
                    .deliverPolicy(DeliverPolicy.New)
                    .ackWait(Duration.ofSeconds(30))
                    .build();
            
            PushSubscribeOptions attentesOptions = PushSubscribeOptions.builder()
                    .stream("transit_stream")
                    .durable(attentesConsumerName)
                    .configuration(attentesConfig)
                    .build();
            
            JetStreamSubscription attentesSub = js.subscribe(
                "city.transit.attente.*", 
                dispatcher, 
                msg -> {
                    try {
                        String messageJson = new String(msg.getData(), StandardCharsets.UTF_8);
                        String subject = msg.getSubject();
                        
                        String[] parts = subject.split("\\.");
                        String ligne = parts[parts.length - 1];
                        
                        String key = ligne + "_" + System.currentTimeMillis();
                        attentesCache.put(key, messageJson);
                        msg.ack();
                        
                        nettoyerAttentes();
                        
                        System.out.println("Attente T2C recue pour ligne: " + ligne);
                    } catch (Exception e) {
                        System.err.println("Erreur traitement attente: " + e.getMessage());
                    }
                },
                false,
                attentesOptions
            );

            System.out.println("En ecoute sur les sujets T2C via JetStream");

        } catch (Exception e) {
            System.err.println("Erreur connexion NATS pour T2C: " + e.getMessage());
        }
    }

    private void nettoyerAttentes() {
        long limite = System.currentTimeMillis() - (5 * 60 * 1000);
        
        attentesCache.entrySet().removeIf(entry -> {
            String key = entry.getKey();
            long timestamp = Long.parseLong(key.split("_")[1]);
            return timestamp < limite;
        });
    }

    public Map<String, String> getAllArrets() {
        return arretsCache;
    }

    public Map<String, String> getAllAttentes() {
        return attentesCache;
    }

    public Map<String, String> getArretsByLigne(String ligne) {
        Map<String, String> result = new ConcurrentHashMap<>();
        
        for (Map.Entry<String, String> entry : arretsCache.entrySet()) {
            String arretId = entry.getKey();
            if (arretId.startsWith(ligne)) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        
        return result;
    }

    public Map<String, String> getAttentesByLigne(String ligne) {
        Map<String, String> result = new ConcurrentHashMap<>();
        
        for (Map.Entry<String, String> entry : attentesCache.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith(ligne)) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        
        return result;
    }
}