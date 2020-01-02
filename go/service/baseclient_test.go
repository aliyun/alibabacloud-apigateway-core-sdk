package service

import (
	"io/ioutil"
	"net/http"
	"strconv"
	"strings"
	"testing"
	"time"

	"github.com/alibabacloud-go/tea/tea"
	"github.com/aliyun/alibabacloud-apigateway-core-sdk/go/utils"
)

var client = new(BaseClient)

func Test_InitClient(t *testing.T) {
	configTest := map[string]interface{}{
		"accessKeyId": "accessKeyId",
	}
	err := client.InitClient(configTest)
	utils.AssertNil(t, err)
}

func Test_GetHost(t *testing.T) {
	client.Domain = "apigateway.aliyuncs.com"
	host := client.GetHost()
	utils.AssertEqual(t, "apigateway.aliyuncs.com", host)
}

func Test_IsStatusCode(t *testing.T) {
	response := &tea.Response{
		StatusCode: 200,
	}
	isEqual := client.IsStatusCode(response, 200)
	utils.AssertEqual(t, true, isEqual)
}

func Test_DefaultNumber(t *testing.T) {
	num := client.DefaultNumber(0, 1)
	utils.AssertEqual(t, 1, num)

	num = client.DefaultNumber(2, 1)
	utils.AssertEqual(t, 2, num)
}

func Test_Default(t *testing.T) {
	str := client.Default("", "1")
	utils.AssertEqual(t, "1", str)

	str = client.Default("2", "1")
	utils.AssertEqual(t, "2", str)
}

func Test_ToJSONString(t *testing.T) {
	raw := map[string]interface{}{
		"apigateway": "sdk",
	}
	body := client.ToJSONString(raw)
	utils.AssertEqual(t, `{"apigateway":"sdk"}`, body)
}

func Test_ReadAsJSON(t *testing.T) {
	body := strings.NewReader("apigateway")
	readCloser := ioutil.NopCloser(body)
	httpResponse := &http.Response{
		Body: readCloser,
	}
	response := tea.NewResponse(httpResponse)
	result, err := client.ReadAsJSON(response)
	utils.AssertNotNil(t, err)
	utils.AssertEqual(t, 0, len(result))

	body = strings.NewReader(`{"apigateway": "sdk"}`)
	readCloser = ioutil.NopCloser(body)
	httpResponse.Body = readCloser
	response = tea.NewResponse(httpResponse)
	result, err = client.ReadAsJSON(response)
	utils.AssertNil(t, err)
	utils.AssertEqual(t, 1, len(result))
}

func Test_GetContentMD5(t *testing.T) {
	md5 := client.GetContentMD5("apigateway")
	utils.AssertEqual(t, "+wzeYiiyHYnsIitF7+xU5w==", md5)
}

func Test_GetSignature(t *testing.T) {
	req := tea.NewRequest()
	req.Query["sdk"] = "apigateway"
	req.Headers["baseclient"] = "go"
	sign := client.GetSignature(req)
	utils.AssertEqual(t, "h3zZzWDRJ+OiWSlhFl1YKhOvk5hOfxxOVIeH9kV86vw=", sign)
}

func Test_GetDate(t *testing.T) {
	date := client.GetDate()
	utils.AssertEqual(t, 29, len(date))
}

func Test_GetTimestamp(t *testing.T) {
	stamp := client.GetTimestamp()
	now := strconv.FormatInt(time.Now().Unix()*1000, 10)
	utils.AssertEqual(t, true, now >= stamp)
}

func Test_GetUUID(t *testing.T) {
	uuid := client.GetUUID()
	utils.AssertEqual(t, 32, len(uuid))
}

func Test_IsFail(t *testing.T) {
	response := &tea.Response{
		StatusCode: 300,
	}

	ok := client.IsFail(response)
	utils.AssertEqual(t, true, ok)

	response.StatusCode = 200
	ok = client.IsFail(response)
	utils.AssertEqual(t, false, ok)
}

func Test_ToForm(t *testing.T) {
	str := client.ToForm(nil)
	utils.AssertEqual(t, "", str)

	a := map[string]interface{}{
		"key1": "value1",
		"key2": "value2",
	}
	str = client.ToForm(a)
	utils.AssertContains(t, str, "key1=value1")
}
