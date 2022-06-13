package net.ixzyj.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import net.ixzyj.network.OdooRepo
import org.apache.xmlrpc.XmlRpcRequest
import org.apache.xmlrpc.client.AsyncCallback

class MenuViewModel : ViewModel() {
    private val _menuInvisible by lazy {
        MutableLiveData<Map<String, String>>()
    }

    fun menuInvisible(): LiveData<Map<String, String>> {
        OdooRepo.models().executeAsync(
            "execute_kw", arrayListOf(
                OdooRepo.database, OdooRepo.uid, OdooRepo.password, "ir.config_parameter", "search_read",
                arrayListOf(
                    arrayListOf(arrayListOf("key", "=", "system_menu.invisible")),
                    arrayListOf("key", "value")
                )
            ), object : AsyncCallback {
                override fun handleResult(pRequest: XmlRpcRequest?, pResult: Any?) {
                    val data = arrayListOf(*pResult as Array<*>)
                    when (data.size) {
                        0 -> {
                            _menuInvisible.postValue(mapOf("value" to ""))
                        }
                        else -> {
                            _menuInvisible.postValue(data[0] as Map<String, String>)
                        }
                    }
                }

                override fun handleError(pRequest: XmlRpcRequest?, pError: Throwable?) {
                }
            }
        )
        return _menuInvisible
    }
}