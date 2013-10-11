package spaceship;

import common.Constants;
import common.math.Vector2d;

import java.awt.*;

/**
 * Created by Samuel Roberts
 * on 27/10/11
 */
public abstract class SimObject {
    // mass of the object
    public double mass = 1;
    public double moment = 1;

    // position, velocity, angle and angular velocity
    public Vector2d pos;
    public Vector2d vel;
    public double rot;
    public double rotvel;

    public double radius = 1;

    public boolean alive = true;
    public double hull = 100;

    public int team = -1;

    public SimObject() {
        this(new Vector2d(), 0);
    }

    public SimObject(Vector2d pos, double rot) {
        this(pos, new Vector2d(), rot, 0);
    }

    public SimObject(Vector2d pos, Vector2d vel, double rot, double rotvel) {
        this.pos = pos;
        this.rot = rot;
        this.vel = vel;
        this.rotvel = rotvel;
    }

    public boolean isColliding(SimObject other) {
        boolean collide = false;
        if(team != other.team) {
            collide = (pos.dist(other.pos) <= radius + other.radius);
        }
        return collide;
    }

    public void setTeam(int team) {
        this.team = team;
    }

    public abstract void draw(Graphics2D g);

    public void update() {
        pos.add(vel, Constants.dt);
        rot += rotvel * Constants.dt;

        vel.mul(Constants.friction);
        rotvel *= Constants.friction;

        // game objects cannot leave the world
        if(pos.x + radius > Constants.screenWidth) {
            pos.x = Constants.screenWidth - radius;
            vel.x *= -Constants.edgeBounceLoss;
            hitSides();
        }
        if(pos.x - radius < 0) {
            pos.x = radius;
            vel.x *= -Constants.edgeBounceLoss;
            hitSides();
        }
        if(pos.y + radius > Constants.screenHeight) {
            pos.y = Constants.screenHeight - radius;
            vel.y *= -Constants.edgeBounceLoss;
            hitSides();
        }
        if(pos.y - radius < 0) {
            pos.y = radius;
            vel.y *= -Constants.edgeBounceLoss;
            hitSides();
        }
    }

    public void hitSides() {
        // for things that do something significant, do it in an override here
    }

    public void kill() {
        alive = false;
    }
}
