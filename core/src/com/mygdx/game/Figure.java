package com.mygdx.game;

import java.util.ArrayList;
import java.util.List;

public class Figure {

    public Type type;
    public Color color;
    public Rotation rotation;
    public List<Tile> tiles;

    public int x;
    public int y;

    public Figure(Type type) {
        this.type = type;
        rotation = Rotation.UP;
        color = Color.values()[type.ordinal() % 3];

        tiles = new ArrayList<>();
        x = 3;
        y = 0;
        int[][] figure = Util.FIGURES[type.ordinal()][rotation.ordinal()];
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if(figure[i][j] != 0) {
                    tiles.add(new Tile(x + j, y + i, color));
                }
            }
        }
    }

    public Figure(Type type, Rotation rotation, int x, int y) {
        this.type = type;
        this.rotation = rotation;
        color = Color.values()[type.ordinal() % 3];

        tiles = new ArrayList<>();
        this.x = x;
        this.y = y;
        int[][] figure = Util.FIGURES[type.ordinal()][rotation.ordinal()];
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if(figure[i][j] != 0) {
                    tiles.add(new Tile(x + j, y + i, color));
                }
            }
        }
    }

    public enum Rotation {
        UP, RIGHT, DOWN, LEFT
    }

    public enum Color {
        FIRST, SECOND, THIRD
    }

    public enum Type {
        T, J, Z, O, S, L, I
    }
}
