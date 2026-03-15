package services

import (
	"context"

	"github.com/rs/zerolog"
	"github.com/sanglam2806/mitsuru-home/internal/domain/graph/model"
	"github.com/sanglam2806/mitsuru-home/internal/domain/repositories"
)

type UserService struct{
	repo *repositories.UserRepository
}

func NewUserService (user_repo *repositories.UserRepository) *UserService {
	return &UserService{repo: user_repo}
}

func (s *UserService) GetUsers (ctx context.Context, logger *zerolog.Logger) (*[]model.User, error) {
	return s.repo.GetAllAccounts(ctx, logger); 
}

func (s *UserService) InsertUser (ctx context.Context, logger *zerolog.Logger, userAccount *model.User) error {
	return s.repo.AddUserAccount(userAccount, logger, ctx);
}
