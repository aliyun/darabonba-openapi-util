using System.Collections.Generic;

using AlibabaCloud.OpenApiUtil;

using tests.Models;

using Tea;

using Xunit;
using System.Text;

namespace tests
{
    public class ClientTest
    {
        [Fact]
        public void Test_Convert()
        {
            TestConvertModel model = new TestConvertModel
            {
                RequestId = "test",
                Dict = new Dictionary<string, object>
                { { "key", "value" },
                { "testKey", "testValue" }
                },
                NoMap = 1,
                SubModel = new TestConvertModel.TestConvertSubModel
                {
                Id = 2,
                RequestId = "subTest"
                }
            };

            TestConvertMapModel mapModel = new TestConvertMapModel();
            Client.Convert(model, mapModel);
            Assert.Equal("test", mapModel.RequestId);
            Assert.Equal(0, mapModel.ExtendId);
            Assert.Equal(2, mapModel.SubModel.Id);
        }

        [Fact]
        public void Test_GetEndpoint()
        {
            Assert.Equal("test", Client.GetEndpoint("test", false, ""));

            Assert.Equal("test-internal.endpoint", Client.GetEndpoint("test.endpoint", false, "internal"));

            Assert.Equal("oss-accelerate.aliyuncs.com", Client.GetEndpoint("test", true, "accelerate"));
        }

        [Fact]
        public void Test_GetStringToSign()
        {
            TeaRequest teaRequestEmpty = new TeaRequest();
            teaRequestEmpty.Method = "GET";
            teaRequestEmpty.Pathname = "Pathname";
            Dictionary<string, string> headersEmpty = new Dictionary<string, string>();
            teaRequestEmpty.Headers = headersEmpty;
            Dictionary<string, string> querysEmpty = new Dictionary<string, string>();
            teaRequestEmpty.Query = querysEmpty;
            Assert.NotNull(Client.GetStringToSign(teaRequestEmpty));
            Assert.NotNull(Client.GetROASignature(Client.GetStringToSign(teaRequestEmpty), "accessKeySecret"));

            TeaRequest teaRequest = new TeaRequest();
            teaRequest.Method = "GET";
            teaRequest.Pathname = "/";
            Dictionary<string, string> headers = new Dictionary<string, string>();
            headers.Add("accept", "application/json");
            teaRequest.Headers = headers;
            Assert.Equal("GET\napplication/json\n\n\n\n/", Client.GetStringToSign(teaRequest));

            teaRequest.Headers.Add("content-md5", "md5");
            teaRequest.Headers.Add("content-type", "application/json");
            teaRequest.Headers.Add("date", "date");
            Assert.Equal("GET\napplication/json\nmd5\napplication/json\ndate\n/", Client.GetStringToSign(teaRequest));

            teaRequest.Headers.Add("x-acs-custom-key", "any value");
            Assert.Equal("GET\napplication/json\nmd5\napplication/json\ndate\nx-acs-custom-key:any value\n/", Client.GetStringToSign(teaRequest));

            Dictionary<string, string> query = new Dictionary<string, string>();
            query.Add("emptyKey", "");
            query.Add("key", "val ue with space");
            teaRequest.Query = query;
            Assert.Equal("GET\napplication/json\nmd5\napplication/json\ndate\nx-acs-custom-key:any value\n/?key=val ue with space", Client.GetStringToSign(teaRequest));
        }

        [Fact]
        public void Test_GetSignature()
        {
            Assert.Equal("OmuTAr79tpI6CRoAdmzKRq5lHs0=", Client.GetROASignature("stringtosign", "secret"));
        }

        [Fact]
        public void Test_ToForm()
        {
            Assert.Empty(Client.ToForm(null));

            Dictionary<string, object> dict = new Dictionary<string, object>();
            Assert.Empty(Client.ToForm(dict));

            Dictionary<string, object> dicObj = new Dictionary<string, object>();
            dicObj.Add("test", "test");
            dicObj.Add("key", "value");
            dicObj.Add("null", null);
            Dictionary<string, object> subDict = new Dictionary<string, object>();
            subDict.Add("subKey", "subValue");
            subDict.Add("subTest", "subTest");
            subDict.Add("subListInt", new List<int> { 1, 2, 3 });
            subDict.Add("subNull", null);
            subDict.Add("subListDict", new List<Dictionary<string, object>>
            {
                new Dictionary<string, object> { { "a", "b" }, { "c", "d" } },
                new Dictionary<string, object> { { "e", "f" }, { "g", "h" } },
            });
            dicObj.Add("sub", subDict);
            List<object> listObj = new List<object>
            {
                new Dictionary<string, object> { { "a", "b" }, { "c", "d" } },
                5,
                new List<string> { "list1", "list2" }
            };

            dicObj.Add("slice", listObj);
            string result = Client.ToForm(dicObj);
            Assert.Equal("test=test&key=value&sub.subKey=subValue&sub.subTest=subTest&sub.subListInt.1=1&sub.subListInt.2=2&sub.subListInt.3=3&sub.subListDict.1.a=b&sub.subListDict.1.c=d&sub.subListDict.2.e=f&sub.subListDict.2.g=h&slice.1.a=b&slice.1.c=d&slice.2=5&slice.3.1=list1&slice.3.2=list2", result);

        }

        [Fact]
        public void Test_GetTimestamp()
        {
            Assert.NotNull(Client.GetTimestamp());

            Assert.Contains("T", Client.GetTimestamp());

            Assert.Contains("Z", Client.GetTimestamp());
        }

        [Fact]
        public void Test_GetRPCSignature()
        {
            Dictionary<string, string> query = new Dictionary<string, string>
            { { "query", "test" },
                { "body", "test" },
            };
            string result = Client.GetRPCSignature(query, "GET", "secret");
            Assert.Equal("XlUyV4sXjOuX5FnjUz9IF9tm5rU=", result);
        }

        [Fact]
        public void Test_ArrayToStringWithSpecifiedStyle()
        {
            Assert.Empty(Client.ArrayToStringWithSpecifiedStyle(null, null, null));

            string prefix = "test";
            string style = "repeatList";
            List<string> list = new List<string>();
            list.Add("test");
            Assert.Equal("test.1=test", Client.ArrayToStringWithSpecifiedStyle(list, prefix, style));

            style = "simple";
            list.Add("testStyle");
            Assert.Equal("test,testStyle", Client.ArrayToStringWithSpecifiedStyle(list, prefix, style));

            style = "spaceDelimited";
            Assert.Equal("test testStyle", Client.ArrayToStringWithSpecifiedStyle(list, prefix, style));

            style = "pipeDelimited";
            Assert.Equal("test|testStyle", Client.ArrayToStringWithSpecifiedStyle(list, prefix, style));

            style = "json";
            Assert.Equal("[\"test\",\"testStyle\"]", Client.ArrayToStringWithSpecifiedStyle(list, prefix, style));

            style = "null";
            Assert.Empty(Client.ArrayToStringWithSpecifiedStyle(list, prefix, style));
        }

        [Fact]
        public void Test_ParseToMap()
        {
            Assert.Null(Client.ParseToMap(null));

            TestConvertMapModel model = new TestConvertMapModel
            {
                RequestId = "requestId",
                Dict = new Dictionary<string, object>
                { { "key", "value" }
                },
                SubModel = new TestConvertMapModel.TestConvertSubModel
                {
                RequestId = "sub"
                }
            };

            var dicModel = Client.ParseToMap(model);

            Assert.Equal("requestId", dicModel["RequestId"]);
            Assert.Equal("value", ((Dictionary<string, object>) dicModel["Dict"]) ["key"]);
            Assert.Equal("sub", ((Dictionary<string, object>) dicModel["SubModel"]) ["RequestId"]);

            Dictionary<string, object> dic = new Dictionary<string, object>
            { 
                { "model", model }
            };

            var dicMap = Client.ParseToMap(dic);

            Assert.Equal(dicModel, dicMap["model"]);
        }


        [Fact]
        public void Test_HexEncode()
        {
            byte[] test = Encoding.UTF8.GetBytes("test");
            var res = Client.HexEncode(Client.Hash(test, "ACS3-HMAC-SHA256"));
            Assert.Equal("9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08", res);

            res = Client.HexEncode(Client.Hash(test, "ACS3-RSA-SHA256"));
            Assert.Equal("9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08", res);

            res = Client.HexEncode(Client.Hash(test, "ACS3-HMAC-SM3"));
            Assert.Equal("55e12e91650d2fec56ec74e1d3e4ddbfce2ef3a65890c2a19ecf88a307e76a23", res);

            res = Client.HexEncode(Client.Hash(test, "ACS3-HM-SM3"));
            Assert.Equal("", res);

        }

        [Fact]
        public void Test_GetEncodePath()
        {
            var res = Client.GetEncodePath("/path/ test");
            Assert.Equal("/path/%20test", res);
        }

        [Fact]
        public void Test_GetAuthorization()
        {
            var query = new Dictionary<string, string>
            {
                { "test", "ok" },
                { "empty", "" }
            };

            var headers = new Dictionary<string, string>
            {
                { "x-acs-test", "http" },
                { "x-acs-TEST", "https"}
            };

            var req = new TeaRequest
            {
                Query = query,
                Headers = headers
            };

            var res = Client.GetAuthorization(req, "ACS3-HMAC-SHA256", "55e12e91650d2fec56ec74e1d3e4ddbfce2ef3a65890c2a19ecf88a307e76a23", "acesskey", "secret");
            Assert.Equal("ACS3-HMAC-SHA256 Credential=acesskey,SignedHeaders=x-acs-test,Signature=da772425f29289d3460d5fc961455d40c5e8c6afd0888b78a910c991e6a14846", res);
        }

        [Fact]
        public void Test_SignatureMethod()
        {
            string priKey = @"MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAKzSQmrnH0YnezZ9
8NK50WjMuci0hgGVcSthIZOTWMIySznY9Jj1hlvek7W0uYagtFHz03BHQnHAb5Xs
0DZm0Sj9+5r79GggwEzTJDYEsLyFwXM3ZOIxqxL4sRg94MHsa81M9NXGHMyMvvff
QTn1OBVLTVz5jgJ48foMn7j7r9kRAgMBAAECgYEAnZppw3/ef2XF8Z3Mnv+iP0Zk
LuqiQpN8TykXK7P1/7NJ8wktlshhrSo/3jdf8axghVQsgHob2Ay8Nidugg4lsxIL
AUBHvfQsQp1MAWvxslsVj+ddw01MQnt8kHmC/qhok+YuNqqAGBcoD6cthRUjEri6
hfs599EfPs2DcWW06qECQQDfNqUUhcDQ/SQHRhfY9UIlaSEs2CVagDrSYFG1wyG+
PXDSMes9ZRHsvVVBmNGmtUTg/jioTU3yuPsis5s9ppbVAkEAxjTAQxv5lBBm/ikM
TzPShljxDZnXh6lKWG9gR1p5fKoQTzLyyhHzkBSFe848sMm68HWCX2wgIpQLHj0G
ccYPTQJAduMKBeY/jpBlkiI5LWtj8b0O2G2/Z3aI3ehDXQYzgLoEz0+bNbYRWAB3
2lpkv+AocZW1455Y+ACichcrhiimiQJAW/6L5hoL4u8h/oFq1zAEXJrXdyqaYLrw
aM947mVN0dDVNQ0+pw9h7tO3iNkWTi+zdnv0APociDASYPyOCyyUWQJACMNRM1/r
boXuKfMmVjmmz0XhaDUC/JkqSwIiaZi+47M21e9BTp1218NA6VaPgJJHeJr4sNOn
Ysx+1cwXO5cuZg==";
            var res = Client.SignatureMethod("secret", "source", "ACS3-HMAC-SM3");
            string resStr = Client.HexEncode(res);
            Assert.Equal("b9ff646822f41ef647c1416fa2b8408923828abc0464af6706e18db3e8553da8", resStr);

            res = Client.SignatureMethod(priKey, "source", "ACS3-RSA-SHA256");
            Assert.Equal("a00b88ae04f651a8ab645e724949ff435bbb2cf9a37aa54323024477f8031f4e13dc948484c5c5a81ba53a55eb0571dffccc1e953c93269d6da23ed319e0f1ef699bcc9823a646574628ae1b70ed569b5a07d139dda28996b5b9231f5ba96141f0893deec2fbf54a0fa2c203b8ae74dd26f457ac29c873745a5b88273d2b3d12", Client.HexEncode(res));
        }
    }
}
