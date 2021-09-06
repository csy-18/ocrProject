package net.ixzyj.activity.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import net.ixzyj.ocr.databinding.ActivityMainBinding
import net.ixzyj.view.material.MaterialFragment
import net.ixzyj.view.materialscene.MaterialSceneFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
    }

    private fun initView() {
        initBinding()
        initViewPager()
        initTabLayout()
    }

    private fun initBinding() {
        tabLayout = binding.tabLayoutMain
        viewPager = binding.viewPagerMain
    }

    private fun initViewPager() {
        viewPager.apply {
            adapter = object : FragmentStateAdapter(this@MainActivity) {
                override fun getItemCount(): Int = 2

                override fun createFragment(position: Int): Fragment = when (position) {
                    0 -> MaterialFragment()
                    else -> MaterialSceneFragment()
                }
            }
            setCurrentItem(0, false)
        }
    }

    private fun initTabLayout() {
        TabLayoutMediator(tabLayout, viewPager) { tab: TabLayout.Tab, i: Int ->
            tab.text = when (i) {
                0 -> "物资管理"
                else -> "项目现场管理"
            }
        }.attach()
    }
}