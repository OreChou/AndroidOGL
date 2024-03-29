package org.orechou.androidogl.sticker

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLUtils
import org.orechou.androidogl.util.ShaderUtils

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class GLSticker(context: Context, id: Int) {

    private var aPositionHandle: Int = 0
    private var uTextureSamplerHandle: Int = 0
    private var aTextureCoordHandle: Int = 0
    private var programId: Int = 0
    private var textures: IntArray? = null
    private var frameBuffers: IntArray? = null
    private val vertexBuffer: FloatBuffer
    private val vertexData = floatArrayOf(1f, -1f, -1f, -1f, 1f, 1f, -1f, 1f)
    private val textureVertexBuffer: FloatBuffer
    private val textureVertexData = floatArrayOf(
        1f, 0f,
        0f, 0f,
        1f, 1f,
        0f, 1f
    )
    private val bitmap: Bitmap
    private val vertexShader = "attribute vec4 aPosition;\n" +
            "attribute vec2 aTexCoord;\n" +
            "varying vec2 vTexCoord;\n" +
            "void main() {\n" +
            "    vTexCoord=aTexCoord;\n" +
            "    gl_Position = aPosition;\n" +
            "}"
    private val fragmentShader = "varying highp vec2 vTexCoord;\n" +
            "uniform highp sampler2D sTexture;\n" +
            "void main() {\n" +
            "    gl_FragColor = texture2D(sTexture,vec2(vTexCoord.x,1.0 - vTexCoord.y));\n" +
            "}"
    private var width: Int = 0
    private var height: Int = 0

    init {
        vertexBuffer = ByteBuffer.allocateDirect(vertexData.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(vertexData)
        vertexBuffer.position(0)

        textureVertexBuffer = ByteBuffer.allocateDirect(textureVertexData.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(textureVertexData)
        textureVertexBuffer.position(0)
        bitmap = BitmapFactory.decodeResource(context.resources, id)
    }

    fun initFrame(width: Int, height: Int) {
        this.width = width
        this.height = height
        programId = ShaderUtils.createProgram(vertexShader, fragmentShader)
        aPositionHandle = GLES20.glGetAttribLocation(programId, "aPosition")
        uTextureSamplerHandle = GLES20.glGetUniformLocation(programId, "sTexture")
        aTextureCoordHandle = GLES20.glGetAttribLocation(programId, "aTexCoord")
        textures = IntArray(2)
        GLES20.glGenTextures(2, textures, 0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures!![0])
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, bitmap, 0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures!![1])
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        frameBuffers = IntArray(1)
        GLES20.glGenFramebuffers(1, frameBuffers, 0)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffers!![0])
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, textures!![1], 0)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
    }

    fun setPoints(points: FloatArray) {
        vertexBuffer.rewind()
        vertexBuffer.put(points)
        vertexBuffer.position(0)
    }

    fun drawFrame(): Int {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffers!![0])
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT or GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glViewport(0, 0, width, height)
        GLES20.glUseProgram(programId)
        GLES20.glEnableVertexAttribArray(aPositionHandle)
        GLES20.glVertexAttribPointer(aPositionHandle, 2, GLES20.GL_FLOAT, false, 8, vertexBuffer)

        GLES20.glEnableVertexAttribArray(aTextureCoordHandle)
        GLES20.glVertexAttribPointer(aTextureCoordHandle, 2, GLES20.GL_FLOAT, false, 8, textureVertexBuffer)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures!![0])
        GLES20.glUniform1i(uTextureSamplerHandle, 0)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        GLES20.glUseProgram(0)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
        return textures!![1]
    }

    fun release() {
        GLES20.glDeleteTextures(2, textures, 0)
        GLES20.glDeleteFramebuffers(1, frameBuffers, 0)
        GLES20.glDeleteProgram(programId)
    }
}
