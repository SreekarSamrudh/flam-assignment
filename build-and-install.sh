#!/bin/bash

echo "=========================================="
echo "Flam Assignment - Automated Build Script"
echo "=========================================="
echo ""

check_command() {
    if ! command -v $1 &> /dev/null; then
        echo "❌ $1 not found. Please install $2"
        exit 1
    else
        echo "✓ $1 found"
    fi
}

echo "Checking prerequisites..."
check_command "java" "Java JDK 11+"
check_command "adb" "Android SDK Platform Tools"

if [ ! -d "$HOME/Library/Android/sdk" ] && [ ! -d "$ANDROID_HOME" ]; then
    echo "❌ Android SDK not found"
    echo "Please set ANDROID_HOME or install Android Studio"
    exit 1
fi
echo "✓ Android SDK found"

if [ ! -f "local.properties" ]; then
    echo ""
    echo "Creating local.properties..."
    
    if [ -d "$HOME/Library/Android/sdk" ]; then
        SDK_PATH="$HOME/Library/Android/sdk"
    else
        SDK_PATH="$ANDROID_HOME"
    fi
    
    echo "sdk.dir=$SDK_PATH" > local.properties
    
    echo ""
    echo "⚠️  OpenCV Android SDK Required"
    echo "Please download from: https://opencv.org/releases/"
    echo "Extract and add this line to local.properties:"
    echo "opencv.dir=/path/to/opencv-android-sdk"
    echo ""
    read -p "Press Enter after setting up OpenCV path..."
fi

echo ""
echo "Checking for connected Android device..."
adb devices

DEVICE_COUNT=$(adb devices | grep -v "List" | grep "device" | wc -l)
if [ $DEVICE_COUNT -eq 0 ]; then
    echo "❌ No Android device connected"
    echo "Please connect device with USB debugging enabled"
    exit 1
fi
echo "✓ Device connected"

echo ""
echo "Building APK..."
chmod +x gradlew
./gradlew assembleDebug

if [ ! -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
    echo "❌ Build failed"
    exit 1
fi

echo ""
echo "✓ Build successful!"
echo ""
echo "Installing APK to device..."
adb install -r app/build/outputs/apk/debug/app-debug.apk

echo ""
echo "=========================================="
echo "✓ Installation Complete!"
echo "=========================================="
echo ""
echo "Open the 'Flam App' on your device"
echo "Grant camera permission when prompted"
echo ""
