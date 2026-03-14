package repositories

import (
	"context"

	"github.com/rs/zerolog"
	"github.com/sanglam2806/mitsuru-home/internal/domain/graph/model"
	"go.mongodb.org/mongo-driver/v2/bson"
	"go.mongodb.org/mongo-driver/v2/mongo"
)

type RoomRepository struct {
	database *mongo.Database
	collection *mongo.Collection
}

func NewRoomRepository (db *mongo.Database) *RoomRepository {
	return &RoomRepository{
		database: db,
		collection: db.Collection("room"),
	}
}

func (repo *RoomRepository) GetAllRooms(ctx context.Context, logger *zerolog.Logger) (*[]model.Room, error) {

	filter :=	bson.D{}
	cursor, err := repo.collection.Find(ctx,filter)
	if err != nil {
		logger.Err(err).Msg("Cannot connect to DB")
	}
	
	var rs []model.Room
	err = cursor.All(ctx, &rs)
	if err != nil {
		logger.Err(err).Msg("Cannot read data from Table")
	}

	return &rs, nil
} 

func(repo *RoomRepository) AddNewRoom(ctx context.Context, logger *zerolog.Logger, room *model.Room) error {
	rs, err := repo.collection.InsertOne(ctx, room)
	if err != nil {
		logger.Err(err).Msg("Cannot insert room to DB")
	}

	logger.Info().Msgf("Document inserted with ID: %s\n", rs.InsertedID)

	return nil
}
