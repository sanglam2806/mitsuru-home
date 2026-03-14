package services

import (
	"context"

	"github.com/rs/zerolog"
	"github.com/sanglam2806/mitsuru-home/internal/domain/graph/model"
	"github.com/sanglam2806/mitsuru-home/internal/domain/repositories"
)

type RoomService struct {
	repository *repositories.RoomRepository
}

func NewRoomService(repo *repositories.RoomRepository) *RoomService {
	return &RoomService{repository: repo}
}

func(service *RoomService)GetAllRooms(ctx context.Context, logger *zerolog.Logger) (*[]model.Room, error) {
	return service.repository.GetAllRooms(ctx, logger)
}

func(service *RoomService)AddNewRoom(ctx context.Context, logger *zerolog.Logger, room *model.Room) error {
	return service.repository.AddNewRoom(ctx, logger, room)
}
