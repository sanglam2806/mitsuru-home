package model

// Order mirrors com.mitsuru.admin.beans.OrderMessage, which Jackson serializes in camelCase.
type Order struct {
	OrderId   int64  `json:"orderId"`
	UserEmail string `json:"userEmail"`
	Note      string `json:"note"`
}
