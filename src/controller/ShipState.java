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
    public Vector2d pos;
    public double vx;
    public double vy;
    public double rot;
    public double vrot;
    public double tx;
    public double ty;
    public double hull;
    public boolean alive;
    public boolean justFired;
    public int bullets;
    public double fuel;
    public int bulletsFired;
    public boolean bounced;
    public int bounces;

    public Vector2d predictedPoint; // used for visualisation


    public ShipState(Spaceship ship) {
        this.ship = ship;
        px = ship.pos.x;
        py = ship.pos.y;
        pos = ship.pos.copy();
        vx = ship.vel.x;
        vy = ship.vel.y;
        rot = ship.rot;
        vrot = ship.rotvel;
        tx = 0;
        ty = 0;
        hull = ship.hull;
        alive = ship.alive;
        justFired = ship.justFired;
        bullets = ship.bullets;
        bulletsFired = ship.bulletsFired;
        fuel = ship.fuel;
        bounced = ship.bounced;
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
        pos = state.pos.copy();
        vx = state.vx;
        vy = state.vy;
        rot = state.rot;
        vrot = state.vrot;
        tx = state.tx;
        ty = state.ty;
        hull = state.hull;
        alive = state.alive;
        justFired = state.justFired;
        bullets = state.bullets;
        bulletsFired = state.bulletsFired;
        fuel = state.fuel;
        bounced = state.bounced;
    }

    public Vector2d vel() {
        return new Vector2d(vx, vy);
    }

    public void setPredictedPoint(Vector2d point) {
        this.predictedPoint = point.copy();
    }
}