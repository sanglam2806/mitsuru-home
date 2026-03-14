package infa

import (
	"github.com/sanglam2806/mitsuru-home/internal/domain"
	"go.mongodb.org/mongo-driver/v2/mongo"
	"go.mongodb.org/mongo-driver/v2/mongo/options"
)

func MongoConnect(mongoConf *domain.MongoConfig) (*mongo.Client, error) {
	opts := options.ClientOptions{}
	return mongo.Connect(opts.ApplyURI(mongoConf.MongoUrl));

}


