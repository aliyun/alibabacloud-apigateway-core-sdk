package service

import (
	"reflect"
	"testing"

	"gitlab.alibaba-inc.com/alicloud-sdk/apigateway-core-sdk/go/utils"
)

type PrettifyTest struct {
	name     string
	Strs     []string
	Nums8    []int8
	Unum8    []uint8
	Value    string
	Mapvalue map[string]string
}

func Test_Prettify(t *testing.T) {
	prettifyTest := &PrettifyTest{
		name:     "prettify",
		Nums8:    []int8{0, 1, 2, 4},
		Unum8:    []uint8{0},
		Value:    "ok",
		Mapvalue: map[string]string{"key": "ccp", "value": "ok"},
	}
	str := Prettify(prettifyTest)
	utils.AssertContains(t, str, "Nums8")

	str = Prettify(nil)
	utils.AssertEqual(t, str, "null")
}

func Test_flatRepeatedList(t *testing.T) {
	input := map[string]interface{}{
		"Nums": []int{1, 2},
		"Maps": []map[string]string{
			map[string]string{
				"key": "value",
			},
		},
		"Nil": nil,
	}
	result := map[string]string{}
	flatRepeatedList(reflect.ValueOf(input), result, "")
	utils.AssertEqual(t, "value", result["Maps.1.key"])
	utils.AssertEqual(t, "1", result["Nums.1"])
	utils.AssertEqual(t, "2", result["Nums.2"])
}

func Test_getFunc(t *testing.T) {
	num := 10
	intval := getIntValue(num)
	utils.AssertEqual(t, 10, intval)

	intval = getIntValue(nil)
	utils.AssertEqual(t, 0, intval)

	boolval := true
	boolval = getBoolValue(boolval)
	utils.AssertEqual(t, true, boolval)

	boolval = getBoolValue(nil)
	utils.AssertEqual(t, false, boolval)

	strval := getStringValue("apigateway")
	utils.AssertEqual(t, "apigateway", strval)
}

func Test_Sort(t *testing.T) {
	config := map[string]string{
		"a": "apigateway",
		"b": "sdk",
	}

	hs := newSorter(config)
	hs.Sort()
	utils.AssertEqual(t, true, hs.Less(0, 1))
	hs.Swap(0, 1)
	utils.AssertEqual(t, false, hs.Less(0, 1))
}
