package hk.collaction.freehkkai;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

/**
 * Created by himphen on 24/5/16.
 */
public class App extends MultiDexApplication {

	public void onCreate() {
		super.onCreate();
	}

	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);
		MultiDex.install(this);
	}

}