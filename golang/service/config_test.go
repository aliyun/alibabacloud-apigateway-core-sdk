package service

import (
	"reflect"
	"testing"

	"github.com/aliyun/alibabacloud-apigateway-core-sdk/golang/utils"
)

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
