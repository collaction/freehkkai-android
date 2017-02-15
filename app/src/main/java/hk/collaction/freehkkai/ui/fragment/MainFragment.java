package hk.collaction.freehkkai.ui.fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hk.collaction.freehkkai.C;
import hk.collaction.freehkkai.R;

/**
 * Created by himphen on 21/5/16.
 */
public class MainFragment extends BaseFragment {

	protected final String PERMISSION_NAME = Manifest.permission.WRITE_EXTERNAL_STORAGE;

	@BindView(R.id.resultTv)
	TextView resultTv;
	@BindView(R.id.inputEt)
	EditText inputEt;
	@BindView(R.id.fontSizeContainer)
	LinearLayout fontSizeContainer;

	private int sizeChange = 8;

	public MainFragment() {
		// Required empty public constructor
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View rootView = inflater.inflate(R.layout.fragment_main, container, false);
		ButterKnife.bind(this, rootView);
		return rootView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
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

				String mPath = folderPath + "/FreeHKKai_" + now + ".jpg";

				View v1 = mContext.getWindow().getDecorView().getRootView();
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
				Toast.makeText(mContext, "無法截圖", Toast.LENGTH_LONG).show();
			}
		} else {
			requestPermissions(new String[]{PERMISSION_NAME}, PERMISSION_REQUEST_CODE);
		}
	}

	@OnClick(R.id.helpBtn)
	void onClickHelp() {
		hideKeyboard();

		View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_help, null);

		Spanned result;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			result = Html.fromHtml(readHelpHtml(), Html.FROM_HTML_MODE_LEGACY);
		} else {
			//noinspection deprecation
			result = Html.fromHtml(readHelpHtml());
		}
		TextView tv = ((TextView) view.findViewById(R.id.helpTv));
		tv.setText(result);
		tv.setMovementMethod(LinkMovementMethod.getInstance());

		MaterialDialog.Builder dialog = new MaterialDialog.Builder(mContext)
				.title("自由香港楷書 Android App")
				.customView(view, true)
				.onPositive(new MaterialDialog.SingleButtonCallback() {
					@Override
					public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
						Uri uri = Uri.parse("https://www.collaction.hk/s/freehkfonts");
						Intent intent = new Intent(Intent.ACTION_VIEW, uri);
						startActivity(intent);
					}
				})
				.onNeutral(new MaterialDialog.SingleButtonCallback() {
					@Override
					public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
						ShareCompat.IntentBuilder
								.from(mContext)
								.setText("下載「自由香港楷書」程式，就可以查詢支援超過 4700 個香港教育局楷書參考寫法，解決因為「電腦輸入法」而令學生 / 家長 / 教師混淆而寫錯字的問題。\n\n" + "https://play.google.com/store/apps/details?id=" + mContext.getPackageName())
								.setType("text/plain")
								.setChooserTitle("選擇程式")
								.startChooser();
					}
				})
				.onNegative(new MaterialDialog.SingleButtonCallback() {
					@Override
					public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
						Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=" + mContext.getPackageName());
						Intent intent = new Intent(Intent.ACTION_VIEW, uri);
						startActivity(intent);
					}
				})
				.positiveText("了解更多")
				.neutralText("分享程式")
				.negativeText("評分");
		dialog.show();
	}

	private void hideKeyboard() {
		InputMethodManager inputManager = (InputMethodManager)
				mContext.getSystemService(Context.INPUT_METHOD_SERVICE);

		inputManager.hideSoftInputFromWindow(mContext.getCurrentFocus().getWindowToken(),
				InputMethodManager.HIDE_NOT_ALWAYS);
	}

	private String readHelpHtml() {
		InputStream inputStream = getResources().openRawResource(R.raw.help);
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		int i;
		try {
			i = inputStream.read();
			while (i != -1) {
				byteArrayOutputStream.write(i);
				i = inputStream.read();
			}
			inputStream.close();
		} catch (IOException ignored) {
		}
		return byteArrayOutputStream.toString();
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
}
