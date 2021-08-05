package net.ixzyj.activity.setting

import android.os.Bundle
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import net.ixzyj.network.OdooUtils
import net.ixzyj.ocr.R
import net.ixzyj.utils.SharedPreferencesUtil
import com.sychen.basic.activity.BaseActivity

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

        private fun addServer() {
            val serverEditText =
                findPreference<EditTextPreference>(getString(R.string.add_server_editTextPerference_title))
            serverEditText?.setOnPreferenceChangeListener { preference, newValue ->
                val serverList = SharedPreferencesUtil.loadJson(getString(R.string.SERVER_LIST))
                serverList.add(newValue.toString())
                SharedPreferencesUtil.saveJson(
                    getString(R.string.SERVER_LIST),
                    serverList
                )
                listPreServer()
                true
            }
        }

        private fun addDb() {
            findPreference<EditTextPreference>(getString(R.string.add_db_editTextPerference_title))?.apply {
                setOnPreferenceChangeListener { preference, newValue ->
                    val dbList = SharedPreferencesUtil.loadJson(getString(R.string.DB_LIST))
                    dbList.add(newValue.toString())
                    SharedPreferencesUtil.saveJson(
                        getString(R.string.DB_LIST),
                        dbList
                    )
                    listPreDb()
                    true
                }
            }

        }

        private fun listPreServer() {
            findPreference<ListPreference>(getString(R.string.server_listPerference_title))?.apply {
                val serverList = SharedPreferencesUtil.loadJson(getString(R.string.SERVER_LIST))
                if (serverList==null){
                    entryValues = arrayOf("")
                    entries = arrayOf("")
                }else{
                    entryValues = serverList.toTypedArray()
                    entries = serverList.toTypedArray()
                }
                negativeButtonText = "确定"
                onPreferenceChangeListener =
                    Preference.OnPreferenceChangeListener { preference, newValue ->
                        SharedPreferencesUtil.sharedPreferencesSave(
                            getString(R.string.SERVER_ADDRESS),
                            newValue.toString()
                        )
                        OdooUtils.url = newValue.toString()
                        true
                    }
            }
        }

        private fun listPreDb() {
            findPreference<ListPreference>(getString(R.string.db_listPerference_title))?.apply {
                val dbList = SharedPreferencesUtil.loadJson(getString(R.string.DB_LIST))
                if (dbList==null){
                    entryValues = arrayOf("")
                    entries = arrayOf("")
                }else{
                    entryValues = dbList.toTypedArray()
                    entries = dbList.toTypedArray()
                }
                negativeButtonText = "确定"
                onPreferenceChangeListener =
                    Preference.OnPreferenceChangeListener { preference, newValue ->
                        SharedPreferencesUtil.sharedPreferencesSave(
                            getString(R.string.DB_ADDRESS),
                            newValue.toString()
                        )
                        OdooUtils.db = newValue.toString()
                        true
                    }
            }
        }

        private fun delServe(){
            findPreference<ListPreference>(getString(R.string.del_server_editTextPerference_title))?.apply {
                val serverList = SharedPreferencesUtil.loadJson(getString(R.string.SERVER_LIST))
                if (serverList==null){
                    entryValues = arrayOf("")
                    entries = arrayOf("")
                }else{
                    entryValues = serverList.toTypedArray()
                    entries = serverList.toTypedArray()
                }
                negativeButtonText = "删除"
                setOnPreferenceChangeListener { preference, newValue ->
                    serverList.remove(newValue.toString())
                    SharedPreferencesUtil.saveJson(
                        getString(R.string.SERVER_LIST),
                        serverList
                    )
                    delServe()
                    listPreServer()
                    true
                }
            }
        }
        private fun delDb(){
            findPreference<ListPreference>(getString(R.string.del_db_editTextPerference_title))?.apply {
                val dbList = SharedPreferencesUtil.loadJson(getString(R.string.DB_LIST))
                if (dbList==null){
                    entryValues = arrayOf("")
                    entries = arrayOf("")
                }else{
                    entryValues = dbList.toTypedArray()
                    entries = dbList.toTypedArray()
                }
                negativeButtonText = "删除"
                setOnPreferenceChangeListener { preference, newValue ->
                    dbList.remove(newValue.toString())
                    SharedPreferencesUtil.saveJson(
                        getString(R.string.DB_LIST),
                        dbList
                    )
                    listPreDb()
                    delDb()
                    true
                }
            }
        }
    }
}