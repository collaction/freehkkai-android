package hk.collaction.freehkkai.ui.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.blankj.utilcode.util.ConvertUtils;
import com.google.android.material.snackbar.Snackbar;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hk.collaction.freehkkai.C;
import hk.collaction.freehkkai.Environment;
import hk.collaction.freehkkai.R;
import hk.collaction.freehkkai.ui.activity.SettingsActivity;
import uk.co.chrisjenx.calligraphy.CalligraphyUtils;

/**
 * @author Himphen
 */
public class MainFragment extends BaseFragment {

	private final String PERMISSION_NAME = Manifest.permission.WRITE_EXTERNAL_STORAGE;
	private static final int REQUEST_SETTINGS = 1000;
	private static final int REQUEST_SPEECH_TO_TEXT = 1001;

	@BindView(R.id.titleTv)
	TextView titleTv;
	@BindView(R.id.resultTv)
	TextView resultTv;
	@BindView(R.id.inputEt)
	EditText inputEt;
	@BindView(R.id.fontSizeContainer)
	LinearLayout fontSizeContainer;
	@BindView(R.id.buttonContainer)
	LinearLayout buttonContainer;
	@BindView(R.id.llView)
	LinearLayout llView;

	private int sizeChange = 8;
	private SharedPreferences settings;
	private boolean isFirst = true;
	private TextToSpeech tts;
	private boolean isTTSReady = false;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_main, container, false);
		ButterKnife.bind(this, rootView);
		return rootView;
	}

	@Override
	public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		settings = PreferenceManager.getDefaultSharedPreferences(mContext);
		updateFontPath();


		tts = new TextToSpeech(mContext, new TextToSpeech.OnInitListener() {
			@Override
			public void onInit(int status) {
				isTTSReady = status == TextToSpeech.SUCCESS;
			}
		});

		inputEt.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int arg1, int arg2, int arg3) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				resultTv.setText(s.toString());
			}
		});

		KeyboardVisibilityEvent.setEventListener(
				mContext,
				new KeyboardVisibilityEventListener() {
					@Override
					public void onVisibilityChanged(boolean isOpen) {
						if (isOpen) {
							buttonContainer.setVisibility(View.GONE);
						} else {
							buttonContainer.setVisibility(View.VISIBLE);
						}
					}
				});

		if (Environment.CONFIG.isBeta()) {
			resultTv.setText("（測試人員版本）\n" + resultTv.getText());
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (tts.isSpeaking()) {
			tts.stop();
		}
	}

	@OnClick(R.id.scrollView)
	void onClickScrollView() {
		hideKeyboard();
	}

	@OnClick(R.id.fontSizeToggleBtn)
	void onClickFontSizeToggle() {
		if (fontSizeContainer.getVisibility() == View.VISIBLE) {
			fontSizeContainer.setVisibility(View.GONE);
		} else {
			fontSizeContainer.setVisibility(View.VISIBLE);
		}
	}

	@OnClick(R.id.fontSizeIncreaseBtn)
	void onClickFontSizeIncrease() {
		float size = ConvertUtils.px2sp(resultTv.getTextSize());
		if (size < 200) {
			resultTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, size + sizeChange);
		}
	}

	@OnClick(R.id.fontSizeDecreaseBtn)
	void onClickFontSizeDecrease() {
		float size = ConvertUtils.px2sp(resultTv.getTextSize());
		if (size > 16) {
			resultTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, size - sizeChange);
		}
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	@SuppressLint("SimpleDateFormat")
	@OnClick(R.id.screenCapBtn)
	void onClickScreenCap() {
		hideKeyboard();
		if (ContextCompat.checkSelfPermission(mContext, PERMISSION_NAME) == PackageManager.PERMISSION_GRANTED) {
			try {
				Toast.makeText(mContext, "截圖中⋯⋯", Toast.LENGTH_SHORT).show();
				String now = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
				String folderPath = android.os.Environment.getExternalStorageDirectory().toString() + "/FreeHKKai";
				File folder = new File(folderPath);
				if (!(folder.exists() && folder.isDirectory())) {
					folder.mkdir();
				}

				String mPath = folderPath + "/FreeHKKai_" + now + ".jpeg";

				// Capture the layout rather then over screen
				// View v1 = mContext.getWindow().getDecorView().getRootView();
				View v1 = llView;
				v1.setDrawingCacheEnabled(true);
				Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
				v1.setDrawingCacheEnabled(false);

				File imageFile = new File(mPath);

				FileOutputStream outputStream = new FileOutputStream(imageFile);
				int quality = 100;
				bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
				outputStream.flush();
				outputStream.close();

				/* Be advices, targetSdkVersion 24+ need FileProvider.getUriForFile() rather than Uri.fromFile() */
				Uri photoURI = FileProvider.getUriForFile(mContext, mContext.getPackageName() + ".provider", imageFile);
				Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setDataAndType(photoURI, "image/jpeg");
				intent.putExtra(Intent.EXTRA_STREAM, photoURI);
				intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
				startActivity(Intent.createChooser(intent, "選擇程式"));
			} catch (Exception e) {
				e.printStackTrace();
				Toast.makeText(mContext, "此設備無法截圖，請報告此問題讓我們改進。", Toast.LENGTH_LONG).show();
			}
		} else {
			requestPermissions(new String[]{PERMISSION_NAME}, PERMISSION_REQUEST_CODE);
		}
	}

	@OnClick(R.id.speechToTextBtn)
	void onClickSpeechToText() {
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
//		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
		intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 10);
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "請說出你的句子");
		try {
			startActivityForResult(intent, REQUEST_SPEECH_TO_TEXT);
		} catch (ActivityNotFoundException a) {
			Toast.makeText(mContext, "此設備不支援語音轉文字輸入", Toast.LENGTH_SHORT).show();
		}
	}

	@OnClick(R.id.helpBtn)
	void onClickHelp() {
		hideKeyboard();

		Intent intent = new Intent().setClass(mContext, SettingsActivity.class);
		startActivityForResult(intent, REQUEST_SETTINGS);
	}

	@OnClick(R.id.ttsBtn)
	void onClickTTS() {
		if (isTTSReady) {
			Locale yueHKLocale = new Locale("yue", "HK");

			if (tts.isLanguageAvailable(yueHKLocale) != TextToSpeech.LANG_COUNTRY_AVAILABLE) {
				Toast.makeText(mContext, "請先安裝 Google 廣東話（香港）文字轉語音檔案。", Toast.LENGTH_LONG).show();
				Intent installIntent = new Intent();
				installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
				startActivity(installIntent);
			} else {
				tts.setLanguage(yueHKLocale);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					tts.speak(resultTv.getText(), TextToSpeech.QUEUE_FLUSH, null, null);
				} else {
					tts.speak(resultTv.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
				}
			}
		} else {
			Toast.makeText(mContext, "此設備不支援文字轉語音輸出", Toast.LENGTH_SHORT).show();
		}
	}

	private void hideKeyboard() {
		InputMethodManager inputManager = (InputMethodManager)
				mContext.getSystemService(Context.INPUT_METHOD_SERVICE);

		if (inputManager != null) {
			if (mContext.getCurrentFocus() != null) {
				inputManager.hideSoftInputFromWindow(mContext.getCurrentFocus().getWindowToken(),
						InputMethodManager.HIDE_NOT_ALWAYS);
			}
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == PERMISSION_REQUEST_CODE) {
			if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				onClickScreenCap();
			} else {
				C.openErrorPermissionDialog(mContext);
			}
		}
	}

	private void updateFontPath() {
		String fontPath = settings.getString(C.PREF_FONT_VERSION, "fonts/freehkkai_4700.ttf");
		CalligraphyUtils.applyFontToTextView(mContext, resultTv, fontPath);

		String fontName = C.getCurrentFontName(mContext, fontPath);

		boolean isShowAlert = settings.getBoolean(C.PREF_FONT_VERSION_ALERT, true);
		if (isShowAlert) {
			Snackbar.make(getView(), "你正在使用" + fontName, Snackbar.LENGTH_LONG).setAction("設定", new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					onClickHelp();
				}
			}).show();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
			case REQUEST_SETTINGS:
				if (resultCode == Activity.RESULT_OK) {
					updateFontPath();
				}
				break;
			case REQUEST_SPEECH_TO_TEXT:
				if (resultCode == Activity.RESULT_OK && data != null) {
					ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

					new MaterialDialog.Builder(mContext)
							.title("請選擇句子")
							.items(result)
							.itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
								@Override
								public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
									if (isFirst) {
										isFirst = false;
										inputEt.setText(text.toString());
									} else {
										inputEt.setText(inputEt.getText() + " " + text.toString());
									}
									return false;
								}
							})
							.negativeText("取消")
							.show();
				}
				break;
		}
	}
}
