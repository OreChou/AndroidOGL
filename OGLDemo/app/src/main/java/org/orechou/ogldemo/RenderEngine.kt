package org.orechou.ogldemo

import android.content.Context
import android.opengl.GLES11Ext
import android.opengl.GLES20
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
        mProgram = linkProgram()
    }

    private fun createBuffer(vertexData: FloatArray): FloatBuffer {
        val buffer = ByteBuffer.allocateDirect(vertexData.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()
        buffer.put(vertexData, 0, vertexData.size).position(0)
        return buffer
    }

    private fun loadShader(type: Int, shaderSource: String): Int {
        val shader = GLES20.glCreateShader(type)
        if (shader == 0) {
            throw RuntimeException("Create Shader Failed!" + GLES20.glGetError())
        }
        GLES20.glShaderSource(shader, shaderSource)
        GLES20.glCompileShader(shader)
        return shader
    }

    private fun linkProgram(): Int {
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, GLUtils.readShaderFromResource(mContext!!, R.raw.base_vertex_shader))
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, GLUtils.readShaderFromResource(mContext!!, R.raw.base_fragment_shader))

        val program = GLES20.glCreateProgram()
        if (program == 0) {
            throw RuntimeException("Create Program Failed!" + GLES20.glGetError())
        }
        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, fragmentShader)
        GLES20.glLinkProgram(program)
        GLES20.glUseProgram(program)
        return program
    }

    fun drawTexture(transformMatrix: FloatArray) {
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
