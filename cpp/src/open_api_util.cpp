#include "crypt/base64.h"
#include "crypt/hmac.h"
#include "crypt/sha1.h"
#include <alibabacloud/open_api_util.hpp>
#include <boost/algorithm/string.hpp>
#include <boost/any.hpp>
#include <darabonba/core.hpp>
#include <darabonba/util.hpp>
#include <iostream>
#include <map>

using namespace std;
using namespace Alibabacloud_OpenApiUtil;

void Alibabacloud_OpenApiUtil::Client::convert(
    shared_ptr<Darabonba::Model> body, shared_ptr<Darabonba::Model> content) {
  map<string, boost::any> props;
  map<std::string, boost::any> properties = body->toMap();
  for (const auto &it : properties) {
    if (typeid(Darabonba::Stream) != it.second.type()) {
      props[it.first] = it.second;
    }
  }
  content->fromMap(props);
}

string get_canonicalized_headers(map<string, string> headers) {
  string canon_header;
  for (const auto &i : headers) {
    if (boost::starts_with(i.first, "x-acs-")) {
      canon_header.append(i.first).append(":").append(i.second).append("\n");
    }
  }
  return canon_header;
}

string get_canonicalized_resource(string pathname, map<string, string> query) {
  if (query.empty()) {
    return pathname;
  }
  string resource = pathname + "?";
  for (const auto &i : query) {
    if (i.second.empty()) {
      resource.append(i.first).append("&");
    } else {
      resource.append(i.first).append("=").append(i.second).append("&");
    }
  }
  return resource.substr(0, resource.size() - 1);
}

string Alibabacloud_OpenApiUtil::Client::getStringToSign(
    shared_ptr<Darabonba::Request> request) {
  if (!request) {
    return "";
  }
  string method = request->method;
  string pathname = request->pathname;
  map<string, string> headers = request->headers;
  map<string, string> query = request->query;
  string accept = headers["accept"];
  string content_md5 = headers["content-md5"];
  string content_type = headers["content-type"];
  string date = headers["date"];

  string sign_str;
  sign_str.append(method)
      .append("\n")
      .append(accept)
      .append("\n")
      .append(content_md5)
      .append("\n")
      .append(content_type)
      .append("\n")
      .append(date)
      .append("\n");

  return sign_str.append(get_canonicalized_headers(headers))
      .append(get_canonicalized_resource(pathname, query));
}

string Alibabacloud_OpenApiUtil::Client::getROASignature(
    shared_ptr<string> stringToSign, shared_ptr<string> secret) {
  boost::uint8_t hash_val[sha1::HASH_SIZE];
  hmac<sha1>::calc(*stringToSign, *secret, hash_val);
  return base64::encode_from_array(hash_val, sha1::HASH_SIZE);
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

string Alibabacloud_OpenApiUtil::Client::toForm(
    shared_ptr<map<string, boost::any>> filter) {
  if (!filter) {
    return "";
  }
  map<string, string> flat;
  flatten(flat, string(""), boost::any(*filter));
  map<string, string> res;
  for (auto it : flat) {
    res.insert(pair<string, string>(it.first, it.second));
  }

  return Darabonba_Util::Client::toFormString(
      make_shared<map<string, boost::any>>(
          Darabonba_Util::Client::anyifyMapValue(
              make_shared<map<string, string>>(res))));
}

string Alibabacloud_OpenApiUtil::Client::getTimestamp() {
  char buf[80];
  time_t t = time(nullptr);
  std::strftime(buf, sizeof buf, "%Y-%m-%dT%H:%M:%SZ", gmtime(&t));
  return buf;
}

map<string, string> Alibabacloud_OpenApiUtil::Client::query(
    shared_ptr<map<string, boost::any>> filter) {
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

string uppercase(string str) {
  std::transform(str.begin(), str.end(), str.begin(),
                 [](unsigned char c) { return std::toupper(c); });
  return str;
}

string url_encode(const std::string &str) {
  std::stringstream escaped;
  escaped.fill('0');
  escaped << hex;

  for (char c : str) {
    if (isalnum(c) || c == '-' || c == '_' || c == '.' || c == '~') {
      escaped << c;
      continue;
    }
    escaped << std::uppercase;
    escaped << '%' << std::setw(2) << int((unsigned char)c);
    escaped << nouppercase;
  }

  return escaped.str();
}

std::string implode(const std::vector<std::string> &vec,
                    const std::string &glue) {
  string res;
  int n = 0;
  for (const auto &str : vec) {
    if (n == 0) {
      res = str;
    } else {
      res += glue + str;
    }
    n++;
  }
  return res;
}

std::string strToSign(std::string method, const map<string, string> &query) {
  std::vector<string> tmp;
  for (const auto &it : query) {
    std::string s;
    s = s.append(url_encode(it.first))
            .append("=")
            .append(url_encode(it.second));
    tmp.push_back(s);
  }
  std::string str = implode(tmp, "&");
  std::string res;
  return res.append(uppercase(std::move(method)))
      .append("&%2F&")
      .append(url_encode(str));
}

string Alibabacloud_OpenApiUtil::Client::getRPCSignature(
    shared_ptr<map<string, string>> signedParams, shared_ptr<string> method,
    shared_ptr<string> secret) {
  map<string, string> sp;
  if (!signedParams) {
    sp = map<string, string>();
  } else {
    sp = *signedParams;
  }
  string m = !method ? "" : *method;
  string s = !secret ? "" : *secret;

  std::string str = strToSign(std::move(m), sp);
  s = s + "&";
  boost::uint8_t hash_val[sha1::HASH_SIZE];
  hmac<sha1>::calc(str, s, hash_val);
  return base64::encode_from_array(hash_val, sha1::HASH_SIZE);
}

string flat_repeat_vec(vector<boost::any> arr, string prefix) {
  map<string, string> flat;
  map<string, boost::any> m = {{prefix, arr}};
  flatten(flat, string(""), boost::any(m));

  vector<string> vec;
  for (const auto &i : flat) {
    vec.push_back(url_encode(i.first) + "=" + url_encode(i.second));
  }
  return boost::join(vec, "&&");
}

string flat_vec(vector<boost::any> arr, string sep) {
  vector<string> str_arr;
  for (auto i : arr) {
    if (typeid(int) == i.type()) {
      str_arr.push_back(to_string(boost::any_cast<int>(i)));
    } else if (typeid(long) == i.type()) {
      str_arr.push_back(to_string(boost::any_cast<long>(i)));
    } else if (typeid(string) == i.type()) {
      str_arr.push_back(boost::any_cast<string>(i));
    } else if (typeid(double) == i.type()) {
      str_arr.push_back(to_string(boost::any_cast<double>(i)));
    } else if (typeid(bool) == i.type()) {
      auto b = boost::any_cast<bool>(i);
      string v = b ? "true" : "false";
      str_arr.push_back(v);
    } else if (typeid(const char *) == i.type()) {
      str_arr.push_back(boost::any_cast<const char *>(i));
    } else if (typeid(char *) == i.type()) {
      str_arr.push_back(boost::any_cast<char *>(i));
    }
  }
  return boost::join(str_arr, sep);
}

string Alibabacloud_OpenApiUtil::Client::arrayToStringWithSpecifiedStyle(
    const boost::any &array, shared_ptr<string> prefix,
    shared_ptr<string> style) {
  string result;
  if (typeid(shared_ptr<vector<boost::any>>) == array.type()) {
    shared_ptr<vector<boost::any>> vec_ptr =
        boost::any_cast<shared_ptr<vector<boost::any>>>(array);
    if (!style || !vec_ptr) {
      return result;
    }

    if (*style == "repeatList") {
      return result = flat_repeat_vec(*vec_ptr, *prefix);
    } else if (*style == "simple") {
      return result = flat_vec(*vec_ptr, ",");
    } else if (*style == "spaceDelimited") {
      return result = flat_vec(*vec_ptr, " ");
      ;
    } else if (*style == "pipeDelimited") {
      return result = flat_vec(*vec_ptr, "|");
      ;
    } else if (*style == "json") {
      return result = Darabonba_Util::Client::toJSONString(array);
    }
    return result;
  }
  return "";
}

std::vector<std::string> explode(const std::string &str,
                                 const std::string &delimiter) {
  int pos = str.find(delimiter, 0);
  int pos_start = 0;
  int split_n = pos;
  string line_text(delimiter);

  std::vector<std::string> dest;

  while (pos > -1) {
    line_text = str.substr(pos_start, split_n);
    pos_start = pos + 1;
    pos = str.find(delimiter, pos + 1);
    split_n = pos - pos_start;
    dest.push_back(line_text);
  }
  line_text = str.substr(pos_start, str.length() - pos_start);
  dest.push_back(line_text);
  return dest;
}

string
Alibabacloud_OpenApiUtil::Client::getEndpoint(shared_ptr<string> endpoint,
                                              shared_ptr<bool> serverUse,
                                              shared_ptr<string> endpointType) {
  string e = !endpoint ? "" : *endpoint;
  bool s;
  if (!serverUse) {
    s = false;
  } else {
    s = *serverUse;
  }
  string et = !endpointType ? "" : *endpointType;
  if (et == string("internal")) {
    std::vector<std::string> tmp = explode(e, ".");
    tmp.at(0) = tmp.at(0).append("-internal");
    e = implode(tmp, ".");
  }
  if (s && et == string("accelerate")) {
    e = "oss-accelerate.aliyuncs.com";
  }
  return e;
}
