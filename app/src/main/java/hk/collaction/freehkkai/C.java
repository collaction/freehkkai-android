package hk.collaction.freehkkai;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

public class C extends Util {
	public static final String TAG = "TAG";
	public static final String PREF_FONT_VERSION = "pref_font_version";
	public static final String PREF_FONT_VERSION_ALERT = "pref_font_version_alert";

	public static void openErrorPermissionDialog(final Activity activity) {
		MaterialDialog.Builder dialog = new MaterialDialog.Builder(activity)
				.customView(R.layout.dialog_permission, true)
				.cancelable(false)
				.negativeText(R.string.ui_cancel)
				.positiveText(R.string.dialog_permission_denied_posbtn)
				.onNegative(new MaterialDialog.SingleButtonCallback() {
					@Override
					public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
						activity.finish();
					}
				})
				.onPositive(new MaterialDialog.SingleButtonCallback() {
					@Override
					public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
						Intent intent = new Intent();
						intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
						intent.addCategory(Intent.CATEGORY_DEFAULT);
						intent.setData(Uri.parse("package:" + activity.getPackageName()));
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
						intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
						activity.startActivity(intent);
						activity.finish();
					}
				});
		dialog.show();
	}

	public static String getCurrentFontName(Context mContext) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
		String fontPath = settings.getString(C.PREF_FONT_VERSION, "fonts/freehkkai_4700.ttf");

		return getCurrentFontName(mContext, fontPath);
	}

	public static String getCurrentFontName(Context mContext, String fontPath) {
		String[] fontVersionArray = mContext.getResources().getStringArray(R.array.font_version_array);
		String fontName = fontVersionArray[0];
		switch (fontPath) {
			case "fonts/freehkkai_extended.ttf":
				fontName = fontVersionArray[1];
				break;
		}

		return fontName;
	}
}
