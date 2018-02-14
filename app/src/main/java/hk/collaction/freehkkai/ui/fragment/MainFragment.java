package hk.collaction.freehkkai.ui.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
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

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hk.collaction.freehkkai.C;
import hk.collaction.freehkkai.Config;
import hk.collaction.freehkkai.R;
import hk.collaction.freehkkai.ui.activity.SettingsActivity;
import uk.co.chrisjenx.calligraphy.CalligraphyUtils;

/**
 * @author Himphen
 */
public class MainFragment extends BaseFragment {

	protected final String PERMISSION_NAME = Manifest.permission.WRITE_EXTERNAL_STORAGE;
	protected static final int REQUEST_SETTINGS = 1000;

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

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View rootView = inflater.inflate(R.layout.fragment_main, container, false);
		ButterKnife.bind(this, rootView);
		return rootView;
	}

	@Override
	public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		settings = PreferenceManager.getDefaultSharedPreferences(mContext);
		updateFontPath();
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
				getActivity(),
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

		if (Config.VERSION.isBeta()) {
			resultTv.setText("（測試人員版本）\n" + resultTv.getText());
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
		float size = C.convertPixelsToSp(resultTv.getTextSize(), mContext);
		if (size < 200) {
			resultTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, size + sizeChange);
		}
	}

	@OnClick(R.id.fontSizeDecreaseBtn)
	void onClickFontSizeDecrease() {
		float size = C.convertPixelsToSp(resultTv.getTextSize(), mContext);
		if (size > 16) {
			resultTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, size - sizeChange);
		}
	}

	@OnClick(R.id.screenCapBtn)
	void onClickScreenCap() {
		hideKeyboard();
		if (ContextCompat.checkSelfPermission(mContext, PERMISSION_NAME) == PackageManager.PERMISSION_GRANTED) {
			try {
				Toast.makeText(mContext, "截圖中⋯⋯", Toast.LENGTH_SHORT).show();
				String now = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
				String folderPath = Environment.getExternalStorageDirectory().toString() + "/FreeHKKai";
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

	@OnClick(R.id.helpBtn)
	void onClickHelp() {
		hideKeyboard();

		Intent intent = new Intent().setClass(mContext, SettingsActivity.class);
		startActivityForResult(intent, REQUEST_SETTINGS);
	}

	private void hideKeyboard() {
		InputMethodManager inputManager = (InputMethodManager)
				mContext.getSystemService(Context.INPUT_METHOD_SERVICE);

		inputManager.hideSoftInputFromWindow(mContext.getCurrentFocus().getWindowToken(),
				InputMethodManager.HIDE_NOT_ALWAYS);
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

		if (requestCode == REQUEST_SETTINGS) {
			if (resultCode == Activity.RESULT_OK) {
				updateFontPath();
			}
		}
	}
}
