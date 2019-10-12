package org.orechou.ogldemo

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES20
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.util.Log
import android.view.TextureView

class RenderThread: SurfaceTexture.OnFrameAvailableListener {

    private val TAG = "RenderThread"
    private val MSG_INIT = 1
    private val MSG_RENDER = 2
    private val MSG_DESTROY = 3

    private var mContext: Context? = null
    private var mPreviewView: TextureView? = null
    private var mOESTextureId: Int = 0
    var mOESSurfaceTexture: SurfaceTexture? = null

    private var mHandlerThread: HandlerThread? = null
    private var mHandler: Handler? = null

    private var mEGLManager: EGLManager? = null
    private var mRenderEngine: RenderEngine? = null

    private val transformMatrix = FloatArray(16)

    fun init(previewView: TextureView, context: Context) {
        mPreviewView = previewView
        mContext = context
        initOESTexture()
        initRenderThread()
    }

    private fun initOESTexture() {
        mOESTextureId = GLUtils.createOESTextureObject()
        mOESSurfaceTexture = SurfaceTexture(mOESTextureId)
        mOESSurfaceTexture!!.setOnFrameAvailableListener(this)
    }

    private fun initRenderThread() {
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
        mHandler!!.sendEmptyMessage(MSG_INIT)
    }

    private fun initEGL() {
        mEGLManager = EGLManager(mPreviewView!!.surfaceTexture)
        mRenderEngine = RenderEngine(mOESTextureId, mContext!!)
    }

    private fun drawFrame() {
        val t1 = System.currentTimeMillis()
        mOESSurfaceTexture!!.updateTexImage()
        mOESSurfaceTexture!!.getTransformMatrix(transformMatrix)
        mEGLManager!!.eglMakeCurrent()
        GLES20.glViewport(0, 0, mPreviewView!!.width, mPreviewView!!.height)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glClearColor(1f, 1f, 0f, 0f)
        mRenderEngine!!.drawTexture(transformMatrix)
        mEGLManager!!.eglSwapBuffers()
        val t2 = System.currentTimeMillis()
        Log.i(TAG, "drawFrame: time = " + (t2 - t1))
    }

    override fun onFrameAvailable(surfaceTexture: SurfaceTexture?) {
        mHandler!!.sendEmptyMessage(MSG_RENDER)
    }
}