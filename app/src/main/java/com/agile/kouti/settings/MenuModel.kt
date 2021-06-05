package com.agile.kouti.settings

import android.icu.text.CaseMap

data class MenuModel(
    val title: String,
    val settingTitle: String,
    val image_drawable: Int,
    val imageUrl: String,
    var isSelected: Boolean
)