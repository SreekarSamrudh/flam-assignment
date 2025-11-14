#include <jni.h>
#include <opencv2/opencv.hpp>
#include <opencv2/imgproc.hpp>
#include <android/log.h>
#include <vector>

#define LOG_TAG "ProcessFrame"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

static cv::Mat grayMat;
static cv::Mat edgesMat;
static cv::Mat rgbaMat;
static cv::Mat yuvMat;

jbyteArray processFrameNative(JNIEnv* env, jbyteArray input, jint width, jint height, jboolean processEnabled) {
    jbyte* inputBytes = env->GetByteArrayElements(input, nullptr);
    jsize inputLength = env->GetArrayLength(input);
    
    if (yuvMat.empty() || yuvMat.cols != width || yuvMat.rows != height) {
        yuvMat = cv::Mat(height + height / 2, width, CV_8UC1);
    }
    
    memcpy(yuvMat.data, inputBytes, inputLength);
    env->ReleaseByteArrayElements(input, inputBytes, JNI_ABORT);
    
    if (rgbaMat.empty() || rgbaMat.cols != width || rgbaMat.rows != height) {
        rgbaMat = cv::Mat(height, width, CV_8UC4);
        grayMat = cv::Mat(height, width, CV_8UC1);
        edgesMat = cv::Mat(height, width, CV_8UC1);
    }
    
    cv::cvtColor(yuvMat, rgbaMat, cv::COLOR_YUV2RGBA_NV21);
    
    if (processEnabled) {
        cv::cvtColor(rgbaMat, grayMat, cv::COLOR_RGBA2GRAY);
        
        cv::GaussianBlur(grayMat, grayMat, cv::Size(5, 5), 1.4);
        
        cv::Canny(grayMat, edgesMat, 50, 150);
        
        rgbaMat.setTo(cv::Scalar(0, 0, 0, 255));
        
        for (int y = 0; y < height; y++) {
            uint8_t* edgeRow = edgesMat.ptr<uint8_t>(y);
            uint8_t* rgbaRow = rgbaMat.ptr<uint8_t>(y);
            for (int x = 0; x < width; x++) {
                if (edgeRow[x] > 0) {
                    rgbaRow[x * 4 + 0] = 255;
                    rgbaRow[x * 4 + 1] = 255;
                    rgbaRow[x * 4 + 2] = 255;
                    rgbaRow[x * 4 + 3] = 255;
                }
            }
        }
    }
    
    int outputSize = width * height * 4;
    jbyteArray output = env->NewByteArray(outputSize);
    env->SetByteArrayRegion(output, 0, outputSize, reinterpret_cast<jbyte*>(rgbaMat.data));
    
    return output;
}
