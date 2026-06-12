# ProGuard/R8 rules applied to the link module's OWN release build (minifyEnabled).
#
# Consumer-facing keep rules live in consumer-rules.pro and are shipped inside the
# AAR. That file is also referenced by this module's release build (see build.gradle),
# so the rules below are only the extras needed when minifying the library itself.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Preserve source-file names and line numbers so the library's own stack traces
# include file/line information. Class and method names will still be obfuscated
# unless a mapping file is uploaded (e.g. to Crashlytics).
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Flatten the SDK's own renamable (non-kept) classes under a single parent package.
# The argument is the DESTINATION package, not a filter — so this is safe ONLY here,
# in the library's own build. It must never live in consumer-rules.pro, where it
# would repackage the entire host app and rename its classes too.
-flattenpackagehierarchy com.meshconnect.link

-dontnote kotlinx.serialization.AnnotationsKt # core serialization annotations
