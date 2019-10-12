package org.orechou.androidogl.camera

import android.app.Activity
import android.graphics.SurfaceTexture
import android.view.Surface
import android.view.SurfaceHolder

class CameraV1(private val mActivity: Activity) {

    private var mCamera: android.hardware.Camera? = null

    fun open() {
        val cameraId = android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK
        mCamera = android.hardware.Camera.open(cameraId)
        val parameters = mCamera!!.getParameters()
        parameters.set("orientation", "portrait")
        parameters.focusMode = android.hardware.Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
        parameters.setPreviewSize(1280, 720)
        setCameraDisplayOrientation(mActivity, cameraId, mCamera!!)
        mCamera!!.parameters = parameters
    }

    private fun setCameraDisplayOrientation(activity: Activity, cameraId: Int, camera: android.hardware.Camera) {
        val info = android.hardware.Camera.CameraInfo()
        android.hardware.Camera.getCameraInfo(cameraId, info)
        val rotation = activity.windowManager.defaultDisplay.rotation
        var degrees = 0
        when (rotation) {
            Surface.ROTATION_0 -> degrees = 0
            Surface.ROTATION_90 -> degrees = 90
            Surface.ROTATION_180 -> degrees = 180
            Surface.ROTATION_270 -> degrees = 270
        }

        var result: Int
        if (info.facing == android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360
            result = (360 - result) % 360  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360
        }
        camera.setDisplayOrientation(result)
    }

    fun startPreview() {
        mCamera!!.startPreview()
    }

    fun stopPreview() {
        mCamera!!.stopPreview()
    }

    fun setPreviewTexture(surfaceTexture: SurfaceTexture) {
        mCamera!!.setPreviewTexture(surfaceTexture)
    }

    fun setPreviewDisplay(surfaceHolder: SurfaceHolder) {
        mCamera!!.setPreviewDisplay(surfaceHolder)
    }

    fun release() {
        mCamera!!.release()
    }

}