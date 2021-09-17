package net.ixzyj.view.materialscene

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import net.ixzyj.network.OdooUtils
import net.ixzyj.network.model.MenuInvisible
import net.ixzyj.ocr.R
import net.ixzyj.ocr.databinding.FragmentMaterialBinding
import net.ixzyj.ocr.databinding.FragmentMaterialSceneBinding
import net.ixzyj.view.material.MaterialFragment
import net.ixzyj.view.material.MaterialListAdapter
import net.ixzyj.view.material.MaterialModel

class MaterialSceneFragment : Fragment() {
    val materialModelList = arrayListOf(
        MaterialModel("项目入库清点", R.color.Blue_900),
        MaterialModel("翻新入库清点", R.color.Blue_700),
        MaterialModel("项目返场清点", R.color.Blue_500),
        MaterialModel("项目退场清点", R.color.Blue_300),
    )
    private lateinit var binding: FragmentMaterialSceneBinding
    private lateinit var materialListAdapter: MaterialListAdapter
    private lateinit var errorHandler: Handler
    private lateinit var work: Handler

    companion object{
        const val FILTER_DATA = 1
    }
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
        initHandler()
        initData()
    }

    private fun initHandler() {
        errorHandler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {

                }
            }
        }
        work = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    MaterialFragment.FILTER_DATA -> {
                        val gson = Gson().fromJson(msg.obj.toString(), MenuInvisible::class.java)
                        gson.value.split(",").forEach { value ->
                            when (value) {
                                "onsite.material_project_out.invisible"->{materialModelList.removeAt(0)}
                                "onsite.material_supp_out.invisible"->{materialModelList.removeAt(1)}
                                "onsite.material_project_back.invisible"->{materialModelList.removeAt(2)}
                                "onsite.material_project_return.invisible"->{materialModelList.removeAt(3)}
                            }
                        }
                        initView(materialModelList)
                    }
                }
            }
        }
    }

    private fun initData() {
        Thread{
            val menuInvisible = OdooUtils.menuInvisible()
            val message = Message()
            message.what = FILTER_DATA
            message.obj = menuInvisible
            work.sendMessage(message)
        }.start()
    }
    private fun initView(data:List<MaterialModel>) {

        materialListAdapter = MaterialListAdapter(data)
        binding.recyclerViewMaterialScene.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = materialListAdapter
        }
    }

}