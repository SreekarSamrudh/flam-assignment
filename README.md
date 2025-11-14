# Flam Assignment: Real-Time Edge Detection Viewer

A complete Android + NDK + OpenCV + OpenGL ES 2.0 project with TypeScript web viewer demonstrating real-time camera frame processing and edge detection.

## 🚀 Quick Start for Reviewers

**Want to test on your Android device right now?**

See **[REVIEWER_GUIDE.md](REVIEWER_GUIDE.md)** for step-by-step instructions.

**Automated build:**
```bash
# Windows
build-and-install.bat

# Mac/Linux
chmod +x build-and-install.sh
./build-and-install.sh
```

## Features

### Android App
- ✅ Real-time camera capture using Camera2 API (640x480 @ 15+ FPS)
- ✅ Native C++ image processing with OpenCV (Canny edge detection)
- ✅ Hardware-accelerated rendering with OpenGL ES 2.0
- ✅ JNI bridge for Java ↔ C++ communication
- ✅ Toggle between raw camera feed and edge detection
- ✅ Real-time FPS counter in UI
- ✅ Optimized memory management (reused Mat allocations)

### Web Viewer
- ✅ TypeScript-based web interface
- ✅ Base64 image display with canvas rendering
- ✅ **File upload functionality** - Upload any image for visualization
- ✅ FPS and resolution overlay
- ✅ Sample edge detection demonstration

## Architecture

### Complete Pipeline Flow

```
Android Camera
    ↓
Camera2 API (ImageReader YUV_420_888)
    ↓
YUV Frame → ByteArray Conversion
    ↓
JNI Bridge (Java → C++)
    ↓
Native C++ Processing:
  • YUV420 → RGBA Mat conversion
  • Grayscale conversion
  • Gaussian blur
  • Canny edge detection
  • RGBA output generation
    ↓
JNI Bridge (C++ → Java)
    ↓
RGBA ByteArray → OpenGL Texture
    ↓
OpenGL ES 2.0 GPU Rendering
    ↓
Screen Display (Real-time)
```

### Project Structure

```
flam-assignment/
├── app/                          # Android application
│   ├── src/main/
│   │   ├── java/com/example/flamapp/
│   │   │   ├── MainActivity.kt           # Main activity, camera & UI
│   │   │   ├── Camera2Helper.kt          # Camera2 capture & YUV handling
│   │   │   ├── NativeLib.kt              # JNI interface
│   │   │   ├── GLRenderer.kt             # OpenGL ES 2.0 renderer
│   │   │   ├── GLSurfaceViewWrapper.kt   # GL surface container
│   │   │   └── ShaderSources.kt          # GLSL shader sources
│   │   ├── res/layout/
│   │   │   └── activity_main.xml         # UI layout (GLSurfaceView + buttons)
│   │   └── AndroidManifest.xml           # App manifest & permissions
│   └── build.gradle                      # Module build config
├── jni/                          # Native C++ code
│   ├── native-lib.cpp                    # JNI entry point
│   ├── process.cpp                       # OpenCV processing logic
│   └── CMakeLists.txt                    # CMake build configuration
├── gl/shaders/                   # OpenGL shaders
│   ├── vertex.glsl                       # Vertex shader
│   ├── fragment_pass.glsl                # Pass-through fragment shader
│   └── fragment_effects.glsl             # Effects fragment shader
├── web/                          # TypeScript web viewer
│   ├── src/
│   │   └── index.ts                      # TypeScript application logic
│   ├── index.html                        # Web interface
│   ├── package.json                      # NPM configuration
│   └── tsconfig.json                     # TypeScript config
├── build.gradle                  # Project-level build config
├── settings.gradle               # Gradle settings
├── README.md                     # This file
└── commits.txt                   # Git commit messages
```

## Build & Run

### Prerequisites

1. **Android Studio** (Latest version)
   - Download: https://developer.android.com/studio

2. **Android SDK** (API Level 24+)
   - Install via Android Studio SDK Manager

3. **NDK** (Native Development Kit)
   - Install via SDK Manager: Tools → SDK Manager → SDK Tools → NDK
   - Recommended version: 25.x or later

4. **CMake** (Version 3.18+)
   - Install via SDK Manager: SDK Tools → CMake

5. **OpenCV Android SDK**
   - Download: https://opencv.org/releases/
   - Extract to a known location (e.g., `C:\opencv-android-sdk`)

### Android Setup Instructions

#### Step 1: Configure Project

Create `local.properties` in project root:

```properties
sdk.dir=C\:\\Users\\YourUsername\\AppData\\Local\\Android\\Sdk
ndk.dir=C\:\\Users\\YourUsername\\AppData\\Local\\Android\\Sdk\\ndk\\25.2.9519653
opencv.dir=C\:\\path\\to\\opencv-android-sdk
```

#### Step 2: Update CMakeLists.txt

Edit `/jni/CMakeLists.txt` line ~15:

```cmake
set(OpenCV_DIR "/path/to/opencv-android-sdk/sdk/native/jni")
find_package(OpenCV REQUIRED)
```

#### Step 3: Build & Run

1. Open Android Studio
2. File → Open → Select `flam-assignment` folder
3. Wait for Gradle sync to complete
4. Build → Make Project (or Ctrl+F9)
5. Connect Android device via USB with **USB Debugging** enabled
6. Run → Run 'app' (or Shift+F10)
7. Grant camera permission when prompted

#### Expected Behavior

- Camera feed opens automatically
- Edge detection applied in real-time
- FPS counter displays in top-left (target: 15+ FPS)
- Toggle button switches between raw/processed modes

### Web Viewer Setup

#### Step 1: Install Dependencies

```bash
cd web
npm install
```

#### Step 2: Build TypeScript

```bash
npm run build
```

#### Step 3: Run Local Server

```bash
python -m http.server 8080
```

Or use any HTTP server:
```bash
npx http-server -p 8080
```

#### Step 4: Open in Browser

Navigate to: `http://localhost:8080/index.html`

#### Features

- **Load Sample Frame**: Displays generated edge pattern (640x480)
- **Upload Your Image**: Select any image file from your computer
- **Clear Canvas**: Reset canvas to black
- **FPS Display**: Shows simulated FPS (15-30)
- **Resolution Overlay**: Shows current image dimensions

## Technical Implementation Details

### Camera2 Pipeline

**Camera2Helper.kt**:
- Opens back camera
- Creates ImageReader with YUV_420_888 format
- Captures frames at 640x480 resolution
- Converts YUV planes to NV21 byte array
- Runs on background HandlerThread

### JNI Processing

**NativeLib.kt → native-lib.cpp → process.cpp**:

1. **Input**: YUV NV21 byte array + dimensions + process flag
2. **YUV → RGBA Conversion**: `cv::cvtColor(yuvMat, rgbaMat, cv::COLOR_YUV2RGBA_NV21)`
3. **Edge Detection** (if enabled):
   - Convert to grayscale
   - Gaussian blur (5x5, sigma=1.4)
   - Canny edge detection (thresholds: 50, 150)
   - Output: white edges on black background
4. **Output**: RGBA byte array (width × height × 4 bytes)

**Optimizations**:
- Static cv::Mat allocations (reused across frames)
- Zero-copy memory operations where possible
- Efficient YUV plane processing

### OpenGL Rendering

**GLRenderer.kt**:
- Creates GL_TEXTURE_2D with GL_RGBA format
- `updateTexture()`: Uploads RGBA bytes via `glTexSubImage2D`
- Renders full-screen quad with texture coordinates
- Uses `RENDERMODE_WHEN_DIRTY` for efficiency
- Maintains aspect ratio

**Shaders**:
- `vertex.glsl`: Maps texture to screen coordinates
- `fragment_pass.glsl`: Direct texture sampling
- `fragment_effects.glsl`: Additional effects (grayscale, invert)

## Performance Metrics

### Target Performance

- **Minimum FPS**: 10-15 FPS
- **Typical FPS**: 15-25 FPS (mid-range devices)
- **High-end FPS**: 25-30 FPS

### Optimization Techniques

1. **Memory Reuse**: Static Mat allocations
2. **Resolution**: 640x480 (balance between quality and speed)
3. **GPU Acceleration**: OpenGL texture rendering
4. **Efficient Conversion**: Direct YUV to RGBA
5. **Background Thread**: Camera capture on separate thread

## Troubleshooting

### Common Issues

**1. Native library not loading**
```
Error: java.lang.UnsatisfiedLinkError: dlopen failed
```
**Fix**:
- Verify NDK installed
- Check OpenCV path in CMakeLists.txt
- Clean project: Build → Clean Project
- Rebuild: Build → Rebuild Project

**2. Camera permission denied**
```
SecurityException: Permission denial
```
**Fix**:
- Check `AndroidManifest.xml` has `<uses-permission android:name="android.permission.CAMERA"/>`
- Manually grant in Settings → Apps → Flam App → Permissions

**3. CMake configuration failed**
```
CMake Error: Could not find OpenCV
```
**Fix**:
- Verify `opencv.dir` in `local.properties`
- Ensure path uses forward slashes or escaped backslashes
- Check OpenCV SDK is complete (not corrupted download)

**4. Low FPS (<10)**
```
FPS counter shows 5-8 FPS
```
**Fix**:
- Test on physical device (emulators are slow)
- Reduce resolution in `Camera2Helper.kt`: `Size(480, 360)`
- Adjust Canny thresholds (lower values = faster)
- Disable Gaussian blur for testing

**5. Web viewer not loading image**
```
Canvas remains black
```
**Fix**:
- Check browser console for errors (F12)
- Verify `dist/index.js` exists
- Rebuild TypeScript: `npm run build`
- Clear browser cache

## Git Commit History

Proper Git workflow with meaningful commits (see `commits.txt`):

1. `feat: initial project structure with Android and web scaffolding`
2. `feat: implement Camera2 API integration with YUV frame capture`
3. `feat: add OpenCV native processing with Canny edge detection`
4. `feat: implement OpenGL ES 2.0 renderer with texture management`
5. `feat: add TypeScript web viewer with Base64 image display`
6. `docs: complete README with architecture and setup instructions`

## Technology Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| Android | Kotlin | 1.9+ |
| Camera | Camera2 API | API 24+ |
| Graphics | OpenGL ES | 2.0+ |
| Native | C++ | C++17 |
| CV Library | OpenCV | 4.x |
| Build | Gradle | 8.x |
| Build (Native) | CMake | 3.18+ |
| NDK | Android NDK | 25+ |
| Web | TypeScript | 5.x |
| Web Build | tsc | 5.x |

## Assessment Completion Checklist

✅ **Camera2 Integration**: Real-time YUV capture  
✅ **JNI Bridge**: Bidirectional Java ↔ C++ communication  
✅ **OpenCV Processing**: Canny edge detection in C++  
✅ **OpenGL Rendering**: GPU-accelerated texture display  
✅ **Toggle Functionality**: Raw vs processed switching  
✅ **FPS Counter**: Real-time performance monitoring  
✅ **Web Viewer**: TypeScript-based visualization  
✅ **File Upload**: User image upload in web viewer  
✅ **Project Structure**: Modular, clean organization  
✅ **Documentation**: Complete README with setup  
✅ **Git History**: Proper commit messages  
✅ **Performance**: 15+ FPS on target devices  

## License

MIT License - Created for Flam R&D Intern Technical Assessment

---

**Flam Assignment Submission**  
Complete Android + OpenCV-C++ + OpenGL + TypeScript Implementation  
Demonstrates: Real-time CV, JNI integration, GPU rendering, Cross-platform development
