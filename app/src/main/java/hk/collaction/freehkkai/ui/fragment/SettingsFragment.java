package hk.collaction.freehkkai.ui.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.view.View;

import hk.collaction.freehkkai.C;
import hk.collaction.freehkkai.R;


public class SettingsFragment extends BasePreferenceFragment {

	public static SettingsFragment newInstance() {
		return new SettingsFragment();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.pref_general);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

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
	}
}