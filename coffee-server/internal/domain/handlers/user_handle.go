package handlers

import (
	"context"
	// "encoding/json"
	// "net/http"
	"time"

	"github.com/rs/zerolog"
	"github.com/sanglam2806/mitsuru-home/internal/domain/entity"
	"github.com/sanglam2806/mitsuru-home/internal/domain/services"
)

type UserHandler struct {
	service *services.UserService
}

func NewUserHandler (service *services.UserService) *UserHandler {
	return &UserHandler{service: service}
}

// func (h *UserHandler) GetUsers(w http.ResponseWriter, r *http.Request, logger *zerolog.Logger) {
func (h *UserHandler) GetUsers(logger *zerolog.Logger) {
	ctx, cancel := context.WithTimeout(context.Background(), 15*time.Second)
	defer cancel()

	user, err := h.service.GetUsers(ctx, logger);
	if err!= nil {
		logger.Error().Msg("internal error");
	}

	logger.Info().Msg(user.Username)
	// json.NewEncoder(w).Encode(user);
}

func (h *UserHandler) InsertUser(userAccount *entity.User, ctx context.Context, logger *zerolog.Logger) error{
	return h.service.InsertUser(ctx, logger, userAccount) 
}
