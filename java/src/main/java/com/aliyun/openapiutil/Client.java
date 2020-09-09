// This file is auto-generated, don't edit it. Thanks.
package com.aliyun.openapiutil;

import com.aliyun.tea.*;
import com.aliyun.tea.utils.StringUtils;
import com.google.gson.Gson;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;


public class Client {

    public final static String SEPARATOR = "&";
    public final static String URL_ENCODING = "UTF-8";
    public static final String ALGORITHM_NAME = "HmacSHA1";

    /**
     * Convert all params of body other than type of readable into content
     *
     * @param body    body Model
     * @param content content Model
     * @return void
     */
    public static void convert(TeaModel body, TeaModel content) throws Exception {
        if (body == null || content == null) {
            return;
        }
        Class bodyClass = body.getClass();
        Class contentClass = content.getClass();
        Field[] fields = bodyClass.getDeclaredFields();
        TeaModel teaModel = (TeaModel) bodyClass.newInstance();
        for (Field field : fields) {
            field.setAccessible(true);
            if (InputStream.class.isAssignableFrom(field.getType())) {
                continue;
            }
            field.set(teaModel, field.get(body));
        }
        Gson gson = new Gson();
        String jsonString = gson.toJson(teaModel);
        Object outPut = gson.fromJson(jsonString, contentClass);
        fields = outPut.getClass().getFields();
        for (Field field : fields) {
            field.setAccessible(true);
            field.set(content, field.get(outPut));
        }
    }

    /**
     * Get the string to be signed according to request
     *
     * @param request which contains signed messages
     * @return the signed string
     */
    public static String getStringToSign(TeaRequest request) throws Exception {
        String method = request.method;
        String pathname = request.pathname;
        Map<String, String> headers = request.headers;
        Map<String, String> query = request.query;
        String accept = headers.get("accept") == null ? "" : headers.get("accept");
        String contentMD5 = headers.get("content-md5") == null ? "" : headers.get("content-md5");
        String contentType = headers.get("content-type") == null ? "" : headers.get("content-type");
        String date = headers.get("date") == null ? "" : headers.get("date");
        String header = method + "\n" + accept + "\n" + contentMD5 + "\n" + contentType + "\n" + date + "\n";
        String canonicalizedHeaders = getCanonicalizedHeaders(headers);
        String canonicalizedResource = getCanonicalizedResource(pathname, query);
        String stringToSign = header + canonicalizedHeaders + canonicalizedResource;
        return stringToSign;
    }

    protected static String getCanonicalizedHeaders(Map<String, String> headers) {
        String prefix = "x-acs-";
        Set<String> keys = headers.keySet();
        List<String> canonicalizedKeys = new ArrayList<>();
        for (String key : keys) {
            if (key.startsWith(prefix)) {
                canonicalizedKeys.add(key);
            }
        }
        String[] canonicalizedKeysArray = canonicalizedKeys.toArray(new String[canonicalizedKeys.size()]);
        Arrays.sort(canonicalizedKeysArray);
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < canonicalizedKeysArray.length; i++) {
            String key = canonicalizedKeysArray[i];
            result.append(key);
            result.append(":");
            result.append(headers.get(key).trim());
            result.append("\n");
        }
        return result.toString();
    }

    protected static String getCanonicalizedResource(String pathname, Map<String, String> query) {
        String[] keys = query.keySet().toArray(new String[query.size()]);
        if (keys.length <= 0) {
            return pathname;
        }
        Arrays.sort(keys);
        StringBuilder result = new StringBuilder(pathname);
        result.append("?");
        String key;
        String value;
        for (int i = 0; i < keys.length; i++) {
            key = keys[i];
            result.append(key);
            value = query.get(key);
            if (!StringUtils.isEmpty(value) && !"".equals(value.trim())) {
                result.append("=");
                result.append(value);
            }
            result.append("&");
        }
        return result.deleteCharAt(result.length() - 1).toString();
    }

    /**
     * Get signature according to stringToSign, secret
     *
     * @param stringToSign the signed string
     * @param secret       accesskey secret
     * @return the signature
     */
    public static String getROASignature(String stringToSign, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(new SecretKeySpec(secret.getBytes("UTF-8"), "HmacSHA1"));
        byte[] signData = mac.doFinal(stringToSign.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(signData);
    }

    /**
     * Parse filter into a form string
     *
     * @param filter object
     * @return the string
     */
    public static String toForm(java.util.Map<String, ?> filter) throws Exception {
       return toFormWithSymbol(filter, "&");
    }

    private static String toFormWithSymbol(java.util.Map<String, ?> filter, String symbol) throws Exception {
        Map<String, String> map = query(filter);
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, ?> entry : map.entrySet()) {
            if (StringUtils.isEmpty(entry.getValue())) {
                continue;
            }
            if (first) {
                first = false;
            } else {
                result.append(symbol);
            }
            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(String.valueOf(entry.getValue()), "UTF-8"));
        }
        return result.toString();
    }

    /**
     * Get timestamp
     *
     * @return the timestamp string
     */
    public static String getTimestamp() throws Exception {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(new SimpleTimeZone(0, "UTC"));
        return df.format(new Date());
    }

    /**
     * Parse filter into a object which's type is map[string]string
     *
     * @param filter query param
     * @return the object
     */
    public static java.util.Map<String, String> query(java.util.Map<String, ?> filter) throws Exception {
        Map<String, String> outMap = new HashMap<>();
        if (null != filter) {
            processeObject(outMap, "", filter);
        }
        return outMap;
    }

    private static void processeObject(Map<String, String> map, String key, Object value) throws UnsupportedEncodingException {
        if (StringUtils.isEmpty(value)) {
            return;
        }
        if (value instanceof List) {
            List list = (List) value;
            for (int i = 0; i < list.size(); i++) {
                processeObject(map, key + "." + (i + 1), list.get(i));
            }
        } else if (value instanceof Map) {
            Map<String, Object> subMap = (Map<String, Object>) value;
            for (Map.Entry<String, Object> entry : subMap.entrySet()) {
                processeObject(map, key + "." + (entry.getKey()), entry.getValue());
            }
        } else {
            if (key.startsWith(".")) {
                key = key.substring(1);
            }
            if (value instanceof byte[]) {
                map.put(key, new String((byte[]) value, "UTF-8"));
            } else {
                map.put(key, String.valueOf(value));
            }
        }
    }


    /**
     * Get signature according to signedParams, method and secret
     *
     * @param signedParams params which need to be signed
     * @param method       http method e.g. GET
     * @param secret       AccessKeySecret
     * @return the signature
     */
    public static String getRPCSignature(java.util.Map<String, String> signedParams, String method, String secret) throws Exception {
        Map<String, String> queries = signedParams;
        String[] sortedKeys = queries.keySet().toArray(new String[]{});
        Arrays.sort(sortedKeys);
        StringBuilder canonicalizedQueryString = new StringBuilder();

        for (String key : sortedKeys) {
            if (StringUtils.isEmpty(queries.get(key))) {
                continue;
            }
            canonicalizedQueryString.append("&")
                    .append(percentEncode(key)).append("=")
                    .append(percentEncode(queries.get(key)));
        }
        StringBuilder stringToSign = new StringBuilder();
        stringToSign.append(method);
        stringToSign.append(SEPARATOR);
        stringToSign.append(percentEncode("/"));
        stringToSign.append(SEPARATOR);
        stringToSign.append(percentEncode(
                canonicalizedQueryString.toString().substring(1)));
        Mac mac = Mac.getInstance(ALGORITHM_NAME);
        mac.init(new SecretKeySpec((secret + SEPARATOR).getBytes(URL_ENCODING), ALGORITHM_NAME));
        byte[] signData = mac.doFinal(stringToSign.toString().getBytes(URL_ENCODING));
        return DatatypeConverter.printBase64Binary(signData);

    }

    public static String percentEncode(String value) throws UnsupportedEncodingException {
        return value != null ? URLEncoder.encode(value, URL_ENCODING).replace("+", "%20")
                .replace("*", "%2A").replace("%7E", "~") : null;
    }


    /**
     * Parse array into a string with specified style
     *
     * @param array  the array
     * @param prefix the prefix string
     * @return the string
     * @style specified style e.g. repeatList
     */
    public static String arrayToStringWithSpecifiedStyle(Object array, String prefix, String style) throws Exception {
        if (null == array) {
            return "";
        }
        switch (style) {
            case "repeatList":
                Map<String, Object> map = new HashMap<String, Object>();
                map.put(prefix, array);
                return toFormWithSymbol(map, "&&");
            case "simple":
            case "spaceDelimited":
            case "pipeDelimited":
                return flatArray((List) array, style);
            case "json":
                return new Gson().toJson(array);
            default:
                return "";
        }
    }

    private static String flatArray(List array, String sty) {
        List<String> strs = new ArrayList<String>();
        for (int i = 0; i < array.size(); i++) {
            strs.add(String.valueOf(array.get(i)));
        }
        if ("simple".equalsIgnoreCase(sty)) {
            return String.join(",", strs);
        } else if ("spaceDelimited".equalsIgnoreCase(sty)) {
            return String.join(" ", strs);
        } else {
            return String.join("|", strs);
        }
    }
}
