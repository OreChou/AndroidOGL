package org.orechou.androidogl.camera

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.EGLSurface
import android.opengl.GLES20
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.util.Log
import android.view.SurfaceView
import org.orechou.androidogl.egl.EGLManager
import org.orechou.androidogl.util.GLUtils

class RenderThread: SurfaceTexture.OnFrameAvailableListener {

    private val TAG = "RenderThread"
    private val MSG_INIT = 1
    private val MSG_RENDER = 2
    private val MSG_DESTROY = 3

    private var mContext: Context? = null
    private var mSurfaceView: SurfaceView? = null
    private var textureId = 0
    private var mEGLSurface: EGLSurface? = null
    private var mSurfaceTexture: SurfaceTexture? = null

    private var mHandlerThread: HandlerThread? = null
    private var mHandler: Handler? = null

    private val mEGLManager: EGLManager = EGLManager()
    private val transformMatrix = FloatArray(16)
    private var mRenderEngine: RenderEngine? = null

    fun init(surfaceView: SurfaceView, context: Context) {
        mSurfaceView = surfaceView
        mContext = context
        initRenderThread()
    }

    fun getSurfaceTexture(): SurfaceTexture{
        return mSurfaceTexture!!
    }

    private fun initRenderThread() {
        initEGL()

        mHandlerThread = HandlerThread("RenderThread")
        mHandlerThread!!.start()
        mHandler = object : Handler(mHandlerThread!!.looper) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    MSG_INIT -> initEGL()
                    MSG_RENDER -> drawFrame()
                    MSG_DESTROY -> return
                    else -> return
                }
            }
        }

        mRenderEngine = RenderEngine(textureId, mContext!!)
    }

    private fun initEGL() {
        textureId = GLUtils.createOESTexture()
        mSurfaceTexture = SurfaceTexture(textureId)
        mEGLSurface = mEGLManager.createWindowSurface(mSurfaceView!!.holder.surface)
        mEGLManager.eglMakeCurrent()

    }

    private fun drawFrame() {
        val t1 = System.currentTimeMillis()
        mSurfaceTexture!!.updateTexImage()
        mSurfaceTexture!!.getTransformMatrix(transformMatrix)
        mEGLManager!!.eglMakeCurrent()
        GLES20.glViewport(0, 0, mSurfaceView!!.width, mSurfaceView!!.height)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glClearColor(1f, 1f, 0f, 0f)
        mRenderEngine!!.drawTexture(transformMatrix)
        mEGLManager!!.eglSwapBuffers()
        val t2 = System.currentTimeMillis()
        Log.i(TAG, "drawFrame: time = " + (t2 - t1))
    }

    override fun onFrameAvailable(surfaceTexture: SurfaceTexture?) {
//        mHandler!!.sendEmptyMessage(MSG_RENDER)
        drawFrame()
    }
}