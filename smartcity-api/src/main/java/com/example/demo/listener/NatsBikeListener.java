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

import com.example.demo.model.StationData;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class NatsBikeListener {

    private final Map<String, StationData> stationsCache = new ConcurrentHashMap<>();
    private Connection nc;
    private Dispatcher dispatcher;
    private JetStream js;

    @PostConstruct
    public void initNatsListener() {
        try {
            String natsUrl = System.getenv("NATS_URL");
            if (natsUrl == null || natsUrl.isEmpty()) {
                natsUrl = "nats://127.0.0.1:4222";
                System.out.println("[INFO] Utilisation de l'URL NATS par defaut: " + natsUrl);
            } else {
                System.out.println("[INFO] Utilisation de l'URL NATS depuis l'environnement: " + natsUrl);
            }

            io.nats.client.Options options = new io.nats.client.Options.Builder()
                    .server(natsUrl)
                    .connectionName("bike-listener")
                    .reconnectWait(Duration.ofSeconds(2))
                    .maxReconnects(-1)
                    .build();

            nc = Nats.connect(options);
            dispatcher = nc.createDispatcher(msg -> {});
            js = nc.jetStream();
            
            System.out.println("[INFO] Connecte a NATS JetStream avec succes !");
            
            // Creer le consumer durable
            String consumerName = "bike-consumer-durable";
            
            ConsumerConfiguration consumerConfig = ConsumerConfiguration.builder()
                    .durable(consumerName)
                    .deliverPolicy(DeliverPolicy.New)
                    .ackWait(Duration.ofSeconds(30))
                    .build();
            
            PushSubscribeOptions pushOptions = PushSubscribeOptions.builder()
                    .stream("bikes_stream")
                    .durable(consumerName)
                    .configuration(consumerConfig)
                    .build();
            
            // S'abonner avec JetStream en utilisant le dispatcher
            JetStreamSubscription sub = js.subscribe(
                "city.bikes.station.*", 
                dispatcher, 
                msg -> {
                    try {
                        String messageJson = new String(msg.getData(), StandardCharsets.UTF_8);
                        
                        String subject = msg.getSubject();
                        String stationId = subject.substring(subject.lastIndexOf('.') + 1);
                        
                        int bikes = extraireInt(messageJson, "bikes_available");
                        int docks = extraireInt(messageJson, "docks_available");
                        String name = extraireString(messageJson, "name");
                        
                        StationData data = stationsCache.get(stationId);
                        if (data == null) {
                            data = new StationData();
                            data.setStationId(stationId);
                        }
                        
                        data.setBikesAvailable(bikes);
                        data.setDocksAvailable(docks);
                        data.setName(name);
                        
                        stationsCache.put(stationId, data);
                        
                        msg.ack();
                        
                        System.out.println("[UPDATE] Station " + stationId + " : " + name + " (" + bikes + " velos)");
                    } catch (Exception e) {
                        System.err.println("[ERREUR] Traitement message: " + e.getMessage());
                    }
                },
                false,
                pushOptions
            );
            
            System.out.println("[INFO] Abonne au stream bikes_stream avec consumer: " + consumerName);

        } catch (Exception e) {
            System.err.println("[ERREUR] Connexion NATS : " + e.getMessage());
        }
    }

    private int extraireInt(String json, String cle) {
        try {
            String recherche = "\"" + cle + "\":";
            int debut = json.indexOf(recherche) + recherche.length();
            int fin = json.indexOf(",", debut);
            if (fin == -1) {
                fin = json.indexOf("}", debut);
            }
            String valeur = json.substring(debut, fin).trim();
            return Integer.parseInt(valeur);
        } catch (Exception e) {
            return 0;
        }
    }

    private String extraireString(String json, String cle) {
        try {
            String recherche = "\"" + cle + "\":\"";
            int debut = json.indexOf(recherche) + recherche.length();
            int fin = json.indexOf("\"", debut);
            return json.substring(debut, fin);
        } catch (Exception e) {
            return "";
        }
    }

    public Map<String, StationData> getAllStations() {
        return stationsCache;
    }
}