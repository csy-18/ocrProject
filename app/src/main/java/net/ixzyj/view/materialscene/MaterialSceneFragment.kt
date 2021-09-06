package net.ixzyj.view.materialscene

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import net.ixzyj.ocr.R
import net.ixzyj.ocr.databinding.FragmentMaterialBinding
import net.ixzyj.ocr.databinding.FragmentMaterialSceneBinding
import net.ixzyj.view.material.MaterialListAdapter
import net.ixzyj.view.material.MaterialModel

class MaterialSceneFragment : Fragment() {

    private lateinit var binding: FragmentMaterialSceneBinding
    private lateinit var materialListAdapter: MaterialListAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentMaterialSceneBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initView()
    }

    private fun initView() {
        val materialModelList = arrayListOf(
            MaterialModel("入库清点", R.color.Blue_900),
            MaterialModel("返场清点", R.color.Blue_700),
            MaterialModel("退场清点", R.color.Blue_500),
        )
        materialListAdapter = MaterialListAdapter(materialModelList)
        binding.recyclerViewMaterialScene.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = materialListAdapter
        }
    }

}