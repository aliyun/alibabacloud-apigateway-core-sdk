// This file is auto-generated, don't edit it. Thanks.

#include <alibabacloud/apigateway_util.hpp>
#include <boost/any.hpp>
#include <darabonba/core.hpp>
#include <iostream>
#include <map>
#include <chrono>
#include "crypt/base64.h"
#include "crypt/hmac.h"
#include "crypt/sha256.h"
#include "crypt/md5.h"

using namespace std;

using namespace Alibabacloud_APIGatewayUtil;

string Alibabacloud_APIGatewayUtil::Client::getSignature(const shared_ptr<Darabonba::Request>& request,
                                                         const shared_ptr<string>& secret) {
  if (!request || !secret) {
    return "";
  }
  return getSignatureV1(request, make_shared<map<string, string>>(request->query), secret);
}

void flatten(map<string, string> &res, std::string prefix, boost::any curr) {
  if (typeid(map<string, boost::any>) == curr.type()) {
    map<string, boost::any> m = boost::any_cast<map<string, boost::any>>(curr);
    for (const auto &it : m) {
      std::string p;
      if (prefix.empty()) {
        p = prefix + it.first;
      } else {
        p = prefix + "." + it.first;
      }
      flatten(res, p, it.second);
    }
  } else if (typeid(vector<boost::any>) == curr.type()) {
    vector<boost::any> v = boost::any_cast<vector<boost::any>>(curr);
    int n = 0;
    for (const auto &it : v) {
      std::string p;
      if (prefix.empty()) {
        p = prefix + to_string(n + 1);
      } else {
        p = prefix + "." + to_string(n + 1);
      }
      flatten(res, p, it);
      n++;
    }
  } else {
    if (typeid(string) == curr.type()) {
      std::string v = boost::any_cast<string>(curr);
      res.insert(pair<string, string>(prefix, v));
    } else if (typeid(int) == curr.type()) {
      string v = std::to_string(boost::any_cast<int>(curr));
      res.insert(pair<string, string>(prefix, v));
    } else if (typeid(long) == curr.type()) {
      string v = std::to_string(boost::any_cast<long>(curr));
      res.insert(pair<string, string>(prefix, v));
    } else if (typeid(double) == curr.type()) {
      string v = std::to_string(boost::any_cast<double>(curr));
      res.insert(pair<string, string>(prefix, v));
    } else if (typeid(float) == curr.type()) {
      string v = std::to_string(boost::any_cast<float>(curr));
      res.insert(pair<string, string>(prefix, v));
    } else if (typeid(bool) == curr.type()) {
      auto b = boost::any_cast<bool>(curr);
      string v = b ? "true" : "false";
      res.insert(pair<string, string>(prefix, v));
    } else if (typeid(const char *) == curr.type()) {
      const char *v = boost::any_cast<const char *>(curr);
      res.insert(pair<string, string>(prefix, v));
    } else if (typeid(char *) == curr.type()) {
      char *v = boost::any_cast<char *>(curr);
      res.insert(pair<string, string>(prefix, v));
    }
  }
}

map<string, string> Alibabacloud_APIGatewayUtil::Client::toQuery(const shared_ptr<map<string, boost::any>>& filter) {
  if (!filter) {
    return map<string, string>();
  }
  map<string, string> flat;
  flatten(flat, string(""), boost::any(*filter));
  map<string, string> res;
  for (auto it : flat) {
    res.insert(pair<string, string>(it.first, it.second));
  }
  return res;
}

bool Alibabacloud_APIGatewayUtil::Client::isFail(const shared_ptr<int>& code) {
  if (code) {
    if (*code < 200 || *code >= 300) {
      return true;
    }
  }
  return false;
}

string Alibabacloud_APIGatewayUtil::Client::getContentMD5(const shared_ptr<string>& body) {
  if (body && !body->empty()) {
    MD5 md5 = MD5(*body);
    uint8_t buf[16];
    md5.get_digest(buf);
    return base64::encode_from_array(buf, 16);
  } else {
    return "";
  }
}

string Alibabacloud_APIGatewayUtil::Client::getTimestamp() {
  time_t t = time(nullptr);
  char timestamp[32] = {0};
  snprintf(timestamp, sizeof(timestamp), "%ld000", t);
  return timestamp;
}

string Alibabacloud_APIGatewayUtil::Client::getSignatureV1(const shared_ptr<Darabonba::Request>& request,
                                                           const shared_ptr<map<string, string>>& signedParams,
                                                           const shared_ptr<string>& secret) {
  if (!request || !secret || !signedParams) {
    return "";
  }
  const vector<string> MOVE_HEADERS = {
      "x-ca-signature",
      "x-ca-signature-headers",
      "accept",
      "content-md5",
      "content-type",
      "date",
      "host",
      "token",
      "user-agent"
  };

  map<string, string> headers = request->headers;
  for (const auto& it : request->headers ) {
    if (find(MOVE_HEADERS.begin(), MOVE_HEADERS.end(), it.first) != MOVE_HEADERS.end()) {
      headers.erase(it.first);
    }
  }

  string sign_headers;
  string header;
  for (const auto& it : headers) {
    sign_headers.append(it.first).append(",");
    header.append(it.first).append(":").append(it.second).append("\n");
  }
  header = header.substr(0, header.size()-1);
  sign_headers = sign_headers.substr(0, sign_headers.size()-1);
  request->headers["x-ca-signature-headers"] = sign_headers;

  map<string, string> queries = request->query;
  string url = request->pathname;
  if (!queries.empty()) {
    url += "?";
    for (const auto& it : queries) {
      url.append(it.first).append("=").append(it.second).append("&");
    }
    url = url.substr(0, url.size()-1);
  }

  string string_to_sign = request->method + "\n" + request->headers["accept"] + "\n" +
      request->headers["content-md5"] + "\n" + request->headers["content-type"] + "\n" +
      request->headers["date"] + "\n" + header + "\n" + url;

  boost::uint8_t hash_val[sha256::HASH_SIZE];
  hmac<sha256>::calc(string_to_sign, *secret, hash_val);
  return base64::encode_from_array(hash_val, sha256::HASH_SIZE);
}

