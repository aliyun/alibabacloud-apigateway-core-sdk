// This file is auto-generated, don't edit it. Thanks.

#ifndef ALIBABACLOUD_APIGATEWAYUTIL_H_
#define ALIBABACLOUD_APIGATEWAYUTIL_H_

#include <boost/any.hpp>
#include <darabonba/core.hpp>
#include <iostream>
#include <map>

using namespace std;

namespace Alibabacloud_APIGatewayUtil {
class Client {
public:
  static string getSignature(const shared_ptr<Darabonba::Request>& request, const shared_ptr<string>& secret);
  static map<string, string> toQuery(const shared_ptr<map<string, boost::any>>& filter);
  static bool isFail(const shared_ptr<int>& code);
  static string getContentMD5(const shared_ptr<string>& body);
  static string getTimestamp();
  static string getSignatureV1(const shared_ptr<Darabonba::Request>& request, const shared_ptr<map<string, string>>& signedParams, const shared_ptr<string>& secret);

  Client() {};
};
} // namespace Alibabacloud_APIGatewayUtil

#endif
