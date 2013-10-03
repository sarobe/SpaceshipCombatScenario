package controller;

import common.math.Vector2d;
import spaceship.Spaceship;

/**
 * Created by Samuel Roberts, 2012
 */
public class ShipState {

    public Spaceship ship;
    public double px;
    public double py;
    public double vx;
    public double vy;
    public double rot;
    public double vrot;
    public double tx;
    public double ty;
    public double hull;
    public boolean alive;
    public boolean justFired;
    public int bulletsFired;
    public double fuel;


    public ShipState(Spaceship ship) {
        this.ship = ship;
        px = ship.pos.x;
        py = ship.pos.y;
        vx = ship.vel.x;
        vy = ship.vel.y;
        rot = ship.rot;
        vrot = ship.rotvel;
        tx = 0;
        ty = 0;
        hull = ship.hull;
        alive = ship.alive;
        justFired = ship.justFired;
        bulletsFired = ship.bulletsFired;
        fuel = ship.fuel;
    }

    public ShipState(Spaceship ship, Vector2d target) {
        this(ship);
        tx = target.x;
        ty = target.y;
    }

    public ShipState(ShipState state) {
        ship = state.ship;
        px = state.px;
        py = state.py;
        vx = state.vx;
        vy = state.vy;
        rot = state.rot;
        vrot = state.vrot;
        tx = state.tx;
        ty = state.ty;
        hull = state.hull;
        alive = state.alive;
        justFired = state.justFired;
        bulletsFired = state.bulletsFired;
        fuel = state.fuel;
    }
}