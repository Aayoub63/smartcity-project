package main

import (
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"os"
	"time"

	"github.com/MobilityData/gtfs-realtime-bindings/golang/gtfs"
	"github.com/nats-io/nats.go"
	"google.golang.org/protobuf/proto"
)

type Arret struct {
	ID      string `json:"arret_id"`
	Nom     string `json:"nom"`
	Ligne   string `json:"ligne"`
	Type    string `json:"type"`
	Commune string `json:"commune"`
}

type Attente struct {
	ArretID     string `json:"arret_id"`
	Ligne       string `json:"ligne"`
	Destination string `json:"destination"`
	Minutes     int    `json:"minutes"`
	Timestamp   int64  `json:"timestamp"`
}

type Position struct {
	VehiculeID string  `json:"vehicule_id"`
	Ligne      string  `json:"ligne"`
	Latitude   float64 `json:"latitude"`
	Longitude  float64 `json:"longitude"`
	Timestamp  int64   `json:"timestamp"`
}

var gtfsToInternalMap = map[string]string{
	"ALVAA": "tramA_vergnes",
	"JAUDA": "tramA_jaude",
	"PRMAA": "tramA_pardieu",
	"VGNEA": "tramA_vergnes",
	"COACA": "tramA_pardieu",
	"HACHA": "tramA_jaude",
	"CRNEA": "tramA_jaude",
	"CHGMA": "tramA_jaude",
	"SJDOA": "tramA_jaude",
	"UCACA": "tramA_jaude",
	"DEMOA": "tramA_jaude",
	"MTFDA": "tramA_jaude",
	"MURQA": "tramA_jaude",
	"PISTA": "tramA_jaude",
	"BIHDA": "tramA_jaude",
	"LYABA": "tramA_jaude",
	"GAILA": "tramA_jaude",
	"HOVIA": "tramA_jaude",
	"AUSEA": "tramA_jaude",
	"AUMAA": "tramA_vergnes",
	"CHAPS": "tramA_jaude",
	"CHESA": "tramA_jaude",
	"DEPAA": "tramA_jaude",
	"EDROS": "tramA_jaude",
	"GASNA": "tramA_jaude",
	"GEHAA": "tramA_jaude",
	"PKSTV": "tramA_jaude",
	"ROPAA": "tramA_jaude",
	"THERR": "tramA_jaude",
	"GARNR": "tramA_jaude",
	"EUROR": "tramA_jaude",
	"CHMAR": "tramA_jaude",
	"DUCLR": "tramA_jaude",
	"RESJR": "tramA_jaude",
	"LYGER": "tramA_jaude",
	"JUVER": "tramA_jaude",
	"CUGNR": "tramA_jaude",
	"NEWTR": "tramA_jaude",
	"FRLUR": "tramA_jaude",
	"ELRES": "tramA_jaude",
	"JEMES": "tramA_jaude",
	"GAGAR": "tramA_jaude",
	"AEROR": "tramA_jaude",
	"AUMAR": "tramA_vergnes",
	"AUSER": "tramA_jaude",
	"BALLS": "tramA_jaude",
	"FACUR": "tramA_jaude",
	"CARNR": "tramA_jaude",
	"PKDUC": "tramC_tamaris",
	"DUCIR": "tramC_tamaris",
	"LAYAR": "tramC_tamaris",
	"MTCHR": "tramC_tamaris",
	"CHPTR": "tramC_tamaris",
	"QUROS": "tramC_tamaris",
	"GONCR": "tramC_tamaris",
	"GALAR": "tramC_tamaris",
	"BERTR": "tramC_tamaris",
	"FRROR": "tramC_tamaris",
	"PRDLR": "tramC_toulaits",
	"FERAR": "tramC_toulaits",
	"LCHAR": "tramC_toulaits",
	"CAMER": "tramC_toulaits",
	"SARRR": "tramC_toulaits",
	"PANER": "tramC_toulaits",
	"ORGAR": "tramC_toulaits",
	"LYLAS": "tramC_toulaits",
	"LEDVR": "tramC_toulaits",
	"CLSAR": "tramC_toulaits",
	"POSAR": "tramC_toulaits",
	"SARLR": "tramC_toulaits",
	"GHZER": "tramC_toulaits",
	"GADCR": "tramC_toulaits",
	"GETIR": "tramC_toulaits",
	"ALOTR": "tramC_toulaits",
	"ROBOR": "tramC_toulaits",
	"LLACR": "tramC_toulaits",
	"SAMPR": "tramC_toulaits",
	"PLRER": "tramC_toulaits",
	"GESAR": "tramC_toulaits",
	"LERIR": "tramC_toulaits",
	"ALMIR": "tramC_toulaits",
	"LYRDR": "tramC_toulaits",
	"COALR": "tramC_toulaits",
}

var arretsSimules = []Arret{
	{ID: "tramA_vergnes", Nom: "Les Vergnes", Ligne: "A", Type: "tram", Commune: "Clermont"},
	{ID: "tramA_jaude", Nom: "Jaude", Ligne: "A", Type: "tram", Commune: "Clermont"},
	{ID: "tramA_pardieu", Nom: "La Pardieu", Ligne: "A", Type: "tram", Commune: "Clermont"},
	{ID: "tramB_royat", Nom: "Royat Place Allard", Ligne: "B", Type: "tram", Commune: "Royat"},
	{ID: "tramB_stade", Nom: "Stade Marcel Michelin", Ligne: "B", Type: "tram", Commune: "Clermont"},
	{ID: "tramC_tamaris", Nom: "Tamaris", Ligne: "C", Type: "tram", Commune: "Clermont"},
	{ID: "tramC_toulaits", Nom: "Cournon Toulaits", Ligne: "C", Type: "tram", Commune: "Cournon"},
	{ID: "bus3_stade", Nom: "Stade Gabriel Montpied", Ligne: "3", Type: "bus", Commune: "Clermont"},
	{ID: "bus3_vignes", Nom: "Les Vignes", Ligne: "3", Type: "bus", Commune: "Aubiere"},
	{ID: "bus3_romagnat", Nom: "Romagnat Gergovia", Ligne: "3", Type: "bus", Commune: "Romagnat"},
	{ID: "bus4_charcot", Nom: "Tremonteix Charcot", Ligne: "4", Type: "bus", Commune: "Clermont"},
	{ID: "bus4_ceyrat", Nom: "Ceyrat Pradeaux", Ligne: "4", Type: "bus", Commune: "Ceyrat"},
	{ID: "bus5_royat", Nom: "Royat Pepiniere", Ligne: "5", Type: "bus", Commune: "Royat"},
	{ID: "bus5_gaillard", Nom: "Gaillard", Ligne: "5", Type: "bus", Commune: "Durtol"},
	{ID: "bus7_cotes", Nom: "Les Cotes", Ligne: "7", Type: "bus", Commune: "Clermont"},
	{ID: "bus7_tillon", Nom: "Tillon", Ligne: "7", Type: "bus", Commune: "Clermont"},
	{ID: "bus8_matussat", Nom: "Matussat Chataigneraie", Ligne: "8", Type: "bus", Commune: "Clermont"},
	{ID: "bus8_beaumont", Nom: "Beaumont Place d'Armes", Ligne: "8", Type: "bus", Commune: "Beaumont"},
	{ID: "bus8_vallieres", Nom: "Vallieres", Ligne: "8", Type: "bus", Commune: "Clermont"},
}

func main() {
	natsURL := os.Getenv("NATS_URL")
	if natsURL == "" {
		natsURL = nats.DefaultURL
	}

	nc, err := nats.Connect(natsURL)
	if err != nil {
		fmt.Println("Erreur de connexion a NATS:", err)
		return
	}
	defer nc.Close()

	// Initialisation JetStream
	js, err := nc.JetStream()
	if err != nil {
		fmt.Println("Erreur JetStream:", err)
		return
	}

	fmt.Println("Service T2C avec JetStream demarre")

	// Publication des arrets (donnees statiques)
	for _, arret := range arretsSimules {
		arretJSON, err := json.Marshal(arret)
		if err != nil {
			fmt.Println("Erreur encodage arret:", err)
			continue
		}
		sujet := "city.transit.arret." + arret.Ligne + "." + arret.ID

		// Publication JetStream pour les arrets
		_, err = js.Publish(sujet, arretJSON)
		if err != nil {
			nc.Publish(sujet, arretJSON)
			fmt.Printf("Publie arret %s (simple)\n", arret.ID)
		} else {
			fmt.Printf("Publie arret %s (persiste)\n", arret.ID)
		}
	}
	fmt.Printf("Publie %d arrets\n", len(arretsSimules))

	for {
		err := chargerDonneesTempsReel(nc, js)
		if err != nil {
			fmt.Println("Erreur GTFS-RT:", err)
		} else {
			fmt.Println("Donnees temps reel mises a jour")
		}
		time.Sleep(30 * time.Second)
	}
}

func chargerDonneesTempsReel(nc *nats.Conn, js nats.JetStreamContext) error {
	url := "https://opendata.clermontmetropole.eu/api/explore/v2.1/catalog/datasets/gtfs-smtc/files/e5e30f61a74af2b5c3b504b8e9c908f0"

	resp, err := http.Get(url)
	if err != nil {
		return fmt.Errorf("erreur requete: %v", err)
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return fmt.Errorf("erreur lecture: %v", err)
	}

	feed := &gtfs.FeedMessage{}
	err = proto.Unmarshal(body, feed)
	if err != nil {
		return fmt.Errorf("erreur parsing protobuf: %v", err)
	}

	fmt.Printf("Flux GTFS-RT recu: %d entites\n", len(feed.Entity))

	for _, entity := range feed.Entity {
		if entity.TripUpdate != nil {
			traiterTripUpdate(entity.TripUpdate, nc, js)
		}
		if entity.Vehicle != nil {
			traiterVehiclePosition(entity.Vehicle, nc, js)
		}
	}
	return nil
}

func traiterTripUpdate(tu *gtfs.TripUpdate, nc *nats.Conn, js nats.JetStreamContext) {
	routeId := tu.GetTrip().GetRouteId()
	for _, stu := range tu.StopTimeUpdate {
		if stu.Arrival != nil {
			gtfsStopId := stu.GetStopId()

			internalStopId := gtfsToInternalMap[gtfsStopId]
			if internalStopId == "" {
				continue
			}

			now := time.Now().Unix()
			minutes := int((stu.Arrival.GetTime() - now) / 60)
			if minutes > 0 && minutes < 120 {
				attente := Attente{
					ArretID:     internalStopId,
					Ligne:       routeId,
					Destination: tu.GetTrip().GetTripId(),
					Minutes:     minutes,
					Timestamp:   now,
				}
				attenteJSON, _ := json.Marshal(attente)
				sujet := "city.transit.attente." + routeId

				// Publication JetStream pour les attentes
				_, err := js.Publish(sujet, attenteJSON)
				if err != nil {
					nc.Publish(sujet, attenteJSON)
					fmt.Printf("Attente: arret %s -> %s, ligne %s, %d min (simple)\n",
						gtfsStopId, internalStopId, routeId, minutes)
				} else {
					fmt.Printf("Attente: arret %s -> %s, ligne %s, %d min (persiste)\n",
						gtfsStopId, internalStopId, routeId, minutes)
				}
			}
		}
	}
}

func traiterVehiclePosition(vp *gtfs.VehiclePosition, nc *nats.Conn, js nats.JetStreamContext) {
	pos := Position{
		VehiculeID: vp.GetVehicle().GetId(),
		Ligne:      vp.GetTrip().GetRouteId(),
		Latitude:   float64(vp.GetPosition().GetLatitude()),
		Longitude:  float64(vp.GetPosition().GetLongitude()),
		Timestamp:  time.Now().Unix(),
	}
	posJSON, _ := json.Marshal(pos)
	sujet := "city.transit.position." + pos.Ligne

	// Publication JetStream pour les positions
	_, err := js.Publish(sujet, posJSON)
	if err != nil {
		nc.Publish(sujet, posJSON)
		fmt.Printf("Position: vehicule %s, ligne %s (simple)\n",
			pos.VehiculeID, pos.Ligne)
	} else {
		fmt.Printf("Position: vehicule %s, ligne %s (persiste)\n",
			pos.VehiculeID, pos.Ligne)
	}
}
