package com.apigateway.baseclient;

import com.aliyun.tea.TeaRequest;
import com.aliyun.tea.TeaResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

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
        Assert.assertEquals("", client._getContentMD5(null));
        Assert.assertEquals("CY9rzUYh03PK3k6DJie09g==", client._getContentMD5("test"));
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
    public void _isFailTest() throws Exception{
        TeaResponse teaResponse = new TeaResponse();
        teaResponse.statusCode = 100;
        Assert.assertTrue(client._isFail(teaResponse));

        teaResponse.statusCode = 200;
        Assert.assertFalse(client._isFail(teaResponse));

        teaResponse.statusCode = 400;
        Assert.assertTrue(client._isFail(teaResponse));
    }
}
