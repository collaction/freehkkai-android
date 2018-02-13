package hk.collaction.freehkkai.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.preference.Preference;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;

import hk.collaction.freehkkai.C;
import hk.collaction.freehkkai.R;


public class SettingsFragment extends BasePreferenceFragment {

	private SharedPreferences settings;
	private Preference prefFontVersion;

	public static SettingsFragment newInstance() {
		return new SettingsFragment();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.pref_general);
	}

	@Override
	public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		settings = PreferenceManager.getDefaultSharedPreferences(mContext);

		/* Set version */
		Preference prefVersion = findPreference("pref_version");
		prefVersion.setSummary(C.getCurrentVersionName(mContext));

		findPreference("pref_report").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent(Intent.ACTION_SEND);

				String meta = "Android Version: " + Build.VERSION.RELEASE + "\n";
				meta += "SDK Level: " + String.valueOf(Build.VERSION.SDK_INT) + "\n";
				meta += "Version: " + C.getCurrentVersionName(mContext) + "\n";
				meta += "Brand: " + Build.BRAND + "\n";
				meta += "Model: " + Build.MODEL + "\n\n";
				meta += "我的訊息是如下\n\n";

				intent.setType("message/rfc822");
				intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"hello@collaction.hk"});
				intent.putExtra(Intent.EXTRA_SUBJECT, "自由香港楷書回報問題");
				intent.putExtra(Intent.EXTRA_TEXT, meta);
				startActivity(intent);
				return false;
			}
		});

		findPreference("pref_rate").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Uri uri = Uri.parse("market://details?id=" + mContext.getPackageName());
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(intent);
				return false;
			}
		});

		findPreference("pref_share").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_SEND);
				intent.putExtra(Intent.EXTRA_TEXT, "下載「自由香港楷書」程式，就可以查詢支援超過 4700 個香港教育局楷書參考寫法，解決因為「電腦輸入法」而令學生 / 家長 / 教師混淆而寫錯字的問題。\n\n" + "https://play.google.com/store/apps/details?id=" + mContext.getPackageName());
				intent.setType("text/plain");
				startActivity(Intent.createChooser(intent, "分享此程式"));
				return false;
			}
		});

		findPreference("pref_author").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Uri uri = Uri.parse("market://search?q=pub:\"Collaction 小隊\"");
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(intent);
				return false;
			}
		});

		findPreference("pref_collaction").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Uri uri = Uri.parse("https://www.collaction.hk/s/collactionopensource");
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(intent);
				return false;
			}
		});

		findPreference("pref_hkfreekai").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Uri uri = Uri.parse("https://www.collaction.hk/s/freehkfonts");
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(intent);
				return false;
			}
		});

		prefFontVersion = findPreference("pref_font_version");
		prefFontVersion.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				String fontVersion = settings.getString("pref_font_version", "fonts/freehkkai_4700.ttf");
				int a = 0;
				switch (fontVersion) {
					case "fonts/freehkkai_extended.ttf":
						a = 1;
						break;
				}

				MaterialDialog.Builder dialog = new MaterialDialog.Builder(mContext)
						.title("切換字型檔案版本")
						.items(R.array.font_version_array)
						.itemsCallbackSingleChoice(a, new MaterialDialog.ListCallbackSingleChoice() {
							@Override
							public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
								switch (which) {
									case 0:
										settings.edit().putString(C.PREF_FONT_VERSION, "fonts/freehkkai_4700.ttf").apply();
										break;
									case 1:
										settings.edit().putString(C.PREF_FONT_VERSION, "fonts/freehkkai_extended.ttf").apply();
										break;
								}
								setFontVersionSummary();
								return false;
							}
						})
						.negativeText(R.string.ui_cancel);
				dialog.show();

				mContext.setResult(Activity.RESULT_OK);

				return false;
			}
		});
		setFontVersionSummary();
	}

	void setFontVersionSummary() {
		prefFontVersion.setSummary(C.getCurrentFontName(mContext));
	}
}