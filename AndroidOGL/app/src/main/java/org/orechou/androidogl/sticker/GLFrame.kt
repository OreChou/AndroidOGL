package org.orechou.androidogl.sticker

import android.graphics.Rect
import android.opengl.GLES11Ext
import android.opengl.GLES20

import org.orechou.androidogl.util.ShaderUtils

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class GLFrame {

    private var width: Int = 0
    private var height: Int = 0
    private var screenWidth: Int = 0
    private var screenHeight: Int = 0

    private val vertexData = floatArrayOf(1f, -1f, 0f, -1f, -1f, 0f, 1f, 1f, 0f, -1f, 1f, 0f)
    private val textureVertexData = floatArrayOf(1f, 0f, 0f, 0f, 1f, 1f, 0f, 1f)
    private val vertexBuffer: FloatBuffer

    private val textureVertexBuffer: FloatBuffer

    private var programId = -1
    private var aPositionHandle: Int = 0
    private var uTextureSamplerHandle: Int = 0
    private var iTextureSamplerHandle: Int = 0
    private var aTextureCoordHandle: Int = 0
    private var uSTMMatrixHandle: Int = 0

    private var sHandle: Int = 0
    private var hHandle: Int = 0
    private var lHandle: Int = 0

    private var iHandle: Int = 0


    private var vertexBuffers: IntArray? = null

    private val fragmentShader = "#extension GL_OES_EGL_image_external : require\n" +
            "varying highp vec2 vTexCoord;\n" +
            "uniform samplerExternalOES sTexture;\n" +
            "uniform sampler2D iTexture;\n" +
            "uniform highp mat4 uSTMatrix;\n" +
            "uniform highp float S;\n" +
            "uniform highp float H;\n" +
            "uniform highp float L;\n" +
            "uniform highp float i;\n" +
            "highp vec3 rgb2hsv(highp vec3 c){\n" +
            "    highp vec4 K = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);\n" +
            "    highp vec4 p = mix(vec4(c.bg, K.wz), vec4(c.gb, K.xy), step(c.b, c.g));\n" +
            "    highp vec4 q = mix(vec4(p.xyw, c.r), vec4(c.r, p.yzx), step(p.x, c.r));\n" +
            "    highp float d = q.x - min(q.w, q.y);\n" +
            "    highp float e = 1.0e-10;\n" +
            "    return vec3(abs(q.z + (q.w - q.y) / (6.0 * d + e)), d / (q.x + e), q.x);\n" +
            "}\n" +
            "highp vec3 hsv2rgb(highp vec3 c){\n" +
            "    highp vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);\n" +
            "    highp vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);\n" +
            "    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);\n" +
            "}" +
            "void main() {\n" +
            "    highp vec2 tx_transformed = (uSTMatrix * vec4(vTexCoord, 0, 1.0)).xy;\n" +
            "    highp vec4 video = texture2D(sTexture, tx_transformed);\n" +
            "    highp vec4 rgba;\n" +
            "    if(i == 0.0){\n" +
            "       rgba = video;\n" +
            "    }\n" +
            "    else{\n" +
            "       highp vec4 image = texture2D(iTexture, vTexCoord);\n" +
            "       rgba = mix(video,image,image.a);\n" +
            "    }\n" +
            "    highp vec3 hsl = rgb2hsv(rgba.xyz);\n" +
            "    if(H != 0.0)hsl.x = H;\n" +
            "    if(hsl.x<0.0)hsl.x = hsl.x+1.0;\n" +
            "    else if(hsl.x>1.0)hsl.x = hsl.x-1.0;\n" +
            "    if(S != 1.0)hsl.y = hsl.y*S;\n" +
            "    highp vec3 rgb = hsv2rgb(hsl);\n" +
            "    if (L < 0.0) rgb = rgb + rgb * vec3(L);\n" +
            "    else rgb = rgb + (1.0 - rgb) * vec3(L);\n" +
            "    gl_FragColor = vec4(rgb,rgba.w);\n" +
            "}"
    private val vertexShader = "attribute vec4 aPosition;\n" +
            "attribute vec2 aTexCoord;\n" +
            "varying vec2 vTexCoord;\n" +
            "void main() {\n" +
            "    vTexCoord = aTexCoord;\n" +
            "    gl_Position = aPosition;\n" +
            "}"

    private val s = 1.0f
    private val h = 0f
    private val l = 0f


    private val rect = Rect()

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
    }

    fun initFrame() {
        programId = ShaderUtils.createProgram(vertexShader, fragmentShader)
        aPositionHandle = GLES20.glGetAttribLocation(programId, "aPosition")
        uSTMMatrixHandle = GLES20.glGetUniformLocation(programId, "uSTMatrix")
        uTextureSamplerHandle = GLES20.glGetUniformLocation(programId, "sTexture")
        iTextureSamplerHandle = GLES20.glGetUniformLocation(programId, "iTexture")
        aTextureCoordHandle = GLES20.glGetAttribLocation(programId, "aTexCoord")
        sHandle = GLES20.glGetUniformLocation(programId, "S")
        hHandle = GLES20.glGetUniformLocation(programId, "H")
        lHandle = GLES20.glGetUniformLocation(programId, "L")

        iHandle = GLES20.glGetUniformLocation(programId, "i")

        vertexBuffers = IntArray(2)
        GLES20.glGenBuffers(2, vertexBuffers, 0)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBuffers!![0])
        GLES20.glBufferData(
            GLES20.GL_ARRAY_BUFFER,
            vertexData.size * 4,
            vertexBuffer,
            GLES20.GL_STATIC_DRAW
        )

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBuffers!![1])
        GLES20.glBufferData(
            GLES20.GL_ARRAY_BUFFER,
            textureVertexData.size * 4,
            textureVertexBuffer,
            GLES20.GL_STATIC_DRAW
        )
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
    }

    fun setSize(screenWidth: Int, screenHeight: Int, videoWidth: Int, videoHeight: Int) {
        this.screenWidth = screenWidth
        this.screenHeight = screenHeight
        this.width = videoWidth
        this.height = videoHeight
        rect()
    }

    private fun rect() {
        val left: Int
        val top: Int
        val viewWidth: Int
        val viewHeight: Int
        val sh = screenWidth * 1.0f / screenHeight
        val vh = width * 1.0f / height
        if (sh < vh) {
            left = 0
            viewWidth = screenWidth
            viewHeight = (height * 1.0f / width * viewWidth).toInt()
            top = (screenHeight - viewHeight) / 2
        } else {
            top = 0
            viewHeight = screenHeight
            viewWidth = (width * 1.0f / height * viewHeight).toInt()
            left = (screenWidth - viewWidth) / 2
        }
        rect.left = left
        rect.top = top
        rect.right = viewWidth
        rect.bottom = viewHeight
    }

    fun drawFrame(tId: Int, textureId: Int, STMatrix: FloatArray) {
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT or GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glViewport(rect.left, rect.top, rect.right, rect.bottom)
        GLES20.glUseProgram(programId)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBuffers!![0])
        GLES20.glEnableVertexAttribArray(aPositionHandle)
        GLES20.glVertexAttribPointer(aPositionHandle, 3, GLES20.GL_FLOAT, false, 0, 0)

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBuffers!![1])
        GLES20.glEnableVertexAttribArray(aTextureCoordHandle)
        GLES20.glVertexAttribPointer(aTextureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, 0)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
        GLES20.glUniform1i(uTextureSamplerHandle, 0)
        GLES20.glUniformMatrix4fv(uSTMMatrixHandle, 1, false, STMatrix, 0)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tId)
        GLES20.glUniform1i(iTextureSamplerHandle, 1)

        GLES20.glUniform1f(sHandle, s)
        GLES20.glUniform1f(hHandle, h)
        GLES20.glUniform1f(lHandle, l)
        GLES20.glUniform1f(iHandle, tId.toFloat())

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
    }

    fun release() {
        GLES20.glDeleteProgram(programId)
        GLES20.glDeleteBuffers(2, vertexBuffers, 0)
    }
}
