package hk.collaction.freehkkai.helper;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewConfiguration;
import android.widget.RelativeLayout;

import com.blankj.utilcode.util.SizeUtils;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Locale;

import hk.collaction.freehkkai.BuildConfig;
import hk.collaction.freehkkai.Environment;

/**
 * UtilHelper Class
 * Created by Himphen on 10/1/2016.
 */
@SuppressWarnings("unused")
public class UtilHelper {
	public static final String TAG = "tag";
	public static final String DEBUG_TAG = "debug_tag";

	public static final String PREF_IAP = "iap";
	public static final String PREF_LANGUAGE = "PREF_LANGUAGE";
	public static final String PREF_LANGUAGE_COUNTRY = "PREF_LANGUAGE_COUNTRY";

	@Nullable
	public static AdView initAdView(Activity c, RelativeLayout adLayout) {
		return initAdView(c, adLayout, false);
	}

	@Nullable
	public static AdView initAdView(Activity mContext, RelativeLayout adLayout, Boolean isPreserveSpace) {
		if (isPreserveSpace) {
			adLayout.getLayoutParams().height = SizeUtils.dp2px(50);
		}

		SharedPreferences defaultPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
		AdView adView = null;
		try {
			if (!defaultPreferences.getBoolean(PREF_IAP, false)) {
				adView = new AdView(mContext);
				adView.setAdUnitId(BuildConfig.ADMOB_BANNER_ID);
				adView.setAdSize(AdSize.BANNER);
				adLayout.addView(adView);

				AdRequest.Builder adRequest = new AdRequest.Builder();
				adRequest.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);

				for (String id : BuildConfig.ADMOB_DEVICE_ID) {
					adRequest.addTestDevice(id);
				}

				adView.loadAd(adRequest.build());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return adView;
	}

	@SuppressWarnings("JavaReflectionMemberAccess")
	public static void forceShowMenu(Context mContext) {
		try {
			ViewConfiguration config = ViewConfiguration.get(mContext);
			Field menuKeyField = ViewConfiguration.class
					.getDeclaredField("sHasPermanentMenuKey");
			menuKeyField.setAccessible(true);
			menuKeyField.setBoolean(config, false);
		} catch (Exception ignored) {
		}
	}

	public static double round(double value, int places) {
		if (places < 0) throw new IllegalArgumentException();

		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}

	public static String formatSignificant(double value, int significant) {
		MathContext mathContext = new MathContext(significant, RoundingMode.DOWN);
		BigDecimal bigDecimal = new BigDecimal(value, mathContext);
		return bigDecimal.toPlainString();
	}

	public static void detectLanguage(Context mContext) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
		String language = preferences.getString(UtilHelper.PREF_LANGUAGE, "");
		String languageCountry = preferences.getString(UtilHelper.PREF_LANGUAGE_COUNTRY, "");

		if (language.equals("")) {
			Locale locale;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
				locale = Resources.getSystem().getConfiguration().getLocales().get(0);
			} else {
				locale = Resources.getSystem().getConfiguration().locale;
			}
			language = locale.getLanguage();
			languageCountry = locale.getCountry();
		}


		Resources res = mContext.getResources();
		Configuration conf = res.getConfiguration();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			conf.setLocale(new Locale(language, languageCountry));
		} else {
			conf.locale = new Locale(language, languageCountry);
		}

		DisplayMetrics dm = res.getDisplayMetrics();
		res.updateConfiguration(conf, dm);
	}

	public static void log(String message) {
		Log.d(TAG, message);
	}

	public static void debug(String message) {
		if (Environment.CONFIG.isDebug()) {
			Log.d(DEBUG_TAG, message);
		}
	}
}
