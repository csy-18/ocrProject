package com.baidu.paddle.lite.demo.network.model

data class ReceiptsModelItem(
    val building_id: List<Any>,
    val date: String,
    val id: Int,
    val name: String,
    val order_id: List<Any>,
    val partner_id: List<Any>,
    val state: String,
    val user_id: List<Any>,
    val warehouse_id: List<Any>
)