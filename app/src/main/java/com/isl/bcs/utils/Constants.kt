package com.isl.bcs.utils

import com.drake.serialize.serialize.serial
import com.isl.bcs.model.Staff
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


/**
 * 创建者     彭龙
 * 创建时间   2021/6/11 12:59 下午
 * 描述
 *
 * 更新者     $
 * 更新时间   $
 * 更新描述
 */
class Constants {

    companion object {
        const val TYPE_CHECK: String = "check"
        const val INTENT_TYPE: String = "intentType"
        const val TYPE_INOUT: String = "inOut"
        const val TYPE_TRANSFER: String = "transfer"
        var currentStaff: Staff? by serial()
        var companyCode: String? by serial("AC0001")
        var endPoint: String? by serial("https://flex1.marrella.biz:91/")
        var token: String? by serial()
        fun toMD5(text: String): String {
            try {
                //获取md5加密对象
                val instance: MessageDigest = MessageDigest.getInstance("MD5")
                //对字符串加密，返回字节数组
                val digest: ByteArray = instance.digest(text.toByteArray())
                var sb: StringBuffer = StringBuffer()
                for (b in digest) {
                    //获取低八位有效值
                    var i: Int = b.toInt() and 0xff
                    //将整数转化为16进制
                    var hexString = Integer.toHexString(i)
                    if (hexString.length < 2) {
                        //如果是一位的话，补0
                        hexString = "0" + hexString
                    }
                    sb.append(hexString)
                }
                return sb.toString()

            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            }
            return ""
        }
    }

}