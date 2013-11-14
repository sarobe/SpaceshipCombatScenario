package controller.mcts;

import common.Constants;

import java.util.Random;

public class BasicRoller implements IRoller
{
    private static Random rollerRand = new Random();

    public BasicRoller() {
    }

    public int roll(IGameState gameState)
    {
        return rollerRand.nextInt(gameState.nActions());
    }
}
