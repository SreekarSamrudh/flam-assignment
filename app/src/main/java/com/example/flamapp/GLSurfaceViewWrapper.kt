package com.example.flamapp

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet

class GLSurfaceViewWrapper : GLSurfaceView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
}
