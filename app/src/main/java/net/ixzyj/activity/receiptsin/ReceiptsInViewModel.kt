package net.ixzyj.activity.receiptsin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import net.ixzyj.network.OdooRepo
import org.apache.xmlrpc.XmlRpcRequest
import org.apache.xmlrpc.client.AsyncCallback

class ReceiptsInViewModel : ViewModel() {
    private val _getReceiptsInList by lazy {
        MutableLiveData<String>()
    }

    fun getReceiptsInList(): LiveData<String> {
        OdooRepo.models().executeAsync(
            "execute_kw", arrayListOf(
                OdooRepo.database, OdooRepo.uid, OdooRepo.password, "buy.receipt", "search_read",
                arrayListOf(
                    arrayListOf(arrayListOf("state", "=", "draft"),
                            arrayListOf("is_return", "=", false)),
                    arrayListOf(
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
                )
            ),object : AsyncCallback{
                override fun handleResult(pRequest: XmlRpcRequest?, pResult: Any?) {
                    val data = arrayListOf(*pResult as Array<*>)
                    when(data.size){
                        0->{}
                        else->_getReceiptsInList.postValue(Gson().toJson(data))
                    }
                }

                override fun handleError(pRequest: XmlRpcRequest?, pError: Throwable?) {
                }
            }
        )
        return _getReceiptsInList
    }
}