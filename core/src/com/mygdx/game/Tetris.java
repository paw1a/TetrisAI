package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mygdx.game.ai.NeuralNetwork;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Nadam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.util.*;
import java.util.stream.Collectors;

public class Tetris {

    public int level;
    private int dropCounter;
    private int dxCounter;

    private boolean movingDown = true;
    public boolean isGameOver;

    public TextureRegion[][] levelColors;
    public int[][] field;

    public Figure currentFigure;
    public Figure nextFigure;

    //Statistics
    public int lines;
    public int[] spawnedFigures;
    public int score;

    public MultiLayerNetwork model;
    private final List<StateTransition> memoryReplay;
    private static int episode;
    private double epsilon;
    public boolean learning;
    private double[] currentState;

    private Thread learningThread;

    public Tetris() {
        memoryReplay = new ArrayList<>();
        initGraphics();
        initGame();
    }

    public Tetris(MultiLayerNetwork model) {
        this.model = model;
        epsilon = 1;
        memoryReplay = new ArrayList<>();
        initGraphics();
        initGame();
    }

    private void initGraphics() {
        levelColors = new TextureRegion[40][3];
        Texture tileset = new Texture("tetris.png");
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 3; j++) {
                levelColors[i][j] = new TextureRegion(tileset, j * 16, i * 16, 16, 16);
            }
        }
    }

    private void initGame() {
        isGameOver = false;
        score = 0;
        lines = 0;
        level = 0;
        field = new int[22][10];
        spawnedFigures = new int[7];

        currentFigure = generateNextFigure();
        spawnedFigures[currentFigure.type.ordinal()]++;
        nextFigure = generateNextFigure();

        currentState = getStats(field);
    }

    private void makeGameReplays() {
        for (int i = 0; i < Util.REPLAY_NUMBER-1; i++) {
            initGame();
            while (true) {
                if (isValid(currentFigure, field)) {
                    List<StateAction> generatedStates = generateStates(currentFigure, field);
                    StateAction bestState;
                    if (Math.random() < epsilon) {
                        bestState = generatedStates.get((int) (Math.random() * (double) generatedStates.size()));
                    } else
                        bestState = generatedStates
                            .stream()
                            .max(Comparator.comparingDouble(value -> model.output(vectorData(value.state)).getDouble(0)))
                            .orElse(null);

                    currentFigure = bestState.action;
                    dropFigure(currentFigure, field);
                    clearLines(field);
                    currentFigure = nextFigure;
                    nextFigure = generateNextFigure();

                    double reward = 1 + Math.pow(bestState.state[3], 2) * 10;
                    boolean isGameOver = false;
                    if(!isValid(currentFigure, field)) {
                        reward -= 2;
                        isGameOver = true;
                    }

                    memoryReplay.add(new StateTransition(currentState, isGameOver ? null : bestState.state, reward, isGameOver));
                    if(memoryReplay.size() > 20000) memoryReplay.remove(0);

                    currentState = bestState.state;
                } else break;
            }
            episode++;
            epsilon -= 0.002;
            System.out.println(episode);
            train();
        }
        initGame();
    }

    public void update() {
        learning = (episode % Util.REPLAY_NUMBER) != 0;

        if(model == null) {
            int framesPerDrop = Util.dropSpeedFunc(level);

            if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) movingDown = true;
            if (Gdx.input.isKeyPressed(Input.Keys.DOWN) && movingDown) {
                framesPerDrop = 2;
            }

            dropCounter++;
            if (dropCounter >= framesPerDrop) {
                dropCounter = 0;

                if(!dropFigure(currentFigure, field)) {
                    currentFigure = nextFigure;
                    if(!isValid(currentFigure, field)) isGameOver = true;

                    spawnedFigures[currentFigure.type.ordinal()]++;
                    nextFigure = generateNextFigure();

                    int linesCleared = clearLines(field);
                    lines += linesCleared;
                    score += Util.scoreFunc(level, linesCleared);
                }
            }

            int dx = 0;
            dxCounter++;
            boolean momentumDx = false;
            if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) dx = 1;
            if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) dx = -1;

            if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
                dx = 1;
                momentumDx = true;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
                dx = -1;
                momentumDx = true;
            }

            if(dxCounter >= 6 || momentumDx) {
                moveFigure(currentFigure, field, dx, 0);
                dxCounter = 0;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) rotate();

            if (lines >= (level + 1) * 10) level++;
        } else {
            if(learningThread == null || learningThread.getState() == Thread.State.TERMINATED) {
                if (currentFigure.y == 0) {
                    List<StateAction> generatedStates = generateStates(currentFigure, field);
                    Collections.shuffle(generatedStates);
                    StateAction bestState = generatedStates
                            .stream()
                            .max(Comparator.comparingDouble(value -> model.output(vectorData(value.state)).getDouble(0)))
                            .orElse(null);
                    currentFigure = bestState.action;
                    moveFigure(currentFigure, field, 0, -currentFigure.y);
                }
                if (!dropFigure(currentFigure, field)) {
                    clearLines(field);
                    currentFigure = nextFigure;
                    if (!isValid(currentFigure, field)) {
                        initGame();
                        learningThread = new Thread(this::makeGameReplays);
                        learningThread.start();
                        episode++;
                        epsilon -= 0.002;
                        return;
                    }
                    nextFigure = generateNextFigure();
                }
            }
        }
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 10; j++) {
                field[i][j] = 0;
            }
        }
    }

    private void train() {
        if(memoryReplay.size() < 2000) return;

        List<StateTransition> batch = new ArrayList<>();
        List<StateTransition> tempMemoryReplay = new ArrayList<>(memoryReplay);

        double[][] x = new double[Util.BATCH_SIZE][4];
        double[][] y = new double[Util.BATCH_SIZE][1];

        while (batch.size() < Util.BATCH_SIZE) {
            StateTransition selected = tempMemoryReplay.get((int) (Math.random() * tempMemoryReplay.size()));
            batch.add(selected);
            tempMemoryReplay.remove(selected);
        }
        for(int i = 0; i < batch.size(); i++) {
            StateTransition transition = batch.get(i);
            double q;
            if(transition.gameOver) {
                q = transition.reward;
            } else {
                INDArray predict = model.output(vectorData(transition.nextState));
                q = transition.reward + predict.getDouble(0) * 0.95;
            }

            x[i] = transition.prevState;
            y[i] = new double[] {q};
            //System.out.println(model.output(vectorData(transition.prevState)).getDouble(0));
            //System.out.println(model.params());
        }
        model.fit(Nd4j.createFromArray(x), Nd4j.createFromArray(y));
    }

    private INDArray vectorData(double[] array) {
        return Nd4j.createFromArray(new double[][] {array});
    }

    private boolean moveFigure(Figure figure, int[][] field, int dx, int dy) {
        figure.x += dx;
        figure.y += dy;
        for(Tile tile : figure.tiles) {
            tile.x += dx;
            tile.y += dy;
        }
        if(!isValid(figure, field)) {
            figure.x -= dx;
            figure.y -= dy;
            for(Tile tile : figure.tiles) {
                tile.x -= dx;
                tile.y -= dy;
            }
            return false;
        }
        return true;
    }

    private boolean dropFigure(Figure figure, int[][] field) {
        figure.y++;
        for(Tile tile : figure.tiles)
            tile.y++;

        if(!isValid(figure, field)) {
            figure.y--;
            for(Tile tile : figure.tiles)
                tile.y--;

            for (Tile tile : figure.tiles) {
                field[tile.y][tile.x] = tile.color.ordinal() + 1;
            }
            return false;
        }
        return true;
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
        Figure.Rotation newRotation;

        if(currentFigure.rotation.ordinal() + 1 > 3) newRotation = Figure.Rotation.UP;
        else newRotation = Figure.Rotation.values()[currentFigure.rotation.ordinal() + 1];

        Figure newFigure = new Figure(currentFigure.type, newRotation, currentFigure.x, currentFigure.y);
        if(isValid(newFigure, this.field)) currentFigure = newFigure;
    }

    private boolean isValid(Figure figure, int[][] field) {
        for(Tile tile : figure.tiles) {
            if(tile.x < 0 || tile.x > 9 || tile.y < 0 || tile.y > 21 || field[tile.y][tile.x] != 0) {
                return false;
            }
        }
        return true;
    }

    public static int clearLines(int[][] field) {
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
                for (int j = i; j >= 2; j--) {
                    System.arraycopy(field[j - 1], 0, field[j], 0, 10);
                }
            }
        }
        return rowsCount;
    }

    private List<StateAction> generateStates(Figure figure, int[][] field) {
        List<StateAction> states = new ArrayList<>();

        // Проверка на то, можно ли достигнуть данного состояния
        for (int r = 0; r < 4; r++) {
            for (int x = 3; x >= -3; x--) {
                StateAction state = createState(x, r, figure.type, field);
                if(state == null) break;
                states.add(state);
            }
            for (int x = 4; x < 12; x++) {
                StateAction state = createState(x, r, figure.type, field);
                if(state == null) break;
                states.add(state);
            }
        }
        return states;
    }

    private StateAction createState(int x, int r, Figure.Type type, int[][] field) {
        Figure experimentFigure = new Figure(type, Figure.Rotation.values()[r], x, 0);
        if(isValid(experimentFigure, field)) {
            int[][] tempField = new int[22][10];
            for (int i = 0; i < 22; i++)
                System.arraycopy(field[i], 0, tempField[i], 0, 10);

            while(dropFigure(experimentFigure, tempField)) {}
            return new StateAction(getStats(tempField), experimentFigure);
        }
        return null;
    }

    public static double[] getStats(int[][] field) {
        double[] state = new double[4];

        state[3] = clearLines(field);

        int totalHeight = 0;
        int[] heights = new int[10];
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 22; j++) {
                if(field[j][i] != 0) {
                    totalHeight += (22 - j);
                    heights[i] = 22 - j;
                    break;
                }
            }
        }

        int bumpiness = 0;
        for (int i = 1; i < heights.length; i++) {
            bumpiness += Math.abs(heights[i] - heights[i-1]);
        }

        int holes = 0;
        for (int i = 0; i < 10; i++) {
            boolean found = false;
            for (int j = 0; j < 22; j++) {
                if(field[j][i] != 0 && !found) found = true;
                if(found && field[j][i] == 0) holes++;
            }
        }

        state[0] = totalHeight;
        state[1] = bumpiness;
        state[2] = holes;

        return state;
    }

    public void render(SpriteBatch batch) {
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

    private static class StateTransition {
        double[] prevState;
        double[] nextState;
        double reward;
        boolean gameOver;

        public StateTransition(double[] prevState, double[] nextState, double reward, boolean gameOver) {
            this.prevState = prevState;
            this.nextState = nextState;
            this.reward = reward;
            this.gameOver = gameOver;
        }
    }

    private static class StateAction {
        double[] state;
        Figure action;

        public StateAction(double[] state, Figure action) {
            this.state = state;
            this.action = action;
        }
    }
}