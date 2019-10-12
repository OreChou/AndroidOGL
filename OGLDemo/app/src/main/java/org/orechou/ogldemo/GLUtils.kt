package org.orechou.ogldemo

import android.content.Context
import android.opengl.GLES11Ext
import android.opengl.GLES20
import java.io.*
import javax.microedition.khronos.opengles.GL10

object GLUtils {

    fun createOESTextureObject(): Int {
        // 1. 生成一个纹理，返回一个id
        val tex = IntArray(1)
        GLES20.glGenTextures(1, tex, 0)

        // 2. 把纹理绑定到 GL_TEXTURE_EXTERNAL_OES 上，可以想象成指针赋值
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, tex[0])

        // 3. 对 GL_TEXTURE_EXTERNAL_OES 设一堆参数，这些参数都设到我们上一步绑定的纹理上
        // 设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST.toFloat())
        // 设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR.toFloat())
        // 设置环绕方向 S，截取纹理坐标到 [1/2n,1-1/2n], 将导致永远不会与 border 融合
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE.toFloat())
        // 设置环绕方向T，截取纹理坐标到 [1/2n,1-1/2n], 将导致永远不会与 border 融合
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE.toFloat())

        // 4. 把 0 绑定到 GL_TEXTURE_EXTERNAL_OES，解绑了我们的纹理
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0)
        return tex[0]
    }

    fun readShaderFromResource(context: Context, resourceId: Int): String {
        val builder = StringBuilder()
        val bufferedReader = BufferedReader(InputStreamReader(context.resources.openRawResource(resourceId)))
        bufferedReader.readLines().forEach { builder.append(it + "\n") }
        bufferedReader.close()
        return builder.toString()
    }

}