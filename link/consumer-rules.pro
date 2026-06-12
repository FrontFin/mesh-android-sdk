# Consumer ProGuard/R8 rules — shipped inside the AAR and applied to the
# consuming app's R8 pass.
#
# IMPORTANT: keep this file limited to -keep rules that protect the SDK's
# public API and its runtime-reflected (Gson) types. Do NOT put global
# obfuscation directives here (e.g. -flattenpackagehierarchy, -repackageclasses,
# -renamesourcefileattribute) — they would leak into and reshape the entire
# host app's build. Library-only build options belong in proguard-rules.pro.

# Public API surface — must survive the consumer's R8 pass.
-keep class com.meshconnect.link.entity.** { *; }
-keep class com.meshconnect.link.ui.LaunchLink { *; }
-keep class com.meshconnect.link.ui.LinkExit { *; }
-keep class com.meshconnect.link.ui.LinkResult { *; }
-keep class com.meshconnect.link.ui.LinkSuccess { *; }
-keep class com.meshconnect.link.LinkEventsKt { *; }
-keep class com.meshconnect.link.LinkPayloadsKt { *; }

-keepattributes *Annotation*, InnerClasses, Signature
#--------- Begin: proguard configuration for Gson ------------
# https://github.com/google/gson/blob/master/examples/android-proguard-example/proguard.cfg
-keep public class * implements java.lang.reflect.Type
# Retain generic signatures of TypeToken and its subclasses with R8 version 3.0 and higher.
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken
#--------- End: proguard configuration for Gson ------------
