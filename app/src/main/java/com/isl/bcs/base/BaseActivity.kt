package com.isl.bcs.base

import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity


/**
 * 创建者     彭龙
 * 创建时间   2021/6/17 2:32 下午
 * 描述
 *
 * 更新者     $
 * 更新时间   $
 * 更新描述
 */
abstract class BaseActivity :AppCompatActivity() {

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

}