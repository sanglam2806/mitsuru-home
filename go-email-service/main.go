package main

import (
	"fmt"

	"github.com/sanglam2806/go-email-service/consumer"
)

func main () {
	fmt.Println("Hello Na-chan from with love")
	consumer.StartConsuming();
}
