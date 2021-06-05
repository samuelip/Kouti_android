package com.agile.kouti.utils

import android.app.Activity
import android.graphics.RectF
import android.util.Size

import android.graphics.Matrix
import android.media.Image
import android.util.Log
import android.view.Surface

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.util.ArrayList
import java.util.Collections
import java.util.Comparator

object CameraUtils {
    fun configureTransform(
        viewWidth: Int,
        viewHeight: Int,
        mPreviewSize: Size,
        context: Activity
    ): Matrix? {
        val rotation = context.windowManager.defaultDisplay.rotation
        val matrix = Matrix()
        val viewRect = RectF(0.toFloat(), 0.toFloat(), viewWidth.toFloat(), viewHeight.toFloat())
        val bufferRect = RectF(0.toFloat(), 0.toFloat(), mPreviewSize.height.toFloat(), mPreviewSize.width.toFloat())
        val centerX = viewRect.centerX()
        val centerY = viewRect.centerY()
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY())
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL)
            val scale: Float = Math.max(
                viewHeight.toFloat() / mPreviewSize.height,
                viewWidth.toFloat() / mPreviewSize.width
            )
            matrix.postScale(scale, scale, centerX, centerY)
            matrix.postRotate((90 * (rotation - 2)).toFloat(), centerX, centerY)
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180.toFloat(), centerX, centerY)
        }
        return matrix
    }

    fun chooseOptimalSize(
        choices: Array<Size>, textureViewWidth: Int,
        textureViewHeight: Int, maxWidth: Int, maxHeight: Int, aspectRatio: Size
    ): Size? {

        // Collect the supported resolutions that are at least as big as the preview Surface
        val bigEnough: MutableList<Size> = ArrayList()
        // Collect the supported resolutions that are smaller than the preview Surface
        val notBigEnough: MutableList<Size> = ArrayList()
        val w: Int = aspectRatio.width
        val h: Int = aspectRatio.height
        for (option in choices) {
            if (option.width <= maxWidth && option.height <= maxHeight && option.height == option.width * h / w) {
                if (option.width >= textureViewWidth &&
                    option.height >= textureViewHeight
                ) {
                    bigEnough.add(option)
                } else {
                    notBigEnough.add(option)
                }
            }
        }
        return if (bigEnough.size > 0) {

            Collections.min(bigEnough, compareSizesByArea)
        } else if (notBigEnough.size > 0) {
            Collections.max(notBigEnough, compareSizesByArea)
        } else {
            Log.e("tag", "Couldn't find any suitable preview size")
            choices[0]
        }
    }

    val compareSizesByArea: (Size, Size) -> Int = { lhs, rhs ->
        java.lang.Long.signum(
            lhs.width.toLong() * lhs.height -
                    rhs.width.toLong() * rhs.height
        )
    }

//    internal class CompareSizesByArea : Comparator<Size?> {
//        override fun compare(lhs: Size, rhs: Size): Int {
//            return java.lang.Long.signum(
//                lhs.width as Long * lhs.height -
//                        rhs.width as Long * rhs.height
//            )
//        }
//    }


    class ImageSaver(image: Image, file: File) : Runnable {
        private val mImage: Image
        private val mFile: File
        override fun run() {
            val buffer: ByteBuffer = mImage.planes.get(0).buffer
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)
            var output: FileOutputStream? = null
            try {
                output = FileOutputStream(mFile)
                output.write(bytes)
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                mImage.close()
                if (null != output) {
                    try {
                        output.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }

        init {
            mImage = image
            mFile = file
        }
    }
}