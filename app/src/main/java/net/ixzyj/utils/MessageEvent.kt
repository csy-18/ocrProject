package com.sychen.basic


import android.os.Bundle
import android.os.Parcelable
import java.io.Serializable

private const val KEY_INT = "key_int"
private const val KEY_STRING = "key_string"
private const val KEY_BOOL = "key_bool"
private const val KEY_SERIALIZABLE = "key_serializable"
private const val KEY_PARCELABLE = "key_parcelable"


data class MessageEvent(var type: MessageType) {

    var bundle = Bundle()

    fun put(value: Int): MessageEvent {
        bundle.putInt(KEY_INT, value)
        return this
    }

    fun put(value: String): MessageEvent {
        bundle.putString(KEY_STRING, value)
        return this
    }

    fun put(value: Boolean): MessageEvent {
        bundle.putBoolean(KEY_BOOL, value)
        return this
    }

    fun put(value: Serializable): MessageEvent {
        bundle.putSerializable(KEY_SERIALIZABLE, value)
        return this
    }

    fun put(value: Parcelable): MessageEvent {
        bundle.putParcelable(KEY_PARCELABLE, value)
        return this
    }

    fun put(key: String, value: Int): MessageEvent {
        bundle.putInt(key, value)
        return this
    }

    fun put(key: String, value: String): MessageEvent {
        bundle.putString(key, value)
        return this
    }

    fun put(key: String, value: Boolean): MessageEvent {
        bundle.putBoolean(key, value)
        return this
    }

    fun put(key: String, value: Serializable): MessageEvent {
        bundle.putSerializable(key, value)
        return this
    }

    fun put(key: String, value: Parcelable): MessageEvent {
        bundle.putParcelable(key, value)
        return this
    }


    fun getInt(): Int {
        return bundle.getInt(KEY_INT)
    }

    fun getString(): String? {
        return bundle.getString(KEY_STRING)
    }

    fun getBoolean(): Boolean {
        return bundle.getBoolean(KEY_BOOL)
    }

    fun <T : Serializable> getSerializable(): Serializable {
        return bundle.getSerializable(KEY_SERIALIZABLE) as T
    }

    fun <T : Parcelable> getParcelable(): T? {
        return bundle.getParcelable<T>(KEY_PARCELABLE)
    }

    fun getInt(key: String): Int {
        return bundle.getInt(key)
    }

    fun getString(key: String): String? {
        return bundle.getString(key)
    }

    fun getBoolean(key: String): Boolean {
        return bundle.getBoolean(key)
    }

    fun <T : Serializable> getSerializable(key: String): Serializable {
        return bundle.getSerializable(key) as T
    }

    fun <T : Parcelable> getParcelable(key: String): T? {
        return bundle.getParcelable<T>(key)
    }

}

enum class MessageType {
    TypeOne, //启动Activity
    TypeTwo,
}


