package net.ixzyj.view.onsite

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import net.ixzyj.network.OdooRepo
import net.ixzyj.network.model.MenuInvisible
import net.ixzyj.ocr.R
import net.ixzyj.ocr.databinding.FragmentMaterialSceneBinding
import net.ixzyj.utils.MyApplication.Companion.logi
import net.ixzyj.view.MenuViewModel
import net.ixzyj.view.material.MaterialFragment
import net.ixzyj.view.material.MaterialListAdapter
import net.ixzyj.view.material.MaterialModel

class OnsiteFragment : Fragment() {
    private val materialModelList = arrayListOf(
        MaterialModel("项目入库清点", R.color.Blue_900),
        MaterialModel("翻新入库清点", R.color.Blue_700),
        MaterialModel("项目返场清点", R.color.Blue_500),
        MaterialModel("项目退场清点", R.color.Blue_300),
    )
    private val viewModel by lazy {
        ViewModelProvider(this).get(MenuViewModel::class.java)
    }
    private lateinit var binding: FragmentMaterialSceneBinding
    private lateinit var materialListAdapter: MaterialListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMaterialSceneBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initData()
    }

    private fun initData() {
        viewModel.menuInvisible().observe(requireActivity(), {
            it["value"]?.split(",")?.forEach { value ->
                "不显示的：$value".logi()
                when (value.trim()) {
                    "onsite.material_project_out.invisible" -> {
                        materialModelList.removeAt(0)
                    }
                    "onsite.material_supp_out.invisible" -> {
                        materialModelList.removeAt(1)
                    }
                    "onsite.material_project_back.invisible" -> {
                        materialModelList.removeAt(2)
                    }
                    "onsite.material_project_return.invisible" -> {
                        materialModelList.removeAt(3)
                    }
                }
            }
            initView(materialModelList)
        })
    }

    private fun initView(data: List<MaterialModel>) {
        materialListAdapter = MaterialListAdapter(data)
        binding.recyclerViewMaterialScene.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = materialListAdapter
        }
    }

}