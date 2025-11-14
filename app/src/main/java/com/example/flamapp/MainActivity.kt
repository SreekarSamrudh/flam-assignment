package com.example.flamapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private lateinit var glSurfaceView: GLSurfaceViewWrapper
    private lateinit var glRenderer: GLRenderer
    private lateinit var camera2Helper: Camera2Helper
    private lateinit var toggleButton: Button
    private lateinit var fpsTextView: TextView
    private lateinit var backgroundThread: HandlerThread
    private lateinit var backgroundHandler: Handler
    
    private var isProcessingEnabled = true
    private var frameCount = 0
    private var lastFpsTime = System.currentTimeMillis()
    private var currentFps = 0.0
    
    companion object {
        private const val CAMERA_PERMISSION_REQUEST = 100
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        glSurfaceView = findViewById(R.id.glSurfaceView)
        toggleButton = findViewById(R.id.toggleButton)
        fpsTextView = findViewById(R.id.fpsTextView)
        
        glRenderer = GLRenderer()
        glSurfaceView.setEGLContextClientVersion(2)
        glSurfaceView.setRenderer(glRenderer)
        glSurfaceView.renderMode = android.opengl.GLSurfaceView.RENDERMODE_WHEN_DIRTY
        
        toggleButton.setOnClickListener {
            isProcessingEnabled = !isProcessingEnabled
            toggleButton.text = if (isProcessingEnabled) "Toggle: Edge Detection" else "Toggle: Raw Feed"
        }
        
        startBackgroundThread()
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            initCamera()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST)
        }
    }
    
    private fun startBackgroundThread() {
        backgroundThread = HandlerThread("CameraBackground")
        backgroundThread.start()
        backgroundHandler = Handler(backgroundThread.looper)
    }
    
    private fun stopBackgroundThread() {
        backgroundThread.quitSafely()
        try {
            backgroundThread.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }
    
    private fun initCamera() {
        camera2Helper = Camera2Helper(this, backgroundHandler) { imageBytes, width, height ->
            processFrame(imageBytes, width, height)
        }
        camera2Helper.openCamera()
    }
    
    private fun processFrame(imageBytes: ByteArray, width: Int, height: Int) {
        val outputBytes = NativeLib.processFrame(imageBytes, width, height, isProcessingEnabled)
        
        glRenderer.updateTexture(outputBytes, width, height)
        glSurfaceView.requestRender()
        
        updateFps()
    }
    
    private fun updateFps() {
        frameCount++
        val currentTime = System.currentTimeMillis()
        val elapsed = currentTime - lastFpsTime
        
        if (elapsed >= 1000) {
            currentFps = frameCount * 1000.0 / elapsed
            frameCount = 0
            lastFpsTime = currentTime
            
            runOnUiThread {
                fpsTextView.text = String.format("FPS: %.1f", currentFps)
            }
        }
    }
    
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initCamera()
            } else {
                finish()
            }
        }
    }
    
    override fun onPause() {
        super.onPause()
        camera2Helper.closeCamera()
    }
    
    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            if (::camera2Helper.isInitialized) {
                camera2Helper.openCamera()
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopBackgroundThread()
    }
}
