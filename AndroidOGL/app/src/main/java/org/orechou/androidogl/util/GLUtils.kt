package org.orechou.androidogl.util

import android.content.Context
import android.opengl.GLES11Ext
import android.opengl.GLES20
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.microedition.khronos.opengles.GL10

object GLUtils {

    /**
     * 生成纹理一共有 4 步：
     * 1. 生成纹理名称：glGenTextures
     * 2. 绑定纹理名称到指定激活的纹理单元中（current active texture unit）：glBindTexture
     * 3. 创建参数:
     *      3.1 设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
     *      3.2 设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
     *      3.3 设置环绕方向 S，截取纹理坐标到 [1/2n,1-1/2n], 将导致永远不会与 border 融合
     *      3.4 设置环绕方向T，截取纹理坐标到 [1/2n,1-1/2n], 将导致永远不会与 border 融合
     * 4. 解绑纹理与激活的纹理单元: glBindTexture
     */
    fun createOESTexture(): Int {
        var texture = IntArray(1)
        GLES20.glGenTextures(1, texture, 0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0])
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST.toFloat())
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR.toFloat())
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE.toFloat())
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE.toFloat())
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0)
        return texture[0]
    }

    fun createTexture(): Int {
        var texture = IntArray(1)
        GLES20.glGenTextures(1, texture, 0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0])
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST.toFloat())
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR.toFloat())
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE.toFloat())
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE.toFloat())
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        return texture[0]
    }

    fun linkProgram(context: Context, vertexShaderResId: Int, fragmentShaderResId: Int) : Int {
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, readShaderFromResource(context, vertexShaderResId))
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, readShaderFromResource(context, fragmentShaderResId))
        val program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, fragmentShader)
        GLES20.glLinkProgram(program)
        return program
    }

    private fun readShaderFromResource(context: Context, resourceId: Int): String {
        val builder = StringBuffer()
        val bufferedReader = BufferedReader(InputStreamReader(context.resources.openRawResource(resourceId)))
        bufferedReader.readLines().forEach { builder.append(it + "\n") }
        bufferedReader.close()
        return builder.toString()
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)

        val compileStatus = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0)
        return shader
    }

}
