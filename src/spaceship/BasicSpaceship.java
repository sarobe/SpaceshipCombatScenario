package spaceship;

import common.Constants;
import common.math.Vector2d;
import controller.Controller;
import controller.ShipState;
import controller.mcts.SimpleAction;
import problem.ProjectileManager;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Samuel Roberts
 * on 24/10/11
 */
public class BasicSpaceship extends Spaceship {

    // hull polygon
    public int xpShip[] = {20,-16,8,-16};
    public int ypShip[] = {0,18,0,-18};

    public SimpleAction currentAction;

    static double thrustPower = 200;
    static double steerStep = Math.PI/3;

    public BasicSpaceship() {
        super();
        mass = 10;
        hull = Constants.maximumHull;
        maxHull = Constants.maximumHull;
        moment = 1;
        radius = 30;
        fuel = Constants.maximumFuel;
        bullets = Constants.maximumBullets;
        hullShape = new Polygon();
        shipColor = Color.WHITE;
        shipHighlightColor = Color.WHITE;
        hullShape = new Polygon(xpShip, ypShip, xpShip.length);
        reset();
    }

    public Vector2d getForward() {
        Vector2d d = new Vector2d(1, 0);
        d.rotate(rot);
        d.normalise();
        return d;
    }

    public void useAction(int index) {
        currentAction = Constants.actions[index];
    }

    public void setState(ShipState state) {
        pos.x = state.px;
        pos.y = state.py;
        vel.x = state.vx;
        vel.y = state.vy;
        rot = state.rot;
        rotvel = state.vrot;
        hull = state.hull;
        alive = state.alive;
        bulletsFired = state.bulletsFired;
        bullets = state.bullets;
        fuel = state.fuel;
    }

    public void harm(double harm) {
        hull -= harm;
        justHit = true;
        if(hull < 0) {
            alive = false;
        }
    }

    public void reset() {
        pos.x = Constants.screenWidth/2;
        pos.y = Constants.screenHeight/2;
        rot = 0;
        vel.x = 0;
        vel.y = 0;
        hull = maxHull;
        bullets = Constants.maximumBullets;
        fuel = Constants.maximumFuel;
        alive = true;
    }


    public void update() {
        if(!alive) return;

        if(currentAction.thrust > 0) {
            Vector2d d = getForward();
            vel.add(d, thrustPower * Constants.dt);
        }
        rot += currentAction.turn * steerStep * Constants.dt;

        super.update();
    }

    public BasicSpaceship copyShip() {
        BasicSpaceship copy = new BasicSpaceship();
        copy.pos = pos.copy();
        copy.rot = rot;
        copy.vel = vel.copy();
        copy.rotvel = rotvel;
        return copy;
    }
}
