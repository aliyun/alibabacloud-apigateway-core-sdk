package service

import (
	"crypto/md5"
	"encoding/base64"
	"encoding/json"
	"fmt"
	"net/http"
	"net/url"
	"reflect"
	"strconv"
	"time"

	"github.com/alibabacloud-go/tea/tea"
	"github.com/aliyun/alibabacloud-apigateway-core-sdk/go/utils"
)

type BaseClient struct {
	RegionId       string `json:"regionId" xml:"regionId"`
	Protocol       string `json:"protocol" xml:"protocol"`
	Domain         string `json:"domain" xml:"domain"`
	Token          string `json:"token" xml:"token"`
	Stage          string `json:"stage" xml:"stage"`
	AppKey         string `json:"appKey" xml:"appKey"`
	AppSecret      string `json:"appSecret" xml:"appSecret"`
	ReadTimeout    int    `json:"readTimeout" xml:"readTimeout"`
	ConnectTimeout int    `json:"connectTimeout" xml:"connectTimeout"`
	LocalAddr      string `json:"localAddr" xml:"localAddr"`
	HttpProxy      string `json:"httpProxy" xml:"httpProxy"`
	HttpsProxy     string `json:"httpsProxy" xml:"httpsProxy"`
	NoProxy        string `json:"noProxy" xml:"noProxy"`
	MaxIdleConns   int    `json:"maxIdleConns" xml:"maxIdleConns"`
}

func (client *BaseClient) InitClient(config map[string]interface{}) error {
	client.Protocol = getStringValue(config["protocol"])
	client.RegionId = getStringValue(config["regionId"])
	client.Domain = getStringValue(config["domain"])
	client.Token = getStringValue(config["token"])
	client.AppKey = getStringValue(config["appKey"])
	client.AppSecret = getStringValue(config["appSecret"])
	client.ReadTimeout = getIntValue(config["readTimeout"])
	client.ConnectTimeout = getIntValue(config["connectTimeout"])
	client.LocalAddr = getStringValue(config["localAddr"])
	client.HttpProxy = getStringValue(config["httpProxy"])
	client.HttpsProxy = getStringValue(config["httpsProxy"])
	client.NoProxy = getStringValue(config["noProxy"])
	client.MaxIdleConns = getIntValue(config["maxIdleConns"])
	client.Stage = getStringValue(config["stage"])
	return nil
}

func (client *BaseClient) GetHost() string {
	return client.Domain
}

func (client *BaseClient) GetContentMD5(body map[string]interface{}) string {
	byt, _ := json.Marshal(body)
	sum := md5.Sum(byt)
	b64 := base64.StdEncoding.EncodeToString(sum[:])
	return b64
}

func (client *BaseClient) GetSignature(request *tea.Request) string {
	return getSignature(client.AppSecret, request)
}

func (client *BaseClient) GetDate() string {
	return time.Now().UTC().Format(http.TimeFormat)
}

func (client *BaseClient) GetTimestamp() string {
	return strconv.FormatInt(time.Now().Unix()*1000, 10)
}

func (client *BaseClient) GetUUID() string {
	return utils.GetUUID()
}

func (client *BaseClient) ToJSONString(body map[string]interface{}) string {
	byt, _ := json.Marshal(body)

	return string(byt)
}

func (client *BaseClient) IsStatusCode(response *tea.Response, code int) bool {
	return response.StatusCode == code
}

func (client *BaseClient) ReadAsJSON(response *tea.Response) (map[string]interface{}, error) {
	tmp := make(map[string]interface{})
	body, err := response.ReadBody()
	if err != nil {
		return tmp, err
	}

	err = json.Unmarshal(body, &tmp)
	if err != nil {
		return tmp, err
	}
	return tmp, nil
}

func (client *BaseClient) IsFail(response *tea.Response) bool {
	return response.StatusCode < 200 || response.StatusCode >= 300
}

func (client *BaseClient) DefaultNumber(realNum, defaultNum int) int {
	if realNum == 0 {
		return defaultNum
	}

	return realNum
}

func (client *BaseClient) Default(realStr, defaultStr string) string {
	if realStr == "" {
		return defaultStr
	}

	return realStr
}

func (client *BaseClient) Equal(realStr, defaultStr string) bool {
	return realStr == defaultStr
}

func (client *BaseClient) NotNull(a map[string]interface{}) bool {
	if a == nil {
		return false
	}
	return len(a) > 0
}

func (client *BaseClient) ToQuery(filter map[string]interface{}) map[string]string {
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

func (client *BaseClient) ToForm(a map[string]interface{}) string {
	if a == nil {
		return ""
	}
	res := ""
	first := true
	for k, v := range a {
		if first {
			first = false
		} else {
			res += "&"
		}
		res += url.QueryEscape(k)
		res += "="
		res += url.QueryEscape(fmt.Sprintf("%v", v))
	}
	return res
}
