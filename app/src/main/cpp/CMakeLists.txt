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

include_directories(D:/Android/gst-sdk-1.16/armv7/lib/glib-2.0/include)
include_directories(D:/Android/gst-sdk-1.16/armv7/include/glib-2.0)
include_directories(D:/Android/gst-sdk-1.16/armv7/include/gstreamer-1.0)

# Include libraries needed for tutorial1 lib
target_link_libraries(gstTelloVideo
                      gstreamer_android
                      android
                      log)
