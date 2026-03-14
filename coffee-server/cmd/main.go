package main

import (
	"context"
	"time"

	"github.com/rs/zerolog"
	"github.com/sanglam2806/mitsuru-home/internal/domain"
	"github.com/sanglam2806/mitsuru-home/internal/domain/graph/model"
	"github.com/sanglam2806/mitsuru-home/internal/domain/handlers"
	"github.com/sanglam2806/mitsuru-home/internal/domain/repositories"
	"github.com/sanglam2806/mitsuru-home/internal/domain/services"
	"github.com/sanglam2806/mitsuru-home/internal/infa"
	"github.com/sanglam2806/mitsuru-home/internal/logger"
)

func main() {
	ctx := context.Background();
	log := logger.NewLogger(zerolog.InfoLevel);
	log.Info().Msg("Hello Na-chan");

	domain.LoadMongoConfig(); 
	mongoConf := domain.LoadMongoConfig()

	client, err := infa.MongoConnect(mongoConf)
	if err != nil {
		log.Err(err).Msg("Cannot connect to MongoDB")
	}

	repo := repositories.NewUserRepository(client);
	service := services.NewUserService(repo);
	handler := handlers.NewUserHandler(service);
	userAccount := model.User{
		Userid:   "001",
		Username: "Moena",
		Email:    "na-chan@gmail.com",
		Phone:    "080xxxxx",
		Role:     model.HOST,
		Create_at: time.Now(),
	}

	err = handler.InsertUser(&userAccount, ctx, log);
	if err != nil {
		log.Err(err).Msg("Insert new user is failed")
	} else {
		log.Info().Msg("User was inserted")
	}

	handler.GetUsers(log)
	

	//
	// http.HandleFunc("/users", func(w http.ResponseWriter, r *http.Request) {
	// 	switch r.Method {
	// 	case http.MethodGet:
	// 		handler.GetUsers(w, r, log);
	// 	}
	// })
	//
// log.Fatal().Msg(http.ListenAndServe(":8080", nil).Error())

	// main()
}
