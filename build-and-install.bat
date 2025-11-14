@echo off
setlocal enabledelayedexpansion

echo ==========================================
echo Flam Assignment - Automated Build Script
echo ==========================================
echo.

echo Checking prerequisites...

where java >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo X Java not found. Please install Java JDK 11+
    exit /b 1
)
echo [OK] Java found

where adb >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo X adb not found. Please install Android SDK Platform Tools
    exit /b 1
)
echo [OK] adb found

if not exist "%LOCALAPPDATA%\Android\Sdk" (
    if not defined ANDROID_HOME (
        echo X Android SDK not found
        echo Please set ANDROID_HOME or install Android Studio
        exit /b 1
    )
)
echo [OK] Android SDK found

if not exist "local.properties" (
    echo.
    echo Creating local.properties...
    
    if exist "%LOCALAPPDATA%\Android\Sdk" (
        set SDK_PATH=%LOCALAPPDATA%\Android\Sdk
    ) else (
        set SDK_PATH=%ANDROID_HOME%
    )
    
    echo sdk.dir=!SDK_PATH:\=\\!> local.properties
    
    echo.
    echo [!] OpenCV Android SDK Required
    echo Please download from: https://opencv.org/releases/
    echo Extract and add this line to local.properties:
    echo opencv.dir=C:\\path\\to\\opencv-android-sdk
    echo.
    pause
)

echo.
echo Checking for connected Android device...
adb devices

adb devices | find "device" >nul
if %ERRORLEVEL% NEQ 0 (
    echo X No Android device connected
    echo Please connect device with USB debugging enabled
    exit /b 1
)
echo [OK] Device connected

echo.
echo Building APK...
call gradlew.bat assembleDebug

if not exist "app\build\outputs\apk\debug\app-debug.apk" (
    echo X Build failed
    exit /b 1
)

echo.
echo [OK] Build successful!
echo.
echo Installing APK to device...
adb install -r app\build\outputs\apk\debug\app-debug.apk

echo.
echo ==========================================
echo [OK] Installation Complete!
echo ==========================================
echo.
echo Open the 'Flam App' on your device
echo Grant camera permission when prompted
echo.
pause
