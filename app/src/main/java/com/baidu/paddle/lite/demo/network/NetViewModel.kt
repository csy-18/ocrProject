package com.baidu.paddle.lite.demo.network

import androidx.lifecycle.ViewModel
import org.apache.xmlrpc.client.XmlRpcClient
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl
import java.net.URL

class NetViewModel : ViewModel() {
    private val models = XmlRpcClient()
    private val modelsConfig = XmlRpcClientConfigImpl()

    companion object {
        const val BASE_URL = "http://114.67.113.2:8069"
        const val DB = "xinshengteng"
        const val USER_NAME = "admin"
        const val PWD = "sh76yKR;hnbaGacLU.kT2YFQpf"
    }

    init {
        modelsConfig.serverURL = URL(String.format("%s/xmlrpc/2/object", BASE_URL))
    }

    fun getReceipts(uid: Int): Any? {
        val parameter1 = arrayListOf<String>()
        val parameter2 = arrayListOf(
            "id",
            "state",
            "date",
            "name",
            "order_id",
            "partner_id",
            "user_id",
            "building_id",
            "warehouse_id"
        )
        val arrayOf = arrayOf(parameter1, parameter2)
        return models.execute(
            "execute_kw",
            arrayListOf(DB, uid, PWD, "buy.receipt", "search_read", arrayOf)
        )
    }
}