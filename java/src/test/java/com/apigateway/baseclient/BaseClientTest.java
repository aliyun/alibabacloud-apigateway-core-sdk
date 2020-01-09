package com.apigateway.baseclient;

import com.aliyun.tea.TeaRequest;
import com.aliyun.tea.TeaResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import static org.mockito.Mockito.when;


public class BaseClientTest {
    private BaseClient client;

    @Before
    public void createClient() {
        client = new BaseClient(new HashMap<>());
    }

    @Test
    public void constructTest() {
        Map<String, Object> map = new HashMap<>();
        map.put("protocol", "https");
        map.put("readTimeout", 888);
        map.put("connectTimeout", 666);
        map.put("maxIdleConns", 111);
        client  = new BaseClient(map);
        Assert.assertEquals("https", client._protocol);
        Assert.assertEquals(888, client._readTimeout);
        Assert.assertEquals(666, client._connectTimeout);
        Assert.assertEquals(111, client._maxIdleConns);
    }

    @Test
    public void _defaultNumberTest() {
        Assert.assertEquals(6l, client._defaultNumber(null, 6L));
        Assert.assertEquals(6l, client._defaultNumber(-1, 6L));
        Assert.assertEquals(8, client._defaultNumber(8, 6L));
    }

    @Test
    public void _defaultTest() {
        Assert.assertEquals("default", client._default(null, "default"));
        Assert.assertEquals("test", client._default("test", "default"));
    }

    @Test
    public void _getHostTest() {
        client._domain = "test";
        Assert.assertEquals(client._domain, client._getHost());
    }

    @Test
    public void _getDateTest() throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        Assert.assertNotNull(df.parse(client._getDate()));
    }

    @Test
    public void _getTimestampTest() {
        long now = System.currentTimeMillis();
        long result = Long.parseLong(client._getTimestamp());
        Assert.assertTrue(result >= now);
    }

    @Test
    public void _getUUIDTest() {
        Assert.assertEquals(36, client._getUUID().length());
    }

    @Test
    public void _toJSONStringTest() {
        Map<String, Object> map = new HashMap<>();
        map.put("key", "value");
        Assert.assertEquals("{\"key\":\"value\"}", client._toJSONString(map));
    }

    @Test
    public void _getContentMD5Test() throws Exception {
        Map<String, Object> map = new HashMap<>();
        Assert.assertEquals("", client._getContentMD5(map));
        map.put("test", "test");
        Assert.assertEquals("", client._getContentMD5(null));
        Assert.assertEquals("govO+HY8G8YW4loGvkuQ/w==", client._getContentMD5(map));
    }

    @Test
    public void _getSignatureTest() throws Exception {
        TeaRequest request = new TeaRequest();
        request.method = "test";
        request.headers.put("accept", "json");
        request.headers.put("date", "now");
        request.pathname = "test";
        client._appSecret = "sk";
        client._appKey = "ak";
        Assert.assertEquals("lzttujz/sOhS2QSBBSZBq58ZNSxvSDdkWWlknQxnrt0=", client._getSignature(request));

        request.headers.put("content-md5", "md5");
        request.headers.put("content-type", "type");
        Assert.assertEquals("HcjhFWjGB9Xyitos6CHnJvwQoPQzdPUBgv5oUd0tdoA=", client._getSignature(request));
    }

    @Test
    public void getSignHeaderTest() throws Exception {
        Method getSignHeader = BaseClient.class.getDeclaredMethod("getSignHeader", TeaRequest.class);
        getSignHeader.setAccessible(true);
        TeaRequest request = new TeaRequest();
        String result = (String) getSignHeader.invoke(client, request);
        Assert.assertEquals("", result);

        request.headers.put("testKey", "value");
        request.headers.put("testNull", null);
        result = (String) getSignHeader.invoke(client, request);
        Assert.assertEquals("testKey:value\ntestNull:", result);
    }

    @Test
    public void getUrlTest() throws Exception {
        Method getUrl = BaseClient.class.getDeclaredMethod("getUrl", TeaRequest.class);
        getUrl.setAccessible(true);
        TeaRequest request = new TeaRequest();
        request.pathname = "test";
        request.query.put("testKey", "value");
        Assert.assertEquals("test?testKey=value", getUrl.invoke(client, request));
    }

    @Test
    public void _isStatusCodeTest() {
        TeaResponse teaResponse = new TeaResponse();
        teaResponse.statusCode = 200;
        Assert.assertFalse(client._isStatusCode(teaResponse, 201));
        Assert.assertTrue(client._isStatusCode(teaResponse, 200));
    }

    @Test
    public void _readAsJSONTest() throws Exception {
        TeaResponse teaResponse = Mockito.mock(TeaResponse.class);
        when(teaResponse.getResponseBody()).thenReturn("{\"key\":\"value\"}");
        Assert.assertEquals("value", client._readAsJSON(teaResponse).get("key"));
    }

    @Test
    public void _isFailTest() throws Exception {
        Assert.assertFalse(client._isFail(null));
        TeaResponse teaResponse = new TeaResponse();
        teaResponse.statusCode = 100;
        Assert.assertTrue(client._isFail(teaResponse));

        teaResponse.statusCode = 200;
        Assert.assertFalse(client._isFail(teaResponse));

        teaResponse.statusCode = 400;
        Assert.assertTrue(client._isFail(teaResponse));
    }

    @Test
    public void _toFormTest() throws UnsupportedEncodingException {
        Map<String, Object> map = new HashMap<>();
        String result = client._toForm(null);
        Assert.assertEquals("", result);

        result = client._toForm(map);
        Assert.assertEquals("", result);

        map.put("form", "test");
        map.put("param", "test");
        map.put("nullTest", null);
        result = client._toForm(map);
        Assert.assertEquals("form=test&param=test", result);
    }

    @Test
    public void _equalTest() {
        Assert.assertFalse(client._equal("", ""));
        Assert.assertFalse(client._equal("ss", ""));
        Assert.assertFalse(client._equal("ss", "sa"));
        Assert.assertTrue(client._equal("ss", "ss"));
    }

    @Test
    public void _toQueryTest() {
        Map<String, Object> map = new HashMap<>();
        Assert.assertEquals(0, client._toQuery(null).size());

        map.put("test", "test");
        map.put("nullTest", null);
        Assert.assertEquals("test", client._toQuery(map).get("test"));
        Assert.assertFalse(client._toQuery(map).containsKey("nullTest"));
    }

    @Test
    public void _notNullTest() {
        Map<String, Object> map = new HashMap<>();
        Assert.assertFalse(client._notNull(null));
        Assert.assertFalse(client._notNull(map));
        map.put("test", "test");
        Assert.assertTrue(client._notNull(map));
    }
}
