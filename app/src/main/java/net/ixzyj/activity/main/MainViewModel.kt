package net.ixzyj.activity.main

import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import net.ixzyj.network.OdooRepo
import net.ixzyj.utils.MyApplication.Companion.logi
import org.apache.xmlrpc.XmlRpcRequest
import org.apache.xmlrpc.client.AsyncCallback

class MainViewModel : ViewModel() {
    private val corpName by lazy {
        MutableLiveData<String>()
    }
    private val _materialPermissions =
        MutableLiveData<Boolean>()

    val materialPermissions: LiveData<Boolean> by lazy {
        _materialPermissions
    }
    private val _onsitePermissions =
        MutableLiveData<Boolean>()

    val onsitePermissions: LiveData<Boolean> by lazy {
        _onsitePermissions
    }

    //获取主页面公司名称
    fun getCorpName(activity: OcrMainActivity): LiveData<String> {
        OdooRepo.models.executeAsync(
            "execute_kw", arrayListOf(
                OdooRepo.database,
                OdooRepo.uid,
                OdooRepo.password, "res.company", "search_read", emptyList<Any>()
            ), object : AsyncCallback {
                override fun handleResult(pRequest: XmlRpcRequest?, pResult: Any?) {
                    val data = arrayListOf(*pResult as Array<*>)
                    when (data.size) {
                        0 -> {
                            corpName.postValue("请在数据库中填充公司名称")
                        }
                        else -> {
                            val mapData = data[0] as Map<String, String>
                            corpName.postValue(mapData["name"])
                        }
                    }
                }

                override fun handleError(pRequest: XmlRpcRequest?, pError: Throwable?) {
                    "获取主页面公司名称-请求失败:pError${pError?.message}".logi()
                    "获取主页面公司名称-请求失败:pRequest${pRequest?.methodName}".logi()
                    OdooRepo.doNetError(activity)
                    corpName.postValue("请在数据库中填充公司名称")
                }
            }
        )
        return corpName
    }

    //检查是否有权限显示一级菜单
    fun checkPermissions(scenes: String, activity: OcrMainActivity) {
        OdooRepo.models.executeAsync(
            "execute_kw", arrayListOf(
                OdooRepo.database, OdooRepo.uid, OdooRepo.password, "res.users", "user_has_groups",
                arrayListOf(scenes)
            ), object : AsyncCallback {
                override fun handleResult(pRequest: XmlRpcRequest?, pResult: Any?) {
                    when (scenes) {
                        "gooderp_els.group_els_material" -> _materialPermissions.postValue(
                            pResult.toString().toBoolean()
                        )
                        "gooderp_els.group_els_onsite" -> _onsitePermissions.postValue(
                            pResult.toString().toBoolean()
                        )
                    }
                }

                override fun handleError(pRequest: XmlRpcRequest?, pError: Throwable?) {
                    OdooRepo.doNetError(activity)
                }
            })
    }
}