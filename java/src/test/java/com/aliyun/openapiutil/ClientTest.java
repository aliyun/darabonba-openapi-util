package com.aliyun.openapiutil;

import com.aliyun.tea.TeaRequest;
import com.aliyun.openapiutil.PaserObjectTest.*;
import org.junit.Assert;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.*;

public class ClientTest {

    @Test
    public void convertTest() throws Exception {
        SourceClass sourceClass = new SourceClass();
        TargetClass targetClass = new TargetClass();
        Client.convert(null, targetClass);
        Assert.assertNull(targetClass.test);
        Assert.assertNull(targetClass.empty);
        Assert.assertNull(targetClass.body);

        Client.convert(sourceClass, null);
        Assert.assertNull(targetClass.test);
        Assert.assertNull(targetClass.empty);
        Assert.assertNull(targetClass.body);

        Client.convert(sourceClass, targetClass);
        Assert.assertEquals("test", targetClass.test);
        Assert.assertNull(targetClass.empty);
        Assert.assertNull(targetClass.body);
    }

    @Test
    public void getStringToSignTest() throws Exception {
        TeaRequest request = new TeaRequest();
        String signature = Client.getStringToSign(request);
        Assert.assertEquals("GET\n\n\n\n\nnull", signature);

        new Client();
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("x-acs-security-token", "test");
        requestMap.put("x-acs-security-test", "test");
        requestMap.put("accept", "accept");
        requestMap.put("content-md5", "content-md5");
        requestMap.put("content-type", "content-type");
        requestMap.put("date", "date");
        requestMap.put("chineseTest", "汉语");
        requestMap.put("emptyTest", "");
        requestMap.put("spaceTest", "   ");
        request.headers = requestMap;
        request.query = requestMap;
        request.pathname = "/test";
        signature = Client.getStringToSign(request);
        Assert.assertEquals("GET\n" +
                        "accept\n" +
                        "content-md5\n" +
                        "content-type\n" +
                        "date\n" +
                        "x-acs-security-test:test\n" +
                        "x-acs-security-token:test\n" +
                        "/test?accept=accept&chineseTest=汉语&content-md5=content-md5&content-type=content-type&date=date&" +
                        "emptyTest&spaceTest=   &x-acs-security-test=test&x-acs-security-token=test",
                signature);
    }

    @Test
    public void getROASignatureTest() throws Exception {
        TeaRequest request = new TeaRequest();
        String stringToSign = Client.getStringToSign(request);
        String signature = Client.getROASignature(stringToSign, "sk");
        Assert.assertEquals("gyDNeagg5mpqgcAIZEfocyrreho=", signature);
    }

    @Test
    public void toFormTest() throws Exception {
        Map<String, Object> map = new HashMap<>();
        String result = Client.toForm(null);
        Assert.assertEquals("", result);

        result = Client.toForm(map);
        Assert.assertEquals("", result);

        map.put("form", "test");
        map.put("param", "test");
        map.put("nullTest", null);
        result = Client.toForm(map);
        Assert.assertEquals("form=test&param=test", result);
    }

    @Test
    public void getTimeStampTest() throws Exception {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(new SimpleTimeZone(0, "UTC"));
        Date date = df.parse(Client.getTimestamp());
        Assert.assertNotNull(date);
    }

    @Test
    public void queryTest() throws Exception {
        Map<String, Object> query = new HashMap<>();
        query.put("StringTest", "test");
        query.put("nullTest", null);
        query.put("IntegerTest", Integer.valueOf(1));

        List<Object> firstList = new ArrayList<>();
        firstList.add(Integer.valueOf(1));
        firstList.add(null);
        Map<String, Object> subMapInFirstList = new HashMap<>();
        subMapInFirstList.put("nullTest", null);
        subMapInFirstList.put("IntegerTest", Integer.valueOf(2));
        subMapInFirstList.put("StringTest", "test");
        firstList.add(subMapInFirstList);
        List<Object> secondList = new ArrayList<>();
        secondList.add(Integer.valueOf(1));
        secondList.add(null);
        firstList.add(secondList);
        query.put("list", firstList);


        Map<String, Object> firstMap = new HashMap<>();
        Map<String, Object> subMapInFirstMap = new HashMap<>();
        subMapInFirstMap.put("nullTest", null);
        subMapInFirstMap.put("IntegerTest", Integer.valueOf(2));
        subMapInFirstMap.put("StringTest", "test");
        subMapInFirstList.put("nullTest", null);
        subMapInFirstList.put("IntegerTest", Integer.valueOf(2));
        subMapInFirstList.put("StringTest", "test");
        firstMap.put("firstMapMap", subMapInFirstMap);
        firstMap.put("firstMapList", secondList);
        firstMap.put("nullTest", null);
        firstMap.put("IntegerTest", Integer.valueOf(2));
        firstMap.put("StringTest", "test");
        query.put("map", firstMap);

        Map<String, String> result = Client.query(null);
        Assert.assertEquals(0, result.size());

        result = Client.query(query);
        Assert.assertEquals("test", result.get("StringTest"));
        Assert.assertNull(result.get("nullTest"));
        Assert.assertEquals("1", result.get("IntegerTest"));

        Assert.assertEquals("1", result.get("list.1"));
        Assert.assertNull(result.get("list.2"));
        Assert.assertEquals("1", result.get("IntegerTest"));
        Assert.assertEquals("2", result.get("list.3.IntegerTest"));
        Assert.assertNull(result.get("list.3.nulTest"));
        Assert.assertEquals("test", result.get("list.3.StringTest"));
        Assert.assertEquals("1", result.get("list.4.1"));

        Assert.assertEquals("1", result.get("map.firstMapList.1"));
        Assert.assertNull(result.get("map.nullTest"));
        Assert.assertEquals("2", result.get("map.IntegerTest"));
        Assert.assertEquals("test", result.get("map.StringTest"));
        Assert.assertNull(result.get("map.firstMapMap.nullTest"));
        Assert.assertEquals("2", result.get("map.firstMapMap.IntegerTest"));
        Assert.assertEquals("test", result.get("map.firstMapMap.StringTest"));

        SourceClass model = new SourceClass();
        query.put("model", model);
        result = Client.query(query);
        Assert.assertEquals("test", result.get("model.test"));

    }

    @Test
    public void getRPCSignatureTest() throws Exception {
        TeaRequest teaRequest = new TeaRequest();
        Map<String, String> map = new HashMap<>();
        map.put("query", "test");
        map.put("body", "test");
        teaRequest.query = map;
        String result = Client.getRPCSignature(map, "GET", "secret");
        Assert.assertEquals("XlUyV4sXjOuX5FnjUz9IF9tm5rU=", result);
    }

    @Test
    public void arrayToStringWithSpecifiedStyleTest() throws Exception {
        String result = Client.arrayToStringWithSpecifiedStyle(null, null, null);
        Assert.assertEquals("", result);

        String prefix = "test";
        String style = "repeatList";
        List emptyList = new ArrayList();
        List list = new ArrayList();
        list.add("test");
        list.add("symbol");
        result = Client.arrayToStringWithSpecifiedStyle(emptyList, prefix, style);
        Assert.assertEquals("", result);
        result = Client.arrayToStringWithSpecifiedStyle(list, prefix, style);
        Assert.assertEquals("test.1=test&&test.2=symbol", result);

        list.remove(1);
        style = "simple";
        result = Client.arrayToStringWithSpecifiedStyle(emptyList, prefix, style);
        Assert.assertEquals("", result);
        list.add("testStyle");
        result = Client.arrayToStringWithSpecifiedStyle(list, prefix, style);
        Assert.assertEquals("test,testStyle", result);

        style = "spaceDelimited";
        result = Client.arrayToStringWithSpecifiedStyle(emptyList, prefix, style);
        Assert.assertEquals("", result);
        result = Client.arrayToStringWithSpecifiedStyle(list, prefix, style);
        Assert.assertEquals("test testStyle", result);

        style = "pipeDelimited";
        result = Client.arrayToStringWithSpecifiedStyle(emptyList, prefix, style);
        Assert.assertEquals("", result);
        result = Client.arrayToStringWithSpecifiedStyle(list, prefix, style);
        Assert.assertEquals("test|testStyle", result);

        style = "json";
        result = Client.arrayToStringWithSpecifiedStyle(emptyList, prefix, style);
        Assert.assertEquals("[]", result);
        result = Client.arrayToStringWithSpecifiedStyle(list, prefix, style);
        Assert.assertEquals("[\"test\",\"testStyle\"]", result);

        style = "null";
        result = Client.arrayToStringWithSpecifiedStyle(emptyList, prefix, style);
        Assert.assertEquals("", result);
        result = Client.arrayToStringWithSpecifiedStyle(list, prefix, style);
        Assert.assertEquals("", result);
    }

    @Test
    public void parseToMapTest() {
        Assert.assertNull(Client.parseToMap(null));

        PaserObjectTest test = new PaserObjectTest();
        List<String> stringList = new ArrayList<>();
        stringList.add("listTest");

        Map<String, Object> stringObjectMap = new HashMap<>();
        stringObjectMap.put("mapTest", "test");

        SubType subType = test.new SubType();
        ListValue listValue = test.new ListValue();
        MapValue mapValue = test.new MapValue();
        Map<String, MapValue> mapValueMap = new HashMap<>();
        mapValueMap.put("mapValueTest", mapValue);
        listValue.map = mapValueMap;
        List<ListValue> listValues = new ArrayList<>();
        listValues.add(listValue);
        subType.list = listValues;
        test.subType = subType;

        Map<String, Object> result = Client.parseToMap(test);
        Map subMap = (Map) result.get("SubType");
        List subList = (List) subMap.get("List");
        Map subMapValue = (Map) subList.get(0);
        Map map = (Map) subMapValue.get("Map");
        Assert.assertEquals("string", ((Map) map.get("mapValueTest")).get("MapValueString"));
    }

    @Test
    public void getEndpointTest() {
        Assert.assertEquals("cc-internal.abc.com",
                Client.getEndpoint("cc.abc.com", false, "internal"));

        Assert.assertEquals("oss-accelerate.aliyuncs.com",
                Client.getEndpoint("", true, "accelerate"));

        Assert.assertEquals("test",
                Client.getEndpoint("test", true, "test"));
    }

    @Test
    public void hexEncodeTest() throws Exception {
        String result = Client.hexEncode(null);
        Assert.assertNull(result);

        result = Client.hexEncode("".getBytes(Client.UTF8));
        Assert.assertEquals("", result);

        byte[] hash = Client.hash("test".getBytes(Client.UTF8), "ACS3-HMAC-SHA256");
        result = Client.hexEncode(hash);
        Assert.assertEquals("9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08", result);

        hash = Client.hash("test".getBytes(Client.UTF8), "ACS3-RSA-SHA256");
        result = Client.hexEncode(hash);
        Assert.assertEquals("9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08", result);

        hash = Client.hash("test".getBytes(Client.UTF8), "ACS3-HMAC-SM3");
        result = Client.hexEncode(hash);
        Assert.assertEquals("55e12e91650d2fec56ec74e1d3e4ddbfce2ef3a65890c2a19ecf88a307e76a23", result);
    }

    @Test
    public void getEncodePathTest() throws Exception {
        String result = Client.getEncodePath("");
        Assert.assertEquals("", result);
        result = Client.getEncodePath("test");
        Assert.assertEquals("test", result);
        result = Client.getEncodePath("/");
        Assert.assertEquals("/", result);
        result = Client.getEncodePath("/path/ test");
        Assert.assertEquals("/path/%20test", result);
        result = Client.getEncodePath("/path/#test");
        Assert.assertEquals("/path/%23test", result);
        result = Client.getEncodePath("/path/\"test");
        Assert.assertEquals("/path/%22test", result);
    }

    @Test
    public void getEncodeParamTest() throws Exception {
        String result = Client.getEncodeParam("a/b/c/ test");
        Assert.assertEquals("a%2Fb%2Fc%2F%20test", result);
    }

    @Test
    public void getAuthorizationTest() throws Exception {
        Map<String, String> query = new HashMap<>();
        query.put("test", "ok");
        query.put("empty", "");

        Map<String, String> headers = new HashMap<>();
        headers.put("x-acs-test", "http");
        headers.put("x-acs-TEST", "https");

        TeaRequest req = new TeaRequest();
        req.query = query;
        req.headers = headers;

        String auth = Client.getAuthorization(req, "ACS3-HMAC-SHA256",
                "55e12e91650d2fec56ec74e1d3e4ddbfce2ef3a65890c2a19ecf88a307e76a23", "acesskey", "secret");
        Assert.assertEquals("ACS3-HMAC-SHA256 Credential=acesskey,SignedHeaders=x-acs-test,Signature=02e81f9f3cc8839151b0c7278024cbc4bfc9fa786085a0b8305f825f17b5dae7", auth);
    }

    @Test
    public void signatureMethodTest() throws Exception {

        String priKey = "-----BEGIN RSA PRIVATE KEY-----\nMIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAKzSQmrnH0YnezZ9" +
                "8NK50WjMuci0hgGVcSthIZOTWMIySznY9Jj1hlvek7W0uYagtFHz03BHQnHAb5Xs" +
                "0DZm0Sj9+5r79GggwEzTJDYEsLyFwXM3ZOIxqxL4sRg94MHsa81M9NXGHMyMvvff" +
                "QTn1OBVLTVz5jgJ48foMn7j7r9kRAgMBAAECgYEAnZppw3/ef2XF8Z3Mnv+iP0Zk" +
                "LuqiQpN8TykXK7P1/7NJ8wktlshhrSo/3jdf8axghVQsgHob2Ay8Nidugg4lsxIL" +
                "AUBHvfQsQp1MAWvxslsVj+ddw01MQnt8kHmC/qhok+YuNqqAGBcoD6cthRUjEri6" +
                "hfs599EfPs2DcWW06qECQQDfNqUUhcDQ/SQHRhfY9UIlaSEs2CVagDrSYFG1wyG+" +
                "PXDSMes9ZRHsvVVBmNGmtUTg/jioTU3yuPsis5s9ppbVAkEAxjTAQxv5lBBm/ikM" +
                "TzPShljxDZnXh6lKWG9gR1p5fKoQTzLyyhHzkBSFe848sMm68HWCX2wgIpQLHj0G" +
                "ccYPTQJAduMKBeY/jpBlkiI5LWtj8b0O2G2/Z3aI3ehDXQYzgLoEz0+bNbYRWAB3" +
                "2lpkv+AocZW1455Y+ACichcrhiimiQJAW/6L5hoL4u8h/oFq1zAEXJrXdyqaYLrw" +
                "aM947mVN0dDVNQ0+pw9h7tO3iNkWTi+zdnv0APociDASYPyOCyyUWQJACMNRM1/r" +
                "boXuKfMmVjmmz0XhaDUC/JkqSwIiaZi+47M21e9BTp1218NA6VaPgJJHeJr4sNOn" +
                "Ysx+1cwXO5cuZg==-----END RSA PRIVATE KEY-----\n\r\r";

        byte[] signature = Client.SignatureMethod(null,null,null);
        Assert.assertNull(Client.hexEncode(signature));

        signature = Client.SignatureMethod("","secret","ACS3-HMAC-SM3");
        Assert.assertEquals("71e9db0344cd62427ccb824234214e14a0a54fe80adfb46bd12453270961dd5b", Client.hexEncode(signature));

        signature = Client.SignatureMethod("source", "secret", "ACS3-HMAC-SM3");
        Assert.assertEquals("b9ff646822f41ef647c1416fa2b8408923828abc0464af6706e18db3e8553da8", Client.hexEncode(signature));

        signature = Client.SignatureMethod("source", priKey, "ACS3-RSA-SHA256");
        Assert.assertEquals("a00b88ae04f651a8ab645e724949ff435bbb2cf9a37aa54323024477f8031f4e13dc948484c5c5a81ba53a55eb0571dffccc1e953c93269d6da23ed319e0f1ef699bcc9823a646574628ae1b70ed569b5a07d139dda28996b5b9231f5ba96141f0893deec2fbf54a0fa2c203b8ae74dd26f457ac29c873745a5b88273d2b3d12", Client.hexEncode(signature));
    }

}