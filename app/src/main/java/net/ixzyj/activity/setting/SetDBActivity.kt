package net.ixzyj.activity.setting

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import net.ixzyj.network.OdooRepo
import net.ixzyj.ocr.R
import net.ixzyj.utils.SharedPreferencesUtil
import com.sychen.basic.activity.BaseActivity
import net.ixzyj.activity.login.LoginActivity

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
        findViewById<Toolbar>(R.id.toolbarSetting).apply {
            title = "设置"
            setNavigationOnClickListener {
                startActivity(Intent(this@SetDBActivity,LoginActivity::class.java))
                finish()
            }
        }
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
                negativeButtonText = ""
                loadServerData().observe(this@SettingsFragment, {
                    entryValues = it.toTypedArray()
                    entries = it.toTypedArray()
                    onPreferenceChangeListener =
                        Preference.OnPreferenceChangeListener { _, newValue ->
                            SharedPreferencesUtil.sharedPreferencesSave(
                                getString(R.string.SERVER_ADDRESS),
                                newValue.toString()
                            )
                            OdooRepo.serveUrl = newValue.toString()
                            true
                        }
                })
            }
        }

        private fun listPreDb() {
            findPreference<ListPreference>(getString(R.string.db_listPerference_title))?.apply {
                negativeButtonText = ""
                loadDbData().observe(this@SettingsFragment, {
                    entryValues = it.toTypedArray()
                    entries = it.toTypedArray()
                    onPreferenceChangeListener =
                        Preference.OnPreferenceChangeListener { _, newValue ->
                            SharedPreferencesUtil.sharedPreferencesSave(
                                getString(R.string.DB_ADDRESS),
                                newValue.toString()
                            )
                            OdooRepo.database = newValue.toString()
                            true
                        }
                })
            }
        }

        private fun addServer() {
            val serverEditText =
                findPreference<EditTextPreference>(getString(R.string.add_server_editTextPerference_title))
            serverEditText?.setOnPreferenceChangeListener { _, newValue ->
                serverList.value?.add(newValue.toString())
                loadServerData().observe(this, {
                    saveServerData(it)
                })
                listPreServer()
                true
            }
        }

        private fun addDb() {
            findPreference<EditTextPreference>(getString(R.string.add_db_editTextPerference_title))?.apply {
                setOnPreferenceChangeListener { _, newValue ->
                    dbList.value?.add(newValue.toString())
                    loadDbData().observe(this@SettingsFragment, {
                        saveDbData(it)
                    })
                    listPreDb()
                    true
                }
            }

        }

        private fun delServe() {
            findPreference<ListPreference>(getString(R.string.del_server_editTextPerference_title))?.apply {
                negativeButtonText = ""
                loadServerData().observe(this@SettingsFragment, {
                    entryValues = it.toTypedArray()
                    entries = it.toTypedArray()
                    setOnPreferenceChangeListener { _, newValue ->
                        serverList.value?.remove(newValue.toString())
                        saveServerData(it)
                        listPreServer()
                        true
                    }
                })
            }
        }

        private fun delDb() {
            findPreference<ListPreference>(getString(R.string.del_db_editTextPerference_title))?.apply {
                negativeButtonText = ""
                loadDbData().observe(this@SettingsFragment, {
                    entryValues = it.toTypedArray()
                    entries = it.toTypedArray()
                    setOnPreferenceChangeListener { _, newValue ->
                        dbList.value?.remove(newValue.toString())
                        saveDbData(it)
                        listPreDb()
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
                dbList.postValue(arrayListOf(getString(R.string.DB_ADDRESS_VALUE_DEFAULT),"xst_xj"))
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