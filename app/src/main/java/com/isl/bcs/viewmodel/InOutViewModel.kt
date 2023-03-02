package com.isl.bcs.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.isl.bcs.model.BoxIn


/**
 * 创建者     彭龙
 * 创建时间   2021/7/13 9:53 上午
 * 描述
 *
 * 更新者     $
 * 更新时间   $
 * 更新描述
 */
class InOutViewModel : ViewModel() {
    val mInOutList: MutableLiveData<MutableList<BoxIn>> = MutableLiveData()

    init {
        mInOutList.value = ArrayList()
    }

    fun addBox(boxIn: BoxIn) {
        mInOutList.value?.add(boxIn)
        mInOutList.value = mInOutList.value
        Log.e("123", mInOutList.value?.size.toString())
    }

    fun clear() {
        mInOutList.value?.clear()
        mInOutList.value = mInOutList.value
    }
}