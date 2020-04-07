using System;
using System.Collections.Generic;
using System.Net;
using AlibabaCloud.APIGatewayUtil;
using Moq;
using Tea;
using Tea.Utils;
using Xunit;

namespace tests
{
    public class CommonTest
    {
        [Fact]
        public void Test_GetSignature()
        {
            TeaRequest request = new TeaRequest();
            request.Query.Add("sdk", "apigateway");
            request.Headers.Add("baseclient", "go");
            
            string sign = Common.GetSignature(request, "");
            Assert.Equal("wEZB57fwR/8gGbLPqa7/5DtppgRVGVN8Yy7Cm7A5Ko8=", sign);
        }

        [Fact]
        public void Test_ToQuery()
        {
            Dictionary<string, object> dic = new Dictionary<string, object>();
            dic["test"] = "test";
            dic["nullTest"] = null;

            Dictionary<string, string> result = Common.ToQuery(dic);
            Assert.Equal("test", result.Get("test"));
            Assert.False(result.ContainsKey("nullTest"));
        }

        [Fact]
        public void Test_IsFail()
        {
            Assert.True(Common.IsFail(100));
            Assert.False(Common.IsFail(200));
            Assert.True(Common.IsFail(300));
        }

        [Fact]
        public void Test_GetContentMD5()
        {
            Assert.Equal("+wzeYiiyHYnsIitF7+xU5w==", Common.GetContentMD5("apigateway"));
        }

    }
}
