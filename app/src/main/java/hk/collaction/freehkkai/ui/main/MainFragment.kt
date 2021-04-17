package hk.collaction.freehkkai.ui.main

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.res.ResourcesCompat
import androidx.preference.PreferenceManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.blankj.utilcode.util.ConvertUtils
import com.github.javiersantos.appupdater.AppUpdater
import com.github.javiersantos.appupdater.enums.Display
import hk.collaction.freehkkai.BuildConfig
import hk.collaction.freehkkai.databinding.FragmentMainBinding
import hk.collaction.freehkkai.ui.base.BaseFragment
import hk.collaction.freehkkai.ui.settings.SettingsActivity
import hk.collaction.freehkkai.util.Utils.PREF_FONT_VERSION
import hk.collaction.freehkkai.util.Utils.PREF_FONT_VERSION_ALERT
import hk.collaction.freehkkai.util.Utils.getBitmapFromView
import hk.collaction.freehkkai.util.Utils.getCurrentFontName
import hk.collaction.freehkkai.util.Utils.getFontID
import hk.collaction.freehkkai.util.Utils.saveImage
import hk.collaction.freehkkai.util.Utils.snackbar
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener
import java.util.*


/**
 * @author Himphen
 */
class MainFragment : BaseFragment<FragmentMainBinding>() {

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentMainBinding.inflate(inflater, container, false)

    private val sizeChange = 8
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var tts: TextToSpeech
    private var isFirst = true
    private var isTTSReady = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        AppUpdater(context)
            .showEvery(4)
            .setDisplay(Display.NOTIFICATION)
            .start()

        updateFontPath()
        tts = TextToSpeech(context) { status -> isTTSReady = status == TextToSpeech.SUCCESS }
        viewBinding?.inputEt?.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(cs: CharSequence, arg1: Int, arg2: Int, arg3: Int) {}
            override fun beforeTextChanged(s: CharSequence, arg1: Int, arg2: Int, arg3: Int) {}
            override fun afterTextChanged(s: Editable) {
                viewBinding?.resultTv?.text = s.toString()
            }
        })

        activity?.let { activity ->
            KeyboardVisibilityEvent.setEventListener(
                activity,
                viewLifecycleOwner,
                object : KeyboardVisibilityEventListener {
                    override fun onVisibilityChanged(isOpen: Boolean) {
                        if (activity.isFinishing) return

                        viewBinding?.buttonContainer?.visibility = when {
                            isOpen -> View.GONE
                            else -> View.VISIBLE
                        }
                    }
                }
            )
        }

        if (BuildConfig.IS_BETA) {
            viewBinding?.resultTv?.text = "（測試人員版本）\n" + viewBinding?.resultTv?.text
        }

        viewBinding?.scrollView?.setOnClickListener {
            hideKeyboard()
        }

        viewBinding?.fontSizeToggleBtn?.setOnClickListener {
            when (viewBinding?.fontSizeContainer?.visibility) {
                View.VISIBLE -> viewBinding?.fontSizeContainer?.visibility = View.GONE
                else -> viewBinding?.fontSizeContainer?.visibility = View.VISIBLE
            }
        }

        viewBinding?.fontSizeIncreaseBtn?.setOnClickListener {
            viewBinding?.let { viewBinding ->
                val size = ConvertUtils.px2sp(viewBinding.resultTv.textSize).toFloat()
                if (size < 200) {
                    viewBinding.resultTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, size + sizeChange)
                }
            }
        }

        viewBinding?.fontSizeDecreaseBtn?.setOnClickListener {
            viewBinding?.let { viewBinding ->
                val size = ConvertUtils.px2sp(viewBinding.resultTv.textSize).toFloat()
                if (size > 16) {
                    viewBinding.resultTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, size - sizeChange)
                }
            }
        }

        viewBinding?.screenCapBtn?.setOnClickListener {
            onClickScreenCap()
        }

        viewBinding?.speechToTextBtn?.setOnClickListener {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-HK")
            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 10)
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "請說出你的句子")
            try {
                ttsLauncher.launch(intent)
            } catch (a: ActivityNotFoundException) {
                Toast.makeText(context, "此設備不支援語音轉文字輸入", Toast.LENGTH_SHORT).show()
            }
        }

        viewBinding?.helpBtn?.setOnClickListener {
            onClickHelp()
        }

        viewBinding?.ttsBtn?.setOnClickListener {
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
            viewBinding?.llView?.let { llView ->
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
            settingLauncher.launch(Intent(context, SettingsActivity::class.java))
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
                tts.speak(viewBinding?.resultTv?.text, TextToSpeech.QUEUE_FLUSH, null, null)
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

    private fun updateFontPath() {
        context?.let { context ->
            val fontVersion = sharedPreferences.getString(PREF_FONT_VERSION, "4700")

            viewBinding?.resultTv?.typeface =
                ResourcesCompat.getFont(context, getFontID(fontVersion))

            val fontName: String = getCurrentFontName(context, fontVersion)
            val isShowAlert = sharedPreferences.getBoolean(PREF_FONT_VERSION_ALERT, true)
            if (isShowAlert) {
                snackbar(view, "你正在使用$fontName")?.setAction("設定") { onClickHelp() }?.show()
            }
        }
    }

    private val ttsLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode != Activity.RESULT_OK) return@registerForActivityResult

            context?.let { context ->
                val ttsResult = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                MaterialDialog(context)
                    .title(text = "請選擇句子")
                    .listItemsSingleChoice(
                        items = ttsResult,
                        waitForPositiveButton = false
                    ) { dialog, _, text ->
                        if (isFirst) {
                            isFirst = false
                            viewBinding?.inputEt?.setText(text.toString())
                        } else {
                            viewBinding?.inputEt?.setText(viewBinding?.inputEt?.text?.toString() + " " + text.toString())
                        }
                        dialog.dismiss()
                    }
                    .negativeButton(text = "取消")
                    .show()
            }
        }

    private val settingLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode != Activity.RESULT_OK) return@registerForActivityResult

            updateFontPath()
        }
}