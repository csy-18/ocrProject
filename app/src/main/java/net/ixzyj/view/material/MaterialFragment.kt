package net.ixzyj.view.material

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
import net.ixzyj.ocr.databinding.FragmentMaterialBinding
import net.ixzyj.utils.MyApplication.Companion.logi
import net.ixzyj.view.MenuViewModel

data class MaterialModel(
    val title: String,
    val color: Int
)

class MaterialFragment : Fragment() {
    private val materialModelList = arrayListOf(
        MaterialModel("物资入库", R.color.Blue_900),
        MaterialModel("补料出库", R.color.Blue_700),
        MaterialModel("返场入库", R.color.Blue_500),
        MaterialModel("退场入库", R.color.Blue_400),
        MaterialModel("物资调拨", R.color.Blue_200),
        MaterialModel("翻新出库", R.color.Blue_100),
        MaterialModel("翻新入库", R.color.gray),
    )
    private val viewModel by lazy {
        ViewModelProvider(this).get(MenuViewModel::class.java)
    }
    private lateinit var binding: FragmentMaterialBinding
    private lateinit var materialListAdapter: MaterialListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMaterialBinding.inflate(layoutInflater)
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
                    "material.buy_receipt_in.invisible" -> {
                        materialModelList.removeAt(0)
                    }
                    "material.material_supp_out.invisible" -> {
                        materialModelList.removeAt(1)
                    }
                    "material.material_project_back.invisible" -> {
                        materialModelList.removeAt(2)
                    }
                    "material.material_project_return.invisible" -> {
                        materialModelList.removeAt(3)
                    }
                    "material.wh_internal_dispatch.invisible" -> {
                        materialModelList.removeAt(4)
                    }
                    "material.material_renew_out.invisible" -> {
                        materialModelList.removeAt(5)
                    }
                    "material.material_renew_in.invisible" -> {
                        materialModelList.removeAt(6)
                    }
                }
                initView(materialModelList)
            }
        })
    }

    private fun initView(data: List<MaterialModel>) {
        materialListAdapter = MaterialListAdapter(data)
        binding.recyclerViewMaterial.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = materialListAdapter
        }
    }

}