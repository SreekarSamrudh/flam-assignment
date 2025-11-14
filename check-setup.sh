#!/bin/bash

echo "Flam Assignment - Environment Check"
echo "===================================="
echo ""

ISSUES=0

echo "Checking Android SDK..."
if [ -d "$HOME/Library/Android/sdk" ] || [ -d "$ANDROID_HOME" ]; then
    echo "✓ Android SDK found"
else
    echo "✗ Android SDK not found"
    echo "  → Install Android Studio or set ANDROID_HOME"
    ISSUES=$((ISSUES+1))
fi

echo ""
echo "Checking Java..."
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1)
    echo "✓ Java found: $JAVA_VERSION"
else
    echo "✗ Java not found"
    echo "  → Install Java JDK 11 or higher"
    ISSUES=$((ISSUES+1))
fi

echo ""
echo "Checking adb (Android Debug Bridge)..."
if command -v adb &> /dev/null; then
    echo "✓ adb found"
    echo ""
    echo "Connected devices:"
    adb devices
else
    echo "✗ adb not found"
    echo "  → Install Android SDK Platform Tools"
    ISSUES=$((ISSUES+1))
fi

echo ""
echo "Checking local.properties..."
if [ -f "local.properties" ]; then
    echo "✓ local.properties exists"
    if grep -q "opencv.dir" local.properties; then
        echo "✓ OpenCV path configured"
    else
        echo "✗ OpenCV path not configured"
        echo "  → Add opencv.dir=/path/to/opencv-android-sdk to local.properties"
        ISSUES=$((ISSUES+1))
    fi
else
    echo "✗ local.properties not found"
    echo "  → Run build script to create it"
    ISSUES=$((ISSUES+1))
fi

echo ""
echo "Checking OpenCV SDK..."
if [ -f "local.properties" ]; then
    OPENCV_PATH=$(grep "opencv.dir" local.properties | cut -d'=' -f2)
    if [ -d "$OPENCV_PATH" ]; then
        echo "✓ OpenCV SDK found at: $OPENCV_PATH"
    else
        echo "✗ OpenCV SDK not found"
        echo "  → Download from https://opencv.org/releases/"
        ISSUES=$((ISSUES+1))
    fi
fi

echo ""
echo "===================================="
if [ $ISSUES -eq 0 ]; then
    echo "✓ All checks passed!"
    echo "Ready to build. Run: ./build-and-install.sh"
else
    echo "✗ Found $ISSUES issue(s)"
    echo "Please fix the issues above before building"
fi
echo ""
