# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/marcel/Android/Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

#Line numbers
#-renamesourcefileattribute SourceFile
#-keepattributes SourceFile,LineNumberTable

#NetLog
-keepnames class edu.nudt.netlog.** { *; }

#JNI
-keepclasseswithmembernames class * {
    native <methods>;
}

#JNI callbacks
-keep class edu.nudt.netlog.Allowed { *; }
-keep class edu.nudt.netlog.Packet { *; }
-keep class edu.nudt.netlog.ResourceRecord { *; }
-keep class edu.nudt.netlog.Usage { *; }
-keep class edu.nudt.netlog.ServiceSinkhole {
    void nativeExit(java.lang.String);
    void nativeError(int, java.lang.String);
    void logPacket(edu.nudt.netlog.Packet);
    void dnsResolved(edu.nudt.netlog.ResourceRecord);
    boolean isDomainBlocked(java.lang.String);
    edu.nudt.netlog.Allowed isAddressAllowed(edu.nudt.netlog.Packet);
    void accountUsage(edu.nudt.netlog.Usage);
}

#Support library
-keep class android.support.v7.widget.** { *; }
-dontwarn android.support.v4.**

#Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep enum com.bumptech.glide.** {*;}
#-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
#    **[] $VALUES;
#    public *;
#}

#AdMob
-dontwarn com.google.android.gms.internal.**
