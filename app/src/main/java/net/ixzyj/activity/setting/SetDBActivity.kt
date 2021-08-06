package net.ixzyj.activity.setting

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import net.ixzyj.network.OdooUtils
import net.ixzyj.ocr.R
import net.ixzyj.utils.SharedPreferencesUtil
import com.sychen.basic.activity.BaseActivity
import net.ixzyj.utils.MyApplication.Companion.logi

class SetDBActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        private val serverList by lazy {
            MutableLiveData<ArrayList<String>>()
        }

        private val dbList by lazy {
            MutableLiveData<ArrayList<String>>()
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            initDb()
            initServer()
        }

        private fun initServer() {
            listPreServer()
            addServer()
            delServe()
        }

        private fun initDb() {
            listPreDb()
            addDb()
            delDb()
        }

        private fun listPreServer() {
            findPreference<ListPreference>(getString(R.string.server_listPerference_title))?.apply {
                negativeButtonText = "确定"
                loadServerData().observe(this@SettingsFragment, {
                    entryValues = it.toTypedArray()
                    entries = it.toTypedArray()
                    onPreferenceChangeListener =
                        Preference.OnPreferenceChangeListener { preference, newValue ->
                            SharedPreferencesUtil.sharedPreferencesSave(
                                getString(R.string.SERVER_ADDRESS),
                                newValue.toString()
                            )
                            OdooUtils.url = newValue.toString()
                            true
                        }
                })
            }
        }

        private fun listPreDb() {
            findPreference<ListPreference>(getString(R.string.db_listPerference_title))?.apply {
                negativeButtonText = "确定"
                loadDbData().observe(this@SettingsFragment, {
                    entryValues = it.toTypedArray()
                    entries = it.toTypedArray()
                    onPreferenceChangeListener =
                        Preference.OnPreferenceChangeListener { preference, newValue ->
                            SharedPreferencesUtil.sharedPreferencesSave(
                                getString(R.string.DB_ADDRESS),
                                newValue.toString()
                            )
                            OdooUtils.db = newValue.toString()
                            true
                        }
                })
            }
        }

        private fun addServer() {
            val serverEditText =
                findPreference<EditTextPreference>(getString(R.string.add_server_editTextPerference_title))
            serverEditText?.setOnPreferenceChangeListener { preference, newValue ->
                serverList.value?.add(newValue.toString())
                loadServerData().observe(this, {
                    saveServerData(it)
                })
                true
            }
        }

        private fun addDb() {
            findPreference<EditTextPreference>(getString(R.string.add_db_editTextPerference_title))?.apply {
                setOnPreferenceChangeListener { preference, newValue ->
                    dbList.value?.add(newValue.toString())
                    loadDbData().observe(this@SettingsFragment, {
                        saveDbData(it)
                    })
                    true
                }
            }

        }

        private fun delServe() {
            findPreference<ListPreference>(getString(R.string.del_server_editTextPerference_title))?.apply {
                negativeButtonText = "删除"
                loadServerData().observe(this@SettingsFragment, {
                    entryValues = it.toTypedArray()
                    entries = it.toTypedArray()
                    setOnPreferenceChangeListener { preference, newValue ->
                        serverList.value?.remove(newValue.toString())
                        saveServerData(it)
                        true
                    }
                })
            }
        }

        private fun delDb() {
            findPreference<ListPreference>(getString(R.string.del_db_editTextPerference_title))?.apply {
                negativeButtonText = "删除"
                loadDbData().observe(this@SettingsFragment, {
                    entryValues = it.toTypedArray()
                    entries = it.toTypedArray()
                    setOnPreferenceChangeListener { preference, newValue ->
                        dbList.value?.remove(newValue.toString())
                        saveDbData(it)
                        true
                    }
                })
            }
        }

        private fun loadServerData(): LiveData<ArrayList<String>> {
            val serverListSp = SharedPreferencesUtil.loadJson(getString(R.string.SERVER_LIST))
            if (serverListSp.toString() == "[]") {
                serverList.postValue(arrayListOf(getString(R.string.SERVER_ADDRESS_VALUE_DEFAULT)))
            } else {
//                serverList.value?.remove(getString(R.string.SERVER_ADDRESS_VALUE_DEFAULT))
                serverList.postValue(serverListSp)
            }
            return serverList
        }

        private fun saveServerData(data: ArrayList<String>) {
            SharedPreferencesUtil.saveJson(
                getString(R.string.SERVER_LIST),
                data
            )
        }

        private fun loadDbData(): LiveData<ArrayList<String>> {
            val dbListSp = SharedPreferencesUtil.loadJson(getString(R.string.DB_LIST))
            if (dbListSp.toString() == "[]") {
                dbList.postValue(arrayListOf(getString(R.string.DB_ADDRESS_VALUE_DEFAULT)))
            } else {
                dbList.postValue(dbListSp)
            }
            return dbList
        }

        private fun saveDbData(data: ArrayList<String>) {
            SharedPreferencesUtil.saveJson(
                getString(R.string.DB_LIST),
                data
            )
        }
    }
}