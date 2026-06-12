# Consumer ProGuard/R8 rules.
#
# These rules are bundled into the published AAR (via `consumerProguardFiles`)
# and merged into the CONSUMING app's R8 configuration. Keep this file limited to
# rules that protect the Mesh SDK's public API and runtime reflection.
#
# IMPORTANT: never add global directives here (e.g. -flattenpackagehierarchy,
# -repackageclasses, -overloadaggressively, optimization flags). They apply to the
# ENTIRE consuming app and will rename/repackage the consumer's own classes. The
# SDK's own build-time obfuscation belongs in proguard-rules.pro, not here.

# Retain annotations (e.g. Gson @SerializedName) and inner classes on kept SDK
# types so runtime (de)serialization of entities keeps working in the consumer app.
-keepattributes *Annotation*, InnerClasses

# Mesh SDK public API surface.
-keep class com.meshconnect.link.entity.** { *; }
-keep class com.meshconnect.link.ui.LaunchLink { *; }
-keep class com.meshconnect.link.ui.LinkExit { *; }
-keep class com.meshconnect.link.ui.LinkResult { *; }
-keep class com.meshconnect.link.ui.LinkSuccess { *; }
-keep class com.meshconnect.link.LinkEventsKt { *; }
-keep class com.meshconnect.link.LinkPayloadsKt { *; }

#--------- Begin: proguard configuration for Gson ------------
# The SDK uses Gson at runtime to (de)serialize entities in the consumer process.
# https://github.com/google/gson/blob/master/examples/android-proguard-example/proguard.cfg
-keep public class * implements java.lang.reflect.Type
# Retain generic signatures of TypeToken and its subclasses with R8 version 3.0 and higher.
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken
#--------- End: proguard configuration for Gson ------------
