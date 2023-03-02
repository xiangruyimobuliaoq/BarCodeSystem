package com.isl.bcs.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.litepal.crud.LitePalSupport


/**
 * 创建者     彭龙
 * 创建时间   2021/7/12 11:47 上午
 * 描述
 *
 * 更新者     $
 * 更新时间   $
 * 更新描述
 */
@Serializable
class InstItemCheck(
    var SCAN_KEY: Int,
    var IN_DATE: String = "",
    var ITEM_KEY: Int,
    var ITEM_1: String = "",
    var DESCRIPTION_1: String = "",
    var BOX_LABEL1: String = "",
    var WH_LOC1: String = "",
    var WH_LOC2: String = "",
    var NG_FLAG: Boolean,
    var CHECKED_FLAG: Boolean,
    var CHECKED_DATE: String = "",
    var U_ID_NO: String = "",
    var U_DATETIME: String = "",
) : LitePalSupport() ,java.io.Serializable{
    @Transient
    var id: Long = 0
}