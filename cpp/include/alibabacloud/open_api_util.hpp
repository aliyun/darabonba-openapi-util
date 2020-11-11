// This file is auto-generated, don't edit it. Thanks.

#ifndef ALIBABACLOUD_OPENAPIUTIL_H_
#define ALIBABACLOUD_OPENAPIUTIL_H_

#include <boost/any.hpp>
#include <darabonba/core.hpp>
#include <iostream>
#include <map>

using namespace std;

namespace Alibabacloud_OpenApiUtil {
class Client {
public:
  static void convert(shared_ptr<Darabonba::Model> body, shared_ptr<Darabonba::Model> content);
  static string getStringToSign(shared_ptr<Darabonba::Request> request);
  static string getROASignature(shared_ptr<string> stringToSign, shared_ptr<string> secret);
  static string toForm(shared_ptr<map<string, boost::any>> filter);
  static string getTimestamp();
  static map<string, string> query(shared_ptr<map<string, boost::any>> filter);
  static string getRPCSignature(shared_ptr<map<string, string>> signedParams, shared_ptr<string> method, shared_ptr<string> secret);
  static string arrayToStringWithSpecifiedStyle(const boost::any &array, shared_ptr<string> prefix, shared_ptr<string> style);
//  static map<string, boost::any> parseToMap(const boost::any &input);

  Client() {};
};
} // namespace Alibabacloud_OpenApiUtil

#endif
