package hk.collaction.freehkkai.core

import android.app.Application
import android.os.Build
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.himphen.logger.AndroidLogAdapter
import com.himphen.logger.Logger
import hk.collaction.freehkkai.BuildConfig
import hk.collaction.freehkkai.util.Utils.getAdMobDeviceID

/**
 * Created by himphen on 24/5/16.
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()

        initLogger()
        initAdMob()
        initCrashlytics()
    }

    private fun initLogger() {
        Logger.addLogAdapter(object : AndroidLogAdapter() {
            override fun isLoggable(priority: Int, tag: String?): Boolean {
                return BuildConfig.DEBUG
            }
        })
    }

    private fun initAdMob() {
        val requestConfiguration = RequestConfiguration.Builder()
        requestConfiguration.setTagForChildDirectedTreatment(TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE)
        if (BuildConfig.DEBUG) {
            val testDevices = ArrayList<String>()
            testDevices.add(AdRequest.DEVICE_ID_EMULATOR)
            testDevices.add(getAdMobDeviceID(this))

            requestConfiguration.setTestDeviceIds(testDevices)
        }
        MobileAds.setRequestConfiguration(requestConfiguration.build())
        MobileAds.initialize(this)
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