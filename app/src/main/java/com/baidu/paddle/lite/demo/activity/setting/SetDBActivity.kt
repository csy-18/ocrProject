package com.baidu.paddle.lite.demo.activity.setting

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.baidu.paddle.lite.demo.network.OdooUtils
import com.baidu.paddle.lite.demo.ocr.R
import com.baidu.paddle.lite.demo.utils.MyApplication.Companion.logi
import com.baidu.paddle.lite.demo.utils.SharedPreferencesUtil
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
            dbSet()
            serviceSet()
        }

        private fun serviceSet() {
            val server = findPreference<ListPreference>(getString(R.string.server_title))
            server?.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { preference, newValue ->
                    SharedPreferencesUtil.sharedPreferencesSave("SERVER",newValue.toString())
                    OdooUtils.url = newValue.toString()
                    true
                }
        }

        private fun dbSet() {
            val db = findPreference<ListPreference>(getString(R.string.db_title))
            db?.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { preference, newValue ->
                    SharedPreferencesUtil.sharedPreferencesSave("DB",newValue.toString())
                    true
                }
        }
    }
}