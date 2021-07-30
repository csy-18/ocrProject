package com.sychen.basic.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.baidu.paddle.lite.demo.utils.MyApplication.Companion.log
import com.sychen.basic.MessageEvent
import com.sychen.basic.MessageType
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

open class BaseActivity : AppCompatActivity() {
    companion object{
        const val TAG = "BaseActivity"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        log("BaseActivity:onCreate${javaClass.simpleName}")
        EventBus.getDefault().register(this)
        ActivityCollector.addActivity(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        log("BaseActivity:onDestroy${javaClass.simpleName}")
        ActivityCollector.removeActivity(this)
        EventBus.getDefault().unregister(this)
    }
    //接收消息
    @Subscribe(threadMode = ThreadMode.MAIN)
    open fun onMessageEvent(event: MessageEvent) {
        when (event.type) {
            MessageType.TypeTwo -> {
            }
            MessageType.TypeOne -> {
            }
        }
    }
}