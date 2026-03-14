package model

import (
	"context"
	"time"

	"github.com/rs/zerolog"
)


type User struct {
	ID       string `json:"id"`
	Userid    string    `json:"userid,omitempty" bson:"userid"`
	Username  string    `json:"username,omitempty" bson:"username"`
	Email     string    `json:"email,omitempty" bson:"email"`
	Phone     string    `json:"phone,omitempty" bson:"phone"`
	Role      User_role `json:"role,omitempty" bson:"role"`
	Create_at time.Time `json:"create_at,omitempty" bson:"create_at"`
}

type User_role int

const HOST User_role = 1
const STAFF User_role = 2
const GUEST User_role = 3

type UserQueries interface {
	GetAllAccounts(context.Context, *zerolog.Logger) (*User, error)
}
