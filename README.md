# NES TETRIS AI

## Description

NES Tetris emulator with AI on Java.

![preview](core/assets/2021-08-02%2018-47-12_3.gif)

## [YouTube video](https://youtu.be/QEz9RnBrZns)

## Installation

- Java 1.8+ (download [here](https://www.java.com/ru/download/ie_manual.jsp?locale=ru))
- In DesktopLauncher class you can choose 3 game modes, just replace it in this line 
  ```java 
   new LwjglApplication(new Main(Util.DEMO_AI_MODE), config);
  ```
    - PLAY_MODE - basic emulator of NES Tetris with arrows control
    - LEARNING_MODE - you can see whole process of q-learning and get perfect AI
    - DEMO_AI_MODE - demonstration of the best tetris AI (can solve 200+ levels with average value 70 levels)
    
- To change the best ai model, you can choose your own keras model in json format
    ```java
        game = new Tetris(loadModel(Gdx.files.internal("").file().getAbsolutePath() + "/core/assets/NAME_OF_YOUR_MODEL.txt"));
    ```