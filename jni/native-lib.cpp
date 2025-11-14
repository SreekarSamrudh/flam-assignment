#include <jni.h>
#include <android/log.h>

#define LOG_TAG "NativeLib"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

extern "C" {
    jbyteArray processFrameNative(JNIEnv* env, jbyteArray input, jint width, jint height, jboolean processEnabled);
}

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_example_flamapp_NativeLib_processFrame(JNIEnv* env, jobject thiz, jbyteArray input, jint width, jint height, jboolean processEnabled) {
    return processFrameNative(env, input, width, height, processEnabled);
}

extern "C" JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
    LOGI("JNI_OnLoad called");
    return JNI_VERSION_1_6;
}
