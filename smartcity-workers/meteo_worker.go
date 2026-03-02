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

type MeteoData struct {
	Ville       string  `json:"ville"`
	Temperature float64 `json:"temperature"`
	Humidite    int     `json:"humidite"`
	Description string  `json:"description"`
	Timestamp   int64   `json:"timestamp"`
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

	fmt.Println("Service Meteo demarre")

	villes := []string{
		"Clermont-Ferrand",
		"Aubiere",
		"Chamalieres",
		"Riom",
		"Aulnat",
	}

	for {
		for _, ville := range villes {
			meteo, err := getMeteo(ville)
			if err != nil {
				fmt.Printf("Erreur pour %s: %v\n", ville, err)
				continue
			}

			meteoJSON, err := json.Marshal(meteo)
			if err != nil {
				fmt.Println("Erreur encodage JSON:", err)
				continue
			}

			sujet := "city.weather." + normalizeVille(ville)

			// Publication JetStream
			_, err = js.Publish(sujet, meteoJSON)
			if err != nil {
				// Fallback: publication normale si JetStream echoue
				nc.Publish(sujet, meteoJSON)
				fmt.Printf("Meteo %s: %.1f°C, %s (simple)\n",
					ville, meteo.Temperature, meteo.Description)
			} else {
				fmt.Printf("Meteo %s: %.1f°C, %s (persiste)\n",
					ville, meteo.Temperature, meteo.Description)
			}
		}

		fmt.Println("Meteo mise a jour")
		time.Sleep(10 * time.Minute)
	}
}

func getMeteo(ville string) (*MeteoData, error) {
	apiKey := os.Getenv("OPENWEATHER_API_KEY")
	if apiKey == "" {
		return nil, fmt.Errorf("OPENWEATHER_API_KEY non definie")
	}

	url := "https://api.openweathermap.org/data/2.5/weather?q=" +
		ville + ",FR&appid=" + apiKey + "&units=metric&lang=fr"

	resp, err := http.Get(url)
	if err != nil {
		return nil, err
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, err
	}

	var result struct {
		Main struct {
			Temp     float64 `json:"temp"`
			Humidity int     `json:"humidity"`
		} `json:"main"`
		Weather []struct {
			Description string `json:"description"`
		} `json:"weather"`
		Cod int `json:"cod"`
	}

	err = json.Unmarshal(body, &result)
	if err != nil {
		return nil, err
	}

	if result.Cod != 200 && result.Cod != 0 {
		return nil, fmt.Errorf("API a retourne code %d", result.Cod)
	}

	description := "Donnee non disponible"
	if len(result.Weather) > 0 {
		description = result.Weather[0].Description
	}

	meteo := &MeteoData{
		Ville:       ville,
		Temperature: result.Main.Temp,
		Humidite:    result.Main.Humidity,
		Description: description,
		Timestamp:   time.Now().Unix(),
	}

	return meteo, nil
}

func normalizeVille(ville string) string {
	switch ville {
	case "Clermont-Ferrand":
		return "clermont"
	case "Aubiere":
		return "aubiere"
	case "Chamalieres":
		return "chamalieres"
	case "Riom":
		return "riom"
	case "Aulnat":
		return "aulnat"
	default:
		return ville
	}
}
