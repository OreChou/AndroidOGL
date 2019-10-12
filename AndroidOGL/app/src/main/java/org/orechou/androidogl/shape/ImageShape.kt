package org.orechou.androidogl.shape

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.Matrix
import org.orechou.androidogl.R
import org.orechou.androidogl.util.GLUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class ImageShape(context: Context, bitmap: Bitmap) : Shape(context) {

    private var mBitmap: Bitmap = bitmap
    private var mProgram: Int = 0
    private var vPosition: Int = 0
    private var vCoordinate: Int = 0
    private var vMatrix: Int = 0
    private var vTexture: Int = 0

    // screen coordinates
    private val sCoordinates = floatArrayOf(
        -1.0f, 1.0f,
        -1.0f, -1.0f,
        1.0f, 1.0f,
        1.0f, -1.0f
    )
    // texture coordinates
    private val tCoordinates = floatArrayOf(
        0.0f, 0.0f,
        0.0f, 1.0f,
        1.0f, 0.0f,
        1.0f, 1.0f
    )
    private val sCoordsBuffer: FloatBuffer = ByteBuffer.allocateDirect(sCoordinates.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()
    private val tCoordsBuffer: FloatBuffer = ByteBuffer.allocateDirect(tCoordinates.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()

    private var mViewMatrix = FloatArray(16)
    private var mProjectMatrix = FloatArray(16)
    private var mMVPMatrix = FloatArray(16)

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f)

        mProgram = GLUtils.linkProgram(mContext, R.raw.shape_vertex_shader, R.raw.shape_fragment_shader)

        vPosition = GLES20.glGetAttribLocation(mProgram, "vPosition")
        vCoordinate = GLES20.glGetAttribLocation(mProgram, "vCoordinate")
        vTexture = GLES20.glGetUniformLocation(mProgram, "vTexture")
        vMatrix = GLES20.glGetUniformLocation(mProgram, "vMatrix")

        sCoordsBuffer.put(sCoordinates)
        sCoordsBuffer.position(0)
        tCoordsBuffer.put(tCoordinates)
        tCoordsBuffer.position(0)

        var textureId = GLUtils.createTexture()
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        android.opengl.GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBitmap, 0)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0,0, width, height)

        val w = mBitmap.width
        val h = mBitmap.height
        val sWH = w / h.toFloat()
        val sWidthHeight = width / height.toFloat()
        if (width > height) {
            if (sWH > sWidthHeight) {
                Matrix.orthoM(mProjectMatrix, 0, -sWidthHeight * sWH, sWidthHeight * sWH, -1f, 1f, 3f, 5f)
            } else {
                Matrix.orthoM(mProjectMatrix, 0, -sWidthHeight / sWH, sWidthHeight / sWH, -1f, 1f, 3f, 5f)
            }
        } else {
            if (sWH > sWidthHeight) {
                Matrix.orthoM(mProjectMatrix, 0, -1f, 1f, -1 / sWidthHeight * sWH, 1 / sWidthHeight * sWH, 3f, 5f)
            } else {
                Matrix.orthoM(mProjectMatrix, 0, -1f, 1f, -sWH / sWidthHeight, sWH / sWidthHeight, 3f, 5f)
            }
        }
//        val ratio = width.toFloat() / height
//        // 设置透视投影
//        Matrix.orthoM(mProjectMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 5f)
        //设置相机位置
        Matrix.setLookAtM(mViewMatrix, 0, 0f, 0f, 5.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)
        //计算变换矩阵
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0)

    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT and GLES20.GL_DEPTH_BUFFER_BIT)
        GLES20.glUseProgram(mProgram)

        GLES20.glUniformMatrix4fv(vMatrix,1,false, mMVPMatrix,0)
        GLES20.glEnableVertexAttribArray(vPosition)
        GLES20.glEnableVertexAttribArray(vCoordinate)
        GLES20.glUniform1i(vTexture, 0)

        // 传入顶点坐标
        GLES20.glVertexAttribPointer(vPosition,2, GLES20.GL_FLOAT,false,0, sCoordsBuffer)
        // 传入纹理坐标
        GLES20.glVertexAttribPointer(vCoordinate,2, GLES20.GL_FLOAT,false,0, tCoordsBuffer)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP,0,4)
    }
}