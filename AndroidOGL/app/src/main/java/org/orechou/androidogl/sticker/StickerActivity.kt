package org.orechou.androidogl.sticker

import android.hardware.Camera
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.Button
import org.orechou.androidogl.R
import org.orechou.androidogl.util.EGLUtils
import zeusees.tracking.FaceTracking

class StickerActivity : AppCompatActivity(), View.OnClickListener {

    private val TAG = "StickerActivity"

    private var mMultiTrack106: FaceTracking? = null
    private var isTracking = false

    private var mNv21Data: ByteArray? = null
    private var mFrameTexture: GLFrameTexture? = null
    private var mFrame: GLFrame? = null
    private var mPoints: GLPoints? = null
    private var mSticker: GLSticker? = null

    private var mCameraProxy: CameraProxy? = null

    private var mSurfaceView: SurfaceView? = null
    private var mBtnTrack: Button? = null
    private var mBtnSticker: Button? = null
    private var mBtnNext: Button? = null

    private var isShowTrack = false
    private var isShowSticker = false
    private var position = 0

    private var mRenderThread: HandlerThread? = null
    private var mHandler: Handler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sticker)
        init()
    }

    private fun init() {
        // 1. init model
        mMultiTrack106 = FaceTracking("/sdcard/ZeuseesFaceTracking/models")

        mCameraProxy = CameraProxy(this)
        mNv21Data = ByteArray(mCameraProxy!!.PREVIEW_WIDTH * mCameraProxy!!.PREVIEW_HEIGHT * 2)
        mFrameTexture = GLFrameTexture()
        mFrame = GLFrame()
        mPoints = GLPoints()
        mSticker = GLSticker(this, R.drawable.fengj)

        mRenderThread = HandlerThread("RenderThread")
        mRenderThread!!.start()
        mHandler = Handler(mRenderThread!!.looper)

        mCameraProxy!!.setPreviewCallback(mPreviewCallback)
        mSurfaceView = findViewById(R.id.surface_view)
        mSurfaceView!!.holder.addCallback(mSurfaceCallBack)

        mBtnTrack = findViewById(R.id.btn_track)
        mBtnSticker = findViewById(R.id.btn_sticker)
        mBtnNext = findViewById(R.id.btn_next)
        mBtnTrack!!.setOnClickListener(this)
        mBtnSticker!!.setOnClickListener(this)
        mBtnNext!!.setOnClickListener(this)
    }

    private val mSurfaceCallBack = object : SurfaceHolder.Callback {

        override fun surfaceCreated(holder: SurfaceHolder) {
        }

        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            Log.d(TAG, "surfaceChanged")
            mHandler!!.post {
                EGLUtils.initEGL(holder.surface)
                mFrameTexture!!.initFrameTexture()
                mFrame!!.initFrame()
                mFrame!!.setSize(width, height, mCameraProxy!!.PREVIEW_HEIGHT, mCameraProxy!!.PREVIEW_WIDTH)
                mPoints!!.initPoints()
                mSticker!!.initFrame(mCameraProxy!!.PREVIEW_HEIGHT, mCameraProxy!!.PREVIEW_WIDTH)
                mCameraProxy!!.openCamera(mFrameTexture!!.getSurfaceTexture())
            }
        }

        override fun surfaceDestroyed(holder: SurfaceHolder) {
            mHandler!!.post {
                mCameraProxy!!.release()
                mFrameTexture!!.release()
                mFrame!!.release()
                mPoints!!.release()
                mSticker!!.release()
            }
        }
    }

    private val lockObj = Any()
    private val mPreviewCallback = object : Camera.PreviewCallback {
        override fun onPreviewFrame(data: ByteArray?, camera: Camera?) {
            synchronized(lockObj) {
                System.arraycopy(data!!, 0, mNv21Data, 0, data.size)
            }
            mHandler!!.post {
                if (isTracking) {
                    mMultiTrack106!!.FaceTrackingInit(mNv21Data, mCameraProxy!!.PREVIEW_HEIGHT, mCameraProxy!!.PREVIEW_WIDTH)
                    isTracking = !isTracking
                } else {
                    mMultiTrack106!!.Update(mNv21Data, mCameraProxy!!.PREVIEW_HEIGHT, mCameraProxy!!.PREVIEW_WIDTH)
                }
                val faceActions = mMultiTrack106!!.trackingInfo
                var location: FloatArray? = null
                var points: FloatArray? = null
                for (face in faceActions) {
                    points = FloatArray(106 * 2)
                    for (i in 0..105) {

                        val x = face.landmarks[i * 2]
                        val y = face.landmarks[i * 2 + 1]

                        points[i * 2] = view2OpenglX(x, mCameraProxy!!.PREVIEW_HEIGHT)
                        points[i * 2 + 1] = view2OpenglY(y, mCameraProxy!!.PREVIEW_WIDTH)

                        if (i == position) {
                            location = FloatArray(8)
                            location[0] = view2OpenglX(x + 20, mCameraProxy!!.PREVIEW_HEIGHT)
                            location[1] = view2OpenglY(y - 20, mCameraProxy!!.PREVIEW_WIDTH)
                            location[2] = view2OpenglX(x - 20, mCameraProxy!!.PREVIEW_HEIGHT)
                            location[3] = view2OpenglY(y - 20, mCameraProxy!!.PREVIEW_WIDTH)
                            location[4] = view2OpenglX(x + 20, mCameraProxy!!.PREVIEW_HEIGHT)
                            location[5] = view2OpenglY(y + 20, mCameraProxy!!.PREVIEW_WIDTH)
                            location[6] = view2OpenglX(x - 20, mCameraProxy!!.PREVIEW_HEIGHT)
                            location[7] = view2OpenglY(y + 20, mCameraProxy!!.PREVIEW_WIDTH)
                        }
                    }
                    if (location != null) {
                        break
                    }
                }
                var tid = 0
                if (location != null && isShowSticker) {
                    mSticker!!.setPoints(location)
                    tid = mSticker!!.drawFrame()
                }
                mFrame!!.drawFrame(tid, mFrameTexture!!.drawFrameTexture(), mFrameTexture!!.getMatrix())
                if (points != null && isShowTrack) {
                    mPoints!!.setPoints(points!!)
                    mPoints!!.drawPoints()
                }

                EGLUtils.swap()
            }
        }
    }

    private fun view2OpenglX(x: Int, width: Int): Float {
        val centerX = width / 2.0f
        val t = x - centerX
        return t / centerX
    }

    private fun view2OpenglY(y: Int, height: Int): Float {
        val centerY = height / 2.0f
        val s = centerY - y
        return s / centerY
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.btn_track -> isShowTrack = !isShowTrack
            R.id.btn_sticker -> isShowSticker = !isShowSticker
            R.id.btn_next -> {
                Log.d(TAG, "Position: " + position)
                position = (position + 1) % 106
            }
        }
    }
}
