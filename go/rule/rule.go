package main

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	"os"
	"strings"
)

type Expr struct {
	Left     *Expr
	Operator string
	Right    *Expr
	Value    string
}

func inspect(expr *Expr) string {
	if len(expr.Value) > 0 {
		return expr.Value
	}
	return fmt.Sprintf("(%s %s %s)", inspect(expr.Left), expr.Operator, inspect(expr.Right))
}

func getFieldValue(field string) string {
	switch field {
	case "amount":
		return "1000"
	case "description":
		return "Fuel from caltex"
	case "price":
		return "200"
	case "comments":
		return "switched from myob"
	case "name":
		return "xero accounting"
	default:
		panic(fmt.Sprintf("Field %s is not implemented", field))
	}
}

func execute(expr *Expr) bool {
	if len(expr.Value) > 0 {
		return true
	}
	switch expr.Operator {
	case "and":
		return execute(expr.Left) && execute(expr.Right)
	case "or":
		return execute(expr.Left) || execute(expr.Right)
	case "equals":
		return getFieldValue(expr.Left.Value) == expr.Right.Value
	case "contains":
		return strings.Contains(getFieldValue(expr.Left.Value), expr.Right.Value)
	default:
		panic(fmt.Sprintf("Operator %s is not implemented", expr.Operator))
	}
}

func main() {
	if jsonBlob, err := ioutil.ReadAll(os.Stdin); err != nil {
		fmt.Println("read error:", err)
	} else {
		var expr Expr
		err := json.Unmarshal(jsonBlob, &expr)
		if err != nil {
			fmt.Println("json error:", err)
		} else {
			fmt.Printf("%s = %t\n", inspect(&expr), execute(&expr))
		}
	}
}
