package com.sychen.basic.activity

import android.app.Activity

object ActivityCollector {
    private val activities = ArrayList<Activity>()
    fun addActivity(activity: Activity) {
        activities.add(activity)
    }
    fun removeActivity(activity: Activity){
        activities.remove(activity)
    }
    fun finishAll(){
        activities.forEach { activity ->
            if (!activity.isFinishing){
                activity.finish()
            }
        }
        activities.clear()
    }
}