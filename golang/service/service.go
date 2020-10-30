package service

import (
	"bytes"
	"crypto/md5"
	"encoding/base64"
	"encoding/json"
	"reflect"
	"time"

	"github.com/alibabacloud-go/tea/tea"
)

func GetContentMD5(body *string) *string {
	sum := md5.Sum([]byte(tea.StringValue(body)))
	b64 := base64.StdEncoding.EncodeToString(sum[:])
	return tea.String(b64)
}

func GetSignature(request *tea.Request, secret *string) *string {
	return tea.String(getSignature(tea.StringValue(secret), request.Query, request))
}

func GetSignatureV1(request *tea.Request, signedParams map[string]*string, secret *string) *string {
	return tea.String(getSignature(tea.StringValue(secret), signedParams, request))
}

func IsFail(code *int) *bool {
	return tea.Bool(tea.IntValue(code) < 200 || tea.IntValue(code) >= 300)
}

func ToQuery(filter map[string]interface{}) map[string]*string {
	tmp := make(map[string]interface{})
	byt, _ := json.Marshal(filter)
	d := json.NewDecoder(bytes.NewReader(byt))
	d.UseNumber()
	_ = d.Decode(&tmp)

	result := make(map[string]*string)
	for key, value := range tmp {
		filterValue := reflect.ValueOf(value)
		flatRepeatedList(filterValue, result, key)
	}

	return result
}

func GetTimestamp() *string {
	return tea.String(tea.ToString(time.Now().UnixNano() / 1000000))
}
