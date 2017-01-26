package hk.collaction.freehkkai.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ShareCompat;
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
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hk.collaction.freehkkai.C;
import hk.collaction.freehkkai.R;

/**
 * Created by himphen on 21/5/16.
 */
public class MainFragment extends BaseFragment {

	@BindView(R.id.resultTv)
	TextView resultTv;

	@BindView(R.id.inputEt)
	EditText inputEt;

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
	public void onClickScrollView() {
		hideKeyboard();
	}

	@OnClick(R.id.fontSizeIncreaseBtn)
	public void onClickFontSizeIncrease() {
		float size = C.convertPixelsToSp(resultTv.getTextSize(), mContext);
		if (size < 200) {
			resultTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, size + sizeChange);
		}
	}

	@OnClick(R.id.fontSizeDecreaseBtn)
	public void onClickFontSizeDecrease() {
		float size = C.convertPixelsToSp(resultTv.getTextSize(), mContext);
		if (size > 16) {
			resultTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, size - sizeChange);
		}
	}

	@OnClick(R.id.helpBtn)
	public void onClickHelp() {
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
}
