@echo off
echo Flam Assignment - Environment Check
echo ====================================
echo.

set ISSUES=0

echo Checking Android SDK...
if exist "%LOCALAPPDATA%\Android\Sdk" (
    echo [OK] Android SDK found
) else if defined ANDROID_HOME (
    echo [OK] Android SDK found at ANDROID_HOME
) else (
    echo [X] Android SDK not found
    echo     Install Android Studio or set ANDROID_HOME
    set /a ISSUES+=1
)

echo.
echo Checking Java...
where java >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo [OK] Java found
    java -version 2>&1 | findstr "version"
) else (
    echo [X] Java not found
    echo     Install Java JDK 11 or higher
    set /a ISSUES+=1
)

echo.
echo Checking adb (Android Debug Bridge)...
where adb >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo [OK] adb found
    echo.
    echo Connected devices:
    adb devices
) else (
    echo [X] adb not found
    echo     Install Android SDK Platform Tools
    set /a ISSUES+=1
)

echo.
echo Checking local.properties...
if exist "local.properties" (
    echo [OK] local.properties exists
    findstr /C:"opencv.dir" local.properties >nul
    if %ERRORLEVEL% EQU 0 (
        echo [OK] OpenCV path configured
    ) else (
        echo [X] OpenCV path not configured
        echo     Add opencv.dir=C:\path\to\opencv-android-sdk to local.properties
        set /a ISSUES+=1
    )
) else (
    echo [X] local.properties not found
    echo     Run build-and-install.bat to create it
    set /a ISSUES+=1
)

echo.
echo Checking OpenCV SDK...
if exist "local.properties" (
    for /f "tokens=2 delims==" %%a in ('findstr "opencv.dir" local.properties') do set OPENCV_PATH=%%a
    if exist "!OPENCV_PATH!" (
        echo [OK] OpenCV SDK found
    ) else (
        echo [X] OpenCV SDK not found
        echo     Download from https://opencv.org/releases/
        set /a ISSUES+=1
    )
)

echo.
echo ====================================
if %ISSUES% EQU 0 (
    echo [OK] All checks passed!
    echo Ready to build. Run: build-and-install.bat
) else (
    echo [X] Found %ISSUES% issue(s)
    echo Please fix the issues above before building
)
echo.
pause
