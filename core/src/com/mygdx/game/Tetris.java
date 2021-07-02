package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.ArrayList;
import java.util.List;

public class Tetris {

    public int level;
    private int dropCounter;
    private int dxCounter;

    private boolean movingDown = true;
    public boolean isGameOver = false;

    public final TextureRegion[][] levelColors;
    public int[][] field;

    public Figure currentFigure;
    public Figure nextFigure;

    //Statistics
    public int lines;
    public int[] spawnedFigures;
    public int score;

    public Tetris() {
        field = new int[22][10];
        spawnedFigures = new int[7];

        levelColors = new TextureRegion[4][3];
        Texture tileset = new Texture("tetris.png");
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 3; j++) {
                levelColors[i][j] = new TextureRegion(tileset, j * 16, i * 16, 16, 16);
            }
        }

        currentFigure = generateNextFigure();
        spawnedFigures[currentFigure.type.ordinal()]++;
        nextFigure = generateNextFigure();
    }

    public void update() {
        int framesPerDrop = Util.dropSpeedFunc(level);

        if(Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) movingDown = true;
        if(Gdx.input.isKeyPressed(Input.Keys.DOWN) && movingDown) {
            framesPerDrop = 2;
        }

        dropCounter++;
        if(dropCounter >= framesPerDrop) {
            if(isGameOver) System.exit(1);

            dropCounter = 0;
            boolean canDrop = true;
            for(Tile tile : currentFigure.tiles) {
                if (tile.y == 21 || field[tile.y + 1][tile.x] != 0) {
                    canDrop = false;
                    break;
                }
            }
            if(canDrop) {
                for(Tile tile : currentFigure.tiles) {
                    tile.y++;
                }
                currentFigure.y++;
            } else {
                for(Tile tile : currentFigure.tiles) {
                    field[tile.y][tile.x] = tile.color.ordinal() + 1;
                }
                isGameOver = false;
                for(Tile tile : nextFigure.tiles) {
                    if(field[tile.y][tile.x] != 0) {
                        isGameOver = true;
                        break;
                    }
                }

                currentFigure = nextFigure;
                spawnedFigures[currentFigure.type.ordinal()]++;
                nextFigure = generateNextFigure();
                int rowsCount = 0;
                for (int i = 2; i < 22; i++) {
                    boolean deleteRow = true;
                    for (int j = 0; j < 10; j++) {
                        if(field[i][j] == 0) {
                            deleteRow = false;
                            break;
                        }
                    }
                    if(deleteRow) {
                        rowsCount++;
                        lines++;
                        for (int j = i; j >= 2; j--) {
                            System.arraycopy(field[j - 1], 0, field[j], 0, 10);
                        }
                    }
                }
                switch (rowsCount) {
                    case 1: score += 40 * (level + 1); break;
                    case 2: score += 100 * (level + 1); break;
                    case 3: score += 300 * (level + 1); break;
                    case 4: score += 1200 * (level + 1); break;
                }
            }
        }

        int dx = 0;
        dxCounter++;
        boolean momentumDx = false;
        if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) dx = 1;
        if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) dx = -1;

        if(Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            dx = 1;
            momentumDx = true;
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            dx = -1;
            momentumDx = true;
        }

        if(dx != 0 && (dxCounter >= 6 || momentumDx)) {
            boolean canDrop = true;
            for(Tile tile : currentFigure.tiles) {
                if (tile.x + dx == -1 || tile.x + dx == 10 || field[tile.y][tile.x + dx] != 0) {
                    canDrop = false;
                    break;
                }
            }
            if(canDrop) {
                for(Tile tile : currentFigure.tiles) {
                    tile.x += dx;
                }
                currentFigure.x += dx;
            }
            dxCounter = 0;
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.UP)) rotate();

        if(lines >= (level + 1) * 10) level++;
    }

    public void render(SpriteBatch batch) {
        update();
        for (int i = 2; i < 22; i++) {
            for (int j = 0; j < 10; j++) {
                if(field[i][j] != 0)
                    batch.draw(levelColors[level][field[i][j] - 1], (12 + j) * 16 * Util.SCALE, (24 - i) * 16 * Util.SCALE,
                            16 * Util.SCALE, 16 * Util.SCALE);
            }
        }
        for(Tile tile : currentFigure.tiles) {
            if(tile.y < 2) continue;
            batch.draw(levelColors[level][tile.color.ordinal()], (12 + tile.x) * 16 * Util.SCALE, (24 - tile.y) * 16 * Util.SCALE,
                    16 * Util.SCALE, 16 * Util.SCALE);
        }
    }

    private Figure generateNextFigure() {
        int generatedValue;
        if(currentFigure == null)  generatedValue = ((int) (Math.random() * 7)) % 7;
        else {
            generatedValue = ((int) (Math.random() * 8)) % 8;
            if(currentFigure.type.ordinal() == generatedValue || generatedValue == 7)
                generatedValue = ((int) (Math.random() * 7)) % 7;
        }
        movingDown = false;
        return new Figure(Figure.Type.values()[generatedValue]);
    }

    private void rotate() {
        List<Tile> tempTiles = currentFigure.tiles;
        Figure.Rotation newRotation;

        if(currentFigure.rotation.ordinal() + 1 > 3) newRotation = Figure.Rotation.UP;
        else newRotation = Figure.Rotation.values()[currentFigure.rotation.ordinal() + 1];

        boolean canRotate = true;
        int[][] rotatedTiles = Util.FIGURES[currentFigure.type.ordinal()][newRotation.ordinal()];
        currentFigure.tiles = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if(rotatedTiles[i][j] != 0) {
                    if(currentFigure.x + j < 0 || currentFigure.x + j > 9
                            || currentFigure.y + i > 21 || field[currentFigure.y + i][currentFigure.x + j] != 0) {
                        canRotate = false;
                        break;
                    } else currentFigure.tiles.add(new Tile(currentFigure.x + j, currentFigure.y + i, currentFigure.color));
                }
            }
        }
        if(!canRotate) currentFigure.tiles = tempTiles;
        else currentFigure.rotation = newRotation;
    }
}