package repositories

import (
	"context"
	"fmt"

	"github.com/rs/zerolog"
	"github.com/sanglam2806/mitsuru-home/internal/domain/graph/model"
	"go.mongodb.org/mongo-driver/v2/bson"
	"go.mongodb.org/mongo-driver/v2/mongo"
)

type UserRepository struct {
	client *mongo.Client
}

func NewUserRepository (client *mongo.Client) *UserRepository {
	return &UserRepository{
		client: client,
	}
}

func (user_repo *UserRepository) GetAllAccounts(ctx context.Context, logger *zerolog.Logger) (*model.User, error) {

	col := user_repo.client.Database("mitsuru_base").Collection("user_account")
	var rs []model.User
	filter := bson.D{}
	cursor, err := col.Find(ctx,filter)
	if err != nil {
		logger.Err(err).Msg("Cannot get data from DB")
		return &model.User{}, nil
	}
	err = cursor.All(ctx, &rs)

	if err!= nil {
		logger.Err(err).Msg("Cannot convert data to result")	
		return &model.User{}, nil
	}
	
	// return &entity.User{
	// 	Userid:    "",
	// 	Username:  "Mitsuru",
	// 	Email:     "mitsuru_home@gmail.com",
	// 	Phone:     "0xx0xx",
	// 	Role:      0,
	// 	Create_at: time.Now(),
	// }, nil

	return &rs[0],nil
}

func (user_repo *UserRepository) AddUserAccount(userAccount *model.User, logger *zerolog.Logger, 
	ctx context.Context) error {

	col := user_repo.client.Database("mitsuru_base").Collection("user_account");
	rs, err := col.InsertOne(ctx, userAccount)

	if err != nil {
		return err
	}
	logger.Info().Msg(fmt.Sprintf("Document inserted with ID: %s\n", rs.InsertedID))

	return nil
}
	
