package com.isl.bcs.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.litepal.crud.LitePalSupport

@Serializable
class Remain(
    var INST_KEY: String = "",
    var SCANNED_ITEM_QTY: Double,
    var SCANNED_BOX_QTY: Double

) : LitePalSupport(), java.io.Serializable {
    @Transient
    var id: Long = 0
}