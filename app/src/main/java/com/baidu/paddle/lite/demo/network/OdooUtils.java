package com.baidu.paddle.lite.demo.network;

import com.google.gson.Gson;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class OdooUtils {
    private static final String url = "http://demo.gooderp.org:8888",
            db = "demo.gooderp.org",
            username = "demo",
            password = "demo";

    public static String getVersion() throws XmlRpcException, MalformedURLException {
        final XmlRpcClient client = new XmlRpcClient();
        final XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        config.setServerURL(new URL(String.format("%s/xmlrpc/2/common", url)));
        // 获取版本信息
        final Map<String, String> info = (Map<String, String>) client.execute(config, "version", Collections.emptyList());
        final Gson gson = new Gson();
        return gson.toJson(info);
    }

    // 验证用户名、密码，返回uid
    public static int authenticate(XmlRpcClient client, XmlRpcClientConfigImpl config) {
        int uid = 0;
        try {
            uid = (int) client.execute(config, "authenticate", Arrays.asList(
                    db, username, password, Collections.emptyMap()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return uid;
    }

    // 获取uid的partner
    public static List<Object> getPartner() throws MalformedURLException, XmlRpcException {
        final XmlRpcClient client = new XmlRpcClient();
        final XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        config.setServerURL(new URL(String.format("%s/xmlrpc/2/common", url)));

        XmlRpcClient models = new XmlRpcClient() {{
            setConfig(new XmlRpcClientConfigImpl() {{
                setServerURL(new URL(String.format("%s/xmlrpc/2/object", url)));
            }});
        }};
        int uid = authenticate(client, config);
        return Arrays.asList((Object[]) models.execute(
                "execute_kw", Arrays.asList(db, uid, password, "res.partner", "search",
                        Arrays.asList(Arrays.asList(Arrays.asList("is_company", "=", true)))
                )));
    }

    public static void main(String[] args) {
        try {
            System.out.println(OdooUtils.getVersion());
            System.out.println(OdooUtils.getPartner());
        } catch (XmlRpcException | MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
