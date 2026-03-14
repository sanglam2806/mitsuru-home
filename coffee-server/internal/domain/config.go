package domain

import (
	"log"
	"os"

	"github.com/joho/godotenv"
)

func LoadAllConfigs() *Config{
	return &Config{
		MongoConfig : LoadMongoConfig(),
	}
}

type Config struct{
	MongoConfig *MongoConfig
}

type MongoConfig struct {
	MongoUrl string
}

func LoadMongoConfig() *MongoConfig{
	err := godotenv.Load("./.env")
	if err != nil{
		log.Fatal("Error loading .env file")
	}

	return &MongoConfig{
		MongoUrl : LoadEnvKey("MONGO_URI"),
	}
}

func LoadEnvKey(key string) string {
	val, exist := os.LookupEnv(key)

	if !exist {
		log.Fatal("key %s does not exist", key)
	}

	return val
}
