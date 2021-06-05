package com.agile.kouti.view_picture

import com.agile.kouti.db.picture.PictureList
import com.agile.kouti.home.SetupModel

interface OnPictureClickListener {
    fun onPictureItemClick(list: PictureList)
}