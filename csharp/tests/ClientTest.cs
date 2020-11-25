using System.Collections.Generic;

using AlibabaCloud.OpenApiUtil;

using tests.Models;

using Tea;

using Xunit;

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
    }
}
