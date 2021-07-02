package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.Locale;

public class Main extends ApplicationAdapter {
	private SpriteBatch batch;
	private Tetris game;
	private Texture hud;
	private BitmapFont font;
	
	@Override
	public void create () {
		batch = new SpriteBatch();
		game = new Tetris();
		hud = new Texture("hud.png");
		font = new BitmapFont(Gdx.files.internal("font.fnt"));
		font.getRegion().getTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
	}

	@Override
	public void render () {
		if(game.isGameOver) game = new Tetris();
		ScreenUtils.clear(0, 0, 0, 1);
		batch.begin();

		batch.draw(hud, 0, 0, hud.getWidth() * Util.SCALE, hud.getHeight() * Util.SCALE);
		game.render(batch);

		font.setColor(Color.WHITE);
		font.getData().setScale(0.7f, 0.8f);
		font.draw(batch, "STATISTICS", 80, 640);
		font.getData().setScale(1f);
		font.draw(batch, "A-TYPE", 96, 796);
		font.draw(batch, String.format(Locale.ENGLISH, "LINES-%03d", game.lines), 404, 828);
		font.draw(batch, "TOP", 770, 796);
		font.draw(batch, "010000", 770, 764);
		font.draw(batch, "SCORE", 770, 700);
		font.draw(batch, String.format(Locale.ENGLISH, "%06d", game.score), 770, 668);
		font.draw(batch, "NEXT", 770, 504);
		font.draw(batch, "LEVEL", 775, 288);
		font.draw(batch, String.format(Locale.ENGLISH, "%02d", game.level), 820, 256);

		font.setColor(Color.valueOf("#d82800"));
		for (int i = 0; i < 7; i++) {
			for (int j = 0; j < 5; j++) {
				for (int k = 0; k < 5; k++) {
					if(Util.FIGURES[i][0][j][k] != 0)
						batch.draw(game.levelColors[game.level][i % 3], 80 + k * 24, 600 - i * 70 - j * 24, 24, 24);
				}
			}
			font.draw(batch, String.format(Locale.ENGLISH, "%03d", game.spawnedFigures[i]), 200, 570 - i * 70);
		}
		for (int j = 0; j < 5; j++) {
			for (int k = 0; k < 5; k++) {
				if(Util.FIGURES[game.nextFigure.type.ordinal()][0][j][k] != 0)
					batch.draw(game.levelColors[game.level][game.nextFigure.color.ordinal()], 766 + k * 30,
							470 - j * 30, 30, 30);
			}
		}

		batch.end();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
	}
}