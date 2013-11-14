package controller;

import spaceship.SimObject;
import spaceship.Spaceship;

import java.awt.*;
import java.util.List;

/**
 * Created by Samuel Roberts, 2013
 */
public abstract class Controller {

    public Spaceship ship;

    public Controller(Spaceship ship) {
        this.ship = ship;
    }

    protected void binaryToActions(Spaceship target, int encodedActions) {
        int j = 1;
        int actionNum = 0;
        int totalPossibleActions = target.components.size();
        while(actionNum < totalPossibleActions) {
            if((encodedActions & j) != 0) {
                target.components.get(actionNum).active = true;
            } else {
                target.components.get(actionNum).active = false;
            }
            actionNum++;
            j *= 2;
        }
    }

    abstract public void draw(Graphics2D g);
    abstract public void think(List<SimObject> ships);
    abstract public void think();

    public Spaceship getShip() {
        return ship;
    }
}
