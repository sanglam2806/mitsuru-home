package logger

import (
	"fmt"
	"os"
	"time"

	"github.com/rs/zerolog"
)

func NewLogger(level zerolog.Level) *zerolog.Logger {
	consoleWriter := zerolog.ConsoleWriter{
		Out:                   os.Stdout,
		NoColor:               false,
		TimeFormat:            time.RFC822Z, 
		FormatLevel:           func(i interface {}) string {
			return	fmt.Sprintf("[%s]", i) 
		},
		FormatCaller:          func(i interface{}) string {
			return fmt.Sprintf("[%s]", i)
		},
	}

	logger := zerolog.New(consoleWriter).Level(level).With().Caller().Timestamp().Logger()
	return &logger;
}
