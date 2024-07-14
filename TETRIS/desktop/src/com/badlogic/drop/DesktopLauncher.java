package com.badlogic.drop;

import static com.badlogic.drop.Tetris.BOARD_HEIGHT;
import static com.badlogic.drop.Tetris.BOARD_WIDTH;
import static com.badlogic.drop.Tetris.TILE_SIZE;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

// Please note that on macOS your application needs to be started with the -XstartOnFirstThread JVM argument
public class DesktopLauncher {
	public static void main (String[] arg) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setForegroundFPS(60);
		config.setWindowedMode(BOARD_WIDTH * TILE_SIZE, BOARD_HEIGHT * TILE_SIZE); // установка фиксированного размера окна
		config.setResizable(false); // запрет на изменение размеров окна
		config.setTitle("TETRIS");
		new Lwjgl3Application(new Tetris(), config);
	}
}
