package com.apigateway.baseclient;

import com.aliyun.tea.TeaRequest;
import com.aliyun.tea.TeaResponse;
import com.aliyun.tea.utils.StringUtils;
import com.google.gson.Gson;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
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

    protected long _readTimeout;
    protected long _connectTimeout;
    protected String _localAddr;
    protected String _httpProxy;
    protected String _httpsProxy;
    protected String _noProxy;
    protected long _maxIdleConns;
    protected String _protocol;

    private static final String[] moveList = new String[]{
            "x-ca-signature", "x-ca-signature-headers", "accept", "content-md5", "content-type", "date", "host", "token"};


    public BaseClient(Map<String, Object> map) {
        this._appKey = (String) map.get("appKey");
        this._stage = (String) map.get("stage");
        this._token = (String) map.get("token");
        this._domain = (String) map.get("domain");
        this._appSecret = (String) map.get("appSecret");
        this._protocol = map.get("protocol") == null ? "http" : (String) map.get("protocol");
        this._noProxy = (String) map.get("noProxy");
        this._httpsProxy = (String) map.get("httpsProxy");
        this._httpProxy = (String) map.get("httpProxy");
        this._localAddr = (String) map.get("localAddr");
        this._readTimeout = map.get("readTimeout") == null ? 10000 : Long.parseLong(String.valueOf(map.get("readTimeout")));
        this._connectTimeout = map.get("connectTimeout") == null ? 5000 : Long.parseLong(String.valueOf(map.get("connectTimeout")));
        this._maxIdleConns = map.get("maxIdleConns") == null ? 0 : Long.parseLong(String.valueOf(map.get("maxIdleConns")));
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

    protected String _getContentMD5(Map<String, Object> map) throws NoSuchAlgorithmException, IOException {
        if (null == map || map.size() <= 0) {
            return "";
        }
        String bodyStr = new Gson().toJson(map);
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

    protected boolean _equal(String bodystr, String type) {
        if (StringUtils.isEmpty(bodystr) || StringUtils.isEmpty(type)) {
            return false;
        }
        return type.equals(bodystr);
    }

    protected String _toForm(Map<String, ?> map) throws UnsupportedEncodingException {
        if (null == map) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, ?> entry : map.entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }
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

    protected boolean _notNull(Map<String, ?> map) {
        if (null == map) {
            return false;
        }
        return map.size() > 0;
    }

    protected Map<String, String> _toQuery(Map<String, ?> map) {
        Map<String, String> query = new HashMap<>();
        if (null == map) {
            return query;
        }
        for (Map.Entry<String, ?> entry : map.entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }
            query.put(entry.getKey(), String.valueOf(entry.getValue()));
        }
        return query;
    }

    protected boolean _isFail(TeaResponse response) {
        if (null == response) {
            return false;
        }
        if (200 > response.statusCode || 300 <= response.statusCode) {
            return true;
        }
        return false;
    }
}
