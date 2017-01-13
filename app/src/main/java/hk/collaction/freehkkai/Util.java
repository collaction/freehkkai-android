package hk.collaction.freehkkai;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.ViewConfiguration;
import android.widget.RelativeLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Locale;

/**
 * Util Class
 * Created by Himphen on 10/1/2016.
 */
@SuppressWarnings("unused")
public class Util {

	public static final String PREF_IAP = "iap";
	public static final String PREF_LANGUAGE = "PREF_LANGUAGE";

	@Nullable
	public static AdView initAdView(Activity c, RelativeLayout adLayout) {
		AdView adView = null;
		try {
			if (!PreferenceManager.getDefaultSharedPreferences(c).getBoolean(Util.PREF_IAP, false)) {
				adView = new AdView(c);
				adView.setAdUnitId(BuildConfig.ADMOB_KEY);
				adView.setAdSize(AdSize.BANNER);
				adLayout.addView(adView);

				AdRequest.Builder adRequest = new AdRequest.Builder();
				adRequest.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
				adRequest.addTestDevice(BuildConfig.DEVICE_ID);
				adView.loadAd(adRequest.build());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return adView;
	}

	public static void forceShowMenu(Context mContext) {
		try {
			ViewConfiguration config = ViewConfiguration.get(mContext);
			Field menuKeyField = ViewConfiguration.class
					.getDeclaredField("sHasPermanentMenuKey");
			if (menuKeyField != null) {
				menuKeyField.setAccessible(true);
				menuKeyField.setBoolean(config, false);
			}
		} catch (Exception ignored) {
		}
	}

	public static String getCurrentVersionName(Context c) {
		try {
			PackageInfo pInfo = c.getPackageManager().getPackageInfo(
					c.getPackageName(), 0);
			return pInfo.versionName;
		} catch (PackageManager.NameNotFoundException e) {
			return "NA";
		}
	}

	public static Bitmap drawableToBitmap(Drawable drawable) {
		if (drawable instanceof BitmapDrawable) {
			return ((BitmapDrawable) drawable).getBitmap();
		}

		final int width = !drawable.getBounds().isEmpty() ? drawable
				.getBounds().width() : drawable.getIntrinsicWidth();

		final int height = !drawable.getBounds().isEmpty() ? drawable
				.getBounds().height() : drawable.getIntrinsicHeight();

		final Bitmap bitmap = Bitmap.createBitmap(width <= 0 ? 1 : width,
				height <= 0 ? 1 : height, Bitmap.Config.ARGB_8888);

		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		drawable.draw(canvas);

		return bitmap;
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

	@SuppressWarnings("deprecation")
	public static void detectLanguage(Context context) {
		SharedPreferences setting = PreferenceManager
				.getDefaultSharedPreferences(context);
		String language = setting.getString(Util.PREF_LANGUAGE, "auto");
		Resources res = context.getResources();
		Configuration conf = res.getConfiguration();
		switch (language) {
			case "en":
			case "zh":
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
					conf.setLocale(new Locale(language));
				} else {
					conf.locale = new Locale(language);
				}
				break;
			default:
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
					conf.setLocale(Resources.getSystem().getConfiguration().getLocales().get(0));
				} else {
					conf.locale = Resources.getSystem().getConfiguration().locale;
				}
		}
		DisplayMetrics dm = res.getDisplayMetrics();
		res.updateConfiguration(conf, dm);
	}

	public static boolean isTablet(Context context) {
		return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
	}

	/**
	 * This method converts dp unit to equivalent pixels, depending on device density.
	 *
	 * @param dp      A value in dp (density independent pixels) unit. Which we need to convert into pixels
	 * @param context Context to get resources and device specific display metrics
	 * @return A float value to represent px equivalent to dp depending on device density
	 */
	public static float convertDpToPixel(float dp, Context context) {
		Resources resources = context.getResources();
		DisplayMetrics metrics = resources.getDisplayMetrics();
		return dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
	}

	/**
	 * This method converts device specific pixels to density independent pixels.
	 *
	 * @param px      A value in px (pixels) unit. Which we need to convert into dp
	 * @param context Context to get resources and device specific display metrics
	 * @return A float value to represent dp equivalent to px value
	 */
	public static float convertPixelsToDp(float px, Context context) {
		Resources resources = context.getResources();
		DisplayMetrics metrics = resources.getDisplayMetrics();
		return px / ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
	}

	/**
	 * This method converts device specific pixels to scaled pixels.
	 *
	 * @param px      A value in px (pixels) unit. Which we need to convert into sp
	 * @param context Context to get resources and device specific display metrics
	 * @return A float value to represent sp equivalent to px value
	 */
	public static float convertPixelsToSp(float px, Context context) {
		Resources resources = context.getResources();
		DisplayMetrics metrics = resources.getDisplayMetrics();
		return px / metrics.scaledDensity;
	}
}
