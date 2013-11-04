package game;

/**
 * Created by Samuel Roberts, 2013
 */
public class PlayerAction {
    public int forward = 0;
    public int turn = 0;
    public boolean shoot = false;

    public PlayerAction(int forward, int turn, boolean shoot) {
        this.forward = forward;
        this.turn = turn;
        this.shoot = shoot;
    }
}
