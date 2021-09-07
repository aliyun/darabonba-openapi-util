// This file is auto-generated, don't edit it. Thanks.
package com.aliyun.openapiutil;

import com.aliyun.tea.*;
import com.aliyun.tea.utils.StringUtils;
import com.google.gson.Gson;
import org.bouncycastle.crypto.digests.SM3Digest;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.*;


public class Client {

    public final static String SEPARATOR = "&";
    public final static String URL_ENCODING = "UTF-8";
    public static final String ALGORITHM_NAME = "HmacSHA1";
    public static final String HASH_SHA256 = "SHA-256";
    public static final String HASH_SM3 = "SM3";
    public static final String HMAC_SHA256 = "ACS3-HMAC-SHA256";
    public static final String RSA_SHA256 = "ACS3-RSA-SHA256";
    public static final String HMAC_SM3 = "ACS3-HMAC-SM3";
    public static final String UTF8 = "UTF-8";
    public static final String PEM_BEGIN = "-----BEGIN RSA PRIVATE KEY-----\n";
    public static final String PEM_END = "\n-----END RSA PRIVATE KEY-----";

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
        if (request == null) {
            return "";
        }
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
        if (headers == null) {
            return "";
        }
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

    protected static Map<String, String> getCanonicalizedHeadersMap(Map<String, String> headers) {
        Map<String, String> result = new HashMap<>();
        if (headers == null) {
            return result;
        }
        String prefix = "x-acs-";
        Set<String> keys = headers.keySet();
        List<String> canonicalizedKeys = new ArrayList<>();
        Map<String, String> valueMap = new HashMap<>();
        for (String key : keys) {
            String lowerKey = key.toLowerCase();
            if (lowerKey.startsWith(prefix) || lowerKey.equals("host")
                    || lowerKey.equals("content-type")) {
                if (!canonicalizedKeys.contains(lowerKey)) {
                    canonicalizedKeys.add(lowerKey);
                }
                valueMap.put(lowerKey, headers.get(key).trim());
            }
        }
        String[] canonicalizedKeysArray = canonicalizedKeys.toArray(new String[canonicalizedKeys.size()]);
        String signedHeaders = StringUtils.join(";", Arrays.asList(canonicalizedKeysArray));
        Arrays.sort(canonicalizedKeysArray);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < canonicalizedKeysArray.length; i++) {
            String key = canonicalizedKeysArray[i];
            sb.append(key);
            sb.append(":");
            sb.append(valueMap.get(key));
            sb.append("\n");
        }
        result.put("canonicalHeaders", sb.toString());
        result.put("signedHeaders", signedHeaders);
        return result;
    }

    protected static String getCanonicalizedQueryString(StringBuilder sb, Map<String, String> query, String[] keys) throws Exception {
        if (query == null || query.size() == 0) {
            return "";
        }
        if (keys == null || keys.length == 0) {
            return "";
        }
        if (sb == null) {
            sb = new StringBuilder();
        }
        Arrays.sort(keys);
        String key;
        String value;
        for (int i = 0; i < keys.length; i++) {
            key = keys[i];
            sb.append(percentEncode(key));
            value = query.get(key);
            sb.append("=");
            if (!StringUtils.isEmpty(value)) {
                sb.append(percentEncode(value));
            }
            sb.append(SEPARATOR);
        }
        return sb.deleteCharAt(sb.length() - 1).toString();
    }

    protected static String getCanonicalizedQueryStringForROA(StringBuilder sb, Map<String, String> query, String[] keys) throws Exception {
        if (query == null || query.size() == 0) {
            return "";
        }
        if (keys == null || keys.length == 0) {
            return "";
        }
        if (sb == null) {
            sb = new StringBuilder();
        }
        Arrays.sort(keys);
        String key;
        String value;
        for (int i = 0; i < keys.length; i++) {
            key = keys[i];
            sb.append(key);
            value = query.get(key);
            if (!StringUtils.isEmpty(value)) {
                sb.append("=");
                sb.append(value);
            }
            sb.append(SEPARATOR);
        }
        return sb.deleteCharAt(sb.length() - 1).toString();
    }

    protected static String getCanonicalizedResource(Map<String, String> query) throws Exception {
        if (query == null || query.size() == 0) {
            return "";
        }
        String[] keys = query.keySet().toArray(new String[query.size()]);
        StringBuilder result = new StringBuilder();
        return getCanonicalizedQueryString(result, query, keys);
    }

    protected static String getCanonicalizedResource(String pathname, Map<String, String> query) throws Exception {
        if (query == null || query.size() == 0) {
            return pathname;
        }
        String[] keys = query.keySet().toArray(new String[query.size()]);
        if (pathname == null || keys.length <= 0) {
            return pathname;
        }
        StringBuilder result = new StringBuilder(pathname);
        result.append("?");
        return getCanonicalizedQueryStringForROA(result, query, keys);
    }

    /**
     * Get signature according to stringToSign, secret
     *
     * @param stringToSign the signed string
     * @param secret       accesskey secret
     * @return the signature
     */
    public static String getROASignature(String stringToSign, String secret) throws Exception {
        if (StringUtils.isEmpty(secret)) {
            return secret;
        }
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(new SecretKeySpec(secret.getBytes(UTF8), "HmacSHA1"));
        byte[] signData = mac.doFinal(stringToSign.getBytes(UTF8));
        return Base64.encodeBase64String(signData);
    }

    /**
     * Parse filter into a form string
     *
     * @param filter object
     * @return the string
     */
    public static String toForm(java.util.Map<String, ?> filter) throws Exception {
        return toFormWithSymbol(filter, SEPARATOR);
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
            result.append(URLEncoder.encode(entry.getKey(), UTF8));
            result.append("=");
            result.append(URLEncoder.encode(String.valueOf(entry.getValue()), UTF8));
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
            processObject(outMap, "", filter);
        }
        return outMap;
    }

    private static void processObject(Map<String, String> map, String key, Object value) throws UnsupportedEncodingException {
        if (null == value) {
            return;
        }
        if (value instanceof List) {
            List list = (List) value;
            for (int i = 0; i < list.size(); i++) {
                processObject(map, key + "." + (i + 1), list.get(i));
            }
        } else if (value instanceof TeaModel) {
            Map<String, Object> subMap = (Map<String, Object>) (((TeaModel) value).toMap());
            for (Map.Entry<String, Object> entry : subMap.entrySet()) {
                processObject(map, key + "." + (entry.getKey()), entry.getValue());
            }
        } else if (value instanceof Map) {
            Map<String, Object> subMap = (Map<String, Object>) value;
            for (Map.Entry<String, Object> entry : subMap.entrySet()) {
                processObject(map, key + "." + (entry.getKey()), entry.getValue());
            }
        } else {
            if (key.startsWith(".")) {
                key = key.substring(1);
            }
            if (value instanceof byte[]) {
                map.put(key, new String((byte[]) value, UTF8));
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
        if (signedParams == null || StringUtils.isEmpty(secret)) {
            return secret;
        }
        Map<String, String> queries = signedParams;
        String[] sortedKeys = queries.keySet().toArray(new String[]{});
        Arrays.sort(sortedKeys);
        StringBuilder canonicalizedQueryString = new StringBuilder();

        for (String key : sortedKeys) {
            if (StringUtils.isEmpty(queries.get(key))) {
                continue;
            }
            canonicalizedQueryString.append(SEPARATOR)
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
        return Base64.encodeBase64String(signData);

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
                Class clazz = array.getClass();
                List list = new ArrayList();
                if (List.class.isAssignableFrom(clazz)) {
                    list = (List) array;
                }
                if (list.size() > 0) {
                    if (TeaModel.class.isAssignableFrom(list.get(0).getClass())) {
                        List<TeaModel> teaModels = (List<TeaModel>) array;
                        List<Map<String, Object>> mapList = new ArrayList<>();
                        for (TeaModel teaModel : teaModels) {
                            mapList.add(teaModel.toMap());
                        }
                        return new Gson().toJson(mapList);
                    }
                }
                return new Gson().toJson(array);
            default:
                return "";
        }
    }

    private static String flatArray(List array, String sty) {
        if (array == null || array.size() <= 0 || sty == null) {
            return "";
        }
        String flag;
        if ("simple".equalsIgnoreCase(sty)) {
            flag = ",";
        } else if ("spaceDelimited".equalsIgnoreCase(sty)) {
            flag = " ";
        } else {
            flag = "|";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.size(); i++) {
            sb.append(array.get(i));
            sb.append(flag);
        }
        return sb.deleteCharAt(sb.length() - 1).toString();
    }

    public static Map<String, Object> parseToMap(Object o) {
        if (null == o) {
            return null;
        }
        return (Map<String, Object>) TeaModel.parseObject(o);
    }

    public static String getEndpoint(String endpoint, boolean useAccelerate, String endpointType) {
        if ("internal".equals(endpointType)) {
            String[] strs = endpoint.split("\\.");
            strs[0] += "-internal";
            endpoint = StringUtils.join(".", Arrays.asList(strs));
        }
        if (useAccelerate && "accelerate".equals(endpointType)) {
            return "oss-accelerate.aliyuncs.com";
        }
        return endpoint;
    }

    public static String hexEncode(byte[] raw) {
        if (raw == null) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < raw.length; i++) {
            String hex = Integer.toHexString(raw[i] & 0xFF);
            if (hex.length() < 2) {
                sb.append(0);
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    public static byte[] hash(byte[] raw, String signAlgorithm) throws Exception {
        if (signAlgorithm == null) {
            return null;
        }
        if (signAlgorithm.equals(HMAC_SHA256) || signAlgorithm.equals(RSA_SHA256)) {
            MessageDigest digest = MessageDigest.getInstance(HASH_SHA256);
            return digest.digest(raw);
        } else if (signAlgorithm.equals(HMAC_SM3)) {
            BouncyCastleProvider provider = new BouncyCastleProvider();
            MessageDigest digest = MessageDigest.getInstance(HASH_SM3, provider);
            return digest.digest(raw);
        }
        return null;
    }


    public static String getAuthorization(TeaRequest request, String signAlgorithm, String payload, String accessKey, String secret) throws Exception {
        if (request == null) {
            return null;
        }
        if (secret == null) {
            throw new Exception("Need secret!");
        }
        if (StringUtils.isEmpty(signAlgorithm)) {
            throw new Exception("Need signAlgorithm!");
        }
        String canonicalURI = request.pathname;
        if (canonicalURI == null || StringUtils.isEmpty(canonicalURI) || "".equals(canonicalURI.trim())) {
            canonicalURI = "/";
        }
        String method = request.method;
        Map<String, String> headers = request.headers;
        Map<String, String> query = request.query;
        Map<String, String> cannoicalHeaders = getCanonicalizedHeadersMap(headers);
        String signedHeaders = cannoicalHeaders.get("signedHeaders");
        String queryString = getCanonicalizedResource(query);
        StringBuilder sb = new StringBuilder(method);
        sb.append("\n").append(canonicalURI).append("\n").append(queryString).append("\n").append(cannoicalHeaders.get("canonicalHeaders"))
                .append("\n").append(signedHeaders).append("\n").append(payload);
        String hex = hexEncode(hash(sb.toString().getBytes(UTF8), signAlgorithm));
        String stringToSign = signAlgorithm + "\n" + hex;
        String signature = hexEncode(SignatureMethod(stringToSign, secret, signAlgorithm));
        String auth = signAlgorithm + " Credential=" + accessKey + ",SignedHeaders=" + signedHeaders
                + ",Signature=" + signature;
        return auth;
    }

    protected static String checkRSASecret(String secret) {
        if (secret != null) {
            if (secret.startsWith(PEM_BEGIN)) {
                secret = secret.replace(PEM_BEGIN, "");
            }
            while (secret.endsWith("\n") || secret.endsWith("\r")) {
                secret = secret.substring(0, secret.length() - 1);
            }
            if (secret.endsWith(PEM_END)) {
                secret = secret.replace(PEM_END, "");
            }
        }
        return secret;
    }

    public static byte[] SignatureMethod(String stringToSign, String secret, String signAlgorithm) throws Exception {
        if (stringToSign == null || secret == null || signAlgorithm == null) {
            return null;
        }
        byte[] bytes = null;
        if (signAlgorithm.equals(HMAC_SHA256)) {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            bytes = sha256_HMAC.doFinal(stringToSign.getBytes());
            return bytes;
        } else if (signAlgorithm.equals(RSA_SHA256)) {
            secret = checkRSASecret(secret);
            Signature rsaSign = Signature.getInstance("SHA256withRSA");
            KeyFactory kf = KeyFactory.getInstance("RSA");
            System.out.println(secret);
            byte[] keySpec = Base64.decodeBase64(secret);
            PrivateKey privateKey = kf.generatePrivate(new PKCS8EncodedKeySpec(keySpec));
            rsaSign.initSign(privateKey);
            rsaSign.update(stringToSign.getBytes(UTF8));
            bytes = rsaSign.sign();
        } else if (signAlgorithm.equals(HMAC_SM3)) {
            SecretKey key = new SecretKeySpec((secret).getBytes(UTF8), "HMAC-SM3");
            HMac mac = new HMac(new SM3Digest());
            bytes = new byte[mac.getMacSize()];
            byte[] inputBytes = stringToSign.getBytes(UTF8);
            mac.init(new KeyParameter(key.getEncoded()));
            mac.update(inputBytes, 0, inputBytes.length);
            mac.doFinal(bytes, 0);
        }
        return bytes;
    }

    public static String getEncodePath(String path) throws UnsupportedEncodingException {
        if (StringUtils.isEmpty(path) || "/".equals(path)) {
            return path;
        }
        String[] strs = path.split("/");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < strs.length; i++) {
            sb.append(percentEncode(strs[i]));
            sb.append("/");
        }
        return sb.deleteCharAt(sb.length() - 1).toString();
    }

    public static String getEncodeParam(String param) throws UnsupportedEncodingException {
        if (StringUtils.isEmpty(param)) {
            return param;
        }
        return percentEncode(param);
    }
}
