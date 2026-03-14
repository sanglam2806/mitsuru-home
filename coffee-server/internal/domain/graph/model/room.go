package model

type Room struct {
	ID         string      `json:"id,omitempty" bson:"id"`
	RoomName   string      `json:"room_name,omitempty" bson:"room_name"`
	Type       room_type   `json:"type,omitempty" bson:"type"`
	Price      int         `json:"price,omitempty" bson:"price"`
	MaxGuests  int         `json:"max_guests,omitempty" bson:"max_guests"`
	Amentities string    `json:"amentities,omitempty" bson:"amentities"`
	Status     room_status `json:"status,omitempty" bson:"status"`
}

type room_type int

const SINGLE room_type = 1
const DOUBLE room_type = 2

type room_status int
const AVAILABLE room_status = 1
const INVAIABLE room_status = 2
