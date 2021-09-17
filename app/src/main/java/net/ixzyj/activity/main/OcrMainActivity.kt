package net.ixzyj.activity.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import net.ixzyj.network.OdooUtils
import net.ixzyj.network.OdooUtils.checkPermissions
import net.ixzyj.ocr.databinding.ActivityMainBinding
import net.ixzyj.view.material.MaterialFragment
import net.ixzyj.view.materialscene.MaterialSceneFragment

class OcrMainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var work: Handler

    companion object {
        val LOAD_RECEIPTS_FAILED: Int = 0
        const val GO_TAB1 = 1
        const val GO_TAB2 = 2
        const val GO_TAB3 = 3
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
    }

    private fun initView() {
        initBinding()
        initHandler()
        initBottomDBName()
        checkScenePermissions()
    }

    private fun initHandler() {
        work = object : Handler(mainLooper) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    GO_TAB1 -> initViewPagerAndTab1()
                    GO_TAB2 -> initViewPagerAndTab2()
                    GO_TAB3 -> initViewPagerAndTab3()
                }
            }
        }
    }

    private fun checkScenePermissions() {
        Thread {
            val material = checkPermissions("gooderp_els.group_els_material")
            val onsite = checkPermissions("gooderp_els.group_els_onsite")
            if (material && onsite) {
                work.sendEmptyMessage(GO_TAB1)
            } else if (material) {
                work.sendEmptyMessage(GO_TAB2)
            } else if (onsite) {
                work.sendEmptyMessage(GO_TAB3)
            }
        }.start()
    }

    private fun initBottomDBName() {
        Thread {
            val corpName = OdooUtils.getCorpName().getValue("name")
            Looper.prepare()
            binding.corpName.text = "当前账套：$corpName"
            Looper.loop()
        }.start()
    }

    private fun initBinding() {
        tabLayout = binding.tabLayoutMain
        viewPager = binding.viewPagerMain
    }

    private fun initViewPagerAndTab1() {
        viewPager.apply {
            adapter = object : FragmentStateAdapter(this@OcrMainActivity) {
                override fun getItemCount(): Int = 2

                override fun createFragment(position: Int): Fragment = when (position) {
                    0 -> MaterialFragment()
                    else -> MaterialSceneFragment()
                }
            }
            setCurrentItem(0, false)
        }
        TabLayoutMediator(tabLayout, viewPager) { tab: TabLayout.Tab, i: Int ->
            tab.text = when (i) {
                0 -> "物资管理"
                else -> "项目现场管理"
            }
        }.attach()
    }

    private fun initViewPagerAndTab2() {
        viewPager.apply {
            adapter = object : FragmentStateAdapter(this@OcrMainActivity) {
                override fun getItemCount(): Int = 1

                override fun createFragment(position: Int): Fragment = MaterialFragment()
            }
            setCurrentItem(0, false)
        }
        TabLayoutMediator(tabLayout, viewPager) { tab: TabLayout.Tab, i: Int ->
            tab.text = "物资管理"
        }.attach()
    }

    private fun initViewPagerAndTab3() {
        viewPager.apply {
            adapter = object : FragmentStateAdapter(this@OcrMainActivity) {
                override fun getItemCount(): Int = 1

                override fun createFragment(position: Int): Fragment = MaterialSceneFragment()
            }
            setCurrentItem(0, false)
        }
        TabLayoutMediator(tabLayout, viewPager) { tab: TabLayout.Tab, i: Int ->
            tab.text = "项目现场管理"
        }.attach()
    }

}