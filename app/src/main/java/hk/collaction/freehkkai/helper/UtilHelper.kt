package hk.collaction.freehkkai.helper

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.provider.MediaStore
import android.provider.Settings
import android.view.PixelCopy
import android.view.View
import android.view.ViewConfiguration
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.preference.PreferenceManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.blankj.utilcode.util.SizeUtils
import com.crashlytics.android.Crashlytics
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.material.snackbar.Snackbar
import hk.collaction.freehkkai.BuildConfig
import hk.collaction.freehkkai.Environment
import hk.collaction.freehkkai.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.util.Locale

/**
 * UtilHelper Class
 * Created by Himphen on 10/1/2016.
 */
open class UtilHelper {
    companion object {
        private const val PREF_IAP = "iap"
        private const val PREF_LANGUAGE = "PREF_LANGUAGE"
        private const val PREF_LANGUAGE_COUNTRY = "PREF_LANGUAGE_COUNTRY"
        const val PREF_FONT_VERSION = "pref_font_version"
        const val PREF_FONT_VERSION_ALERT = "pref_font_version_alert"
        fun initAdView(c: Activity?, adLayout: RelativeLayout): AdView? {
            return initAdView(c, adLayout, false)
        }

        fun initAdView(context: Activity?, adLayout: RelativeLayout, isPreserveSpace: Boolean): AdView? {
            if (isPreserveSpace) {
                adLayout.layoutParams.height = SizeUtils.dp2px(50f)
            }
            val defaultPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            var adView: AdView? = null
            try {
                if (!defaultPreferences.getBoolean(PREF_IAP, false)) {
                    adView = AdView(context)
                    adView.adUnitId = BuildConfig.ADMOB_BANNER_ID
                    adView.adSize = AdSize.BANNER
                    adLayout.addView(adView)
                    val adRequest = AdRequest.Builder()
                    adRequest.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    for (id in BuildConfig.ADMOB_DEVICE_ID) {
                        adRequest.addTestDevice(id)
                    }
                    adView.loadAd(adRequest.build())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return adView
        }

        fun forceShowMenu(context: Context?) {
            try {
                val config = ViewConfiguration.get(context)
                val menuKeyField = ViewConfiguration::class.java
                        .getDeclaredField("sHasPermanentMenuKey")
                menuKeyField.isAccessible = true
                menuKeyField.setBoolean(config, false)
            } catch (ignored: Exception) {
            }
        }

        fun round(value: Double, places: Int): Double {
            require(places >= 0)
            var bd = BigDecimal(value)
            bd = bd.setScale(places, RoundingMode.HALF_UP)
            return bd.toDouble()
        }

        fun formatSignificant(value: Double, significant: Int): String {
            val mathContext = MathContext(significant, RoundingMode.DOWN)
            val bigDecimal = BigDecimal(value, mathContext)
            return bigDecimal.toPlainString()
        }

        @Suppress("DEPRECATION")
        fun detectLanguage(context: Context) {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            var language = preferences.getString(PREF_LANGUAGE, "") ?: ""
            var languageCountry = preferences.getString(PREF_LANGUAGE_COUNTRY, "") ?: ""
            if (language == "") {
                val locale: Locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Resources.getSystem().configuration.locales[0]
                } else {
                    Resources.getSystem().configuration.locale
                }
                language = locale.language
                languageCountry = locale.country
            }
            val res = context.resources
            val conf = res.configuration
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                conf.setLocale(Locale(language, languageCountry))
            } else {
                conf.locale = Locale(language, languageCountry)
            }
            val dm = res.displayMetrics
            res.updateConfiguration(conf, dm)
        }

        fun logException(e: Exception) {
            if (Environment.CONFIG.isDebug()) {
                e.printStackTrace()
            } else {
                Crashlytics.logException(e)
            }
        }

        fun isPermissionsGranted(context: Context?, permissions: Array<String>): Boolean {
            for (permission in permissions) {
                if (context?.let { ContextCompat.checkSelfPermission(it, permission) } == PackageManager.PERMISSION_DENIED) {
                    return false
                }
            }
            return true
        }

        fun hasAllPermissionsGranted(grantResults: IntArray): Boolean {
            for (grantResult in grantResults) {
                if (grantResult == PackageManager.PERMISSION_DENIED) {
                    return false
                }
            }
            return true
        }

        fun scanForActivity(context: Context?): Activity? {
            return when (context) {
                is Activity -> context
                is ContextWrapper -> scanForActivity(context.baseContext)
                else -> null
            }
        }

        fun startSettingsActivity(context: Context?, action: String?) {
            try {
                context?.startActivity(Intent(action))
            } catch (e: Exception) {
                context?.startActivity(Intent(Settings.ACTION_SETTINGS))
            }
        }

        fun openErrorPermissionDialog(context: Context?) {
            context?.let {
                MaterialDialog(it)
                        .customView(R.layout.dialog_permission)
                        .cancelable(false)
                        .positiveButton(R.string.ui_okay) { dialog ->
                            scanForActivity(dialog.context)?.let { activity ->
                                try {
                                    val intent = Intent()
                                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                    intent.addCategory(Intent.CATEGORY_DEFAULT)
                                    intent.data = Uri.parse("package:" + activity.packageName)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                                    activity.startActivity(intent)
                                    activity.finish()
                                } catch (e: Exception) {
                                    val intent = Intent()
                                    intent.action = Settings.ACTION_APPLICATION_SETTINGS
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                                    activity.startActivity(intent)
                                    activity.finish()
                                }
                            }
                        }
                        .negativeButton(R.string.ui_cancel) { dialog ->
                            scanForActivity(dialog.context)?.finish()
                        }
                        .show()
            }
        }

        fun getCurrentFontName(context: Context): String {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            val fontPath = sharedPreferences.getString(PREF_FONT_VERSION, "4700")
            return getCurrentFontName(context, fontPath)
        }

        fun getCurrentFontName(context: Context, fontPath: String?): String {
            val fontVersionArray = context.resources.getStringArray(R.array.font_version_array)
            var fontName = fontVersionArray[0]
            if (fontPath != null) {
                when (fontPath) {
                    "7000" -> fontName = fontVersionArray[1]
                }
            }
            return fontName
        }

        fun getFontID(fontVersion: String?): Int {
            return when (fontVersion) {
                "7000" -> R.font.freehkkai_extended
                else -> R.font.freehkkai_4700
            }
        }

        fun saveImage(context: Context, bitmap: Bitmap): Uri? {
            val imagesFolder = File(context.cacheDir, "images")
            var uri: Uri? = null
            try {
                imagesFolder.mkdirs()
                val file = File(imagesFolder, "shared_image.png")
                val stream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)
                stream.flush()
                stream.close()
                uri = FileProvider.getUriForFile(context, context.packageName + ".provider", file)

            } catch (e: IOException) {
                e.printStackTrace()
            }
            return uri
        }

        fun saveBitmapInPicture(context: Context, bitmap: Bitmap, name: String): Boolean {
            val fos: OutputStream?
            var result = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues()
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "$name.jpg")
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_PICTURES)
                val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                imageUri?.let {
                    fos = resolver.openOutputStream(imageUri)
                    result = bitmap.compress(Bitmap.CompressFormat.JPEG, 95, fos)
                    fos?.close()
                }
            } else {
                @Suppress("DEPRECATION")
                val imagesDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_PICTURES).toString()
                val image = File(imagesDir, "$name.jpg")
                fos = FileOutputStream(image)
                result = bitmap.compress(Bitmap.CompressFormat.JPEG, 95, fos)
                fos.close()
            }

            return result
        }

        @Suppress("DEPRECATION")
        fun getBitmapFromView(activity: Activity, view: View, callback: (Bitmap) -> Unit) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                activity.window?.let { window ->
                    val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
                    val locationOfViewInWindow = IntArray(2)
                    view.getLocationInWindow(locationOfViewInWindow)
                    try {
                        PixelCopy.request(window, Rect(locationOfViewInWindow[0], locationOfViewInWindow[1], locationOfViewInWindow[0] + view.width, locationOfViewInWindow[1] + view.height), bitmap, { copyResult ->
                            if (copyResult == PixelCopy.SUCCESS) {
                                callback(bitmap)
                            } else {
                                snackbar(activity.window.decorView.rootView, "此設備無法截圖，請報告此問題讓我們改進。")?.show()
                            }
                        }, Handler())
                    } catch (e: IllegalArgumentException) {
                        // PixelCopy may throw IllegalArgumentException, make sure to handle it
                        logException(e)
                        snackbar(activity.window.decorView.rootView, "此設備無法截圖，請報告此問題讓我們改進。")?.show()
                    }
                }
            } else {
                view.isDrawingCacheEnabled = true
                val bitmap = Bitmap.createBitmap(view.drawingCache)
                view.isDrawingCacheEnabled = false

                callback(bitmap)
            }
        }


        @Suppress("SameParameterValue")
        fun snackbar(view: View?, stringRid: Int): Snackbar? {
            view?.let {
                val snackbar = Snackbar.make(view, stringRid, Snackbar.LENGTH_LONG)
                val sbView = snackbar.view
                sbView.setBackgroundResource(R.color.primary_dark)
                sbView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(Color.WHITE)
                sbView.findViewById<TextView>(com.google.android.material.R.id.snackbar_action).setTextColor(
                        ContextCompat.getColor(snackbar.context, R.color.gold))
                return snackbar
            }
            return null
        }

        @Suppress("SameParameterValue")
        fun snackbar(view: View?, string: String): Snackbar? {
            view?.let {
                val snackbar = Snackbar.make(view, string, Snackbar.LENGTH_LONG)
                val sbView = snackbar.view
                sbView.setBackgroundResource(R.color.primary_dark)
                sbView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(Color.WHITE)
                sbView.findViewById<TextView>(com.google.android.material.R.id.snackbar_action).setTextColor(
                        ContextCompat.getColor(snackbar.context, R.color.gold))
                return snackbar
            }

            return null
        }
    }
}