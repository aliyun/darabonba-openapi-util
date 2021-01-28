#include "gtest/gtest.h"
#include <darabonba/core.hpp>
#include <alibabacloud/open_api_util.hpp>

using namespace std;

int main(int argc, char **argv) {
  ::testing::InitGoogleTest(&argc, argv);
  return RUN_ALL_TESTS();
}

class TestCoverModel : public Darabonba::Model {
public:
  shared_ptr<string> requestId;
  shared_ptr<int> num;
  shared_ptr<Darabonba::Stream> stream;

  map<string, boost::any> toMap() override {
    map<string, boost::any> mp;
    if (requestId) {
      mp["requestId"] = *requestId;
    }
    if (num) {
      mp["num"] = *num;
    }
    if (stream) {
      mp["stream"] = *stream;
    }
    return mp;
  }

  void fromMap(map<string, boost::any> m) override {
    if (m.find("requestId") != m.end()) {
      requestId = make_shared<string>(boost::any_cast<string>(m["requestId"]));
    }

    if (m.find("num") != m.end()) {
      num = make_shared<int>(boost::any_cast<int>(m["num"]));
    }

    if (m.find("stream") != m.end()) {
      stream = make_shared<Darabonba::Stream>(boost::any_cast<Darabonba::Stream>(m["stream"]));
    }
  };
  void validate() override {};
};

TEST(tests_Client, test_convert) {
  shared_ptr<TestCoverModel> model1(new TestCoverModel());
  shared_ptr<TestCoverModel> model2(new TestCoverModel());
  model1->requestId = make_shared<string>("req001x");
  model1->num = make_shared<int>(10);
  model1->stream = make_shared<Darabonba::Stream>(make_shared<stringstream>("test_stream"));
  Alibabacloud_OpenApiUtil::Client::convert(
      model1,
      model2
  );
  ASSERT_EQ("req001x", *(model2->requestId));
  ASSERT_EQ(10, *(model2->num));
  ASSERT_TRUE(!model2->stream);
}

TEST(tests_Client, test_getStringToSign) {
  shared_ptr<Darabonba::Request> request(new Darabonba::Request());
  request->method = "POST";
  request->query = {
      {"test", "tests"}
  };
  string string_to_sign = Alibabacloud_OpenApiUtil::Client::getStringToSign(request);
  ASSERT_EQ("POST\n\n\n\n\n?test=tests", string_to_sign);

  shared_ptr<Darabonba::Request> request1(new Darabonba::Request());
  request1->method = "POST";
  request1->headers = {
      {"content-md5", "md5"}
  };
  string string_to_sign1 = Alibabacloud_OpenApiUtil::Client::getStringToSign(request1);
  ASSERT_EQ("POST\n\nmd5\n\n\n", string_to_sign1);

  shared_ptr<Darabonba::Request> request2(new Darabonba::Request());
  request2->pathname = "Pathname";
  request2->query = {
      {"ccp", "ok"},
      {"test", "tests"},
      {"test1", ""}
  };
  request2->headers = {
      {"x-acs-meta", "user"},
      {"accept", "application/json"},
      {"content-md5", "md5"},
      {"content-type", "application/json"},
      {"date", "date"},
  };
  string string_to_sign2 = Alibabacloud_OpenApiUtil::Client::getStringToSign(request2);
  ASSERT_EQ("GET\napplication/json\nmd5\napplication/json\ndate\nx-acs-meta:user\nPathname?ccp=ok&test=tests&test1",
            string_to_sign2);

}

TEST(tests_Client, test_getROASignature) {
  shared_ptr<Darabonba::Request> request(new Darabonba::Request());
  string string_to_sign = Alibabacloud_OpenApiUtil::Client::getStringToSign(request);
  ASSERT_EQ("GET\n\n\n\n\n", string_to_sign);

  string signature = Alibabacloud_OpenApiUtil::Client::getROASignature(
      make_shared<string>(string_to_sign),
      make_shared<string>("secret")
  );
  ASSERT_EQ("XGXDWA78AEvx/wmfxKoVCq/afWw=", signature);
}

TEST(tests_Client, test_toForm) {
  shared_ptr<map<string, boost::any>> filter(new map<string, boost::any>({
                                                                             {"client", string("test")},
                                                                             {"client1", boost::any()},
                                                                             {"strs",
                                                                              vector<boost::any>({"str1", "str2"})},
                                                                             {"tag", map<string, boost::any>({{"key",
                                                                                                               "value"}})},
                                                                         }));
  string result = Alibabacloud_OpenApiUtil::Client::toForm(filter);
  ASSERT_EQ("client=test&strs.1=str1&strs.2=str2&tag.key=value",
            result);
}

TEST(tests_Client, test_getTimestamp) {
  ASSERT_EQ(20, Alibabacloud_OpenApiUtil::Client::getTimestamp().size());
}

TEST(tests_Client, test_query) {
  shared_ptr<map<string, boost::any>> filter(new map<string, boost::any>({
                                                                             {"str_test", string("test")},
                                                                             {"none_test", boost::any()},
                                                                             {"int_test", 1}
                                                                         }));
  map<string, string> result = Alibabacloud_OpenApiUtil::Client::query(filter);
  ASSERT_EQ("test", result["str_test"]);
  ASSERT_TRUE(result["none_test"].empty());
  ASSERT_EQ("1", result["int_test"]);

  vector<boost::any> fl = {1, boost::any()};
  map<string, boost::any> sub_map_fl = {
      {"str_test", string("test")},
      {"none_test", boost::any()},
      {"int_test", 2}
  };
  fl.push_back(sub_map_fl);
  vector<boost::any> sl = {1, boost::any()};
  fl.push_back(sl);
  (*filter)["list"] = fl;
  map<string, string> result1 = Alibabacloud_OpenApiUtil::Client::query(filter);
  ASSERT_EQ("1", result1["list.1"]);
  ASSERT_TRUE(result1["list.2"].empty());
  ASSERT_EQ("1", result1["int_test"]);
  ASSERT_EQ("2", result1["list.3.int_test"]);
  ASSERT_TRUE(result1["list.3.none_test"].empty());
  ASSERT_EQ("test", result1["list.3.str_test"]);
  ASSERT_EQ("1", result1["list.4.1"]);

  map<string, boost::any> sub_map_fd = {
      {"str_test", string("test")},
      {"none_test", boost::any()},
      {"int_test", 2}
  };
  map<string, boost::any> fd = {
      {"first_map_map", sub_map_fd},
      {"first_map_list", sl},
      {"none_test", boost::any()},
      {"int_test", 2},
      {"str_test", "test"}
  };
  (*filter)["map"] = fd;
  map<string, string> result2 = Alibabacloud_OpenApiUtil::Client::query(filter);
  ASSERT_EQ("1", result2["map.first_map_list.1"]);
  ASSERT_TRUE(result2["map.none_test"].empty());
  ASSERT_EQ("2", result2["map.int_test"]);
  ASSERT_EQ("test", result2["map.str_test"]);
  ASSERT_TRUE(result2["map.first_map_map.none_test"].empty());
  ASSERT_EQ("2", result2["map.first_map_map.int_test"]);
  ASSERT_EQ("test", result2["map.first_map_map.str_test"]);
}

TEST(tests_Client, test_getRPCSignature) {
  map<string, string> query = {
      {"query", "test"},
      {"body", "test"}
  };
  string signature = Alibabacloud_OpenApiUtil::Client::getRPCSignature(
      make_shared<map<string, string>>(query),
      make_shared<string>("GET"),
      make_shared<string>("secret")
  );
  ASSERT_EQ("XlUyV4sXjOuX5FnjUz9IF9tm5rU=", signature);
}

TEST(tests_Client, test_arrayToStringWithSpecifiedStyle) {
  shared_ptr<vector<boost::any>> empty_ptr;
  vector<boost::any> array = {"ok", "test", 2, 3};
  shared_ptr<string> prefix(new string("instance"));
  string t1 = Alibabacloud_OpenApiUtil::Client::arrayToStringWithSpecifiedStyle(
      make_shared<vector<boost::any>>(array),
      prefix,
      make_shared<string>("repeatList")
  );

  string t2 = Alibabacloud_OpenApiUtil::Client::arrayToStringWithSpecifiedStyle(
      make_shared<vector<boost::any>>(array),
      prefix,
      make_shared<string>("json")
  );

  string t3 = Alibabacloud_OpenApiUtil::Client::arrayToStringWithSpecifiedStyle(
      make_shared<vector<boost::any>>(array),
      prefix,
      make_shared<string>("simple")
  );

  string t4 = Alibabacloud_OpenApiUtil::Client::arrayToStringWithSpecifiedStyle(
      make_shared<vector<boost::any>>(array),
      prefix,
      make_shared<string>("spaceDelimited")
  );

  string t5 = Alibabacloud_OpenApiUtil::Client::arrayToStringWithSpecifiedStyle(
      make_shared<vector<boost::any>>(array),
      prefix,
      make_shared<string>("pipeDelimited")
  );

  string t6 = Alibabacloud_OpenApiUtil::Client::arrayToStringWithSpecifiedStyle(
      make_shared<vector<boost::any>>(array),
      prefix,
      make_shared<string>("piDelimited")
  );

  string t7 = Alibabacloud_OpenApiUtil::Client::arrayToStringWithSpecifiedStyle(
      empty_ptr,
      prefix,
      make_shared<string>("pipeDelimited")
  );

  ASSERT_EQ("instance.1=ok&&instance.2=test&&instance.3=2&&instance.4=3", t1);
  ASSERT_EQ("[\"ok\", \"test\", 2, 3]", t2);
  ASSERT_EQ("ok,test,2,3", t3);
  ASSERT_EQ("ok test 2 3", t4);
  ASSERT_EQ("ok|test|2|3", t5);
  ASSERT_EQ("", t6);
  ASSERT_EQ("", t7);
}

TEST(tests, getEndpoint) {
  shared_ptr<string> endpoint(new string("ecs.cn-hangzhou.aliyuncs.com"));
  shared_ptr<bool> serverUse(new bool(false));
  shared_ptr<string> endpointType(new string("public"));

  ASSERT_EQ(string("ecs.cn-hangzhou.aliyuncs.com"),
            Alibabacloud_OpenApiUtil::Client::getEndpoint(endpoint, serverUse, endpointType));
  *endpointType = "internal";
  ASSERT_EQ(string("ecs-internal.cn-hangzhou.aliyuncs.com"),
            Alibabacloud_OpenApiUtil::Client::getEndpoint(endpoint, serverUse, endpointType));
  *serverUse = true;
  *endpointType = "accelerate";
  ASSERT_EQ(string("oss-accelerate.aliyuncs.com"),
            Alibabacloud_OpenApiUtil::Client::getEndpoint(endpoint, serverUse, endpointType));
}

class MockModel : public Darabonba::Model {
public:
  MockModel() {}
  explicit MockModel(const std::map<string, boost::any> &config)
      : Darabonba::Model(config) {
    fromMap(config);
  };

  void validate() override {}

  map<string, boost::any> toMap() override {
    map<string, boost::any> res;
    if (headers) {
      res["headers"] = boost::any(*headers);
    }
    if (query) {
      res["query"] = boost::any(*query);
    }
    if (body) {
      res["body"] = boost::any(*body);
    }
    return res;
  }

  void fromMap(map<string, boost::any> m) override {
    if (m.find("headers") != m.end()) {
      map<string, string> map1 =
          boost::any_cast<map<string, string>>(m["headers"]);
      map<string, string> toMap1;
      for (auto item : map1) {
        toMap1[item.first] = item.second;
      }
      headers = make_shared<map<string, string>>(toMap1);
    }
    if (m.find("query") != m.end()) {
      map<string, string> map1 =
          boost::any_cast<map<string, string>>(m["query"]);
      map<string, string> toMap1;
      for (auto item : map1) {
        toMap1[item.first] = item.second;
      }
      query = make_shared<map<string, string>>(toMap1);
    }
    if (m.find("body") != m.end()) {
      body = make_shared<boost::any>(boost::any_cast<boost::any>(m["body"]));
    }
  }

  shared_ptr<map<string, string>> headers{};
  shared_ptr<map<string, string>> query{};
  shared_ptr<boost::any> body{};

  virtual ~MockModel() = default;
};

TEST(tests, parseToMap) {
  shared_ptr<map<string, boost::any>> m = make_shared<map<string, boost::any>>(
      map<string, boost::any>(
          {{"foo", make_shared<vector<boost::any>>(vector<boost::any>({string("bar")}))}}
    )
  );
  map<string, boost::any> msa = Alibabacloud_OpenApiUtil::Client::parseToMap(m);
  vector<boost::any> foo = *boost::any_cast<shared_ptr<vector<boost::any>>>((*m)["foo"]);
  ASSERT_EQ(string("bar"), boost::any_cast<string>(foo[0]));
}


TEST(tests, hexEncode) {
  string str = "test";

  string res = Alibabacloud_OpenApiUtil::Client::hexEncode(
      make_shared<vector<uint8_t>>(Alibabacloud_OpenApiUtil::Client::hash(make_shared<vector<uint8_t>>(vector<uint8_t>(str.begin(), str.end())),
          make_shared<string>("ACS3-RSA-SHA256")))
      );

  ASSERT_EQ("9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08", res);

  string res1 = Alibabacloud_OpenApiUtil::Client::hexEncode(
      make_shared<vector<uint8_t>>(Alibabacloud_OpenApiUtil::Client::hash(make_shared<vector<uint8_t>>(vector<uint8_t>(str.begin(), str.end())),
                                                                          make_shared<string>("ACS3-HMAC-SM3")))
  );

  ASSERT_EQ("55e12e91650d2fec56ec74e1d3e4ddbfce2ef3a65890c2a19ecf88a307e76a23", res1);
}

TEST(tests, getAuthorization) {
  Darabonba::Request request;
  request.query = map<string, string>({
    {"test", "ok"},
    {"empty", ""}
  });
  request.headers = map<string, string>({
                                            {"x-acs-test", "http"},
                                            {"x-acs-TEST", "https"}
  });
  string res = Alibabacloud_OpenApiUtil::Client::getAuthorization(
      make_shared<Darabonba::Request>(request),
      make_shared<string>("ACS3-HMAC-SHA256"),
      make_shared<string>("55e12e91650d2fec56ec74e1d3e4ddbfce2ef3a65890c2a19ecf88a307e76a23"),
      make_shared<string>("acesskey"),
      make_shared<string>("secret")
      );
  ASSERT_EQ(
      "ACS3-HMAC-SHA256 Credential=acesskey,SignedHeaders=x-acs-test,Signature=910e59dd385879346de704a06c96613f8157aa7124e6d1c4f2bdffa1e5588187",
      res
  );
}


TEST(tests, getEncodePath) {
  string res = Alibabacloud_OpenApiUtil::Client::getEncodePath(
      make_shared<string>("/path/ test")
  );
  ASSERT_EQ("/path/%20test", res);
}
