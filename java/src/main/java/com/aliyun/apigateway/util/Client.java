// This file is auto-generated, don't edit it. Thanks.
package com.aliyun.apigateway.util;

import com.aliyun.tea.*;
import com.aliyun.tea.utils.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.util.*;


public class Client {

    private static final String[] moveList = new String[]{
            "x-ca-signature", "x-ca-signature-headers", "user-agent", "accept", "content-md5", "content-type", "date", "host", "token"};

    public static String getSignature(TeaRequest request, String secret) throws Exception {
        String httpMethod = request.method;
        String accept = request.headers.get("accept");
        String contentMd5 = request.headers.get("content-md5") == null ? "" : request.headers.get("content-md5");
        String contentType = request.headers.get("content-type") == null ? "" : request.headers.get("content-type");
        String date = request.headers.get("date");
        String header = getSignHeader(request);
        String url = getUrl(request);
        String stringToSign = httpMethod + "\n" + accept + "\n" + contentMd5 + "\n" + contentType + "\n" +
                date + "\n" + header + "\n" + url;
        Mac hmacSha256 = Mac.getInstance("HmacSHA256");
        byte[] keyBytes = secret.getBytes(TeaRequest.URL_ENCODING);
        hmacSha256.init(new SecretKeySpec(keyBytes, 0, keyBytes.length, "HmacSHA256"));
        String sign = new String(Base64.getEncoder().encode(hmacSha256.doFinal(stringToSign.getBytes(TeaRequest.URL_ENCODING))));
        return sign;
    }

    private static String getSignHeader(TeaRequest request) {
        if (request.headers.size() <= 0) {
            return "";
        }
        Map<String, String> map = new HashMap<>();
        map.putAll(request.headers);
        for (String moveName : moveList) {
            map.remove(moveName);
        }
        Set<String> keySet = map.keySet();
        String[] keys = keySet.toArray(new String[]{});
        Arrays.sort(keys);
        StringBuilder stringBuilder = new StringBuilder();
        StringBuilder headerKeys = new StringBuilder();
        for (String key : keys) {
            headerKeys.append(key).append(",");
            stringBuilder.append(key).append(":").append(map.get(key) == null ? "" : map.get(key)).append("\n");
        }
        if (headerKeys.length() >= 1) {
            headerKeys.deleteCharAt(headerKeys.length() - 1);
        }
        if (stringBuilder.length() >= 1) {
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        }
        request.headers.put("x-ca-signature-headers", headerKeys.toString());
        return stringBuilder.toString();
    }

    private static String getUrl(TeaRequest request) {
        String pathName = request.pathname;
        Map<String, String> map = new HashMap<>();
        map.putAll(request.query);
        String[] keys = map.keySet().toArray(new String[]{});
        StringBuilder stringBuilder = new StringBuilder(pathName);
        if (map.size() > 0) {
            stringBuilder.append("?");
            for (String key : keys) {
                stringBuilder.append(key).append("=").append(map.get(key)).append("&");
            }
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        }
        return stringBuilder.toString();
    }

    public static java.util.Map<String, String> toQuery(java.util.Map<String, Object> filter) throws Exception {
        Map<String, String> query = new HashMap<>();
        if (null == filter) {
            return query;
        }
        for (Map.Entry<String, ?> entry : filter.entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }
            query.put(entry.getKey(), String.valueOf(entry.getValue()));
        }
        return query;
    }

    public static Boolean isFail(Integer code) throws Exception {
        if (null == code) {
            return false;
        }
        if (200 > code || 300 <= code) {
            return true;
        }
        return false;
    }

    public static String getContentMD5(String body) throws Exception {
        if (StringUtils.isEmpty(body)) {
            return body;
        }
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] result = md.digest(body.getBytes(TeaRequest.URL_ENCODING));
        return Base64.getEncoder().encodeToString(result);
    }
}
