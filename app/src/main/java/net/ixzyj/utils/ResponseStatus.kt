package net.ixzyj.utils

import androidx.lifecycle.MutableLiveData
import com.sychen.basic.activity.BaseActivity

enum class ResponseStatus {
    Error,
    NetError,
    SettingError
}

class SetResponse {
    val getStatus = MutableLiveData<ResponseStatus>()

    fun alertStatusDialog(activity: BaseActivity) {
        getStatus.observe(activity, {
            when (getStatus.value) {
                ResponseStatus.Error -> {
                    DialogUtil.alertDialog("错误", activity)
                }
                ResponseStatus.NetError -> {
                    DialogUtil.alertDialog("网络错误", activity)
                }
                ResponseStatus.SettingError -> {
                    DialogUtil.alertDialog("服务器或数据库设置错误", activity)
                }
            }
        })
    }

}