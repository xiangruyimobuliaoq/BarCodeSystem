package com.isl.bcs.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import org.litepal.crud.LitePalSupport

@Serializable
class Warehouse(
    var to_description: String,
    var type: String,
    var wh_code: String
) : LitePalSupport(){
    val id: Long = 0
}