package com.apigateway.baseclient;

import com.aliyun.tea.TeaRequest;
import com.aliyun.tea.TeaResponse;
import com.aliyun.tea.utils.StringUtils;
import com.google.gson.Gson;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;

public class BaseClient {
    protected String _appKey;
    protected String _stage;
    protected String _token;
    protected String _domain;
    protected String _appSecret;
    private static final String[] moveList = new String[]{
            "x-ca-signature", "x-ca-signature-headers", "accept", "content-md5", "content-type", "date", "host", "token"};


    public BaseClient(Map<String, Object> map) {
        this._appKey = (String) map.get("appKey");
        this._stage = (String) map.get("stage");
        this._token = (String) map.get("token");
        this._domain = (String) map.get("domain");
        this._appSecret = (String) map.get("appSecret");
    }

    protected Number _defaultNumber(Integer maxAttempts, long defaultNumber) {
        if (maxAttempts != null && maxAttempts >= 0) {
            return maxAttempts;
        }
        return defaultNumber;
    }


    protected String _default(String backoffPolicy, String no) {
        return StringUtils.isEmpty(backoffPolicy) ? no : backoffPolicy;
    }

    protected String _getHost() {
        return this._domain;
    }

    protected String _getDate() {
        SimpleDateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        return df.format(new Date());
    }

    protected String _getTimestamp() {
        return String.valueOf(System.currentTimeMillis());
    }

    protected String _getUUID() {
        return UUID.randomUUID().toString();
    }

    protected String _toJSONString(Map<String, Object> map) {
        return new Gson().toJson(map);
    }

    protected String _toForm(Map<String, Object> map) throws UnsupportedEncodingException {
        if (null == map || map.size() <= 0) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (first) {
                first = false;
            } else {
                result.append("&");
            }
            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(String.valueOf(entry.getValue()), "UTF-8"));
        }
        return result.toString();
    }

    protected String _getContentMD5(String bodyStr) throws NoSuchAlgorithmException, IOException {
        if (StringUtils.isEmpty(bodyStr)) {
            return "";
        }
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] result = md.digest(bodyStr.getBytes(TeaRequest.URL_ENCODING));
        return Base64.getEncoder().encodeToString(result);
    }

    protected String _getSignature(TeaRequest request) throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {
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
        byte[] keyBytes = this._appSecret.getBytes(TeaRequest.URL_ENCODING);
        hmacSha256.init(new SecretKeySpec(keyBytes, 0, keyBytes.length, "HmacSHA256"));
        String sign = new String(Base64.getEncoder().encode(hmacSha256.doFinal(stringToSign.getBytes(TeaRequest.URL_ENCODING))));
        return sign;
    }

    private String getSignHeader(TeaRequest request) {
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

    private String getUrl(TeaRequest request) {
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

    protected boolean _isStatusCode(TeaResponse response_, int code) {
        return response_.statusCode == code;
    }

    protected Map<String, Object> _readAsJSON(TeaResponse response_) throws IOException {
        String body = response_.getResponseBody();
        Gson gson = new Gson();
        Map<String, Object> map = gson.fromJson(body, Map.class);
        return map;
    }

    public boolean _isFail(TeaResponse resp) {
        return resp.statusCode < 200 || resp.statusCode >= 300;
    }
}
