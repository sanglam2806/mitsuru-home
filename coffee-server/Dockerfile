FROM golang:1.25.7 as builder

WORKDIR /app

COPY go.mod go.sum ./
RUN go mod tidy

 COPY . .
# Build
RUN CGO_ENABLED=0 GOOS=linux go build -o /mitsuru-home ./cmd/main.go

# stage 2 alpine
FROM alpine:latest
WORKDIR /root/
COPY --from=builder /mitsuru-home .

# To bind to a TCP port, runtime parameters must be supplied to the docker command.
# But we can (optionally) document in the Dockerfile what ports
# the application is going to listen on by default.
# https://docs.docker.com/engine/reference/builder/#expose
EXPOSE 8080

# Run
CMD [ "/mitsuru-home" ]
