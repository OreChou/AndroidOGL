package org.orechou.androidogl.camera

import android.graphics.SurfaceTexture
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import org.orechou.androidogl.R

class CameraActivity : AppCompatActivity() {
    private val TAG = "CameraActivity"

    private var mSurfaceView: SurfaceView? = null
    private val mCamera: CameraV1 = CameraV1(this)
    private var mSurfaceTexture: SurfaceTexture? = null
    private var mRenderThread: RenderThread = RenderThread()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        mSurfaceView = findViewById(R.id.surface_view)
        mSurfaceView!!.holder.addCallback(mSurfaceCallBack)
    }

    private fun initRenderThread() {
        mRenderThread.init(mSurfaceView!!, this)
        mSurfaceTexture = mRenderThread.getSurfaceTexture()
        mSurfaceTexture!!.setOnFrameAvailableListener(mRenderThread)
    }

    private val mSurfaceCallBack = object : SurfaceHolder.Callback {
        override fun surfaceCreated(holder: SurfaceHolder) {
            initRenderThread()
            mCamera.open()
            mCamera.setPreviewTexture(mSurfaceTexture!!)
            mCamera.startPreview()
        }

        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {

        }

        override fun surfaceDestroyed(holder: SurfaceHolder) {
        }
    }

}
