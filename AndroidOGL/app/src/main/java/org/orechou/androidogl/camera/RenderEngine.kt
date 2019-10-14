package org.orechou.androidogl.camera

import android.content.Context
import android.opengl.GLES11Ext
import android.opengl.GLES20
import org.orechou.androidogl.R
import org.orechou.androidogl.util.GLUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class RenderEngine {

    private val vertexData = floatArrayOf(
        1f, 1f, 1f, 1f,
        -1f, 1f, 0f, 1f,
        -1f, -1f, 0f, 0f,
        1f, 1f, 1f, 1f,
        -1f, -1f, 0f, 0f,
        1f, -1f, 1f, 0f
    )

    private var mContext: Context? = null
    private var mBuffer: FloatBuffer? = null
    private var mOESTextureId: Int = 0
    private var mProgram: Int = 0

    private var aPosition = -1
    private var aTextureCoord = -1
    private var uTextureMatrix = -1
    private var uTextureSampler = -1

    constructor(OESTextureId: Int, context: Context) {
        mContext = context
        mOESTextureId = OESTextureId
        mBuffer = createBuffer(vertexData)
        mProgram = GLUtils.linkProgram(mContext!!, R.raw.base_vertex_shader, R.raw.base_fragment_shader)

    }

    private fun createBuffer(vertexData: FloatArray): FloatBuffer {
        val buffer = ByteBuffer.allocateDirect(vertexData.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()
        buffer.put(vertexData, 0, vertexData.size).position(0)
        return buffer
    }

    fun drawTexture(transformMatrix: FloatArray) {
        GLES20.glUseProgram(mProgram)
        aPosition = GLES20.glGetAttribLocation(mProgram, "aPosition")
        aTextureCoord = GLES20.glGetAttribLocation(mProgram, "aTextureCoord")
        uTextureMatrix = GLES20.glGetUniformLocation(mProgram, "uTextureMatrix")
        uTextureSampler = GLES20.glGetUniformLocation(mProgram, "uTextureSampler")

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mOESTextureId)
        GLES20.glUniform1i(uTextureSampler, 0)
        GLES20.glUniformMatrix4fv(uTextureMatrix, 1, false, transformMatrix, 0)

        mBuffer!!.position(0)
        GLES20.glEnableVertexAttribArray(aPosition)
        GLES20.glVertexAttribPointer(aPosition, 2, GLES20.GL_FLOAT, false, 16, mBuffer)

        mBuffer!!.position(2)
        GLES20.glEnableVertexAttribArray(aTextureCoord)
        GLES20.glVertexAttribPointer(aTextureCoord, 2, GLES20.GL_FLOAT, false, 16, mBuffer)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6)
    }
}
