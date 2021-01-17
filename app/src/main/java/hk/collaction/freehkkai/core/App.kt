package hk.collaction.freehkkai.core

import android.app.Application
import android.content.Context
import android.os.Build
import com.blankj.utilcode.util.Utils
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import hk.collaction.freehkkai.BuildConfig
import hk.collaction.freehkkai.util.Utils.getAdMobDeviceID
import java.util.ArrayList

/**
 * Created by himphen on 24/5/16.
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()

        // init logger
        Logger.addLogAdapter(object : AndroidLogAdapter() {
            override fun isLoggable(priority: Int, tag: String?): Boolean {
                return BuildConfig.DEBUG
            }
        })

        Utils.init(this)

        // init AdMob
        initAdMob()

        // init Crashlytics
        initCrashlytics()
    }

    private fun initAdMob() {
        if (BuildConfig.DEBUG) {
            val testDevices = ArrayList<String>()
            testDevices.add(AdRequest.DEVICE_ID_EMULATOR)
            testDevices.add(getAdMobDeviceID(this))

            val requestConfiguration = RequestConfiguration.Builder()
                .setTestDeviceIds(testDevices)
                .build()
            MobileAds.setRequestConfiguration(requestConfiguration)
        }
    }

    private fun initCrashlytics() {
        var isGooglePlay = false
        val allowedPackageNames = ArrayList<String>()
        allowedPackageNames.add("com.android.vending")
        allowedPackageNames.add("com.google.android.feedback")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            packageManager.getInstallSourceInfo(packageName).initiatingPackageName?.let { initiatingPackageName ->
                isGooglePlay = allowedPackageNames.contains(initiatingPackageName)
            }
        } else {
            @Suppress("DEPRECATION")
            packageManager.getInstallerPackageName(packageName)?.let { installerPackageName ->
                isGooglePlay = allowedPackageNames.contains(installerPackageName)
            }
        }

        if (isGooglePlay || BuildConfig.DEBUG) {
            Firebase.crashlytics.setCrashlyticsCollectionEnabled(true)
            Firebase.crashlytics.setCustomKey("isGooglePlay", isGooglePlay)
        }
    }
}