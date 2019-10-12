package org.orechou.androidogl.camera

import android.opengl.EGLSurface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import org.orechou.androidogl.R
import org.orechou.androidogl.egl.EGLManager

class CameraActivity : AppCompatActivity() {

    private var mSurfaceView: SurfaceView? = null
    private var mPreviewSurface: EGLSurface? = null
    private val mCamera: CameraV1 = CameraV1(this)
    private val mEGLManager: EGLManager = EGLManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        mSurfaceView = findViewById(R.id.surface_view)
        mSurfaceView!!.holder.addCallback(mSurfaceCallBack)

    }

    private val mSurfaceCallBack = object : SurfaceHolder.Callback {
        override fun surfaceCreated(holder: SurfaceHolder) {
            mPreviewSurface = mEGLManager.createWindowSurface(mSurfaceView!!.holder.surface)
            mEGLManager.eglMakeCurrent()
            mCamera.open()
            mCamera.setPreviewDisplay(holder)
            mCamera.startPreview()
        }

        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {

        }

        override fun surfaceDestroyed(holder: SurfaceHolder) {
        }
    }

}
