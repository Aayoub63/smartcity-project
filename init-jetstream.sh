#!/bin/bash

echo "Creation des streams JetStream..."

# Creation du stream pour les velos
docker run --rm --network smartcity-project_smartcity-network natsio/nats-box nats stream add bikes_stream \
  --server=nats://smartcity-nats:4222 \
  --subjects='city.bikes.>' \
  --storage=file \
  --retention=limits \
  --max-msgs=-1 \
  --max-bytes=-1 \
  --max-age=30d \
  --replicas=1 \
  --defaults

# Creation du stream pour les transports
docker run --rm --network smartcity-project_smartcity-network natsio/nats-box nats stream add transit_stream \
  --server=nats://smartcity-nats:4222 \
  --subjects='city.transit.>' \
  --storage=file \
  --retention=limits \
  --max-msgs=-1 \
  --max-bytes=-1 \
  --max-age=30d \
  --replicas=1 \
  --defaults

# Creation du stream pour la meteo
docker run --rm --network smartcity-project_smartcity-network natsio/nats-box nats stream add weather_stream \
  --server=nats://smartcity-nats:4222 \
  --subjects='city.weather.>' \
  --storage=file \
  --retention=limits \
  --max-msgs=-1 \
  --max-bytes=-1 \
  --max-age=30d \
  --replicas=1 \
  --defaults

# Verification
docker run --rm --network smartcity-project_smartcity-network natsio/nats-box nats stream ls --server=nats://smartcity-nats:4222

echo "Initialisation terminee avec succes"