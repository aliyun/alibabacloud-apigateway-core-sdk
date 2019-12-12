package service

import (
	"crypto/md5"
	"encoding/base64"
	"encoding/json"
	"net/http"
	"strconv"
	"time"

	"github.com/alibabacloud-go/tea/tea"
	"gitlab.alibaba-inc.com/alicloud-sdk/apigateway-core-sdk/go/utils"
)

type BaseClient struct {
	RegionId  string `json:"regionId" xml:"regionId"`
	Protocol  string `json:"protocol" xml:"protocol"`
	Domain    string `json:"domain" xml:"domain"`
	Token     string `json:"token" xml:"token"`
	Stage     string `json:"stage" xml:"stage"`
	AppKey    string `json:"appKey" xml:"appKey"`
	AppSecret string `json:"appSecret" xml:"appSecret"`
}

func (client *BaseClient) InitClient(config map[string]interface{}) error {
	client.Protocol = getStringValue(config["protocol"])
	client.RegionId = getStringValue(config["regionId"])
	client.Domain = getStringValue(config["domain"])
	client.Token = getStringValue(config["token"])
	client.AppKey = getStringValue(config["appKey"])
	client.AppSecret = getStringValue(config["appSecret"])
	return nil
}

func (client *BaseClient) GetHost() string {
	return client.Domain
}

func (client *BaseClient) GetContentMD5(bodyStr string) string {
	sum := md5.Sum([]byte(bodyStr))
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
