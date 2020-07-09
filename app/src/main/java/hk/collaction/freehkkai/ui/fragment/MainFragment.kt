package hk.collaction.freehkkai.ui.fragment

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.preference.PreferenceManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.blankj.utilcode.util.ConvertUtils
import com.github.javiersantos.appupdater.AppUpdater
import com.github.javiersantos.appupdater.enums.Display
import hk.collaction.freehkkai.BuildConfig
import hk.collaction.freehkkai.R
import hk.collaction.freehkkai.helper.UtilHelper.PREF_FONT_VERSION
import hk.collaction.freehkkai.helper.UtilHelper.PREF_FONT_VERSION_ALERT
import hk.collaction.freehkkai.helper.UtilHelper.getBitmapFromView
import hk.collaction.freehkkai.helper.UtilHelper.getCurrentFontName
import hk.collaction.freehkkai.helper.UtilHelper.getFontID
import hk.collaction.freehkkai.helper.UtilHelper.openErrorPermissionDialog
import hk.collaction.freehkkai.helper.UtilHelper.saveImage
import hk.collaction.freehkkai.helper.UtilHelper.snackbar
import hk.collaction.freehkkai.ui.activity.SettingsActivity
import kotlinx.android.synthetic.main.fragment_main.*
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import java.util.Locale

/**
 * @author Himphen
 */
class MainFragment : BaseFragment() {

    private val sizeChange = 8
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var tts: TextToSpeech
    private var isFirst = true
    private var isTTSReady = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        AppUpdater(context)
            .showEvery(4)
            .setDisplay(Display.NOTIFICATION)
            .start()

        updateFontPath()
        tts = TextToSpeech(
            context,
            OnInitListener { status -> isTTSReady = status == TextToSpeech.SUCCESS })
        inputEt.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(cs: CharSequence, arg1: Int, arg2: Int, arg3: Int) {}
            override fun beforeTextChanged(s: CharSequence, arg1: Int, arg2: Int, arg3: Int) {}
            override fun afterTextChanged(s: Editable) {
                resultTv.text = s.toString()
            }
        })
        KeyboardVisibilityEvent.setEventListener(activity) { isOpen ->
            buttonContainer.visibility = when {
                isOpen -> View.GONE
                else -> View.VISIBLE
            }
        }
        if (BuildConfig.IS_BETA) {
            resultTv.text = "（測試人員版本）\n" + resultTv!!.text
        }

        scrollView.setOnClickListener {
            hideKeyboard()
        }

        fontSizeToggleBtn.setOnClickListener {
            when (fontSizeContainer.visibility) {
                View.VISIBLE -> fontSizeContainer.visibility = View.GONE
                else -> fontSizeContainer.visibility = View.VISIBLE
            }
        }

        fontSizeIncreaseBtn.setOnClickListener {
            val size = ConvertUtils.px2sp(resultTv.textSize).toFloat()
            if (size < 200) {
                resultTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, size + sizeChange)
            }
        }

        fontSizeDecreaseBtn.setOnClickListener {
            val size = ConvertUtils.px2sp(resultTv.textSize).toFloat()
            if (size > 16) {
                resultTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, size - sizeChange)
            }
        }

        screenCapBtn.setOnClickListener {
            onClickScreenCap()
        }

        speechToTextBtn.setOnClickListener {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            //		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 10)
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "請說出你的句子")
            try {
                startActivityForResult(intent, REQUEST_SPEECH_TO_TEXT)
            } catch (a: ActivityNotFoundException) {
                Toast.makeText(context, "此設備不支援語音轉文字輸入", Toast.LENGTH_SHORT).show()
            }
        }

        helpBtn.setOnClickListener {
            onClickHelp()
        }

        ttsBtn.setOnClickListener {
            onClickTTS()
        }
    }

    override fun onPause() {
        super.onPause()
        if (tts.isSpeaking) {
            tts.stop()
        }
    }

    private fun onClickScreenCap() {
        hideKeyboard()
        activity?.let { activity ->
            if (ContextCompat.checkSelfPermission(
                    activity,
                    PERMISSION_NAME
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                try {
                    snackbar(view, "截圖中⋯⋯")?.show()
                    // Capture the layout rather then over screen
                    // context.getWindow().getDecorView().getRootView();
                    getBitmapFromView(activity, llView) { bitmap ->
                        onGotScreenBitmap(bitmap)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, "此設備無法截圖，請報告此問題讓我們改進。", Toast.LENGTH_LONG).show()
                }
            } else {
                requestPermissions(arrayOf(PERMISSION_NAME), PERMISSION_REQUEST_CODE)
            }
        }
    }

    private fun onGotScreenBitmap(bitmap: Bitmap) {
        context?.let { context ->
            val uri = saveImage(context, bitmap)

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, "選擇程式"))
        }
    }

    private fun onClickHelp() {
        hideKeyboard()
        context?.let { context ->
            val intent = Intent().setClass(context, SettingsActivity::class.java)
            startActivityForResult(intent, REQUEST_SETTINGS)
        }
    }

    private fun onClickTTS() {
        if (isTTSReady) {
            val yueHKLocale = Locale("yue", "HK")
            if (tts.isLanguageAvailable(yueHKLocale) != TextToSpeech.LANG_COUNTRY_AVAILABLE) {
                snackbar(view, "請先安裝 Google 廣東話（香港）文字轉語音檔案。")?.show()
                val installIntent = Intent()
                installIntent.action = TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA
                startActivity(installIntent)
            } else {
                tts.language = yueHKLocale
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    tts.speak(resultTv.text, TextToSpeech.QUEUE_FLUSH, null, null)
                } else {
                    @Suppress("DEPRECATION")
                    tts.speak(resultTv.text.toString(), TextToSpeech.QUEUE_FLUSH, null)
                }
            }
        } else {
            snackbar(view, "此設備不支援文字轉語音輸出")?.show()
        }
    }

    private fun hideKeyboard() {
        activity?.let { activity ->
            val inputManager =
                activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

            inputManager.hideSoftInputFromWindow(
                activity.currentFocus?.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onClickScreenCap()
            } else {
                openErrorPermissionDialog(context)
            }
        }
    }

    private fun updateFontPath() {
        context?.let { context ->
            val fontVersion = sharedPreferences.getString(PREF_FONT_VERSION, "4700")

            resultTv.typeface = ResourcesCompat.getFont(context, getFontID(fontVersion))

            val fontName: String = getCurrentFontName(context, fontVersion)
            val isShowAlert = sharedPreferences.getBoolean(PREF_FONT_VERSION_ALERT, true)
            if (isShowAlert) {
                snackbar(view, "你正在使用$fontName")?.setAction("設定") { onClickHelp() }?.show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_SETTINGS -> if (resultCode == Activity.RESULT_OK) {
                updateFontPath()
            }
            REQUEST_SPEECH_TO_TEXT -> if (resultCode == Activity.RESULT_OK && data != null) {
                context?.let { context ->
                    val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    MaterialDialog(context)
                        .title(text = "請選擇句子")
                        .listItemsSingleChoice(
                            items = result,
                            waitForPositiveButton = false
                        ) { dialog, _, text ->
                            if (isFirst) {
                                isFirst = false
                                inputEt.setText(text.toString())
                            } else {
                                inputEt.setText(inputEt.text.toString() + " " + text.toString())
                            }
                            dialog.dismiss()
                        }
                        .negativeButton(text = "取消")
                        .show()
                }
            }
        }
    }

    companion object {
        private const val REQUEST_SETTINGS = 1000
        private const val REQUEST_SPEECH_TO_TEXT = 1001
        private const val PERMISSION_NAME = Manifest.permission.WRITE_EXTERNAL_STORAGE
    }
}