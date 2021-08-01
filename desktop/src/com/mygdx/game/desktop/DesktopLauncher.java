package com.mygdx.game.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.mygdx.game.Main;
import com.mygdx.game.Util;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 512 * Util.SCALE;
		config.height = 448 * Util.SCALE;
		config.foregroundFPS = 60;
		config.backgroundFPS = 60;
		config.vSyncEnabled = false;
		config.useGL30 = false;
		new LwjglApplication(new Main(Util.LEARNING_MODE), config);
	}
}
