package com.aliyun.api_gateway_baseclient;

import com.aliyun.tea.TeaRequest;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ClientTest {

    @Test
    public void getSignatureTest() throws Exception {
        TeaRequest request = new TeaRequest();
        request.method = "test";
        request.headers.put("accept", "json");
        request.headers.put("date", "now");
        request.pathname = "test";
        Assert.assertEquals("lzttujz/sOhS2QSBBSZBq58ZNSxvSDdkWWlknQxnrt0=", Client.getSignature(request, "sk"));

        request.headers.put("content-md5", "md5");
        request.headers.put("content-type", "type");
        Assert.assertEquals("HcjhFWjGB9Xyitos6CHnJvwQoPQzdPUBgv5oUd0tdoA=", Client.getSignature(request, "sk"));
    }

    @Test
    public void toQueryTest() throws Exception {
        Map<String, Object> map = new HashMap<>();
        Assert.assertEquals(0, Client.toQuery(null).size());

        map.put("test", "test");
        map.put("nullTest", null);
        Assert.assertEquals("test", Client.toQuery(map).get("test"));
        Assert.assertFalse(Client.toQuery(map).containsKey("nullTest"));
    }

    @Test
    public void isFailTest() throws Exception {
        Assert.assertFalse(Client.isFail(null));

        Assert.assertTrue(Client.isFail(100));

        Assert.assertFalse(Client.isFail(200));

        Assert.assertTrue(Client.isFail(400));
    }

    @Test
    public void getContentMD5Test() throws Exception {
        Assert.assertEquals("", Client.getContentMD5(""));
        Assert.assertEquals("govO+HY8G8YW4loGvkuQ/w==", Client.getContentMD5("{\"test\":\"test\"}"));
    }

    @Test
    public void getSignHeaderTest() throws Exception {
        Client client = new Client();
        Method getSignHeader = Client.class.getDeclaredMethod("getSignHeader", TeaRequest.class);
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
        Method getUrl = Client.class.getDeclaredMethod("getUrl", TeaRequest.class);
        getUrl.setAccessible(true);
        TeaRequest request = new TeaRequest();
        request.pathname = "test";
        request.query.put("testKey", "value");
        Assert.assertEquals("test?testKey=value", getUrl.invoke(new Client(), request));
    }
}
