package com.isl.bcs.model

import java.io.Serializable
import org.litepal.crud.LitePalSupport

@kotlinx.serialization.Serializable
class Staff(
    var id_no: String,
    var name: String,
    var valid_flag: Boolean
) : LitePalSupport(), Serializable {
    val id: Long = 0
}