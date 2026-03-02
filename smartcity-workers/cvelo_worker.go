package main

import (
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"os"
	"time"

	"github.com/nats-io/nats.go"
)

type StationEvent struct {
	StationID string `json:"station_id"`
	Bikes     int    `json:"bikes_available"`
	Docks     int    `json:"docks_available"`
	Name      string `json:"name"`
}

type StationInfo struct {
	StationID string `json:"station_id"`
	Name      string `json:"name"`
}

var stationNames = make(map[string]string)

func loadStationNames() error {
	url := "https://clermontferrand.publicbikesystem.net/customer/gbfs/v2/en/station_information.json"

	resp, err := http.Get(url)
	if err != nil {
		return fmt.Errorf("erreur: %v", err)
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return fmt.Errorf("erreur lecture: %v", err)
	}

	var data struct {
		Data struct {
			Stations []StationInfo `json:"stations"`
		} `json:"data"`
	}

	if err := json.Unmarshal(body, &data); err != nil {
		return fmt.Errorf("erreur parsing: %v", err)
	}

	for _, s := range data.Data.Stations {
		stationNames[s.StationID] = s.Name
	}

	fmt.Printf("Charge %d noms de stations\n", len(stationNames))
	return nil
}

func main() {
	natsURL := os.Getenv("NATS_URL")
	if natsURL == "" {
		natsURL = nats.DefaultURL
	}

	nc, err := nats.Connect(natsURL)
	if err != nil {
		fmt.Println("Erreur NATS:", err)
		return
	}
	defer nc.Close()

	// Initialisation JetStream
	js, err := nc.JetStream()
	if err != nil {
		fmt.Println("Erreur JetStream:", err)
		return
	}

	if err := loadStationNames(); err != nil {
		fmt.Println("Erreur chargement noms:", err)
	}

	fmt.Println("Worker C.velo demarre")

	for {
		url := "https://clermontferrand.publicbikesystem.net/customer/gbfs/v2/en/station_status"

		resp, err := http.Get(url)
		if err != nil {
			fmt.Println("Erreur HTTP:", err)
			time.Sleep(15 * time.Second)
			continue
		}

		body, err := io.ReadAll(resp.Body)
		resp.Body.Close()
		if err != nil {
			fmt.Println("Erreur lecture:", err)
			continue
		}

		var data struct {
			Data struct {
				Stations []struct {
					StationID         string `json:"station_id"`
					NumBikesAvailable int    `json:"num_bikes_available"`
					NumDocksAvailable int    `json:"num_docks_available"`
					IsRenting         bool   `json:"is_renting"`
				} `json:"stations"`
			} `json:"data"`
		}

		if err := json.Unmarshal(body, &data); err != nil {
			fmt.Println("Erreur parsing:", err)
			time.Sleep(15 * time.Second)
			continue
		}

		count := 0
		for _, s := range data.Data.Stations {
			if s.IsRenting {
				event := StationEvent{
					StationID: s.StationID,
					Bikes:     s.NumBikesAvailable,
					Docks:     s.NumDocksAvailable,
					Name:      stationNames[s.StationID],
				}

				eventJSON, _ := json.Marshal(event)
				subject := "city.bikes.station." + s.StationID

				// Publication JetStream
				_, err = js.Publish(subject, eventJSON)
				if err != nil {
					// Fallback: publication normale si JetStream echoue
					nc.Publish(subject, eventJSON)
					fmt.Printf("Publie station %s (simple)\n", s.StationID)
				} else {
					fmt.Printf("Publie station %s (persiste)\n", s.StationID)
				}
				count++
			}
		}

		fmt.Printf("Publie %d stations\n", count)
		time.Sleep(15 * time.Second)
	}
}
