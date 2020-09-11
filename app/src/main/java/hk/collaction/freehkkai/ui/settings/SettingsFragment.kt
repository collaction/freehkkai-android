package hk.collaction.freehkkai.ui.settings

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.blankj.utilcode.util.AppUtils
import hk.collaction.freehkkai.BuildConfig
import hk.collaction.freehkkai.R
import hk.collaction.freehkkai.util.Utils.PREF_FONT_VERSION
import hk.collaction.freehkkai.util.Utils.getCurrentFontName

class SettingsFragment : PreferenceFragmentCompat() {
    private lateinit var sharedPreferences: SharedPreferences
    private var prefFontVersion: Preference? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_general)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        /* Set version */
        val prefVersion = findPreference<Preference>("pref_version")
        prefVersion!!.summary = AppUtils.getAppVersionName()
        findPreference<Preference>("pref_report")?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val intent = Intent(Intent.ACTION_SEND)
            var text = "Android Version: " + Build.VERSION.RELEASE + "\n"
            text += "SDK Level: " + Build.VERSION.SDK_INT.toString() + "\n"
            text += "Version: " + AppUtils.getAppVersionName() + "\n"
            text += "Brand: " + Build.BRAND + "\n"
            text += "Model: " + Build.MODEL + "\n\n\n"
            intent.type = "message/rfc822"
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(BuildConfig.CONTACT_EMAIL))
            intent.putExtra(Intent.EXTRA_SUBJECT, "自由香港楷書回報問題")
            intent.putExtra(Intent.EXTRA_TEXT, text)
            startActivity(intent)
            false
        }
        findPreference<Preference>("pref_rate")?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val uri = Uri.parse("market://details?id=" + context?.packageName)
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
            false
        }
        findPreference<Preference>("pref_share")?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_SEND
            intent.putExtra(Intent.EXTRA_TEXT, "下載「自由香港楷書」程式，就可以查詢支援超過 4700 個香港教育局楷書參考寫法，解決因為「電腦輸入法」而令學生 / 家長 / 教師混淆而寫錯字的問題。\n\n" + "https://play.google.com/store/apps/details?id=" + context?.packageName)
            intent.type = "text/plain"
            startActivity(Intent.createChooser(intent, "分享此程式"))
            false
        }
        findPreference<Preference>("pref_author")?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val uri = Uri.parse("market://search?q=pub:\"Collaction 小隊\"")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
            false
        }
        findPreference<Preference>("pref_collaction")?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val uri = Uri.parse("https://www.collaction.hk/s/collactionopensource")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
            false
        }
        findPreference<Preference>("pref_hkfreekai")?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val uri = Uri.parse("https://www.collaction.hk/s/freehkfonts")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
            false
        }
        findPreference<Preference>("pref_privacy")?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val uri = Uri.parse("https://www.collaction.hk/about/privacy")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
            false
        }
        prefFontVersion = findPreference("pref_font_version")
        prefFontVersion?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val a = when (sharedPreferences.getString(PREF_FONT_VERSION, "4700")) {
                "7000" -> 1
                else -> 0
            }
            activity?.let { activity ->
                MaterialDialog(activity)
                        .title(text = "切換字型檔案版本")
                        .listItemsSingleChoice(
                                R.array.font_version_array,
                                initialSelection = a,
                                waitForPositiveButton = false
                        ) { dialog, index, _ ->
                            when (index) {
                                0 -> sharedPreferences.edit().putString(PREF_FONT_VERSION, "4700").apply()
                                1 -> sharedPreferences.edit().putString(PREF_FONT_VERSION, "7000").apply()
                            }
                            setFontVersionSummary()
                            dialog.dismiss()
                        }
                        .negativeButton(R.string.ui_cancel)
                        .show()

                activity.setResult(Activity.RESULT_OK)
            }
            false
        }
        setFontVersionSummary()
    }

    private fun setFontVersionSummary() {
        context?.let { context ->
            prefFontVersion?.setSummary(getCurrentFontName(context))
        }
    }

    companion object {
        fun newInstance(): SettingsFragment {
            return SettingsFragment()
        }
    }
}