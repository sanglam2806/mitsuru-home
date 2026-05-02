# build stage
FROM golang:1.25.7 as builder
WORKDIR /app
COPY go.mod go.sum ./
RUN go mod download
COPY . .
RUN CGO_ENABLED=0 GOOS=linux go build -o mail-service

#Run stage
FROM alpine:latest
WORKDIR /app
COPY --from=builder /app/mail-service .
ENTRYPOINT [ "./mail-service" ]

