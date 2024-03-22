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

# NL App rulesx
-keepclasseswithmembers class com.chatbot.model.** { *; }
-keepclasseswithmembers class com.chatbot.ChatbotListingState.** { *; }
-keepclasseswithmembers class com.chatbot.notification.model.** { *; }
-keepclasseswithmembers class com.samagra.parent.authentication.** { *; }
-keepclasseswithmembers class com.samagra.parent.data.models.** { *; }
-keepclasseswithmembers class com.samagra.parent.data.** { *; }
-keepclasseswithmembers class com.samagra.parent.ui.** { *; }
-keepclasseswithmembers class com.samagra.ancillaryscreens.data.** { *; }
-keepclasseswithmembers class com.samagra.ancillaryscreens.fcm.** { *; }
-keepclasseswithmembers class com.samagra.commons.models.** { *; }
-keepclasseswithmembers class com.samagra.data.** { *; }
-keepclasseswithmembers class com.samagra.network.** { *; }
-keepclasseswithmembers class com.samagra.gatekeeper.** { *; }
-keepclasseswithmembers class com.data.models.** { *; }
-keepclasseswithmembers class com.data.db.models.** { *; }
-keepclasseswithmembers class com.samagra.workflowengine.odk.** { *; }
-keepclasseswithmembers class com.samagra.workflowengine.workflow.model.** { *; }
-keepclasseswithmembers class com.assessment.studentselection.data.** { *; }
-keepclasseswithmembers class com.morziz.network.models.** { *; }
-keepclasseswithmembers class org.json.** { *; }
-keepclasseswithmembers class org.kxml2.io.** { *; }
-keepclasseswithmembers class com.samagra.commons.posthog.data.** { *; }

# Collect App rules
-dontwarn com.google.**
-dontwarn au.com.bytecode.**
-dontwarn org.joda.time.**
-dontwarn org.osmdroid.**
-dontwarn org.xmlpull.v1.**
-dontwarn org.hamcrest.**
-dontwarn com.rarepebble.**

-keep class org.javarosa.**
-keep class org.odk.collect.android.logic.actions.**
-keep class android.support.v7.widget.** { *; }
-keep class org.mp4parser.boxes.** { *; }
-keepclassmembers class * {
  @com.google.api.client.util.Key <fields>;
}
-keep class * extends androidx.fragment.app.Fragment{}

-dontobfuscate

# Custom ODK
-keepclasseswithmembers class org.xmlpull.v1.** { *; }

# recommended okhttp rules
# https://github.com/square/okhttp#r8--proguard
-dontwarn javax.annotation.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
-dontwarn org.codehaus.mojo.animal_sniffer.*
-dontwarn okhttp3.internal.platform.ConscryptPlatform

# Gson uses generic type information stored in a class file when working with
# fields. Proguard removes such information by default, keep it.
-keepattributes Signature

# This is also needed for R8 in compat mode since multiple
# optimizations will remove the generic signature such as class
# merging and argument removal. See:
# https://r8.googlesource.com/r8/+/refs/heads/main/compatibility-faq.md#troubleshooting-gson-gson
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

-printusage usage.txt
