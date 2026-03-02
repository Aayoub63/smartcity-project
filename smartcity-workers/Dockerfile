FROM golang:1.24-alpine AS builder
WORKDIR /app
COPY go.mod go.sum ./
RUN go mod download
COPY *.go ./
RUN go build -o cvelo_worker cvelo_worker.go
RUN go build -o t2c_worker t2c_worker.go
RUN go build -o meteo_worker meteo_worker.go

FROM alpine:latest
RUN apk --no-cache add ca-certificates
WORKDIR /root/
COPY --from=builder /app/cvelo_worker .
COPY --from=builder /app/t2c_worker .
COPY --from=builder /app/meteo_worker .
CMD ["./cvelo_worker"]
