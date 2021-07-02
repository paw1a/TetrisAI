package com.mygdx.game;

import com.badlogic.gdx.Gdx;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Util {

    public static final int[][][][] FIGURES = loadFigures();

    public static final int SCALE = 2;

    public static final int PARENTS_NUMBER = 500;
    public static final int CHILDREN_NUMBER = 1000;
    public static final double MUTATION_RATE = 0.05;
    public static final double ETA = 100;

    public static int random(int value) {
        return ((((value >> 9) & 1) ^ ((value >> 1) & 1)) << 15) | (value >> 1);
    }

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
}
