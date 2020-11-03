#include "gtest/gtest.h"
#include <darabonba/core.hpp>
#include <alibabacloud/apigateway_util.hpp>

int main(int argc, char **argv) {
  ::testing::InitGoogleTest(&argc, argv);
  return RUN_ALL_TESTS();
}

TEST(tests_Client, test_getSignature)
{
  Darabonba::Request request;
  request.method = "test";
  request.headers["accept"] = "json";
  request.headers["date"] = "now";
  request.pathname = "test";
  ASSERT_EQ("lzttujz/sOhS2QSBBSZBq58ZNSxvSDdkWWlknQxnrt0=",
            Alibabacloud_APIGatewayUtil::Client::getSignature(
                make_shared<Darabonba::Request>(request),
                make_shared<string>("sk")
            ));

  Darabonba::Request query_test;
  query_test.pathname = "test";
  query_test.query["testKey"] = "value";
  ASSERT_EQ("YqikZu1HxGEbfG2w4Hj95LyievbItAnBc8pnXF4Otgg=",
            Alibabacloud_APIGatewayUtil::Client::getSignature(
                make_shared<Darabonba::Request>(query_test),
                make_shared<string>("sk")
            ));
}

TEST(tests_Client, test_toQuery)
{
  map<string, boost::any> m = {
      {"test", "test"}
  };
  ASSERT_EQ(
      "test",
      Alibabacloud_APIGatewayUtil::Client::toQuery(make_shared<map<string, boost::any>>(m)).at("test")
  );
}

TEST(tests_Client, test_isFail)
{
  ASSERT_TRUE(Alibabacloud_APIGatewayUtil::Client::isFail(make_shared<int>(100)));
  ASSERT_FALSE(Alibabacloud_APIGatewayUtil::Client::isFail(make_shared<int>(200)));
  ASSERT_TRUE(Alibabacloud_APIGatewayUtil::Client::isFail(make_shared<int>(400)));
}

TEST(tests_Client, test_getContentMD5)
{
  ASSERT_EQ("", Alibabacloud_APIGatewayUtil::Client::getContentMD5(make_shared<string>("")));
  ASSERT_EQ("govO+HY8G8YW4loGvkuQ/w==", Alibabacloud_APIGatewayUtil::Client::getContentMD5(make_shared<string>("{\"test\":\"test\"}")));
  string s = R"({"id": "66d4e368-0acd-48ab-a7d1-38c0f662a69f", "params": {"input": "test"}, "request": {"apiVer": "1.0.0"}})";
  ASSERT_EQ("2UqEQRjhvD1CAMcIb3rEhw==", Alibabacloud_APIGatewayUtil::Client::getContentMD5(make_shared<string>(s)));
}

TEST(tests_Client, test_getTimestamp)
{
  ASSERT_EQ(13, Alibabacloud_APIGatewayUtil::Client::getTimestamp().size());
}

TEST(tests_Client, test_getSignatureV1)
{
  Darabonba::Request request;
  request.pathname = "test";
  request.query["testKey"] = "value";
  ASSERT_EQ("YqikZu1HxGEbfG2w4Hj95LyievbItAnBc8pnXF4Otgg=",
            Alibabacloud_APIGatewayUtil::Client::getSignatureV1(
                make_shared<Darabonba::Request>(request),
                make_shared<map<string, string>>(request.query),
                make_shared<string>("sk")
            ));
}
