package service

import (
	"crypto/md5"
	"encoding/base64"
	"encoding/json"
	"reflect"

	"github.com/alibabacloud-go/tea/tea"
)

func GetContentMD5(body string) string {
	sum := md5.Sum([]byte(body))
	b64 := base64.StdEncoding.EncodeToString(sum[:])
	return b64
}

func GetSignature(request *tea.Request, secret string) string {
	return getSignature(secret, request)
}

func IsFail(code int) bool {
	return code < 200 || code >= 300
}

func ToQuery(filter map[string]interface{}) map[string]string {
	tmp := make(map[string]interface{})
	byt, _ := json.Marshal(filter)
	_ = json.Unmarshal(byt, &tmp)

	result := make(map[string]string)
	for key, value := range tmp {
		filterValue := reflect.ValueOf(value)
		flatRepeatedList(filterValue, result, key)
	}

	return result
}
