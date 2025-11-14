package com.example.flamapp

object ShaderSources {
    const val vertexShader = """
        attribute vec4 aPosition;
        attribute vec2 aTexCoord;
        varying vec2 vTexCoord;
        void main() {
            gl_Position = aPosition;
            vTexCoord = aTexCoord;
        }
    """
    
    const val fragmentPassShader = """
        precision mediump float;
        varying vec2 vTexCoord;
        uniform sampler2D uTexture;
        void main() {
            gl_FragColor = texture2D(uTexture, vTexCoord);
        }
    """
    
    const val fragmentEffectsShader = """
        precision mediump float;
        varying vec2 vTexCoord;
        uniform sampler2D uTexture;
        uniform int uEffectMode;
        void main() {
            vec4 color = texture2D(uTexture, vTexCoord);
            if (uEffectMode == 1) {
                float gray = dot(color.rgb, vec3(0.299, 0.587, 0.114));
                gl_FragColor = vec4(gray, gray, gray, color.a);
            } else if (uEffectMode == 2) {
                gl_FragColor = vec4(1.0 - color.rgb, color.a);
            } else {
                gl_FragColor = color;
            }
        }
    """
}
