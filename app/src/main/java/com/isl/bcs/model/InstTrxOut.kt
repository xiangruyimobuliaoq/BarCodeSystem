package com.isl.bcs.model

import kotlinx.serialization.Serializable
import org.litepal.crud.LitePalSupport


/**
 * 创建者     彭龙
 * 创建时间   2021/8/3 10:15 上午
 * 描述
 *
 * 更新者     $
 * 更新时间   $
 * 更新描述
 */
@Serializable
class InstTrxOut(
    var INST_OUT_KEY: String = "",
    var PKG_QTY: Int = 0,
    var SCAN_QTY: Int = 0,
    var INST_CLOSE_FLAG: Boolean,
    var S_DATETIME: String = "",
    var S_USER: String? = "",
    var PCS_QTY: Int = 0,
    var SCAN_ITEM_QTY: Int = 0,
) : LitePalSupport() {
    var id: Long = 0
}