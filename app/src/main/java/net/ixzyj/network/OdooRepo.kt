package net.ixzyj.network

import android.app.Activity
import android.content.Intent
import android.os.Looper
import kotlin.Throws
import org.apache.xmlrpc.XmlRpcException
import org.apache.xmlrpc.client.XmlRpcClient
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl
import com.google.gson.Gson
import net.ixzyj.activity.setting.SetDBActivity
import net.ixzyj.ocr.R
import net.ixzyj.utils.DialogUtil
import net.ixzyj.utils.MyApplication
import net.ixzyj.utils.MyApplication.Companion.getContext
import net.ixzyj.utils.MyApplication.Companion.logi
import net.ixzyj.utils.SharedPreferencesUtil
import java.net.MalformedURLException
import java.net.URL
import java.util.*
import kotlin.collections.HashMap

object OdooRepo {
    var serveUrl: String = ""
        get() {
            val url = SharedPreferencesUtil.sharedPreferencesLoad(
                getContext().getString(R.string.SERVER_ADDRESS),
                getContext().getString(R.string.SERVER_ADDRESS_VALUE_DEFAULT)
            )
            return url.toString()
        }

    var database: String = ""
        get() {
            val db = SharedPreferencesUtil.sharedPreferencesLoad(
                getContext().getString(R.string.DB_ADDRESS),
                getContext().getString(R.string.DB_ADDRESS_VALUE_DEFAULT)
            )
            return db.toString()
        }

    var uid: Int = -1
    var username: String? = ""
    var password: String? = ""

    val client = XmlRpcClient()
    val xmlRpcConfig = object : XmlRpcClientConfigImpl() {
        init {
            serverURL = URL(String.format("%s/xmlrpc/2/common", serveUrl))
            connectionTimeout = 3000
            "xmlRpcConfig-url地址:$serveUrl".logi()
            "xmlRpcConfig-数据库：$database".logi()
        }
    }

    val models: XmlRpcClient = object : XmlRpcClient() {
        init {
            setConfig(object : XmlRpcClientConfigImpl() {
                init {
                    serverURL = URL(String.format("%s/xmlrpc/2/object", serveUrl))
                    "models-url地址：$serveUrl".logi()
                    "models-数据库：$database".logi()
                }
            })
        }
    }

    fun doSettingError(activity: Activity) {
        DialogUtil.alertDialog("查询不到该用户\n请重新配置服务器和数据库", activity).apply {
            setOnDismissListener {
                MyApplication.getContext()
                    .startActivity(Intent(activity, SetDBActivity::class.java))
                activity.finish()
            }
        }
    }

    fun doNetError(activity: Activity) {
        Looper.prepare()
        DialogUtil.alertDialog("网络异常\n或服务器、数据库错误\n请检查手机网络\n或重新配置服务器", activity).apply {
            setOnDismissListener {
                activity.startActivity(Intent(activity,SetDBActivity::class.java))
            }
        }
        Looper.loop()
    }

    fun doErrorWork(activity: Activity) {
        DialogUtil.alertDialog("登录失败，原因未知\n请联系开发人员", activity)
    }

    @Throws(XmlRpcException::class, MalformedURLException::class)
    fun uploadRec(receiptId: Int, result: String): String {
        val resultMap: Map<String, String> = models.execute(
            "execute_kw", Arrays.asList(
                database,
                uid,
                password,
                "buy.receipt", "process_barcode",
                Arrays.asList(Arrays.asList(receiptId), result)
            )
        ) as Map<String, String>
        val gson = Gson()
        return gson.toJson(resultMap)
    }


    @Throws(MalformedURLException::class, XmlRpcException::class)
    fun uploadRecScene(orderId: Int?, content: String?, warehouseId: Int?): String {
        var resultMap: Map<String?, String?> = HashMap()
        resultMap = models.execute(
            "execute_kw",
            Arrays.asList(
                database, uid, password,
                "wh.internal", "process_barcode",
                Arrays.asList(orderId, content, warehouseId)
            )
        ) as Map<String?, String?>
        val gson = Gson()
        return gson.toJson(resultMap)
    }


}