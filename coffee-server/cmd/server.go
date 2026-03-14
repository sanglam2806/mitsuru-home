package main

import (
	"log"
	"net/http"
	"os"

	"github.com/99designs/gqlgen/graphql/handler"
	"github.com/99designs/gqlgen/graphql/handler/extension"
	"github.com/99designs/gqlgen/graphql/handler/lru"
	"github.com/99designs/gqlgen/graphql/handler/transport"
	"github.com/99designs/gqlgen/graphql/playground"
	"github.com/sanglam2806/mitsuru-home/internal/domain"
	"github.com/sanglam2806/mitsuru-home/internal/domain/graph"
	"github.com/sanglam2806/mitsuru-home/internal/domain/repositories"
	"github.com/sanglam2806/mitsuru-home/internal/domain/services"
	"github.com/sanglam2806/mitsuru-home/internal/infa"
	"github.com/vektah/gqlparser/v2/ast"
)

const defaultPort = "8080"

func main() {
	port := os.Getenv("PORT")
	if port == "" {
		port = defaultPort
	}

	domain.LoadMongoConfig(); 
	mongoConf := domain.LoadMongoConfig()

	client, err := infa.MongoConnect(mongoConf)
	if err != nil {
		// log.Err(err).Msg("Cannot connect to MongoDB")
	}
	repo := repositories.NewUserRepository(client);
	sv := services.NewUserService(repo);

	resolver := graph.NewResolver(sv)

	srv := handler.New(graph.NewExecutableSchema(graph.Config{Resolvers: resolver,}))
	srv.AddTransport(transport.Options{})
	srv.AddTransport(transport.GET{})
	srv.AddTransport(transport.POST{})

	srv.SetQueryCache(lru.New[*ast.QueryDocument](1000))

	srv.Use(extension.Introspection{})
	srv.Use(extension.AutomaticPersistedQuery{
		Cache: lru.New[string](100),
	})

	http.Handle("/", playground.Handler("GraphQL playground", "/query"))
	http.Handle("/query", srv)

	log.Printf("connect to http://localhost:%s/ for GraphQL playground", port)
	log.Fatal(http.ListenAndServe(":"+port, nil))
}
