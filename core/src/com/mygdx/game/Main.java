package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.mygdx.game.ai.NeuralNetwork;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.GradientNormalization;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.modelimport.keras.KerasModelImport;
import org.deeplearning4j.nn.modelimport.keras.KerasSequentialModel;
import org.deeplearning4j.nn.modelimport.keras.exceptions.InvalidKerasConfigurationException;
import org.deeplearning4j.nn.modelimport.keras.exceptions.UnsupportedKerasConfigurationException;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.learning.config.Nadam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

public class Main extends ApplicationAdapter {

	private SpriteBatch batch;
	private Tetris game;
	private Texture hud;
	private Texture background;
	private BitmapFont font;

	public static int GAME_MODE;
	public static boolean render;

	public Main(int gameMode) {
		GAME_MODE = gameMode;
	}
	
	@Override
	public void create () {
		batch = new SpriteBatch();
		hud = new Texture("hud.png");
		background = new Texture("background.png");
		font = new BitmapFont(Gdx.files.internal("font.fnt"));
		font.getRegion().getTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

		if(GAME_MODE == Util.PLAY_MODE)
			game = new Tetris();
		else if(GAME_MODE == Util.DEMO_AI_MODE)
			game = new Tetris(loadModel("C:/Users/paw1a/IdeaProjects/libgdx/TetrisAI/core/assets/best_model.txt"));
		else game = new Tetris(buildModel());
		render = true;
	}

	@Override
	public void render () {
		game.update();
		ScreenUtils.clear(0, 0, 0, 1);
		if(game.learning) {
			batch.begin();

			batch.draw(hud, 0, 0, hud.getWidth() * Util.SCALE, hud.getHeight() * Util.SCALE);
			batch.draw(background, 192 * Util.SCALE, 96, background.getWidth(), background.getHeight());

			batch.end();
		} else {
			batch.begin();

			batch.draw(hud, 0, 0, hud.getWidth() * Util.SCALE, hud.getHeight() * Util.SCALE);
			game.render(batch);

			font.setColor(Color.WHITE);
			font.getData().setScale(0.7f, 0.8f);
			font.draw(batch, "STATISTICS", 80, 640);
			font.getData().setScale(1f);
			font.draw(batch, "E." + game.episode, 96, 796);
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
						if (Util.FIGURES[i][0][j][k] != 0)
							batch.draw(game.levelColors[game.level % 30][i % 3], 80 + k * 24, 600 - i * 70 - j * 24, 24, 24);
					}
				}
				font.draw(batch, String.format(Locale.ENGLISH, "%03d", game.spawnedFigures[i]), 200, 570 - i * 70);
			}
			for (int j = 0; j < 5; j++) {
				for (int k = 0; k < 5; k++) {
					if (Util.FIGURES[game.nextFigure.type.ordinal()][0][j][k] != 0)
						batch.draw(game.levelColors[game.level % 30][game.nextFigure.color.ordinal()], 766 + k * 30,
								470 - j * 30, 30, 30);
				}
			}

			batch.end();
		}
	}

	private MultiLayerNetwork buildModel() {
		try {
			MultiLayerNetwork model = KerasModelImport
					.importKerasSequentialModelAndWeights("C:/Users/paw1a/IdeaProjects/libgdx/TetrisAI/core/assets/keras-model-import.h5");
			model.setInputMiniBatchSize(Util.BATCH_SIZE);
			model.setEpochCount(Util.EPOCHS);
			return model;
		} catch (IOException | UnsupportedKerasConfigurationException | InvalidKerasConfigurationException e) {
			e.printStackTrace();
		}
		return null;
	}

	private MultiLayerNetwork loadModel(String name) {
		File file = new File(name);
		try {
			return ModelSerializer.restoreMultiLayerNetwork(file);
		} catch (IOException e) {e.printStackTrace(); }
		return null;
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		font.dispose();
		hud.dispose();
	}
}