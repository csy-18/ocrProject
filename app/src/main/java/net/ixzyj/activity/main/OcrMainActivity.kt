package net.ixzyj.activity.main

import android.os.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import net.ixzyj.ocr.databinding.ActivityMainBinding
import net.ixzyj.utils.MyApplication.Companion.logi
import net.ixzyj.view.material.MaterialFragment
import net.ixzyj.view.onsite.OnsiteFragment

class OcrMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private val viewModel by lazy {
        ViewModelProvider(this).get(MainViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
    }

    private fun initView() {
        initBinding()
        initBottomDBName()
        checkScenePermissions()
    }

    private fun checkScenePermissions() {
        viewModel.checkPermissions("gooderp_els.group_els_material", this)
        viewModel.checkPermissions("gooderp_els.group_els_onsite", this)
        viewModel.materialPermissions.observe(this, { material ->
            viewModel.onsitePermissions.observe(this, { onsite ->
                "物资管理-权限：$material 项目现场管理-权限：$onsite".logi()
                if (material && onsite) {
                    initViewPagerAndTab(
                        listOf(MaterialFragment(), OnsiteFragment()),
                        listOf("物资管理", "项目现场管理")
                    )
                } else if (material) {
                    initViewPagerAndTab(listOf(MaterialFragment()), listOf("物资管理"))
                } else if (onsite) {
                    initViewPagerAndTab(listOf(OnsiteFragment()), listOf("项目现场管理"))
                }
            })
        })
    }

    private fun initBottomDBName() {
        viewModel.getCorpName(this).observe(this, {
            binding.corpName.text = "当前账套：$it"
        })
    }

    private fun initBinding() {
        tabLayout = binding.tabLayoutMain
        viewPager = binding.viewPagerMain
    }

    private fun initViewPagerAndTab(listFragment: List<Fragment>, fragmentTitle: List<String>) {
        viewPager.apply {
            adapter = object : FragmentStateAdapter(this@OcrMainActivity) {
                override fun getItemCount(): Int = listFragment.size

                override fun createFragment(position: Int): Fragment = when (position) {
                    0 -> listFragment[0]
                    else -> listFragment[1]
                }
            }
            setCurrentItem(0, false)
        }
        TabLayoutMediator(tabLayout, viewPager) { tab: TabLayout.Tab, i: Int ->
            tab.text = when (i) {
                0 -> fragmentTitle[0]
                else -> fragmentTitle[1]
            }
        }.attach()
    }

}