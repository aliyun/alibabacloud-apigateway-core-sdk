using AlibabaCloud.Apigateway.Utils;

using Tea;

using Xunit;

namespace baseClientTest.Utils
{
    public class BaseUtilsTest
    {
        [Fact]
        public void TestGetSignedHeader()
        {
            TeaRequest request = new TeaRequest();
            request.Headers["key"] = "value";
            request.Headers["host"] = "hostNull";
            string signHeader = BaseUtils.GetSignedHeader(request);
            Assert.NotNull(signHeader);
            Assert.Contains("key", signHeader);
            Assert.DoesNotContain("host", signHeader);
        }

        [Fact]
        public void TestBuildUrl()
        {
            TeaRequest request = new TeaRequest();
            request.Pathname = "pathName";
            string url = BaseUtils.BuildUrl(request);
            Assert.NotNull(url);
            Assert.Equal("pathName", url);

            request.Query["key"] = "value";
            url = BaseUtils.BuildUrl(request);
            Assert.NotNull(url);
            Assert.Equal("pathName?key=value", BaseUtils.BuildUrl(request));

            request.Pathname = "pathName";
            url = BaseUtils.BuildUrl(request);
            Assert.NotNull(url);
            Assert.Equal("pathName?key=value", BaseUtils.BuildUrl(request));

            request.Query["keyAnd"] = "valueAnd";
            url = BaseUtils.BuildUrl(request);
            Assert.NotNull(url);
            Assert.Equal("pathName?key=value&keyAnd=valueAnd", BaseUtils.BuildUrl(request));
        }
    }
}
