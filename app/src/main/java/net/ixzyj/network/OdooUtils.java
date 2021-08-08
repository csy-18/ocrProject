package net.ixzyj.network;

import android.util.Log;

import com.google.gson.Gson;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OdooUtils {
    public static String
            TAG = OdooUtils.class.getName(),
            url,
            db,
            uid,
            username,
            password;

    //获取版本
    public static String getVersion() throws MalformedURLException, XmlRpcException {
        Map<String, String> info = new HashMap<>();
        final XmlRpcClient client = new XmlRpcClient();
        final XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        // 获取版本信息
        config.setServerURL(new URL(String.format("%s/xmlrpc/2/common", url)));
        info = (Map<String, String>) client.execute(config, "version", Collections.emptyList());
        final Gson gson = new Gson();
        return gson.toJson(info);
    }

    //用户登录
    public static int userLogin(String username, String password) throws XmlRpcException, MalformedURLException {
        int uid;
        final XmlRpcClient client = new XmlRpcClient();
        final XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        config.setServerURL(new URL(String.format("%s/xmlrpc/2/common", url)));
        uid = (int) client.execute(config, "authenticate", Arrays.asList(
                db, username, password, Collections.emptyMap()));
        Log.i(TAG, "userLogin: uid" + uid);
        return uid;
    }

    // 获取入库清单
    public static List<Object> getReceipts() throws MalformedURLException, XmlRpcException {
        List<Object> info = new ArrayList<>();
        XmlRpcClient models = new XmlRpcClient() {{
            setConfig(new XmlRpcClientConfigImpl() {{
                setServerURL(new URL(String.format("%s/xmlrpc/2/object", url)));
            }});
        }};
        info = Arrays.asList((Object[]) models.execute(
                "execute_kw", Arrays.asList(db, uid, password, "buy.receipt", "search_read",
                        Arrays.asList(Arrays.asList(Arrays.asList("state", "=", "draft")), Arrays.asList(
                                "id",
                                "state",
                                "date",
                                "name",
                                "order_id",
                                "partner_id",
                                "user_id",
                                "building_id",
                                "warehouse_id"))
                )));
        return info;
    }


    public static String uploadRec(Integer receiptId, String result) throws XmlRpcException, MalformedURLException {
        Map<String, String> resultMap;
        XmlRpcClient models = new XmlRpcClient() {{
            setConfig(new XmlRpcClientConfigImpl() {{
                setServerURL(new URL(String.format("%s/xmlrpc/2/object", url)));
            }});
        }};
        resultMap = (Map<String, String>) models.execute("execute_kw", Arrays.asList(
                db,
                uid,
                password,
                "buy.receipt", "process_barcode",
                Arrays.asList(Arrays.asList(receiptId), result)));
        final Gson gson = new Gson();
        return gson.toJson(resultMap);
    }

    public static List<Object> getSendOrders() throws MalformedURLException, XmlRpcException {
        List<Object> info = new ArrayList<>();
        XmlRpcClient models = new XmlRpcClient() {{
            setConfig(new XmlRpcClientConfigImpl() {{
                setServerURL(new URL(String.format("%s/xmlrpc/2/object", url)));
            }});
        }};
        info = Arrays.asList((Object[]) models.execute("execute_kw", Arrays.asList(db, uid, password,
                "wh.internal", "search_read",
                Arrays.asList(
                        Arrays.asList(Arrays.asList("state", "=", "draft"),
                                Arrays.asList("is_open", "=", true),
                                Arrays.asList("origin", "=", "material_project_out")),
                        Arrays.asList("state", "name", "date", "building_id", "warehouse_id")))));
        return info;
    }


    public static String uploadRecScene(Integer orderId, String content, Integer warehouseId) throws MalformedURLException, XmlRpcException {
        Map<String, String> resultMap = new HashMap<>();
        XmlRpcClient models = new XmlRpcClient() {{
            setConfig(new XmlRpcClientConfigImpl() {{
                setServerURL(new URL(String.format("%s/xmlrpc/2/object", url)));
            }});
        }};
        resultMap = (Map<String, String>) models.execute("execute_kw",
                Arrays.asList(db, uid, password,
                        "wh.internal", "process_barcode",
                        Arrays.asList(orderId, content, warehouseId)));
        Gson gson = new Gson();
        return gson.toJson(resultMap);
    }

    public static void main(String[] args) {
        try {
            OdooUtils.getVersion();
        } catch (MalformedURLException | XmlRpcException e) {
            e.printStackTrace();
        }
    }
}
