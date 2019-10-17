package org.orechou.androidogl.sticker

import android.opengl.GLES20
import org.orechou.androidogl.util.ShaderUtils

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class GLPoints {
    private val vertexBuffer: FloatBuffer
    private val bufferLength = 106 * 2 * 4
    private var programId = -1
    private var aPositionHandle: Int = 0

    private var vertexBuffers: IntArray? = null


    private val fragmentShader = "void main() {\n" +
            "    gl_FragColor = vec4(0.0,1.0,0.0,1.0);\n" +
            "}"
    private val vertexShader = "attribute vec2 aPosition;\n" +
            "void main() {\n" +
            "    gl_Position = vec4(aPosition,0.0,1.0);\n" +
            "    gl_PointSize = 10.0;\n" +
            "}"

    init {
        vertexBuffer = ByteBuffer.allocateDirect(bufferLength)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        vertexBuffer.position(0)
    }

    fun initPoints() {
        programId = ShaderUtils.createProgram(vertexShader, fragmentShader)
        aPositionHandle = GLES20.glGetAttribLocation(programId, "aPosition")

        vertexBuffers = IntArray(1)
        GLES20.glGenBuffers(1, vertexBuffers, 0)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBuffers!![0])
        GLES20.glBufferData(
            GLES20.GL_ARRAY_BUFFER,
            bufferLength,
            vertexBuffer,
            GLES20.GL_STATIC_DRAW
        )

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
    }

    fun setPoints(points: FloatArray) {
        vertexBuffer.rewind()
        vertexBuffer.put(points)
        vertexBuffer.position(0)
    }


    fun drawPoints() {
        GLES20.glUseProgram(programId)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBuffers!![0])
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0, bufferLength, vertexBuffer)
        GLES20.glEnableVertexAttribArray(aPositionHandle)
        GLES20.glVertexAttribPointer(
            aPositionHandle, 2, GLES20.GL_FLOAT, false,
            0, 0
        )
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 106)
    }

    fun release() {
        GLES20.glDeleteProgram(programId)
        GLES20.glDeleteBuffers(1, vertexBuffers, 0)
    }
}
