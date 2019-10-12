package org.orechou.ogldemo

import android.graphics.SurfaceTexture
import android.opengl.EGL14
import android.util.Log
import android.view.Surface
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLContext
import javax.microedition.khronos.egl.EGLSurface

class EGLManager {

    private var mEgl: EGL10? = null
    private var mEGLDisplay = EGL10.EGL_NO_DISPLAY
    private var mEGLContext = EGL10.EGL_NO_CONTEXT
    private var mEGLConfig = arrayOfNulls<EGLConfig>(1)
    private var mEglSurface: EGLSurface? = null


    // 构造需要的配置列表（只读）
    private val attributes = intArrayOf(
        EGL10.EGL_RED_SIZE, 8,
        EGL10.EGL_GREEN_SIZE, 8,
        EGL10.EGL_BLUE_SIZE, 8,
        EGL10.EGL_ALPHA_SIZE, 8,
        EGL10.EGL_BUFFER_SIZE, 32,
        EGL10.EGL_RENDERABLE_TYPE, 4,
        EGL10.EGL_SURFACE_TYPE, EGL10.EGL_WINDOW_BIT,
        EGL10.EGL_NONE
    )

    constructor(previewViewTexture: SurfaceTexture) {
        init(previewViewTexture)
    }

    /**
     * 使用 EGL 绘图的基本步骤:
     * 1. 获取 EGL Display 对象（获取显示设备）：eglGetDisplay()
     * 2. 初始化与 EGLDisplay 之间的连接：eglInitialize()
     * 3. 获取 EGLConfig 对象：eglChooseConfig()
     * 4. 创建 EGLContext 实例：eglCreateContext()
     * 5. 创建 EGLSurface 实例：eglCreateWindowSurface()
     * 6. 连接 EGLContext 和 EGLSurface（将 EGL 渲染上下文附加到 EGLSurface 上）：eglMakeCurrent()
     */
    private fun init(previewTexture: SurfaceTexture) {
        mEgl = EGLContext.getEGL() as EGL10

        mEGLDisplay = mEgl!!.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY)
        if (mEGLDisplay === EGL10.EGL_NO_DISPLAY) {
            throw RuntimeException("eglGetDisplay failed! " + mEgl!!.eglGetError())
        }

        var version = IntArray(2)
        if (!mEgl!!.eglInitialize(mEGLDisplay, version)) {
            throw RuntimeException("eglInitialize failed! " + mEgl!!.eglGetError())
        }

        var configsNum = IntArray(1)
        if (!mEgl!!.eglChooseConfig(mEGLDisplay, attributes, mEGLConfig, 1, configsNum)) {
            throw RuntimeException("eglChooseConfig failed! " + mEgl!!.eglGetError())
        }

        mEglSurface = mEgl!!.eglCreateWindowSurface(mEGLDisplay, mEGLConfig[0], previewTexture, null)

        var contextAttributes = intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE)
        mEGLContext = mEgl!!.eglCreateContext(mEGLDisplay, mEGLConfig[0], EGL10.EGL_NO_CONTEXT, contextAttributes)

        if (mEGLDisplay === EGL10.EGL_NO_DISPLAY || mEGLContext === EGL10.EGL_NO_CONTEXT) {
            throw RuntimeException("eglCreateContext fail failed! " + mEgl!!.eglGetError())
        }

        // the second and third parameter is draw and read surface
        if (!mEgl!!.eglMakeCurrent(mEGLDisplay, mEglSurface, mEglSurface, mEGLContext)) {
            throw RuntimeException("eglMakeCurrent failed! " + mEgl!!.eglGetError())
        }
    }

    fun eglMakeCurrent() {
        mEgl!!.eglMakeCurrent(mEGLDisplay, mEglSurface, mEglSurface, mEGLContext)
    }

    fun eglSwapBuffers() {
        mEgl!!.eglSwapBuffers(mEGLDisplay, mEglSurface)
    }

    private var mEGLWindowSurface: EGLSurface? = null

    fun createWindowSurface(surface: Surface): EGLSurface {
        val surfaceAttribs = intArrayOf(EGL10.EGL_NONE)
        mEGLWindowSurface = mEgl!!.eglCreateWindowSurface(mEGLDisplay, mEGLConfig[0], surface, surfaceAttribs)
        val error = mEgl!!.eglGetError()
        if (error != EGL10.EGL_SUCCESS) {

        }
        return mEGLWindowSurface!!
    }

}