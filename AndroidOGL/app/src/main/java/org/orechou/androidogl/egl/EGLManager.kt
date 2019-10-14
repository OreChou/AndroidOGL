package org.orechou.androidogl.egl

import android.opengl.*
import android.util.Log
import android.view.Surface

class EGLManager {

    private val TAG = "EGLManager"

    private var mEGLDisplay: EGLDisplay
    private var mEGLContext: EGLContext
    private var mEGLConfig: EGLConfig
    private var mEglSurface: EGLSurface? = null

    constructor() {
        mEGLDisplay = getEGLDisplay()
        mEGLConfig = getEGLConfig(mEGLDisplay)
        mEGLContext = getEGLContext(mEGLDisplay, mEGLConfig)
        Log.d(TAG, "Create EGLManager finished.")
    }

    private fun getEGLDisplay(): EGLDisplay {
        val display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        val version = IntArray(2)
        EGL14.eglInitialize(display, version, 0, version, 1)
        return display
    }

    private fun getEGLConfig(display: EGLDisplay): EGLConfig {
        val configs = arrayOfNulls<EGLConfig>(1)
        val configSpec = intArrayOf(
            EGL14.EGL_BUFFER_SIZE, 32,
            EGL14.EGL_ALPHA_SIZE, 8,
            EGL14.EGL_BLUE_SIZE, 8,
            EGL14.EGL_GREEN_SIZE, 8,
            EGL14.EGL_RED_SIZE, 8,
            EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
            EGL14.EGL_SURFACE_TYPE, EGL14.EGL_WINDOW_BIT,
            EGL14.EGL_NONE
        )
        val numConfig = intArrayOf(0)
        EGL14.eglChooseConfig(display, configSpec, 0, configs, 0, 1, numConfig, 0)
        if (EGL14.eglGetError() == EGL14.EGL_FALSE || numConfig[0] == 0) {
            Log.d(TAG, "get display config failed")
            throw Exception("get display config failed")
        }
        return configs[0]!!
    }

    private fun getEGLContext(display: EGLDisplay, config: EGLConfig): EGLContext {
        val attribs = intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION, 3, EGL14.EGL_NONE)
        val context = EGL14.eglCreateContext(display, config, EGL14.EGL_NO_CONTEXT, attribs, 0)
        val error = EGL14.eglGetError()
        if (error != EGL14.EGL_SUCCESS) {
            Log.d(TAG, "fail to get EGLContext error: $error")
            throw Exception("fail to get EGLContext error: $error")
        }
        return context
    }

    fun eglMakeCurrent() {
        if (!EGL14.eglMakeCurrent(mEGLDisplay, mEglSurface, mEglSurface, mEGLContext)) {
            Log.d(TAG, "EglMakeCurrent failed. " +  EGL14.eglGetError())
        }
    }

    fun eglSwapBuffers() {
        EGL14.eglSwapBuffers(mEGLDisplay, mEglSurface)
    }

    fun createWindowSurface(surface: Surface): EGLSurface {
        val surfaceAttribs = intArrayOf(EGL14.EGL_NONE)
        mEglSurface = EGL14.eglCreateWindowSurface(mEGLDisplay, mEGLConfig, surface, surfaceAttribs, 0)
        val error = EGL14.eglGetError()
        if (error != EGL14.EGL_SUCCESS) {

        }
        return mEglSurface!!
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
}