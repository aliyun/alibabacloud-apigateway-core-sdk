package service

import (
	"testing"

	"github.com/alibabacloud-go/tea/tea"
	"github.com/alibabacloud-go/tea/utils"
)

func Test_GetContentMD5(t *testing.T) {
	md5 := GetContentMD5(tea.String(`{"test":"ok"}`))
	utils.AssertEqual(t, "b969h28MOfCVGrra1smdCg==", tea.StringValue(md5))
}

func Test_GetSignature(t *testing.T) {
	req := tea.NewRequest()
	req.Query["sdk"] = "apigateway"
	req.Headers["baseclient"] = "go"
	sign := GetSignature(req, tea.String(""))
	utils.AssertEqual(t, "h3zZzWDRJ+OiWSlhFl1YKhOvk5hOfxxOVIeH9kV86vw=", tea.StringValue(sign))
}

func Test_IsFail(t *testing.T) {
	ok := IsFail(tea.Int(300))
	utils.AssertEqual(t, true, ok)

	ok = IsFail(tea.Int(200))
	utils.AssertEqual(t, false, ok)
}

func Test_ToQuery(t *testing.T) {
	res := ToQuery(map[string]interface{}{"test": "ok"})
	utils.AssertEqual(t, res["test"], "ok")
}
