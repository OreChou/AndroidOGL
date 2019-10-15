package org.orechou.androidogl.util

import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.view.Surface

object EGLUtils {

    private val EGL_RECORDABLE_ANDROID = 0x3142

    private var mEglSurface = EGL14.EGL_NO_SURFACE
    private var meEglContext = EGL14.EGL_NO_CONTEXT
    private var mEglDisplay = EGL14.EGL_NO_DISPLAY

    fun initEGL(surface: Surface) {
        mEglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        val version = IntArray(2)
        EGL14.eglInitialize(mEglDisplay, version, 0, version, 1)
        val confAttr = intArrayOf(
            EGL14.EGL_SURFACE_TYPE, EGL14.EGL_WINDOW_BIT,
            EGL14.EGL_RED_SIZE, 8,
            EGL14.EGL_GREEN_SIZE, 8,
            EGL14.EGL_BLUE_SIZE, 8,
            EGL14.EGL_ALPHA_SIZE, 8,
            EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
            EGL_RECORDABLE_ANDROID, 1,
            EGL14.EGL_SAMPLE_BUFFERS, 1,
            EGL14.EGL_SAMPLES, 4,
            EGL14.EGL_NONE
        )

        val configs = arrayOfNulls<EGLConfig>(1)
        val numConfigs = IntArray(1)
        EGL14.eglChooseConfig(mEglDisplay, confAttr, 0, configs, 0, 1, numConfigs, 0)
        val ctxAttr = intArrayOf(
            EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, // 0x3098
            EGL14.EGL_NONE
        )
        meEglContext = EGL14.eglCreateContext(mEglDisplay, configs[0], EGL14.EGL_NO_CONTEXT, ctxAttr, 0)
        val surfaceAttr = intArrayOf(EGL14.EGL_NONE)
        mEglSurface = EGL14.eglCreateWindowSurface(mEglDisplay, configs[0], surface, surfaceAttr, 0)

        EGL14.eglMakeCurrent(mEglDisplay, mEglSurface, mEglSurface, meEglContext)
    }

    fun getContext(): EGLContext {
        return meEglContext
    }

    fun swap() {
        EGL14.eglSwapBuffers(mEglDisplay, mEglSurface)
    }

    fun release() {
        if (mEglSurface !== EGL14.EGL_NO_SURFACE) {
            EGL14.eglMakeCurrent(mEglDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT)
            EGL14.eglDestroySurface(mEglDisplay, mEglSurface)
            mEglSurface = EGL14.EGL_NO_SURFACE
        }
        if (meEglContext !== EGL14.EGL_NO_CONTEXT) {
            EGL14.eglDestroyContext(mEglDisplay, meEglContext)
            meEglContext = EGL14.EGL_NO_CONTEXT
        }
        if (mEglDisplay !== EGL14.EGL_NO_DISPLAY) {
            EGL14.eglTerminate(mEglDisplay)
            mEglDisplay = EGL14.EGL_NO_DISPLAY
        }
    }

}