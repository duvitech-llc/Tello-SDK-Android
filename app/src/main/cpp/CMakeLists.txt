cmake_minimum_required(VERSION 3.4.1)

add_library(gstTelloVideo SHARED
            gstTelloVideo.c)

add_library( gstreamer_android
            SHARED
            IMPORTED )

set_target_properties( # Specifies the target library.
        gstreamer_android

        # Specifies the parameter you want to define.
        PROPERTIES IMPORTED_LOCATION

        # Provides the path to the library you want to import.
        ${PROJECT_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libgstreamer_android.so )

message( ${ANDROID_ABI} )
IF(${ANDROID_ABI} STREQUAL armeabi-v7a)
    include_directories(D:/Android/gst-sdk-1.16/armv7/lib/glib-2.0/include)
    include_directories(D:/Android/gst-sdk-1.16/armv7/include/glib-2.0)
    include_directories(D:/Android/gst-sdk-1.16/armv7/include/gstreamer-1.0)
ELSEIF(${ANDROID_ABI} STREQUAL arm64-v8a)
    include_directories(D:/Android/gst-sdk-1.16/arm64/lib/glib-2.0/include)
    include_directories(D:/Android/gst-sdk-1.16/arm64/include/glib-2.0)
    include_directories(D:/Android/gst-sdk-1.16/arm64/include/gstreamer-1.0)
ELSEIF(${ANDROID_ABI} STREQUAL x86)
    include_directories(D:/Android/gst-sdk-1.16/x86/lib/glib-2.0/include)
    include_directories(D:/Android/gst-sdk-1.16/x86/include/glib-2.0)
    include_directories(D:/Android/gst-sdk-1.16/x86/include/gstreamer-1.0)
ELSEIF(${ANDROID_ABI} STREQUAL x86_64)
    include_directories(D:/Android/gst-sdk-1.16/x86_64/lib/glib-2.0/include)
    include_directories(D:/Android/gst-sdk-1.16/x86_64/include/glib-2.0)
    include_directories(D:/Android/gst-sdk-1.16/x86_64/include/gstreamer-1.0)
ENDIF()

# Include libraries needed for tutorial1 lib
target_link_libraries(gstTelloVideo
                      gstreamer_android
                      android
                      log)
