package com.isl.bcs.model

import kotlinx.serialization.Serializable
import org.litepal.crud.LitePalSupport


/**
 * 创建者     彭龙
 * 创建时间   2021/8/9 9:39 上午
 * 描述
 *
 * 更新者     $
 * 更新时间   $
 * 更新描述
 */
@Serializable
class InstItemOut(
    var SCAN_KEY: Int,
    var INST_IN_KEY: String? = null,
    var IN_DATE: String = "",
    var ITEM_KEY: Int,
    var ITEM_1: String = "",
    var DESCRIPTION_1: String = "",
    var BOX_LABEL1: String = "",
    var WH_LOC1: String = "",
    var WH_LOC2: String = "",
    var NG_FALG: String = "",
    var INST_OUT_KEY: String? = null,
    var OUT_DATE: String = "",
    var A_ID_NO: String = "",
    var A_DATETIME: String = "",
    var ITEM_QTY: Int = 0
) : LitePalSupport(), java.io.Serializable {
}