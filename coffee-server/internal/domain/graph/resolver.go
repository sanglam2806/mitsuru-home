package graph

import "github.com/sanglam2806/mitsuru-home/internal/domain/services"

// This file will not be regenerated automatically.
//
// It serves as dependency injection for your app, add any dependencies you require
// here.

type Resolver struct{
	service *services.UserService
}
