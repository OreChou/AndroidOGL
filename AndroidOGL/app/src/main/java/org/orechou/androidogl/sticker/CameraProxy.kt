package org.orechou.androidogl.sticker

import android.content.Context
import android.content.res.Configuration
import android.graphics.SurfaceTexture
import android.hardware.Camera

class CameraProxy(context: Context) {

    private val mContext = context
    private var mCamera: Camera? = null
    private var mCameraInfo: Camera.CameraInfo? = null

    val PREVIEW_WIDTH = 640
    val PREVIEW_HEIGHT = 480
    private val CameraFacing = Camera.CameraInfo.CAMERA_FACING_FRONT
    private var mPreviewCallback: Camera.PreviewCallback? = null

    fun openCamera(surfaceTexture: SurfaceTexture) {
        if (null != mCamera) {
            mCamera!!.setPreviewCallback(null)
            mCamera!!.stopPreview()
            mCamera!!.release()
            mCamera = null
        }
        val info = Camera.CameraInfo()
        for (i in 0 until Camera.getNumberOfCameras()) {
            Camera.getCameraInfo(i, info)
            if (info.facing == CameraFacing) {
                try {
                    mCamera = Camera.open(i)
                    mCameraInfo = info
                } catch (e: RuntimeException) {
                    e.printStackTrace()
                    mCamera = null
                    continue
                }

                break
            }
        }
        try {
            mCamera!!.setPreviewTexture(surfaceTexture)
            initCamera()
        } catch (ex: Exception) {
            if (null != mCamera) {
                mCamera!!.release()
                mCamera = null
            }
        }
    }

    private fun initCamera() {
        try {
            val parameters = mCamera!!.getParameters()
            val flashModes = parameters.supportedFlashModes
            if (flashModes != null && flashModes.contains(Camera.Parameters.FLASH_MODE_OFF)) {
                parameters.flashMode = Camera.Parameters.FLASH_MODE_OFF
            }
            val pictureSizes = mCamera!!.getParameters().supportedPictureSizes
            parameters.setPreviewSize(PREVIEW_WIDTH, PREVIEW_HEIGHT)
            var fs: Camera.Size? = null
            for (i in pictureSizes.indices) {
                val psize = pictureSizes[i]
                if (fs == null && psize.width >= 1280)
                    fs = psize

            }
            parameters.setPictureSize(fs!!.width, fs.height)
            if (mContext.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
                parameters.set("orientation", "portrait")
                parameters.set("rotation", 90)

                val orientation = if (CameraFacing == Camera.CameraInfo.CAMERA_FACING_FRONT) 360 - mCameraInfo!!.orientation else mCameraInfo!!.orientation
                mCamera!!.setDisplayOrientation(orientation)

            } else {
                parameters.set("orientation", "landscape")
                mCamera!!.setDisplayOrientation(0)
            }
            if (CameraFacing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                if (parameters.supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                    parameters.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO
                } else {
                    parameters.focusMode = Camera.Parameters.FOCUS_MODE_AUTO
                }
            }
            mCamera!!.setParameters(parameters)
            mCamera!!.setPreviewCallback(this.mPreviewCallback)
            mCamera!!.startPreview()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setPreviewCallback(previewCallback: Camera.PreviewCallback) {
        this.mPreviewCallback = previewCallback
        mCamera?.setPreviewCallback(previewCallback)
    }

    fun getOrientation(): Int {
        return if (mCameraInfo != null) { mCameraInfo!!.orientation } else 0
    }

    fun release() {
        mCamera!!.setPreviewCallback(null)
        mCamera!!.stopPreview()
        mCamera!!.release()
        mCamera = null
    }

}
