package com.mygdx.game.ai;

public class NeuralNetwork {

    //NN scheme [4, 32, 32, 1], activation: relu, relu, linear, loss: mean squared error

    public double[][] weights1 = new double[4][32];
    public double[][] weights2 = new double[32][32];
    public double[][] weights3 = new double[32][1];

    public double[] biases1 = new double[32];
    public double[] biases2 = new double[32];
    public double[] biases3 = new double[1];

    private final double[] weightedSum1 = new double[32];
    private final double[] weightedSum2 = new double[32];
    private final double[] weightedSum3 = new double[1];

    private final double[] activated1 = new double[32];
    private final double[] activated2 = new double[32];

    double learningRate = 0.01;

    public NeuralNetwork() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 32; j++) {
                if(i == 0) biases1[j] = Math.random() * 2.0 - 1.0;
                weights1[i][j] = Math.random() * 2.0 - 1.0;
            }
        }
        for (int i = 0; i < 32; i++) {
            for (int j = 0; j < 32; j++) {
                if(i == 0) biases2[j] = Math.random() * 2.0 - 1.0;
                weights2[i][j] = Math.random() * 2.0 - 1.0;
            }
        }
        for (int i = 0; i < 32; i++) {
            for (int j = 0; j < 1; j++) {
                if(i == 0) biases3[j] = Math.random() * 2.0 - 1.0;
                weights3[i][j] = Math.random() * 2.0 - 1.0;
            }
        }
    }

    public double[] predict(double[] inputs) {
        double[] y = new double[1];

        //Прогоняем через 1 слой
        for (int l = 0; l < 32; l++) {
            for (int m = 0; m < 4; m++) {
                weightedSum1[l] += weights1[m][l] * inputs[m];
            }
            weightedSum1[l] += biases1[l];
        }
        //Активируем второй слой
        for (int l = 0; l < 32; l++) {
            activated1[l] = reluActivation(weightedSum1[l]);
        }
        //Прогоняем через 2 слой
        for (int l = 0; l < 32; l++) {
            for (int m = 0; m < 32; m++) {
                weightedSum2[l] += weights2[m][l] * activated1[m];
            }
            weightedSum2[l] += biases2[l];
        }
        //Активируем третий слой
        for (int l = 0; l < 32; l++) {
            activated2[l] = reluActivation(weightedSum2[l]);
        }
        for (int l = 0; l < 1; l++) {
            for (int m = 0; m < 32; m++) {
                weightedSum3[l] += weights3[m][l] * activated2[m];
            }
            weightedSum3[l] += biases3[l];
        }

        //Активируем выходной слой
        for (int l = 0; l < 1; l++) {
            y[l] = linearActivation(weightedSum3[l]);
        }

        for (int k = 0; k < 32; k++) {
            weightedSum1[k] = 0;
        }
        for (int k = 0; k < 32; k++) {
            weightedSum2[k] = 0;
        }
        for (int k = 0; k < 1; k++) {
            weightedSum3[k] = 0;
        }
        return y;
    }

    public void backPropagation(double[] y, double[] inputs, double[] outputs) {

        //Обратное распространение ошибки
        double[] delta = new double[1];
        double[] delta1 = new double[32];
        double[] delta2 = new double[32];

        for (int l = 0; l < 1; l++) {
            delta[l] = 2 * (y[l] - outputs[l]);
        }
        //Корректировка весов третьего слоя
        for (int l = 0; l < 32; l++) {
            for (int m = 0; m < 1; m++) {
                if (l == 0) biases3[m] -= learningRate * delta[m];
                weights3[l][m] -= learningRate * delta[m] * activated2[l];
            }
        }

        //Градиенты для третьего слоя
        for (int l = 0; l < 32; l++) {
            for (int m = 0; m < 1; m++) {
                delta1[l] += delta[m] * weights3[l][m];
            }
        }

        //Корректировка весов второго слоя
        for (int l = 0; l < 32; l++) {
            for (int m = 0; m < 32; m++) {
                if (l == 0) biases2[m] -= learningRate * delta1[m];
                weights2[l][m] -= learningRate * delta1[m] * activated1[l] * reluDerivative(weightedSum2[m]);
            }
        }

        //Градиенты для второго слоя
        for (int l = 0; l < 32; l++) {
            for (int m = 0; m < 32; m++) {
                delta2[l] += delta1[m] * weights2[l][m];
            }
        }

        //Корректировка весов первого слоя
        for (int l = 0; l < 4; l++) {
            for (int m = 0; m < 32; m++) {
                if (l == 0) biases1[m] -= learningRate * delta2[m];
                weights1[l][m] -= learningRate * delta2[m] * inputs[l] * reluDerivative(weightedSum1[m]);
            }
        }
    }

    public static double reluActivation(double x) { return x <= 0 ? 0 : x; }
    public static double reluDerivative(double x) { return x <= 0 ? 0 : 1; }
    public static double linearActivation(double x) { return x; }
}
