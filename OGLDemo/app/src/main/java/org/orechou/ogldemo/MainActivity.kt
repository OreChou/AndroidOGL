package org.orechou.ogldemo

import android.graphics.SurfaceTexture
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.TextureView

class MainActivity : AppCompatActivity() {

    private var mTextureView: TextureView? = null
    private var mRenderThread: RenderThread? = null
    private var mCamera: CameraV1? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mTextureView = findViewById(R.id.texture_view)
        mTextureView!!.surfaceTextureListener = mPreviewTextureListener
    }

    private var mPreviewTextureListener: TextureView.SurfaceTextureListener =
        object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                mRenderThread = RenderThread()
                mRenderThread!!.init(mTextureView!!, this@MainActivity)

                mCamera = CameraV1(this@MainActivity)
                mCamera!!.open()
                mCamera!!.setPreviewTexture(mRenderThread?.mOESSurfaceTexture!!)
                mCamera!!.startPreview()
            }

            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                mCamera!!.stopPreview()
                mCamera!!.release()
                return true
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
        }
}
