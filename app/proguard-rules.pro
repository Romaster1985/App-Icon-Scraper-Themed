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

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Configuración de ProGuard existente
-keep class com.romaster.appiconscrapper.** { *; }

# ✅ NUEVO: Mantener clases nativas y JNI
-keepclasseswithmembernames class * {
    native <methods>;
}

# ✅ NUEVO: Mantener librerías nativas
-keep class com.romaster.appiconscrapper.NativeZipAlign { *; }

# ✅ Mantener clases de firma APK
-keep class com.android.apksig.** { *; }

# ✅ Mantener clases de Bouncy Castle
-keep class org.bouncycastle.** { *; }
-dontwarn org.bouncycastle.**

# ✅ Mantener clases de Apache Commons IO
-keep class org.apache.commons.io.** { *; }
-dontwarn org.apache.commons.io.**

# ✅ Configuraciones generales de Android
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes *Annotation*

# ✅ Mantener View Binding
-keep class * extends android.view.View { *; }

# ✅ Mantener recursos
-keepclassmembers class **.R$* {
    public static <fields>;
}