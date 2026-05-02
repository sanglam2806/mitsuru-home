package domain

import (
	"log"
	"os"
	// "github.com/joho/godotenv"
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
	MongoDBName string
}

func LoadMongoConfig() *MongoConfig{
	_ = godotenv.Load()

	return &MongoConfig{
		MongoUrl : LoadEnvKey("MONGO_URI"),
		MongoDBName: LoadEnvKey("MONGO_DATABASE_NAME"),
	}
}

func LoadEnvKey(key string) string {
	val, exist := os.LookupEnv(key)

	if !exist {
		log.Fatalf("key %s does not exist", key)
	}

	return val
}
