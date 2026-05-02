package model

type Order struct {
	OrderId   int    `Json:"order_id"`
	UserEmail string `Json:"user_email"`
	Note      string `Json:"note"`
}
