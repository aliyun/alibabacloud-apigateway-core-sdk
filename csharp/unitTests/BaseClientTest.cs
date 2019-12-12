using System;
using System.Collections.Generic;
using System.IO;
using System.Net;
using System.Text;
using System.Threading;

using AlibabaCloud.Apigateway;

using Aliyun.Credentials.Utils;

using Moq;

using Tea;

using Xunit;

namespace baseClientTest
{
    public class BaseClientTest
    {
        public class ConfigTest : TeaModel
        {
            [NameInMap("domain")]
            [Validation(Required = false)]
            public string Domain { get; set; }

            [NameInMap("protocol")]
            [Validation(Required = false)]
            public string Protocol { get; set; }

            [NameInMap("appKey")]
            [Validation(Required = false)]
            public string AppKey { get; set; }

            [NameInMap("appSecret")]
            [Validation(Required = false)]
            public string AppSecret { get; set; }

            [NameInMap("token")]
            [Validation(Required = false)]
            public string Token { get; set; }

            [NameInMap("stage")]
            [Validation(Required = false)]
            public string Stage { get; set; }
        }

        BaseClient client;

        public BaseClientTest()
        {
            ConfigTest config = new ConfigTest
            {
                AppKey = "accessKeyID",
                AppSecret = "",
                Domain = "domain"
            };
            client = new BaseClient(config.ToMap());
        }

        [Fact]
        public void TestReadAsJSON()
        {
            Mock<HttpWebResponse> mockHttpWebResponse = new Mock<HttpWebResponse>();
            mockHttpWebResponse.Setup(p => p.StatusCode).Returns(HttpStatusCode.OK);
            mockHttpWebResponse.Setup(p => p.StatusDescription).Returns("StatusDescription");
            mockHttpWebResponse.Setup(p => p.Headers).Returns(new WebHeaderCollection());
            string jsonStr = "{\"arrayObj\":[[{\"itemName\":\"item\",\"itemInt\":1},{\"itemName\":\"item2\",\"itemInt\":2}],[{\"itemName\":\"item3\",\"itemInt\":3}]],\"arrayList\":[[[1,2],[3,4]],[[5,6],[7]],[]],\"listStr\":[1,2,3],\"items\":[{\"total_size\":18,\"partNumber\":1,\"tags\":[{\"aa\":\"11\"}]},{\"total_size\":20,\"partNumber\":2,\"tags\":[{\"aa\":\"22\"}]}],\"next_marker\":\"\",\"test\":{\"total_size\":19,\"partNumber\":1,\"tags\":[{\"aa\":\"11\"}]}}";
            byte[] array = Encoding.UTF8.GetBytes(jsonStr);
            MemoryStream stream = new MemoryStream(array);
            mockHttpWebResponse.Setup(p => p.GetResponseStream()).Returns(stream);
            TeaResponse teaResponse = new TeaResponse(mockHttpWebResponse.Object);
            Dictionary<string, object> dic = (Dictionary<string, object>) TestHelper.RunInstanceMethod(client.GetType(), "_readAsJSON", client, new object[] { teaResponse });
            Assert.NotNull(dic);
        }

        [Fact]
        public void TestIsStatusCode()
        {
            Mock<HttpWebResponse> mockHttpWebResponse = new Mock<HttpWebResponse>();
            mockHttpWebResponse.Setup(p => p.StatusCode).Returns(HttpStatusCode.OK);
            mockHttpWebResponse.Setup(p => p.StatusDescription).Returns("StatusDescription");
            mockHttpWebResponse.Setup(p => p.Headers).Returns(new WebHeaderCollection());
            string jsonStr = "test";
            byte[] array = Encoding.UTF8.GetBytes(jsonStr);
            MemoryStream stream = new MemoryStream(array);
            mockHttpWebResponse.Setup(p => p.GetResponseStream()).Returns(stream);
            TeaResponse teaResponse = new TeaResponse(mockHttpWebResponse.Object);
            Assert.True((bool) TestHelper.RunInstanceMethod(client.GetType(), "_isStatusCode", client, new object[] { teaResponse, 200 }));
        }

        [Fact]
        public void TestToJsonString()
        {
            Dictionary<string, object> dict = new Dictionary<string, object>
            { { "key", "value" }
            };
            string jsonStr = (string) TestHelper.RunInstanceMethod(client.GetType(), "_toJSONString", client, new object[] { dict });
            Assert.Equal("{\"key\":\"value\"}", jsonStr);
        }

        [Fact]
        public void TestGetUUID()
        {
            Assert.NotNull(TestHelper.RunInstanceMethod(client.GetType(), "_getUUID", client, new object[] { }));
        }

        [Fact]
        public void TestGetTimeStamp()
        {
            string timeBefore = DateTime.UtcNow.GetTimeMillis().ToString();
            Thread.Sleep(10);
            string timeStamp = (string) TestHelper.RunInstanceMethod(client.GetType(), "_getTimeStamp", client, new object[] { });
            Thread.Sleep(10);
            string timeAfter = DateTime.UtcNow.GetTimeMillis().ToString();
            Assert.True(Convert.ToInt64(timeStamp) > Convert.ToInt64(timeBefore));
            Assert.True(Convert.ToInt64(timeAfter) > Convert.ToInt64(timeStamp));
        }

        [Fact]
        public void TestGetDate()
        {
            string Date = (string) TestHelper.RunInstanceMethod(typeof(BaseClient), "_getDate", client, null);
            Assert.NotNull(Date);
        }

        [Fact]
        public void TestGetContentMD5()
        {
            TeaRequest request = new TeaRequest();

            request.Body = new MemoryStream(Encoding.UTF8.GetBytes("apigateway"));
            Assert.Equal("+wzeYiiyHYnsIitF7+xU5w==", TestHelper.RunInstanceMethod(client.GetType(), "_getContentMD5", client, new object[] { request }));
        }

        [Fact]
        public void TestGetHost()
        {
            string host = (string) TestHelper.RunInstanceMethod(client.GetType(), "_getHost", client, new object[] { });
            Assert.Equal("domain", host);
        }

        [Fact]
        public void TestDefaultNumber()
        {
            Assert.Equal(0, (int) TestHelper.RunInstanceMethod(typeof(BaseClient), "_defaultNumber", client, new object[] {-1, 0 }));

            Assert.Equal(1, (int) TestHelper.RunInstanceMethod(typeof(BaseClient), "_defaultNumber", client, new object[] { 1, 0 }));
        }

        [Fact]
        public void TestDefault()
        {
            Assert.Equal("default", (string) TestHelper.RunInstanceMethod(typeof(BaseClient), "_default", client, new object[] { string.Empty, "default" }));

            Assert.Equal("inputStr", (string) TestHelper.RunInstanceMethod(typeof(BaseClient), "_default", client, new object[] { "inputStr", "default" }));
        }

        [Fact]
        public void TestGetSignature()
        {
            TeaRequest request = new TeaRequest();
            request.Query.Add("sdk", "apigateway");
            request.Headers.Add("baseclient", "go");
            string sign = (string) TestHelper.RunInstanceMethod(typeof(BaseClient), "_getSignature", client, new object[] { request });
            Assert.Equal("h3zZzWDRJ+OiWSlhFl1YKhOvk5hOfxxOVIeH9kV86vw=", sign);
        }

    }
}
