package graph

import "github.com/sanglam2806/mitsuru-home/internal/domain/services"

// This file will not be regenerated automatically.
//
// It serves as dependency injection for your app, add any dependencies you require
// here.

type Resolver struct{
	service *services.UserService
	room_service *services.RoomService
}

func NewResolver(sv *services.UserService) *Resolver{
	return &Resolver{
		service: sv,
	}
}

func NewRoomResolver( sv *services.RoomService) *Resolver {
	return  &Resolver{room_service: sv}
	
}
