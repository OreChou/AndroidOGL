package org.orechou.androidogl.sticker

import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES20

class GLFrameTexture {
    private var mSTMatrix = FloatArray(16)

    private var textures: IntArray = IntArray(1)
    private var surfaceTexture: SurfaceTexture? = null

    fun initFrameTexture() {
        GLES20.glGenTextures(1, textures, 0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textures[0])
        GLES20.glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_NEAREST.toFloat()
        )
        GLES20.glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES20.GL_TEXTURE_MAG_FILTER,
            GLES20.GL_LINEAR.toFloat()
        )
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
    }

    fun getSurfaceTexture(): SurfaceTexture {
        surfaceTexture = SurfaceTexture(textures[0])
        return surfaceTexture!!
    }

    fun release() {
        GLES20.glDeleteTextures(1, textures, 0)
        surfaceTexture!!.release()
        surfaceTexture = null
    }

    fun drawFrameTexture(): Int {
        surfaceTexture!!.updateTexImage()
        surfaceTexture!!.getTransformMatrix(mSTMatrix)
        return textures[0]
    }

    fun getMatrix(): FloatArray {
        return mSTMatrix
    }
}