package com.braunschweiler.games;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.braunschweiler.games.Breakout;
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

public class AndroidLauncher extends AndroidApplication {
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Fabric.with(this, new Crashlytics());
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		initialize(new Breakout(), config);
	}
}
