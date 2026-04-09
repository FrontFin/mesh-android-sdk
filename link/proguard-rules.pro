# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Preserve source-file names and line numbers so Crashlytics stack traces include
# file/line information. Class and method names will still be obfuscated unless a
# mapping file is uploaded to Crashlytics.
-keepattributes SourceFile,LineNumberTable

# ProGuard/R8 obfuscation option that moves all obfuscated classes into a single flat package
# (or a specified package), collapsing the original package structure.
-flattenpackagehierarchy com.meshconnect.link

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt # core serialization annotations

-keep class com.meshconnect.link.entity.** { *; }
-keep class com.meshconnect.link.ui.LaunchLink { *; }
-keep class com.meshconnect.link.ui.LinkExit { *; }
-keep class com.meshconnect.link.ui.LinkResult { *; }
-keep class com.meshconnect.link.ui.LinkSuccess { *; }
-keep class com.meshconnect.link.LinkEventsKt { *; }
-keep class com.meshconnect.link.LinkPayloadsKt { *; }

#--------- Begin: proguard configuration for Gson ------------
# https://github.com/google/gson/blob/master/examples/android-proguard-example/proguard.cfg
-keep public class * implements java.lang.reflect.Type
# Retain generic signatures of TypeToken and its subclasses with R8 version 3.0 and higher.
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken
#--------- End: proguard configuration for Gson ------------
