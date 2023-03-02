@file:Suppress("UNCHECKED_CAST", "MemberVisibilityCanBePrivate")

package com.isl.bcs.utils

import android.util.Log
import com.drake.net.convert.NetConverter
import com.drake.net.exception.ConvertException
import com.drake.net.exception.RequestParamsException
import com.drake.net.exception.ServerResponseException
import com.drake.net.request.kType
import com.isl.bcs.model.UserInfo
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import okhttp3.Response
import org.json.JSONObject
import java.lang.reflect.Type
import kotlin.reflect.KType

class SerializationConverter(
    val success: String = "100",
    val code: String = "Code",
    val message: String = "msg",
    val data: String = "data"
) : NetConverter {

    private val jsonDecoder = Json {
        ignoreUnknownKeys = true // JSON和数据模型字段可以不匹配
        coerceInputValues = true // 如果JSON字段是Null则使用默认值
        allowStructuredMapKeys = true
    }

    override fun <R> onConvert(succeed: Type, response: Response): R? {
        try {
            return NetConverter.DEFAULT.onConvert<R>(succeed, response)
        } catch (e: ConvertException) {
            val code = response.code
            when {
                code in 200..299 -> { // 请求成功
                    val bodyString = response.body?.string() ?: return null
                    val kType = response.request.kType ?: return null
                    val json = JSONObject(bodyString) // 获取JSON中后端定义的错误码和错误信息
                    return if (json.getString(this.code) == success) { // 对比后端自定义错误码
                        Log.e("123", json.getString(data))
                        json.getString(data).parseBody<R>(kType)
                    } else {
                        Log.e("123", "111111")
                        null
                    }
                }
                code in 400..499 -> throw RequestParamsException(response) // 请求参数错误
                code >= 500 -> throw ServerResponseException(response) // 服务器异常错误
                else -> {
                    Log.e("123", "22222222")
                    throw ConvertException(response)
                }
            }
        }
    }

    fun <R> String.parseBody(succeed: KType): R? {
        return jsonDecoder.decodeFromString(Json.serializersModule.serializer(succeed), this) as R
    }
}