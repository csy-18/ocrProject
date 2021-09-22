package net.ixzyj.activity.materiallist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import net.ixzyj.network.OdooRepo
import net.ixzyj.utils.MyApplication.Companion.logi
import org.apache.xmlrpc.XmlRpcRequest
import org.apache.xmlrpc.client.AsyncCallback

class MateriaViewModel : ViewModel() {
    private val _listData by lazy {
        MutableLiveData<String>()
    }

    fun getListData(scene: String): LiveData<String> {
        OdooRepo.models.executeAsync("execute_kw", arrayListOf(
            OdooRepo.database,
            OdooRepo.uid,
            OdooRepo.password, "wh.internal", "search_read",
            arrayListOf(
                arrayListOf(
                    arrayListOf("state", "=", "draft"),
                    arrayListOf("origin", "=", scene)
                ),
                arrayListOf("state","name","date","building_id","warehouse_id")
            )
        ),
            object : AsyncCallback {
                override fun handleResult(pRequest: XmlRpcRequest?, pResult: Any?) {
                    val data = arrayListOf(*pResult as Array<*>)
                    _listData.postValue(Gson().toJson(data))
                }

                override fun handleError(pRequest: XmlRpcRequest?, pError: Throwable?) {
                    "错误信息：$pError".logi()
                }
            }
        )
        return _listData
    }
}