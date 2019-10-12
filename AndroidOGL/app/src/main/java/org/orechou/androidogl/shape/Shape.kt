package org.orechou.androidogl.shape

import android.content.Context
import android.opengl.GLSurfaceView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

abstract class Shape(context: Context): GLSurfaceView.Renderer {
    val mContext: Context = context
}