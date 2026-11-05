package consumer

import (
	"testing"

	amqp "github.com/rabbitmq/amqp091-go"
)

func TestRetryCount(t *testing.T) {
	tests := []struct {
		name    string
		headers amqp.Table
		want    int
	}{
		{"no headers", nil, 0},
		{"other headers only", amqp.Table{"x-death": "something"}, 0},
		{"int32", amqp.Table{retryCountHeader: int32(2)}, 2},
		{"int64", amqp.Table{retryCountHeader: int64(3)}, 3},
		{"unexpected type", amqp.Table{retryCountHeader: "2"}, 0},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			got := retryCount(amqp.Delivery{Headers: tt.headers})
			if got != tt.want {
				t.Errorf("retryCount() = %d, want %d", got, tt.want)
			}
		})
	}
}
