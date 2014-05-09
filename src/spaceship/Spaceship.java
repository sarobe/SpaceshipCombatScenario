package spaceship;

import common.Constants;
import common.math.Vector2d;
import controller.ShipState;

import java.awt.*;
import java.awt.geom.AffineTransform;

/**
 * Created by Samuel Roberts, 2013
 */
public abstract class Spaceship extends SimObject {

    // hull polygon
    public Polygon hullShape;
    public Shape hitShape;

    // draw a hit flash?
    public boolean justHit = false;

    // just fired a projectile?
    public boolean justFired = false;

    // ship color (random, derived from chromosome)
    public Color shipColor;
    public Color shipHighlightColor;

    // total bullets fired
    public int bulletsFired = 0;

    // what is the maximum hull the ship can have?
    public double maxHull = 0;

    // how much fuel does the ship have?
    public double fuel = 0;

    // how many bullets does the ship have left?
    public int bullets = 0;

    public Spaceship() {
        super();
        mass = 10;
        hull = Constants.maximumHull;
        maxHull = Constants.maximumHull;
        moment = 1;
        fuel = Constants.maximumFuel;
        bullets = Constants.maximumBullets;
        hullShape = new Polygon();
        shipColor = Color.WHITE;
        shipHighlightColor = Color.WHITE;
    }

    public void harm(double harm) {
        hull -= harm;
        justHit = true;
        if(hull < 0) {
            alive = false;
        }
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
        justFired = state.justFired;
        bulletsFired = state.bulletsFired;
        bullets = state.bullets;
        fuel = state.fuel;
    }

    public void draw(Graphics2D g) {
        AffineTransform at = g.getTransform();

        // draw the ship itself
        g.translate(pos.x, pos.y);
        g.rotate(rot);

        // set team colours
        // Color shipColor = Color.WHITE;
        //if(team == Constants.TEAM_LEFT) shipColor = Color.RED;
        //if(team == Constants.TEAM_RIGHT) shipColor = Color.BLUE;

        g.setColor(shipColor);
        g.fillPolygon(hullShape);
        g.setColor(Color.GRAY);
        g.drawPolygon(hullShape);

        if(justHit) {
            g.setColor(Color.WHITE);
            g.fillPolygon(hullShape);
            justHit = false;
        }

        // draw HEALTH
        g.rotate(-rot);
        g.setColor(shipHighlightColor);
        // first off, the tank outline
        g.drawRect((int)(-radius), (int)(-radius*1.5), (int)(radius*2), 6);
        // then, the tank contents
        int hullIntegrity = (int)((radius*2) * (hull/maxHull));
        g.fillRect((int)(-radius), (int)(-radius*1.5), hullIntegrity, 6);

        // draw FUEL
        g.setColor(Color.GRAY);
        g.drawRect((int)(-radius), (int)(-radius*1.5 + 12), (int)(radius*2), 3);
        int fuelContained = (int)((radius*2) * (fuel/Constants.maximumFuel));
        g.fillRect((int)(-radius), (int)(-radius*1.5 + 12), fuelContained, 3);

        // draw BULLETS
        g.setColor(Color.WHITE);
        g.drawRect((int)(-radius), (int)(-radius*1.5 + 17), (int)(radius*2), 2);
        int bulletsHeld = (int)((radius*2) * (bullets/(float)Constants.maximumBullets));
        g.fillRect((int)(-radius), (int)(-radius*1.5 + 17), bulletsHeld, 2);


        g.setTransform(at);
    }

    abstract public void useAction(int action);
    abstract public void useRawAction(int thrust, int turn);
    abstract public Vector2d getForward();

}
