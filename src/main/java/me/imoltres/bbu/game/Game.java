package me.imoltres.bbu.game;

import lombok.Getter;

public class Game {

    private final GameProgression progression = new GameProgression();

    @Getter
    private boolean paused = false;

    public void startGame() {

    }

    public void pauseGame() {
        paused = true;
    }

    public void stopGame() {

    }

    public GameState getGameState() {
        return progression.getGameState();
    }

    public void setGameState(GameState state) {
        System.out.printf("Changing game state from %s to %s\n", getGameState().name(), state.name());
        progression.setGameState(state);
    }


}
