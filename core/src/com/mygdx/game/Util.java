package com.mygdx.game;

import com.badlogic.gdx.Gdx;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Util {

    public static final int[][][][] FIGURES = loadFigures();

    public static final int SCALE = 2;

    public static final int REPLAY_NUMBER = 50;

    public static final int PLAY_MODE = 0;
    public static final int LEARNING_MODE = 1;
    public static final int DEMO_AI_MODE = 2;

    public static final int BATCH_SIZE = 512;
    public static final int EPOCHS = 1;

    public static int[][][][] loadFigures() {
        int[][][][] figures = new int[7][4][5][5];
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(Gdx.files.internal("figures.txt").read()));
        try {
            for (int i = 0; i < 7; i++) {
                for (int j = 0; j < 4; j++) {
                    for (int k = 0; k < 5; k++) {
                        String[] chars = reader.readLine().split("");
                        for (int l = 0; l < 5; l++) {
                            figures[i][j][k][l] = Integer.parseInt(chars[l]);
                        }
                    }
                    reader.readLine();
                }
            }
        } catch (IOException e) { e.printStackTrace(); }

        return figures;
    }

    public static int dropSpeedFunc(int level) {
        if(level >= 0 && level <= 8) return 48 - level * 5;
        if(level == 9) return 6;
        if(level >= 10 && level <= 12) return 5;
        if(level >= 13 && level <= 15) return 4;
        if(level >= 16 && level <= 18) return 3;
        if(level >= 19 && level <= 28) return 2;
        if(level >= 29) return 1;
        return -1;
    }

    public static int scoreFunc(int level, int linesCleared) {
        switch (linesCleared) {
            case 1: return 40 * (level + 1);
            case 2: return 100 * (level + 1);
            case 3: return 300 * (level + 1);
            case 4: return  1200 * (level + 1);
        }
        return 0;
    }
}