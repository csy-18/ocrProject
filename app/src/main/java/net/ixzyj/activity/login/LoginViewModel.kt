package net.ixzyj.activity.login

import android.app.Activity
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import net.ixzyj.network.OdooRepo
import net.ixzyj.network.OdooRepo.doNetError
import net.ixzyj.utils.MyApplication.Companion.getContext
import net.ixzyj.utils.MyApplication.Companion.logi
import org.apache.xmlrpc.XmlRpcRequest
import org.apache.xmlrpc.client.AsyncCallback

class LoginViewModel : ViewModel() {
    private val versionInfo by lazy {
        MutableLiveData<String>()
    }
    private val userId by lazy {
        MutableLiveData<Int>()
    }

    // 获取版本信息
    fun version(activity: Activity): LiveData<String>{
            OdooRepo.client.executeAsync(
                OdooRepo.xmlRpcConfig,
                "version",
                emptyList<Any>(),
                object :
                    AsyncCallback {
                    override fun handleResult(pRequest: XmlRpcRequest?, pResult: Any?) {
                        versionInfo.postValue(Gson().toJson(pResult.toString()))
                    }

                    override fun handleError(pRequest: XmlRpcRequest?, pError: Throwable?) {
                        doNetError(activity)
                    }
                })
            return versionInfo
        }

    //用户登陆
    fun userLogin(username: String?, password: String?, activity: Activity): LiveData<Int> {
        OdooRepo.client.executeAsync(
            OdooRepo.xmlRpcConfig, "authenticate", arrayListOf(
                OdooRepo.database, username, password, emptyMap<Any, Any>()
            ), object : AsyncCallback {
                override fun handleResult(pRequest: XmlRpcRequest?, pResult: Any?) {
                    if (!pResult.toString().equals("false")) {
                        userId.postValue(pResult.toString().toInt())
                    }
                }

                override fun handleError(pRequest: XmlRpcRequest?, pError: Throwable?) {
                    "用户登陆-请求失败:pError${pError?.suppressedExceptions}".logi()
                    "用户登陆-请求失败:pRequest${pRequest?.methodName}".logi()
                    userId.postValue(-1)
                    Looper.prepare()
                    doNetError(activity)
                    Looper.loop()
                }
            }
        )
        return userId
    }
}