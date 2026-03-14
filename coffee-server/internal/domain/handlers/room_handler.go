package handlers

import (
	"context"
	"time"

	"github.com/rs/zerolog"
	"github.com/sanglam2806/mitsuru-home/internal/domain/graph/model"
	"github.com/sanglam2806/mitsuru-home/internal/domain/services"
)

type RoomHandler struct {
	service *services.RoomService
}

func NewRoomHandler (service *services.RoomService) *RoomHandler {
	return &RoomHandler{service: service}
}

func(h *RoomHandler) GetAllRooms(logger *zerolog.Logger) {
	ctx, cancel := context.WithTimeout(context.Background(), 15*time.Second)
	defer cancel()

	rooms, err := h.service.GetAllRooms(ctx, logger);
	if err!= nil {
		logger.Error().Msg("internal error");
	}

	for _,room := range *rooms {
		logger.Info().Msgf("Room : %s\n", room.RoomName)
	}
}

func(h *RoomHandler) InsertNewRoom(ctx context.Context, logger *zerolog.Logger, room *model.Room) error{
	return h.service.AddNewRoom(ctx, logger, room)
}
