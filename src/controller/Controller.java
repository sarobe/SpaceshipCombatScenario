package controller;

import common.math.Vector2d;
import spaceship.Spaceship;
import spaceship.SimObject;

import java.awt.*;
import java.util.List;

/**
 * Created by Samuel Roberts, 2013
 */
public abstract class Controller {

    public List<Vector2d> trace;
    public Spaceship ship;

    public Controller(Spaceship ship) {
        this.ship = ship;
    }



    abstract public void draw(Graphics2D g);
    abstract public void think(List<SimObject> ships);
    abstract public void think();

    public Spaceship getShip() {
        return ship;
    }
}
