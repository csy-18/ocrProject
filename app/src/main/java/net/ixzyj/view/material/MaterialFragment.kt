package net.ixzyj.view.material

import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import net.ixzyj.ocr.R
import net.ixzyj.ocr.databinding.FragmentMaterialBinding

data class MaterialModel(
    val title: String,
    val color: Int
)

class MaterialFragment : Fragment() {
    private lateinit var binding: FragmentMaterialBinding
    private lateinit var materialListAdapter: MaterialListAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentMaterialBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initView()
    }

    private fun initView() {
        val materialModelList = arrayListOf(
            MaterialModel("物资入库", R.color.Blue_900),
            MaterialModel("补料出库", R.color.Blue_700),
            MaterialModel("返场入库", R.color.Blue_500),
            MaterialModel("退场入库", R.color.Blue_300),
            MaterialModel("物资出库", R.color.Blue_100),
            MaterialModel("物资调拨", R.color.gray),
        )
        materialListAdapter = MaterialListAdapter(materialModelList)
        binding.recyclerViewMaterial.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = materialListAdapter
        }
    }

}