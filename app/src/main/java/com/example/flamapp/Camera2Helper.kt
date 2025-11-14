package com.example.flamapp

import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Handler
import android.util.Size
import java.nio.ByteBuffer

class Camera2Helper(
    private val context: Context,
    private val backgroundHandler: Handler,
    private val onFrameAvailable: (ByteArray, Int, Int) -> Unit
) {
    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null
    private var imageReader: ImageReader? = null
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private var cameraId: String = ""
    
    init {
        setupCamera()
    }
    
    private fun setupCamera() {
        for (id in cameraManager.cameraIdList) {
            val characteristics = cameraManager.getCameraCharacteristics(id)
            val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
            if (facing == CameraCharacteristics.LENS_FACING_BACK) {
                cameraId = id
                break
            }
        }
    }
    
    fun openCamera() {
        try {
            cameraManager.openCamera(cameraId, stateCallback, backgroundHandler)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
    
    fun closeCamera() {
        captureSession?.close()
        captureSession = null
        cameraDevice?.close()
        cameraDevice = null
        imageReader?.close()
        imageReader = null
    }
    
    private val stateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            cameraDevice = camera
            createCaptureSession()
        }
        
        override fun onDisconnected(camera: CameraDevice) {
            camera.close()
            cameraDevice = null
        }
        
        override fun onError(camera: CameraDevice, error: Int) {
            camera.close()
            cameraDevice = null
        }
    }
    
    private fun createCaptureSession() {
        val size = Size(640, 480)
        
        imageReader = ImageReader.newInstance(size.width, size.height, ImageFormat.YUV_420_888, 2)
        imageReader?.setOnImageAvailableListener({ reader ->
            val image = reader.acquireLatestImage()
            if (image != null) {
                val yuvBytes = yuv420ToNV21(image)
                onFrameAvailable(yuvBytes, image.width, image.height)
                image.close()
            }
        }, backgroundHandler)
        
        val surface = imageReader?.surface
        if (surface != null) {
            val captureRequestBuilder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            captureRequestBuilder?.addTarget(surface)
            
            cameraDevice?.createCaptureSession(
                listOf(surface),
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) {
                        captureSession = session
                        captureRequestBuilder?.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
                        captureRequestBuilder?.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON)
                        
                        try {
                            session.setRepeatingRequest(captureRequestBuilder!!.build(), null, backgroundHandler)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    
                    override fun onConfigureFailed(session: CameraCaptureSession) {
                    }
                },
                backgroundHandler
            )
        }
    }
    
    private fun yuv420ToNV21(image: android.media.Image): ByteArray {
        val width = image.width
        val height = image.height
        val ySize = width * height
        val uvSize = width * height / 4
        
        val nv21 = ByteArray(ySize + uvSize * 2)
        
        val yBuffer = image.planes[0].buffer
        val uBuffer = image.planes[1].buffer
        val vBuffer = image.planes[2].buffer
        
        var rowStride = image.planes[0].rowStride
        var pixelStride = image.planes[0].pixelStride
        
        var pos = 0
        if (pixelStride == 1) {
            for (row in 0 until height) {
                yBuffer.position(row * rowStride)
                yBuffer.get(nv21, pos, width)
                pos += width
            }
        }
        
        rowStride = image.planes[2].rowStride
        pixelStride = image.planes[2].pixelStride
        
        for (row in 0 until height / 2) {
            for (col in 0 until width / 2) {
                val vuPos = col * pixelStride + row * rowStride
                nv21[pos++] = vBuffer.get(vuPos)
                nv21[pos++] = uBuffer.get(vuPos)
            }
        }
        
        return nv21
    }
}
