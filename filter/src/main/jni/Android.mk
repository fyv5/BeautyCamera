LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
APP_ABI := all

LOCAL_MODULE    := native-lib
LOCAL_CPPFLAGS     := -O0 -D_UNICODE -DUNICODE -DUSE_DUMP -Wno-error=format-security
LOCAL_CPP_EXTENSION := .cpp
LOCAL_LDLIBS    := -lm -llog -lz
LOCAL_SHORT_COMMANDS := true
INC_DIRS = -I$(LOCAL_PATH)/jni
LOCAL_CPPFLAGS += $(INC_DIRS)

LOCAL_SRC_FILES    := \
    jni_lib.cpp    \    \


LOCAL_SHARED_LIBRARIES += libandroid_runtime


include $(BUILD_SHARED_LIBRARY)