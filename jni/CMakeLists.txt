cmake_minimum_required(VERSION 3.18.1)

project("native-lib")

set(OpenCV_DIR "/path/to/opencv-android-sdk/sdk/native/jni" CACHE PATH "Path to OpenCV Android SDK")

find_package(OpenCV REQUIRED)

include_directories(${OpenCV_INCLUDE_DIRS})

add_library(native-lib SHARED
    native-lib.cpp
    process.cpp
)

target_link_libraries(native-lib
    ${OpenCV_LIBS}
    android
    log
)
