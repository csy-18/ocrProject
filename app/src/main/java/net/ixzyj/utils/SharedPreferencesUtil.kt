package net.ixzyj.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import net.ixzyj.ocr.R
import net.ixzyj.utils.MyApplication.Companion.getContext
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import net.ixzyj.utils.MyApplication.Companion.logi
import kotlin.collections.ArrayList

@SuppressLint("StaticFieldLeak", "CommitPrefEdits")
object SharedPreferencesUtil {
    private var mContext: Context = getContext()
    var shp: SharedPreferences
    var editor: SharedPreferences.Editor

    init {
        val name = mContext.resources.getString(R.string.MY_DATA)
        shp = mContext.getSharedPreferences(name, Context.MODE_PRIVATE)
        editor = shp.edit()
    }

    /**
     * 写入
     */
    fun saveJson(key: String, value: ArrayList<String>) {
        editor.putString(key, Gson().toJson(value))
            .apply()
    }

    inline fun <reified T : Any> sharedPreferencesSave(key: String, value: T) = when (T::class) {
        String::class -> editor.putString(key, value.toString()).apply()
        Int::class -> editor.putInt(key, value.toString().toInt()).apply()
        Boolean::class -> editor.putBoolean(key, value.toString().toBoolean()).apply()
        Float::class -> editor.putFloat(key, value.toString().toFloat()).apply()
        Long::class -> editor.putLong(key, value.toString().toLong()).apply()
        ArrayList<String>()::class -> editor.putString(key, Gson().toJson(value)).apply()
        else -> throw IllegalArgumentException("不支持的类型: ${T::class.java}")
    }

    /**
     * 读取
     * @return
     */
    fun loadJson(key: String): ArrayList<String> {
        val data = shp.getString(key, "123")
        try {
            return Gson().fromJson(data, object : TypeToken<ArrayList<String>>() {}.type)
        } catch (e: Exception) {
            e.message?.logi()
            return arrayListOf("")
        }
    }

    inline fun <reified T : Any> sharedPreferencesLoad(key: String, defValue: T) = when (T::class) {
        String::class -> shp.getString(key, defValue.toString())
        Int::class -> shp.getInt(key, defValue.toString().toInt())
        Boolean::class -> shp.getBoolean(key, defValue.toString().toBoolean())
        Float::class -> shp.getFloat(key, defValue.toString().toFloat())
        Long::class -> shp.getLong(key, defValue.toString().toLong())
        else -> throw IllegalArgumentException("不支持的类型: ${T::class.java}")
    }
}