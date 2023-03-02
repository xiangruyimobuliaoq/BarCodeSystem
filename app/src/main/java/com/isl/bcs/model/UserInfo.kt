package com.isl.bcs.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

/**
 * 创建者     彭龙
 * 创建时间   2021/6/11 3:10 下午
 * 描述
 *
 * 更新者     $
 * 更新时间   $
 * 更新描述
 */
@Serializable
data class UserInfo(
    var companyCode: String = "",
    var userId: String = "",
    var password: String = "",
    @JsonNames("TokenID")
    var tokenID: String = ""
)
