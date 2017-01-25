#include <jni.h>
#include <string>

extern "C"
jstring
Java_com_example_felidadae_rosetus_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++; Dupa pana Janka";
    return env->NewStringUTF(hello.c_str());
}
