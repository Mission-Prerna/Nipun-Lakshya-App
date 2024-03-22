package com.samagra.commons.utils

import android.content.pm.PackageManager
import android.os.Handler
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.gson.Gson
import com.samagra.commons.models.ActorVisibility
import com.samagra.grove.logging.Grove
import java.util.regex.Pattern

// todo clean extensions
fun addFragment(
    containerViewId: Int,
    manager: FragmentManager,
    fragment: Fragment,
    fragmentTag: String,
    addToBackStack: Boolean
) {
    try {
        val fragmentName = fragment.javaClass.name
        Grove.d("addFragment() :: Adding new fragment $fragmentName")
        // Create new fragment and transaction
        val transaction = manager.beginTransaction()
        val fragmentByTag = manager.findFragmentByTag(fragmentTag)
        if (fragmentByTag != null) {
            transaction.remove(fragmentByTag);
            transaction.add(containerViewId, fragment, fragmentTag)
        } else {
            transaction.add(containerViewId, fragment, fragmentTag)
        }
        if (addToBackStack) {
            transaction.addToBackStack(fragmentTag)
        }
        transaction.commit()
    } catch (ex: java.lang.IllegalStateException) {
        //  reportException(new IllegalStateException("Non App crash custom Exception addFragment",ex));
    }
}

fun removeFragment(fragment: Fragment?, manager: FragmentManager?) {
    if (fragment == null || manager == null) return
    try {
        val fragmentName = fragment.javaClass.name
        Grove.d("removeFragment() :: Removing current fragment $fragmentName")
        val transaction = manager.beginTransaction()
        transaction.remove(fragment)
        Handler().post {
            try {
                transaction.commit()
                manager.popBackStack()
            } catch (ex: IllegalStateException) {
                //  reportException(new IllegalStateException("Non App crash custom Exception in removeFragment in " + fragmentname,ex));
            }
        }
    } catch (ex: IllegalStateException) {
//            reportException(new IllegalStateException("Non App crash custom Exception in removeFragment",ex));
    }
}

fun replaceFragment(
    containerViewId: Int,
    manager: FragmentManager,
    fragment: Fragment,
    fragmentTag: String,
    addToBackStack: Boolean
) {
    try {
        val fragmentName = fragment.javaClass.name
        Grove.d("addFragment() :: Adding new fragment $fragmentName")
        // Create new fragment and transaction
        val transaction = manager.beginTransaction()
        val fragmentByTag = manager.findFragmentByTag(fragmentTag)
        if (fragmentByTag != null) {
            transaction.remove(fragmentByTag);
            transaction.replace(containerViewId, fragment, fragmentTag)
        } else {
            transaction.replace(containerViewId, fragment, fragmentTag)
        }
        if (addToBackStack) {
            transaction.addToBackStack(fragmentTag)
        }
        transaction.commit()
    } catch (ex: java.lang.IllegalStateException) {
        //  reportException(new IllegalStateException("Non App crash custom Exception addFragment",ex));
    }
}

/*
* Extension for valid phone no.
* */
fun String.isValidPhoneNumber(): Boolean {
    val p = Pattern.compile("[6-9][0-9]{9}")
    val m = p.matcher(this)
    return !m.find() || m.group() != this
}

/*
* Extension for crossed phone no.
* */
fun String.getHiddenMobileNumber(): String {
    return "+91-${this.substring(0, 3)}xxxx${this.substring(7)}"
}

fun isChatBotEnabled(actorId: Int): Boolean {
    val configJson = RemoteConfigUtils.getFirebaseRemoteConfigInstance()
        .getString(RemoteConfigUtils.CHATBOT_ICON_VISIBILITY_TO_ACTOR)
    val gson = Gson()
    val myData = gson.fromJson(configJson, ActorVisibility::class.java)
    // Check if the value 1 exists in the enabled_actors list
    return myData.enabled_actors.contains(actorId);
}

fun isAppInstalled(pm: PackageManager, uri: String): Boolean {
    return try {
        pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
}