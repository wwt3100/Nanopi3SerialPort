#include <jni.h>
#include <string>
#include <termios.h>
#include <unistd.h>
#include <fcntl.h>
#include <string.h>
#include <stdlib.h>
#include <pthread.h>
#include <android/log.h>
#include "crc16.h"

#define TAG  "NDK"

// 串口速率
#define COM_SPEED B115200

//设备地址
#define TTY_DEVICE_PATH   "/dev/ttySAC3"

#define PROTOCOL_HEAD_SEND 0xaa
#define PROTOCOL_FOOT 0xda
#define PROTOCOL_HEAD_RECEIVE  0xbb


#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO,  TAG, fmt, ##args)
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, TAG, fmt, ##args)
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, TAG, fmt, ##args)


static JavaVM *jvm = NULL;
static jobject jv_obj = NULL;

int fd_com;
pthread_t pid;
bool thread_flag;
const int bitSize = 128;
unsigned char buffer[bitSize];
const int CacheLength = 256;
unsigned char cache[CacheLength];
int cacheSize = 0;

extern "C"
JNIEXPORT jstring
JNICALL
Java_com_a9klab_nanopi3serialport_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

int callBack(unsigned char *data, int length) {
    JNIEnv *env;
    jclass cls;
    jmethodID mid;
    if (jvm->AttachCurrentThread(&env, NULL) != JNI_OK) {
        LOGE("%s: AttachCurrentThread failed", __FUNCTION__);
        return -1;
    }
    jbyteArray arr = env->NewByteArray(length);
    jbyte *mdata = (jbyte *) data;
    env->SetByteArrayRegion(arr, 0, length, mdata);
    cls = env->GetObjectClass(jv_obj);
    if (cls == NULL) {
        goto error;
    }
    mid = env->GetStaticMethodID(cls, "receiveData", "([B)V");
    if (mid == NULL) {
        goto error;
    }
    env->CallStaticVoidMethod(cls, mid, arr);

    error:
    env->DeleteLocalRef(cls);
    env->DeleteLocalRef(arr);
    if (jvm->DetachCurrentThread() != JNI_OK) {
        LOGE("%s: DetachCurrentThread failed", __FUNCTION__);
    }
    return 0;
}

void dataProcess(unsigned char buf[], int size) {
    for (int i = 0; i < size; i++) {
        if (cacheSize + i > CacheLength - 1) {
            continue;
        }
        cache[cacheSize + i] = buf[i];
    }
    cacheSize = cacheSize + size;
    if (cacheSize > CacheLength) {
        cacheSize = CacheLength;
    }
    for (int i = 0; i < CacheLength - cacheSize; i++) {
        cache[cacheSize + i] = 0x00;
    }
    int head_pos = 0, foot_pos = 0;
    bool finded_head = false, finded_foot = false;
    for (int j = 0; j < cacheSize; j++) {
        //接受到头
        if (!finded_head && cache[j] == PROTOCOL_HEAD_RECEIVE) {
            head_pos = j;
            finded_head = true;
            //接受到头后，接受到尾
        } else if (finded_head && !finded_foot && cache[j] == PROTOCOL_FOOT) {
            foot_pos = j;
            finded_foot = true;
        }
        //头尾都接受
        if (finded_head && finded_foot) {
            //验证是否真结束
            int len = foot_pos - head_pos;
            if (len >= 3) {
//                if (cache[head_pos + 3] + 6 == foot_pos - head_pos) {
                    //该条数据完整
                    //LOGD("callBack func");
                    callBack(cache + head_pos, len); // 数据完整 回调交由java
//                    finded_head = false;
//                    finded_foot = false;
//                } else if (cache[head_pos + 3] + 6 > foot_pos - head_pos) {
//                    //该条数据未完
//                    finded_foot = false;
//                } else if (cache[head_pos + 3] + 6 < foot_pos - head_pos) {
//                    //该数据出错
//                    finded_head = false;
//                    finded_foot = false;
//                }
            } else {
                //该数据出错
                finded_head = false;
                finded_foot = false;
            }
        }
    }
    if (finded_head && !finded_foot) {
        for (int k = 0; k < cacheSize - head_pos; k++) {
            cache[k] = cache[head_pos + k];
        }
        cacheSize = cacheSize - head_pos;
    } else {
        cacheSize = 0;
    }
}

extern "C"
JNIEXPORT jint
JNICALL
Java_com_a9klab_nanopi3serialport_SerialPort_InitComm(JNIEnv *env, jobject obj) {

    speed_t speed = COM_SPEED;
    int ret = -1;
    char path[] = TTY_DEVICE_PATH;
    /* Opening device */
    {
        fd_com = open(path, O_RDWR | O_SYNC);
        if (fd_com == -1) {
            LOGE("Cannot open %d", fd_com);
            return ret;
        }
    }

    {
        struct termios cfg;
        if (tcgetattr(fd_com, &cfg)) {
            LOGE("tcgetattr() failed %d", fd_com);
            close(fd_com);
            return ret;
        }

        cfmakeraw(&cfg);
        cfsetispeed(&cfg, speed);
        cfsetospeed(&cfg, speed);

        if (tcsetattr(fd_com, TCSANOW, &cfg)) {
            LOGE("tcsetattr failed %d", fd_com);
            close(fd_com);
            ret = 0;
            return ret;
        }

        env->GetJavaVM(&jvm);
        jv_obj = env->NewGlobalRef(obj);

        ret = 1;
        return ret;
    }

}

extern "C"
JNIEXPORT jint
JNICALL
Java_com_a9klab_nanopi3serialport_SerialPort_SendEx(JNIEnv *env, jclass jc_,
                                                    jbyteArray data_);

jint
Java_com_a9klab_nanopi3serialport_SerialPort_SendEx(JNIEnv *env, jclass jc_, jbyteArray data_) {
    jbyte *data = env->GetByteArrayElements(data_, NULL);
    size_t len = (size_t) env->GetArrayLength(data_);
    jint ret;
    if (fd_com) {
        ret = write(fd_com, data, len);
    } else {
        LOGE("Serial Port not open");
        ret = -1;
    }

    env->ReleaseByteArrayElements(data_, data, 0);
    return ret;
}

extern "C"
JNIEXPORT jint
JNICALL
Java_com_a9klab_nanopi3serialport_SerialPort_Send(JNIEnv *env, jclass jc_,
                                                  jbyteArray data_);

jint Java_com_a9klab_nanopi3serialport_SerialPort_Send(JNIEnv *env, jclass jc_, jbyteArray data_) {
    jbyte *data = env->GetByteArrayElements(data_, NULL);
    size_t len = (size_t) env->GetArrayLength(data_);
    jint ret = -1;
    if (fd_com) {
        unsigned char *d;
        d = (unsigned char *) malloc(len + 4);
        memset(d, 0, len + 4);
        d[0] = PROTOCOL_HEAD_SEND;
        d[len + 3] = PROTOCOL_FOOT;
        memcpy(d + 1, data, len);
        unsigned short c16 = getCRC16(d, len + 1);
        d[len + 1] = (uint8_t) (c16 >> 8);
        d[len + 2] = (uint8_t) c16;
        int b = write(fd_com, d, len + 4);
        if (b == len + 4) {
            ret = 1;
        } else {
            ret = 0;
        }
        free(d);
    } else {
        LOGE("Serial Port not open");
        ret = -1;
    }
    env->ReleaseByteArrayElements(data_, data, 0);
    return ret;
}

extern "C"
JNIEXPORT jshort JNICALL
Java_com_a9klab_nanopi3serialport_SerialPort_GetCRC16(JNIEnv *env, jobject instance,
                                                      jbyteArray data_, jint len);

jshort Java_com_a9klab_nanopi3serialport_SerialPort_GetCRC16(JNIEnv *env, jobject instance,
                                                             jbyteArray data_, jint len) {
    jshort ret;
    jbyte *data = env->GetByteArrayElements(data_, NULL);
    ret = getCRC16((uint8_t *) data, len);
    env->ReleaseByteArrayElements(data_, data, 0);
    return ret;
}


void *thread_fun(void *arg) {
    int count;
    while (thread_flag) {
        if (fd_com > 0) {
            count = read(fd_com, buffer, bitSize);
            if (count > 0) {
                dataProcess(buffer, count);
                //LOGD("Revice Data");
            }
        } else {
            LOGD("thread no read fd_com %d", fd_com);
        }
    }
    thread_flag = false;
    pthread_exit(0);
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_a9klab_nanopi3serialport_SerialPort_Start(JNIEnv *env, jobject instance) {

    thread_flag = true;
    int ret = pthread_create(&pid, NULL, &thread_fun, NULL);
    if (ret >= 0) {
        LOGE("pthread_create %d", ret);
        return 0;
    } else {
        thread_flag = false;
        LOGE("pthread_create failed %d", ret);
        return -1;
    }
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_a9klab_nanopi3serialport_SerialPort_Stop(JNIEnv *env, jobject instance);

jint Java_com_a9klab_nanopi3serialport_SerialPort_Stop(JNIEnv *env, jobject instance) {
    thread_flag = false;
    sleep(10);
    close(fd_com);
    return 0;
}