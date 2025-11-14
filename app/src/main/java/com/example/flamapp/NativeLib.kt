package com.example.flamapp

object NativeLib {
    init {
        System.loadLibrary("native-lib")
    }
    
    external fun processFrame(input: ByteArray, width: Int, height: Int, processEnabled: Boolean): ByteArray
}
