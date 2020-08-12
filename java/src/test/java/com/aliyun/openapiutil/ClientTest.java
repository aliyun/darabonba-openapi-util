package com.aliyun.openapiutil;

import com.aliyun.tea.TeaRequest;
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
                        "/test?accept=accept&content-md5=content-md5&content-type=content-type&date=date&" +
                        "emptyTest&spaceTest&x-acs-security-test=test&x-acs-security-token=test",
                signature);
    }

    @Test
    public void getROASignatureTest() throws Exception {
        Assert.assertEquals("Ioag9x03doHFkSCveh2h1RE+hsM=",
                Client.getROASignature("test", "sk"));
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
    public void arrayToStringWithSpecifiedStyleTest() throws Exception{
        String result = Client.arrayToStringWithSpecifiedStyle(null, null, null);
        Assert.assertEquals("", result);

        String prefix = "test";
        String style = "repeatList";
        List list = new ArrayList();
        list.add("test");
        result = Client.arrayToStringWithSpecifiedStyle(list, prefix, style);
        Assert.assertEquals("test.1=test", result);

        style = "simple";
        list.add("testStyle");
        result = Client.arrayToStringWithSpecifiedStyle(list, prefix, style);
        Assert.assertEquals("test,testStyle", result);

        style = "spaceDelimited";
        result = Client.arrayToStringWithSpecifiedStyle(list, prefix, style);
        Assert.assertEquals("test testStyle", result);

        style = "pipeDelimited";
        result = Client.arrayToStringWithSpecifiedStyle(list, prefix, style);
        Assert.assertEquals("test|testStyle", result);

        style = "json";
        result = Client.arrayToStringWithSpecifiedStyle(list, prefix, style);
        Assert.assertEquals("[\"test\",\"testStyle\"]", result);

        style = "null";
        result = Client.arrayToStringWithSpecifiedStyle(list, prefix, style);
        Assert.assertEquals("", result);
    }
}