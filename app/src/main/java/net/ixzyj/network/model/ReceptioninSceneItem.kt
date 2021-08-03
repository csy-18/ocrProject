package net.ixzyj.network.model

data class ReceptioninSceneItem(
    val building_id: List<Any>,
    val date: String,
    val id: Int,
    val name: String,
    val state: String,
    val warehouse_id: List<Any>
)